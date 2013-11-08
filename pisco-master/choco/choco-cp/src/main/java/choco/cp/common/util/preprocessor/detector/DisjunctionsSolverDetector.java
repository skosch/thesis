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

import choco.Choco;
import choco.cp.common.util.preprocessor.DetectorFactory;
import choco.cp.common.util.preprocessor.ExpressionTools;
import choco.cp.model.CPModel;
import choco.cp.solver.constraints.reified.ExpressionSConstraint;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.MetaConstraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;

import static choco.Choco.constant;
import static choco.Choco.makeIntVar;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 2 avr. 2010br/>
 * Since : Choco 2.1.1<br/>
 *
 * A class dedicated to detect clique of disjonctions
 * and state the corresponding global constraints
 */
public class DisjunctionsSolverDetector extends AbstractGraphBasedDetector{

    private final PreProcessCPSolver ppsolver;


    public DisjunctionsSolverDetector(final CPModel model, final PreProcessCPSolver solver) {
        super(model);
        ppsolver = solver;
    }

    private static int[] getVarIndexes(final IntegerVariable[] vs) {
        final int[] idxs = new int[vs.length];
        for (int i = 0; i < idxs.length; i++) {
            idxs[i] = vs[i].getHook();
        }
        return idxs;
    }

    /**
     * Apply the detection defined within the detector.
     */
    @Override
    public void apply() {
        int currentHook = DetectorFactory.maxHookOnMultipleVariables(model);
        final int[] durations = addAllDisjunctiveEdges(ppsolver);
        if (durations != null) {
            final BitSet[] precedenceAlreadyAdded = new BitSet[model.getNbIntVars()];
            final int nbIvar = model.getNbIntVars();
            for (int i = 0; i < nbIvar; i++){
                precedenceAlreadyAdded[i] = new BitSet(nbIvar);
            }

            final CliqueIterator it = cliqueIterator();
            while (it.hasNext()) {
                final IntegerVariable[] cl = it.next();
                final int[] idxs = getVarIndexes(cl);
                final int[] dur = new int[cl.length];
                final TaskVariable[] tasks = new TaskVariable[cl.length];
                for (int i = 0; i < cl.length; i++) {
                    dur[i] = durations[idxs[i]];
                    tasks[i] = Choco.makeTaskVar("", cl[i], constant(dur[i]));
                }
                //automatically add reified precedences to make branching easier
                for (int j = 0; j < cl.length; j++) {
                    for (int k = j + 1; k < cl.length; k++) {
                        if (!precedenceAlreadyAdded[idxs[j]].get(idxs[k])) {
                            final IntegerVariable b = makeIntVar(String.format("%d", (dur[j] + dur[k])), 0, 1);
                            add(Choco.precedenceDisjoint(cl[j], dur[j], cl[k], dur[k], b));
                            precedenceAlreadyAdded[idxs[j]].set(idxs[k]);
                            precedenceAlreadyAdded[idxs[k]].set(idxs[j]);
                        }
                    }
                }
                add(Choco.disjunctive(tasks));
                // new variables required indexes
                DetectorFactory.associateIndexes(currentHook, tasks);
                currentHook +=  tasks.length;
                //delete the disjunctions
                it.remove();
            }
        }
    }

    /**
         * Build the constraint graph of disjonctions and compute
         * the duration of each task
         * @param ppcs PreProcessCPSolver
         * @return int[]
         */
        public int[] addAllDisjunctiveEdges(PreProcessCPSolver ppcs) {
            Iterator<Constraint> it = model.getConstraintIterator();
            int[] durations = new int[model.getNbIntVars()];
            Arrays.fill(durations,-1);
            boolean b = false;
            while (it.hasNext()) {
                Constraint ic = it.next();
                if (ic instanceof MetaConstraint) {
                    ExpressionSConstraint es = new ExpressionSConstraint(ppcs.getMod2Sol().buildNode(ic));
                    ExpressionTools.SimplePrecedence sp = ExpressionTools.getPrecedenceConstraint(es);
                    if (sp != null) {
                        b = true;
                        addEdge(sp.v1, sp.v2, ic);
                        int idx1 = sp.v1.getHook();
                        int idx2 = sp.v2.getHook();
                        if (durations[idx1] == -1)
                            durations[idx1] = sp.d1;
                        else if (durations[idx1] != sp.d1) {
                            return null; // the same task do not have always the same duration so give up
                        }
                        if (durations[idx2] == -1)
                            durations[idx2] = sp.d2;
                        else if (durations[idx2] != sp.d2) {
                            return null; // the same task do not have always the same duration so give up
                        }
                    }
                }
            }
            if (b) return durations;
            else{
                return null;
            }
        }
}
