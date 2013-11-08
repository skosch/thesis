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


import choco.kernel.memory.*;
import choco.kernel.memory.trailing.trail.*;

/**
 * The root class for managing memory and sessions.
 * <p/>
 * A environment is associated to each problem.
 * It is responsible for managing backtrackable data.
 */
public final class EnvironmentTrailing extends AbstractEnvironment {


	/**
	 * The maximum numbers of worlds that a
	 * {@link ITrailStorage} can handle.
	 */
	private int maxWorld = 100; //1000;

	/**
	 * The maximum numbers of updates that a
	 * {@link ITrailStorage} can handle.
	 */
	private static final int MaxHist = 5000;

	//Contains all the {@link ITrailStorage} trails for
	// storing different kinds of data.
	private StoredIntTrail intTrail;
    private StoredBoolTrail boolTrail;
    private StoredVectorTrail vectorTrail;
    private StoredLongTrail longTrail;
    private StoredIntVectorTrail intVectorTrail;
    private StoredDoubleVectorTrail doubleVectorTrail;
    private StoredDoubleTrail doubleTrail;
    private StoredBinaryTreeTrail btreeTrail;
    private StoredLongVectorTrail longVectorTrail;

	/**
	 * Contains all the {@link ITrailStorage} trails for
	 * storing different kinds of data.
	 */
	private ITrailStorage[] trails;
    private int trailSize;

	/**
	 * Constructs a new <code>IEnvironment</code> with
	 * the default stack sizes : 50000 and 1000.
	 */

	public EnvironmentTrailing() {
		trails = new ITrailStorage[0];
        trailSize = 0;
	}

	@Override
	public void worldPush() {
        final int wi = currentWorld + 1;
        for (int i = 0; i < trailSize; i++) {
            trails[i].worldPush(wi);
        }
        currentWorld++;
        if (wi == maxWorld - 1) {
            resizeWorldCapacity(maxWorld * 3 / 2);
        }
	}


	@Override
	public void worldPop() {
		final int wi = currentWorld;
        for (int i = trailSize-1; i >= 0; i--) {
            trails[i].worldPop(wi);
        }
        currentWorld--;
	}

	@Override
	public void worldCommit() {
		if (currentWorld == 0) {
			throw new IllegalStateException("Commit in world 0?");
		}
		final int wi = currentWorld;
        for (int i = trailSize-1; i >= 0; i--) {
            trails[i].worldCommit(wi);
        }
		currentWorld--;
	}

    @Override
    public void clear() {
        for (int i = trailSize-1; i >= 0; i--) {
            trails[i].clear();
        }
    }

    @Override
	public IStateInt makeInt() {
		return makeInt(0);
	}

	@Override
	public IStateInt makeInt(final int initialValue) {
		return new StoredInt(this, initialValue);
	}

	@Override
	public IStateInt makeIntProcedure(final IStateIntProcedure procedure,
			final int initialValue) {
		return new StoredIntProcedure(this, procedure, initialValue);
	}

	@Override
	public IStateBool makeBool(final boolean initialValue) {
		return new StoredBool(this, initialValue);
	}

	@Override
	public IStateIntVector makeIntVector() {
		return new StoredIntVector(this);
	}

	@Override
	public IStateIntVector makeIntVector(final int size, final int initialValue) {
		return new StoredIntVector(this, size, initialValue);
	}

	@Override
	public IStateIntVector makeIntVector(final int[] entries) {
		return new StoredIntVector(this, entries);
	}
	@Override
	public IStateLongVector makeLongVector() {
		return new StoredLongVector(this);
	}

	@Override
	public IStateLongVector makeLongVector(final int size, final long initialValue) {
		return new StoredLongVector(this, size, initialValue);
	}

	@Override
	public IStateLongVector makeLongVector(final long[] entries) {
		return new StoredLongVector(this, entries);
	}


	@Override
	public IStateDoubleVector makeDoubleVector() {
		return new StoredDoubleVector(this);
	}

	@Override
	public IStateDoubleVector makeDoubleVector(final int size, final double initialValue) {
		return new StoredDoubleVector(this, size, initialValue);
	}

	@Override
	public IStateDoubleVector makeDoubleVector(final double[] entries) {
		return new StoredDoubleVector(this, entries);
	}

	@Override
	public <T> IStateVector<T> makeVector() {
		return new StoredVector<T>(this);
	}

	//    @Override
	//	public AbstractStateBitSet makeBitSet(int size) {
	//		return new StoredBitSet(this, size);
	//	}

	@Override
	public IStateDouble makeFloat() {
		return makeFloat(Double.NaN);
	}

	@Override
	public IStateDouble makeFloat(final double initialValue) {
		return new StoredDouble(this, initialValue);
	}

	@Override
	public IStateBinaryTree makeBinaryTree(final int inf, final int sup) {
		return new StoredBinaryTree(this, inf, sup);
	}

	@Override
	public IStateLong makeLong() {
		return makeLong(0);
	}

	@Override
	public IStateLong makeLong(final int init) {
		return new StoredLong(this,init);
	}

	@Override
	public IStateObject makeObject(final Object obj) {
		throw (new UnsupportedOperationException());
	}

	public int getTrailSize() {
		int s = 0;
		for (int i = 0; i < trailSize; i++) {
            s+=trails[i].getSize();
        }
		return s;
	}

	private void resizeWorldCapacity(final int newWorldCapacity) {
		for (int i = 0; i < trailSize; i++) {
            trails[i].resizeWorldCapacity(newWorldCapacity);
        }
		maxWorld = newWorldCapacity;
	}

    //****************************************************************************************************************//
    //************************************* TRAIL GETTER *************************************************************//
    //****************************************************************************************************************//

    private void increaseTrail() {
        ITrailStorage[] tmp = trails;
        trails = new ITrailStorage[tmp.length + 1];
        System.arraycopy(tmp, 0, trails, 0, tmp.length);
    }

    protected StoredIntTrail getIntTrail() {
        if (intTrail == null) {
            intTrail = new StoredIntTrail(MaxHist, maxWorld);
            increaseTrail();
            trails[trailSize++] = intTrail;
        }
        return intTrail;
    }

    protected StoredLongTrail getLongTrail() {
        if (longTrail == null) {
            longTrail = new StoredLongTrail(MaxHist, maxWorld);
            increaseTrail();
            trails[trailSize++] = longTrail;
        }
        return longTrail;
    }

    protected StoredBoolTrail getBoolTrail() {
        if (boolTrail == null) {
            boolTrail = new StoredBoolTrail(MaxHist, maxWorld);
            increaseTrail();
            trails[trailSize++] = boolTrail;
        }
        return boolTrail;
    }

    protected StoredDoubleTrail getDoubleTrail() {
        if (doubleTrail == null) {
            doubleTrail = new StoredDoubleTrail(MaxHist, maxWorld);
            increaseTrail();
            trails[trailSize++] = doubleTrail;
        }
        return doubleTrail;
    }

    protected StoredVectorTrail getVectorTrail() {
        if (vectorTrail == null) {
            vectorTrail = new StoredVectorTrail(MaxHist, maxWorld);
            increaseTrail();
            trails[trailSize++] = vectorTrail;
        }
        return vectorTrail;
    }

    protected StoredIntVectorTrail getIntVectorTrail() {
        if (intVectorTrail == null) {
            intVectorTrail = new StoredIntVectorTrail(MaxHist, maxWorld);
            increaseTrail();
            trails[trailSize++] = intVectorTrail;
        }
        return intVectorTrail;
    }

    protected StoredDoubleVectorTrail getDoubleVectorTrail() {
        if (doubleVectorTrail == null) {
            doubleVectorTrail = new StoredDoubleVectorTrail(MaxHist, maxWorld);
            increaseTrail();
            trails[trailSize++] = doubleVectorTrail;
        }
        return doubleVectorTrail;
    }

    protected StoredLongVectorTrail getLongVectorTrail() {
        if (longVectorTrail == null) {
            longVectorTrail = new StoredLongVectorTrail(MaxHist, maxWorld);
            increaseTrail();
            trails[trailSize++] = longVectorTrail;
        }
        return longVectorTrail;
    }


    public StoredBinaryTreeTrail getBinaryTreeTrail() {
        if (btreeTrail == null) {
            btreeTrail = new StoredBinaryTreeTrail(MaxHist, maxWorld);
            increaseTrail();
            trails[trailSize++] = btreeTrail;
        }
        return btreeTrail;
    }
}

