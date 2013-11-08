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

package choco.visu.components.panels;

import choco.kernel.model.variables.Variable;
import choco.kernel.visu.components.IVisuVariable;
import choco.kernel.visu.components.panels.AVarChocoPanel;
import choco.visu.components.papplets.AChocoPApplet;
import choco.visu.components.papplets.ChocoPApplet;

import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
/* 
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 31 oct. 2008
 * Since : Choco 2.0.1
 *
 * {@code VarChocoPanel} is a specific {@code AVarChocoPanel} to add visualization of variables.
 */

public final class VarChocoPanel extends AVarChocoPanel {

    private final AChocoPApplet chopapplet;

    public VarChocoPanel(final String name, final Variable[] variables, final ChocoPApplet chocopapplet, final Object parameters) {
        this(name, variables, chocopapplet.path, parameters);
    }

    private VarChocoPanel(final String name, final Variable[] variables, final String path, Object parameters) {
        super(name, variables);
        //We get it by reflection !
        AChocoPApplet tmp = null;
        Class componentClass = null;
        try {
            componentClass = Class.forName(path);
        } catch (ClassNotFoundException e) {
            LOGGER.severe("Component class could not be found: " + path);
            System.exit(-1);
        }
        try {
            Constructor constructeur =
                                 componentClass.getConstructor (new Class [] {Class.forName ("java.lang.Object")});
            tmp = (AChocoPApplet)constructeur.newInstance (parameters);
        } catch (InstantiationException e) {
            LOGGER.severe("Component class could not be instantiated: " + path);
            System.exit(-1);
        } catch (IllegalAccessException e) {
            LOGGER.severe("Component class could not be accessed: " + path);
            System.exit(-1);
        } catch (InvocationTargetException e) {
            LOGGER.severe("Component class could not be invocated: " + path);
            System.exit(-1);
        } catch (ClassNotFoundException e) {
            LOGGER.severe("Component class could not be found: " + path);
            System.exit(-1);
        } catch (NoSuchMethodException e) {
            LOGGER.severe("Component class could not be get correct constructor: " + path);
            System.exit(-1);
        }
        this.setLayout(new BorderLayout());
        this.chopapplet = tmp;
        this.add(this.chopapplet);
    }

    public VarChocoPanel(final String name, final Variable[] variables, final Class classname, Object parameters) {
        super(name, variables);
        //We get it by reflection !
        AChocoPApplet tmp = null;
        try {
            Constructor constructeur =
                                 classname.getConstructor (new Class [] {Class.forName ("java.lang.Object")});
            tmp = (AChocoPApplet)constructeur.newInstance (parameters);
        } catch (InstantiationException e) {
            LOGGER.severe("Component class could not be instantiated: " + classname);
            System.exit(-1);
        } catch (IllegalAccessException e) {
            LOGGER.severe("Component class could not be accessed: " + classname);
            System.exit(-1);
        } catch (InvocationTargetException e) {
            LOGGER.severe("Component class could not be invocated: " + classname);
            System.exit(-1);
        } catch (ClassNotFoundException e) {
            LOGGER.severe("Component class could not be found: " + classname);
            System.exit(-1);
        } catch (NoSuchMethodException e) {
            LOGGER.severe("Component class could not be get correct constructor: " + classname);
            System.exit(-1);
        }
        this.setLayout(new BorderLayout());
        this.chopapplet = tmp;
        this.add(this.chopapplet);
    }

    /**
     * Initialize every object of the frame.
     *
     * @param list list of visuvariables
     */
   public final void init(final ArrayList<IVisuVariable> list) {
        chopapplet.initialize(list);
    }

    /**
     * Return the dimensions of the Panel
     *
     * @return a {@code Dimension}
     */
    public final Dimension getDimensions() {
        return chopapplet.getDimension();
    }
}
