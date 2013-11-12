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
package org.jboss.netty.xnio.transport;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.socket.ServerSocketChannelConfig;
import org.xnio.Options;


/**
 * {@link ServerSocketChannelConfig} implementation for our XNIO transport
 *
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
final class XnioServerSocketChannelConfig extends DefaultChannelConfig implements ServerSocketChannelConfig {
    private final AbstractXnioServerSocketChannel channel;
    public XnioServerSocketChannelConfig(AbstractXnioServerSocketChannel channel) {
        super(channel);
        this.channel = channel;
    }

    @Override
    public int getBacklog() {
        return channel.getOption(Options.BACKLOG);
    }

    @Override
    public ServerSocketChannelConfig setBacklog(int backlog) {
        channel.setOption(Options.BACKLOG, backlog);
        return this;
    }

    @Override
    public boolean isReuseAddress() {
        return channel.getOption(Options.REUSE_ADDRESSES);
    }

    @Override
    public ServerSocketChannelConfig setReuseAddress(boolean reuseAddress) {
        channel.setOption(Options.REUSE_ADDRESSES, reuseAddress);
        return this;
    }

    @Override
    public int getReceiveBufferSize() {
        return channel.getOption(Options.RECEIVE_BUFFER);
    }

    @Override
    public ServerSocketChannelConfig setReceiveBufferSize(int receiveBufferSize) {
        channel.setOption(Options.RECEIVE_BUFFER, receiveBufferSize);
        return this;
    }

    @Override
    public ServerSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServerSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        super.setConnectTimeoutMillis(connectTimeoutMillis);
        return this;
    }

    @Override
    public ServerSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
        super.setMaxMessagesPerRead(maxMessagesPerRead);
        return this;
    }

    @Override
    public ServerSocketChannelConfig setWriteSpinCount(int writeSpinCount) {
        super.setWriteSpinCount(writeSpinCount);
        return this;
    }

    @Override
    public ServerSocketChannelConfig setAllocator(ByteBufAllocator allocator) {
        super.setAllocator(allocator);
        return this;
    }

    @Override
    public ServerSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
        super.setRecvByteBufAllocator(allocator);
        return this;
    }

    @Override
    public ServerSocketChannelConfig setAutoRead(boolean autoRead) {
        super.setAutoRead(autoRead);
        return this;
    }

    @Override
    public ServerSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
        return this;
    }

    @Override
    public ServerSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
        return this;
    }

    @Override
    public ServerSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
        super.setMessageSizeEstimator(estimator);
        return this;
    }
}
