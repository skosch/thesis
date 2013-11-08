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

package choco.kernel.model.constraints;

import choco.IPretty;
import choco.kernel.common.IIndex;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.IFindManager;
import choco.kernel.model.IOptions;
import choco.kernel.model.IVariableArray;

import java.util.logging.Logger;

/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 18 mars 2008
 * Since : Choco 2.0.0
 *
 */
public interface Constraint extends IPretty, IIndex,IVariableArray, IFindManager, IOptions {

    final static Logger LOGGER = ChocoLogging.getEngineLogger();

    public ConstraintType getConstraintType();

    String getName();
    
    /**
     * @return a list of domains accepted by the constraint and sorted
     *         by order of preference
     */
    public int[] getFavoriteDomains();

    /**
     * Return the constraint manager
     * @return constraint manager
     */
    public ExpressionManager getExpressionManager();

   
}
