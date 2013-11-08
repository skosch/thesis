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
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.copy.EnvironmentCopying;
import choco.kernel.memory.trailing.EnvironmentTrailing;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;
/**
 * a class implementing tests for backtrackable search
 */
public class StoredIntTest {
	protected final static Logger LOGGER = ChocoLogging.getTestLogger();
  private EnvironmentTrailing env;
  private IStateInt x1;
  private IStateInt x2;
  private IStateInt x3;

    @Before
  public void setUp() {
    LOGGER.fine("StoredInt Testing...");
    env = new EnvironmentTrailing();
    x1 = env.makeInt(0);
    x2 = env.makeInt(0);
    x3 = env.makeInt();
  }

    @After
  public void tearDown() {
    x1 = null;
    x2 = null;
    x3 = null;
    env = null;
  }

  /**
   * testing one backtrack
   */
  @Test
  public void test1() {
    LOGGER.finer("test1");
    assertTrue(env.getWorldIndex() == 0);
    assertTrue(env.getTrailSize() == 0);
    env.worldPush();
    x1.set(1);
    assertTrue(x1.get() == 1);
    assertTrue(env.getWorldIndex() == 1);
    assertTrue(env.getTrailSize() == 1);
    env.worldPop();
    assertTrue(x1.get() == 0);
    assertTrue(env.getWorldIndex() == 0);
    assertTrue(env.getTrailSize() == 0);
  }

  /**
   * testing a bunch of backtracks
   */
  @Test
  public void test2() {
    LOGGER.finer("test2");
    assertTrue(env.getWorldIndex() == 0);
    assertTrue(env.getTrailSize() == 0);
    for (int i = 1; i <= 100; i++) {
      env.worldPush();
      x1.set(i);
      x1.set(2 * i);
      x1.set(3 * i);
      x1.set(i);
      assertTrue(env.getWorldIndex() == i);
      assertTrue(env.getTrailSize() == i);
      assertTrue(x1.get() == i);
    }
    for (int i = 100; i >= 1; i--) {
      env.worldPop();
      assertTrue(env.getWorldIndex() == i - 1);
      assertTrue(env.getTrailSize() == i - 1);
      assertTrue(x1.get() == i - 1);
    }
  }

    @Test
    public void test3(){
        EnvironmentTrailing et = new EnvironmentTrailing();
        EnvironmentCopying ec = new EnvironmentCopying();
        IStateInt t1 = et.makeInt(0);
        IStateInt c1 = ec.makeInt(0);

        et.worldPush();
        ec.worldPush();
        Assert.assertEquals(t1.get(), c1.get());

        t1.set(1);
        c1.set(1);
        Assert.assertEquals(t1.get(), c1.get());

        et.worldPush();
        ec.worldPush();
        Assert.assertEquals(t1.get(), c1.get());

        t1.set(2);
        c1.set(2);
        Assert.assertEquals(t1.get(), c1.get());

        et.worldPush();
        ec.worldPush();
        Assert.assertEquals(t1.get(), c1.get());

        t1.set(1);
        c1.set(1);
        Assert.assertEquals(t1.get(), c1.get());

        et.worldPop();
        ec.worldPop();
        Assert.assertEquals(t1.get(), c1.get());

        et.worldPop();
        ec.worldPop();
        Assert.assertEquals(t1.get(), c1.get());

        et.worldPop();
        ec.worldPop();
        Assert.assertEquals(t1.get(), c1.get());

        et.worldPush();
        ec.worldPush();
        Assert.assertEquals(t1.get(), c1.get());

        t1.set(1);
        c1.set(1);
        Assert.assertEquals(t1.get(), c1.get());
    }


}