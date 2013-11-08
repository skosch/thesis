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

package parser.instances;

import static parser.instances.BasicSettings.BRANCHING;
import static parser.instances.BasicSettings.MIN_VALUE;
import static parser.instances.BasicSettings.RANDOM_VALUE;
import choco.cp.solver.preprocessor.PreProcessConfiguration;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.Configuration.Default;
import choco.kernel.solver.search.limit.Limit;

import java.io.File;
import java.io.IOException;

// TODO - Rename with Configuration suffix - created 12 août 2011 by Arnaud Malapert
public class BasicSettings extends PreProcessConfiguration{

	private static final long serialVersionUID = 7557235241412627008L;

	/**
	 * <br/><b>Goal</b>: time limit of a preprocessing step.
	 * <br/><b>Type</b>: int
	 * <br/><b>Default value</b>: 15
	 */
	@Default(value = "15")
	public static final String PREPROCESSING_TIME_LIMIT = "tools.preprocessing.limit.time.value";

	/**
	 * <br/><b>Goal</b>: indicates if the heuristics is applied during preprocessing.
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: true
	 */
	@Default(value = VALUE_TRUE)
	public static final String PREPROCESSING_HEURISTICS = "tools.preprocessing.heuristics";
	
	/**
	 * <br/><b>Goal</b>: indicates that the constraint model use light propagation algorithms (for example, it decomposes some global constraints).
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: false
	 */
	@Default(value = VALUE_FALSE)
	public static final String LIGHT_MODEL = "tools.cp.model.light";

	/**
	 * 
	 * <br/><b>Goal</b>: indicates the branching strategy.
	 *  
	 * <br/><b>Type</b>: String
	 * <br/><b>Default value</b>: LEX
	 */
	@Default(value = "LEX")
	public static final String BRANCHING = "tools.branching.variable";
	//TODO Change branching keys

	
	/**
	 * <br/><b>Goal</b>: indicates if selection is random in value-selection heuristics.
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: false
	 */
	@Default(value = VALUE_FALSE)
	public static final String RANDOM_VALUE = "tools.random.value";

	/**
	 * <br/><b>Goal</b>: indicates if value-selection is either min-val or max-val
	 * Note that {@link BasicSettings#RANDOM_VALUE} cancels this property.
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: true
	 */
	@Default(value = VALUE_TRUE)
	public static final String MIN_VALUE = "tools.branching.value.min";

	/**
	 * <br/><b>Goal</b>: indicates if the ties are broken randomly in variable-selection or value-selection heuristics.
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: false
	 */
	@Default(value = VALUE_FALSE)
	public static final String RANDOM_TIE_BREAKING = "tools.random.break_tie";

	/**
	 * <br/><b>Goal</b>: indicates that the constraint programming step is cancelled (only preprocessing step).
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: false
	 */
	@Default(value = VALUE_FALSE)
	public static final String CANCEL_CP_SOLVE= "tools.cp.cancel";


	/**
	 * <br/><b>Goal</b>: indicates that the best solution is reported.
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: false
	 */
	@Default(value = VALUE_FALSE)
	public static final String SOLUTION_REPORT = "tools.solution.report";

	/**
	 * <br/><b>Goal</b>: indicates that the best solution is exported.
	 * <br/><b>Type</b>: boolean
	 * <br/><b>Default value</b>: false
	 */
	@Default(value = VALUE_FALSE)
	public static final String SOLUTION_EXPORT = "tools.solution.export";


	private static final String TMPDIR_PROPPERTY = "java.io.tmpdir";
	/**
	 * <br/><b>Goal</b>: indicates that the best solution is exported.
	 * <br/><b>Type</b>: File
	 * <br/><b>Default value</b>: TMP (codename of property java.io.tmpdir"
	 */
	@Default(value = TMPDIR_PROPPERTY)
	public static final String OUTPUT_DIRECTORY = "tools.output.directory";


	public BasicSettings() {
		super();
	}

	public static File getOutputDirectory(Configuration conf) {
		final String path = conf.readString(OUTPUT_DIRECTORY);
		return new File( path.equals(TMPDIR_PROPPERTY) ?
				System.getProperty(TMPDIR_PROPPERTY) : path 
		);
	}
	public static void updateTimeLimit(Configuration conf, long delta) {
		final Limit lim = conf.readEnum(Configuration.SEARCH_LIMIT, Limit.class);
		if( Limit.TIME.equals(lim)) {
			final int limVal = (int) (conf.readInt(SEARCH_LIMIT_BOUND) + delta);
			if(limVal > 0) conf.putInt(SEARCH_LIMIT_BOUND, limVal);
			else conf.putInt(SEARCH_LIMIT_BOUND, 0);
		}
	}
	
	public static String getBranchingMsg(Configuration conf) {
		//FIXME only the branching factory knows what branching is really applied !
		StringBuilder b = new StringBuilder();
		b.append(conf.readString(BRANCHING)).append(" BRANCHING    ");
		if( conf.readBoolean(RANDOM_VALUE) ) b.append("RAND_VAL    ");
		else if( conf.readBoolean(MIN_VALUE) ) b.append("MIN_VAL");
		else b.append("MAX_VALUE");
		return b.toString();
	}

	public static String getInstModelMsg(Configuration conf) {
		final StringBuilder b = new StringBuilder(32);
		if( conf.readBoolean(LIGHT_MODEL)) b.append("LIGHT_MODEL    ");
		if( conf.readBoolean(RANDOM_VALUE)) b.append("RAND_VAL    ");
		if( conf.readBoolean(RANDOM_TIE_BREAKING)) b.append("RAND_TIE_BREAKING");
		return new String(b);
	}
	

	public static void main(String[] args) throws IOException {
		BasicSettings settings = new BasicSettings();
		File f = File.createTempFile("settings-",".properties");
		System.out.println("Generate "+f);
		settings.storeDefault(f, "");
	}

	
}