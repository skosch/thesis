/*
 * RootNode.cpp
 *
 *  Created on: Nov 16, 2012
 *      Author: sebastian
 */

#include "RootNode.h"

using namespace std;

RootNode::RootNode(vector<Job> *all_jobs, int capacity, int nk, int Dmax) {
	this->all_jobs = all_jobs;
	this->capacity = capacity;
	if(nk == 0) {
		this->nk = this->all_jobs->size();
	} else {
		this->nk = nk;
	}
	this->Dmax = Dmax;

	model = IloModel(env);

	sj = IloNumArray(env, all_jobs->size());
	pj = IloNumArray(env, all_jobs->size());
	dj = IloNumArray(env, all_jobs->size());

	for(int j=0; j<all_jobs->size(); j++) {
		Job jj = (*all_jobs)[j];
		sj[j] = jj.s; // I realize this can be done inline, but eclipse keeps whining
		pj[j] = jj.p;
		dj[j] = jj.d;
	}

	Lmax_incumbent = 34;
	best_solution = vector<int>(all_jobs->size(), 0);
	current_solution = vector<int>(all_jobs->size(), 0);
}


int RootNode::run() {
	/* create MIP model to create a child from the jobs
	 * then keep going until we've explored all the children.
	 */


	int nj = all_jobs->size();

		/* create MIP model to create a child with a new batch from the rest jobs
		 * then keep going until we've explored all the children.
		 */
		model = IloModel(env);
		is_inbatch = IloIntVarArray(env, nj); // assumed ordering just like jobs_in_rest, values don't matter
		IloIntervalVarArray J(env, nj); // all jobs
		IloCumulFunctionExpr cumulResource(env,"cumulResource");
		IloIntVar Pk(env, 0, IloMax(pj));
		IloNumVar Lmax(env, 0, Lmax_incumbent);


		for(int j=0; j<nj; j++) {
			is_inbatch[j] = IloIntVar(env, 0, 1);
			J[j] = IloIntervalVar(env, (IloInt)sj[j]);
			cumulResource += IloPulse(J[j], (IloInt)sj[j]);

			model.add(IloIfThen(env,is_inbatch[j]==1, IloStartOf(J[j]) == 0));
			model.add(IloIfThen(env,is_inbatch[j]==0, IloStartOf(J[j]) >= Pk));
			model.add(Pk >= is_inbatch[j] * (IloNum)pj[j]);
			model.add(IloEndOf(J[j]) <= (IloNum)dj[j] + (Lmax_incumbent) - 1);
			model.add(Lmax >= IloEndOf(J[j]) - (IloNum)dj[j]);

		}

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
				{ dmin_in_batch = dj[j]; }
		}
		// make sure we're not trying things worse than the incumbent Lmax
		model.add( Pk - dmin_in_batch <= (Lmax_incumbent) -1);

		// always make the batch as least as full as there's room for evenly sized jobs.
		// except if
		model.add( IloSum(is_inbatch) >= 1);

		// make sure the batch contains at least one of the earliest jobs
		IloNumExpr forceEarliest(env);
		for(int j=0; j<nj; j++){
			if(dj[j] == dmin_in_batch) {
				forceEarliest += is_inbatch[j];
			}
		}

		model.add( forceEarliest >= 1 ); // batch must contain min due date.

		model.add(IloMinimize(env, Lmax));


		IloCP cp(model);

	cout << "Running root node." << endl;

	while(cp.solve()) { // keep solving until there are no more children

		cout << "Solving the root node" << endl;

		vector<int> child_jobs_in_batch, child_jobs_in_rest; // this is given to the child node to use

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

		child->run(); // let the child take care of things, wait until it's done.
		for(int i=0; i<child_jobs_in_batch.size(); i++) {
			current_solution[child_jobs_in_batch[i]] = 0;
		}

		// kill child
		delete child;
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

RootNode::~RootNode() {
	for(int i=0; i<additionalConstraints.size(); i++) {
		delete additionalConstraints[i];
	}
}
