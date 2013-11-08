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

package choco.model.constraints.global;

import choco.Choco;
import choco.Reformulation;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import gnu.trove.TIntHashSet;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

/**
 * User : cprudhom
 * Mail : cprudhom(a)emn.fr
 * Date : 22 févr. 2010
 * Since : Choco 2.1.1
 */
public class AmongGACTest {

    private static final Logger LOGGER = ChocoLogging.getTestLogger();

    IntegerVariable[] _variables;

    private int[] buildValues(Random r, int low, int up){
        int nb = 1 + r.nextInt(up-low + 1);
        TIntHashSet set = new TIntHashSet(nb);
        for(int i = 0 ; i < nb; i++){
            set.add(low + r.nextInt(up - low + 1));
        }
        int[] values = set.toArray();
        Arrays.sort(values);
        return values;
    }

    private IntegerVariable[] buildVars(Random r, int low, int up){
        int nb = 1 + r.nextInt(up - low + 1);
        IntegerVariable[] vars = new IntegerVariable[nb];
        for(int i = 0 ; i < nb; i++){
            vars[i] = Choco.makeIntVar("vars_"+i, buildValues(r, low, up));
        }
        return vars;
    }

    private CPModel[] model(Random r, int size){
        int low = r.nextInt(size);
        int up = low + r.nextInt(2*size);
        int[] values = buildValues(r, low, up);
        IntegerVariable[] vars = buildVars(r, low,up);
        int max = r.nextInt(vars.length);
        int min = Math.max(0, max-1 - r.nextInt(vars.length));
        IntegerVariable N = Choco.makeIntVar("N", min,max);

        _variables = ArrayUtils.append(vars, new IntegerVariable[]{N});

        CPModel[] ms = new CPModel[2];
        for(int i = 0; i< ms.length; i++){
            CPModel m = new CPModel();
            switch (i){
                case 0 :
                    m.addConstraints(Reformulation.among(N, vars, values));
                    break;
                case 1:
                    m.addConstraint(Choco.among(N, vars, values));
                break;
            }
            ms[i] = m;
        }
        return ms;
    }


    private CPSolver solve(int seed, CPModel m, boolean sta_tic){
        CPSolver s = new CPSolver();
        s.read(m);
        if(sta_tic){
            s.setVarIntSelector(new StaticVarOrder(s, s.getVar(_variables)));
            s.setValIntIterator(new IncreasingDomain());
        }else{
            s.setVarIntSelector(new RandomIntVarSelector(s, seed));
            s.setValIntSelector(new RandomIntValSelector(seed));
        }
        s.solveAll();
        return s;
    }

    @Test
    public void test0(){
        Model m = new CPModel();
        IntegerVariable[] vars = Choco.makeIntVarArray("vars", 3, 0,3);
        
        int[] values = new int[]{0, 1};
        IntegerVariable N = Choco.makeIntVar("N", 0, 2);
        Constraint among =Choco.among(N, vars, values);

        m.addConstraint(among);
        Solver s = new CPSolver();
        s.read(m);
        s.solveAll();

        Model mr = new CPModel();
        IntegerVariable[] bools = Choco.makeBooleanVarArray("bools", vars.length);
        for(int j = 0; j < bools.length; j++){
            mr.addConstraint(Choco.reifiedConstraint(bools[j], Choco.member(vars[j], values)));
        }
        mr.addConstraint(Choco.eq(Choco.sum(bools), N));
        Solver sr = new CPSolver();
        sr.read(mr);
        sr.solveAll();

        Assert.assertEquals(sr.getSolutionCount(), s.getSolutionCount());
    }


    @Test
    public void test1() throws IOException {
        Random r;
        for(int st = 0; st < 2; st++){
            for(int i = 0; i < 500; i++){
                r = new Random(i);
                CPModel[] ms = model(r, 5);
                CPSolver[] ss = new CPSolver[ms.length];
                for(int j = 0; j < ms.length; j++){
                    ss[j] = solve(i, ms[j], (st == 1));
                }
                for(int j = 1; j < ms.length; j++){
                    Assert.assertEquals("nb solutions, seed:"+i, ss[0].getSolutionCount(), ss[j].getSolutionCount());
                    if(st > 0){
                        Assert.assertEquals("nb nodes, seed:"+i, ss[0].getNodeCount(), ss[j].getNodeCount());
                    }
                }
            }
        }
    }


}
