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

import choco.kernel.common.logging.ChocoLogging;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import parser.absconparseur.InstanceTokens;
import parser.absconparseur.PredicateTokens;
import parser.absconparseur.Toolkit;
import parser.absconparseur.XMLManager;
import parser.absconparseur.components.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class corresponds to a Java parser that uses DOM (Document Object Model) to parse CSP and WCSP instances in format "XCSP 2.1". <br>
 * Here, we assume that the instance is well-formed (valid). This class is given for illustration purpose. Feel free to adapt it !
 * 
 * @author christophe lecoutre
 * @version 2.1.1
 */
public class InstanceParser {

	protected final static Logger LOGGER = ChocoLogging.getMainLogger();

	public static final String VERSION = "version 2.1.4 (November 4, 2008)";

	public Document document;

	private String type;

    private int maxConstraintArity;

	private Map<String, PDomain> mapOfDomains;

	private Map<String, PVariable> mapOfVariables;

	private Map<String, PRelation> mapOfRelations;

	private Map<String, PFunction> mapOfFunctions;

	private Map<String, PPredicate> mapOfPredicates;

	private Map<String, PConstraint> mapOfConstraints;

	private PVariable[] variables;

	private int nbExtensionConstraints;

	private int nbSoftIntensionConstraints;

	private int nbIntensionConstraints;

	private int nbGlobalConstraints;

	private int nbDomains;
	/**
	 * Used for WCSP
	 */
	//private long maximalCost;

	/**
	 * Used for WCSP
	 */
	//private long intialCost;

	private String satisfiable;

	private String minViolatedConstraints;

	public String getType() {
		return type;
	}

	public PVariable[] getVariables() {
		return variables;
	}

	public int getNbVariables() {
		return variables.length;
	}

	public int getMaxConstraintArity() {
		return maxConstraintArity;
	}

	public Map<String, PConstraint> getMapOfConstraints() {
		return mapOfConstraints;
	}

	public Map<String, PDomain> getMapOfDomains() {
		return mapOfDomains;
	}

	public Map<String, PRelation> getMapOfRelations() {
		return mapOfRelations;
	}

	public Map<String, PPredicate> getMapOfPredicat() {
		return mapOfPredicates;
	}
	

	public Map<String, PVariable> getMapOfVariables() {
		return mapOfVariables;
	}

	public int getNbExtensionConstraints() {
		return nbExtensionConstraints;
	}

	public int getNbIntensionConstraints() {
		return nbIntensionConstraints;
	}

	public int getNbGlobalConstraints() {
		return nbGlobalConstraints;
	}

	public String getConstraintsCategory() {
		return (nbExtensionConstraints > 0 ? "E" : "") + (nbIntensionConstraints > 0 ? "I" : "") + (nbGlobalConstraints > 0 ? "G" : "");
	}

	public int getNBDomain() {
		return nbDomains;
	}

	public String getSatisfiable() {
		return satisfiable;
	}

	public String getMinViolatedConstraints() {
		return minViolatedConstraints;
	}

	/**
	 * Used to determine if elements of the instance must be displayed when parsing.
	 */
	private boolean displayInstance = true;

	/**
	 * Build a DOM object that corresponds to the file whose name is given. <br>
	 * The file must represent a CSP instance according to format XCSP 2.1
	 * 
	 * @param fileName the name of a file representing a CSP instance.
	 */
	public void loadInstance(String fileName) {
		document = XMLManager.load(fileName);
	}

	private void parsePresentation(Element presentationElement) {
		String s = presentationElement.getAttribute(InstanceTokens.MAX_CONSTRAINT_ARITY.trim());
		maxConstraintArity = s.length() == 0 || s.equals("?") ? -1 : Integer.parseInt(s);
		type = presentationElement.getAttribute(InstanceTokens.TYPE.trim());
		type = type.length() == 0 || type.equals("?") ? InstanceTokens.CSP : type;
        final String format = presentationElement.getAttribute(InstanceTokens.FORMAT.trim());
		if (displayInstance)
			LOGGER.info("Instance with maxConstraintArity=" + maxConstraintArity + " type=" + type + " format=" + format);
		s = presentationElement.getAttribute(InstanceTokens.NB_SOLUTIONS).trim();
		satisfiable = s.length() == 0 || s.equals("?") ? "unknown" : s.equals("0") ? "false" : "true";
		s = presentationElement.getAttribute(InstanceTokens.MIN_VIOLATED_CONSTRAINTS).trim();
		minViolatedConstraints = satisfiable.equals("true") ? "0" : s.length() == 0 || s.equals("?") ? "unknown" : s;
	}

	private static int[] parseDomainValues(int nbValues, String stringOfValues) {
		int cnt = 0;
		int[] values = new int[nbValues];
		StringTokenizer st = new StringTokenizer(stringOfValues);
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			int position = token.indexOf(InstanceTokens.DISCRETE_INTERVAL_SEPARATOR);
			if (position == -1)
				values[cnt++] = Integer.parseInt(token);
			else {
				int min = Integer.parseInt(token.substring(0, position));
				int max = Integer.parseInt(token.substring(position + InstanceTokens.DISCRETE_INTERVAL_SEPARATOR.length()));
				for (int j = min; j <= max; j++)
					values[cnt++] = j;
			}
		}
		return values;
	}

	private static PDomain parseDomain(Element domainElement) {
		String name = domainElement.getAttribute(InstanceTokens.NAME);
		int nbValues = Integer.parseInt(domainElement.getAttribute(InstanceTokens.NB_VALUES));
		int[] values = parseDomainValues(nbValues, domainElement.getTextContent());
		if (nbValues != values.length)
			throw new RuntimeException();
		return new PDomain(name, values);
	}

	private void parseDomains(Element domainsElement) {
		mapOfDomains = new HashMap<String, PDomain>(16);
		int nbDomains = Integer.parseInt(domainsElement.getAttribute(InstanceTokens.NB_DOMAINS));
		if (displayInstance)
			LOGGER.log(Level.INFO, "=> {0} domains",nbDomains);

		NodeList nodeList = domainsElement.getElementsByTagName(InstanceTokens.DOMAIN);
		for (int i = 0; i < nodeList.getLength(); i++) {
			PDomain domain = parseDomain((Element) nodeList.item(i));
			mapOfDomains.put(domain.getName(), domain);
			if (displayInstance)
				LOGGER.info(String.valueOf(domain));
		}
	}

	private PVariable parseVariable(Element variableElement) {
		String name = variableElement.getAttribute(InstanceTokens.NAME);
		String domainName = variableElement.getAttribute(InstanceTokens.DOMAIN);
		return new PVariable(name, mapOfDomains.get(domainName));
	}

	private void parseVariables(Element variablesElement) {
		mapOfVariables = new HashMap<String, PVariable>(16);
		int nbVariables = Integer.parseInt(variablesElement.getAttribute(InstanceTokens.NB_VARIABLES));
		if (displayInstance)
			LOGGER.info("=> " + nbVariables + " variables");

		variables = new PVariable[nbVariables];
		NodeList nodeList = variablesElement.getElementsByTagName(InstanceTokens.VARIABLE);
		for (int i = 0; i < nodeList.getLength(); i++) {
			PVariable variable = parseVariable((Element) nodeList.item(i));
			mapOfVariables.put(variable.getName(), variable);
			variables[i] = variable;
			if (displayInstance)
				LOGGER.info(String.valueOf(variable));
		}
	}

	private static PRelation parseRelationTuples(String name, int arity, int nbTuples, String semantics, String textContent) {
		int[][] tuples = new int[nbTuples][arity];
		StringTokenizer st = new StringTokenizer(textContent, InstanceTokens.WHITE_SPACE + InstanceTokens.TUPLES_SEPARATOR);
		for (int i = 0; i < tuples.length; i++)
			for (int j = 0; j < arity; j++)
				tuples[i][j] = Integer.parseInt(st.nextToken());
		return new PRelation(name, arity, nbTuples, semantics, tuples);
	}

	private static PRelation parseSoftRelationTuples(String name, int arity, int nbTuples, String semantics, String textContent, String textDefaultCost) {
		int[][] tuples = new int[nbTuples][arity];
		int[] weights = new int[nbTuples];
		StringTokenizer st = new StringTokenizer(textContent, InstanceTokens.WHITE_SPACE + InstanceTokens.TUPLES_SEPARATOR);
		int currentCost = -2;
		for (int i = 0; i < nbTuples; i++) {
			String token = st.nextToken();
			int costFlagPosition = token.lastIndexOf(InstanceTokens.COST_SEPARATOR);
			if (costFlagPosition != -1) {
				currentCost = Integer.parseInt(token.substring(0, costFlagPosition));
				token = token.substring(costFlagPosition + 1);
			}
			weights[i] = currentCost;
			tuples[i][0] = Integer.parseInt(token);
			for (int j = 1; j < arity; j++)
				tuples[i][j] = Integer.parseInt(st.nextToken());
		}
		int defaultCost = textDefaultCost.equals(InstanceTokens.INFINITY) ? Integer.MAX_VALUE : Integer.parseInt(textDefaultCost);
		return new PSoftRelation(name, arity, nbTuples, semantics, tuples, weights, defaultCost);
	}

	private static PRelation parseRelation(Element relationElement) {
		String name = relationElement.getAttribute(InstanceTokens.NAME);
		int arity = Integer.parseInt(relationElement.getAttribute(InstanceTokens.ARITY));
		int nbTuples = Integer.parseInt(relationElement.getAttribute(InstanceTokens.NB_TUPLES));
		String semantics = relationElement.getAttribute(InstanceTokens.SEMANTICS);
		if (semantics.equals(InstanceTokens.SOFT))
			return parseSoftRelationTuples(name, arity, nbTuples, semantics, relationElement.getTextContent(), relationElement.getAttribute(InstanceTokens.DEFAULT_COST));
		else
			return parseRelationTuples(name, arity, nbTuples, semantics, relationElement.getTextContent());
	}

	private void parseRelations(Element relationsElement) {
		mapOfRelations = new HashMap<String, PRelation>(16);
		if (relationsElement == null)
			return;
		if (displayInstance)
			LOGGER.log(Level.INFO, "=> {0} nbRelations", relationsElement.getAttribute(InstanceTokens.NB_RELATIONS));
		NodeList nodeList = relationsElement.getElementsByTagName(InstanceTokens.RELATION);
		for (int i = 0; i < nodeList.getLength(); i++) {
			PRelation relation = parseRelation((Element) nodeList.item(i));
			mapOfRelations.put(relation.getName(), relation);
			if (displayInstance)
				LOGGER.log(Level.INFO, "{0}", relation);
		}
	}

	private static PPredicate parsePredicate(Element predicateElement) {
		String name = predicateElement.getAttribute(InstanceTokens.NAME);
		Element parameters = (Element) predicateElement.getElementsByTagName(InstanceTokens.PARAMETERS).item(0);
		Element expression = (Element) predicateElement.getElementsByTagName(InstanceTokens.EXPRESSION).item(0);
		Element functional = (Element) expression.getElementsByTagName(InstanceTokens.FUNCTIONAL).item(0);
		return new PPredicate(name, parameters.getTextContent().trim(), functional.getTextContent().trim());
	}

	private void parsePredicates(Element predicatesElement) {
		mapOfPredicates = new HashMap<String, PPredicate>(16);
		if (predicatesElement == null)
			return;
		int nbPredicates = Integer.parseInt(predicatesElement.getAttribute(InstanceTokens.NB_PREDICATES));
		if (displayInstance)
			LOGGER.log(Level.INFO, "=> {0} predicates", nbPredicates);

		NodeList nodeList = predicatesElement.getElementsByTagName(InstanceTokens.PREDICATE);
		for (int i = 0; i < nodeList.getLength(); i++) {
			PPredicate predicate = parsePredicate((Element) nodeList.item(i));
			mapOfPredicates.put(predicate.getName(), predicate);
			if (displayInstance)
				LOGGER.log(Level.INFO, "{0}", predicate);
		}
	}

	private static PFunction parseFunction(Element functionElement) {
		String name = functionElement.getAttribute(InstanceTokens.NAME);
		Element parameters = (Element) functionElement.getElementsByTagName(InstanceTokens.PARAMETERS).item(0);
		Element expression = (Element) functionElement.getElementsByTagName(InstanceTokens.EXPRESSION).item(0);
		Element functional = (Element) expression.getElementsByTagName(InstanceTokens.FUNCTIONAL).item(0);
		return new PFunction(name, parameters.getTextContent(), functional.getTextContent());
	}

	private void parseFunctions(Element functionsElement) {
		mapOfFunctions = new HashMap<String, PFunction>(16);
		if (functionsElement == null)
			return;
		int nbFunctions = Integer.parseInt(functionsElement.getAttribute(InstanceTokens.NB_FUNCTIONS));
		if (displayInstance)
			System.out.println("=> " + nbFunctions + " functions");

		NodeList nodeList = functionsElement.getElementsByTagName(InstanceTokens.FUNCTION);
		for (int i = 0; i < nodeList.getLength(); i++) {
			PFunction function = parseFunction((Element) nodeList.item(i));
			mapOfFunctions.put(function.getName(), function);
			if (displayInstance)
				System.out.println(function);
		}
	}

	private PVariable[] parseScope(String scope) {
		StringTokenizer st = new StringTokenizer(scope, " ");
		PVariable[] involvedVariables = new PVariable[st.countTokens()];
		for (int i = 0; i < involvedVariables.length; i++)
			involvedVariables[i] = mapOfVariables.get(st.nextToken());
		return involvedVariables;
	}

	private static int searchIn(String s, PVariable[] t) {
		for (int i = 0; i < t.length; i++)
			if (t[i].getName().equals(s))
				return i;
		return -1;
	}

	private PConstraint parseElementConstraint(String name, PVariable[] scope, Element parameters) {
		StringTokenizer st = new StringTokenizer(Toolkit.insertWhitespaceAround(parameters.getTextContent().trim(), InstanceTokens.BRACKETS), InstanceTokens.WHITE_SPACE);
		PVariable index = mapOfVariables.get(st.nextToken()); // index is necessarily a variable
		st.nextToken(); // token [ skipped
		List<Object> table = new ArrayList<Object>(16);
		String token = st.nextToken();
		while (!token.equals("]")) {
			Object object = mapOfVariables.get(token);
			if (object == null)
				object = Integer.parseInt(token);
			table.add(object);
			token = st.nextToken();
		}
		token = st.nextToken();
		Object value = mapOfVariables.get(token);
		if (value == null)
			value = Integer.parseInt(token);
		return new PElement(name, scope, index, table.toArray(new Object[table.size()]), value);
	}

	private static PConstraint parseWeightedSumConstraint(String name, PVariable[] scope, Element parameters) {
		NodeList nodeList = parameters.getChildNodes();
		StringTokenizer st = new StringTokenizer(nodeList.item(0).getTextContent(), InstanceTokens.WHITE_SPACE + "[{}]");
		int[] coeffs = new int[scope.length];
		while (st.hasMoreTokens()) {
			int coeff = Integer.parseInt(st.nextToken());
			int position = searchIn(st.nextToken(), scope);
			coeffs[position] += coeff;
		}
		PredicateTokens.RelationalOperator operator = PredicateTokens.RelationalOperator.getRelationalOperatorFor(nodeList.item(1).getNodeName());
		int limit = Integer.parseInt(nodeList.item(2).getTextContent().trim());
		return new PWeightedSum(name, scope, coeffs, operator, limit);
	}

	protected static String buildStringRepresentationOf(Element parameters) {
		NodeList nodeList = parameters.getChildNodes();
        StringBuilder sb = new StringBuilder(128);
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeName().equals(InstanceTokens.NIL)) {
				sb.append(' ');
				sb.append(InstanceTokens.NIL);
				sb.append(' ');
			} else
				sb.append(Toolkit.insertWhitespaceAround(node.getTextContent(), InstanceTokens.BRACKETS));
		}
		return sb.toString();
	}

	protected PConstraint parseCumulativeConstraint(String name, PVariable[] scope, Element parameters) {
		StringTokenizer st = new StringTokenizer(buildStringRepresentationOf(parameters), InstanceTokens.WHITE_SPACE + "{}");
		st.nextToken(); // token '[' skipped
		List<PTask> tasks = new ArrayList<PTask>(16);
		String token = st.nextToken();
		while (!token.equals("]")) {
			Object origin = mapOfVariables.get(token);
			if (origin == null)
				origin = token.equals(InstanceTokens.NIL) ? null : Integer.parseInt(token);
			token = st.nextToken();
			Object duration = mapOfVariables.get(token);
			if (duration == null)
				duration = token.equals(InstanceTokens.NIL) ? null : Integer.parseInt(token);
			token = st.nextToken();
			Object end = mapOfVariables.get(token);
			if (end == null)
				end = token.equals(InstanceTokens.NIL) ? null : Integer.parseInt(token);
			token = st.nextToken();
			Object height = mapOfVariables.get(token);
			if (height == null)
				height = Integer.parseInt(token);
			tasks.add(new PTask(origin, duration, end, height));
			token = st.nextToken();
		}
		int limit = Integer.parseInt(st.nextToken());
		return new PCumulative(name, scope, tasks.toArray(new PTask[tasks.size()]), limit);
	}


	protected PConstraint parseDisjunctiveConstraint(String name, PVariable[] scope, Element parameters) {
		StringTokenizer st = new StringTokenizer(buildStringRepresentationOf(parameters), InstanceTokens.WHITE_SPACE + "{}");
		st.nextToken(); // token '[' skipped
		List<PTask> tasks = new ArrayList<PTask>(16);
		String token = st.nextToken();
		while (!token.equals("]")) {
			Object origin = mapOfVariables.get(token);
			if (origin == null)
				origin = token.equals(InstanceTokens.NIL) ? null : Integer.parseInt(token);
			token = st.nextToken();
			Object duration = mapOfVariables.get(token);
			if (duration == null)
				duration = token.equals(InstanceTokens.NIL) ? null : Integer.parseInt(token);
			tasks.add(new PTask(origin, duration, null, 1));
			token = st.nextToken();
		}
		return new PDisjunctive(name, scope, tasks.toArray(new PTask[tasks.size()]));
	}


	private PConstraint parseGlobalCardinalityConstraint(String name, PVariable[] involvedVariables, Element parameters){
		StringTokenizer st = new StringTokenizer(buildStringRepresentationOf(parameters));
		st.nextToken(); // token '[' skipped
		Set<PVariable> involvedVariablesInParameters = new HashSet<PVariable>(16);
		String token = st.nextToken();
		List<Object> table = new ArrayList<Object>(16);
		while (!token.equals("]")) {
			Object var = mapOfVariables.get(token);
			if (var == null)
				var = token.equals(InstanceTokens.NIL) ? null : Integer.parseInt(token);
			else
				involvedVariablesInParameters.add((PVariable) var);
			table.add(var);
			token = st.nextToken();
		}
		int nbVars = table.size();
		token = st.nextToken();
		token = st.nextToken();
		while (!token.equals("]")) {
			token = st.nextToken();
			Integer val = token.equals(InstanceTokens.NIL) ? null : Integer.parseInt(token);
			table.add(val);
			token = st.nextToken();
			Object noccurrence = mapOfVariables.get(token);
			if (noccurrence == null)
				noccurrence = token.equals(InstanceTokens.NIL) ? null : Integer.parseInt(token);
			else
				involvedVariablesInParameters.add((PVariable) noccurrence);
			table.add(noccurrence);
			token = st.nextToken();
			token = st.nextToken();
		}
		return new PGlobalCardinality(name, involvedVariables,
				table.toArray(new Object[table.size()]),
				nbVars);
	}

	private PConstraint parseLexLessConstraint(String name, PVariable[] involvedVariables, Element parameters){
		StringTokenizer st = new StringTokenizer(buildStringRepresentationOf(parameters));
		st.nextToken(); // token '[' skipped
		Set<PVariable> involvedVariablesInParameters = new HashSet<PVariable>(16);
		String token = st.nextToken();
		List<Object> table = new ArrayList<Object>(16);
		while (!token.equals("]")) {
			Object var = mapOfVariables.get(token);
			if (var == null)
				var = token.equals(InstanceTokens.NIL) ? null : Integer.parseInt(token);
			else
				involvedVariablesInParameters.add((PVariable) var);
			table.add(var);
			token = st.nextToken();
		}
		int nbVars = table.size();
		token = st.nextToken();
		token = st.nextToken();
		while (!token.equals("]")) {
			Object var = mapOfVariables.get(token);
			if (var == null)
				var = token.equals(InstanceTokens.NIL) ? null : Integer.parseInt(token);
			else
				involvedVariablesInParameters.add((PVariable) var);
			table.add(var);
			token = st.nextToken();
		}
		return new PLexLess(name, involvedVariables,
				table.toArray(new Object[table.size()]),
				nbVars);
	}

	private PConstraint parseLexLessEqConstraint(String name, PVariable[] involvedVariables, Element parameters){
		StringTokenizer st = new StringTokenizer(buildStringRepresentationOf(parameters));
		st.nextToken(); // token '[' skipped
		Set<PVariable> involvedVariablesInParameters = new HashSet<PVariable>(16);
		String token = st.nextToken();
		List<Object> table = new ArrayList<Object>(16);
		while (!token.equals("]")) {
			Object var = mapOfVariables.get(token);
			if (var == null)
				var = token.equals(InstanceTokens.NIL) ? null : Integer.parseInt(token);
			else
				involvedVariablesInParameters.add((PVariable) var);
			table.add(var);
			token = st.nextToken();
		}
		int nbVars = table.size();
		token = st.nextToken();
		token = st.nextToken();
		while (!token.equals("]")) {
			Object var = mapOfVariables.get(token);
			if (var == null)
				var = token.equals(InstanceTokens.NIL) ? null : Integer.parseInt(token);
			else
				involvedVariablesInParameters.add((PVariable) var);
			table.add(var);
			token = st.nextToken();
		}
		return new PLexLessEq(name, involvedVariables,
				table.toArray(new Object[table.size()]),
				nbVars);
	}

	

	private PConstraint parseConstraint(Element constraintElement) throws UnsupportedConstraintException {
		String name = constraintElement.getAttribute(InstanceTokens.NAME);
		int arity = Integer.parseInt(constraintElement.getAttribute(InstanceTokens.ARITY));
		if (arity > maxConstraintArity)
			maxConstraintArity = arity;
		PVariable[] scope = parseScope(constraintElement.getAttribute(InstanceTokens.SCOPE));

		String reference = constraintElement.getAttribute(InstanceTokens.REFERENCE);
		if (mapOfRelations.containsKey(reference)) {
			nbExtensionConstraints++;
			return new PExtensionConstraint(name, scope, mapOfRelations.get(reference));
		}

		if (mapOfFunctions.containsKey(reference)) {
			Element parameters = (Element) constraintElement.getElementsByTagName(InstanceTokens.PARAMETERS).item(0);
			nbSoftIntensionConstraints++;
			return new PIntensionConstraint(name, scope, mapOfFunctions.get(reference), parameters.getTextContent());
		}

		if (mapOfPredicates.containsKey(reference)) {
			Element parameters = (Element) constraintElement.getElementsByTagName(InstanceTokens.PARAMETERS).item(0);
			nbIntensionConstraints++;
			return new PIntensionConstraint(name, scope, mapOfPredicates.get(reference), parameters.getTextContent());
		}

		nbGlobalConstraints++;
		final Element parameters = (Element) constraintElement.getElementsByTagName(InstanceTokens.PARAMETERS).item(0);
		return parseGlobalConstraint(reference, name, scope, parameters);
	}
	
	protected PConstraint parseGlobalConstraint(String reference, String name, PVariable[] scope, Element parameters) {
		final String lreference = reference.toLowerCase();
		if (lreference.equals(InstanceTokens.getLowerCaseGlobalNameOf(InstanceTokens.ALL_DIFFERENT)))
			return new PAllDifferent(name, scope);
		if (lreference.equals(InstanceTokens.getLowerCaseGlobalNameOf(InstanceTokens.ELEMENT)))
			return parseElementConstraint(name, scope, parameters);
		if (lreference.equals(InstanceTokens.getLowerCaseGlobalNameOf(InstanceTokens.WEIGHTED_SUM)))
			return parseWeightedSumConstraint(name, scope, parameters);
		if (lreference.equals(InstanceTokens.getLowerCaseGlobalNameOf(InstanceTokens.CUMULATIVE)))
			return parseCumulativeConstraint(name, scope, parameters);
		if (lreference.equals(InstanceTokens.getLowerCaseGlobalNameOf(InstanceTokens.DISJUNCTIVE)))
			return parseDisjunctiveConstraint(name, scope, parameters);
		if (lreference.equals(InstanceTokens.getLowerCaseGlobalNameOf(InstanceTokens.GLOBALCARDINALITY)))
			return parseGlobalCardinalityConstraint(name, scope, parameters);
		if (lreference.equals(InstanceTokens.getLowerCaseGlobalNameOf(InstanceTokens.LEXLESS)))
			return parseLexLessConstraint(name, scope, parameters);
		if (lreference.equals(InstanceTokens.getLowerCaseGlobalNameOf(InstanceTokens.LEXLESSEQ)))
			return parseLexLessEqConstraint(name, scope, parameters);
		LOGGER.log(Level.WARNING, "Problem with the reference {0}", reference);
		return null;
	}

	private void parseConstraints(Element constraintsElement) throws UnsupportedConstraintException {
		mapOfConstraints = new HashMap<String, PConstraint>(16);
		int nbConstraints = Integer.parseInt(constraintsElement.getAttribute(InstanceTokens.NB_CONSTRAINTS));
		if (displayInstance && LOGGER.isLoggable(Level.INFO)) {
            StringBuilder st = new StringBuilder(128);
			st.append("=> ").append(nbConstraints).append(" constraints");
			if (type.equals(InstanceTokens.WCSP)) {
				int maximalCost = Integer.parseInt(constraintsElement.getAttribute(InstanceTokens.MAXIMAL_COST));
				String s = constraintsElement.getAttribute(InstanceTokens.INITIAL_COST);
				int initialCost = s.length() == 0 ? 0 : Integer.parseInt(s);
				st.append(" maximalCost=").append(maximalCost).append(" initialCost=").append(initialCost);
			}
			LOGGER.info(st.toString());
		}

		NodeList nodeList = constraintsElement.getElementsByTagName(InstanceTokens.CONSTRAINT);
		for (int i = 0; i < nodeList.getLength(); i++) {
			PConstraint constraint = parseConstraint((Element) nodeList.item(i));
			mapOfConstraints.put(constraint.getName(), constraint);
			if (displayInstance)
				LOGGER.log(Level.INFO,"{0}",constraint);
		}
	}

	/**
	 * Parse the DOM object that has been loaded.
	 * 
	 * @param displayInstance if <code> true </code>, elements of the instance will be displayed.
	 */
	public void parse(boolean displayInstance) throws UnsupportedConstraintException {
		this.displayInstance = displayInstance;
		parsePresentation((Element) document.getDocumentElement().getElementsByTagName(InstanceTokens.PRESENTATION).item(0));
		parseDomains((Element) document.getDocumentElement().getElementsByTagName(InstanceTokens.DOMAINS).item(0));
		parseVariables((Element) document.getDocumentElement().getElementsByTagName(InstanceTokens.VARIABLES).item(0));
		parseRelations((Element) document.getDocumentElement().getElementsByTagName(InstanceTokens.RELATIONS).item(0));
		parseFunctions((Element) document.getDocumentElement().getElementsByTagName(InstanceTokens.FUNCTIONS).item(0));
		parsePredicates((Element) document.getDocumentElement().getElementsByTagName(InstanceTokens.PREDICATES).item(0));
		parseConstraints((Element) document.getDocumentElement().getElementsByTagName(InstanceTokens.CONSTRAINTS).item(0));
	}

	public static void main(String[] args) throws UnsupportedConstraintException {
		if (args.length != 1) {
			LOGGER.log(Level.INFO, "InstanceParser {0}\nUsage : java ... InstanceParser <instanceName>", VERSION);
			System.exit(1);
		}

		InstanceParser parser = new InstanceParser();
		parser.loadInstance(args[0]);
		parser.parse(true);
	}
}
