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

package choco.visu.components.bricks;

import choco.kernel.solver.search.ISearchLoop;
import choco.kernel.solver.variables.Var;
import choco.visu.components.papplets.AChocoPApplet;
import choco.visu.components.papplets.DottyTreeSearchPApplet;
import choco.visu.searchloop.ObservableStepSearchLoop;
import choco.visu.searchloop.State;
/* 
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 12 nov. 2008
 * Since : Choco 2.0.1
 *
 * {@code DotBrick} is a {@IChocoBrick} used inside {@code DottyTreeSearchPApplet}.
 * It is used to deal with tree search representation in a Dotty file.
 */

public final class DotBrick extends AChocoBrick{

    public DotBrick(AChocoPApplet chopapplet, Var var) {
        super(chopapplet, var);
    }

    /**
     * Refresh data of the PApplet in order to refresh the visualization
     *
     * @param arg an object to precise the refreshing
     */
    public final void refresh(final Object arg) {
        if (arg instanceof ISearchLoop) {
            ObservableStepSearchLoop ossl = (ObservableStepSearchLoop)arg;
            State state = ossl.state;
            switch (state) {
                case SOLUTION:
                    ((DottyTreeSearchPApplet)chopapplet).updateNodes(this.var.getName()+ ':' +getValues(), true);
                    break;
                case DOWN:
                    ((DottyTreeSearchPApplet)chopapplet).updateNodes(this.var.getName()+ ':' +getValues(), false);
                    ((DottyTreeSearchPApplet)chopapplet).updateEdges(false);
                    break;
                case UP:
                    ((DottyTreeSearchPApplet)chopapplet).updateEdges(true);
                    break;
                case END:
                    ((DottyTreeSearchPApplet)chopapplet).printGraph();
                    ((DottyTreeSearchPApplet)chopapplet).clean(true);
                    break;
                case RESTART:
                    ((DottyTreeSearchPApplet)chopapplet).clean(false);
                default:
                    break;
            }
        }
    }

    /**
     * Draw the graphic representation of the var associated to the brick
     */
    public final void drawBrick(int x, int y, int widht, int height) {
        // nothing to do
    }
}
