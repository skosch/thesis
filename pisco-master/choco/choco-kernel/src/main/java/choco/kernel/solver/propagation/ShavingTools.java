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

package choco.kernel.solver.propagation;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.search.IObjectiveManager;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;


public class ShavingTools {
	// TODO - Add logging statement - created 5 juil. 2011 by Arnaud Malapert

	public final Solver solver;

	public final IntDomainVar[] vars;

	public boolean shaveLowerBound = false;

	public boolean detectLuckySolution = false;

	private int nbRemovals;

	/**
	 * 
	 * @param solver
	 * @param vars The scope of shaving algorithm must not contain the objective.
	 */
	public ShavingTools(Solver solver, IntDomainVar[] vars) {
		super();
		this.solver = solver;
		this.vars = checkAbsentVar(vars, solver.getObjective() );
	}

	public final boolean isShavingLowerBound() {
		return shaveLowerBound;
	}

	public final void setShavingLowerBound(boolean b) {
		shaveLowerBound = b;
	}

	public final boolean isDetectingLuckySolution() {
		return detectLuckySolution;
	}

	public final void setDetectLuckySolution(boolean detectLuckySolution) {
		this.detectLuckySolution = detectLuckySolution;
	}

	private static IntDomainVar[] checkAbsentVar(IntDomainVar[] vars, Var var) {
		if(var != null && var instanceof IntDomainVar) {
			int idx = -1;
			for (int i = 0; i < vars.length; i++) {
				if(vars[i] == var) {idx = i; break;}
			}
			if(idx >= 0) {
				final IntDomainVar[] newVars = new IntDomainVar[vars.length - 1];
				System.arraycopy(vars, 0, newVars, 0, idx);
				System.arraycopy(vars, idx + 1, newVars, idx, vars.length - idx - 1);
				return newVars;
			}
		}
		return vars;
	}


	public final Solver getSolver() {
		return solver;
	}

	public final IntDomainVar[] getVars() {
		return vars;
	}

	public final int getNbRemovals() {
		return nbRemovals;
	}


	protected final void shaveVars() throws ContradictionException, LuckySolutionException {
		nbRemovals = 0;
		for (IntDomainVar var : vars) {
			if( ! var.isInstantiated() ) {
				if(var.hasEnumeratedDomain()) shaveEnumVar(var);
				else shaveBoundVar(var);			
			}
		}	
	}

	public final void shaving() throws ContradictionException {
		try {
			shaveVars();
		} catch (LuckySolutionException e) {}
	}

	protected void shaveEnumVar(IntDomainVar var) throws LuckySolutionException, ContradictionException {
		final DisposableIntIterator iter = var.getDomain().getIterator();
		try{
			while (iter.hasNext()) {
				shaving(var, iter.next());
			} 
		}finally {iter.dispose();}
	}

	protected void shaveBoundVar(IntDomainVar var) throws LuckySolutionException, ContradictionException {
		int oldNbR;
		do {
			oldNbR = nbRemovals;
			shaving(var, var.getInf());
		}while(nbRemovals > oldNbR);
		do {
			oldNbR = nbRemovals;
			shaving(var, var.getSup());
		}while(nbRemovals > oldNbR);
	}

	protected void shaving(IntDomainVar var, int val) throws LuckySolutionException, ContradictionException  {
		solver.worldPush();
		try {
			var.instantiate(val, null, true);
			solver.propagate();
			if( detectLuckySolution ) detectLuckySolution();	
			solver.worldPop();
		} catch (ContradictionException e) {
			solver.worldPop();
			nbRemovals++;
			var.removeVal(val, null, true);
			solver.propagate();
		}
	}

	public final void destructiveLowerBound(final IObjectiveManager objM) throws ContradictionException {
		boolean mem = detectLuckySolution;
		detectLuckySolution = true;
		objM.initBounds();
		try {
			while(shaveObjective(objM)) {
				objM.incrementFloorBound();
			}
			assert ! objM.isTargetInfeasible();
			if( ! objM.getObjectiveValue().equals(objM.getObjectiveFloor())) {
				try {
					objM.postFloorBound();
				} catch (ContradictionException e) {
					throw new SolverException("Destructive Lower Bound: Invalid bounds");
				}
				solver.propagate();
			}
		} catch (LuckySolutionException e) {}
		detectLuckySolution = mem;
	}


	protected final boolean shaveObjective(final IObjectiveManager objM) throws ContradictionException, LuckySolutionException {
		boolean shave = false;
		solver.worldPush();
		objM.postIncFloorBound();
		try {
			solver.propagate();
			detectLuckySolution();
			if(isShavingLowerBound()) shaveVars();
		} catch (ContradictionException e) {
			shave = true;
		} 
		solver.worldPop();
		return shave;
	}

	public Boolean nextBottomUp(IObjectiveManager objM) {
		try {
			objM.postIncFloorBound();
		} catch (ContradictionException e) {
			throw new SolverException("Destructive Lower Bound: Invalid bounds : "+objM.getObjectiveFloor());
		}
		try {
			solver.propagate();
			if(isShavingLowerBound()) shaving();
		} catch (ContradictionException e) {
			return Boolean.FALSE;
		}
		solver.worldPush();
		return solver.getSearchStrategy().nextSolution();
	}


	protected final void detectLuckySolution() throws LuckySolutionException {
		int n = solver.getNbIntVars();
		for (int i = 0; i < n; i++) {
			if( ! solver.getIntVarQuick(i).isInstantiated()) return;
		}
		n = solver.getNbSetVars();
		for (int i = 0; i < n; i++) {
			if( ! solver.getSetVarQuick(i).isInstantiated()) return;
		}
		if(solver.getNbRealVars() > 0) return; 
		// FIXME - How to handle real variables ? - created 4 juil. 2011 by Arnaud Malapert
		throw LuckySolutionException.SINGLOTON;
	}


	final static class LuckySolutionException extends Exception {

		private static final long serialVersionUID = -1476316199858738423L;

		public final static LuckySolutionException SINGLOTON = new LuckySolutionException();

		private LuckySolutionException() {
			super("Shaving lead to a solution");
		}


	}

}
