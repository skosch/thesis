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

package choco.memory;

import choco.cp.solver.CPSolver;
import choco.cp.solver.variables.integer.IntDomainVarImpl;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.structure.StoredBipartiteVarSet;
import choco.kernel.memory.trailing.EnvironmentTrailing;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 17 juil. 2009
* Since : Choco 2.1.0
* Update : Choco 2.1.0
*/
public class StoredBipartiteVarListTest {

    StoredBipartiteVarSet<IntDomainVar> ar;
    IEnvironment env;
    CPSolver s;

    @Before
    public void b(){
        env = new EnvironmentTrailing();
        s = new CPSolver(env);
        ar = new StoredBipartiteVarSet<IntDomainVar>(env);
    }

    @After
    public void a(){
        ar = null;
        s = null;
        env = null;
    }


    @Test
    public void test1() throws ContradictionException {
        int n = 4;
        IntDomainVar[] var = new IntDomainVar[n];
        for(int i = 0; i < n; i++){
            var[i] = new IntDomainVarImpl(s, i+"",IntDomainVar.BITSET, 0, 1);
            ar.add(var[i]);
        }

        Iterator it = ar.quickIterator();
        checkIterator(it, var);
        it = ar.getInstanciatedVariableIterator();
        checkIterator(it, new IntDomainVar[0]);
        it = ar.getNotInstanciatedVariableIterator();
        checkIterator(it, var);

        env.worldPush();
        var[1].instantiate(0, null, true);

//        ar.isInstanciated(var[1]);

        it = ar.quickIterator();
        checkIterator(it, var);
        it = ar.getInstanciatedVariableIterator();
        checkIterator(it, new IntDomainVar[]{var[1]});
        it = ar.getNotInstanciatedVariableIterator();
        checkIterator(it, new IntDomainVar[]{var[0], var[3], var[2]});

        env.worldPush();
        var[0].instantiate(0, null, true);
//        ar.isInstanciated(var[0]);

        it = ar.quickIterator();
        checkIterator(it, var);
        it = ar.getInstanciatedVariableIterator();
        checkIterator(it, new IntDomainVar[]{var[0], var[1]});
        it = ar.getNotInstanciatedVariableIterator();
        checkIterator(it, new IntDomainVar[]{var[2], var[3]});


    }

    @Test
    public void test2() throws ContradictionException {
        int n = 4;
        IntDomainVar[] var = new IntDomainVar[n];
        for(int i = 0; i < n; i++){
            var[i] = new IntDomainVarImpl(s, i+"",IntDomainVar.BITSET, 0, 1);
            ar.add(var[i]);
        }

        s.worldPush();
        var[0].instantiate(0, null, true);

        s.worldPush();
        var[3].instantiate(0, null, true);
        var[1].instantiate(0, null, true);
        var[2].instantiate(0, null, true);

        s.worldPop();
        s.worldPush();
        var[3].instantiate(0, null, true);
        var[1].instantiate(0, null, true);
        var[2].instantiate(0, null, true);

        s.worldPop();
        s.worldPop();

        s.worldPush();
        var[0].instantiate(0, null, true);
        var[1].instantiate(0, null, true);

        s.worldPush();
        var[2].instantiate(0, null, true);
        var[3].instantiate(0, null, true);

        s.worldPop();
        Iterator it = ar.quickIterator();
        checkIterator(it, var);
        it = ar.getInstanciatedVariableIterator();
        checkIterator(it, new IntDomainVar[]{var[0], var[1]});
        it = ar.getNotInstanciatedVariableIterator();
        checkIterator(it, new IntDomainVar[]{var[3], var[2]});

        s.worldPop();
        it = ar.quickIterator();
        checkIterator(it, var);
        it = ar.getNotInstanciatedVariableIterator();
        checkIterator(it, new IntDomainVar[]{var[3], var[2], var[1], var[0]});
        it = ar.getInstanciatedVariableIterator();
        checkIterator(it, new IntDomainVar[]{});


    }

    private void checkIterator(Iterator it, IntDomainVar[] var){
        for (IntDomainVar aVar : var) {
            Assert.assertTrue(it.hasNext());
            Assert.assertEquals(aVar, it.next());
        }
        Assert.assertFalse(it.hasNext());
    }

}
