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

import static choco.kernel.common.Constant.STORED_OFFSET;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.structure.PartiallyStoredVector;
import choco.kernel.memory.trailing.EnvironmentTrailing;
import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;
import java.util.logging.Logger;

public class PartiallyStoredVectorTest {
	
	protected final static Logger LOGGER = ChocoLogging.getTestLogger();
	
  private IEnvironment env;
  private PartiallyStoredVector<Integer> vector;

    @Before
  public void setUp() {
    LOGGER.fine("StoredIntVector Testing...");

    env = new EnvironmentTrailing();
    vector = env.makePartiallyStoredVector();
  }

    @After
  public void tearDown() {
    vector = null;
    env = null;
  }

  /**
   * testing the empty constructor with a few backtracks, additions, and updates
   */
  @Test
  public void test1() {
    LOGGER.finer("test1");
    assertEquals(0, env.getWorldIndex());
    assertTrue(vector.isEmpty());
    env.worldPush();
    assertEquals(1, env.getWorldIndex());
    vector.add(0);
    vector.add(1);
    env.worldPush();
    assertEquals(2, env.getWorldIndex());
    vector.add(2);
    vector.add(3);
    vector.staticAdd(4);
    assertTrue(vector.size() == 5);
    env.worldPop();
    assertTrue(vector.size() == 3);
    assertEquals(1, env.getWorldIndex());
    env.worldPop();
    assertTrue(vector.size() == 1);
    assertEquals(0, env.getWorldIndex());
  }

    static Integer A=0, B=1, C=2, D=3;
    @Test
    @Ignore
    public void test2() {
        int[] types = {A, B, C, D};
        int n = 10;
        Random r = new Random(0);

        IStateInt b = env.makeInt(0);
        IStateInt c = env.makeInt(0);
        IStateInt d = env.makeInt(0);
        int bs = 0, cs = 0, ds = 0;

        long t1 = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            int obj = types[r.nextInt(types.length-1)];

            if ((obj == A)) {
                vector.insert(b.get(), obj);
                b.add(1);
                c.add(1);
                d.add(1);
            } else if ((obj == B)) {
                vector.insert(c.get(), obj);
                c.add(1);
                d.add(1);
            } else if ((obj == C)) {
                vector.insert(d.get(), obj);
                d.add(1);
            } else if ((obj == D)) {
                vector.add(obj);
            }
            env.worldPush();
        }
        long t2 = System.currentTimeMillis();
        for(int i= 0; i < bs; i++){
            Assert.assertEquals(A, vector.get(i));
        }
        for(int i= bs; i < cs; i++){
            Assert.assertEquals(B, vector.get(i));
        }
        for(int i= cs; i < ds; i++){
            Assert.assertEquals(C, vector.get(i));
        }
        for(int i= ds; i < vector.getLastStaticIndex(); i++){
            Assert.assertEquals(D, vector.get(i));
        }
        long t3 = System.currentTimeMillis();
        for(int j = 0; j < n; j++){
            env.worldPop();
            for(int i= 0; i < b.get(); i++){
            Assert.assertEquals(A, vector.get(i+ STORED_OFFSET));
            }
            for(int i= b.get(); i < c.get(); i++){
                Assert.assertEquals(B, vector.get(i+STORED_OFFSET));
            }
            for(int i= c.get(); i < d.get(); i++){
                Assert.assertEquals(C, vector.get(i+STORED_OFFSET));
            }
            for(int i= d.get(); i < vector.getLastStoredIndex()+1; i++){
                Assert.assertEquals(D, vector.get(i+STORED_OFFSET));
            }
        }
        long t4 = System.currentTimeMillis();
        LOGGER.info("t1:"+t1);
        LOGGER.info("t2:"+t2);
        LOGGER.info("t3:"+t3);
        LOGGER.info("t4:"+t4);
    }

}
