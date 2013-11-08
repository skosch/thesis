/*
 * RootNode.h
 *
 *  Created on: Nov 16, 2012
 *      Author: sebastian
 */

#ifndef ROOTNODE_H_
#define ROOTNODE_H_

#include <vector>
#include <cmath>
#include "Job.h"
#include "BBNode.h"

class RootNode {
public:
	RootNode(std::vector<Job> *all_jobs, int capacity, int nk = 0, int Dmax = 2000);
	virtual ~RootNode();

	int run();

private:
	// only for the root node
	std::vector<Job> *all_jobs;
	IloEnv env;
	IloModel model;
	IloIntVarArray is_inbatch;
	IloNumVar  Pk;
	std::vector<IloIntExpr*> additionalConstraints;
	// given to the children for them to use
	int nk, capacity;
	int Dmax;
	IloIntArray sj, pj, dj;
	int Lmax_incumbent;
	std::vector<int> best_solution;
	std::vector<int> current_solution;
	int nodesVisited = 0;
	double timeCounter = 0;
};

#endif /* ROOTNODE_H_ */
