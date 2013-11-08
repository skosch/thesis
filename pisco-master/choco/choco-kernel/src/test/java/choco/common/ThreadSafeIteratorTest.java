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
import choco.kernel.common.util.iterators.ArrayIterator;
import choco.kernel.common.util.iterators.DisposableIterator;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 26 mars 2010<br/>
 * Since : Choco 2.1.1<br/>
 */
public class ThreadSafeIteratorTest {

    private static final Logger LOGGER = ChocoLogging.getTestLogger();

    private class ThreadArray<E> extends Thread {
        private final E[] toiterate;
        private final int size;

        public ThreadArray(final E[] array) {
            this.toiterate = array;
            this.size = array.length;
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
            DisposableIterator<E> it = ArrayIterator.getIterator(toiterate, size);
            while (it.hasNext()) {
                it.next();
                try {
                    sleep(1);
                } catch (InterruptedException e) {
                    Assert.fail();
                }
            }
            it.dispose();
        }
    }


    private static void test1() {
        long t = -System.currentTimeMillis();
        for (int i = 0; i < 400000; i++) {
            Integer[] array = new Integer[1000];
            Arrays.fill(array, i);
            DisposableIterator<Integer> it = ArrayIterator.getIterator(array, array.length);
            while (it.hasNext()) {
                it.next();
            }
            it.dispose();
        }
        t += System.currentTimeMillis();
        LOGGER.info("SEQ t:" + t);
        ChocoLogging.flushLogs();
    }

    private static void test2() {
        long t = -System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            Integer[] array = new Integer[100];
            Arrays.fill(array, i);
            DisposableIterator<Integer> it = ArrayIterator.getIterator(array, array.length);
            while (it.hasNext()) {
                it.next();
                for (int j = 0; j < 400; j++) {
                    Integer[] array2 = new Integer[10];
                    Arrays.fill(array2, j);
                    DisposableIterator<Integer> it2 = ArrayIterator.getIterator(array2, array2.length);
                    while (it2.hasNext()) {
                        it2.next();
                    }
                    it2.dispose();
                }
            }
            it.dispose();
        }
        t += System.currentTimeMillis();
        LOGGER.info("SEQ t:" + t);
        ChocoLogging.flushLogs();
    }

    private void test3() throws InterruptedException {
        long t = -System.currentTimeMillis();
        for (int i = 0; i < 4000; i++) {
            Integer[] array = new Integer[100];
            Arrays.fill(array, i);
            new ThreadArray<Integer>(array).start();
            Thread.sleep(1);
        }
        t += System.currentTimeMillis();
        LOGGER.info("PAR t:" + t);
        ChocoLogging.flushLogs();
    }

    public ThreadSafeIteratorTest() throws InterruptedException {
        test1();
        test2();
        test3();
    }

    public static void main(String[] args) throws InterruptedException {
        new ThreadSafeIteratorTest();
    }

    @Test
    public void noTest() {
        Assert.assertTrue(true);
    }
}
