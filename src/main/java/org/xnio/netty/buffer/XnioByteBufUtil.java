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
package org.xnio.netty.buffer;

import io.netty.util.internal.PlatformDependent;
import org.xnio.Pooled;

import java.nio.ByteBuffer;
import org.xnio.ByteBufferPool;

/**
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
final class XnioByteBufUtil {

    private XnioByteBufUtil() {
        // Utility
    }

    static Pooled<ByteBuffer> allocateDirect(ByteBufferPool pool, int initialCapacity) {
        Pooled<ByteBuffer> pooled;
        if (initialCapacity <= pool.getSize()) {
            pooled = new PooledByteBuf(pool.allocate());
        } else {
            pooled = new PooledByteBuf(ByteBuffer.allocateDirect(initialCapacity));
        }
        return pooled;
    }

    static void freeDirect(ByteBuffer buffer, ByteBuffer newBuffer) {
        if (buffer != newBuffer) {
            PlatformDependent.freeDirectBuffer(buffer);
        }
    }

}
