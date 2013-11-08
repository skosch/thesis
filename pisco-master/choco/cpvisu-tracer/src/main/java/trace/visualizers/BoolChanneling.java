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

import choco.cp.solver.variables.integer.BooleanVarImpl;
import choco.kernel.solver.search.IntBranchingDecision;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.slf4j.Logger;
import trace.Visualizer;

import static trace.visualizers.Writer.*;

/**
 * A specialized visualizer for the boolean channeling constraint.
 * (in Choco: domainChanneling)
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/12/10
 */
public final class BoolChanneling extends Visualizer {

    private static final String type = "bool_channeling";

    final IntDomainVar var;

    final BooleanVarImpl[] bool;

    final int offset;

    /**
     * Build a visualizer for the boolean channeling constraint
     *
     * @param var     domain variable
     * @param bool    collection of boolean variables
     * @param offset  starting value
     * @param display "expanded" or "compact"
     * @param width   width of the visualizer
     * @param height  height of the visualizer
     */
    public BoolChanneling(IntDomainVar var, IntDomainVar[] bool, int offset, String display, int width, int height) {
        super(type, display, width, height);
        this.var = var;
        this.bool = new BooleanVarImpl[bool.length];
        for (int b = 0; b < bool.length; b++) {
            this.bool[b] = (BooleanVarImpl) bool[b];
        }
        this.offset = offset;
    }

    /**
     * Build a visualizer for the boolean channeling constraint
     *
     * @param var     domain variable
     * @param bool    collection of boolean variables
     * @param display "expanded" or "compact"
     * @param width   width of the visualizer
     * @param height  height of the visualizer
     */
    public BoolChanneling(IntDomainVar var, IntDomainVar[] bool, String display, int width, int height) {
        super(type, display, width, height);
        this.var = var;
        this.bool = new BooleanVarImpl[bool.length];
        for (int b = 0; b < bool.length; b++) {
            this.bool[b] = (BooleanVarImpl) bool[b];
        }
        this.offset = var.getInf();
    }

    /**
     * Build a visualizer for the boolean channeling constraint
     *
     * @param var     domain variable
     * @param bool    collection of boolean variables
     * @param offset  starting value
     * @param display "expanded" or "compact"
     * @param x       coordinate of the visualizer in the x-axis (horizontal)
     * @param y       coordinate of the visualizer in the y-axis (vertical)
     * @param width   width of the visualizer
     * @param height  height of the visualizer
     * @param group   group name (to group multiple constraints)
     * @param min     expected minimal value of any of the domains
     * @param max     expected maximal value of any of the domains
     */
    public BoolChanneling(IntDomainVar var, IntDomainVar[] bool, int offset, String display, int x, int y, int width, int height, String group, int min, int max) {
        super(type, display, x, y, width, height, group, min, max);
        this.var = var;
        this.bool = new BooleanVarImpl[bool.length];
        for (int b = 0; b < bool.length; b++) {
            this.bool[b] = (BooleanVarImpl) bool[b];
        }
        this.offset = offset;
    }

    /**
     * Build a visualizer for the boolean channeling constraint
     *
     * @param var     domain variable
     * @param bool    collection of boolean variables
     * @param display "expanded" or "compact"
     * @param x       coordinate of the visualizer in the x-axis (horizontal)
     * @param y       coordinate of the visualizer in the y-axis (vertical)
     * @param width   width of the visualizer
     * @param height  height of the visualizer
     * @param group   group name (to group multiple constraints)
     * @param min     expected minimal value of any of the domains
     * @param max     expected maximal value of any of the domains
     */
    public BoolChanneling(IntDomainVar var, IntDomainVar[] bool, String display, int x, int y, int width, int height, String group, int min, int max) {
        super(type, display, x, y, width, height, group, min, max);
        this.var = var;
        this.bool = new BooleanVarImpl[bool.length];
        for (int b = 0; b < bool.length; b++) {
            this.bool[b] = (BooleanVarImpl) bool[b];
        }
        this.offset = var.getInf();
    }

    @Override
    protected void print(Logger logger, boolean focus, IntBranchingDecision decision) {
        writer.argumentIn(_1, 3).ivar(var, _1, 4).argumentOut(3);

        if (decision != null && decision.getBranchingObject() == var) {
            if (focus) {
                writer.focus(_1 + _S + Integer.toString(1), group, type);
            } else {
                writer.fail(_1 + _S + Integer.toString(1), group, decision.getBranchingValue());
            }
        }

        writer.argumentIn(_2, 3).arrayDvar(bool, 4).argumentOut(3);

        if (decision != null) {
            for (int i = 0; i < bool.length; i++) {
                if (decision.getBranchingObject() == bool[i]) {
                    if (focus) {
                        writer.focus(_3 + _S + Integer.toString(i + 1), group, type);
                        break;
                    } else {
                        writer.fail(_3 + _S + Integer.toString(i + 1), group, decision.getBranchingValue());
                        break;
                    }
                }
            }
        }
        writer.argumentIn(_3, 3).integer(offset, _3, 4).argumentOut(3);
    }
}
