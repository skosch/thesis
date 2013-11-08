#include <iostream>
#include <sstream>
#include <cmath>
#include <string>
#include <cstdlib>
#include <algorithm>
#include <vector>
#include <deque>
#include <ilcplex/ilocplex.h>
#include <ilcp/cp.h>
#include <ilconcert/ilocsvreader.h>
#include <ilconcert/iloexpression.h>
#define FUZZ 0.1

using namespace std;

typedef IloArray<IloBoolVarArray> xjk_matrix;
typedef IloArray<IloNumVarArray> fjk_matrix;
// read in job lists
int nj;
int nk;
int ttc;
int ttl;

vector<int> getSortedJobs(IloEnv env, string datafilename) {
	IloCsvReader csvr(env, datafilename.c_str());

	vector<int> jc; // this structure is necessary to sort the jobs.

	IloCsvLine curLine;
  nj = csvr.getLineByNumber(0).getIntByPosition(0);
  nk = csvr.getLineByNumber(1).getIntByPosition(0);
  ttc = csvr.getLineByNumber(2).getIntByPosition(0);
  ttl = csvr.getLineByNumber(3).getIntByPosition(0);

  cout << "Problem: nj=" << nj << " nk=" << nk << " ttc=" << ttc << " ttl=" << ttl << endl;
  for (int j=4; j<4+nj; j++) {
		jc.push_back(csvr.getLineByNumber(j).getIntByPosition(0));
	}

	// sort the jobs by non-decreasing due date
	sort(jc.begin(), jc.end());
  for(int j=0; j<nj; j++) cout << jc[j] << endl;
	return jc;
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
	IloEnv env;
	string datafilename;

	if (argc >= 2) {
		datafilename = argv[1];
	}
  vector<int> pj = getSortedJobs(env, datafilename);

	/*************** Get this party started ... model stuff: *****************/
  IloNumVar Csum(env);

  /*********** HERE IS THE ACTUAL MODEL ************/
	try {
		IloModel mip_cj(env);

		// create xjk matrix
		xjk_matrix xjk(env, nj);
		for (int j = 0; j < nj; j++) {
			xjk[j] = IloBoolVarArray(env, nk);
			for (int k = 0; k < nk; k++) {
				xjk[j][k] = IloBoolVar(env);
			}
		}

		// create fjk matrix
		fjk_matrix fjk(env, nj);
		for (int j = 0; j < nj; j++) {
			fjk[j] = IloNumVarArray(env, nk);
			for (int k = 0; k < nk; k++) {
				fjk[j][k] = IloNumVar(env);
			}
		}

    // define fjk
		for(int j = 0; j < nj; j++) {
			for(int k = 0; k < nk; k++) {
        // must be greater than zero.
        mip_cj.add(fjk[j][k] >= 0);
        // for the actual minimum, first calc sum of slots before
        IloNumExpr beforeSum(env);
        int beforecounter = 0;
        for(int h=0; h < k; h++) {
          for(int i=0; i < nj; i++) {
            beforeSum += xjk[i][h]; // before in earlier batches
          }
        }
        for(int i=0; i<j; i++) {
          beforeSum += xjk[i][k]; // before in this batch
        }
        // now define fjk
        mip_cj.add(fjk[j][k] >= nj - beforeSum - nj*(1 - xjk[j][k]));
			}
		}
    
    // make sure there's only one of each, and batches don't become too long
		for (int j = 0; j < nj; j++) {
      mip_cj.add(xjksum(xjk,j, 0, -1, false) == 1);
		}
    for (int k = 0; k < nk; k++) {
      IloNumExpr batchLength(env);
      for (int j = 0; j < nj; j++) {
        batchLength += xjk[j][k] * pj[j];
      }
      mip_cj.add(batchLength <= ttl);
    }

		// calculate final answer
    IloNumExpr totalBatchC(env);
		for (int j = 0; j < nj; j++) {
        totalBatchC += IloSum(fjk[j]) * pj[j];
		}
    IloNumExpr totalTtcC(env);
    for(int k=0; k<nk; k++) {
      // find number of jobs in later batches
      IloNumExpr laterJobs(env);
      for(int h=k+1; h<nk; h++) {
        for(int i=0; i<nj; i++) {
          laterJobs += xjk[i][h];
        }
      }

      totalTtcC += laterJobs * ttc;
    }

    mip_cj.add(Csum >= totalBatchC + totalTtcC);
    mip_cj.add(IloMinimize(env, Csum));
		IloCplex mip_cj_solver(mip_cj);
		mip_cj_solver.solve();
		cout << mip_cj_solver.getStatus() << endl;

		/********** output results ***********/

		cout << mip_cj_solver.getValue(Csum) << endl;

		cout << endl;
		for (int j = 0; j < nj; j++) {
			if (j < 10)
				cout << " ";
			cout << (j);
			for (int k = 0; k < nk; k++) {
				cout << (mip_cj_solver.getValue(xjk[j][k]) == true ? " X" : " ·");
			}
      cout << "   " << mip_cj_solver.getValue(IloSum(fjk[j]));
			cout << endl;
		}

    for(int k=0; k<nk; k++) {
      cout << "Batch " << k << ": " << mip_cj_solver.getValue(xjksum(xjk, k, 0, -1, true)) << endl;
    }

		cout << mip_cj_solver.getTime() << " seconds" << endl;
	} catch (IloException& e) {
		cout << "Error!: " << e << endl;
	}

	env.end();

	return 0;
}
