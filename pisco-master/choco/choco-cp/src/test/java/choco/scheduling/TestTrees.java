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

import choco.cp.solver.constraints.global.scheduling.trees.DisjTreeT;
import choco.cp.solver.constraints.global.scheduling.trees.DisjTreeTL;
import choco.cp.solver.constraints.global.scheduling.trees.IThetaLambdaTree;
import choco.cp.solver.constraints.global.scheduling.trees.IThetaTree;
import choco.cp.solver.constraints.global.scheduling.trees.IVilimTree.TreeMode;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.opres.graph.IBinaryNode;
import choco.kernel.common.opres.graph.INodeLabel;
import choco.kernel.common.opres.graph.ProperBinaryTree;
import choco.kernel.visu.VisuFactory;
import static junit.framework.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;




class DummyStatus implements INodeLabel {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

	@Override
	public void updateInternalNode(IBinaryNode node) {
		LOGGER.info("update: "+node);
	}


	@Override
	public int getNbParameters() {
		return 0;
	}


	@Override
	public Object getParameter(int idx) {
		return null;
	}


	@Override
	public void setParameter(int idx, Object parameter) {
		throw new UnsupportedOperationException("no parameter allowed");

	}


	@Override
	public String toDotty() {
		return "";
	}
}
/**
 * @author Arnaud Malapert</br> 
 * @since 9 févr. 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public class TestTrees {

	private final static int EXAMPLE_SIZE = 12;

	public static final int NB_LEAVES = 100;

	private List<SimpleTask> tasksL;

	@Before
	public void initialize() {
		tasksL=TestTask.getExample();
	}

	protected void compareTrees(ProperBinaryTree tree1, ProperBinaryTree tree2) {
		assertEquals("nb leaves", tree1.getNbLeaves(), tree2.getNbLeaves());
		assertEquals("depth", tree1.getDepth(), tree2.getDepth());
	}

	protected void checkEmptyTree(ProperBinaryTree tree) {
		assertEquals("nb leaves", 0, tree.getNbLeaves());
		assertEquals("depth", -1, tree.getDepth());
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testRemove() {
		ProperBinaryTree tree = new ProperBinaryTree();
		tree.insert(new DummyStatus(),new DummyStatus(),false);
		tree.insert(new DummyStatus(),new DummyStatus(),false);
		tree.insert(new DummyStatus(),new DummyStatus(),false);
		tree.remove((new ProperBinaryTree()).insert(new DummyStatus(),new DummyStatus(), false), false);

	}

	@Test(expected=UnsupportedOperationException.class)
	public void testRemoveInternalNode() {
		ProperBinaryTree tree = new ProperBinaryTree();
		tree.insert(new DummyStatus(),new DummyStatus(),false);
		tree.insert(new DummyStatus(),new DummyStatus(),false);
		tree.remove(tree.getRoot(), false);

	}


	@Test
	public void testProperBinaryTree() {
		ProperBinaryTree tree1 = new ProperBinaryTree();
		ProperBinaryTree tree2 = new ProperBinaryTree();
		LinkedList<IBinaryNode> leaf1 = new LinkedList<IBinaryNode>();
		checkEmptyTree(tree1);
		checkEmptyTree(tree2);
		for (int i = 0; i < NB_LEAVES; i++) {
			leaf1.add(tree1.insert(new DummyStatus(),new DummyStatus(),false));
			tree2.insert(new DummyStatus(),new DummyStatus(),false);
			compareTrees(tree1, tree2);
		}
		Collections.shuffle(leaf1);
		for (int i = 0; i < NB_LEAVES; i++) {
			tree1.remove(leaf1.removeFirst(), false);
			tree2.removeLast(false);
			compareTrees(tree1, tree2);
		}
		checkEmptyTree(tree1);
		checkEmptyTree(tree2);

	}
	
	
	@Test
	public void examples() {
		List<SimpleTask> l=new LinkedList<SimpleTask>();
		for (int i = 0; i < EXAMPLE_SIZE;i++) {
			l.add(new SimpleTask(i,100+i,5+i));
		}
//		DisjTreeT tree = new DisjTreeT(l);
//		tree.setMode(TreeMode.ECT);
//		toDotty(tree);
		DisjTreeTL tree2 = new DisjTreeTL(l);
		tree2.setMode(TreeMode.ECT);
		VisuFactory.getDotManager().show(tree2);
	}

	
	public void launch(IThetaTree tree, int[] res,String label) {
		for (int i = 0; i < tree.getNbLeaves(); i++) {
			SimpleTask t=this.tasksL.get(i);
			assertTrue(tree.insertInTheta(t));
			assertEquals("Ttime "+label+" ",res[i],tree.getTime());
		}
		//toDotty(tree);
		for (int i = tree.getNbLeaves()-1;i>=0; i--) {
			SimpleTask t=this.tasksL.get(i);
			assertEquals("Ttime "+label,res[i],tree.getTime());
			assertTrue(tree.removeFromTheta(t));
		}
	}
	
	@Test
	public void testFailInsertTheta() {
		DisjTreeT tree=new DisjTreeT(tasksL);
		tree.setMode(TreeMode.ECT);
		assertTrue(tree.insertInTheta(tasksL.get(0)));
		assertFalse(tree.insertInTheta(tasksL.get(0)));
	}
	
	@Test
	public void testFailRemoveTheta() {
		DisjTreeT tree=new DisjTreeT(tasksL);
		tree.setMode(TreeMode.LST);
		assertTrue(tree.insertInTheta(tasksL.get(0)));
		assertTrue(tree.insertInTheta(tasksL.get(1)));
		assertTrue(tree.insertInTheta(tasksL.get(2)));
		assertTrue(tree.removeFromTheta(tasksL.get(1)));
		assertFalse(tree.removeFromTheta(tasksL.get(1)));
	}
	
	@Test
	public void testECTDisjTreeT() {
		int[] res={5,11,15,25,32};
		DisjTreeT tree=new DisjTreeT(tasksL);
		tree.setMode(TreeMode.ECT);
		launch(tree, res,"ECT");
	}

	@Test
	public void testLSTDisjTreeT() {
		int[] res={23,21,16,7,0};
		DisjTreeT tree=new DisjTreeT(tasksL);
		tree.setMode(TreeMode.LST);
		launch(tree,res,"LST");
	}
	
	
	@Test
	public void testFailInsertLambda() {
		DisjTreeTL tree=new DisjTreeTL(tasksL);
		tree.setMode(TreeMode.ECT);
		//VizFactory.toDotty(tree);
		assertTrue(tree.removeFromThetaAndInsertInLambda(tasksL.get(0)));
		assertFalse(tree.removeFromThetaAndInsertInLambda(tasksL.get(0)));
	}
	
	@Test
	public void testFailRemoveTheta2() {
		DisjTreeTL tree=new DisjTreeTL(tasksL);
		tree.setMode(TreeMode.LST);
		assertTrue(tree.removeFromThetaAndInsertInLambda(tasksL.get(0)));
		assertTrue(tree.removeFromThetaAndInsertInLambda(tasksL.get(1)));
		assertTrue(tree.removeFromThetaAndInsertInLambda(tasksL.get(2)));
		assertTrue(tree.removeFromLambda(tasksL.get(0)));
		assertFalse(tree.removeFromLambda(tasksL.get(0)));
	}
	
	public void launchRmInsert(IThetaLambdaTree tree,int[] res1,int[] res2,int[] res3,String label) {
		for (int i = 0; i < res1.length; i++) {
			SimpleTask t=tasksL.get(i);
			tree.removeFromThetaAndInsertInLambda(t);
			assertEquals("time "+label,	res1[i],tree.getTime());
			assertEquals("gray time "+label,res2[i],tree.getGrayTime());
			assertEquals("resp. task"+label,tasksL.get(res3[i]),tree.getResponsibleTask());
		}
	}

	public void launchRm(IThetaLambdaTree tree,int[] idx,int[] res1,int[] res2,String label) {
		for (int i = 0; i < idx.length; i++) {
			SimpleTask t=tasksL.get(idx[i]);
			tree.removeFromLambda(t);
			assertEquals("gray time "+label,res1[i],tree.getGrayTime());
			assertEquals("resp. task"+label,res2[i]>0 ? tasksL.get(res2[i]) : null ,tree.getResponsibleTask());

		}
	}
	
	@Test
	public void testECTDisjTreeTL() {
		int[] res1={29,23,20,13};
		int[] res2={32,29,26,20,};
		int[] res3={0,1,1,3};
		DisjTreeTL tree=new DisjTreeTL(tasksL);
		tree.setMode(TreeMode.ECT);
		launchRmInsert(tree, res1, res2, res3,"ECT");
		//toDotty(tree);
		int[] idx={0,3,1};
		int[] res4={20,17,13};
		int[] res5={3,1,-1};
		launchRm(tree, idx, res4, res5, "ECT");
	}
	
	@Test
	public void testLSTDisjTreeTL() {
		int[] res1={3,3,7};
		int[] res2={0,2,3};
		int[] res3={0,0,2};
		DisjTreeTL tree=new DisjTreeTL(tasksL);
		tree.setMode(TreeMode.LST);
		launchRmInsert(tree, res1, res2, res3,"ECT");
	}
	
//	@Test
//	public void testCumulTree() {
//		tasksL.clear();
//		tasksL.add(new SimpleTask(0,2,5));
//		tasksL.add(new SimpleTask(0,3,3));
//		tasksL.add(new SimpleTask(0,0,2));
//		SimpleResource rsc=new SimpleResource(tasksL,new int[] {2,2,3},4);
//		int[] LE = new int[]{7,7,2};
//		//changer en energy
//		long[] energy=new long[3];
//		int[] energy2=new int[3];
//		for (int i = 0; i < energy.length; i++) {
//			//energy[i]=rsc.getMinConsumption(tasksL.get(i));
//			energy2[i]= (int) energy[i];
//		}
//		CumTreeT<SimpleTask> tree = new CumTreeT<SimpleTask>(rsc);
//		tree.setMode(TreeMode.ECT);
//		int[] t={2,1,0};
//		for (int i = 0; i < t.length; i++) {
//			tree.insertInTheta(tasksL.get(t[i]));
//			//toDotty(tree);
//			if (tree.getEnergy() > 4 * LE[t[i]]) {
//				fail();
//			}
//		}
//		tree.reset();
//	}
}


