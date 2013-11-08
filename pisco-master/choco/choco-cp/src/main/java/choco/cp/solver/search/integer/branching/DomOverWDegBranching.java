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

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.AbstractLargeIntBranchingStrategy;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.SConstraintType;
import choco.kernel.solver.constraints.integer.AbstractIntSConstraint;
import choco.kernel.solver.propagation.listener.PropagationEngineListener;
import choco.kernel.solver.search.IntBranchingDecision;
import choco.kernel.solver.search.ValIterator;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.variables.AbstractVar;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.integer.IntVar;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/* History:
 * 2008-04-23 : Creation : dom / wdeg needs to be a branching not just an heuristic to allow to deal with
 *              backtracking events !
 */
/**
 * WARNING ! This implementation suppose that the variables will not change. It copies all variables in an array
 * at the beginning !!
 * @deprecated use {@link choco.cp.solver.search.integer.branching.domwdeg.DomOverWDegBranchingNew}
 */
public class DomOverWDegBranching extends AbstractLargeIntBranchingStrategy implements PropagationEngineListener {
	protected static final int ABSTRACTCONTRAINT_EXTENSION =
		AbstractSConstraint.getAbstractSConstraintExtensionNumber("choco.cp.cpsolver.search.integer.varselector.DomOverWDeg");

    protected static final int ABSTRACTVAR_EXTENSION =
		AbstractVar.getAbstractVarExtensionNumber("choco.cp.cpsolver.search.integer.varselector.DomOverWDeg");

    // Les variables parmis lesquelles on veut brancher !
	private IntVar[] _vars;

	// L'heuristique pour le svaleurs
	private ValIterator _valIterator;

	// L'heuristique pour le svaleurs
	private ValSelector _valSelector;

	// Le solveur
	private Solver _solver;

	//a reference to a random object when random ties are wanted
	protected Random randomBreakTies;

	private AbstractSConstraint reuseCstr;

	// Le constructeur avec :
	// * le solver pour fournir les variables
	// * l'heuristique de valeurs pour instantier une valeur
	public DomOverWDegBranching(Solver s, ValIterator valHeuri, IntVar[] vars) {
		_solver = s;

        DisposableIterator<SConstraint> iter = s.getConstraintIterator();
        for (; iter.hasNext();) {
            AbstractSConstraint c = (AbstractSConstraint) iter.next();
            c.addExtension(ABSTRACTCONTRAINT_EXTENSION);
        }
        iter.dispose();
        for (int i = 0; i < s.getNbIntVars(); i++) {
			IntDomainVar v = s.getIntVar(i);
			v.addExtension(ABSTRACTVAR_EXTENSION);
		}

		for (Iterator it = s.getIntConstantSet().iterator(); it.hasNext();) {
			int val = (Integer) it.next();
			Var v = s.getIntConstant(val);
			v.addExtension(ABSTRACTVAR_EXTENSION);
		}

		s.getPropagationEngine().addPropagationEngineListener(this);
		// On sauvegarde l'heuristique
		_valIterator = valHeuri;
		_vars = vars;
	}

    /**
     * Define action to do just before a deletion.
     */
    @Override
    public void safeDelete() {
        _solver.getPropagationEngine().removePropagationEngineListener(this);
    }

    public DomOverWDegBranching(Solver s, ValIterator valHeuri) {
		this(s, valHeuri, buildVars(s));
	}


	public DomOverWDegBranching(Solver s, ValSelector valHeuri, IntVar[] vars) {
		_solver = s;

        DisposableIterator<SConstraint> iter = s.getConstraintIterator();
		for (; iter.hasNext();) {
			AbstractSConstraint c = (AbstractSConstraint) iter.next();
			c.addExtension(ABSTRACTCONTRAINT_EXTENSION);
		}
        iter.dispose();
		for (int i = 0; i < s.getNbIntVars(); i++) {
			IntDomainVar v = s.getIntVar(i);
			v.addExtension(ABSTRACTVAR_EXTENSION);
		}

		for (Iterator it = s.getIntConstantSet().iterator(); it.hasNext();) {
			int val = (Integer) it.next();
			Var v = s.getIntConstant(val);
			v.addExtension(ABSTRACTVAR_EXTENSION);
		}

		s.getPropagationEngine().addPropagationEngineListener(this);

		// On sauvegarde l'heuristique
		_valSelector = valHeuri;
		_vars = vars;
	}

	public DomOverWDegBranching(Solver s, ValSelector valHeuri) {
		this(s, valHeuri, buildVars(s));
	}

	private static IntVar[] buildVars(Solver s) {
		IntVar[] vars = new IntVar[s.getNbIntVars()];
		for (int i = 0; i < vars.length; i++) {
			vars[i] = s.getIntVar(i);
		}
		return vars;
	}


	public void initBranching() {
		int nb_variables = _vars.length;
		for (int variable_idx = 0; variable_idx < nb_variables; variable_idx++) {
			// On ajoute la variable et le poids
			IntVar v = _vars[variable_idx];// = _solver.getIntVar(variable_idx);

			// Pour etre sur, on verifie toutes les contraintes... au cas ou une d'entre elle serait deja instanti�e !!
			int weight = 0;
			DisposableIntIterator c = v.getIndexVector().getIndexIterator();
			int idx = 0;
			while (c.hasNext()) {
				idx = c.next();
				AbstractSConstraint cstr = (AbstractSConstraint) v.getConstraint(idx);
				if (cstr.getNbVarNotInst() > 1) {
					weight += cstr.getExtension(ABSTRACTCONTRAINT_EXTENSION).get() + cstr.getFineDegree(v.getVarIndex(idx));
				}
			}
            c.dispose();
			v.getExtension(ABSTRACTVAR_EXTENSION).set(weight);
		}
		//logWeights(ChocoLogging.getChocoLogger(), Level.INFO);
	}

	@Override
	public void initConstraintForBranching(SConstraint c) {
		c.addExtension(ABSTRACTCONTRAINT_EXTENSION);
	}

	public void setBranchingVars(IntVar[] vs) {
		_vars = vs;
	}

	public void setRandomVarTies(int seed) {
		randomBreakTies = new Random(seed);
	}

	public Object selectBranchingObject() throws ContradictionException {
		int previous_Size = -1;
		int previous_Weight = -1;
		IntVar previous_Variable = null;
		if (randomBreakTies == null) {
			for (int i = 0; i < _vars.length; i++) {
				IntDomainVar var = (IntDomainVar) _vars[i];
				/*if (var.isInstantiated()) continue;
				else*/
                if(!var.isInstantiated()) {
					if (previous_Variable == null) {
						previous_Variable = var;
						previous_Size = var.getDomainSize();
						previous_Weight = var.getExtension(ABSTRACTVAR_EXTENSION).get();
					} else {
						if (( var.getExtension(ABSTRACTVAR_EXTENSION)).get()
								* previous_Size - previous_Weight * var.getDomainSize() > 0) {
							previous_Variable = var;
							previous_Size = var.getDomainSize();
							previous_Weight = var.getExtension(ABSTRACTVAR_EXTENSION).get();
						}
					}
				}
			}
			return previous_Variable;
		} else {
			//redondant code with previous case, really ugly.
			List<IntDomainVar> lvs = new LinkedList<IntDomainVar>();
			for (int i = 0; i < _vars.length; i++) {
				IntDomainVar var = (IntDomainVar) _vars[i];
				/*if (var.isInstantiated()) continue;
				else{*/
                     if (!var.isInstantiated()) {
					if (previous_Variable == null) {
						previous_Variable = var;
						previous_Size = var.getDomainSize();
						previous_Weight = var.getExtension(ABSTRACTVAR_EXTENSION).get();
						lvs.add(var);
					} else {
						int note = var.getExtension(ABSTRACTVAR_EXTENSION).get()
						* previous_Size - previous_Weight * var.getDomainSize();
						if (note > 0) {
							lvs.clear();
							lvs.add(var);
							previous_Size = var.getDomainSize();
							previous_Weight = var.getExtension(ABSTRACTVAR_EXTENSION).get();
						} else if (note >= 0) {
							lvs.add(var);
						}

					}
				}
			}
			if (lvs.isEmpty()) return null;
			return lvs.get(randomBreakTies.nextInt(lvs.size()));
		}
	}

	IntDomainVar v;
	public void setFirstBranch(final IntBranchingDecision decision) {
		v = decision.getBranchingIntVar();
        DisposableIterator<SConstraint> iter = v.getConstraintsIterator();
		for (; iter.hasNext();) {
			reuseCstr = (AbstractSConstraint) iter.next();
			if (SConstraintType.INTEGER.equals(reuseCstr.getConstraintType())) {
				if (reuseCstr.getNbVarNotInst() == 2) {
					for (int k = 0; k < reuseCstr.getNbVars(); k++) {
						AbstractVar var = (AbstractVar) ((AbstractIntSConstraint) reuseCstr).getVar(k);
						if (var != v && !var.isInstantiated()) {
							var.getExtension(ABSTRACTVAR_EXTENSION).add(-reuseCstr.getExtension(ABSTRACTCONTRAINT_EXTENSION).get());
						}
					}
				}
			}
		}
        iter.dispose();
		if (_valIterator != null) {
			decision.setBranchingValue( _valIterator.getFirstVal(v));
		} else {
			decision.setBranchingValue( _valSelector.getBestVal(v));
		}
	}

	public void setNextBranch(final IntBranchingDecision decision) {
		if (_valIterator != null) {
			decision.setBranchingValue( _valIterator.getNextVal( decision.getBranchingIntVar(), decision.getBranchingValue()));
		} else {
			decision.setBranchingValue( _valSelector.getBestVal(decision.getBranchingIntVar()));
		}
	}

	public boolean finishedBranching(final IntBranchingDecision decision) {
		if (_valIterator != null) {
			v = decision.getBranchingIntVar();
			final boolean finished = !_valIterator.hasNextVal(v, decision.getBranchingValue());
			if (finished) {
                DisposableIterator<SConstraint> iter = v.getConstraintsIterator();
				for (; iter.hasNext();) {
					reuseCstr = (AbstractSConstraint) iter.next();
					if (SConstraintType.INTEGER.equals(reuseCstr.getConstraintType())) {
						if (reuseCstr.getNbVarNotInst() == 2) {
							for (int k = 0; k < reuseCstr.getNbVars(); k++) {
								AbstractVar var = (AbstractVar) ((AbstractIntSConstraint) reuseCstr).getVar(k);
								if (var != v && !var.isInstantiated()) {
									var.getExtension(ABSTRACTVAR_EXTENSION).add(reuseCstr.getExtension(ABSTRACTCONTRAINT_EXTENSION).get());
								}
							}
						}
					}
				}
                iter.dispose();
			}
			return finished;
		} else {
			//return _valSelector.getBestVal((IntDomainVar) x) == null;
			return false;
		}
	}
	public void goDownBranch(final IntBranchingDecision decision) throws ContradictionException {
		decision.setIntVal();
	}
	public void goUpBranch(final IntBranchingDecision decision) throws ContradictionException {
		//decision.remIntVal();     // On le retire !! mais attention pas de selector pour les variables du coup !!!!
		//The weights are updated for the current branching object in setFirstBranch and finishedBranching.
		//We cant use a selector yet because the condition in finishedBranching is never activated and the weights become inconsistent.
	
	}
	
	

	@Override
	public String getDecisionLogMessage(IntBranchingDecision decision) {
		return decision.getBranchingObject() + LOG_DECISION_MSG_ASSIGN + decision.getBranchingValue();
	}

	public void contradictionOccured(ContradictionException e) {
		Object cause = e.getDomOverDegContradictionCause();
		if (cause != null) {
			reuseCstr = (AbstractSConstraint) cause;
			if (SConstraintType.INTEGER.equals(reuseCstr.getConstraintType())) {
				try {
					reuseCstr.getExtension(ABSTRACTCONTRAINT_EXTENSION).increment();
				} catch (NullPointerException npe) {
					// If there was a postCut, the extension has not been generated at the Branching creation
					reuseCstr.addExtension(ABSTRACTCONTRAINT_EXTENSION);
					reuseCstr.getExtension(ABSTRACTCONTRAINT_EXTENSION).increment();
				}
				for (int k = 0; k < reuseCstr.getNbVars(); k++) {
					AbstractVar var = (AbstractVar) ((AbstractIntSConstraint) reuseCstr).getVar(k);
					var.getExtension(ABSTRACTVAR_EXTENSION).increment();
				}
			}
		}
	}

	protected static void appendConstraint( StringBuilder b, SConstraint c) {
		final AbstractSConstraint cstr = (AbstractSConstraint) c;
		b.append("w=").append( cstr.getExtension(ABSTRACTCONTRAINT_EXTENSION).get());
		b.append('\t').append(cstr.pretty());
		b.append('\n');
	}

	protected static void appendVariable( StringBuilder b, Var v) {
		AbstractVar var = (AbstractVar) v;
		b.append("w=").append( var.getExtension(ABSTRACTVAR_EXTENSION).get());
		b.append('\t').append(var.pretty());
		b.append('\n');
	}
	
	public final void logWeights(Logger logger, Level level) {
		if(logger.isLoggable(level)) {
			final StringBuilder b = new StringBuilder(20);
			b.append("===> Display DomWDeg weights\n");
			b.append("\n###\tConstraints\t###\n");
            DisposableIterator<SConstraint> iter = _solver.getConstraintIterator();
			for (; iter.hasNext();) {
				appendConstraint(b, iter.next());
			}
            iter.dispose();
			b.append("\n###\tVariables\t###\n");
			for (int i = 0; i < _solver.getNbIntVars(); i++) {
				appendVariable(b, _solver.getIntVar(i));
			}
			b.append("\n###\tConstants\t###\n");
			for (Iterator<Integer> it = _solver.getIntConstantSet().iterator(); it.hasNext();) {
				appendVariable(b, _solver.getIntConstant(it.next()));
			}
			b.append("<=== End Display DomWDeg weights\n");
			logger.log(level, new String(b));
		}
	}
	
}
