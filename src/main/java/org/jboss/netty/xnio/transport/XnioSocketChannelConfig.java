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
import io.netty.channel.ChannelConfig;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.socket.SocketChannelConfig;

/**
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
public interface XnioSocketChannelConfig extends ChannelConfig, SocketChannelConfig {

    @Override
    XnioSocketChannelConfig setTcpNoDelay(boolean tcpNoDelay);

    @Override
    XnioSocketChannelConfig setSoLinger(int soLinger);

    @Override
    XnioSocketChannelConfig setSendBufferSize(int sendBufferSize);

    @Override
    XnioSocketChannelConfig setReceiveBufferSize(int receiveBufferSize);

    @Override
    XnioSocketChannelConfig setKeepAlive(boolean keepAlive);

    @Override
    XnioSocketChannelConfig setTrafficClass(int trafficClass);

    @Override
    XnioSocketChannelConfig setReuseAddress(boolean reuseAddress);

    @Override
    XnioSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth);

    @Override
    XnioSocketChannelConfig setAllowHalfClosure(boolean allowHalfClosure);

    @Override
    XnioSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

    @Override
    XnioSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead);

    @Override
    XnioSocketChannelConfig setWriteSpinCount(int writeSpinCount);

    @Override
    XnioSocketChannelConfig setAllocator(ByteBufAllocator allocator);

    @Override
    XnioSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator);

    @Override
    XnioSocketChannelConfig setAutoRead(boolean autoRead);

    @Override
    XnioSocketChannelConfig setAutoClose(boolean autoClose);

    @Override
    XnioSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator);

    @Override
    XnioSocketChannelConfig setWriteBufferHighWaterMark(int mark);

    @Override
    XnioSocketChannelConfig setWriteBufferLowWaterMark(int mark);
}
