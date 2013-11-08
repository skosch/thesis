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

package parser.chocogen;


import choco.Choco;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.automaton.DFA;
import choco.kernel.solver.constraints.integer.extension.BinRelation;
import choco.kernel.solver.constraints.integer.extension.LargeRelation;
import parser.absconparseur.components.*;
import parser.absconparseur.tools.InstanceParser;

import java.util.*;

/**
 * Factory to create relations
 */
public class RelationFactory extends ObjectFactory {

	HashMap<PRelation, List<PExtensionConstraint>> extlist;

	public RelationFactory(Model model, InstanceParser parser) {
		super(model, parser);
		initializeExtList();
	}

	public void initializeExtList() {
		extlist = new HashMap<PRelation, List<PExtensionConstraint>>(16);
        for (final PConstraint pConstraint : parser.getMapOfConstraints().values()) {
            if (pConstraint instanceof PExtensionConstraint) {
                PExtensionConstraint p1 = (PExtensionConstraint) pConstraint;
                List<PExtensionConstraint> lcts = extlist.get(p1.getRelation());
                if (lcts == null) {
                    lcts = new LinkedList<PExtensionConstraint>();
                    extlist.put(p1.getRelation(), lcts);
                }
                lcts.add(p1);
            }
        }
	}

	public static DFA makeDFA(PRelation prel) {
		DFA dfa = null;
		if (prel.getSemantics().equals("supports")) {
			dfa = new DFA(prel.getListTuples());
		}
		return dfa;
	}

	public LargeRelation makeLargeRelation(PRelation prel) {
		LargeRelation rel = null;
		int[] min = getMin(prel);
		int[] max = getMax(prel);
		if (prel.getSemantics().equals("supports")) {
			//define by feasibility
			double matrixsize = getCartesianProduct(prel);
			double nbt = prel.getNbTuples();
			double tightness = 1 - nbt / matrixsize;
			if (matrixsize >= 32000000 || tightness >= 0.98)
				rel = Choco.makeLargeRelation(min, max, prel.getListTuples(), true, (algorithmAC == AC2008) ? 2 : 0);
			else rel = Choco.makeLargeRelation(min, max, prel.getListTuples(), true, 1);
		} else {
			//define by infeasibility
			rel = Choco.makeLargeRelation(min, max, prel.getListTuples(), false);
		}
		return rel;
	}


	public BinRelation makeBinRelation(PRelation prel) {
		BinRelation brel = null;
		int[] min = getMin(prel);
		int[] max = getMax(prel);
		if (prel.getSemantics().equals("supports")) {
			//define by feasibility
			brel = Choco.makeBinRelation(min, max, prel.getListTuples(), true, true);
			if (prel.checkEqInCouples()) {
				prel.setEqInTuples(true);
				prel.setNeqInTuples(false);
			} else if (prel.checkNeqInCouples()) {
				prel.setNeqInTuples(true);
				prel.setEqInTuples(false);
			}
			prel.eraseListTuple();
		} else {
			//define by infeasibility
			brel = Choco.makeBinRelation(min, max, prel.getListTuples(), false, true);
			if (prel.checkEqInCouples()) {
				prel.setEqInTuples(true);
				prel.setNeqInTuples(false);
			} else if (prel.checkNeqInCouples()) {
				prel.setNeqInTuples(true);
				prel.setEqInTuples(false);
			}
			prel.eraseListTuple();
		}
		return brel;
	}

    public boolean isSatDecomposable(PRelation prel) {
        if (isAllBoolean(prel) && prel.getArity() <= 10) {
           double cp = getCartesianProduct(prel);
           int nbsupports = prel.getNbTuples();
           //we put a clause if there is less than 3 conflicts
           if (prel.getSemantics().equals("supports")) {
             return (cp - nbsupports) < 5;
           } else {
             return nbsupports < 5;
           }
        }
        return false;
    }

    //convert a 0/1 tuple in an integer value
    public static int getVal(int[] bint) {
        int val = 0;
        for (int i = 0; i < bint.length; i++) {
            val += bint[i]*Math.pow(2,i);
        }
        return val;
    }

    //convert a 0/1 tuple in an integer value
    public static int[] getTuple(int val, int arity) {
        int[] t = new int[arity];
        for (int i = 0; i < arity; i++) {
            if((val & (1 << i)) != 0) {
                t[i] = 1;
            }

        }
        return t;
    }

    public static void makeClausesEncoding(PRelation prel) {
        List<XmlClause> enc = new ArrayList<XmlClause>(16);
        if (prel.getSemantics().equals("conflicts")) {
            List<int[]> ltuples = prel.getListTuples();
            for(int[] cl : ltuples) {
               enc.add(makeClause(cl,0));
            }
        } else {
            //compute the conflicts !
            List<int[]> ltuples = prel.getListTuples();
            List<int[]> conflict = new LinkedList<int[]>();
            BitSet validTuple = new BitSet(16);
            for(int[] cl : ltuples) {
                validTuple.set(getVal(cl));
            }
            int cartp = (int) Math.pow(2,prel.getArity());
            for (int i = 0; i < cartp; i++) {
                if (!validTuple.get(i)) {
                    conflict.add(getTuple(i,prel.getArity()));
                }
            }
            for(int[] cl : conflict) {
               enc.add(makeClause(cl,0));
            }
        }
		prel.setClauseEncoding(enc);
	}

    public static XmlClause makeClause(int[] cl, int valtest) {
        int nbposv = 0;
        int nbnegv = 0;
        for (int aCl : cl) {
            if (aCl == valtest) nbposv++;
            else nbnegv++;
        }
        int[] poslit = new int[nbposv];
        int[] neglit = new int[nbnegv];
        int cpt1 = 0, cpt2 = 0;
        for (int i = 0; i < cl.length; i++) {
            if (cl[i] == valtest) {
                poslit[cpt1] = i;
                cpt1++;
            } else {
                neglit[cpt2] = i;
                cpt2++;
            }
        }
        return new XmlClause(poslit,neglit);
    }

    /**
	 * return the cartesian product of the variables
	 * prel holds.
	 *
	 * @param prel
	 * @return
	 */
	public double getCartesianProduct(PRelation prel) {
		double cartprod = 1D;
		int[] nbvalues = new int[prel.getArity()];
        for (PExtensionConstraint ct : extlist.get(prel)) {
            PVariable[] vs = ct.getScope();
            for (int i = 0; i < vs.length; i++) {
                nbvalues[i] = Math.max(vs[i].getDomain().getNbValues(), nbvalues[i]);
            }
        }
        for (int nbvalue : nbvalues) {
            cartprod *= nbvalue;
        }
		return cartprod;
	}

     /**
	 * return the cartesian product of the variables
	 * prel holds.
	 *
	 * @param prel
	 * @return
	 */
	public boolean isAllBoolean(PRelation prel) {
		for (Iterator<PExtensionConstraint> iterator = extlist.get(prel).iterator(); iterator.hasNext();) {
			PExtensionConstraint ct = iterator.next();
			PVariable[] vs = ct.getScope();
            for (PVariable v : vs) {
                if (v.getDomain().getMinValue() < 0 ||
                        v.getDomain().getMaxValue() > 1)
                    return false;
            }
		}
		return true;
	}

    /**
	 * return the minimum value of the variables on which
	 * prel holds.
	 *
	 * @param prel
	 * @return
	 */
	public int[] getMin(PRelation prel) {
		int[] min = new int[prel.getArity()];
		Arrays.fill(min, Integer.MAX_VALUE);
        for (PExtensionConstraint ct : extlist.get(prel)) {
            PVariable[] vs = ct.getScope();
            for (int i = 0; i < vs.length; i++) {
                PVariable v = vs[i];
                if (v.getDomain().getMinValue() < min[i]) {
                    min[i] = v.getDomain().getMinValue();
                }
            }
        }
		return min;

	}

	/**
	 * return the minimum value of the variables on which
	 * prel holds.
	 *
	 * @param prel
	 * @return
	 */
	public int[] getMax(PRelation prel) {
		int[] max = new int[prel.getArity()];
		Arrays.fill(max, Integer.MIN_VALUE);
        for (PExtensionConstraint ct : extlist.get(prel)) {
            if (ct.getRelation() == prel) {
                PVariable[] vs = ct.getScope();
                for (int i = 0; i < vs.length; i++) {
                    PVariable v = vs[i];
                    if (v.getDomain().getMaxValue() > max[i]) {
                        max[i] = v.getDomain().getMaxValue();
                    }
                }
            }
        }
		return max;
	}


	/**
	 * Try to recognize if brel (a binary relation only) is :
	 * - x = y   0
	 * - x != y  1
	 * - x >= y  2
	 * - x > y   3
	 * - x <= y  4
	 * - x < y   5
	 *
	 * @param pec
	 */
	public static boolean detectIntensionConstraint(PExtensionConstraint pec) {
		assert (pec.getArity() == 2);
		PRelation brel = pec.getRelation();
		PDomain[] doms = new PDomain[2];
		doms[0] = pec.getScope()[0].getDomain();
		doms[1] = pec.getScope()[1].getDomain();
		for (int i = 0; i < 2; i++) {
			if (i == 0) {
				if (checkEqInCouples(brel, doms)) {
					if (brel.getSemantics().equals("supports")) {
						pec.setIntensionCts(ModelConstraintFactory.ConstExp.eq);
					} else {
						pec.setIntensionCts(ModelConstraintFactory.ConstExp.ne);
					}
					return true;
				}
			} else if (i == 1) {
				if (checkNeqInCouples(brel, doms)) {
					if (brel.getSemantics().equals("supports")) {
						pec.setIntensionCts(ModelConstraintFactory.ConstExp.ne);
					} else {
						pec.setIntensionCts(ModelConstraintFactory.ConstExp.eq);
					}
					return true;
				}
			}
		}
		return false;
	}

	public static boolean checkEqInCouples(PRelation brel, PDomain[] pdom) {
		if (brel.isEqInTuples() &&
				pdom[0].getNbValues() < 5000 &&
				pdom[1].getNbValues() < 5000) {
			int scard = pdom[0].getIntersectionSize(pdom[1]);
			return scard == brel.getNbTuples();
		}
		return false;
	}

	public static boolean checkNeqInCouples(PRelation brel, PDomain[] pdom) {
		if (brel.isNeqInTuples() &&
				pdom[0].getNbValues() < 5000 &&
				pdom[1].getNbValues() < 5000) {

			int inter = pdom[0].getIntersectionSize(pdom[1]);
			if (inter != -1) {
				int scard = pdom[0].getNbValues() * pdom[1].getNbValues() - inter;
				return scard == brel.getNbTuples();
			} else return false;
		}
		return false;
	}

}
