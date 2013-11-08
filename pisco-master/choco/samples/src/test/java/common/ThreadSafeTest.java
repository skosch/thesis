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

import choco.kernel.common.logging.ChocoLogging;
import junit.framework.Assert;
import org.junit.Test;
import samples.tutorials.PatternExample;
import samples.tutorials.continuous.CycloHexan;
import samples.tutorials.puzzles.Queen;
import samples.tutorials.set.SteinerSystem;

import java.util.logging.Logger;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 12 mars 2010<br/>
 * Since : Choco 2.1.1<br/>
 */
public class ThreadSafeTest {

    private static final Logger LOGGER  = ChocoLogging.getTestLogger();

    private static class ThreadProblem extends Thread {
        private final PatternExample toRun;

        public ThreadProblem(final PatternExample example) {
            this.toRun = example;
        }

        /**
         * If this thread was constructed using a separate
         * <code>Runnable</code> run object, then that
         * <code>Runnable</code> object's <code>run</code> method is called;
         * otherwise, this method does nothing and returns.
         * <p/>
         * Subclasses of <code>Thread</code> should override this method.
         *
         * @see #start()
         * @see #stop()
         * @see #Thread(ThreadGroup, Runnable, String)
         */
        @Override
        public void run() {
		    toRun.execute();
            Assert.assertTrue(toRun.solver.isFeasible());
        }
    }

    public ThreadSafeTest() throws InterruptedException {
        for (int i = 0; i < 20; i++) {
            new ThreadProblem(new Queen()).start();
//            Thread.sleep(2);
            new ThreadProblem(new SteinerSystem()).start();
//            Thread.sleep(1);
            new ThreadProblem(new CycloHexan()).start();
        }
    }

    public static void main(String[] args) throws InterruptedException {
//        ChocoLogging.setVerbosity(Verbosity.VERBOSE);
        new ThreadSafeTest();
    }

    @Test
    public void test(){
        // for test to run
        Assert.assertTrue(true);
    }

}
