package parser.instances;

import static parser.instances.BasicSettings.*;
import java.util.ArrayList;
import java.util.List;

import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.BoundAllDiff;
import choco.cp.solver.constraints.global.matching.AllDifferent;
import choco.cp.solver.constraints.global.scheduling.cumulative.Cumulative;
import choco.cp.solver.constraints.integer.DistanceXYC;
import choco.cp.solver.constraints.integer.DistanceXYZ;
import choco.cp.solver.constraints.integer.IntLinComb;
import choco.cp.solver.constraints.integer.bool.BoolIntLinComb;
import choco.cp.solver.constraints.reified.ReifiedIntSConstraint;
import choco.cp.solver.search.BranchingFactory;
import choco.cp.solver.search.integer.branching.AssignVar;
import choco.cp.solver.search.integer.branching.ImpactBasedBranching;
import choco.cp.solver.search.integer.valiterator.DecreasingDomain;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.valselector.MaxVal;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.ConstraintType;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.search.ValIterator;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

public final class AdvancedBranchingFactory {


	public static enum Branching {
		RAND("Random"),
		LEX("Lexicographic"),
		DOM("MinDomain"),
		DDEG("Dom/DDEG"),
		WDEG("Dom/WDEG"),
		BWDEG("Bin-Dom/WDEG"),
		IMPACT("ImpactBasedSearch"),
		AUTO("Versatile"),
		USER("UserSearch");

		private final String name;

		private Branching(String name) {
			this.name = name;
		}
	}

	private static boolean isNaryExtensional(Solver solver) {
		return solver.getModel().getNbConstraintByType(ConstraintType.TABLE) > 0;
	}

	private static boolean isSat(Solver solver) {
		return solver.getModel().getNbConstraintByType(ConstraintType.CLAUSES) > 0;
	}

	private static boolean isScheduling(Solver solver) {
		return solver.getModel().getNbConstraintByType(ConstraintType.DISJUNCTIVE) > 0;
	}



	private static boolean isMixedScheduling(Solver solver) {
		final Model mod = solver.getModel();
		return mod.getNbConstraintByType(ConstraintType.DISJUNCTIVE) +
				mod.getNbConstraintByType(ConstraintType.PRECEDENCE_DISJOINT) +
				mod.getNbConstraintByType(ConstraintType.LEQ) +
				mod.getNbConstraintByType(ConstraintType.LT) +
				mod.getNbConstraintByType(ConstraintType.GT) +
				mod.getNbConstraintByType(ConstraintType.GEQ) !=
				mod.getNbConstraints();
	}

	private static IntDomainVar[] getOtherVars(CPSolver s) {
		List<IntDomainVar> ldvs = new ArrayList<IntDomainVar>(s.getNbIntVars());
		for (int i = 0; i < s.getNbIntVars(); i++) {
			IntDomainVar v = s.getIntVar(i);
			if (!v.hasBooleanDomain()) {
				ldvs.add(v);
			}
		}
		return ldvs.toArray(new IntDomainVar[ldvs.size()]);
	}


	private static boolean useRandomValue(Solver s) {
		try {
			return s.getConfiguration().readBoolean(BasicSettings.RANDOM_VALUE);
		} catch (NullPointerException e) {
			return false;
		}
	}
	
	private static boolean useMinValue(Solver s) {
		try {
			return s.getConfiguration().readBoolean(BasicSettings.MIN_VALUE);
		} catch (NullPointerException e) {
			return true;
		}
	}

	private static ValSelector<IntDomainVar> createValSelector(Solver s) {
		//s.getConfiguration().list(System.out);
		if(useRandomValue(s)) {
			final long seed = s.getConfiguration().readLong(Configuration.RANDOM_SEED);
			return new RandomIntValSelector(seed);
		} else if  (useMinValue(s)) {
			return new MinVal();
		} else return new MaxVal();
	}


	public static Branching lexicographic(Solver s) {
		s.addGoal(BranchingFactory.lexicographic(s, s.getIntDecisionVars(), createValSelector(s)));
		return Branching.LEX;
	}

	public static Branching minDom(Solver s) {
		s.addGoal(new AssignVar( new MinDomain(s, s.getIntDecisionVars()), createValSelector(s)));
		return Branching.DOM;
	}

	public static Branching random(Solver s) {
		final long seed = s.getConfiguration().readLong(Configuration.RANDOM_SEED);
		s.addGoal(new AssignVar( new RandomIntVarSelector(s, s.getIntDecisionVars(), seed), createValSelector(s)));
		return Branching.RAND;
	}

	public static Branching domDDeg(Solver s) {
		s.addGoal(BranchingFactory.domDDeg(s, createValSelector(s)));
		return Branching.DDEG;
	}

	public static Branching domWDegBin(CPSolver s) {
		if (s.getNbIntConstraints() == 1) {
			return Branching.AUTO;
		} else {
			ValSelector<IntDomainVar> valSel = createValSelector(s);
			if (isScheduling(s)) {
				if (isMixedScheduling(s)) { 
					//side constraints added
					s.addGoal(BranchingFactory.incDomWDegBin(s, ArrayUtils.append(s.getBooleanVariables(), getOtherVars(s)),valSel));
				} else {
					//pure scheduling
					s.addGoal(BranchingFactory.incDomWDegBin(s,s.getBooleanVariables(), valSel));
					AssignVar dwd2 = BranchingFactory.minDomMinVal(s, getOtherVars(s));
					s.addGoal(dwd2);
				}
			} else {                        
				//general case
				s.addGoal(BranchingFactory.incDomWDegBin(s, valSel));
			}
			return Branching.BWDEG;
		}
	}

	public static Branching domWDeg(CPSolver s) {
		if (s.getNbIntConstraints() == 1) {
			return Branching.AUTO;
		} else {
			if(useRandomValue(s)) {
				return domWDegBin(s);
			} else {
				final ValIterator<IntDomainVar> valSel = useMinValue(s) ? new IncreasingDomain() : new DecreasingDomain();
				if (isScheduling(s)) {
					if (isMixedScheduling(s)) { 
						//side constraints added
						s.addGoal(BranchingFactory.incDomWDeg(s, ArrayUtils.append(s.getBooleanVariables(), getOtherVars(s)),valSel));
					} else {
						//pure scheduling
						s.addGoal(BranchingFactory.incDomWDeg(s,s.getBooleanVariables(), valSel));
						AssignVar dwd2 = BranchingFactory.minDomMinVal(s, getOtherVars(s));
						s.addGoal(dwd2);
					}
				} else {                        
					//general case
					s.addGoal(BranchingFactory.incDomWDeg(s, valSel));
				}
				return Branching.WDEG;
			}
		}
	}

	public static Branching impact(CPSolver s) {
		ImpactBasedBranching ibb;
		AssignVar dwd2 = null;
		if (isScheduling(s)) {
			final IntDomainVar[] bvs = s.getBooleanVariables();
			final IntDomainVar[] ovs = getOtherVars(s);
			if (!isMixedScheduling(s)) { 
				//pure scheduling
				ibb = new ImpactBasedBranching(s, bvs);
				dwd2 = BranchingFactory.minDomIncDom(s, ovs);
			} else {                    
				//side constraints added
				ibb = new ImpactBasedBranching(s, ArrayUtils.append(s.getBooleanVariables(), getOtherVars(s)));
			}
		} else {                       
			//general case
			ibb = new ImpactBasedBranching(s);
		}

		if(useRandomValue(s)) {
			ibb.setRandomValueChoice(s.getConfiguration().readLong(Configuration.RANDOM_SEED));
		}
		s.addGoal(ibb);
		if(dwd2 != null) s.addGoal(dwd2);
		return Branching.IMPACT;
	}


	public interface IGoalManager {

		Branching addGoals(Solver s);
	}

	public interface IVersatileMode{

		Branching determineSearchHeuristics(Solver s);
	}	

	private static Branching _addGoals(CPSolver s, Branching branching, IGoalManager userGoals) {
		switch (branching) {
		case RAND:return random(s);
		case LEX: return lexicographic(s);
		case DOM: return minDom(s);
		case DDEG: return domDDeg(s);
		case WDEG: return domWDeg(s);
		case BWDEG: return domWDegBin(s);
		case IMPACT: return impact(s);
		case AUTO: return Branching.AUTO; //Beware : potential loop
		case USER: return userGoals.addGoals(s);
		default :
			throw new SolverException("Unknown Search Heuristics: "+branching);
		}

	}

	public static Branching addGoals(CPSolver s, Branching branching, IVersatileMode versatile, IGoalManager userGoals) {
		Branching chosen = branching == Branching.AUTO ? 
				versatile.determineSearchHeuristics(s) : branching;
		Branching applied = _addGoals(s, chosen, userGoals);
		if(applied == Branching.AUTO) {
			if(branching == Branching.AUTO) {
				//We have determined a branching which is not relevant : switch to minDom
				applied = _addGoals(s, Branching.DOM, userGoals);
			}
			else {
				//the specified branching : switch to versatile
				chosen = versatile.determineSearchHeuristics(s);
				applied = _addGoals(s, chosen, userGoals);
				if(applied == Branching.AUTO) {
					//We have determined a branching which is not relevant : switch to minDom
					applied = _addGoals(s, Branching.DOM, userGoals);
				}
			}
		}
		if(applied == Branching.AUTO) {
			throw new SolverException("Failed to set a search heuristics");
		}
		return applied;
	}

	public static Branching addGoals(CPSolver s, Branching branching, IGoalManager userGoals) {
		return addGoals(s, branching, DefaultVersatileMode.SINGLOTON, userGoals);
	}

	public static Branching addGoals(CPSolver s, Branching branching, IVersatileMode versatile) {
		return addGoals(s, branching, versatile, DefaultUserGoals.SINGLOTON);
	}

	public static Branching addGoals(CPSolver s, Branching branching) {
		return addGoals(s, branching, DefaultVersatileMode.SINGLOTON, DefaultUserGoals.SINGLOTON);
	}

	public static Branching addGoals(CPSolver s) {
		return addGoals(s, Branching.AUTO, DefaultVersatileMode.SINGLOTON, DefaultUserGoals.SINGLOTON);
	}

	private static class DefaultUserGoals implements IGoalManager {

		public final static DefaultUserGoals SINGLOTON = new DefaultUserGoals();

		private DefaultUserGoals() {
			super();
		}

		@Override
		public Branching addGoals(Solver s) {
			throw new SolverException("No user search heuristics");
		}
	}

	private static class DefaultVersatileMode implements IVersatileMode {

		public final static DefaultVersatileMode SINGLOTON = new DefaultVersatileMode();

		private DefaultVersatileMode() {
			super();
		}

		/**
		 * @return {@link Branching#BWDEG} or {@link Branching#IMPACT} depending on the nature of the problem
		 */
		@Override
		public Branching determineSearchHeuristics(Solver s) {
			DisposableIterator<SConstraint> it = s.getConstraintIterator();
			Branching heuristic = Branching.BWDEG;
			if (isSat(s)) return Branching.IMPACT; //degree is unrelevant using the clause propagator
			if (isNaryExtensional(s)) {
				return Branching.BWDEG;
			}
			while(it.hasNext()) {
				SConstraint constraint = it.next();
				if (constraint instanceof Cumulative) return Branching.IMPACT;
				if (constraint instanceof AllDifferent) return Branching.IMPACT;
				if (constraint instanceof BoundAllDiff) {
					if (constraint.getNbVars() > 10) {
						heuristic = Branching.IMPACT;
					}
				}
				if (constraint instanceof ReifiedIntSConstraint) return Branching.IMPACT;
				if (constraint instanceof IntLinComb ||
						constraint instanceof BoolIntLinComb) {
					int arity = constraint.getNbVars();
					if (arity >= 6) {
						return Branching.IMPACT;
					}
				}
				if (constraint instanceof DistanceXYZ) return Branching.BWDEG;
				if (constraint instanceof DistanceXYC) return Branching.BWDEG;
			}
			it.dispose();
			if (getSumOfDomains(s) > 500000) {
				return Branching.BWDEG;
			}
			return heuristic;
		}

		private int getSumOfDomains(Solver s) {
			int sum = 0;
			for (int i = 0; i < s.getNbIntVars(); i++) {
				sum += s.getIntVar(i).getDomainSize();

			}
			return sum;
		}
	}
		
}


