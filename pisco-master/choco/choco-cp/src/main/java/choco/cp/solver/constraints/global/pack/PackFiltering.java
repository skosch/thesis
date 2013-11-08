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

package choco.cp.solver.constraints.global.pack;

import java.awt.Point;
import java.util.ListIterator;

import choco.kernel.common.opres.nosum.INoSumCell;
import choco.kernel.common.opres.nosum.NoSumList;
import choco.kernel.common.util.bitmask.BitMask;
import choco.kernel.memory.IStateIntVector;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.integer.IntDomainVar;


/**
 * The Class {@link PackFiltering} is implements filtering rules for one-dimensional bin packing constraint.
 *In fact, all variables are not given. So, the constraint used an interface {@link IPackSConstraint} to get informations.
 *
 * </br> This class is a global constraint inspired from the 1BP constraint proposed by
 * [1].</br>
 * <tr valign="top">
 * <td align="right"> [<a name="shaw-04">1</a>] </td>
 *
 * <td> Paul Shaw. A constraint for bin packing. In Mark Wallace, editor,
 * <em>Principles and Practice of Constraint
 * Programming - CP 2004, 10th International Conference, CP 2004, Toronto,
 * Canada, September 27 - October 1, 2004, Proceedings</em>,
 * volume 3258 of <em>
 * Lecture Notes in Computer Science</em>, pages 648-662.
 * Springer, 2004. [&nbsp;<a
 * href="http://springerlink.metapress.com/openurl.asp?genre=article&amp;issn=0302-9743&amp;volume=3258&amp;spage=648">http</a>&nbsp;]
 * </td>
 * </tr>
 *
 * @author Arnaud Malapert
 * @since 2.0.0
 * @version 2.0.1
 */
public final class PackFiltering {

	public final IPackSConstraint cstr;

	protected final BitMask flags;

	/** The sizes of the items. */
	protected final int[] sizes;

	/** The loads of the bins. */
	protected final IntDomainVar[] loads;

	//general propagation info

	/** information about a given bin. */
	private NoSumList reuseStatus;

	/** The no fix point. */
	private boolean noFixPoint;

	protected final SumDataStruct loadSum;


	// TODO - protected SumDataStruct cardSum; implémenter les règles -Arnaud Malapert - 4 juil. 2011

	// TODO -  affiner les réveil - Arnaud Malapert - 4 juil. 2011
	// TODO - getRemainingSpace() : ne pas recalculer - Arnaud Malapert - 4 juil. 2011
	/**
	 * Instantiates a new 1BP constraint.
	 * @param environment
	 */
	public PackFiltering(IPackSConstraint cstr, BitMask flags) {
		this.cstr = cstr;
		this.sizes = cstr.getSizes();
		this.loads = cstr.getLoads();
		loadSum = new SumDataStruct(loads,computeTotalSize());
		this.flags = flags;
	}

	/**
	 * Compute the total size and check that sizes are constant.
	 *
	 */
	private long computeTotalSize() {
		long l=0;
		int last=Integer.MAX_VALUE;
		for (int i = 0; i < sizes.length; i++) {
			if(sizes[i]>last) {
				// TODO - allow non sorted items - Arnaud Malapert - 4 juil. 2011
				throw new SolverException("size must be sorted according to non increasing order");
			} else {
				l+=sizes[i];
				last=sizes[i];
			}
		}
		return l;
	}





	/**
	 * Update the minimal load of a given bin.
	 *
	 * @param bin the index of bin
	 * @param load the new load
	 *
	 * @throws ContradictionException the contradiction exception
	 */
	protected final void updateInfLoad(final int bin,final int load) throws ContradictionException {
		noFixPoint |= cstr.updateInfLoad(bin, load);
	}


	/**
	 * Update the maximal load of a given bin.
	 *
	 * @param bin the index of bin
	 * @param load the new load
	 *
	 * @throws ContradictionException the contradiction exception
	 */
	protected final void updateSupLoad(final int bin,final int load) throws ContradictionException {
		noFixPoint |= cstr.updateSupLoad(bin, load);
	}

	/**
	 * Do not update status
	 */
	protected final void pack(final int item,final int bin) throws ContradictionException {
		noFixPoint |= cstr.pack(item, bin);
	}


	/**
	 * Do not update status
	 */
	protected final void remove(final int item,final int bin) throws ContradictionException {
		noFixPoint |=  cstr.remove(item, bin);
	}


	//	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%//
	//	%%%%%%%%%%%%%%%%%%%%%%%%%% TYPICAL MODEL %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%//
	//	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%//



	/**
	 * The minimum and maximum load of each bin {@link PackFiltering#loads } is maintained according to the domains of the bin assignment variables.
	 *
	 * @param bin the bin
	 *
	 * @throws ContradictionException the contradiction exception
	 */
	protected final void loadAndSizeCoherence(final int bin) throws ContradictionException {
		final Point p = loadSum.getBounds(bin);
		updateInfLoad(bin, p.x);
		updateSupLoad(bin, p.y);
	}

	protected final void cardAndItemsCoherence(final int bin) throws ContradictionException {
		// TODO - cardAndItemsCoherence(int) - created 12 juil. 2011 by Arnaud Malapert
	}
	/**
	 * The minimum and maximum load of each bin {@link PackFiltering#loads } is maintained according to the domains of the bin assignment variables.
	 *
	 * @param bin the index of the bin
	 *
	 * @throws ContradictionException the contradiction exception
	 */
	protected final void loadMaintenance(final int bin) throws ContradictionException {
		updateInfLoad(bin,reuseStatus.getRequiredLoad());
		updateSupLoad(bin,reuseStatus.getMaximumLoad());
	}

	protected final void cardMaintenance(final int bin) throws ContradictionException {
		// TODO - cardMaintenance(int) - created 12 juil. 2011 by Arnaud Malapert
	}

	/**
	 * Single item elimination and commitment.
	 *
	 * @param bin the bin
	 *
	 * @throws ContradictionException the contradiction exception
	 */
	protected final void singleItemEliminationAndCommitment(final int bin) throws ContradictionException {
		final ListIterator<INoSumCell> iter = reuseStatus.listIterator();
		while(iter.hasNext()) {
			final int item = iter.next().getID();
			if(sizes[item] + reuseStatus.getRequiredLoad()>loads[bin].getSup()) {
				reuseStatus.remove(iter, item);
				remove(item, bin);
			}else if(reuseStatus.getMaximumLoad()-sizes[item]<loads[bin].getInf()) {
				reuseStatus.pack(iter, item);
				pack(item, bin);
			}
			// FIXME - Add break statement ? - Arnaud Malapert - 4 juil. 2011
			//			else {	
			//break;
			//			}
		}
	}

	/**
	 *
	 * @param bin the bin
	 *
	 * @throws ContradictionException the contradiction exception
	 */
	protected final void singleItemEliminationAndCommitmentAndFill(final int bin) throws ContradictionException {
		//Warning: bins must be equivalent ...
		final ListIterator<INoSumCell> iter = reuseStatus.listIterator();
		while(iter.hasNext()) {
			final int item = iter.next().getID();
			if(sizes[item] + reuseStatus.getRequiredLoad()>loads[bin].getSup()) {
				reuseStatus.remove(iter, item);
				remove(item, bin);
			}else if( reuseStatus.getMaximumLoad()-sizes[item] < loads[bin].getInf() ||
					reuseStatus.getRequiredLoad()+sizes[item] ==loads[bin].getSup() ) {
				reuseStatus.pack(iter, item);
				pack(item, bin);
			} 
			// TODO - Improve completion if a single item can fit into the bin - created 10 juil. 2011 by Arnaud Malapert
			// FIXME - Add break statement ? - Arnaud Malapert - 4 juil. 2011
			//			else {	
			//break;
			//			}
		}
	}




	//	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%//
	//	%%%%%%%%%%%%%%%%%%%%%%%%%% ADDITIONAL RULES %%%%%%%%%%%%%%%%%%%%%%%%%%%%//
	//	%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%//



	/**
	 * Feasibility test on the load of a given bin using no sum algorothm.
	 *
	 *
	 * @throws ContradictionException the contradiction exception
	 */
	protected final void noSumPruningRule(final NoSumList nosum,final int bin) throws ContradictionException {
		if(nosum.noSum(loads[bin].getInf()-reuseStatus.getRequiredLoad(),loads[bin].getSup()-reuseStatus.getRequiredLoad())) {
			cstr.fail();
		}
	}

	/**
	 * Update the load of a given bin with no sum algorithm
	 *
	 *
	 * @throws ContradictionException the contradiction exception
	 */
	protected final void noSumBinLoads(final NoSumList nosum,final int bin) throws ContradictionException {
		int value = loads[bin].getInf()-reuseStatus.getRequiredLoad();
		if(nosum.noSum(value, value) ) {
			updateInfLoad(bin, reuseStatus.getRequiredLoad()+value);
		}
		value = loads[bin].getSup()-reuseStatus.getRequiredLoad();
		if(nosum.noSum(value, value)) {
			updateSupLoad(bin,reuseStatus.getRequiredLoad()+ value);
		}
	}

	/**
	 * use no sum algorithm to pack into or remove from.
	 *
	 *
	 * @throws ContradictionException the contradiction exception
	 */
	protected final void noSumItemEliminationAndCommitment(final NoSumList nosum,final int bin) throws ContradictionException {
		final ListIterator<INoSumCell> iter = reuseStatus.listIterator();
		while(reuseStatus.getNbCandidates() > 1 && iter.hasNext()) {
			final int item = iter.next().getID();
			reuseStatus.remove(iter, item);
			if(nosum.noSum(loads[bin].getInf()-reuseStatus.getRequiredLoad()-sizes[item], loads[bin].getSup()-reuseStatus.getRequiredLoad()-sizes[item])) {
				remove(item, bin);
			}else if (nosum.noSum(loads[bin].getInf()-reuseStatus.getRequiredLoad(),loads[bin].getSup()-reuseStatus.getRequiredLoad())) {
				reuseStatus.packRemoved(item);
				pack(item, bin);
			}else {
				reuseStatus.undoRemove(iter, item);
			}
		}
	}




	//	****************************************************************//
	//	********* PROPAGATION LOOP *************************************//
	//	****************************************************************//



	public void propagate() throws ContradictionException {
		//CPSolver.flushLogs();
		final IStateIntVector abins = cstr.getAvailableBins();
		final int n = abins.size();
		noFixPoint=true;
		while(noFixPoint) {
			noFixPoint=false;
			loadSum.update();
			for (int i = 0; i < n ; i++) {
				propagate( abins.quickGet(i));
			}
		}
		cstr.fireAvailableBins(); 

	}

	/**
	 *
	 * @throws ContradictionException the contradiction exception
	 */
	private void propagate(final int bin) throws ContradictionException {
		loadAndSizeCoherence(bin);
		reuseStatus = cstr.getStatus(bin);
		loadMaintenance(bin);
		if(flags.contains(PackSConstraint.FILL_BIN)) {singleItemEliminationAndCommitmentAndFill(bin);}
		else {singleItemEliminationAndCommitment(bin);}
		if( flags.contains(PackSConstraint.ADDITIONAL_RULES) && reuseStatus.getNbCandidates() > 1) {
			noSumPruningRule(reuseStatus,bin);
			noSumBinLoads(reuseStatus,bin);
			noSumItemEliminationAndCommitment(reuseStatus, bin);
		}
	}



	static final class SumDataStruct {

		/** variables to sum */
		protected final IntDomainVar[] vars;

		/** the constant sum. */
		public final long sum;

		protected long sumMinusInfs;

		protected long sumMinusSups;

		public SumDataStruct(IntDomainVar[] vars, long sum) {
			super();
			this.vars = vars;
			this.sum = sum;
		}

		public void update() {
			sumMinusInfs = sum;
			sumMinusSups = sum;
			for (int i = 0; i < vars.length; i++) {
				sumMinusInfs -= vars[i].getInf();
				sumMinusSups -= vars[i].getSup();
			}
		}

		// FIXME - why is a Point object created ? - created 12 juil. 2011 by Arnaud Malapert
		public Point getBounds(int idx) {
			return new Point( (int) (sumMinusSups + vars[idx].getSup()),
					(int) (sumMinusInfs + vars[idx].getInf()));
		}
	}
}
