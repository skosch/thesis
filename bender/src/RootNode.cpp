/*
 * RootNode.cpp
 *
 *  Created on: Nov 16, 2012
 *      Author: sebastian
 *
 *
 *      SEE BBNode.cpp FOR MORE DOCUMENTATION
 *
 */

#include "RootNode.h"

using namespace std;

RootNode::RootNode(vector<Job> *all_jobs, int capacity, int nk, int Dmax) {

	this->all_jobs = all_jobs;
	this->capacity = capacity;
	if (nk == 0) {
		this->nk = this->all_jobs->size();
	} else {
		this->nk = nk;
	}
	this->Dmax = Dmax;

	sj = IloIntArray(env, all_jobs->size());
	pj = IloIntArray(env, all_jobs->size());
	dj = IloIntArray(env, all_jobs->size());

	for (int j = 0; j < all_jobs->size(); j++) {
		Job jj = (*all_jobs)[j];
		sj[j] = jj.s; // I realize this can be done inline, but eclipse keeps whining
		pj[j] = jj.p;
		dj[j] = jj.d;
	}

	Lmax_incumbent = 200;
	best_solution = vector<int>(all_jobs->size(), 0);
	current_solution = vector<int>(all_jobs->size(), 0);
}

#ifdef BENDER_MIP
int RootNode::run() {
	/* create MIP model to create a child from the jobs
	 * then keep going until we've explored all the children.
	 */
	cout << "Running root node." << endl;

	model = IloModel(env);

	is_inbatch = IloBoolVarArray(env, all_jobs->size());
	for(int j=0; j<all_jobs->size(); j++) {
		is_inbatch[j] = IloBoolVar(env, 0, 1);
	}

	model.add(IloScalProd(sj, is_inbatch) <= capacity);

	Pk = IloNumVar(env, 0, IloMax(pj));
	// define Pk
	for(int j=0; j<all_jobs->size(); j++) {
		model.add(IloRange(env, 0, Pk - pj[j]*is_inbatch[j], IloInfinity));
	}
	// make sure we're not trying things worse than the incumbent Lmax
	model.add( Pk - IloMin(dj) <= Lmax_incumbent - 1);

	model.add( IloSum(is_inbatch) >= 1);// min number of jobs in batch

	// make sure the batch contains at least one of the earliest jobs
	IloNumExpr forceEarliest(env);
	for(int j=0; j<all_jobs->size(); j++) {
		if(dj[j] == IloMin(dj)) {
			forceEarliest += is_inbatch[j];
		}
	}
	//model.add(forceEarliest);
	model.add(forceEarliest >= 1);

	for(int j=0; j<all_jobs->size(); j++) {
		IloNumExpr rest_areas(env);
		for(int i=0; i<=j; i++) {
			rest_areas += ((1 - is_inbatch[j]) * (pj[i] * sj[i] / capacity));
		}
		model.add( IloConstraint((dj)[j] + Lmax_incumbent - 1 >= Pk + rest_areas));
	}

	/********* time-indexed cumul constraint *********/

	IloIntVar Lmax_mip(env, 0, Lmax_incumbent);
	model.add(IloMinimize(env, Lmax_mip));
	// ** Find an upper bound on cumul nt:

	int nt = 0;// number of time points

	// first, sort the jobs by due date, except for the longest job, which is added after.
	int maxp = 0;
	bool maxp_skipped = false;// pretend the batch is as long as the longest job, but add this only once
	for(int j=0; j<all_jobs->size(); j++) {
		nt += pj[j];
		if(pj[j] > maxp) maxp = pj[j];
	}

	vector<int> st(nt,0);
	int lt=0, lj=0;
	while(lj < all_jobs->size()) {
		if((capacity - st[lt]) >= sj[lj]) {
			if(pj[lj] == maxp && !maxp_skipped) {
				maxp_skipped = true;
				lj++;
			} else {
				for(int llt=lt; llt<lt+pj[lj]; llt++) st[llt] += sj[lj];
				lj++;
			}
		}
		lt++;
	}
	//nt = lt + maxp + 1;

	IloArray<IloBoolVarArray> ujt(env, all_jobs->size());
	for(int j=0; j<all_jobs->size(); j++) {
		// initialize variables as variable objects in the model
		ujt[j] = IloBoolVarArray(env, nt);
	}

	for(int j=0; j<all_jobs->size(); j++) {
		model.add(IloSum( ujt[j]) == 1); // every job starts once

		for(int t=0; t<nt; t++) {
			// no job after its latest finish date
			model.add( (t + (IloInt)pj[j]) * ujt[j][t] <= (IloInt)dj[j] + Lmax_incumbent - 1 );
			model.add( Lmax_mip >= (t + (IloInt)pj[j]) * ujt[j][t] - (IloInt)dj[j]);
			// batched jobs start at 0, others after Pk
			model.add( ujt[j][0] == is_inbatch[j] );
		}
	}

	// cumulative constraint
	for(int j=0; j<all_jobs->size(); j++) {
		for(int t=0; t<nt; t++) {
			// first, generate inner sum over Tjt

			for(int tt= (t-pj[j] + 1 > 0 ? t-pj[j] + 1 : 0); tt <= t; tt++) {
				model.add( (IloInt)sj[j] * ujt[j][tt] <= capacity );
			}
		}
	}

	for(int i=0; i<all_jobs->size(); i++) {
		for(int j=0; j<all_jobs->size(); j++) {
			for(int t=1; t<pj[j]; t++) {
				model.add(ujt[i][t] <= (1 - is_inbatch[j]));
			}
		}
	}
	/* safe eliminations constraint */
	IloBoolVarArray longer_than_Pk(env, all_jobs->size());
	for(int j=0; j<all_jobs->size(); j++) {
		longer_than_Pk[j] = IloBoolVar(env);
		model.add(capacity - IloScalProd(sj, is_inbatch) <= (capacity * longer_than_Pk[j] + 1) * (IloNum)sj[j]);
		model.add(Pk + longer_than_Pk[j]*2* nt >= (IloNum)pj[j] + nt*is_inbatch[j]);
		model.add(Pk - (1-longer_than_Pk[j])*2* nt <= (IloNum)pj[j] -1 + nt*is_inbatch[j]);
	}

	IloCplex cplex(model);
	cplex.setOut(env.getNullStream());
	cplex.setError(env.getNullStream());

	cplex.setParam(IloCplex::ClockType, 1);
	cplex.setParam(IloCplex::Threads, 1);
	double timeneeded = cplex.getCplexTime();
	while(cplex.solve()) { // keep solving until there are no more children
		timeneeded = cplex.getCplexTime() - timeneeded;
		timeCounter += timeneeded;

		cout << "Solving the root node" << endl;

		vector<int> child_jobs_in_batch, child_jobs_in_rest;// this is given to the child node to use

		for(int j=0; j<all_jobs->size(); j++) { // fill this with the MIP solution
			if(cplex.getValue(is_inbatch[j]) > 0.1) {
				child_jobs_in_batch.push_back(j);
			} else {
				child_jobs_in_rest.push_back(j);
			}

		}

		IloIntExpr *currentSolutionSum = new IloIntExpr(env);
		additionalConstraints.push_back(currentSolutionSum);
		(*currentSolutionSum) *= 0;
		for(int i=0; i<all_jobs->size(); i++) {
			if(cplex.getValue(is_inbatch[i]) > 0.1) {
				(*currentSolutionSum) += is_inbatch[i];
			} else {
				(*currentSolutionSum) += (1-is_inbatch[i]);
			}
		}

		//cout << "CurSolSum: " << currentSolutionSum << endl;
		//	cout << "<= than: " << (int) child_jobs_in_batch.size() - 1 << endl;
		int nj = all_jobs->size();
		model.add( (*currentSolutionSum) <= nj - 1 );

		//cout << "Creating child" << endl;
		BBNode* child = new BBNode(&sj, &pj, &dj, child_jobs_in_batch, child_jobs_in_rest, &Lmax_incumbent, &best_solution, &current_solution, 0, 1, &nk, capacity, Dmax, &nodesVisited, &timeCounter);

		child->run();// let the child take care of things, wait until it's done.
		for(int i=0; i<child_jobs_in_batch.size(); i++) {
			current_solution[child_jobs_in_batch[i]] = 0;
		}

		// kill child
		delete child;

		timeneeded = cplex.getCplexTime();
	}
	cout << "Best known Lmax: " << Lmax_incumbent << endl;
	cout << "Best known solution: ";
	for(int j=0; j<all_jobs->size(); j++) {
		cout << best_solution[j] << " ";
	}
	cout << endl;
	cout << "Visited " << nodesVisited << " nodes." << endl;
	cout << timeCounter << endl;
	return 0;
}
#endif // BENDER_MIP
#ifdef BENDER_CP
int RootNode::run() {
	/* create MIP model to create a child from the jobs
	 * then keep going until we've explored all the children.
	 */

	int nj = all_jobs->size();

	/* create MIP model to create a child with a new batch from the rest jobs
	 * then keep going until we've explored all the children.
	 */
	try {
		model = IloModel(env);
		is_inbatch = IloIntVarArray(env, nj); // assumed ordering just like jobs_in_rest, values don't matter

		IloIntervalVarArray J(env, nj);// all jobs

		IloCumulFunctionExpr cumulResource(env,"cumulResource");
		IloIntVar Pk(env, 0, IloMax(pj));
		IloIntVar Lmax(env, 0, Lmax_incumbent);

		for(int j=0; j<nj; j++) {
			is_inbatch[j] = IloIntVar(env, 0, 1);
			J[j] = IloIntervalVar(env, (IloInt)pj[j]);
		}

		for(int j=0; j<nj; j++) {

			model.add(IloIfThen(env,is_inbatch[j]>=0.9, IloStartOf(J[j]) == 0));
			model.add(IloIfThen(env, IloStartOf(J[j]) == 0, is_inbatch[j] == 1));
			model.add(IloIfThen(env,is_inbatch[j]<=0.1, IloStartOf(J[j]) >= Pk));
			model.add(IloIfThen(env, IloStartOf(J[j]) >= Pk, is_inbatch[j] == 0));

			model.add(Pk >= is_inbatch[j] * (IloNum)pj[j]);
			J[j].setEndMax((IloNum)dj[j] + (Lmax_incumbent) - 1);

			model.add(Lmax >= IloEndOf(J[j]) - (IloNum)dj[j]);
			cumulResource += IloPulse(J[j], (IloInt)sj[j]);
			model.add ( IloIfThen(env, ((pj[j] <= Pk) && (is_inbatch[j] <= 0.1)), capacity - IloScalProd(sj, is_inbatch) <= sj[j] ));
		}
		cout << "got to here" << endl;

		model.add(IloAlwaysIn(env, cumulResource, 0, IloSum(pj), 0, capacity));
		for(int j=0; j<nj; j++) {
			IloNumExpr rest_areas(env);
			for(int i=0; i<=j; i++) {
				rest_areas += ((1 - is_inbatch[i]) * (pj[i] * sj[i] / capacity)); // yji[j][i] *
			}
			model.add( IloConstraint(dj[j] + (Lmax_incumbent) - 1 >= Pk + rest_areas));
		}

		IloNum dmin_in_batch = Dmax;
		for(int j=0; j<nj; j++) { // go through the jobs in rest and see what the min dj for the next batch is going to be
			if(dj[j] > 0 && dj[j] < dmin_in_batch)
			{	dmin_in_batch = dj[j];}
		}
		// make sure we're not trying things worse than the incumbent Lmax
		model.add( Pk - dmin_in_batch <= (Lmax_incumbent) -1);

		// make sure the batch contains at least one of the earliest jobs
		IloNumExpr forceEarliest(env);
		for(int j=0; j<nj; j++) {
			if(dj[j] == dmin_in_batch) {
				forceEarliest += is_inbatch[j];
			}
		}

		model.add( forceEarliest >= 1 ); // batch must contain min due date.

		//model.add(IloMinimize(env, Lmax));

		IloCP cp(model);

		cout << "Running root node." << endl;

		while(cp.solve()) { // keep solving until there are no more children

			cout << "Solving the root node" << endl;

			vector<int> child_jobs_in_batch, child_jobs_in_rest;// this is given to the child node to use

			for(int j=0; j<all_jobs->size(); j++) { // fill this with the MIP solution
				if(cp.getValue(is_inbatch[j]) > 0.1) {
					child_jobs_in_batch.push_back(j);
				} else {
					child_jobs_in_rest.push_back(j);
				}

			}

			IloIntExpr *currentSolutionSum = new IloIntExpr(env);
			additionalConstraints.push_back(currentSolutionSum);
			(*currentSolutionSum) *= 0;
			for(int i=0; i<all_jobs->size(); i++) {
				if(cp.getValue(is_inbatch[i]) > 0.1) {
					(*currentSolutionSum) += is_inbatch[i];
				} else {
					(*currentSolutionSum) += (1-is_inbatch[i]);
				}
			}

			//cout << "CurSolSum: " << currentSolutionSum << endl;
			//	cout << "<= than: " << (int) child_jobs_in_batch.size() - 1 << endl;
			int nj = all_jobs->size();
			model.add( (*currentSolutionSum) <= nj - 1 );

			//cout << "Creating child" << endl;
			BBNode* child = new BBNode(&sj, &pj, &dj, child_jobs_in_batch, child_jobs_in_rest, &Lmax_incumbent, &best_solution, &current_solution, 0, 1, &nk, capacity, Dmax, &nodesVisited, &timeCounter);

			child->run();// let the child take care of things, wait until it's done.
			for(int i=0; i<child_jobs_in_batch.size(); i++) {
				current_solution[child_jobs_in_batch[i]] = 0;
			}

			// kill child
			delete child;
		}
	} catch (IloException& e) {
		cout << "Error: " << e << endl;
	}
	cout << "Best known Lmax: " << Lmax_incumbent << endl;
	cout << "Best known solution: ";
	for(int j=0; j<all_jobs->size(); j++) {
		cout << best_solution[j] << " ";
	}
	cout << endl;
	cout << "Visited " << nodesVisited << " nodes." << endl;
	cout << timeCounter << endl;
	return 0;
}
#endif
RootNode::~RootNode() {
	for (int i = 0; i < additionalConstraints.size(); i++) {
		delete additionalConstraints[i];
	}
}

