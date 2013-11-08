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

package choco.cp.solver.constraints.integer.bool.sat;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 *
 **/
public final class Vec<T> {

//    private static final long serialVersionUID = 1L;

    private static final int RANDOM_SEED = 91648253;

    /**
     * Create a Vector with an initial capacity of 5 elements.
     */
    public Vec() {
        this(5);
    }

    /**
     * Adapter method to translate an array of int into an IVec.
     * <p/>
     * The array is used inside the Vec, so the elements may be modified
     * outside the Vec. But it should not take much memory. The size of
     * the created Vec is the length of the array.
     *
     * @param elts a filled array of T.
     */
    public Vec(T[] elts) {
        myarray = elts;
        nbelem = elts.length;
    }

    /**
     * Create a Vector with a given capacity.
     *
     * @param size the capacity of the vector.
     */
    @SuppressWarnings("unchecked")
    public Vec(int size) {
        myarray = (T[]) new Object[size];
    }

    /**
     * Construit un vecteur contenant de taille size rempli ? l'aide de size
     * pad.
     *
     * @param size la taille du vecteur
     * @param pad  l'objet servant ? remplir le vecteur
     */
    @SuppressWarnings("unchecked")
    public Vec(int size, T pad) {
        myarray = (T[]) new Object[size];
        for (int i = 0; i < size; i++) {
            myarray[i] = pad;
        }
        nbelem = size;
    }

    public int size() {
        return nbelem;
    }

    /**
     * Remove nofelems from the Vector. It is assumed that the number of
     * elements to remove is smaller or equals to the current number of elements
     * in the vector
     *
     * @param nofelems the number of elements to remove.
     */
    public void shrink(int nofelems) {
        assert nofelems <= nbelem;
        while (nofelems-- > 0) {
            myarray[--nbelem] = null;
        }
    }

    /**
     * reduce the Vector to exactly newsize elements
     *
     * @param newsize the new size of the vector.
     */
    public void shrinkTo(final int newsize) {
        assert newsize <= size();
        for (int i = nbelem; i > newsize; i--) {
            myarray[i - 1] = null;
        }
        nbelem = newsize;
        assert size() == newsize;
    }

    /**
     * Pop the last element on the stack. It is assumed that the stack is not
     * empty!
     */
    public void pop() {
        assert size() > 0;
        myarray[--nbelem] = null;
    }

    public void growTo(final int newsize, final T pad) {
        assert newsize >= size();
        ensure(newsize);
        for (int i = nbelem; i < newsize; i++) {
            myarray[i] = pad;
        }
        nbelem = newsize;
    }

    @SuppressWarnings("unchecked")
    public final void ensure(final int nsize) {
        if (nsize >= myarray.length) {
            T[] narray = (T[]) new Object[Math.max(nsize, nbelem * 2)];
            System.arraycopy(myarray, 0, narray, 0, nbelem);
            myarray = narray;
        }
    }

    public Vec<T> push(final T elem) {
        ensure(nbelem + 1);
        myarray[nbelem++] = elem;
        return this;
    }

    public void unsafePush(final T elem) {
        myarray[nbelem++] = elem;
    }

    /**
     * Insert an element at the very begining of the vector. The former first
     * element is appended to the end of the vector in order to have a constant
     * time operation.
     *
     * @param elem the element to put first in the vector.
     */
    public void insertFirst(final T elem) {
        if (nbelem > 0) {
            push(myarray[0]);
            myarray[0] = elem;
            return;
        }
        push(elem);
    }

    public void insertFirstWithShifting(final T elem) {
        if (nbelem > 0) {
            ensure(nbelem + 1);
            for (int i = nbelem; i > 0; i--) {
                myarray[i] = myarray[i - 1];
            }
            myarray[0] = elem;
            nbelem++;
            return;
        }
        push(elem);
    }

    public void clear() {
        while (nbelem > 0) {
            myarray[--nbelem] = null;
        }
    }

    /**
     * return the latest element on the stack. It is assumed that the stack is
     * not empty!
     *
     * @return the last element on the stack (the one on the top)
     */
    public T last() {
        assert size() != 0;
        return myarray[nbelem - 1];
    }

    public T get(int i) {
        return myarray[i];
    }

    public void set(int i, T o) {
        myarray[i] = o;
    }

    /**
     * Enleve un element qui se trouve dans le vecteur!!!
     *
     * @param elem un element du vecteur
     */
    public void remove(T elem) {
        assert size() > 0;
        int j = 0;
        for (; myarray[j] != elem; j++) {
            assert j < size();
        }
        for (; j < size() - 1; j++) {
            myarray[j] = myarray[j + 1];
        }
        pop();
    }

    /**
     * Delete the ith element of the vector. The latest element of the vector
     * replaces the removed element at the ith indexer.
     *
     * @param i the indexer of the element in the vector
     * @return the former ith element of the vector that is now removed from the
     *         vector
     */
    public T delete(int i) {
        assert i >= 0;
        assert i < nbelem;
        T ith = myarray[i];
        myarray[i] = myarray[--nbelem];
        myarray[nbelem] = null;
        return ith;
    }

    private int nbelem;

    private T[] myarray;

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer stb = new StringBuffer();
        for (int i = 0; i < nbelem - 1; i++) {
            stb.append(myarray[i]);
            stb.append(","); //$NON-NLS-1$
        }
        if (nbelem > 0) {
            stb.append(myarray[nbelem - 1]);
        }
        return stb.toString();
    }

    private static Random rand = new Random(RANDOM_SEED);

    void selectionSort(int from, int to, Comparator<T> cmp) {
        int i, j, best_i;
        T tmp;

        for (i = from; i < to - 1; i++) {
            best_i = i;
            for (j = i + 1; j < to; j++) {
                if (cmp.compare(myarray[j], myarray[best_i]) < 0)
                    best_i = j;
            }
            tmp = myarray[i];
            myarray[i] = myarray[best_i];
            myarray[best_i] = tmp;
        }
    }

    void sort(int from, int to, Comparator<T> cmp) {
        int width = to - from;
        if (to - from <= 15)
            selectionSort(from, to, cmp);

        else {
            T pivot = myarray[rand.nextInt(width) + from];
            T tmp;
            int i = from - 1;
            int j = to;

            for (; ;) {
                do
                    i++;
                while (cmp.compare(myarray[i], pivot) < 0);
                do
                    j--;
                while (cmp.compare(pivot, myarray[j]) < 0);

                if (i >= j)
                    break;

                tmp = myarray[i];
                myarray[i] = myarray[j];
                myarray[j] = tmp;
            }

            sort(from, i, cmp);
            sort(i, to, cmp);
        }
    }

    /**
     * @param comparator
     */
    public void sort(Comparator<T> comparator) {
        sort(0, nbelem, comparator);
    }

    public void sortUnique(Comparator<T> cmp) {
        int i, j;
        T last;

        if (nbelem == 0)
            return;

        sort(0, nbelem, cmp);

        i = 1;
        last = myarray[0];
        for (j = 1; j < nbelem; j++) {
            if (cmp.compare(last, myarray[j]) < 0) {
                last = myarray[i] = myarray[j];
                i++;
            }
        }

        nbelem = i;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vec) {
            Vec<?> v = (Vec<?>) obj;
            if (v.size() != size())
                return false;
            for (int i = 0; i < size(); i++) {
                if (!v.get(i).equals(get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int sum = 0;
        for (int i = 0; i < nbelem; i++) {
            sum += myarray.hashCode() / nbelem;
        }
        return sum;
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int i = 0;

            public boolean hasNext() {
                return i < nbelem;
            }

            public T next() {
                if (i == nbelem)
                    throw new NoSuchElementException();
                return myarray[i++];
            }

            public void remove() {
                //delete(i); i--;
                throw new UnsupportedOperationException();
            }
        };
    }

}
