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

import io.netty.channel.ChannelPromise;
import org.xnio.Option;
import org.xnio.OptionMap;
import org.xnio.StreamConnection;

import java.io.IOException;
import java.net.SocketAddress;

public class XnioSocketChannel extends AbstractXnioSocketChannel {
    private volatile StreamConnection channel;
    private final OptionMap.Builder options = OptionMap.builder();

    public XnioSocketChannel() {
        super(null);
    }

    @Override
    protected AbstractXnioUnsafe newUnsafe() {
        return new XnioUnsafe();
    }

    @Override
    protected <T> void setOption0(Option<T> option, T value) throws IOException {
       if (channel == null) {
           options.set(option, value);
       } else {
           channel.setOption(option, value);
       }
    }

    @Override
    protected <T> T getOption0(Option<T> option) throws IOException {
        if (channel == null) {
            return options.getMap().get(option);
        } else {
            return channel.getOption(option);
        }
    }

    @Override
    protected StreamConnection connection() {
        return channel;
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        throw new UnsupportedOperationException("Not support to bind first with XNIO");
    }

    private final class XnioUnsafe extends AbstractXnioUnsafe {
        @Override
        public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            ((XnioEventLoop) eventLoop()).executor.openStreamConnection(localAddress, remoteAddress, null, null, null);
            // TODO: Fix me
        }
    }
}
