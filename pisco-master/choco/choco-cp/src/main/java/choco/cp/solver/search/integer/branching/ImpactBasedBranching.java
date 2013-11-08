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

package choco.cp.solver.search.integer.branching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import choco.kernel.common.TimeCacheThread;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.AbstractLargeIntBranchingStrategy;
import choco.kernel.solver.search.IntBranchingDecision;
import choco.kernel.solver.variables.AbstractVar;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Impact based branching based on the code from Hadrien
 * <p/>
 * Written by Guillaumme on 17 may 2008
 */
public class ImpactBasedBranching extends AbstractLargeIntBranchingStrategy {
	Solver _solver;
	IntDomainVar[] _vars;
	AbstractImpactStrategy _ibs;

	protected Random randValueChoice;
	protected Random randomBreakTies;

	private static final int ABSTRACTVAR_EXTENSION =
			AbstractVar.getAbstractVarExtensionNumber("choco.cp.cpsolver.search.integer.ImpactBasedBranching");

	static IntDomainVar[] varsFromSolver(Solver s) {
		IntDomainVar[] vars = new IntDomainVar[s.getNbIntVars()];
		for (int i = 0; i < vars.length; i++) {
			vars[i] = s.getIntVar(i);
		}
		return vars;
	}

	public ImpactBasedBranching(Solver solver, IntDomainVar[] vars, AbstractImpactStrategy ibs) {
		super();
		_solver = solver;
		_vars = vars;
		for (IntDomainVar var : _vars) {
			var.addExtension(ABSTRACTVAR_EXTENSION);
		}
		_ibs = ibs;
	}

	@Override
	public void initBranching() throws ContradictionException {
		super.initBranching();
		if(_solver.getNodeCount() == 0) {
			_ibs.initImpacts();
		}
	}

	public ImpactBasedBranching(Solver solver, IntDomainVar[] vars) {
		this(solver, vars, null);
		_ibs = new ImpactRef(this, _vars);
	}

	public ImpactBasedBranching(Solver solver) {
		this(solver, varsFromSolver(solver));
	}

	public AbstractImpactStrategy getImpactStrategy() {
		return _ibs;
	}

	public void setRandomVarTies(int seed) {
		randomBreakTies = new Random(seed);
	}

	public Object selectBranchingObject() throws ContradictionException {
		double min = Double.MAX_VALUE;
		IntDomainVar minVar = null;
		if (randomBreakTies == null) {
			for (IntDomainVar var : _vars) {
				if (!var.isInstantiated()) {
					double note;
					if (var.hasEnumeratedDomain())
						note = _ibs.getEnumImpactVar(var);
					else
						note = _ibs.getBoundImpactVar(var);
					if (note < min) {
						min = note;
						minVar = var;
					}
				}
			}
			return minVar;
		} else {
			//return null;
			List<IntDomainVar> lvs = new LinkedList<IntDomainVar>();
			for (IntDomainVar var : _vars) {
				if (!var.isInstantiated()) {
					double note;
					if (var.hasEnumeratedDomain())
						note = _ibs.getEnumImpactVar(var);
					else
						note = _ibs.getBoundImpactVar(var);
					if (note < min) {
						lvs.clear();
						min = note;
						lvs.add(var);
					} else if (note == min) {
						lvs.add(var);
					}
				}
			}
			if (lvs.isEmpty()) {
				return null;
			}
			return lvs.get(randomBreakTies.nextInt(lvs.size()));
		}

	}

	public final void setFirstBranch(final IntBranchingDecision ctx) {
		ctx.setBranchingValue( getBestVal( ctx.getBranchingIntVar()));
	}

	public void setNextBranch(final IntBranchingDecision ctx) {
		setFirstBranch(ctx);
	}

	public void setRandomValueChoice(long seed) {
		randValueChoice = new Random(seed);
	}

	public int getBestVal(IntDomainVar var) {
		if (randValueChoice == null) {
			if (var.hasEnumeratedDomain()) {
				DisposableIntIterator iter = var.getDomain().getIterator();
				double min = Double.MAX_VALUE;
				int minVal = Integer.MAX_VALUE;
				while (iter.hasNext()) {
					int val = iter.next();
					double note = _ibs.getImpactVal(var, val);
					if (note < min) {
						min = note;
						minVal = val;
					}
				}
				iter.dispose();
				return minVal;
			} else {
				return var.getInf();
			}
		} else {
			if (var.hasEnumeratedDomain()) {
				if (var.isInstantiated()) return var.getVal();                
				int val = (randValueChoice.nextInt(var.getDomainSize()));
				DisposableIntIterator iterator = var.getDomain().getIterator();
				for (int i = 0; i < val; i++) {
					iterator.next();
				}
				int res = iterator.next();
				iterator.dispose();
				return res;
			} else {
				int val = (randValueChoice.nextInt(2));
				if (val == 0) return var.getInf();
				else return var.getSup();
			}
		}
	}

	public boolean finishedBranching(final IntBranchingDecision ctx) {
		return ctx.getBranchingIntVar().getDomainSize() == 0; //FIXME why do we test the condition, it is always false ?
	}


	protected final void goDownBranch(final IntDomainVar var, final int val) throws ContradictionException {
		_ibs.doBeforePropagDownBranch(var, val);
		try {
			var.setVal(val);
			_solver.propagate();
		} catch (ContradictionException e) {
			_ibs.doAfterFail(var, val);
			throw e;
		}
		_ibs.doAfterPropagDownBranch(var, val);

	}

	@Override
	public void goDownBranch(final IntBranchingDecision ctx) throws ContradictionException {
		goDownBranch(ctx.getBranchingIntVar(), ctx.getBranchingValue());
	}

	@Override
	public void goUpBranch(final IntBranchingDecision decision) throws ContradictionException {
		decision.remIntVal();
	}

	@Override

	public String getDecisionLogMessage(IntBranchingDecision decision) {
		return getDefaultAssignMsg(decision);
	}



	public interface ImpactStrategy {
		/**
		 * return the impact of the variable var.
		 *
		 * @param var variable
		 * @return the value of the impact.
		 */
		public double getEnumImpactVar(IntDomainVar var);

		/**
		 * Only one impact is stored for a BoundIntVar (not an impact per value)
		 *
		 * @param var variable
		 * @return the value of the impact.
		 */
		public double getBoundImpactVar(IntDomainVar var);

		/**
		 * return the impact of the choice var == val.
		 *
		 * @param var variable
		 * @param val value
		 * @return the value of the impact.
		 */
		public double getImpactVal(IntDomainVar var, int val);

		public void doBeforePropagDownBranch(Object o, int i);

		public void doAfterPropagDownBranch(Object o, int i);


		public void doAfterFail(Object o, int i);
	}

	public abstract static class AbstractImpactStrategy implements ImpactStrategy {
		ImpactBasedBranching _branching;

		// The subset of variables on which impact are maintained and computed
		ArrayList svars;
		int nbVar;
		int sumDom = 0;
		ImpactStorage dataS;

		public AbstractImpactStrategy(ImpactBasedBranching branching, ArrayList subset) {
			svars = subset;
			_branching = branching;
			dataS = new ImpactStorage(_branching._solver, subset);
			nbVar = subset.size();
			for (Object svar : svars) {
				sumDom += ((IntDomainVar) svar).getDomainSize();
			}
		}

		public void setDataS(ImpactStorage dataS) {
			this.dataS = dataS;
		}



		//		/**
		//		 * Each value of each variable is tried to initialize
		//		 * impact. A pruning according to this singleton
		//		 * consistency phase's is done.
		//		 *
		//		 * @param timelimit limit to achieve the singleton algorithm
		//		 * @return true if no contradiction occured
		//		 */
		//		public boolean initImpacts(int timelimit) {
		//			if (timelimit != 0) {
		//				long tps = TimeCacheThread.currentTimeMillis;
		//				_branching._solver.generateSearchStrategy();
		//				_branching.setSolver(_branching._solver.getSearchStrategy());
		//				try {
		//					_branching._solver.propagate();
		//					_branching._solver.worldPush();
		//					for (int i = 0; i < svars.size(); i++) {
		//						//for (Object svar : svars) {
		//						IntDomainVar v = (IntDomainVar) svars.get(i);
		//						if (!v.isInstantiated() && v.hasEnumeratedDomain()) {
		//							DisposableIntIterator it = v.getDomain().getIterator();
		//							while (it != null && it.hasNext()) {
		//								int val = it.next();
		//								boolean cont = false;
		//								if (v.hasBooleanDomain() && val > v.getInf() && val < v.getSup())
		//									break;							
		//								_branching._solver.worldPush();
		//								try {
		//									_branching.goDownBranch(v, val);
		//								} catch (ContradictionException e) {
		//									cont = true;
		//								}
		//								_branching._solver.worldPop();
		//								if (cont) {
		//									_branching._solver.worldPop();
		//									try {
		//										v.remVal(val);
		//										_branching._solver.propagate();
		//									} catch (ContradictionException e) {
		//										return false;
		//									}
		//									_branching._solver.worldPush();								
		//								}
		//								if ((TimeCacheThread.currentTimeMillis - tps) > timelimit) {
		//									_branching._solver.worldPop();
		//									_branching._solver.getSearchStrategy().clearTrace();
		//									((CPSolver) _branching._solver).resetSearchStrategy();
		//									return true;
		//								}
		//							}
		//							it.dispose();
		//						}
		//
		//					}
		//					_branching._solver.worldPop();
		//				} catch (ContradictionException e) {
		//					return false;
		//				} catch (Exception e) {
		//					e.printStackTrace();
		//				}
		//				_branching._solver.getSearchStrategy().clearTrace();
		//				((CPSolver) _branching._solver).resetSearchStrategy();
		//			}
		//			return true;
		//
		//		}

		/**
		 * Each value of each variable is tried to initialize
		 * impact. A pruning according to this singleton
		 * consistency phase's is done.
		 *
		 * @param timelimit limit to achieve the singleton algorithm
		 * @return true if no contradiction occured
		 * @throws ContradictionException 
		 */
		public void initImpacts() throws ContradictionException {
			int timelimit = _branching._solver.getConfiguration().readInt(Configuration.INIT_IMPACT_TIME_LIMIT);
			if (timelimit > 0) {
				long tps = TimeCacheThread.currentTimeMillis;
				_branching._solver.propagate();
				_branching._solver.worldPush();
				for (int i = 0; i < svars.size(); i++) {
					//for (Object svar : svars) {
					IntDomainVar v = (IntDomainVar) svars.get(i);
					if (!v.isInstantiated() && v.hasEnumeratedDomain()) {
						DisposableIntIterator it = v.getDomain().getIterator();
						while (it.hasNext()) {
							int val = it.next();
							_branching._solver.worldPush();
							doBeforePropagDownBranch(v, val);
							try {
								v.setVal(val);
								_branching._solver.propagate();
								doAfterPropagDownBranch(v, val);
								_branching._solver.worldPop();
							} catch (ContradictionException e) {
								doAfterFail(v, val);
								_branching._solver.worldPop();
								v.remVal(val);
								_branching._solver.propagate();
								_branching._solver.worldPush();		
							}

							if ((TimeCacheThread.currentTimeMillis - tps) > timelimit) {
								_branching._solver.worldPop();
								return;
							}
						}
						it.dispose();
					}

				}
				_branching._solver.worldPop();
			}
		}


		protected static class ImpactStorage {

			public int[] offsets;
			public int[] sizes;

			/**
			 * in order to speed up the computation of the index of a tuple
			 * in the table, blocks[i] stores the sum of the domain sizes of variables j with j < i.
			 */
			public int[] blocks;

			public Solver pb;

			public ImpactStorage(ImpactStorage impst) {
				offsets = impst.offsets;
				sizes = impst.sizes;
				blocks = impst.blocks;
			}

			public ImpactStorage(Solver pb, ArrayList subset) {
				this.pb = pb;
				offsets = new int[subset.size()];
				sizes = new int[subset.size()];
				blocks = new int[subset.size()];
				if (!subset.isEmpty())
					blocks[0] = 0;
				for (int i = 0; i < subset.size(); i++) {
					IntDomainVar tv = (IntDomainVar) subset.get(i);
					tv.getExtension(ABSTRACTVAR_EXTENSION).set(i);
					if (tv.hasEnumeratedDomain()) {
						offsets[i] = tv.getInf();
						sizes[i] = tv.getSup() - tv.getInf() + 1; //((IntDomainVar) subset.get(i)).getDomainSize(); // tv.getSup() - tv.getInf() + 1;
					} else { // pour les variables sur bornes, on ne stocke que l'impact sur la variable pas sur la valeur
						offsets[i] = 0;
						sizes[i] = 1;
					}
					if (i > 0)
						blocks[i] = blocks[i - 1] + sizes[i - 1];
				}
			}

			public double computeCurrentTreeSize() {
				double prod = 1;
				for (int i = 0; i < pb.getNbIntVars(); i++) {
					prod *= pb.getIntVar(i).getDomainSize();
				}
				return prod;
			}

			public int getChoiceAddress(IntDomainVar var, int val) {
				int idx = var.getExtension(ABSTRACTVAR_EXTENSION).get();
				//int idx = ((IBSIntVarImpl) var).getIndex();
				return (blocks[idx] + val - offsets[idx]);
			}
		}

	}

	private static final class ImpactRef extends AbstractImpactStrategy {

		/**
		 * I(x_i = a) = 1 - Pafter(x_i = a) / Pbefore(x_i = a)
		 * High impacts (close to 1) denote high search space reductions
		 */
		protected double[] impact;

		/**
		 * the number of time a decision x_i = a is taken
		 */
		protected int[] nbDecOnVarVal;

		protected ImpactBasedBranching _branching;


		public ImpactRef(ImpactBasedBranching branching, IntDomainVar[] subset) {
			this(branching, new ArrayList<IntDomainVar>(Arrays.asList(subset)));
		}


		public ImpactRef(ImpactBasedBranching branching, ArrayList vars) {
			super(branching, vars);
			_branching = branching;
			int totalSize = 0;
			if (!vars.isEmpty())
				totalSize = dataS.blocks[vars.size() - 1] + dataS.sizes[vars.size() - 1];
			impact = new double[totalSize];
			nbDecOnVarVal = new int[totalSize];
			domBefore = new int[vars.size()];
			domAfter = new int[vars.size()];
		}

		public void addImpact(IntDomainVar v, int val, double value) {
			if (v.hasEnumeratedDomain())
				impact[dataS.getChoiceAddress(v, val)] += value;
			else impact[dataS.getChoiceAddress(v, 0)] += value;
		}

		public void updateSearchState(IntDomainVar var, int val) {
			if (var.hasEnumeratedDomain())
				nbDecOnVarVal[dataS.getChoiceAddress(var, val)] += 1;
			else nbDecOnVarVal[dataS.getChoiceAddress(var, 0)] += 1;
		}

		/**
		 * Return I(var = val)
		 */
		public double getImpactVal(IntDomainVar var, int val) {
			int idx = dataS.getChoiceAddress(var, val);
			if (nbDecOnVarVal[idx] > 0) {
				return impact[idx] / (double) nbDecOnVarVal[idx];
			} else
				return 0.0;
		}

		/**
		 * Return impact by giving directly the adress in the table
		 * @param idx index
		 * @return value
		 */
		public double getImpactVal(int idx) {
			if (nbDecOnVarVal[idx] > 0) {
				return impact[idx] / (double) nbDecOnVarVal[idx];
			} else
				return 0.0;
		}

		/**
		 * sum over each value of var, the remaining search space
		 *
		 * @param var variable
		 */
		public double getEnumImpactVar(IntDomainVar var) {
			int idx = var.getExtension(ABSTRACTVAR_EXTENSION).get();
			if (idx != -1) {
				double imp = 0.0;
				DisposableIntIterator it = var.getDomain().getIterator();
				int blockadress = dataS.blocks[idx] - dataS.offsets[idx];
				while (it.hasNext()) {
					int val = it.next();
					imp += 1 - getImpactVal(blockadress + val);
				}
				it.dispose();
				return imp;
			} else
				return 0;
		}

		public double getBoundImpactVar(IntDomainVar var) {
			int idx = var.getExtension(ABSTRACTVAR_EXTENSION).get();
			if (idx != -1) {
				return 1 - getImpactVal(var, 0);
			} else
				return 0;
		}

		// --------------- Computation of tree sizes  --------------------------- //

		protected int[] domBefore, domAfter;
		protected boolean flag = false;

		/**
		 * The sizes of the domains are stored before and after each choice.
		 * The search space reduction is then computed as the product of pAfter[i]/pBfore[i] for all i
		 * @param x variable
		 * @param val value
		 * @param pAfter domain size after the choice
		 * @param pBefore domain size before the choice
		 */
		public void computeSearchReduction(IntDomainVar x, int val, int[] pAfter, int[] pBefore) {
			double reduc = 1.0;
			for (int i = 0; i < pAfter.length; i++) {
				reduc *= (double) pAfter[i] / (double) pBefore[i];
			}
			reduc = 1.0 - reduc;
			addImpact(x, val, reduc);
		}

		public void computeCurrentDomSize(int[] domSizes) {
			for (int i = 0; i < domSizes.length; i++) {
				domSizes[i] = ((IntDomainVar) svars.get(i)).getDomainSize();
			}
		}

		public void doBeforePropagDownBranch(Object o, int i) {
			flag = ((IntDomainVar) o).getDomainSize() > 1;
			if (flag) {  // Once a domain is reduced to a singleton, choco still call the godownbranch method
				computeCurrentDomSize(domBefore);
				updateSearchState((IntDomainVar) o, i);
			}
		}

		public void doAfterPropagDownBranch(Object o, int i) {
			if (flag) {
				computeCurrentDomSize(domAfter);
				computeSearchReduction((IntDomainVar) o, i, domAfter, domBefore);
			}
		}

		public void doAfterFail(Object o, int i) {
			addImpact((IntDomainVar) o, i, 1.0);
		}
	}
}
