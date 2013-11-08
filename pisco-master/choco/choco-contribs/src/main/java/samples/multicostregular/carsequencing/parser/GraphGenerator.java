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

package samples.multicostregular.carsequencing.parser;

import choco.kernel.model.constraints.automaton.FA.FiniteAutomaton;
import choco.kernel.model.constraints.automaton.FA.IAutomaton;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import gnu.trove.TIntHashSet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;


/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Mail: julien.menana{at}emn.fr
 * Date: Jan 26, 2009
 * Time: 5:11:22 PM
 */
public class GraphGenerator {

    private static class Couple
    {
        int a,b;
        public Couple(int a, int b) {this.a = a ; this.b = b;}
        public boolean equals(Object o)
        {
            if (o instanceof Couple)
            {
                Couple c = (Couple) o;
                return c.a == a && c.b == b;
            }
            return false;
        }
    }

    private static HashMap<Couple,GraphGenerator> map = new HashMap<Couple,GraphGenerator>();

    public static GraphGenerator make(int a, int b)
    {

        Couple tmp = new Couple(a,b);
        GraphGenerator gg = map.get(tmp);
        if (gg == null)
        {
            gg = new GraphGenerator(a,b);
            map.put(tmp,gg);
        }

        return gg;
    }


    int occurence;
    int periode;
    Node root;
    HashSet<Node> nodes;


    private static class Node
    {
        Node parent;
        Node n0;
        Node n1;
        int nocc = 0;
        String seq;


        public Node()
        {
            this(null);
        }

        public Node(Node parent)
        {
            this(parent,null,null);
        }

        public Node(Node parent,Node n0, Node n1)
        {
            this.parent = parent;
            this.n0 = n0;
            this.n1 = n1;
        }


    }


    private GraphGenerator(int occ, int over)
    {
        this.nodes = new HashSet<Node>();
        this.occurence = occ;
        this.periode = over;
        this.root = new Node();
        this.nodes.add(root);
        expand(root,0,0,"");
        sliderize();


    }



    HashMap<String,Node> nodeSeq = new HashMap<String,Node>();


    public void expand(Node n, int occ, int count,String seq)
    {
        if (count < this.periode-1)
        {
            n.n0 = new Node(n);
            this.nodes.add(n.n0);
            expand(n.n0,occ,count+1,seq+"0");
            if (occ < this.occurence)
            {
                n.n1 = new Node(n);
                this.nodes.add(n.n1);
                expand(n.n1,occ+1,count+1,seq+"1");
            }
        }
        else {

            n.nocc = occ;
            n.seq = seq;
            nodeSeq.put(seq,n);

        }

    }
    public void sliderize()
    {
        for (Node n : nodeSeq.values())
        {
            String subs = n.seq.substring(1,n.seq.length());
            n.n0 = nodeSeq.get(subs+"0");
            if (n.nocc < this.occurence)
            {
                n.n1 = nodeSeq.get(subs+"1");
            }
        }
    }

    public Automaton toBricsAutomaton(final int[] authorize, final int[] forbiden)
    {
        Automaton auto = new Automaton();
        HashMap<Node,State> nsmap = new HashMap<Node,State>();
        HashSet<Node> visited = new HashSet<Node>();
        for (Node n : this.nodes)
        {
            State s  = new State();
            s.setAccept(true);
            nsmap.put(n,s);
        }
        copyFromGraph(this.root,nsmap,visited,authorize, forbiden);




        auto.setInitialState(nsmap.get(this.root));

        auto.restoreInvariant();
        auto.minimize();

        return auto;
    }

    private void copyFromGraph(Node n,HashMap<Node,State> nsmap,HashSet<Node> visited,final int[] authorize, final int[] forbiden)
    {
        if (!visited.contains(n))
        {
            visited.add(n);
            State s = nsmap.get(n);

            if (n.n0 != null)
            {
                State s2 = nsmap.get(n.n0);
                for (int tr : forbiden)
                    s.addTransition(new Transition((char) (tr+48),s2));
                copyFromGraph(n.n0,nsmap,visited,authorize,forbiden);
            }
            if (n.n1 != null)
            {
                State s2 = nsmap.get(n.n1);
                for (int tr : authorize)
                    s.addTransition(new Transition((char) (tr+48),s2));
                copyFromGraph(n.n1,nsmap,visited,authorize,forbiden);
            }
        }

    }


    public static void write(String s)
    {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter("brics.dot"));
            out.write(s);
            out.close();

        } catch (IOException ignored) {

        }


    }


    public static void main(String[] args) {
        GraphGenerator gg = GraphGenerator.make(1,5);
        TIntHashSet alpha = new TIntHashSet();
        alpha.add(1);alpha.add(2);alpha.add(8);alpha.add(9);
        Automaton auto = gg.toBricsAutomaton(new int[]{1,2},new int[]{8,9});
        write(auto.toDot());
        FiniteAutomaton a = new FiniteAutomaton();

        a.fill(auto,alpha);

    }


    


}