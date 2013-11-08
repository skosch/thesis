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

// Petite API de Graphes pour tester BK73 
// TP 16/02/2007
// --------------------------------------

package choco.kernel.common.util.objects;

import java.util.Arrays;
import java.util.Iterator;

/**
 * A simple representation of a 0-1 sparse matrix
 */
public class BooleanSparseMatrix implements ISparseMatrix{

    /**
     * valued rows
     */
    private long[] elements;


    private int nbElement;
    /**
     * Number of rows of the matrix
     */
    final int size;

    public BooleanSparseMatrix(final int n) {
        size = n;
        elements = new long[n];
        nbElement = 0;
    }

    /**
     * Add a new element in the matrix
     * @param i
     * @param j
     */
    public void add(int i, int j){
        ensureCapacity(nbElement+1);
        long v  = ((long)Math.min(i,j)*(long)size+(long)Math.max(i,j));
        elements[nbElement++] = v;
    }

    private void ensureCapacity(int nsize) {
        if(elements.length<nsize){
            long[] oldElements = new long[nsize * 3/2];
            System.arraycopy(elements, 0, oldElements, 0, elements.length);
            elements = oldElements;
        }
    }


    /**
     * get the number of element contained in the matrix
     * @return the number of element
     */
    public int getNbElement(){
        return nbElement;
    }


    /**
     * Return an iterator over the values
     *
     * @return an iterator
     */
    @Override
    public Iterator<Long> iterator() {
        return new Iterator<Long>(){
            int i = 0;
            /**
             * Returns <tt>true</tt> if the iteration has more elements. (In other
             * words, returns <tt>true</tt> if <tt>next</tt> would return an element
             * rather than throwing an exception.)
             *
             * @return <tt>true</tt> if the getIterator has more elements.
             */
            @Override
            public boolean hasNext() {
                return i < nbElement;
            }

            /**
             * Returns the next element in the iteration.
             *
             * @return the next element in the iteration.
             * @throws java.util.NoSuchElementException
             *          iteration has no more elements.
             */
            @Override
            public Long next() {
                return elements[i++];
            }

            /**
             * Removes from the underlying collection the last element returned by the
             * getIterator (optional operation).  This method can be called only once per
             * call to <tt>next</tt>.  The behavior of an getIterator is unspecified if
             * the underlying collection is modified while the iteration is in
             * progress in any way other than by calling this method.
             *
             * @throws UnsupportedOperationException if the <tt>remove</tt>
             *                                       operation is not supported by this Iterator.
             * @throws IllegalStateException         if the <tt>next</tt> method has not
             *                                       yet been called, or the <tt>remove</tt> method has already
             *                                       been called after the last call to the <tt>next</tt>
             *                                       method.
             */
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Prepare the matrix for correct iteration.
     */
    @Override
    public void prepare() {
        long[] n = new long[nbElement];
        System.arraycopy(elements, 0, n, 0, nbElement);
        Arrays.sort(n);
        elements = n;
    }
}