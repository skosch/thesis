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

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.objects.IPrioritizable;
import choco.kernel.common.util.objects.PriorityQueue;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

public class PriorityQueueTest {
 
	protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    @Before
  public void setUp() {
    LOGGER.fine("PriorityQueue Testing...");
  }

    @After
  public void tearDown() {
  }

    @Test
  public void test1() {
    LOGGER.finer("test1");

    Entity obj1 = new Entity("Objet 1", 2);
    Entity obj2 = new Entity("Objet 2", 0);
    Entity obj3 = new Entity("Objet 3", 3);
    Entity obj4 = new Entity("Objet 4", 1);

    PriorityQueue queue = new PriorityQueue(4);

    Object[] ret;

    LOGGER.finest("Step 1");
    queue.add(obj1);

    ret = queue.toArray();
    assertEquals(ret.length, 1);
    assertEquals(ret[0], obj1);

    LOGGER.finest("Step 2");
    queue.add(obj2);

    ret = queue.toArray();
    assertEquals(ret.length, 2);
    assertEquals(ret[0], obj2);
    assertEquals(ret[1], obj1);

    LOGGER.finest("Step 3");
    queue.add(obj3);

    ret = queue.toArray();
    assertEquals(ret.length, 3);
    assertEquals(ret[0], obj2);
    assertEquals(ret[1], obj1);
    assertEquals(ret[2], obj3);

    LOGGER.finest("Step 4");
    queue.add(obj4);

    ret = queue.toArray();
    assertEquals(ret.length, 4);
    assertEquals(ret[0], obj2);
    assertEquals(ret[1], obj4);
    assertEquals(ret[2], obj1);
    assertEquals(ret[3], obj3);

    LOGGER.finest("Step 5");
    obj3.priority = 1;
    queue.updatePriority(obj3);

    ret = queue.toArray();
    assertEquals(ret.length, 4);
    assertEquals(ret[0], obj2);
    assertEquals(ret[1], obj4);
    assertEquals(ret[2], obj3);
    assertEquals(ret[3], obj1);

    LOGGER.finest("Step 6");
    obj2.priority = 3;
    queue.updatePriority(obj2);

    ret = queue.toArray();
    assertEquals(ret.length, 4);
    assertEquals(ret[0], obj4);
    assertEquals(ret[1], obj3);
    assertEquals(ret[2], obj1);
    assertEquals(ret[3], obj2);

    LOGGER.finest("Step 7");
    Object obj = queue.popFirst();

    ret = queue.toArray();
    assertEquals(ret.length, 3);
    assertEquals(obj, obj4);
    assertEquals(ret[0], obj3);
    assertEquals(ret[1], obj1);
    assertEquals(ret[2], obj2);
  }

  private class Entity implements IPrioritizable {
    public String name;
    public int priority;

    public Entity(String name, int prio) {
      this.name = name;
      this.priority = prio;
    }

    public int getPriority() {
      return priority;
    }

    @Override
	public String toString() {
      return this.name + " " + this.priority;
    }
  }
}
