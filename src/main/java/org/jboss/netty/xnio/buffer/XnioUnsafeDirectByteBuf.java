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
package org.jboss.netty.xnio.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledUnsafeDirectByteBuf;
import org.xnio.Pooled;

import java.nio.ByteBuffer;

/**
 * @author <a href="mailto:nmaurer@redhat.com">Norman Maurer</a>
 */
final class XnioUnsafeDirectByteBuf extends UnpooledUnsafeDirectByteBuf {

    private Pooled<ByteBuffer> pooled;

    XnioUnsafeDirectByteBuf(XnioByteBufAllocator alloc, int initialSize, int maxCapacity) {
        super(alloc, initialSize, maxCapacity);
    }

    @Override
    public ByteBuf capacity(int newCapacity) {
        ensureAccessible();
        if (newCapacity < 0 || newCapacity > maxCapacity()) {
            throw new IllegalArgumentException("newCapacity: " + newCapacity);
        }
        Pooled<ByteBuffer> oldPooled = this.pooled;
        super.capacity(newCapacity);
        if (oldPooled != pooled) {
            oldPooled.free();
        }
        return this;
    }

    @Override
    protected ByteBuffer allocateDirect(int initialCapacity) {
        Pooled<ByteBuffer> pooled = XnioByteBufUtil.allocateDirect(
                ((XnioByteBufAllocator) alloc()).pool, initialCapacity);
        this.pooled = pooled;
        return pooled.getResource();
    }

    @Override
    protected void freeDirect(ByteBuffer buffer) {
        XnioByteBufUtil.freeDirect(buffer, pooled.getResource());
    }

    @Override
    protected void deallocate() {
        super.deallocate();
        pooled.free();
    }
}
