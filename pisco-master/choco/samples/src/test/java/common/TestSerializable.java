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

package common;

import choco.cp.model.CPModel;
import choco.kernel.common.logging.ChocoLogging;
import org.junit.Assert;
import org.junit.Test;
import samples.tutorials.continuous.CycloHexan;
import samples.tutorials.puzzles.Queen;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 26 avr. 2010<br/>
 * Since : Choco 2.1.1<br/>
 */
public class TestSerializable {

    private static final Logger LOGGER = ChocoLogging.getTestLogger();

    private static File create() throws IOException {
        return File.createTempFile("MODEL", ".ser");
    }

     @Test
    public void testQueen() throws IOException {
        final Queen pb = new Queen();
        pb.buildModel();
        final File file = create();
        try {
            CPModel.writeInFile((CPModel)pb.model, file);
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            Assert.fail("IOException");
        }
        pb.model = null;
        Assert.assertNull(pb.model);
        try {
            pb.model = CPModel.readFromFile(file);
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            Assert.fail("IOException");
        } catch (ClassNotFoundException e) {
            LOGGER.severe(e.getMessage());
            Assert.fail("ClassNotFoundException");
        }
        pb.buildSolver();
        pb.solve();
        Assert.assertEquals(92, pb.solver.getSolutionCount());
    }


    @Test
    public void testCycloHexan() throws IOException {
        final CycloHexan pb = new CycloHexan();
        pb.buildModel();
        File file = null;
        try {
            file = CPModel.writeInFile((CPModel)pb.model);
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            Assert.fail("IOException");
        }
        pb.model = null;
        Assert.assertNull(pb.model);
        try {
            pb.model = CPModel.readFromFile(file);
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            Assert.fail("IOException");
        } catch (ClassNotFoundException e) {
            LOGGER.severe(e.getMessage());
            Assert.fail("ClassNotFoundException");
        }
        pb.buildSolver();
        pb.solve();
        Assert.assertEquals(69, pb.solver.getSolutionCount());
    }
}
