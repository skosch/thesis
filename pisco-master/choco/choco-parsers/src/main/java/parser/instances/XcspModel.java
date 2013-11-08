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

import static parser.instances.xcsp.XcspSettings.Heuristic.IMPACT;

import java.io.File;

import parser.absconparseur.components.PVariable;
import parser.absconparseur.tools.InstanceParser;
import parser.absconparseur.tools.SolutionChecker;
import parser.absconparseur.tools.UnsupportedConstraintException;
import parser.chocogen.ChocoFactory;
import parser.chocogen.ObjectFactory;
import parser.instances.xcsp.XcspSettings;
import choco.cp.model.CPModel;
import choco.cp.solver.constraints.integer.extension.ValidityChecker;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.cp.solver.preprocessor.PreProcessConfiguration;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.kernel.model.Model;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.checker.SolutionCheckerException;


class ParserWrapper implements InstanceFileParser {

	public InstanceParser source = new InstanceParser(); 

	public File file;
	@Override
	public void cleanup() {
		source = new InstanceParser();
		file = null;
	}


	@Override
	public File getInstanceFile() {
		return file;
	}

	@Override
	public void loadInstance(File file) {
		this.file = file;
		source.loadInstance(file.getAbsolutePath());

	}

	@Override
	public void parse(boolean displayInstance)
	throws UnsupportedConstraintException {
		source.parse(displayInstance);

	}


}
/**
 * User:    charles
 * Date:    19 août 2008
 * <p/>
 * A class to provide facilities for loading and solving
 * CSP described in the xml format of the 2008 competition
 */
public class XcspModel extends AbstractInstanceModel {

	//temporary data
	private XcspSettings.Heuristic cheuri;
	private String[] values;


	public XcspModel(Configuration settings) {
		super(new ParserWrapper(), settings);
	}

	public final XcspSettings getXcspSettings() {
		return (XcspSettings) defaultConf;
	}
	
	@Override
	public void initialize() {
		super.initialize();
		values = null;
	}






	@Override
	public Model buildModel() {
		InstanceParser parser = ( (ParserWrapper) this.parser).source;
		boolean forceExp = false; //force all expressions to be handeled by arc consistency
		CPModel m = new CPModel(parser.getMapOfConstraints().size(), parser.getNbVariables(), 50, 0, 100, 100, 100);
		ChocoFactory chocofact = new ChocoFactory(parser, m);
		chocofact.createVariables();
		chocofact.createRelations();
		chocofact.createConstraints(forceExp);
		return m;
	}


	@Override
	public Solver buildSolver() {
		PreProcessCPSolver s = new PreProcessCPSolver(defaultConf);
		s.read(model);
		return s;
	}



	@Override
	public String getValuesMessage() {
		if(values != null) {
			final StringBuilder b = new StringBuilder(16);
			for (int i = 1; i < values.length; i++) {
				b.append(values[i]).append(' ');
			}
			return b.toString();
		}else return "";

	}



	@Override
	public Boolean preprocess() {
		return null;
	}

	@Override
	public Boolean solve() {
		PreProcessCPSolver s = (PreProcessCPSolver) solver;
		Boolean isFeasible = Boolean.TRUE;
		final int timeLimitPP = defaultConf.readInt(BasicSettings.PREPROCESSING_TIME_LIMIT);
		//do the initial propagation to decide to do restarts or not
		if (!s.initialPropagation()) {
			return Boolean.FALSE;
		} else {
			if (defaultConf.readBoolean(BasicSettings.RANDOM_VALUE) ) s.setRandomValueOrdering( (int) getSeed());
			cheuri = defaultConf.readEnum(XcspSettings.HEURISTIC, XcspSettings.Heuristic.class);
			//set the search
			switch (cheuri) {
			case VERSATILE:
				isFeasible = s.setVersatile(s, timeLimitPP);
				cheuri = XcspSettings.match(s.getBBSearch().determineHeuristic(s));
				break;
			case DOMOVERDEG:
				isFeasible = s.setDomOverDeg(s); break;
			case DOMOVERWDEG:
				isFeasible = s.setDomOverWeg(s, timeLimitPP);
				//((DomOverWDegBranching) s.tempGoal).setRandomVarTies(seed);
				break;
			case IMPACT:
				isFeasible = s.setImpact(s, timeLimitPP);
				//((ImpactBasedBranching) s.tempGoal).setRandomVarTies(seed);
				break;
			case SIMPLE:
				s.setVarIntSelector(new MinDomain(s));
				if ( defaultConf.readBoolean(BasicSettings.RANDOM_VALUE) ) s.setValIntSelector(new RandomIntValSelector(getSeed()));
				else s.setValIntIterator(new IncreasingDomain());
			default:
				break;
			}
		}
        final boolean doSingleton = defaultConf.readBoolean(XcspSettings.SINGLETON_CONSISTENCY) ;
		if (isFeasible && (cheuri == IMPACT ||(!doSingleton || s.rootNodeSingleton(timeLimitPP)))) {
			return s.solve();
		} else {
			return Boolean.FALSE;
		}
	}


	public static boolean checkEverythingIsInstantiated(InstanceParser parser, Solver s) {
		for (int i = 0; i < parser.getVariables().length; i++) {
			try {
				if (!s.getVar(parser.getVariables()[i].getChocovar()).isInstantiated()) {
					return false;
				}
			} catch (NullPointerException ignored) {
			}
		}
		return true;
	}

	@Override
	public void checkSolution() throws SolutionCheckerException {
		super.checkSolution();
		InstanceParser pars = ( (ParserWrapper) parser).source;
		if( !checkEverythingIsInstantiated(pars, solver) ) throw new SolutionCheckerException("Some Variables are not instantiated");
		PVariable[] vars = pars.getVariables(); 
		values = new String[vars.length + 1];
		values[0] = parser.getInstanceFile().getPath();
		for (int i = 1; i < vars.length + 1; i++) {
			try {
				values[i] = String.valueOf(solver.getVar(vars[i-1].getChocovar()).getVal());
			} catch (NullPointerException e) {
				values[i] = String.valueOf(vars[i-1].getChocovar().getLowB());
			}
		}
		ValidityChecker.nbCheck = 0;
		if (defaultConf.readBoolean(XcspSettings.EXTERNAL_CHECK)) SolutionChecker.main(values);
	}




	@Override
	protected void logOnDiagnostics() {
		super.logOnDiagnostics();
		logMsg.appendDiagnostic("CHECKS",  ValidityChecker.nbCheck);
		logMsg.appendDiagnostic("AC",  ObjectFactory.algorithmAC);
	}

	@Override
	protected void logOnConfiguration() {
		super.logOnConfiguration();
		logMsg.appendConfiguration(PreProcessConfiguration.getPreProcessMsg(defaultConf)
                + cheuri+" HEURISTIC    "
                + defaultConf.readBoolean(BasicSettings.RANDOM_VALUE)+" RANDVAL");
	}


}
