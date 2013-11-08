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

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBool;
import choco.kernel.memory.structure.StoredIndexedBipartiteSet;
import choco.kernel.model.constraints.automaton.FA.CostAutomaton;
import choco.kernel.model.constraints.automaton.FA.IAutomaton;
import choco.kernel.model.constraints.automaton.FA.ICostAutomaton;
import choco.kernel.model.constraints.automaton.FA.utils.Bounds;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.global.automata.fast_costregular.structure.Arc;
import choco.kernel.solver.constraints.global.automata.fast_costregular.structure.Node;
import choco.kernel.solver.constraints.global.automata.fast_costregular.structure.StoredValuedDirectedMultiGraph;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntStack;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Feb 4, 2010
 * Time: 1:03:04 PM
 */
public class CostRegular extends AbstractLargeIntSConstraint{


    IntDomainVar[] vs;
    IntDomainVar z;
    StoredValuedDirectedMultiGraph graph;
    TIntStack toRemove;
    IStateBool boundChange;
    int lastWorld = -1;
    int lastNbOfBacktracks = -1;
    int lastNbOfRestarts = -1;
    protected final IEnvironment environment;


    //int[][][] costs;
    Solver solver;
    ICostAutomaton pi;
    DirectedMultigraph<Node, Arc> originalGraph;
    Node source;

    private CostRegular(IntDomainVar[] vars,Solver s)
    {
        super(ConstraintEvent.CUBIC, vars);
        this.environment = s.getEnvironment();
        this.solver = s;
        this.vs = new IntDomainVar[vars.length-1];
        System.arraycopy(vars, 0, vs, 0, vs.length);
        this.z = vars[vars.length-1];
        this.toRemove = new TIntStack();
        this.boundChange = environment.makeBool(false);
    }

    public CostRegular(IntDomainVar[] vars, IAutomaton pi, int[][][] costs, Solver s)
    {
        this(vars,s);
        this.pi = CostAutomaton.makeSingleResource(pi,costs,this.z.getInf(),this.z.getSup());
    }

    public CostRegular(IntDomainVar[] vars, IAutomaton pi, int[][] costs, Solver s)
    {
        this(vars,s);
        this.pi = CostAutomaton.makeSingleResource(pi,costs,this.z.getInf(),this.z.getSup());
    }

    public CostRegular(IntDomainVar[] vars, ICostAutomaton pi, Solver s)
    {
        this(vars,s);
        this.pi = pi;
    }



    public CostRegular(IntDomainVar[] vars, DirectedMultigraph<Node, Arc> graph, Node source, Solver s)
    {
        this(vars,s);

        this.originalGraph =graph;
        this.source = source;


    }

    public void initGraph(DirectedMultigraph<Node,Arc> graph, Node source)
    {

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


        TIntArrayList[] layers = new TIntArrayList[vs.length+1];
        for (int i = 0 ; i < layers.length ;i++)
        {
            layers[i] = new TIntArrayList();
        }
        Queue<Node> queue = new ArrayDeque<Node>();
        source.layer = 0;
        queue.add(source);

        int nid = 0;
        int aid = 0;
        while(!queue.isEmpty())
        {
            Node n = queue.remove();
            n.id  = nid++;
            layers[n.layer].add(n.id);
            Set<Arc> tmp = graph.outgoingEdgesOf(n);
            for (Arc a : tmp)
            {
                a.id = aid++;
                Node next = graph.getEdgeTarget(a);
                next.layer = n.layer+1;
                queue.add(next);
            }
        }
        int[][] lays = new int[layers.length][];
        for (int i = 0 ; i < lays.length ;i++)
        {
            lays[i] = layers[i].toNativeArray();
        }
        this.graph = new StoredValuedDirectedMultiGraph(solver.getEnvironment(), this,graph,lays,starts,offsets,totalSizes);



    }


    public void initGraph(ICostAutomaton pi) throws ContradictionException {
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

        layer.get(0).add(pi.getInitialState());

        TIntHashSet succ = new TIntHashSet();
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
                    succ.clear();
                    pi.delta(k,j,succ);
                    if (!succ.isEmpty())
                    {
                        TIntIterator it = succ.iterator();
                        for (;it.hasNext();)
                            layer.get(i+1).add(it.next());
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
            if (!pi.isFinal(k))
            {
                layerIter.remove();
            }

        }


        //backward pass, removing arcs that does not lead to an accepting state
        int nbNodes = pi.getNbStates();
        BitSet mark = new BitSet(nbNodes);

        Node[] in = new Node[pi.getNbStates()*(n+1)];
        Node tink = new Node(pi.getNbStates()+1,n+1,nid++);
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
                        succ.clear();
                        pi.delta(k,j,succ);
                        TIntIterator it = succ.iterator();
                        boolean added = false;
                        for(;it.hasNext();)
                        {
                            int qn = it.next();
                            if (layer.get(i+1).contains(qn))
                            {
                                added = true;
                                Node a = in[i*pi.getNbStates()+k];
                                if (a == null)
                                {
                                    a = new Node(k,i,nid++);
                                    in[i*pi.getNbStates()+k] = a;
                                    graph.addVertex(a);
                                }



                                Node b = in[(i+1)*pi.getNbStates()+qn];
                                if (b == null)
                                {
                                    b = new Node(qn,i+1,nid++);
                                    in[(i+1)*pi.getNbStates()+qn] = b;
                                    graph.addVertex(b);
                                }


                                Arc arc = new Arc(a,b,j,aid++,pi.getCostByState(i,j,a.state));
                                graph.addEdge(a,b,arc);
                                tmp.get(idx).add(arc);

                                // addToOutarc(k,qn,j,i);
                                //  addToInarc(k,qn,j,i+1);
                                mark.set(k);
                            }
                        }
                        if (!added)
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
        for (k = 0 ; k < pi.getNbStates() ; k++)
        {
            Node o = in[n*pi.getNbStates()+k];
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
            for (k = 0 ; k < pi.getNbStates() ; k++)
            {
                Node o = in[i*pi.getNbStates()+k];
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



    public void awake() throws ContradictionException
    {
        checkBounds();
        if (pi !=null)
            initGraph(pi);
        else if (originalGraph != null)
            initGraph(originalGraph,source);
        this.prefilter();
        this.pi = null;
        this.originalGraph = null;
        this.source = null;

    }

private void checkBounds() throws ContradictionException
{
        Bounds bounds = this.pi.getCounters().get(0).bounds();
        z.updateInf(bounds.min.value,this,false);
        z.updateSup(bounds.max.value,this,false);
}


public void prefilter() throws ContradictionException {
        double zinf = this.graph.GNodes.spft.get(this.graph.sourceIndex);
        double zsup = this.graph.GNodes.lpfs.get(this.graph.tinkIndex);

        z.updateInf((int)Math.ceil(zinf), this, false);
        z.updateSup((int)Math.floor(zsup), this, false);

        DisposableIntIterator it = this.graph.inGraph.getIterator();
        //for (int id = this.graph.inGraph.nextSetBit(0) ; id >=0 ; id = this.graph.inGraph.nextSetBit(id+1))  {
        while(it.hasNext())
        {
            int id = it.next();
            int orig = this.graph.GArcs.origs[id];
            int dest = this.graph.GArcs.dests[id];

            double acost = this.graph.GArcs.costs[id];

            double spfs = this.graph.GNodes.spfs.get(orig);
            double lpfs = this.graph.GNodes.lpfs.get(orig);

            double spft = this.graph.GNodes.spft.get(dest);
            double lpft = this.graph.GNodes.lpft.get(dest);


            if ((spfs + spft + acost > z.getSup() || lpfs + lpft + acost < z.getInf()) && !this.graph.isInStack(id))
            {
                this.graph.setInStack(id);
                this.toRemove.push(id);
            }
        }

        it.dispose();

        try
        {
            do
            {
                while (toRemove.size() > 0)
                {
                    int id = toRemove.pop();
                    // toRemove.removeLast();
                    this.graph.removeArc(id,toRemove);
                }
                while (this.graph.toUpdateLeft.size() > 0)
                {
                    this.graph.updateLeft(this.graph.toUpdateLeft.pop(),toRemove);
                }
                while(this.graph.toUpdateRight.size() > 0)
                {
                    this.graph.updateRight(this.graph.toUpdateRight.pop(),toRemove);
                }
            } while (toRemove.size() > 0);
        }
        catch (ContradictionException e)
        {
            toRemove.reset();
            this.graph.inStack.clear();
            this.graph.toUpdateLeft.reset();
            this.graph.toUpdateRight.reset();

            throw e;
        }






        /*for (int i  = 0 ; i < vs.length ; i++)
        {
            for (int j = vs[i].getInf() ; j <= vs[i].getSup() ; j = vs[i].getNextDomainValue(j))
            {
                StoredIndexedBipartiteSet sup = graph.getSupport(i,j);
                if (sup == null || sup.isEmpty())
                {
                    vs[i].removeVal(j,this.getConstraintIdx(i));
                }
            }
        }*/

    }

    protected void checkWorld()
    {
        int currentworld = environment.getWorldIndex();
        int currentbt = solver.getBackTrackCount();
        int currentrestart = solver.getRestartCount();
        //System.err.println("TIME STAMP : "+currentbt+"   BT COUNT : "+solver.getBackTrackCount());
       // assert (currentbt == solver.getBackTrackCount());
        if (currentworld < lastWorld || currentbt != lastNbOfBacktracks || currentrestart > lastNbOfRestarts)
        {
            this.toRemove.reset();
            this.graph.inStack.clear();
            this.graph.toUpdateLeft.reset();
            this.graph.toUpdateRight.reset();
        }
        lastWorld = currentworld;
        lastNbOfBacktracks = currentbt;
        lastNbOfRestarts = currentrestart;
    }


    public void awakeOnRemovals(int idx, DisposableIntIterator it2) throws ContradictionException {
        checkWorld();
        boolean mod = false;
        while (it2.hasNext())
        {
            int val = it2.next();
            StoredIndexedBipartiteSet sup = graph.getSupport(idx,val);
            if (sup != null)
            {
                DisposableIntIterator it=  sup.getIterator();
                while (it.hasNext())
                {
                    int arcId  = it.next();
                    if (!graph.isInStack(arcId))
                    {
                        graph.setInStack(arcId);
                        toRemove.push(arcId);
                        mod = true;
                    }
                }
                it.dispose();
            }

        }
        it2.dispose();
        if (mod)
            this.constAwake(false);

    }

    public void awakeOnInf(int idx) throws ContradictionException {
        checkWorld();
        boundChange.set(true);
        //propagate();
        this.constAwake(false);

    }

    public void awakeOnSup(int idx) throws ContradictionException {
        checkWorld();
        boundChange.set(true);
        //propagate();
        this.constAwake(false);

    }


    public void awakeOnInst(int idx){
        System.err.println("CALLED INST");
    }
    public void awakeOnBounds(int idx){
        System.err.println("CALLED BOUNDS");
    }
    public void awakeOnRem(int idx, int val) {
        System.err.println("CALLED REM");
    }


    @Override
    public void propagate() throws ContradictionException {

        if (boundChange.get())
        {
            boundChange.set(false);
            DisposableIntIterator it = this.graph.inGraph.getIterator();
            //for (int id = this.graph.inGraph.nextSetBit(0) ; id >=0 ; id = this.graph.inGraph.nextSetBit(id+1))  {
            while(it.hasNext())
            {
                int id = it.next();
                int orig = this.graph.GArcs.origs[id];
                int dest = this.graph.GArcs.dests[id];

                double acost = this.graph.GArcs.costs[id];
                double lpfs = this.graph.GNodes.lpfs.get(orig);
                double lpft = this.graph.GNodes.lpft.get(dest);

                double spfs = this.graph.GNodes.spfs.get(orig);
                double spft = this.graph.GNodes.spft.get(dest);


                if ((lpfs + lpft + acost < z.getInf() || spfs + spft + acost > z.getSup()) && !this.graph.isInStack(id))
                {
                    this.graph.setInStack(id);
                    this.toRemove.push(id);
                }
            }
            it.dispose();

        }

        do
        {
            while (toRemove.size() > 0)
            {
                int id = toRemove.pop();
                // toRemove.removeLast();
                this.graph.removeArc(id,toRemove);
            }
            while (this.graph.toUpdateLeft.size() > 0)
            {
                this.graph.updateLeft(this.graph.toUpdateLeft.pop(),toRemove);
            }
            while(this.graph.toUpdateRight.size() > 0)
            {
                this.graph.updateRight(this.graph.toUpdateRight.pop(),toRemove);
            }
        } while (toRemove.size() > 0);


        double zinf = this.graph.GNodes.spft.get(this.graph.sourceIndex);
        double zsup = this.graph.GNodes.lpfs.get(this.graph.tinkIndex);

        z.updateInf((int)Math.ceil(zinf), this, false);
        z.updateSup((int)Math.floor(zsup), this, false);



    }

    public final int getFilteredEventMask(int idx) {
        return (idx < vs.length ? IntVarEvent.REMVAL_MASK : IntVarEvent.BOUNDS_MASK);
    }


    public boolean isSatisfied(int[] tuple)
    {

        int first = this.graph.sourceIndex;
        boolean found;
        double cost = 0.0;
        for (int i = 0 ; i< tuple.length -1 ; i ++)
        {
            found = false;
            StoredIndexedBipartiteSet bs = this.graph.GNodes.outArcs[first];
            DisposableIntIterator it = bs.getIterator();
            while(!found && it.hasNext())
            {
                int idx = it.next();
                if (this.graph.GArcs.values[idx] == tuple[i])
                {
                    found = true;
                    first = this.graph.GArcs.dests[idx];
                    cost+= this.graph.GArcs.costs[idx];
                }
            }
            if (!found)
                return false;

        }
        int intCost = tuple[tuple.length-1];
        return cost == intCost;

    }
}
