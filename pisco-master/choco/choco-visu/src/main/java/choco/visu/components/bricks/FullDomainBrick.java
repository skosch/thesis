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
import static choco.visu.components.ColorConstant.BLUE;
import static choco.visu.components.ColorConstant.GREEN;
import choco.visu.components.papplets.AChocoPApplet;

import java.util.BitSet;
/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 31 oct. 2008
 * Since : Choco 2.0.1
 *
 * {@code FullDomainBrick} is a {@code IChocoBrick} representing the domain of a variable as an array of colored
 * square.
 * Green square means inside the domain,
 * Blue square means outside the domain.
 *
 * Powered by Processing    (http://processing.org/)
 */

public final class FullDomainBrick extends AChocoBrick{

    private final BitSet values;
    private final int capacity;
    private final int offset;
    private DisposableIntIterator it;
    private final int size;


    public FullDomainBrick(final AChocoPApplet chopapplet, final Var var, final int size) {
        super(chopapplet, var);
        this.size = size;
        final int lb = getLowBound();
        final int up = getUppBound();
        this.capacity = up - lb;
        this.offset = -lb;
        this.values = new BitSet(capacity);
        this.it = getDomainValues();
        while(it.hasNext()){
            this.values.set(it.next()+offset);
        }
        it.dispose();
    }

    /**
     * Refresh data of the PApplet in order to refresh the visualization
     *
     * @param arg an object to precise the refreshing
     */
    public final void refresh(final Object arg) {
        values.clear();
        it = getDomainValues();
        while(it.hasNext()){
            values.set(it.next()+offset);
        }
    }


    /**
     * Draw the graphic representation of the var associated to the brick
     */
    public final void drawBrick(final int x, final int y, final int width, final int height) {
        for(int j = 0; j < capacity+1; j++){
            if(values.get(j)){
                chopapplet.fill(GREEN);
            }else{
                chopapplet.fill(BLUE);
            }
            chopapplet.rect(y + size*j, x,  width, height);
        }
    }
}
