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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import parser.instances.ResolutionStatus;
import parser.instances.checker.IStatusChecker;
import parser.instances.checker.SCheckFactory;
import choco.kernel.solver.search.checker.SolutionCheckerException;

public class StatusTest {

	

	@BeforeClass
	public static void initalize() {
		SCheckFactory.PROPERTIES.setProperty("csp1", "true");
		SCheckFactory.PROPERTIES.setProperty("csp2", "False");
		SCheckFactory.PROPERTIES.setProperty("optim1", "10");
		SCheckFactory.PROPERTIES.setProperty("optim2", "10:20");
	}
	
	@AfterClass
	public static void teardDown() {
		SCheckFactory.PROPERTIES.clear();
	}
	
	@Test
	public void testGoodStatus() throws SolutionCheckerException {
		IStatusChecker scheck = SCheckFactory.makeStatusChecker("csp1");
		scheck.checkStatus(null, ResolutionStatus.SAT,null);
		scheck.checkStatus(null, ResolutionStatus.UNKNOWN,null);
		scheck.checkStatus(null, ResolutionStatus.TIMEOUT,null);
		scheck = SCheckFactory.makeStatusChecker("csp2");
		scheck.checkStatus(null, ResolutionStatus.UNSAT,null);
		scheck = SCheckFactory.makeStatusChecker("optim1");
		scheck.checkStatus(Boolean.TRUE, ResolutionStatus.SAT, Integer.valueOf(8));
		scheck.checkStatus(Boolean.TRUE, ResolutionStatus.OPTIMUM, Integer.valueOf(10));
		scheck = SCheckFactory.makeStatusChecker("optim2");
		scheck.checkStatus(Boolean.FALSE, ResolutionStatus.SAT, Integer.valueOf(12));
		scheck.checkStatus(Boolean.FALSE, ResolutionStatus.OPTIMUM, Integer.valueOf(10));

	}
	

	@Test(expected = SolutionCheckerException.class)
	public void testBadStatus1() throws SolutionCheckerException {
		SCheckFactory.makeStatusChecker("csp1").checkStatus(null, ResolutionStatus.UNSAT,null);
	}
	
	@Test(expected = SolutionCheckerException.class)
	public void testBadStatus2() throws SolutionCheckerException {
		SCheckFactory.makeStatusChecker("csp2").checkStatus(null, ResolutionStatus.SAT,null);
	}
	
	@Test(expected = SolutionCheckerException.class)
	public void testBadStatus3() throws SolutionCheckerException {
		SCheckFactory.makeStatusChecker("optim1").checkStatus(Boolean.TRUE, ResolutionStatus.UNSAT,null);
	}
	
	@Test(expected = SolutionCheckerException.class)
	public void testBadStatus4() throws SolutionCheckerException {
		SCheckFactory.makeStatusChecker("optim1").checkStatus(Boolean.TRUE, ResolutionStatus.SAT,Integer.valueOf(11));
	}
	
	@Test(expected = SolutionCheckerException.class)
	public void testBadStatus5() throws SolutionCheckerException {
		SCheckFactory.makeStatusChecker("optim2").checkStatus(Boolean.FALSE, ResolutionStatus.SAT,Integer.valueOf(9));
	}
}
