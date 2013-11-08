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

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.automaton.FA.IAutomaton;
import choco.kernel.model.constraints.automaton.FA.FiniteAutomaton;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Nov 24, 2008
 * Time: 1:18:18 PM
 */
public class LayeredGraph {

    IEnvironment env;
    IStateInt[] lp;
    IStateInt[] sp;
    IntDomainVar[] vars;
    IntDomainVar z;
    IAutomaton pi;
    double[][][] costs;
    Node source;
    Node tink;
    ArrayList<Edge> sortOut;
    ArrayList<Edge> sortIn;
    IStateBitSet activeIn;
    IStateBitSet activeOut;
    ArrayList<Node[]> layers;
    ArrayList<IStateInt> Q;

    AllActiveEdgeIterator activeIterator;
    InEdgeIterator inIterator;
    OutEdgeIterator outIterator;

    int bs;




    public LayeredGraph(IntDomainVar[] vars, IntDomainVar z, IAutomaton pi, double[][][] costs, IEnvironment environment)
    {
        this.env = environment;
        this.vars = vars;
        this.z = z;
        this.pi = pi;
        this.costs = costs;



        bs = Integer.MIN_VALUE;
        for (IntDomainVar v : vars)
        {
            int sz = v.getSup()+1;
            if (bs < sz)
                bs =sz;

        }

        this.sortIn = new ArrayList<Edge>();
        this.sortOut = new ArrayList<Edge>();
        this.Q = new ArrayList<IStateInt>();
        this.Q.ensureCapacity(bs*vars.length);
        for (int i  =0 ; i < bs*vars.length ;i++)
            this.Q.add(null);
        this.layers = new ArrayList<Node[]>();

        this.source=  new Node(env,0,pi.getInitialState());
        this.tink = new Node(env,vars.length+1,Integer.MAX_VALUE);
    }

    public void makeGraph() throws ContradictionException {

        HashSet<Edge> needRemoval = new HashSet<Edge>();
        ArrayList<TreeSet<Node>> layers = new ArrayList<TreeSet<Node>>();
        for (int i = 0 ; i < vars.length+2 ; i++)
            layers.add(new TreeSet<Node>());

        int nb = vars.length;
        ArrayList<Node> tmp = new ArrayList<Node>();
        tmp.add(source);
        tmp.add(tink);
        HashMap<Integer,Node>[] lay = new HashMap[vars.length+2];

        for (int i = 0 ; i < lay.length ; i++)
            lay[i] = new HashMap<Integer,Node>();

        layers.get(0).add(this.source);
        layers.get(vars.length+1).add(this.tink);
        this.source.setLongestPathFromSource(0.0);
        this.source.setShortestPathFromSource(0.0);
        lay[0].put(this.source.state,this.source);

        for (int i = 0 ; i < vars.length ; i++)
        {
            for (int j = vars[i].getInf() ; j <= vars[i].getSup() ; j = vars[i].getNextDomainValue(j))
            {
                for(Node n : lay[i].values())
                {
                    int k = n.state;
                    int succ = 0;
                    try {
                        succ = pi.delta(k,j);
                    } catch (IAutomaton.NonDeterministicOperationException e) {
                        System.err.println("You must use a deterministic automaton with this constraint");
                        throw new RuntimeException();
                    }
                    if (succ >= 0)
                    {
                        Node next = lay[i+1].get(succ);
                        if (next == null)
                        {
                            next = new Node(env,i+1,succ);
                            tmp.add(next);
                            next.setShortestPathFromSource(n.getShortestPathFromSource() + costs[i][j][k]);
                            next.setShortestPathToSource(n.id);

                            next.setLongestPathFromSource(n.getLongestPathFromSource() + costs[i][j][k]);
                            next.setLongestPathToSource(n.id);

                            lay[i+1].put(succ,next);
                        }
                        else
                        {
                            double pccSTmp = next.getShortestPathFromSource();
                            double pccSNew = n.getShortestPathFromSource() + costs[i][j][k];
                            if (pccSTmp > pccSNew)
                            {
                                next.setShortestPathFromSource(pccSNew);
                                next.setShortestPathToTink(n.id);
                            }

                            double pgcSTmp = next.getLongestPathFromSource();
                            double pgcSNew = n.getLongestPathFromSource() + costs[i][j][k];

                            if (pgcSTmp < pgcSNew)
                            {
                                next.setLongestPathFromSource(pgcSNew);
                                next.setLongestPathToSource(n.id);
                            }
                        }
                        incQ(i,j);

                    }
                }

            }
        }

        double[] minmax = {Integer.MAX_VALUE,Integer.MIN_VALUE};
        Node [] prefminmax = {null,null};

        HashSet<Integer> toRem = new HashSet<Integer>();
        for (Node rem : lay[vars.length].values())
        {
            if (!pi.isFinal(rem.state))
            {
                toRem.add(rem.state);
            }
            else
            {
                if (minmax[0] > rem.getShortestPathFromSource())
                {
                    minmax[0] = rem.getShortestPathFromSource();
                    prefminmax[0] = rem;
                }
                if (minmax[1] < rem.getLongestPathFromSource())
                {
                    minmax[1] = rem.getLongestPathFromSource();
                    prefminmax[1] = rem;
                }

                rem.setLongestPathFromTink(0);
                rem.setShortestPathFromTink(0);
                rem.setLongestPathToTink(this.tink.id);
                rem.setShortestPathToSource(this.tink.id);
            }
        }
        this.tink.setShortestPathToSource(prefminmax[0].id);
        this.tink.setShortestPathFromSource(minmax[0]);
        this.tink.setLongestPathToSource(prefminmax[1].id);
        this.tink.setLongestPathFromSource(minmax[1]);
        this.tink.setLongestPathFromTink(0);
        this.tink.setShortestPathFromTink(0);


        for (int k : toRem)
            lay[vars.length].remove(k);

        lay[vars.length+1].put(this.tink.state,this.tink);
        BitSet mark = new BitSet(pi.getNbStates());


        for (int i = nb-1 ; i >=0 ; i--)
        {

            mark.clear(0,pi.getNbStates());
            for (Node n : lay[i].values())
            {
                for (int j = vars[i].getInf() ; j <= vars[i].getSup() ; j = vars[i].getNextDomainValue(j))
                {
                    int fol = 0;
                    try {
                        fol = pi.delta(n.state,j);
                    } catch (IAutomaton.NonDeterministicOperationException e) {
                        System.err.println("You must use a deterministic automaton with this constraint");
                        throw new RuntimeException();
                    }
                    if (fol >= 0)
                    {
                        Node t = lay[i+1].get(fol);
                        if (t!=null)
                        {
                            double c1 = n.getShortestPathFromSource()+t.getShortestPathFromTink()+costs[i][j][n.state];
                            double c2 = n.getLongestPathFromSource()+t.getLongestPathFromTink()+costs[i][j][n.state];
                            Edge e = new Edge(n,t,j);

                            if (c1 > z.getSup() || c2 < z.getInf())
                            {

                                needRemoval.add(e);


                            }

                            // else
                            // {
                            double pccPTmp = n.getShortestPathFromTink();
                            double pccPNew = t.getShortestPathFromTink() + costs[i][j][n.state];

                            if(pccPNew < pccPTmp)
                            {
                                n.setShortestPathFromTink(pccPNew);
                                n.setShortestPathToTink(t.id);
                            }

                            double pgcPTmp = n.getLongestPathFromTink();
                            double pgcPNew = t.getLongestPathFromTink() + costs[i][j][n.state];

                            if (pgcPNew > pgcPTmp)
                            {
                                n.setLongestPathFromTink(pgcPNew);
                                n.setLongestPathToTink(t.id);
                            }
                            layers.get(i+1).add(t);
                            mark.set(n.state);
                            sortOut.add(e);
                            sortIn.add(e);
                            // }
                        }
                        else
                        {
                            decQ(i,j);
                        }
                    }


                }
            }
            Iterator<Integer> it = lay[i].keySet().iterator();
            while (it.hasNext())
                if(!mark.get(it.next()))
                    it.remove();

        }

        for (Node end : lay[nb].values())
        {
            Edge e = new Edge(end,this.tink,0);
            sortOut.add(e);
            sortIn.add(e);
        }


        activeOut = env.makeBitSet(sortOut.size());
        activeIn = env.makeBitSet(sortOut.size());

        for (int i = 0  ; i < sortOut.size() ; i++) {
            activeOut.set(i);
            activeIn.set(i);
        }

        //trier sortIn;

        Collections.sort(sortIn);

        Collections.sort(sortOut, new Edge.OutComparator());

        if (sortOut.size() > 0)
        {

            Node out = sortOut.get(0).orig;
            Node in =sortIn.get(0).dest;

            // Creating the future iterators
            this.inIterator = new InEdgeIterator(in);
            this.outIterator  = new OutEdgeIterator(out);

            in.offsetRev = 0;
            out.offset = 0;

            in.nbEdgesRev = 1;
            out.nbEdges = 1;

            Node next;

            sortOut.get(0).index = 0;
            sortIn.get(0).indexRev = 0;

            for (int i = 0 ; i < sortOut.size()-1 ; i++)
            {
                sortOut.get(i+1).index = i+1;
                out = sortOut.get(i).orig;
                next= sortOut.get(i+1).orig;
                if (out == next)
                {
                    out.nbEdges++;
                }
                else
                {
                    next.offset = i+1;
                    next.nbEdges++;
                }

                sortIn.get(i+1).indexRev = i+1;
                in = sortIn.get(i).dest;
                next = sortIn.get(i+1).dest;
                if (in == next)
                {
                    in.nbEdgesRev++;
                }
                else
                {
                    next.offsetRev = i+1;
                    next.nbEdgesRev++;
                }
            }


            for (TreeSet<Node>  hn : layers)
            {
                Node[] tmp2 = hn.toArray(new Node[hn.size()]);
                this.layers.add(tmp2);
            }
        }



        this.reIndexPred(tmp);


        this.initialFilter();



    }

    private void reIndexPred(List<Node> l)
    {
        Iterator<Edge> iter;
        for (Node n : l)
        {

            int a = n.getLongestPathToSource();
            int c = n.getShortestPathToSource();

            iter = this.getInEdgeIterator(n);
            while(iter.hasNext())
            {
                Edge e = iter.next();
                if (e.orig.id == a)
                {
                    n.setLongestPathToSource(e.index);
                }
                if (e.orig.id == c)
                {
                    n.setShortestPathToSource(e.index);
                }
            }

            iter = this.getOutEdgeIterator(n);
            int b = n.getLongestPathToTink();
            int d = n.getShortestPathToTink();
            while (iter.hasNext())
            {
                Edge e = iter.next();
                if (e.dest.id == b)
                {
                    n.setLongestPathToTink(e.index);
                }
                if (e.dest.id == d)
                {
                    n.setShortestPathToTink(e.index);
                }
            }

        }

    }

    protected void initialFilter() throws ContradictionException {
        for (int idx = 0 ; idx < this.Q.size() ; idx++)
        {
            int i = idx/bs;
            int j = idx%bs;
            IStateInt tmp = this.Q.get(idx);
            if (tmp == null || tmp.get() == 0)
            {
                vars[i].removeVal(j, null, true);
            }
        }
    }

    Stack<Edge> update = new Stack<Edge>();
    public int removeEdge(Edge e) throws ContradictionException {
        int out = 1;
        activeOut.clear(e.index);
        activeOut.clear(e.indexRev);
        decQ(e.orig.layer,e.j);
        Node or = e.orig;
        Node de = e.dest;

        if (e == sortOut.get(e.orig.getLongestPathToTink()))
        {
                
        }


        Iterator<Edge> outIt = getOutEdgeIterator(or);
        Iterator<Edge> inIt;
        if (!outIt.hasNext())
        {
            inIt =getInEdgeIterator(or);
            while (inIt.hasNext())
                out+= removeEdge(inIt.next());
        }
        inIt =getInEdgeIterator(de);
        if (!inIt.hasNext())
        {
            outIt = getOutEdgeIterator(de);
            while(outIt.hasNext())
                out+=removeEdge(outIt.next());
        }
        return out;


    }


    public Iterator<Edge> getOutEdgeIterator(Node n)
    {
        this.outIterator.n = n;
        this.outIterator.lastReturned  = Integer.MIN_VALUE;
        this.outIterator.currentBit = activeOut.nextSetBit(n.offset);
        return this.outIterator;
    }

    protected class OutEdgeIterator implements Iterator<Edge> {

        int currentBit;
        int lastReturned = Integer.MIN_VALUE;
        Node n;

        public OutEdgeIterator(Node n)
        {
            this.n = n;
            this.currentBit = activeOut.nextSetBit(n.offset);
        }


        public boolean hasNext() {
            return currentBit >= 0 && currentBit <= n.offset+n.nbEdges -1;
        }

        public Edge next() {
            Edge out = sortOut.get(currentBit);
            lastReturned = currentBit;
            currentBit = activeOut.nextSetBit(currentBit+1);
            return out;

        }

        public void remove() {
            activeOut.clear(lastReturned);
            Edge e = sortOut.get(lastReturned);
            activeIn.clear(e.indexRev);
        }
    }

    public Iterator<Edge> getInEdgeIterator(Node n)
    {
        this.inIterator.n = n;
        this.inIterator.lastReturned = Integer.MIN_VALUE;
        this.inIterator.currentBit = activeIn.nextSetBit(n.offsetRev);
        return this.inIterator;
    }


    protected class InEdgeIterator implements Iterator<Edge> {

        int currentBit;
        int lastReturned = Integer.MIN_VALUE;
        Node n;

        public InEdgeIterator(Node n)
        {
            this.n = n;
            this.currentBit = activeIn.nextSetBit(n.offsetRev);
        }


        public boolean hasNext() {
            return currentBit >= 0 && currentBit <= n.offsetRev+n.nbEdgesRev -1;
        }

        public Edge next() {
            Edge out = sortIn.get(currentBit);
            lastReturned = currentBit;
            currentBit = activeIn.nextSetBit(currentBit+1);
            return out;

        }

        public void remove() {
            activeIn.clear(lastReturned);
            Edge e = sortIn.get(lastReturned);
            activeOut.clear(e.index);
        }
    }

    public class AllActiveEdgeIterator implements Iterator<Edge>
    {

        int current;

        public AllActiveEdgeIterator()
        {

            current = -1   ;


        }

        public boolean hasNext() {
            return activeOut.nextSetBit(current+1) >= 0;
        }

        public Edge next() {
            current = activeOut.nextSetBit(current+1);
            return sortOut.get(current);
        }

        public void remove() {
            Edge e = sortOut.get(current);
            activeOut.clear(e.index);
            activeIn.clear(e.indexRev);
        }
    }

    public Iterator<Edge> getAllActiveEdgeIterator()
    {
        this.activeIterator.current = -1;
        return this.activeIterator;
    }



    protected IStateInt getQ(int i, int j)
    {
        int idx  = i*bs+j;
        IStateInt tmp = this.Q.get(idx);
        if (tmp == null) {
            tmp = env.makeInt(0);
            this.Q.set(idx,tmp);
        }
        return tmp;
    }

    protected int getQSize(int i, int j)
    {
        return getQ(i,j).get();
    }

    protected void decQ(int i, int j) throws ContradictionException {
        if (i < vars.length)
        {
            IStateInt tmp = getQ(i,j);
            tmp.add(-1);
            if (tmp.get() == 0)
                vars[i].removeVal(j, null, true);
        }

    }
    protected void incQ(int i, int j)
    {
        getQ(i,j).add(1);
    }


    public static void main(String[] args) {
        IntegerVariable[] vs = Choco.makeIntVarArray("x",3,0,2);
        Random r = new Random(0);

        IntegerVariable z0 = Choco.makeIntVar("z",8,10, Options.V_BOUND);

        Model m = new CPModel();
        m.addVariables(vs);
        m.addVariable(z0);

        Solver s = new CPSolver();
        s.read(m);
        IntDomainVar[] vars = s.getVar(vs);
        IntDomainVar z = s.getVar(z0);
        IAutomaton pi = new FiniteAutomaton("222|000|111|012");
        double[][][] csts= new double[vs.length][3][pi.getNbStates()];

        for (int i = 0 ; i < csts.length ;i++)
            for (int j = 0 ; j < csts[i].length ; j++)
                for (int k = 0 ; k < csts[i][j].length ; k++)
                    csts[i][j][k] = r.nextInt(5);


        LayeredGraph g = new LayeredGraph(vars,z,pi,csts, s.getEnvironment());
        try {
            g.makeGraph();
        } catch (ContradictionException e) {
            System.err.println("Excepion");
        }


    }


}
