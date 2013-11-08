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

package choco.cp.common.util.preprocessor;

import choco.cp.model.CPModel;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.variables.MultipleVariables;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 2 avr. 2010br/>
 * Since : Choco 2.1.1<br/>
 */
public abstract class DetectorFactory {
    /**
     * Logger
     */
    protected static final Logger LOGGER = ChocoLogging.getEngineLogger();

    /**
     * Add an index to the variables to be able to map them easily
     * to nodes of the constraint graph
     *
     * @param m model
     */
    public static void associateIndexes(final CPModel m) {
        Iterator it = m.getIntVarIterator();
        int cpt = 0;
        while (it.hasNext()) {
            final IntegerVariable iv = (IntegerVariable) it.next();
            iv.setHook(cpt);
            cpt++;
        }
        it = m.getMultipleVarIterator();
        cpt = 0;
        while (it.hasNext()) {
            final MultipleVariables iv = (MultipleVariables) it.next();
            if (iv instanceof TaskVariable) {
                iv.setHook(cpt);
                cpt++;
            }
        }
    }


    public static void associateIndexes(int from, Variable... ivars) {
        int cpt = from;
        for (int i = 0; i < ivars.length; i++) {
            ivars[i].setHook(cpt++);
        }
    }

    /**
     * Get the max hook from a list of multiple variables
     * @param model current model
     * @return
     */
    public static int maxHookOnMultipleVariables(CPModel model) {
        Iterator<MultipleVariables> it = model.getMultipleVarIterator();
        int hook = Integer.MIN_VALUE;
        while (it.hasNext()) {
            final MultipleVariables iv = it.next();
            if (iv instanceof TaskVariable) {
                if (iv.getHook() > hook) {
                    hook = iv.getHook();
                }
            }
        }
        return Math.max(0, hook);
    }

    /**
     * Add an index to the variables to be able to map them easily
     * to nodes of the constraint graph
     *
     * @param m model
     */

    public static void resetIndexes(final CPModel m) {
        Iterator it = m.getIntVarIterator();
        while (it.hasNext()) {
            final IntegerVariable iv = (IntegerVariable) it.next();
            iv.resetHook();
        }
        it = m.getMultipleVarIterator();
        while (it.hasNext()) {
            final MultipleVariables iv = (MultipleVariables) it.next();
            if (iv instanceof TaskVariable) {
                iv.resetHook();
            }
        }
    }
}
