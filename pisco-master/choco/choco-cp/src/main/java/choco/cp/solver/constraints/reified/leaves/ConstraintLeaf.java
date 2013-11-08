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

package choco.cp.solver.constraints.reified.leaves;

import choco.cp.solver.constraints.reified.ReifiedFactory;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.integer.AbstractIntSConstraint;
import choco.kernel.solver.constraints.reified.BoolNode;
import choco.kernel.solver.constraints.reified.INode;
import choco.kernel.solver.constraints.reified.NodeType;
import choco.kernel.solver.variables.integer.IntDomainVar;

/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 22 mai 2008
 * Since : Choco 2.0.0
 *
 */
public final class ConstraintLeaf extends INode implements BoolNode {

    protected AbstractIntSConstraint c;
	protected AbstractIntSConstraint oppositec;
	protected int[] idxtuple;
	protected int[] tup;


    public ConstraintLeaf(SConstraint c) {
        super(NodeType.CONSTRAINTLEAF);
        this.c = (AbstractIntSConstraint) c;
	    idxtuple = new int[c.getNbVars()];
	    tup = new int[c.getNbVars()];
    }

	public ConstraintLeaf(SConstraint c, SConstraint oppositec) {
        super(NodeType.CONSTRAINTLEAF);
	    this.c = (AbstractIntSConstraint) c;
	    idxtuple = new int[c.getNbVars()];
	    tup = new int[c.getNbVars()];
        this.oppositec = (AbstractIntSConstraint) oppositec;
	}

    public boolean checkTuple(int[] tuple) {
        setTuple(tuple);
	    return c.isSatisfied(tup);
    }

  public void setTuple(int[] tuple) {
		for (int i = 0; i < tup.length; i++) {
			tup[i] = tuple[idxtuple[i]];
		}
	}

	public IntDomainVar extractResult(Solver s) {
		IntDomainVar v = s.createBooleanVar(StringUtils.randomName());
		if (oppositec != null)
			s.post(ReifiedFactory.builder(v,c,oppositec, s));
		else s.post(ReifiedFactory.builder(v, c,s));
		return v;
	}

  public SConstraint extractConstraint(Solver s) {
    return c;
  }

  public boolean isReified() {
		return false;
	}

    public int getNbSubTrees() {
        return 0;
    }

    public boolean isDecompositionPossible() {
		return true;
	}

	public IntDomainVar[] getScope(Solver s) {
		IntDomainVar[] vars = new IntDomainVar[c.getNbVars()];
		for (int i = 0; i < vars.length; i++) {
			vars[i] = c.getVar(i);
		}
		return vars;
	}

	public void setIndexes(IntDomainVar[] vs) {
		idxtuple = new int[c.getNbVars()];
		for (int i = 0; i < c.getNbVars(); i++) {
			IntDomainVar v = c.getVar(i);
			for (int j = 0; j < vs.length; j++) {
				if (vs[j].equals(v)) {
			        idxtuple[i] = j;
					break;
				}
			}
		}
	}

    public String pretty() {
        return c.pretty();
    }

    public int countNbVar() {
        return c.getNbVars();
    }

}
