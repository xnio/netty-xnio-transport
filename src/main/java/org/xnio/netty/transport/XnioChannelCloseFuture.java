/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xnio.netty.transport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.xnio.ChannelListener;
import org.xnio.conduits.ConduitStreamSourceChannel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * ChannelFuture to be provided when shutting down channels, performs as a close listener on the Xnio side.
 * 
 * 
 * @author Flavia Rainone
 *
 */
public class XnioChannelCloseFuture implements ChannelFuture, ChannelListener<ConduitStreamSourceChannel>{

		private IOException error;
		private final AtomicBoolean closed;
		private final AbstractXnioSocketChannel channel;
		private final CountDownLatch countDownLatch = new CountDownLatch(1);
		private List<GenericFutureListener<? extends Future<? super Void>>> listeners;
		
		public XnioChannelCloseFuture(AbstractXnioSocketChannel channel/*, IOException error*/) {
			//this.error = error;
			this.closed = new AtomicBoolean(false);
			this.channel = channel;
		}

		public void setError(IOException error) {
			this.error = error;
		}
		
		@Override
		public boolean isSuccess() {
			return closed.get() && error != null;
		}

		@Override
		public boolean isCancellable() {
			return false;
		}

		@Override
		public Throwable cause() {
			return error;
		}

		@Override
		public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
			countDownLatch.await(timeout, unit);
			return closed.get();
		}

		@Override
		public boolean await(long timeoutMillis) throws InterruptedException {
			countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
			return closed.get();
		}

		@Override
		public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
			try {
				countDownLatch.await(timeout, unit);
			} catch (InterruptedException e) {
				// discard interrupted
			}
			return closed.get();
		}

		@Override
		public boolean awaitUninterruptibly(long timeoutMillis) {
			try {
				countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// discard interrupted
			}
			return closed.get();
		}

		@Override
		public Void getNow() {
			return null;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return false;
		}

		@Override
		public boolean isCancelled() {
			return false;
		}

		@Override
		public boolean isDone() {
			return closed.get();
		}

		@Override
		public Void get() throws InterruptedException, ExecutionException {
			return null;
		}

		@Override
		public Void get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
			countDownLatch.await(timeout, unit);
			return null;
		}

		@Override
		public Channel channel() {
			return channel;
		}

		@Override
		public ChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
			final boolean notify;
			synchronized (this) {
				if (closed.get()) {
					notify = true;
				} else {
					notify = false;
					if (listeners == null) {
						listeners = new ArrayList<GenericFutureListener<? extends Future<? super Void>>>();
					}
					listeners.add(listener);
				}
			}
			if (notify) 
				notifyListener(listener);
			return this;
		}

		@SuppressWarnings("unchecked")
		private <T extends Future<? super Void>> void notifyListener(GenericFutureListener<T> listener) {
			try {
				listener.operationComplete((T) this);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public ChannelFuture addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
			for (GenericFutureListener<? extends Future<? super Void>> listener: listeners)
				addListener(listener);
			return this;
		}

		@Override
		public ChannelFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
			synchronized (this) {
				if (listeners != null) {
					listeners.remove(listener);
				}
			}
			return this;
		}

		@Override
		public ChannelFuture removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
			synchronized (this) {
				if (this.listeners != null) {
					for (GenericFutureListener<? extends Future<? super Void>> listener: listeners)
						this.listeners.remove(listener);
				}
			}
			return this;
		}

		@Override
		public ChannelFuture sync() throws InterruptedException {
			closed.wait();
			if (error != null)
				throw new RuntimeException(error);
			return this;
		}

		@Override
		public ChannelFuture syncUninterruptibly() {
			try {
				closed.wait();
			} catch (InterruptedException e) {
				// ignore
			}
			if (error != null)
				throw new RuntimeException(error);
			return this;
		}

		@Override
		public ChannelFuture await() throws InterruptedException {
			countDownLatch.await();
			return this;
		}

		@Override
		public ChannelFuture awaitUninterruptibly() {
			try {
				countDownLatch.await();
			} catch (InterruptedException e) {
				// ignore
			}
			return this;
		}

		@Override
		public boolean isVoid() {
			return true;
		}

		@Override
		public void handleEvent(ConduitStreamSourceChannel channel) {
			closed.compareAndSet(false, true);
			countDownLatch.countDown();
		}

}
