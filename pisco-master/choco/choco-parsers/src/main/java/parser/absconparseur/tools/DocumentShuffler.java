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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

public class DocumentShuffler {

	private static String changeNames(String s, Map<String, String> map) {
        StringBuilder sb = new StringBuilder(128);
		StringTokenizer st = new StringTokenizer(s);
		String name = st.nextToken();
		sb.append(map.get(name));
		while (st.hasMoreTokens()) {
			name = st.nextToken();
            sb.append(' ').append(map.get(name));
		}
		return sb.toString();
	}

	Map<String, String> variableNamesMap = new HashMap<String, String>(16);

	private static int[] buildPermutation(Random random, int size) {
		int[] values = new int[size];
		for (int i = 0; i < values.length; i++)
			values[i] = i;
        int[] t = new int[size];
		for (int i = 0; i < size; i++) {
			int j = random.nextInt(size - i);
			t[i] = values[j];
			values[j] = values[size - i - 1];
        }
		return t;
	}

	private static Map<String, String> modifyOrder(Random random, Element parent, NodeList nodeList, boolean variables, int mode) {
		Map<String, String> variablesMap = new HashMap<String, String>(16);
		int[] permutation = buildPermutation(random, nodeList.getLength());
		Element[] elements = new Element[nodeList.getLength()];
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element element = (Element) nodeList.item(i);
			String name = element.getAttribute(InstanceTokens.NAME);
			String canonicalName = null;
			if (variables)
				canonicalName = mode == 2 ? name : InstanceTokens.getVariableNameFor(permutation[i]);
			else
				canonicalName = mode == 1 ? name : InstanceTokens.getConstraintNameFor(permutation[i]);
			// String canonicalName = (variables ? InstanceTokens.getVariableNameFor(permutation[i]) : InstanceTokens.getConstraintNameFor(permutation[i]));
			element.setAttribute(InstanceTokens.NAME, canonicalName);
			variablesMap.put(name, canonicalName);
			if (variables)
				elements[mode == 2 ? i : permutation[i]] = element;
			else
				elements[mode == 1 ? i : permutation[i]] = element;
			//elements[permutation[i]] = element;
		}
		for (int i = 0; i < nodeList.getLength(); i++)
			parent.removeChild(nodeList.item(i));
		for (int i = 0; i < elements.length; i++)
			parent.appendChild(elements[i]);
		return variablesMap;
	}

	public static Document shuffle(Document document, int seed, int mode) {
		Random random = new Random(seed);

		Element variablesElement = XMLManager.getFirstElementByTagNameFromRoot(document, InstanceTokens.VARIABLES);
		NodeList nodeList = variablesElement.getElementsByTagName(InstanceTokens.VARIABLE);
		Map<String, String> variablesMap = modifyOrder(random, variablesElement, nodeList, true,mode);

		// Map<String, String> relationsMap = new HashMap<String, String>();
		// Element rels = getFirstElementByTagNameFromRoot(document, XMLInstanceRepresentation.RELATIONS);
		// if (rels != null) {
		// nodeList = rels.getElementsByTagName(XMLInstanceRepresentation.RELATION);
		//
		// for (int i = 0; i < nodeList.getLength(); i++) {
		// Element element = (Element) nodeList.item(i);
		// String name = element.getAttribute(XMLInstanceRepresentation.NAME);
		// String canonicalName = XMLInstanceRepresentation.getRelationNameFor(i);
		// element.setAttribute(XMLInstanceRepresentation.NAME, canonicalName);
		// relationsMap.put(name, canonicalName);
		// }
		// }

		Element constraintsElement = XMLManager.getFirstElementByTagNameFromRoot(document, InstanceTokens.CONSTRAINTS);
		nodeList = constraintsElement.getElementsByTagName(InstanceTokens.CONSTRAINT);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element element = (Element) nodeList.item(i);
			// String name = element.getAttribute("name");
			String canonicalName = InstanceTokens.getConstraintNameFor(i);
			element.setAttribute(InstanceTokens.NAME, canonicalName);

			String scope = element.getAttribute(InstanceTokens.SCOPE);
			element.setAttribute(InstanceTokens.SCOPE, changeNames(scope, variablesMap));
			// String reference = element.getAttribute(XMLInstanceRepresentation.REFERENCE);
			Element parameters = XMLManager.getElementByTagNameFrom(element, InstanceTokens.PARAMETERS, 0);
			if (parameters != null) {
				StringBuilder canonicalExpression = new StringBuilder(128);
				//LOGGER.info("before " + parameters.getTextContent());
				StringTokenizer st = new StringTokenizer(parameters.getTextContent());
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					if (variablesMap.containsKey(token))
                        canonicalExpression.append(' ').append(variablesMap.get(token));
					else
                        canonicalExpression.append(' ').append(token);
				}
				//LOGGER.info("after " + canonicalExpression);
				
				parameters.setTextContent(canonicalExpression.toString().trim());
			}
		}

		modifyOrder(random, constraintsElement, nodeList, false,mode);
		return document;
	}
}
