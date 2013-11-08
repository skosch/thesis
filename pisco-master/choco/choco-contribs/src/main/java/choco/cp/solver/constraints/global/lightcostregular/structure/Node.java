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

package choco.cp.solver.constraints.global.lightcostregular.structure;

import choco.kernel.memory.IStateDouble;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.IEnvironment;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Nov 24, 2008
 * Time: 1:00:47 PM
 */

public class Node implements Comparable{
    static int ID = 0;

    IEnvironment env;

    IStateDouble spfs;
    IStateDouble spft;
    IStateDouble lpfs;
    IStateDouble lpft;

    IStateInt sptt;
    IStateInt spts;
    IStateInt lptt;
    IStateInt lpts;

    int id;
    int layer;
    int state;
    int offset;
    int nbEdges;
    int offsetRev;
    int nbEdgesRev;





    public Node(IEnvironment env, int layer, int state)
    {
        this.env = env;
        this.layer = layer;
        this.state = state;
        this.id = ID++;
    }

    public void setShortestPathFromSource(double val)
    {
        if (spfs == null)
            spfs = env.makeFloat(val);
        else
            spfs.set(val);
    }
    public void setShortestPathFromTink(double val)
    {
        if (spft == null)
            spft = env.makeFloat(val);
        else
            spft.set(val);
    }
    public void setLongestPathFromTink(double val)
    {
        if (lpft == null)
            lpft = env.makeFloat(val);
        else
            lpft.set(val);
    }
    public void setLongestPathFromSource(double val)
    {
        if (lpfs == null)
            lpfs = env.makeFloat(val);
        else
            lpfs.set(val);
    }

    public double getShortestPathFromSource()
    {
        if (spfs == null)
            return Integer.MAX_VALUE;
        else
            return spfs.get();
    }
    public double getShortestPathFromTink()
    {
        if (spft == null)
            return Integer.MAX_VALUE;
        else
            return spft.get();
    }
    public double getLongestPathFromTink()
    {
        if (lpft == null)
            return Integer.MIN_VALUE;
        else
            return lpft.get();
    }
    public double getLongestPathFromSource()
    {
        if (lpfs == null)
            return Integer.MIN_VALUE;
        else
            return lpfs.get();
    }


    public void setShortestPathToTink(int id)
    {
        if (sptt == null)
            sptt = env.makeInt(id);
        else
            this.sptt.set(id);
    }
    public void setShortestPathToSource(int id)
    {
        if (spts == null)
            spts = env.makeInt(id);
        else
            this.spts.set(id);
    }
    public void setLongestPathToSource(int id)
    {
        if (lpts == null)
            lpts = env.makeInt(id);
        else
            this.lpts.set(id);
    }
    public void setLongestPathToTink(int id)
    {
        if (lptt == null)
            lptt = env.makeInt(id);
        else
            this.lptt.set(id);
    }

    public int getShortestPathToTink()
    {
        if (sptt == null)
            return -1;
        else
            return this.sptt.get();
    }
    public int getShortestPathToSource()
    {
        if (spts == null)
            return -1;
        else
            return this.spts.get();
    }
    public int getLongestPathToSource()
    {
        if (lpts == null)
            return -1;
        else
            return this.lpts.get();
    }
    public int getLongestPathToTink()
    {
        if (lptt == null)
            return -1;
        else
            return this.lptt.get();
    }

    public void updateShortestPathtoSource() {
        
    }

    public void updateLongestPathFromSource() {
        
    }

    public int compareTo(Object o) {
        Node n = (Node) o;
        return new Integer(this.id).compareTo(n.id);
    }
}
