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

package choco.cp.solver.constraints.integer;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.integer.AbstractBinIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomain;
import choco.kernel.solver.variables.integer.IntDomainVar;

/*
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: 23 f�vr. 2007
 * Since : Choco 2.0.0
 *
 */
public final class DistanceXYC extends AbstractBinIntSConstraint {

    protected int operator;

    protected final int cste;

    public final static int EQ = 0;

    public final static int LT = 1;

    public final static int GT = 2;

	public final static int NEQ = 3;

    public DistanceXYC(IntDomainVar v1, IntDomainVar v2, int c, int oper) {
        super(v1, v2);
        this.cste = c;
        this.operator = oper;
        if (oper != 0 && oper != 1 && oper != 2 && oper != 3) {
            throw new SolverException("operateur inconnu " + oper);
        }
    }

    @Override
    public int getFilteredEventMask(int idx) {
        if(idx == 0){
            if(v0.hasEnumeratedDomain()){
                return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
            }else{
                return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
            }
        }else{
            if(v1.hasEnumeratedDomain()){
                return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
            }else{
                return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
            }
        }
    }

//*************************************************************//
//        Methods for filtering                                //
//*************************************************************//


    /**
     * Initial propagation in case of EQ and enumerated domains
     * @throws ContradictionException
     */
    public void filterFromVarToVar(IntDomainVar var1, IntDomainVar var2) throws ContradictionException {
        DisposableIntIterator it = var1.getDomain().getIterator();
        try{
        for (; it.hasNext();) {
            int value = it.next();
            if (!var2.canBeInstantiatedTo(value - cste) &&
                    !var2.canBeInstantiatedTo(value + cste)) {
                var1.removeVal(value, this, false);
            }
        }
        }finally {
            it.dispose();
        }
    }

    /**
     * In case of a GT
     */
    public void filterGT() throws ContradictionException {
        if(cste>=0){
            int lbv0 = v1.getSup() - cste;
            int ubv0 = v1.getInf() + cste;
            // remove interval [lbv0, ubv0] from domain of v0
            v0.removeInterval(lbv0, ubv0, this, false);
            int lbv1 = v0.getSup() - cste;
            int ubv1 = v0.getInf() + cste;
            // remove interval [lbv1, ubv1] from domain of v1
            v1.removeInterval(lbv1, ubv1, this, false);
        }else{
            this.setEntailed();
        }
    }

    /**
     * In case of a GT, due to a modification on vv0 domain
     */
    public void filterGTonVar(IntDomainVar vv0, IntDomainVar vv1) throws ContradictionException {
        if(cste>=0){
            int lbv0 = vv0.getSup() - cste;
            int ubv0 = vv0.getInf() + cste;
            // remove interval [lbv0, ubv0] from domain of v0
            vv1.removeInterval(lbv0, ubv0, this, false);
        }else{
            this.setEntailed();
        }
    }

    /**
     * In case of a LT
     */
    public void filterLT() throws ContradictionException {
        v0.updateInf(v1.getInf() - cste + 1, this, false);
        v0.updateSup(v1.getSup() + cste - 1, this, false);
        v1.updateInf(v0.getInf() - cste + 1, this, false);
        v1.updateSup(v0.getSup() + cste - 1, this, false);
    }

    /**
     * In case of a LT, due to a modification on vv0 domain
     */
    public void filterLTonVar(IntDomainVar vv0, IntDomainVar vv1) throws ContradictionException {
        vv1.updateInf(vv0.getInf() - cste + 1, this, false);
        vv1.updateSup(vv0.getSup() + cste - 1, this, false);
    }

    /**
     * In case of a EQ, due to a modification of the lower bound of vv0
     */
     public void filterOnInf(IntDomainVar vv0, IntDomainVar vv1) throws ContradictionException {
        if (vv1.hasEnumeratedDomain()) {
            IntDomain dom = vv1.getDomain();
            int end = vv0.getInf() + cste;
            for (int val = vv0.getInf(); val <= end; val = dom.getNextValue(val)) {
                if (!vv0.canBeInstantiatedTo(val - cste) && !vv0.canBeInstantiatedTo(val + cste)) {
                    vv1.removeVal(val, this, false);
                }
            }
        } else {
            vv1.updateInf(vv0.getInf() - cste, this, false);
        }
    }

    /**
     * In case of a EQ, due to a modification of the upper bound of vv0
     */
    public void filterOnSup(IntDomainVar vv0, IntDomainVar vv1) throws ContradictionException {
        if (vv1.hasEnumeratedDomain()) {
            IntDomain dom = vv1.getDomain();
            int initval;
            if (vv0.getSup() - cste > vv1.getInf()) {
                initval = dom.getNextValue(vv0.getSup() - cste - 1);
            } else {
				initval = vv1.getInf();
			}
            int val = initval;
            do {
                if (!vv0.canBeInstantiatedTo(val - cste) && !vv0.canBeInstantiatedTo(val + cste)) {
                    vv1.removeVal(val, this, false);
                }
                val = dom.getNextValue(val);
            } while (val <= vv1.getSup() && val > initval); //todo : pourquoi besoin du deuxieme currentElement ?
        } else {
            vv1.updateSup(vv0.getSup() + cste, this, false);
        }
    }

    /**
     * In case of a EQ, due to the instantion to one variable to val
     */
    public void filterOnInst(IntDomainVar v, int val) throws ContradictionException {
        if (!v.canBeInstantiatedTo(val + cste)) {
            v.instantiate(val - cste, this, false);
        } else if (!v.canBeInstantiatedTo(val - cste)) {
            v.instantiate(val + cste, this, false);
        } else {
            if (v.hasEnumeratedDomain()) {
                DisposableIntIterator it = v.getDomain().getIterator();
                try{
                for (; it.hasNext();) {
                    int value = it.next();
                    if (value != (val - cste) && value != (val + cste)) {
                        v.removeVal(value, this, false);
                    }
                }
                }finally {
                    it.dispose();
                }
            } else {
                v.updateInf(val - cste, this, false);
                v.updateSup(val + cste, this, false);
            }
        }
    }

	public void filterNeq() throws ContradictionException {
        if(cste>=0){
            if (v0.isInstantiated()) {
                 v1.removeVal(v0.getVal() + cste, this, false);
                 v1.removeVal(v0.getVal() - cste, this, false);
             }
             if (v1.isInstantiated()) {
                 v0.removeVal(v1.getVal() + cste, this, false);
                 v0.removeVal(v1.getVal() - cste, this, false);
             }
        }else{
            this.setEntailed();
        }
	}

//*************************************************************//
//        API on events                                        //
//*************************************************************//

    /**
     * Default initial propagation: full constraint re-propagation.
     */
    @Override
    public void awake() throws ContradictionException {
        //cste < 0, and |v0-v1| always >= 0
        if(cste < 0){
            switch(operator){
                case EQ: case LT:
                    this.fail();
                    break;
                case NEQ: case GT:
                    this.setEntailed();
                    break;
            }
        }
    }

    @Override
	public void propagate() throws ContradictionException {
        if (operator == EQ) {
            if (v0.hasEnumeratedDomain()) {
                filterFromVarToVar(v0, v1);
            } else {
                v0.updateInf(v1.getInf() - cste, this, false);
                v0.updateSup(v1.getSup() + cste, this, false);
            }
            if (v1.hasEnumeratedDomain()) {
                filterFromVarToVar(v1, v0);
            } else {
                v1.updateInf(v0.getInf() - cste, this, false);
                v1.updateSup(v0.getSup() + cste, this, false);
            }
        } else if (operator == GT) {
            filterGT();
        } else if (operator == LT) {
            filterLT();
        } else {
	        filterNeq();
        }
    }

    @Override
	public void awakeOnRem(int idx, int x) throws ContradictionException {
        if (operator == EQ) {
            if (idx == 0) {
                if (!v0.canBeInstantiatedTo(x + 2 * cste)) {
					v1.removeVal(x + cste, this, false);
				}
                if (!v0.canBeInstantiatedTo(x - 2 * cste)) {
					v1.removeVal(x - cste, this, false);
				}
            } else {
                if (!v1.canBeInstantiatedTo(x + 2 * cste)) {
					v0.removeVal(x + cste, this, false);
				}
                if (!v1.canBeInstantiatedTo(x - 2 * cste)) {
					v0.removeVal(x - cste, this, false);
				}
            }
        } else if (operator == NEQ) {
	        filterNeq();
        }
        //else if (operator == GT) {
            //filterGT();
        //} else {
            //filterLT();
        //}
    }

    @Override
	public void awakeOnSup(int idx) throws ContradictionException {
        if (operator == EQ) {
            if (idx == 0) {
                filterOnSup(v0, v1);
            } else {
                filterOnSup(v1, v0);
            }
        } else if (operator == GT) {
            if (idx == 0) {
                filterGTonVar(v0,v1);
            } else {
                filterGTonVar(v1,v0);
            }
        } else if (operator == LT) {
            if (idx == 0) {
                filterLTonVar(v0,v1);
            } else {
                filterLTonVar(v1,v0);
            }
        } else {
	        filterNeq();
        }
    }

    @Override
	public void awakeOnInf(int idx) throws ContradictionException {
        if (operator == EQ) {
            if (idx == 0) {
                filterOnInf(v0, v1);
            } else {
                filterOnInf(v1, v0);
            }
        } else if (operator == GT) {
            if (idx == 0) {
                filterGTonVar(v0,v1);
            } else {
                filterGTonVar(v1,v0);
            }
        } else if (operator == LT) {
            if (idx == 0) {
                filterLTonVar(v0,v1);
            } else {
                filterLTonVar(v1,v0);
            }
        } else {
	        filterNeq();
        }

    }

    @Override
	public void awakeOnInst(int idx) throws ContradictionException {
        if (operator == EQ) {
            if (idx == 0) {
                filterOnInst(v1, v0.getVal());
            } else {
                filterOnInst(v0, v1.getVal());
            }
        } else if (operator == GT) {
            if (idx == 0) {
                filterGTonVar(v0,v1);
            } else {
                filterGTonVar(v1,v0);
            }
        } else if (operator == LT) {
            if (idx == 0) {
                filterLTonVar(v0,v1);
            } else {
                filterLTonVar(v1,v0);
            }
        } else {
	        filterNeq();
        }
    }

    @Override
	public String toString() {
        String op;
        if (operator == EQ) {
			op = "=";
		} else if (operator == GT) {
			op = ">";
		} else if (operator == LT) {
			op = "<";
		} else if (operator == NEQ) {
			op = "!=";
		} else {
			throw new SolverException("unknown operator");
		}
        return "|" + v0 + " - " + v1 + "| " + op + " " + cste;
    }

  @Override
public String pretty() {
    StringBuilder sb = new StringBuilder();
    sb.append("| ").append(v0.pretty()).append(" - ").append(v1.pretty()).append(" | ");
    switch (operator) {
      case EQ: sb.append("="); break;
	  case NEQ: sb.append("!="); break;
      case GT: sb.append(">"); break;
      case LT: sb.append("<"); break;
      default: sb.append("???"); break;
    }
    sb.append(cste);
    return sb.toString();
  }

    @Override
	public Boolean isEntailed() {
        throw new UnsupportedOperationException("isEntailed not yet implemented on DistanceXYC constraint");
    }

    @Override
	public boolean isSatisfied(int[] tuple) {
		if (operator == EQ) {
			return Math.abs(tuple[0] - tuple[1]) == cste;
		} else if (operator == LT) {
			return Math.abs(tuple[0] - tuple[1]) < cste;
		} else if (operator == GT) {
			return Math.abs(tuple[0] - tuple[1]) > cste;
		} else if (operator == NEQ) {
			return Math.abs(tuple[0] - tuple[1]) != cste;
		} else {
			throw new SolverException("operator not known");
		}
	}

}
