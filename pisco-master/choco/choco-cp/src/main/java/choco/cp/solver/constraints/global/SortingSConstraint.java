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


import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.integer.IntVar;

import java.util.Arrays;
import java.util.logging.Level;

/**
 * <code>SortingConstraint</code> is a constraint that ensures
 * that a vector is the sorted version of a second one. The filtering
 * algorithm is the version of Kurt Mehlhorn and Sven Thiel, from
 * CP'00 (<i>Faster algorithms for Bound-Consistency of the Sortedness
 * and the Alldifferent Constraint</i>).
 *
 * @author <a href="mailto:sylvain.bouveret@cert.fr">Sylvain Bouveret</a>
 * @version 1.0
 */

public final class SortingSConstraint extends AbstractLargeIntSConstraint {

	private int n;
	// These fields are the temporary structures used by the algorithms.
	// Declaring these variables as attributes avoid for wasting time
	// to re-declare them during each filtering process.

	private PriorityQueue pQueue;
	private IntDomainVar[] x, y;
	private int[] f, fPrime;
	private int[][] xyGraph;
	private int[] dfsNodes;
	private int[] sccNumbers;
	private int currentSccNumber;
	private int[] tmpArray;
	private int[][] sccSequences;
	private Stack1 s1;
	private Stack2 s2;
	private int[] recupStack = new int[3];
	private int[] recupStack2 = new int[3];

	/**
	 * Creates a new <code>SortingConstraint</code> instance.
	 *
	 * @param x the first array of integer variables
     * @param y the second array of integer variables
     */
	public SortingSConstraint(IntDomainVar[] x, IntDomainVar[] y) {
		super(ConstraintEvent.LINEAR, SortingSConstraint.mergeIntVarArrays(x, y));
		if (x.length != y.length || x.length == 0 || y.length == 0) {
			throw new IllegalArgumentException("SortingConstraint Error: the two vectors "
					+ "must be of the same (non zero) size");
		}
		this.n = x.length;
		this.x = x;
		this.y = y;
		this.f = new int[this.n];
		this.fPrime = new int[this.n];
		this.xyGraph = new int[this.n][this.n];
		this.sccSequences = new int[this.n][this.n];
		this.dfsNodes = new int[this.n];
		this.sccNumbers = new int[this.n];
		this.tmpArray = new int[this.n];
		this.pQueue = new PriorityQueue(this.n);
		this.s1 = new Stack1(this.n);
		this.s2 = new Stack2(this.n);
	}


	public void boundConsistency() throws ContradictionException {
		int i, jprime, tmp, j, k;

		for (i = 0; i < this.n; i++) {
			Arrays.fill(this.xyGraph[i], -1);
			Arrays.fill(this.sccSequences[i], -1);
		}

		this.pQueue.clear();// It may happen that the queue is not empty if the previous run raised a ContradictionException.

		/////////////////////////////////////////////////////////////
		// Normalizing the vectors...
		/////////////////////////////////////////////////////////////

		for (i = 1; i < this.n; i++) {
			if (y[i].getInf() < y[i - 1].getInf()) {
				y[i].updateInf(y[i - 1].getInf(), this, false);
			}
		}

		for (i = this.n - 2; i >= 0; i--) {
			if (y[i].getSup() > y[i + 1].getSup()) {
				y[i].updateSup(y[i + 1].getSup(), this, false);
			}
		}

		/////////////////////////////////////////////////////////////
		// Computing the perfect maching f... (optimized !)
		/////////////////////////////////////////////////////////////

		for (i = 0; i < this.n; i++) {
			if ((x[i].getInf() >= y[0].getInf() && x[i].getInf() <= y[0].getSup())
					|| (x[i].getSup() >= y[0].getInf() && x[i].getSup() <= y[0].getSup())
					|| (y[0].getInf() >= x[i].getInf() && y[0].getInf() <= x[i].getSup())
					|| (y[0].getSup() >= x[i].getInf() && y[0].getSup() <= x[i].getSup())) {
				this.pQueue.addElement(i, x[i].getSup());
			}
		}

		this.f[0] = this.computeF(0);

		for (j = 1; j < this.n; j++) {
			for (i = 0; i < this.n; i++) {
				if (x[i].getInf() > y[j - 1].getSup() && x[i].getInf() <= y[j].getSup()) {
					this.pQueue.addElement(i, x[i].getSup());
				}
			}
			this.f[j] = this.computeF(j);
		}

		/////////////////////////////////////////////////////////////
		// Narrowing the upper bounds of y...
		/////////////////////////////////////////////////////////////

		for (i = 0; i < this.n; i++) {
			if (x[this.f[i]].getSup() < y[i].getSup()) {
				y[i].updateSup(x[this.f[i]].getSup(), this, false);
			}
		}

		/////////////////////////////////////////////////////////////
		// Computing the perfect maching f'... (optimized !)
		/////////////////////////////////////////////////////////////

		this.pQueue.clear();

		for (i = 0; i < this.n; i++) {
			if ((x[i].getInf() >= y[this.n - 1].getInf() && x[i].getInf() <= y[this.n - 1].getSup())
					|| (x[i].getSup() >= y[this.n - 1].getInf() && x[i].getSup() <= y[this.n - 1].getSup())
					|| (y[this.n - 1].getInf() >= x[i].getInf() && y[this.n - 1].getInf() <= x[i].getSup())
					|| (y[this.n - 1].getSup() >= x[i].getInf() && y[this.n - 1].getSup() <= x[i].getSup())) {
				this.pQueue.addElement(i, -x[i].getInf());
			}
		}

		this.fPrime[this.n - 1] = this.computeFPrime(this.n - 1);

		for (j = this.n - 2; j >= 0; j--) {
			for (i = 0; i < this.n; i++) {
				if (x[i].getSup() < y[j + 1].getInf() && x[i].getSup() >= y[j].getInf()) {
					this.pQueue.addElement(i, -x[i].getInf());
				}
			}
			this.fPrime[j] = this.computeFPrime(j);
		}

		/////////////////////////////////////////////////////////////
		// Narrowing the lower bounds of y...
		/////////////////////////////////////////////////////////////

		for (i = 0; i < this.n; i++) {
			if (x[this.fPrime[i]].getInf() > y[i].getInf()) {
				y[i].updateInf(x[this.fPrime[i]].getInf(), this, false);
			}
		}

		/////////////////////////////////////////////////////////////
		// Computing the strong connected components (optimized)...
		/////////////////////////////////////////////////////////////

		for (j = 0; j < this.n; j++) { // for each y
			tmp = 0;
			jprime = this.f[j]; // jprime is the number of x associated with y_j
			for (i = 0; i < this.n; i++) { // for each other y
				if (j != i && ((x[jprime].getInf() >= y[i].getInf() && x[jprime].getInf() <= y[i].getSup())
						|| (x[jprime].getSup() >= y[i].getInf() && x[jprime].getSup() <= y[i].getSup())
						|| (y[i].getInf() >= x[jprime].getInf() && y[i].getInf() <= x[jprime].getSup())
						|| (y[i].getSup() >= x[jprime].getInf() && y[i].getSup() <= x[jprime].getSup()))) {
					this.xyGraph[j][tmp] = i;
					tmp++;
				}
			}
		}

		this.dfs2();

		/////////////////////////////////////////////////////////////
		// Narrowing the lower bounds of x... (to be optimized)
		/////////////////////////////////////////////////////////////

		Arrays.fill(this.tmpArray, 0);
		for (i = 0; i < this.n; i++) {
			this.sccSequences[this.sccNumbers[i]][tmpArray[this.sccNumbers[i]]] = i;
			tmpArray[this.sccNumbers[i]]++;
		}

		for (i = 0; i < this.n && this.sccSequences[i][0] != -1; i++) { // for each strongly connected component...
			for (j = 0; j < this.n && this.sccSequences[i][j] != -1; j++) { // for each x of the component
				jprime = this.f[this.sccSequences[i][j]];
				for (k = 0; k < this.n && this.sccSequences[i][k] != -1 && x[jprime].getInf() > y[this.sccSequences[i][k]].getSup(); k++)
				{
				}
				// scan the sequence of the ys of the connected component, until one becomes greater than or equal to x
				assert (this.sccSequences[i][k] != -1);
				if (y[this.sccSequences[i][k]].getInf() > x[jprime].getInf()) {
					x[jprime].updateInf(y[this.sccSequences[i][k]].getInf(), this, false);
				}
			}
		}

		/////////////////////////////////////////////////////////////
		// Narrowing the upper bounds of x... (to be optimized)
		/////////////////////////////////////////////////////////////

		Arrays.fill(this.tmpArray, 0);
		for (i = this.n - 1; i >= 0; i--) {
			this.sccSequences[this.sccNumbers[i]][tmpArray[this.sccNumbers[i]]] = i;
			tmpArray[this.sccNumbers[i]]++;
		}

		for (i = 0; i < this.n && this.sccSequences[i][0] != -1; i++) { // for each strongly connected component...
			for (j = 0; j < this.n && this.sccSequences[i][j] != -1; j++) { // for each x of the component
				jprime = this.f[this.sccSequences[i][j]];
				for (k = 0; k < this.n && this.sccSequences[i][k] != -1 && x[jprime].getSup() < y[this.sccSequences[i][k]].getInf(); k++)
				{
				}
				// scan the sequence of the ys of the connected component, until one becomes lower than or equal to x
				assert (this.sccSequences[i][k] != -1);
				if (y[this.sccSequences[i][k]].getSup() < x[jprime].getSup()) {
					x[jprime].updateSup(y[this.sccSequences[i][k]].getSup(), this, false);
				}
			}
		}
	}


	private int computeF(int j) throws ContradictionException {
		if (this.pQueue.isEmpty()) {
			propagationEngine.raiseContradiction(this);
		}
		int i = this.pQueue.pop();
		if (x[i].getSup() < y[j].getInf()) {
			propagationEngine.raiseContradiction(this);
		}

		return i;
	}

	private int computeFPrime(int j) throws ContradictionException {
		if (this.pQueue.isEmpty()) {
			propagationEngine.raiseContradiction(this);
		}
		int i = this.pQueue.pop();
		if (x[i].getInf() > y[j].getSup()) {
			propagationEngine.raiseContradiction(this);
		}

		return i;
	}


	private void dfs2() {
		Arrays.fill(this.dfsNodes, 0);
		this.s1.clear();
		this.s2.clear();
		this.currentSccNumber = 0;

		int i;
		for (i = 0; i < this.n; i++) {
			if (this.dfsNodes[i] == 0) {
				this.dfsVisit2(i);
			}
		}
		while (!this.s1.isEmpty()) {
			i = this.s1.pop();
			this.sccNumbers[i] = currentSccNumber;
		}
		this.s2.pop();
	}

	private void dfsVisit2(int node) {
		int i;
		this.dfsNodes[node] = 1;
		if (this.s2.isEmpty()) {
			this.s1.push(node);
			this.s2.push(node, node, x[f[node]].getSup());
			i = 0;
			while (xyGraph[node][i] != -1) {
				if (dfsNodes[xyGraph[node][i]] == 0) {
					this.dfsVisit2(xyGraph[node][i]);
				}
				i++;
			}
		} else {
			this.s2.peek(this.recupStack);
			if (this.recupStack[2] < y[node].getInf()) { // the topmost component cannot reach "node".
				while ((i = this.s1.pop()) != this.recupStack[0]) {
					this.sccNumbers[i] = currentSccNumber;
				}
			this.sccNumbers[i] = currentSccNumber;
			this.s2.pop();
			currentSccNumber++;
			}
			this.s1.push(node);
			this.recupStack[0] = node;
			this.recupStack[1] = node;
			this.recupStack[2] = this.x[this.f[node]].getSup();
			this.mergeStack(node);
			i = 0;
			while (xyGraph[node][i] != -1) {
				if (dfsNodes[xyGraph[node][i]] == 0) {
					this.dfsVisit2(xyGraph[node][i]);
				}
				i++;
			}
		}

		this.dfsNodes[node] = 2;
	}

	private boolean mergeStack(int node) {
		this.s2.peek(this.recupStack2);
		boolean flag = false;
		while (!this.s2.isEmpty() && y[this.recupStack2[1]].getSup() >= x[this.f[node]].getInf()) {
			flag = true;
			this.recupStack[0] = this.recupStack2[0];
			this.recupStack[1] = node;
			this.recupStack[2] = this.recupStack[2] > this.recupStack2[2] ? this.recupStack[2] : this.recupStack2[2];
			this.s2.pop();
			this.s2.peek(this.recupStack2);
		}
		this.s2.push(this.recupStack[0], this.recupStack[1], this.recupStack[2]);
		return flag;
	}


	/**
	 * A utility class method that merges two <code>IntVar</code>
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
	 * This method is invoked during the first propagation.
	 *
	 * @throws ContradictionException if a variable has an empty domain.
	 */
	@Override
	public void awake() throws ContradictionException {
		this.boundConsistency();
	}


	/**
	 * This methode propagates the constraint events.
	 *
	 * @throws ContradictionException if a variable has an empty domain.
	 */
	@Override
	public void propagate() throws ContradictionException {
		this.boundConsistency();
	}


	/**
	 * This method is called when a variable has been instanciated
	 *
	 * @param idx the index of the instanciated variable.
	 */

	@Override
	public void awakeOnInst(int idx) throws ContradictionException {
		this.constAwake(false);
		//this.boundConsistency();
	}

	/**
	 * This method is called when the minimal value of the domain of a variable
	 * has been updated.
	 *
	 * @param idx the index of the variable whose domain has been updated.
	 */

	@Override
	public void awakeOnInf(int idx) throws ContradictionException {
		this.constAwake(false);
		//this.boundConsistency();
	}

	/**
	 * This method is called when the maximal value of the domain of a variable
	 * has been updated.
	 *
	 * @param idx the index of the variable whose domain has been updated.
	 */

	@Override
	public void awakeOnSup(int idx) throws ContradictionException {
		this.constAwake(false);
		//this.boundConsistency();
	}


	/**
	 * This method checks if the constraint is satisfied or not.
	 *
	 * @return <code>true</code> if and only if the constraint is satisfied.
	 */
	@Override
	public boolean isSatisfied(int[] tuple) {
		int[] x = new int[this.n];
		int[] y = new int[this.n];
		for (int i = 0; i < n; i++) {
			x[i] = tuple[i];
			y[i] = tuple[n + i];
		}
		java.util.Arrays.sort(x);

		int i;
		for (i = 0; i < n && x[i] == y[i]; i++) {
		}
		return i == n;
	}

	public final void printVectors() {
		if(LOGGER.isLoggable(Level.INFO)) {
			StringBuilder st = new StringBuilder();
			st.append("x = ( ");
			for (IntDomainVar aX : x) {
				st.append("[").append(aX.getInf()).append(", ").append(aX.getSup()).append("]");
			}
			st.append(")");
			st.append("y = ( ");
			for (IntDomainVar aY : y) {
				st.append("[").append(aY.getInf()).append(",").append(aY.getSup()).append("] ");
			}
			st.append(")");
			LOGGER.info(st.toString());
		}
	}

	@Override
	public String pretty() {
		StringBuilder sb = new StringBuilder();
		sb.append("Sorting({");
		for (int i = 0; i < x.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(x[i].pretty());
		}
		sb.append("}) = {");
		for (int i = 0; i < y.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(y[i].pretty());
		}
		sb.append("}");
		return sb.toString();
	}


	private static class PriorityQueue {
		private int n;
		private int[] indices;
		private int[] values;
		private int[] pointers;
		private int first, lastElt;

		public PriorityQueue(int _n) {
			this.n = _n;
			this.indices = new int[_n];
			this.pointers = new int[_n];
			this.values = new int[_n];
			this.clear();
		}

		/**
		 * Adds an integer into the list. The element is inserted at its right
		 * place (the list is sorted) in O(n).
		 *
		 * @param index the element to insert.
		 * @param value the value to be used for the comparison of the elements to add.
		 * @return <code>true</code> if and only if the list is not full.
		 */
		public boolean addElement(int index, int value) {
			int i;
			int j = -1;
			if (this.lastElt == this.n) {
				return false;
			}
			this.indices[this.lastElt] = index;
			this.values[this.lastElt] = value;

			for (i = this.first; i != -1 && this.values[i] <= value; i = this.pointers[i]) {
				j = i;
			}
			this.pointers[this.lastElt] = i;
			if (j == -1) {
				this.first = this.lastElt;
			} else {
				this.pointers[j] = this.lastElt;
			}
			this.lastElt++;
			return true;
		}

		/**
		 * Returns and removes the element with highest priority (i.e. lowest value) in O(1).
		 *
		 * @return the lowest element.
		 */
		public int pop() {
			if (this.isEmpty()) {
				return -1;
			}
			int elt = this.indices[this.first];
			this.first = this.pointers[this.first];
			return elt;
		}

		/**
		 * Tests if the list is empty or not.
		 *
		 * @return <code>true</code> if and only if the list is empty.
		 */
		public boolean isEmpty() {
			return (this.first == -1);
		}

		/**
		 * Clears the list.
		 */
		public void clear() {
			this.first = -1;
			this.lastElt = 0;
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder("<");
			for (int i = this.first; i != -1; i = this.pointers[i]) {
                s.append(" ").append(this.indices[i]);
			}
			s.append(" >");
			return s.toString();
		}
	}

	private static class Stack1 {
		private int[] values;
		private int n;
		private int nbElts = 0;

		public Stack1(int _n) {
			this.n = _n;
			this.values = new int[_n];
		}

		public boolean push(int elt) {
			if (this.nbElts == this.n) {
				return false;
			}
			this.values[this.nbElts] = elt;
			this.nbElts++;
			return true;
		}

		public int pop() {
			if (this.isEmpty()) {
				return -1;
			}
			this.nbElts--;
			return this.values[this.nbElts];
		}

		public int peek() {
			if (this.isEmpty()) {
				return -1;
			}
			return this.values[this.nbElts - 1];
		}

		public boolean isEmpty() {
			return (this.nbElts == 0);
		}

		public void clear() {
			this.nbElts = 0;
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			for (int i = 0; i < this.nbElts; i++) {
                s.append(" ").append(this.values[i]);
			}
			return s.toString();
		}
	}

	private static class Stack2 {
		private int[] roots;
		private int[] rightMosts;
		private int[] maxXs;
		private int n;
		private int nbElts = 0;

		public Stack2(int _n) {
			this.n = _n;
			this.roots = new int[_n];
			this.rightMosts = new int[_n];
			this.maxXs = new int[_n];
		}

		public boolean push(int root, int rightMost, int maxX) {
			if (this.nbElts == this.n) {
				return false;
			}
			this.roots[this.nbElts] = root;
			this.rightMosts[this.nbElts] = rightMost;
			this.maxXs[this.nbElts] = maxX;
			this.nbElts++;
			return true;
		}

		public boolean pop() {
			if (this.isEmpty()) {
				return false;
			}
			this.nbElts--;
			return true;
		}

		public boolean pop(int[] x) {
			if (this.isEmpty()) {
				return false;
			}
			this.nbElts--;
			x[0] = this.roots[this.nbElts];
			x[1] = this.rightMosts[this.nbElts];
			x[2] = this.maxXs[this.nbElts];
			return true;
		}

		public boolean peek(int[] x) {
			if (this.isEmpty()) {
				return false;
			}
			x[0] = this.roots[this.nbElts - 1];
			x[1] = this.rightMosts[this.nbElts - 1];
			x[2] = this.maxXs[this.nbElts - 1];
			return true;
		}

		public boolean isEmpty() {
			return (this.nbElts == 0);
		}

		public void clear() {
			this.nbElts = 0;
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			for (int i = 0; i < this.nbElts; i++) {
                s.append(" <").append(this.roots[i]).append(", ")
                        .append(this.rightMosts[i]).append(", ").append(this.maxXs[i]).append(">");
			}
			return s.toString();
		}
	}

}
