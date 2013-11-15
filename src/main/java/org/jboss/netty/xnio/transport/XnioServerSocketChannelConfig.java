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
import io.netty.channel.socket.ServerSocketChannelConfig;

/**
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
public interface XnioServerSocketChannelConfig extends ChannelConfig, ServerSocketChannelConfig {

    /**
     * @see {@link XnioChannelOption#CONNECTION_HIGH_WATER}
     */
    XnioServerSocketChannelConfig setConnectionHighWater(int connectionHighWater);

    /**
     * @see {@link XnioChannelOption#CONNECTION_HIGH_WATER}
     */
    int getConnectionHighWater();

    /**
     * @see {@link XnioChannelOption#CONNECTION_LOW_WATER}
     */
    XnioServerSocketChannelConfig setConnectionLowWater(int connectionLowWater);

    /**
     * @see {@link XnioChannelOption#CONNECTION_LOW_WATER}
     */
    int getConnectionLowWater();

    /**
     * @see {@link XnioChannelOption#BALANCING_TOKENS}
     */
    XnioServerSocketChannelConfig setBalancingTokens(int balancingTokens);

    /**
     * @see {@link XnioChannelOption#BALANCING_TOKENS}
     */
    int getBalancingTokens();

    /**
     * @see {@link XnioChannelOption#BALANCING_CONNECTIONS}
     */
    XnioServerSocketChannelConfig setBalancingConnections(int connections);

    /**
     * @see {@link XnioChannelOption#BALANCING_CONNECTIONS}
     */
    int getBalancingConnections();

    @Override
    XnioServerSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

    @Override
    XnioServerSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead);

    @Override
    XnioServerSocketChannelConfig setWriteSpinCount(int writeSpinCount);

    @Override
    XnioServerSocketChannelConfig setAllocator(ByteBufAllocator allocator);

    @Override
    XnioServerSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator);

    @Override
    XnioServerSocketChannelConfig setAutoRead(boolean autoRead);

    @Deprecated
    XnioServerSocketChannelConfig setAutoClose(boolean autoClose);

    @Override
    XnioServerSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark);

    @Override
    XnioServerSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark);

    @Override
    XnioServerSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator);

    @Override
    XnioServerSocketChannelConfig setBacklog(int backlog);

    @Override
    XnioServerSocketChannelConfig setReuseAddress(boolean reuseAddress);

    @Override
    XnioServerSocketChannelConfig setReceiveBufferSize(int receiveBufferSize);

    @Override
    XnioServerSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth);
}