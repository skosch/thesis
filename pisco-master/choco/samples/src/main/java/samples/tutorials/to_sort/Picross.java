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

package samples.tutorials.to_sort;


import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.automaton.DFA;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static choco.Choco.makeIntVar;
import static choco.Choco.regular;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: 30 juil. 2007
 * Time: 09:30:26
 */

/**
 * This class implements a nonogram strategy.
 */


public class Picross extends CPModel {

    /**
     * The model variables.
     */
    public IntegerVariable[][] myvars;
    public IntegerVariable[][] dualmyvars;

    /**
     * The width of the nonogram
     */
    int X;

    /**
     * The height of the nonogram
     */
    int Y;

    /**
     * The pattern constraints on the rows
     */
    int[][] consRows;

    /**
     * The pattern constraints on the columns
     */
    int[][] consCols;


    /**
     * The automatons used to describe the pattern constraints
     */
    DFA[] dfas;


    /**
     * Create a new nanogram
     * @param consRows The constraint on the rows
     * @param consCols The constraint on the columns
     * @param s
     */

    public Picross(int[][] consRows,int[][] consCols, Solver s) {
        X=consCols.length;
        Y=consRows.length;


        this.consRows = consRows;
        this.consCols = consCols;



        this.makeVar();
        this.makeDFAs();
        this.makeConstraint();
        s.read(this);
    }

    /**
     * Creates the choco variable :
     * each variable represents a square, it is
     * wether 0 (white) or 1 (black)
     */
    public void makeVar() {
        myvars = new IntegerVariable[X][Y];
        dualmyvars = new IntegerVariable[Y][X];
        for (int i = 0 ; i < X ; i++){
            for (int j = 0 ; j < Y ; j++){
                myvars[i][j] = makeIntVar("var "+i+" "+j+" ",0,1);
                dualmyvars[j][i] = myvars[i][j];
            }
        }
    }


    /**
     * Creates the Automaton used to describe the pattern constraints.
     * makeDFAs() first converts the two int[][] into regular expression
     * then it makes automatons out of the regular expressions.
     */
    public void makeDFAs() {
        dfas = new DFA[consRows.length + consCols.length];
        int idx = 0;
        for (int[] tab : consRows) {
            String regexp = "0*";
            for (int i = 0 ; i < tab.length ; i++) {
                for (int j = 0 ; j < tab[i] ; j++) {
                    regexp+="1";
                }
                if (i == tab.length - 1) {
                    regexp += "0*";
                }
                else {
                    regexp += "0+";
                }
            }
            LOGGER.info(regexp);
            dfas[idx++] = new DFA(regexp,X);
        }

        for (int[] tab : consCols) {
            String regexp = "0*";
            for (int i = 0 ; i < tab.length ; i++) {
                for (int j = 0 ; j < tab[i] ; j++) {
                    regexp+="1";
                }
                if (i == tab.length - 1) {
                    regexp += "0*";
                }
                else {
                    regexp += "0+";
                }
            }
            LOGGER.info(regexp);
            dfas[idx++] = new DFA(regexp,Y);
        }



    }


    /**
     * Post the regular constraint with the DFAs created earlier
     */
    public void makeConstraint() {

        Constraint[] cons = new Constraint[X+Y];

        for (int i = 0 ; i < X ; i++) {
            cons[i] = regular(myvars[i], dfas[i]);
        }

        for (int i = 0 ; i < Y ; i++) {
            cons[i+X] = regular(dualmyvars[i], dfas[i+X]);
        }
        
        for (Constraint c : cons)
            this.addConstraint(c);

    }

    public String toString(Solver solver) {
        StringBuffer s = new StringBuffer();
        for (int i = 0 ; i < X ; i++) {
            for (int j = 0 ; j < Y ; j++) {
                s.append(solver.getVar(myvars[i][j]).getVal()).append("\t");

            }
            s.append("\n");
        }

        return s.toString();
    }

    /**
     * Class to draw the solution on a JPanel.
     */
    private class Drawing extends JPanel {

        Solver solver;
        public Drawing(Solver s) {
            this.solver = s;
            this.setSize(new Dimension(X,Y));
            this.setEnabled(true);
        }


        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            int szX = 600/X;
            int szY = 600/Y ;
            for (int i = 0 ; i < X ; i++) {
                for (int j = 0 ; j < Y ; j++) {
                    g.setColor(java.awt.Color.gray);
                    g.fillRect(j*szY,i*szX,szY,szX);

                    if (solver.getVar(myvars[i][j]).getVal() == 1)
                        g.setColor(java.awt.Color.black);
                    else
                        g.setColor(java.awt.Color.white);

                    g.fillRect(j*szY+2,i*szX+2,szY-2,szX-2);

                }
            }

        }


    }

    /**
     * Draw the solution in a new Frame
     */
    public void showSolution(Solver s)  {
        JFrame frame = new JFrame();
        frame.setTitle("NonoGram");
        frame.setSize(600, 600+Y);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        Container contentPane = frame.getContentPane();
        contentPane.add(new Drawing(s));

        frame.setVisible(true);
    }


    public static void main(String[] args) {
        Picross p;
        Solver s = new CPSolver();

        /**
         * Creates a new nanogram, the parameters indicates how many black squares there must be and
         * the pattern it must follow. i.e. 1,1,1 means, 0 or more white, 1 black, 1 or more white, 1 black,
         * 1 or more white, 1 black, 0 or more white.
         */
        p = new Picross
                (
                        new int[][]
                                {
                                        new int[]{3},
                                        new int[]{5},
                                        new int[]{3,1},
                                        new int[]{2,1},
                                        new int[]{3,3,4},
                                        new int[]{2,2,7},
                                        new int[]{6,1,1},
                                        new int[]{4,2,2},
                                        new int[]{1,1},
                                        new int[]{3,1},
                                        new int[]{6},
                                        new int[]{2,7},
                                        new int[]{6,3,1},
                                        new int[]{1,2,2,1,1},
                                        new int[]{4,1,1,3},
                                        new int[]{4,2,2},
                                        new int[]{3,3,1},
                                        new int[]{3,3},
                                        new int[]{3},
                                        new int[]{2,1}
                                },
                        new int[][]
                                {
                                        new int[]{2},
                                        new int[]{1,2},
                                        new int[]{2,3},
                                        new int[]{2,3},
                                        new int[]{3,1,1},
                                        new int[]{2,1,1},
                                        new int[]{1,1,1,2,2},
                                        new int[]{1,1,3,1,3},
                                        new int[]{2,6,4},
                                        new int[]{3,3,9,1},
                                        new int[]{5,3,2},
                                        new int[]{3,1,2,2},
                                        new int[]{2,1,7},
                                        new int[]{3,3,2},
                                        new int[]{2,4},
                                        new int[]{2,1,2},
                                        new int[]{2,2,1},
                                        new int[]{2,2},
                                        new int[]{1},
                                        new int[]{1}
                                }, s
                );

        s.solve();
        System.out.println(s.isFeasible());


        LOGGER.info(""+p);

        p.showSolution(s);



    }



}
