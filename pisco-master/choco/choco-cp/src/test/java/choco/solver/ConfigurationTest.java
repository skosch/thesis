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

package choco.solver;

import static choco.kernel.solver.Configuration.STOP_AT_FIRST_SOLUTION;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.Configuration;
import choco.kernel.solver.search.limit.Limit;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 22 avr. 2010br/>
 * Since : Choco 2.1.1<br/>
 */
public class ConfigurationTest {

    private static final String USER_KEY = "user.key";

    private Configuration configuration;


    @Before
    public void load() {
        configuration = new Configuration();
    }


    @Test
    public void test1() {
        configuration.putBoolean(USER_KEY, true);
        Boolean mykey = configuration.readBoolean(USER_KEY);
        Assert.assertTrue(mykey);
        Boolean safs = configuration.readBoolean(STOP_AT_FIRST_SOLUTION);
        Assert.assertTrue(safs);
    }

    @Test(expected = NullPointerException.class)
    public void testNoInt() {
        configuration.readInt(USER_KEY);
    }

    @Test
    public void testInt() {
        configuration.putInt(USER_KEY, 99);
        int value = configuration.readInt(USER_KEY);
        Assert.assertEquals(99, value);
        configuration.putInt(USER_KEY, 9);
        value = configuration.readInt(USER_KEY);
        Assert.assertEquals(9, value);
    }

    @Test(expected = NullPointerException.class)
    public void testNoBoolean() {
        configuration.readBoolean(USER_KEY);
    }

    @Test
    public void testBoolean() {
        configuration.putBoolean(USER_KEY, true);
        boolean value = configuration.readBoolean(USER_KEY);
        Assert.assertEquals(true, value);
        configuration.putBoolean(USER_KEY, false);
        value = configuration.readBoolean(USER_KEY);
        Assert.assertEquals(false, value);
    }

    @Test(expected = NullPointerException.class)
    public void testNoDouble() {
        configuration.readDouble(USER_KEY);
    }

    @Test
    public void testDouble() {
        configuration.putDouble(USER_KEY, 9.99);
        double value = configuration.readDouble(USER_KEY);
        Assert.assertEquals(9.99, value, 0.01);
        configuration.putDouble(USER_KEY, 1.e-9);
        value = configuration.readDouble(USER_KEY);
        Assert.assertEquals(1.e-9, value, 0.01);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    @Test(expected = NullPointerException.class)
    public void testNoEnum() {
        Limit lim = configuration.readEnum(USER_KEY, Limit.class);
    }

    @Test
    public void testEnum() {
        configuration.putEnum(USER_KEY, Limit.TIME);
        Limit value = configuration.readEnum(USER_KEY, Limit.class);
        Assert.assertEquals(Limit.TIME, value);
        configuration.putEnum(USER_KEY, Limit.UNDEF);
        value= configuration.readEnum(USER_KEY, Limit.class);
        Assert.assertEquals(Limit.UNDEF, value);
    }

    public void test2() {
        configuration.clear();
        configuration.putBoolean(USER_KEY, true);
        Boolean mykey = configuration.readBoolean(USER_KEY);
        Assert.assertTrue(mykey);
        Boolean safs = configuration.readBoolean(STOP_AT_FIRST_SOLUTION);
        Assert.assertTrue(safs);
    }

    @Test
    public void test3() throws IOException {
        FileOutputStream fos = new FileOutputStream(File.createTempFile("CONF_", ".properties"));
        configuration.store(fos, ChocoLogging.START_MESSAGE);
    }

    @Test
    public void test4() {
        Properties properties = new Properties();
        try {
            final InputStream is = getClass().getResourceAsStream("/conf1.properties");
            properties.load(is);
        } catch (IOException e) {
            Assert.fail();
        }
        configuration = new Configuration(properties);
        configuration.putBoolean(USER_KEY, true);
        Boolean mykey = configuration.readBoolean(USER_KEY);
        Assert.assertTrue(mykey);
        Boolean safs = configuration.readBoolean(Configuration.STOP_AT_FIRST_SOLUTION);
        Assert.assertTrue(safs);
    }

    @Test
    public void test5() {
        Properties empty = new Properties();
        configuration = new Configuration(empty);
        configuration.putBoolean(USER_KEY, true);
        Boolean mykey = configuration.readBoolean(USER_KEY);
        Assert.assertTrue(mykey);
        Boolean safs = configuration.readBoolean(STOP_AT_FIRST_SOLUTION);
        Assert.assertTrue(safs);
    }

    @Test
    public void test6() {
    	configuration = new Configuration(configuration);
    	Assert.assertTrue(configuration.readBoolean(STOP_AT_FIRST_SOLUTION));
    	configuration.putFalse(STOP_AT_FIRST_SOLUTION);
    	Assert.assertFalse(configuration.readBoolean(STOP_AT_FIRST_SOLUTION));
    	configuration.remove(STOP_AT_FIRST_SOLUTION);
    	Assert.assertTrue(configuration.readBoolean(STOP_AT_FIRST_SOLUTION));
//        Boolean mykey = configuration.readBoolean(USER_KEY);
//        Assert.assertTrue(mykey);
//        Boolean safs = configuration.readBoolean(Configuration.STOP_AT_FIRST_SOLUTION);
//        Assert.assertTrue(safs);
    }

}
