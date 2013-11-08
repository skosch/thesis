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

package common;

import choco.cp.solver.CPSolver;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.copy.EnvironmentCopying;
import choco.kernel.model.Model;
import choco.kernel.solver.Solver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import samples.tutorials.puzzles.DonaldGeraldRobert;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 13 oct. 2008
 * Time: 10:45:04
 * To change this template use File | Settings | File Templates.
 */
public class CopyTest {

    IEnvironment env;

    @Before
    public void before(){
        env = new EnvironmentCopying();
    }

    @After
    public void after(){
        env = null;
    }

    @Test
        public void donaldGeraldRobert(){
            DonaldGeraldRobert pb = new DonaldGeraldRobert();
            pb.buildModel();
        	Model m = pb.model;
            Solver s = new CPSolver(env);
            Solver _s = new CPSolver();
            // Read the model
            s.read(m);
            _s.read(m);

            // Then solve it
            s.solve();
            _s.solve();

            // Print name value
//            Assert.assertEquals("donald is not equal",_s.getVar(_donald).getVal(),s.getVar(_donald).getVal());
//            Assert.assertEquals("gerald is not equal",_s.getVar(_gerald).getVal(),s.getVar(_gerald).getVal());
//            Assert.assertEquals("robert is not equal",_s.getVar(_robert).getVal(),s.getVar(_robert).getVal());
        }



}
