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

package choco.kernel.solver.search.checker;

import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.integer.AbstractIntSConstraint;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.logging.Level;



public class SolutionCheckerEngine extends AbstractSolutionCheckerEngine {
	
	public boolean enableConsistency;

    private String reuseLabel;


	public SolutionCheckerEngine() {
		super();
		enableConsistency = true;
	}


    /**
     * Return if the constraints checking enables consistency.
     * @return true if consistency is checked.
     */
	public final boolean isConsistencyEnabled() {
		return enableConsistency;
	}


    /**
     * Enable consistency for constraints checking.
     * @param enableConsistency true if constraint consistency must be check
     */
	public final void setEnableConsistency(final boolean enableConsistency) {
		this.enableConsistency = enableConsistency;
	}


    /**
     * Inspect the instantiation of {@code var}.
     * @param var variable to check
     * @return false if the variable is not instantiated.
     */
	public boolean inspectVariable(final Var var) {
		if ( ! var.isInstantiated() ) {
			if(LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(Level.SEVERE, "  FAILURE Not Instantiated: {0}",var.pretty());
			}
			return false;
		}
		return true;
	}

    /**
     * Check the instantiation of {@code var}.
     * @param var variable to check
     * @throws SolutionCheckerException if {@code var} is not instantiated.
     */
	public void checkVariable(final Var var) throws SolutionCheckerException {
		if ( ! var.isInstantiated() ) {
			throw new SolutionCheckerException("FAILURE Not Instantiated: "+var.pretty());
		}
	}

    /**
     * Check the satisfaction of {@code c}.
     * @param c constraint to check
     * @throws SolutionCheckerException if {@code c} is not satisfied
     */
	public void checkConstraint(final SConstraint c) throws SolutionCheckerException {
		if( ! isSatisfied(c)) {
			throw new SolutionCheckerException("FAILURE "+reuseLabel+": "+c.pretty());
		}
		
	}

    /**
     * Inspect the satisfaction of {@code c}.
     * @param c contraint to check
     * @return false if the constraint is not satisfied.
     */
	public boolean inspectConstraint(final SConstraint<?> c) {
		if( isSatisfied(c)) {
			if(LOGGER.isLoggable(Level.FINEST)) {
				LOGGER.log(Level.FINEST, "  {0}: {1}", new Object[]{reuseLabel, c.pretty()});
			}
			return true;
		}else if(LOGGER.isLoggable(Level.INFO)) {
			LOGGER.log(Level.INFO, "  FAILURE {0}: {1}({2})", new Object[]{reuseLabel, c.pretty(), c.getClass().getSimpleName()});
		}
		return false;
	}

    /**
     * Check if {@code c} is satisfied.
     * @param c constraint to check.
     * @return false if the {@code c} is not satisfied.
     */
	protected boolean isSatisfied(final SConstraint<?> c) {
		boolean isOk;
		if(c instanceof AbstractIntSConstraint){
			try{
				isOk = isSatisfied( (AbstractIntSConstraint) c);
			}catch (UnsupportedOperationException e){
				LOGGER.log(Level.WARNING,"- Check solution: isSatisified(int[]) is not implemented: {0}", c);
				reuseLabel = "is isSatisified()";
				isOk = c.isSatisfied();
			}
		}else{
			reuseLabel = "is isSatisfied()";
			isOk = c.isSatisfied();
		}
		return isOk;
	}

    /**
     * Check isSatisfied on {@code ic}.
     * {@code ic} is an integer constraint and allows consistency checking (with tuple).
     * @param ic integer constraint to check
     * @return false if the {@code c} is satisfied
     * @throws UnsupportedOperationException if {@code ic} doesn't allow consistency checking.
     */
	protected boolean isSatisfied(final AbstractIntSConstraint ic)throws UnsupportedOperationException  {
		final int[] tuple = new int[ic.getNbVars()];
		final int[] tupleL = new int[ic.getNbVars()];
		final int[] tupleU = new int[ic.getNbVars()];
		boolean fullInstantiated = true;
		for(int i = 0; i < ic.getNbVars(); i++){
			final IntDomainVar v = ic.getVar(i);
			if(v.isInstantiated()){
				tuple[i] = v.getVal();
				tupleL[i] = tuple[i];
				tupleU[i] = tuple[i];
			}else{
				fullInstantiated = false;
				tupleL[i] = v.getInf();
				tupleU[i] = v.getSup();
			}
		}
		boolean isOk = true;
		if(fullInstantiated){
			reuseLabel = "Fully Instantiated isSatisified(int[])"; 
			isOk = ic.isSatisfied(tuple);
		}else if(enableConsistency){
			reuseLabel = "Consistency isSatisfied(int[])";
			isOk = ic.isSatisfied(tupleL) && ic.isSatisfied(tupleU);
		} else {
			reuseLabel= "No Consistency Test";
		}
		return isOk;
	}

}
