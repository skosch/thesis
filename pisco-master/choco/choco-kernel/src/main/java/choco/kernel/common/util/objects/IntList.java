/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package choco.kernel.common.util.objects;

import choco.kernel.common.util.iterators.DisposableIntIterator;


/**
 * A list over integers
 */
public class IntList {
    protected final int[] content;
    protected int size;
    protected int currentIdx;
    private IntListIterator _iterator;

    public IntList(int size) {
        this.size = size;
        this.content = new int[size];
    }

    public IntList(int[] content, int size) {
        this.content = content;
        this.size = size;
    }

    public IntList copy() {
        int[] copContent = new int[content.length];
        System.arraycopy(content, 0, copContent, 0, copContent.length);
        return new IntList(copContent, size);
    }

    public int getFirst() {
        if (size > 0) return content[0];
        else throw new IllegalArgumentException("List is empty");
    }

    public int getSize() {
        return size;
    }

    public void reInit() {
        size = 0;
    }

    public void add(int v) {
        if (size == content.length)
            throw new IllegalArgumentException(String.valueOf(size));
        else
            content[size++] = v;
    }


    @Override
    public String toString() {
        StringBuilder b = new StringBuilder(16);
        b.append('{');
        DisposableIntIterator iter = iterator();
        if (iter.hasNext()) {
            b.append(iter.next());
            while (iter.hasNext()) {
                b.append(',').append(iter.next());
            }
        }
        iter.dispose();
        b.append('}');
        return new String(b);
    }

    public final DisposableIntIterator iterator() {
        if (_iterator == null || !_iterator.reusable()) {
            _iterator = new IntListIterator();
        }
        _iterator.init(this);
        return _iterator;
    }


    protected static class IntListIterator extends DisposableIntIterator {

        int currentIdx;

        IntList list;

        public void init(final IntList list) {
            super.init();
            this.list = list;
            currentIdx = 0;
        }

        public boolean hasNext() {
            return (currentIdx < list.size);
        }

        public int next() {
            return list.content[currentIdx++];
        }

        public void remove() {
            if (currentIdx != list.size) {
                currentIdx--; // back to last returned
                list.content[currentIdx] = list.content[list.size - 1]; // reinsert the last one where we remove one
            }
            list.size--;
        }

        /**
         * Read the next element wihtout incrementing
         */
        public int read() {
            return list.content[currentIdx];
        }
    }
}