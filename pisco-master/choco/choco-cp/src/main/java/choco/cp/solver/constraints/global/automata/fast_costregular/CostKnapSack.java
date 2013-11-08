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

package choco.cp.solver.constraints.global.automata.fast_costregular;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.model.constraints.automaton.FA.IAutomaton;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.global.automata.fast_costregular.structure.Arc;
import choco.kernel.solver.constraints.global.automata.fast_costregular.structure.Node;
import choco.kernel.solver.constraints.global.automata.fast_costregular.structure.StoredValuedDirectedMultiGraph;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Feb 8, 2010
 * Time: 6:55:03 PM
 */
public final class CostKnapSack extends CostRegular
{

    IntDomainVar bVar;
    int[] cost;
    int[] gain;

private static IntDomainVar[] merge(IntDomainVar[] vars, IntDomainVar bound, IntDomainVar cost)
    {
        IntDomainVar[] nyv = new IntDomainVar[vars.length+2];
        System.arraycopy(vars,0,nyv,0,vars.length);
        nyv[vars.length] = bound;
        nyv[vars.length+1] = cost;
        return nyv;
    }



    public CostKnapSack(IntDomainVar[] vars, IntDomainVar bVar, IntDomainVar cVar, int[] cost, int[] gain, Solver solver)
    {
        super(merge(vars,bVar,cVar), (IAutomaton) null,(int[][])null, solver);
        this.bVar = bVar;
        this.cost = cost;
        this.gain = gain;
    }

    public void awake() throws ContradictionException {
        this.initGraph();
        this.prefilter();
    }

    public void initGraph() throws ContradictionException {
        int aid = 0;
        int nid = 0;


        int[] offsets = new int[vs.length];
        int[] sizes = new int[vs.length];
        int[] starts = new int[vs.length];

        int totalSizes = 0;

        starts[0] = 0;
        for (int i = 0 ; i < vs.length ; i++)
        {
            offsets[i] = vs[i].getInf();
            sizes[i] = vs[i].getSup() - vs[i].getInf()+1;
            if (i > 0) starts[i] = sizes[i-1] + starts[i-1];
            totalSizes += sizes[i];
        }



        DirectedMultigraph<Node, Arc> graph;

        int n = vs.length;
        graph = new DirectedMultigraph<Node, Arc>(new Arc.ArcFacroty());
        ArrayList<HashSet<Arc>> tmp = new ArrayList<HashSet<Arc>>(totalSizes);
        for (int i = 0 ; i < totalSizes ;i++)
            tmp.add(new HashSet<Arc>());



        int i,j,k;
        DisposableIntIterator varIter;
        TIntIterator layerIter;
        TIntIterator qijIter;

        ArrayList<TIntHashSet> layer = new ArrayList<TIntHashSet>();
        TIntHashSet[] tmpQ = new TIntHashSet[totalSizes];
        // DLList[vars.length+1];

        for (i = 0 ; i <= n ; i++)
        {
            layer.add(new TIntHashSet());// = new DLList(nbNodes);
        }

        //forward pass, construct all paths described by the automaton for word of length nbVars.

        layer.get(0).add(0);

        for (i = 0 ; i < n ; i++)
        {
            varIter = vs[i].getDomain().getIterator();
            while(varIter.hasNext())
            {
                j = varIter.next();
                layerIter = layer.get(i).iterator();//getIterator();
                while(layerIter.hasNext())
                {
                    k = layerIter.next();
                    int succ = delta(i,j,k);
                    if (succ >= 0)
                    {
                        layer.get(i+1).add(succ);
                        //incrQ(i,j,);

                        int idx = starts[i]+j-offsets[i];
                        if (tmpQ[idx] == null)
                            tmpQ[idx] =  new TIntHashSet();

                        tmpQ[idx].add(k);


                    }
                }
            }
            varIter.dispose();
        }

        //removing reachable non accepting states

        layerIter = layer.get(n).iterator();
        while (layerIter.hasNext())
        {
            k = layerIter.next();
            if (!isAccepting(k))
            {
                layerIter.remove();
            }

        }


        //backward pass, removing arcs that does not lead to an accepting state
        int nbNodes = bVar.getSup()+1;
        BitSet mark = new BitSet(nbNodes);

        Node[] in = new Node[nbNodes*(n+1)];
        Node tink = new Node(nbNodes+1,n+1,nid++);
        graph.addVertex(tink);

        for (i = n -1 ; i >=0 ; i--)
        {
            mark.clear(0,nbNodes);
            varIter = vs[i].getDomain().getIterator();
            while (varIter.hasNext())
            {
                j = varIter.next();
                int idx = starts[i]+j-offsets[i];
                TIntHashSet l = tmpQ[idx];
                if (l!= null)
                {
                    qijIter = l.iterator();
                    while (qijIter.hasNext())
                    {
                        k = qijIter.next();
                        int qn = delta(i,j,k);
                        if (layer.get(i+1).contains(qn))
                        {
                            Node a = in[i*nbNodes+k];
                            if (a == null)
                            {
                                a = new Node(k,i,nid++);
                                in[i*nbNodes+k] = a;
                                graph.addVertex(a);
                            }



                            Node b = in[(i+1)*nbNodes+qn];
                            if (b == null)
                            {
                                b = new Node(qn,i+1,nid++);
                                in[(i+1)*nbNodes+qn] = b;
                                graph.addVertex(b);
                            }


                            Arc arc = new Arc(a,b,j,aid++,getCost(i,j));
                            graph.addEdge(a,b,arc);
                            tmp.get(idx).add(arc);

                            // addToOutarc(k,qn,j,i);
                            //  addToInarc(k,qn,j,i+1);
                            mark.set(k);
                        }
                        else
                            qijIter.remove();
                        //  decrQ(i,j);
                    }
                }
            }
            varIter.dispose();
            layerIter = layer.get(i).iterator();

            // If no more arcs go out of a given state in the layer, then we remove the state from that layer
            while (layerIter.hasNext())
                if(!mark.get(layerIter.next()))
                    layerIter.remove();
        }

        TIntHashSet th = new TIntHashSet();
        int[][] intLayer = new int[n+2][];
        for (k = 0 ; k < nbNodes ; k++)
        {
            Node o = in[n*nbNodes+k];
            {
                if (o != null)
                {
                    Arc a = new Arc(o,tink,0,aid++,0.0);
                    graph.addEdge(o,tink,a);
                }
            }
        }


        for (i = 0 ; i <= n ; i++)
        {
            th.clear();
            for (k = 0 ; k < nbNodes ; k++)
            {
                Node o = in[i*nbNodes+k];
                if (o != null)
                {
                    th.add(o.id);
                }
            }
            intLayer[i] = th.toArray();
        }
        intLayer[n+1] = new int[]{tink.id};


        if (intLayer[0].length > 0)
            this.graph = new StoredValuedDirectedMultiGraph(solver.getEnvironment(), this,graph,intLayer,starts,offsets,totalSizes);
        else
            this.fail();
    }


     protected int delta(int i, int j, int k)
    {
        if (i == vs.length -1)
        {
            return (j==k)?0:-1;
        }
        else
        {
            int lgth = k+(cost[i])*(j);
            if (lgth <= bVar.getSup())
                return lgth;
            else
                return -1;
        }
    }

      protected int getCost(int i, int j)
    {

        if (i >= vs.length -1)
            return 0;
        else
            return j * gain[i];
    }

     protected final boolean isAccepting(int idx)
    {
        return idx == 0;
    }

}
