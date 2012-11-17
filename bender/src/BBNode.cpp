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
		vector<int>* current_solution, int starttime, int k, int* nk, int capacity, int Dmax) {

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

	// update the current assignment:
	// put the jobs_in_batch into batch k.
	for(int j=0; j<jobs_in_batch.size(); j++) {
		(*current_solution)[jobs_in_batch[j]] = k;
	}
	cout << "Node created!" << endl;
}

BBNode::~BBNode() {
	// delete dynamically allocated stuff
	// right now, there's nothing here because it's all RootNode's business.
}

int BBNode::run() {
	cout << "Running a node on level " << k << endl;
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
		if((*pj)[j] > P_batch) P_batch = (*pj)[j];
		if((*dj)[j] < D_batch) D_batch = (*dj)[j];
	}
	Lmax_batch = D_batch - (starttime + P_batch);

	// find a lower bound on the lateness of the rest
	int Lmax_rest_interval;
	float moving_elastic_LB_enddate = starttime + P_batch;
	for(int j=0; j<jobs_in_rest.size(); j++) { // they're already sorted by due date
		moving_elastic_LB_enddate += (*pj)[jobs_in_rest[j]] * (*sj)[jobs_in_rest[j]] / capacity;
		if(moving_elastic_LB_enddate > (*dj)[jobs_in_rest[j]] + (*Lmax_incumbent) - 1) {
			// even with elastic EDD, we couldn't make the rest be less than the incumbent.
			// don't even bother with this node :(
			cout << "Didn't make it past elastic EDD:" << endl;
			cout << "jobs_in_rest[j=" << j << "] has dj=" << (int)(*dj)[jobs_in_rest[j]] << endl;
			cout << "Lmax_incumbent is " << *Lmax_incumbent << endl;
			cout << "current edd enddate is " << moving_elastic_LB_enddate << endl;
			return UNSUCCESSFUL_NODE;
		}
	}
	// create CP model and find cumulative Lmax of rest jobs -- this is a stricter test but takes longer.


	// if the result is bigger than the incumbent, do a return 0;

	if(jobs_in_rest.size() == 0) { // if this is a leaf node: is this a better solution?
		if(Lmax_batch < (*Lmax_incumbent)) {
			// update best_solution:
			(*best_solution) = (*current_solution);
			(*Lmax_incumbent) = Lmax_batch;
			cout << "Successful leaf node!" << endl;
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
		is_inbatch = IloNumVarArray(env, nj); // assumed ordering just like jobs_in_rest, values don't matter
		for(int j=0; j<nj; j++) {
			is_inbatch[j] = IloNumVar(env, 0, 1);
		}

		model.add(IloScalProd((*sj), is_inbatch) <= capacity);

		Dk = IloNumVar(env, 0, IloMax((*dj)));
		Pk = IloNumVar(env, 0, IloMax((*pj)));
		// define Dk
		for(int j=0; j<nj; j++) {
			model.add(IloRange(env, -IloInfinity, Dk - (*dj)[jobs_in_rest[j]]*is_inbatch[j] + Dmax*is_inbatch[j], Dmax));
		}
		// define Pk
		for(int j=0; j<nj; j++) {
			model.add(IloRange(env, 0, Pk - (*pj)[jobs_in_rest[j]]*is_inbatch[j], IloInfinity));
		}
		// make sure we're not trying things worse than the incumbent Lmax
		model.add( Pk - Dk <= (*Lmax_incumbent) );


		// add model objective here later

		IloCplex cplex(model);
		while(cplex.solve()) { // keep solving until there are no more children
			// create child and give it the MIP solution
			vector<int> child_jobs_in_batch, child_jobs_in_rest; // this is given to the child node to use

			for(int j=0; j<nj; j++) { // fill this with the MIP solution
				if(cplex.getValue(is_inbatch[j]) > 0) {
					child_jobs_in_batch.push_back((int) jobs_in_rest[j]);
				} else {
					child_jobs_in_rest.push_back((int) jobs_in_rest[j]);
				}

			}

			BBNode* child = new BBNode(sj, pj, dj, child_jobs_in_batch, child_jobs_in_rest, Lmax_incumbent, best_solution, current_solution, starttime + cplex.getValue(Pk), this->k + 1, nk, capacity, Dmax);

			child->run(); // let the child take care of things, wait until it's done.

			// remove solution from model and update the incumbent Lmax value
			IloNumExpr currentSolutionSum(env);
			for(int i=0; i<nj; i++) {
				if(cplex.getValue(is_inbatch[i]) > 0) currentSolutionSum += is_inbatch[i];
			}
			model.add( IloRange(env, 0, currentSolutionSum, child_jobs_in_batch.size() - 1));

			// kill child
			delete child;

			cplex.extract(model); // prepare for re-solving the MIP
		}

		return 1;
}

