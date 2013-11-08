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

import choco.Choco;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.memory.structure.StoredBipartiteSet;
import choco.kernel.memory.trailing.EnvironmentTrailing;
import choco.kernel.model.variables.scheduling.TaskVariable;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Arnaud Malapert</br> 
 * @since 5 févr. 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public class StoredBipartiteListTest {
  
	protected final static Logger LOGGER = ChocoLogging.getTestLogger();
    private EnvironmentTrailing env;
   
    private StoredBipartiteSet<TaskVariable> iVectA;
    private TaskVariable[] tasks;
    @Before
    public void setUp() {
        LOGGER.fine("StoredIntBipartiteList Testing...");

        env = new EnvironmentTrailing();
        tasks = Choco.makeTaskVarArray("T", 0, 20, new int[]{1, 2, 3, 4, 5, 10, 11, 12, 13, 14, 15, 200});
        iVectA = new StoredBipartiteSet<TaskVariable>(env, tasks);
    }

    @After
    public void tearDown() {
        iVectA = null;
        env = null;
    }

    @Test
    public void test1() {
        assertEquals(12, iVectA.size());
        LOGGER.info(StringUtils.pretty(iVectA));
        env.worldPush();
        Iterator<TaskVariable> it = iVectA.iterator();
        int cpt = 5;
        while (it.hasNext() && cpt > 0) {
            it.next();
            cpt--;
            if (cpt == 0) {
                it.remove();
            }
        }
       // it.dispose();
        LOGGER.info(StringUtils.pretty(iVectA));
        assertEquals(11, iVectA.size());
        assertEquals(tasks[10], iVectA.get(10));
        env.worldPush();
        it = iVectA.iterator();
        cpt = 6;
        while (it.hasNext() && cpt > 0) {
            it.next();
            it.remove();
            cpt--;
        }
      //  it.dispose();
        LOGGER.info(StringUtils.pretty(iVectA));
        assertEquals(5, iVectA.size());
        env.worldPop();
        LOGGER.info(StringUtils.pretty(iVectA));
        assertEquals(11, iVectA.size());
        env.worldPop();
        assertEquals(12, iVectA.size());
        env.worldPush();
        it = iVectA.iterator();
        LOGGER.info(StringUtils.pretty(iVectA));
        while (it.hasNext()) {
            LOGGER.log(Level.INFO, "value {0}", it.next());
            it.remove();
        }
       // it.dispose();
        LOGGER.info(StringUtils.pretty(iVectA));
        assertEquals(0, iVectA.size());

    }

  
}
