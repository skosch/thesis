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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import choco.kernel.common.opres.nosum.NoSumList;

/**
 * @author Arnaud Malapert
 *
 */
@SuppressWarnings({"PMD.LocalVariableCouldBeFinal","PMD.MethodArgumentCouldBeFinal"})
public class TestNoSum {

	private final static String MSG="noSum";

	//arrays sorted in non-increasing order
	private final int[] sizes={20,17,15,13,11,9,7,5,3,2,1};

	private final int[] sizes2={100,85,70,60,47,35,27,17,6,4};

	private NoSumList nosum;

	private void initialize(int[] sizes) {
		nosum=new NoSumList(sizes);
		nosum.fillCandidates();
	}

	@Test
	public void testHasSum() {
		initialize(sizes);
		assertFalse(MSG,nosum.noSum(10, 20));
		assertFalse(MSG,nosum.noSum(34, 40));
		assertFalse(MSG,nosum.noSum(65,80));
		assertFalse(MSG,nosum.noSum(65,80));
		assertFalse(MSG,nosum.noSum(85,85));
		assertFalse(MSG,nosum.noSum(103,103));
		assertFalse(MSG,nosum.noSum(50,55));
		assertFalse(MSG,nosum.noSum(55,65));
	}

	@Test
	public void testHasSum2() {
		initialize(sizes2);
		assertFalse(MSG,nosum.noSum(35,39));
		assertFalse(MSG,nosum.noSum(50,51));
		assertFalse(MSG,nosum.noSum(215,225));
		assertFalse(MSG,nosum.noSum(313,314));
		assertFalse(MSG,nosum.noSum(375,378));
		assertFalse(MSG,nosum.noSum(421,427));
		assertFalse(MSG,nosum.noSum(431,436));
	}

	@Test
	public void testHasSumBug() {
		int[] sizes={8,7,6,1};
		initialize(sizes);
		assertFalse(MSG,nosum.noSum(9,9));
	}

	@Test
	public void testNoSum() {
		initialize(sizes);
		nosum.remove(8);
		nosum.remove(9);
		assertTrue(MSG,nosum.noSum(94,96));
		assertTrue(MSG,nosum.noSum(2,4));
		nosum.remove(6);
		assertTrue(MSG,nosum.noSum(7,8));
		assertFalse(MSG,nosum.noSum(82,84));
		assertTrue(MSG,nosum.noSum(87,89));
		nosum.remove(7);
		assertTrue(MSG,nosum.noSum(2,8));
		assertTrue(MSG,nosum.noSum(78,84));
	}

	@Test
	public void testNoSum2() {
		initialize(sizes2);
		assertTrue(MSG,nosum.noSum(11,16));
		assertFalse(MSG,nosum.noSum(42,43)); //false positive
		assertFalse(MSG,nosum.noSum(408,409)); //false positive
		assertFalse(MSG,nosum.noSum(421,423)); //false positive
		assertTrue(MSG,nosum.noSum(435,440));
		assertTrue(MSG,nosum.noSum(448,450));
	}
}


