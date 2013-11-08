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

package choco.solver.shaving;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import org.junit.Test;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 20 avr. 2010br/>
 * Since : Choco 2.1.1<br/>
 */
public class ShavingTest {

    @Test
    public void test2989765_1() {
        Model m = new CPModel();
        IntegerVariable[] pos = new IntegerVariable[4];
        for (int i = 0; i < pos.length; i++) {
            pos[i] = Choco.makeIntVar("VM" + i + "on ?", 0, 4);
            IntegerVariable [] bools = Choco.makeBooleanVarArray("VM" + i + "on ", 6);
            m.addConstraint(Choco.domainChanneling(pos[i], bools));
            m.addConstraint(Choco.neq(pos[i], 3));
            m.addConstraint(Choco.neq(pos[i], 4));
        }
        IntegerVariable nbNodes = Choco.makeIntVar("nbNodes", 3, 3, Options.V_OBJECTIVE);
        m.addConstraint(Choco.atMostNValue(nbNodes, pos));
        Solver s = new CPSolver();
        s.read(m);
        s.minimize(s.getVar(nbNodes), false);
    }

     @Test
    public void test2989765_2() {
        Model m = new CPModel();
        IntegerVariable[] pos = new IntegerVariable[4];
        for (int i = 0; i < pos.length; i++) {
            pos[i] = Choco.makeIntVar("VM" + i + "on ?", 0, 0);
            IntegerVariable [] bools = Choco.constantArray(new int[]{0,0,0,0,0,0});
            m.addConstraint(Choco.domainChanneling(pos[i], bools));
            m.addConstraint(Choco.neq(pos[i], 3));
            m.addConstraint(Choco.neq(pos[i], 4));
        }
        IntegerVariable nbNodes = Choco.makeIntVar("nbNodes", 3, 3, Options.V_OBJECTIVE);
        m.addConstraint(Choco.atMostNValue(nbNodes, pos));
        Solver s = new CPSolver();
        s.read(m);
        s.minimize(s.getVar(nbNodes), false);
    }

}
