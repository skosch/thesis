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
import parser.absconparseur.InstanceTokens;
import parser.absconparseur.ReflectionManager;
import parser.absconparseur.Toolkit;
import parser.absconparseur.components.PVariable;
import parser.absconparseur.intension.arithmetic.*;
import parser.absconparseur.intension.logical.AndEvaluator;
import parser.absconparseur.intension.logical.LogicalEvaluator;
import parser.absconparseur.intension.logical.NotEvaluator;
import parser.absconparseur.intension.logical.OrEvaluator;
import parser.absconparseur.intension.relational.RelationalEvaluator;
import parser.absconparseur.intension.terminal.FalseEvaluator;
import parser.absconparseur.intension.terminal.LongEvaluator;
import parser.absconparseur.intension.terminal.TrueEvaluator;
import parser.absconparseur.intension.terminal.VariableEvaluator;
import parser.absconparseur.intension.types.BooleanType;
import parser.absconparseur.intension.types.IntegerType;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;


public class EvaluationManager {
	protected final static Logger LOGGER = ChocoLogging.getMainLogger();

	protected String[] universalPostfixExpression;

	protected Evaluator[] evaluators;

	/**
	 * 1D = index of evaluator; <br>
	 * value = 1 means that if the result of the evaluator is 1 it can be returned immediately, <br>
	 * value = 0 means that if the result of the evaluator is 0 it can be returned immediately, <br>
	 * value = -1 means that we have to keep evaluating
	 */
	protected int[] shortCircuits;

	/**
	 * This field is inserted in order to avoid to have a tuple as parameter of method evaluate in Evaluator.
	 */
	protected int[] currentValues;

	public int getCurentValueOf(int variablePosition) {
		return currentValues[variablePosition];
	}

	private Evaluator buildEvaluator(String token) {
		Long l = Toolkit.parseLong(token);
		if (l != null)
			return new LongEvaluator(l);
		if (token.startsWith(InstanceTokens.PARAMETER_PREFIX))
			return new VariableEvaluator(this, Integer.parseInt(token.substring(InstanceTokens.PARAMETER_PREFIX.length())));
		return (Evaluator) ReflectionManager.getInstanceOf(Evaluator.getClassOf(token));
	}

	private void buildEvaluatorsFrom(String[] canonicalPostfixExpression) {
		evaluators = new Evaluator[canonicalPostfixExpression.length];
		for (int i = 0; i < evaluators.length; i++)
			evaluators[i] = buildEvaluator(canonicalPostfixExpression[i]);
		//display();
	}

	// Reste a faire pour IF
	protected void dealWithShortCircuits() {
		boolean useShortCircuits;// TODO
        shortCircuits = new int[evaluators.length];
		useShortCircuits = false;

		for (int i = 0; i < evaluators.length - 1; i++) {
			if (evaluators[i] instanceof IntegerType)
				continue;
			// from a Boolean evaluator, we may find a short circuit
			int j = i + 1;
			int nbStackedElements = 1;
			while (j < evaluators.length) {
				nbStackedElements += 1 - evaluators[j].getArity();
				if (nbStackedElements <= 1)
					break;
				j++;
			}
			if (j == i + 1)
				continue;
			if (evaluators[j] instanceof OrEvaluator) {
				shortCircuits[i] = j + 1;
				useShortCircuits = true;
			} else if (evaluators[j] instanceof AndEvaluator) {
				shortCircuits[i] = -j - 1;
				useShortCircuits = true;
			}
			// LOGGER.info(" i= " + i + " j = " + j);
		}
		if (!useShortCircuits)
			shortCircuits = null;
	}

	public EvaluationManager(String[] universalPostfixExpression) {
		this.universalPostfixExpression = universalPostfixExpression;
		buildEvaluatorsFrom(universalPostfixExpression);
		dealWithShortCircuits();
		Evaluator.checkStackSize(evaluators.length);
	}

	//	public EvaluationManager(String universalPostfixExpression) {
	//		this.universalPostfixExpression = universalPostfixExpression;
	//		evaluators = buildEvaluatorsFrom(universalPostfixExpression);
	//		dealWithShortCircuits();
	//		Evaluator.checkStackSize(evaluators.length);
	//	}

	private int nextEvaluator(int i) {
		if (shortCircuits[i] > 0)
			return (Evaluator.getTopValue() == 1 ? shortCircuits[i] : i + 1);
		return (Evaluator.getTopValue() == 0 ? -shortCircuits[i] : i + 1);
	}

	/**
	 * Evaluates the recorded postfix expression with respect to the given tuple.
     */
	public final long evaluate(int[] values) {
		this.currentValues = values;
		Evaluator.resetTop();

		if (shortCircuits == null)
			for (Evaluator evaluator : evaluators)
				evaluator.evaluate();
		else
			for (int i = 0; i < evaluators.length; i = shortCircuits[i] == 0 ? i + 1 : nextEvaluator(i))
				evaluators[i].evaluate();

		assert Evaluator.getTop() == 0 : String.valueOf(Evaluator.getTop());
		return Evaluator.getTopValue(); // == 1; // 1 means true while 0 means false
	}

	public boolean controlArityOfEvaluators() {
		int nbStackedElements = 0;
		for (Evaluator evaluator : evaluators) nbStackedElements += 1 - evaluator.getArity();
		return nbStackedElements == 1;
	}

	public boolean controlTypeOfEvaluators(boolean booleanType) {
		if (booleanType && !(evaluators[evaluators.length - 1] instanceof BooleanType))
			return false;
		if (!booleanType && !(evaluators[evaluators.length - 1] instanceof IntegerType))
			return false;

		boolean[] booleanTypes = new boolean[evaluators.length];
		int top = -1;
		for (Evaluator evaluator : evaluators) {
			// LOGGER.info(evaluators[i].getClass().getName());
			if (evaluator instanceof ArithmeticEvaluator) {
				if (evaluator instanceof IfEvaluator) {
					if (!booleanTypes[top] || booleanTypes[top - 1] || booleanTypes[top - 2])
						return false;
					top -= 3;
				} else {
					for (int j = 0; j < evaluator.getArity(); j++) {
						if (booleanTypes[top])
							return false;
						top--;
					}
				}
			} else if (evaluator instanceof LogicalEvaluator) {
				for (int j = 0; j < evaluator.getArity(); j++) {
					if (!booleanTypes[top])
						return false;
					top--;
				}
			} else if (evaluator instanceof RelationalEvaluator) {
				for (int j = 0; j < evaluator.getArity(); j++) {
					if (booleanTypes[top])
						return false;
					top--;
				}
			}
			booleanTypes[++top] = (evaluator instanceof BooleanType);
		}
		return true;
	}

	public boolean isGuaranteedToBeDivisionByZeroFree(PVariable[] variables) {
		for (int i = 0; i < evaluators.length; i++) {
			if (!(evaluators[i] instanceof DivEvaluator) && !(evaluators[i] instanceof ModEvaluator))
				continue;
			if (evaluators[i - 1] instanceof LongEvaluator && ((LongEvaluator) evaluators[i - 1]).getValue() != 0)
				continue;
			if (evaluators[i - 1] instanceof VariableEvaluator && !variables[((VariableEvaluator) evaluators[i - 1]).getPosition()].getDomain().contains(0))
				continue;
			//LOGGER.info(evaluators[i - 1]);
			return false;
		}
		return true;
	}

	public boolean isGuaranteedToBeOverflowFree(PVariable[] variables) {
		int[] lstack = new int[evaluators.length];
		// long[] lstack = new long[evaluators.length];
		double[] dstack = new double[evaluators.length];
		int top = -1;

		// LOGGER.info("d = " + Math.pow(Integer.MAX_VALUE,Integer.MAX_VALUE) + " max " + Integer.MAX_VALUE);
		for (int i = 0; i < evaluators.length; i++) {
			Evaluator evaluator = evaluators[i];
            if (evaluator instanceof LongEvaluator) {
                top++;
                // lstack[top] = Math.abs(((LongEvaluator) evaluator).getValue());
                lstack[top] = (int) Math.abs(((LongEvaluator) evaluator).getValue());
                dstack[top] = Math.abs(((LongEvaluator) evaluator).getValue());
            } else if (evaluator instanceof VariableEvaluator) {
                top++;
                int[] values = variables[((VariableEvaluator) evaluator).getPosition()].getDomain().getValues();
                int maxAbsoluteValue = Math.max(Math.abs(values[0]), Math.abs(values[values.length - 1]));
                lstack[top] = maxAbsoluteValue;
                dstack[top] = maxAbsoluteValue;
            } else if (evaluator instanceof TrueEvaluator) {
                top++;
                lstack[top] = 1;
                dstack[top] = 1;
            } else if (evaluator instanceof FalseEvaluator) {
                top++;
                lstack[top] = 0;
                dstack[top] = 0;
			}/* else if (evaluator instanceof AbsEvaluator)
				;*/
            else if (evaluator instanceof AddEvaluator) {
                top--;
                lstack[top] = lstack[top + 1] + lstack[top];
                dstack[top] = dstack[top + 1] + dstack[top];
            } else if (evaluator instanceof DivEvaluator) {
                top--;
            } else if (evaluator instanceof IfEvaluator) {
                top -= 2;
                lstack[top] = Math.max(lstack[top + 1], lstack[top]);
                dstack[top] = Math.max(dstack[top + 1], dstack[top]);
            } else if (evaluator instanceof MaxEvaluator) {
                top--;
                lstack[top] = Math.max(lstack[top + 1], lstack[top]);
                dstack[top] = Math.max(dstack[top + 1], dstack[top]);
            } else if (evaluator instanceof MinEvaluator) {
                top--;
                lstack[top] = Math.min(lstack[top + 1], lstack[top]);
                dstack[top] = Math.min(dstack[top + 1], dstack[top]);
            } else if (evaluator instanceof ModEvaluator) {
                top--;
            } else if (evaluator instanceof MulEvaluator) {
                top--;
                lstack[top] = lstack[top + 1] * lstack[top];
                dstack[top] = dstack[top + 1] * dstack[top];
			} /*else if (evaluator instanceof NegEvaluator)
				;*/
            else if (evaluator instanceof PowEvaluator) {
                top--;
                // lstack[top] = (long) Math.pow(lstack[top + 1], lstack[top]);
                lstack[top] = (int) Math.pow(lstack[top + 1], lstack[top]);
                dstack[top] = Math.pow(dstack[top + 1], dstack[top]);
            } else if (evaluator instanceof SubEvaluator) {
                top--;
                lstack[top] = lstack[top + 1] + lstack[top];
                dstack[top] = dstack[top + 1] + dstack[top];
            } else if (evaluator instanceof NotEvaluator) {
                lstack[top] = 1;
                dstack[top] = 1;
            } else if (evaluator instanceof LogicalEvaluator) {
                top--;
                lstack[top] = 1;
                dstack[top] = 1;
            } else if (evaluator instanceof RelationalEvaluator) {
                top--;
                lstack[top] = 1;
                dstack[top] = 1;
            } else
                throw new IllegalArgumentException();
            if (lstack[top] != dstack[top] || Double.isInfinite(dstack[top]))
                return false;
        }
		// LOGGER.info(" topL = " + topL + " topD = " + topD + "max = " + lstack[1]);
		return true;
	}

	public void display() {
		if(LOGGER.isLoggable(Level.INFO)) {
            StringBuilder st = new StringBuilder(128);
			for (Evaluator evaluator : evaluators) st.append(MessageFormat.format("{0} ", evaluator));
			LOGGER.info(st.toString());
		}
	}


}