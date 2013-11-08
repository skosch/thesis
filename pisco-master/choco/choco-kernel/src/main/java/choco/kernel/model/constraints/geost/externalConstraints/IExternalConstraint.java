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

package choco.kernel.model.constraints.geost.externalConstraints;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 9 févr. 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*/
public abstract class IExternalConstraint {
    protected int ectrID;
    protected int[] dim;
    protected int[] objectIds;

    public IExternalConstraint()
    {
    }

	public IExternalConstraint(int ectrID, int[] dimensions, int[] objectIdentifiers)
	{
		this.ectrID = ectrID;
		this.dim = dimensions;
		this.objectIds = objectIdentifiers;
	}

	/**
	 * Gets the list of dimensions that an external constraint is active for.
	 */
	public int[] getDim() {
		return dim;
	}

	/**
	 * Gets the external constraint ID
	 */
	public int getEctrID() {
		return ectrID;
	}

	/**
	 * Gets the list of object IDs that this external constraint affects.
	 */
	public int[] getObjectIds() {
		return objectIds;
	}

	/**
	 * Sets the list of dimensions that an external constraint is active for.
	 */
	public void setDim(int[] dim) {
		this.dim = dim;
	}

//	public void setEctrID(int ectrID) {
//		this.ectrID = ectrID;
//	}

	/**
	 * Sets the list of object IDs that this external constraint affects.
	 */
	public void setObjectIds(int[] objectIds) {
		this.objectIds = objectIds;
	}




}
