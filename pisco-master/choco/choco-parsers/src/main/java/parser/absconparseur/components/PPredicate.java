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

package parser.absconparseur.components;

import parser.absconparseur.Toolkit;
import parser.absconparseur.intension.PredicateManager;

import static java.lang.Integer.parseInt;

public class PPredicate  extends PFunction {

	private final String name;

	private String[] formalParameters;

	private String functionalExpression;

	private final String[] unversalPostfixExpression;

    private final int index;

    public String getName() {
		return name;
	}

	public String[] getFormalParameters() {
		return formalParameters;
	}

	public String[] getUniversalPostfixExpression() {
		return unversalPostfixExpression;
	}

	public PPredicate(String name, String formalParametersExpression, String functionalExpression) {
		super(name, formalParametersExpression, functionalExpression);		
		this.name = name;
		this.formalParameters =  PredicateManager.extractFormalParameters(formalParametersExpression,true);
		this.functionalExpression = functionalExpression.trim();
		this.unversalPostfixExpression = PredicateManager.buildUniversalPostfixExpression(functionalExpression, formalParameters);
        this.index = name.hashCode(); //parseInt(name.substring(1).replaceAll("_", "00"));
    }

    public void setFormalParameters(String[] formalParameters) {
        this.formalParameters = formalParameters;
    }

    public void setFunctionalExpression(String functionalExpression) {
        this.functionalExpression = functionalExpression;
    }

    public String toString() {
		return "  predicate " + name + " with functional expression = " + functionalExpression + " and (universal) postfix expression = " + Toolkit.buildStringFromTokens(unversalPostfixExpression);
	}

    public String getFunctionalExpression() {
        return functionalExpression;
    }

    public int hashCode() {
		return index;
	}
}
