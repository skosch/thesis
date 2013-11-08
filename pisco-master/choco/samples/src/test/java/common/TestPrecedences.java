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

import choco.cp.solver.CPSolver;
import junit.framework.Assert;
import org.junit.Test;
import samples.tutorials.to_sort.scheduling.PertCPM;

import java.util.Random;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 26 avr. 2010<br/>
 * Since : Choco 2.1.1<br/>
 */
public class TestPrecedences {

	
    private final static Random RANDOM = new Random();

    private void testPertCPM(int horizon, int nbSol) {
    	PertCPM ex = new PertCPM(horizon);
        ex.buildModel();
    	for (int i = 0; i < 5; i++) {
        	ex.buildSolver();
        	CPSolver s = (CPSolver) ex.solver;
        	s.setRandomSelectors(RANDOM.nextLong());
        	Assert.assertEquals(s.solveAll().booleanValue(), nbSol > 0);
        	Assert.assertEquals(s.getSolutionCount(), nbSol);
    	}
    }

    @Test
    public void testPert1() {
    	testPertCPM(17, 0);
    }

    @Test
    public void testPert2() {
    	testPertCPM(18, 154);

    }

    @Test
    public void testPert3() {
    	testPertCPM(19, 1764);
    }


}
