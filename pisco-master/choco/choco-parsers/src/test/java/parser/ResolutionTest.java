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

package parser;

import choco.kernel.common.logging.ChocoLogging;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import parser.absconparseur.tools.SolutionChecker;
import parser.chocogen.XmlModel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.Permission;
import java.util.Properties;
import java.util.logging.Logger;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 12 févr. 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*/
public class ResolutionTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    Properties properties = new Properties();
    String[] args = new String[24];

    @Before
    public void before() throws IOException, URISyntaxException {
        InputStream is = getClass().getResourceAsStream("/csp.properties");
        properties.load(is);
        String f = getClass().getResource("/csp").toURI().getPath();
        args[0] = "-file";
        args[1] = f;
        args[2] = "-h";
        args[3] = properties.getProperty("csp.h");
        args[4] = "-ac";
        args[5] = properties.getProperty("csp.ac");
        args[6] = "-s";
        args[7] = properties.getProperty("csp.s");
        args[8] = "-verb";
        args[9] = properties.getProperty("csp.verb");
        args[10] = "-time";
        args[11] = properties.getProperty("csp.time");
        args[12] = "-randval";
        args[13] = properties.getProperty("csp.randval");
        args[14] = "-rest";
        args[15] = properties.getProperty("csp.rest");
        args[16] = "-rb";
        args[17] = properties.getProperty("csp.rb");
        args[18] = "-rg";
        args[19] = properties.getProperty("csp.rg");
        args[20] = "-saclim";
        args[21] = properties.getProperty("csp.saclim");
        args[22] = "-seed";
        args[23] = properties.getProperty("csp.seed");
    }


    public void run(int i) {
        String directory = args[1];
        String name = (String)properties.get(String.format("pb.%d.name", i));
        LOGGER.info("Solve "+name);
        args[1] = String.format("%s/%s.xml", directory,name);
        XmlModel xm = new XmlModel();
        try {
            xm.generate(args);
        } catch (Exception e) {
            LOGGER.severe(e.toString());
            Assert.fail();
        }
        System.setSecurityManager(new NoExitSecurityManager());
        try {
            if (xm.isFeasible() == Boolean.TRUE)
                SolutionChecker.main(XmlModel.getValues());
        } catch (ExitException e) {
            LOGGER.severe(e.toString());
            Assert.fail();
        } finally {
            System.setSecurityManager(null);
        }
        int time = Integer.valueOf((String) properties.get(String.format("pb.%d.buildtime", i)));
        LOGGER.info(String.format("%d > %d? for pb.%d", XmlModel.getBuildTime(), time, i));
        Assert.assertTrue(String.format("%s: too much time spending in reading problem... (excepted: %dms, actual: %dms)",
                properties.get(String.format("pb.%d.name", i)), time, XmlModel.getBuildTime()), XmlModel.getBuildTime() < time);

        time = Integer.valueOf((String) properties.get(String.format("pb.%d.conftime", i)));
        LOGGER.info(String.format("%d > %d? for pb.%d", XmlModel.getConfTime(), time, i));
        Assert.assertTrue(String.format("%s: too much time spending in preprocessing problem...(excepted :%dms, actual : %dms)",
                properties.get(String.format("pb.%d.name", i)), time, XmlModel.getConfTime()), XmlModel.getConfTime() < time);
    }

    @Test
    public void test1() {
        run(1);
    }

    @Test
    public void test2() {
        run(2);
    }
    @Test
    public void test3() {
        run(3);
    }
    @Test
    public void test4() {
        run(4);
    }
    @Test
    public void test5() {
        run(5);
    }
    @Test
    public void test6() {
        run(6);
    }
    @Test
    public void test7() {
        run(7);
    }
    @Test
    public void test8() {
        run(8);
    }
    @Test
    public void test9() {
        run(9);
    }
    @Test
    public void test10() {
        run(10);
    }
    @Test
    public void test11() {
        run(11);
    }
    @Test
    public void test12() {
        run(12);
    }
    @Test
    public void test13() {
        run(13);
    }
    @Test
    public void test14() {
        run(14);
    }
    @Test
    public void test15() {
        run(15);
    }
    @Test
    public void test16() {
        run(16);
    }
    @Test
    public void test17() {
        run(17);
    }
    @Test
    public void test18() {
        run(18);
    }
    @Test
    public void test19() {
        run(19);
    }
    @Test
    public void test20() {
        run(20);
    }
    @Test
    public void test21() {
        run(21);
    }

    @Test
    public void bibdTest() {
        XmlModel xm = new XmlModel();
        args[1] = args[1] + "/bibd-8-14-7-4-3_glb.xml";
        int nbNodes = -1;
        for (int i = 0; i < 5; i++) {
            try {
                xm.generate(args);
            } catch (Exception e) {
                LOGGER.severe(e.toString());
                Assert.fail();
            }
            if (nbNodes == -1) {
                nbNodes = xm.getNbNodes();
            } else {
                Assert.assertEquals("not same number of nodes", nbNodes, xm.getNbNodes());
            }
        }
    }

    @Test
    public void aTest() {
        XmlModel xm = new XmlModel();
        args[1] = args[1] + "/normalized-ssa-0432-003_ext.xml";
//        ChocoLogging.setVerbosity(Verbosity.VERBOSE);
        try {
            xm.generate(args);
        } catch (Exception e) {
            LOGGER.severe(e.toString());
            Assert.fail();
        }
    }

    @Test
    public void gccTest() {
        XmlModel xm = new XmlModel();
        args[1] = args[1] + "/gcc2.xml";
        try {
            xm.generate(args);
        } catch (Exception e) {
            LOGGER.severe(e.toString());
            Assert.fail();
        }
    }

    @Test
    public void patatTest() {
        XmlModel xm = new XmlModel();
        args[1] = args[1] + "/normalized-patat-02-small-2.xml";
        try {
            xm.generate(args);
        } catch (Exception e) {
            LOGGER.severe(e.toString());
            Assert.fail();
        }
    }


    //***************************************************************************************
/**
 * An exception thrown when an System.exit() occurred.
 *
 * @author Fabien Hermenier
 */
static class ExitException extends SecurityException {

        /**
         * The exit status.
         */
        private final int status;

        /**
         * A new exception.
         *
         * @param st the exit status
         */
        public ExitException(int st) {
            super("There is no escape");
            this.status = st;
        }

        /**
         * Return the error message.
         *
         * @return a String!
         */
        public String getMessage() {
            return "Application execute a 'System.exit(" + this.status + ")'";
        }
    }

    /**
     * A Mock security manager to "transform" a System.exit() into
     * a ExitException.
     *
     * @author Fabien Hermenier
     */
    static class NoExitSecurityManager extends SecurityManager {

        /**
         * {@inheritDoc}
         */
        @Override
        public void checkPermission(Permission perm, Object ctx) {

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void checkPermission(Permission perm) {

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void checkExit(int st) {
            super.checkExit(st);
            throw new ExitException(st);
		}
	}
    

}
