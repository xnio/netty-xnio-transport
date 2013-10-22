package org.jboss.netty.xnio;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.AbstractEventExecutorGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ImmediateEventExecutor;
import org.xnio.XnioWorker;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class XnioEventLoopGroup extends AbstractEventExecutorGroup implements EventLoopGroup {

    private final XnioWorker worker;

    public XnioEventLoopGroup(XnioWorker worker) {
        this.worker = worker;
    }

    @Override
    public void shutdown() {
        worker.shutdown();
    }

    @Override
    public EventLoop next() {
        return new XnioEventLoop(worker.getIoThread());
    }

    @Override
    public ChannelFuture register(Channel channel) {
        return next().register(channel);
    }

    @Override
    public ChannelFuture register(Channel channel, ChannelPromise promise) {
        return next().register(channel, promise);
    }

    @Override
    public boolean isShuttingDown() {
        return worker.isTerminated();
    }

    @Override
    public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
        shutdown();
        if (isShutdown()) {
            return ImmediateEventExecutor.INSTANCE.newSucceededFuture( null);
        } else {
            return ImmediateEventExecutor.INSTANCE.newFailedFuture(new TimeoutException());
        }
    }

    @Override
    public Future<?> terminationFuture() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<EventExecutor> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isShutdown() {
        return worker.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return worker.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return  worker.awaitTermination(timeout, unit);
    }
}
