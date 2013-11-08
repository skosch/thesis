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

package choco.cp.common.util.preprocessor.detector;

import choco.cp.common.util.preprocessor.AbstractAdvancedDetector;
import choco.cp.common.util.preprocessor.graph.ArrayGraph;
import choco.cp.common.util.preprocessor.graph.MaxCliques;
import choco.cp.model.CPModel;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerVariable;

import java.util.Iterator;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 2 avr. 2010br/>
 * Since : Choco 2.1.1<br/>
 *
 * A class dedicated to detect clique of differences or disjonctions
 * and state the corresponding global constraints
 */
public abstract class AbstractGraphBasedDetector extends AbstractAdvancedDetector {

    /**
     * The graph of differences
     */
    protected ArrayGraph diffs;

    protected CliqueIterator itc;

    protected AbstractGraphBasedDetector(final CPModel model) {
        super(model);
        this.diffs = new ArrayGraph(model.getNbIntVars());
    }


    public void addEdge(Variable a, Variable b, Constraint c) {
        final int idxa = a.getHook();
        final int idxb = b.getHook();
        diffs.addEdge(idxa, idxb);
        diffs.storeEdge(c, idxa, idxb);
    }

    public void removeConstraint(int a, int b) {
        delete(diffs.getConstraintEdge(a, b));
    }

    //**************************************************************//
    //******************** Iterator on cliques ********************//
    //*************************************************************//


    /**
     * An iterator over all the cliques detected by the Bron and Kerbosh
     *
     * @return CliqueIterator
     */
    public CliqueIterator cliqueIterator() {
        if (itc == null) {
            return new CliqueIterator();
        } else {
            itc.init();
            return itc;
        }
    }

    public class CliqueIterator implements Iterator<IntegerVariable[]> {

        protected int idx = 0;

        protected int[][] clique;

        public CliqueIterator() {
            MaxCliques mc = new MaxCliques(diffs);
            clique = mc.getMaxCliques();
        }

        public void init() {
            idx = 0;
        }

        public boolean hasNext() {
            return idx < clique.length;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public IntegerVariable[] next() {
            IntegerVariable[] c = new IntegerVariable[clique[idx].length];
            for (int j = 0; j < c.length; j++) {
                c[j] = model.getIntVar(clique[idx][j]);
            }
            idx++;
            return c;
        }


        public void remove() {
            int id = idx - 1;
            for (int j = 0; j < clique[id].length; j++) {
                for (int k = j + 1; k < clique[id].length; k++) {
                    diffs.remEdge(clique[id][j], clique[id][k]);
                    removeConstraint(clique[id][j],
                            clique[id][k]);
                }
            }
        }
    }

}
