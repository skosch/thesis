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

package parser.absconparseur.tools;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import parser.absconparseur.InstanceTokens;
import parser.absconparseur.XMLManager;
import parser.absconparseur.components.PRelation;
import parser.absconparseur.components.PSoftRelation;
import parser.absconparseur.intension.PredicateManager;

import java.util.*;


public class DocumentModifier {

	public static boolean isPresentChild(Document document, String tagName) {
		NodeList nodeList = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++)
			if (nodeList.item(i).getNodeName().equals(tagName))
				return true;
		return false;
	}

	public static boolean areOrderedChilds(Document document, String tagName1, String tagName2) {
		NodeList nodeList = document.getDocumentElement().getChildNodes();
		int i = 0;
		for (i = 0; i < nodeList.getLength(); i++)
			if (nodeList.item(i).getNodeName().equals(tagName1))
				break;
		if (i >= nodeList.getLength())
			return false;
		i++;
		for (; i < nodeList.getLength(); i++)
			if (nodeList.item(i).getNodeName().equals(tagName2))
				return true;
		return false;
	}

	public static Document modifyDocumentFrom(InstanceCheckerEngine logic, Document document, InstanceCheckerParser problem) {
		Element functionsElement = XMLManager.getElementByTagNameFrom(document.getDocumentElement(), InstanceTokens.FUNCTIONS, 0);
		if (functionsElement != null)
			document.getDocumentElement().removeChild(functionsElement);

		Element predicatesElement = XMLManager.getElementByTagNameFrom(document.getDocumentElement(), InstanceTokens.PREDICATES, 0);
		if (predicatesElement != null)
			document.getDocumentElement().removeChild(predicatesElement);

		Element constraintsElement = XMLManager.getElementByTagNameFrom(document.getDocumentElement(), InstanceTokens.CONSTRAINTS, 0);
		NodeList nodeList = constraintsElement.getElementsByTagName(InstanceTokens.CONSTRAINT);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element element = (Element) nodeList.item(i);

			String reference = element.getAttribute(InstanceTokens.REFERENCE);
			if (problem.getFunctionsMap().containsKey(reference) || problem.getPredicatesMap().containsKey(reference)) {
				Element parameters = XMLManager.getElementByTagNameFrom(element, InstanceTokens.PARAMETERS, 0);
				element.removeChild(parameters);
				String name = element.getAttribute(InstanceTokens.NAME);
				element.setAttribute(InstanceTokens.REFERENCE, problem.getConstraintsToNewRelations().get(name));
			}
		}
		Element relationsElement = XMLManager.getElementByTagNameFrom(document.getDocumentElement(), InstanceTokens.RELATIONS, 0);
		if (relationsElement == null) {
			relationsElement = document.createElement(InstanceTokens.RELATIONS);
			document.getDocumentElement().insertBefore(relationsElement, constraintsElement);
		}
		relationsElement.setAttribute(InstanceTokens.NB_RELATIONS, String.valueOf(problem.getRelationsMap().size() + problem.getNewRelations().size())); // relations.size() + "");

		// int spotLimit = (problem.getNewRelations().size() / 20);
		int cpt = 0;
		for (PRelation relation : problem.getNewRelations()) {
			cpt++;
			Element relationElement = document.createElement(InstanceTokens.RELATION);
			relationElement.setAttribute(InstanceTokens.NAME, relation.getName());
			relationElement.setAttribute(InstanceTokens.ARITY, String.valueOf(relation.getArity()));
			relationElement.setAttribute(InstanceTokens.NB_TUPLES, String.valueOf(relation.getTuples().length));
			relationElement.setAttribute(InstanceTokens.SEMANTICS, relation.getSemantics());
			if (relation instanceof PSoftRelation)
				relationElement.setAttribute(InstanceTokens.DEFAULT_COST, String.valueOf(relation.getDefaultCost()));
			
			relationElement.setTextContent(relation.getStringListOfTuples());
			relationsElement.appendChild(relationElement);
			if (cpt % 10 == 0)
				logic.spot();
		}
		return document;
	}

	private static String changeToCanonicalNames(String s, Map<String, String> map) {
        StringBuilder sb = new StringBuilder(128);
		StringTokenizer st = new StringTokenizer(s);

		// String name = st.nextToken();
		// sb.append(map.get(name));
		do {
			String name = st.nextToken();
			String newName = map.get(name);
			if (newName == null)
				sb.append(name);
			else
				sb.append(newName);
			if (st.hasMoreTokens())
				sb.append(' ');
		} while (st.hasMoreTokens());
		// {
		// name = st.nextToken();
		// sb.append(" " + map.get(name));
		// }
		return sb.toString();
	}

	public static Document setCanonicalFormOf(InstanceCheckerEngine logic, Document document, boolean canonicalNames, int maxConstraintArity) {
		Element presentationElement = XMLManager.getFirstElementByTagNameFromRoot(document, InstanceTokens.PRESENTATION);
		presentationElement.removeAttribute(InstanceTokens.NAME);
		presentationElement.removeAttribute(InstanceTokens.NB_SOLUTIONS);
		presentationElement.removeAttribute(InstanceTokens.SOLUTION);
		presentationElement.setTextContent("");
		presentationElement.setAttribute(InstanceTokens.MAX_CONSTRAINT_ARITY, String.valueOf(maxConstraintArity));
		presentationElement.setAttribute(InstanceTokens.FORMAT, InstanceTokens.XCSP_2_1);
		logic.spot();

		// LOGGER.info("can " + canonicalNames);

		if (canonicalNames)
			return document;

		Map<String, String> domainsMap = new HashMap<String, String>(16);
		NodeList nodeList = XMLManager.getFirstElementByTagNameFromRoot(document, InstanceTokens.DOMAINS).getElementsByTagName(InstanceTokens.DOMAIN);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element element = (Element) nodeList.item(i);
			String name = element.getAttribute(InstanceTokens.NAME);
			String canonicalName = InstanceTokens.getDomainNameFor(i);
			if (!name.equals(canonicalName)) {
				element.setAttribute(InstanceTokens.NAME, canonicalName);
				domainsMap.put(name, canonicalName);
			}
		}
		logic.spot();

		Map<String, String> variablesMap = new HashMap<String, String>(16);
		nodeList = XMLManager.getFirstElementByTagNameFromRoot(document, InstanceTokens.VARIABLES).getElementsByTagName(InstanceTokens.VARIABLE);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element element = (Element) nodeList.item(i);
			String name = element.getAttribute(InstanceTokens.NAME);
			String canonicalName = InstanceTokens.getVariableNameFor(i);
			if (!name.equals(canonicalName)) {
				element.setAttribute(InstanceTokens.NAME, canonicalName);
				variablesMap.put(name, canonicalName);
			}
			if (domainsMap.isEmpty())
				continue;

			String domainName = element.getAttribute(InstanceTokens.DOMAIN);
			String canonicalDomainName = domainsMap.get(domainName);
			if (canonicalDomainName != null)
				element.setAttribute(InstanceTokens.DOMAIN, canonicalDomainName);
		}
		logic.spot();

		Map<String, String> relationsMap = new HashMap<String, String>(16);
		Element rels = XMLManager.getFirstElementByTagNameFromRoot(document, InstanceTokens.RELATIONS);
		if (rels != null) {
			nodeList = rels.getElementsByTagName(InstanceTokens.RELATION);

			for (int i = 0; i < nodeList.getLength(); i++) {
				Element element = (Element) nodeList.item(i);
				String name = element.getAttribute(InstanceTokens.NAME);
				String canonicalName = InstanceTokens.getRelationNameFor(i);
				if (!name.equals(canonicalName))
					element.setAttribute(InstanceTokens.NAME, canonicalName);
				relationsMap.put(name, canonicalName); // To put in any case
			}
		}
		logic.spot();

			// A ELIMINER
			// preds.setAttribute(XMLInstanceRepresentation.NB_PREDICATES,"1");
		Map<String, String> functionsMap = new HashMap<String, String>(16);
		Element funcs = XMLManager.getFirstElementByTagNameFromRoot(document, InstanceTokens.FUNCTIONS);

		if (funcs != null) {

			nodeList = funcs.getElementsByTagName(InstanceTokens.FUNCTION);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element element = (Element) nodeList.item(i);
				String name = element.getAttribute(InstanceTokens.NAME);
				String canonicalName = InstanceTokens.getFunctionNameFor(i);
				if (!name.equals(canonicalName))
					element.setAttribute(InstanceTokens.NAME, canonicalName);
				functionsMap.put(name, canonicalName);// To put in any case

				Element parameters = XMLManager.getElementByTagNameFrom(element, InstanceTokens.PARAMETERS, 0);
				Element expression = XMLManager.getElementByTagNameFrom(element, InstanceTokens.EXPRESSION, 0);
				Element functional = XMLManager.getElementByTagNameFrom(expression, InstanceTokens.FUNCTIONAL, 0);

				Map<String, String> parametersMap = new HashMap<String, String>(16);
				String oldFormalParameters = parameters.getTextContent();
				StringBuilder newFormalParameters = new StringBuilder(16);

				List<String> formalParameters = new ArrayList<String>(16);
				StringTokenizer st = new StringTokenizer(oldFormalParameters);
				int cpt = 0;
				while (st.hasMoreTokens()) {
                    newFormalParameters.append(' ').append(st.nextToken()); // type is int and not used
					String oldParameter = st.nextToken();
					String newParameter = InstanceTokens.getParameterNameFor(cpt++);
					parametersMap.put(oldParameter, newParameter);
					formalParameters.add(oldParameter);
					// LOGGER.info("old = " + oldParameter + " new = " + newParameter);
                    newFormalParameters.append(' ').append(newParameter);
				}

				parameters.setTextContent(newFormalParameters.toString().trim());

				String[] t = PredicateManager.buildUniversalPostfixExpression(functional.getTextContent().trim(), formalParameters.toArray(new String[formalParameters.size()]));
				String s2 = PredicateManager.buildFunctionalExpression(t);
				functional.setTextContent(s2.trim());
			}
		}
		logic.spot();
				
		
		Map<String, String> predicatesMap = new HashMap<String, String>(16);
		Element preds = XMLManager.getFirstElementByTagNameFromRoot(document, InstanceTokens.PREDICATES);

		if (preds != null) {

			// A ELIMINER
			// preds.setAttribute(XMLInstanceRepresentation.NB_PREDICATES,"1");

			nodeList = preds.getElementsByTagName(InstanceTokens.PREDICATE);
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element element = (Element) nodeList.item(i);

				// A ELIMINER
				// if (i == 0) {
				// preds.removeChild(element);
				// continue;
				// }

				String name = element.getAttribute(InstanceTokens.NAME);
				String canonicalName = InstanceTokens.getPredicateNameFor(i);
				if (!name.equals(canonicalName))
					element.setAttribute(InstanceTokens.NAME, canonicalName);
				predicatesMap.put(name, canonicalName);// To put in any case

				Element parameters = XMLManager.getElementByTagNameFrom(element, InstanceTokens.PARAMETERS, 0);
				Element expression = XMLManager.getElementByTagNameFrom(element, InstanceTokens.EXPRESSION, 0);
				Element functional = XMLManager.getElementByTagNameFrom(expression, InstanceTokens.FUNCTIONAL, 0);

				Map<String, String> parametersMap = new HashMap<String, String>(16);
				String oldFormalParameters = parameters.getTextContent();
				StringBuilder newFormalParameters = new StringBuilder(128);

				List<String> formalParameters = new ArrayList<String>(16);
				StringTokenizer st = new StringTokenizer(oldFormalParameters);
				int cpt = 0;
				while (st.hasMoreTokens()) {
                    newFormalParameters.append(' ').append(st.nextToken()); // type is int and not used
					String oldParameter = st.nextToken();
					String newParameter = InstanceTokens.getParameterNameFor(cpt++);
					parametersMap.put(oldParameter, newParameter);
					formalParameters.add(oldParameter);
					// System.out.println("old = " + oldParameter + " new = " + newParameter);
                    newFormalParameters.append(' ').append(newParameter);
				}

				parameters.setTextContent(newFormalParameters.toString().trim());

				String[] t = PredicateManager.buildUniversalPostfixExpression(functional.getTextContent().trim(), formalParameters.toArray(new String[formalParameters.size()]));
				String s2 = PredicateManager.buildFunctionalExpression(t);
				// LOGGER.info("s1 = " + s1 + " s2 = " + s2);

				// String canonicalExpression = "";
				// st = new StringTokenizer(functional.getTextContent(), "(), ");
				// while (st.hasMoreTokens()) {
				// String token = st.nextToken();
				// LOGGER.info("token = " + token + " belong = " + parametersMap.containsKey(token));
				//
				// if (parametersMap.containsKey(token))
				// canonicalExpression += " " + parametersMap.get(token);
				// else
				// canonicalExpression += " " + token;
				// }
				// functional.setTextContent(canonicalExpression.trim());
				functional.setTextContent(s2.trim());
			}
		}
		logic.spot();

		nodeList = XMLManager.getFirstElementByTagNameFromRoot(document, InstanceTokens.CONSTRAINTS).getElementsByTagName(InstanceTokens.CONSTRAINT);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element element = (Element) nodeList.item(i);

			// LOGGER.info(" constraint " + i);
			String name = element.getAttribute(InstanceTokens.NAME);
			String canonicalName = InstanceTokens.getConstraintNameFor(i);
			if (!name.equals(canonicalName))
				element.setAttribute(InstanceTokens.NAME, canonicalName);

			if (!variablesMap.isEmpty()) {
				String scope = element.getAttribute(InstanceTokens.SCOPE);
				element.setAttribute(InstanceTokens.SCOPE, changeToCanonicalNames(scope, variablesMap));
			}

			String reference = element.getAttribute(InstanceTokens.REFERENCE);
			if (relationsMap.containsKey(reference))
				element.setAttribute(InstanceTokens.REFERENCE, relationsMap.get(reference));
			else if (functionsMap.containsKey(reference)) {
				element.setAttribute(InstanceTokens.REFERENCE, functionsMap.get(reference));

				if (!variablesMap.isEmpty()) {
					Element parameters = XMLManager.getElementByTagNameFrom(element, InstanceTokens.PARAMETERS, 0);
					StringBuilder canonicalExpression = new StringBuilder(128);
					StringTokenizer st = new StringTokenizer(parameters.getTextContent());
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						String newToken = variablesMap.get(token);
						if (newToken != null)
                            canonicalExpression.append(' ').append(newToken);
						else
                            canonicalExpression.append(' ').append(token);
					}
					parameters.setTextContent(canonicalExpression.toString().trim());
				}
			}
			else if (predicatesMap.containsKey(reference)) {
				element.setAttribute(InstanceTokens.REFERENCE, predicatesMap.get(reference));

				if (!variablesMap.isEmpty()) {
					Element parameters = XMLManager.getElementByTagNameFrom(element, InstanceTokens.PARAMETERS, 0);
					StringBuilder canonicalExpression = new StringBuilder(128);
					StringTokenizer st = new StringTokenizer(parameters.getTextContent());
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						String newToken = variablesMap.get(token);
						if (newToken != null)
                            canonicalExpression.append(' ').append(newToken);
						else
                            canonicalExpression.append(' ').append(token);
					}
					parameters.setTextContent(canonicalExpression.toString().trim());
				}
			}
		}
		logic.spot();
		return document;
	}
}
