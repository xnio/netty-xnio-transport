/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xnio.netty.transport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.xnio.conduits.ConduitStreamSourceChannel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * ChannelFuture to be provided when shutting down channels, performs as a close listener on the Xnio side.
 * 
 * 
 * @author Flavia Rainone
 *
 */
public class XnioChannelPromiseCloseFuture extends XnioChannelCloseFuture {

		private final ChannelPromise channelPromise;
		
		public XnioChannelPromiseCloseFuture(AbstractXnioSocketChannel channel, ChannelPromise channelPromise) {
			super(channel);
			this.channelPromise = channelPromise;
		}

		@Override
		public void setError(IOException error) {
			super.setError(error);
			channelPromise.setFailure(error);
		}

		@Override
		public void handleEvent(ConduitStreamSourceChannel channel) {
			super.handleEvent(channel);
			channelPromise.trySuccess();
		}

}
