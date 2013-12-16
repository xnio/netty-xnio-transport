/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */
package org.xnio.netty.transport;


import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.AbstractEventExecutor;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ScheduledFuture;
import org.xnio.XnioExecutor;
import org.xnio.XnioIoThread;

import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * {@link EventLoop} implementation which uses a {@link XnioIoThread}.
 *
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
final class XnioEventLoop extends AbstractEventExecutor implements EventLoop, IoThreadPowered{
    private final XnioIoThread executor;
    private final EventLoopGroup parent;

    XnioEventLoop(EventLoopGroup parent, XnioIoThread executor) {
        this.parent = parent ;
        this.executor = executor;
    }

    XnioEventLoop(XnioIoThread executor) {
        this.parent = this;
        this.executor = executor;
    }

    @Override
    public XnioIoThread ioThread() {
        return executor;
    }

    @Override
    public void shutdown() {
        // Not supported, just ignore
    }

    @Override
    public EventLoopGroup parent() {
        return parent;
    }

    @Override
    public boolean inEventLoop(Thread thread) {
        return thread == executor;
    }

    @Override
    public ChannelFuture register(Channel channel) {
        return register(channel, channel.newPromise());
    }

    @Override
    public ChannelFuture register(final Channel channel, final ChannelPromise promise) {
        if (channel == null) {
            throw new NullPointerException("channel");
        }
        if (promise == null) {
            throw new NullPointerException("promise");
        }
        channel.unsafe().register(this, promise);
        return promise;
    }

    @Override
    public boolean isShuttingDown() {
        return executor.getWorker().isShutdown();
    }

    @Override
    public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
        return newFailedFuture(new UnsupportedOperationException());
    }

    @Override
    public Future<?> terminationFuture() {
        return newFailedFuture(new UnsupportedOperationException());
    }

    @Override
    public boolean isShutdown() {
        return executor.getWorker().isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executor.getWorker().isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executor.getWorker().awaitTermination(timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(command);
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return schedule(Executors.callable(command), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        ScheduledFutureWrapper<V> wrapper = new ScheduledFutureWrapper<V>(callable, delay, unit);
        wrapper.key = executor.executeAfter(wrapper, delay, unit);
        return wrapper;
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        FixedScheduledFuture wrapper = new FixedScheduledFuture(command, initialDelay, delay, unit);
        wrapper.key = executor.executeAfter(wrapper, delay, unit);
        return wrapper;
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        FixedRateScheduledFuture wrapper = new FixedRateScheduledFuture(command, initialDelay, period, unit);
        wrapper.key = executor.executeAfter(wrapper, initialDelay, unit);
        return wrapper;
    }

    @Override
    public XnioEventLoop next() {
        return this;
    }

    private final class FixedRateScheduledFuture extends ScheduledFutureWrapper<Object> {
        private final long period;
        private int count = 1;
        private final long initialDelay;
        private FixedRateScheduledFuture(Runnable task, long delay, long period, TimeUnit unit) {
            super(Executors.callable(task), delay, unit);
            this.initialDelay = delay;
            this.period = period;
        }

        @Override
        public void run() {
            try {
                task.call();
                start = System.nanoTime();
                delay = initialDelay + period * (++count);
                key = executor.executeAfter(this,delay, TimeUnit.NANOSECONDS);
            } catch (Throwable cause) {
                tryFailure(cause);
            }
        }

    }

    private final class FixedScheduledFuture extends ScheduledFutureWrapper<Object> {
        private long furtherDelay;
        private FixedScheduledFuture(Runnable task, long initialDelay, long delay, TimeUnit unit) {
            super(Executors.callable(task), initialDelay, unit);
            this.furtherDelay = unit.toNanos(delay);
        }

        @Override
        public void run() {
            try {
                task.call();
                start = System.nanoTime();
                delay = furtherDelay;
                key = executor.executeAfter(this, furtherDelay, TimeUnit.NANOSECONDS);
            } catch (Throwable cause) {
                tryFailure(cause);
            }
        }

    }

    private class ScheduledFutureWrapper<V> extends DefaultPromise<V> implements ScheduledFuture<V>, Runnable {
        protected volatile XnioExecutor.Key key;
        protected final Callable<V> task;
        protected volatile long delay;
        protected volatile long start;

        ScheduledFutureWrapper(Callable<V> task, long delay, TimeUnit unit) {
            this.task = task;
            this.start = System.nanoTime();
            this.delay = unit.toNanos(delay);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long remaining = (start + delay) - System.nanoTime();
            if (remaining < 1 || unit == TimeUnit.NANOSECONDS) {
                return remaining;
            }
            return unit.convert(remaining, TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            long d = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
            if (d < 0) {
                return -1;
            } else if (d > 0) {
                return 1;
            } else {
                return 0;
            }
        }

        @Override
        public void run() {
            try {
                trySuccess(task.call());
            } catch (Throwable t) {
                tryFailure(t);
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return setUncancellable() && key.remove();
        }
    }

}
