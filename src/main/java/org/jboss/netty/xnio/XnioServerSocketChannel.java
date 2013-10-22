package org.jboss.netty.xnio;

import io.netty.channel.AbstractServerChannel;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.ServerSocketChannelConfig;
import org.xnio.ChannelListener;
import org.xnio.IoFuture;
import org.xnio.StreamConnection;
import org.xnio.channels.AcceptingChannel;
import org.xnio.channels.BoundChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class XnioServerSocketChannel extends AbstractServerChannel implements ServerSocketChannel {
    private volatile AcceptingChannel channel;
    private final XnioChannelConfig config = new XnioChannelConfig(this);

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return loop instanceof XnioEventLoop;
    }


    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
       IoFuture<StreamConnection> future = ((XnioEventLoop) eventLoop()).executor.acceptStreamConnection(localAddress, new ChannelListener<StreamConnection>() {
           @Override
           public void handleEvent(StreamConnection channel) {
               pipeline().fireChannelRead(channel);
               pipeline().fireChannelReadComplete();
           }
       }, new ChannelListener<BoundChannel>() {
                   @Override
                   public void handleEvent(BoundChannel channel) {
                    XnioServerSocketChannel.this.channel = (AcceptingChannel) channel;
                   }
               }, null);
        IOException exception = future.getException();
        if (exception != null) {
            throw exception;
        }
    }

    @Override
    protected void doClose() throws Exception {
        if (channel != null) {
            channel.close();
        }
    }

    @Override
    protected void doBeginRead() throws Exception {
        if (channel != null) {
            channel.resumeAccepts();
        } else {
            throw new IOException("Channel not bound yet");
        }
    }

    @Override
    public boolean isOpen() {
        return channel != null && channel.isOpen();
    }

    @Override
    public boolean isActive() {
        return isOpen();
    }

    @Override
    public ServerSocketChannelConfig config() {
        return config;
    }

    @Override
    protected InetSocketAddress localAddress0() {
        if (channel != null) {
            return channel.getLocalAddress(InetSocketAddress.class);
        }
        return null;
    }

    @Override
    public InetSocketAddress localAddress() {
        return (InetSocketAddress) super.localAddress();
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return (InetSocketAddress) super.remoteAddress();
    }
}
