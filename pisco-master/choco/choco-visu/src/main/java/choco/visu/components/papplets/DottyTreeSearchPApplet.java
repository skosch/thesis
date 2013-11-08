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

package choco.visu.components.papplets;

import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.real.RealVar;
import choco.kernel.visu.components.IVisuVariable;
import choco.visu.components.bricks.AChocoBrick;
import choco.visu.components.bricks.DotBrick;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 12 nov. 2008
 * Since : Choco 2.0.1
 *
 * {@code DottyTreeSearchPApplet} is the {@code AChocoPApplet} that creates a dotty file of the tree search as output.
 *
 * Powered by Processing    (http://processing.org/)
 */

public final class DottyTreeSearchPApplet extends AChocoPApplet {

    private Var watch = null;
    private int intobjective = 0;
    private int last = -1;
    private Boolean maximize = null;
    private Boolean restart = null;
    private final String fileName;
    private final LinkedList<String> q = new LinkedList<String>();
    private final LinkedList<String> nodes = new LinkedList<String>();
    private final LinkedList<String> edges = new LinkedList<String>();
    private int width = -1;
    private int depth = -1;
    private static final String ROOT = "ROOT";
    private int nodeLimit = 100;


    public DottyTreeSearchPApplet(final Object parameters) {
        super(parameters);

        nodes.add(ROOT + "[shape=circle,style=filled,fillcolor=darkorange,fontcolor=black]\n");
        q.add(ROOT);

        Object[] params = (Object[]) parameters;
        fileName = (String) params[0];
        nodeLimit = (Integer) params[1];
        watch = (Var) params[2];
        maximize = (Boolean) params[3];
        restart = (Boolean) params[4];

        if (watch != null) {
            if (watch instanceof IntDomainVar) {
                if (maximize) {
                    this.intobjective = Integer.MIN_VALUE;
                } else {
                    this.intobjective = Integer.MAX_VALUE;
                }
            }
        }
    }


    //////////////////////////////////////////////////Methods to dot the tree search////////////////////////////////////
    /**
     * Change the node limit
     * Default value : 100
     *
     * @param nodeLimit
     */
    public final void setNodeLimit(final int nodeLimit) {
        this.nodeLimit = nodeLimit;
    }

    /**
     * Update the edges of the graph
     *
     * @param back
     */
    public final void updateEdges(final boolean back) {
        String to = null;
        String from = null;
        StringBuilder sb = new StringBuilder(128);
        if (q.size() > 1) {
            if (back) {
                from = q.removeLast();
                //Cas d'un solveAll ou d'un optimize
                if (from.equals(q.getLast())) from = q.removeLast();
                to = q.getLast();
            } else {
                to = q.getLast();
                from = q.get(q.size() - 2);
            }
            sb.append(from).append(" -> ").append(to);
            if (back) {
                sb.append("[color=red]\n");
            } else {
                sb.append("[color=blue]\n");
            }

            edges.add(sb.toString());
        }
    }

    /**
     * Update the node of the graph
     *
     * @param sol
     */
    public final void updateNodes(final String name, final boolean sol) {
        if (q.getLast().equals(ROOT)) {
            width++;
            depth = -1;
        }
        if (!sol) depth++;
        final StringBuilder node = new StringBuilder("\"").append(name).append('(')
                .append(width).append(',').append(depth).append(")\"");
        q.add(node.toString());
        node.append("[label=\"").append(name).append("\",");
        if (sol) {
            nodes.removeLast();
            node.append("shape=circle,style=filled,fillcolor=green,fontcolor=black]\n");
        } else {
            node.append("shape=circle]\n");
        }
        nodes.add(node.toString());

    }

    /**
     * Update the objective if necessary
     */
    final void updateObjective() {
        if (watch.isInstantiated()) {
            if (watch instanceof IntDomainVar) {
                IntDomainVar v = (IntDomainVar) watch;
                if (((maximize == Boolean.TRUE && v.getVal() > intobjective)
                        || (maximize == Boolean.FALSE && v.getVal() < intobjective))) {
                    intobjective = v.getVal();
                    if (last != -1) {
                        String tmp = nodes.remove(last).replace("green", "palegreen");
                        nodes.add(last, tmp);
                    }
                    last = nodes.size() - 1;
                }
            } else if (watch instanceof RealVar) {
                //TODO : do the realVar  case
            }
        }
    }

    /**
     * Print the dotty graph
     */
    public final void printGraph() {
        if (nodes.size() < nodeLimit) {
            if (watch != null) updateObjective();
            final FileWriter fw;
            try {
                fw = new FileWriter(fileName);
                fw.write("digraph G {\n");

            } catch (IOException e) {
                throw new SolverException("Cannot create the graph file - " + e.getMessage());
            }
            try {
                for (int i = 0; i < nodes.size(); i++) {
                    fw.write(nodes.get(i));
                }
                for (int i = 0; i < edges.size(); i++) {
                    fw.write(edges.get(i));
                }
                fw.write("}");
                fw.close();
            } catch (IOException e) {
                throw new SolverException("Cannot write into the graph file - " + e.getMessage());
            }
        } else {
            LOGGER.warning("TOO MANY NODES :  the dot file will not be generated!");
        }

    }

    /**
     * Clean the queue and restart from scratch
     */
    public final void clean(final boolean checkRestart) {
        if (!checkRestart || restart == Boolean.TRUE) {
            q.clear();
            q.add(ROOT);
        }
    }


    /////////////////////////////////////////////Methods of AChocoPApplet///////////////////////////////////////////////
    /**
     * Initialize the ChocoPApplet with the list of concerning VisuVariables
     *
     * @param list of visu variables o watch
     */
    public final void initialize(final ArrayList<IVisuVariable> list) {
        bricks = new AChocoBrick[list.size()];
        for (int i = 0; i < list.size(); i++) {
            IVisuVariable vv = list.get(i);
            Var v = vv.getSolverVar();
            bricks[i] = new DotBrick(this, v);
            vv.addBrick(bricks[i]);
        }
        this.init();
    }


    private float s = 0;
    private static final float xincrement = (float)0.1;

    /**
     * build the specific PApplet.
     * This method is called inside the {@code PApplet#setup()} method.
     */
    public final void build() {
        size(200, 200);
        stroke(255);
        smooth();
    }

    /**
     * draws the back side of the representation.
     * This method is called inside the {@code PApplet#draw()} method.
     * For exemple, the sudoku grid is considered as a back side
     */
    public final void drawBackSide() {
        background(80);
        fill(100);
        noStroke();
        ellipse(100, 100, 155, 155);
        s += xincrement;
        for (int i = 0; i < 20; i++) {
            stroke(100 + 5 * i);
            strokeWeight(10);
            float j = (float)(0.05 * i);
            line(100, 100, cos(s + j) * 72 + 100, sin(s + j) * 72 + 100);
        }
    }

        /**
         * draws the front side of the representation.
         * This method is called inside the {@code PApplet#draw()} method.
         * For exemple, values of cells in a sudoku are considered as a back side
         */

    public final void drawFrontSide() {
        // nothing to do
    }

    /**
     * Return the ideal dimension of the chopapplet
     *
     * @return
     */
    public final Dimension getDimension() {
        return new Dimension(200, 300);
    }
}
