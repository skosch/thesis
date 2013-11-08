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

package choco.cp.solver.search.task.profile;

import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import choco.cp.common.util.preprocessor.detector.scheduling.DisjunctiveSModel;
import choco.cp.solver.constraints.global.scheduling.precedence.ITemporalSRelation;
import choco.cp.solver.search.task.OrderingValSelector;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintType;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.global.scheduling.IResource;
import choco.kernel.solver.search.integer.IntVarValPair;
import choco.kernel.solver.search.integer.VarValPairSelector;
import choco.kernel.solver.variables.scheduling.ITask;


public final class ProfileSelector implements VarValPairSelector {

	private final Solver solver;

	private final OrderingValSelector precSelector;

	private DisjunctiveSModel disjSModel;

	private final ProbabilisticProfile profiles;

	public final IResource<?>[] rscL;

	public ProfileSelector(Solver solver, IResource<?>[] resources, DisjunctiveSModel disjSModel, OrderingValSelector precSelector) {
		super();
		this.solver=solver;
		this.disjSModel = disjSModel;
		this.precSelector = precSelector;
		// FIXME - Set the minimal number of involved tasks - created 12 août 2011 by Arnaud Malapert
		profiles = new ProbabilisticProfile(solver, disjSModel);
		// FIXME - profiles.precStore = precStore; - created 12 août 2011 by Arnaud Malapert
		rscL = resources;
	}

	public ProfileSelector(Solver solver, DisjunctiveSModel disjSModel, OrderingValSelector precSelector) {
		super();
		this.solver=solver;
		this.disjSModel = disjSModel;
		this.precSelector = precSelector;
		profiles = new ProbabilisticProfile(solver, disjSModel);
		rscL = new IResource<?>[solver.getModel().getNbConstraintByType(ConstraintType.DISJUNCTIVE)];
		Iterator<Constraint> iter = solver.getModel().getConstraintByType(ConstraintType.DISJUNCTIVE);
		int cpt = 0;
		while(iter.hasNext()) {
			rscL[cpt++] = (IResource<?>) solver.getCstr(iter.next());
		}
	}

	@Override
	public IntVarValPair selectVarValPair() throws ContradictionException {
		//compute maximal contention point
		profiles.initializeEvents();
		profiles.computeMaximum(rscL);
		//find best task pair
		final int c = profiles.getMaxProfileCoord();
		if(c >= 0) {
			BitSet involved = profiles.getInvolvedInMaxProf();
			ITemporalSRelation sdisjunct = null;
			double maxContrib = Double.MIN_VALUE;
			for (int i = involved.nextSetBit(0); i >= 0; i = involved.nextSetBit(i + 1)) {
				final ITask t1 = solver.getTaskVarQuick(i);
				assert(t1.getID() == i);
				final double contrib1 = profiles.getIndividualContribution(t1,c);
				for (int j = involved.nextSetBit(i+1); j >= 0; j = involved.nextSetBit(j + 1)) {
					final ITask t2 = solver.getTaskVarQuick(j);
					assert(t2.getID() == j);
					final double contrib = contrib1 + profiles.getIndividualContribution(t1,c);
					if(contrib > maxContrib && disjSModel.containsEdge(t1, t2)) {
						final ITemporalSRelation disjunct = disjSModel.getConstraint(t1, t2);
						if(! disjunct.isFixed()) {
							sdisjunct=disjunct;
							maxContrib = contrib;
						}
					}
				}
			}
			assert(sdisjunct != null);
			return new IntVarValPair(sdisjunct.getDirection(), precSelector.getBestVal(sdisjunct));
		}
		assert(disjSModel.isFixed());
		return null;
	}


}