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

package choco.model;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.logging.Logger;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 30 mars 2010br/>
 * Since : Choco 2.1.1<br/>
 */
public class SerializableTest {

    private static final Logger LOGGER = ChocoLogging.getTestLogger();

    private static File create() throws IOException {
        return File.createTempFile("MODEL", ".ser");
    }


    private static File write(final Object o) throws IOException {
        final File file = create();
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        fos = new FileOutputStream(file);
        out = new ObjectOutputStream(fos);
        out.writeObject(o);
        out.close();
        return file;
    }


    private static Object read(final File file) throws IOException, ClassNotFoundException {
        FileInputStream fis = null;
        ObjectInputStream in = null;
        fis = new FileInputStream(file);
        in = new ObjectInputStream(fis);
        final Object o = in.readObject();
        in.close();
        return o;
    }


    @Test
    public void testEmptyModel(){
        Model m = new CPModel();
        File file = null;
        try {
            file = write(m);
        } catch (IOException e) {
            e.printStackTrace();
        }
        m = null;
        Assert.assertNull(m);
        try {
            m = (Model)read(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(m);
        LOGGER.info(m.pretty());
    }

    @Test
    public void testIntegerVariable(){
        IntegerVariable var = Choco.makeIntVar("var", 1 , 10);
        File file = null;
        try {
            file = write(var);
        } catch (IOException e) {
            e.printStackTrace();
        }
        var = null;
        try {
            var = (IntegerVariable)read(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(var);
        LOGGER.info(var.pretty());
    }

    @Test
    public void testConstraint(){
        final IntegerVariable var = Choco.makeIntVar("var", 1 , 10);
        Constraint cstr = Choco.eq(var, 5);
        File file = null;
        try {
            file = write(cstr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        cstr = null;
        Assert.assertNull(cstr);
        try {
            cstr = (Constraint)read(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(cstr);
        LOGGER.info(cstr.pretty());
    }

}
