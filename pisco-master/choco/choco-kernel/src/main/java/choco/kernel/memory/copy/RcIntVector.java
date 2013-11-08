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

package choco.kernel.memory.copy;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IStateIntVector;

/* 
 * Created by IntelliJ IDEA.
 * User: Julien
 * Date: 29 mars 2007
 * Since : Choco 2.0.0
 *
 */
public final class RcIntVector implements IStateIntVector, RecomputableElement {

    /**
     * Minimal capacity of a vector
     */
    public static final int MIN_CAPACITY = 8;

    /**
     * Contains the elements of the vector.
     */

    private int[] elementData;

    /**
     * A backtrackable search with the size of the vector.
     */

    private RcInt size;


    /**
     * The current environment.
     */

    private final EnvironmentCopying environment;

    private int timeStamp;


    /**
     * Constructs a stored search vector with an initial size, and initial values.
     *
     * @param env          The current environment.
     * @param initialSize  The initial size.
     * @param initialValue The initial common value.
     */

    public RcIntVector(EnvironmentCopying env, int initialSize, int initialValue) {
        int initialCapacity = MIN_CAPACITY;
        int w = env.getWorldIndex();

        if (initialCapacity < initialSize)
            initialCapacity = initialSize;

        this.environment = env;
        timeStamp = environment.getWorldIndex();
        this.elementData = new int[initialCapacity];
        for (int i = 0; i < initialSize; i++) {
            this.elementData[i] = initialValue;
        }
        this.size = new RcInt(env, initialSize);
        env.add(this);
    }


    public RcIntVector(EnvironmentCopying env, int[] entries) {
        int initialCapacity = MIN_CAPACITY;
        int w = env.getWorldIndex();
        int initialSize = entries.length;

        if (initialCapacity < initialSize)
            initialCapacity = initialSize;

        this.environment = env;
        this.elementData = new int[initialCapacity];
        for (int i = 0; i < initialSize; i++) {
            this.elementData[i] = entries[i]; // could be a System.arrayCopy but since the loop is needed...
        }
        this.size = new RcInt(env, initialSize);
        env.add(this);
        timeStamp = environment.getWorldIndex();
    }

    /**
     * Constructs an empty stored search vector.
     *
     * @param env The current environment.
     */

    public RcIntVector(EnvironmentCopying env) {
        this(env, 0, 0);
    }


    /**
     * Returns the current size of the stored search vector.
     */

    public int size() {
        return size.get();
    }


    /**
     * Checks if the vector is empty.
     */

    public boolean isEmpty() {
        return (size.get() == 0);
    }

/*    public Object[] toArray() {
        // TODO : voir ci c'est utile
        return new Object[0];
    }*/


    /**
     * Checks if the capacity is great enough, else the capacity
     * is extended.
     *
     * @param minCapacity the necessary capacity.
     */

    public void ensureCapacity(int minCapacity) {
        int oldCapacity = elementData.length;
        if (minCapacity > oldCapacity) {
            int[] oldData = elementData;
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity)
                newCapacity = minCapacity;
            elementData = new int[newCapacity];
            System.arraycopy(oldData, 0, elementData, 0, size.get());
        }
    }


    /**
     * Adds a new search at the end of the vector.
     *
     * @param i The search to add.
     */

    public void add(int i) {
        timeStamp = environment.getWorldIndex();
        int newsize = size.get() + 1;
        ensureCapacity(newsize);
        size.set(newsize);
        elementData[newsize - 1] = i;
    }

    /**
     * removes the search at the end of the vector.
     * does nothing when called on an empty vector
     */

    public void removeLast() {
        timeStamp = environment.getWorldIndex();
        int newsize = size.get() - 1;
        if (newsize >= 0)
            size.set(newsize);
    }

    /**
     * Returns the <code>index</code>th element of the vector.
     */

    public int get(int index) {
        if (index < size.get() && index >= 0) {
            return elementData[index];
        }
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size.get());
    }

    @Override
    public int quickGet(int index) {
        return elementData[index];
    }

    public boolean contain(int val){
        int ssize = size.get();
         for(int i = 0; i < ssize; i++){
             if(val == elementData[i])return true;
         }
        return false;
    }
    /**
     * Assigns a new value <code>val</code> to the element <code>index</code>.
     */

    public int set(int index, int val) {
        if (index < size.get() && index >= 0) {
            //<hca> je vire cet assert en cas de postCut il n est pas vrai ok ?
            //assert(this.worldStamps[index] <= environment.getWorldIndex());
            int oldValue = elementData[index];
            if (val != oldValue) {
                elementData[index] = val;
            }
            timeStamp = environment.getWorldIndex();
            return oldValue;
        }
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size.get());
    }

    @Override
    public int quickSet(int index, int val) {
       int oldValue = elementData[index];
            if (val != oldValue) {
                elementData[index] = val;
            }
            timeStamp = environment.getWorldIndex();
            return oldValue;
    }

    public int[] toArray(int[] tab) {
        return new int[0];
    }

    public void remove(int i) {

    }

    public void _set(int[] vals) {
        timeStamp = environment.getWorldIndex();
        System.arraycopy(vals,0,elementData,0,vals.length);
    }

    public void _set(int[] vals, int timeStamp) {
        this.timeStamp = timeStamp;
        System.arraycopy(vals,0,elementData,0,vals.length);
    }

    public int[] deepCopy() {
        int[] ret = new int[size.get()];
        System.arraycopy(elementData,0,ret,0,size.get());
        return ret;
    }

    public int getType() {
        return INTVECTOR;
    }

    public int getTimeStamp() {
        return timeStamp;
    }
    
	@Override
	public DisposableIntIterator getIterator() {
		throw new UnsupportedOperationException("not yet implemented");
	}

}
