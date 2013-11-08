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

package choco.cp.solver.search.integer.branching.domwdeg;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.solver.Solver;
import choco.kernel.solver.branch.Extension;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.AbstractVar;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Iterator;

import static choco.kernel.solver.constraints.SConstraintType.INTEGER;
/**
 * computation of the weighted degrees of variables and number of failures of constraints
 * @author Arnaud Malapert</br> 
 * @since 24 mars 2010 version 2.1.1</br>
 * @version 2.1.1</br>
 */
public class DomWDegUtils {


	//*****************************************************************//
	//*********** computation of weighted degrees and failures ****//
	//***************************************************************//

	private static final int ABSTRACTCONTRAINT_EXTENSION = AbstractSConstraint.getAbstractSConstraintExtensionNumber("choco.cp.cpsolver.search.integer.varselector.DomOverWDeg");

	private static final int ABSTRACTVAR_EXTENSION = AbstractVar.getAbstractVarExtensionNumber("choco.cp.cpsolver.search.integer.varselector.DomOverWDeg");


	private DomWDegUtils() {
		super();
	}

	public static void addConstraintExtension(SConstraint<?> c) {
		c.addExtension(ABSTRACTCONTRAINT_EXTENSION);
	}

	public static void addVariableExtension(Var v) {
		v.addExtension(ABSTRACTVAR_EXTENSION);
	}

	public static Extension getConstraintExtension(SConstraint<?> c) {
		return 	c.getExtension(ABSTRACTCONTRAINT_EXTENSION);
	}

	public static Extension getVarExtension(Var v) {
		return 	v.getExtension(ABSTRACTVAR_EXTENSION);
	}

	public static int getFineDegree(Var v, SConstraint<?> c, int cIdx) {
		return c.getFineDegree(v.getVarIndex(cIdx));
	}

	public static void initConstraintExtensions(Solver s) {
		DisposableIterator<SConstraint> iter = s.getConstraintIterator();
		for (; iter.hasNext();) {
			addConstraintExtension(iter.next());
		}
		iter.dispose();
	}

	public static void initVarExtensions(Solver s) {
		final DisposableIterator<IntDomainVar> iter = s.getIntVarIterator();
		while(iter.hasNext()) {
			addVariableExtension(iter.next());
		}
		iter.dispose();
		for (Iterator<Integer> it = s.getIntConstantSet().iterator(); it.hasNext();) {
			addVariableExtension(s.getIntConstant(it.next()));
		}
		iter.dispose();
	}

	public static boolean hasAtLeastTwoNotInstVars(SConstraint<?> c) {
		final int n = c.getNbVars();
		boolean hasNotInst = false;
		for (int i = 0; i < n; i++) {
			if( ! c.getVarQuick(i).isInstantiated()) {
				if(hasNotInst) {
					return true;
				}
				else hasNotInst = true;
			}
		}
		return false;
	}

	protected static boolean hasTwoNotInstVars(SConstraint<?> c) {
		final int n = c.getNbVars();
		int cpt = -2;
		for (int i = 0; i < n; i++) {
			if( ! c.getVarQuick(i).isInstantiated()) {
				cpt++;
				if(cpt>0) return false;
			}
		}
		return cpt == 0;
	}

	public final static int computeWeightedDegreeFromScratch(Var var) {
		int weight = 0;
		final DisposableIntIterator iter= var.getIndexVector().getIndexIterator();
		while (iter.hasNext()) {
			final int idx = iter.next();
			final SConstraint<?> c = var.getConstraint(idx);
			if ( INTEGER.isTypeOf(c)  && hasAtLeastTwoNotInstVars(c)) {
				try {
					weight+= getConstraintExtension(c).get() + getFineDegree(var, c, idx);
				} catch (NullPointerException npe) {
					// If there was a postCut, the extension has not been generated at the Branching creation
					addConstraintExtension(c);
					weight+= getConstraintExtension(c).get() + getFineDegree(var, c, idx);
				}
			}
		}
		iter.dispose();
		return weight;
	}

	public static void addFailure(SConstraint<?> cause) {
		if(cause != null) {
			if(INTEGER.isTypeOf(cause)) {
				try {
					getConstraintExtension(cause).increment();
				} catch (NullPointerException npe) {
					// If there was a postCut, the extension has not been generated at the Branching creation
					addConstraintExtension(cause);
					getConstraintExtension(cause).increment();
				}
			}
		}
	}

	public static void addConstraintToVarWeights(SConstraint<?> c) {
		final int nbF = getConstraintExtension(c).get();
		final int n = c.getNbVars();
		for (int k = 0; k < n; k++) {
			if(getVarExtension(c.getVarQuick(k)) == null){
                addVariableExtension(c.getVarQuick(k));
            }
            getVarExtension(c.getVarQuick(k)).add( c.getFineDegree(k) + nbF);
		}
	}
	
	public static void addIncFailure(SConstraint<?> cause) {
		if(cause != null) {
			if( INTEGER.isTypeOf(cause)) {
				try {
					getConstraintExtension(cause).increment();
					final int n = cause.getNbVars();
					for (int k = 0; k < n; k++) {
						getVarExtension(cause.getVarQuick(k)).increment();
					}
				} catch (NullPointerException npe) {
					// If there was a postCut, the extension has not been generated at the Branching creation
					//final AbstractSConstraint reuseCstr = (AbstractSConstraint) cause;
					addConstraintExtension(cause);
					getConstraintExtension(cause).increment();
					addConstraintToVarWeights(cause);
				}
			}
		}
	}

	public static final String getConstraintFailures(Solver solver) {
		final StringBuilder b = new StringBuilder();
		final DisposableIterator<SConstraint> iter = solver.getConstraintIterator();
		while(iter.hasNext() ) {
			final SConstraint<?> cstr = iter.next();
			b.append("failures=").append(getConstraintExtension(cstr).get());
			b.append("\t").append(cstr.pretty());
			b.append('\n');
		}
		iter.dispose();
		return new String(b);

	}

	public static final String getVariableWDeg(Solver solver) {
		final StringBuilder b = new StringBuilder();
		final DisposableIterator<IntDomainVar> iter = solver.getIntVarIterator();
		while(iter.hasNext() ) {
			final IntDomainVar v = iter.next();
			b.append("wdeg=").append(computeWeightedDegreeFromScratch(v));
			b.append("\t").append(v.pretty());
			b.append('\n');
		}
		iter.dispose();
		return new String(b);
	}

	public static final String checkVariableIncWDeg(Solver solver) {
		final StringBuilder b = new StringBuilder();
		final DisposableIterator<IntDomainVar> iter = solver.getIntVarIterator();
		while(iter.hasNext() ) {
			final IntDomainVar v = iter.next();
			int w1 = computeWeightedDegreeFromScratch(v);
			int w2 = getVarExtension(v).get();
			if(w1 != w2) {
			b.append("wdeg=").append(w2).append(" vs ").append(w1);
			b.append("\t").append(v.pretty());
			b.append('\n');
			}
		}
		iter.dispose();
		return new String(b);
	}
	
	public static final String getVariableIncWDeg(Solver solver) {
		final StringBuilder b = new StringBuilder();
		final DisposableIterator<IntDomainVar> iter = solver.getIntVarIterator();
		while(iter.hasNext() ) {
			final IntDomainVar v = iter.next();
			b.append("wdeg=").append(getVarExtension(v).get());
			b.append("\t").append(v.pretty());
			b.append('\n');
		}
		iter.dispose();
		return new String(b);
	}



}
