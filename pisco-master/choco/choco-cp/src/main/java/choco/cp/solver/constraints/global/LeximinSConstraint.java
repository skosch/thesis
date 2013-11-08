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

package choco.cp.solver.constraints.global;


import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.integer.IntVar;

import java.util.logging.Level;

/**
 * <code>SemiLeximinConstraint</code> is a constraint that ensures
 * the leximin ordering between one vector of variables and one of integers.
 * We define the leximin preorder as follows:
 * Let x and y be two vectors of n integers, and let x? and y? be the version of x and y rearranged in increasing order.
 * x and y are said leximin-indifferent if x?=y?. y is leximin-preferred to x (written y>leximinx if and only if there is an i< n such that for all j� i:
 * - the jth component of x? is equal to the jth component of y?
 * - the ith component of x? is lower than the ith component of y?
 * Let x and x' be two vectors of variables, and v be an instantiation.
 * The constraint Leximin(x, x') holds on the set of variables belonging to x or x', and is satisfied by v if and only if v(x) <leximin v(x').
 * [Frisch et al. 2003]	A. Frisch, B. Hnich, Z. Kiziltan, I. Miguel, and T. Walsh. Multiset ordering constraints. In Proc. of IJCAI'03. Acapulco, Mexico, 2003.
 * Description of the implementation
 * The implementation of this constraint is adapted from the algorithm given
 * in [Frisch et al. 2003] and enforces generalized arc-consistency on the constraint Leximin in O(n log(n)) (we choose tha version of the algorithm that does not build the entire occurrence vectors, as the size of our domains can be huge).
 *
 * @author <a href="mailto:sylvain.bouveret@cert.fr">Sylvain Bouveret</a>
 * @version 1.0
 */

public final class LeximinSConstraint extends AbstractLargeIntSConstraint {

	private IStateInt alpha, beta;
	private IStateInt gamma, delta;
	private IStateBitSet epsilon;
	/*
      epsilon is a set of additional flags useful for the gac() algorithm :
      - epsilon[0] = true iff occx[alpha] = occy[alpha] - 1
      - epsilon[1] = true iff occx[alpha] = occy[alpha] + 1
      - epsilon[2] = true iff occx[beta] = occy[beta] - 1
      - epsilon[3] = true iff occx[beta] = occy[beta] + 1
	 */
	//private StoredInt[] occx, occy;
	private IStateInt[] floorx, ceily;
	private IStateInt[] sortedFloorx, sortedCeily;
	private int maximum;
	private int minimum;
	private int n;

	private static final boolean VERBOSE = false;

    private final IEnvironment environment;

	/**
	 * Creates a new <code>LeximinConstraint</code> instance.
	 *
	 * @param x the first array of integer variables
     * @param y the second array of integer variables
     * @param environment
     */
	public LeximinSConstraint(IntVar[] x, IntVar[] y, IEnvironment environment) {
		super(ConstraintEvent.LINEAR, LeximinSConstraint.mergeIntVarArrays(x, y));
		if (x.length != y.length || x.length == 0 || y.length == 0) {
			throw new IllegalArgumentException("LeximinConstraint Error: the two vectors "
					+ "must be of the same (non zero) size");
		}
		this.n = x.length;

		this.alpha = environment.makeInt();
		this.beta = environment.makeInt();
		this.gamma = environment.makeInt();
		this.delta = environment.makeInt();
		this.epsilon = environment.makeBitSet(4);
        this.environment = environment;
		this.generateVectors();
	}

	/**
	 * Creates a new <code>LeximinConstraint</code> instance.
	 *
	 * @param x the concatenation of two integer variables array
     * @param environment
	 */
	public LeximinSConstraint(IntDomainVar[] x, IEnvironment environment) {
		super(ConstraintEvent.LINEAR, x);
		if (x.length % 2 != 0 || x.length == 0) {
			throw new IllegalArgumentException("LeximinConstraint Error: the two vectors "
					+ "must be of the same (non zero) size");
		}
		this.n = x.length / 2;

		this.alpha = environment.makeInt();
		this.beta = environment.makeInt();
		this.gamma = environment.makeInt();
		this.delta = environment.makeInt();
		this.epsilon = environment.makeBitSet(4);
        this.environment = environment;
		this.generateVectors();
	}



	@Override
	public int getFilteredEventMask(int idx) {
		return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
	}
	/**
	 * A utility class that merges two <code>IntVar</code>
	 * arrays into an <code>IntDomainVar</code> one.
	 *
	 * @param firstArray  an <code>IntVar</code> array
	 * @param secondArray an <code>IntVar</code> array
	 * @return the <code>IntDomainVar</code> array built from the parameters
	 */
	private static IntDomainVar[] mergeIntVarArrays(IntVar[] firstArray, IntVar[] secondArray) {
		IntDomainVar[] newArray = new IntDomainVar[firstArray.length + secondArray.length];
		for (int i = 0; i < firstArray.length; i++) {
			newArray[i] = (IntDomainVar) firstArray[i];
		}
		for (int i = 0; i < secondArray.length; i++) {
			newArray[i + firstArray.length] = (IntDomainVar) secondArray[i];
		}
		return (newArray);
	}

	/**
	 * This methods builds the vectors used by the gac algorithm.
	 */
	private void generateVectors() {
		this.floorx = new IStateInt[n];
		this.ceily = new IStateInt[n];
		this.sortedFloorx = new IStateInt[n];
		this.sortedCeily = new IStateInt[n];
		int minimumX = Integer.MAX_VALUE;
		int minimumY = Integer.MAX_VALUE;
		int maximumX = 0;
		int maximumY = 0;
		for (int i = 0; i < n; i++) {
			this.sortedFloorx[i] = environment.makeInt(super.vars[i].getInf());
			this.floorx[i] = environment.makeInt(super.vars[i].getInf());
			minimumX = minimumX > this.sortedFloorx[i].get() ? this.sortedFloorx[i].get() : minimumX;
			maximumX = maximumX < this.sortedFloorx[i].get() ? this.sortedFloorx[i].get() : maximumX;
			this.sortedCeily[i] = environment.makeInt(super.vars[n + i].getSup());
			this.ceily[i] = environment.makeInt(super.vars[n + i].getSup());
			minimumY = minimumY > this.sortedCeily[i].get() ? this.sortedCeily[i].get() : minimumY;
			maximumY = maximumY < this.sortedCeily[i].get() ? this.sortedCeily[i].get() : maximumY;
		}
		this.minimum = minimumX < minimumY ? minimumX : minimumY;
		this.maximum = maximumX > maximumY ? maximumX : maximumY;

		java.util.Arrays.sort(this.sortedFloorx, new LeximinSConstraint.SIComparator());
		java.util.Arrays.sort(this.sortedCeily, new LeximinSConstraint.SIComparator());
	}

	/**
	 * This methods updates the vectors used by the gac algorithm.
	 * @param idx indice
	 */
	private void updateVectors(int idx) {
		int oldValue, newValue, i;
		if (idx < this.n) {
			oldValue = this.floorx[idx].get();
			newValue = super.vars[idx].getInf();
			this.floorx[idx].set(newValue);
			for (i = 0; this.sortedFloorx[i].get() < oldValue; i++) {}
			int j;
			for (j = i + 1; (j < this.n) && (this.sortedFloorx[j].get() < newValue); j++) {
				this.sortedFloorx[j - 1].set(this.sortedFloorx[j].get());
			}
			this.sortedFloorx[j - 1].set(newValue);
		} else {
			oldValue = this.ceily[idx - n].get();
			newValue = super.vars[idx].getSup();
			this.ceily[idx - n].set(newValue);
			for (i = this.n - 1; this.sortedCeily[i].get() > oldValue; i--) {}
			int j;
			for (j = i - 1; (j >= 0) && (this.sortedCeily[j].get() > newValue); j--) {
				this.sortedCeily[j + 1].set(this.sortedCeily[j].get());
			}
			this.sortedCeily[j + 1].set(newValue);
		}
	}

	/**
	 * The <code>setPointersAndFlags</code> method sets the values
	 * &alpha;, &beta;, &gamma; and &delta;, used by the algorithm.
	 *
	 * @throws choco.kernel.solver.ContradictionException if the model instance is inconsistant
	 */
	public void setPointersAndFlags() throws ContradictionException {
		int l = this.minimum;
		int u = this.maximum;
		int currentX = 0;
		int currentY = 0;
		int currentOccX = 0;
		int currentOccY = 0;
		this.gamma.set(0);
		this.delta.set(0);
		this.alpha.set(l - 1);
		for (int i = 0; i < 4; i++) {
			this.epsilon.set(i, false);
		}
		if (VERBOSE) {
			LOGGER.log(Level.INFO, "l = {0} / u = {1}", new Object[]{l, u});
		}
		while (this.alpha.get() <= u && currentOccX == currentOccY) {
			this.alpha.set(this.alpha.get() + 1); // Increase alpha by one.
			currentOccX = 0;
			while (currentX < this.sortedFloorx.length && this.sortedFloorx[currentX].get() == this.alpha.get()) {
				currentOccX++;
				currentX++;
			} // Read sortedFloorx array in order to compute occX
			currentOccY = 0;
			while (currentY < this.sortedCeily.length && this.sortedCeily[currentY].get() == this.alpha.get()) {
				currentOccY++;
				currentY++;
			} // Read sortedCeily array in order to compute occY
			if (VERBOSE) {
				LOGGER.log(Level.INFO, "cX = {0} / cOX = {1}", new Object[]{currentX, currentOccX});
				LOGGER.log(Level.INFO, "cY = {0} / cOY = {1}", new Object[]{currentY, currentOccY});
			}
		}

		if (this.alpha.get() <= u && currentOccX < currentOccY) {
			this.fail();
		}
		if (this.alpha.get() == u + 1) {
			//this.alpha.set(Integer.MAX_VALUE);
			//this.beta.set(Integer.MAX_VALUE);
			this.fail();
		} else {
			if (currentOccX == currentOccY - 1) {
				this.epsilon.set(0, true);
			}
			if (currentOccX == currentOccY + 1) {
				this.epsilon.set(1, true);
			}
			this.beta.set(this.alpha.get());
			this.gamma.set(1);
			currentOccX = 0;
			currentOccY = 0;
			while (this.beta.get() <= u && currentOccX >= currentOccY) {
				if (currentOccX > currentOccY) {
					this.gamma.set(0);
				}
				this.beta.set(this.beta.get() + 1);
				currentOccX = 0;
				while (currentX < this.sortedFloorx.length && this.sortedFloorx[currentX].get() == this.beta.get()) {
					currentOccX++;
					currentX++;
				} // Read sortedFloorx array in order to compute occX
				currentOccY = 0;
				while (currentY < this.sortedCeily.length && this.sortedCeily[currentY].get() == this.beta.get()) {
					currentOccY++;
					currentY++;
				} // Read sortedCeily array in order to compute occY
			}
			if (this.beta.get() == u + 1) {
				this.beta.set(Integer.MAX_VALUE);
				this.gamma.set(0);
			}
			if (currentOccX == currentOccY - 1) {
				this.epsilon.set(2, true);
			}
			if (currentOccX == currentOccY + 1) {
				this.epsilon.set(3, true);
			}
			if (this.beta.get() < u) {
				int i = this.beta.get();
				currentOccX = 0;
				currentOccY = 0;
				while (i <= u && currentOccX == currentOccY) {
					i++;
					currentOccX = 0;
					while (currentX < this.sortedFloorx.length && this.sortedFloorx[currentX].get() == i) {
						currentOccX++;
						currentX++;
					} // Read sortedFloorx array in order to compute occX
					currentOccY = 0;
					while (currentY < this.sortedCeily.length && this.sortedCeily[currentY].get() == i) {
						currentOccY++;
						currentY++;
					} // Read sortedCeily array in order to compute occY
				}
				if (i <= u && currentOccX < currentOccY) {
					this.delta.set(1);
				}
			}
		}
	}


	/**
	 * The <code>setPointersAndFlags</code> method updates the values
	 * &alpha;, &beta;, &gamma; and &delta;, used by the algorithm,
	 * when the domain of a variable has changed.
	 *
	 * @param idx the index of the variable whose domain has changed
	 * @param inf a <code>boolean</code> value indicating whether the change
	 *            occured with the lower bound (<code>true</code>) or with thee upper bound
	 *            (<code>false</code>).
	 * @throws ContradictionException if the model instance is inconsistant
	 */
	public void updatePointersAndFlags(int idx, boolean inf) throws ContradictionException {
		/* TODO : for optimizing the constraint update ? */
	}


	/**
	 * The <code>gac</code> method ("gac" for Generalized Arc Consistency)
	 * checks for a support for each value of the variables, and removes
	 * a value if it has no support.
	 *
	 * @throws choco.kernel.solver.ContradictionException if a variable has an empty domain.
	 */
	private void gac() throws ContradictionException {
		int a, b;
		for (int i = 0; i < n; i++) {
			// Check support for x
			if ((a = super.vars[i].getInf()) < (b = super.vars[i].getSup())) {
				if (a < this.alpha.get()) {
					super.vars[i].instantiate(a, this, false);
				}
				if (a == this.alpha.get() && this.epsilon.get(1)) {
					if (b >= this.beta.get() && this.gamma.get() > 0) {
						if (this.epsilon.get(2)) {
							if (this.delta.get() > 0) {
								//super.vars[i].updateSup(this.beta.get() - 1, super.cste);
								super.vars[i].updateSup(this.beta.get(), this, false);
							} else {
								super.vars[i].updateSup(this.beta.get(), this, false);
							}
						} else {
							//super.vars[i].updateSup(this.beta.get() - 1, super.cste);
							super.vars[i].updateSup(this.beta.get(), this, false);
						}
					} else {
						super.vars[i].updateSup(this.beta.get(), this, false);
					}
				}
			}

			// Check support for y
			if ((a = super.vars[n + i].getInf()) < (b = super.vars[n + i].getSup())) {
				if (b <= this.alpha.get()) {
					super.vars[n + i].instantiate(b, this, false);
				}
				if (this.alpha.get() < b &&
						b < this.beta.get() &&
						a <= this.alpha.get()) {
					super.vars[n + i].updateInf(this.alpha.get(), this, false);
				}
				if (b == this.beta.get() && a <= this.alpha.get()) {
					if (this.epsilon.get(1)) {
						if (this.gamma.get() > 0 &&
								this.epsilon.get(2) &&
								this.delta.get() > 0) {
							//super.vars[n + i].updateInf(this.alpha.get() + 1, super.cste);
							super.vars[n + i].updateInf(this.alpha.get(), this, false);
						} else {
							super.vars[n + i].updateInf(this.alpha.get(), this, false);
						}
					} else {
						super.vars[n + i].updateInf(this.alpha.get(), this, false);
					}
				}
				if (b > this.beta.get() && a <= this.alpha.get()) {
					if (this.epsilon.get(1)) {
						if (this.gamma.get() > 0) {
							//super.vars[n + i].updateInf(this.alpha.get() + 1, super.cste);
							super.vars[n + i].updateInf(this.alpha.get(), this, false);
						}
					} else {
						//super.vars[n + i].updateInf(this.alpha.get() + 1, super.cste);
						super.vars[n + i].updateInf(this.alpha.get(), this, false);
					}
				}

			}
		}
	}


	/**
	 * This method is invoked during the first propagation.
	 *
	 * @throws choco.kernel.solver.ContradictionException if a variable has an empty domain.
	 */
	@Override
	public void awake() throws ContradictionException {
		this.setPointersAndFlags();
		this.gac();
		//propagate();
	}


	/**
	 * This methode propagates the constraint events.
	 *
	 * @throws ContradictionException if a variable has an empty domain.
	 */
	@Override
	public void propagate() throws ContradictionException {
		this.setPointersAndFlags();
		this.gac();
	}


	/**
	 * This method is called when a variable has been instanciated
	 *
	 * @param idx the index of the instanciated variable.
	 */

	@Override
	public void awakeOnInst(int idx) throws ContradictionException {
		if ((idx < this.n) && (super.vars[idx].getInf() > this.maximum)) {
			this.maximum = super.vars[idx].getInf();
		}
		if ((idx >= this.n) && (super.vars[idx].getSup() < this.minimum)) {
			this.minimum = super.vars[idx].getSup();
		}
		this.updateVectors(idx);
		this.setPointersAndFlags();
		this.gac();
		//propagate();
	}

	/**
	 * Cette m�thode r�agit si une variable a vu sa borne inf�rieure augmenter.
	 *
	 * @param idx l'indice de la variable qui a �t� instanci�e.
	 */

	@Override
	public void awakeOnInf(int idx) throws ContradictionException {
		if (idx < this.n) {
			if (super.vars[idx].getInf() > this.maximum) {
				this.maximum = super.vars[idx].getInf();
			}
			this.updateVectors(idx);
			this.setPointersAndFlags();
			this.gac();
		}
		//propagate();
	}

	/**
	 * Cette m�thode r�agit si une variable a vu sa borne sup�rieure diminuer.
	 *
	 * @param idx l'indice de la variable qui a �t� instanci�e.
	 */

	@Override
	public void awakeOnSup(int idx) throws ContradictionException {
		if (idx >= this.n) {
			if (super.vars[idx].getSup() < this.minimum) {
				this.minimum = super.vars[idx].getSup();
			}
			this.updateVectors(idx);
			this.setPointersAndFlags();
			this.gac();
		}
		//propagate();
	}


	/**
	 * This method checks if the constraint is satisfied, once the variables have
	 * all been satisfied.
	 *
	 * @return <code>true</code> iff the constraint is satisfied.
	 */

	@Override
	public boolean isSatisfied() {
		IntDomainVar[] x = new IntDomainVar[n];
		IntDomainVar[] y = new IntDomainVar[n];
		for (int i = 0; i < n; i++) {
			x[i] = super.vars[i];
			y[i] = super.vars[n + i];
		}

		java.util.Arrays.sort(x, new LeximinSConstraint.IDVComparator());
		java.util.Arrays.sort(y, new LeximinSConstraint.IDVComparator());
		int i;
		for (i = 0; i < n && x[i].getVal() >= y[i].getVal(); i++) {
		}
		return i != n;
	}


	/**
	 * The rather classical <code>toString</code> method...
	 *
	 * @return a <code>String</code> representing the object.
	 */

	@Override
	public String toString() {
		return "Leximin ordering constraint.";
	}

	/**
	 * Prints details about the constraint and its variables.
	 * @return The details in a string.
	 */
	@Override
	public String pretty() {
		StringBuilder sb = new StringBuilder();
		sb.append("Leximin({");
		for (int i = 0; i < n; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			IntDomainVar var = vars[i];
			sb.append(var.pretty());
		}
		sb.append("}, {");
		for (int i = 0; i < n; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			IntDomainVar var = vars[n + i];
			sb.append(var.pretty());
		}
		sb.append("})");
		return sb.toString();
	}


	/**
	 * Prints some of the useful private fields that are used by the gac
	 * algorithm.
	 */

	public void printOccVectors() {
		if(LOGGER.isLoggable(Level.INFO)) {
			StringBuffer st = new StringBuffer("x = [");
			for (int i = 0; i < this.sortedFloorx.length; i++) {
				st.append(" ").append(super.vars[i].pretty());
			}
			st.append(" ]\n");
			st.append("y = [");
			for (int i = 0; i < this.sortedCeily.length; i++) {
				st.append(" ").append(super.vars[n + i].pretty());
			}
			st.append(" ]\n");
			st.append("sortedFloor(x) = [");
			for (IStateInt aSortedFloorx : this.sortedFloorx) {
				st.append(" ").append(aSortedFloorx.get());
			}
			st.append(" ]\n");
			st.append("sortedCeil(y) = [");
			for (IStateInt aSortedCeily : this.sortedCeily) {
				st.append(" ").append(aSortedCeily.get());
			}
			st.append(" ]\n");
			st.append("floor(x) = [");
			for (IStateInt aFloorx : this.floorx) {
				st.append(" ").append(aFloorx.get());
			}
			st.append(" ]\n");
			st.append("ceil(y) = [");
			for (IStateInt aCeily : this.ceily) {
				st.append(" ").append(aCeily.get());
			}
			st.append(" ]\n");
			st.append("alpha = ").append(this.alpha.get()).append("\n");
			st.append("beta = ").append(this.beta.get()).append("\n");
			st.append("gamma = ").append(this.gamma.get()).append("\n");
			st.append("delta = ").append(this.delta.get()).append("\n");
			st.append("epsilon = [");
			for (int i = 0; i < 4; i++) {
				st.append(" ").append(this.epsilon.get(i));
			}
			st.append(" ]\n");
			LOGGER.info(st.toString());
		}
	}


	private static class IDVComparator implements java.util.Comparator<IntDomainVar> {
		public int compare(IntDomainVar o1, IntDomainVar o2) throws ClassCastException {
			return (o1.getVal() > o2.getVal() ? 1 : (o1.getVal() < o2.getVal() ? -1 : 0));
		}
	}

	private static class SIComparator implements java.util.Comparator<IStateInt> {
		public int compare(IStateInt o1, IStateInt o2) throws ClassCastException {
			return (o1.get() > o2.get() ? 1 : (o1.get() < o2.get() ? -1 : 0));
		}
	}

}
