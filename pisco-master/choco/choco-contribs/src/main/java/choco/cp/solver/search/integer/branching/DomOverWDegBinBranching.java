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

package choco.cp.solver.search.integer.branching;

import choco.cp.solver.variables.integer.IntDomainVarImpl;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.SConstraintType;
import choco.kernel.solver.constraints.integer.AbstractIntSConstraint;
import choco.kernel.solver.search.IntBranchingDecision;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.search.integer.IntVarValPair;
import choco.kernel.solver.variables.AbstractVar;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Iterator;
import java.util.Random;

/* History:
 * 2008-04-23 : Creation : dom / wdeg needs to be a branching not just an heuristic to allow to deal with
 *              backtracking events !
 */
/**
 * WARNING ! This implementation suppose that the variables will not change. It
 * copies all variables in an array at the beginning !!
 * @deprecated use {@link DomOverWDegBinBranching2} instead.
 */
@Deprecated
public class DomOverWDegBinBranching extends AbstractAssignOrForbidBranching {
	private static final int CONSTRAINT_EXTENSION = AbstractSConstraint
	.getAbstractSConstraintExtensionNumber("choco.cp.cpsolver.search.integer.varselector.DomOverWDeg");

	private static final int VAR_EXTENSION = AbstractVar
	.getAbstractVarExtensionNumber("choco.cp.cpsolver.search.integer.varselector.DomOverWDeg");

    // Les variables parmis lesquelles on veut brancher !
	private IntDomainVarImpl[] vars;

	

	// a reference to a random object when random ties are wanted
	protected Random randomBreakTies;

	// Le constructeur avec :
	// * le solver pour fournir les variables
	// * l'heuristique de valeurs pour instantier une valeur
	public DomOverWDegBinBranching(Solver s, ValSelector valHeuri,
			IntDomainVar[] intDomainVars) {
		super(valHeuri);
        DisposableIterator<SConstraint> iter = s.getConstraintIterator();
		for (; iter.hasNext();) {
			AbstractSConstraint c = (AbstractSConstraint) iter.next();
			c.addExtension(CONSTRAINT_EXTENSION);
		}
        iter.dispose();
		for (int i = 0; i < s.getNbIntVars(); i++) {
			IntDomainVar v = s.getIntVar(i);
			v.addExtension(VAR_EXTENSION);
		}

		for (int val : s.getIntConstantSet()) {
			Var v = s.getIntConstant(val);
			v.addExtension(VAR_EXTENSION);
		}

		// On sauvegarde l'heuristique
		valSelector = valHeuri;
		vars = new IntDomainVarImpl[intDomainVars.length];
		for (int i = intDomainVars.length; --i >= 0;) {
			vars[i] = (IntDomainVarImpl) intDomainVars[i];
		}
	}

	public DomOverWDegBinBranching(Solver s, ValSelector valHeuri) {
		this(s, valHeuri, buildVars(s));
	}

	@Override
	public void initConstraintForBranching(SConstraint s) {
		s.addExtension(CONSTRAINT_EXTENSION);
	}

	private static IntDomainVarImpl[] buildVars(Solver s) {
		IntDomainVarImpl[] vars = new IntDomainVarImpl[s.getNbIntVars()];
		for (int i = 0; i < vars.length; i++) {
			vars[i] = (IntDomainVarImpl) s.getIntVar(i);
		}
		return vars;
	}

	@Override
	public void initBranching() {
		for (IntDomainVarImpl v : vars) {
			// Pour etre sur, on verifie toutes les contraintes... au cas ou une
			// d'entre elle serait deja instanti�e !!
			int weight = 0;
			int idx = 0;
			DisposableIntIterator c = v.getIndexVector().getIndexIterator();
			for (; c.hasNext();) {
				idx = c.next();
				AbstractSConstraint cstr = (AbstractSConstraint) v.getConstraint(idx);
				if (cstr.getNbVarNotInst() > 1) {
					weight += cstr
							.getExtension(CONSTRAINT_EXTENSION).get() + cstr.getFineDegree(v.getVarIndex(idx));
				}
			}
			c.dispose();
			v.getExtension(VAR_EXTENSION).set(weight);
		}
	}

	public void setRandomVarTies(int seed) {
		randomBreakTies = new Random(seed);
	}

	public Object selectBranchingObject() throws ContradictionException {
		int bestSize = 0;
		int bestWeight = 0;
		int ties = 1;
		IntDomainVar bestVariable = null;

		for (IntDomainVar var : vars) {
			if (var.isInstantiated())
				continue;

			final int weight = ((AbstractVar) var)
					.getExtension(VAR_EXTENSION).get();
			final int size = var.getDomainSize();

			if (bestVariable == null || weight * bestSize >= bestWeight * size) {
				if (bestVariable != null
						&& weight * bestSize == bestWeight * size) {
					if (randomBreakTies == null) {
						continue;
					}
					ties++;
					if (randomBreakTies.nextInt(ties) > 0) {
						continue;
					}
				} else {
					ties = 1;
				}
				bestVariable = var;
				bestSize = size;
				bestWeight = weight;
			}
		}
		if (bestVariable == null) {
			return null;
		}
		return new IntVarValPair(bestVariable, valSelector
				.getBestVal(bestVariable));
	}

	private void assign(IntDomainVar v) {
		for (Iterator<SConstraint> iter = v.getConstraintsIterator(); iter
		.hasNext();) {
			final AbstractSConstraint reuseCstr = (AbstractSConstraint) iter
			.next();
			if (SConstraintType.INTEGER.equals(reuseCstr.getConstraintType())
					&& reuseCstr.getNbVarNotInst() == 2) {
				for (int k = 0; k < reuseCstr.getNbVars(); k++) {
					AbstractVar var = (AbstractVar) ((AbstractIntSConstraint) reuseCstr)
					.getVar(k);
					if (var != v && !var.isInstantiated()) {
						var.getExtension(VAR_EXTENSION).set(-reuseCstr
								.getExtension(CONSTRAINT_EXTENSION).get());
					}
				}
			}
		}
	}

	private void unassign(IntDomainVar v) {
		for (Iterator<SConstraint> iter = v.getConstraintsIterator(); iter
		.hasNext();) {
			final AbstractSConstraint reuseCstr = (AbstractSConstraint) iter
			.next();
			if (SConstraintType.INTEGER.equals(reuseCstr.getConstraintType())) {
				if (reuseCstr.getNbVarNotInst() == 2) {
					for (int k = 0; k < reuseCstr.getNbVars(); k++) {
						AbstractVar var = (AbstractVar) ((AbstractIntSConstraint) reuseCstr)
						.getVar(k);
						if (var != v && !var.isInstantiated()) {
							((AbstractVar) var)
									.getExtension(VAR_EXTENSION).set(reuseCstr
											.getExtension(CONSTRAINT_EXTENSION).get());
						}
					}
				}
			}
		}
	}

	@Override
	public void goDownBranch(final IntBranchingDecision ctx) throws ContradictionException {
		final IntDomainVar v = ctx.getBranchingIntVar();
		if (ctx.getBranchIndex() == 0) {
			assign(v);
			v.setVal(ctx.getBranchingValue());
		} else {
			unassign(v);
			v.remVal(ctx.getBranchingValue());
		}
		// Calls to propagate() are useless since it is done in the SearchLoop
	}



	public void contradictionOccured(ContradictionException e) {
		Object cause = e.getDomOverDegContradictionCause();
		if (cause != null) {
			final AbstractSConstraint causeCstr = (AbstractSConstraint) cause;
			if (SConstraintType.INTEGER.equals(causeCstr.getConstraintType())) {
				try {
					causeCstr.getExtension(CONSTRAINT_EXTENSION).increment();
				} catch (NullPointerException npe) {
					// If there was a postCut, the extension has not been
					// generated at the Branching creation
					causeCstr.addExtension(CONSTRAINT_EXTENSION
                    );
					causeCstr.getExtension(CONSTRAINT_EXTENSION).increment();
				}
				for (int k = 0; k < causeCstr.getNbVars(); k++) {
					AbstractVar var = (AbstractVar) ((AbstractIntSConstraint) causeCstr)
					.getVar(k);
					var.getExtension(VAR_EXTENSION).increment();
				}
			}
		}
	}
}
