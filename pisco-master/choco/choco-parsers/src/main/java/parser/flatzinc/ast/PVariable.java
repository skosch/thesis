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

package parser.flatzinc.ast;

import choco.Choco;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;
import parser.flatzinc.ast.declaration.*;
import parser.flatzinc.ast.expression.EAnnotation;
import parser.flatzinc.ast.expression.EArray;
import parser.flatzinc.ast.expression.Expression;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 11 janv. 2010
* Since : Choco 2.1.1
*
* Class for Variable definition based on flatzinc-like objects.
* A variable is defined like:
* </br> type : identifier annotations [= expression]
*
*/

public final class PVariable extends ParVar{

    static Logger LOGGER = ChocoLogging.getMainLogger();

    public static final String OUTPUT = "output_";

    public PVariable(HashMap<String, Object> map, Declaration type, String identifier, List<EAnnotation> annotations, Expression expression) {
        try {
            // value is always null, except for ARRAY, it can be defined
            // see Flatzinc specifications for more informations.
            switch (type.typeOf) {
                case INT1:
                    buildWithInt(identifier, map);
                    break;
                case INT2:
                    buildWithInt2(identifier, (DInt2) type, map);
                    return;
                case INTN:
                    buildWithManyInt(identifier, (DManyInt) type, map);
                    return;
                case BOOL:
                    buildWithBool(identifier, map);
                    return;
                case SET:
                    buildWithSet(identifier, (DSet) type, map);
                    return;
                case ARRAY:
                    if (expression == null) {
                        buildWithDArray(identifier, (DArray) type, map);
                    } else {
                        buildWithDArray(identifier, (DArray) type, (EArray) expression, map);
                    }
            }
        } finally {
            readAnnotations(annotations);
        }
    }

    private static void readAnnotations(List<EAnnotation> annotations) {
        //TODO: manage annotations
        }

    /**
     * Build a {@link IntegerVariable} named {@code name}.
     *
     * @param name name of the boolean variable
     * @param map
     * @return {@link IntegerVariable}
     */
    private static IntegerVariable buildWithBool(String name, HashMap<String, Object> map) {
        final IntegerVariable bi = Choco.makeBooleanVar(name);
        map.put(name, bi);
        return bi;
    }

    /**
     * Build an unbounded {@link IntegerVariable} named {@code name}, defined by {@code type}.
     *
     * @param name name of the variable
     * @param map
     * @return {@link IntegerVariable}
     */
    private static IntegerVariable buildWithInt(String name, HashMap<String, Object> map) {
        final IntegerVariable iv = Choco.makeIntVar(name);
        map.put(name, iv);
        return iv;
    }

    /**
     * Build a {@link IntegerVariable} named {@code name}, defined by {@code type}.
     *
     * @param name name of the variable
     * @param type {@link parser.flatzinc.ast.declaration.DInt2} object
     * @param map
     * @return {@link IntegerVariable}
     */
    private static IntegerVariable buildWithInt2(String name, DInt2 type, HashMap<String, Object> map) {
        final IntegerVariable iv = Choco.makeIntVar(name, type.getLow(), type.getUpp());
        map.put(name, iv);
        return iv;
    }

    /**
     * Build a {@link IntegerVariable} named {@code name}, defined by {@code type}.
     * {@code type} is expected to be a {@link parser.flatzinc.ast.declaration.DManyInt} object.
     *
     * @param name name of the variable
     * @param type {@link parser.flatzinc.ast.declaration.DManyInt} object.
     * @param map
     * @return {@link IntegerVariable}
     */
    private static IntegerVariable buildWithManyInt(String name, DManyInt type, HashMap<String, Object> map) {
        final IntegerVariable iv = Choco.makeIntVar(name, type.getValues());
        map.put(name, iv);
        return iv;
    }


    /**
     * Build a {@link SetVariable} named {@code name}, defined by {@code type}.
     *
     * @param name name of the variable
     * @param type {@link parser.flatzinc.ast.declaration.DSet} object.
     * @param map
     * @return {@link SetVariable}.
     */
    private static SetVariable buildWithSet(String name, DSet type, HashMap<String, Object> map) {
        final Declaration what = type.getWhat();
        final SetVariable sv;
        switch (what.typeOf) {
            case INT1:
                LOGGER.severe("PVariable#buildWithSet INT1: unknown constructor for " + name);
                throw new UnsupportedOperationException();
            case INT2:
                DInt2 bounds = (DInt2) what;
                sv = Choco.makeSetVar(name, bounds.getLow(), bounds.getUpp());
                map.put(name, sv);
                return sv;
            case INTN:
                DManyInt values = (DManyInt) what;
                sv = Choco.makeSetVar(name, values.getValues());
                map.put(name, sv);
                return sv;
        }
        return null;
    }


    /**
     * Build an array of <? extends {@link choco.kernel.model.variables.Variable}>.
     * </br>WARNING: array's indice are from 1 to n.
     *
     * @param name name of the array of variables.</br> Each variable is named like {@code name}_i.
     * @param type {@link parser.flatzinc.ast.declaration.DArray} object.
     * @param map
     */
    private static void buildWithDArray(String name, DArray type, HashMap<String, Object> map) {
        final DInt2 index = (DInt2) type.getIndex();
        // no need to get lowB, it is always 1 (see specification of FZN for more informations)
        final int size = index.getUpp();
        final Declaration what = type.getWhat();
        final IntegerVariable[] vs;
        switch (what.typeOf) {
            case BOOL:
                vs = new IntegerVariable[size];
                for (int i = 1; i <= size; i++) {
                    vs[i - 1] = buildWithBool(name + '_' + i, map);
                }
                map.put(name, vs);
                break;
            case INT1:
                vs = new IntegerVariable[size];
                for (int i = 1; i <= size; i++) {
                    vs[i - 1] = buildWithInt(name + '_' + i, map);
                }
                map.put(name, vs);
                break;
            case INT2:
                vs = new IntegerVariable[size];
                for (int i = 1; i <= size; i++) {
                    vs[i - 1] = buildWithInt2(name + '_' + i, (DInt2) what, map);
                }
                map.put(name, vs);
                break;
            case INTN:
                vs = new IntegerVariable[size];
                for (int i = 1; i <= size; i++) {
                    vs[i - 1] = buildWithManyInt(name + '_' + i, (DManyInt) what, map);
                }
                map.put(name, vs);
                break;
            case SET:
                final SetVariable[] svs = new SetVariable[size];
                for (int i = 1; i <= size; i++) {
                    svs[i - 1] = buildWithSet(name + '_' + i, (DSet) what, map);
                }
                map.put(name, svs);
                break;
            default:
                break;
        }

    }

    /**
     * Build an array of <? extends {@link choco.kernel.model.variables.Variable}>.
     * </br>WARNING: array's indice are from 1 to n.
     *
     * @param name name of the array of variables.</br> Each variable is named like {@code name}_i.
     * @param type {@link parser.flatzinc.ast.declaration.DArray} object.
     * @param earr array of {@link parser.flatzinc.ast.expression.Expression}
     * @param map
     */
    private static void buildWithDArray(String name, DArray type, EArray earr, HashMap<String, Object> map) {
        final DInt2 index = (DInt2) type.getIndex();
        // no need to get lowB, it is always 1 (see specification of FZN for more informations)
        final int size = index.getUpp();
        final Declaration what = type.getWhat();

        switch (what.typeOf) {
            case BOOL:
            case INT1:
            case INT2:
            case INTN:
                final IntegerVariable[] vs = new IntegerVariable[size];
                for (int i = 0; i < size; i++) {
                    vs[i] = earr.getWhat_i(i).intVarValue();
                }
                map.put(name, vs);
                break;
            case SET:
                final SetVariable[] svs = new SetVariable[size];
                for (int i = 0; i < size; i++) {
                    svs[i] = earr.getWhat_i(i).setVarValue();
                }
                map.put(name, svs);
                break;
            default:
                break;
        }
    }
}
