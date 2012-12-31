#include <iostream>
#include <cmath>
#include <string>
#include <cstdlib>
#include <algorithm>
#include <vector>
#include <deque>

#include <ilcplex/ilocplex.h>
#include <ilconcert/ilocsvreader.h>
#include <ilconcert/iloexpression.h>

using namespace std;


// explicit data structure makes sorting easier
struct job{
  IloNum s;
  IloNum p;
  IloNum d;
};
bool operator<(const job &a, const job &b) {return a.d < b.d;}
 


IloNumExpr secondarySumExpr(IloEnv env, IloNumArray coeff, IloArray<IloNumVarArray> matrix, int secondIndex, int coeffsize) {
  // builds an expression in a loop.
  // input: coeff, matrix
  // output: sum over all i for given secondindex: coeff[i] * matrix[i][secondIndex]
  
  IloNumExpr result(env);
  for(int i = 0; i<coeffsize; i++) { 
    result += (coeff[i] * matrix[i][secondIndex]);
  }
  return result;

}


int main(int argc, char *argv[]) {
  
  // time calculations

  IloEnv env;
  try {

    // declarations
    IloModel model(env);
    int nj;
    int nk;
    string datafilename;


    if(argc >= 2) {
      nj = atoi(argv[1]);
      nk = nj;
    } else {
      nj = 10;
    }

    if(argc >= 3 && !strcmp(argv[2], "v")) {
      //  verboseoutput=true;
    }

    if(argc >= 4) {
      datafilename = argv[3];
    }


    int capacity = 20;
    int Dmax = 2000;

    /************ constants ***********/

 stringstream filename;
   
    if(datafilename.length() > 0) {
      filename << datafilename;
    } else {
      filename << "../../data/data_" << nj;
    }
    cout << filename.str() << endl;
    IloCsvReader csvr(env, filename.str().c_str());

    if(csvr.getNumberOfItems()-1 != nj) {
      cout << "Error: Expected " << nj << " jobs, but found " << csvr.getNumberOfItems() << " in CSV file. Aborting.\n";
      return 1;
    }

    vector<job> jc(nj); // this structure is necessary to sort the jobs.
    IloNumArray sj(env,nj);
    IloNumArray pj(env,nj);
    IloNumArray dj(env,nj);

    IloCsvLine curLine;
    for(int j=0; j<nj; j++) {
      curLine = csvr.getLineByNumber(j+1);
      jc[j].s = curLine.getIntByPosition(0);
      jc[j].p = curLine.getIntByPosition(1);
      jc[j].d = curLine.getIntByPosition(2);
    }

    // sort the jobs by non-decreasing due date
    sort(jc.begin(), jc.end());

    // now put the constants into the IloNumArrays
    for(int j=0; j<nj; j++) {
      sj[j] = jc[j].s;
      pj[j] = jc[j].p;
      dj[j] = jc[j].d;
    }

    /********* calculate bounds *******/

    // dispatch function for n_k
    int nk_UB = nj;
    int j = 0;
    int jmaxindex = nj-1;
    int c = 0;
    int LmaxUB = 0;
    deque<int> jobs(nj);
    vector<int> Lj(nj);
    for(int j=0; j<nj; j++) {jobs[j] = j; Lj[j]=0;}

    for(int k=0; k<nj; k++) { // only go to jmaxindex?
      j = k+1;
      while(j <= jmaxindex) {
	if(pj[j] < pj[k] and sj[j]+sj[k] <= capacity and Lj[j] >= Lj[k]) {
	  jobs.erase(jobs.begin() + j);
	  nk_UB--;
	  // recalculate lateness values of remaining jobs:
	  c = 0;
	  for(int lj=0; lj<jobs.size(); lj++) {
	    Lj[jobs[lj]] = c + pj[jobs[lj]] - dj[jobs[lj]];
	    c += pj[jobs[lj]];
	    if(Lj[jobs[lj]] > LmaxUB) {
	      LmaxUB = Lj[jobs[lj]];
	      jmaxindex = lj;
	    }
	  }

	  // get out of while loop
	  break;
	}

	j++;
      }
    }
    cout << "nkUB is " << nk_UB << endl;
    //nk = nk_UB;


    /************ variables ***********/
    
    // x_jk
    // This is an array of j*j binary variables.

    typedef IloArray<IloNumVarArray> xjk_matrix;

    xjk_matrix xjk(env, nj);
    
    for(int j=0; j<nj; j++) { // initialize each matrix row (reprs. jobs)
      xjk[j] = IloNumVarArray(env, nk);
      for(int k=0; k<nk; k++) {
	xjk[j][k] = IloNumVar(env, 0, 1, ILOINT);
      }
    }

    // P_k, D_k, C_k
    // One for each of j batches

    IloNumVarArray Pk(env, nk);
    IloNumVarArray Dk(env, nk);
    IloNumVarArray Ck(env, nk);
    IloNumVarArray ek(env, nk);

    for(int k=0; k<nk; k++) {
      Pk[k] = IloNumVar(env, 0, IloInfinity, ILOFLOAT); /// FIX THIS: WHATS UPPER BOUND?
      Dk[k] = IloNumVar(env, 0, Dmax, ILOFLOAT);
      Ck[k] = IloNumVar(env, 0, IloInfinity, ILOFLOAT);
      ek[k] = IloNumVar(env, 0, 1, ILOINT);
    }

    // Lmax
    // This one is used in the objective.
    IloNumVar Lmax(env, -IloInfinity, IloInfinity, ILOFLOAT);


    /********* objective function *****/

    model.add(IloMinimize(env, Lmax));

    /********** constraints ***********/

    // 6. sum of xjk[j] over all k is 1
    for(int j=0; j<nj; j++) {
      model.add( IloRange(env, 1, IloSum(xjk[j]), 1) );
    }

    // 7. sum of sizes can't exceed batch capacity
    for(int k=0; k<nk; k++) {
      IloNumExpr sumExpression = secondarySumExpr(env, sj, xjk, k, nj);
      model.add( IloRange(env, 0, sumExpression, capacity) );
    }

    // 8. Pk is greater than greatest processing time in batch.
    for(int j=0; j<nj; j++) {
      // cout << "pj[" << j << "] = " << pj[j] << endl;
      // cout << "sj[" << j << "] = " << sj[j] << endl;
      // cout << "dj[" << j << "] = " << dj[j] << endl;
      for(int k=0; k<nk; k++) {
	model.add( IloRange(env, 0, Pk[k] - pj[j] * xjk[j][k], IloInfinity));
      }
    }

    // 9. Ck is always Ck of the last, plus current Pk
    //    model.add( IloRange(env, 0, Ck[0] - Pk[0], 0) );
    model.add( Ck[0] = Pk[0]);
    for(int k=1; k<nk; k++) {
      model.add( IloRange(env, 0, Ck[k-1] + Pk[k] - Ck[k], 0));
    }

    // 10. Every batch is due when the earliest job is due.
    for(int k=0; k<nk; k++) {
      for(int j=0; j<nj; j++) {
	model.add( IloRange(env, -IloInfinity, Dk[k] - dj[j]*xjk[j][k] + Dmax*xjk[j][k], Dmax));
      }
    }

    // 11. EDD
    for(int k=1; k<nk; k++) {
      model.add( IloRange(env, 0, Dk[k] - Dk[k-1], IloInfinity));
    }

    // 12. Lmax
    for(int k=0; k<nk; k++) {
      model.add( IloRange(env, -IloInfinity, Ck[k] - Dk[k] - Lmax, 0)) ;
    }

    // 13. Grouping empty batches. 
#ifdef MIP_IMPROVEMENTS
    IloNumArray ones(env, nj);
    for(int j=0; j<nj; j++) ones[j] = int(1);

    for(int k=0; k<nk; k++) {
      IloNumExpr sumExpression = secondarySumExpr(env, ones, xjk, k, nj);
      model.add( ek[k] + sumExpression >= 1 );
      model.add( nj*(ek[k] - 1) + sumExpression <= 0 );
    }
    for(int k=1; k<nk; k++) {
            model.add( ek[k] - ek[k-1] >= 0 );
    } 
    model.add( ek[0] == 0 );


    // 14. Lower bound for Lmax
    float Lmax_LB = -IloIntMax; //pj[0]*sj[0]/capacity - dj[0];
    float Cmax_LB_temp = 0;
    for(int j=0; j<nj; j++) {
      Cmax_LB_temp += pj[j]*sj[j]/capacity;
      if(j < nj-1) {
        if(dj[j+1] == dj[j]) {cout<<"same duedate: " << j << endl; continue;} // still the same bucket
      } 
        // new bucket, update Lmax_LB if necessary
        if(Cmax_LB_temp - dj[j] > Lmax_LB) {
          Lmax_LB = Cmax_LB_temp - dj[j];
          cout << "Updating LmaxLB to " << Lmax_LB << ". C=" << Cmax_LB_temp <<
          ", d=" << dj[j];
        
      }
    } 
    cout << "LmaxLB:" << Lmax_LB << endl;
    model.add( Lmax >= ceil(Lmax_LB));

    // 15. Upper bound for Lmax
    //     Get feasible solution by means of EDD, find Lmax

    // 16. No batches later than necessary. This works because we sorted things.
    for(int j=0; j<nj; j++) {
      for(int k=j+1; k<nk; k++) {
	model.add( xjk[j][k] == 0 );
      }
    }
#endif
    // 17. No batch should have empty space if safe eliminations are possible
   /* for(int k=0; k<nk-1; k++) {
      for(int j=0; j<nj; j++) {
        model.add(IloIfThen(env, (pj[j] <= Pk[k] && dj[j] >= Dk[k+1]),
        capacity-secondarySumExpr(env, sj, xjk, k, nj) <= sj[j]));
      }
    }*/

    /********* solving the model ******/



    IloCplex cplex(model);   
    cplex.setParam(IloCplex::ClockType, 1);
    cplex.setParam(IloCplex::MIPDisplay  , 3);   // MIP node log display information
    cplex.setParam(IloCplex::MIPInterval , 1);  // Controls the frequency of node logging when the MIP display parameter is set higher than 1.
    double timeneeded = cplex.getCplexTime();
    cplex.setParam(IloCplex::Threads, 1);
    cplex.setParam(IloCplex::NodeSel, IloCplex::DFS); // depth-first
    cplex.solve();
    cout << cplex.getStatus() << endl;

    timeneeded = cplex.getCplexTime() - timeneeded;

    /********** printing results ********/

    cout << "Lmax: " << cplex.getValue(Lmax) << endl;
//    cout << "Lmax_LB: " << int(Lmax_LB) << endl;
    cout << "Jobs:" << endl;
    for(int j=0; j<nj; j++) {
      if(j<10) cout << " ";
      cout << j << "\t" << "s=" << sj[j] << " p=" << pj[j] << " d=" << dj[j] <<
      endl;
    }
    cout << "Solution: " << endl << "  ";
 cout << "Batch completion dates:" << endl;
    for(int k=0; k<nk; k++) {
      if(k<10) cout << " ";
      cout << cplex.getValue(Ck[k]);
    }
    cout << endl << "Batch due dates:" << endl;
    for(int k=0; k<nk; k++) {
      if(k<10) cout << " ";
      cout << cplex.getValue((Dk[k]));
    }
    cout << endl << "Batch lateness:" << endl;
    for(int k=0; k<nk; k++) {
      if(k<10) cout << " ";
      cout << cplex.getValue((Ck[k] - Dk[k]));
    }

    cout << endl;
    for(int j=0; j<nj; j++) {
      if(j<10) cout << " ";
      cout << (j);
      for(int k=0; k<nk; k++) {
        cout << (cplex.getValue(xjk[j][k]) == true ? " X" : " ·");
      }
      cout << endl;
    }

    // printing batch results

    for(int k=0; k<nk; k++) {
      cout << "Batch " << k << ":\t Pk=" << cplex.getValue(Pk[k]) << "\t Dk=" << cplex.getValue(Dk[k]) << "\t Ck=" << cplex.getValue(Ck[k]) << endl;
    }

    cout << timeneeded << endl;
  } catch( IloException& e) {
    cout << "Some error: " << e << endl;
  }

  env.end();
  cout << "Done!" << endl;
  return 0;
}
