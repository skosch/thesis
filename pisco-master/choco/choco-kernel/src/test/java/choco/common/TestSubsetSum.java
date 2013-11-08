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

package choco.common;


import choco.kernel.common.opres.ssp.AbstractSubsetSumSolver;
import choco.kernel.common.opres.ssp.BellmanWithLists;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Arnaud Malapert
 *
 */
public class TestSubsetSum {

	protected void test(long[] capacities,long[] optimums,AbstractSubsetSumSolver... solvers) {
		for (AbstractSubsetSumSolver solver : solvers) {
			for (int i = 0; i < capacities.length; i++) {
				solver.setCapacity(capacities[i]);
				solver.reset();
				Assert.assertEquals("subset sum problem ",optimums[i], solver.run());
			}
		}
	}

	protected void launch(int[] sizes,long[] capacities,long[] optimums) {
		test(capacities,optimums, new BellmanWithLists(sizes,0));
	}


	@Test
	public void testSSPNoLoopNeeded() {
		int[] w={1,3,5,7,11,13};
		long[] c={9,27,40,45};
		long[] r={9,27,40,40};
		launch(w, c, r);
	}


	@Test
	public void testSSP1() {
		int[] v={2,6,8,10,16,20,27};
		long[] c={35,31,86};
		long[] r={35,30,83};
		launch(v,c,r);
	}



	@Test
	public void testSSP2()  {
		int[] v={3,6,9,12,15,18};
		long[] c={32,34,43,45,51};
		long[] r={30,33,42,45,51};
		launch(v,c,r);
	}


	@Test
	public void testSSP3()  {
		int[] v={7,14,21,28,35,42,49};
		long[] c={98,83,110,112,116};
		long[] r={98,77,105,112,112};
		launch(v,c,r);
	}

}
