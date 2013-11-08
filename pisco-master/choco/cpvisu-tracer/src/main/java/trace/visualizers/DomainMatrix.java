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
package trace.visualizers;

import choco.kernel.solver.search.IntBranchingDecision;
import choco.kernel.solver.variables.Var;
import org.slf4j.Logger;
import trace.Visualizer;

import static trace.visualizers.Writer._S;

/**
 * ,
 * A specialized visualizer for a matrix of domain variables
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 14/12/10
 */
public class DomainMatrix extends Visualizer {

    private static final String type = "domain_matrix";

    final Var[][] vars;

    /**
     * Build a visualizer for a matrix of domain variables
     *
     * @param vars    domain variables
     * @param display "expanded" or "compact"
     * @param width   width of the visualizer
     * @param height  height of the visualizer
     */
    public DomainMatrix(Var[][] vars, String display, int width, int height) {
        super(type, display, width, height);
        this.vars = vars;
    }

    protected DomainMatrix(Var[][] vars, String type, String display, int width, int height) {
        super(type, display, width, height);
        this.vars = vars;
    }

    /**
     * Build a visualizer for a matrix of domain variables
     *
     * @param vars    domain variables
     * @param display "expanded" or "compact"
     * @param x       coordinate of the visualizer in the x-axis (horizontal)
     * @param y       coordinate of the visualizer in the y-axis (vertical)
     * @param width   width of the visualizer
     * @param height  height of the visualizer
     * @param group   group name (to group multiple constraints)
     * @param min     expected minimal value of any of the domains
     * @param max     expected maximal value of any of the domains
     */
    public DomainMatrix(Var[][] vars, String display, int x, int y, int width, int height, String group, int min, int max) {
        super(type, display, x, y, width, height, group, min, max);
        this.vars = vars;
    }

    public DomainMatrix(Var[][] vars, String type, String display, int x, int y, int width, int height, String group, int min, int max) {
        super(type, display, x, y, width, height, group, min, max);
        this.vars = vars;
    }

    @Override
    protected void print(Logger logger, boolean focus, IntBranchingDecision decision) {
        for (int i = 0; i < vars.length; i++) {
            for (int j = 0; j < vars[i].length; j++) {
                writer.var(vars[i][j], (i + 1) + " " + (j + 1), 3);
            }
        }
        if (decision != null) {
            for (int i = 0; i < vars.length; i++) {
                for (int j = 0; j < vars[i].length; j++) {
                    if (decision.getBranchingObject() == vars[i][j]) {
                        if (focus) {
                            writer.focus((i + 1) + _S + (j + 1), group);
                        } else {
                            writer.fail((i + 1) + _S + (j + 1), group, decision.getBranchingValue());
                        }
                    }
                }
            }
        }
    }
}
