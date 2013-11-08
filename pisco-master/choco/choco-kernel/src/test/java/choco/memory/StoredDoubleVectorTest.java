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

package choco.memory;

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.memory.IStateDoubleVector;
import choco.kernel.memory.trailing.EnvironmentTrailing;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

public class StoredDoubleVectorTest {

    static double PREC = Math.pow(10,15);

	protected final static Logger LOGGER = ChocoLogging.getTestLogger();
	private EnvironmentTrailing env;
	private IStateDoubleVector iVectA;
	private IStateDoubleVector iVectB;
	private IStateDoubleVector vector;

	@Before
	public void setUp() {
		LOGGER.fine("StoredIntVector Testing...");

		env = new EnvironmentTrailing();
		iVectA = env.makeDoubleVector();
		iVectB = env.makeDoubleVector(10, 1000.0);
		vector = env.makeDoubleVector();
	}

	@After
	public void tearDown() {
		vector = null;
		iVectA = null;
		iVectB = null;
		env = null;
	}

	/**
	 * testing the empty constructor with a few backtracks, additions, and updates
	 */
	@Test
	public void test1() {
		LOGGER.finer("test1");
		assertEquals(0, env.getWorldIndex());
		assertEquals(0, env.getTrailSize());
		assertTrue(iVectA.isEmpty());
		LOGGER.finest("iVectA OK in root world 0");

		env.worldPush();
		assertEquals(1, env.getWorldIndex());
		iVectA.add(0.0);
		iVectA.add(1.0);
		iVectA.add(2.0);
		iVectA.add(0.0);
		iVectA.set(3, 3.0);
		assertEquals(0.0, iVectA.get(0),PREC);
		assertEquals(1.0, iVectA.get(1),PREC);
		assertEquals(2.0, iVectA.get(2),PREC);
		assertEquals(3.0, iVectA.get(3),PREC);
		assertEquals(4, iVectA.size());
		assertEquals(1, env.getTrailSize());
		LOGGER.finest("iVectA OK in updated world 1");

		env.worldPush();
		for (int i = 0; i < 4; i++)
			iVectA.set(i, 50 + i);
		for (int i = 0; i < 4; i++)
			assertTrue(iVectA.get(i) == 50 + i);
		assertEquals(5, env.getTrailSize());
		assertEquals(2, env.getWorldIndex());
		LOGGER.finest("iVectA OK in updated world 2");

		env.worldPop();
		assertEquals(0, iVectA.get(0),PREC);
		assertEquals(1, iVectA.get(1),PREC);
		assertEquals(2, iVectA.get(2),PREC);
		assertEquals(3, iVectA.get(3),PREC);
		assertEquals(4, iVectA.size());
		assertEquals(1, env.getTrailSize());
		assertEquals(1, env.getWorldIndex());
		LOGGER.finest("iVectA OK in restored world 1");

		env.worldPop();
		assertEquals(0, env.getWorldIndex());
		assertEquals(0, env.getTrailSize());
		assertTrue(iVectA.isEmpty());
		LOGGER.finest("iVectA OK in world 0");
	}


	/**
	 * testing the two constructors with a few backtrack, additions, updates and deletions
	 */
	@Test
	public void test2() {
		assertEquals(0, env.getWorldIndex());
		assertEquals(0, env.getTrailSize());
		assertTrue(!iVectB.isEmpty());
		assertEquals(10, iVectB.size());
		for (int i = 0; i < 10; i++)
			assertEquals(1000, iVectB.get(i),PREC);
		LOGGER.finest("iVectB OK in root world 0");

		env.worldPush();
		assertEquals(1, env.getWorldIndex());
		for (int i = 0; i < 10; i++) {
			iVectB.set(i, 2000 + i);
			iVectB.set(i, 3000 + i);
		}
		for (int i = 0; i < 10; i++)
			assertEquals(3000 + i, iVectB.get(i),PREC);
		assertEquals(10, env.getTrailSize());   // 10 entries
		LOGGER.finest("iVectB OK in updated world 1");

		env.worldPush();
		assertEquals(2, env.getWorldIndex());
		for (int i = 10; i < 20; i++)
			iVectB.add(4000 + i);
		assertEquals(20, iVectB.size());
		for (int i = 10; i < 20; i++)
			assertEquals(4000 + i, iVectB.get(i),PREC);
		assertEquals(11, env.getTrailSize());  // only the size is pushed on the trail, not the additions
		for (int i = 10; i < 20; i++)
			iVectB.set(i, 5000 + i);
		assertEquals(11, env.getTrailSize());// 10 modified entries, but in same world -> nothing trailed
		LOGGER.finest("iVectB OK in updated world 2");

		LOGGER.finest("OK before worldPush");
		env.worldPush();
		assertEquals(3, env.getWorldIndex());
		for (int i = 20; i > 10; i--)
			iVectB.removeLast();
		assertEquals(10, iVectB.size());
		assertEquals(12, env.getTrailSize());  // modified the size
		LOGGER.finest("iVectB OK in updated world 3");

		LOGGER.finest("OK before worldPop");
		env.worldPop();
		assertEquals(2, env.getWorldIndex());
		assertEquals(11, env.getTrailSize());
		assertTrue(iVectB.size() == 20);
		for (int i = 10; i < 20; i++)
			assertTrue(iVectB.get(i) == 5000 + i);
		LOGGER.finest("iVectB OK in restored world 2");

		LOGGER.finest("OK before worldPop");
		env.worldPop();
		assertEquals(1, env.getWorldIndex());
		assertEquals(10, iVectB.size());
		assertEquals(10, env.getTrailSize());
		for (int i = 0; i < 10; i++)
			assertTrue(iVectB.get(i) == 3000 + i);
		LOGGER.finest("iVectB OK in restored world 1");

		LOGGER.finest("OK before worldPop");
		env.worldPop();
		assertEquals(0, env.getWorldIndex());
		assertTrue(iVectB.size() == 10);
		assertEquals(0, env.getTrailSize());
		LOGGER.finest("iVectB OK in root world 0");
	}

	/**
	 * another small currentElement
	 */
	@Test
	public void test3() {
		LOGGER.finer("test3");
		env.worldPush();
		vector.add(1);
		vector.add(2);
		env.worldPush();
		vector.set(0, 2);
		vector.add(3);
		env.worldPush();
		assertEquals(vector.size(), 3);
		assertEquals(vector.get(0), 2,PREC);
		assertEquals(vector.get(1), 2,PREC);
		assertEquals(vector.get(2), 3,PREC);

		env.worldPop();
		env.worldPop();
		assertEquals(vector.size(), 2);
		assertEquals(vector.get(0), 1,PREC);
		assertEquals(vector.get(1), 2,PREC);

		env.worldPop();
		assertEquals(vector.size(), 0);
	}
}
