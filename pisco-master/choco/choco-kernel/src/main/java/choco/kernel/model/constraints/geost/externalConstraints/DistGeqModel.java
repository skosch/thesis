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

import choco.kernel.model.variables.integer.IntegerVariable;

/**
 * Created by IntelliJ IDEA.
 * User: szampelli
 * Date: 7 aožt 2009
 * Time: 18:56:08
 * To change this template use File | Settings | File Templates.
 */
public class DistGeqModel extends IExternalConstraint {

    public int D;
    public int o1;
    public int o2;
    public int q;
    public IntegerVariable modelDVar = null;

	public DistGeqModel(int ectrID, int[] dimensions, int[] objectIdentifiers, int D_, int q_)
	{
        this(ectrID,dimensions,objectIdentifiers,D_,q_,null);
	}

    public DistGeqModel(int ectrID, int[] dimensions, int[] objectIdentifiers, int D_, int q_, IntegerVariable var)
    {
        super(ectrID, dimensions, null);
        int[] oids = new int[2];
        oids[0] = objectIdentifiers[0];
        oids[1] = objectIdentifiers[1];

        setObjectIds(oids); //Prune only the first object!
        D=D_;
        o1=objectIdentifiers[0];
        o2=objectIdentifiers[1];
        q=q_;
        setObjectIds(oids);
        modelDVar=var;
    }


    public String toString() {
        StringBuilder r= new StringBuilder();
        if (modelDVar!=null){
            r.append("Geq(D=[").append(modelDVar.getLowB()).append(",").append(modelDVar.getUppB())
                    .append("],q=").append(q).append(",o1=").append(o1).append(",o2=").append(o2).append(")");
        }
        else{
            r.append("Geq(D=").append(D).append(",q=").append(q).append(",o1=").append(o1)
                    .append(",o2=").append(o2).append(")");
        }
        return r.toString();
    }

    public boolean hasDistanceVar() { return (modelDVar!=null); }

    public IntegerVariable getDistanceVar() { return modelDVar; }


}
