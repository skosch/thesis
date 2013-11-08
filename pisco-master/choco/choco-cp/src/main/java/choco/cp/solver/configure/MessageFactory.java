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

package choco.cp.solver.configure;

import choco.kernel.solver.Configuration;
import static choco.kernel.solver.Configuration.*;
import choco.kernel.solver.ResolutionPolicy;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.limit.Limit;

public final class MessageFactory {

	private MessageFactory() {
		super();
	}
	

	public static String getGeneralMsg(Configuration conf, String problemName, String instanceName) {
		final StringBuilder b = new StringBuilder(128);
		ResolutionPolicy policy = conf.readEnum(RESOLUTION_POLICY, ResolutionPolicy.class);
		switch (policy) {
		case MAXIMIZE:
			b.append("MAXIMIZE    ");
			break;
		case MINIMIZE:
			b.append("MINIMIZE    ");
			break;
		case SATISFACTION:
		default:
			b.append("CSP    ");
			break;
		}
		if( conf.readBoolean(STOP_AT_FIRST_SOLUTION) ) b.append("FIRST_SOLUTION    ");
		else if(policy == ResolutionPolicy.SATISFACTION) b.append("ALL_SOLUTIONS    ");
		b.append(problemName).append("    ");
		b.append(instanceName).append("    ");
		b.append(conf.readString(RANDOM_SEED)).append(" SEED");
		return b.toString();
	}

	private static String getLimitMsg(Configuration conf, String name, String key, String boundKey) {
		final Limit lim = conf.readEnum(key, Limit.class);
		if( ! lim.equals(Limit.UNDEF) ) {
			return name+ ' ' + conf.readString(boundKey)+ ' ' +lim.getUnit()+"    ";
		} else return "";
	}

	public static String getLimitMsg(Solver solver) {
		final Configuration conf = solver.getConfiguration();
		return getLimitMsg(conf, "SEARCH_LIMIT", SEARCH_LIMIT, SEARCH_LIMIT_BOUND) + 
				getLimitMsg(conf, "RESTART_LIMIT", SEARCH_LIMIT, SEARCH_LIMIT_BOUND);
	}

	private static String getPolicyMsg(Configuration conf, String name, String growKey) {
		return name+"( "+conf.readString(RESTART_BASE)+", "+conf.readString(growKey)+" )    ";
	}

	public static String getRestartMsg(Solver solver) {
		final StringBuilder b = new StringBuilder(128);
		final Configuration conf = solver.getConfiguration();
		if( conf.readBoolean(RESTART_LUBY)) {
			b.append(getPolicyMsg(conf, "LUBY", RESTART_LUBY_GROW));
		} else if(conf.readBoolean(RESTART_GEOMETRICAL) ) {
			b.append(getPolicyMsg(conf, "GEOM", RESTART_GEOM_GROW));
		}
		if(conf.readBoolean( RESTART_AFTER_SOLUTION)) b.append("FROM_SOLUTION    ");
		if( conf.readBoolean(NOGOOD_RECORDING_FROM_RESTART) ) b.append("NOGOOD_RECORDING");
		return b.toString();
	}

	public static String getShavingMsg(Solver solver) {
		final StringBuilder b = new StringBuilder(128);
		final Configuration conf = solver.getConfiguration();
		if(conf.readBoolean(BOTTOM_UP)) {b.append("BOTTOM_UP    ");}
		if(conf.readBoolean(INIT_SHAVING)) {b.append("SHAVING    ");}
		if(conf.readBoolean(INIT_SHAVING)) {
			b.append("DESTRUCTIVE_LOWER_BOUND");
			if(conf.readBoolean(INIT_DLB_SHAVING)) {b.append("_WITH_SHAVING");}
		}
		return b.toString();
	}

	public static String getModellingMsg(Solver solver) {
		return solver.getNbVars()+" VARIABLES\t"+solver.getNbConstraints()+" CONSTRAINTS";
	}

}
