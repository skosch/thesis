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

package choco.kernel.solver.search.limit;

import choco.IPretty;
import choco.kernel.solver.search.AbstractGlobalSearchStrategy;


/**
 * An abstract class for limiting tree search (imposing conditions on depth, ...)
 */
public abstract class AbstractGlobalSearchLimit implements IPretty {

	/**
	 * the strategy that delegates the limit checking task to such AbstractGlobalSearchLimit objects
	 */
	protected final AbstractGlobalSearchStrategy strategy;

	/**
	 * for pretty printing
	 */
	protected final String unit;

	/**
	 * type of limit
	 */
	protected final Limit type;

	/**
	 * maximal value limitting the search exploration
	 */
	protected int nbMax;
	
	
	public AbstractGlobalSearchLimit(AbstractGlobalSearchStrategy theStrategy,int theLimit, String unit) {
		strategy = theStrategy;
		nbMax = theLimit;
		this.type=null;
		this.unit=unit;
	}

	public AbstractGlobalSearchLimit(AbstractGlobalSearchStrategy theStrategy,int theLimit, Limit type) {
		strategy = theStrategy;
		nbMax = theLimit;
		this.type=type;
		this.unit= type.getUnit();
	}


	public final AbstractGlobalSearchStrategy getSearchStrategy() {
		return strategy;
	}
	
	
	@Override
	public String toString() {
		return pretty();
	}



	public String pretty() {
		StringBuilder b = new StringBuilder();
		b.append(getNb());
		if (nbMax != Integer.MAX_VALUE) {
			b.append('/').append(nbMax);
		}
		b.append(' ').append(unit);
		return new String(b);
	}

	/**
	 * get the current counter
	 */

	public abstract int getNb();

	/**
	 * @return the limit value
	 */
	public final int getNbMax() {
		return nbMax;
	}

	public void setNbMax(int nbMax) {
		this.nbMax = nbMax;
	}

	public final Limit getType() {
		return type;
	}

	
	public final String getUnit() {
		return unit;
	}

	
}



