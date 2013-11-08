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

package choco.cp.solver.preprocessor;

import choco.cp.common.util.preprocessor.AbstractAdvancedDetector;
import choco.cp.common.util.preprocessor.DetectorFactory;
import choco.cp.common.util.preprocessor.detector.AbstractIntegerVariableEqualitiesDetector;
import choco.cp.common.util.preprocessor.detector.AbstractTaskVariableEqualitiesDetector;
import choco.cp.common.util.preprocessor.detector.DisjunctionsSolverDetector;
import choco.cp.common.util.preprocessor.detector.ExpressionSolverDetector;
import choco.cp.model.CPModel;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 2 avr. 2010br/>
 * Since : Choco 2.1.1<br/>
 */
public class SolverDetectorFactory extends DetectorFactory{

    /**
     * Detect equalities between {@link IntegerVariable} within a model
     * @param m model
     * @return new instance of {@link choco.cp.common.util.preprocessor.detector.AbstractIntegerVariableEqualitiesDetector}
     */
    public static AbstractAdvancedDetector intVarEqDet(final CPModel m,
                                                                        final PreProcessCPSolver ppsolver){
        return new AbstractIntegerVariableEqualitiesDetector.IntegerVariableEqualitiesSolverDetector(m, ppsolver);
    }

    /**
     * Detect equalities between {@link TaskVariable} within a model
     * @param m model
     * @param ppsolver instance of {@link PreProcessCPSolver}
     * @return new instance of {@link choco.cp.common.util.preprocessor.detector.AbstractTaskVariableEqualitiesDetector}
     */
    public static AbstractAdvancedDetector taskVarEqDet(final CPModel m, final PreProcessCPSolver ppsolver){
        return new AbstractTaskVariableEqualitiesDetector.TaskVariableEqualitiesSolverDetector(m, ppsolver);
    }

    /**
     * Detect disjunctions.
     * @param m model to analyze
     * @param ppsolver solver scope
     * @return new instance of {@link choco.cp.common.util.preprocessor.detector.DisjunctionsSolverDetector}
     */
    public static AbstractAdvancedDetector disjunctionDetector(final CPModel m, final PreProcessCPSolver ppsolver){
        return new DisjunctionsSolverDetector(m, ppsolver);
    }

    /**
     * Detect expressions.
     * @param m model to analyze
     * @param ppsolver solver scope
     * @return new instance of {@link choco.cp.common.util.preprocessor.detector.ExpressionSolverDetector}
     */
    public static AbstractAdvancedDetector expressionDetector(final CPModel m, final PreProcessCPSolver ppsolver){
        return new ExpressionSolverDetector(m, ppsolver);
    }
}
