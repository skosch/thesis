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

	Lmax_incumbent = 60;
	best_solution = vector<int>(all_jobs->size(), 0);
	current_solution = vector<int>(all_jobs->size(), 0);
}

RootNode::~RootNode() {
}

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

	//Dk = IloNumVar(env, 0, IloMax(dj));
	Pk = IloNumVar(env, 0, IloMax(pj));
	// define Dk
	//for(int j=0; j<all_jobs->size(); j++) {
	//	model.add(IloRange(env, -IloInfinity, Dk - dj[j]*is_inbatch[j] + Dmax*is_inbatch[j], Dmax));
	//}
	// define Pk
	for(int j=0; j<all_jobs->size(); j++) {
		model.add(IloRange(env, 0, Pk - pj[j]*is_inbatch[j], IloInfinity));
	}
	// make sure we're not trying things worse than the incumbent Lmax
	model.add( Pk - IloMin(dj) <= Lmax_incumbent );
//floor(capacity/IloMax(sj))
	model.add( IloSum(is_inbatch) >= 1); // min number of jobs in batch

	// make sure the batch contains at least one of the earliest jobs
	IloNumExpr forceEarliest(env);
	for(int j=0; j<all_jobs->size(); j++){
		if(dj[j] == IloMin(dj)) {
			cout << "added earliest jobs" << endl;
			forceEarliest += is_inbatch[j];
		}
	}
	//model.add(forceEarliest);
	model.add(forceEarliest >= 1);


	// add model objective here later
	model.add(IloMaximize(env, IloScalProd(sj, is_inbatch)));

	IloCplex cplex(model);
	while(cplex.solve()) { // keep solving until there are no more children
		cout << "Solving the root node" << endl;

		vector<int> child_jobs_in_batch, child_jobs_in_rest; // this is given to the child node to use

		for(int j=0; j<all_jobs->size(); j++) { // fill this with the MIP solution
			if(cplex.getValue(is_inbatch[j]) > 0) {
				child_jobs_in_batch.push_back(j);
			} else {
				child_jobs_in_rest.push_back(j);
			}

		}
		cout << "Removing solution" << endl;
		// remove solution from model and update the incumbent Lmax value
		IloNumExpr currentSolutionSum(env);
		for(int i=0; i<all_jobs->size(); i++) {
	//		cout << i << ",";
			if(cplex.getValue(is_inbatch[i]) > 0) currentSolutionSum += is_inbatch[i];
		}

/*		cout << "This is root node ";
		for(int j=0; j<all_jobs->size(); j++) {
			cout << (cplex.getValue(is_inbatch[j]) > 0 ? 1 : 0 ) << " ";
		}
		cout << endl;*/

//		cout << "Creating child" << endl;
		BBNode* child = new BBNode(&sj, &pj, &dj, child_jobs_in_batch, child_jobs_in_rest, &Lmax_incumbent, &best_solution, &current_solution, 0, 1, &nk, capacity, Dmax);

		child->run(); // let the child take care of things, wait until it's done.
		for(int i=0; i<child_jobs_in_batch.size(); i++) {
			current_solution[child_jobs_in_batch[i]] = 0;
		}
		cout << "Printing solution" << endl;
		for(int j=0; j<current_solution.size(); j++) {
			cout << cplex.getValue(is_inbatch[j]) << " ";
		}
		cout << "removing old solutions" << endl;
		model.add( currentSolutionSum <= (IloNum) (child_jobs_in_batch.size() - 1));

		// kill child
		//delete child;

		//cplex.extract(model); // prepare for re-solving the MIP
	}
	cout << "Best known Lmax: " << Lmax_incumbent << endl;
	cout << "Best known solution: ";
	for(int j=0; j<all_jobs->size(); j++) {
		cout << best_solution[j] << " ";
	}
	cout << endl;
	return 0;
}

