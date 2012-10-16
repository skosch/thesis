#include <iostream>
#include <cmath>
#include <string>
#include <cstdlib>
#include <algorithm>
#include <vector>

#include <ilcplex/ilocplex.h>
#include <ilcp/cp.h>
#include <ilconcert/ilocsvreader.h>
#include <ilconcert/iloexpression.h>

using namespace std;


// explicit data structure makes sorting easier
struct job{
  IloInt s;
  IloInt p;
  IloInt d;
};
bool operator<(const job &a, const job &b) {return a.d < b.d;}


IloIntExpr secondarySumExpr(IloEnv env, IloIntArray coeff, IloArray<IloIntVarArray> matrix, int secondIndex, int coeffsize) {
  // builds an expression in a loop.
  // input: coeff, matrix
  // output: sum over all i for given secondindex: coeff[i] * matrix[i][secondIndex]
  
  IloIntExpr result(env);
  for(int i = 0; i<coeffsize; i++) { 
    result += (coeff[i] * matrix[i][secondIndex]);
  }
  return result;

}

IloIntExprArray secondaryProdArray(IloEnv env, IloIntArray coeff, IloArray<IloIntVarArray> matrix, int secondIndex, int coeffsize) {
  IloIntExprArray result(env, coeffsize);
  for(int i=0; i<coeffsize; i++) {

    result[i] = coeff[i] * matrix[i][secondIndex];
  }
  return result;
}

IloIntExprArray secondaryNZProdArray(IloEnv env, IloIntArray coeff, IloArray<IloIntVarArray> matrix, int secondIndex, int coeffsize) {
  IloIntExprArray result(env);
  for(int i=0; i<coeffsize; i++) {
    if(matrix[i][secondIndex] > 0) result.add(IloIntExpr(coeff[i]));
  }
  return result;
}


int main(int argc, const char * argv[]){
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


    /************** variables *************/
    typedef IloArray<IloIntVarArray> xjk_matrix;
    xjk_matrix xjk(env, nj);

    for(int j=0; j<nj; j++) {
      xjk[j] = IloIntVarArray(env, nk);
      for(int k=0; k<nk; k++) {
	xjk[j][k] = IloIntVar(env, 0, 1);
      }
    }

    IloIntVarArray Dk(env, nk);
    IloIntervalVarArray K(env, nk); // the batches
    IloIntervalVarArray J(env, nj); // the jobs

    for(int k=0; k < nk; k++) {
      Dk[k] = IloIntVar(env, 0, Dmax);
      K[k] = IloIntervalVar(env);
      K[k].setStartMin(0);
    }

    IloIntVar Lmax(env, -IloInfinity, IloInfinity);

    /*************** constants *************/
   

    stringstream filename;
    filename << "../../data/data_" << nj;

    IloCsvReader csvr(env, filename.str().c_str());

    if(csvr.getNumberOfItems()-1 != nj) {
      cout << "Error: Expected " << nj << " jobs, but found " << csvr.getNumberOfItems() << " in CSV file. Aborting.\n";
      return 1;
    }

    vector<job> jc(nj); // this structure is necessary to sort the jobs.
    IloIntArray sj(env,nj);
    IloIntArray pj(env,nj);
    IloIntArray dj(env,nj);

    IloCsvLine curLine;
    for(int j=0; j<nj; j++) {
      curLine = csvr.getLineByNumber(j+1);
      jc[j].s = curLine.getIntByPosition(0);
      jc[j].p = curLine.getIntByPosition(1);
      jc[j].d = curLine.getIntByPosition(2);
    }

    // sort the jobs by non-decreasing due date
    sort(jc.begin(), jc.end());

    // now put the constants into the IloIntArrays
      cout << "Batches:" << endl;
    for(int j=0; j<nj; j++) {
      sj[j] = jc[j].s;
      pj[j] = jc[j].p;
      dj[j] = jc[j].d;
      cout << "Batch " << j << ": " << sj[j] << "\t" << pj[j] << "\t" << dj[j] << endl;
    }

    for(int j=0; j<nj; j++) {
      J[j] = IloIntervalVar(env);
      J[j].setLengthMin(pj[j]);
      J[j].setLengthMax(pj[j]);
    }

    // number of non-empty batches
    IloIntVar batchesUsed(env, 0, nk); 
    IloIntVarArray batchloads(env, nk, 0, capacity);
    IloIntVarArray assignments(env, nj, 0, nk);
    

    /*********** objective function ************/

    model.add(IloMinimize(env, Lmax));

    /*********** constraints *****************/

    model.add(IloPack(env, batchloads, assignments, sj, batchesUsed));

    // batches are as long as their longest job
    // jobs in a batch are given by max(pj[j where assignments[j]=k])
    

    for(int k = 0; k<nk; k++) {
      for(int j = 0; j<nj; j++) {
	model.add( xjk[j][k] == (assignments[j]==k) );
      }
    }
    /*
    // batches span their jobs
    for(int k=0; k<nk; k++) {

      IloIntExprArray prodArray = secondaryProdArray(env, pj, xjk, k, nj);

      model.add( IloLengthOf(K[k]) ==  4); //IloMax(prodArray) );
      model.add( Dk[k] == 6);
      /*
	IloIntervalVarArray includantJob(env,1);
	includantJob[0] = J[j];
	model.add(IloIfThen(env, assignments[j]==k, IloSpan(env, K[k], includantJob)));
      
    }
    */



    // 8. batches are as long as their longest job
    for(int k=0; k<nk; k++) {
      IloIntExprArray prodExpression = secondaryProdArray(env, pj, xjk, k, nj);
      //cout << prodExpression << endl;
      model.add( IloLengthOf(K[k]) == IloMax(prodExpression) );
    }


    // 9. batches are due as early as the earliest job
    for(int k=0; k<nk; k++) {
      IloIntExprArray prodExpression = secondaryNZProdArray(env, dj, xjk, k, nj);
      model.add( Dk[k] == IloMin(prodExpression) );
    }

    // 10. Lmax definition
    //  Latenesses: { Dk[k] - IloEndOf(K[k]) }
    IloIntExprArray L(env, nk);
    for(int k=0; k<nk; k++) {
      L[k] = Dk[k] - IloEndOf(K[k]);
    }
    model.add( Lmax == IloMax(L) );

    // 11. Sequential setup
    for(int k=1; k<nk; k++) {
      model.add(IloEndAtStart(env, K[k-1], K[k], 0));
    }

    /************** solve the model ************/

    IloCP cp(model);
    cp.setParameter(IloCP::OptimalityTolerance, 1);
    cp.setParameter(IloCP::TimeLimit, 5);
    cp.solve();

 /********** printing results ********/

        cout << "Lmax: " << cp.getValue(batchloads[4]) << endl;
    cout << "Solution: " << endl << "  ";

    for(int k=0; k<nk; k++) {
      if(k<10) cout << " ";
      cout << cp.getValue(IloEndOf(K[k]));
    }
   for(int k=0; k<nk; k++) {
      if(k<10) cout << " ";
      cout << cp.getValue((Dk[k]));
    }
    cout << endl;
       for(int j=0; j<nj; j++) {
      if(j<10) cout << " ";
      cout << (j);
      for(int k=0; k<nk; k++) {
	cout << (cp.getValue(xjk[j][k]) == true ? " X" : " ·");
      }
      cout << endl;
    }
    
    // printing batch results

    for(int k=0; k<nk; k++) {
      //      cout << "Batch " << k << ":\t Pk=" << cp.getValue(Pk[k]) << "\t Dk=" << cp.getValue(Dk[k]) << "\t Ck=" << cp.getValue(Ck[k]) << endl;
    }
  }
  catch (IloException& ex) {
    env.out() << "Error: " << ex << " " << ex.getMessage() << std::endl;
  }
  env.end();
  return 0;
}
