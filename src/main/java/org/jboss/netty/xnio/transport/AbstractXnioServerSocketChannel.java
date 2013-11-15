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

import io.netty.channel.AbstractServerChannel;
import io.netty.channel.ChannelException;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.ServerSocketChannel;
import org.xnio.ChannelListener;
import org.xnio.Option;
import org.xnio.StreamConnection;
import org.xnio.channels.AcceptingChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;


/**
 * {@link ServerSocketChannel} base class for our XNIO transport
 *
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
abstract class AbstractXnioServerSocketChannel extends AbstractServerChannel implements ServerSocketChannel {
    private final XnioServerSocketChannelConfigImpl config = new XnioServerSocketChannelConfigImpl(this);

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return loop instanceof XnioEventLoop;
    }

    @Override
    public boolean isActive() {
        return isOpen();
    }

    @Override
    public InetSocketAddress localAddress() {
        return (InetSocketAddress) super.localAddress();
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return (InetSocketAddress) super.remoteAddress();
    }

    @Override
    public XnioServerSocketChannelConfigImpl config() {
        return config;
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


    @Override
    protected SocketAddress localAddress0() {
        AcceptingChannel channel = xnioChannel();
        if (channel == null) {
            return null;
        }
        return channel.getLocalAddress();
    }

    @Override
    protected void doClose() throws Exception {
        AcceptingChannel channel = xnioChannel();
        if (channel == null) {
            return;
        }
        channel.close();
    }

    @Override
    protected void doBeginRead() throws Exception {
        AcceptingChannel channel = xnioChannel();
        if (channel == null) {
            return;
        }
        channel.resumeAccepts();
    }

    @Override
    public boolean isOpen() {
        AcceptingChannel channel = xnioChannel();
        return channel == null || channel.isOpen();
    }

    /**
     * Return the underyling {@link AcceptingChannel}
     */
    protected abstract AcceptingChannel xnioChannel();

    /**
     * Set the given {@link Option} to the given value.
     */
    protected abstract <T> void setOption0(Option<T> option, T value) throws IOException;

    /**
     * Return the value for the given {@link Option}.
     */
    protected abstract <T> T getOption0(Option<T> option) throws IOException;

    /**
     * {@link ChannelListener} implementation which takes care of accept connections and fire them through the
     * {@link io.netty.channel.ChannelPipeline}.
     */
    final class AcceptListener implements ChannelListener<AcceptingChannel<StreamConnection>> {
        @Override
        public void handleEvent(AcceptingChannel<StreamConnection> channel) {
            try {
                int messagesToRead = config().getMaxMessagesPerRead();
                for (int i = 0; i < messagesToRead; i++) {
                    StreamConnection conn = channel.accept();
                    if (conn == null) {
                        break;
                    }
                    pipeline().fireChannelRead(new WrappingXnioSocketChannel(AbstractXnioServerSocketChannel.this, conn));
                }
            } catch (Throwable cause) {
                pipeline().fireExceptionCaught(cause);
            }
            pipeline().fireChannelReadComplete();
        }
    }
}
