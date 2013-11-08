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

package choco.kernel.solver;

import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.search.AbstractGlobalSearchStrategy;
import choco.kernel.solver.search.limit.AbstractGlobalSearchLimit;

/**
 * An exception thrown when a contradiction achieved.
 */
public final class ContradictionException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1542770449283056616L;

	private Object contradictionCause;
    private int contradictionMove;

    /**
     * Builder of contradiction.
     * BEWARE: user should understand the way a contradiction is used in CHOCO.
     * There is only one contradiction per propagation engine (and per solver).
     * If another objects are created, it could lead to a loss of performance!
     *
     * @return a new ContradictionException
     */
    public static ContradictionException build(){
        return new ContradictionException();
    }


    /**
	 * Constructs a new contradiction with the specified cause.
	 *
	 * @param contradictionCause the the last object variable responsible
	 *              for the failure of propagation
     */
	private ContradictionException() {
		super();
		this.contradictionCause = null;
		this.contradictionMove = AbstractGlobalSearchStrategy.UP_BRANCH;
    }



	public final void set(Object cause) {
		contradictionCause = cause;
        contradictionMove = AbstractGlobalSearchStrategy.UP_BRANCH;
	}

	public final void set(Object cause, int move) {
		contradictionCause = cause;
		contradictionMove = move;
    }

	@Override
	public String toString() {
		return "Exception due to " + contradictionCause;
	}

	public final Object getContradictionCause() {
		return contradictionCause;
	}

    public final SConstraint getDomOverDegContradictionCause(){
        if(contradictionCause instanceof SConstraint){
            return (SConstraint)contradictionCause;
        }
        return null;
    }

    public final int getContradictionMove(){
        return contradictionMove;
    }

	public final boolean isSearchLimitCause(){
        return contradictionCause instanceof AbstractGlobalSearchLimit;
    }

    @Override
    public Throwable fillInStackTrace() {
        return null;
    }
}

