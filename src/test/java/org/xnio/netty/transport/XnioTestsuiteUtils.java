package org.xnio.netty.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.testsuite.transport.TestsuitePermutation;
import org.xnio.ByteBufferSlicePool;
import org.xnio.netty.buffer.XnioByteBufAllocator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
public final class XnioTestsuiteUtils {
    static List<ByteBufAllocator> newAllocators(List<ByteBufAllocator> allocs) {
        List<ByteBufAllocator> allocators = new ArrayList<>(allocs);
        allocators.add(new XnioByteBufAllocator(new ByteBufferSlicePool(1024 * 16, 1024 * 32)));
        return allocators;
    }

    static List<TestsuitePermutation.BootstrapComboFactory<ServerBootstrap, Bootstrap>> newFactories() {
        return Collections.<TestsuitePermutation.BootstrapComboFactory<ServerBootstrap, Bootstrap>>singletonList(
                new TestsuitePermutation.BootstrapComboFactory<ServerBootstrap, Bootstrap>() {
                    @Override
                    public ServerBootstrap newServerInstance() {
                        try {
                            return new ServerBootstrap().channel(XnioServerSocketChannel.class).group(new XnioEventLoopGroup());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public Bootstrap newClientInstance() {
                        try {
                            return new Bootstrap().channel(XnioSocketChannel.class).group(new XnioEventLoopGroup());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    private XnioTestsuiteUtils() {
        // utility
    }
}
