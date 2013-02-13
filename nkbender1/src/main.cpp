#include <iostream>
#include <sstream>
#include <cmath>
#include <string>
#include <cstdlib>
#include <algorithm>
#include <vector>
#include <deque>

#include "Job.h"

using namespace std;

struct rjob{
	int s;
	int p;
	int d;
	int r;
	int pk;
	int dk;

	rjob() {};
	bool operator<(const rjob &b) const {
		return this->p > b.p;
		//return abs(this->pk - this->p) < abs(b.p - this->pk); // this may have an effect
	}
};


vector<Job> getSortedJobs(IloEnv env, string datafilename, int nj) {
	stringstream filename;
	if(datafilename.length() > 0) {
		filename << datafilename;
	} else {
		filename << "../../data/data_" << nj;
	}
	cout << "Loading file: " << filename.str() << endl;
	IloCsvReader csvr(env, filename.str().c_str());

    if(csvr.getNumberOfItems()-1 != nj) {
      cout << "Error: Expected " << nj << " jobs, but found " << csvr.getNumberOfItems() << " in CSV file. Aborting.\n";
      exit(0);
    }

    vector<Job> jc; // this structure is necessary to sort the jobs.

    IloCsvLine curLine;
    for(int j=0; j<nj; j++) {
      curLine = csvr.getLineByNumber(j+1);

      Job curLineJob((IloNum) curLine.getIntByPosition(0), (IloNum) curLine.getIntByPosition(1), (IloNum) curLine.getIntByPosition(2));
      jc.push_back(curLineJob);
    }

    // sort the jobs by non-decreasing due date
    sort(jc.begin(), jc.end());

    for(int j=0; j<nj; j++) cout << ((Job)jc[j]).d << endl;

    return jc;
}



vector<int> sortRemJ(vector<int> remJ, vector<Job> jc, int Dk, int Pk) {
	// sort the remJ vector based on

	vector<rjob> rjobs;
	for(int i=0; i<remJ.size(); i++) {
		rjob nj;
		nj.d = (int)(jc[remJ[i]].d);
		nj.p = (int)(jc[remJ[i]].p);
		nj.s = (int)(jc[remJ[i]].s);
		nj.r = remJ[i];
		nj.dk = Dk;
		nj.pk = Pk;

		rjobs.push_back(nj);
	}

	sort(rjobs.begin(), rjobs.end());

	vector<int> result;
	for(int i=0; i<rjobs.size(); i++) {
		result.push_back((rjobs[i]).r);
	}
	return result;
}


int main(int argc, char *argv[]) {

	// define Ilog stuff

	IloEnv env;

	// read in job lists
	int nj, njm;
	int LmaxLB = -IloInfinity;
	int LmaxUB = IloInfinity;
	string datafilename;

	int capacity = 20;
	//int Dmax = 2000;

    if(argc >= 2) {
    	nj = atoi(argv[1]);
    }

    if(argc >= 3) {
      njm = atoi(argv[2]);
      cout << "number of moves (njm): " << njm << endl;
    }

    if(argc >= 4) {
      LmaxLB = atoi(argv[3]);
      cout << "Min Lmax: " << LmaxLB << endl;
    }

    if(argc >= 5) {
      LmaxUB = atoi(argv[4]);
      cout << "Max Lmax: " << LmaxUB << endl;
    }

    if(argc >= 6) {
      datafilename = argv[5];
    }

    //int njm = 15;

    /*************** GET THIS PARTY STARTED! *****************/

    vector<Job> jc = getSortedJobs(env, datafilename, nj);
	// get the jobs into the MIP
	IloIntArray sj(env, jc.size());
	IloIntArray pj(env, jc.size());
	IloIntArray dj(env, jc.size());

	for(int j=0; j<jc.size(); j++) {
		Job jj = (jc)[j];
		sj[j] = jj.s; // I realize this can be done inline, but eclipse keeps whining
		pj[j] = jj.p;
		dj[j] = jj.d;
		cout << "Job " << j << ": due " << dj[j] << endl;
	}

	// find the single latenesses
	IloIntArray Lksingle(env, nj);
	int t = 0;
	for(int j=0; j<nj; j++) {
		t += pj[j];
		Lksingle[j] = t - dj[j];
	}
	int fromHereIgnore = nj-1;
	for(int j=nj-1; j>=0; j--) {
		if(Lksingle[j] == IloMax(Lksingle)) {
			fromHereIgnore = j+1;
			break;
		}
	}
	cout << "Ignore all jobs after " << fromHereIgnore << endl;

	int njfull = nj;
	nj = fromHereIgnore;

	IloNum Lmaxinc = IloMin(IloMax(Lksingle), LmaxUB);
	cout << "Lmaxinc before safe moves: " << Lmaxinc << endl;
	// create an safe-move upper bound on Lmax. This is important, possibly.

	vector<int> Bj(nj);
	vector<int> Pk(nj);
	int Lmaxincnew, Lmaxincbest = 0;
	int Lmaxinc_diff = IloInfinity;
		Lmaxincnew = -IloInfinity;

			cout << "Now using unsafety tolerance " << (Lmaxinc_diff > IloSum(pj) ? 0 : IloMax(Lmaxinc - 1,0)) << endl;

			vector<bool> xj(nj, true); // true = job is a host, false = job has been moved
			for(int j=0; j<nj; j++) {
				if(!xj[j]) continue; // moved jobs can't be hosts, try the next one.
				int csj = 0;
				Bj[j] = j;
				Pk[j] = pj[j];
				vector<int> remJ;
				for(int r=j+1; r<nj; r++) {
					if(pj[r] <= pj[j] + 1 && xj[r]) {
						remJ.push_back(r); // all remaining feasible guests
					}
				}
				if(remJ.size() > 0) {
					vector<int> cremJ(remJ.size());
					cremJ = sortRemJ(remJ, jc, (int)(dj[j]), (int)(pj[j]));

					for(int i=0; i<cremJ.size(); i++) {
						if(capacity - sj[j] - csj >= sj[cremJ[i]]) {
							xj[cremJ[i]] = false;
							csj += sj[cremJ[i]];
							Bj[cremJ[i]] = j;
							if (Pk[j] < pj[cremJ[i]]) Pk[j] = pj[cremJ[i]];
						}

						if (capacity == csj) break;
					}
				}
			}
			t = 0;

			for(int j=0; j<nj; j++) {
				if(xj[j]) {
					t += Pk[j];
					if(t - dj[j] > Lmaxincnew) Lmaxincnew = t - dj[j];
				}
			}
			cout << "New Lmaxincnew: " << Lmaxincnew << endl;
			if(Lmaxinc > Lmaxincnew) {
				Lmaxinc_diff = Lmaxinc - Lmaxincnew;
				Lmaxinc = Lmaxincnew;
				if(Lmaxincnew < Lmaxincbest) Lmaxincbest = Lmaxincnew;
			}
			cout << "Difference: " << Lmaxinc_diff << endl;


	cout << "Lmaxinc after safe moves: " << Lmaxinc << endl;

	cout << "Initial solution:" << endl;
	for(int j=0; j<nj; j++) cout << Bj[j] << " ";
	cout << endl;

	// find a lower bound on the completion time of job j.

	vector<int> lb_ct(nj);
	t = 0;
	for(int j=0; j<nj; j++) {
		t += ceil(pj[j] * sj[j] / capacity);
		lb_ct[j] = t;
	}

    // create MIP or CP to find the best jobs to move.
try {
	IloModel mip_cj(env);


	typedef IloArray<IloNumVarArray> xjh_matrix;

	// END XJ CONSTRAINTS
	// BEGIN XJH CONSTRAINTS

	// create xjh matrix (triangular)

	xjh_matrix xjh(env, nj);
	for(int j=0; j<nj; j++) {
		xjh[j] = IloNumVarArray(env, nj);
		for(int k=0; k<nj; k++) {
			xjh[j][k] = IloNumVar(env, 0, 1, ILOINT);
		}
	}


	/* total
	IloNumExpr totalsum(env);
	for(int j=0; j<nj; j++) totalsum += IloSum(xjh[j]);
	//mip_cj.add( totalsum == njm ); */

	// jobs can only be moved back
	for(int j=0; j<nj; j++) {
		for(int k=j; k<nj; k++) {
			mip_cj.add( xjh[j][k] == 0 );
		}
	}

	/* jobs after the single-EDD Lmax shouldn't be considered at all
	for(int j=fromHereIgnore; j<nj; j++) {
		for(int k=0; k<nj; k++) {
			//mip_cj.add( xjh[j][k] == 0);
		}
	}*/

	// moves that violate capacities by themselves shouldn't be considered at all
	// moves that violate single-EDD Lmaxinc deadlines shouldn't be considered at all

	for(int j=0; j<nj; j++) {
		for(int k=j; k<nj; k++) {
			if(sj[j] + sj[k] >= capacity + 1) mip_cj.add( xjh[j][k] == 0 );
			if((pj[j] - pj[k]) + (lb_ct[k]-dj[j]) >= Lmaxinc + 1) mip_cj.add( xjh[j][k] == 0);
		}
	}
	// if the unsafety margin + theoretical min lateness exceeds Lmaxinc + 1, don't consider.

	IloNumVarArray Pk(env, nj);
	// define Pk
	for(int k=0; k<nj; k++) {
		Pk[k] = IloNumVar(env, pj[k], IloMax(pj)+1, ILOFLOAT);
		for(int j=0; j<nj; j++) {
			mip_cj.add(Pk[k] >= pj[j]*xjh[j][k]);
		}
	}

	// define Lmax
	IloNumVar Lmax(env, LmaxLB, Lmaxinc, ILOINT);

	for(int k=0; k<nj; k++) {
		IloNumExpr c_sum(env);
		for(int ki=0; ki<=k; ki++) c_sum += Pk[ki] - pj[ki] + (-pj[ki] * IloSum(xjh[ki]));
		mip_cj.add( Lmax >= Lksingle[k] + c_sum - IloSum(xjh[k])*Lmaxinc); //
	}

	/* lower bound on Lmax
	for(int d=0; d<nj-1; d++) {
		if(dj[d] == dj[d+1]) continue;
		IloNumExpr areas(env);
		for(int j=0; j<=d; j++) areas += (sj[j]*pj[j]);
		for(int j=d+1; j<nj; j++) {
			IloNumExpr isInBucket(env);
			for(int kd=0; kd<=d; kd++) isInBucket += xjh[j][kd];
			areas += isInBucket * sj[j]*pj[j];
		}
		//mip_cj.add( Lmax >= areas/capacity - dj[d]);
	}*/


	// add that jobs can't be moved to where another job just left
	for(int j=0; j<nj; j++) {
		for(int k=0; k<nj; k++) {
			IloNumExpr xjhk_sum(env);
			for(int jo=0; jo<nj; jo++) xjhk_sum += xjh[k][jo];
			mip_cj.add( xjh[j][k] + xjhk_sum <= 1);
		}
	}

	// capacity constraint
	for(int k=0; k<nj; k++) {
		IloNumExpr s_sum(env);
		for(int j=0; j<nj; j++) s_sum += sj[j] * xjh[j][k];

		mip_cj.add( s_sum + sj[k] <= capacity );
	}

	// vertical constraint (if a job fits lengthwise safely and is single, it can't fit sizewise.)
	for(int j=0; j<nj; j++) {
		for(int k=0; k<j; k++) {
			IloNumExpr sizeInK(env);
			for(int i=k; i<nj; i++) sizeInK += sj[i] * xjh[i][k];
			mip_cj.add(IloIfThen(env, (Pk[k] >= pj[j] + Lmax && IloSum(xjh[j])<=0.1 && IloSum(xjh[k])<=0.1 ), capacity-( sj[k] + sizeInK ) + 1 <= sj[j]));
		}
	}

	// every job can only be moved into one other
	IloNumExpr batchxjs(env);
	for(int j=0; j<nj; j++) {
		mip_cj.add(IloSum(xjh[j]) <= 1);

		/*IloNumArray weights(env, nj);
		/for(int k=0; k<nj; k++) {
			weights[k] = (10000*IloMin(pj[j], pj[k])+(nj-k));
			cout << weights[k] << endl;
		}*/

		mip_cj.add(IloSOS1(env, xjh[j]));
	}

	/* find pairs of jobs that are mutually exclusive
	for(int k=0; k<nj; k++) {
		for(int j1=k+1; j1<nj; j1++) {
			for(int j2=j1+1; j2<nj; j2++) {
				if(sj[j1] + sj[j2] >= capacity - sj[k] + 1) { //
					mip_cj.add(xjh[j1][k] + xjh[j2][k] <= (1-IloSum(xjh[k])));
					mip_cj.add(xjh[j1][k] + xjh[j2][k] <= 1);
					IloNumVarArray sospair(env);
					sospair.add(xjh[j1][k]);
					sospair.add(xjh[j2][k]);
					mip_cj.add(IloSOS1(env, sospair));
				}
			}
		}
	}*/

	mip_cj.add( IloMinimize(env, Lmax) );

	IloCplex mip_cj_solver(mip_cj);

	// add initial solution

    //IloNumVarArray startVar(env);
    //IloNumArray startVal(env);
/*
    for (int j = 0; j < nj; ++j)
        for (int k = 0; k < nj; ++k) {
        	int start;
        	if(Bj[j] == k && j != k) {start = 1;} else {start = 0;}

      //      startVar.add(xjh[j][k]);
        //    startVal.add(start);
        }
    //mip_cj_solver.addMIPStart(startVar, startVal);

    //startVal.end();
    //startVar.end();
*/
	mip_cj_solver.setParam(IloCplex::ClockType, 1);
	mip_cj_solver.setParam(IloCplex::MIPDisplay , 3); // MIP node log display information
	mip_cj_solver.setParam(IloCplex::MIPInterval , 1); // Controls the frequency of node logging when the MIP display parameter is set higher than 1.
	mip_cj_solver.setParam(IloCplex::Threads, 1);

	//mip_cj_solver.setParam(IloCplex::NodeSel, IloCplex::DFS); // depth-first

	ifstream solfile("/tmp/mipsolutions");
	if(solfile) {
		//mip_cj_solver.readMIPStarts("/tmp/mipsolutions");
		mip_cj_solver.setParam(IloCplex::AdvInd, 1);
	}
	if(mip_cj_solver.solve()) {
		//mip_cj_solver.writeMIPStart("/tmp/mipsolutions");
	}
	cout << mip_cj_solver.getStatus() << endl;

	cout << "Lmax: " << mip_cj_solver.getValue(Lmax) << endl;
	cout << "We moved the following jobs: " << endl;
			    	for(int j=0; j<nj; j++) {
			    		int xjresult = 0;
			    		for(int k=0; k<nj; k++) if(mip_cj_solver.getValue(xjh[j][k]) > 0.1) {xjresult = k;
			    		cout << "Job " << j << " into batch ";
			    		cout << xjresult << endl;}
			    	}


	/*for(int k=0; k<nj; k++) {
		for(int j=0; j<nj; j++) {
			cout << "---" << endl;
			cout << "Job " << j << "'s host is before batch " << k << "?" << mip_cj_solver.getValue(isafterhost[j][k]) << endl;
			cout << "Total length of the host of job " << j << ":" << mip_cj_solver.getValue(PH[j]) << endl;
		}
	}*/

	for(int j=0; j<nj; j++) {
		int c_sum = 0;
		IloNumExpr sizeInK(env);
		for(int i=0; i<nj; i++) sizeInK += sj[i] * xjh[i][j];
		for(int ki=0; ki<=j; ki++) c_sum += mip_cj_solver.getValue(Pk[ki]) - pj[ki] + (-pj[ki] * mip_cj_solver.getValue(IloSum(xjh[ki])));
		cout << "Job " << j << ":\ts=" << sj[j] << "\tp=" << pj[j] << "\td=" << dj[j] << "\tL=(" << Lksingle[j] << "+" << c_sum << "-" << mip_cj_solver.getValue(IloSum(xjh[j]))*Lmaxinc << ")=" << Lksingle[j] + c_sum- mip_cj_solver.getValue(IloSum(xjh[j]))*Lmaxinc << "\tPk=" << mip_cj_solver.getValue(Pk[j]) << "\tTotal size:" << mip_cj_solver.getValue( sj[j] + sizeInK ) <<  endl;
	}

} catch(IloException& e ) {
	cout << "Error, dude!: " << e << endl;
}
    cout << "Closing modelling environment ..." << endl;
    env.end();

	cout << "We're done here. Thanks." << endl;
	return 0;
}
