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

package choco.kernel.solver.constraints.global.automata.fast_multicostregular.algo;

import choco.kernel.common.Constant;
import choco.kernel.memory.structure.StoredIndexedBipartiteSet;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.global.automata.fast_multicostregular.structure.SoftStoredMultiValuedDirectedMultiGraph;
import choco.kernel.solver.constraints.integer.AbstractIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TIntStack;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Apr 27, 2010
 * Time: 3:31:13 PM
 */
public class SoftPathFinder
{


SoftStoredMultiValuedDirectedMultiGraph graph;

int[] sp;
int[] lp;

int nbLayer;
int nbR;

public double[][] spfs;
public double[][] spft;
double[][] lpfs;
double[][] lpft;
boolean[] modified = new boolean[2];

int[][] prevSP;
int[][] nextSP;
int[][] prevLP;
int[][] nextLP;


public SoftPathFinder(SoftStoredMultiValuedDirectedMultiGraph graph)
{
        this.graph = graph;
        this.sp = new int[graph.layers.length-1];
        this.lp = new int[graph.layers.length-1];
        this.nbLayer= graph.layers.length-1;
        this.nbR =  this.graph.nbR;
        spfs = this.graph.GNodes.spfsI;
        spft = this.graph.GNodes.spftI;
        lpfs = this.graph.GNodes.lpfsI;
        lpft = this.graph.GNodes.lpftI;
        prevSP = this.graph.GNodes.prevSPI;
        nextSP = this.graph.GNodes.nextSPI;
        prevLP = this.graph.GNodes.prevLPI;
        nextLP = this.graph.GNodes.nextLPI;



}

private final double getCost(int e, double[] u)
{
        double cost = 0.0;
        for (int r = 0 ; r < u.length ; r++)
                cost+= graph.GArcs.originalCost[e][r]*u[r];
        graph.GArcs.temporaryCost[e] = cost;
        return cost;
}


public void computeLongestPath(TIntStack removed, double lb, double[] u) throws ContradictionException
{

        boolean update;
        graph.GNodes.lpfs[graph.sourceIndex] = 0.0;
        graph.GNodes.lpft[graph.tinIndex] = 0.0;

        for (int i = 1 ; i <= nbLayer ; i++)
        {
                update = false;

                final int[] list = graph.layers[i]._getStructure();
                final int size = graph.layers[i].size();
                for (int w = size -1 ; w >= 0 ; w--) {
                        int dest = list[w];
                        StoredIndexedBipartiteSet bs = graph.GNodes.inArcs[dest];
                        assert(!bs.isEmpty());
                        final int[] inlist = bs._getStructure();
                        final int insize = bs.size();
                        graph.GNodes.lpfs[dest] = Double.NEGATIVE_INFINITY;
                        for (int x = 0 ; x < insize; x++)
                        {
                                int e = inlist[x];
                                if (!graph.isInStack(e))
                                {
                                        int orig = graph.GArcs.origs[e];
                                        double newCost = graph.GNodes.lpfs[orig] + getCost(e,u);

                                        if (graph.GNodes.lpfs[dest] < newCost)
                                        {
                                                graph.GNodes.lpfs[dest] = newCost;
                                                graph.GNodes.prevLP[dest] = e;
                                                update =  true;
                                        }
                                }

                        }
                }
                if (!update) graph.constraint.fail();
        }
        for (int i = nbLayer -1 ; i >= 0 ; i--)
        {
                update = false;
                final int[] list = graph.layers[i]._getStructure();
                final int size = graph.layers[i].size();
                for (int w = size -1 ; w >= 0 ; w--) {
                        int orig = list[w];
                        StoredIndexedBipartiteSet bs = graph.GNodes.outArcs[orig];
                        assert(!bs.isEmpty());

                        final int[] outlist = bs._getStructure();
                        final int outsize = bs.size();
                        graph.GNodes.lpft[orig] = Double.NEGATIVE_INFINITY;
                        for (int x = 0 ; x < outsize ; x++)
                        {
                                int e = outlist[x];
                                if (!graph.isInStack(e))
                                {
                                        int next = graph.GArcs.dests[e];
                                        double newCost = graph.GNodes.lpft[next] +graph.GArcs.temporaryCost[e];//cost[graph.GNodes.layers[next]][graph.GArcs.values[e]];
                                        if (newCost + graph.GNodes.lpfs[orig] -lb <= -Constant.MCR_DECIMAL_PREC)
                                        {
                                                graph.setInStack(e);
                                                removed.push(e);
                                        }
                                        else if (graph.GNodes.lpft[orig] < newCost)
                                        {
                                                graph.GNodes.lpft[orig] = newCost;
                                                graph.GNodes.nextLP[orig] = e;
                                                update = true;
                                        }
                                }

                        }
                        //out.dispose();

                }
                //origIter.dispose();
                if (!update) this.graph.constraint.fail();
        }


}

public final double getLongestPathValue() {
        return graph.GNodes.lpft[graph.sourceIndex];
}

public int[] getLongestPath() {
        int i = 0;
        int current = this.graph.sourceIndex;
        do {
                int e = graph.GNodes.nextLP[current];//current.getSptt();
                sp[i++]= e;
                current = graph.GArcs.dests[e];//.getDestination();

        } while(graph.GNodes.nextLP[current] != Integer.MIN_VALUE);
        return sp;
}


public void computeShortestPath(TIntStack removed, double ub, double[] u) throws ContradictionException {

        graph.GNodes.spfs[graph.sourceIndex] = 0.0;
        graph.GNodes.spft[graph.tinIndex] = 0.0;
        boolean update;

        for (int i = 1 ; i <= nbLayer ; i++)
        {
                update = false;

                int[] list = graph.layers[i]._getStructure();
                int size = graph.layers[i].size();

                for (int w = size -1 ; w >= 0 ; w--) {

                        int dest = list[w];
                        graph.GNodes.spfs[dest] = Double.POSITIVE_INFINITY;
                        StoredIndexedBipartiteSet bs = graph.GNodes.inArcs[dest];
                        assert(!bs.isEmpty());
                        final int[] inlist = bs._getStructure();
                        final int insize = bs.size();

                        for (int x = 0 ; x < insize; x++)
                        {
                                int e = inlist[x];
                                if (!graph.isInStack(e))
                                {
                                        int orig = graph.GArcs.origs[e];
                                        double newCost = graph.GNodes.spfs[orig] +  getCost(e,u);
                                        if (graph.GNodes.spfs[dest] > newCost)
                                        {
                                                graph.GNodes.spfs[dest] = newCost;
                                                graph.GNodes.prevSP[dest] = e;
                                                update = true;
                                        }
                                }
                        }
                }
                if (!update)  this.graph.constraint.fail();
        }
        for (int i = nbLayer-1 ; i >= 0 ; i--)
        {
                update  = false;
                int[] list = graph.layers[i]._getStructure();
                int size = graph.layers[i].size();
                for (int w = size -1 ; w >= 0 ; w--) {
                        int orig = list[w];
                        graph.GNodes.spft[orig] = Double.POSITIVE_INFINITY;
                        StoredIndexedBipartiteSet bs = graph.GNodes.outArcs[orig];
                        assert(!bs.isEmpty());
                        final int[] outlist = bs._getStructure();
                        final int outsize = bs.size();
                        for (int x = 0 ; x < outsize ; x++)
                        {
                                int e = outlist[x];
                                if (!graph.isInStack(e))
                                {
                                        int dest = graph.GArcs.dests[e];
                                        double newCost = graph.GNodes.spft[dest] + graph.GArcs.temporaryCost[e];
                                        if (newCost + graph.GNodes.spfs[orig] - ub >= Constant.MCR_DECIMAL_PREC)
                                        {
                                                graph.setInStack(e);
                                                removed.push(e);
                                        }
                                        else if (graph.GNodes.spft[orig] > newCost)
                                        {
                                                graph.GNodes.spft[orig] = newCost;
                                                graph.GNodes.nextSP[orig] = e;
                                                update = true;
                                        }
                                }
                        }
                }
                if (!update) this.graph.constraint.fail();
        }

}

public final double getShortestPathValue() {
        return graph.GNodes.spft[graph.sourceIndex];
}

public int[] getShortestPath() {
        int i = 0;
        int current = this.graph.sourceIndex;
        do {
                int e = graph.GNodes.nextSP[current];
                sp[i++]= e;
                current = graph.GArcs.dests[e];

        } while(graph.GNodes.nextSP[current] != Integer.MIN_VALUE);
        return sp;
}

public boolean[] computeShortestAndLongestPath(TIntStack removed, IntDomainVar[] y, AbstractIntSConstraint[] tables) throws ContradictionException {

        int nbr = y.length;

        for (int i = 0 ; i < nbr ; i++){
                spfs[graph.sourceIndex][i] = 0.0;
                spft[graph.tinIndex][i] = 0.0;
                lpfs[graph.sourceIndex][i] = 0.0;
                lpft[graph.tinIndex][i] = 0.0;

        }
        boolean update;

        for (int i = 1 ; i <= nbLayer ; i++)
        {
                update = false;
                int[] list = graph.layers[i]._getStructure();
                int size = graph.layers[i].size();
                for (int w = size -1 ; w >= 0 ; w--) {
                        int dest = list[w];
                        Arrays.fill(spfs[dest],Double.POSITIVE_INFINITY);
                        Arrays.fill(lpfs[dest],Double.NEGATIVE_INFINITY);

                        StoredIndexedBipartiteSet bs = graph.GNodes.inArcs[dest];
                        assert(!bs.isEmpty());
                        final int[] inlist = bs._getStructure();
                        final int insize = bs.size();

                        for (int x = 0 ; x < insize; x++)
                        {
                                int e = inlist[x];
                                if (!graph.isInStack(e))
                                {
                                        int orig = graph.GArcs.origs[e];//.getDestination();
                                        double[] cost= graph.GArcs.originalCost[e];
                                        for (int d = 0 ; d < nbr ; d++)
                                        {
                                                if (spfs[dest][d] > cost[d]+spfs[orig][d])
                                                {
                                                        spfs[dest][d] = cost[d]+spfs[orig][d];
                                                        prevSP[dest][d] = e;
                                                        update = true;
                                                }
                                                if (lpfs[dest][d] < lpfs[orig][d]+ cost[d])
                                                {
                                                        lpfs[dest][d] = lpfs[orig][d]+ cost[d];
                                                        prevLP[dest][d] = e;
                                                        update =true;
                                                }
                                        }
                                }
                        }
                }
                if (!update) this.graph.constraint.fail();
        }
        for (int i = nbLayer -1 ; i >= 0 ; i--)
        {
                update = false;
                int[] list = graph.layers[i]._getStructure();
                int size = graph.layers[i].size();
                for (int w = size -1 ; w >= 0 ; w--) {
                        int orig = list[w];
                        Arrays.fill(spft[orig],Double.POSITIVE_INFINITY);
                        Arrays.fill(lpft[orig],Double.NEGATIVE_INFINITY);
                        StoredIndexedBipartiteSet bs = graph.GNodes.outArcs[orig];
                        assert(!bs.isEmpty());
                        final int[] outlist = bs._getStructure();
                        final int outsize = bs.size();
                        for (int x = 0 ; x < outsize ; x++)
                        {
                                int e = outlist[x];
                                if (!graph.isInStack(e))
                                {
                                        int dest = graph.GArcs.dests[e];
                                        double[] cost= graph.GArcs.originalCost[e];

                                        for (int d = 0 ; d < nbr ; d++)
                                        {
                                                if (spft[dest][d]+cost[d] + spfs[orig][d] - y[d].getSup() >= Constant.MCR_DECIMAL_PREC)
                                                {
                                                        graph.getInStack().set(e);
                                                        removed.push(e);
                                                        break;
                                                }
                                                else if (spft[orig][d] > spft[dest][d]+cost[d])
                                                {
                                                        spft[orig][d] = spft[dest][d]+cost[d];
                                                        nextSP[orig][d] = e;
                                                        update = true;
                                                }

                                                if (lpft[dest][d] + cost[d] + lpfs[orig][d] - y[d].getInf() <= -Constant.MCR_DECIMAL_PREC)
                                                {
                                                        graph.setInStack(e);
                                                        removed.push(e);
                                                        break;
                                                }
                                                else if (lpft[orig][d] < lpft[dest][d]+cost[d])
                                                {
                                                        lpft[orig][d] = lpft[dest][d]+cost[d];
                                                        nextLP[orig][d] = e;
                                                        update = true;
                                                }

                                        }

                                }
                        }

                }              
                if (!update) this.graph.constraint.fail();
        }

        for (int i = 0  ;i < nbr ;i++)
        {
                if (y[i].updateInf((int)Math.ceil(spft[graph.sourceIndex][i]),this.graph.constraint,false))
                {
                        tables[i].awakeOnInf(0);
                }
                if (y[i].updateSup((int)Math.floor(lpft[graph.sourceIndex][i]),this.graph.constraint,false))
                {
                        tables[i].awakeOnSup(0);
                }
        }

        return modified;
}



}
