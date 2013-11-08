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

package choco.visu.components.bricks;

import choco.kernel.solver.search.ISearchLoop;
import choco.kernel.solver.variables.Var;
import choco.visu.components.ColorConstant;
import choco.visu.components.papplets.AChocoPApplet;
import choco.visu.components.papplets.TreeSearchPApplet;
import choco.visu.searchloop.ObservableStepSearchLoop;
import choco.visu.searchloop.State;
import traer.physics.Particle;
/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 6 nov. 2008
 * Since : Choco 2.0.1
 *
 * {@code NodeBrick} is a {@code IChocoBrick} representing a node in a tree search.
 * To pretty print it, it is based on a {@code ParticleSystem} object (@see http://www.cs.princeton.edu/~traer/physics/}.
 *
 * Powered by Processing        (http://processing.org/),
 *            traer.physics     (http://www.cs.princeton.edu/~traer/physics/),
 *            traer.animation   (http://www.cs.princeton.edu/~traer/animation/) 
 * 
 */

public final class NodeBrick extends AChocoBrick {

    private int color;
    private String name;

    public NodeBrick(AChocoPApplet chopapplet, Var var) {
        super(chopapplet, var);
    }



    /**
     * Refresh data of the PApplet in order to refresh the visualization
     *
     * @param arg an object to precise the refreshing
     */
    public final void refresh(final Object arg) {
        //Condition
        if (arg instanceof ISearchLoop) {
            ObservableStepSearchLoop ossl = (ObservableStepSearchLoop)arg;
            State state = ossl.state;
            switch (state) {
                case SOLUTION:
                    color = ColorConstant.GREEN;
                    CParticle p = ((TreeSearchPApplet) chopapplet).q.getLast();
                    Object[] o = ((TreeSearchPApplet) chopapplet).settings.remove(p.particle);
                    ((TreeSearchPApplet) chopapplet).settings.put(p.particle, new Object[]{o[0], color});
                    break;
                case DOWN:
                    color = ColorConstant.BLUE;
                    name = var.getName()+" = "+getValues();
                    ((TreeSearchPApplet) chopapplet).tsdepth++;
                    ((TreeSearchPApplet) chopapplet).q.add(addNode());
                    break;
                case UP:
                    if (((TreeSearchPApplet) chopapplet).q.size() > 1) {
                        ((TreeSearchPApplet) chopapplet).q.removeLast();
                    }
                    if((((TreeSearchPApplet) chopapplet).q.size() == 1)){
                        ((TreeSearchPApplet) chopapplet).tsdepth = 0;
                    }
                    ((TreeSearchPApplet) chopapplet).tswidth++;

                    break;
                case END:
                    break;
                case RESTART:
                    while(((TreeSearchPApplet) chopapplet).q.size() > 1) {
                        ((TreeSearchPApplet) chopapplet).q.removeLast();
                    }
                    ((TreeSearchPApplet) chopapplet).tsdepth = 0;
                    ((TreeSearchPApplet) chopapplet).tswidth++;
                default:
                    break;
            }
        }
    }

    /**
     * Draw the graphic representation of the var associated to the brick
     */
    public final void drawBrick(final int x, final int y, final int widht, final int height) {
        //Not used
    }

    private CParticle addNode() {
        final CParticle p = new CParticle();
        p.particle = ((TreeSearchPApplet) chopapplet).physics.makeParticle();
        p.particle.setMass(1);
        p.name = var.getName();
        final CParticle q = ((TreeSearchPApplet) chopapplet).q.getLast();
        addSpacersToNode(p, q);
        makeEdgeBetween(p, q);
        p.particle.moveTo(q.particle.position().x() + ((TreeSearchPApplet) chopapplet).tswidth,
                q.particle.position().y() + ((TreeSearchPApplet) chopapplet).tsdepth, 0);
        ((TreeSearchPApplet) chopapplet).settings.put(p.particle, new Object[]{name, color});
        return p;
    }

    private void addSpacersToNode(final CParticle p, final CParticle r) {
        for (int i = 0; i < ((TreeSearchPApplet) chopapplet).physics.numberOfParticles(); ++i) {
            Particle q = ((TreeSearchPApplet) chopapplet).physics.getParticle(i);
            if (p.particle != q && p.particle != r.particle)
                ((TreeSearchPApplet) chopapplet).physics.makeAttraction(p.particle, q,
                        -TreeSearchPApplet.SPACER_STRENGTH, 20);
        }
    }

    private void makeEdgeBetween(final CParticle a, final CParticle b) {
        ((TreeSearchPApplet) chopapplet).physics.makeSpring(a.particle, b.particle, TreeSearchPApplet.EDGE_STRENGTH,
                TreeSearchPApplet.EDGE_STRENGTH, TreeSearchPApplet.EDGE_LENGTH);
    }

    public static CParticle createParticle(){
        return new CParticle();
    }


    public static final class CParticle {
        public Particle particle;
        public String name;
    }

}
