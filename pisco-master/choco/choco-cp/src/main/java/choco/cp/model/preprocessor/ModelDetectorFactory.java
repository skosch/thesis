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

package choco.cp.model.preprocessor;

import java.util.Arrays;

import choco.cp.common.util.preprocessor.AbstractDetector;
import choco.cp.common.util.preprocessor.DetectorFactory;
import choco.cp.common.util.preprocessor.detector.AbstractIntegerVariableEqualitiesDetector;
import choco.cp.common.util.preprocessor.detector.AbstractTaskVariableEqualitiesDetector;
import choco.cp.common.util.preprocessor.detector.AnalysisModelDetector;
import choco.cp.common.util.preprocessor.detector.CliquesModelDetector;
import choco.cp.common.util.preprocessor.detector.scheduling.*;
import choco.cp.model.CPModel;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 1 avr. 2010br/>
 * Since : Choco 2.1.1<br/>
 *
 * A factory to apply detectors on a model.
 */
public final class ModelDetectorFactory extends DetectorFactory {



	/**
	 * Run {@link choco.cp.common.util.preprocessor.AbstractAdvancedDetector#apply()} and {@link choco.cp.common.util.preprocessor.AbstractAdvancedDetector#commit()} for each {@code detectors}.
	 * @param detectors list of detectors to run
	 */
	public static void run(final CPModel model, final AbstractDetector... detectors){
		associateIndexes(model);
		for(AbstractDetector detector : detectors){
			detector.applyThenCommit();
		}
		resetIndexes(model);
	}

	/**
	 * Anslyses a model and print messages about general statistics
	 * @return new instance of {@link choco.cp.common.util.preprocessor.detector.AbstractIntegerVariableEqualitiesDetector}
	 */
	public static AbstractDetector analysis(final CPModel m){
		return new AnalysisModelDetector(m);
	}

	/**
	 * Detect equalities between {@link IntegerVariable} within a model
	 * @param m model
	 * @return new instance of {@link choco.cp.common.util.preprocessor.detector.AbstractIntegerVariableEqualitiesDetector}
	 */
	public static AbstractDetector intVarEqDet(final CPModel m){
		return new AbstractIntegerVariableEqualitiesDetector.IntegerVariableEqualitiesModelDetector(m);
	}

	/**
	 * Detect equalities between {@link TaskVariable} within a model
	 * @param m model
	 * @return new instance of {@link choco.cp.common.util.preprocessor.detector.AbstractTaskVariableEqualitiesDetector}
	 */
	public static AbstractDetector taskVarEqDet(final CPModel m){
		return new AbstractTaskVariableEqualitiesDetector.TaskVariableEqualitiesModelDetector(m);
	}

	/**
	 * Detect cliques.
	 * @param m model to analyze
	 * @param breakSymetries
	 * @return new instance of {@link choco.cp.common.util.preprocessor.detector.CliquesModelDetector.CliqueModelDetector}
	 */
	public static AbstractDetector cliqueDetector(final CPModel m, final boolean breakSymetries){
		return new CliquesModelDetector(m, breakSymetries);
	}

	/**
	 * Detect disjunctive from cumulative (redundant constraint).
	 * @param m model to analyze
	 */
	public static AbstractDetector disjFromCumulDetector(final CPModel m){
		return new DisjFromCumulModelDetector(m);
	}

	/**
	 * 
	 * @param m model to analyze
	 */
	public static AbstractDetector precFromImpliedDetector(final CPModel m, DisjunctiveModel disjMod){
		return new PrecFromImpliedModelDetector(m, disjMod);
	}

	/**
	 * 
	 * @param m model to analyze
	 */
	public static AbstractDetector precFromReifiedDetector(final CPModel m, DisjunctiveModel disjMod){
		return new PrecFromReifiedModelDetector(m, disjMod);
	}

	/**
	 * 
	 * @param m model to analyze
	 */
	public static AbstractDetector precFromTimeWindowDetector(final CPModel m, DisjunctiveModel disjMod){
		return new PrecFromTimeWindowModelDetector(m, disjMod);
	}

	/**
	 * 
	 * @param m model to analyze
	 */
	public static AbstractDetector precFromDisjointDetector(final CPModel m, DisjunctiveModel disjMod){
		return new PrecFromDisjointModelDetector(m, disjMod);
	}


	/**
	 * 
	 * @param m model to analyze
	 */
	public static AbstractDetector disjointDetector(final CPModel m, DisjunctiveModel disjMod){
		return new DisjointModelDetector(m, disjMod);
	}

	public static AbstractDetector disjointFromUnaryDetector(final CPModel m, DisjunctiveModel disjMod){
		return new DisjointFromUnaryModelDetector(m, disjMod);
	}

	public static AbstractDetector disjointFromCumulDetector(final CPModel m, DisjunctiveModel disjMod){
		return new DisjointFromCumulModelDetector(m, disjMod);
	}

	public static AbstractDetector clauseFromDisjointDetector(final CPModel m, DisjunctiveModel disjMod) {
		return new ClauseFromDisjointModelDetector(m, disjMod);
	}

	public static AbstractDetector precReductionDetector(final CPModel m, DisjunctiveModel disjMod) {
		return new PrecReductionModelDetector(m, disjMod);
	}

	public static AbstractDetector rmDisjDetector(final CPModel m){
		return new RmUnaryModelDetector(m);
	}


	public static AbstractDetector[] disjunctiveModelDetectors(final CPModel m, final DisjunctiveModel disjMod, boolean generateClauses) {
		return 	generateClauses ?
				new AbstractDetector[] {
				precFromImpliedDetector(m, disjMod),
				precFromReifiedDetector(m, disjMod),
				precFromDisjointDetector(m, disjMod),
				//compute precedence transitive closure 
				disjointDetector(m, disjMod),
				disjointFromUnaryDetector(m, disjMod),
				disjointFromCumulDetector(m, disjMod),
				clauseFromDisjointDetector(m, disjMod),
				//restore precedence graph.
				precReductionDetector(m, disjMod)
		} : 
			new AbstractDetector[] {
					precFromImpliedDetector(m, disjMod),
					precFromReifiedDetector(m, disjMod),
					precFromDisjointDetector(m, disjMod),
					//compute precedence transitive closure 
					disjointDetector(m, disjMod),
					disjointFromUnaryDetector(m, disjMod),
					disjointFromCumulDetector(m, disjMod),
					//restore precedence graph.
					precReductionDetector(m, disjMod)
				};
	}

	public static AbstractDetector[] allSchedulingModelDetectors(final CPModel m, final DisjunctiveModel disjMod) {
		return ArrayUtils.append( new AbstractDetector[] {
				precFromTimeWindowDetector(m, disjMod),
				disjFromCumulDetector(m)},
				disjunctiveModelDetectors(m, disjMod, true),
				new AbstractDetector[] {rmDisjDetector(m)}) ;
	}
}
