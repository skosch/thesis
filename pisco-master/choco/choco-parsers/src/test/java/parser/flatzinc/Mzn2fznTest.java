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

package parser.flatzinc;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 15 janv. 2010
* Since : Choco 2.1.1
* 
*/
public class Mzn2fznTest {

    @Test
    public void testNoArgs() throws IOException, InterruptedException, URISyntaxException {
        Mzn2fzn.main(new String[]{""});
    }

    @Test
    public void testNoData() throws IOException, InterruptedException, URISyntaxException {
        String[] args = new String[]{
                "--mzn-dir", "/home/charles/Bureau/minizinc-rotd-2009-11-02",
                "-m", "/home/charles/Bureau/minizinc-rotd-2009-11-02/benchmarks/alpha/alpha.mzn",
                "-o", "/tmp/alpha.fzn"
        };
        Mzn2fzn.main(args);
    }

    @Test
    public void testData() throws IOException, InterruptedException, URISyntaxException {
        String[] args = new String[]{
                "--mzn-dir", "/Users/cprudhom/Documents/Projects/_Librairies/minizinc-1.2.2",
                "-lib", "/home/charles/Choco/sources/choco/trunk/choco-tools/choco-parsers/src/main/resources/std_lib",
                "-m", "/home/charles/Bureau/minizinc-rotd-2009-11-02/benchmarks/queens/queens.mzn",
                "-d", "/home/charles/Bureau/minizinc-rotd-2009-11-02/benchmarks/queens/004.dzn",
                "-o", "/tmp/queens_004.fzn"
        };
        Mzn2fzn.main(args);
    }

}
