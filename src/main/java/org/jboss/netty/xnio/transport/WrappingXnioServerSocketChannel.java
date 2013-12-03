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

import org.xnio.Option;
import org.xnio.XnioIoThread;
import org.xnio.channels.AcceptingChannel;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * {@link AbstractXnioServerSocketChannel} implementation which allows to use a {@link AcceptingChannel} and wrap it.
 *
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
public final class WrappingXnioServerSocketChannel extends AbstractXnioServerSocketChannel implements IoThreadPowered {

    private final AcceptingChannel channel;
    private final XnioIoThread thread;

    /**
     * Create a new instance wrapping the given {@link AcceptingChannel}
     */
    @SuppressWarnings("unchecked")
    public WrappingXnioServerSocketChannel(AcceptingChannel channel) {
        if (channel == null) {
            throw new NullPointerException("channel");
        }
        this.channel = channel;
        thread = channel.getIoThread();
        channel.getAcceptSetter().set(new AcceptListener());
        // register a EventLoop and start read
        unsafe().register(new XnioEventLoop(channel.getWorker().getIoThread()), unsafe().voidPromise());
        read();
    }

    @Override
    public XnioIoThread ioThread() {
        return thread;
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
    protected void doBind(SocketAddress localAddress) throws Exception {
        throw XnioUtils.unsupportedForWrapped();
    }

    @Override
    protected AcceptingChannel xnioChannel() {
        return channel;
    }
}
