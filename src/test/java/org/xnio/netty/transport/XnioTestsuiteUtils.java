/*
 * Copyright 2014 Red Hat, Inc.
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
package org.xnio.netty.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import io.netty.testsuite.transport.TestsuitePermutation;
import org.xnio.netty.buffer.XnioByteBufAllocator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.xnio.ByteBufferPool;

/**
 *
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
final class XnioTestsuiteUtils {
    private static final EventLoopGroup GROUP;

    static {
        try {
            GROUP = new XnioEventLoopGroup();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static List<ByteBufAllocator> newAllocators(List<ByteBufAllocator> allocs) {
        List<ByteBufAllocator> allocators = new ArrayList<>(allocs);
        allocators.add(new XnioByteBufAllocator(ByteBufferPool.LARGE_DIRECT));
        return allocators;
    }

    static List<TestsuitePermutation.BootstrapComboFactory<ServerBootstrap, Bootstrap>> newFactories() {
        return Collections.<TestsuitePermutation.BootstrapComboFactory<ServerBootstrap, Bootstrap>>singletonList(
                new TestsuitePermutation.BootstrapComboFactory<ServerBootstrap, Bootstrap>() {
                    @Override
                    public ServerBootstrap newServerInstance() {
                        return new ServerBootstrap().channel(XnioServerSocketChannel.class).group(GROUP);
                    }

                    @Override
                    public Bootstrap newClientInstance() {
                        return new Bootstrap().channel(XnioSocketChannel.class).group(GROUP);
                    }
                });
    }

    private XnioTestsuiteUtils() {
        // utility
    }
}
