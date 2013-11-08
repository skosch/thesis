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

package parser.absconparseur.tools;

import choco.kernel.common.logging.ChocoLogging;
import parser.absconparseur.InstanceTokens;
import parser.absconparseur.components.PConstraint;
import parser.absconparseur.components.PVariable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author christophe lecoutre
 * @version 2.1.1
 */
public class SolutionChecker {

    protected final static Logger LOGGER = ChocoLogging.getMainLogger();

	private InstanceParser parser;

	private static InstanceParser loadAndParseInstance(String instanceFileName) throws Exception {
		InstanceParser parser = new InstanceParser();
		parser.loadInstance(instanceFileName);
		parser.parse(false);
		return parser;
	}

	private static int[] buildSolutionFrom(String line, int nbVariables) throws Exception {
		int[] t = new int[nbVariables];
		int i = 0;
		StringTokenizer st = new StringTokenizer(line);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			int last = token.length() - 1;
			while (last >= 0 && Character.isDigit(token.charAt(last)))
				last--;
			if (last >= 0 && token.charAt(last) == '-')
				last--;
			t[i++] = Integer.parseInt(token.substring(last + 1));
		}
		if (i < nbVariables)
			throw new IllegalArgumentException();
		return t;
	}

	private static int[] loadSolution(String solutionFileName, int nbVariables) throws Exception {
		BufferedReader rd = new BufferedReader(new FileReader(solutionFileName));
		String line = rd.readLine().trim();
		if (line.equals(InstanceTokens.UNSAT)) {
			LOGGER.severe("PROBLEM \t The file " + solutionFileName + " does not contain any solution.");
			System.exit(2);
		}
		if (line.equals(InstanceTokens.SAT)) {
			line = rd.readLine().trim();
			return buildSolutionFrom(line, nbVariables);
		}
		String previousLine = null;
		while (line != null && line.startsWith(InstanceTokens.SOL)) {
			previousLine = line;
			line = rd.readLine().trim();
		}
		if (previousLine != null)
			return buildSolutionFrom(previousLine, nbVariables);
		return buildSolutionFrom(line, nbVariables);
	}

	private void dealWithInstanceFileName(String instanceFileName) {
		if (!new File(instanceFileName).exists()) {
			LOGGER.severe("PROBLEM \t The file " + instanceFileName + " has not been found.");
			System.exit(2);
		}
		try {
			parser = loadAndParseInstance(instanceFileName);
		} catch (Exception e) {
			LOGGER.severe("PROBLEM \t When loading and/or parsing file " + instanceFileName + ' ' + e);
			e.printStackTrace();
			System.exit(2);
		}
	}

	private int[] dealWithSolutionFileName(String solutionFileName) {
		try {
			if (!new File(solutionFileName).exists()) {
				LOGGER.severe("The file " + solutionFileName + " has not been found");
				System.exit(2);
			}
			try {
				return loadSolution(solutionFileName, parser.getVariables().length);
			} catch (Exception e) {
				LOGGER.severe("PROBLEM \t When loading and/or parsing file " + solutionFileName + ' ' + e);
				System.exit(2);
				return null;
			}
		} catch (Throwable e) {
			LOGGER.severe("PROBLEM \t " + e.getMessage());
			System.exit(2);
			return null;
		}
	}

	/**
	 * Returns -1 if the solution is valid, the position of ther first invalid value otherwise
	 */
	public int isSolutionValid(int[] solution) {
		assert parser.getVariables().length == solution.length;
		for (int i = 0; i < solution.length; i++)
			if (!parser.getVariables()[i].getDomain().contains(solution[i]))
				return i;
		return -1;
	}

	/**
	 * Extract from the given solution the tuple of values that corresponds to the scope of the given constraint. <br>
	 * Of course, a more efficient approach could be devised, but here efficiency does not seem very important.
     * @param constraint
     * @param solution
     * @return
     */
	private int[] buildTupleFor(PConstraint constraint, int[] solution) {
		int[] tuple = new int[constraint.getScope().length];
		PVariable[] involvedVariables = constraint.getScope();
		for (int i = 0; i < involvedVariables.length; i++) {
			int position = 0;
			while (involvedVariables[i] != parser.getVariables()[position])
				position++;
			tuple[i] = solution[position];
		}
		return tuple;
	}

	private void checkSolution(int[] solution) {
		if (parser.getVariables().length != solution.length) {
			LOGGER.severe("PROBLEM \t The number of variables is " + parser.getVariables().length + " while the size of the solution is " + solution.length);
			System.exit(2);
		}

		int invalidPosition = isSolutionValid(solution);
		if (invalidPosition != -1) {
			LOGGER.log(Level.SEVERE,"ERROR \t The given solution is not valid as the {0}th value of the solution is not present in the domain of the associated variable",invalidPosition);
			System.exit(1);
		}

		List<String> list = new LinkedList<String>();
		long sum = 0;
		for (PConstraint constraint : parser.getMapOfConstraints().values()) {
			long cost = constraint.computeCostOf(buildTupleFor(constraint, solution));
			if (cost > 0)
				list.add(constraint.getName());
			sum += cost;
		}

		LOGGER.info("solutionCost " + sum);
		LOGGER.info("listOfUnsatisfiedConstraints " + list);
		//System.exit(0);
	}

	public SolutionChecker(String instanceFileName) {
		dealWithInstanceFileName(instanceFileName);
		String s0 = "satisfiable " + parser.getSatisfiable() + "  minViolatedConstraints " + parser.getMinViolatedConstraints();
		String s1 = "\t nbVariables " + parser.getVariables().length + "  nbConstraints " + parser.getMapOfConstraints().size();
		String s2 = "\t maxConstraintArity " + parser.getMaxConstraintArity() + "  nbExtensionConstraints " + parser.getNbExtensionConstraints() + "  nbIntensionConstraints "
				+ parser.getNbIntensionConstraints() + "  nbGlobalConstraints " + parser.getNbGlobalConstraints();
		LOGGER.log(Level.INFO, "{0} {1} {2}",new Object[]{s0, s1, s2});
		System.exit(0);
	}

	public SolutionChecker(String instanceFileName, String solutionFileName) {
		dealWithInstanceFileName(instanceFileName);
		checkSolution(dealWithSolutionFileName(solutionFileName));
	}

	public SolutionChecker(String instanceFileName, int[] solution) {
		dealWithInstanceFileName(instanceFileName);
		checkSolution(solution);
	}

	public static void main(String[] args) {
		try {
			if (args.length == 1)
				new SolutionChecker(args[0]);
			else if (args.length == 2)
				new SolutionChecker(args[0], args[1]);
			else if (args.length > 2) {
				int[] solution = new int[args.length - 1];
				for (int i = 0; i < solution.length; i++) {
					try {
						solution[i] = Integer.parseInt(args[i + 1]);
					} catch (NumberFormatException e) {
						LOGGER.severe("PROBLEM \t With the given solution: " + args[i + 1] + " is not an integer " + e);
						System.exit(2);
					}
				}
				new SolutionChecker(args[0], solution);
			} else {
				LOGGER.info("SolutionChecker " + InstanceParser.VERSION);
				LOGGER.info("Usage 1: java ... SolutionChecker <instanceFileName>");
				LOGGER.info("Usage 2: java ... SolutionChecker <instanceFileName> <solutionFileName>");
				LOGGER.info("Usage 3: java ... SolutionChecker <instanceFileName> <solution>\n");
				LOGGER.info("  <instanceFileName> must be the name of a file which contains the representation of a CSP or WCSP instance in format XCSP 2.1.");
				LOGGER.info("  <solutionFileName> must be the name of a file which:");
				LOGGER.info("     - either contains on the first line a sequence of values (integers separated by whitespace(s)), one for each variable of the instance");
				LOGGER.info("     - or respects the output format of the 2008 CSP competition");
				LOGGER.info("  <solution> must be a sequence of values (integers separated by whitespace(s)), one for each variable of the instance\n");
				LOGGER.info("With Usage 1, SolutionChecker outputs some information about the given instance");
				LOGGER.info("With Usage 2 and Usage 3, SolutionChecker outputs the cost of the given solution (number of violated constraints for CSP)\n");
				LOGGER.info("Exit code of solutionChecker is as follows:");
				LOGGER.info("  0 : no problem occurs and the solution is valid");
				LOGGER.info("  1 : the solution is not valid");
				LOGGER.info("  2 : a problem occurs (file not found, ...)");
				System.exit(0);
			}
		} catch (Throwable e) {
			LOGGER.severe("PROBLEM \t " + e.getMessage());
			System.exit(2);
		}
	}
}
