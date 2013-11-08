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

package choco.cp.solver.constraints.global.scheduling.disjunctive;

import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.scheduling.trees.AltDisjTreeTL;
import choco.cp.solver.constraints.global.scheduling.trees.AltDisjTreeTLTO;
import choco.cp.solver.constraints.global.scheduling.trees.IVilimTree.TreeMode;
import static choco.cp.solver.constraints.global.scheduling.trees.IVilimTree.TreeMode.ECT;
import static choco.cp.solver.constraints.global.scheduling.trees.IVilimTree.TreeMode.LST;
import static choco.kernel.common.util.comparator.TaskComparators.*;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.IStateIntProcedure;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.scheduling.IRMakespan;
import choco.kernel.solver.variables.scheduling.IRTask;
import choco.kernel.solver.variables.scheduling.ITask;

import java.util.*;

/**
 * @author Arnaud Malapert</br> 
 * @since 2 mars 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public final class AltDisjRules extends AbstractDisjRules implements Iterable<IRTask>, IStateIntProcedure {

	private final IStateInt size;

	private AltBipartiteQueue<IRTask> rqueue;

	protected AltDisjTreeTL altDisjTreeTL;

	////*****************************/////
	protected AltDisjTreeTLTO altDisjTreeTLTO;
	////*****************************/////

	// FIXME - avoid useless insertions/deletions in trees (trigger according to rules) - created 4 juil. 2011 by Arnaud Malapert
	public AltDisjRules(final IRTask[] rtasks, IRMakespan makespan, IEnvironment environment) {
		super(rtasks, makespan, true);
		size = environment.makeIntProcedure(this, rtasks.length);
		rqueue = new AltBipartiteQueue<IRTask>(rtasks);
		final ITask[] tasks = getTaskArray();//uses getHTask
		altDisjTreeTL = new AltDisjTreeTL(Arrays.asList(tasks));
		///*****************///
		altDisjTreeTLTO = new AltDisjTreeTLTO(Arrays.asList(tasks));
		///*****************///
	}
	
	@Override
	protected void sortRTasks(Comparator<IRTask> cmp) {
		Arrays.sort(rtasks, 0, size.get(), cmp);
	}


	/**
	 * call during backtrack if necessary
	 */
	@Override
	public void apply(final int oldVal, final int newVal) {
		for (int i = oldVal; i < newVal; i++) {
			altDisjTreeTL.insert(rtasks[i].getHTask());
			//Should insert tasks to TLTO tree as well
			altDisjTreeTLTO.insert(rtasks[i].getHTask());
		}
	}
	
	private void setupListsAndTreeTL(final Comparator<IRTask> taskComp,final Comparator<IRTask> queueComp, final TreeMode mode) {
		sortRTasks(taskComp);
		sortQueue(rqueue, queueComp);
		setupMasterTree(altDisjTreeTL, mode);
	}

	@Override
	public boolean isActive() {
		return size.get()>0;
	}

	private void makeRemovalSwap(final IRTask[] rtasks, final IRTask rtask) {
		int i = 0;
		while( i < size.get()) {
			if( rtasks[i] == rtask) {
				final int newIndex = size.get()-1;
				final IRTask tmp = rtasks[i];
				rtasks[i] = rtasks[newIndex];
				rtasks[newIndex] = tmp;
				return;
			}
			i++;
		}
		throw new NoSuchElementException("cant remove task from the constraint .");
	}

	private void insertInTree(final IRTask rtask) {
		if(rtask.isOptional()) {
			altDisjTreeTL.insertInLambda(rtask);
		}else if(rtask.isRegular()) {
			altDisjTreeTL.insertInTheta(rtask);
		}
	}

	/**
	 * Used by Edge Finding algorithm
	 * @param respTask: task that will be removed
	 * @param omega: specifies whether the task should be removed from Omega, or Lambda
	 */
	private void setAsRemoval(IRTask respTask, boolean omega) throws ContradictionException{
		//Disable optional task.
		if(!omega)
			updateManager.storeLambdaRemoval(respTask, altDisjTreeTLTO);
		else
			updateManager.storeOmegaRemoval(respTask, altDisjTreeTLTO);
	}

	@Override
	public void remove(IRTask rtask) {
		makeRemovalSwap(rtasks, rtask);
		makeRemovalSwap(rqueue.elementData, rtask);
		//Remove disabled task from TL, and TLTO trees.
		altDisjTreeTL.remove(rtask.getHTask());
		altDisjTreeTLTO.remove(rtask.getHTask());
		size.add(-1);
	}

	@Override
	public boolean detectablePrecedenceEST() throws ContradictionException {
		setupListsAndTreeTL(makeREarliestCompletionTimeCmp(),makeRLatestStartingTimeCmp(), ECT);
		for (IRTask rti : this) {
			final ITask i = rti.getHTask();
			if(!rti.isEliminated()){
				while( !rqueue.isEmpty() && i.getECT() > rqueue.peek().getHTask().getLST()){
					insertInTree(rqueue.poll());
				}
				boolean rm = false;
				if(rti.isRegular()){
					rm = altDisjTreeTL.removeFromTheta(i);
				}
				//*****************************************
				if(altDisjTreeTL.getTime() > i.getEST()){
					//update is possible
					if(altDisjTreeTL.getTime() > i.getLST()){
						//update will lead to incoherent time window
						if(rti.isRegular()) {rti.fail();}
						if(rti.isOptional()){
							if(altDisjTreeTL.getTaskType(rti) == 2)
								updateManager.storeLambdaRemoval(rti,altDisjTreeTL);
							else
								updateManager.storeRemoval(rti);
						}
					}
					else{
						//Update will be done
						updateManager.storeUpdate(rti,altDisjTreeTL.getTime());
					}
				}
				//*******************************************
				if(rti.isRegular()){
					while( altDisjTreeTL.getGrayTime() > i.getLST()) {
						updateManager.storeLambdaRemoval(altDisjTreeTL);
					}
					if(rm) {assert altDisjTreeTL.insertInTheta(i);}
				}
			}
		}
		setMakespanLB(altDisjTreeTL);
		return updateManager.fireAndUpdateEST();
	}

	@Override
	public boolean detectablePrecedenceLCT() throws ContradictionException {
		setupListsAndTreeTL(makeReverseRLatestStartingTimeCmp(), makeReverseREarliestCompletionTimeCmp(), LST);
		for (IRTask rti : this) {
			if(!rti.isEliminated()){
				final ITask i = rti.getHTask();
				while(!rqueue.isEmpty() && i.getLST()< rqueue.peek().getHTask().getECT()) {
					insertInTree(rqueue.poll());
				}
				boolean rm = false;
				if(rti.isRegular()) {
					rm=altDisjTreeTL.removeFromTheta(i);
				}
				//updating regular, and optional tasks using regular tasks.
				//*********************************************************
				if(altDisjTreeTL.getTime() < i.getLCT()){
					if(altDisjTreeTL.getTime() < i.getECT()){
						if(rti.isRegular()) {rti.fail();}
						if(rti.isOptional()){
							if(altDisjTreeTL.getTaskType(rti) == 2)
								//already added to lambda, remove it from there
								updateManager.storeLambdaRemoval(rti,altDisjTreeTL);
							else
								//Not added to lambda
								updateManager.storeRemoval(rti);
						}
					}
					else
						//update task.
						updateManager.storeUpdate(rti,altDisjTreeTL.getTime());
				}
				if(rti.isRegular()){
					while( altDisjTreeTL.getGrayTime() < i.getECT()) {
						updateManager.storeLambdaRemoval(altDisjTreeTL);
					}
					//we have to be sure that i was active in disjTreeT
					if(rm) {altDisjTreeTL.insertInTheta(i);}
				}
			}
		}
		return updateManager.fireAndUpdateLCT();
	}

	@Override
	public boolean notFirst() throws ContradictionException {
		setupListsAndTreeTL(makeReverseREarliestStartingTimeCmp(), makeReverseREarliestCompletionTimeCmp(), LST);
		ITask j=null, ja = null;
		for (IRTask rti : this) {
			final ITask i = rti.getHTask();
			if(!rti.isEliminated()){
				while(!rqueue.isEmpty()  && i.getEST() < rqueue.peek().getHTask().getECT()) {
					final IRTask tmp = rqueue.poll();
					ja = tmp.getHTask();
					if(tmp.isRegular()) { j = ja;}
					insertInTree(tmp);
				}
				boolean rm =false;
				if(rti.isRegular()) {
					rm = altDisjTreeTL.removeFromTheta(i);
				}
				if( altDisjTreeTL.getTime() < i.getECT()) {
					if(j.getECT() > i.getEST()){
						//Incoherent domains
						if(j.getECT() > i.getLST()){
							if(rti.isRegular()) {rti.fail();}
							if(rti.isOptional()) {
								assert altDisjTreeTL.getTaskType(rti)== 2;
								updateManager.storeLambdaRemoval(rti, altDisjTreeTL);
							}
						}
						else{
							//Update is possible
							updateManager.storeUpdate(rti,j.getECT());
						}
					}
				}
				if(rti.isRegular() && i.getLST() < ja.getECT()){
					while( altDisjTreeTL.getGrayTime() < i.getECT()) {
							updateManager.storeLambdaRemoval(altDisjTreeTL);
						}
				}
				if(rm) {altDisjTreeTL.insertInTheta(i);}
			}
		}
		return updateManager.fireAndUpdateEST();
	}

	@Override
	public boolean notLast() throws ContradictionException {
		setupListsAndTreeTL(makeRLatestCompletionTimeCmp(),makeRLatestStartingTimeCmp(), ECT);
		ITask j=null, ja=null;
		for (IRTask rti : this) {
			final ITask i = rti.getHTask();
			if(!rti.isEliminated()){
				while(!rqueue.isEmpty() && i.getLCT()> rqueue.peek().getHTask().getLST()) {
					final IRTask tmp = rqueue.poll();
					ja = tmp.getHTask();				
					if(tmp.isRegular()) {j = ja;}
					insertInTree(tmp);
				}
				boolean rm = false;
				if(rti.isRegular()) {
					//compute pruning
					rm = altDisjTreeTL.removeFromTheta(i);
				}
				//********************************
				if(altDisjTreeTL.getTime() > i.getLST()) {
					//updating both regular, and optional tasks based on regular ones
					if(j.getLST() < i.getLCT()){
						//update is possible.
						if(j.getLST() < i.getECT()){
							//update that leads to Time window incoherence.
							if(rti.isRegular()) {rti.fail();}
							if(rti.isOptional()) {
								assert altDisjTreeTL.getTaskType(rti)== 2;
								updateManager.storeLambdaRemoval(rti, altDisjTreeTL);}
						}
						else{
							updateManager.storeUpdate(rti,j.getLST());
						}
					}
				}
				//***********************************
				if(rti.isRegular() && ja.getLST() < i.getECT()){
					while( altDisjTreeTL.getGrayTime() > i.getLST()) {
						updateManager.storeLambdaRemoval(altDisjTreeTL);
					}
				}
				//return back the removed task i.
				if(rm) { assert altDisjTreeTL.insertInTheta(i);}
			}
		}
		setMakespanLB(altDisjTreeTL);
		return updateManager.fireAndUpdateLCT();
	}

	@Override
	public void overloadChecking() throws ContradictionException{
		sortRTasks(makeRLatestCompletionTimeCmp());
		this.setupMasterTree(altDisjTreeTL, ECT);
		for(IRTask t : this) {
			final ITask i = t.getHTask();
			insertInTree(t);
			if(t.isRegular()){
			//This part is checked if the current task is regular (p. 419).
			//In paper:"Extension of O(n log n) filtering algorithms"	
				if(altDisjTreeTL.getTime()> i.getLCT()) t.fail();
				while( altDisjTreeTL.getGrayTime() >  i.getLCT()) {
					updateManager.storeLambdaRemoval(altDisjTreeTL);
				}
			}
		}
		setMakespanLB(altDisjTreeTL);
	}

	/**
	 * Edge Finding supporting optional activities reasoning by Sebastian Kuhnert
	 * @return true if filtering takes place
	 */
	@Override
	public boolean edgeFindingEST()throws ContradictionException{
		//rqueue should be sorted in descending order of LCT
		//While the Tasks list should be sorted in ascending order of EST 
		setupEF(ECT, makeReverseRLatestCompletionTimeCmp());
		setMakespanLB(altDisjTreeTLTO);
		IRTask rtj=rqueue.peek();
		ITask j=rtj.getHTask();
		if(rtj.isRegular())
			if(altDisjTreeTLTO.getTime() > j.getLCT()) {rtj.fail();}
		do{
			//Remove Task j from either Theta, or Omega, and insert it in Lambda
			if(rtj.isOptional()){;
				altDisjTreeTLTO.removeFromOmegaAndInsertInLambda(rtj);
			}
			else if(rtj.isRegular()){
				altDisjTreeTLTO.removeFromThetaAndInsertInLambda(rtj);
			}
			rqueue.poll();
			if(!rqueue.isEmpty()) {
				rtj=rqueue.peek();
				j= rtj.getHTask();
			}
			else {break;}
			if(rtj.isRegular()){
				if(altDisjTreeTLTO.getTime() > j.getLCT()) {rtj.fail();}//Overload Rule applies.
				checkTL(j, ECT);
				checkTO(j, ECT);
				checkFTLO(j, ECT);
			}
			else if(rtj.isOptional()){
				checkSTLOEST(rtj);
			}
		}while(!rqueue.isEmpty());
		//Apply all removals
		return updateManager.fireAndUpdateEST();
	}

	private int [] calculateECTTOL(int [] ectTUnionOArr){
		//Array holds the ectTOL in the first postion, and index
		//(if any)of responsible optional task
		int [] results = new int[2];
		int ectT = Integer.MIN_VALUE; //ECT(Theta)
		int ectTL = Integer.MIN_VALUE; //ECT(Theta,Lambda)
		int ectTO = Integer.MIN_VALUE; //ECT(Theta,Omega)
		int ectTOL = Integer.MIN_VALUE;	//ECT(Theta,Lambda',Omega)
		int respTOLIdx = -1; //Contains the index of optional task responsible for highest ECT(T,O,L').
		int respTOIdx = -1; //Contains the index of optional task responsible for highest ECT(T,O).
		int respEnabledIdx = -1;//Contains the index of enabled task responsible for highest ECT(T,L').
		for(int j=0; j< size.get(); j++){
			final IRTask rtaskAtj = rtasks[j];
			final ITask taskAtj = rtaskAtj.getHTask();
			final int taskType = altDisjTreeTLTO.getTaskType(rtaskAtj);
			if(taskType == 1){
				//Theta
				//======
				assert rtaskAtj.isRegular();
				//-- ECT(T) -------
				final int newEctT = ectT + taskAtj.getMinDuration();
				ectT = Math.max(newEctT, taskAtj.getECT());
				//-----------------
				//-- ECT(T,L) -----
				final int newEctTL = ectTL +taskAtj.getMinDuration();
				ectTL = Math.max(newEctTL, taskAtj.getECT());
				//-----------------
				//-- ECT(T,O) -----
				final int newEctTO = ectTO + taskAtj.getMinDuration();
				if( respTOIdx == -1 ){ ectTO = Math.max(newEctTO, taskAtj.getECT());}
				else{ ectTO = newEctTO;}
				//-----------------
				//-- ECT(T,O,L) ---
				final int newEctTOL = ectTOL + taskAtj.getMinDuration();
				if( respTOLIdx == -1 ){ 
					ectTOL = Math.max(newEctTOL, taskAtj.getECT());}
				else {ectTOL = newEctTOL;}
				//-----------------
			}else if(taskType == 2){
				assert rtaskAtj.isOptional();
				//Omega
				//--- ECT(T,O,L)---
				int newEctTOL = ectTOL;
				if( (respTOLIdx == -1) || newEctTOL < taskAtj.getECT()){
					 newEctTOL = taskAtj.getECT();
					 respTOLIdx = j;
				}
				if( newEctTOL < ectT + taskAtj.getMinDuration()){
					newEctTOL = ectT + taskAtj.getMinDuration();
					respTOLIdx = j;
				}
				if( newEctTOL < ectTL + taskAtj.getMinDuration()){
					if(respEnabledIdx != -1){
						//Comparing the task's LST that causes the highest ect(Theta, Lamda')
						//With the ECT(Theta Union j) j: current task.
						if(rtasks[respEnabledIdx].getHTask().getLST() < ectTUnionOArr[j]){
							newEctTOL = ectTL + taskAtj.getMinDuration();
							respTOLIdx = j;
						}
					}
				}
				ectTOL = newEctTOL;
				//-----------------
				//---- ECT(T,O) ---
				int newEctTO = ectTO;
				if( respTOIdx == -1 || newEctTO < taskAtj.getECT()){
						newEctTO = taskAtj.getECT();
						//Index of the rtaskAtj
						respTOIdx = j;
				}
				if(newEctTO < ectT + taskAtj.getMinDuration()){
					newEctTO = ectT + taskAtj.getMinDuration();
					respTOIdx = j;
				}
				ectTO = newEctTO; 
				//-----------------
				// ectT, ectTL are unchanged
				//-----------------
			}else{
				//Either Lambda or NIL
				if(rtaskAtj.isRegular()){
					//--- ECT(T,O,L) ------
					int newEctTOL = ectTOL;
					if(newEctTOL < ectTO + taskAtj.getMinDuration()){
						if(respTOLIdx == -1){
							//They should both be the same.
							assert respTOIdx == -1;
							newEctTOL = ectTO + taskAtj.getMinDuration();
						}
						//comparing LST of the task at j with ECT(Theta Union o)
						//Where O is the optional task that gives the highest ECT(Theta,O)
						else if(taskAtj.getLST() < ectTUnionOArr[respTOIdx]){
							newEctTOL = ectTO + taskAtj.getMinDuration();
							respTOLIdx = respTOIdx;
						}
					}
					if(newEctTOL < taskAtj.getECT() && respTOLIdx == -1){
						newEctTOL = taskAtj.getECT();
					}
					ectTOL = newEctTOL;
					//---------------------
					//--- ECT(T,L) --------
					int newEctTL  = Math.max(taskAtj.getECT(), ectT + taskAtj.getMinDuration());
					if(newEctTL > ectTL){
						ectTL = newEctTL;
						respEnabledIdx = j;
					}
					//---------------------
					// ectT, ectTO are unchanged.
				}
			}	
		}
		assert altDisjTreeTLTO.getTime() == ectT;
		results[0] = ectTOL;
		results[1] = respTOLIdx;
		return results;
	}

	private void checkFTLO(ITask j, TreeMode mode)throws ContradictionException{
		
		//Sort array
		switch(mode){
		case ECT:
			//sort array in ascending order of est
			sortRTasks(makeREarliestStartingTimeCmp());
			break;
		case LST:
			//Sort array in ascending order of lct
			sortRTasks(makeRLatestCompletionTimeCmp());
			break;
		default:
			throw new UnsupportedOperationException("unknown tree mode.");
		}
		//Stores the results
		int [] results ;
		switch (mode){
		case ECT:
			//If number of omega tasks equal to zero, then ectTOL cannot be calculated.
			//Initialise The array only once per iteration of the main loop in EF.
			int [] ectTUnionOArr = this.calculateECT();
			while(altDisjTreeTLTO.getNbOmegaTasks() != 0){
				  assert altDisjTreeTLTO.getNbOmegaTasks() >= 0;	
				  results = calculateECTTOL(ectTUnionOArr);	
				  assert results[0] >=0;
				  if(results[0] > j.getLCT()){
					  //get responsible task.
					  assert results[1]!= -1;
					  final IRTask resprTask = rtasks[results[1]];
					  setAsRemoval(resprTask, true);
				  }
				  else
					  break;
			}
			break;
		case LST:
			//If number of omega tasks equal to zero, then ectTOL cannot be calculated.
			int [] lctTUnionOArr = this.calculateLST();
			while(altDisjTreeTLTO.getNbOmegaTasks() != 0){
				  assert altDisjTreeTLTO.getNbOmegaTasks() >= 0;		
				  results = calculateLSTTOL(lctTUnionOArr);	
				  if(results[0] < j.getEST()){
					  //get responsible task.
					  assert results[1]!= -1;
					  final IRTask resprTask = rtasks[results[1]];
					  setAsRemoval(resprTask, true);
				  }
				  else
					  break;
			}
			break;
		default:
			throw new UnsupportedOperationException("unknown tree mode.");
		}
	}
 
	/**
	 * Method calculates the minimum LST(Theta,Omega,Lambda') as defined by Sebastian
	 * @param lstTUnionOArr
	 * @return
	 */
	private int [] calculateLSTTOL(int [] lstTUnionOArr){
		int [] results = new int [2];
		int lstT = Integer.MAX_VALUE; //LST(Theta)
		int lstTL = Integer.MAX_VALUE; //LST(Theta,Lambda)
		int lstTO = Integer.MAX_VALUE; //LST(Theta,Omega)
		int lstTOL = Integer.MAX_VALUE;	//LST(Theta,Lambda',Omega)
		int pTOL = 0;
		int pTL = 0;
		int pTO = 0;
		int pT = 0;
		int respTOLIdx = -1; //Contains the index of optional task responsible for highest LST(T,O,L').
		int respTOIdx = -1; //Contains the index of optional task responsible for highest LST(T,O).
		int respEnabledIdx = -1;//Contains the index of enabled task responsible for highest LST(T,L').
		for(int j=0; j< size.get(); j++){
			final IRTask rtaskAtj = rtasks[j];
			final ITask taskAtj = rtaskAtj.getHTask();
			final int taskType = altDisjTreeTLTO.getTaskType(rtaskAtj);
			if(taskType == 1){
				//Theta
				assert rtaskAtj.isRegular();
				//-- LST(T,L,O) ---
				lstTOL = Math.min(lstTOL, taskAtj.getLST() - pTOL);
				//-----------------
				//-- LST(T,O) -----
				lstTO = Math.min(lstTO, taskAtj.getLST() - pTO);
				//-----------------
				//-- LST(T,L) -----
				lstTL = Math.min(lstTL, taskAtj.getLST() - pTL);  
				//-----------------
				//-- LST(T) -------
				lstT = Math.min(lstT, taskAtj.getLST() - pT);
				//-----------------
				//Update duration
				pT = pT + taskAtj.getMinDuration();
			}else if(taskType ==2){
				//Omega
				//-- LST(T,L,O) ---
				int newLSTTOL = lstTOL;
				if(respTOLIdx == -1 || newLSTTOL > taskAtj.getLST() - pT){
					newLSTTOL = taskAtj.getLST() - pT;
					pTOL = pT + taskAtj.getMinDuration();
					respTOLIdx = j;
				}
				if(newLSTTOL > taskAtj.getLST() - pTL){
					if(respEnabledIdx != -1){
						//Condition for lambda'
						if(rtasks[respEnabledIdx].getHTask().getECT() > lstTUnionOArr[j]){
							newLSTTOL = taskAtj.getLST() - pTL;
							//update the duration according to the selected optional task.
							pTOL = pTL + taskAtj.getMinDuration();
							respTOLIdx = j;
						}
					}
				}
				lstTOL = newLSTTOL;
				//-----------------
				//-- LST(T,O) -----
				int newLSTTO = lstTO;
				if(respTOIdx == -1 || newLSTTO > taskAtj.getLST() - pT){
					newLSTTO = taskAtj.getLST() - pT;
					pTO = pT + taskAtj.getMinDuration();
					respTOIdx = j;
				}
				lstTO = newLSTTO;
				//-----------------
				//LST(T,L), and LST(T) are not changed
			}else if(rtaskAtj.isRegular()){
				//-- LST(T,L,O) ---
				int newLSTTOL = lstTOL;
				if(newLSTTOL > taskAtj.getLST() - pTO){
					if(respTOLIdx == -1){
						assert respTOIdx == -1;
						newLSTTOL = taskAtj.getLST() - pTO;
						assert pTO == pT;
						pTOL = pTO + taskAtj.getMinDuration();
					}
					//Condition for Lambda'
					else if(taskAtj.getECT() > lstTUnionOArr[respTOIdx]){
						newLSTTOL = taskAtj.getLST() - pTO;
						pTOL = pTO + taskAtj.getMinDuration();
						respTOLIdx = respTOIdx;
					}	
				}
				lstTOL = newLSTTOL; 
				//-----------------
				//-- LST(T,L) -----
				int newLSTTL = lstTL;
				if(newLSTTL > taskAtj.getLST() - pT){
					newLSTTL = taskAtj.getLST() - pT;
					pTL = pT + taskAtj.getMinDuration();
					respEnabledIdx = j;
				}
				lstTL = newLSTTL;
				//-----------------
				//LST(T,O), and LST(T) are not changed
			}
		}
		assert lstT == altDisjTreeTLTO.getTime();
		results[0] = lstTOL;
		results[1] = respTOLIdx;
		return results;
	}
	
	private int [] calculateLST(){
		
		int [] lstAfter = new int[size.get()];
		int [] lstBefore = new int[size.get()];
		int [] pBefore = new int[size.get()];
		int [] lstWith = new int[size.get()];
		//set lctBefore_0 to + Infinity, the same for lstAfter_(size-1)
		final int first = 0; 
		final int last = size.get()-1;
		lstBefore[first] = Integer.MAX_VALUE;
		lstAfter[last] = Integer.MAX_VALUE;
		pBefore[first] = 0;
		//Looping over tasks list front to back.
		for(int i = 0; i < size.get()-1; i++){
			final IRTask taskAtI = rtasks[i];
			final ITask tAtI = taskAtI.getHTask();
			if(altDisjTreeTLTO.getTaskType(taskAtI)==1){
				lstBefore[i+1] = Math.min(lstBefore[i], tAtI.getLST() - pBefore[i]);
				pBefore[i+1] = pBefore[i]+tAtI.getMinDuration();
			}else{
				lstBefore[i+1] = lstBefore[i];
				pBefore[i+1] = pBefore[i];
			}
		}
		//Looping over tasks list back to front.
		for(int i = size.get()-1 ; i >= 1; i--){
			final IRTask taskAtI = rtasks[i];
			final ITask tAtI = taskAtI.getHTask();
			final IRTask trBeforeI = rtasks[i-1];
			final ITask tBeforeI = trBeforeI.getHTask();
			if(altDisjTreeTLTO.getTaskType(taskAtI)== 1){
				lstAfter[i-1] = Math.min( tAtI.getLST(), lstAfter[i] - tAtI.getMinDuration());
			}
			else{
				lstAfter[i-1] = lstAfter[i];
			}
			//calculate lstWith
			int newLST = lstBefore[i-1];
			if(newLST > tBeforeI.getLST() - pBefore[i-1]){
				newLST = tBeforeI.getLST() - pBefore[i-1];
			}
			int totalP = pBefore[i-1]+ tBeforeI.getMinDuration();
			if(newLST > lstAfter[i-1] - totalP){
				newLST = lstAfter[i-1] - totalP;
			}
			lstWith[i-1] = newLST;
		}
		//Calculate lstWith[size-1].
		lstWith[last] = Math.min(lstBefore[last], rtasks[last].getHTask().getLST() - pBefore[last]);
		return lstWith;
	}
	
	/**
	 * This method is used to calculate ECT(Theta Union O) for each optional task in Omega
	 * It allows accessing this value in constant time. Has time complexity O(n)
	 */
	private int[] calculateECT()
	{
		int[] ectAfter = new int[size.get()]; //Used to hold the total sum of ECT of Theta tasks after task t. 
		int[] ectBefore = new int [size.get()]; // holds the total sum of ECT of Theta tasks before task t.
		int[] pAfter = new int[size.get()]; // holds the total sum of processing time of Theta tasks after task t.
		int[] ectWith = new int[size.get()]; //holds the ECT(Theta Union task t).
		//Set ectBefore_0 to -Infinity, the same for ectAfter_(size-1)
		ectBefore[0] = Integer.MIN_VALUE;
		ectAfter[size.get() - 1] = Integer.MIN_VALUE;
		//Set pAfter_(size-1) to zero.
		pAfter[size.get() - 1] = 0;
		//Looping over rtasks list back to front to calculate ectAfter, and pAfter
		for(int i = size.get() - 1 ; i >= 1; i--){
			final IRTask taskAtI = rtasks[i];
			final ITask tAtI = taskAtI.getHTask();
			//THETA = 1
			if(altDisjTreeTLTO.getTaskType(taskAtI) == 1){
				
				pAfter[i-1] =  pAfter[i] + tAtI.getMinDuration() ;
				ectAfter[i-1] = Math.max(ectAfter[i], tAtI.getECT()+ pAfter[i]);
				
			}else{
				
				//task is not in theta, pass the values
				pAfter[i-1] =  pAfter[i];
				ectAfter[i-1] = ectAfter[i];
			}
		}
		//Looping again over rtasks list front to back, to calculate ectBefore, and ectWith
		for(int i=0; i < size.get()-1; i++){
			final IRTask taskAtI = rtasks[i];
			final ITask tAtI = taskAtI.getHTask();
			final ITask taskAfterI = rtasks[i+1].getHTask();//used in calculation of ectWith
			if(altDisjTreeTLTO.getTaskType(taskAtI) == 1){
				//Theta
				ectBefore[i+1] = Math.max( tAtI.getECT() , ectBefore[i] + 
						tAtI.getMinDuration());
			}else{ ectBefore[i+1] = ectBefore[i];}
			//Calculate ECT
			int newEctWith = ectAfter[i+1];
			if(newEctWith < taskAfterI.getECT() + pAfter[i+1])
				newEctWith = taskAfterI.getECT() + pAfter[i+1];
			if(newEctWith < ectBefore[i+1] + taskAfterI.getMinDuration() + pAfter[i+1])
				newEctWith = ectBefore[i+1] + taskAfterI.getMinDuration() + pAfter[i+1];  
			ectWith[i+1] = newEctWith;
		}
		final ITask first = rtasks[0].getHTask();
		ectWith[0] = Math.max(ectAfter[0], first.getECT() + pAfter[0]);
		
		return ectWith;
	}
	
	/**
	 * Method calculates the ECT(Theta,Omega,Lambda), when the current task from the queue
	 * is optional
	 * @param j
	 */
	private void checkSTLOEST(IRTask j)throws ContradictionException{
			//Sort array in ascending order of est.
		sortRTasks(makeREarliestStartingTimeCmp());
			//Temporarily remove task j from omega, and embed it in Theta
			altDisjTreeTLTO.removeFromOmega(j);
			altDisjTreeTLTO.insertInTheta(j);
			//Cache the value
			final int ectTUO = altDisjTreeTLTO.getTime();
			int ectT = Integer.MIN_VALUE;
			int ectTL = Integer.MIN_VALUE;
			//Tasks in the list are in ascending order of est
			for(IRTask rtask: this){
				final ITask i = rtask.getHTask();
				final int taskType = altDisjTreeTLTO.getTaskType(rtask);
				if(taskType == 1){
					//Theta
					ectTL = Math.max( ectTL + i.getMinDuration() , i.getECT());
					ectT = Math.max( ectT + i.getMinDuration() , i.getECT());
				}else if(rtask.isRegular()){
					//Then task i belongs to Tenabled\Theta
					if(i.getLST() < ectTUO){
						final int l = ectT + i.getMinDuration();
						final int r = i.getECT();
						ectTL = Math.max(ectTL, Math.max(l, r));
					}
				}
			}
			//Reverse the operation
			altDisjTreeTLTO.removeFromTheta(j.getHTask());
			altDisjTreeTLTO.insertInOmega(j);
			final int lct = j.getHTask().getLCT();
			if( ectTUO > lct || ectTL > lct ){
				setAsRemoval(j, true);
			}
	}

	private void checkSTLOLCT(IRTask j)throws ContradictionException{
		//Sort array in ascending order of lct
		sortRTasks(makeRLatestCompletionTimeCmp());
		//Temporarily remove task j from omega, and embed it in Theta
		altDisjTreeTLTO.removeFromOmega(j);
		altDisjTreeTLTO.insertInTheta(j);
		//Cache the value
		final int lstTUO = altDisjTreeTLTO.getTime();
		int lstT = Integer.MAX_VALUE;
		int lstTL = Integer.MAX_VALUE;
		int pT = 0;
		int pTL = 0;
		for(int tIdx =0; tIdx < size.get(); tIdx++){
			final IRTask ir = rtasks[tIdx];
			final ITask i = ir.getHTask();
			final int taskType = altDisjTreeTLTO.getTaskType(rtasks[tIdx]);
			if( taskType == 1){
				//Theta
				lstTL = Math.min(lstTL, i.getLST() - pTL );
				lstT = Math.min(lstT, i.getLST() - pT);
				pT = pT + i.getMinDuration();
			}else if(ir.isRegular()){
				if(i.getECT() > lstTUO){
					if(lstTL > i.getLST() - pT){
						lstTL = i.getLST() - pT;
						pTL = pT + i.getMinDuration();
					}
				}
			}
		}
		//Reverse the operation
		altDisjTreeTLTO.removeFromTheta(j.getHTask());
		altDisjTreeTLTO.insertInOmega(j);
		final int est = j.getHTask().getEST();
		if(lstTL < est||lstTUO < est){
			setAsRemoval(j, true);
		}
		
	}

	private void checkTL(ITask j, TreeMode mode) throws ContradictionException{
		switch(mode){
		case ECT:
			while(altDisjTreeTLTO.getGrayTime() > j.getLCT()){
				//get responsible task from Lambda
				final IRTask rti = (IRTask) altDisjTreeTLTO.getResponsibleTask();
				final ITask i = rti.getHTask();
				if(altDisjTreeTLTO.getTime() > i.getEST()){
					if(altDisjTreeTLTO.getTime() > i.getLST()){
						//IF task is Regular, then its time window will become incoherent 
						if(rti.isRegular()) { rti.fail();}
						//Otherwise, remove optional task from Lambda
						else if(rti.isOptional()){
							setAsRemoval(rti, false);
							continue;
						}
						else{
							throw new SolverException("Eliminated tasks should not exist in a TL tree");
						}
					}else{
						assert!rti.isEliminated();
						updateManager.storeUpdate(rti,altDisjTreeTLTO.getTime());
					}
				}
				altDisjTreeTLTO.removeFromLambda(i);
			}
			break;
		case LST:
			while(altDisjTreeTLTO.getGrayTime() < j.getEST()){
				final IRTask rti = (IRTask) altDisjTreeTLTO.getResponsibleTask();
				final ITask i = rti.getHTask();
				if(altDisjTreeTLTO.getTime() < i.getLCT()){
					if(altDisjTreeTLTO.getTime() < i.getECT()){ 
						if(rti.isRegular()) { rti.fail();}
						else if(rti.isOptional()){
							setAsRemoval(rti, false);
							continue;
						}
						else{
							throw new SolverException("Eliminated tasks should not exist in a TL tree");
						}
					}
					else{
						assert ! rti.isEliminated();
						updateManager.storeUpdate(rti,altDisjTreeTLTO.getTime());
					}
				}
				altDisjTreeTLTO.removeFromLambda(i);
			}
			break;
		default:
			throw new UnsupportedOperationException("unknown tree mode.");
		}
	}

	private void checkTO(ITask j, TreeMode mode) throws ContradictionException
	{
		switch(mode){
		case ECT:
			while(altDisjTreeTLTO.getTOTime() > j.getLCT()){
				//get responsible optional task.
				final IRTask rti= (IRTask) altDisjTreeTLTO.getResponsibleTOTask();
				assert rti != null;
				//Remove optional task from OMEGA
				setAsRemoval(rti, true);
			}
			break;
		case LST:
			while(altDisjTreeTLTO.getTOTime() < j.getEST()){
				final IRTask rti = (IRTask) altDisjTreeTLTO.getResponsibleTOTask();
				assert rti != null;
				setAsRemoval(rti, true);
			}
			break;
		default:
			throw new UnsupportedOperationException("unknown tree mode.");
		}
	}

	private void setupEF(final TreeMode mode,final Comparator<IRTask> queueComp){
		//Initialise Edge Finding Tree
		this.altDisjTreeTLTO.initializeEdgeFinding(mode, this);
		rqueue.reset();
		this.rqueue.sort(queueComp);
	}

	@Override
	public boolean edgeFindingLCT()throws ContradictionException{
		//rqueue should be sorted in ascending order of EST.
		//While Tasks List should still be sorted in ascending order of EST.
		setupEF(LST, makeREarliestStartingTimeCmp());
		IRTask rtj=rqueue.peek();
		ITask j= rtj.getHTask();
		if(rtj.isRegular())
			if(altDisjTreeTLTO.getTime() < j.getEST()) {rtj.fail();}
		do{
			//Remove Task j from either Theta, or Omega, and insert it in Lambda
			if(rtj.isOptional()){
				altDisjTreeTLTO.removeFromOmegaAndInsertInLambda(rtj);
			}
			else if(rtj.isRegular()){
				altDisjTreeTLTO.removeFromThetaAndInsertInLambda(rtj);
			}
			rqueue.poll();
			if(!rqueue.isEmpty()) {
				rtj=rqueue.peek();
				j= rtj.getHTask();
			}
			else {break;}
			if(rtj.isRegular()){
				if(altDisjTreeTLTO.getTime() < j.getEST()) {rtj.fail();}
				checkTL(j, LST);
				checkTO(j, LST);
				checkFTLO(j, LST);
			}else if(rtj.isOptional()){
				checkSTLOLCT(rtj);
			}
		}while(!rqueue.isEmpty());
		return updateManager.fireAndUpdateLCT();
	}
 	
	@Override
	public Iterator<IRTask> iterator() {
		return new Itr();
	}

	final class Itr implements Iterator<IRTask> {

		private int level = 0;

		@Override
		public boolean hasNext() {
			return level < size.get();
		}

		@Override
		public IRTask next() {
			return rtasks[level++];
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("not available");
		}


	}

	final class AltBipartiteQueue<E> implements IBipartiteQueue<E> {

		public final E[] elementData;

		private int level;

		public AltBipartiteQueue(final E[] elementData) {
			super();
			this.elementData = Arrays.copyOf(elementData, elementData.length);
			this.reset();
		}

		public void reset() {
			level =0;
		}

		public boolean isEmpty() {
			return level == size.get();
		}

		public E poll() {
			return elementData[level++];
		}

		public E peek(){
			return elementData[level];
		}

		public void sort(final Comparator<? super E> cmp) {
			Arrays.sort(elementData, level,size.get(),cmp);
		}
	}
}

