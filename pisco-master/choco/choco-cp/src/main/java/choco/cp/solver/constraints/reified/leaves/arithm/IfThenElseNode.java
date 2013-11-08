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

package choco.cp.solver.constraints.reified.leaves.arithm;

import choco.cp.solver.constraints.reified.IfThenElse;
import choco.cp.solver.constraints.reified.leaves.bool.AbstractBoolNode;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.integer.AbstractIntSConstraint;
import choco.kernel.solver.constraints.reified.ArithmNode;
import choco.kernel.solver.constraints.reified.BoolNode;
import choco.kernel.solver.constraints.reified.INode;
import choco.kernel.solver.constraints.reified.NodeType;
import choco.kernel.solver.variables.integer.IntDomainVar;

/*
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: 23 avr. 2008
 * Since : Choco 2.0.0
 *
 */
public final class IfThenElseNode extends AbstractBoolNode implements ArithmNode, BoolNode {

	public IfThenElseNode(INode[] subt) {
		super(subt, NodeType.IFTHENELSE);
	}

	public int eval(int[] tuple) {
        if(((BoolNode) subtrees[0]).checkTuple(tuple)){
	        return ((ArithmNode) subtrees[1]).eval(tuple);
        }else{
            return ((ArithmNode) subtrees[2]).eval(tuple);
        }
    }

	public boolean checkTuple(int[] tuple) {
		if(((BoolNode) subtrees[0]).checkTuple(tuple)){
	        return ((BoolNode) subtrees[1]).checkTuple(tuple);
        } else{
            return ((BoolNode) subtrees[2]).checkTuple(tuple);
		}
	}

	public IntDomainVar extractResult(Solver s) {
		IntDomainVar v1 = subtrees[0].extractResult(s);
		IntDomainVar v2 = subtrees[1].extractResult(s);
		IntDomainVar v3 = subtrees[2].extractResult(s);
		IntDomainVar v4;
		int a = Math.min(v2.getInf(),v3.getInf());
		int b = Math.max(v2.getSup(),v3.getSup());
        if(a==0 && b==1){
            v4 = s.createBooleanVar(StringUtils.randomName());
        }else
		if (v1.hasEnumeratedDomain() || v2.hasEnumeratedDomain()) {
			v4 = s.createEnumIntVar(StringUtils.randomName(), a, b);
		} else {
			v4 = s.createBoundIntVar(StringUtils.randomName(), a, b);
		}
		s.post(new IfThenElse(v1,(AbstractIntSConstraint) s.eq(v4,v2), (AbstractIntSConstraint) s.eq(v4,v3)));
        //((CPSolver)s).reifiedIntConstraint(v1,s.eq(v4,v2),s.eq(v4,v3)));
		return v4;
	}

  public SConstraint extractConstraint(Solver s) {
      IntDomainVar v1;
      if (checkIfConditionAlreadyABooleanVar()) {
        v1 = subtrees[0].getScope(s)[0];
      } else v1 = subtrees[0].extractResult(s);
      SConstraint c1;
      SConstraint c2;
      if(   subtrees[1] instanceof BoolNode
         && subtrees[2] instanceof BoolNode){
          c1 = ((BoolNode)subtrees[1]).extractConstraint(s);
          c2 = ((BoolNode)subtrees[2]).extractConstraint(s);
      }else{
          IntDomainVar v2 = subtrees[1].extractResult(s);
          IntDomainVar v3 = subtrees[2].extractResult(s);
          IntDomainVar v4;
          int a = Math.min(v2.getInf(), v3.getInf());
          int b = Math.max(v2.getSup(), v3.getSup());
          if (v1.hasEnumeratedDomain() || v2.hasEnumeratedDomain()) {
              v4 = s.createEnumIntVar(StringUtils.randomName(), a, b);
          } else {
              v4 = s.createBoundIntVar(StringUtils.randomName(), a, b);
          }
          c1 = s.eq(v4, v2);
          c2 = s.eq(v4, v3);
      }
      return new IfThenElse(v1,(AbstractIntSConstraint) c1, (AbstractIntSConstraint) c2);
  }

    /**
     * A common use of the If Then Else is to use
     * a boolean variable as a condition, in which cas it
     * is useless to introduce another one.
     * @return true is one implicated variable is already a boolean variable
     */
  protected boolean checkIfConditionAlreadyABooleanVar() {
     return subtrees[0].getType().equals(NodeType.EQ) &&
            ((subtrees[0].getSubtree(0).isAVariable() &&
              subtrees[0].getSubtree(0).isBoolean() &&
             subtrees[0].getSubtree(1).isCsteEqualTo(1)) ||
            (subtrees[0].getSubtree(1).isAVariable() &&
             subtrees[0].getSubtree(1).isBoolean() &&
             subtrees[0].getSubtree(0).isCsteEqualTo(1)));
  }


  public boolean isReified() {
		return true;
	}

    public String pretty() {
        return "ifThenElse("+subtrees[0].pretty()+","+subtrees[1].pretty()+", "+subtrees[2].pretty()+")";
    }
	
}
