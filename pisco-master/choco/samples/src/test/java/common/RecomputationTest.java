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
import choco.cp.solver.search.integer.branching.AssignOrForbidIntVarVal;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.memory.copy.EnvironmentCopying;
import choco.kernel.model.Model;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.Solver;
import org.junit.Assert;
import org.junit.Test;
import samples.tutorials.puzzles.DonaldGeraldRobert;
import samples.tutorials.puzzles.Queen;

import static choco.kernel.common.util.tools.StringUtils.pad;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 13 oct. 2008
 */
public class RecomputationTest {

        @Test
        public void donaldGeraldRobert(){
            DonaldGeraldRobert pb = new DonaldGeraldRobert();
            pb.buildModel();
        	Model m = pb.model;
            CPSolver s = new CPSolver();
            s.getConfiguration().putInt(Configuration.RECOMPUTATION_GAP, 10);
            Solver _s = new CPSolver();
            // Read the model
            s.read(m);
            s.addGoal(new AssignOrForbidIntVarVal(new MinDomain(s), new MinVal()));
            _s.read(m);

           // ChocoLogging.setVerbosity(Verbosity.SOLUTION);

            // Then solve it
            s.solve();
            //ChocoLogging.flushLogs();
            _s.solve();
            //ChocoLogging.flushLogs();
            // Print name value

//            Assert.assertEquals("donald is not equal",_s.getVar(DonaldGeraldRobert._donald).getVal(),s.getVar(DonaldGeraldRobert._donald).getVal());
//            Assert.assertEquals("gerald is not equal",_s.getVar(DonaldGeraldRobert._gerald).getVal(),s.getVar(DonaldGeraldRobert._gerald).getVal());
//            Assert.assertEquals("robert is not equal",_s.getVar(DonaldGeraldRobert._robert).getVal(),s.getVar(DonaldGeraldRobert._robert).getVal());
        }

    @Test
        public void nQueen() {
        //ChocoLogging.setVerbosity(Verbosity.OFF);


        System.out.print(pad("Q", 10, " "));
        System.out.print(pad("T", 15, " "));
        System.out.print(pad("C", 15, " "));
        System.out.print(pad("R+T", 15, " "));
        System.out.println(pad("R+C", 15, " "));
        for(int q = 4; q <= 11; q++){
            System.out.print(pad(pad(""+q, -2, " "),10, " "));
            Queen pb = new Queen();
            pb.readArgs("-n", Integer.toString(q));
            pb.buildModel();

            Solver s1 = new CPSolver();
            Solver s2 = new CPSolver(new EnvironmentCopying());

            CPSolver sr1 = new CPSolver();
            CPSolver sr2 = new CPSolver(new EnvironmentCopying());

            sr1.getConfiguration().putInt(Configuration.RECOMPUTATION_GAP, 10);
//            sr1.attachGoal(new AssignOrForbidIntVarVal(new MinDomain(sr1), new MinVal()));

            sr2.getConfiguration().putInt(Configuration.RECOMPUTATION_GAP, 10);
//            sr2.attachGoal(new AssignOrForbidIntVarVal(new MinDomain(sr2), new MinVal()));
            // Read the model
            sr1.read(pb.model);
            sr2.read(pb.model);
            s1.read(pb.model);
            s2.read(pb.model);

            // Then solve it
            ChocoLogging.flushLogs();
            s1.solveAll();
            System.out.print(pad(s1.getTimeCount()+"ms", -15, " "));
            s2.solveAll();
            System.out.print(pad(s2.getTimeCount()+"ms", -15, " "));
            sr1.solveAll();
            System.out.print(pad(sr1.getTimeCount()+"ms", -15, " "));
            sr2.solveAll();
            System.out.println(pad(sr2.getTimeCount()+"ms", -15, " "));

            //ChocoLogging.flushLogs();
            // Print name value

            Assert.assertEquals("nb solutions 1", sr1.getNbSolutions(), sr2.getNbSolutions());
            Assert.assertEquals("nb solutions 2", s1.getNbSolutions(), s2.getNbSolutions());
            Assert.assertEquals("nb solutions 3", s1.getNbSolutions(), sr1.getNbSolutions());

            Assert.assertEquals("nb nodes 1", sr1.getNodeCount(), sr2.getNodeCount());
            Assert.assertEquals("nb nodes 2", s1.getNodeCount(), s2.getNodeCount());
        }

    }
        


}

