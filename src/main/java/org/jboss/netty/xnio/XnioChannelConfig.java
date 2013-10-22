package org.jboss.netty.xnio;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.MessageSizeEstimator;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.socket.ServerSocketChannelConfig;
import org.xnio.OptionMap;

import java.util.Map;

public class XnioChannelConfig extends DefaultChannelConfig implements ServerSocketChannelConfig {
    private final OptionMap.Builder options = OptionMap.builder();
    public XnioChannelConfig(Channel channel) {
        super(channel);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public int getBacklog() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ServerSocketChannelConfig setBacklog(int backlog) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isReuseAddress() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ServerSocketChannelConfig setReuseAddress(boolean reuseAddress) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getReceiveBufferSize() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ServerSocketChannelConfig setReceiveBufferSize(int receiveBufferSize) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ServerSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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

    OptionMap options() {
        return options.getMap();
    }
}
