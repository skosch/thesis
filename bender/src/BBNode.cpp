/*
 * BBNode.cpp
 *
 *  Created on: Nov 16, 2012
 *      Author: sebastian
 */

#include "BBNode.h"

using namespace std;

BBNode::BBNode(IloIntArray *sj, IloIntArray *pj, IloIntArray* dj, vector<int> jobs_in_batch,
		vector<int> jobs_in_rest, int* Lmax_incumbent,
		vector<int>* best_solution,
		vector<int>* current_solution, int starttime, int k, int* nk, int capacity, int Dmax, int* nodesVisited, double* timeCounter) {
	//cout << "Creating child" << endl;
	// copy all parameters into variables
	this->sj = sj;
	this->pj = pj;
	this->dj = dj;
	this->jobs_in_batch = jobs_in_batch;
	this->jobs_in_rest = jobs_in_rest;
	this->Lmax_incumbent = Lmax_incumbent;
	this->best_solution = best_solution;
	this->current_solution = current_solution;
	this->k = k;
	this->nk = nk;
	this->capacity = capacity;
	this->Dmax = Dmax;
	this->nj = jobs_in_rest.size();
	this->starttime = starttime;
	this->nodesVisited = nodesVisited;
	this->timeCounter = timeCounter;

	(*nodesVisited)++;

	// update the current assignment:
	// put the jobs_in_batch into batch k.
	//cout << "Updating current solution" << endl;
	cout << "Now testing:";
	for(int j=0; j<jobs_in_batch.size(); j++) {
		(*current_solution)[(int) jobs_in_batch[j]] = k;
	}
	for(int j=0; j<(*current_solution).size(); j++) {
		cout << (*current_solution)[j] << " ";
	}
	cout << endl;
	//cout << "Node created!" << endl;
}

BBNode::~BBNode() {
	// delete dynamically allocated stuff
	// right now, there's nothing here because it's all RootNode's business.
	for(int i=0; i<additionalConstraints.size(); i++) {
		delete additionalConstraints[i];
	}
}

#ifdef BENDER_MIP
int BBNode::run() {
	//cout << "Running a node on level " << k << endl;
	/* Calculate L_max by calculating a lower bound on the lateness of the rest.
	 * We assume that the jobs that were given to be in the batch aren't worse
	 * than the incumbent because those assignments were filtered out by the
	 * MIP in the parent node.
	 */
	// find the lateness of the batch that was given to us
	int Lmax_batch;

	int P_batch = 0;
	int D_batch = Dmax;
	for(int j=0; j<jobs_in_batch.size(); j++) {
		if((*pj)[jobs_in_batch[j]] > P_batch) P_batch = (*pj)[jobs_in_batch[j]];
		if((*dj)[jobs_in_batch[j]] < D_batch) D_batch = (*dj)[jobs_in_batch[j]];
	}
	Lmax_batch = (starttime + P_batch) - D_batch;
	//cout << D_batch << " - (" << starttime << " + " << P_batch << ") = " << Lmax_batch << endl;
			//	cout << "Length of batch:" << P_batch << endl;
	// find a lower bound on the lateness of the rest
	//int Lmax_rest_interval;

	/*
	float moving_elastic_LB_enddate = starttime + P_batch;
	for(int j=0; j<jobs_in_rest.size(); j++) { // they're already sorted by due date
		// move one forward
		moving_elastic_LB_enddate += (*pj)[jobs_in_rest[j]] * (*sj)[jobs_in_rest[j]] / capacity;
		// only check this if we're at the end of a bucket (ie if the next job is due later, or the last job)
		//if((*dj)[jobs_in_rest[j]] < (*dj)[jobs_in_rest[ (j<jobs_in_rest.size()-1 ? j+1 : j) ]])
		if(moving_elastic_LB_enddate > (*dj)[jobs_in_rest[j]] + (*Lmax_incumbent) - 1) {
			return UNSUCCESSFUL_NODE;
		}
	}
	*/

	// create CP model and find cumulative Lmax of rest jobs -- this is a stricter test but takes longer.


	// if the result is bigger than the incumbent, do a return 0;

	if(nj == 0) { // if this is a leaf node: is this a better solution?
		//cout << "This is a leaf node! Yay" << endl;
		// time to find out how we did!
		// this is a quadratic time loop-in-loop that goes through all jobs and finds Lmax
		int Lmax_current = -IloInfinity;
		int startk = 0;
		for(int k=1; k <= *(max_element((*current_solution).begin(), (*current_solution).end())); k++) {
			int dk = Dmax;
			int pk = 0;
			for(int j=0; j<(*current_solution).size(); j++) {
				if((*current_solution)[j] == k) {
					if( (*dj)[j] < dk ) dk = (*dj)[j];
					if( (*pj)[j] > pk ) pk = (*pj)[j];
				}
			}
			if(Lmax_current < (pk + startk - dk)) Lmax_current = (pk + startk - dk);
			startk += pk;
		}


		if(Lmax_current < (*Lmax_incumbent)) {
			// update best_solution:
			(*best_solution) = (*current_solution);
			(*Lmax_incumbent) = Lmax_current;
			cout << "Successful leaf node! New Lmax: " << Lmax_current << endl;
			return SUCCESSFUL_NODE;
		} else { // well, we were so close :(
			cout << "Unsuccessful leaf node!" << endl;
			return UNSUCCESSFUL_NODE;
		}
	}


		/* create MIP model to create a child with a new batch from the rest jobs
		 * then keep going until we've explored all the children.
		 */
		model = IloModel(env);
		is_inbatch = IloIntVarArray(env, nj); // assumed ordering just like jobs_in_rest, values don't matter
		for(int j=0; j<nj; j++) {
			is_inbatch[j] = IloIntVar(env, 0, 1);
		}
		cout << "Here1" << endl;
		// batch_sj; consisting of all sizes of jobs in jobs_in_rest
		IloNumArray rest_sj(env);
		for(int j=0; j<nj; j++) {
			rest_sj.add((IloNum) (*sj)[jobs_in_rest[j]]);
		}
		model.add(IloScalProd((rest_sj), is_inbatch) <= capacity);

		IloNumVar Pk(env, 0, IloMax((*pj)));

		// define Pk
		for(int j=0; j<nj; j++) {
			model.add(IloRange(env, 0, Pk - (*pj)[jobs_in_rest[j]]*is_inbatch[j], IloInfinity));
		}

		IloNum dmin_in_batch = Dmax;
		for(int j=0; j<nj; j++) { // go through the jobs in rest and see what the min dj for the next batch is going to be
			if((*dj)[jobs_in_rest[j]] > 0 && (*dj)[jobs_in_rest[j]] < dmin_in_batch)
				{ dmin_in_batch = (*dj)[jobs_in_rest[j]]; }
		}
		// make sure we're not trying things worse than the incumbent Lmax
		model.add( starttime + Pk - dmin_in_batch <= (*Lmax_incumbent) -1);

		// always make the batch as least as full as there's room for evenly sized jobs.
		// except if
		model.add( IloSum(is_inbatch) >= 1) ; //IloMin( floor(capacity/IloMax(batch_sj)), nj)); // min number of jobs in batch

		// make sure the batch contains at least one of the earliest jobs
		IloNumExpr forceEarliest(env);
		for(int j=0; j<nj; j++){
			if((*dj)[jobs_in_rest[j]] == dmin_in_batch) {
	//			cout << "added earliest" << endl;
				forceEarliest += is_inbatch[j];
			}
		}

		model.add( forceEarliest >= 1 ); // batch must contain min due date.
		cout << "Here2" << endl;
		// always try and make the batch as full as possible.
		//model.add(IloMaximize(env, IloScalProd((rest_sj), is_inbatch)));
		//model.add(IloMaximize(env, IloSum(is_inbatch)));
		IloNumVar Lmax_mip(env, 0, (*Lmax_incumbent));
		model.add(IloMinimize(env, Lmax_mip));

		// My Beck modification:

		for(int j=0; j<nj; j++) {
			IloNumExpr rest_areas(env);
			for(int i=0; i<=j; i++) {
				rest_areas += ((1 - is_inbatch[i]) * ((*pj)[jobs_in_rest[i]] * (*sj)[jobs_in_rest[i]] / capacity)); // yji[j][i] *
			}
			model.add( IloConstraint((*dj)[jobs_in_rest[j]] + (*Lmax_incumbent) - 1 - starttime >= Pk + rest_areas));
		}



		/********* time-indexed cumul constraint *********/
		// ** Find an upper bound on cumul nt:

		int nt = 0; // number of time points

		// first, sort the jobs by due date, except for the longest job, which is added after.
		int maxp = 0;
		bool maxp_skipped = false; // pretend the batch is as long as the longest job, but add this only once
		for(int j=0; j<nj; j++) {
			nt += (*pj)[jobs_in_rest[j]];
			if((*pj)[jobs_in_rest[j]] > maxp) maxp = (*pj)[jobs_in_rest[j]];
		}

		vector<int> st(nt,0);
		int lt=0, lj=0;
		while(lj < nj) {
			if((capacity - st[lt]) >= (*sj)[jobs_in_rest[lj]]) {
				if((*pj)[jobs_in_rest[lj]] == maxp && !maxp_skipped) {
					maxp_skipped = true;
					lj++;
				} else {
					for(int llt=lt; llt<lt+(*pj)[jobs_in_rest[lj]]; llt++) st[llt] += (*sj)[jobs_in_rest[lj]];
					lj++;
				}
			}
			lt++;
		}
		nt = lt + maxp;

		IloArray<IloBoolVarArray> ujt(env, nj);
	/*	IloArray<IloBoolVarArray> vjt(env, nj);
		IloBoolVarArray tb(env, nt);*/
		for(int j=0; j<nj; j++) {
			// initialize variables as variable objects in the model
			ujt[j] = IloBoolVarArray(env, (nt));
			for(int t=0; t<nt; t++) {
				ujt[j][t] = IloBoolVar(env,0, (*dj)[jobs_in_rest[j]] + (*Lmax_incumbent) - 1);
			}
		}

	/*	for(int t=0; t<nt; t++) {
			tb[t] = IloBoolVar(env);
			model.add(t <= Pk + nt*(1-tb[t])); // forces tb=0 if t > Pk
			model.add(t + (nt * tb[t]) >= Pk + 1); // forces tb=1 if t <= Pk
		}*/

		for(int j=0; j<nj; j++) {
		model.add(IloSum( ujt[j]) == 1); // every job starts once

			for(int t=0; t<nt; t++) {
				// no job after its latest finish date
				model.add(  (starttime + t + (IloInt)(*pj)[jobs_in_rest[j]]) * ujt[j][t] <=  (IloInt)(*dj)[jobs_in_rest[j]] + (*Lmax_incumbent) - 1 );
				model.add( Lmax_mip >= (starttime + t + (IloInt)(*pj)[jobs_in_rest[j]]) * ujt[j][t] - (IloInt)(*dj)[jobs_in_rest[j]]);
				// batched jobs start at 0, others after Pk
				model.add( ujt[j][0] == is_inbatch[j] );
			}
		}


		// cumulative constraint
		for(int j=0; j<nj; j++) {
			for(int t=0; t<nt; t++) {
				// first, generate inner sum over Tjt

				for(int tt= (t-(*pj)[jobs_in_rest[j]] + 1 > 0 ? t-(*pj)[jobs_in_rest[j]] + 1 : 0); tt <= t; tt++) {
					model.add( (IloInt)(*sj)[jobs_in_rest[j]] * ujt[j][tt] <= capacity );
				}
			}
		}

		for(int i=0; i<nj; i++) {
			for(int j=0; j<nj; j++) {
				for(int t=1; t<(*pj)[jobs_in_rest[j]]; t++) {
					model.add(ujt[i][t] <= (1 - is_inbatch[j]));
				}
			}
		}

		/* safe eliminations constraint*/

		IloBoolVarArray longer_than_Pk(env, nj);
		for(int j=0; j<nj; j++) {
			longer_than_Pk[j] = IloBoolVar(env);
			model.add(capacity - IloScalProd(rest_sj, is_inbatch) <= (capacity * longer_than_Pk[j] + 1) * (IloNum)(*sj)[jobs_in_rest[j]]);
			model.add(Pk + longer_than_Pk[j]*2* nt >= (IloNum)(*pj)[jobs_in_rest[j]] + nt*is_inbatch[j]);
			model.add(Pk - (1-longer_than_Pk[j])*2* nt <= (IloNum)(*pj)[jobs_in_rest[j]] -1 + nt*is_inbatch[j]);
		}
		cout << "Here3" << endl;
		IloCplex cplex(model);
		cout << "Here4" << endl;
		//IloBool nextSolution = cplex.solve();
		//cplex.setOut(env.getNullStream()); // shut the fuck up, cplex
		//cplex.setError(env.getNullStream());

		//cplex.setParam(IloCplex::MIPEmphasis, IloCplex::MIPEmphasisFeasibility);

		// -======= before we solve: let's print what we know about the model =======

		cout << "Size of isinbatch:" << is_inbatch.getSize() << endl;

		cplex.setParam(IloCplex::ClockType, 1);
		double timeneeded = cplex.getCplexTime();

		while(cplex.solve()) { // keep solving until there are no more children
			cout << "Just solved again, status is: " << cplex.getStatus() << endl;
			//cout << "Last cursolsum is now " << cplex.getValue(additionalConstraints[additionalConstraints.size()-1]) << endl;
			timeneeded = cplex.getCplexTime() - timeneeded;
			(*timeCounter) += timeneeded;
			// create child and give it the MIP solution
			vector<int> child_jobs_in_batch(0), child_jobs_in_rest(0); // this is given to the child node to use

			for(int j=0; j<nj; j++) { // fill this with the MIP solution
				if(cplex.getValue(is_inbatch[j]) > 0.1) {
					child_jobs_in_batch.push_back((int) jobs_in_rest[j]);
				} else {
					//cout << "Pushed into rest" << endl;
					child_jobs_in_rest.push_back((int) jobs_in_rest[j]);
				}

			}

			cout << "Just solved the MIP and propose sumS=" << cplex.getObjValue() << " and Pk=" << cplex.getValue(Pk) << ":";
					for(int j=0; j<nj; j++) {
						cout << (cplex.getValue(is_inbatch[j])) << " ";
					}
					cout << endl << " and will now run the child node: " << endl;


			BBNode* child = new BBNode(sj, pj, dj, child_jobs_in_batch, child_jobs_in_rest, Lmax_incumbent, best_solution, current_solution, P_batch + starttime, this->k + 1, nk, capacity, Dmax, nodesVisited, timeCounter);

			child->run(); // let the child take care of things, wait until it's done.

			// remove solution from model and update the incumbent Lmax value
			IloIntExpr *currentSolutionSum = new IloIntExpr(env);
			additionalConstraints.push_back(currentSolutionSum);
			(*currentSolutionSum) *= 0;
			for(int i=0; i<nj; i++) {
				if(cplex.getValue(is_inbatch[i]) > 0.1) {
					(*currentSolutionSum) += (1-is_inbatch[i]);
				} else {
					if((*pj)[jobs_in_rest[i]] <= cplex.getValue(Pk))
					(*currentSolutionSum) += (is_inbatch[i]);
				}
			}
			for(int i=0; i<child_jobs_in_batch.size(); i++) {
				(*current_solution)[child_jobs_in_batch[i]] = 0;
			}

			model.add( (*currentSolutionSum) >= 1 ); // <= nj - 1
			model.add( Pk - dmin_in_batch <= (*Lmax_incumbent) ); //update this!
			//cplex.extract(model);
			// kill child
			delete child;
			timeneeded = cplex.getCplexTime();
		}


		return 1;
}
#endif

#ifdef BENDER_CP
int BBNode::run() {
	//cout << "Running a node on level " << k << endl;
	/* Calculate L_max by calculating a lower bound on the lateness of the rest.
	 * We assume that the jobs that were given to be in the batch aren't worse
	 * than the incumbent because those assignments were filtered out by the
	 * MIP in the parent node.
	 */

	if(nj == 0) { // if this is a leaf node: is this a better solution?
		//cout << "This is a leaf node! Yay" << endl;
		// time to find out how we did!
		// this is a quadratic time loop-in-loop that goes through all jobs and finds Lmax
		int Lmax_current = -IloInfinity;
		int startk = 0;
		for(int k=1; k <= *(max_element((*current_solution).begin(), (*current_solution).end())); k++) {
			int dk = Dmax;
			int pk = 0;
			for(int j=0; j<(*current_solution).size(); j++) {
				if((*current_solution)[j] == k) {
					if( (*dj)[j] < dk ) dk = (*dj)[j];
					if( (*pj)[j] > pk ) pk = (*pj)[j];
				}
			}
			if(Lmax_current < (pk + startk - dk)) Lmax_current = (pk + startk - dk);
			startk += pk;
		}


		if(Lmax_current < (*Lmax_incumbent)) {
			// update best_solution:
			(*best_solution) = (*current_solution);
			(*Lmax_incumbent) = Lmax_current;
			cout << "Successful leaf node! New Lmax: " << Lmax_current << endl;
			return SUCCESSFUL_NODE;
		} else { // well, we were so close :(
			cout << "Unsuccessful leaf node!" << endl;
			return UNSUCCESSFUL_NODE;
		}
	}


		/* create MIP model to create a child with a new batch from the rest jobs
		 * then keep going until we've explored all the children.
		 */
		model = IloModel(env);
		is_inbatch = IloIntVarArray(env, nj); // assumed ordering just like jobs_in_rest, values don't matter
		IloIntervalVarArray J(env, nj); // all jobs
		IloCumulFunctionExpr cumulResource(env,"cumulResource");
		IloIntVar Pk(env, 0, IloMax((*pj)));
		IloIntVar Lmax(env, 0, (*Lmax_incumbent));
		IloNumExprArray allBatchJobLengths(env);
		IloNumExprArray allJobLs(env);

		IloNum dmin_in_batch = Dmax;
				for(int j=0; j<nj; j++) { // go through the jobs in rest and see what the min dj for the next batch is going to be
					if((*dj)[jobs_in_rest[j]] > 0 && (*dj)[jobs_in_rest[j]] < dmin_in_batch)
						{ dmin_in_batch = (*dj)[jobs_in_rest[j]]; }
				}
				// make sure we're not trying things worse than the incumbent Lmax
				model.add( starttime + Pk - dmin_in_batch <= (*Lmax_incumbent) -1);
		IloNumArray rest_sj(env);
		for(int j=0; j<nj; j++) {
			rest_sj.add((IloNum) (*sj)[jobs_in_rest[j]]);
		}
		for(int j=0; j<nj; j++) {
			is_inbatch[j] = IloIntVar(env, 0, 1);
			J[j] = IloIntervalVar(env, (IloInt)(*pj)[jobs_in_rest[j]]);
		}
		for(int j=0; j<nj; j++) {
			is_inbatch[j] = IloIntVar(env, 0, 1);
			J[j] = IloIntervalVar(env, (IloInt)(*pj)[jobs_in_rest[j]]);

			model.add(IloIfThen(env,is_inbatch[j]>=0.9, IloStartOf(J[j]) == 0));
			model.add(IloIfThen(env,is_inbatch[j]<=0.1, IloStartOf(J[j]) >= Pk));
			model.add(IloIfThen(env, IloStartOf(J[j]) == 0, is_inbatch[j] == 1));
			model.add(IloIfThen(env, IloStartOf(J[j]) >= Pk, is_inbatch[j] == 0));

			J[j].setEndMax((IloNum)(*dj)[jobs_in_rest[j]] + (*Lmax_incumbent) - 1 - starttime);

			cumulResource += IloPulse(J[j], (IloInt)(*sj)[jobs_in_rest[j]]);

			allBatchJobLengths.add(is_inbatch[j] * (IloInt)(*pj)[jobs_in_rest[j]]);
			allJobLs.add(starttime + IloEndOf(J[j]) - (IloInt)(*dj)[jobs_in_rest[j]]);
			model.add ( IloIfThen(env, (((*pj)[jobs_in_rest[j]] <= Pk) && (is_inbatch[j] <= 0.1)), capacity - IloScalProd(rest_sj, is_inbatch) <= (*sj)[jobs_in_rest[j]] ));
		}
		model.add(Lmax == IloMax(allJobLs));
		model.add(Pk == IloMax(allBatchJobLengths));

		model.add(IloAlwaysIn(env, cumulResource, 0, IloSum(*pj), 0, capacity));

		for(int j=0; j<nj; j++) {
			IloNumExpr rest_areas(env);
			for(int i=0; i<=j; i++) {
				rest_areas += ((1 - is_inbatch[i]) * ((*pj)[jobs_in_rest[i]] * (*sj)[jobs_in_rest[i]] / capacity)); // yji[j][i] *
			}
			model.add( IloConstraint((*dj)[jobs_in_rest[j]] + (*Lmax_incumbent) - 1 - starttime >= Pk + rest_areas));
		}



		// always make the batch as least as full as there's room for evenly sized jobs.
		// except if
		model.add( IloSum(is_inbatch) >= 1);

		// make sure the batch contains at least one of the earliest jobs
		IloNumExpr forceEarliest(env);
		for(int j=0; j<nj; j++){
			if((*dj)[jobs_in_rest[j]] == dmin_in_batch) {
				forceEarliest += is_inbatch[j];
			}
		}

		model.add( forceEarliest >= 1 ); // batch must contain min due date.

		//model.add(IloMinimize(env, Lmax));


		IloCP cp(model);

		while(cp.solve()) { // keep solving until there are no more children
			//cout << "Last cursolsum is now " << cplex.getValue(additionalConstraints[additionalConstraints.size()-1]) << endl;

			// create child and give it the MIP solution
			vector<int> child_jobs_in_batch(0), child_jobs_in_rest(0); // this is given to the child node to use

			for(int j=0; j<nj; j++) { // fill this with the MIP solution
				if(cp.getValue(is_inbatch[j]) > 0.1) {
					child_jobs_in_batch.push_back((int) jobs_in_rest[j]);
				} else {
					//cout << "Pushed into rest" << endl;
					child_jobs_in_rest.push_back((int) jobs_in_rest[j]);
				}

			}

		//	cout << "Just solved the MIP and propose Lmaxcumul,lastlevel=" << cp.getObjValue() << " and Pk=" << cp.getValue(Pk) << ":";
					for(int j=0; j<nj; j++) {
						cout << (cp.getValue(is_inbatch[j])) << " ";
					}
					cout << endl << " and will now run the child node: " << endl;


			BBNode* child = new BBNode(sj, pj, dj, child_jobs_in_batch, child_jobs_in_rest, Lmax_incumbent, best_solution, current_solution, cp.getValue(Pk) + starttime, this->k + 1, nk, capacity, Dmax, nodesVisited, timeCounter);

			child->run(); // let the child take care of things, wait until it's done.

			// remove solution from model and update the incumbent Lmax value
			IloIntExpr *currentSolutionSum = new IloIntExpr(env);
			additionalConstraints.push_back(currentSolutionSum);
			(*currentSolutionSum) *= 0;
			for(int i=0; i<nj; i++) {
				if(cp.getValue(is_inbatch[i]) > 0.1) {
					(*currentSolutionSum) += (1-is_inbatch[i]);
				} else {
					(*currentSolutionSum) += (is_inbatch[i]);
				}
			}
			for(int i=0; i<child_jobs_in_batch.size(); i++) {
				(*current_solution)[child_jobs_in_batch[i]] = 0;
			}

			model.add( (*currentSolutionSum) >= 1 ); // <= nj - 1
			//model.add( Pk - dmin_in_batch <= (*Lmax_incumbent) ); //update this!
			//cplex.extract(model);
			// kill child
			delete child;
		}

		cout << "Returning to parent node." << endl;
		return 1;
}
#endif
