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

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.common.util.tools.ArrayUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import samples.tutorials.to_sort.packing.parser.BinPackingCmd;

public class TestBinPacking {

	private final BinPackingCmd cmd = new BinPackingCmd();

	private final static String PATH = "./src/main/resources/bin-packing-tut/instances/";

	private final static String[] CMD_PREFIX = {"--seed","0"};


	@BeforeClass
	public final static void setUp() {
		ChocoLogging.setVerbosity(Verbosity.QUIET);
	}

	@AfterClass
	public final static void tearDown() {
		ChocoLogging.setVerbosity(Verbosity.SILENT);
	}


	private void testInstances(String... args) {
		cmd.doMain(ArrayUtils.append(new String[] {
				"--seed", String.valueOf(0),
				"-time", String.valueOf(15),
				"-f",PATH}, 
				args
		));
	}

	@Test
	public void testBinPacking1() {
		testInstances();
	}
	
	@Test
	public void testBinPacking2() {
		testInstances("--light");
	}
}
