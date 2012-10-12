#include <iostream>
#include <cmath>
#include <string>
#include <cstdlib>

#include <ilcplex/ilocplex.h>
#include <ilconcert/ilocsvreader.h>
#include <ilconcert/iloexpression.h>

using namespace std;

int main() {
  IloEnv env;
  try {

    // declarations
    IloModel model(env);
    int nj = 10;   // number of jobs

    /************ variables ***********/
    
    // x_jk
    // This is an array of j*j binary variables.

    typedef IloArray<IloNumVarArray> xjk_matrix;

    xjk_matrix xjk(env, nj);
    
    for(int j=0; j<nj; j++) { // initialize each matrix row (reprs. jobs)
      xjk[j] = IloNumVarArray(env, nj);
      for(int k=0; k<nj; k++) {
	xjk[j][k] = IloNumVar(env, 0, 1, ILOBOOL);
      }
    }

    // P_k, D_k, C_k
    // One for each of j batches

    IloNumVarArray Pk(env, nj);
    IloNumVarArray Dk(env, nj);
    IloNumVarArray Ck(env, nj);
    
    for(int k=0; k<nj; k++) {
      Pk[k] = IloNumVar(env, 0, 20, ILOFLOAT); /// FIX THIS: WHATS UPPER BOUND?
      Dk[k] = IloNumVar(env, 0, 2000, ILOFLOAT);
      Ck[k] = IloNumVar(env, 0, 2000, ILOFLOAT);
    }

    // Lmax
    // This one is used in the objective.

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

    IloObjective obj(env, Lmax, IloObjective::Minimize);

    // constraints

    
    // solving the model
    IloCplex cplex(model);
    cplex.solve();
    cout << cplex.getStatus() << endl;

  } catch( IloException& e) {
    cout << "Some error: " << e << endl;
  }

  env.end();
  cout << "Done!" << endl;
  return 0;
}
