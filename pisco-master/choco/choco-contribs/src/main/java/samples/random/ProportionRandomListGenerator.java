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

package samples.random;

import java.util.Arrays;


/**
 * This class allows generating integer random lists using a proportion model.
 * A given fixed number of tuples is randomly generated.
 */
public abstract class ProportionRandomListGenerator extends RandomListGenerator
{
	protected static final int OCCURENCES_LIMIT = 100;

	/**
	* The wished number of occurences of each value. 
	*/
	protected int nbWishedOccurences;

	/**
	 * The maximum number of occurences of each value. 
	 */
	protected int nbMaxOccurences;

	/**
	 * The number of allowed overflows wrt the maximum number of occurences of each value. 
	 */
	protected int nbAllowedOverflows;

	/**
	* The current number of overflows wrt the maximum number of occurences of each value. 
	*/
	protected int nbCurrentOverflows;

	private int nbCurrentOverflowsBis;

	private int[] nbOccurencesBis;

	private LexicographicComparator lexicographicComparator = new LexicographicComparator();

	/**
	 * Builds a proportion random list generator.
	 * @param nbValues the number of values for each element of the tuples
	 * @param seed the seed used to generate random numbers
	 */
	public ProportionRandomListGenerator(int[] nbValues, long seed)
	{
		super(nbValues, seed);
		nbOccurencesBis = new int[nbMaxValues];
	}

	/**
	 * Builds a proportion random list generator.
	 * @param nb the uniform number of values used to build tuples
	 * @param tupleLength the length of each tuple
	 * @param seed the seed used to generate random numbers
	 */
	public ProportionRandomListGenerator(int nb, int tupleLength, long seed)
	{
		super(nb, tupleLength, seed);
		nbOccurencesBis = new int[nb];
	}

	/**
	 * Saves the current number of occurences of each value and the current number of overflows.
	 */
	protected void storeNbOccurrences()
	{
		for (int i = 0; i < nbOccurences.length; i++)
			nbOccurencesBis[i] = nbOccurences[i];
		nbCurrentOverflowsBis = nbCurrentOverflows;
	}

	/**
	 * Restores the current number of occurences of each value and the current number of overflows.
	 */
	protected void restoreNbOccurrences()
	{
		for (int i = 0; i < nbOccurences.length; i++)
			nbOccurences[i] = nbOccurencesBis[i];
		nbCurrentOverflows = nbCurrentOverflowsBis;
	}

	/**
	 * Fixes some limits, according to the given type, about the generation of tuples. 
	 * @param type the type of the generated lists which can be UNSTRUCTURED, CONNECTED or BALANCED
	 */
	protected void fixLimits(Structure type)
	{
		int nbTuples = tuples.length;
		switch (type) {
		case UNSTRUCTURED:
			nbWishedOccurences = Integer.MAX_VALUE; //nbTuples;
			nbMaxOccurences = Integer.MAX_VALUE; //nbTuples;
			nbAllowedOverflows = 0;
			break;
		
		case CONNECTED:
			nbWishedOccurences = 1;
			nbMaxOccurences = nbTuples;
			nbAllowedOverflows = tupleLength * nbTuples - (nbWishedOccurences * nbMaxValues);
			break;

		case BALANCED:
			nbWishedOccurences = tupleLength * nbTuples / nbMaxValues;
			nbMaxOccurences = nbWishedOccurences + 1;
			nbAllowedOverflows = tupleLength * nbTuples % nbMaxValues; // O A REMETRE ???
		}
	}

	/**
	 * Determines (according to some selection constraints) if the given value can be currently selected.
	 * @param value a given value
	 * @return <code> false </code> iff the given value can be currently put in a tuple. 
	 */
	protected boolean mustValueWait(int value)
	{
		nbOccurences[value]++;
		if (nbOccurences[value] <= nbWishedOccurences)
			return false;
		if (nbOccurences[value] > nbMaxOccurences)
			return true;
		nbCurrentOverflows++;
		if (nbCurrentOverflows > nbAllowedOverflows)
			return true;
		return false;
	}

	/**
	 * Determines if the given tuple can be currently selected.
	 * @param tuple the given tuple
	 * @return <code> false </code> iff the given value can be currently put in a tuple
	 */
	protected boolean mustTupleWait(int[] tuple)
	{
		for (int i = 0; i < tuple.length; i++)
			if (mustValueWait(tuple[i]))
				return true;
		return false;
	}

	/**
	 * Makes the selection of the given number of tuples.
	 */
	protected abstract void makeSelection();

	/**
	 * Generates and returns a random list.
	 * @param nbTuples the number of tuples to be selected
	 * @param type the type of the generated lists which can be UNSTRUCTURED, CONNECTED or BALANCED
	 * @param tupleRepetition indicates if the same tuple can occur several times in the generated lists
	 * @param valueRepetition indicates if the same value can occur several times in a generated tuple
	 * @param fixedTuple a particular tuple, which if not <code> null </code>, must or must not belong to the generated lists 
	 * @param requiredFixedTuple indicates if the fixed tuple, if not <code> null </code>, must or must not belong to the generated lists 
	 * @return a random generated list
	 */
	public int[][] selectTuples(
		int nbTuples,
		Structure type,
		boolean tupleRepetition,
		boolean valueRepetition,
		int[] fixedTuple,
		boolean requiredFixedTuple)
	{
		setParameters(type, tupleRepetition, valueRepetition, fixedTuple, requiredFixedTuple);
		if (tuples == null || tuples.length != nbTuples)
			tuples = new int[nbTuples][tupleLength];

		if (!tupleRepetition)
		{
			double nbDistinctTuples = computeNbDistinctTuples();
			if (nbTuples > nbDistinctTuples)
				throw new IllegalArgumentException("The number of tuples " + nbTuples + " is greater than the maximum number " + nbDistinctTuples);
		}
		fixLimits(type);
		makeSelection();
		Arrays.sort(tuples, lexicographicComparator);
		return tuples;
	}

	/**
	 * Generates and returns a random list.
	 * @param nbTuples the number of tuples to be selected
	 * @param type the type of the generated lists which can be UNSTRUCTURED, CONNECTED or BALANCED
	 * @param tupleRepetition indicates if the same tuple can occur several times in the generated lists
	 * @param valueRepetition indicates if the same value can occur several times in a generated tuple
	 * @return a random generated list
	 */
	public int[][] selectTuples(int nbTuples, Structure type, boolean tupleRepetition, boolean valueRepetition)
	{
		return selectTuples(nbTuples, type, tupleRepetition, valueRepetition, null, false);
	}

	public void displayTuples()
	{
		super.displayTuples();
	}

}
