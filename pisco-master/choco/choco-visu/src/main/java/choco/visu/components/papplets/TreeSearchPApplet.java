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

import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.visu.components.IVisuVariable;
import static choco.visu.components.ColorConstant.WHITE;
import choco.visu.components.bricks.AChocoBrick;
import choco.visu.components.bricks.NodeBrick;
import processing.core.PApplet;
import processing.core.PFont;
import traer.animation.Smoother3D;
import traer.physics.Particle;
import traer.physics.ParticleSystem;
import traer.physics.Spring;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 6 nov. 2008
 * Since : Choco 2.0.1
 *
 * {@code TreeSearchPApplet} is the {@code AChocoPApplet} that represents the tree search in a dynamic way.
 * Every time a new node is created in the search loop, its equivalent is created in the {@code ParticleSystem}.
 *
 * Powered by Processing        (http://processing.org/),
 *            traer.physics     (http://www.cs.princeton.edu/~traer/physics/),
 *            traer.animation   (http://www.cs.princeton.edu/~traer/animation/)
 */

public final class TreeSearchPApplet extends AChocoPApplet {

    private static final float NODE_SIZE = 10;
    public static final float EDGE_LENGTH = 10;
    public static final float EDGE_STRENGTH = (float) 0.2;
    public static final float SPACER_STRENGTH = 1000;

    public ParticleSystem physics;
    private Smoother3D centroid;
    public LinkedList<NodeBrick.CParticle> q;
    public HashMap<Particle, Object[]> settings;
    public int tswidth = 0, tsdepth = 0;
    private float sval= (float)1.0;


    public TreeSearchPApplet(final Object parameters) {
        super(parameters);
        if(parameters == null){
            try{
            this.font = loadFont("./fonts/FreeSans-48.vlw") ;
        }catch(Exception e){
            LOGGER.warning("\"FreeSans-48.vlw\" not found. Use default one instead (can be slower)");
            this.font = getFont(48);
        }
        }else{
            font = (PFont)parameters;
        }
    }

    /**
     * Initialize the ChocoPApplet with the list of concerning VisuVariables
     *
     * @param list of visu variables o watch
     */
    public final void initialize(final ArrayList<IVisuVariable> list) {
        final IVisuVariable[] vars = ArrayUtils.getNonRedundantObjects(IVisuVariable.class,
                list.toArray(new IVisuVariable[list.size()]));
        bricks = new AChocoBrick[list.size()];
        int i =0;
        for (IVisuVariable var : vars){
            bricks[i] = new NodeBrick(this, var.getSolverVar());
            var.addBrick(bricks[i++]);
        }
        settings = new HashMap<Particle, Object[]>(16);
        this.init();
    }

    /**
     * Return the ideal dimension of the chopapplet
     *
     * @return
     */
    public final Dimension getDimension() {
        return new Dimension(800, 900);
    }

    /**
     * build the specific PApplet.
     * This method is called inside the {@code PApplet#setup()} method.
     */
    public final void build() {
        size(800, 800);
        smooth();
        strokeWeight(2);
        ellipseMode(CENTER);

        physics = new ParticleSystem((float)0.5, (float) 0.25);
        centroid = new Smoother3D((float) 0.8);

        textFont(font, 4);

        initialize();
    }

    /**
     * draws the back side of the representation.
     * This method is called inside the {@code PApplet#draw()} method.
     * For exemple, the sudoku grid is considered as a back side
     */
    public final void drawBackSide() {

        if(this.mousePressed){
            sval += 0.05;
          }
          else {
            sval -= 0.1;
          }
        sval = PApplet.constrain(sval, (float)1.0, (float)2.5);

        physics.tick((float) 1.0);
        if (physics.numberOfParticles() > 1)
            updateCentroid();
        centroid.tick();

        background(WHITE);
        if(mousePressed){
            translate(mouseX, mouseY);
        }else{
            translate(width/2, height/2);
        }
        scale(sval * centroid.z());
        translate(-centroid.x(), -centroid.y());

    }

    /**
     * draws the front side of the representation.
     * This method is called inside the {@code PApplet#draw()} method.
     * For exemple, values of cells in a sudoku are considered as a back side
     */
    public final void drawFrontSide() {
        // draw edges
        stroke(200);
        beginShape(LINES);
        for (int i = 0; i < physics.numberOfSprings(); ++i) {
            Spring e = physics.getSpring(i);
            Particle a = e.getOneEnd();
            Particle b = e.getTheOtherEnd();
            vertex(a.position().x(), a.position().y());
            vertex(b.position().x(), b.position().y());
        }
        endShape();
        // draw vertices
        fill(160);
        noStroke();
        for (int i = 0; i < physics.numberOfParticles(); ++i) {
            Particle v = physics.getParticle(i);
            fill((Integer) settings.get(v)[1]);
            ellipse(v.position().x(), v.position().y(), NODE_SIZE, NODE_SIZE);
            fill(0);
            text((String) settings.get(v)[0], v.position().x(), v.position().y());
            fill(160);
        }
    }


    final void initialize() {
        q = new LinkedList<NodeBrick.CParticle>();
        physics.clear();
        NodeBrick.CParticle lastNode = NodeBrick.createParticle();
        lastNode.particle = physics.makeParticle();
        lastNode.particle.makeFixed();
        q.add(lastNode);
        settings.put(lastNode.particle, new Object[]{"ROOT", 120});
        centroid.setValue(0, 0, (float) 1.0);
    }


    final void updateCentroid() {
        float
                xMax = Float.NEGATIVE_INFINITY,
                xMin = Float.POSITIVE_INFINITY,
                yMin = Float.POSITIVE_INFINITY,
                yMax = Float.NEGATIVE_INFINITY;

        for (int i = 0; i < physics.numberOfParticles(); ++i) {
            Particle p = physics.getParticle(i);
            xMax = max(xMax, p.position().x());
            xMin = min(xMin, p.position().x());
            yMin = min(yMin, p.position().y());
            yMax = max(yMax, p.position().y());
        }
        float deltaX = xMax - xMin;
        float deltaY = yMax - yMin;
        if (deltaY > deltaX)
            centroid.setTarget((float) (xMin + 0.5 * deltaX), (float) (yMin + 0.5 * deltaY), height / (deltaY + 50));
        else
            centroid.setTarget((float) (xMin + 0.5 * deltaX), (float) (yMin + 0.5 * deltaY), width / (deltaX + 50));
    }

}



