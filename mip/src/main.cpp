#include <iostream>
#include <cmath>
#include <string>
#include <cstdlib>

#include <ilcplex/ilocplex.h>
#include <ilconcert/ilocsvreader.h>
#include <ilconcert/iloexpression.h>

using namespace std;

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
  IloEnv env;
  try {

    // declarations
    IloModel model(env);
    int nj;

    if(argc >= 2) {
      nj = atoi(argv[1]);
    } else {
      nj = 10;
    }

    int nk = nj;
    int capacity = 20;
    int Dmax = 2000;

    /************ variables ***********/
    
    // x_jk
    // This is an array of j*j binary variables.

    typedef IloArray<IloNumVarArray> xjk_matrix;

    xjk_matrix xjk(env, nj);
    
    for(int j=0; j<nj; j++) { // initialize each matrix row (reprs. jobs)
      xjk[j] = IloNumVarArray(env, nj);
      for(int k=0; k<nk; k++) {
	xjk[j][k] = IloNumVar(env, 0, 1, ILOINT);
      }
    }

    // P_k, D_k, C_k
    // One for each of j batches

    IloNumVarArray Pk(env, nk);
    IloNumVarArray Dk(env, nk);
    IloNumVarArray Ck(env, nk);
    
    for(int k=0; k<nk; k++) {
      Pk[k] = IloNumVar(env, 0, IloInfinity, ILOFLOAT); /// FIX THIS: WHATS UPPER BOUND?
      Dk[k] = IloNumVar(env, -IloInfinity, Dmax, ILOFLOAT);
      Ck[k] = IloNumVar(env, 0, IloInfinity, ILOFLOAT);
    }

    // Lmax
    // This one is used in the objective.
    IloNumVar Lmax(env, -IloInfinity, IloInfinity, ILOFLOAT);

    /************ constants ***********/

    IloNumArray sj(env, nj);
    IloNumArray pj(env, nj);
    IloNumArray dj(env, nj);

    stringstream filename;
    filename << "../../data/data_" << nj;

    IloCsvReader csvr(env, filename.str().c_str());

    if(csvr.getNumberOfItems()-1 != nj) {
      cout << "Error: Expected " << nj << " jobs, but found " << csvr.getNumberOfItems() << " in CSV file. Aborting.\n";
      return 1;
    }

    IloCsvLine curLine;

    for(int j=0; j<nj; j++) {
      curLine = csvr.getLineByNumber(j+1);
      sj[j] = curLine.getIntByPosition(0);
      pj[j] = curLine.getIntByPosition(1);
      dj[j] = curLine.getIntByPosition(2);
    }

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
    for(int k=1; k<nk; k++) {
      model.add( IloRange(env, -IloInfinity, Ck[k-1] + Pk[k] - Ck[k], 0));
    }

    // 10a. Every batch is due when the earliest job is due.
    for(int k=0; k<nk; k++) {
      for(int j=0; j<nj; j++) {
	model.add( IloRange(env, 0, Dk[k] - (dj[j] - Dmax)*xjk[j][k], Dmax));
      }
    }

    // 11. EDD
    for(int k=1; k<nk; k++) {
      model.add( IloRange(env, 0, Dk[k] - Dk[k-1], IloInfinity));
    }

    // 12. Lmax
    for(int k=0; k<nk; k++) {
      model.add( IloRange(env, 0, Ck[k] - Dk[k] - Lmax, 0)) ;
    }


    /********* solving the model ******/
    IloCplex cplex(model);
    cplex.solve();
    cout << cplex.getStatus() << endl;

    /********** printing results ********/

    cout << "Lmax: " << cplex.getValue(Lmax) << endl;
    cout << "Solution: " << endl;

    for(int k=0; k<nk+1; k++) {
      if(k<10) cout << " ";
      cout << k;
    }
    cout << endl;
    for(int j=0; j<nj; j++) {
      if(j<10) cout << " ";
      cout << (j+1);
      for(int k=0; k<nk; k++) {
	cout << (cplex.getValue(xjk[j][k]) == true ? " X" : " ·");
      }
      cout << endl;
    }

    // printing batch results

    for(int k=0; k<nk; k++) {
      cout << "Batch " << k << ":\t Pk=" << cplex.getValue(Pk[k]) << "\t Dk=" << cplex.getValue(Dk[k]) << "\t Ck=" << cplex.getValue(Ck[k]) << endl;
    }

  } catch( IloException& e) {
    cout << "Some error: " << e << endl;
  }

  env.end();
  cout << "Done!" << endl;
  return 0;
}
