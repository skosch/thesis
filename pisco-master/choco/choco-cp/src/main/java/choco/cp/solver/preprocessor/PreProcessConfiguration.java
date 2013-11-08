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

import choco.kernel.solver.Configuration;
import choco.kernel.solver.Solver;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 7 mai 2010<br/>
 * Since : Choco 2.1.1<br/>
 *
 * Specific {@link Configuration} extension for {@link PreProcessCPSolver}.
 */
public class PreProcessConfiguration extends Configuration {
		
	private static final long serialVersionUID = 683407604054648550L;

	/**
	 * <br/><b>Goal</b>: Does it perform restart mode?
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: false
	 */
	@Default(value = VALUE_FALSE)
	public static final String RESTART_MODE = "ppcp.restartMode";

	/**
	 * <br/><b>Goal</b>: Active detection equalities between integer variables
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: true
	 */
	@Default(value = VALUE_TRUE)
	public static final String INT_EQUALITY_DETECTION = "ppcp.detection.intEq";

	/**
	 * <br/><b>Goal</b>: Active detection equalities between task variables
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: true
	 */
	@Default(value = VALUE_TRUE)
	public static final String TASK_EQUALITY_DETECTION = "ppcp.detection.taskEq";

	/**
	 * <br/><b>Goal</b>: Active disjunctive detection
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: true
	 */
	@Default(value = VALUE_TRUE)
	public static final String DISJUNCTIVE_DETECTION = "ppcp.detection.disjunctive";

	/**
	 * <br/><b>Goal</b>: Active expression detection
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: true
	 */
	@Default(value = VALUE_TRUE)
	public static final String EXPRESSION_DETECTION = "ppcp.detection.expression";

	/**
	 * <br/><b>Goal</b>: Active cliques detection
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: true
	 */
	@Default(value = VALUE_TRUE)
	public static final String CLIQUES_DETECTION = "ppcp.detection.cliques";

	/**
	 * <br/><b>Goal</b>: Active symetrie breaking detection during cliques detection
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: true
	 */
	@Default(value = VALUE_TRUE)
	public static final String SYMETRIE_BREAKING_DETECTION = "ppcp.detection.cliques.symetrieBreaking";

	/**
	 * <br/><b>Goal</b>: detection of a generalized disjunctive graph within the model from scheduling constraints.
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: true
	 */
	@Default(value = VALUE_TRUE)
	public static final String DISJUNCTIVE_MODEL_DETECTION = "ppcp.detection.scheduling.disjMod";
	
	/**
	 * <br/><b>Goal</b>: infer also precedence form time windows
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: true
	 */
	@Default(value = VALUE_TRUE)
	public static final String DMD_USE_TIME_WINDOWS= "ppcp.detection.scheduling.disjMod.timeWindows";
	
	/**
	 * <br/><b>Goal</b>: Generate ternary clauses to avoid the creation of cycle in the disjunctive graphs. 
	 * Warning : generate at most a cubic number of clauses from the tasks in the disjunctive graph.
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: true
	 */
	@Default(value = VALUE_TRUE)
	public static final String DMD_GENERATE_CLAUSES = "ppcp.detection.scheduling.disjMod.clauses";
	
	
	/**
	 * <br/><b>Goal</b>: Remove global disjunctive constraints after the inferrence of disjunctions.
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: false
	 */
	@Default(value = VALUE_FALSE)
	public static final String DMD_REMOVE_DISJUNCTIVE = "ppcp.detection.scheduling.disjMod.removeDisjunctive";
	
	/**
	 * <br/><b>Goal</b>: state disjunctive global constraints extracted from cumulatives.
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: false
	 */
	@Default(value = VALUE_FALSE)
	public static final String DISJUNCTIVE_FROM_CUMULATIVE_DETECTION= "ppcp.detection.scheduling.cumulative.disjunctive";
	
	public final static String getPreProcessMsg(Configuration conf) {
		final StringBuilder b = new StringBuilder(18);
		if( conf.readBoolean(RESTART_MODE)) b.append(" RESTART    ");
		return new String(b);
	}

	
	public final static void cancelSchedulingPreProcess(Configuration conf) {
		conf.putFalse(DISJUNCTIVE_MODEL_DETECTION);
		conf.putFalse(DMD_USE_TIME_WINDOWS);
		conf.putFalse(DMD_GENERATE_CLAUSES);
		conf.putFalse(DMD_REMOVE_DISJUNCTIVE);
		conf.putFalse(DISJUNCTIVE_FROM_CUMULATIVE_DETECTION);
	}
	
	public final static void cancelNonSchedulingPreProcess(Configuration conf) {
		conf.putFalse(RESTART_MODE);
		conf.putFalse(INT_EQUALITY_DETECTION);
		conf.putFalse(TASK_EQUALITY_DETECTION);
		conf.putFalse(DISJUNCTIVE_DETECTION);
		conf.putFalse(EXPRESSION_DETECTION);
		conf.putFalse(CLIQUES_DETECTION);
		conf.putFalse(SYMETRIE_BREAKING_DETECTION);
	}
	

	public final static void keepSchedulingPreProcess(Solver solver) {
		cancelNonSchedulingPreProcess(solver.getConfiguration());
	}	

	public final static void cancelPreProcess(Configuration conf) {
		cancelNonSchedulingPreProcess(conf);
		cancelSchedulingPreProcess(conf);
	}
	
	public final static void cancelPreProcess(Solver solver) {
		cancelPreProcess(solver.getConfiguration());
	}
	

}
