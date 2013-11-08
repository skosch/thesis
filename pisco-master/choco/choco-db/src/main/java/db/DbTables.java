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

package db;


public final class DbTables {

	//*****************************************************************//
	//*******************  CONSTANTS  ********************************//
	//***************************************************************//
	/**
	 * Number of bytes per Mo.
	 */
	public static final int Mo = 1048576;

	public static final String ID = "ID";

	public static final String NULL = "NULL";

	public final static String CALL_IDENTITY = "CALL IDENTITY()";

	public final static Object[] EMPTY_MODEL = {0, 0, 0, 0, 0, 0};
	
	//*****************************************************************//
	//*******************  T_SOLVERS  ********************************//
	//***************************************************************//

	public final static DbTableView T_MODELS = new DbTableView(
			"T_MODELS",
			new String[]{
					"ID", "NB_CONSTRAINTS", "NB_BOOLVARS", "NB_INTVARS",
					"NB_SETVARS", "NB_TASKVARS", "NB_REALVARS"
			},
			new String[]{
					null, "nbIntConstraints", "nbBooleanVars", "nbIntVars",
					"nbSetVars", "nbTaskVars", "nbRealVars"
			}
	);

	public final static DbTableView T_CONFIGURATIONS = new DbTableView (
			"T_CONFIGURATIONS",
			new String[] {"ID","SOLVER_ID", "DESCRIPTION"}
	);
	

	public final static DbTableView T_SOLVERS = new DbTableView (
			"T_SOLVERS",
			"ID","INSTANCE_NAME", "STATUS", "RUNTIME", "SOLUTION", "MODEL_ID", "ENVIRONMENT_ID", "SEED", "TIMESTAMP"
	);


	//*****************************************************************//
	//*******************  T_MEASURES, T_LIMITS  *********************//
	//***************************************************************//

	public final static DbTableView T_LIMITS= new DbTableView (
			"T_LIMITS",
			"MEASURE_ID", "SOLVER_ID"
	);

	public final static DbTableView T_MEASURES = new DbTableView (
			"T_MEASURES",
			new String[] {
					"ID","NB_SOLUTIONS","OBJECTIVE",
					"TIME", "NODES","BACKTRACKS","FAILS", "RESTARTS"
			},
			new String[] {
					null, "solutionCount", "objectiveValue", 
					"timeCount", "nodeCount", "backTrackCount", "failCount", "restartCount"
			}
	);


	//*****************************************************************//
	//*******************  T_ENVIRONMENT  ****************************//
	//***************************************************************//

	public final static DbTableView T_RUNTIMES = new DbTableView (
			"T_RUNTIMES",
			"ID", "HOST", "USER", "MAX_MEMORY"
	);


	public final static DbTableView T_OS = new DbTableView (
			"T_OS",
			"ID", "NAME", "VERSION", "ARCH"
	);


	public final static DbTableView T_JVM = new DbTableView (
			"T_JVM",
			"ID", "NAME", "VERSION", "VENDOR"
	);

	public final static DbTableView T_ENVIRONMENTS = new DbTableView (
			"T_ENVIRONMENTS",
			"ID", "RUNTIME_ID", "OS_ID", "JVM_ID"
	);






}
