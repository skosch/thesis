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

package choco.kernel.solver.variables.integer;

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.variables.Domain;
import choco.kernel.solver.variables.delta.IDeltaDomain;

import java.util.logging.Logger;

/**
 * An interface for all domains of search variables
 */
public interface IntDomain extends Domain {

	public final static Logger LOGGER = ChocoLogging.getEngineLogger();
	/**
	 * Retrieve an getIterator for traversing the sequence of values contained in the domain
	 */

	public DisposableIntIterator getIterator();

	/**
	 * Access the minimal value stored in the domain.
	 */

	public int getInf();


	/**
	 * Access the maximal value stored in the domain/
	 */

	public int getSup();


	/**
	 * Augment the minimal value stored in the domain.
	 * returns the new lower bound (<i>x</i> or more, in case <i>x</i> was
	 * not in the domain)
	 */

	public int updateInf(int x);


	/**
	 * Diminish the maximal value stored in the domain.
	 * returns the new upper bound (<i>x</i> or more, in case <i>x</i> was
	 * not in the domain).
	 */

	public int updateSup(int x);


	/**
	 * Testing whether an search value is contained within the domain.
	 */

	public boolean contains(int x);


	/**
	 * Removing a single value from the domain.
	 */

	public boolean remove(int x);


	/**
	 * Restricting the domain to a singleton
	 */

	public void restrict(int x);


	/**
	 * Access the total number of values stored in the domain.
	 */

	public int getSize();


	/**
	 * Accessing the smallest value stored in the domain and strictly greater
	 * than <i>x</i>.
	 * Does not require <i>x</i> to be in the domain.
     * <p/>
     * To iterate over the values in a <code>IntDomain</code>,
     * use the following loop:
     *
     * <pre>
     * int ub = dom.getSup();
     * for (int val = dom.getInf(); val <= ub; val = dom.getNextValue(val)) {
     *     // operate on value 'val' here
     * }</pre>
     *
	 */

	public int getNextValue(int x);

    /**
	 * Accessing the smallest value stored in the domain and strictly greater
	 * than <i>x</i>, assuming <i>x</i> is greater or equal to the lower bound.
     * <p/>
     * To iterate over the values in a <code>IntDomain</code>,
     * use the following loop:
     *
     * <pre>
     * int ub = dom.getSup();
     * for (int val = dom.getInf(); val <= ub; val = dom.fastNextValue(val)) {
     *     // operate on value 'val' here
     * }</pre>
     *
	 */
    public int fastNextValue(int x);


	/**
	 * Accessing the largest value stored in the domain and strictly smaller
	 * than <i>x</i>.
	 * Does not require <i>x</i> to be in the domain.
     *
     * <p/>
     * To iterate over the values in a <code>IntDomain</code>,
     * use the following loop:
     *
     * <pre>
     * int lb = dom.getInf();
     * for (int val = dom.getSup(); val >= lb; val = dom.getPrevValue(val)) {
     *     // operate on value 'val' here
     * }</pre>
	 */

	public int getPrevValue(int x);

    /**
	 * Accessing the largest value stored in the domain and strictly smaller
	 * than <i>x</i>, assuming <i>x</i> is less or equal to the upper bound.
     *
     * <p/>
     * To iterate over the values in a <code>IntDomain</code>,
     * use the following loop:
     *
     * <pre>
     * int lb = dom.getInf();
     * for (int val = dom.getSup(); val >= lb; val = dom.fastPrevValue(val)) {
     *     // operate on value 'val' here
     * }</pre>
	 */
    public int fastPrevValue(int x);

	/**
	 * Testing whether there are values in the domain that are strictly greater
	 * than <i>x</i>.
	 * Does not require <i>x</i> to be in the domain.
	 */

	public boolean hasNextValue(int x);


	/**
	 * Testing whether there are values in the domain that are strictly smaller
	 * than <i>x</i>.
	 * Does not require <i>x</i> to be in the domain.
	 */

	public boolean hasPrevValue(int x);


	/**
	 * Draws a value at random from the domain.
	 */

	public int getRandomValue();

	/**
	 * Returns an getIterator over the set of values that have been removed from the domain since the last propagation
	 */
	public DisposableIntIterator getDeltaIterator();

	/**
	 * The delta domain container is "frozen" (it can no longer accept new value removals)
	 * so that this set of values can be iterated as such
	 */
	public void freezeDeltaDomain();

	/**
	 * after an iteration over the delta domain, the delta domain is reopened again.
	 *
	 * @return true iff the delta domain is reopened empty (no updates have been made to the domain
	 *         while it was frozen, false iff the delta domain is reopened with pending value removals (updates
	 *         were made to the domain, while the delta domain was frozen).
	 */
	public boolean releaseDeltaDomain();

	/**
	 * checks whether the delta domain has indeed been released (ie: chechks that no domain updates are pending)
	 */
	public boolean getReleasedDeltaDomain();

	/**
	 * cleans the data structure implementing the delta domain
	 */
	public void clearDeltaDomain();

	public boolean isEnumerated();

	/**
	 * Is it a 0/1 domain ?
	 */
	public boolean isBoolean();

    public IDeltaDomain copyDelta();

}
