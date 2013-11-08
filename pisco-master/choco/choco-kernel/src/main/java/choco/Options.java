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

package choco;

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.ModelException;
import gnu.trove.TObjectIntHashMap;

import java.util.logging.Logger;


/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 25 mars 2010<br/>
 * Since : Choco 2.1.1<br/>
 * <p/>
 * A class to declare options concerning variables and constraints.
 * Available for module choco-cp only.
 */
public class Options {

	private final static Logger LOGGER  = ChocoLogging.getMainLogger();

	public static final String NO_OPTION = "";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////// VARIABLE //////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * <br/><b>Goal</b> : force Solver to create bounded domain variable.
	 * <br/><b>Scope</b>: {@link choco.kernel.model.variables.integer.IntegerVariable},
	 * {@link choco.kernel.model.variables.set.SetVariable}'s cardinality variable.
	 */
	public static final String V_BOUND = "cp:bound";

	/**
	 * <br/><b>Goal</b> : force Solver to create enumerated domain variable (default options if options is empty).
	 * <br/><b>Scope</b>: {@link choco.kernel.model.variables.integer.IntegerVariable},
	 * {@link choco.kernel.model.variables.set.SetVariable}'s cardinality variable
	 * (default option)
	 */
	public static final String V_ENUM = "cp:enum";

	/**
	 * <br/><b>Goal</b> : force Solver to create binary tree domain variable.
	 * <br/><b>Scope</b>: {@link choco.kernel.model.variables.integer.IntegerVariable}.
	 */
	public static final String V_BTREE = "cp:btree";

	/**
	 * <br/><b>Goal</b> : force Solver to create bipartite list domain variable.
	 * <br/><b>Scope</b>: {@link choco.kernel.model.variables.integer.IntegerVariable}.
	 */
	public static final String V_BLIST = "cp:blist";


	/**
	 * <br/><b>Goal</b> : force Solver to create linked list domain variable.
	 * <br/><b>Scope</b>: {@link choco.kernel.model.variables.integer.IntegerVariable}.
	 */
	public static final String V_LINK = "cp:link";

	/**
	 * <br/><b>Goal</b> : declare the current variable as makespan.
	 * <br/><b>Scope</b>: {@link choco.kernel.model.variables.integer.IntegerVariable}.
	 */
	public static final String V_MAKESPAN = "cp:makespan";

	/**
	 * <br/><b>Goal</b> : declare variable as a decisional one.
	 * <br/><b>Scope</b>: {@link choco.kernel.model.variables.integer.IntegerVariable},
	 * {@link choco.kernel.model.variables.set.SetVariable}
	 * and {@link choco.kernel.model.variables.real.RealVariable}.
	 * @deprecated This option has no longer effect
	 *             as by default every variables are put in the decision variable pool.
	 */
	public static final String V_DECISION = "cp:decision";

	/**
	 * <br/><b>Goal</b> : force variable to be removed from the pool of decisionnal variables.
	 * <br/><b>Scope</b>: {@link choco.kernel.model.variables.integer.IntegerVariable},
	 * {@link choco.kernel.model.variables.set.SetVariable}
	 * and {@link choco.kernel.model.variables.real.RealVariable}.
	 */
	public static final String V_NO_DECISION = "cp:no_decision";

	/**
	 * <br/><b>Goal</b> : declare objective variable.
	 * <br/><b>Scope</b>: {@link choco.kernel.model.variables.integer.IntegerVariable},
	 * {@link choco.kernel.model.variables.set.SetVariable}
	 * and {@link choco.kernel.model.variables.real.RealVariable}.
	 */
	public static final String V_OBJECTIVE = "cp:objective";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////// EXPRESSION ////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * <br/><b>Goal</b> : force decomposition of the <b>scoped</b> expression.
	 * <br/><b>Scope</b> : {@link choco.kernel.model.variables.integer.IntegerExpressionVariable}.
	 */
	public static final String E_DECOMP = "cp:decomp";

	/**
	 * <br/><b>Goal</b> : to get AC algorithm
	 * <br/><b>Scope</b> : {@link choco.kernel.model.variables.integer.IntegerExpressionVariable}.
	 */
	public static final String E_AC= "cp:ac";
	
	/**
	 * <br/><b>Goal</b> :  set filter policy to forward checking.
	 * <br/><b>Scope</b> : {@link choco.kernel.model.variables.integer.IntegerExpressionVariable}.
	 */
	public static final String E_FC= "cp:fc";
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////// CONSTRAINT ////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * <br/><b>Goal</b> : to get AC3 algorithm (searching from scratch for supports on all values).
	 * <br/><b>Scope</b> :
	 * {@link choco.Choco#feasPairAC(String, choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.model.variables.integer.IntegerVariable, java.util.List)},
	 * {@link choco.Choco#infeasPairAC(String, choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.model.variables.integer.IntegerVariable, java.util.List)},
	 * {@link choco.Choco#infeasPairAC(String, choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.model.variables.integer.IntegerVariable, boolean[][])},
	 * {@link choco.Choco#relationPairAC(String, choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.solver.constraints.integer.extension.BinRelation)},
	 */
	public static final String C_EXT_AC3 = "cp:ac3";

	/**
	 * <br/><b>Goal</b> : to get AC3rm algorithm (maintaining the current support of each value in a non backtrackable way).
	 * <br/><b>Scope</b> :
	 * {@link choco.Choco#feasPairAC(String, choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.model.variables.integer.IntegerVariable, java.util.List)},
	 * {@link choco.Choco#feasTupleAC(String, java.util.List, choco.kernel.model.variables.integer.IntegerVariable[])},
	 * {@link choco.Choco#infeasPairAC(String, choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.model.variables.integer.IntegerVariable, java.util.List)},
	 * {@link choco.Choco#infeasPairAC(String, choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.model.variables.integer.IntegerVariable, boolean[][])},
	 * {@link choco.Choco#relationPairAC(String, choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.solver.constraints.integer.extension.BinRelation)},
	 * {@link choco.Choco#relationTupleAC(String, choco.kernel.model.variables.integer.IntegerVariable[],
	 * choco.kernel.solver.constraints.integer.extension.LargeRelation)}
	 */
	public static final String C_EXT_AC32 = "cp:ac32";

	/**
	 * <br/><b>Goal</b> : to get AC3 with the used of {@link java.util.BitSet} to know if a support still exists.
	 * <br/><b>Scope</b> :
	 * {@link choco.Choco#feasPairAC(String, choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.model.variables.integer.IntegerVariable, java.util.List)},
	 * {@link choco.Choco#infeasPairAC(String, choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.model.variables.integer.IntegerVariable, java.util.List)},
	 * {@link choco.Choco#infeasPairAC(String, choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.model.variables.integer.IntegerVariable, boolean[][])},
	 * {@link choco.Choco#relationPairAC(String, choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.solver.constraints.integer.extension.BinRelation)},
	 */
	public static final String C_EXT_AC322 = "cp:ac322";

	/**
	 * <br/><b>Goal</b> : to get AC2001 algorithm (maintaining the current support of each value).
	 * <br/><b>Scope</b> :
	 * {@link choco.Choco#feasPairAC(String, choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.model.variables.integer.IntegerVariable, java.util.List)},
	 * {@link choco.Choco#feasTupleAC(String, java.util.List, choco.kernel.model.variables.integer.IntegerVariable[])},
	 * {@link choco.Choco#infeasPairAC(String, choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.model.variables.integer.IntegerVariable, java.util.List)},
	 * {@link choco.Choco#infeasPairAC(String, choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.model.variables.integer.IntegerVariable, boolean[][])},
	 * {@link choco.Choco#relationPairAC(String, choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.solver.constraints.integer.extension.BinRelation)},
	 * {@link choco.Choco#relationTupleAC(String, choco.kernel.model.variables.integer.IntegerVariable[],
	 * choco.kernel.solver.constraints.integer.extension.LargeRelation)}
	 */
	public static final String C_EXT_AC2001 = "cp:ac2001";

	/**
	 * <br/><b>Goal</b> : to get AC2008 algorithm (maintained by STR).
	 * <br/><b>Scope</b> :
	 * {@link choco.Choco#feasTupleAC(String, java.util.List, choco.kernel.model.variables.integer.IntegerVariable[])},
	 * {@link choco.Choco#infeasTupleAC(String, java.util.List, choco.kernel.model.variables.integer.IntegerVariable[])},
	 * {@link choco.Choco#relationTupleAC(String, choco.kernel.model.variables.integer.IntegerVariable[],
	 * choco.kernel.solver.constraints.integer.extension.LargeRelation)}
	 */
	public static final String C_EXT_AC2008 = "cp:ac2008";

	/**
	 * <br/><b>Goal</b> : set filter policy to forward checking.
	 * <br/><b>Scope</b> :
	 * {@link choco.Choco#feasTupleAC(String, java.util.List, choco.kernel.model.variables.integer.IntegerVariable[])},
	 * {@link choco.Choco#infeasTupleAC(String, java.util.List, choco.kernel.model.variables.integer.IntegerVariable[])},
	 * {@link choco.Choco#relationTupleAC(String, choco.kernel.model.variables.integer.IntegerVariable[],
	 * choco.kernel.solver.constraints.integer.extension.LargeRelation)}.
	 */
	public static final String C_EXT_FC = "cp:fc";

	/**
	 * <br/><b>Goal</b> : for Regin implementation.
	 * <br/><b>Scope</b>: {@link choco.Choco#allDifferent(String, choco.kernel.model.variables.integer.IntegerVariable[])} .
	 */
	public static final String C_ALLDIFFERENT_AC = "cp:ac";

	/**
	 * <br/><b>Goal</b> : for bound all different using the propagator of
	 * A. Lopez-Ortiz, C.-G. Quimper, J. Tromp, and P. van Beek.
	 * A fast and simple algorithm for bounds consistency of the alldifferent
	 * constraint. IJCAI-2003.
	 * <br/><b>Scope</b>: {@link choco.Choco#allDifferent(String, choco.kernel.model.variables.integer.IntegerVariable[])} .
	 */
	public static final String C_ALLDIFFERENT_BC = "cp:bc";

	/**
	 * <br/><b>Goal</b> : propagate on the clique of differences.
	 * <br/><b>Scope</b>: {@link choco.Choco#allDifferent(String, choco.kernel.model.variables.integer.IntegerVariable[])} .
	 */
	public static final String C_ALLDIFFERENT_CLIQUE = "cp:clique";


	/**
	 * <br/><b>Goal</b> : for Regin implementation.
	 * <br/><b>Scope</b>: {@link choco.Choco#globalCardinality(String, choco.kernel.model.variables.integer.IntegerVariable[],
	 * int[], int[], int)} .
	 */
	public static final String C_GCC_AC = "cp:ac";

	/**
	 * <br/><b>Goal</b> : for Quimper implementation.
	 * <br/><b>Scope</b>: {@link choco.Choco#globalCardinality(String, choco.kernel.model.variables.integer.IntegerVariable[],
	 * int[], int[], int)} .
	 */
	public static final String C_GCC_BC = "cp:bc";

	/**
	 * <br/><b>Goal</b> : set filtering policy to filter on lower bound only.
	 * <br/><b>Scope</b>: {@link choco.Choco#increasing_nvalue(String, choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.model.variables.integer.IntegerVariable[])} .
	 */
	public static final String C_INCREASING_NVALUE_ATLEAST = "cp:atleast";

	/**
	 * <br/><b>Goal</b> : set filtering policy to filter on upper bound only.
	 * <br/><b>Scope</b>: {@link choco.Choco#increasing_nvalue(String, choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.model.variables.integer.IntegerVariable[])} .
	 */
	public static final String C_INCREASING_NVALUE_ATMOST = "cp:atmost";

	/**
	 * <br/><b>Goal</b> : set filtering policy to filter on lower and upper bound only.
	 * <br/><b>Scope</b>: {@link choco.Choco#increasing_nvalue(String, choco.kernel.model.variables.integer.IntegerVariable,
	 * choco.kernel.model.variables.integer.IntegerVariable[])} .
	 */
	public static final String C_INCREASING_NVALUE_BOTH = "cp:both";


	/**
	 * <br/><b>Goal</b> : global consistency.
	 * <br/><b>Scope</b> : {@link choco.Choco#nth(String, choco.kernel.model.variables.integer.IntegerVariable, int[],
	 * choco.kernel.model.variables.integer.IntegerVariable)},
	 * {@link choco.Choco#nth(String, choco.kernel.model.variables.integer.IntegerVariable, int[],
	 * choco.kernel.model.variables.integer.IntegerVariable, int)}
	 */
	public static final String C_NTH_G = "cp:G";


	/**
	 * <br/><b>Goal</b>: Ensure quick entailment tests.
	 * <br/><b>Scope</b> : {@link choco.Choco#clause(choco.kernel.model.variables.integer.IntegerVariable[],
	 * choco.kernel.model.variables.integer.IntegerVariable[])}
	 */
	public static final String C_CLAUSES_ENTAIL = "cp:entail";

	/**
	 * <br/><b>Goal</b>: postponed a constraint.
	 * <br/><b>Scope</b> : {@link choco.kernel.model.constraints.Constraint}.
	 */
	public static final String C_POST_PONED = "cp:postponed";


	/**
	 * <br/><b>Goal</b> : set filtering policy to apply Overload Checking ( O(n*log(n)), Vilim), also known as task interval.
	 * <br/><b>Scope</b> : 
	 * {@link choco.Choco#disjunctive(choco.kernel.model.variables.scheduling.TaskVariable[], String...)}, </br>
	 * {@link choco.Choco#disjunctive(String, choco.kernel.model.variables.scheduling.TaskVariable[], String...)},</br>
	 * {@link choco.Choco#disjunctive(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], String...)},</br>
	 * {@link choco.Choco#disjunctive(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], String...)},</br>
	 * {@link choco.Choco#disjunctive(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)}.
	 */
	public static final String C_DISJ_OC = "cp:disjunctive:overload_checking";

	/**
	 * <br/><b>Goal</b> : set filtering policy to apply NotFirst/NotLast ( O(n*log(n)), Vilim).
	 * <br/><b>Scope</b> : 
	 * {@link choco.Choco#disjunctive(choco.kernel.model.variables.scheduling.TaskVariable[], String...)}, </br>
	 * {@link choco.Choco#disjunctive(String, choco.kernel.model.variables.scheduling.TaskVariable[], String...)},</br>
	 * {@link choco.Choco#disjunctive(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], String...)},</br>
	 * {@link choco.Choco#disjunctive(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], String...)},</br>
	 * {@link choco.Choco#disjunctive(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)}.
	 * */
	public static final String C_DISJ_NFNL = "cp:disjunctive:not_first_not_last";

	/**
	 * <br/><b>Goal</b> : set filtering policy to apply Detectable Precedence ( O(n*log(n)), Vilim).
	 * <br/><b>Scope</b> : 
	 * {@link choco.Choco#disjunctive(choco.kernel.model.variables.scheduling.TaskVariable[], String...)}, </br>
	 * {@link choco.Choco#disjunctive(String, choco.kernel.model.variables.scheduling.TaskVariable[], String...)},</br>
	 * {@link choco.Choco#disjunctive(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], String...)},</br>
	 * {@link choco.Choco#disjunctive(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], String...)},</br>
	 * {@link choco.Choco#disjunctive(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)}.
	 * */
	public static final String C_DISJ_DP = "cp:disjunctive:detectable_precedence";

	/**
	 * <br/><b>Goal</b> : set filtering policy to apply Edge Finding ( O(n*log(n)), Vilim).
	 * <br/><b>Scope</b> : 
	 * {@link choco.Choco#disjunctive(choco.kernel.model.variables.scheduling.TaskVariable[], String...)}, </br>
	 * {@link choco.Choco#disjunctive(String, choco.kernel.model.variables.scheduling.TaskVariable[], String...)},</br>
	 * {@link choco.Choco#disjunctive(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], String...)},</br>
	 * {@link choco.Choco#disjunctive(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], String...)},</br>
	 * {@link choco.Choco#disjunctive(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)}.
	 * */
	public static final String C_DISJ_EF = "cp:disjunctive:edge_finding";



	/**
	 * <br/><b>Goal</b> : set filtering policy to apply Vilim Filtering Algorithm : <br/>
	 * The filtering algorithm executes an internal loop until it reaches a global fixpoint.
	 * The default internal loop applies each rule once whereas the Vilim's one applies each rule until it reaches its local fixpoint.
	 * <br/><b>Scope</b> : 
	 * {@link choco.Choco#disjunctive(choco.kernel.model.variables.scheduling.TaskVariable[], String...)}, </br>
	 * {@link choco.Choco#disjunctive(String, choco.kernel.model.variables.scheduling.TaskVariable[], String...)},</br>
	 * {@link choco.Choco#disjunctive(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], String...)},</br>
	 * {@link choco.Choco#disjunctive(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], String...)},</br>
	 * {@link choco.Choco#disjunctive(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)}.
	 */
	public static final String C_DISJ_VF = "cp:disjunctive:vilim_filtering";

	/**
	 * <br/><b>Goal</b> : set filtering policy to apply task intervals (O(n*log(n)) with Vilim Tree).
	 * <br/><b>Scope</b> : 
	 * {@link choco.Choco#cumulative(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulative(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulative(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, choco.kernel.model.variables.integer.IntegerVariable, choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulativeMax(choco.kernel.model.variables.scheduling.TaskVariable[], int[], int, String...)},</br>
	 * {@link choco.Choco#cumulativeMax(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulativeMax(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulativeMin(choco.kernel.model.variables.scheduling.TaskVariable[], int[], int, String...)},</br>
	 * {@link choco.Choco#cumulativeMin(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulativeMin(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 */
	public static final String C_CUMUL_TI = "cp:cumulative:task_intervals";


	/**
	 * <br/><b>Goal</b> : set filtering policy to apply task intervals (O(n*n) but stronger deductions).
	 * <br/><b>Scope</b> : 
	 * {@link choco.Choco#cumulative(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulative(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulative(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, choco.kernel.model.variables.integer.IntegerVariable, choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulativeMax(choco.kernel.model.variables.scheduling.TaskVariable[], int[], int, String...)},</br>
	 * {@link choco.Choco#cumulativeMax(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulativeMax(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulativeMin(choco.kernel.model.variables.scheduling.TaskVariable[], int[], int, String...)},</br>
	 * {@link choco.Choco#cumulativeMin(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulativeMin(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 */
	public static final String C_CUMUL_STI = "cp:cumulative:slow_task_intervals";

	/**
	 * <br/><b>Goal</b> : set filtering policy to apply Edge Finding:
	 * Simple n^2 \times k algorithm (lazy for R) (CalcEF in the paper of Van Hentenrick)
	 * <br/><b>Scope</b> : 
	 * {@link choco.Choco#cumulative(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulative(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulative(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, choco.kernel.model.variables.integer.IntegerVariable, choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulativeMax(choco.kernel.model.variables.scheduling.TaskVariable[], int[], int, String...)},</br>
	 * {@link choco.Choco#cumulativeMax(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulativeMax(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulativeMin(choco.kernel.model.variables.scheduling.TaskVariable[], int[], int, String...)},</br>
	 * {@link choco.Choco#cumulativeMin(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulativeMin(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 */
	public static final String C_CUMUL_EF = "cp:cumulative:edge_finding";

	/**
	 * <br/><b>Goal</b> : set filtering policy to apply another Edge Finding algorithm (not yet implemented):
	 *  Vilim theta lambda tree + lazy computation of the inner maximization of the edge finding rule of Van hentenrick and Mercier
	 * <br/><b>Scope</b> : 
	 * {@link choco.Choco#cumulative(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulative(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulative(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, choco.kernel.model.variables.integer.IntegerVariable, choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulativeMax(choco.kernel.model.variables.scheduling.TaskVariable[], int[], int, String...)},</br>
	 * {@link choco.Choco#cumulativeMax(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulativeMax(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulativeMin(choco.kernel.model.variables.scheduling.TaskVariable[], int[], int, String...)},</br>
	 * {@link choco.Choco#cumulativeMin(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 * {@link choco.Choco#cumulativeMin(String, choco.kernel.model.variables.scheduling.TaskVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...)},</br>
	 */
	public static final String C_CUMUL_VEF = "cp:cumulative:edge_finding:vilim";

	/**
	 * <br/><b>Goal</b> : set filtering policy to apply additional rules based on the algorithm "NoSum" (Shaw-2004)
	 * <br/><b>Scope</b> :
	 * {@link choco.Choco#pack(choco.kernel.model.constraints.pack.PackModel, String...)},</br>
	 * {@link choco.Choco#pack(choco.kernel.model.variables.set.SetVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerConstantVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...) 
	 */
	public static final String C_PACK_AR = "cp:pack:additional_rules";

	/**
	 * <br/><b>Goal</b> : set filtering policy to apply a feasibility tests based on dynamic lower bounds on the number of non empty bins.
	 * <br/><b>Scope</b> :
	 * {@link choco.Choco#pack(choco.kernel.model.constraints.pack.PackModel, String...)},</br>
	 * {@link choco.Choco#pack(choco.kernel.model.variables.set.SetVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerConstantVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...) 
	 */
	public static final String C_PACK_DLB = "cp:pack:dynamic_lower_bound";

	/**
	 * <br/><b>Goal</b> : set filtering policy to apply a dominance rule which pack an item which matches exactly the remaining space into a bin.
	 * <br/><b>Scope</b> :
	 * {@link choco.Choco#pack(choco.kernel.model.constraints.pack.PackModel, String...)},</br>
	 * {@link choco.Choco#pack(choco.kernel.model.variables.set.SetVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerConstantVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...) 
	 */
	public static final String C_PACK_FB = "cp:pack:fill_bins";

	/**
	 * <br/><b>Goal</b> : set filtering policy to apply a symmetry breaking rule which imposes that the last bins are empty.
	 * <br/><b>Scope</b> :
	 * {@link choco.Choco#pack(choco.kernel.model.constraints.pack.PackModel, String...)},</br>
	 * {@link choco.Choco#pack(choco.kernel.model.variables.set.SetVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerConstantVariable[], choco.kernel.model.variables.integer.IntegerVariable, String...) 
	 */
	public static final String C_PACK_LBE = "cp:pack:last_bins_empty";

	/**
	 * <br/><b>Goal</b> : set a policy which instantiates the minimum/maximum variable to its minimum if the set is empty.
	 * <br/><b>Scope</b> :
	 * {@link Choco#min(choco.kernel.model.variables.set.SetVariable, choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable) }, </br>
	 * {@link Choco#max(choco.kernel.model.variables.set.SetVariable, choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable)}. 
	 */
	public static final String C_MINMAX_INF = "cp:min-max:inf";
	
	/**
	 * <br/><b>Goal</b> : set a policy which instantiates the minimum/maximum variable to its maximum if the set is empty.
	 * <br/><b>Scope</b> :
	 * {@link Choco#min(choco.kernel.model.variables.set.SetVariable, choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable) }, </br>
	 * {@link Choco#max(choco.kernel.model.variables.set.SetVariable, choco.kernel.model.variables.integer.IntegerVariable[], choco.kernel.model.variables.integer.IntegerVariable)}. 
	 */
	public static final String C_MINMAX_SUP = "cp:min-max:sup";
	
	
	//////////////////////// ////////////////////////

	/**
	 * preprocessing ignores the given constraint for detection.
	 */
	public final static String C_NO_DETECTION = "ppcp:no_detection";


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/* Options are sorted by categories. 2 options of the same categories are exclusive */
	protected static TObjectIntHashMap<String> categories = new TObjectIntHashMap<String>(16);

	static {
		categories.put(NO_OPTION, 0);
		// VARIABLES
		categories.put(V_BLIST, 0);
		categories.put(V_BOUND, 0);
		categories.put(V_BTREE, 0);
		categories.put(V_ENUM, 0);
		categories.put(V_LINK, 0);

		categories.put(V_OBJECTIVE, 1);

		categories.put(V_NO_DECISION, 2);

		categories.put(V_MAKESPAN, 3);

		// EXPRESSIONS
		categories.put(E_DECOMP, 0);

		// CONSTRAINTS
		categories.put(C_EXT_AC2001, 0);
		categories.put(C_EXT_AC2008, 0);
		categories.put(C_EXT_AC3, 0);
		categories.put(C_EXT_AC32, 0);
		categories.put(C_EXT_AC322, 0);
		categories.put(C_EXT_FC, 0);

		categories.put(C_ALLDIFFERENT_AC, 0);
		categories.put(C_ALLDIFFERENT_BC, 0);
		categories.put(C_ALLDIFFERENT_CLIQUE, 0);

		categories.put(C_GCC_AC, 0);
		categories.put(C_GCC_BC, 0);

		categories.put(C_INCREASING_NVALUE_ATLEAST, 0);
		categories.put(C_INCREASING_NVALUE_ATMOST, 0);
		categories.put(C_INCREASING_NVALUE_BOTH, 0);

		categories.put(C_NTH_G, 0);

		categories.put(C_CLAUSES_ENTAIL, 0);

		categories.put(C_POST_PONED, 1);

		categories.put(C_PACK_AR, 0);
		//...
		categories.put(C_PACK_DLB, 3);
		categories.put(C_PACK_FB, 4);
		categories.put(C_PACK_LBE, 5);

		categories.put(C_CUMUL_TI, 0);
		categories.put(C_CUMUL_STI, 0);
		//...
		categories.put(C_CUMUL_EF, 3);
		categories.put(C_CUMUL_VEF, 3);

		categories.put(C_DISJ_OC, 0);
		categories.put(C_DISJ_EF, 0);
		//...
		categories.put(C_DISJ_NFNL, 3);
		categories.put(C_DISJ_DP, 4);
		categories.put(C_DISJ_VF, 5);
		
		categories.put(C_MINMAX_INF, 0);
		categories.put(C_MINMAX_SUP, 0);
		
		categories.put(C_NO_DETECTION, 6); // TODO - set to 0 - created 11 mars 2012 by A. Malapert

	}


	private Options() {
		super();
	}

	/**
	 * Retrieves the categorie of the given option.
	 * If <code>option</code> doesn't exist, return 0 (default categorie).
	 * See
	 *
	 * @param name
	 * @return
	 */
	public static int getCategorie(String name) {
		if(!categories.contains(name)){
			LOGGER.warning("No categorie defines for \""+ name+"\".\n See Options.create(String name, int categorie) for " +
					"more information.");
		}
		return categories.get(name);
	}

	/**
	 * Declares a new option and define its categorie.
	 * Categaorie is mandatory to set a hierachy between options of an object: exclusive options should have the same
	 * categorie.
	 * @param name option name
	 * @param categorie option categorie
	 */
	public static void create(String name, int categorie) {
		if (categories.containsKey(name)) {
			int c = categories.get(name);
			if (categorie != c) {
				throw new ModelException("option " + name + " already exists and categorie is set to " + c);
			}
		} else {
			categories.put(name, categorie);
		}
	}


}
