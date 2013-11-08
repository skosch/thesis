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

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.memory.structure.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

public interface IEnvironment {
   
	/**
	 * Reference to an object for logging trace statements related memory & backtrack (using the java.util.logging package)
	 */
	final static Logger LOGGER = ChocoLogging.getEngineLogger();

    /**
     * Returns the world number.
     *
     * @return current world index
     */

    int getWorldIndex();

//    /**
//     * Returns the time stamp of the world (only change during worldPush.
//     * @return
//     */
//    long getWorldTimeStamp();

    /**
     * Starts a new branch in the search tree.
     */

    void worldPush();

    /**
     * Backtracks to the previous choice point in the search tree.
     */
    void worldPop();


    /**
	 * Comitting the current world: merging it with the previous one.
	 * <p/>
	 * Not used yet.
	 */
    void worldCommit();

    /**
     * Clear the entire internal structures
     */
    void clear();

    /**
     * Factory pattern: new IStateInt objects are created by the environment
     * (no initial value is assigned to the backtrackable search)
     * @return new IStateInt computed by the environment
     */

    IStateInt makeInt();

    /**
     * Factory pattern: new IStateInt objects are created by the environment
     *
     * @param initialValue the initial value of the backtrackable integer
     * @return new IStateInt computed by the environment
     */

    IStateInt makeInt(int initialValue);

    /**
     * Factory pattern : new IntInterval objects are created by the environment
     * @param lowB intitial lower bound
     * @param upB intial upper bound
     * @return new IntInterval computed by the environment
     */
    IntInterval makeIntInterval(int lowB, int upB);

    /**
     * Factory pattern : new IStateInt with procedure objects are created by the environment
     * @param procedure the procedure to apply
     * @param initialValue the intial value of the integer
     * @return IStateInt with procedure
     */
    IStateInt makeIntProcedure(IStateIntProcedure procedure, int initialValue);

    /**
     * Factory pattern: new IStateBool objects are created by the environment
     *
     * @param initialValue the initial value of the backtrackable boolean
     * @return Boolean object created by the environment
     */

    IStateBool makeBool(boolean initialValue);

    /**
     * Factory pattern: new IStateIntVector objects are created by the environment.
     * Creates an empty vector
     * @return IStateIntVector
     */

    IStateIntVector makeIntVector();

    /**
     * Factory pattern: new IStateIntVector objects are created by the environment
     *
     * @param size         the number of entries in the vector
     * @param initialValue the common initial value for all entries (backtrackable integers)
     * @return IStateIntVector
     */

    IStateIntVector makeIntVector(int size, int initialValue);

    /**
     * Factory pattern: new IStateIntVector objects are created by the environment
     *
     * @param entries an array to be copied as set of initial contents of the vector
     * @return IStateIntVector
     */

    IStateIntVector makeIntVector(int[] entries);


    /**
     * Factory pattern: new IStateIntVector objects are created by the environment.
     * Creates an empty vector
     * @return IStateIntVector
     */

    IStateLongVector makeLongVector();

    /**
     * Factory pattern: new IStateIntVector objects are created by the environment
     *
     * @param size         the number of entries in the vector
     * @param initialValue the common initial value for all entries (backtrackable integers)
     * @return IStateIntVector
     */

    IStateLongVector makeLongVector(int size, long initialValue);

    /**
     * Factory pattern: new IStateIntVector objects are created by the environment
     *
     * @param entries an array to be copied as set of initial contents of the vector
     * @return IStateIntVector
     */

    IStateLongVector makeLongVector(long[] entries);

    /**
     * Factory pattern: new IStateDoubleVector objects are created by the environment.
     * Creates an empty vector
     * @return IStateDoubleVector
     */

    IStateDoubleVector makeDoubleVector();

    /**
     * Factory pattern: new IStateDoubleVector objects are created by the environment
     *
     * @param size         the number of entries in the vector
     * @param initialValue the common initial value for all entries (backtrackable integers)
     * @return IStateDoubleVector
     */

    IStateDoubleVector makeDoubleVector(int size, double initialValue);

    /**
     * Factory pattern: new IStateDoubleVector objects are created by the environment
     *
     * @param entries an array to be copied as set of initial contents of the vector
     * @return IStateDoubleVector
     */

    IStateDoubleVector makeDoubleVector(double[] entries);

    /**
     * Factory pattern: new IStateVector objects are created by the environment.
     * Creates an empty vector
     * @return IStateIntVector
     */
    <T> IStateVector<T> makeVector();

    /**
     * Factory pattern : create a new partially stored vector via the environment.
     * @param <T> object to store
     * @return PartiallyStoredVector
     */
    <T> PartiallyStoredVector<T> makePartiallyStoredVector();

    /**
     * Factory pattern : create a new partially stored int vector via the environment.
     * @return PartiallyStoredVector
     */
    PartiallyStoredIntVector makePartiallyStoredIntVector();

    /**
     * Factory pattern: new IStateBitSet objects are created by the environment
     *
     * @param size initail size of the IStateBitSet
     * @return IStateBitSet
     */
    IStateBitSet makeBitSet(int size);


    /**
     * Build a shared bipartite set
     * @param size size of the bi partite set
     */
    void createSharedBipartiteSet(int size);

    /**
     * Factory pattern : shared IStateIntVector object is return by the environment
     * @return IStateIntVector
     */
    IStateIntVector getSharedBipartiteSetForBooleanVars();

    /**
     * Increase the size of the shared bi partite set,
     * it HAS to be called before the end of the environment creation
     * BEWARE: be sure you are correctly calling this method
     * @param gap the gap the reach the expected size
     */
    void increaseSizeOfSharedBipartiteSet(int gap);

    /**
     * Return the next free bit in the shared StoredBitSetVector object
     * @return
     */
    int getNextOffset();

    <E> StoredBipartiteSet makeStoredBipartiteList(Collection<E> coll);

    <E> StoredBipartiteSet makeStoredBipartiteList(E[] elm);

    IStateIntVector makeBipartiteIntList(int[] entries);

    IStateIntVector makeBipartiteSet(int[] entries);

    IStateIntVector makeBipartiteSet(int nbEntries);

    IStateIntVector makeBipartiteSet(IndexedObject[] entries);

    IStateIntVector makeBipartiteSet(ArrayList<IndexedObject> entries);

    /**
     * Factory pattern: new StoredFloat objects are created by the environment
     * (no initial value is assigned to the backtrackable search)
     */

    IStateDouble makeFloat();

    public IStateLong makeLong();

    public IStateLong makeLong(int init);


    /**
     * Factory pattern: new StoredFloat objects are created by the environment
     *
     * @param initialValue the initial value of the backtrackable search
     */

    IStateDouble makeFloat(double initialValue);



    IStateBinaryTree makeBinaryTree(int inf, int sup);

    IStateObject makeObject(Object obj);

}
