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

	// My Beck modification:

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

			int nt = 0; // number of time points

			// first, sort the jobs by due date, except for the longest job, which is added after.
			int maxp = 0;
			bool maxp_skipped = false; // pretend the batch is as long as the longest job, but add this only once
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
					model.add(  (t + (IloInt)pj[j]) * ujt[j][t] <=  (IloInt)dj[j] + Lmax_incumbent - 1 );
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
	double timeneeded = cplex.getCplexTime();
	while(cplex.solve()) { // keep solving until there are no more children
		timeneeded = cplex.getCplexTime() - timeneeded;
		timeCounter += timeneeded;

		cout << "Solving the root node" << endl;

		vector<int> child_jobs_in_batch, child_jobs_in_rest; // this is given to the child node to use

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

		child->run(); // let the child take care of things, wait until it's done.
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
