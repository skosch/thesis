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

import choco.cp.solver.CPSolver;
import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.Constant;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.structure.StoredIndexedBipartiteSet;
import choco.kernel.model.constraints.automaton.FA.IAutomaton;
import choco.kernel.model.constraints.automaton.penalty.IPenaltyFunction;
import choco.kernel.model.constraints.automaton.penalty.IsoPenaltyFunction;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.global.automata.fast_multicostregular.algo.SoftPathFinder;
import choco.kernel.solver.constraints.global.automata.fast_multicostregular.structure.Arc;
import choco.kernel.solver.constraints.global.automata.fast_multicostregular.structure.Node;
import choco.kernel.solver.constraints.global.automata.fast_multicostregular.structure.SoftStoredMultiValuedDirectedMultiGraph;
import choco.kernel.solver.constraints.integer.AbstractIntSConstraint;
import choco.kernel.solver.constraints.integer.AbstractLargeIntSConstraint;
import choco.kernel.solver.propagation.event.ConstraintEvent;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIterator;
import gnu.trove.TIntStack;
import gnu.trove.TObjectIntHashMap;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Apr 27, 2010
 * Time: 11:04:12 AM
 */
public class SoftMultiCostRegular extends AbstractLargeIntSConstraint
{

/** table constraint linking z and y */
AbstractIntSConstraint[] tableConstraints;

/** Sequence variables */
IntDomainVar[] x;

/** Counter variables */
IntDomainVar[] y;

/** Penalty variables */
IntDomainVar[] z;

/** Overall violation variable */
IntDomainVar Z;

/** The penalty functions linking y and z */
IPenaltyFunction[] f;

/** Automaton describing the authorized words */
IAutomaton pi;

/** Cost Function */
int[][][][] costs;

/** Attached solver */
CPSolver solver;

/** Index r which sum(z_r) = Z */
TIntHashSet indexes;

/** Stored Directed multi valued multi graph */
SoftStoredMultiValuedDirectedMultiGraph graph;

/** Class used to compute path in layered graph */
SoftPathFinder path;

/**
 * Map to retrieve rapidly the index of a given variable.
 */
public final TObjectIntHashMap<IntDomainVar> map;

/**
 * The last computed Shortest Path
 */
public int[] lastSp;
public double lastSpValue;


/**
 * The last computed Longest Path
 */
public int[] lastLp;
public double lastLpValue;



final static int U0 = 10;
int lastWorld = -1;
int lastNbOfBacktracks = -1;
int lastNbOfRestarts =-1;

/**
 * Stack to store removed edges index, for delayed update
 */
protected final TIntStack toRemove;

protected final TIntStack[] toUpdateLeft;
protected final TIntStack[] toUpdateRight;

int xOff;
int yOff;
int zOff;
int Zidx;

private TIntHashSet boundUpdate;
private boolean computed;

private static final double PRECISION = Math.pow(10,Constant.MCR_PRECISION);


public SoftMultiCostRegular(IntDomainVar[] x, IntDomainVar[] y, IntDomainVar[] z, IntDomainVar Z, int[] indexes, IPenaltyFunction[] f, IAutomaton pi, int[][][][] costs, CPSolver solver)
{
        super(ConstraintEvent.VERY_SLOW, ArrayUtils.append(x,y,z,new IntDomainVar[]{Z}));
        this.x  = x;
        this.y = y;
        this.z = z;
        this.Z = Z;
        this.f = f;
        this.pi = pi;
        this.costs = costs;
        this.indexes = new TIntHashSet(indexes);
        this.solver = solver;

        this.toRemove = new TIntStack();
        this.toUpdateLeft = new TIntStack[y.length];
        this.toUpdateRight = new TIntStack[y.length];

        for (int i = 0 ; i < toUpdateLeft.length ; i++)
        {
                this.toUpdateLeft[i] = new TIntStack();
                this.toUpdateRight[i] = new TIntStack();
        }
        this.xOff = 0;
        this.yOff = x.length;
        this.zOff = yOff+y.length;
        this.Zidx = zOff+z.length;
        this.map = new TObjectIntHashMap<IntDomainVar>();
        for (int i = 0 ; i < x.length ; i++)
        {
                this.map.put(x[i],i);
        }

        this.boundUpdate = new TIntHashSet();


}




public void initGraph()
{
        int aid = 0;
        int nid = 0;


        int[] offsets = new int[x.length];
        int[] sizes = new int[x.length];
        int[] starts = new int[x.length];

        int totalSizes = 0;

        starts[0] = 0;
        for (int i = 0 ; i < x.length ; i++)
        {
                offsets[i] = x[i].getInf();
                sizes[i] = x[i].getSup() - x[i].getInf()+1;
                if (i > 0) starts[i] = sizes[i-1] + starts[i-1];
                totalSizes += sizes[i];
        }



        DirectedMultigraph<Node,Arc> graph;

        int n = x.length;
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
        TIntHashSet nexts = new TIntHashSet();

        for (i = 0 ; i < n ; i++)
        {
                varIter = x[i].getDomain().getIterator();
                while(varIter.hasNext())
                {
                        j = varIter.next();
                        layerIter = layer.get(i).iterator();//getIterator();
                        while(layerIter.hasNext())
                        {
                                k = layerIter.next();
                                nexts.clear();
                                pi.delta(k,j,nexts);
                                TIntIterator it = nexts.iterator();
                                for (;it.hasNext();)
                                {
                                        int succ = it.next();
                                        layer.get(i+1).add(succ);
                                }
                                if(!nexts.isEmpty())
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
                varIter = x[i].getDomain().getIterator();
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
                                        pi.delta(k,j,nexts);
                                        if (nexts.size() > 1)
                                                System.err.println("STOP");
                                        boolean added = false;
                                        for (TIntIterator it = nexts.iterator();it.hasNext();)
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

        TIntHashSet th = new TIntHashSet();
        int[][] intLayer = new int[n+2][];
        for (k = 0 ; k < pi.getNbStates() ; k++)
        {
                Node o = in[n*pi.getNbStates()+k];
                {
                        if (o != null)
                        {
                                Arc a = new Arc(o,tink,0,aid++);
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
                this.graph = new SoftStoredMultiValuedDirectedMultiGraph(solver.getEnvironment(), this,graph,intLayer,starts,offsets,totalSizes,costs,y);
}

private void makePathFinder()
{
        this.path = new SoftPathFinder(this.graph);
        this.graph.pf = path;
}


public boolean updateViolationLB() throws ContradictionException
{
        boolean modbound = false;
        double[] lambda = new double[y.length];
        Arrays.fill(lambda,0);
        //  lambda[0] = 1.0;
        double step;
        int k=0;
        int[] sp;
        double value;
        boolean modif;


        do
        {


                modif = false;
                double ghat = ghatSP(lambda);
                double gline = glineSP(lambda,ghat);
                sp = path.getShortestPath();
                value = ghat+gline;

                modbound |= Z.updateInf((int) Math.ceil(Math.round((value)*PRECISION)/PRECISION),this,true);


                step = U0 *Math.pow(0.7,k) ;

                for (int i = 0 ; i < lambda.length ; i++)
                {
                        double dimcost = 0.0;
                        for (int e = 0 ; e < x.length ; e++)
                        {
                                dimcost+= graph.GArcs.originalCost[sp[e]][i];
                        }
                        double move = step*(dimcost - y[i].getSup());
                        if (Math.abs(move) >= Constant.MCR_DECIMAL_PREC)
                        {
                                lambda[i] += move;
                                modif = true;
                        }
                }

        } while (k++ < 0 && modif);

        this.lastSp = sp;
        this.lastSpValue = value;
        return modbound;

}

private double glineSP(double[] lambda, double constante) throws ContradictionException
{
        path.computeShortestPath(toRemove,Z.getSup()-constante,lambda);

        return path.getShortestPathValue();
}

private double ghatSP(double[] lambda)
{

        double ghat = 0;
        final int R = lambda.length;
        for (int r = 0 ; r < R ; r++)
        {
                if (indexes.contains(r))
                        ghat+= f[r].minGHat(lambda[r],y[r]);
                else
                        ghat+= -lambda[r] *  ((lambda[r] > 0) ? y[r].getSup() : y[r].getInf());
        }
        return ghat;
}




public boolean updateViolationUB() throws ContradictionException
{
        boolean modBound = false;
        double[] lambda = new double[y.length];
        //Arrays.fill(lambda,0);
        double step;
        int k=0;
        int[] lp;
        double value;
        boolean modif;


        do
        {

                boolean tmp = true;

                modif = false;
                double ghat = ghatLP(lambda);
                double gline = glineLP(lambda,ghat);
                value = ghat+gline;
                lp = path.getLongestPath();


                modBound |= Z.updateSup((int) Math.floor( Math.round((value)*PRECISION)/PRECISION),this,true);

                step = U0 *Math.pow(0.7,k) ;

                for (int i = 0 ; i < lambda.length ; i++)
                {
                        double dimcost = 0.0;
                        for (int e = 0 ; e < x.length ; e++)
                        {
                                dimcost+= graph.GArcs.originalCost[lp[e]][i];
                        }
                        double move = step*(dimcost - y[i].getInf());
                        if (Math.abs(move) >= Constant.MCR_DECIMAL_PREC)
                        {
                                lambda[i] -= move;
                                modif = true;
                        }
                }

        } while (k++ < 0 && modif);
        this.lastLp = lp;
        this.lastLpValue = value;
        return modBound;

}

private double glineLP(double[] lambda, double constante) throws ContradictionException
{
        path.computeLongestPath(toRemove,Z.getInf()-constante,lambda);
        return path.getLongestPathValue();
}

private double ghatLP(double[] lambda)
{
        double ghat = 0;
        final int R = lambda.length;
        for (int r = 0 ; r < R ; r++)
        {
                if (indexes.contains(r))
                        ghat+= f[r].maxGHat(lambda[r],y[r]);
                else
                        ghat+= -lambda[r] *  ((lambda[r] < 0) ? y[r].getSup() : y[r].getInf());
        }
        return ghat;
}


public void makeTableConstraints() throws ContradictionException
{
        tableConstraints  = new AbstractIntSConstraint[y.length];
        for (int i  = 0 ; i < tableConstraints.length ; i++)
        {
                AbstractIntSConstraint table;
                if (y[i].getDomain().isEnumerated())
                {
                        List<int[]> tuples = new ArrayList<int[]>();
                        DisposableIntIterator it = y[i].getDomain().getIterator();
                        while (it.hasNext())
                        {
                                int val = it.next();
                                int other = f[i].penalty(val);
                                if (z[i].canBeInstantiatedTo(other))
                                        tuples.add(new int[]{val,other});
                        }
                        it.dispose();
                        table = (AbstractIntSConstraint)solver.feasiblePairAC(y[i],z[i],tuples,32);
                }
                else if (f[i] instanceof IsoPenaltyFunction)
                {
                        int fact = ((IsoPenaltyFunction)f[i]).getFactor();
                        table = (AbstractIntSConstraint) solver.eq(solver.mult(fact,y[i]),z[i]);
                }
                else
                {
                        LOGGER.severe("Cannot create table constraint! domain is too big.");
                        throw new UnsupportedOperationException();
                }

                table.awake();
                tableConstraints[i] = table;

        }
}

private void makeRedondantSumConstraint()
{
        IntDomainVar[] summed = new IntDomainVar[indexes.size()];
        int idx = 0;
        for (TIntIterator it = indexes.iterator() ; it.hasNext();)
        {
                summed[idx++] = z[it.next()];
        }
        solver.post(solver.eq(solver.sum(summed),Z));
}


public void checkWorld() throws ContradictionException
{
        int currentworld = solver.getEnvironment().getWorldIndex();
        int currentbt = solver.getBackTrackCount();
        int currentrestart = solver.getRestartCount();
        if (currentworld < lastWorld || currentbt != lastNbOfBacktracks || currentrestart > lastNbOfRestarts)
        {
                for (int i = 0 ; i < y.length ; i++)
                {
                        this.toUpdateLeft[i].reset();
                        this.toUpdateRight[i].reset();
                }

                this.toRemove.reset();
                this.graph.inStack.clear();
                path.computeShortestAndLongestPath(toRemove,y,tableConstraints);
                computed = true;
                // this.delayedGraphUpdate();

        }
        lastWorld = currentworld;
        lastNbOfBacktracks = currentbt;
        lastNbOfRestarts = currentrestart;
}


public void awake() throws ContradictionException
{
        makeTableConstraints();
        // makeRedondantSumConstraint();
        //  solver.post(solver.eq(x[0],0));
        //  solver.post(solver.eq(x[2],0));

        initGraph();
        makePathFinder();
        int left, right;
        for (int i  = 0 ; i < x.length ; i++)
        {
                left = right = Integer.MIN_VALUE;
                for (int j = x[i].getInf() ; j <= x[i].getSup() ; j = x[i].getNextDomainValue(j))
                {
                        StoredIndexedBipartiteSet sup = graph.getSupport(i,j);
                        if (sup == null || sup.isEmpty())
                        {
                            if (j == right + 1) {
                                right = j;
                            } else {
                                x[i].removeInterval(left, right, this, false);
                                left = right = j;
                            }
//                                x[i].removeVal(j, this, false);
                        }
                }
                x[i].removeInterval(left, right, this, false);
        }
        propagate();

}


public void awakeOnRem(final int idx, final int val) throws ContradictionException {
        checkWorld();
        if (idx < yOff)
        {
                StoredIndexedBipartiteSet support = this.graph.getSupport(idx,val);
                if (support != null)
                {
                        final int[] list = support._getStructure();
                        final int size = support.size();
                        for (int i = 0 ; i < size ; i++)//while (it.hasNext())
                        {
                                int e = list[i];//t.next();
                                if (!graph.isInStack(e))
                                {
                                        graph.setInStack(e);
                                        toRemove.push(e);
                                }
                        }
                        if (toRemove.size() > 0)
                        {
                                this.constAwake(false);
                        }

                }
        }
        else if (idx < zOff)
        {
                tableConstraints[idx-yOff].awakeOnRem(0,val);
                boundChange(idx);

        }
        else if (idx < Zidx)
        {
                tableConstraints[idx-zOff].awakeOnRem(1,val);
        }

}

public void awakeOnInst(int idx) throws ContradictionException
{
        checkWorld();
        if (idx >= yOff)
        {
                if (idx < zOff)
                {
                        tableConstraints[idx-yOff].awakeOnInst(0);
                        boundChange(idx);
                }
                else if (idx < Zidx)
                {
                        tableConstraints[idx-zOff].awakeOnInst(1);
                }
        }
        if (idx == Zidx)
                System.err.print("");
        this.constAwake(false);
}

public void awakeOnSup(int idx) throws ContradictionException
{
        checkWorld();
        if (idx >= yOff)
        {
                if (idx < zOff)
                {
                        tableConstraints[idx-yOff].awakeOnSup(0);
                        boundChange(idx);

                }
                else if (idx < Zidx)
                {
                        tableConstraints[idx-zOff].awakeOnSup(1);
                }
        }
        this.constAwake(false);
}

public void awakeOnInf(int idx) throws ContradictionException
{
        checkWorld();
        if (idx >= yOff)
        {
                if (idx < zOff)
                {
                        tableConstraints[idx-yOff].awakeOnInf(0);
                        boundChange(idx);

                }
                else if (idx < Zidx)
                {
                        tableConstraints[idx-zOff].awakeOnInf(1);
                }
        }
        this.constAwake(false);
}

public void boundChange(final int idx)
{
        boundUpdate.add(idx-yOff);
        computed =false;
}


int count = 0;

@Override
public void propagate() throws ContradictionException
{
        count++;
        checkWorld();
        //this.delayedGraphUpdate();
        this.delayedBoundUpdate();
        boolean b = this.delayedGraphUpdate();
        b|=  this.updateViolationLB();
        b|= this.updateViolationUB();
        b |= this.delayedGraphUpdate();
        while (b)
        {
                if (b = this.updateViolationLB())
                        b = this.updateViolationUB();
                this.delayedGraphUpdate();

        }
        assert(check());
        //this.delayedGraphUpdate();

//        this.updateViolationUB();

}

private void delayedBoundUpdate() throws ContradictionException
{
        if (!computed && boundUpdate.size() > 0)
        {

                //this.slp.computeShortestAndLongestPath(toRemove,z);
                this.getGraph().delayedBoundUpdate(toRemove,y,boundUpdate.toArray());
                boundUpdate.clear();
        }
}

/**
 * Updates the graphs w.r.t. the caught event during event-based propagation
 * @throws ContradictionException if removing an edge causes a domain to be emptied
 */
protected boolean delayedGraphUpdate() throws ContradictionException {

        boolean modBound = false;
        do
        {
                while (toRemove.size() > 0)
                {
                        int n = toRemove.pop();
                        this.graph.removeArc(n, toRemove,toUpdateLeft,toUpdateRight);
                }
                for (int k = 0 ; k < y.length ; k++)
                {
                        while (this.toUpdateLeft[k].size() > 0)
                        {
                                modBound |= this.graph.updateLeft(this.toUpdateLeft[k],toRemove,k,tableConstraints[k]);
                                if (toRemove.size() > 0) break;
                        }
                        while(this.toUpdateRight[k].size() > 0)
                        {
                                modBound |= this.graph.updateRight(this.toUpdateRight[k],toRemove,k,tableConstraints[k]);
                                if (toRemove.size() > 0) break;
                        }
                }




        } while (toRemove.size() > 0) ;

        // System.err.println("MAX : "+max);
        //  this.prefilter();
        return modBound;
}


public int getFilteredEventMask(int idx) {
        return (idx < yOff ? IntVarEvent.REMVAL_MASK :  IntVarEvent.INSTINT_MASK + IntVarEvent.INCINF_MASK + IntVarEvent.DECSUP_MASK);
}


public boolean check()
{
        int[] word = new int[x.length] ;
        for (int i = 0; i < x.length ; i++)
        {
                if (!x[i].isInstantiated())
                        return true;
                word[i] = x[i].getVal();
        }
        for (IntDomainVar aZ : z) {
                if (!aZ.isInstantiated()) return true;
        }
        return check(word);
}

public boolean check(int[] word)
{
        if (!pi.run(word))
        {
                System.err.println("Word is not accepted by the automaton");
                System.err.print("{"+word[0]);
                for (int i = 1 ; i < word.length ;i++)
                        System.err.print(","+word[i]);
                System.err.println("}");

                return false;
        }
        int[] gcost = new int[z.length];
        for (int l = 0 ; l < graph.layers.length -2; l++)
        {
                DisposableIntIterator it = graph.layers[l].getIterator();
                while (it.hasNext())
                {
                        int orig = it.next();
                        DisposableIntIterator arcIter = graph.GNodes.outArcs[orig].getIterator();
                        while (arcIter.hasNext())
                        {
                                int arc = arcIter.next();
                                for (int i = 0 ;i < z.length ; i++)
                                        gcost[i] += graph.GArcs.originalCost[arc][i];
                        }
                        arcIter.dispose();

                }
                it.dispose();
        }
        for (int i = 0 ;i < gcost.length ; i++)
        {
                if (!z[i].isInstantiated() || !y[i].isInstantiated())
                {
                        LOGGER.severe("Error, z["+i+"] in SMCR should be instantiated : "+z[i]);
                        return false;
                }
                else if (y[i].getVal() != gcost[i])
                {
                        LOGGER.severe("counter: "+gcost[i]+" != y:"+y[i].getVal());
                        return false;
                }
                else if (z[i].getVal() != f[i].penalty(gcost[i]))
                {
                        LOGGER.severe("penalty_"+i+": "+f[i].penalty(gcost[i])+" != z:"+z[i].getVal());
                        return false;
                }

        }
        return true;

}

public boolean isSatisfied(int[] tuple)
{
        int[] tmp = new int[x.length];
        System.arraycopy(tuple,0,tmp,0,tmp.length);
        return check(tmp);
}


public final boolean needPropagation()
{
        int currentworld = solver.getEnvironment().getWorldIndex();
        int currentbt = solver.getBackTrackCount();
        int currentrestart = solver.getRestartCount();

        return (currentworld < lastWorld || currentbt != lastNbOfBacktracks || currentrestart > lastNbOfRestarts);

}

public SoftStoredMultiValuedDirectedMultiGraph getGraph()
{
        return this.graph;
}

public int getMinPathCostForAssignment(int col, int val, int... resources) { return this.graph.getMinPathCostForAssignment(col, val, resources); }
public int[] getMinMaxPathCostForAssignment(int col, int val, int... resources) { return this.graph.getMinMaxPathCostForAssignment(col, val, resources); }
public int getMinPathCost(int... resources) { return this.graph.getMinPathCost(resources); }

public double[] getInstantiatedLayerCosts(int layer) { return this.graph.getInstantiatedLayerCosts(layer);}

}
