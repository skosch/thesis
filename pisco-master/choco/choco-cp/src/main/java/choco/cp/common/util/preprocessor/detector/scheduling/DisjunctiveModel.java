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

package choco.cp.common.util.preprocessor.detector.scheduling;

import java.util.Iterator;

import choco.Choco;
import choco.kernel.common.IDotty;
import choco.kernel.model.Model;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.ITemporalRelation;
import choco.kernel.model.constraints.TemporalConstraint;
import choco.kernel.model.variables.MultipleVariables;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;

public class DisjunctiveModel extends DisjunctiveGraph<TemporalConstraint> {

	public final Model model;
	
	public DisjunctiveModel(Model model) {
		super(model.getNbStoredMultipleVars());
		this.model = model;
	}

	
	public final Model getModel() {
		return model;
	}

	public final int setupTime(TaskVariable i, TaskVariable j) {
		return setupTime(i.getHook(), j.getHook());
	}

	public final boolean containsArc(TaskVariable i, TaskVariable j) {
		return containsArc(i.getHook(), j.getHook());
	}

	public final boolean containsEdge(TaskVariable i, TaskVariable j) {
		return containsEdge(i.getHook(), j.getHook());
	}
	
	public final boolean containsRelation(TaskVariable i, TaskVariable j) {
		return containsRelation(i.getHook(), j.getHook());
	}

	
	public final void safeAddArc(TaskVariable i, TaskVariable j) {
		safeAddArc(i.getHook(), j.getHook(), j.start().getLowB() - i.end().getUppB());
	}

	private boolean mergeSetupTime(int key, int setupTime) {
		if(setupTimes.get(key) < setupTime) {
			setupTimes.put(key, setupTime);
			return true;
		}else return false;
	}

	private boolean mergeArc(int i, int j, int setupTime, TemporalConstraint c) {
		final int key = getKey(i, j);
		if( mergeSetupTime(key, setupTime) ) {
			final TemporalConstraint cdm = storedConstraints.get(key);
			if(cdm == null) {
				storedConstraints.put(key, c);
				return false;
			} else {
				if( i == cdm.getOHook()) cdm.setForwardSetup(setupTime);
				else cdm.setBackwardSetup(setupTime);
			}
		}
		return true;
	}

	/**
	 * @return deleteC
	 */
	public boolean safeAddArc(TemporalConstraint c) {
		assert c.isFixed();
		final int i = c.getOHook();
		final int j = c.getDHook();
		if(c.getDirVal() == ITemporalRelation.BWD) {
			//add backward arc
			if(containsArc(j, i)) return mergeArc(j, i, c.backwardSetup(), c); 
			else addArc(j, i, c.backwardSetup(), c);
		}else {
			//add forward arc
			if(containsArc(i, j)) return mergeArc(i, j, c.forwardSetup(), c); 
			else addArc(i, j, c.forwardSetup(), c);
		}
		return false;
	}

	private void mergeFwdSetup(int key,int setupTime, TemporalConstraint dest) {
		if(mergeSetupTime(key, setupTime)) dest.setForwardSetup(setupTime);
	}

	private void mergeBwdSetup(int key,int setupTime, TemporalConstraint dest) {
		if(mergeSetupTime(key, setupTime)) dest.setBackwardSetup(setupTime);
	}

	private AddEdgeStatus mergeEdge(int i, int j, TemporalConstraint ct) {
		final int key1 = getKey(i, j);
		final int key2 = getKey(j, i);
		final TemporalConstraint cij = storedConstraints.get(key1);
		final TemporalConstraint cji = storedConstraints.get(key2);
		if(cij != null) {
			mergeFwdSetup(key1, ct.forwardSetup(), cij);
			mergeBwdSetup(key2, ct.backwardSetup(), cij);
			return new AddEdgeStatus(true, cij.getDirection());
		} else if(cji != null) {
			mergeBwdSetup(key1, ct.forwardSetup(), cji);
			mergeFwdSetup(key2, ct.backwardSetup(), cji);
			return new AddEdgeStatus(true, null, cji.getDirection());
		} else throw new ModelException("No Edge");
	}

	
	final static class AddEdgeStatus {
		
		public boolean deleteC;
		
		public IntegerVariable repV;
		
		public IntegerVariable oppV;

		public AddEdgeStatus(boolean deleteC) {
			this(deleteC, null, null);
		}
		
		public AddEdgeStatus(boolean deleteC, IntegerVariable repV) {
			this(deleteC, repV, null);
		}
		
		public AddEdgeStatus(boolean deleteC, IntegerVariable repV,
				IntegerVariable oppV) {
			super();
			this.deleteC = deleteC;
			this.repV = repV;
			this.oppV = oppV;
		}
	}
	
	public final AddEdgeStatus safeAddEdge(TemporalConstraint c) {
		assert ! c.getDirection().isConstant();
		final int i = c.getOHook();
		final int j = c.getDHook();
		if( containsArc(i, j)) {
			return new AddEdgeStatus(mergeArc(i, j, c.forwardSetup(), c), Choco.ONE);
		}else if(containsArc(j, i)) {
			return new AddEdgeStatus(mergeArc(j, i, c.backwardSetup(), c), Choco.ZERO);
		} else if(containsEdge(i, j)) {
			return mergeEdge(i, j, c);
		} else {
			addEdge(i, j, c);
			return new AddEdgeStatus(false);
		}
	}
	
	@Override
	protected void writeArcAttributes(StringBuilder b, int i, int j) {
		if(containsConstraint(i, j)) super.writeArcAttributes(b, i, j);
		else writeAttributes(b, ARC_COLOR, STY_BOLD_DASHED);
	}

	@Override
	protected final StringBuilder toDottyNodes() {
		final StringBuilder  b = new StringBuilder();
		Iterator<MultipleVariables> iter = model.getMultipleVarIterator();
		while(iter.hasNext()) {
			final MultipleVariables mv = iter.next();
			if (mv instanceof IDotty) b.append(((IDotty) mv).toDotty()).append('\n');
		}
		return b;
	}
}
