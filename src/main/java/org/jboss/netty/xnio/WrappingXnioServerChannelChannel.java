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
import org.xnio.Option;
import org.xnio.channels.AcceptingChannel;

import java.io.IOException;
import java.net.SocketAddress;

public class WrappingXnioServerChannelChannel extends AbstractXnioServerSocketChannel {

    private final AcceptingChannel channel;
    private final EventLoop eventLoop;

    @SuppressWarnings("unchecked")
    public WrappingXnioServerChannelChannel(AcceptingChannel channel) {
        this.channel = channel;
        eventLoop = new XnioEventLoop(channel.getWorker().getIoThread());
        channel.getAcceptSetter().set(new AcceptListener());
    }

    @Override
    public EventLoop eventLoop() {
        return eventLoop;
    }

    @Override
    protected <T> void setOption0(Option<T> option, T value) throws IOException {
        channel.setOption(option, value);
    }

    @Override
    protected <T> T getOption0(Option<T> option) throws IOException {
        return channel.getOption(option);
    }

    @Override
    protected SocketAddress localAddress0() {
        return channel.getLocalAddress();
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        throw new UnsupportedOperationException("Wrapped XNIO Channel");
    }

    @Override
    protected void doClose() throws Exception {
        channel.close();
    }

    @Override
    protected void doBeginRead() throws Exception {
        channel.resumeAccepts();
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }
}
