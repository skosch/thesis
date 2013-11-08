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

package choco.cp.solver.constraints.global.automata.fast_multicostregular;

import choco.kernel.common.util.tools.ArrayUtils;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Apr 2, 2010
 * Time: 11:39:15 AM
 */
public class CostWrapper
{


enum Representation
{
        MAT3D,
        MAT4D,
        SPARSE3D,
        SPARSE4D
}

int ressources;
int size;
int values;
int states;


int[][][]  cost3d;
int[][][][] cost4d;
FourTuple[] sparse3d;
FiveTuple[] sparse4d;

Representation representation;

public CostWrapper(int[][][] costs)
{
        representation = Representation.MAT3D;
        this.cost3d = ArrayUtils.swallowCopy(costs);
        this.size = costs.length;
        this.values = costs[0].length;
        this.ressources = costs[0][0].length;
        this.states = 1;

}

public CostWrapper(int[][][][] costs)
{
        representation = Representation.MAT4D;
        this.cost4d = ArrayUtils.swallowCopy(costs);
                this.size = costs.length;
        this.values = costs[0].length;
        this.ressources = costs[0][0].length;
        this.states = costs[0][0][0].length;

}

public CostWrapper(int[] i, int[] j, int[] r, int[] q, int[] val)
{
        int sz = i.length;
        this.sparse4d = new FiveTuple[sz];
        for (int idx = 0 ; idx < sz ; idx++)
        {
                this.sparse4d[idx] = new FiveTuple(i[idx],j[idx],r[idx],q[idx],val[idx]);
        }
        
}


public int get(int i, int j, int r, int q)
{
        switch(representation)
        {
                case MAT3D      : return get3D(i,j,r);
                case MAT4D      : return get4D(i,j,r,q);
                case SPARSE3D   : return getS3D(i,j,r);
                case SPARSE4D   : return getS4D(i,j,r,q);
                default         : return Integer.MIN_VALUE;

        }
}

private int getS3D(int i, int j, int r)
{
        return 0;
}

private int getS4D(int i, int j, int r, int q)
{
        return 0;
}

private int get4D(int i, int j, int r, int q)
{
        return cost4d[i][j][r][q];
}

private int get3D(int i, int j, int r)
{
        return cost3d[i][j][r];
}


public int ressouces()
{
        return ressources;
}

public int size()
{
        return size;
}

public int values()
{
        return values;
}

public int states()
{
        return states;
}

public boolean isStateDependant()
{
        return representation != Representation.MAT3D;
}


private class FourTuple
{
        int i;
        int j;
        int r;
        int val;

        public FourTuple(int i, int j, int r, int val)
        {
                this.i = i;
                this.j = j;
                this.r = r;
                this.val = val;
        }
        public final int i() {return i;}
        public final int j() {return j;}
        public final int r() {return r;}
        public final int val() {return val;}


}

private class FiveTuple extends FourTuple
{
        int q;

        public FiveTuple(int i, int j, int r, int q, int val)
        {
                super(i, j, r, val);
                this.q = q;
        }

        public final int q() {return q;}
}

}
