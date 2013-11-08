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

package choco.shaker.tools.factory;

import choco.Choco;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;

import java.util.ArrayList;
import java.util.Random;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 12 mars 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*/
public class OperatorFactory {

    VariableFactory vf;
    ConstraintFactory cf;

    int depth = 2;

    public enum O {ABS, DIV, IFTHENELSE, MAX, MIN, MINUS, MOD, MULT, NEG,
                    NONE, PLUS, POWER, SCALAR, SUM};

    public ArrayList<O> scope = new ArrayList();


    /**
     * Declare factory dependencies
     * @param vf
     * @parma cf
     */
    public void depends(VariableFactory vf, ConstraintFactory cf){
        this.vf = vf;
        this.cf = cf;
    }

    /**
     * Define the depth of the operator
     * @param d
     */
    public void depth(int d){
        this.depth = d;
    }

    /**
     * Define a specific scope of operators type to pick up in
     * @param os
     */
    public void scopes(O... os){
        scope.clear();
        for(int i = 0; i < os.length; i++){
            scope.add(os[i]);
        }
    }

    /**
     * Select randomly (among scope if defined)
     * and return an operator type
     * @param r
     * @return
     */
    public O any(Random r){
        if(scope.size()>0){
            return scope.get(r.nextInt(scope.size()));
        }
        O[] values = O.values();
        return values[r.nextInt(values.length)];
    }


    /**
     * Make a operator
     * @param r
     * @return
     */
    public IntegerExpressionVariable make(Random r){
        return make(any(r), r, depth);
    }

    /**
     * Make a specific operator
     * @param o
     * @param r
     * @return
     */
    public IntegerExpressionVariable make(O o, Random r, int depth) {
        if(depth == 0){
            o = O.NONE;
        }
        switch (o){
            case ABS:
                return Choco.abs(make(any(r), r, depth-1));
            case DIV:
                return Choco.div(make(any(r), r, depth-1), make(any(r), r, depth-1));
            case IFTHENELSE:
                return Choco.ifThenElse(cf.make(r), make(any(r), r, depth-1), make(any(r), r, depth-1));
            case MAX:
                return Choco.max(make(r.nextInt(4)+1,r, depth-1));
            case MIN:
                return Choco.min(make(r.nextInt(4)+1,r, depth-1));
            case MINUS:
                return Choco.minus(make(any(r), r, depth-1), make(any(r), r, depth-1));
            case MOD:
                return Choco.mod(make(any(r), r, depth-1), make(any(r), r, depth-1));
            case MULT:
                return Choco.mult(make(any(r), r, depth-1), make(any(r), r, depth-1));
            case NEG:
                return Choco.neg(make(any(r), r, depth-1));
            case NONE:
                return vf.make(r);
            case PLUS:
                return Choco.plus(make(any(r), r, depth-1), make(any(r), r, depth-1));
            case POWER:
                return Choco.power(make(any(r), r, depth-1), make(any(r), r, depth-1));
            case SCALAR:
                IntegerVariable[] vars = vf.make(r.nextInt(4)+1, r);
                int[] coeff = new int[vars.length];
                for(int i = 0; i < coeff.length; i++){
                    coeff[i] = 10 - r.nextInt(10);
                }
                return Choco.scalar(coeff, vars);
            case SUM:
                return Choco.sum(vf.make(r.nextInt(4)+1, r));
        }
        return null;
    }

    /**
     * Make an array of operators
     * @param nb
     * @param r
     * @param depth
     * @return
     */
    public IntegerExpressionVariable[] make(int nb, Random r, int depth) {
        IntegerExpressionVariable[] vars = new IntegerExpressionVariable[nb];
        for(int i = 0; i < nb; i++ ){
            vars[i] = make(any(r), r, depth);
        }
        return vars;
    }

    /**
     * Make an array of integervariable
     * @param nb
     * @param r
     * @return
     */
    public IntegerVariable[] make(int nb, Random r) {
        IntegerVariable[] vars = new IntegerVariable[nb];
        for(int i = 0; i < nb; i++ ){
            vars[i] = (IntegerVariable)make(O.NONE, r, 0);
        }
        return vars;
    }

    /**
     * Make an array of specific operators
     * @param nb
     * @param r
     * @param depth
     * @return
     */
    public IntegerExpressionVariable[] make(int nb, O o, Random r, int depth) {
        IntegerExpressionVariable[] vars = new IntegerExpressionVariable[nb];
        for(int i = 0; i < nb; i++ ){
            vars[i] = make(o, r, depth);
        }
        return vars;
    }

}
