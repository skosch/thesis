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

import choco.kernel.memory.*;

import java.util.Stack;

import static choco.kernel.memory.copy.RecomputableElement.NB_TYPE;

public class EnvironmentCopying extends AbstractEnvironment {


    /**
	 * The current world number (should be less
	 * than <code>maxWorld</code>).
	 */

	private boolean newEl = false;

    protected final static Stack<Integer> clonedWorldIdxStack;
    public static RecomputableElement[][] elements;
    public static int[] indices;
	private static RcSave save;

	public int nbCopy = 0 ;


    static{
        elements = new RecomputableElement[NB_TYPE][64];
        indices = new int[NB_TYPE];
        clonedWorldIdxStack = new Stack<Integer>();
    }


	public EnvironmentCopying() {
        for(int i = NB_TYPE; --i>=0;)indices[i] = 0;
        clonedWorldIdxStack.clear();
        save = new RcSave(this);
	}

	public int getNbCopy() {
		return nbCopy;
	}

	public void add(RecomputableElement rc) {
        ensureCapacity(rc.getType(), indices[rc.getType()]+1);
		elements[rc.getType()][indices[rc.getType()]++] = rc;
		newEl = true;
	}

    private void ensureCapacity(int type, int n) {
        if (n > elements[type].length) {
          int newSize = elements[type].length;
          while (n >= newSize) {
              newSize = (3 * newSize) / 2;
          }
          RecomputableElement[] oldElements = elements[type];
          elements[type] = new RecomputableElement[newSize];
          System.arraycopy(oldElements, 0, elements[type], 0, oldElements.length);
      }
    }

    @Override
	public void worldPush() {
		if (newEl) {
			save.currentElement = new RecomputableElement[NB_TYPE][];
			for (int i = NB_TYPE ; --i>=0;) {
				save.currentElement[i] = new RecomputableElement[indices[i]];
                System.arraycopy(elements[i], 0, save.currentElement[i], 0, indices[i]);
			}
			newEl = false;
		}
		this.saveEnv();
		currentWorld++;
    }

	private void saveEnv() {
		if (!(currentWorld != 0 && currentWorld == clonedWorldIdxStack.peek())) {

			nbCopy++;

			if (clonedWorldIdxStack.empty())
				clonedWorldIdxStack.push(currentWorld);
			else if (clonedWorldIdxStack.peek() < currentWorld)
				clonedWorldIdxStack.push(currentWorld);

			save.save(currentWorld);
		}
	}

    @Override
	public void worldPop() {
		save.restore(--currentWorld);
		clonedWorldIdxStack.pop();
	}

    @Override
    public void clear() {
        for(int i = NB_TYPE; --i>=0;){
            indices[i] = 0;
        }
        clonedWorldIdxStack.clear();
        save.clear();
    }

    @Override
	public void worldCommit() {
		//TODO
		throw (new UnsupportedOperationException());
	}

    @Override
	public IStateInt makeInt() {
		return new RcInt(this);
	}

    @Override
	public IStateInt makeInt(int initialValue) {
		return new RcInt(this,initialValue);
	}

    @Override
	public IStateInt makeIntProcedure(IStateIntProcedure procedure,
			int initialValue) {
		return new RcIntProcedure(this, procedure, initialValue);
	}

    @Override
	public IStateBool makeBool(boolean initialValue) {
		return new RcBool(this,initialValue);
	}


        @Override
	public IStateIntVector makeIntVector() {
		return new RcIntVector(this);
	}



    @Override
	public IStateIntVector makeIntVector(int size, int initialValue) {
		return new RcIntVector(this,size,initialValue);
	}

    @Override
	public IStateIntVector makeIntVector(int[] entries) {
		return new RcIntVector(this,entries);
	}
 @Override
	public IStateLongVector makeLongVector(int size, long initialValue) {
		return new RcLongVector(this,size,initialValue);
	}

    @Override
	public IStateLongVector makeLongVector(long[] entries) {
		return new RcLongVector(this,entries);
	}

       @Override
	public IStateLongVector makeLongVector() {
		return new RcLongVector(this);
	}

    @Override
    public IStateDoubleVector makeDoubleVector() {
        return new RcDoubleVector(this);
    }

    @Override
    public IStateDoubleVector makeDoubleVector(int size, double initialValue) {
        return new RcDoubleVector(this,size,initialValue);
    }

    @Override
    public IStateDoubleVector makeDoubleVector(double[] entries) {
        return new RcDoubleVector(this,entries);
    }

//    @Override
//	public IStateBitSet makeBitSet(int size) {
//		return new RcBitSet(this,size);
//	}

    @Override
	public IStateDouble makeFloat() {
		return new RcDouble(this);
	}

    @Override
	public IStateDouble makeFloat(double initialValue) {
		return new RcDouble(this, initialValue);
	}

    @Override
	public IStateLong makeLong() {
		return new RcLong(this);
	}

    @Override
	public IStateLong makeLong(int init) {
		return new RcLong(this, init);
	}

    @Override
	public <T> IStateVector<T> makeVector() {
		return new RcVector<T>(this);
	}

    @Override
	public IStateBinaryTree makeBinaryTree(int inf, int sup) {
        return null;
	}

    @Override
    public IStateObject makeObject(Object obj){
        return new RcObject(this, obj);
    }
}

