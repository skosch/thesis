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

package choco.visu;

import choco.IObservable;
import choco.IObserver;
import choco.cp.solver.search.AbstractSearchLoopWithRestart;
import choco.kernel.solver.propagation.event.VarEvent;
import choco.kernel.solver.search.ISearchLoop;
import choco.kernel.solver.search.IntBranchingTrace;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.visu.searchloop.ObservableStepSearchLoop;
import choco.visu.variables.VisuVariable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 23 oct. 2008
 * Since : Choco 2.0.1
 *
 * This class is the main observer of the visualization.
 * Every modification over variables are observed and send to
 * the specific vizualisation.
 *
 */
public final class Tracer implements IObserver {

	protected ArrayList<VisuVariable> vars;
	protected HashMap<Var, VisuVariable> mapvars;
	protected int breaklength;

	public Tracer() {
		this.breaklength = 10;
	}

	/**
	 * Change the break length value
	 * @param breaklength the new break length
	 */
	public final void setBreaklength(final int breaklength) {
		this.breaklength = breaklength;
	}

	/**
	 * Set the variable event queue to observe
	 *
	 * @param observable
	 */
	public final void addObservable(final IObservable observable) {
		observable.addObserver(this);
		observable.notifyObservers(null);
	}

	/**
	 * Set the variables to draw
	 *
	 * @param vars
	 */
	public final void setVariables(final Collection<VisuVariable> vars) {
		if(this.vars == null){
			this.vars = new ArrayList<VisuVariable>();
			mapvars = new HashMap<Var, VisuVariable>();
		}

        for (VisuVariable var : vars) {
            this.vars.add(var);
            mapvars.put(var.getSolverVar(), var);
        }
	}

	/**
	 * This method is called whenever the observed object is changed. An
	 * application calls an <tt>Observable</tt> object's
	 * <code>notifyObservers</code> method to have all the object's
	 * observers notified of the change.
	 *
	 * @param o   the observable object.
	 * @param arg an argument passed to the <code>notifyObservers</code>
	 *            method.
	 *            <p/>
	 *            In that case, it redraw the canvas of the modified variable
	 *            or redraw every canvas if "fail" (arg = 1).
	 */
	public final void update(final IObservable o, final Object arg) {
		haveBreak();
		if(arg instanceof VarEvent){
			VarEvent ve = (VarEvent)arg;
			VisuVariable v =mapvars.get(ve.getModifiedVar());
			if(v != null){
				v.refresh(ve.getEventType());
			}
		}else if(arg instanceof ISearchLoop){
			IntBranchingTrace ctx = ((AbstractSearchLoopWithRestart)((ObservableStepSearchLoop)arg)
                    .getInternalSearchLoop()).getCurrentTrace();
            if(ctx==null)return;
            Object ob = ctx.getBranchingObject();
            VisuVariable v = null;
            if(ob instanceof IntDomainVar){
                v = mapvars.get(ob);
            }else if (ob instanceof Object[]){
                v = mapvars.get(((Object[])ob)[0]);
            }
            if(v != null){
                v.refresh(arg);
            }
		}
	}

	/**
	 * Create a visual pause
	 */
	private void haveBreak(){
		try {
			Thread.sleep(this.breaklength);
		} catch (InterruptedException e) {
            // nothing to do
		}
	}
}
