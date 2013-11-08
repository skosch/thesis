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

package choco.cp.solver.search.integer.varselector;

import choco.cp.solver.search.BranchingFactory;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.SConstraintType;
import choco.kernel.solver.propagation.listener.PropagationEngineListener;
import choco.kernel.solver.search.integer.DoubleHeuristicIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * History:
 * 2007-12-07 : FR_1873619 CPRU: DomOverDeg+DomOverWDeg
 * @deprecated @see {@link BranchingFactory}
 */
@Deprecated
public class DomOverWDeg extends DoubleHeuristicIntVarSelector implements PropagationEngineListener {

    private AbstractSConstraint reuseCstr;

	private static final int ABSTRACTCONTRAINT_EXTENSION =
			AbstractSConstraint.getAbstractSConstraintExtensionNumber("choco.cp.cpsolver.search.integer.varselector.DomOverWDeg");

	public DomOverWDeg(Solver solver) {
		super(solver);
        DisposableIterator<SConstraint> iter = solver.getConstraintIterator();
		for (; iter.hasNext();) {
			AbstractSConstraint c = (AbstractSConstraint) iter.next();
			c.addExtension(ABSTRACTCONTRAINT_EXTENSION);
		}
        iter.dispose();
		solver.getPropagationEngine().addPropagationEngineListener(this);
	}

	public DomOverWDeg(Solver solver, IntDomainVar[] vs) {
		super(solver, vs);
        DisposableIterator<SConstraint> iter = solver.getConstraintIterator();
		for (; iter.hasNext();) {
			AbstractSConstraint c = (AbstractSConstraint) iter.next();
			c.addExtension(ABSTRACTCONTRAINT_EXTENSION);
		}
        iter.dispose();
		solver.getPropagationEngine().addPropagationEngineListener(this);
	}

    /**
     * Define action to do just before a deletion.
     */
    @Override
    public void safeDelete() {
        solver.getPropagationEngine().removePropagationEngineListener(this);
    }

    public void initConstraintForBranching(SConstraint c) {
        c.addExtension(ABSTRACTCONTRAINT_EXTENSION);
    }

    public double getHeuristic(IntDomainVar v) {
		int dsize = v.getDomainSize();
		int weight = 0;
		// Calcul du poids:
		DisposableIntIterator c = v.getIndexVector().getIndexIterator();
		int idx = 0;
        while (c.hasNext()) {
            idx = c.next();
            reuseCstr = (AbstractSConstraint) v.getConstraint(idx);
			if (SConstraintType.INTEGER.equals(reuseCstr.getConstraintType())
                && reuseCstr.getNbVarNotInst() > 1) {
				weight += reuseCstr.getExtension(ABSTRACTCONTRAINT_EXTENSION).get() + reuseCstr.getFineDegree(v.getVarIndex(idx));
			}
        }
        c.dispose();
		if (weight == 0)
			return Double.MAX_VALUE;
		else
			return (double) dsize / ((double) weight);
	}

	public void contradictionOccured(ContradictionException e) {
		Object cause = e.getDomOverDegContradictionCause();
		if (cause != null) {
			reuseCstr = (AbstractSConstraint) cause;
            if(SConstraintType.INTEGER.equals(reuseCstr.getConstraintType())){
			    reuseCstr.getExtension(ABSTRACTCONTRAINT_EXTENSION).increment();
            }
		}
	}
}
