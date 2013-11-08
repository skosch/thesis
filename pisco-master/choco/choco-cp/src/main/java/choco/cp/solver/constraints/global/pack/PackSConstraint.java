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

import choco.kernel.common.opres.nosum.NoSumList;
import choco.kernel.common.opres.pack.LowerBoundFactory;
import choco.kernel.common.util.bitmask.BitMask;
import choco.kernel.common.util.bitmask.StringMask;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateIntVector;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.set.AbstractLargeSetIntSConstraint;
import choco.kernel.solver.search.ISolutionDisplay;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntProcedure;

import java.util.Arrays;
import java.util.List;

import static choco.Options.*;

/**
 * <b>{@link Pack} which maintains a primal-dual packing model.</b><br>
 * The primal model consists of {@link bins} variables. {@link bins}[item] = bin means that item is packed into bin.<br>
 * The dual model consists of {@link svars} variables. item is in {@link svars}[bin] also means that item is packed into bin.
 * @author Arnaud Malapert</br>
 * @since 5 déc. 2008 version 2.0.1</br>
 * @version 2.1.0</br>
 */
public class PackSConstraint extends AbstractLargeSetIntSConstraint implements IPackSConstraint, ISolutionDisplay {

	public final static StringMask ADDITIONAL_RULES = new StringMask(C_PACK_AR,1);
	public final static StringMask DYNAMIC_LB = new StringMask(C_PACK_DLB,1 << 2);
	public final static StringMask FILL_BIN = new StringMask(C_PACK_FB,1 << 3);
	public final static StringMask LAST_BINS_EMPTY = new StringMask(C_PACK_LBE,1 << 4);

	public final BitMask flags = new BitMask();

	public final PackFiltering filtering;

	protected final BoundNumberOfBins bounds;

	private final NoSumList reuseStatus;

	private IStateIntVector availableBins;

	/** The sizes of the items. */
	protected final int[] sizes;

	/** The loads of the bins. */
	protected final IntDomainVar[] loads;

	/** The bin of each item. */
	protected final IntDomainVar[] bins;

// TODO - provide d-dimensional constraint - Arnaud Malapert - 4 juil. 2011	

	
	public PackSConstraint(IEnvironment environment, SetVar[] itemSets, IntDomainVar[] loads, IntDomainVar[] sizes,
			IntDomainVar[] bins, IntDomainVar nbNonEmpty) {
		super(ArrayUtils.append(loads,sizes,bins,new IntDomainVar[]{nbNonEmpty}),itemSets);
		this.loads=loads;
		this.sizes=VariableUtils.getConstantValues(sizes);
		this.bins =bins;
		this.bounds = new BoundNumberOfBins();
		filtering = new PackFiltering(this,flags);
		availableBins = environment.makeBipartiteIntList(ArrayUtils.zeroToN(getNbBins()));
		reuseStatus = new NoSumList(this.sizes);
		// FIXME - Pas de Mask : getFilteredEventMask(idx) - Arnaud Malapert - 4 juil. 2011
	}

	public void readOptions(final List<String> options) {
		flags.read(options, PackSConstraint.ADDITIONAL_RULES, PackSConstraint.DYNAMIC_LB, PackSConstraint.FILL_BIN, PackSConstraint.LAST_BINS_EMPTY);
	}

	public final boolean isEmpty(int bin) {
		return svars[bin].getKernelDomainSize()==0;
	}

	
	@Override
	public void fireAvailableBins() {
		final DisposableIntIterator iter = availableBins.getIterator();
		while(iter.hasNext()) {
			final int b = iter.next();
			if( svars[b].isInstantiated()) {
				iter.remove();
			}
		}
		iter.dispose();

	}

	@Override
	public final IStateIntVector getAvailableBins() {
		return availableBins;
	}

	public final int getRequiredSpace(int bin) {
		final DisposableIntIterator iter= svars[bin].getDomain().getKernelIterator();
		int load = 0;
		while(iter.hasNext()) {
			load+= sizes[iter.next()];
		}
		iter.dispose();
		return load;
	}


	public final int getRemainingSpace(int bin) {
		return loads[bin].getSup() - getRequiredSpace(bin);
	}

	protected final boolean isSetEvent(final int varIdx) {
		return varIdx < svars.length;
	}

	protected final boolean isItemEvent(final int varIdx) {
		final int a = 2*getNbBins() + getNbItems();
		final int b = a + getNbItems();
		return varIdx >= a && varIdx < b ;
	}

	protected final int getItemIndex(final int varIdx) {
		return varIdx- 2*getNbBins() - getNbItems();
	}


	public final IntDomainVar[] getBins() {
		return bins;
	}


	//****************************************************************//
	//********* Filtering interface **********************************//
	//****************************************************************//


	@Override
	public final int getNbBins() {
		return svars.length;
	}

	@Override
	public final int getNbItems() {
		return sizes.length;
	}


	@Override
	public final IntDomainVar[] getLoads() {
		return loads;
	}


	@Override
	public final int[] getSizes() {
		return sizes;
	}

	@Override
	public final NoSumList getStatus(int bin) {
		reuseStatus.setCandidatesFromVar(svars[bin]);
		return reuseStatus;
	}


	@Override
	public final boolean pack(int item, int bin) throws ContradictionException {
		boolean res = svars[bin].addToKernel(item, this, false);
		if(bins[item].canBeInstantiatedTo(bin)) {
			final DisposableIntIterator iter = bins[item].getDomain().getIterator();
			while(iter.hasNext()) {
				final int b = iter.next();
				if(b != bin) {
					res |= svars[b].remFromEnveloppe(item, this, false);
				}
			}
			// FIXME - need only to check the last assignment (lazy) ?  - created 6 juil. 2011 by Arnaud Malapert
            res |= bins[item].instantiate(bin, this, false);
		}else {
			this.fail();
		}
		return res;
	}

	@Override
	public final boolean remove(int item, int bin) throws ContradictionException {
		boolean res = svars[bin].remFromEnveloppe(item, this, false);
		res |= bins[item].removeVal(bin, this, false);
		if(bins[item].isInstantiated()) {
			final int b = bins[item].getVal();
			svars[b].addToKernel(item, this, false);
		}
		return res;
	}


	@Override
	public final boolean updateInfLoad(int bin, int load) throws ContradictionException {
		return loads[bin].updateInf(load, this, false);

	}

	@Override
	public final boolean updateNbNonEmpty(int min, int max) throws ContradictionException {
		// TODO - Try to empty sets faster - Arnaud Malapert - 4 juil. 2011
		//// TODO - use Rmax instead of R0 (see phd P. Schaus) - created 12 juil. 2011 by Arnaud Malapert
		boolean res = false;
		final int idx = ivars.length-1;
		//LOGGER.info(min+ " "+max + " -> "+ivars[idx].pretty());
		ivars[idx].updateInf( min, this, false);
		final int oldSup = ivars[idx].getSup();
		if( ivars[idx].updateSup(max, this, false)
				&& flags.contains(PackSConstraint.LAST_BINS_EMPTY)) {
			for (int b = max; b < oldSup; b++) {
				
				final DisposableIntIterator iter = svars[b].getDomain().getEnveloppeIterator();
				try{
					while(iter.hasNext()) {
						res |= remove(iter.next(), b);
					}
				}finally {
					iter.dispose();
				}
			}
		}
		return res;
	}

	@Override
	public final boolean updateSupLoad(int bin, int load) throws ContradictionException {
		return loads[bin].updateSup(load, this, false);
	}

	//****************************************************************//
	//********* Events *******************************************//
	//****************************************************************//



	@Override
	public boolean isConsistent() {
		//no idea. wait and propagate
		return false;
	}

	protected final void checkBounds(int item) throws ContradictionException {
		bins[item].updateInf(0, this, false);
		bins[item].updateSup(svars.length-1, this, false);
	}

	protected final void checkEnveloppes() throws ContradictionException {
		for (int bin = 0; bin < svars.length; bin++) {
			int inf;
			// check if enveloppe is empty, to avoid infinite loop
			while( (inf = svars[bin].getEnveloppeInf())<0
					&& svars[bin].remFromEnveloppe(inf, this, false)) {
			}
			int sup;
			// check if enveloppe is empty, to avoid infinite loop
			while( (sup = svars[bin].getEnveloppeSup()) > bins.length-1
					&& svars[bin].remFromEnveloppe(sup, this, false) ) {}
		}
	}
	@Override
	public void awake() throws ContradictionException {
		//initial channeling
		checkEnveloppes();
        int left, right;
		for (int item = 0; item < bins.length; item++) {
			checkBounds(item);
			if(bins[item].isInstantiated()) {
				//the item is packed
				final int b0 = bins[item].getVal();
				svars[b0].addToKernel(item, this, false);
				for (int b = 0; b < b0; b++) {
					svars[b].remFromEnveloppe(item, this, false);
				}
				for (int b = b0+1; b < svars.length; b++) {
					svars[b].remFromEnveloppe(item, this, false);
				}
			}else {
                left = right = Integer.MIN_VALUE;
				for (int bin = 0; bin < svars.length; bin++) {
					if(svars[bin].isInDomainEnveloppe(item)) {
						//item could be packed here
						if(svars[bin].isInDomainKernel(item)) {
							//item is packed
							bins[item].instantiate(bin, this, false);
						}else if(! bins[item].canBeInstantiatedTo(bin)) {
							//in fact, channeling fails
							svars[bin].remFromEnveloppe(item, this, false);
						}
						//channeling ok enveloppe-domain
					}else {
						//otherwise remove from domain
                        if (bin == right + 1) {
                                right = bin;
                            } else {
                                bins[item].removeInterval(left, right, this, false);
                                left = right = bin;
                            }
//						bins[item].removeVal(bin, this, false);
					}
				}
                bins[item].removeInterval(left, right, this, false);
			}
		}
		super.awake();
	}

	@Override
	public void awakeOnEnv(int varIdx, int x) throws ContradictionException {
		bins[x].removeVal(varIdx, this, false);
		//if the item is packed, update variables
		if(bins[x].isInstantiated()) {
			final int b = bins[x].getVal();
			svars[b].addToKernel(x, this, false);
		}
		this.constAwake(false);
	}


	protected void checkDeltaDomain(int item) throws ContradictionException {
		final DisposableIntIterator iter=bins[item].getDomain().getDeltaIterator();
		if(iter.hasNext()) {
			try{
				while(iter.hasNext()) {
					final int b=iter.next();
					svars[b].remFromEnveloppe(item, this, false);
				}
			}finally {
				iter.dispose();
			}
		}else {
			throw new SolverException("empty delta domain: "+bins[item].pretty());
		}
	}

	@Override
	public void awakeOnBounds(int varIndex) throws ContradictionException {
		if(isItemEvent(varIndex)) {
			final int item = getItemIndex(varIndex);
			//the item is not packed
			//so, we can safely remove from other enveloppes
			checkDeltaDomain(item);
		}
		this.constAwake(false);
	}


	@Override
	public void awakeOnInf(int varIdx) throws ContradictionException {
		awakeOnBounds(varIdx);
	}

	@Override
	public void awakeOnInst(int varIdx) throws ContradictionException {
		if(isSetEvent(varIdx)) {
			DisposableIntIterator iter= svars[varIdx].getDomain().getKernelIterator();
			try{
				while(iter.hasNext()) {
					final int item=iter.next();
					if(! bins[item].isInstantiated()) {
						pack(item,varIdx);
					}
				}
			}finally {
				iter.dispose();
			}
			iter= svars[varIdx].getDomain().getEnveloppeDomain().getDeltaIterator();
			try{
				while(iter.hasNext()) {
					final int item=iter.next();
					if(bins[item].canBeInstantiatedTo(varIdx)) {
						remove(item, varIdx);
					}
				}
			}finally {
				iter.dispose();
			}
		}else if(isItemEvent(varIdx)){
			final int item=getItemIndex(varIdx);
			final int b = bins[item].getVal();
			svars[b].addToKernel(item, this, false);
			checkDeltaDomain(item);
		} else handleNbBinsEvent(varIdx);
		constAwake(false);
	}

	private void handleNbBinsEvent(int varIdx) throws ContradictionException {
		// TODO - Try to empty sets faster - Arnaud Malapert - 4 juil. 2011
		if(varIdx == getNbVars() - 1 &&
				flags.contains(PackSConstraint.LAST_BINS_EMPTY)) {
			for (int b = ivars[ivars.length - 1].getSup(); b < getNbBins(); b++) {
				final DisposableIntIterator iter = svars[b].getDomain().getEnveloppeIterator();
				try{
					while(iter.hasNext()) remove(iter.next(), b);
				}finally {
					iter.dispose();
				}
			}
		}
	}

	@Override
	public void awakeOnKer(int varIdx, int x) throws ContradictionException {
		pack(x,varIdx);
		this.constAwake(false);
	}

	@Override
	public void awakeOnRem(int varIdx, int val) throws ContradictionException {
		if(isItemEvent(varIdx)) {
			//remove from associated enveloppe
			svars[val].remFromEnveloppe(getItemIndex(varIdx), this, false);
		}
		this.constAwake(false);
	}

	@Override
	public void awakeOnSup(int varIdx) throws ContradictionException {
		handleNbBinsEvent(varIdx);
		awakeOnBounds(varIdx);
	}

	@Override
	public void propagate() throws ContradictionException {
		do {
			filtering.propagate();
			//feasibility test (MDFF)
			if ( ! bounds.computeBounds(flags.contains(PackSConstraint.DYNAMIC_LB)) ) {
				fail();
			}
			// FIXME - is this loop useful (after MDFF) ? - Arnaud Malapert - 4 juil. 2011
		}while( updateNbNonEmpty(bounds.getMinimumNumberOfBins(), bounds.getMaximumNumberOfBins()));


	}

	@Override
	public boolean isSatisfied() {
		int[] l = new int[loads.length];
		int[] c = new int[loads.length];
		for (int i = 0; i < bins.length; i++) {
			final int b =  bins[i].getVal();
			if( ! svars[b].isInDomainKernel(i)) return false; //check channeling
			l[b] += sizes[i];
			c[b] ++;
		}
		int nbb = 0;
		for (int i = 0; i < loads.length; i++) {
			if( svars[i].getCard().getVal() != c[i]) return false; //check cardinality
			if( loads[i].getVal() != l[i]) return false; //check load
			if( c[i] != 0) {nbb++;}
		}
		return ivars[ivars.length-1].getVal() == nbb; //check number of bins
	}

	public final String solutionToString() {
		StringBuilder b = new StringBuilder();
		for (SetVar s : svars) {
			int[] t= s.getValue();
			if (t != null && t.length > 0) {
				int l = t.length - 1;
				b.append('[');
				for (int i = 0; i < l; i++) {
					b.append(sizes[t[i]]).append(", ");
				}
				b.append(sizes[t[l]]).append("] ");
			}
		}
		return b.toString();
	}



	protected final class BoundNumberOfBins {

		private final int[] remainingSpace;

		private final TIntArrayList itemsMLB;

		protected int capacityMLB;

		private final TIntArrayList binsMLB;

		private int sizeIMLB;

		private int totalSizeCLB;

		private final TIntArrayList binsCLB;

		protected int nbEmpty;

		protected int nbSome;

		protected int nbFull;

		protected int nbNewCLB;

		private final TIntProcedure minimumNumberOfNewBins = new TIntProcedure() {
			@Override
			public boolean execute(int arg0) {
				nbNewCLB++;
				if( totalSizeCLB <= arg0) {
					return false;
				}
				totalSizeCLB -= arg0;
				return true;
			}
		};


		public BoundNumberOfBins() {
			super();
			itemsMLB=new TIntArrayList(getNbBins() + getNbItems());
			binsMLB = new TIntArrayList(getNbBins());
			binsCLB = new TIntArrayList(getNbBins());
			remainingSpace = new int[getNbBins()];
		}


		public void reset() {
			Arrays.fill(remainingSpace, 0);
			itemsMLB.resetQuick();
			capacityMLB=0;
			binsMLB.resetQuick();
			totalSizeCLB = 0;
			binsCLB.resetQuick();
			nbEmpty=0;
			nbSome = 0;
			nbFull=0;
			nbNewCLB = 0;
		}

		/**
		 * add unpacked items (MLB) compute their total size (CLB).
		 */
		private void handleItems() {
			final int n = getNbItems();
			for (int i = 0; i < n; i++) {
				final int size = sizes[i];
				if(bins[i].isInstantiated()) {
					remainingSpace[bins[i].getVal()] -= sizes[i];
				}else {
					totalSizeCLB += sizes[i];
					itemsMLB.add(sizes[i]);
				}
			}
			sizeIMLB = itemsMLB.size();
		}


		/**
		 * compute the remaining space in each bin and the cardinality of sets (empty, partially filled, full)
		 */
		private void handleBins() {
			final int n = getNbBins();
			//compute the number of empty, partially filled and closed bins
			//also compute the remaining space in each open bins
			for (int b = 0; b < n; b++) {
				if(svars[b].isInstantiated()) {
					//we ignore closed bins
					if(loads[b].isInstantiatedTo(0)) nbEmpty++;
					else nbFull++;
				}else {
					//the bins is used by the modified lower bound
					binsMLB.add(b);
					remainingSpace[b] += loads[b].getSup();
					capacityMLB = Math.max(capacityMLB, remainingSpace[b]);
					if(svars[b].getKernelDomainSize()>0) {
						//partially filled
						nbSome++;
						totalSizeCLB -= remainingSpace[b]; //fill partially filled bin before empty ones
					} else {
						//still empty
						binsCLB.add(remainingSpace[b]); //record empty bins to fill them later
					}
				}
			}
		}

		/**
		 * compute fake top-items which fills the bin until the current capacity.
		 */
		private void createFakeItems() {
			final int n = binsMLB.size();
			for (int i = 0; i < n; i++) {
				final int size = capacityMLB - remainingSpace[ binsMLB.getQuick(i)];
				if( size > 0) itemsMLB.add(size);
			}
		}

		private void computeMinimumNumberOfNewBins() {
			binsCLB.sort();
			binsCLB.forEachDescending(minimumNumberOfNewBins);
		}

		/**
		 *
		 * @param useMDFF do we use advanced and costly bounding procedure for a feaasibility test.
		 * @return <code>false</code>  if the current state is infeasible.
		 */
		public boolean computeBounds(boolean useMDFF) {
			reset();
			//the order of the following calls is important
			handleItems();
			handleBins();
			if( ! itemsMLB.isEmpty() ) {
				//if( sizeMLB < maximumNumberOfNewBins.get() ) maximumNumberOfNewBins.set(sizeMLB);
				//there is unpacked items
				//handleBins();
				if( totalSizeCLB > 0) {
					//compute an estimation of the minimal number of additional bins.
					if( binsCLB.isEmpty()) return false;  //no more available bins for remaining unpacked items
					computeMinimumNumberOfNewBins();
				}
				if( getMinimumNumberOfBins() > ivars[ivars.length - 1].getSup()) return false; //the continous bound prove infeasibility
				if( useMDFF) {
					createFakeItems();
					return LowerBoundFactory.testPackingConsistencyWithMDFF(itemsMLB, capacityMLB, binsMLB.size());
				}
			}
			return true;
		}

		public int getMaximumNumberOfBins() {
			return Math.min(getNbBins() -nbEmpty, nbFull + nbSome + sizeIMLB);
		}

		public int getMinimumNumberOfBins() {
			return nbFull + nbSome + nbNewCLB;
		}
	}


}