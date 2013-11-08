#include <iostream>
#include <sstream>
#include <cmath>
#include <string>
#include <cstdlib>
#include <algorithm>
#include <vector>
#include <deque>

#include "Job.h"
#define FUZZ 0.1

using namespace std;

typedef IloArray<IloBoolVarArray> xjh_matrix;
typedef IloArray<IloNumArray> xjh_nummatrix;

struct rjob {
	int s;
	int p;
	int d;
	int r;
	int pk;
	int dk;

	rjob() {
	}
	;
	bool operator<(const rjob &b) const {
		return this->p > b.p; //
		//return abs(this->pk - this->p) < abs(b.p - this->pk);
		// this or variations on it may be used to implement batch fit; changes are required in the rest of the code.
	}
};

vector<Job> getSortedJobs(IloEnv env, string datafilename, int nj) {
	stringstream filename;
	if (datafilename.length() > 0) {
		filename << datafilename;
	} else {
		filename << "../../data/data_" << nj;
	}
	cout << "Loading file: " << filename.str() << endl;
	IloCsvReader csvr(env, filename.str().c_str());

	if (csvr.getNumberOfItems() - 1 != nj) {
		cout << "Error: Expected " << nj << " jobs, but found "
				<< csvr.getNumberOfItems() << " in CSV file. Aborting.\n";
		exit(0);
	}

	vector<Job> jc; // this structure is necessary to sort the jobs.

	IloCsvLine curLine;
	for (int j = 0; j < nj; j++) {
		curLine = csvr.getLineByNumber(j + 1);

		Job curLineJob((IloNum) curLine.getIntByPosition(0),
				(IloNum) curLine.getIntByPosition(1),
				(IloNum) curLine.getIntByPosition(2));
		jc.push_back(curLineJob);
	}

	// sort the jobs by non-decreasing due date
	sort(jc.begin(), jc.end());

	for (int j = 0; j < nj; j++)
		cout << ((Job) jc[j]).d << endl;

	return jc;
}

vector<int> sortRemJ(vector<int> remJ, vector<Job> jc, int Dk, int Pk) {
	// quick and dirty routine to sort the remJ vector by whatever heuristic
	// and return the indices of the original vector in sorted order.
	// not actually necessary (but it used to be for other experiments)

	vector<rjob> rjobs;
	for (int i = 0; i < remJ.size(); i++) {
		rjob nj;
		nj.d = (int) (jc[remJ[i]].d);
		nj.p = (int) (jc[remJ[i]].p);
		nj.s = (int) (jc[remJ[i]].s);
		nj.r = remJ[i];
		nj.dk = Dk;
		nj.pk = Pk;

		rjobs.push_back(nj);
	}

	sort(rjobs.begin(), rjobs.end());

	vector<int> result;
	for (int i = 0; i < rjobs.size(); i++) {
		result.push_back((rjobs[i]).r);
	}
	return result;
}

// calculates a lower bound on the completion time of a set of jobs jc

float minCmax(vector<Job> jc, int index, int capacity, float smallerfactor) {

	int totalsize = 0;
	vector<int> remIndices;
	for (int j = 0; j <= index; j++) {
		totalsize += ((Job) jc[index]).s;
		remIndices.push_back(j);
	}
	int nk = ceil(totalsize / capacity);

	// go through all jobs sorted by length:
	vector<int> sortedIndices = sortRemJ(remIndices, jc, 0, 0);
	int curbatch = 0;
	totalsize = 0;
	float totallength = ((Job) jc[sortedIndices[0]]).p;
	for (int j = 0; j <= index && curbatch < nk; j++) {
		totalsize += ((Job) jc[sortedIndices[j]]).s;
		if (ceil(totalsize / capacity) > curbatch && curbatch < nk - 1) {
			curbatch++;
			totallength += ((Job) jc[sortedIndices[j]]).p;
		}
		if (curbatch >= nk)
			break;
	}
	return totallength / smallerfactor;
}

// quick macro to sum over xjh in both directions (batches and jobs), since IloSum only sums over batches
IloNumExpr xjksum(IloArray<IloBoolVarArray> xjk, int row, int firstindex = 0,
		int lastindex = -1, bool rowtocol = false) {
	if (rowtocol) {
		lastindex = (lastindex == -1 ? xjk.getSize() - 1 : lastindex);
		IloNumExpr result(xjk.getEnv());
		for (int i = firstindex; i <= lastindex; i++)
			result += xjk[i][row];
		return result;
	} else {
		lastindex = (lastindex == -1 ? xjk[0].getSize() - 1 : lastindex);
		IloNumExpr result(xjk.getEnv());
		for (int i = firstindex; i <= lastindex; i++)
			result += xjk[row][i];
		return result;
	}

}

int main(int argc, char *argv[]) {

	// define Ilog stuff
	float smallerfactor = 10.0; // no longer used
	IloEnv env;

	// read in job lists
	int nj;
	float njm;
	int LmaxLB = -IloInfinity;
	int LmaxUB = IloInfinity;
	string datafilename;

	int capacity = 10;
	//int Dmax = 2000;

	if (argc >= 2) {
		nj = atoi(argv[1]);
	}

	if (argc >= 3) {
		njm = atoi(argv[2]);
		cout << "number of moves (njm): " << njm << endl;
	}

	if (argc >= 4) {
		LmaxLB = atoi(argv[3]);
		cout << "Min Lmax: " << LmaxLB << endl;
	}

	if (argc >= 5) {
		LmaxUB = atoi(argv[4]);
		cout << "Max Lmax: " << LmaxUB << endl;
	}

	if (argc >= 6) {
		datafilename = argv[5];
	}

	/*************** Get this party started ... model stuff: *****************/

	vector<Job> jc = getSortedJobs(env, datafilename, nj);
	// get the jobs into the MIP
	IloIntArray sj(env, jc.size());
	IloNumArray pj(env, jc.size());
	IloNumArray dj(env, jc.size());

	for (int j = 0; j < jc.size(); j++) {
		Job jj = (jc)[j];
		sj[j] = jj.s; // I realize this can be done inline, but eclipse keeps whining
		pj[j] = (jj.p);
		dj[j] = (jj.d);
		cout << "Job " << j << ": due " << dj[j] << endl;
	}

	// find the single-EDD lateness values

	IloNumArray Lksingle(env, nj);
	float t = 0;
	for (int j = 0; j < nj; j++) {
		t += pj[j];
		Lksingle[j] = t - dj[j];
	}
	int fromHereIgnore = nj - 1;
	for (int j = nj - 1; j >= 0; j--) { // find the latest job that has Lksingle = max(Lksingle)
		if (Lksingle[j] == IloMax(Lksingle)) {
			fromHereIgnore = j + 1;
			break;
		}
	}
	cout << "Ignore all jobs after " << fromHereIgnore << endl;

	//int njfull = nj;
	nj = fromHereIgnore;

	/****** The following code used to calculate an upper bound on Lmax. It works well, but it isn't actually used in the model right now. *****/

	IloNum Lmaxinc = IloMin(IloMax(Lksingle), LmaxUB);
	cout << "Lmaxinc before safe moves: " << Lmaxinc << endl;

	vector<int> Bj(nj);
	vector<int> Pk(nj);
	int Lmaxincnew, Lmaxincbest = 0;
	int Lmaxinc_diff = IloInfinity;
	Lmaxincnew = -IloInfinity;

	cout << "Now using unsafety tolerance "
			<< (Lmaxinc_diff > IloSum(pj) ? 0 : IloMax(Lmaxinc - 1, 0)) << endl;

	vector<bool> xj(nj, true); // true = job is a host, false = job has been moved
	for (int j = 0; j < nj; j++) {
		if (!xj[j])
			continue; // moved jobs can't be hosts, try the next one.
		int csj = 0;
		Bj[j] = j;
		Pk[j] = pj[j];
		vector<int> remJ;
		for (int r = j + 1; r < nj; r++) {
			if (pj[r] <= pj[j] + 1 && xj[r]) {
				remJ.push_back(r); // all remaining feasible guests
			}
		}
		if (remJ.size() > 0) {
			vector<int> cremJ(remJ.size());
			cremJ = sortRemJ(remJ, jc, (int) (dj[j]), (int) (pj[j]));

			for (int i = 0; i < cremJ.size(); i++) {
				if (capacity - sj[j] - csj >= sj[cremJ[i]]) {
					xj[cremJ[i]] = false;
					csj += sj[cremJ[i]];
					Bj[cremJ[i]] = j;
					if (Pk[j] < pj[cremJ[i]])
						Pk[j] = pj[cremJ[i]];
				}

				if (capacity == csj)
					break;
			}
		}
	}
	t = 0;

	for (int j = 0; j < nj; j++) {
		if (xj[j]) {
			t += Pk[j];
			if (t - dj[j] > Lmaxincnew)
				Lmaxincnew = t - dj[j];
		}
	}
	cout << "New Lmaxincnew: " << Lmaxincnew << endl;
	if (Lmaxinc > Lmaxincnew) {
		Lmaxinc_diff = Lmaxinc - Lmaxincnew;
		Lmaxinc = Lmaxincnew;
		if (Lmaxincnew < Lmaxincbest)
			Lmaxincbest = Lmaxincnew;
	}
	cout << "Difference: " << Lmaxinc_diff << endl;

	//Lmaxinc *= Lmaxinc;
	cout << "Lmaxinc after safe moves: " << Lmaxinc * Lmaxinc << endl;

	cout << "Initial solution:" << endl;
	for (int j = 0; j < nj; j++)
		cout << Bj[j] << " ";
	cout << endl;

	/**** find a lower bound on the completion time of job j. Also not used in the model right now. *****/

	vector<float> lb_ct(nj);
	t = 0;
	for (int j = 0; j < nj; j++) {
		t += ceil(pj[j] * sj[j] / capacity);
		lb_ct[j] = (float) minCmax(jc, j, capacity, smallerfactor);
		//cout << "Min Lateness of job " << j << " is " << lb_ct[j] << " instead of " << t << endl;
	}

	/*********** HERE IS THE ACTUAL MODEL ************/
	try {
		IloModel mip_cj(env);

		// create xjh matrix (triangular)

		xjh_matrix xjh(env, nj);
		for (int j = 0; j < nj; j++) {
			xjh[j] = IloBoolVarArray(env, nj);
			for (int k = 0; k < nj; k++) {
				xjh[j][k] = IloBoolVar(env);
			}
		}

		// jobs can only be moved back
		for (int j = 0; j < nj; j++) {
			for (int k = j + 1; k < nj; k++) {
				mip_cj.add(xjh[j][k] == 0);
			}
		}

		/* jobs after the single-EDD Lmax shouldn't be considered at all */
		for (int j = fromHereIgnore; j < nj; j++) {
			mip_cj.add(xjh[j][j] == 1);
		}

		IloNumVarArray Pk(env, nj);
		// define Pk
		for (int k = 0; k < nj; k++) {
			Pk[k] = IloNumVar(env, pj[k], IloMax(pj) + 1, ILOFLOAT);
			for (int j = k; j < nj; j++) {
				mip_cj.add(Pk[k] >= pj[j] * xjh[j][k]);
			}
		}

		/***** Lower bound on Lmax: also not used in the model right now, but it could be again in the future? ******/
		float Cmax_LB_temp = 0;
		for (int j = 0; j < nj; j++) {
			Cmax_LB_temp += pj[j] * sj[j] / capacity;
			if (j < nj - 1) {
				if (dj[j + 1] == dj[j]) {
					cout << "same duedate: " << j << endl;
					continue;
				} // still the same bucket
			}
			// new bucket, update Lmax_LB if necessary
			if (Cmax_LB_temp - dj[j] > LmaxLB) {
				LmaxLB = Cmax_LB_temp - dj[j];
				cout << "Updating LmaxLB to " << LmaxLB << ". C="
						<< Cmax_LB_temp << ", d=" << dj[j];

			}
		}
		LmaxLB = 0;
		cout << "Lower bound on Lmax based on areas:" << LmaxLB << endl;
		cout << "Max Lksingle:" << IloMax(Lksingle) << endl;
		cout << "Max reduction:" << IloMax(Lksingle) - LmaxLB << endl;

		// defintion of Lmax

		IloNumVar Lmax(env, LmaxLB, Lmaxinc, ILOFLOAT);
		for (int k = 0; k < nj; k++) {
			IloNumExpr c_sum(env);
			if (k == nj - 1 or dj[k + 1] > dj[k]) {
				for (int ki = 0; ki <= k; ki++)
					c_sum += Pk[ki] - pj[ki] + (-pj[ki] * (1.0 - xjh[ki][ki]));
				mip_cj.add(Lmax >= Lksingle[k] + c_sum);
			}
		}

		// jobs can't be moved to where another job just left
		for (int j = 0; j < nj; j++) {
			for (int k = 0; k < j; k++) {
				mip_cj.add(xjh[j][k] <= xjh[k][k]);
			}
		}

		// capacity constraint
		for (int k = 0; k < nj; k++) {
			IloNumExpr s_sum(env);
			for (int j = 0; j < nj; j++)
				s_sum += sj[j] * xjh[j][k];
			mip_cj.add(s_sum <= capacity);
		}

		// every job can only be moved into one other batch, or stay home
		IloNumExpr batchxjs(env);
		for (int j = 0; j < nj; j++) {
			mip_cj.add(IloSum(xjh[j]) == 1);
		}

		// add psi-2 dominance rules:
		mip_cj.add(IloMinimize(env, Lmax));

		IloCplex mip_cj_solver(mip_cj);
		/**/
		cout << "Now adding static psi-2-in-2 dominance rules ..." << endl;
		if (nj >= 30) {
			for (int j1 = 0; j1 < nj - 1; j1++) {
				for (int j2 = j1 + 1; j2 < nj; j2++) {
					for (int k1 = 0; k1 < j1 - 1; k1++) {
						for (int k2 = k1 + 1; k2 < j1; k2++) {
							// order now: k1, k2, j1, j2
							if (pj[j1] > pj[k1] or pj[j2] > pj[k1]
									or pj[j1] > pj[k2] or pj[j2] > pj[k2])
								continue;
							if (capacity - sj[k1] < sj[j1]
									or capacity - sj[k1] < sj[j2]
									or capacity - sj[k2] < sj[j1]
									or capacity - sj[k2] < sj[j2])
								continue;
							IloNumExpr nsumk1(env);

							for (int j = 0; j < nj; j++) {
								if (j == j1 or j == j2 or j == k1 or j == k2)
									continue;
								nsumk1 += xjh[j][k1] + xjh[j][k2];

							}
							// if k1 and k2 are hosts, and j1 and j2 are in k1 and k2, and
							mip_cj_solver.addLazyConstraint(
									2
											* ((1 - xjh[k1][k1])
													+ (1 - xjh[k2][k2])
													+ (2 - xjh[j1][k1]
															- xjh[j1][k2]
															- xjh[j2][k1]
															- xjh[j2][k2])
													+ nsumk1)
											>= xjh[j1][k2] + xjh[j2][k1]);
						}
					}
				}
			}
		}
		// moves that violate capacities by themselves shouldn't be considered at all
		// moves that violate single-EDD Lmaxinc deadlines shouldn't be considered at all

		// safe move dominance
		int safemovecounter = 0;
		for (int k = 0; k < nj; k++) {
			for (int j1 = k + 1; j1 < nj; j1++) {
				for (int j2 = j1 + 1; j2 < nj; j2++) {
					if ((sj[j1] <= sj[j2]) && sj[j2] + sj[k] <= capacity
							&& pj[j1] <= pj[k] && pj[j1] >= pj[j2]) {
						mip_cj_solver.addLazyConstraint(
								(1 - xjh[j1][j1]) >= xjh[j2][k]);
						if (sj[j1] + sj[j2] + sj[k] > capacity) {
							safemovecounter++;
						}
						safemovecounter++;
					}
				}
			}
		}
		cout << "Added " << safemovecounter
				<< " safe move dominance lazy constraints" << endl;

		// safe move requirement
		if (nj >= 40) { // only run these for instances > 40 jobs as of now
			int safemovereqcounter = 0;
			for (int k = 0; k < nj; k++) {
				for (int j = k + 1; j < nj; j++) {

					if (pj[k] >= pj[j] && sj[k] + sj[j] <= capacity) {
						IloNumExpr sumInKnoj(env);
						for (int i = k; i < nj; i++)
							if (i != j)
								sumInKnoj += sj[i] * xjh[i][k];
						mip_cj_solver.addLazyConstraint(
								(1 - xjh[j][j]) + (1 - xjh[k][k])
										>= (capacity - sj[j] - sumInKnoj + 1.0)
												/ capacity);
						safemovereqcounter++;
					}

				}
			}
			cout << "Added " << safemovereqcounter
					<< " safe move requirement lazy constraints" << endl;
		}

		mip_cj_solver.setParam(IloCplex::ClockType, 1);
		mip_cj_solver.setParam(IloCplex::MIPDisplay, 2); // MIP node log display information
		mip_cj_solver.setParam(IloCplex::MIPInterval, 100); // Controls the frequency of node logging when the MIP display parameter is set higher than 1.
		mip_cj_solver.setParam(IloCplex::Threads, 1);
		mip_cj_solver.setParam(IloCplex::TiLim, 3600);
		mip_cj_solver.setParam(IloCplex::TuningTiLim, 100);
		mip_cj_solver.solve();

		cout << mip_cj_solver.getStatus() << endl;

		/********** output results ***********/

		cout << "Lmax: " << mip_cj_solver.getValue(Lmax) << endl;
		cout << "We moved the following jobs: " << endl;
		for (int j = 0; j < nj; j++) {
			int xjresult = 0;
			for (int k = 0; k < nj; k++)
				if (mip_cj_solver.getValue(xjh[j][k]) > 0.1) {
					xjresult = k;
					cout << "Job " << j << " into batch ";
					cout << xjresult << endl;
				}
		}

		for (int j = 0; j < nj; j++) {
			int c_sum = 0;
			IloNumExpr sizeInK(env);
			for (int i = 0; i < nj; i++)
				sizeInK += xjh[i][j] * sj[i];
			for (int ki = 0; ki <= j; ki++)
				c_sum += mip_cj_solver.getValue(Pk[ki]) - pj[ki]
						+ (-pj[ki] * mip_cj_solver.getValue(1.0 - xjh[ki][ki]));
			cout << "Job " << j << ":\ts=" << sj[j] << "\tp=" << pj[j] << "\td="
					<< dj[j] << "\tL=(" << Lksingle[j] << "+" << c_sum << "-"
					<< mip_cj_solver.getValue(1.0 - xjh[j][j]) * Lmaxinc << ")="
					<< Lksingle[j] + c_sum
							- mip_cj_solver.getValue(1.0 - xjh[j][j]) * Lmaxinc
					<< "=" /*<< mip_cj_solver.getValue(Lk[j])*/<< "\t\t\tPk="
					<< mip_cj_solver.getValue(Pk[j]) << "\tTotal size:"
					<< mip_cj_solver.getValue(sizeInK) << endl;
		}
		cout << endl;

		// find average number of jobs per batch
		int numberofbatches = 0;
		for (int k = 0; k < nj; k++) {
			int jobs_in_k = mip_cj_solver.getValue(xjksum(xjh, k, 0, -1, true));
			if (jobs_in_k > 0)
				numberofbatches++;
		}

		cout << nj * 1.0 / numberofbatches << endl;
		cout << mip_cj_solver.getValue(Lmax) << endl;
		cout << mip_cj_solver.getTime() << " seconds" << endl;
	} catch (IloException& e) {
		cout << "Error!: " << e << endl;
	}

	env.end();

	return 0;
}
