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

package choco.visu.papplet;

import choco.kernel.solver.variables.Var;
import choco.kernel.visu.components.IVisuVariable;
import choco.visu.brick.StateColorBrick;
import static choco.visu.components.ColorConstant.WHITE;
import choco.visu.components.bricks.AChocoBrick;
import choco.visu.components.papplets.AChocoPApplet;
import processing.core.PShape;

import java.awt.*;
import java.util.ArrayList;
/* 
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 3 nov. 2008
 */

public class ColoringPApplet extends AChocoPApplet{

    public PShape card;
    private String cardname;

    public ColoringPApplet(final Object parameters) {
        super(parameters);
        cardname = (String)parameters;
    }

    /**
     * Initialize the ChocoPApplet with the list of concerning VisuVariables
     *
     * @param list of visu variables o watch
     */
    public void initialize(ArrayList<IVisuVariable> list) {
        Var[] vars = new Var[list.size()];
        for(int i = 0; i < list.size(); i++){
            vars[i] = list.get(i).getSolverVar();
        }
        bricks = new AChocoBrick[list.size()];
        for(int i = 0; i < list.size(); i++){
            IVisuVariable vv = list.get(i);
            Var v = vv.getSolverVar();
            bricks[i] = new StateColorBrick(this, v);
            vv.addBrick(bricks[i]);
        }
        this.init();
    }

    /**
     * build the specific PApplet.
     * This method is called inside the {@code PApplet#setup()} method.
     */
    public void build() {
        //background(LIGHTBLUE);
        size(800, 1000);
        card = loadShape(cardname);
        card.disableStyle();
        smooth();  // Improves the drawing quality of the SVG
    }

    /**
     * draws the back side of the representation.
     * This method is called inside the {@code PApplet#draw()} method.
     * For exemple, the sudoku grid is considered as a back side
     */
    public void drawBackSide() {
        background(WHITE);
        shape(card, 0, 0);
    }

    /**
     * draws the front side of the representation.
     * This method is called inside the {@code PApplet#draw()} method.
     * For exemple, values of cells in a sudoku are considered as a back side
     */
    public void drawFrontSide() {
        for(int x = 0; x < bricks.length; x++){
            bricks[x].drawBrick(0,0,0,0);
        }
    }

    /**
     * Return the ideal dimension of the chopapplet
     *
     * @return
     */
    public Dimension getDimension() {
        return new Dimension(1300, 800);
    }
}
