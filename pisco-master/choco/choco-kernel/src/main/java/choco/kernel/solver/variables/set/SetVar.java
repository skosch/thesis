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

package choco.kernel.solver.variables.set;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;

/* 
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 6 juin 2004
 * Since : Choco 2.0.0
 *
 */
public interface SetVar extends Var {


    /**
     * <b>Public user API:</b>
     * static constants associated to the encoding of the variable domain
     * these constants are passed as parameters to the constructor of Set Vars
     */
    public static int BOUNDSET_BOUNDCARD = 0;


    /**
     * <b>Public user API:</b>
     * static constants associated to the encoding of the variable domain
     * these constants are passed as parameters to the constructor of Set Vars
     */
    public static int BOUNDSET_ENUMCARD = 1;


    /**
     * <b>Public user API:</b>
     * static constants associated to the encoding of the variable domain
     * these constants are passed as parameters to the constructor of Set Vars
     */
    public static int BOUNDSET_CONSTANT = 2;


    /**
     * @return the IntDomainVar representing the cardinality
     *         of this set
     */
    public IntDomainVar getCard();

    /**
     * <b>Public user API:</b>
     * setting a value to the kernel of a set variable
     *
     * @param x the value that is set to the variable
     */

    public void setValIn(int x) throws ContradictionException;


    /**
     * <b>Public user API:</b>
     * removing a value from the Enveloppe of a set variable.
     *
     * @param x the removed value
     */

    public void setValOut(int x) throws ContradictionException;


    /**
     * <b>Public user API:</b>
     * <i>Domains :</i> returns the object responsible for storing the enumeration of values in the domain
     */

    public SetDomain getDomain();

    /**
     * <b>Public user API:</b>
     * <i>Domains :</i> testing whether a value is in the kernel domain
     *
     * @param x the tested value
     */
    public boolean isInDomainKernel(int x);

    /**
     * <b>Public user API:</b>
     * <i>Domains :</i> testing whether a value is in the enveloppe domain.
     *
     * @param x the tested value
     */

    public boolean isInDomainEnveloppe(int x);


    /**
     * <b>Public user API:</b>
     * <i>Domains :</i> testing whether two variables have intersecting domains.
     *
     * @param x the other variable
     */

    public boolean canBeEqualTo(SetVar x);


    /**
     * <b>Public user API:</b>
     * <i>Domains :</i> retrieves the number of values in the kernel domain.
     */

    public int getKernelDomainSize();

    /**
     * <b>Public user API:</b>
     * <i>Domains :</i> retrieves the number of values in the enveloppe domain.
     */

    public int getEnveloppeDomainSize();


    /**
     * Returns the lower bound of the enveloppe variable domain.
     * (i.e the smallest value contained in the enveloppe)
     *
     * @return the enveloppe domain lower bound
     */

    public int getEnveloppeInf();

    public int getEnveloppeSup();

    public int getKernelInf();

    public int getKernelSup();


    /**
     * Returns the value of the variable if instantiated.
     *
     * @return the value of the variable
     */

    public int[] getValue();

    /**
     * set the value of the variable to the set val.
     *
     * @param val the value to be set
     */
    public void setVal(int[] val) throws ContradictionException;

    /**
     * <i>Propagation events</i> updating the kernel of a variable
     * (i.e adding a value)
     *
     * @param x   a value of the enveloppe domain to be added to the kernel
     * @param cause
     * @param forceAwake
     * @return a boolean indicating whether this method call added new information or not
     */

    public boolean addToKernel(int x, final SConstraint cause, final boolean forceAwake) throws ContradictionException;
    @Deprecated
    public boolean addToKernel(int x, final int idx) throws ContradictionException;


    /**
     * <i>Propagation events</i> updating the enveloppe of a variable
     * (i.e removing a value)
     *
     * @param x   a value of the enveloppe domain to be removed
     * @param cause
     * @param forceAwake
     * @return a boolean indicating whether this method call added new information or not
     */

    public boolean remFromEnveloppe(int x, final SConstraint cause, final boolean forceAwake) throws ContradictionException;
    @Deprecated
    public boolean remFromEnveloppe(int x, final int idx) throws ContradictionException;

    /**
     * <i>Propagation events</i> instantiated a set var to a specific set of values
     *
     * @param x   a set of values describing the final instantiated kernel
     * @param cause
     * @param forceAwake
     * @return a boolean indicating whether this method call added new information or not
     */
    public boolean instantiate(int[] x, final SConstraint cause, final boolean forceAwake) throws ContradictionException;
    @Deprecated
    public boolean instantiate(int[] x, final int idx) throws ContradictionException;
}
