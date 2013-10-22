package org.jboss.netty.xnio;

import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.SocketChannelConfig;

import java.net.SocketAddress;


public class XnioSocketChannel extends AbstractChannel implements SocketChannel {
    XnioSocketChannel(XnioServerSocketChannel parent) {
        super(parent);
    }
    @Override
    protected AbstractUnsafe newUnsafe() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected SocketAddress localAddress0() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void doDisconnect() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void doClose() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void doBeginRead() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer in) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SocketChannelConfig config() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isInputShutdown() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isOutputShutdown() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ChannelFuture shutdownOutput() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ChannelFuture shutdownOutput(ChannelPromise promise) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isOpen() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isActive() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ChannelMetadata metadata() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
