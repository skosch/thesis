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

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.variables.Var;
import static choco.visu.components.ColorConstant.BLACK;
import static choco.visu.components.ColorConstant.WHITE;
import choco.visu.components.papplets.AChocoPApplet;

import java.util.Random;
/* 
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 31 oct. 2008
 * Since : Choco 2.0.1
 *
 * {@code HazardOrValueBrick} is a {@code IChocoBrick} representing the value of a variable in two ways:
 * - the variable is not yet instanciated, print a value inside the domain (in increasing order)
 * - otherwise, print the instanciated values.
 *
 * Powered by Processing    (http://processing.org/)
 */

public final class HazardOrValueBrick extends AChocoBrick{

    private String value;
    private boolean inst;
    private int last;
    private int low;
    private int upp;

    public HazardOrValueBrick(final AChocoPApplet chopapplet, final Var var, final int policy) {
        super(chopapplet, var);
        this.low = getLowBound();
        this.upp = getUppBound();
        this.value = Integer.toString(low);
        this.policy = policy;
        this.inst = false;
        Random r= new Random();
        this.last = r.nextInt(upp+1);
    }

    /**
     * Refresh data of the PApplet in order to refresh the visualization
     *
     * @param arg an object to precise the refreshing
     */
    public final void refresh(final Object arg) {
        if(var.isInstantiated()){
            StringBuilder st  = new StringBuilder(128);
            final DisposableIntIterator it = getDomainValues();
            while(it.hasNext()){
                if(st.length()>0){
                    st.append(" - ");
                }
                st.append(it.next());
            }
            it.dispose();
            value = st.toString();
            this.inst = true;
        }else{
            low = getLowBound();
            upp = getUppBound();
            this.inst = false;
        }
    }


    /**
     * Draw the graphic representation of the var associated to the brick
     */
    public final void drawBrick(final int x, final int y, final int width, final int height) {
        if(!inst){
            value = Integer.toString(last++);
            if(last > upp)last = low;
        }
        chopapplet.fill(BLACK);
        chopapplet.text(value, alignText(y, value.length()), x);
        chopapplet.fill(WHITE);
    }
}