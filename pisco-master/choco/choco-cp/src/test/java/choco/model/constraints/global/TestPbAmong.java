/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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
package choco.model.constraints.global;

import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.extension.BinRelation;
import junit.framework.Assert;

import java.util.ArrayList;

import static choco.Choco.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30/01/13
 */
public class TestPbAmong {

    public static void main(String[] args) {

        final CPSolver solver = new CPSolver();
        CPModel model = new CPModel();

        // VARIABLES série varA :
        // 12 variables varsA[i] avec D( varsA[i] ) = { 0, 1, 2, .., 15, i+16}
        int tmpdomain[] = new int[17];
        for (int i = 0; i < 16; i++)
            tmpdomain[i] = i;
        IntegerVariable[] varsA = new IntegerVariable[12];
        for (int i = 0; i < 12; i++) {
            tmpdomain[16] = 16 + i;
            varsA[i] = makeIntVar("varsA" + i, tmpdomain.clone());
        }
        model.addVariables(varsA);

        // VARIABLES série varB
        // 16 variables varsB[i] avec D( varsB[i] ) = { 1, 2, .., 11, i+12}
        tmpdomain = new int[13];
        for (int i = 0; i < 12; i++)
            tmpdomain[i] = i;
        IntegerVariable[] varsB = new IntegerVariable[16];
        for (int i = 0; i < 16; i++) {
            tmpdomain[12] = 16 + i;
            varsB[i] = makeIntVar("varsB" + i, tmpdomain.clone());
        }
        model.addVariables(varsB);

        // variable pour compter les varsA et varsB inférieureures à 16
        final IntegerVariable nbvarsInf16 = makeIntVar("nbvarsInf16", 0, 28, Options.V_NO_DECISION,
                Options.V_BOUND);
        model.addVariable(nbvarsInf16);

        // ----------- définition relation binaire : rel
        int[] min = new int[]{0, 0};
        int[] max = new int[]{27, 27};
        ArrayList<int[]> couplesOK = new ArrayList<int[]>();
        addCouples(couplesOK, 0, 1);
        addCouples(couplesOK, 0, 4);
        addCouples(couplesOK, 0, 3);
        addCouples(couplesOK, 1, 2);
        addCouples(couplesOK, 2, 3);
        addCouples(couplesOK, 2, 11);
        addCouples(couplesOK, 3, 5);
        addCouples(couplesOK, 3, 10);
        addCouples(couplesOK, 4, 5);
        addCouples(couplesOK, 4, 6);
        addCouples(couplesOK, 5, 7);
        addCouples(couplesOK, 5, 8);
        addCouples(couplesOK, 6, 7);
        addCouples(couplesOK, 7, 9);
        addCouples(couplesOK, 8, 10);
        addCouples(couplesOK, 8, 14);
        addCouples(couplesOK, 8, 9);
        addCouples(couplesOK, 9, 15);
        addCouples(couplesOK, 10, 11);
        addCouples(couplesOK, 10, 13);
        addCouples(couplesOK, 11, 12);
        addCouples(couplesOK, 12, 13);
        addCouples(couplesOK, 13, 14);
        addCouples(couplesOK, 14, 15);
        for (int j = 0; j < 12; j++) {
            for (int k = 0; k < 12; k++) {
                addCouples(couplesOK, j + 16, k);
                if (j <= k)
                    addCouples(couplesOK, j + 16, k + 16);
            }
        }
        BinRelation rel = makeBinRelation(min, max, couplesOK, true);

        // ----contraintes entre des Vars A basées sur rel2
        model.addConstraint(allDifferent(varsA));

        model.addConstraint(relationPairAC(varsA[10], varsA[11], rel));
        model.addConstraint(relationPairAC(varsA[8], varsA[9], rel));
        model.addConstraint(relationPairAC(varsA[6], varsA[7], rel));
        model.addConstraint(relationPairAC(varsA[5], varsA[9], rel));
        model.addConstraint(relationPairAC(varsA[5], varsA[7], rel));
        model.addConstraint(relationPairAC(varsA[4], varsA[10], rel));
        model.addConstraint(relationPairAC(varsA[4], varsA[8], rel));
        model.addConstraint(relationPairAC(varsA[4], varsA[5], rel));
        model.addConstraint(relationPairAC(varsA[3], varsA[6], rel));
        model.addConstraint(relationPairAC(varsA[3], varsA[5], rel));
        model.addConstraint(relationPairAC(varsA[2], varsA[3], rel));
        model.addConstraint(relationPairAC(varsA[1], varsA[2], rel));
        model.addConstraint(relationPairAC(varsA[0], varsA[11], rel));
        model.addConstraint(relationPairAC(varsA[0], varsA[4], rel));
        model.addConstraint(relationPairAC(varsA[0], varsA[3], rel));
        model.addConstraint(relationPairAC(varsA[0], varsA[1], rel));

        // autres contraintes
        model.addConstraint(lt(varsB[0], varsB[2]));
        model.addConstraint(lt(varsB[0], varsB[4]));
        model.addConstraint(lt(varsB[0], varsB[7]));
        model.addConstraint(lt(varsB[0], varsB[9]));
        model.addConstraint(lt(varsB[0], varsB[11]));
        model.addConstraint(lt(varsB[0], varsB[13]));
        model.addConstraint(lt(varsB[0], varsB[14]));

        // ---- channeling entre vars A et vars B
        model.addConstraint(inverseChannelingWithinRange(varsA, varsB));

        // --- among pour compter les varsA et varsB inférieureures à 16
        tmpdomain = new int[16];
        for (int i = 0; i < 16; i++)
            tmpdomain[i] = i;

        IntegerVariable[] vars_A_et_B = ArrayUtils.append(varsA, varsB);

        model.addConstraint(among(nbvarsInf16, vars_A_et_B, tmpdomain));

        // --------
        // model.addConstraint(Choco.eq(nbvarsInfN, 24));

        solver.read(model);

        try {
            solver.propagate();
        } catch (ContradictionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        solver.maximize(solver.getVar(nbvarsInf16), true);
        Assert.assertEquals(solver.getVar(nbvarsInf16).getVal(), 24);

    }

    /**
     * Ajout de {a, b} et {b, a } à lst
     *
     * @param lst
     * @param a
     * @param b
     */
    public static void addCouples(ArrayList<int[]> lst, int a, int b) {
        int[] tab = new int[2];
        tab[0] = a;
        tab[1] = b;
        lst.add(tab);
        if (a != b) {
            tab = new int[2];
            tab[1] = a;
            tab[0] = b;
            lst.add(tab);
        }
    }
}
