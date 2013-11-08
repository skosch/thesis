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

/**
 *
 */
package common;

import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.ResolutionPolicy;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import samples.tutorials.to_sort.MinimumEdgeDeletion;

import java.util.Arrays;
import java.util.logging.Logger;

import static choco.kernel.solver.Configuration.*;
import static org.junit.Assert.assertEquals;

public class TestStrategyMed {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    private final MinimumEdgeDeletion med = new MedShaker();

    private Number objective = null;

    private final static Configuration CONFIG = new Configuration();

    @BeforeClass
    public static void setUp() {
        CONFIG.putFalse(STOP_AT_FIRST_SOLUTION);
        CONFIG.putEnum(Configuration.RESOLUTION_POLICY, ResolutionPolicy.MINIMIZE);
        CONFIG.putInt(RESTART_BASE, 1);//many restarts, bad performance but good testing !
    }

    private void solve(boolean randomSelectors) {
        med.buildSolver();
        if (randomSelectors) ((CPSolver) med.solver).setRandomSelectors(0);
        med.solve();
        med.prettyOut();
        assertEquals("Minimum Edge Deletion is Feasible", Boolean.TRUE, med.solver.isFeasible());
        if (objective == null) {
            objective = med.solver.getOptimumValue();
        } else {
            assertEquals("objective", objective, med.solver.getOptimumValue());
        }
    }

    private void recursiveTestMED(Object parametersMED, String... confBoolValues) {
        if (confBoolValues != null && confBoolValues.length > 0) {
            final int n = confBoolValues.length - 1;
            final String[] newConfboolValues = Arrays.copyOf(confBoolValues, n);
            CONFIG.putFalse(confBoolValues[n]);
            recursiveTestMED(parametersMED, newConfboolValues);
            CONFIG.putTrue(confBoolValues[n]);
            recursiveTestMED(parametersMED, newConfboolValues);
        } else {
            //configuration is set: solve instance
            LOGGER.info(CONFIG.toString());
            solve(false);
            solve(true);
        }

    }

    /**
     * shake a little bit the optimization options.
     *
     * @param parametersMED parameters of the  minimum edge deletion
     */
    public void testMED(String... parametersMED) {
        //CONFIG.clear();
        med.readArgs(parametersMED);
        med.buildModel();
        objective = null;
        recursiveTestMED(parametersMED,
                RESTART_LUBY, RESTART_AFTER_SOLUTION, NOGOOD_RECORDING_FROM_RESTART
                , BOTTOM_UP
                , INIT_SHAVING, INIT_DESTRUCTIVE_LOWER_BOUND
        );
    }

    @Test
    public void testMinimumEquivalenceDetection1() {
        testMED("-n", "6", "-p", "0.7", "-seed", "2");
    }

    @Test
    public void testMinimumEquivalenceDetection2() {
        testMED("-n", "8", "-p", "0.6");
    }

    @Test
    public void testMinimumEquivalenceDetection3() {
        testMED("-n", "9", "-p", "0.6", "-seed", "6");

    }

    @Test
    //@Ignore
    public void testMinimumEquivalenceDetection4() {
        testMED("-n", "10", "-p", "0.9", "-seed", "1");
    }

    @Test
    @Ignore
    public void testLargeMinimumEquivalenceDetection() {
        testMED("-n", "15", "-p", "0.4");
    }

    class MedShaker extends MinimumEdgeDeletion {

        @Override
        public void buildSolver() {
            solver = new CPSolver(CONFIG);
            solver.monitorFailLimit(true);
            solver.read(model);
        }
    }
}




