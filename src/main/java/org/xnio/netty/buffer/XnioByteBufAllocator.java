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

import io.netty.buffer.AbstractByteBufAllocator;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.PlatformDependent;
import org.xnio.ByteBufferSlicePool;

/**
 * {@link io.netty.buffer.ByteBufAllocator} which wraps an existing {@link ByteBufferSlicePool} and use it to allocate direct
 * {@link ByteBuf}. If the requested {@link ByteBuf} is to big it will be allocated directly and not pooled at all.
 *
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
public final class XnioByteBufAllocator extends AbstractByteBufAllocator {
    final ByteBufferSlicePool pool;

    public XnioByteBufAllocator(ByteBufferSlicePool pool) {
        if (pool == null) {
            throw new NullPointerException("pool");
        }
        this.pool = pool;
    }

    @Override
    protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity) {
        return new XnioHeapByteBuf(this, initialCapacity, maxCapacity);
    }

    @Override
    protected ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity) {
        if (PlatformDependent.hasUnsafe()) {
            return new XnioUnsafeDirectByteBuf(this, initialCapacity, maxCapacity);
        }
        return new XnioDirectByteBuf(this, initialCapacity, maxCapacity);
    }

    @Override
    public boolean isDirectBufferPooled() {
        return true;
    }
}
