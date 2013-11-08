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
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;
import choco.kernel.visu.components.bricks.IChocoBrick;
import choco.visu.components.papplets.AChocoPApplet;

/* 
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 24 oct. 2008
 * Since : Choco 2.0.1
 *
 * {@code AChocoBrick} is the abstract class implementing {@code IChocoBrock}.
 * It provides usefull methods for final brick.
 *
 * Powered by Processing    (http://processing.org/)
 */

public abstract class AChocoBrick implements IChocoBrick {

    /**
     * constants for text alignement
     */
    public static final int CENTER = 0, LEFT = 1, RIGHT = 2;

    /**
     * the policy of text alignement 
     */
    protected int policy;

    /**
     * The mother {@code AChocoPApplet} where the brick appears.
     */
    protected final AChocoPApplet chopapplet;

    /**
     * The {@code Var} represented by the brick
     */
    protected final Var var;

    public AChocoBrick(AChocoPApplet chopapplet, Var var) {
        this.var = var;
        this.chopapplet = chopapplet;
    }

    /**
     * Return the var of the brick
     *
     * @return
     */
    public final Var getVar() {
        return var;
    }

    /**
     * Return the domain size of a variable
     * @return the domain size
     */
    protected final int getVarSize() {
        if(var instanceof IntDomainVar){
            return((IntDomainVar)var).getDomainSize();
        }
        if (var instanceof SetVar) {
            return ((SetVar) var).getEnveloppeDomainSize();
        }
        return 0;
    }

    /**
     * Return the lower bound of a variable
     * @return the domain size
     */
    protected final int getLowBound() {
        if(var instanceof IntDomainVar){
            return((IntDomainVar)var).getInf();
        }
        if (var instanceof SetVar) {
            return ((SetVar) var).getEnveloppeInf();
        }
        return 0;
    }


    /**
     * Return the upper bound of a variable
     * @return the domain size
     */
    protected final int getUppBound() {
        if(var instanceof IntDomainVar){
            return((IntDomainVar)var).getSup();
        }
        if (var instanceof SetVar) {
            return ((SetVar) var).getEnveloppeSup();
        }
        return 0;
    }

    /**
     * Return an IntIterator over the values of a specific variable
     *
     * @return an IntIterator
     */
    protected final DisposableIntIterator getDomainValues() {
        if (var instanceof IntDomainVar) {
            return ((IntDomainVar) var).getDomain().getIterator();
        }else
        if (var instanceof SetVar) {
            return ((SetVar) var).getDomain().getEnveloppeIterator();
        }
        return null;
    }

    /**
     * Returns the y coordinate where a text has to be write.
     * It depends on the policy, the lenght of the text to print and the original y.
     * @param y
     * @param length
     * @return
     */
    protected final int alignText(final int y, final int length){
        //x and y represent the coordinates of the middle point of the case
        // we have now to considere the lenght of the value to put inside.
        // we consider the size of a character as 6 px
        switch (policy){
            case CENTER:
                return  y - (length/2)*6;
            case LEFT:
                return y;
            case RIGHT:
                return  y + length*6;
            default:
                return y;
        }
    }

    /**
     * Returns the y coordinate where a text has to be write.
     * It depends on the policy, the lenght of the text to print and the original y.
     * @param y
     * @param length
     * @return
     */
    protected final float alignText(final float y, final float length){
        //x and y represent the coordinates of the middle point of the case
        // we have now to considere the lenght of the value to put inside.
        // we consider the size of a character as 6 px
        switch (policy){
            case CENTER:
                return  y - (length/2)*6;
            case LEFT:
                return y;
            case RIGHT:
                return  y + length*6;
            default:
                return y;
        }
    }


    /**
     * Return a string that represents the instantiated values of {@code Var}.
     * @return
     */
    protected final String getValues(){
        StringBuffer values = new StringBuffer(128);
        final DisposableIntIterator it = getDomainValues();
        while(it.hasNext()){
            int value = it.next();
            values.append(value).append(" - ");
        }
        it.dispose();
        values = values.delete(values.length()-3, values.length());
        return values.toString();
    }

}
