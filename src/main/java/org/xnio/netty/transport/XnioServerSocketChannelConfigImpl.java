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

import java.util.Map;

import org.xnio.Options;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.WriteBufferWaterMark;


/**
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
final class XnioServerSocketChannelConfigImpl extends DefaultChannelConfig implements XnioServerSocketChannelConfig {
    private final AbstractXnioServerSocketChannel channel;

    XnioServerSocketChannelConfigImpl(AbstractXnioServerSocketChannel channel) {
        super(channel);
        this.channel = channel;
    }

    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return getOptions(super.getOptions(), XnioChannelOption.BALANCING_CONNECTIONS,
                XnioChannelOption.BALANCING_TOKENS,
                XnioChannelOption.CONNECTION_HIGH_WATER,
                XnioChannelOption.CONNECTION_LOW_WATER);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOption(ChannelOption<T> option) {
        if (option == XnioChannelOption.BALANCING_CONNECTIONS) {
            return (T) Integer.valueOf(getBalancingConnections());
        }
        if (option == XnioChannelOption.BALANCING_TOKENS) {
            return (T) Integer.valueOf(getBalancingTokens());
        }
        if (option == XnioChannelOption.CONNECTION_HIGH_WATER) {
            return (T) Integer.valueOf(getConnectionHighWater());
        }
        if (option == XnioChannelOption.CONNECTION_LOW_WATER) {
            return (T) Integer.valueOf(getConnectionLowWater());
        }
        return super.getOption(option);
    }

    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        validate(option, value);

        if (option == XnioChannelOption.BALANCING_CONNECTIONS) {
            setBalancingConnections((Integer) value);
        } else if (option == XnioChannelOption.BALANCING_TOKENS) {
            setBalancingTokens((Integer) value);
        } else if (option == XnioChannelOption.CONNECTION_HIGH_WATER) {
            setConnectionHighWater((Integer) value);
        } else if (option == XnioChannelOption.CONNECTION_LOW_WATER) {
            setConnectionLowWater((Integer) value);
        } else {
            return super.setOption(option, value);
        }

        return true;
    }

    @Override
    public int getBacklog() {
        return channel.getOption(Options.BACKLOG);
    }

    @Override
    public XnioServerSocketChannelConfig setBacklog(int backlog) {
        channel.setOption(Options.BACKLOG, backlog);
        return this;
    }

    @Override
    public boolean isReuseAddress() {
        return channel.getOption(Options.REUSE_ADDRESSES);
    }

    @Override
    public XnioServerSocketChannelConfig setReuseAddress(boolean reuseAddress) {
        channel.setOption(Options.REUSE_ADDRESSES, reuseAddress);
        return this;
    }

    @Override
    public int getReceiveBufferSize() {
        return channel.getOption(Options.RECEIVE_BUFFER);
    }

    @Override
    public XnioServerSocketChannelConfig setReceiveBufferSize(int receiveBufferSize) {
        channel.setOption(Options.RECEIVE_BUFFER, receiveBufferSize);
        return this;
    }

    @Override
    public XnioServerSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XnioServerSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        super.setConnectTimeoutMillis(connectTimeoutMillis);
        return this;
    }

    @Override @Deprecated
    public XnioServerSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
        super.setMaxMessagesPerRead(maxMessagesPerRead);
        return this;
    }

    @Override
    public XnioServerSocketChannelConfig setWriteSpinCount(int writeSpinCount) {
        super.setWriteSpinCount(writeSpinCount);
        return this;
    }

    @Override
    public XnioServerSocketChannelConfig setAllocator(ByteBufAllocator allocator) {
        super.setAllocator(allocator);
        return this;
    }

    @Override
    public XnioServerSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
        super.setRecvByteBufAllocator(allocator);
        return this;
    }

    @Override
    public XnioServerSocketChannelConfig setAutoRead(boolean autoRead) {
        super.setAutoRead(autoRead);
        return this;
    }

    @Override @Deprecated
    public XnioServerSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
        return this;
    }

    @Override @Deprecated
    public XnioServerSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
        return this;
    }
    
    public XnioServerSocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
        super.setWriteBufferWaterMark(writeBufferWaterMark);
    	return this;
    }

    @Override
    public XnioServerSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
        super.setMessageSizeEstimator(estimator);
        return this;
    }

    @Override
    public XnioServerSocketChannelConfig setAutoClose(boolean autoClose) {
        super.setAutoClose(autoClose);
        return this;
    }

    @Override
    public XnioServerSocketChannelConfig setConnectionHighWater(int connectionHighWater) {
        channel.setOption(Options.CONNECTION_HIGH_WATER, connectionHighWater);
        return this;
    }

    @Override
    public XnioServerSocketChannelConfig setConnectionLowWater(int connectionLowWater) {
        channel.setOption(Options.CONNECTION_LOW_WATER, connectionLowWater);
        return this;
    }

    @Override
    public XnioServerSocketChannelConfig setBalancingTokens(int balancingTokens) {
        channel.setOption(Options.BALANCING_TOKENS, balancingTokens);
        return this;
    }

    @Override
    public XnioServerSocketChannelConfig setBalancingConnections(int connections) {
        channel.setOption(Options.BALANCING_CONNECTIONS, connections);
        return this;
    }

    @Override
    public int getConnectionHighWater() {
        return channel.getOption(Options.CONNECTION_HIGH_WATER);
    }

    @Override
    public int getConnectionLowWater() {
        return channel.getOption(Options.CONNECTION_LOW_WATER);
    }

    @Override
    public int getBalancingTokens() {
        return channel.getOption(Options.BALANCING_TOKENS);
    }

    @Override
    public int getBalancingConnections() {
        return channel.getOption(Options.BALANCING_CONNECTIONS);
    }
}
