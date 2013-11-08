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

package samples.jobshop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import choco.cp.solver.CPSolver;
import choco.cp.solver.variables.integer.IntDomainVarImpl;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

public class SimpleDTConstraintTest {
    private static final Random RAND = new Random();
    private IntDomainVar v0;
    private IntDomainVar v1;

    @Before
    public void setUp() {
        final Solver s = new CPSolver();
        v0 = new IntDomainVarImpl(s, "v0", IntDomainVar.BITSET, 0, 99);
        v1 = new IntDomainVarImpl(s, "v0", IntDomainVar.BITSET, 0, 99);
    }

    @Test
    public void nextAllowedTest() {
        for (int i = 10000; --i >= 0;) {
            final SimpleDTConstraint sdt = new SimpleDTConstraint(v0, v1, RAND
                    .nextInt(100), RAND.nextInt(100));

            final int[] tuple = new int[2];
            tuple[0] = RAND.nextInt(100);
            tuple[1] = RAND.nextInt(100);

            final boolean allowed = sdt.check(tuple);
            if (allowed) {
                assertEquals("Did not work for " + Arrays.toString(tuple),
                        tuple[0], sdt.nextAllowed(1, tuple[1], tuple[0]));
                assertEquals("Did not work for " + Arrays.toString(tuple),
                        tuple[1], sdt.nextAllowed(0, tuple[0], tuple[1]));
            } else {
                assertTrue(sdt.check(new int[] { tuple[0],
                        sdt.nextAllowed(0, tuple[0], tuple[1]) }));
                assertTrue(sdt.check(new int[] {
                        sdt.nextAllowed(1, tuple[1], tuple[0]), tuple[1] }));
            }
        }
    }
}
