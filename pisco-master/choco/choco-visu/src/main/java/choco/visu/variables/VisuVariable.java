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

package choco.visu.variables;

import choco.kernel.solver.variables.Var;
import choco.kernel.visu.components.IVisuVariable;
import choco.kernel.visu.components.bricks.IChocoBrick;

import java.util.ArrayList;
/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 24 oct. 2008
 * Since : Choco 2.0.1
 *
 * A {@code VisuVariable} is on object taht links a {@code Var} to its graphical representations
 * (one or more {@code IChocoBrick}).
 */

public final class VisuVariable implements IVisuVariable {

    protected final Var var;
    protected final ArrayList<IChocoBrick> brick;

    public VisuVariable(Var var) {
        this.var = var;
        brick = new ArrayList<IChocoBrick>(16);
    }

    /**
     * Return the solver variable
     * @return
     */
    public Var getSolverVar() {
        return var;
    }

    /**
     * Add a brock observer to the visuvariable
     * @param b
     */
    public final void addBrick(final IChocoBrick b){
        brick.add(b);
    }

    public final IChocoBrick getBrick(final int i){
        return brick.get(i);
    }


    /**
     * refresh every visual representation of the variable
     */
    public final void refresh(final Object arg){
        for(IChocoBrick b: brick){
            b.refresh(arg);
        }
    }

    @Override
    public long getIndex() {
        return var.getIndex();
    }
}
