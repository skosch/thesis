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

package parser.chocogen;


import choco.cp.model.CPModel;
import choco.cp.solver.constraints.integer.extension.ValidityChecker;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.cp.solver.preprocessor.PreProcessConfiguration;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.Solver;
import parser.absconparseur.tools.InstanceParser;
import parser.absconparseur.tools.SolutionChecker;
import parser.absconparseur.tools.UnsupportedConstraintException;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User:    charles
 * Date:    19 aoÃ»t 2008
 * <p/>
 * A class to provide facilities for loading and solving
 * CSP described in the xml format of the 2008 competition
 */
public class XmlModel {
	// TODO - Replace class by AbstractInstanceModel ! - created 16 févr. 2012 by A. Malapert
	public final static Logger LOGGER = ChocoLogging.getMainLogger();

	//heuristics
	private static final int DOMOVERDEG = 0;
	private static final int DOMOVERWDEG = 1;
	private static final int IMPACT = 2;
	private static final int VERSATILE = 3;
	private static final int SIMPLE = 4;
	private static int heuristic = 0; // DOMOVERDEG by default

	private static int seed;


	//perform singleton consistency step or not
	private static boolean singleton = false;

	private static boolean ngFromRestart = false;

	//force to restart or not
	private static Boolean forcerestart = null;
	private static int base = 10;
	private static double growth = 1.5d;

	private static int verb = 0; // if O no verb

	//total timelimit in s
	private static int timelimit = 10000; // in sec

	//initialization timelimit (for impact) in ms
	public int initialisationtime = 60000; //60

	public boolean randvalh = false;

	//temporary data
	private Boolean isFeasible = null;
	private int cheuri;
	private int nbnode = 0;
	private int nbback = 0;
	private static long[] time = new long[5];
	private static String[] values;

	public void init() {
		time = new long[5];
		isFeasible = null;
		nbback = 0;
		nbnode = 0;
	}

	/**
	 * Main method. Check arguments and set up the options
	 * accordingly. example of command line :
	 * -file mycsp.xml -h 3 -ac 32 -s true -verb 1 -time 30
	 *
	 * @param args arguments
	 * @throws Exception
	 */
	public void generate(String[] args) throws Exception {
		//ChocoLogging.setVerbosity(Verbosity.SEARCH);
		HashMap<String, String> options = new HashMap<String, String>(16);
		for (int i = 0; i < args.length; i++) {
			String arg = args[i++];
			String val = args[i];
			options.put(arg, val);
		}
		File dossier;
		if (options.containsKey("-file")) {
			dossier = new File(options.get("-file"));
			if (!dossier.exists()) {
				throw new Exception("Unknown file or directory");
			}
		} else {
			throw new Exception("file option -file is missing");
		}
		if (options.containsKey("-h")) {
			heuristic = Integer.parseInt(options.get("-h"));
		} else {
			throw new Exception("heuristic option -h is missing");
		}
		if (options.containsKey("-ac")) {
			ObjectFactory.algorithmAC = Integer.parseInt(options.get("-ac"));
		} else {
			throw new Exception("AC option -ac is missing");
		}
		if (options.containsKey("-s")) {
			singleton = Boolean.parseBoolean(options.get("-s"));
		}
		if (options.containsKey("-time")) {
			timelimit = Integer.parseInt(options.get("-time"));
		}
		if (options.containsKey("-verb")) {
			verb = Integer.parseInt(options.get("-verb"));
		}
		if (options.containsKey("-rest")) {
			forcerestart = Boolean.parseBoolean(options.get("-rest"));
		}
		if (options.containsKey("-rb")) {
			base = Integer.parseInt(options.get("-rb"));
		}
		if (options.containsKey("-rg")) {
			growth = Double.parseDouble(options.get("-rg"));
		}
		if (options.containsKey("-saclim")) {
			initialisationtime = Integer.parseInt(options.get("-saclim")) * 1000;
		}
		if (options.containsKey("-seed")) {
			seed = Integer.parseInt(options.get("-seed"));
		}
		if (options.containsKey("-randval")) {
			randvalh = Boolean.parseBoolean(options.get("-randval"));
		}
		if (options.containsKey("-ngfres")) {
			ngFromRestart = Boolean.parseBoolean(options.get("-ngfres"));
		}
		try {
			if (dossier.isFile()) {
				solveFile(dossier);
			} else {
				solveDirectory(dossier);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		ChocoLogging.flushLogs();
	}

	/**
	 * ei
	 * Solve the csp given by file "fichier"
	 *
	 * @param fichier
	 */
	public void solveFile(File fichier) {
		init();
		if (fichier.getName().endsWith(".xml")
				|| fichier.getName().endsWith(".xml.bz2")) {
			try {
				InstanceParser parser = load(fichier);
				CPModel model = buildModel(parser);
				PreProcessCPSolver s = solve(model);
				postAnalyze(fichier, parser, s);
			} catch (UnsupportedConstraintException ex) {
				LOGGER.info("s UNSUPPORTED");
				ChocoLogging.flushLogs();
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Error e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Solve all the csps contained in the corresponding directory :
	 * dossiers
	 *
	 * @param dossiers : the directory where instances are stored
	 */
	public void solveDirectory(File dossiers) {
		File listingDonneesEntree[] = dossiers.listFiles();
		for (File fichier : listingDonneesEntree) {
			if (fichier.isFile()) {
				solveFile(fichier);
			} else if (fichier.isDirectory()) {
				solveDirectory(fichier);
			}
		}
	}

	/**
	 * Parse the xml and return the parser object (Christophe parser) which
	 * can be used to access variables, constraints, etc...
	 *
	 * @param fichier
	 * @return A parser object containing the description of the problem
	 * @throws Exception
	 * @throws Error
	 */
	public static InstanceParser load(File fichier) throws Exception, Error {
		try {
			if (verb > 0) {
				LOGGER.log(Level.INFO, "========================================================\nTraitement de :{0}", fichier.getName());
			}
			// Parse the xml and get the abscon representation of the problem
			time[0] = System.currentTimeMillis();
			InstanceParser parser = new InstanceParser();
			parser.loadInstance(fichier.getAbsolutePath());
			parser.parse(false);
			time[1] = System.currentTimeMillis();
			return parser;
		} catch (UnsupportedConstraintException ex) {
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
		} catch (Error er) {
			er.printStackTrace();
		}
		return null;
	}


	/**
	 * Building the Model and solver
	 *
	 * @param parser
	 * @return
	 * @throws Exception
	 * @throws Error
	 */
	public CPModel buildModel(InstanceParser parser) throws Exception, Error {
		boolean forceExp = false; //force all expressions to be handeled by arc consistency
		CPModel m = new CPModel(parser.getMapOfConstraints().size(), parser.getNbVariables(), 50, 0, 100, 100, 100);
		ChocoFactory chocofact = new ChocoFactory(parser, m);
		chocofact.createVariables();
		chocofact.createRelations();
		chocofact.createConstraints(forceExp);
		time[2] = System.currentTimeMillis();
		return m;
	}


	/**
	 * Solving process
	 *
	 * @param model
	 * @return
	 * @throws Exception
	 * @throws Error
	 */
	public PreProcessCPSolver solve(CPModel model) throws  Error {

		PreProcessCPSolver s = new PreProcessCPSolver();
        final Configuration conf = s.getConfiguration();
		s.read(model);
		if (verb > 0) {
			LOGGER.info(MessageFormat.format("solve...dim:[nbv:{0}][nbc:{1}][nbconstants:{2}]", s.getNbIntVars(), s.getNbIntConstraints(), s.getNbConstants()));
		}

		time[3] = System.currentTimeMillis();
		s.setTimeLimit(timelimit * 1000);

		if (verb > 1) LOGGER.info(s.pretty());

		isFeasible = true;
		//do the initial propagation to decide to do restarts or not
		if (!s.initialPropagation()) {
			isFeasible = false;
		} else {
			if (randvalh) s.setRandomValueOrdering(seed);
			cheuri = heuristic;
			//set the search
			switch (cheuri) {
			case VERSATILE:
				isFeasible = s.setVersatile(s, initialisationtime);
				cheuri = s.getBBSearch().determineHeuristic(s);
				break;
			case DOMOVERDEG:
				isFeasible = s.setDomOverDeg(s); break;
			case DOMOVERWDEG:
				isFeasible = s.setDomOverWeg(s, initialisationtime);
				//((DomOverWDegBranching) s.tempGoal).setRandomVarTies(seed);
				break;
			case IMPACT:
				isFeasible = s.setImpact(s, initialisationtime);
				//((ImpactBasedBranching) s.tempGoal).setRandomVarTies(seed);
				break;
			case SIMPLE:
				s.setVarIntSelector(new MinDomain(s));
				if (randvalh)
					s.setValIntSelector(new RandomIntValSelector(seed));
				else s.setValIntIterator(new IncreasingDomain());
			default:
				break;
			}
		}
        final boolean restartMode = conf.readBoolean(PreProcessConfiguration.RESTART_MODE);
		if (forcerestart != null) {
			if (forcerestart) {
				s.setGeometricRestart(base, growth);
			}
		} else {
			if (restartMode) {
				s.setGeometricRestart(10, 1.3);                                
				//s.setGeometricRestart(Math.min(Math.max(s.getNbIntVars(), 200), 400), 1.4d);
			}
		}
		//ChocoLogging.setVerbosity(Verbosity.SEARCH);
		if (isFeasible
                && (cheuri == IMPACT || (!singleton || s.rootNodeSingleton(initialisationtime)))) {
			if (ngFromRestart && (restartMode || forcerestart)) {
				conf.putBoolean(Configuration.NOGOOD_RECORDING_FROM_RESTART, true);
				s.generateSearchStrategy();
				//s.getSearchStrategy().setSearchLoop(new SearchLoopWithNogoodFromRestart(s.getSearchStrategy(), s.getRestartStrategy()));
				s.launch();

			} else s.solve();
			//            s.solve();

			isFeasible = s.isFeasible();
			nbnode = s.getNodeCount();
			nbback = s.getBackTrackCount();
		} else {
			isFeasible = false;
		}
		return s;
	}

	/**
	 * Output in the standart console a set of statistics on the search
	 *
	 * @param fichier
	 * @param parser
	 * @param s
	 * @throws Exception
	 * @throws Error
	 */
	public void postAnalyze(File fichier, InstanceParser parser, PreProcessCPSolver s) throws  Error {
		//CPSolver.flushLogs();
		time[4] = System.currentTimeMillis();
		//LOGGER.info("" + isFeasible);
		//Output in a format for internal competition
		if (isFeasible == Boolean.TRUE
				&& ((!checkEverythingIsInstantiated(parser, s))
						|| s.checkSolution(false) !=Boolean.TRUE)) {
			isFeasible = null;
		}
		values = new String[parser.getVariables().length + 1];
		StringBuffer res = new StringBuffer("c ");
		if (isFeasible == null) {
			res.append("TIMEOUT");
			LOGGER.info("s UNKNOWN");
		} else if (!isFeasible) {
			res.append("UNSAT");
			LOGGER.info("s UNSATISFIABLE");
		} else {
			//            if (!s.checkSolution(false)) {
			//                //Check the solution with choco
			//                res.append("WRONGSOL?");
			//                LOGGER.info("s UNKNOWN");
			//            } else {
			res.append("SAT");
			LOGGER.info("s SATISFIABLE");
			StringBuilder sol = new StringBuilder("v ");
			values[0] = fichier.getPath();
			for (int i = 0; i < parser.getVariables().length; i++) {
				try {
					values[i + 1] = String.valueOf(s.getVar(parser.getVariables()[i].getChocovar()).getVal());
				} catch (NullPointerException e) {
					values[i + 1] = String.valueOf(parser.getVariables()[i].getChocovar().getLowB());
				}
                sol.append(values[i + 1]).append(' ');
			}
			LOGGER.info(sol.toString());
			//}
		}
		final double rtime = (double) (time[4] - time[0]) / 1000D;
		res.append(' ').append(rtime).append(" TIME     ");
		res.append(' ').append(nbnode).append(" NDS   ");
		res.append(' ').append(time[1] - time[0]).append(" PARSTIME     ");
		res.append(' ').append(time[2] - time[1]).append(" BUILDPB      ");
		res.append(' ').append(time[3] - time[2]).append(" CONFIG       ");
		res.append(' ').append(time[4] - time[3]).append(" RES      ");
        final boolean restartMode = s.getConfiguration().readBoolean(PreProcessConfiguration.RESTART_MODE);
		res.append(' ').append(restartMode).append(" RESTART      ");
		res.append(' ').append(cheuri).append(" HEURISTIC      ");
		res.append(' ').append(randvalh).append(" RANDVAL      ");
		LOGGER.info("d AC " + ObjectFactory.algorithmAC);
		LOGGER.info("d RUNTIME " + rtime);
		LOGGER.info("d NODES " + nbnode);
		LOGGER.info("d NODES/s " + Math.round((double) nbnode / rtime));
		LOGGER.info("d BACKTRACKS " + nbback);
		LOGGER.info("d BACKTRACKS/s " + Math.round((double) nbback / rtime));
		LOGGER.info("d CHECKS " + ValidityChecker.nbCheck);
		LOGGER.info("d CHECKS/s " + Math.round((double) ValidityChecker.nbCheck / rtime));

		ValidityChecker.nbCheck = 0;

		LOGGER.info(String.valueOf(res));
		if (verb > 0) {
			if (isFeasible == Boolean.TRUE) {
				SolutionChecker.main(values);
			}
		}
		ChocoLogging.flushLogs();
	}

	public static long getParseTime() {
		return (time[1] - time[0]);
	}

	public static long getBuildTime() {
		return (time[2] - time[1]);
	}

	public static long getConfTime() {
		return (time[3] - time[2]);
	}

	public static long getResTime() {
		return (time[4] - time[3]);
	}

	public static long getFullTime() {
		return (time[4] - time[0]);
	}

	public int getNbNodes() {
		return nbnode;
	}

	public Boolean isFeasible() {
		return isFeasible;
	}

	public static String[] getValues() {
		return values;
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


	/**
	 * An example on how to use the xml parser-solver from the api
	 */
	 public static void example() {
		String fichier = "../../ProblemsData/CSPCompet/intension/nonregres/graph1.xml";
		File instance = new File(fichier);
		XmlModel xs = new XmlModel();

		try {
			InstanceParser parser = load(instance);
			CPModel model = xs.buildModel(parser);

			//use the blackbox solver and blackbox search
			PreProcessCPSolver s = xs.solve(model);
			xs.postAnalyze(instance, parser, s);

			//use a blackbox solver or a standart CP solver
			//and perform the search by yourself
			//            BlackBoxCPSolver s = new BlackBoxCPSolver();
			//            CPSolver s = new CPSolver();
			//            s.read(model);
			//            s.solve();
			//            LOGGER.info(s.pretty());
			//            s.printRuntimeSatistics();

		} catch (Exception e) {
			e.printStackTrace();
		}
	 }

	 public static void main(String[] args) throws Exception {
		 //         example();
		 XmlModel xs = new XmlModel();
		 xs.generate(args);
	 }


}
