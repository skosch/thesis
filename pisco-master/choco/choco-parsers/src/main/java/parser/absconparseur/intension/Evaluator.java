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

package parser.absconparseur.intension;


import choco.kernel.common.logging.ChocoLogging;
import parser.absconparseur.ReflectionManager;
import parser.absconparseur.Toolkit;
import parser.absconparseur.intension.arithmetic.*;
import parser.absconparseur.intension.logical.*;
import parser.absconparseur.intension.relational.*;
import parser.absconparseur.intension.terminal.FalseEvaluator;
import parser.absconparseur.intension.terminal.LongEvaluator;
import parser.absconparseur.intension.terminal.TrueEvaluator;
import parser.absconparseur.intension.terminal.VariableEvaluator;
import parser.absconparseur.intension.types.*;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class Evaluator {

	protected final static Logger LOGGER = ChocoLogging.getMainLogger();

	private static final Map<String, Class> classMap;

	private static final Map<String, Integer> arityMap;

	private static final Set<String> symmetricSet;

	private static final Set<String> associativeSet;

	static {
		classMap = new HashMap<String, Class>(16);
		arityMap = new HashMap<String, Integer>(16);
		symmetricSet = new HashSet<String>(16);
		associativeSet = new HashSet<String>(16);

		Class[] classes = ReflectionManager.searchClassesInheritingFrom(Evaluator.class, Modifier.PUBLIC, Modifier.ABSTRACT);
        if(classes.length==0){
            classes = new Class[]{
                AbsEvaluator.class,
                AddEvaluator.class,
                DivEvaluator.class,
                IfEvaluator.class,
                MaxEvaluator.class,
                MinEvaluator.class,
                ModEvaluator.class,
                MulEvaluator.class,
                NegEvaluator.class,
                PowEvaluator.class,
                SubEvaluator.class,
                AndEvaluator.class,
                DistEQEvaluator.class,
                DistGTEvaluator.class,
                DistLTEvaluator.class,
                DistNEQEvaluator.class,
                IffEvaluator.class,
                MaxChocoEvaluator.class,
                MinChocoEvaluator.class,
                NotEvaluator.class,
                OppSignEvaluator.class,
                OrEvaluator.class,
                PrecReiChocoEvaluator.class,
                SameSignEvaluator.class,
                XorEvaluator.class,
                EqEvaluator.class,
                GeEvaluator.class,
                GtEvaluator.class,
                LeEvaluator.class,
                LtEvaluator.class,
                NeEvaluator.class,
                FalseEvaluator.class,
                LongEvaluator.class,
                TrueEvaluator.class,
                VariableEvaluator.class
            };
        }
        for (Class clazz : classes) {
			String className = Toolkit.getRelativeClassNameOf(clazz);
			String evaluatorToken = String.format("%s%s", className.substring(0, 1).toLowerCase(), className.substring(1, className.lastIndexOf("Evaluator")));

			// LOGGER.info("evaluatorToken = " + evaluatorToken + " absoluteClassName = " + clazz.getName());
            classMap.put(evaluatorToken, clazz);

			int arity = -1;
			try {
				if (Arity0Type.class.isAssignableFrom(clazz))
					arity = 0;
				if (Arity1Type.class.isAssignableFrom(clazz))
					arity = 1;
				if (Arity2Type.class.isAssignableFrom(clazz))
					arity = 2;
				if (Arity3Type.class.isAssignableFrom(clazz))
					arity = 3;
				if (Arity4Type.class.isAssignableFrom(clazz))
					arity = 4;
				if (SymmetricType.class.isAssignableFrom(clazz))
					symmetricSet.add(evaluatorToken);
				if (AssociativeType.class.isAssignableFrom(clazz))
					associativeSet.add(evaluatorToken);

			} catch (Exception e) {
				LOGGER.log(Level.SEVERE,"fatal error", e);
				System.exit(1);
			}
			// LOGGER.info("evaluatorToken = " + evaluatorToken + " arity = " + arity);
			arityMap.put(evaluatorToken, arity);
//           System.out.println("arityMap.put(\""+evaluatorToken + "\", "+arity+");");
		}
        // FOR MVN TESTS ONLY
        if(classes.length==0){
            arityMap.put("abs", 1);
            arityMap.put("add", 2);
            arityMap.put("div", 2);
            arityMap.put("if", 3);
            arityMap.put("max", 2);
            arityMap.put("min", 2);
            arityMap.put("mod", 2);
            arityMap.put("mul", 2);
            arityMap.put("neg", 1);
            arityMap.put("pow", 2);
            arityMap.put("sub", 2);
            arityMap.put("and", 2);
            arityMap.put("distEQ", 3);
            arityMap.put("distGT", 3);
            arityMap.put("distLT", 3);
            arityMap.put("distNEQ", 3);
            arityMap.put("iff", 2);
            arityMap.put("maxChoco", 3);
            arityMap.put("minChoco", 3);
            arityMap.put("not", 1);
            arityMap.put("oppSign", 2);
            arityMap.put("or", 2);
            arityMap.put("precReiChoco", 4);
            arityMap.put("sameSign", 2);
            arityMap.put("xor", 2);
            arityMap.put("eq", 2);
            arityMap.put("ge", 2);
            arityMap.put("gt", 2);
            arityMap.put("le", 2);
            arityMap.put("lt", 2);
            arityMap.put("ne", 2);
            arityMap.put("false", 0);
            arityMap.put("long", 0);
            arityMap.put("true", 0);
            arityMap.put("variable", 0);
        }
	}

	public static Class getClassOf(String evaluatorToken) {
		return classMap.get(evaluatorToken);
	}

	public static int getArityOf(String evaluatorToken) {
		Integer i = arityMap.get(evaluatorToken);
		return i == null ? -1 : i;
	}

	public static boolean isSymmetric(String evaluatorToken) {
		return symmetricSet.contains(evaluatorToken);
	}

	public static boolean isAssociative(String evaluatorToken) {
		return associativeSet.contains(evaluatorToken);
	}

	// TODO this method should only be used during initialization (otherwise, too expensive)
	public int getArity() {
		String className = Toolkit.getRelativeClassNameOf(getClass());
		String evaluatorToken = className.substring(0, 1).toLowerCase() + className.substring(1, className.lastIndexOf("Evaluator"));
		return arityMap.get(evaluatorToken);
	}

	protected static int top = -1;

	public static int getTop() {
		return top;
	}

	public static void resetTop() {
		top = -1;
	}

	public static long getTopValue() {
		return stack[top];
	}

	protected static long[] stack = new long[100];

	public static void checkStackSize(int size) {
		if (stack.length < size)
			stack = new long[size];
	}

	public static void displayStack() {
		if(LOGGER.isLoggable(Level.INFO)) {
			StringBuilder s = new StringBuilder(32);
			for (int i = 0; i <= top; i++)
				s.append(stack[i]).append(' ');
			LOGGER.info(new String(s));
		}
	}


	public abstract void evaluate();

	public String toString() {
		return Toolkit.getRelativeClassNameOf(this);
	}
}