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

package common;

import choco.kernel.common.logging.ChocoLogging;
import junit.framework.Assert;
import org.junit.Test;
import samples.tutorials.to_sort.scheduling.OpenShopScheduling;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Arnaud Malapert
 */
public class TestRandomOpenShop {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    private final OpenShopScheduling oss = new OpenShopScheduling();

    private int opt;

    private final static int[][] generateRandOS(int n, int maxDur, int seed) {
        final Random rnd = new Random(seed);
        final int[][] durations = new int[n][n];
        LOGGER.log(Level.INFO, "generate a new open shop instance {0}x{0}", n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                durations[i][j] = rnd.nextInt(maxDur);
            }
        }
        return durations;
    }


    private void testOSS(int[][] durations) {
        oss.setDurations(durations);
        oss.execute("-b", "0");
        if (oss.solver.existsSolution() && oss.solver.isObjectiveOptimal()) {
            opt = oss.solver.getOptimumValue().intValue();
            for (int i = 1; i < 3; i++) {
                oss.setDurations(durations);
                oss.execute("-b", Integer.toString(i));
                if (oss.solver.existsSolution()) {
                    if (oss.solver.isObjectiveOptimal()) {
                        Assert.assertEquals("branching " + oss.branching, opt, oss.solver.getObjectiveValue());
                    } else {
                        Assert.assertTrue("branching " + oss.branching, opt <= oss.solver.getObjectiveValue().intValue());
                    }
                }
            }
        } else LOGGER.info("Unknown optimum : test cancelled");
    }

    public void testOSS(int nbTests, int n, int maxDur) {
        LOGGER.setLevel(Level.INFO);
        for (int i = 0; i < nbTests; i++) {
            testOSS(generateRandOS(n, maxDur, i));
        }
        ChocoLogging.flushLogs();
    }

    @Test
    public void openShop2() {
        testOSS(5, 2, 100);
    }

    @Test
    public void openShop3() {
        testOSS(10, 3, 50);
    }

    @Test
    public void openShop4() {
        testOSS(4, 4, 40);
    }

    @Test
    public void openShop5() {
        testOSS(3, 5, 12);
    }


}
