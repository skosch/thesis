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

package choco.cp.solver.preprocessor;

import choco.Options;
import choco.cp.common.util.preprocessor.ExpressionTools;
import choco.cp.solver.CPModelToCPSolver;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.reified.ExpressionSConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.MetaConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.reified.BoolNode;
import gnu.trove.TLongObjectHashMap;

/* 
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: Aug 9, 2008
 * Since : Choco 2.0.0
 *
 */
public class PPModelToCPSolver extends CPModelToCPSolver {


	/**
	 * store if an expression constraint has already be been built for
	 * a model constraint.
	 */
	//This could be implemented as a hook on constraint ?
	protected TLongObjectHashMap<ExpressionSConstraint> knownExpressionCts;

	public PPModelToCPSolver(CPSolver cpsolver) {
		super(cpsolver);
		knownExpressionCts = new TLongObjectHashMap<ExpressionSConstraint>();
	}


	public void storeExpressionSConstraint(Constraint c, ExpressionSConstraint ic) {
		knownExpressionCts.put(c.getIndex(),ic);
	}

	/**
	 * The number of heavy extensional constraint posted
	 * using an expression
	 */
	protected int nbHeavyBin = 0;


	/**
	 * Override the creation of Expression Constraint as in the preprocessing
	 * they might have been built earlier or identified as an intensional
	 * constraint. 
	 * @param ic
	 * @param decomp
	 * @return
	 */
	protected SConstraint createMetaConstraint(Constraint ic, Boolean decomp) {
		try {
			ExpressionSConstraint c = knownExpressionCts.get(ic.getIndex());
			if (c == null) {
				c = new ExpressionSConstraint(super.buildBoolNode(ic));
			}
			c.setScope(cpsolver);
			c.setDecomposeExp(decomp);
			if (  decomp == null && ExpressionTools.toBeDecomposed(c)) {
				c.setDecomposeExp(true);
			} else if (ExpressionTools.isVeryBinaryHeavy(c)) {
				nbHeavyBin ++;
			}

			if (ic.getOptions().contains(Options.E_AC)) {
				c.setLevelAc(0);
			} else if (ic.getOptions().contains(Options.E_FC)) {
				c.setLevelAc(1);
			}else if (nbHeavyBin > 2000) {
				c.setLevelAc(1);
			} else c.setLevelAc(0);

			SConstraint intensional = c.getKnownIntensionalConstraint();
			if (intensional == null)
				intensional = ExpressionTools.getIntentionalConstraint(c, cpsolver);
			if (intensional != null)
				return intensional;
			else return c;
		} catch (ClassCastException cce) {
			//HACK
			LOGGER.info("createGenericMetaConstraint");
			return createGenericMetaConstraint((MetaConstraint) ic, decomp);
		}
	}



	// to make the builNode method accessible from the blackbox solver
	public BoolNode buildNode(Constraint ic) {
		return super.buildBoolNode(ic);
	}


	protected void readBBDecisionVariables() {
		super.readDecisionVariables();
	}
}
