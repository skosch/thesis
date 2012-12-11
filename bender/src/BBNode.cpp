/*
 * BBNode.cpp
 *
 *  Created on: Nov 16, 2012
 *      Author: sebastian
 */

#include "BBNode.h"

using namespace std;

BBNode::BBNode(IloNumArray *sj, IloNumArray *pj, IloNumArray* dj, vector<int> jobs_in_batch,
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
		IloNumVar Lmax(env, 0, (*Lmax_incumbent));


		for(int j=0; j<nj; j++) {
			is_inbatch[j] = IloIntVar(env, 0, 1);
			J[j] = IloIntervalVar(env, (IloInt)(*sj)[jobs_in_rest[j]]);
			cumulResource += IloPulse(J[j], (IloInt)(*sj)[jobs_in_rest[j]]);

			model.add(IloIfThen(env,is_inbatch[j]==1, IloStartOf(J[j]) == 0));
			model.add(IloIfThen(env,is_inbatch[j]==0, IloStartOf(J[j]) >= Pk));
			model.add(Pk >= is_inbatch[j] * (IloNum)(*pj)[jobs_in_rest[j]]);
			model.add(IloEndOf(J[j]) <= (IloNum)(*dj)[jobs_in_rest[j]] + (*Lmax_incumbent) - 1 - starttime);
			model.add(Lmax >= IloEndOf(J[j]) - (IloNum)(*dj)[jobs_in_rest[j]]);

		}

		for(int j=0; j<nj; j++) {
			IloNumExpr rest_areas(env);
			for(int i=0; i<=j; i++) {
				rest_areas += ((1 - is_inbatch[i]) * ((*pj)[jobs_in_rest[i]] * (*sj)[jobs_in_rest[i]] / capacity)); // yji[j][i] *
			}
			model.add( IloConstraint((*dj)[jobs_in_rest[j]] + (*Lmax_incumbent) - 1 - starttime >= Pk + rest_areas));
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
		model.add( IloSum(is_inbatch) >= 1);

		// make sure the batch contains at least one of the earliest jobs
		IloNumExpr forceEarliest(env);
		for(int j=0; j<nj; j++){
			if((*dj)[jobs_in_rest[j]] == dmin_in_batch) {
				forceEarliest += is_inbatch[j];
			}
		}

		model.add( forceEarliest >= 1 ); // batch must contain min due date.

		model.add(IloMinimize(env, Lmax));


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

			cout << "Just solved the MIP and propose sumS=" << cp.getObjValue() << " and Pk=" << cp.getValue(Pk) << ":";
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


		return 1;
}
