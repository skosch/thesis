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

package choco.scheduling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import choco.cp.solver.search.task.profile.ProbabilisticProfile;
import choco.kernel.solver.constraints.global.scheduling.FakeResource;
import choco.kernel.solver.constraints.global.scheduling.ICumulativeResource;
import choco.kernel.solver.constraints.global.scheduling.IResource;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.IRTask;
import choco.kernel.solver.variables.scheduling.ITask;
import choco.kernel.visu.VisuFactory;

class SizedMaxProbabilisticProfile extends ProbabilisticProfile {

	public int minNbInvolved = 1;


	public SizedMaxProbabilisticProfile(List<? extends ITask> tasks) {
		super(tasks, null);
	}

	@Override
	protected boolean isValidMaximum() {
		return involved.cardinality() >= minNbInvolved;
	}


}

public class TestProbProfile {

	private final static double DELTA=0.01;

	private final static boolean DISPLAY=false;

	private IResource<SimpleTask> rsc;

	private ProbabilisticProfile profile;


	private void display() {
		if(DISPLAY) {
			VisuFactory.getGnuplotManager().show( profile.draw().toString());
		}
	}

	private void setTask(int idx,List<SimpleTask> l) {
		switch (idx) {
		case 0 : l.add(new SimpleTask(10,30,40));break;
		case 1 : l.add(new SimpleTask(15,70,20));break;
		case 2 : l.add(new SimpleTask(0,12,10));break;
		case 3 : l.add(new SimpleTask(0,0,15));break;
		default:
			System.err.println("error while creating profile");
			break;
		}
	}

	protected IResource<SimpleTask> createResource(List<SimpleTask> taskL) {
		return new FakeResource<SimpleTask>(taskL.toArray(new SimpleTask[taskL.size()]));
	}

	public void initialize(boolean[] tasks) {
		initialize(tasks, true);
	}

	public void initialize(boolean[] tasks, boolean probProf) {
		List<SimpleTask> l=new LinkedList<SimpleTask>();
		for (int i = 0; i < tasks.length; i++) {
			if(tasks[i]) {setTask(i, l);}
		}
		rsc= createResource(l);
		profile= probProf ? new ProbabilisticProfile(l, null) : new SizedMaxProbabilisticProfile(l);
		profile.initializeEvents();
		profile.generateEventsList(rsc);
	}


	@Test
	public void testProfileTask1() {
		boolean[] t={true,false,false};
		initialize(t);
		display();
		assertTrue("compute : ",profile.compute(10)>0);
		assertTrue("compute : ",profile.compute(30)==1);
		assertTrue("compute : ",profile.compute(40)==1);
		assertTrue("compute : ",profile.compute(51)<1);
		assertEquals("compute : ",0,profile.compute(70),DELTA);
	}

	@Test
	public void testProfileTask2() {
		boolean[] t={false,true,false};
		initialize(t);
		display();
		assertEquals("compute : ",((double) 1)/56,profile.compute(15),DELTA);
		assertEquals("compute : ",0.357,profile.compute(40),DELTA);
		assertEquals("compute : ",0.357,profile.compute(70),DELTA);
		assertEquals("compute : ",0,profile.compute(90),DELTA);
	}
	@Test
	public void testProfileTask3() {
		boolean[] t={false,false,true};
		initialize(t);
		display();
		//assertEquals("compute : ",0.769,profile.compute(11),DELTA);
		assertEquals("compute : ",( (double) 1)/13,profile.compute(0),DELTA);
		//assertEquals("compute : ",0,profile.compute(22),DELTA);
	}

	@Test
	public void testProfileTask4() {
		boolean[] t={false,false,false,true};
		initialize(t);
		display();
		assertEquals("compute : ",1,profile.compute(0),DELTA);
		assertEquals("compute : ",1,profile.compute(10),DELTA);
		assertEquals("compute : ",0,profile.compute(15),DELTA);
	}


	protected void testProfile(int c,double v) {
		assertEquals("prof. coord. max : ",c,profile.getMaxProfileCoord());
		assertEquals("prof. value max : ",v,profile.getMaxProfileValue(),DELTA);

	}

	protected void testProfile(boolean[] involved) {
		for (int i = 0; i < involved.length; i++) {
			assertEquals("prof. max involved "+i+" : ", involved[i],profile.getInvolvedInMaxProf().get(rsc.getTask(i).getID()));
		}
	}




	@Test
	public void testProfile() {
		boolean[] t={true,true,true,false};
		initialize(t, false);
		display();
		SimpleTask task = rsc.getTask(0);
		assertEquals("ind. contrib. : ",0,profile.getIndividualContribution(task, 9),DELTA);
		assertTrue("ind. contrib. : ",profile.getIndividualContribution(task, 10)>0);
		assertEquals("ind. contrib. : ",1,profile.getIndividualContribution(task, 30),DELTA);
		assertEquals("ind. contrib. : ",1,profile.getIndividualContribution(task, 40),DELTA);
		assertEquals("ind. contrib. : ",0,profile.getIndividualContribution(task, 70),DELTA);
		profile.computeMaximum(rsc);
		testProfile(35,1.35);
		testProfile(new boolean[]{true,true,false});
		((SizedMaxProbabilisticProfile) profile).minNbInvolved=2;
		profile.computeMaximum(rsc);
		testProfile(35,1.35);
		testProfile(new boolean[]{true,true,false});
	}

	@Test
	public void testProfile2() {
		List<SimpleTask> l=new LinkedList<SimpleTask>();
		l.add(new SimpleTask(0,0,10));
		l.add(new SimpleTask(10,20,12));
		l.add(new SimpleTask(28,38,15));
		rsc= createResource(l);
		profile=new SizedMaxProbabilisticProfile(l);
		profile.initializeEvents();
		profile.computeMaximum(rsc);
		testProfile(0,1);
		testProfile(new boolean[]{true,false,false});
		((SizedMaxProbabilisticProfile) profile).minNbInvolved=2;
		profile.computeMaximum(rsc);
		testProfile(28,0.54);
		testProfile(new boolean[]{false,true,true});
	}

}

class SimpleResource implements ICumulativeResource<SimpleTask> {

	private static final long serialVersionUID = 1L;

	public final List<SimpleTask> tasksL;

	public int[] heights;

	public int capacity;


	public SimpleResource(List<SimpleTask> tasksL, int[] heights, int capacity) {
		super();
		this.tasksL = tasksL;
		this.heights = heights;
		this.capacity = capacity;
	}

	public SimpleResource(List<SimpleTask> tasksL) {
		super();
		this.tasksL=new ArrayList<SimpleTask>(tasksL);
		this.capacity = 1;
		this.heights = new int[tasksL.size()];
		Arrays.fill(heights, 1);
	}


	@Override
	public IRTask getRTask(int idx) {
		return null;
	}

	@Override
	public int getNbTasks() {
		return tasksL.size();
	}

	@Override
	public String getRscName() {
		return "internal resource (test)";
	}

	@Override
	public SimpleTask getTask(int idx) {
		return tasksL.get(idx);
	}

	@Override
	public Iterator<SimpleTask> getTaskIterator() {
		return tasksL.listIterator();
	}

	@Override
	public List<SimpleTask> asTaskList() {
		return Collections.unmodifiableList(tasksL);
	}

	@Override
	public Iterator<IRTask> getRTaskIterator() {
		return asRTaskList().iterator();
	}

	@Override
	public IntDomainVar getCapacity() {
		return null;
	}
	@Override

	public int getMaxCapacity() {
		return capacity;
	}

	@Override
	public int getMinCapacity() {
		return getCapacity().getInf();
	}

	public IntDomainVar getHeight(int idx) {
		return null;
	}


	@Override
	public IntDomainVar getConsumption() {
		return null;
	}

	@Override
	public int getMaxConsumption() {
		return 0;
	}

	@Override
	public int getMinConsumption() {
		return 0;
	}

	@Override
	public boolean isInstantiatedHeights() {
		return true;
	}

	@Override
	public boolean hasOnlyPosisiveHeights() {
		return true;
	}

	@Override
	public int getNbOptionalTasks() {
		return 0;
	}

	@Override
	public int getNbRegularTasks() {
		return getNbTasks();
	}

	@Override
	public List<IRTask> asRTaskList() {
		return Collections.<IRTask>emptyList();
	}


}


