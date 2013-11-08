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

package choco.kernel.common.opres.pack;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;

/**
 * Compute lower bounds for one-dimensional bin packing problems using MDFF (F_0^k, f_{CCM,1}^k.
 *  @see Clautiaux, F., Alves, C. & Valério de Carvalho, J. </br>
 *  <i>A survey of dual-feasible and superadditive functions</i> </br>
 *   Annals of Operations Research, Springer Netherlands, 2010, Vol. 179, pp. 317-342
 * @author Arnaud Malapert
 *
 */
public final class LowerBoundFactory {

	/**
	 * empty constructor
	 */
	private LowerBoundFactory() {}

	

	//*****************************************************************//
	//*******************  Multithread MDFF  *************************//
	//***************************************************************//

	/**
	 * Compute the lower bound L0 
	 *
	 * @param sizes items (not sorted) 
	 * @param capacity the capacity of bins
	 * 
	 * @return a Lower bound on the number of bins
	 */
	public static int computeL0(final TIntArrayList items, int capacity) {
		final ComputeL0 l0 = new ComputeL0(capacity);
		items.forEachDescending(l0);
		return l0.getLowerBound();
	}
	/**
	 * Compute a lower bound based on F_0^k.
	 *
	 * @param sizes items sorted according non decreasing sizes 
	 * @param capacity the capacity of bins
	 * @param upperBound an upper bound on the number of bins (can speed up the computation)
	 *
	 * @return a Lower bound on the number of bins
	 */
	public static final int computeF0(final TIntArrayList items, int capacity, int upperBound) {
		return computeMDDFF(items, capacity, upperBound, new FindParametersF0(), new FunctionF0());
	}

	/**
	 * Compute a lower bound based on F_{CCM,1}^k.
	 *
	 * @param sizes items sorted according non decreasing sizes 
	 * @param capacity the capacity of bins
	 * @param upperBound an upper bound on the number of bins (can speed up the computation)
	 *
	 * @return a Lower bound on the number of bins
	 */
	public static final int computeFCCM1(final TIntArrayList items, int capacity, int upperBound) {
		return computeMDDFF(items, capacity, upperBound, new FindParametersFCCM1(), new FunctionFCCM1());
	}
	
	/**
	 * Compute a lower bound based on F_0^k and F_{CCM,1}^k.
	 *
	 * @param sizes items sorted according non decreasing sizes 
	 * @param capacity the capacity of bins
	 * @param upperBound an upper bound on the number of bins (can speed up the computation)
	 *
	 * @return a Lower bound on the number of bins
	 */
	public static final int  computeAllMDFF(final TIntArrayList items, int capacity, int upperBound) {
		int lb = computeF0(items, capacity, upperBound);
		return lb >= upperBound ? 
				lb : Math.max(lb, computeFCCM1(items, capacity, upperBound));
	}

	protected static final int computeMDDFF(final TIntArrayList items, int capacity, int upperBound, AbstractFindParameters findParameters, AbstractFunctionDFF functionDDFF) {
		assert checkIncreasingOrder(items);
		return computeMDDFF(items, items.toNativeArray(), capacity, upperBound, new ComputeL0(), findParameters, functionDDFF);
	}

	private static final int computeMDDFF(final TIntArrayList items, int[] storedItems, int capacity, int upperBound,
			ComputeL0 computeL0, AbstractFindParameters findParameters, AbstractFunctionDFF functionDFF) {
		int lowerBound = 0;
		findParameters.clearParameters();
		findParameters.setCapacity(capacity);
		items.forEachDescending(findParameters);
		final TIntHashSet parameters = findParameters.getParameters();
		if( ! parameters.isEmpty() ) {
			functionDFF.setCapacity(capacity);
			final TIntArrayList transformedItems = new TIntArrayList(items.size());
			final TIntIterator iter = parameters.iterator();
			while(iter.hasNext()) {
				functionDFF.setParameter(iter.next());
				transformedItems.resetQuick();
				transformedItems.add(storedItems);
				transformedItems.transformValues(functionDFF);
				computeL0.reset();
				computeL0.setCapacity(functionDFF.transformCapacity());
				transformedItems.forEachDescending(computeL0);
				if(lowerBound < computeL0.getLowerBound()) {
					lowerBound = computeL0.getLowerBound();
					if(lowerBound >= upperBound) return lowerBound;
				}
			}
		}
		return lowerBound;
	}

	//*****************************************************************//
	//*******************  Mono-thread MDFF  *************************//
	//***************************************************************//
	// Reuse the static fields 

	private final static ComputeL0 COMPUTE_L0 = new ComputeL0();

	private final static AbstractFindParameters FIND_PARAMETERS_F0 = new FindParametersF0();

	private final static AbstractFunctionDFF FUNCTION_F0 = new FunctionF0();

	private final static AbstractFindParameters FIND_PARAMETERS_FCCM1 = new FindParametersFCCM1();

	private final static AbstractFunctionDFF FUNCTION_FCCM1 = new FunctionFCCM1();

	/**
	 * Compute the lower bound L0 (not thread safe)
	 *
	 * @param sizes items (not sorted) 
	 * @param capacity the capacity of bins
	 * 
	 * @return a Lower bound on the number of bins
	 */
	public static int memComputeL0(final TIntArrayList items, int capacity) {
		COMPUTE_L0.reset();
		COMPUTE_L0.setCapacity(capacity);
		items.forEachDescending(COMPUTE_L0);
		return COMPUTE_L0.getLowerBound();
	}
	
	/**
	 * Compute a lower bound based on F_0^k (not thread safe)
	 *
	 * @param sizes items sorted according non decreasing sizes 
	 * @param capacity the capacity of bins
	 * @param upperBound an upper bound on the number of bins (can speed up the computation)
	 *
	 * @return a Lower bound on the number of bins
	 */
	public static int memComputeF0(final TIntArrayList items, int capacity, int upperBound) {
		assert checkIncreasingOrder(items);
		return computeMDDFF(items, items.toNativeArray(), capacity, upperBound, COMPUTE_L0, FIND_PARAMETERS_F0, FUNCTION_F0);
	}


	/**
	 * Compute a lower bound based on F_{CCM,1}^k (not thread safe)
	 *
	 * @param sizes items sorted according non decreasing sizes 
	 * @param capacity the capacity of bins
	 * @param upperBound an upper bound on the number of bins (can speed up the computation)
	 *
	 * @return a Lower bound on the number of bins
	 */
	public static int memComputeFCCM1(final TIntArrayList items, int capacity, int upperBound) {
		assert checkIncreasingOrder(items);
		return computeMDDFF(items, items.toNativeArray(), capacity, upperBound, COMPUTE_L0, FIND_PARAMETERS_FCCM1, FUNCTION_FCCM1);
	}

	/**
	 * Compute a lower bound based on F_0^k and F_{CCM,1}^k (not thread safe)
	 *
	 * @param sizes items sorted according non decreasing sizes 
	 * @param capacity the capacity of bins
	 * @param upperBound an upper bound on the number of bins (can speed up the computation)
	 *
	 * @return a Lower bound on the number of bins
	 */
	public static int memComputeAllMDFF(final TIntArrayList items, int capacity, int upperBound) {
		assert checkIncreasingOrder(items);
		final int[] tab = items.toNativeArray();
		int lb = computeMDDFF(items, tab, capacity, upperBound, COMPUTE_L0, FIND_PARAMETERS_F0, FUNCTION_F0);
		return lb >= upperBound ? 
				lb : Math.max(lb, computeMDDFF(items, tab, capacity, upperBound, COMPUTE_L0, FIND_PARAMETERS_FCCM1, FUNCTION_FCCM1));
	}


	private final static FirstFit1BP FIRST_FIT = new FirstFit1BP();

	private final static BestFit1BP BEST_FIT = new BestFit1BP();

	/**
	 * Apply a consistency test a one-dimensional bin packing limited to a maximum number of bins.
	 *
	 * @param sizes sizes of items (not sorted)
	 * @param capacity the capacity of bins
	 * @param maxNumberOfBins maximum number of bins 
	 *
	 * @return <code>false</code> if the lower bound is strictly greater than the maxumum number of bins
	 */
	public static boolean  testPackingConsistencyWithMDFF(TIntArrayList sizes,final int capacity, int maxNumberOfBins) {
		sizes.sort();
		final int ub1 = FIRST_FIT.executeQuick(sizes, capacity);
		if (ub1 > maxNumberOfBins) {
			final int ub2 = BEST_FIT.executeQuick(sizes, capacity);
			if (ub2 > maxNumberOfBins) {
				//the heuristics solutions are greater than the maximum number of bins.
				//so, what about the lower bound ?
				return memComputeFCCM1(sizes, capacity, ub1 > ub2 ? ub1 : ub2) <= maxNumberOfBins;
			}
		}//otherwise, heuristics give a number of bins lower than the maximum
		return true;
	}
	
	
	private static boolean checkIncreasingOrder(TIntArrayList items) {
		final int n = items.size();
		for (int i = 1; i < n; i++) {
			if(items.getQuick(i) < items.getQuick(i-1)) return false;
		}
		return true;
	}
}
