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

import java.util.Arrays;

/**
 * A double linked list with constant time access, addition and deletion in o(1)
 * The list is encoded by two tables of integers. Efficient implementation regarding
 * time but very poor for memory.
 */
public class DoubleLinkedList extends DisposableIntIterator {

    /**
     * Successors table
     */
    protected final int[] nextT;

    /**
     * Predecessors table
     */
    protected final int[] prevT;


    /**
     * Current number of elements
     */
    protected int size;

    /**
     * util for iteration
     */
    protected int currentT;

    /**
     * maximum size of the list
     */
    protected final int listSize;


    /**
     * build a list of maximum size "listSize"
     *
     * @param listSize
     */
    public DoubleLinkedList(int listSize) {
        this.listSize = listSize;
        this.nextT = new int[listSize + 1];
        this.prevT = new int[listSize + 1];
        reset();
    }

    /**
     * Constructor (copy)
     */
    public DoubleLinkedList(DoubleLinkedList origin) {
        this.nextT = new int[origin.nextT.length];
        this.prevT = new int[origin.prevT.length];
        System.arraycopy(origin.nextT, 0, this.nextT, 0, this.nextT.length);
        System.arraycopy(origin.prevT, 0, this.prevT, 0, this.prevT.length);
        this.size = origin.size;
        this.listSize = origin.listSize;
    }

    /**
     * Add an element "val"
     */
    public void addVal(int val) {
        if (size == 0) {
            nextT[listSize] = val;
            prevT[listSize] = val;
        } else {
            prevT[nextT[listSize]] = val;
            nextT[val] = nextT[listSize];
            nextT[listSize] = val;
        }
        size = size + 1;
    }


    /**
     * Remove an element "val"
     */
    public void removeVal(int val) {
        if (size == 1) {
            nextT[listSize] = -1;
            prevT[listSize] = -1;
        } else if (nextT[listSize] == val) {
            nextT[listSize] = nextT[val];
            prevT[nextT[val]] = -1;
        } else if (prevT[listSize] == val) {
            prevT[listSize] = prevT[val];
            nextT[prevT[val]] = -1;
        } else {
            nextT[prevT[val]] = nextT[val];
            prevT[nextT[val]] = prevT[val];
        }
        nextT[val] = -1;
        prevT[val] = -1;
        size--;
    }

    public final boolean contains(int val) {
        return isIn(val);
    }

    /**
     * Get current number of element
     */
    public final int getSize() {
        return size;
    }

    /**
     * reset
     */
    public void reset() {
        size = 0;
        Arrays.fill(nextT, -1);
        Arrays.fill(prevT, -1);
        this.restart();
    }

    /**
     * Initialize the iterator
     */
    public final void restart() {
        currentT = listSize;
    }

    /**
     * Set the iterator from val
     *
     * @param val
     */
    public final void restartFrom(int val) {
        currentT = val;
    }

    public boolean hasNext() {
        return ((currentT != -1) && (nextT[currentT] != -1));
    }

    public boolean hasNextTo(int val) {
        return ((currentT != -1) && (nextT[currentT] != val));
    }

    /**
     * return the next element
     */
    public int next() {
        currentT = nextT[currentT];
        return currentT;
    }

    /**
     * return the current iterated element
     */
    public int read() {
        return currentT;
    }

    public final int getFirst() {
        return nextT[listSize];
    }

    public final int getLast() {
        return prevT[listSize];
    }

    public final boolean isIn(int val) {
        return nextT[listSize] == val || prevT[val] != -1;
    }

    /**
     * Restrict the domain to the element val
     *
     * @param val
     */
    public final void restrict(int val) {
        reset();
        addVal(val);
    }

    public final int[] toArray() {
        int[] tab = new int[size];
        int cpt = 0;
        restart();
        while (hasNext()) {
            tab[cpt] = next();
            cpt++;
        }
        return tab;
    }

    /**
     * remove the current iterated element
     */
    public void remove() {
        int pred;
        if (currentT != nextT[listSize]) {
            pred = prevT[currentT];
        } else pred = listSize;
        removeVal(currentT);
        currentT = pred;
    }

    // Display the table
    @Override
    public String toString() {
        StringBuilder n = new StringBuilder("next    :");
        StringBuilder p = new StringBuilder("suivant :");
        for (int i = 0; i < nextT.length; i++) {
            n.append(nextT[i]).append(' ');
            p.append(prevT[i]).append(' ');
        }

        return ("first :" + nextT[listSize] + "| " + "last :" + prevT[listSize] + "| " + n + "| " + p);
    }

    @Override
    public void dispose() {
    }
}