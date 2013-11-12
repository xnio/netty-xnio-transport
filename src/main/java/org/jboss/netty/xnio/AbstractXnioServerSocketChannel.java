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

import io.netty.channel.AbstractServerChannel;
import io.netty.channel.ChannelException;
import io.netty.channel.EventLoop;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.ServerSocketChannelConfig;
import org.xnio.ChannelListener;
import org.xnio.Option;
import org.xnio.StreamConnection;

import java.io.IOException;
import java.net.InetSocketAddress;


 abstract class AbstractXnioServerSocketChannel extends AbstractServerChannel implements ServerSocketChannel {
     private final XnioServerSocketChannelConfig config = new XnioServerSocketChannelConfig(this);

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
     public ServerSocketChannelConfig config() {
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

     protected abstract <T> void setOption0(Option<T> option, T value) throws IOException;

     protected abstract <T> T getOption0(Option<T> option) throws IOException;

     final class AcceptListener implements ChannelListener<StreamConnection> {
         @Override
         public void handleEvent(StreamConnection channel) {
             pipeline().fireChannelRead(new WrappingXnioSocketChannel(AbstractXnioServerSocketChannel.this, channel));
             pipeline().fireChannelReadComplete();
         }
     }
}
