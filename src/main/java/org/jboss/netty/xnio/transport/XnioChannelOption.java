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

import io.netty.channel.ChannelOption;

/**
 * {@link ChannelOption}'s specific for the XNIO transport.
 *
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
public final class XnioChannelOption<T> extends ChannelOption<T>{

    /**
     * @see {@link org.xnio.Options#CONNECTION_HIGH_WATER}
     */
    public static final ChannelOption<Integer> CONNECTION_HIGH_WATER = valueOf("CONNECTION_HIGH_WATER");

    /**
     * @see {@link org.xnio.Options#CONNECTION_LOW_WATER}
     */
    public static final ChannelOption<Integer> CONNECTION_LOW_WATER = valueOf("CONNECTION_LOW_WATER");

    /**
     * @see {@link org.xnio.Options#BALANCING_TOKENS}
     */
    public static final ChannelOption<Integer> BALANCING_TOKENS = valueOf("BALANCING_TOKENS");

    /**
     * @see {@link org.xnio.Options#BALANCING_CONNECTIONS}
     */
    public static final ChannelOption<Integer> BALANCING_CONNECTIONS = valueOf("BALANCING_CONNECTIONS");

    @SuppressWarnings("unused")
    private XnioChannelOption(String name) {
        super(name);
    }
}
