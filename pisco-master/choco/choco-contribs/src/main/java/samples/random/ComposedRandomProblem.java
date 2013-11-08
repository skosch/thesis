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

package samples.random;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.strong.DomOverDDegRPC;
import choco.cp.solver.constraints.strong.StrongConsistencyManager;
import choco.cp.solver.constraints.strong.maxrpcrm.MaxRPCrm;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.solver.Solver;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.logging.Logger;

public class ComposedRandomProblem {

    protected final static Logger LOGGER = ChocoLogging.getMainLogger();

	private static double criticTightness(int nbvar, int nbval, int nbcons) {
		// d = n^alpha
		double alpha = Math.log(nbval) / Math.log(nbvar);

		if (alpha <= .5) {
			throw new InvalidParameterException();
		}

		// e = rn ln(n)
		double r = nbcons / (nbvar * Math.log(nbvar));

		// pcr = 1 − exp(−alpha/r)

        // if (pcr > .5) {
		// System.err.println("Warning : tightness should not be > 0.5");
		// }

		return 1 - Math.exp(-alpha / r);
	}

	private static int nbArcs(double n, double density) {
		return (int) (density * n * (n - 1) / 2d);
	}

	private static int TIMEOUT = -1;

	private enum Filter {
		ac, maxrpc, light
	}

	public static void main(String args[]) {
		final int nbVar1;
		final int nbVal1;
		final double density1;
		final double tight1;
		final boolean force1;
		final Filter filter1;

		final int nbVar2;
		final int nbVal2;
		final double density2;
		final double tight2;
		final boolean force2;
		final Filter filter2;

		final List<Long> seeds = new ArrayList<Long>();

		try {
			int i = 0;

			nbVar1 = Integer.valueOf(args[i++]);
			nbVal1 = Integer.valueOf(args[i++]);
			density1 = Double.valueOf(args[i++]);
			tight1 = Double.valueOf(args[i++]);
			force1 = Boolean.valueOf(args[i++]);

			filter1 = Filter.valueOf(args[i++]);

			nbVar2 = Integer.valueOf(args[i++]);
			nbVal2 = Integer.valueOf(args[i++]);
			density2 = Double.valueOf(args[i++]);
			tight2 = Double.valueOf(args[i++]);
			force2 = Boolean.valueOf(args[i++]);

			filter2 = Filter.valueOf(args[i++]);

			TIMEOUT = 1000 * Integer.valueOf(args[i++]);

			while (i < args.length) {
				seeds.add(Long.valueOf(args[i++]));
			}

		} catch (Exception exception) {
			LOGGER.info("Usage : RandomProblem nbVar1 nbVal1 density1 tight1 force1 alg1 nbVar2 nbVal2 density2 tight2 force2 alg2 timeout seeds...");
			return;
		}

		final int e1 = nbArcs(nbVar1, density1);
		final int e2 = nbArcs(nbVar2, density2);

		LOGGER.info("-------------------");
		LOGGER.info("1: " + nbVar1 + " var, " + nbVal1 + " val, "
				+ density1 + " density (" + e1 + " cstr), tightness " + tight1
				+ (force1 ? ", forced" : "") + " (pcr = "
				+ criticTightness(nbVar1, nbVal1, e1) + "), " + filter1);
		LOGGER.info("2: " + nbVar2 + " var, " + nbVal2 + " val, "
				+ density2 + " density (" + e2 + " cstr), tightness " + tight2
				+ (force2 ? ", forced" : "") + " (pcr = "
				+ criticTightness(nbVar2, nbVal2, e2) + "), " + filter2);

		test(nbVar1, nbVal1, e1, tight1, force1, filter1, nbVar2, nbVal2, e2,
				tight2, force2, filter2, seeds);

	}

	private static void test(int nbVar1, int nbVal1, int e1, double tight1,
			boolean force1, Filter filter1, int nbVar2, int nbVal2, int e2,
			double tight2, boolean force2, Filter filter2, List<Long> seeds) {
		final List<Integer> nodes = new ArrayList<Integer>(seeds.size());
		final List<Double> cpu = new ArrayList<Double>(seeds.size());
		final List<Integer> nbAwakes = new ArrayList<Integer>(seeds.size());
		final List<Long> mem = new ArrayList<Long>(seeds.size());
		// final double[] nps = new double[NBINSTANCES - 1];

        for (long seed : seeds) {
            StringBuffer st = new StringBuffer();
            st.append(seed);
			// Build a model
			final Model m = new CPModel();

			final RandomProblem problem1 = new RandomProblem(nbVar1, nbVal1,
					e1, tight1, seed, force1);

			m.addVariables(problem1.getVariables());

			if (filter1 == Filter.ac) {
				for (Constraint c : problem1.getConstraints()) {
					m.addConstraint(c);
				}
			} else {
				final Collection<Constraint> maxRPCConstraints = problem1
						.getConstraints();

				final Constraint cc = new ComponentConstraint(
						StrongConsistencyManager.class,
						new Object[] {
								MaxRPCrm.class,
								maxRPCConstraints
										.toArray(new Constraint[maxRPCConstraints
												.size()]) }, problem1
								.getVariables());

				if (filter1 == Filter.light) {
					cc.addOption("light");
				}

				m.addConstraint(cc);
			}

			if (nbVar2 > 0) {

				final RandomProblem problem2 = new RandomProblem(nbVar2,
						nbVal2, e2, tight2, seed, force2);

				m.addVariables(problem2.getVariables());

				if (filter2 == Filter.ac) {
					for (Constraint c : problem2.getConstraints()) {
						m.addConstraint(c);
					}
				} else {

					final Collection<Constraint> maxRPCConstraints = problem2
							.getConstraints();

					final Constraint cc = new ComponentConstraint(
							StrongConsistencyManager.class,
							new Object[] {
									MaxRPCrm.class,
									maxRPCConstraints
											.toArray(new Constraint[maxRPCConstraints
													.size()]) }, problem2
									.getVariables());

					if (filter2 == Filter.light) {
						cc.addOption("light");
					}

					m.addConstraint(cc);
				}

				m.addConstraint(Choco.infeasPairAC(problem1.getVariables()[0],
						problem2.getVariables()[0], Arrays.asList(new int[] {
								0, 0 })));
			}
			// LOGGER.info("Building solver...");

			// Build a solver
			final Solver s = new CPSolver();
			// CPSolver.setVerbosity(CPSolver.SEARCH);
			// Read the model
			s.read(m);

			s.setVarIntSelector(new DomOverDDegRPC(s));

			s.setTimeLimit(TIMEOUT);
			// LOGGER.info("Solving...");
			st.append("s");

			System.gc();
			final Boolean result = s.solve();
			mem.add(Runtime.getRuntime().totalMemory()
					- Runtime.getRuntime().freeMemory());
			// CPSolver.flushLogs();

			if (result == null) {
				st.append("-");
				nodes.add(Integer.MAX_VALUE);
				cpu.add(Double.POSITIVE_INFINITY);
				nbAwakes.add(Integer.MAX_VALUE);
			} else {
				st.append(result ? "*" : "o");
				nodes.add(s.getNodeCount());
				cpu.add(s.getTimeCount() / 1000.0);
				nbAwakes.add(MaxRPCrm.nbPropag);
				MaxRPCrm.nbPropag = 0;
			}
			// nps[i] = 1000 * s.getNodeCount() / s.getTimeCount();

            LOGGER.info(st.toString());
        }

		// LOGGER.info(average(cpu) + " seconds avg");
		// LOGGER.info(average(nodes) + " nodes avg");
		// LOGGER.info(average(nps) + " nds avg");

		LOGGER.info(median(cpu) + " seconds med");
		LOGGER.info(median(nodes) + " nodes med");
		LOGGER.info(median(nbAwakes) + " awakes med");
		LOGGER.info(median(mem) + " mem med");
		// LOGGER.info(median(nps) + " nds med");
	}

	private static <T extends Comparable<T>> T median(List<T> list) {
		List<T> sorted = new ArrayList<T>(list);
		Collections.sort(sorted);
		return sorted.get((sorted.size() - 1) / 2);
	}

}