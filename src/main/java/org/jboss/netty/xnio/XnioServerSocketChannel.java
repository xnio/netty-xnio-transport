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
package org.jboss.netty.xnio;

import io.netty.channel.EventLoop;
import org.xnio.ChannelListener;
import org.xnio.IoFuture;
import org.xnio.Option;
import org.xnio.OptionMap;
import org.xnio.StreamConnection;
import org.xnio.channels.AcceptingChannel;
import org.xnio.channels.BoundChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class XnioServerSocketChannel extends AbstractXnioServerSocketChannel {
    private volatile AcceptingChannel channel;
    private final OptionMap.Builder options = OptionMap.builder();

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return loop instanceof XnioEventLoop;
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        IoFuture<StreamConnection> future = ((XnioEventLoop) eventLoop()).executor.acceptStreamConnection(
                localAddress, new AcceptListener(), new BoundListener(), options.getMap());
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
    protected InetSocketAddress localAddress0() {
        if (channel != null) {
            return channel.getLocalAddress(InetSocketAddress.class);
        }
        return null;
    }

    @Override
    protected <T> T getOption0(Option<T> option) throws IOException {
        if (channel != null) {
            return channel.getOption(option);
        }
        return options.getMap().get(option);
    }

    @Override
    protected <T> void setOption0(Option<T> option, T value) throws IOException {
        if (channel != null) {
            channel.setOption(option, value);
        } else {
            options.set(option, value);
        }
    }

    private final class BoundListener implements ChannelListener<BoundChannel> {
        @Override
        public void handleEvent(BoundChannel channel) {
            XnioServerSocketChannel.this.channel = (AcceptingChannel) channel;
        }
    }
}
