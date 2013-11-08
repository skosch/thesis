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

package choco.cp.solver.search;

import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.search.IntBranchingTrace;
import choco.kernel.solver.search.integer.IntVarValPair;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * analyze the trace of the branching for nogood recording from restarts. </br>
 * Lecoutre, C.; Sais, L.; Tabary, S. & Vidal, <br>
 * Nogood Recording from Restarts </br>
 * IJCAI 2007 Proceedings of the 20th International Joint Conference on Artificial Intelligence, Hyderabad, India, January 6-12, 2007, 2007, 131-136

 * @author Arnaud Malapert</br> 
 * @since 22 mars 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public final class NogoodRecorder {

	protected final static Logger LOGGER = ChocoLogging.getSearchLogger();

	protected final CPSolver scheduler;

	private int nbPosLits;

	private final IntDomainVar[] positiveLiterals; 

	private int nbNegLits;

	private final IntDomainVar[] negativeLiterals;

	private final List<NoGoodTail> tails = new LinkedList<NoGoodTail>();


	public NogoodRecorder(final CPSolver scheduler) {
		super();
		this.scheduler = scheduler;
		positiveLiterals = new IntDomainVar[scheduler.getNbBooleanVars()];
		negativeLiterals = new IntDomainVar[scheduler.getNbBooleanVars()];
	}


	public void reset() {
		nbPosLits = 0;
		nbNegLits = 0;
		tails.clear();
	}


	protected IntDomainVar getBranchingVar(final IntBranchingTrace trace) {
		if(trace.getBranchingObject() instanceof IntDomainVar) {
			return (IntDomainVar) trace.getBranchingObject();
		}else if(trace.getBranchingObject() instanceof IntVarValPair) {
			return ( (IntVarValPair) trace.getBranchingObject()).var;
		}	
		return null;
	}

	public void handleTrace(final IntBranchingTrace trace) {
		final IntDomainVar bvar = getBranchingVar(trace);
		// nogood is reset because we can nt record quality is decreased : 
		if(bvar==null) {
			LOGGER.finest("reset nogood recording: not a integer variable");
			reset();
		}else if( ! bvar.getDomain().isBoolean()) {
			LOGGER.finest("reset nogood recording: not a boolean variable");
			reset();
		}else {
			//binary node
			if(trace.getBranchIndex() == 0) {
				//positive decision
				if(!tails.isEmpty()) {
					//add litteral
					if(bvar.getVal() == 0) {positiveLiterals[nbPosLits++] = bvar;}
					else {negativeLiterals[nbNegLits++] = bvar;}
				}
			}else {
				//negative decision
				//create a new noGood by adding a tail to the list
				tails.add(new NoGoodTail(bvar, nbPosLits, nbNegLits));
			}
		}
	}


	public void generateNogoods() {
		IntDomainVar[] posLits, negLits;
		for (NoGoodTail tail : tails) {
			//create array
			final int sp = nbPosLits - tail.posLitsOffset;
			final int sn = nbNegLits - tail.negLitsOffset;
			if(tail.tail.getVal() == 0) {
				posLits = new IntDomainVar[sp];
				negLits = new IntDomainVar[sn + 1];
				negLits[sn] = tail.tail; 
			}else {
				posLits = new IntDomainVar[sp + 1];
				negLits = new IntDomainVar[sn];
				posLits[sp] = tail.tail;
			}
			//copy involved nogood
			System.arraycopy(positiveLiterals, tail.posLitsOffset, posLits, 0, sp);
			System.arraycopy(negativeLiterals, tail.negLitsOffset, negLits, 0, sn);
//			LOGGER.finest("Pos "+Arrays.toString(posLits));
//			LOGGER.finest("Neg "+Arrays.toString(negLits));
//			ChocoLogging.flushLogs();
			scheduler.addNogood(posLits, negLits);
		}
	}

	private final static class NoGoodTail {

		public final IntDomainVar tail;

		public final int posLitsOffset;

		public final int negLitsOffset;

		public NoGoodTail(IntDomainVar tail, int nbPosLits, int nbNegLits) {
			super();
			this.tail = tail;
			this.posLitsOffset = nbPosLits;
			this.negLitsOffset = nbNegLits;
		}

		@Override
		public String toString() {
			return this.tail +"("+posLitsOffset+", "+negLitsOffset+")";
		}
	}
}
