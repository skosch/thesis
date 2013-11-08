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

import choco.kernel.common.Constant;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.structure.PartiallyStoredIntVector;
import choco.kernel.memory.trailing.EnvironmentTrailing;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PartiallyStoredIntVectorTest {
  
	protected final static Logger LOGGER = ChocoLogging.getTestLogger();
	
  private IEnvironment env;
  private PartiallyStoredIntVector vector;

    @Before
  public void setUp() {
    LOGGER.fine("StoredIntVector Testing...");

    env = new EnvironmentTrailing();
    vector = env.makePartiallyStoredIntVector();
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
    int[] values = new int[]{4, 0, 1, 2, 3};
    int nValue = 0;
    DisposableIntIterator it = vector.getIndexIterator();
    for (; it.hasNext();) {
      int index = it.next();
      int value = vector.get(index);
      assertEquals(values[nValue], value);
      nValue++;
    }
      it.dispose();
    env.worldPop();
    assertTrue(vector.size() == 3);
    values = new int[]{4, 0, 1};
    nValue = 0;
    for (it = vector.getIndexIterator(); it.hasNext();) {
      int index = it.next();
      int value = vector.get(index);
      assertEquals(values[nValue], value);
      nValue++;
    }
    it.dispose();
    assertEquals(1, env.getWorldIndex());
    env.worldPop();
    assertTrue(vector.size() == 1);
    values = new int[]{4};
    nValue = 0;
    for (it = vector.getIndexIterator(); it.hasNext();) {
      int index = it.next();
      int value = vector.get(index);
      assertEquals(values[nValue], value);
      nValue++;
    }
      it.dispose();

    assertEquals(0, env.getWorldIndex());
  }

    @Test
    public void test2(){
        DisposableIntIterator it = vector.getIndexIterator();
        Assert.assertFalse(it.hasNext());
        it.dispose();

        vector.staticAdd(5);
        it = vector.getIndexIterator();
        Assert.assertTrue(it.hasNext());
        int ind = it.next();
        Assert.assertEquals(0, ind);
        Assert.assertEquals(5, vector.get(ind));
        Assert.assertFalse(it.hasNext());
        it.dispose();

        vector.remove(0);
        vector.add(5);
        it = vector.getIndexIterator();
        Assert.assertTrue(it.hasNext());
        ind = it.next();
        Assert.assertEquals(Constant.STORED_OFFSET, ind);
        Assert.assertEquals(5, vector.get(ind));
        Assert.assertFalse(it.hasNext());
        it.dispose();

        vector.staticAdd(4);
        it = vector.getIndexIterator();
        Assert.assertTrue(it.hasNext());
        ind = it.next();
        Assert.assertEquals(0, ind);
        Assert.assertEquals(4, vector.get(ind));
        Assert.assertTrue(it.hasNext());
        ind = it.next();
        Assert.assertEquals(Constant.STORED_OFFSET, ind);
        Assert.assertEquals(5, vector.get(ind));
        Assert.assertFalse(it.hasNext());


    }
}
