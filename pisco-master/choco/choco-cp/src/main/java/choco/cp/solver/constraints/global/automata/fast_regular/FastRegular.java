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

package choco.cp.solver.constraints.global.automata.fast_regular;


import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.structure.StoredIndexedBipartiteSet;
import choco.kernel.model.constraints.automaton.FA.IAutomaton;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.global.automata.fast_regular.structure.Arc;
import choco.kernel.solver.constraints.global.automata.fast_regular.structure.Node;
import choco.kernel.solver.constraints.global.automata.fast_regular.structure.StoredDirectedMultiGraph;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntStack;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Oct 30, 2009
 * Time: 3:43:21 PM
 */

public class FastRegular extends AbstractLargeIntSConstraint {


    StoredDirectedMultiGraph graph;



    /**
     * Construct a new explained regular constraint
     * @param environment env
     * @param vars Variables that must form a word accepted by auto
     * @param auto An automaton forming a regular languauge
     */
    public FastRegular(IEnvironment environment, IntDomainVar[] vars, IAutomaton auto) {
        super(ConstraintEvent.LINEAR, vars);

        int aid = 0;
        int nid = 0;


        int[] offsets = new int[vars.length];
        int[] sizes = new int[vars.length];
        int[] starts = new int[vars.length];

        int totalSizes = 0;

        starts[0] = 0;
        for (int i = 0 ; i < vars.length ; i++)
        {
            offsets[i] = vars[i].getInf();
            sizes[i] = vars[i].getSup() - vars[i].getInf()+1;
            if (i > 0) starts[i] = sizes[i-1] + starts[i-1];
            totalSizes += sizes[i];
        }



        DirectedMultigraph<Node,Arc> graph;

        int n = vars.length;
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

        layer.get(0).add(auto.getInitialState());
        TIntHashSet nexts = new TIntHashSet();

        for (i = 0 ; i < n ; i++)
        {
            varIter = vars[i].getDomain().getIterator();
            while(varIter.hasNext())
            {
                j = varIter.next();
                layerIter = layer.get(i).iterator();//getIterator();
                while(layerIter.hasNext())
                {
                    k = layerIter.next();
                    nexts.clear();

                    auto.delta(k,j,nexts);
                    for (TIntIterator it = nexts.iterator() ;it.hasNext();)
                    {
                        int succ = it.next();
                        layer.get(i+1).add(succ);
                        //incrQ(i,j,);
                    }
                    if (!nexts.isEmpty())
                    {
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
            if (!auto.isFinal(k))
            {
                layerIter.remove();
            }

        }


        //backward pass, removing arcs that does not lead to an accepting state
        int nbNodes = auto.getNbStates();
        BitSet mark = new BitSet(nbNodes);

        Node[] in = new Node[auto.getNbStates()*(n+1)];

        for (i = n -1 ; i >=0 ; i--)
        {
            mark.clear(0,nbNodes);
            varIter = vars[i].getDomain().getIterator();
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
                        nexts.clear();
                        auto.delta(k,j,nexts);
                        boolean added = false;
                        for (TIntIterator it = nexts.iterator() ; it.hasNext() ;)
                        {
                            int qn = it.next();
                            if (layer.get(i+1).contains(qn))
                            {

                                added = true;
                                Node a = in[i*auto.getNbStates()+k];
                                if (a == null)
                                {
                                    a = new Node(k,i,nid++);
                                    in[i*auto.getNbStates()+k] = a;
                                    graph.addVertex(a);
                                }

                                Node b = in[(i+1)*auto.getNbStates()+qn];
                                if (b == null)
                                {
                                    b = new Node(qn,i+1,nid++);
                                    in[(i+1)*auto.getNbStates()+qn] = b;
                                    graph.addVertex(b);
                                }


                                Arc arc = new Arc(a,b,j,aid++);
                                graph.addEdge(a,b,arc);
                                tmp.get(idx).add(arc);

                                mark.set(k);
                            }
                        }
                        if (!added)
                            qijIter.remove();
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



        this.graph = new StoredDirectedMultiGraph(environment, this,graph,starts,offsets,totalSizes);


    }


    TIntStack temp  = new TIntStack();

    public void awakeOnRem(int i, int j) throws ContradictionException {
        StoredIndexedBipartiteSet sup = graph.getSupport(i,j);

        if (sup != null)
        {

            DisposableIntIterator it = sup.getIterator();
            while (it.hasNext())
            {
                int arcId = it.next();
                temp.push(arcId);


            }
            it.dispose();

            while(temp.size() > 0)
            {
                int arcId = temp.pop();
                try{
                    graph.removeArc(arcId);
                } catch (ContradictionException e)
                {
                    temp.clear();
                    throw e;
                }
            }

        }
    }



    public void propagate() throws ContradictionException {

    }

    public void awake() throws ContradictionException
    {
        int left, right;
        for (int i  = 0 ; i < vars.length ; i++)
        {
            left = right = Integer.MIN_VALUE;
            for (int j = vars[i].getInf() ; j <= vars[i].getSup() ; j = vars[i].getNextDomainValue(j))
            {
                StoredIndexedBipartiteSet sup = graph.getSupport(i,j);
                if (sup == null || sup.isEmpty())
                {
                    if (j == right + 1) {
                        right = j;
                    } else {
                        vars[i].removeInterval(left, right, this, false);
                        left = right = j;
                    }
//                    vars[i].removeVal(j, this, false);
                }
            }
            vars[i].removeInterval(left, right, this, false);
        }


    }

    public int getFilteredEventMask(int idx)
    {
        return IntVarEvent.REMVAL_MASK;
    }







}
