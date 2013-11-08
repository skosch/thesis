/**
 * Copyright (c) 1999-2010, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package trace;

import choco.kernel.solver.search.IntBranchingDecision;

/**
 * A wrapper to communicate with the Visualization, used in weaved code.
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 9 déc. 2010
 */
public class VisuWrapper extends Wrapper {

    final Visualization visu;

    VisuWrapper(Visualization visu) {
        this.visu = visu;
    }

    @Override
    public void init() {
        visu.init();
    }

    @Override
    public void beforeInitialPropagation() {
        visu.beforeInitialPropagation();
    }

    @Override
    public void afterInitialPropagation() {
        visu.afterInitialPropagation();
    }

    @Override
    public void setBranchingDecision(IntBranchingDecision currentDecision) {
        visu.setBranchingDecision(currentDecision);
    }

    @Override
    public void tryNode() {
        visu.tryNode();
    }

    @Override
    public void hasFailed() {
        visu.hasFailed();
    }

    @Override
    public void failNode() {
        visu.failNode();
    }

    @Override
    public void succNode() {
        visu.succNode();
    }
}
