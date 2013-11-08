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

package choco.kernel.memory.trailing;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IStateIntVector;
import choco.kernel.memory.trailing.trail.StoredIntVectorTrail;

/**
 * Implements a backtrackable search vector.
 * <p/>
 * Cette classe permet de stocker facilment des entiers dans un tableau
 * backtrackable d'entiers.
 */
public final class StoredIntVector implements IStateIntVector {


	/**
	 * Minimal capacity of a vector
	 */
	public static final int MIN_CAPACITY = 8;

	/**
	 * Contains the elements of the vector.
	 */

	private int[] elementData;

	/**
	 * Contains time stamps for all entries (the world index of the last update for each entry)
	 */

    public int[] worldStamps;

	/**
	 * A backtrackable search with the size of the vector.
	 */

	private StoredInt size;


	/**
	 * The current environment.
	 */

	private final EnvironmentTrailing environment;

    protected final StoredIntVectorTrail myTrail;


	/**
	 * Constructs a stored search vector with an initial size, and initial values.
	 *
	 * @param env          The current environment.
	 * @param initialSize  The initial size.
	 * @param initialValue The initial common value.
	 */

	public StoredIntVector(EnvironmentTrailing env, int initialSize, int initialValue) {
		int initialCapacity = MIN_CAPACITY;
		int w = env.getWorldIndex();

		if (initialCapacity < initialSize)
			initialCapacity = initialSize;

		this.environment = env;
		this.elementData = new int[initialCapacity];
		this.worldStamps = new int[initialCapacity];
		for (int i = 0; i < initialSize; i++) {
			this.elementData[i] = initialValue;
			this.worldStamps[i] = w;
		}
		this.size = new StoredInt(env, initialSize);
        this.myTrail= environment.getIntVectorTrail();
	}


	public StoredIntVector(EnvironmentTrailing env, int[] entries) {
		int initialCapacity = MIN_CAPACITY;
		int w = env.getWorldIndex();
		int initialSize = entries.length;

		if (initialCapacity < initialSize)
			initialCapacity = initialSize;

		this.environment = env;
		this.elementData = new int[initialCapacity];
		this.worldStamps = new int[initialCapacity];
		for (int i = 0; i < initialSize; i++) {
			this.elementData[i] = entries[i]; // could be a System.arrayCopy but since the loop is needed...
			this.worldStamps[i] = w;
		}
		this.size = new StoredInt(env, initialSize);
        this.myTrail= environment.getIntVectorTrail();
	}

	/**
	 * Constructs an empty stored search vector.
	 *
	 * @param env The current environment.
	 */

	public StoredIntVector(EnvironmentTrailing env) {
		this(env, 0, 0);
	}

	private boolean rangeCheck(int index) {
		return index < size.get() && index >= 0;
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
			int[] oldStamps = worldStamps;
			int newCapacity = (oldCapacity * 3) / 2 + 1;
			if (newCapacity < minCapacity)
				newCapacity = minCapacity;
			elementData = new int[newCapacity];
			worldStamps = new int[newCapacity];
			System.arraycopy(oldData, 0, elementData, 0, size.get());
			System.arraycopy(oldStamps, 0, worldStamps, 0, size.get());
		}
	}


	/**
	 * Adds a new search at the end of the vector.
	 *
	 * @param i The search to add.
	 */

	public void add(int i) {
		int newsize = size.get() + 1;
		ensureCapacity(newsize);
		size.set(newsize);
		elementData[newsize - 1] = i;
		worldStamps[newsize - 1] = environment.getWorldIndex();
	}

	/**
	 * Removes an int.
	 *
	 * @param i The search to remove.
	 */
	@Override
	public void remove(int i) {
		System.arraycopy(elementData, i, elementData, i+1, size.get());
		System.arraycopy(worldStamps, i, worldStamps, i+1, size.get());

		//        for(int j = i; j < size.get()-1; j++){
		//            elementData[j] = elementData[j+1];
		//            worldStamps[j] = worldStamps[j+1];
		//        }
		int newsize = size.get() - 1;
		if (newsize >= 0)
			size.set(newsize);
	}

	/**
	 * removes the search at the end of the vector.
	 * does nothing when called on an empty vector
	 */

	public void removeLast() {
		int newsize = size.get() - 1;
		if (newsize >= 0)
			size.set(newsize);
	}

	/**
	 * Returns the <code>index</code>th element of the vector.
	 */

	public int get(int index) {
		if (rangeCheck(index)) {
			return elementData[index];
		}                                                          
		throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size.get());
	}

    public final int quickGet(int index)
    {
        assert(rangeCheck(index));
    	return elementData[index] ;
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
		if (rangeCheck(index)) {
			//<hca> je vire cet assert en cas de postCut il n est pas vrai ok ?
			//assert(this.worldStamps[index] <= environment.getWorldIndex());
			return quickSet(index, val);
		}
		throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size.get());
	}

    @Override
    public final int quickSet(int index, int val) {
    	assert(rangeCheck(index));
    	final int oldValue = elementData[index];
			if (val != oldValue) {
				final int oldStamp = this.worldStamps[index];
				if (oldStamp < environment.getWorldIndex()) {
					myTrail.savePreviousState(this, index, oldValue, oldStamp);
					worldStamps[index] = environment.getWorldIndex();
				}
				elementData[index] = val;
			}
			return oldValue;
    }


    /**
	 * Sets an element without storing the previous value.
	 */

    public int _set(int index, int val, int stamp) {
    	assert(rangeCheck(index));
    	int oldval = elementData[index];
		elementData[index] = val;
		worldStamps[index] = stamp;
		return oldval;
	}


	@Override
	public DisposableIntIterator getIterator() {
		throw new UnsupportedOperationException("not yet implemented");
	}


}
