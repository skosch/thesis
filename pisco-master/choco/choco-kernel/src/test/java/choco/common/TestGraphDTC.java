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

package choco.common;


import static choco.kernel.common.opres.graph.GraphDTC.ADDED;
import static choco.kernel.common.opres.graph.GraphDTC.CYCLE;
import static choco.kernel.common.opres.graph.GraphDTC.TRANSITIVE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import choco.kernel.common.opres.graph.DagDTC;
import choco.kernel.visu.VisuFactory;





@SuppressWarnings({"PMD.LocalVariableCouldBeFinal","PMD.MethodArgumentCouldBeFinal"})
/**
 * @author Arnaud Malapert
 *
 */
public class TestGraphDTC {


	private final static int NUMBER=6;


	private final static int[][] edges={ {0,1,2,1,3,3,4,3,5,5,1,0,1},
										 {2,2,4,4,5,2,3,1,3,2,0,5,5} };

	private final static int[] states={ADDED,ADDED,ADDED,TRANSITIVE,ADDED,ADDED,
										CYCLE,ADDED,CYCLE,ADDED,ADDED,ADDED,TRANSITIVE};

	private static boolean[][] matrix;

	private static int step;

	private static DagDTC graph;
	//private static GraphDTC graph;

	@BeforeClass
	public static void initialize() {
		graph=new DagDTC(NUMBER);
		//graph=new GraphDTC(NUMBER);
		step=0;
		matrix=new boolean[NUMBER][NUMBER];
		for (int i = 0; i < NUMBER; i++) {
			matrix[i][i]=true;
		}
	}


	private static final void update(final boolean state) {
		switch (step) {
		case 0: matrix[0][2]=state;break;
		case 1:	matrix[1][2]=state;break;
		case 2: {
		matrix[2][4]=state;
		matrix[1][4]=state;
		matrix[0][4]=state;
		break;
		}
		case 3 : matrix[3][5]=state;break;
		case 4: {
		matrix[3][2]=state;
		matrix[3][4]=state;
		break;}
		case 5:matrix[3][1]=state;break;
		case 6: {
		matrix[5][2]=state;
		matrix[5][4]=state;
		break;}
		case 7 :{
		matrix[3][0]=state;
		matrix[1][0]=state;
		break;}
		case 8 : {
		matrix[0][5]=state;
		matrix[1][5]=state;
		break;
		}
		default:
			break;
		}
		boolean[][] tmp = graph.toTreeNodeMatrix();
		for (int i = 0; i < matrix.length; i++) {
			assertTrue("matrix equality", Arrays.equals(matrix[i], tmp[i]));
		}
	}


	@Test
	public void testGraph() {
		//add
		for (int i = 0; i < states.length; i++) {
			final int result=graph.add(edges[0][i], edges[1][i]);
			assertEquals("add arc", states[i],result);
			if(result==ADDED) {
			update(true);
			step++;
			}
		}
		//VisuFactory.getDotManager().show(graph);
		//remove
		assertFalse("rm", graph.remove(0,3));
		for (int i = states.length-1; i >=0; i--) {
			final boolean result=graph.remove(edges[0][i], edges[1][i]);
			assertEquals("remove arc",  states[i] == ADDED,result);
			if(result) {
			step--;
			update(false);
			}
		}
	}
}
