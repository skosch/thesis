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

import static junit.framework.Assert.assertEquals;
import gnu.trove.TIntArrayList;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.opres.pack.AbstractFunctionDFF;
import choco.kernel.common.opres.pack.BestFit1BP;
import choco.kernel.common.opres.pack.ComputeL0;
import choco.kernel.common.opres.pack.FirstFit1BP;
import choco.kernel.common.opres.pack.FunctionF0;
import choco.kernel.common.opres.pack.FunctionFCCM1;
import choco.kernel.common.opres.pack.LowerBoundFactory;
import choco.kernel.common.util.tools.MathUtils;

/**
 * @author Arnaud Malapert
 *
 */
public class TestMDFF {
	
	public final static Logger LOGGER = ChocoLogging.getTestLogger();

	public final static int[][] SIZES= {
		{1, 3, 3, 5, 6, 7, 8, 9, 10, 12, 13, 14},
		{0, 0, 0, 0, 0, 0, 7, 7, 8 , 8 , 9, 9},
		{0, 0, 2, 3, 3, 4, 7, 11, 12, 14, 15, 15},
		{0, 0, 2, 3, 7, 8, 8, 12, 12, 13, 14, 14},
		{0, 1, 3, 3, 5, 5, 11, 11, 11, 12, 13, 15},
		{0,0,0,0,0,0,0,0,0,0,0,0}
	};

	public final static int[] L0={7,4,6,7,6,0};

	public final static int[] FIRST_FIT={7,4,6,7,7,0};
	public final static int[] BEST_FIT={7,4,6,7,7,0};

	public final static int CAPACITY=15;

	private AbstractFunctionDFF ddff;

	private final TIntArrayList items = new TIntArrayList();



	private void setItems(int idx) {
		items.resetQuick();
		items.add(SIZES[idx]);
	}

	@Test
	public void testL0() {
		ComputeL0 l0 = new ComputeL0();
		l0.setCapacity(CAPACITY);
		for (int i = 0; i < SIZES.length; i++) {
			setItems(i);
			items.forEachDescending(l0);
			assertEquals("L0 "+i, L0[i],l0.getLowerBound());
			l0.reset();
		}
	}

	@Test
	public void testFirstFit() {
		FirstFit1BP h = new FirstFit1BP();
		h.setCapacity(CAPACITY);
		h.setItems(items);
		for (int i = 0; i < SIZES.length; i++) {
			setItems(i);
			h.execute();
			assertEquals("First Fit "+i, FIRST_FIT[i],h.getNumberOfBins());
			h.reset();
		}
	}

	@Test
	public void testBestFit() {
		BestFit1BP h = new BestFit1BP();
		h.setCapacity(CAPACITY);
		h.setItems(items);
		for (int i = 0; i < SIZES.length; i++) {
			setItems(i);
			h.execute();
			assertEquals("Best Fit "+i, BEST_FIT[i],h.getNumberOfBins());
			h.reset();
		}
	}

	private void testDDFF(int[] source, int[] expected, int expectedCapa) {
		for (int i = 0; i < SIZES[0].length; i++) {
			assertEquals("Item "+i,expected[i], ddff.execute(source[i]));
		}
		assertEquals(expectedCapa, ddff.transformCapacity());
	}



	@Test
	public void testF0() {
		final int[][] res={ 
				{0, 0, 0, 5, 6, 7, 8, 9, 10, 15, 15, 15},
				{0, 0, 0, 0, 6, 7, 8, 9, 15, 15, 15, 15},
				{0, 0, 0, 0, 0, 0, 7, 7, 8 , 8 , 15, 15}
		};
		ddff = new FunctionF0();
		ddff.setCapacity(CAPACITY);
		ddff.setParameter(4);
		testDDFF(SIZES[0], res[0], CAPACITY);
		ddff.setParameter(6);
		testDDFF(SIZES[0], res[1], CAPACITY);
		ddff.setParameter(7);
		testDDFF(SIZES[1], res[2], CAPACITY);
	}

	@Test
	public void testF2() {
		final int[][] res={ 
				{0, 2, 2, 2, 4, 4, 6, 6, 8, 8, 10, 10},
				{0, 0, 0, 2, 2, 2, 4, 4, 4, 6, 6, 6},
				{0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 4, 4}
		};
		ddff =new FunctionFCCM1();
		ddff.setCapacity(CAPACITY);
		ddff.setParameter(3);
		testDDFF(SIZES[0], res[0], 10);
		ddff.setParameter(4);
		testDDFF(SIZES[0], res[1], 6);
		ddff.setParameter(7);
		testDDFF(SIZES[1], res[2], 4);
	}
	@Test
	public void testBugF2() {
		final int[] bugF2 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
				1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 
				2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 
				3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 7, 7, 7, 
				8, 8, 9, 9, 9, 10, 11, 11, 11, 11, 11, 11, 12, 13, 14, 15, 15};
		ddff =new FunctionFCCM1();
		ddff.setCapacity(CAPACITY);
		ddff.setParameter(1);
		for (int i = 0; i < SIZES[0].length; i++) {
			assertEquals(2 * bugF2[i], ddff.execute(bugF2[i]));
		}
		assertEquals(2 * CAPACITY, ddff.transformCapacity());
	}

	private void generatePacking(int nbB, int capa, long seed) {
		items.resetQuick();
		final Random rnd = new Random(seed);
		int b = 0;
		while(b < nbB) {
			int c = capa;
			while( c > 0) {
				final int s =  rnd.nextInt(c + 1);
				items.add(s);
				c -= s;
			}
			b++;
		}
		items.sort();
		assertEquals( MathUtils.sum(items.toNativeArray()), nbB*capa);
	}


	@Test
	public void testPacking() {
		final int n = 10;
		for (int seed = 0; seed < n; seed++) {
			generatePacking(20, CAPACITY, seed);
			assertEquals("seed "+seed, 20, LowerBoundFactory.computeAllMDFF(items, CAPACITY, Integer.MAX_VALUE));

		}
	}

	@Test
	public void testPackingLargeCapa() {
		final int n = 10;
		for (int seed = 0; seed < n; seed++) {
			generatePacking(20, CAPACITY*10, seed);
			assertEquals(20, LowerBoundFactory.memComputeAllMDFF(items, CAPACITY*10, Integer.MAX_VALUE));
		}
	}

	@Test
	public void testDominanceMDFF() {
		int cpt1 = 0, cpt2 = 0, cpt3 = 0, cpt4=0;
		final int n = 200;
		final int capa = CAPACITY*10;
		final Random rnd = new Random();
		for (int seed = 0; seed < n; seed++) {
			rnd.setSeed(seed);
			items.resetQuick();
			for (int i = 0; i < n; i++) {
				items.add(rnd.nextInt(capa));
			}
			items.sort();
			int lb1 = LowerBoundFactory.memComputeL0(items, capa);
			int lb2 = LowerBoundFactory.memComputeF0(items, capa, Integer.MAX_VALUE);
			Assert.assertTrue("F0 > L0", lb2 >= lb1);
			if(lb2 > lb1) cpt1++;
			int lb3 = LowerBoundFactory.memComputeFCCM1(items, capa, Integer.MAX_VALUE);
			Assert.assertTrue("FCCM,1 > L0", lb3 >= lb1);
			if(lb3 > lb1) cpt2++;
			if(lb3 > lb2) cpt3++;
			else if(lb3 < lb2) cpt4++;
			LOGGER.config("L0="+lb1+" F0="+lb2+" FCCM="+lb3);
		}
		LOGGER.info("F0 > L0: "+cpt1+"/"+n+"\nFCCM,1 > L0: "+cpt2+"/"+n+"\nFCCM,1 > F0: "+cpt3+"/"+n+"\nFCCM,1 < F0: "+cpt4+"/"+n);

	}
}
