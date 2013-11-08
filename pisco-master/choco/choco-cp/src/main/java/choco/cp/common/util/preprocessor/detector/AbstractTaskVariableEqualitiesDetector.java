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
import choco.cp.common.util.preprocessor.AbstractDetector;
import choco.cp.common.util.preprocessor.merger.TaskVariableMerger;
import choco.cp.model.CPModel;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.kernel.common.util.objects.BooleanSparseMatrix;
import choco.kernel.common.util.objects.ISparseMatrix;
import choco.kernel.model.variables.MultipleVariables;
import choco.kernel.model.variables.scheduling.TaskVariable;
import choco.kernel.solver.variables.scheduling.TaskVar;
import gnu.trove.TIntObjectHashMap;

import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 1 avr. 2010br/>
 * Since : Choco 2.1.1<br/>
 * <p/>
 * A class detector to detect equalities between TaskVariable within a model.
 */
public abstract class AbstractTaskVariableEqualitiesDetector extends AbstractAdvancedDetector {


    public AbstractTaskVariableEqualitiesDetector(final CPModel model) {
        super(model);
    }

    /**
     * Apply the detection defined within the detector.
     */
    @Override
    public void apply() {
        if (AbstractDetector.LOGGER.isLoggable(Level.CONFIG)) {
            AbstractDetector.LOGGER.config("TaskVariable equalities detection :");
        }
        final ISparseMatrix matrix = analyze();
        if (matrix.getNbElement() > 0) {
            change(matrix);
        }
    }

    /**
     * Analyze the current model and record equality constraints over
     * {@link choco.kernel.model.variables.scheduling.TaskVariable}.
     *
     * @return
     */
    private ISparseMatrix analyze() {
        final int nbStoredMultipleVars = model.getNbStoredMultipleVars();
        final ISparseMatrix matrix = new BooleanSparseMatrix(nbStoredMultipleVars);
        MultipleVariables m1, m2;
        /// Run over equalities constraints, and create edges
        for (int i = 0; i < nbStoredMultipleVars - 1; i++) {
            m1 = model.getStoredMultipleVar(i);
            if (m1 instanceof TaskVariable) {
                for (int j = i + 1; j < nbStoredMultipleVars; j++) {
                    m2 = model.getStoredMultipleVar(j);
                    if (m2 instanceof TaskVariable) {
                        if (m1.isEquivalentTo(m2)) {
                            matrix.add(m1.getHook(), m2.getHook());
                        }
                    }

                }
            }
        }
        return matrix;
    }

    /**
     * Run detection and modification
     *
     * @param matrix matrix of equalities between task variables.
     */
    private void change(final ISparseMatrix matrix) {
        final int nbStoredMultipleVars = model.getNbStoredMultipleVars();

        matrix.prepare();

        final int[] color = new int[nbStoredMultipleVars];
        Arrays.fill(color, -1);
        final TIntObjectHashMap<TaskVariableMerger> domainByColor = new TIntObjectHashMap<TaskVariableMerger>();

        int nbDiffObject = detect(matrix, nbStoredMultipleVars, color, domainByColor);
        apply(nbDiffObject, nbStoredMultipleVars, color, domainByColor);
    }

    /**
     * Detect equalities between Task Variables.
     *
     * @param matrix               matrix of equalities
     * @param nbStoredMultipleVars number of TaskVariables in the model
     * @param color                indices of unique TaskVariables
     * @param domainByColor        domain of unique TaskVariables
     * @return number of unique taskVariables
     */
    private int detect(final ISparseMatrix matrix, final int nbStoredMultipleVars, final int[] color,
                       final TIntObjectHashMap<TaskVariableMerger> domainByColor) {
        int nb = -1;
        final Iterator<Long> it = matrix.iterator();
        TaskVariableMerger dtmp = new TaskVariableMerger();
        while (it.hasNext()) {
            final long v = it.next();
            final int i = (int) (v / nbStoredMultipleVars);
            final int j = (int) (v % nbStoredMultipleVars);

            if (color[i] == -1) {
                nb++;
                color[i] = nb;
                domainByColor.put(nb, new TaskVariableMerger((TaskVariable) model.getStoredMultipleVar(i)));
            }
            final TaskVariableMerger d = domainByColor.get(color[i]);
            //backup
            dtmp.copy(d);
            if(d.intersection((TaskVariable) model.getStoredMultipleVar(j))){
                color[j] = color[i];
                domainByColor.put(color[i], d);
            } else {
                //rollback
                d.copy(dtmp);
                if (color[j] == -1) {
                    nb++;
                    color[j] = nb;
                    domainByColor.put(nb, new TaskVariableMerger((TaskVariable)model.getStoredMultipleVar(j)));
                }
            }
        }
        return nb;
    }

    /**
     * Apply modification detected previously
     *
     * @param k                    number of unique taskVariables
     * @param nbStoredMultipleVars number of TaskVariables in the model
     * @param color                indices of unique TaskVariables
     * @param domainByColor        domain of unique TaskVariables
     */
    protected abstract void apply(final int k, final int nbStoredMultipleVars, final int[] color,
                                  final TIntObjectHashMap<TaskVariableMerger> domainByColor);

    /**
     * An instance of {@link AbstractTaskVariableEqualitiesDetector} that only modify the model.
     */
    public static final class TaskVariableEqualitiesModelDetector extends AbstractTaskVariableEqualitiesDetector {

        public TaskVariableEqualitiesModelDetector(final CPModel model) {
            super(model);
        }

        /**
         * Apply modification detected previously
         *
         * @param k                    number of unique taskVariables
         * @param nbStoredMultipleVars number of TaskVariables in the model
         * @param color                indices of unique TaskVariables
         * @param domainByColor        domain of unique TaskVariables
         */
        @Override
        protected void apply(final int k, final int nbStoredMultipleVars, final int[] color,
                             final TIntObjectHashMap<TaskVariableMerger> domainByColor) {
            final TaskVariable[] var = new TaskVariable[k + 1];
            TaskVariableMerger dtmp;
            TaskVariable vtmp;
            for (int i = 0; i < nbStoredMultipleVars; i++) {
                final int col = color[i];
                if (col != -1) {
                    final TaskVariable v = (TaskVariable) model.getStoredMultipleVar(i);
                    if (var[col] == null) {
                        dtmp = domainByColor.get(col);
                        vtmp = dtmp.create();
                        vtmp.addOptions(vtmp.getOptions());
                        var[col] = vtmp;
                        add(var[col]);
                    }
                    replaceBy(v, var[col]);
                    replaceBy(v.start(), var[col].start());
                    replaceBy(v.duration(), var[col].duration());
                    replaceBy(v.end(), var[col].end());
                    delete(v);
                }
            }
        }
    }

    /**
     * An instance of {@link AbstractTaskVariableEqualitiesDetector} that modify the model AND the solver,
     * but doesn't ensure variables/constraints matching between each other.
     */
    public static final class TaskVariableEqualitiesSolverDetector extends AbstractTaskVariableEqualitiesDetector {

        private final PreProcessCPSolver ppsolver;

        public TaskVariableEqualitiesSolverDetector(final CPModel model, final PreProcessCPSolver solver) {
            super(model);
            ppsolver = solver;
        }

        /**
         * Apply modification detected previously
         *
         * @param k                    number of unique taskVariables
         * @param nbStoredMultipleVars number of TaskVariables in the model
         * @param color                indices of unique TaskVariables
         * @param domainByColor        domain of unique TaskVariables
         */
        @Override
        protected void apply(final int k, final int nbStoredMultipleVars, final int[] color,
                             final TIntObjectHashMap<TaskVariableMerger> domainByColor) {
            final TaskVar[] var = new TaskVar[k + 1];
            TaskVariableMerger dtmp;
            TaskVariable vtmp;
            for (int i = 0; i < nbStoredMultipleVars; i++) {
                final int col = color[i];
                if (col != -1) {
                    final TaskVariable v = (TaskVariable) model.getStoredMultipleVar(i);
                    if (var[col] == null) {
                        dtmp = domainByColor.get(col);
                        vtmp = dtmp.create();
                        vtmp.addOptions(vtmp.getOptions());
                        vtmp.findManager(model.properties);
                        ppsolver.setVar(vtmp.start(), ppsolver.getMod2Sol().readModelVariable(vtmp.start()));
                        ppsolver.setVar(vtmp.duration(), ppsolver.getMod2Sol().readModelVariable(vtmp.duration()));
                        ppsolver.setVar(vtmp.end(), ppsolver.getMod2Sol().readModelVariable(vtmp.end()));
                        var[col] = (TaskVar) ppsolver.getMod2Sol().readModelVariable(vtmp);
                    }
                    ppsolver.setVar(v, var[col]);
                    ppsolver.setVar(v.start(), var[col].start());
                    ppsolver.setVar(v.duration(), var[col].duration());
                    ppsolver.setVar(v.end(), var[col].end());
                    v.resetHook();
                }
            }
        }
    }

}
