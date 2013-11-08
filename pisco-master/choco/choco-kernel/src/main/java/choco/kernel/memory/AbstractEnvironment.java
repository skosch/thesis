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

package choco.kernel.memory;

import choco.kernel.memory.structure.*;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Super class of all environments !
 */
public abstract class AbstractEnvironment implements IEnvironment {
    /**
     * The current world index.
     */

    protected int currentWorld = 0;

  

    private static final int SIZE = 128;

    /**
     * Shared BitSet
     */
    public IStateIntVector currentBitSet;
    /**
     * Nex free bit in the shared BitSet
     */
    protected int nextOffset;

    public final int getWorldIndex() {
        return currentWorld;
    }

 
    public final void createSharedBipartiteSet(int size){
        currentBitSet = makeBipartiteSet(size);
        nextOffset = -1;
    }

    /**
     * Factory pattern : shared StoredBitSetVector objects is return by the environment
     *
     * @return
     */
    @Override
    public final IStateIntVector getSharedBipartiteSetForBooleanVars() {
        if(currentBitSet == null){
            createSharedBipartiteSet(SIZE);
        }
        nextOffset++;
        if(nextOffset > currentBitSet.size()-1){
//             increaseSizeOfSharedBipartiteSet(currentBitSet.size()); // double the size of the current bitset
             increaseSizeOfSharedBipartiteSet(currentBitSet.size() + 1);
        }
        return currentBitSet;
    }

    /**
     * Return the next free bit in the shared StoredBitSetVector object
     *
     * @return
     */
    @Override
    public final int getNextOffset() {
        return nextOffset;
    }

    @SuppressWarnings({"unchecked"})
    public <E> StoredBipartiteSet makeStoredBipartiteList(Collection<E> coll){
        return new StoredBipartiteSet(this, coll);
    }

    @SuppressWarnings({"unchecked"})
    public <E> StoredBipartiteSet makeStoredBipartiteList(E[] elm){
        return new StoredBipartiteSet(this, elm);
    }

    public IStateIntVector makeBipartiteIntList(int[] entries) {
		return new StoredIntBipartiteList(this,entries);
	}

    public IStateIntVector makeBipartiteSet(int[] entries) {
		return new StoredIndexedBipartiteSet(this,entries);
	}

	public IStateIntVector makeBipartiteSet(int nbEntries) {
		return new StoredIndexedBipartiteSet(this,nbEntries);
	}

	public IStateIntVector makeBipartiteSet(IndexedObject[] entries) {
		return new StoredIndexedBipartiteSet(this,entries);
	}

	public IStateIntVector makeBipartiteSet(ArrayList<IndexedObject> entries) {
		return new StoredIndexedBipartiteSet(this,entries);
	}

    public <T> PartiallyStoredVector<T> makePartiallyStoredVector() {
        return new PartiallyStoredVector<T>(this);
    }

    public PartiallyStoredIntVector makePartiallyStoredIntVector() {
        return new PartiallyStoredIntVector(this);
    }

    public IntInterval makeIntInterval(int inf, int sup){
        return new IntInterval(this, inf, sup);
    }

    /**
     * Factory pattern: new IStateBitSet objects are created by the environment
     *
     * @param size initail size of the IStateBitSet
     * @return IStateBitSet
     */
    @Override
    public IStateBitSet makeBitSet(int size) {
        return new SBitSet(this, size);
    }

    /**
	 * Increase the size of the shared bi partite set,
	 * it HAS to be called before the end of the environment creation
	 * BEWARE: be sure you are correctly calling this method
	 *
	 * @param gap the gap the reach the expected size
	 */
	@Override
	public void increaseSizeOfSharedBipartiteSet(int gap) {
		((StoredIndexedBipartiteSet)currentBitSet).increaseSize(gap);
	}

}
