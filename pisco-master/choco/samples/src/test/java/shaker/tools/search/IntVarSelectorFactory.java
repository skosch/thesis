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

package shaker.tools.search;

import choco.cp.solver.search.integer.varselector.*;
import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.VarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 5 août 2009
* Since : Choco 2.1.0
* Update : Choco 2.1.0
*/
public class IntVarSelectorFactory {

    public ArrayList<V> scope = new ArrayList<V>(10);

    public enum V {
        STATIC, DOMOVERDEG, DOMOVERDYNDEG, DOMOVERWDEG, MINDOMAIN, MAXDOMAIN,
        MAXREGRET, MINVALUEDOMAIN, MAXVALUEDOMAIN, MOSTCONSTRAINED, RANDOM
    }

    /**
     * Define a specific scope of value selector tuple to pick up in
     * @param vs the scope of value selector
     */
    public void scopes(V... vs){
        scope.clear();
        scope.addAll(Arrays.asList(vs));
    }

    /**
     * Select randomly (among scope if defined)
     * and return a value selector type
     * @param r random
     * @return type of value selector
     */
    public V any(Random r) {
        if(!scope.isEmpty()){
            return scope.get(r.nextInt(scope.size()));
        }
        V[] values = V.values();
        return values[r.nextInt(values.length)];
    }


    /**
     * Get one value selector among all
     * @param r random
     * @return value selector
     */
    public VarSelector<IntDomainVar> make(Random r, Solver s, IntDomainVar[] vars){
        return make(any(r), r, s, vars);
    }

    /**
     * Create and return the corresponding value selector
     * @param v the type of value selector
     * @param r random
     * @return value selector
     */
    public static VarSelector<IntDomainVar> make(V v, Random r, Solver s, IntDomainVar[] vars) {
        //Otherwise, select a new val selector
        VarSelector<IntDomainVar> ivs = null;

        switch (v) {
            case DOMOVERDEG:
                ivs = new DomOverDeg(s, vars);
                break;
            case DOMOVERDYNDEG:
                ivs = new DomOverDynDeg(s, vars);
                break;
            case DOMOVERWDEG:
                ivs = new DomOverWDeg(s, vars);
                break;
            case MAXDOMAIN:
                ivs = new MaxDomain(s, vars);
                break;
            case MAXREGRET:
                ivs = new MaxRegret(s, vars);
                break;
            case MAXVALUEDOMAIN:
                ivs = new MaxValueDomain(s, vars);
                break;
            case MINDOMAIN:
                ivs = new MinDomain(s, vars);
                break;
            case MINVALUEDOMAIN:
                ivs = new MinValueDomain(s, vars);
                break;
            case MOSTCONSTRAINED:
                ivs = new MostConstrained(s, vars);
                break;
            case RANDOM:
                ivs = new RandomIntVarSelector(s, vars, r.nextLong());
                break;
            case STATIC:
                ivs = new StaticVarOrder(s, vars);
                break;
        }
        return ivs;
    }
}