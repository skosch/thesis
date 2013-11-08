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


import parser.absconparseur.InstanceTokens;
import parser.absconparseur.Toolkit;

import java.util.Stack;
import java.util.StringTokenizer;


public class PredicateManager {

	// TODO it is assumed here that if a token starts with XMLInstanceRepresentation.PARAMETER_PREFIX, it is a variable
	private static String buildFunctionalToken(String token, Stack<String> stack) {
		if (Toolkit.isInteger(token))
			return token;
		if (token.startsWith(InstanceTokens.PARAMETER_PREFIX))
			return token;
		int arity = Evaluator.getArityOf(token);
		if (arity == 0)
			return token;
		String s = token + '(' + stack.pop();
		for (int i = 1; i < arity; i++)
			s = s + ',' + stack.pop();
		return s + ')';
	}

	public static String buildFunctionalExpression(String[] postfixExpression) {
		Stack<String> stack = new Stack<String>();
		for (int i=0; i<postfixExpression.length;i++)
			stack.add(buildFunctionalToken(postfixExpression[i], stack));
		assert stack.size() == 1;
		return stack.pop();
	}

	public static String buildFunctionalExpression(String postfixExpression) {
		Stack<String> stack = new Stack<String>();
		StringTokenizer st = new StringTokenizer(postfixExpression);
		while (st.hasMoreTokens())
			stack.add(buildFunctionalToken(st.nextToken(), stack));
		assert stack.size() == 1;
		return stack.pop();
	}
	
	public static String[] extractFormalParameters(String formalParametersExpression, boolean controlRedundancy) {
		StringTokenizer st = new StringTokenizer(formalParametersExpression);
		int cpt = 0;
		String[] formalParameters = new String[st.countTokens() / 2];
		while (st.hasMoreTokens()) {
			st.nextToken(); // curently, only int is authorized as type, so we can discard this information
			String token = st.nextToken();
			if (controlRedundancy)
				for (int j = 0; j < cpt; j++)
					if (formalParameters[j].equals(token))
						return null;
			formalParameters[cpt++] = token;
		}
		return formalParameters;
	}

	public static String[] extractFormalParameters(String formalParametersExpression) {
		return extractFormalParameters(formalParametersExpression, false);
	}

//	public static String[] extractEffectiveParameters(String effectiveParametersExpression) {
//		StringTokenizer st = new StringTokenizer(effectiveParametersExpression);
//		String[] effectiveParameters = new String[st.countTokens()];
//		for (int i = 0; i < effectiveParameters.length; i++)
//			effectiveParameters[i] = st.nextToken();
//		return effectiveParameters;
//	}

	public static String[] extractUniversalEffectiveParameters(String effectiveParametersExpression, String[] variableNames) {
		//LOGGER.info(" effective = " + effectiveParametersExpression);
		//LOGGER.info(" variableNames = " + Toolkit.buildStringFromTokens(variableNames));
		
		
		StringTokenizer st = new StringTokenizer(effectiveParametersExpression);
		String[] effectiveParameters = new String[st.countTokens()];
		for (int i = 0; i < effectiveParameters.length; i++) {
			String token = st.nextToken();
			if (!Toolkit.isInteger(token)) {
				int position = Toolkit.searchFirstStringOccurrenceIn(token, variableNames);
				if (position == -1)
					throw new IllegalArgumentException();
				token = InstanceTokens.getParameterNameFor(position);
			}
			effectiveParameters[i] = token;
		}
		return effectiveParameters;
	}

	private static String buildUniversalPostfixExpression(StringTokenizer st, String[] formalParameters, boolean[] found) {
		String token = st.nextToken();
		if (Toolkit.isInteger(token))
			return token;
		int position = Toolkit.searchFirstStringOccurrenceIn(token, formalParameters);
		if (position != -1) {
			found[position] = true;
			return InstanceTokens.getParameterNameFor(position);
		}
		String s = token;
		int arity = Evaluator.getArityOf(token);
		for (int i = 0; i < arity; i++)
			s = buildUniversalPostfixExpression(st, formalParameters, found) + ' ' + s;
		return s;
	}

	public static String[] buildUniversalPostfixExpression(String functionalExpression, String[] formalParameters) {
		StringTokenizer st = new StringTokenizer(functionalExpression, InstanceTokens.WHITE_SPACE + "(),");
		boolean[] found = new boolean[formalParameters.length];
		String postfixExpression = buildUniversalPostfixExpression(st, formalParameters, found);
		for (int i = 0; i < found.length; i++)
			if (!found[i])
				throw new IllegalArgumentException("Formal parameter " + i + " not found in the given expression");
		st = new StringTokenizer(postfixExpression);
		String[] tokens = new String[st.countTokens()];
		for (int i = 0; i < tokens.length; i++)
			tokens[i] = st.nextToken();
		return tokens;
	}

	/*
	 * Builds a universal postfix expression, i.e. a postfix expression such that the name of all formal parameters are put in a canonical form (as defined by
	 * XMLInstanceRepresentation.getParameterNameFor(position))
	 */
	public static String[] buildUniversalPostfixExpression(String functionalExpression, String formalParametersExpression) {
		return buildUniversalPostfixExpression(functionalExpression, extractFormalParameters(formalParametersExpression));
	}

	private static String[] buildNewUniversalPostfixExpression(String[] universalPostfixExpression, String[] universalEffectiveParameters) {
		//LOGGER.info("univer post " + Toolkit.buildStringFromTokens(universalPostfixExpression));
		//LOGGER.info("univer eff " + Toolkit.buildStringFromTokens(universalEffectiveParameters));
		
		
		String[] tokens = new String[universalPostfixExpression.length];
		for (int i = 0; i < tokens.length; i++) {
			String token = universalPostfixExpression[i];
			if (token.startsWith(InstanceTokens.PARAMETER_PREFIX))
				tokens[i] = universalEffectiveParameters[Integer.parseInt(token.substring(InstanceTokens.PARAMETER_PREFIX.length()))];
			else
				tokens[i] = token;
		}
		return tokens;
	}

	/*
	 * Builds a new universal postfix expression, i.e. a postfix expression such that the name of all formal parameters are put in a canonical form (as defined by
	 * XMLInstanceRepresentation.getParameterNameFor(position)) after passing the effective parameters.
	 */
	public static String[] buildNewUniversalPostfixExpression(String[] universalPostfixExpression, String effectiveParametersExpression, String[] variableNames) {
		return buildNewUniversalPostfixExpression(universalPostfixExpression, extractUniversalEffectiveParameters(effectiveParametersExpression, variableNames));
	}
	
	
	public static void modifyPredicateOrder(int[] permutation, String[] predicate) {
		for (int i = 0; i < predicate.length; i++) {
			String token = predicate[i];
			if (token.startsWith(InstanceTokens.PARAMETER_PREFIX)) {
				int id = Integer.parseInt(token.substring(InstanceTokens.PARAMETER_PREFIX.length()));
				predicate[i] = InstanceTokens.getParameterNameFor(permutation[id]);
			}
		}
	}	
}
