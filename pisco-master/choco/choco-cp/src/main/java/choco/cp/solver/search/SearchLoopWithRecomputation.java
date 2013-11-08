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

package choco.cp.solver.search;

import choco.cp.solver.search.restart.IKickRestart;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.search.AbstractGlobalSearchStrategy;
import choco.kernel.solver.search.IntBranchingTrace;
import gnu.trove.TIntStack;

import java.util.Stack;


public class SearchLoopWithRecomputation extends AbstractSearchLoopWithRestart {

	public final int gap;

	private int cpt = 0;

	private int lastSavedTraceIndex = 0;

	private final TIntStack savedTraceIndex;

    private final Stack<IntBranchingTrace> contexts;

    private final TIntStack ctxIndices;


    public SearchLoopWithRecomputation(AbstractGlobalSearchStrategy searchStrategy, IKickRestart kickRestart, int gap) {
		super(searchStrategy, kickRestart);
        this.gap = gap;
        final int n = searchStrategy.solver.getNbIntVars();
        savedTraceIndex = new TIntStack(n);
        contexts = new Stack<IntBranchingTrace>();
        ctxIndices = new TIntStack(n);
	}


	public final int getGap() {
		return gap;
	}

	@Override
	public void initialize() {
		super.initialize();
		savedTraceIndex.reset();
		lastSavedTraceIndex = searchStrategy.getCurrentTraceIndex();
        savedTraceIndex.push(lastSavedTraceIndex);
        ctxIndices.push(contexts.size());
		searchStrategy.solver.worldPush();
	}

    /**
     * perform the restart.
     */
    @Override
    public void restart() {
        cpt = 0;
        super.restart();
    }

    @Override
	protected void worldPop() {
//		cpt--; // not necessary, because, goUpBranch is called just after and set cpt to 0
		//should we pop the delegated environment
		searchStrategy.solver.worldPop();
        // lastSavedTraceIndex  == 0 means we are juste after the root node
        // we do not want to pop to the root node (=> constraints are inactive!!)
        if(lastSavedTraceIndex!=0){
            if(searchStrategy.getCurrentTraceIndex() == lastSavedTraceIndex) {
                savedTraceIndex.pop();
                lastSavedTraceIndex = savedTraceIndex.peek();
                searchStrategy.solver.worldPop();
            }
        }
        int ind = ctxIndices.pop();
        while(contexts.size()>ind){
            contexts.pop();
        }
		searchStrategy.solver.worldPush();
	}

	@Override
	protected void goUpBranch() throws ContradictionException {
		searchStrategy.postDynamicCut();
		LOGGER.finest("recomputation ...");
        /*
           BEWARE, propagation after applying each decision is MANDATORY in order
           to deal with bound variables and ValSelector, when the strategy returns the
           last tested and removed value from the domain which was not a bounds!
         */
		for (int i = lastSavedTraceIndex; i < searchStrategy.getCurrentTraceIndex() ; i++) {
			ctx = searchStrategy.getTrace(i);
			ctx.getBranching().goDownBranch(ctx);
            searchStrategy.solver.propagate();
		}
		ctx = searchStrategy.topTrace();
		LOGGER.finest("backtrack ...");
        int ind = ctxIndices.peek();
        /*
           BEWARE, propagation after applying each decision is MANDATORY in order
           to retrieve all solutions! Otherwise it loops over the first one infinitely
         */
        for(int i = ind; i < contexts.size(); i++){
        	ctx.getBranching().goUpBranch(contexts.get(i));
            searchStrategy.solver.propagate();
        }
		ctx.getBranching().goUpBranch(ctx);
		LOGGER.finest("continue ...");
		searchStrategy.solver.propagate();
        contexts.push(ctx.copy());
        // state cpt to 0 to force backup at next down branch
        // to speed up search by saving when there is a fail
        cpt = 0;

	}

    @Override
	protected void worldPush() {
		if( cpt % gap == 0) {
			searchStrategy.solver.worldPush();
			lastSavedTraceIndex = searchStrategy.getCurrentTraceIndex();
			savedTraceIndex.push(lastSavedTraceIndex);
            cpt=0;
		}
        ctxIndices.push(contexts.size());
        cpt++;
	}

}
