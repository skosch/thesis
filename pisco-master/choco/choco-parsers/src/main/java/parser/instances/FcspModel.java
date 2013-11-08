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

import java.io.File;

import parser.absconparseur.tools.UnsupportedConstraintException;
import parser.flatzinc.ast.SolveGoal;
import parser.flatzinc.parser.FZNParser;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.Solver;


final class FznParserWrapper implements InstanceFileParser {

    public FZNParser source = new FZNParser();
	public File file;

	@Override
	public void cleanup() {
        source = new FZNParser();
		file = null;
	}

	@Override
	public File getInstanceFile() {
		return file;
	}

    @Override
	public void loadInstance(File file) {
		this.file = file;
		source.loadInstance(file);
	}

	@Override
	public void parse(boolean displayInstance)throws UnsupportedConstraintException {
        // nothing to do,
        // cause the parsing build directly the model
    }

}

/**
 * User : cprudhom
 * Mail : cprudhom(a)emn.fr
 * Date : 19 janv. 2010
 * Since : Choco 2.1.1
 *
 * A class to provide facilities for loading and solving
 * CSP described in the flatzinc grammar.
 */
public final class FcspModel extends AbstractInstanceModel {

    private boolean searchSet;

    private SolveGoal solveGoal;

    public FcspModel(Configuration settings) {
        super(new FznParserWrapper(), settings);
    }

    /**
     * Executes preprocessing ( bounding, heuristics ...)
     * default implementation: do nothing.
     *
     * @return <code>true</code> if a solution has been found, <code>false</code> if the infeasibility has been proven and <code>null</code> otherwise.
     */
    @Override
    public Boolean preprocess() {
        return null;
    }

    /**
     * create the choco model after the preprocessing phase.
     */
    @Override
    public Model buildModel() {
        final FZNParser parser = ( (FznParserWrapper) this.parser).source;
		solveGoal = parser.parse();
        return parser.model;
    }

    /**
     * create a solver from the current model
     */
    @Override
    public Solver buildSolver() {
        PreProcessCPSolver s = new PreProcessCPSolver(defaultConf);
		s.read(model);
		searchSet = solveGoal.defineGoal(s);
		return s;
    }

    /**
     * configure and launch the resolution.
     */
    @Override
    public Boolean solve() {
        PreProcessCPSolver s = (PreProcessCPSolver) solver;
		Boolean isFeasible = Boolean.TRUE;
		//do the initial propagation to decide to do restarts or not
		if (!s.initialPropagation()) {
			return Boolean.FALSE;
		} else {
            if(!searchSet){
			// TODO : set default search when 'searchSet' is false
            }
		}
		//TODO Hadrien, Charles check this code samples, it is important that I did not break it
//		settings.applyRestartPolicy(s);
		if (isFeasible){ 
                //&& (cheuri == IMPACT || s.rootNodeSingleton(settings.doSingletonConsistency(), settings.getTimeLimitPP()))) {
			//			if (ngFromRestart && (s.restartMode || forcerestart)) {
			//				s.setRecordNogoodFromRestart(true);
			//				s.generateSearchStrategy();
			//				//s.getSearchStrategy().setSearchLoop(new SearchLoopWithNogoodFromRestart(s.getSearchStrategy(), s.getRestartStrategy()));
			//				s.launch();
			//				return s.isFeasible();
			//			} else return s.solve();
		    s.launch();
			return s.isFeasible();
		} else {
			return Boolean.FALSE;
		}
    }
}
