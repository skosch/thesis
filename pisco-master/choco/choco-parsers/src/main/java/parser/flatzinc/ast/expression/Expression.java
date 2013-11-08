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

package parser.flatzinc.ast.expression;

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;

import java.util.logging.Logger;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 8 janv. 2010
* Since : Choco 2.1.1
*
* Class for expression definition based on flatzinc-like objects.
*/
public abstract class Expression {

    static final Logger LOGGER = ChocoLogging.getMainLogger();

    public enum EType{
        ANN, ARR, BOO, IDA, IDE, INT, SET_B, SET_L, STR 
    }

    @SuppressWarnings({"InstantiatingObjectToGetClassObject"})
    protected static final Class int_arr = new int[0].getClass();
    @SuppressWarnings({"InstantiatingObjectToGetClassObject"})
    protected static final Class bool_arr = new boolean[0].getClass();

    final EType typeOf;

    protected Expression(EType typeOf) {
        this.typeOf = typeOf;
    }

    public final EType getTypeOf() {
        return typeOf;
    }

    static void exit(){
        LOGGER.severe("Expression  unexpected call");
//        new Exception().printStackTrace();
        throw new UnsupportedOperationException();
    }

    /**
     * Get the int value of the {@link Expression}
     * @return int
     */
    public abstract int intValue();

    /**
     * Get array of int of the {@link Expression}
     * @return int[]
     */
    public abstract int[] toIntArray();

    /**
     * Get the {@link IntegerVariable} of the {@link Expression}
     * @return {@link IntegerVariable} or {@link choco.kernel.model.variables.integer.IntegerConstantVariable}
     */
    public abstract IntegerVariable intVarValue();

    /**
     * Get an array of {@link IntegerVariable} of the {@link Expression}
     * @return {{@link IntegerVariable},{@link choco.kernel.model.variables.integer.IntegerConstantVariable}}[]
     */
    public abstract IntegerVariable[] toIntVarArray();

    /**
     * Get the {@link SetVariable} of the {@link Expression}
     * @return {@link SetVariable} or {@link choco.kernel.model.variables.set.SetConstantVariable}
     */
    public abstract SetVariable setVarValue();

    /**
     * Get an array of {@link SetVariable} of the {@link Expression}
     * @return {{@link SetVariable},{@link choco.kernel.model.variables.set.SetConstantVariable}}[]
     */
    public abstract SetVariable[] toSetVarArray();

}
