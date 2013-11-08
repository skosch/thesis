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

package parser.chocogen;

import choco.Choco;
import static choco.Choco.*;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerConstantVariable;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import parser.absconparseur.components.PIntensionConstraint;
import parser.absconparseur.tools.InstanceParser;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * The general factory to handle all constraints except Global and Extensional
 */
public class ModelConstraintFactory extends ObjectFactory{

    public enum ConstExp {
        tru("true", 0),
        fals("false", 0),
        eq("eq", 2),
        ne("ne", 2),
        ge("ge", 2),
        gt("gt", 2),
        le("le", 2),
        lt("lt", 2),
        distanceEQ("distEQ", 3),
        distanceNEQ("distNEQ", 3),
        distanceGT("distGT", 3),
        distanceLT("distLT", 3),
        oppSign("oppSign", 2),
        sameSign("sameSign", 2),
        minChoco("minChoco", 3),
        maxChoco("maxChoco", 3),
        precReiChoco("precReiChoco",4);



        String name;
        int arity;
        private ConstExp(String name, int arity) {
            this.name=name;
            this.arity=arity;
        }
    }

    private static final Map<String, ConstExp> constExp = new HashMap<String, ConstExp>(17);
    static {
        constExp.put("eq", ConstExp.eq);
        constExp.put("ne", ConstExp.ne);
        constExp.put("ge", ConstExp.ge);
        constExp.put("gt", ConstExp.gt);
        constExp.put("le", ConstExp.le);
        constExp.put("lt", ConstExp.lt);
        constExp.put("true", ConstExp.tru);
        constExp.put("false", ConstExp.fals);
        constExp.put("distEQ", ConstExp.distanceEQ);
        constExp.put("distNEQ", ConstExp.distanceNEQ);
        constExp.put("distGT", ConstExp.distanceGT);
        constExp.put("distLT", ConstExp.distanceLT);
        constExp.put("oppSign", ConstExp.oppSign);
        constExp.put("sameSign", ConstExp.sameSign);
        constExp.put("minChoco", ConstExp.minChoco);
        constExp.put("maxChoco", ConstExp.maxChoco);
        constExp.put("precReiChoco", ConstExp.precReiChoco);


    }

    private enum IntExp {
        neg("neg", 1),
        add("add", 2),
        sub("sub", 2),
        mul("mul", 2),
        div("div", 2),
        mod("mod", 2),
        pow("pow", 2),
        abs("abs", 1),
        min("min", 2),
        max("max", 2);


        String name;
        int arity;
        private IntExp(String name, int arity) {
            this.name=name;
            this.arity=arity;
        }
    }

    private static final Map<String, IntExp> intExp = new HashMap<String, IntExp>(10);
    static {
        intExp.put("neg", IntExp.neg);
        intExp.put("add", IntExp.add);
        intExp.put("sub", IntExp.sub);
        intExp.put("mul", IntExp.mul);
        intExp.put("div", IntExp.div);
        intExp.put("mod", IntExp.mod);
        intExp.put("pow", IntExp.pow);
        intExp.put("abs", IntExp.abs);
        intExp.put("min", IntExp.min);
        intExp.put("max", IntExp.max);
    }


    private enum BooleanExp {
        and("and", 2),
        not("not", 1),
        or("or", 2),
        xor("xor", 2),
        iff("iff", 2);

        String name;
        int arity;
        private BooleanExp(String name, int arity) {
            this.name=name;
            this.arity=arity;
        }
    }

    private static final Map<String, BooleanExp> boolExp = new HashMap<String, BooleanExp>(5);
    static {
        boolExp.put("and", BooleanExp.and);
        boolExp.put("not", BooleanExp.not);
        boolExp.put("or", BooleanExp.or);
        boolExp.put("xor", BooleanExp.xor);
        boolExp.put("iff", BooleanExp.iff);
    }

    private enum BooleanPartExp{
        ifte("if", 2);
        String name;
        int arity;
        private BooleanPartExp(String name, int arity) {
            this.name=name;
            this.arity=arity;
        }

    }

    private static final Map<String, BooleanPartExp> boolPartExp = new HashMap<String, BooleanPartExp>(1);
    static {
        boolPartExp.put("if", BooleanPartExp.ifte);
    }


    public ModelConstraintFactory(Model m, InstanceParser parser) {
        super(m, parser);
    }


    @SuppressWarnings({"unchecked"})
    public static Constraint[] makeIntensionConstraint(PIntensionConstraint pic) {

        String[] pp = pic.getUniversalPostfixExpression();
        //List pile = new ArrayList();
        //int indice = 0;
        Deque q = new ArrayDeque();
        for (int i = 0; i < pp.length; i++) {
            String val = pp[i];
            if (!(boolExp.containsKey(val) || constExp.containsKey(val) || intExp.containsKey(val) || boolPartExp.containsKey(val))) {
                try {
                    q.addFirst(constant(Integer.parseInt(val)));
                } catch (NumberFormatException e) {
                    //It must be a variable
                    int idx = Integer.parseInt(val.substring(1));
                    q.addFirst(pic.getScope()[idx].getChocovar());
                }
            } else if (boolExp.containsKey(val)) {
                BooleanExp b = boolExp.get(val);
                Constraint[] is = new Constraint[b.arity];
                for (int j = 0; j < b.arity; j++) {
                    is[j] = (Constraint) q.removeFirst();
                }
                q.addFirst(createBool(b, is));

            } else if (constExp.containsKey(val)) {
                ConstExp b = constExp.get(val);
                IntegerExpressionVariable[] is = new IntegerExpressionVariable[b.arity];
                for (int j = 0; j < b.arity; j++) {
                    is[j] = (IntegerExpressionVariable) q.removeFirst();
                }
                q.addFirst(createExp(b, is));

            } else if (intExp.containsKey(val)) {
                IntExp b = intExp.get(val);
                IntegerExpressionVariable[] is = new IntegerExpressionVariable[b.arity];
                for (int j = 0; j < b.arity; j++) {
                    is[j] = (IntegerExpressionVariable) q.removeFirst();
                }
                q.addFirst(createInt(b, is));

            } else if(boolPartExp.containsKey(val)){

                BooleanPartExp b = boolPartExp.get(val);
                Constraint c = (Constraint)q.removeFirst();
                IntegerExpressionVariable[] iss = new IntegerExpressionVariable[b.arity];
                for (int j = 0; j < b.arity; j++) {
                    iss[j] = (IntegerExpressionVariable) q.removeFirst();
                }

                q.addFirst(createBoolPart(b, c, iss));

            } else {
                throw new Error("I don't know what to do with that :" + val);
            }
        }
        return new Constraint[]{(Constraint) q.removeFirst()};
    }

    private static IntegerExpressionVariable createBoolPart(BooleanPartExp b, Constraint c, IntegerExpressionVariable[] iss) {
        switch (b) {
            case ifte:
                return Choco.ifThenElse(c, iss[0], iss[1]);
            default:
                return null;
        }
    }


    private static Constraint createExp(ConstExp b, IntegerExpressionVariable... i){
        switch (b) {
            case eq:
                return eq(i[0], i[1]);
            case fals:
                return FALSE;
            case ge:
                return geq(i[0], i[1]);
            case gt:
                return gt(i[0], i[1]);
            case le:
                return leq(i[0], i[1]);
            case lt:
                return lt(i[0], i[1]);
            case ne:
                return neq(i[0], i[1]);
            case tru:
                return TRUE;
            case distanceEQ:
	            if (i[2] instanceof IntegerConstantVariable)
		            return distanceEQ((IntegerVariable)i[0],(IntegerVariable)i[1], ((IntegerConstantVariable) i[2]).getValue());
                else return distanceEQ((IntegerVariable)i[0],(IntegerVariable)i[1], (IntegerVariable) i[2]);
	        case distanceNEQ:
		        if (i[2] instanceof IntegerConstantVariable)
					return distanceNEQ((IntegerVariable)i[0],(IntegerVariable)i[1], ((IntegerConstantVariable)i[2]).getValue());
                else throw new Error("todo");
            case distanceGT:
	            if (i[2] instanceof IntegerConstantVariable)
		            return distanceGT((IntegerVariable)i[0],(IntegerVariable)i[1], ((IntegerConstantVariable)i[2]).getValue());
                else return distanceGT((IntegerVariable)i[0],(IntegerVariable)i[1], ((IntegerVariable)i[2]));
            case distanceLT:
	            if (i[2] instanceof IntegerConstantVariable)
		            return distanceLT((IntegerVariable)i[0],(IntegerVariable)i[1], ((IntegerConstantVariable)i[2]).getValue());
                else return distanceLT((IntegerVariable)i[0],(IntegerVariable)i[1], ((IntegerVariable)i[2]));
            case oppSign:
                return oppositeSign(i[0],i[1]);
            case sameSign:
                 return sameSign(i[0],i[1]);
            case minChoco:
                return min((IntegerVariable)i[1], (IntegerVariable)i[2], (IntegerVariable)i[0]);
            case maxChoco:
                return max((IntegerVariable)i[1], (IntegerVariable)i[2], (IntegerVariable)i[0]);
            case precReiChoco:
                return precedenceReified((IntegerVariable)i[1], ((IntegerConstantVariable)i[2]).getValue(), (IntegerVariable)i[3], (IntegerVariable)i[0]);
            default:
                return null;
        }
    }

    private static Constraint createBool(BooleanExp  b, Constraint... i){
        switch (b) {
            case and:
                if(i.length ==2){
                    return Choco.and(i[0], i[1]);
                }
                throw new Error("todo");
            case iff:
                if(i.length == 2){
                    return Choco.ifOnlyIf(i[0], i[1]);
                }
                throw new Error("todo");
            case not:
                if(i.length == 1){
                    return Choco.not(i[0]);
                }
                throw new Error("todo");
            case or:
                if(i.length ==2){
                    return Choco.or(i[0], i[1]);
                }
                throw new Error("todo");
            case xor:
                if(i.length ==2){
                    //A XOR B = (A ET non B) OU (non A ET B).
                    return Choco.or(Choco.and(i[0], Choco.not(i[1])),Choco.and(i[1], Choco.not(i[0])));
                }
                throw new Error("todo");
            default:
                return null;
        }
    }



    private static IntegerExpressionVariable createInt(IntExp b, IntegerExpressionVariable... i){
        switch (b) {
            case add:
                return Choco.plus(i[0], i[1]);
            case div:
                  return Choco.div(i[0],i[1]);
            case mod:
                return Choco.mod(i[0], i[1]);
            case mul:
                return Choco.mult(i[0],i[1]);
            case neg:
                return Choco.neg(i[0]);
            case pow:
                throw new UnsupportedOperationException("POW not yet implemented");
            case sub:
                return Choco.minus(i[0], i[1]);
            case abs:
                return Choco.abs(i[0]);
            case max:
                return Choco.max(i[0], i[1]);
            case min:
                return Choco.min(i[0], i[1]);
            default:
                return null;
        }
    }

}
