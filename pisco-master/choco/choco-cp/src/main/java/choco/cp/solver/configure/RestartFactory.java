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

import static choco.kernel.solver.Configuration.NOGOOD_RECORDING_FROM_RESTART;
import static choco.kernel.solver.Configuration.RESTART_AFTER_SOLUTION;
import static choco.kernel.solver.Configuration.RESTART_BASE;
import static choco.kernel.solver.Configuration.RESTART_GEOMETRICAL;
import static choco.kernel.solver.Configuration.RESTART_GEOM_GROW;
import static choco.kernel.solver.Configuration.RESTART_LUBY;
import static choco.kernel.solver.Configuration.RESTART_LUBY_GROW;
import static choco.kernel.solver.Configuration.RESTART_POLICY_LIMIT;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.search.restart.GeometricalRestartStrategy;
import choco.kernel.solver.search.restart.LubyRestartStrategy;
import choco.kernel.solver.search.restart.UniversalRestartStrategy;
/**
 * @author Arnaud Malapert</br> 
 * @since 27 juil. 2009 version 2.1.1</br>
 * @version 2.1.1</br>
 */
public final class RestartFactory {


	private RestartFactory() {
		super();
	}

	public static void setLubyRestartPolicy(Solver solver, int base, int grow) {
		final Configuration conf = solver.getConfiguration();
		conf.putTrue(RESTART_LUBY);
		conf.putFalse(RESTART_GEOMETRICAL);
		conf.putInt(RESTART_BASE, base);
		conf.putInt(RESTART_LUBY_GROW, grow);
	}

	public static void setGeometricalRestartPolicy(Solver solver, int base, double grow) {
		final Configuration conf = solver.getConfiguration();
		conf.putFalse(RESTART_LUBY);
		conf.putTrue(RESTART_GEOMETRICAL);
		conf.putInt(RESTART_BASE, base);
		conf.putDouble(RESTART_GEOM_GROW, grow);
	}
	
		
	public static void setRecordNogoodFromRestart(Solver solver) {
		solver.getConfiguration().putTrue(Configuration.NOGOOD_RECORDING_FROM_RESTART);
	}
	
	public static void unsetRecordNogoodFromRestart(Solver solver) {
		solver.getConfiguration().putFalse(Configuration.NOGOOD_RECORDING_FROM_RESTART);
	}

	public static void cancelRestarts(Solver solver) {
		final Configuration conf = solver.getConfiguration();
		conf.putFalse(RESTART_AFTER_SOLUTION);
		conf.putFalse(RESTART_LUBY);
		conf.putFalse(RESTART_GEOMETRICAL);
		conf.remove(NOGOOD_RECORDING_FROM_RESTART);
		conf.remove(RESTART_BASE);
		conf.remove(RESTART_LUBY_GROW);
		conf.remove(RESTART_GEOM_GROW);
		conf.remove(RESTART_POLICY_LIMIT);
	}

	
	
	public static UniversalRestartStrategy createRestartStrategy(Solver solver) {
		final Configuration conf = solver.getConfiguration();
		final boolean bL = conf.readBoolean(RESTART_LUBY);
		final boolean bG = conf.readBoolean(RESTART_GEOMETRICAL);
		if(bL) {
			if(bG) throw new SolverException("Invalid Restart Settings: Two policies");
			else return new LubyRestartStrategy(conf.readInt(RESTART_BASE), conf.readInt(RESTART_LUBY_GROW));
		}else if( bG) {
			return new GeometricalRestartStrategy(conf.readInt(RESTART_BASE), conf.readDouble(RESTART_GEOM_GROW));
		}else return null;
	}

}
