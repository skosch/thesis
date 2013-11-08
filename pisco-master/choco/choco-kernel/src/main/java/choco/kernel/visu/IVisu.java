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

package choco.kernel.visu;

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.Solver;
import choco.kernel.visu.components.panels.AVarChocoPanel;

import java.util.logging.Logger;

/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 27 oct. 2008
 * Since : Choco 2.0.1
 *
 * Interface {@code IVisu} is a Choco object to define visualization over Choco's objects.
 */

public interface IVisu {

       final static Logger LOGGER = ChocoLogging.getEngineLogger();
    

    /**
     * Add a new panel to the main frame of the Choco visualizer.
     * Allow user to observe variables during resolution.
     * @param vpanel the new panel to add
     */
    public void addPanel(final AVarChocoPanel vpanel);

    /**
     * Shows or hides this {@code IVisu} depending on the value of parameter
     * {@code visible}.
     * @param visible  if {@code true}, makes the {@code IVisu} visible, 
     * otherwise hides the {@code IVisu}.
     */
    public void setVisible(final boolean visible);

    /**
     * Initializes the {@code IVisu} from the {@code Solver}
     * @param s solver
     */
    public void listen(final Solver s);

}
