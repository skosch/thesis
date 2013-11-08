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
import parser.chocogen.XmlModel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.logging.Logger;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 12 févr. 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*/
public class Performance {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    Properties properties = new Properties();
    String[] args = new String[24];

    public void before() throws IOException, URISyntaxException {
        InputStream is = getClass().getResourceAsStream("/perf.properties");
        properties.load(is);
        String f = getClass().getResource("/csp").toURI().getPath();
        args[0] = "-file";
        args[1] = f;
        args[2] = "-h";
        args[3] = properties.getProperty("perf.h");
        args[4] = "-ac";
        args[5] = properties.getProperty("perf.ac");
        args[6] = "-s";
        args[7] = properties.getProperty("perf.s");
        args[8] = "-verb";
        args[9] = properties.getProperty("perf.verb");
        args[10] = "-time";
        args[11] = properties.getProperty("perf.time");
        args[12] = "-randval";
        args[13] = properties.getProperty("perf.randval");
        args[14] = "-rest";
        args[15] = properties.getProperty("perf.rest");
        args[16] = "-rb";
        args[17] = properties.getProperty("perf.rb");
        args[18] = "-rg";
        args[19] = properties.getProperty("perf.rg");
        args[20] = "-saclim";
        args[21] = properties.getProperty("perf.saclim");
        args[22] = "-seed";
        args[23] = properties.getProperty("perf.seed");
    }


    private void execute() throws IOException, URISyntaxException {
        before();
        String directory = args[1];
        int nbpb=Integer.parseInt((String) properties.get("pb.nbpb"));
        for(int i = 1; i < nbpb+1; i++){
            args[1] = directory + "/"+ properties.get("pb."+i+".name")+".xml";

            XmlModel xm = new XmlModel();
            try {
                xm.generate(args);
            } catch (Exception e) {
                LOGGER.severe(e.toString());
            }
        }
    }

    public static void main(String[] args) {
        try {
            new Performance().execute();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


}