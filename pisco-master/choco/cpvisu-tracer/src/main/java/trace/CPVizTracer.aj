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
package trace;

public aspect CPVizTracer {


    trace.Visualization visu;

    trace.Wrapper wrapper;

    /*************************************************/
    /**                                             **/
    /**                 POINTCUTS 					**/
    /**                                             **/
    /*************************************************/

    // creation of the visualization
    pointcut visualization(trace.Visualization visu): execution(trace.Visualization.new(..)) && target(visu);

    // start of the Tree search
    pointcut incrementalRun(): execution(void choco.kernel.solver.search.AbstractGlobalSearchStrategy.incrementalRun());

    // initial propagation
    pointcut newTreeSearch(): execution(void choco.kernel.solver.search.AbstractGlobalSearchStrategy.newTreeSearch());

    // decision creation
    pointcut storeCurrentDecision(choco.kernel.solver.search.IntBranchingDecision decision):
	    execution (void choco.kernel.solver.branch.IntBranching+.goDownBranch(choco.kernel.solver.search.IntBranchingDecision)) && args(decision);

    // initial propagation
    pointcut intialPropagation(): execution(void choco.kernel.solver.Solver+.propagate()) && cflow(execution(void choco.kernel.solver.search.AbstractGlobalSearchStrategy.initialPropagation()));

    // decision application
    pointcut applyDecision(): execution(void choco.kernel.solver.Solver+.propagate()) && cflow(execution(void choco.kernel.solver.search.AbstractSearchLoop.downBranch()));

    // reconsider decision
    pointcut worldPop(): execution(void choco.kernel.solver.search.AbstractSearchLoop+.worldPop()) && cflow(execution(void choco.kernel.solver.search.AbstractSearchLoop.upBranch()));

    // reconsider decision
    pointcut reconsiderDecision(): execution(void choco.kernel.solver.Solver+.propagate()) && cflow(execution(void choco.kernel.solver.search.AbstractSearchLoop.upBranch()));

    // Solution recording
    pointcut recordSolution(): call(void choco.kernel.solver.search.AbstractSearchStrategy.recordSolution());

    /*************************************************/
    /**                                             **/
    /**                 ADVICES 					**/
    /**                                             **/
    /*************************************************/

    after(trace.Visualization aVisu): visualization(aVisu){
        this.visu = aVisu;
    }

    // start of the tree search
    before(): incrementalRun() {
        this.wrapper = WrapperFactory.build(visu);
        this.wrapper.init();
    }

    before(choco.kernel.solver.search.IntBranchingDecision decision): storeCurrentDecision(decision){
        this.wrapper.setBranchingDecision(decision);
    }

    // start of the tree search
    before(): intialPropagation() {
        this.wrapper.beforeInitialPropagation();
    }


    // Try node
    after() returning: intialPropagation() {
        this.wrapper.afterInitialPropagation();
    }

    // Try node
    after() returning: applyDecision() {
        this.wrapper.tryNode();
    }

    // Fail node
    after() throwing(choco.kernel.solver.ContradictionException c): applyDecision() {
        this.wrapper.hasFailed();
    }

    after(): worldPop(){
        this.wrapper.failNode();
    }

    // Reconsider a decision
    after() throwing(choco.kernel.solver.ContradictionException c): reconsiderDecision() {
        this.wrapper.hasFailed();
    }

    // Record a solution
    after(): recordSolution() {
        this.wrapper.succNode();
    }
}