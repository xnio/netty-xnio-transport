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
import io.netty.channel.socket.SocketChannelConfig;
import org.xnio.Options;


/**
 * {@link SocketChannelConfig} implementation used by the XNIO transport
 *
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
final class XnioSocketChannelConfig extends DefaultChannelConfig implements SocketChannelConfig {

    private final AbstractXnioSocketChannel channel;

    XnioSocketChannelConfig(AbstractXnioSocketChannel channel) {
        super(channel);
        this.channel = channel;
    }

    @Override
    public boolean isTcpNoDelay() {
        return channel.getOption(Options.TCP_NODELAY);
    }

    @Override
    public SocketChannelConfig setTcpNoDelay(boolean tcpNoDelay) {
        channel.setOption(Options.TCP_NODELAY, tcpNoDelay);
        return this;
    }

    @Override
    public int getSoLinger() {
        return 0;
    }

    @Override
    public SocketChannelConfig setSoLinger(int soLinger) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSendBufferSize() {
        return channel.getOption(Options.SEND_BUFFER);
    }

    @Override
    public SocketChannelConfig setSendBufferSize(int sendBufferSize) {
        channel.setOption(Options.SEND_BUFFER, sendBufferSize);
        return this;
    }

    @Override
    public int getReceiveBufferSize() {
        return channel.getOption(Options.RECEIVE_BUFFER);
    }

    @Override
    public SocketChannelConfig setReceiveBufferSize(int receiveBufferSize) {
        channel.setOption(Options.RECEIVE_BUFFER, receiveBufferSize);
        return this;
    }

    @Override
    public boolean isKeepAlive() {
        return channel.getOption(Options.KEEP_ALIVE);
    }

    @Override
    public SocketChannelConfig setKeepAlive(boolean keepAlive) {
        channel.setOption(Options.KEEP_ALIVE, keepAlive);
        return this;
    }

    @Override
    public int getTrafficClass() {
        return channel.getOption(Options.IP_TRAFFIC_CLASS);
    }

    @Override
    public SocketChannelConfig setTrafficClass(int trafficClass) {
        channel.setOption(Options.IP_TRAFFIC_CLASS, trafficClass);
        return this;
    }

    @Override
    public boolean isReuseAddress() {
        return channel.getOption(Options.REUSE_ADDRESSES);

    }

    @Override
    public SocketChannelConfig setReuseAddress(boolean reuseAddress) {
        channel.setOption(Options.REUSE_ADDRESSES, reuseAddress);
        return this;
    }

    @Override
    public SocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAllowHalfClosure() {
        return false;
    }

    @Override
    public SocketChannelConfig setAllowHalfClosure(boolean allowHalfClosure) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        super.setConnectTimeoutMillis(connectTimeoutMillis);
        return this;
    }

    @Override
    public SocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
        super.setMaxMessagesPerRead(maxMessagesPerRead);
        return this;
    }

    @Override
    public SocketChannelConfig setWriteSpinCount(int writeSpinCount) {
        super.setWriteSpinCount(writeSpinCount);
        return this;
    }

    @Override
    public SocketChannelConfig setAllocator(ByteBufAllocator allocator) {
        super.setAllocator(allocator);
        return this;
    }

    @Override
    public SocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
        super.setRecvByteBufAllocator(allocator);
        return this;
    }

    @Override
    public SocketChannelConfig setAutoRead(boolean autoRead) {
        super.setAutoRead(autoRead);
        return this;
    }

    @Override
    public SocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
        return this;
    }

    @Override
    public SocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
        super.setMessageSizeEstimator(estimator);
        return this;
    }

    @Override
    public SocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
        return this;
    }

    @Override
    public SocketChannelConfig setAutoClose(boolean autoClose) {
        super.setAutoClose(autoClose);
        return this;
    }
}
