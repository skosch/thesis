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

package choco.cp.model.managers.constraints.integer;

import choco.Choco;
import choco.Options;
import choco.cp.model.managers.IntConstraintManager;
import choco.cp.solver.constraints.integer.extension.*;
import choco.cp.solver.variables.integer.BitSetIntDomain;
import choco.kernel.memory.IEnvironment;
import choco.kernel.model.ModelException;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.integer.extension.*;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.List;

import static choco.Choco.makeLargeRelation;

/*
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: Aug 6, 2008
 * Since : Choco 2.0.0
 *
 */
public final class TableManager extends IntConstraintManager {

    /**
     * @param solver
     * @param vars
     * @param parameters : a List<Object> which is intended to either a single element i.e the relation or
     *                   two elements :<ul> <li>- a boolean indicating if the consistency relation is feasible or infeasible</li>
     *                   <li>- a list<int[]> or a int[][]</li></ul>
     * @param options
     * @return
     */
    public SConstraint makeConstraint(Solver solver, IntegerVariable[] vars, Object parameters, List<String> options) {
        Object[] ps = (Object[]) parameters;
        IntegerVariable[] vars2 = new IntegerVariable[vars.length];
        System.arraycopy(vars,0,vars2,0,vars.length);
        IntDomainVar[] vs = solver.getVar(vars2);
        if (vs.length == 2
                && !options.contains(Options.C_EXT_AC2008)
                && !(ps[1] instanceof LargeRelation)) { //ac binaire
            return buildBinaryTable(vs, parameters, options, solver.getEnvironment());
        } else { //ac naire
            return buildNaryTable(vs, parameters, options, solver.getEnvironment());

        }
    }

    //*********************************************//
    //*************** Binairy AC ******************//    
    //*********************************************//

    public SConstraint buildBinaryTable(IntDomainVar[] vs, Object parameters, List<String> options, IEnvironment environment) {
        Object[] ps = (Object[]) parameters;
        IntDomainVar v1 = vs[0];
        IntDomainVar v2 = vs[1];
        if (ps[1] instanceof BinRelation) {
            return buildBinaryTable(v1, v2, (BinRelation) ps[1], options, environment);
        } else {
            boolean feas = (Boolean) ps[0];
            BinRelation binR = makePairAC(v1, v2, ps[1], feas, options);
            return buildBinaryTable(v1, v2, binR, options, environment);
        }
    }

    public SConstraint buildBinaryTable(IntDomainVar v1, IntDomainVar v2, BinRelation binR, List<String> options, IEnvironment environment) {
        if (options.contains(Options.C_EXT_FC)) {
            return new FCBinSConstraint(v1,v2,binR);
        } else if (options.contains(Options.C_EXT_AC3)) {
            return new AC3BinSConstraint(v1, v2, binR);
        } else if (options.contains(Options.C_EXT_AC32)) {
            return new AC3rmBinSConstraint(v1, v2, binR);
        } else if (options.contains(Options.C_EXT_AC322)) {
            return new AC3rmBitBinSConstraint(v1, v2, (CouplesBitSetTable) binR);
        } else if (options.contains(Options.C_EXT_AC2001)) {
            return new AC2001BinSConstraint(v1, v2, binR, environment);
        } else { //default choice
            if (binR instanceof CouplesBitSetTable && (v1.getDomain() instanceof BitSetIntDomain) && (v2.getDomain() instanceof BitSetIntDomain)) {
                return new AC3rmBitBinSConstraint(v1, v2, (CouplesBitSetTable) binR);
            } else {
                return new AC3rmBinSConstraint(v1, v2, binR);
            }
        }
    }

    private BinRelation makePairAC(IntDomainVar x, IntDomainVar y, Object mat, boolean feas, List<String> options) {
        int[] min = new int[]{x.getInf(), y.getInf()};
        int[] max = new int[]{x.getSup(), y.getSup()};
        if (mat instanceof List)
            return Choco.makeBinRelation(min, max, (List<int[]>) mat, feas, options.contains(Options.C_EXT_AC322));
        else if (mat instanceof boolean[][])
            return Choco.makeBinRelation(min, max, (boolean[][]) mat, feas, options.contains(Options.C_EXT_AC322));
        else throw new ModelException("a relation should be given a List<int[]> or boolean[][]");
    }

    //*********************************************//
    //*************** Nary AC *********************//    
    //*********************************************//

    public SConstraint buildNaryTable(IntDomainVar[] vs, Object parameters, List<String> options, IEnvironment environment) {
        Object[] ps = (Object[]) parameters;
        if (ps[1] instanceof LargeRelation) {
            return buildNaryTable(vs, (LargeRelation) ps[1], options, environment);
        } else {
            boolean feas = (Boolean) ps[0];
            LargeRelation rela = makeTupleAC(vs,(List<int[]>) ps[1],feas,options);
            return buildNaryTable(vs, rela, options, environment);
        }
    }

    public SConstraint buildNaryTable(IntDomainVar[] vs, LargeRelation rela, List<String> options, IEnvironment environment) {
        if (options.contains(Options.C_EXT_FC)) {
            return new CspLargeSConstraint(vs, rela);
        } else {
            if (rela instanceof IterLargeRelation) {
                if (options.contains(Options.C_EXT_AC32)) {
                    return new GAC3rmPositiveLargeConstraint(vs, (IterTuplesTable) rela);
                } else if (options.contains(Options.C_EXT_AC2001)) {
                    return new GAC2001PositiveLargeConstraint(environment, vs, (IterTuplesTable) rela);
                } else {
                    return new GAC3rmPositiveLargeConstraint(vs, (IterTuplesTable) rela);
                }
            } else {
                if (options.contains(Options.C_EXT_AC32)) {
                    return new GAC3rmLargeConstraint(vs, rela);
                } else if (options.contains(Options.C_EXT_AC2001)) {
                    return new GAC2001LargeSConstraint(vs, rela, environment);
                } else if (options.contains(Options.C_EXT_AC2008) && rela instanceof TuplesList) {
                    return new GACstrPositiveLargeSConstraint(vs, rela, environment);
                } else {
                    return new GAC3rmLargeConstraint(vs, rela); 
                }
            }
        }
    }


    /**
     * Create a constraint to enforce GAC on a list of feasible or infeasible tuples
     *
     * @param vs
     * @param tuples the list of tuples
     * @param feas   specify if the tuples are feasible or infeasible tuples
     * @return
     */
    private LargeRelation makeTupleAC(IntDomainVar[] vs, List<int[]> tuples, boolean feas, List<String> options) {
        int[] min = new int[vs.length];
        int[] max = new int[vs.length];
        for (int i = 0; i < vs.length; i++) {
            min[i] = vs[i].getInf();
            max[i] = vs[i].getSup();
        }
        if (options.contains(Options.C_EXT_AC2008)) {
            return makeLargeRelation(min, max, tuples, feas, 2);
        } else if (options.contains(Options.C_EXT_FC)) {
            return makeLargeRelation(min, max, tuples, feas, 1);
        } else {
            return makeLargeRelation(min, max, tuples, feas);            
        }
    }

    //*********************************************//
    //*************** Favorite Variables **********//    
    //*********************************************//


    public int[] getFavoriteDomains(List<String> options) {
        if (options.contains(Options.C_EXT_AC322)) {
            return new int[]{IntDomainVar.BITSET};
        } else {
            return new int[]{
                             IntDomainVar.BITSET,
                             IntDomainVar.BIPARTITELIST, //<hca>: todo reverse once blist done
                             IntDomainVar.BOUNDS};
        }
    }
}
