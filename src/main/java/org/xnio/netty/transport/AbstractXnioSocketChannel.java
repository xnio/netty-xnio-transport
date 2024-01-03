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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.xnio.ChannelListener;
import org.xnio.Option;
import org.xnio.StreamConnection;
import org.xnio.conduits.ConduitStreamSinkChannel;
import org.xnio.conduits.ConduitStreamSourceChannel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.FileRegion;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.internal.StringUtil;


/**
 * {@link SocketChannel} base class for our XNIO transport
 *
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 * @author Flavia Rainone
 */
abstract class AbstractXnioSocketChannel  extends AbstractChannel implements SocketChannel {

    private static final ChannelMetadata META_DATA = new ChannelMetadata(false);
    private final XnioSocketChannelConfig config = new XnioSocketChannelConfig(this);

    private Runnable flushTask;
    private ChannelListener<ConduitStreamSinkChannel> writeListener;
    private volatile boolean closed;

    AbstractXnioSocketChannel(AbstractXnioServerSocketChannel parent) {
        super(parent);
    }

    @Override
    public ServerSocketChannel parent() {
        return (ServerSocketChannel) super.parent();
    }

    public InetSocketAddress remoteAddress() {
        return (InetSocketAddress) super.remoteAddress();
    }

    @Override
    public InetSocketAddress localAddress() {
        return (InetSocketAddress) super.localAddress();
    }

    @Override
    protected abstract AbstractXnioUnsafe newUnsafe();

    @Override
    protected boolean isCompatible(EventLoop loop) {
        if (!(loop instanceof XnioEventLoop)) {
            return false;
        }
        ServerSocketChannel parent = parent();
        if (parent != null) {
            // if this channel has a parent we need to ensure that both EventLoopGroups are the same for XNIO
            // to be sure it uses a Thread from the correct Worker.
            if (parent.eventLoop().parent() != loop.parent()) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void doDisconnect() throws Exception {
        doClose();
    }



    private void incompleteWrite(boolean setOpWrite) {
        // Did not write completely.
        if (setOpWrite) {
            setOpWrite();
        } else {
            // Schedule flush again later so other tasks can be picked up in the meantime
            Runnable flushTask = this.flushTask;
            if (flushTask == null) {
                flushTask = this.flushTask = new Runnable() {
                    @Override
                    public void run() {
                        flush();
                    }
                };
            }
            eventLoop().execute(flushTask);
        }
    }

    private void setOpWrite() {
        ConduitStreamSinkChannel sink = connection().getSinkChannel();
        if (!sink.isWriteResumed()) {
            ChannelListener<ConduitStreamSinkChannel> writeListener = this.writeListener;
            if (writeListener == null) {
                writeListener = this.writeListener = new WriteListener();
            }
            sink.getWriteSetter().set(writeListener);
            sink.resumeWrites();
        }
    }


    @Override
    protected void doWrite(ChannelOutboundBuffer in) throws Exception {
        int writeSpinCount = -1;

        GatheringByteChannel sink = connection().getSinkChannel();
        for (;;) {
            // Do gathering write for a non-single buffer case.
            final int msgCount = in.size();
            if (msgCount > 0) {
                // Ensure the pending writes are made of ByteBufs only.
                ByteBuffer[] nioBuffers = in.nioBuffers();
                if (nioBuffers != null) {

                    int nioBufferCnt = in.nioBufferCount();
                    long expectedWrittenBytes = in.nioBufferSize();
                    if(nioBufferCnt > 0) {

                        long writtenBytes = 0;
                        boolean done = false;
                        boolean setOpWrite = false;
                        for (int i = config().getWriteSpinCount() - 1; i >= 0; i--) {
                            final long localWrittenBytes = sink.write(nioBuffers, 0, nioBufferCnt);
                            if (localWrittenBytes == 0) {
                                setOpWrite = true;
                                break;
                            }
                            expectedWrittenBytes -= localWrittenBytes;
                            writtenBytes += localWrittenBytes;
                            if (expectedWrittenBytes == 0) {
                                done = true;
                                break;
                            }
                        }

                        if (done) {
                            // Release all buffers
                            for (int i = msgCount; i > 0; i--) {
                                in.remove();
                            }

                            // Finish the write loop if no new messages were flushed by in.remove().
                            if (in.isEmpty()) {
                                connection().getSinkChannel().suspendWrites();
                                break;
                            }
                        } else {
                            // Did not write all buffers completely.
                            // Release the fully written buffers and update the indexes of the partially written buffer.

                            for (int i = msgCount; i > 0; i--) {
                                final ByteBuf buf = (ByteBuf) in.current();
                                final int readerIndex = buf.readerIndex();
                                final int readableBytes = buf.writerIndex() - readerIndex;

                                if (readableBytes < writtenBytes) {
                                    in.progress(readableBytes);
                                    in.remove();
                                    writtenBytes -= readableBytes;
                                } else if (readableBytes > writtenBytes) {
                                    buf.readerIndex(readerIndex + (int) writtenBytes);
                                    in.progress(writtenBytes);
                                    break;
                                } else { // readableBytes == writtenBytes
                                    in.progress(readableBytes);
                                    in.remove();
                                    break;
                                }
                            }

                            incompleteWrite(setOpWrite);
                        }
                    }
                }
            }
            Object msg = in.current();
            if (msg == null) {
                // Wrote all messages.
                connection().getSinkChannel().suspendWrites();
                break;
            }

            if (msg instanceof ByteBuf) {
                ByteBuf buf = (ByteBuf) msg;
                int readableBytes = buf.readableBytes();
                if (readableBytes == 0) {
                    in.remove();
                    continue;
                }

                // code corresponding to previous if (!buf.isDirect()) { ... }
                // has been removed in corresponding Netty's AbstractNioByteChannel
                // in https://github.com/netty/netty/pull/2242

                boolean setOpWrite = false;
                boolean done = false;
                long flushedAmount = 0;
                if (writeSpinCount == -1) {
                    writeSpinCount = config().getWriteSpinCount();
                }
                for (int i = writeSpinCount - 1; i >= 0; i --) {
                    int localFlushedAmount = buf.readBytes(sink, buf.readableBytes());
                    if (localFlushedAmount == 0) {
                        setOpWrite = true;
                        break;
                    }

                    flushedAmount += localFlushedAmount;
                    if (!buf.isReadable()) {
                        done = true;
                        break;
                    }
                }

                in.progress(flushedAmount);

                if (done) {
                    in.remove();
                } else {
                    incompleteWrite(setOpWrite);
                    break;
                }
            } else if (msg instanceof FileRegion) {
                FileRegion region = (FileRegion) msg;
                boolean setOpWrite = false;
                boolean done = false;
                long flushedAmount = 0;
                if (writeSpinCount == -1) {
                    writeSpinCount = config().getWriteSpinCount();
                }
                for (int i = writeSpinCount - 1; i >= 0; i --) {
                    long localFlushedAmount = region.transferTo(sink, region.transferred());
                    if (localFlushedAmount == 0) {
                        setOpWrite = true;
                        break;
                    }

                    flushedAmount += localFlushedAmount;
                    if (region.transferred() >= region.count()) {
                        done = true;
                        break;
                    }
                }

                in.progress(flushedAmount);

                if (done) {
                    in.remove();
                } else {
                    incompleteWrite(setOpWrite);
                    break;
                }
            } else {
                throw new UnsupportedOperationException("unsupported message type: " + StringUtil.simpleClassName(msg));
            }
        }
    }

    @Override
    public SocketChannelConfig config() {
        return config;
    }

    @Override
    public ChannelFuture shutdownOutput() {
        return newFailedFuture(new UnsupportedOperationException());
    }

    @Override
    public ChannelFuture shutdownOutput(ChannelPromise future) {
        return newFailedFuture(new UnsupportedOperationException());
    }

    @Override
    public boolean isOpen() {
        StreamConnection conn = connection();
        return (conn == null || conn.isOpen()) && !closed;
    }

    @Override
    public boolean isActive() {
        StreamConnection conn = connection();
        return conn != null && conn.isOpen() && !closed;
    }

    @Override
    public ChannelMetadata metadata() {
        return META_DATA;
    }

    @Override
    protected SocketAddress localAddress0() {
        StreamConnection conn = connection();
        if (conn == null) {
            return null;
        }
        return conn.getLocalAddress();
    }

    @Override
    protected SocketAddress remoteAddress0() {
        StreamConnection conn = connection();
        if (conn == null) {
            return null;
        }
        return conn.getPeerAddress();
    }

    protected abstract class AbstractXnioUnsafe extends AbstractUnsafe {
        private boolean readPending = false;

        public void beginRead0() {
            readPending = true;
        }

        @Override
        protected void flush0() {
            // Flush immediately only when there's no pending flush.
            // If there's a pending flush operation, event loop will call forceFlush() later,
            // and thus there's no need to call it now.
            if (connection().getSinkChannel().isWriteResumed()) {
                return;
            }
            super.flush0();
        }

        public void forceFlush() {
            super.flush0();
        }
    }

    final class ReadListener implements ChannelListener<ConduitStreamSourceChannel> {
        private RecvByteBufAllocator.Handle allocHandle;

        private void removeReadOp(ConduitStreamSourceChannel channel) {
            if (channel.isReadResumed()) {
                channel.suspendReads();
            }
        }

        private void closeOnRead() {
            StreamConnection connection = connection();
            suspend(connection);
            if (isOpen()) {
                unsafe().close(unsafe().voidPromise());
            }
        }

        private void handleReadException(ChannelPipeline pipeline, ByteBuf byteBuf, Throwable cause, boolean close) {
            if (byteBuf != null) {
                if (byteBuf.isReadable()) {
                    pipeline.fireChannelRead(byteBuf);
                } else {
                    try {
                        byteBuf.release();
                    } catch (IllegalReferenceCountException ignore) {
                        // ignore as it may be released already
                    }
                }
            }
            pipeline.fireChannelReadComplete();
            pipeline.fireExceptionCaught(cause);
            if (close || cause instanceof IOException) {
                closeOnRead();
            }
        }

        @Override
        public void handleEvent(ConduitStreamSourceChannel channel) {
            final ChannelConfig config = config();
            final ChannelPipeline pipeline = pipeline();
            final ByteBufAllocator allocator = config.getAllocator();
            final int maxMessagesPerRead = config.getMaxMessagesPerRead();
            RecvByteBufAllocator.Handle allocHandle = this.allocHandle;
            if (allocHandle == null) {
                this.allocHandle = allocHandle = config.getRecvByteBufAllocator().newHandle();
                this.allocHandle.reset(config);
            }

            ByteBuf byteBuf = null;
            int messages = 0;
            boolean close = false;
            try {
                int byteBufCapacity = allocHandle.guess();
                int totalReadAmount = 0;
                do {
                    byteBuf = allocator.ioBuffer(byteBufCapacity);
                    int writable = byteBuf.writableBytes();
                    int localReadAmount = byteBuf.writeBytes(channel, byteBuf.writableBytes());
                    if (localReadAmount <= 0) {
                        // nothing was read release the buffer
                        byteBuf.release();
                        close = localReadAmount < 0;
                        break;
                    }
                    ((AbstractXnioUnsafe) unsafe()).readPending = false;
                    pipeline.fireChannelRead(byteBuf);
                    byteBuf = null;

                    if (totalReadAmount >= Integer.MAX_VALUE - localReadAmount) {
                        // Avoid overflow.
                        totalReadAmount = Integer.MAX_VALUE;
                        break;
                    }

                    totalReadAmount += localReadAmount;
                    
                    // stop reading
                    if (!config.isAutoRead()) {
                        break;
                    }

                    if (localReadAmount < writable) {
                        // Read less than what the buffer can hold,
                        // which might mean we drained the recv buffer completely.
                        break;
                    }
                } while (++ messages < maxMessagesPerRead && allocHandle.continueReading());

                allocHandle.incMessagesRead(messages);
                allocHandle.lastBytesRead(totalReadAmount);
                allocHandle.readComplete();

                pipeline.fireChannelReadComplete();

                if (close) {
                    closeOnRead();
                    close = false;
                }
            } catch (Throwable t) {
                handleReadException(pipeline, byteBuf, t, close);
            } finally {
                // Check if there is a readPending which was not processed yet.
                // This could be for two reasons:
                // * The user called Channel.read() or ChannelHandlerContext.read() in channelRead(...) method
                // * The user called Channel.read() or ChannelHandlerContext.read() in channelReadComplete(...) method
                //
                // See https://github.com/netty/netty/issues/2254
                if (!config.isAutoRead() && !((AbstractXnioUnsafe) unsafe()).readPending) {
                    removeReadOp(channel);
                }
            }
        }
    }

    private class WriteListener implements ChannelListener<ConduitStreamSinkChannel> {
        @Override
        public void handleEvent(ConduitStreamSinkChannel channel) {
            ((AbstractXnioUnsafe)unsafe()).forceFlush();
        }
    }

    @Override
    public boolean isInputShutdown() {
        StreamConnection conn = connection();
        return conn == null || conn.isReadShutdown();
    }

    @Override
    public boolean isOutputShutdown() {
        StreamConnection conn = connection();
        return conn == null || conn.isWriteShutdown();
    }

    @Override
    protected void doBeginRead() throws Exception {
        StreamConnection conn = connection();
        if (conn == null) {
            return;
        }
        ((AbstractXnioUnsafe)unsafe()).beginRead0();
        ConduitStreamSourceChannel source = conn.getSourceChannel();
        if (!source.isReadResumed()) {
            source.resumeReads();
        }
    }

    @Override
    protected void doClose() throws Exception {
        closed = true;
        StreamConnection conn = connection();
        if (conn != null) {
            suspend(conn);
            conn.close();
        }
    }

    <T> T getOption(Option<T> option) {
        try {
            return getOption0(option);
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }

    <T> void setOption(Option<T> option, T value) {
        try {
            setOption0(option, value);
        } catch (IOException e) {
            throw new ChannelException(e);
        }
    }
    private static void suspend(StreamConnection connection) {
        if (connection == null) {
            return;
        }
        connection.getSourceChannel().suspendReads();
        connection.getSinkChannel().suspendWrites();
    }

    /**
     * Set the given {@link Option} to the given value.
     */
    protected abstract <T> void setOption0(Option<T> option, T value) throws IOException;


    /**
     * Return the value for the given {@link Option}.
     */
    protected abstract <T> T getOption0(Option<T> option) throws IOException;

    /**
     * Returns the underlying {@link StreamConnection} or {@code null} if not created yet.
     */
    protected abstract StreamConnection connection();
    
    @Override
    public boolean isShutdown() {
    	return isInputShutdown() && isOutputShutdown();
    }
    
    @Override
    public ChannelFuture shutdown() {
    	final ChannelFuture inputFuture = shutdownInput();
    	final ChannelFuture outputFuture = shutdownOutput();
    	return getShutdownChannelFuture(inputFuture, outputFuture);
    }	

    @Override
    public ChannelFuture shutdown(ChannelPromise channelPromise) {
    	final ChannelFuture inputFuture = shutdownInput(channelPromise);
    	final ChannelFuture outputFuture = shutdownOutput(channelPromise);
    	return getShutdownChannelFuture(inputFuture, outputFuture);
    }
    
    private ChannelFuture getShutdownChannelFuture(final ChannelFuture inputFuture, final ChannelFuture outputFuture) {
    	return new ChannelFuture() {

			@Override
			public boolean isSuccess() {
				return inputFuture.isSuccess() && outputFuture.isSuccess();
			}

			@Override
			public boolean isCancellable() {
				return inputFuture.isCancellable() && outputFuture.isCancellable();
			}

			@Override
			public Throwable cause() {
				return inputFuture.cause() != null? inputFuture.cause(): outputFuture.cause();
			}

			@Override
			public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
				long currentTimeMillis = System.currentTimeMillis();
				if (inputFuture.await(timeout, unit)) { 
					currentTimeMillis -= System.currentTimeMillis();
					if (currentTimeMillis <= 0) 
						return outputFuture.isDone(); // not enough time to await outputFuture
					return outputFuture.await(currentTimeMillis, TimeUnit.MILLISECONDS);
				}
				return false;
			}

			@Override
			public boolean await(long timeoutMillis) throws InterruptedException {
				return await(timeoutMillis, TimeUnit.MILLISECONDS);
			}

			@Override
			public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
				long currentTimeMillis = System.currentTimeMillis();
				if (inputFuture.awaitUninterruptibly(timeout, unit)) { 
					currentTimeMillis -= System.currentTimeMillis();
					if (currentTimeMillis <= 0)
						return outputFuture.isDone(); // not enough time to await outputFuture
					return outputFuture.awaitUninterruptibly(currentTimeMillis, TimeUnit.MILLISECONDS);
				}
				return false;
			}

			@Override
			public boolean awaitUninterruptibly(long timeoutMillis) {
				return awaitUninterruptibly(timeoutMillis, TimeUnit.MILLISECONDS);
			}

			@Override
			public Void getNow() {
				return inputFuture.isDone() && outputFuture.isDone()? inputFuture.getNow(): null;
			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				boolean cancelInput = inputFuture.cancel(mayInterruptIfRunning);
				boolean cancelOutput = outputFuture.cancel(mayInterruptIfRunning);
				return cancelInput && cancelOutput;
			}

			@Override
			public boolean isCancelled() {
				return inputFuture.isCancelled() && outputFuture.isCancelled();
			}

			@Override
			public boolean isDone() {
				return inputFuture.isDone() && outputFuture.isDone();
			}

			@Override
			public Void get() throws InterruptedException, ExecutionException {
				Void inputResult = inputFuture.get();
				outputFuture.get();
				return inputResult;
			}

			@Override
			public Void get(long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException, TimeoutException {
				Void inputResult = inputFuture.get(timeout, unit);
				outputFuture.get(timeout, unit);
				return inputResult;
			}

			@Override
			public Channel channel() {
				return AbstractXnioSocketChannel.this;
			}

			@Override
			public ChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
				inputFuture.addListener(listener);
				outputFuture.addListener(listener);
				return this;
			}

			@Override
			public ChannelFuture addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
				inputFuture.addListeners(listeners);
				outputFuture.addListeners(listeners);
				return this;
			}

			@Override
			public ChannelFuture removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
				inputFuture.removeListener(listener);
				outputFuture.removeListener(listener);
				return this;
			}

			@Override
			public ChannelFuture removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
				inputFuture.removeListeners(listeners);
				outputFuture.removeListeners(listeners);
				return this;
			}

			@Override
			public ChannelFuture sync() throws InterruptedException {
				inputFuture.sync();
				outputFuture.sync();
				return this;
			}

			@Override
			public ChannelFuture syncUninterruptibly() {
				inputFuture.syncUninterruptibly();
				outputFuture.syncUninterruptibly();
				return this;
			}

			@Override
			public ChannelFuture await() throws InterruptedException {
				inputFuture.await();
				outputFuture.await();
				return this;
			}

			@Override
			public ChannelFuture awaitUninterruptibly() {
				inputFuture.awaitUninterruptibly();
				outputFuture.awaitUninterruptibly();
				return this;
			}

			@Override
			public boolean isVoid() {
				return inputFuture.isVoid() || outputFuture.isVoid();
			}
    	};
    }
    
}
