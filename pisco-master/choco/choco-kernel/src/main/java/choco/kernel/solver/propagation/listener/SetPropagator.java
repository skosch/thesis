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

package choco.kernel.solver.propagation.listener;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;

/*
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 6 juin 2004
 * Since : Choco 2.0.0
 *
 */
public interface SetPropagator {
    /**
     * Default propagation on kernel modification: propagation on adding a value to the kernel.
     */
    public void awakeOnKer(int varIdx, int x) throws ContradictionException;


    /**
     * Default propagation on enveloppe modification: propagation on removing a value from the enveloppe.
     */
    public void awakeOnEnv(int varIdx, int x) throws ContradictionException;


    /**
     * Default propagation on instantiation.
     */
    public void awakeOnInst(int varIdx) throws ContradictionException;

    /**
     * BEWARE: No need to dipose the iterator, this done in the calling methode
     * @param varIdx
     * @param deltaDomain
     * @throws ContradictionException
     */
    public void awakeOnkerAdditions(int varIdx, DisposableIntIterator deltaDomain) throws ContradictionException;

    /**
     * BEWARE: No need to dipose the iterator, this done in the calling methode
     * @param varIdx
     * @param deltaDomain
     * @throws ContradictionException
     */
    public void awakeOnEnvRemovals(int varIdx, DisposableIntIterator deltaDomain) throws ContradictionException;

}
