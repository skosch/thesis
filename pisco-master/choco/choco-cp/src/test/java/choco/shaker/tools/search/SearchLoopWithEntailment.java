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

package choco.shaker.tools.search;

import choco.kernel.solver.propagation.Propagator;
import choco.kernel.solver.search.AbstractGlobalSearchStrategy;
import choco.kernel.solver.search.AbstractSearchLoop;

/*
 * User : charles
 * Mail : cprudhom(a)emn.fr
 * Date : 11 mars 2009
 * Since : Choco 2.0.1
 * Update : Choco 2.0.1
 * 
 * SearchLoop with entailment checker
 */
public class SearchLoopWithEntailment extends AbstractSearchLoop {

	Propagator p;
	
	public Boolean entail = null;

	public final AbstractSearchLoop searchLoop;

    private int previousNbSolutions;

	public SearchLoopWithEntailment(AbstractGlobalSearchStrategy searchStrategy, Propagator propagator) {
		super(searchStrategy);
		this.searchLoop = (AbstractSearchLoop) searchStrategy.searchLoop;
		this.p = propagator;
	}

	
	
	@Override
	public void downBranch() {
		searchLoop.downBranch();
		checkEntailment();
		
	}

	@Override
	public Boolean endLoop() {
		return searchLoop.endLoop();
	}

	@Override
	public void initLoop() {
		searchLoop.initLoop();
        checkEntailment();
	}

	@Override
	public void initSearch() {
		searchLoop.initSearch();
	}

	@Override
	public void openNode() {
		searchLoop.openNode();
        if(searchStrategy.getSolutionCount() > previousNbSolutions) {
            previousNbSolutions++;
            stop = true;
		}
	}

	@Override
	public void restart() {
		searchLoop.restart();
//		checkEntailment();
	}

	@Override
	public void upBranch() {
		searchLoop.upBranch();
        if(searchStrategy.isTraceEmpty()){
            stop = true;
        }
		checkEntailment();
		
	}

	private void checkEntailment(){
		if(p.isActive()){
			entail = p.isEntailed();
			if(entail!=null){
				p.setPassive();
			}
		}
	}
}