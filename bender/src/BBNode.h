/*
 * BBNode.h
 *
 *  Created on: Nov 16, 2012
 *      Author: sebastian
 */

#ifndef BBNODE_H_
#define BBNODE_H_

#define SUCCESSFUL_NODE 1
#define UNSUCCESSFUL_NODE 0

#include <vector>
#include <iterator>
#include <algorithm>
#include "Job.h"

class BBNode {
public:
	BBNode(IloNumArray *sj, IloNumArray *pj, IloNumArray* dj, std::vector<int> jobs_in_batch, std::vector<int> jobs_in_rest, int *Lmax_incumbent, std::vector<int> *best_solution, std::vector<int> *current_solution, int starttime, int k, int *nk, int capacity, int Dmax);
	int run(); // returns 0 if was unsuccessful against incumbent, 1 if had children
	virtual ~BBNode();

private:
	IloNumArray *sj, *pj, *dj;
	std::vector<int> jobs_in_batch;
	std::vector<int> jobs_in_rest;

	int *Lmax_incumbent;
	std::vector<int>  *best_solution;
	std::vector<int>  *current_solution;

	IloEnv env;
	IloModel model;
	IloBoolVarArray is_inbatch;
	IloNumVar Pk;
	std::vector<IloIntExpr*> additionalConstraints;

	int starttime;
	int nj;
	int k; // the current batch being assigned. k_root=0, first level has k1 assigned, etc.
	int *nk; // the total number of batches, which may be able to the number of jobs.
	int capacity, Dmax;
};

#endif /* BBNODE_H_ */
