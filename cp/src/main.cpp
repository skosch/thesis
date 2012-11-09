#include <iostream>
#include <cmath>
#include <string>

#include <cstdlib>
#include <stdlib.h>
#include <algorithm>
#include <vector>

#include <ilcplex/ilocplex.h>
#include <ilcp/cp.h>
#include <ilconcert/ilocsvreader.h>
#include <ilconcert/iloexpression.h>

#include "MinNonzeroDuedateI.h"

using namespace std;


// explicit data structure makes sorting easier
struct job{
  IloInt s;
  IloInt p;
  IloInt d;
};
bool operator<(const job &a, const job &b) {return a.d < b.d;}

const char* charcat(string first, int second, int third) {
  stringstream res;
  res << first << second << "_" << third;
  return res.str().c_str();
}

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

/*** Custom propagators ***/
IloConstraint MinNonzeroDuedate(IloIntVar Dk, IloArray<IloIntVarArray> matrix, IloIntArray
    dj, int k) {
  return IloCustomConstraint(Dk.getEnv(), new (Dk.getEnv())
      MinNonzeroDuedateI(Dk, matrix, dj, k));
}


// Same as secondaryProdArray, but doesn't include zeros
IloIntExprArray secondaryProdArray(IloEnv env, IloIntArray coeff,
    IloArray<IloIntVarArray> matrix, int secondIndex, int coeffsize) {
  IloIntExprArray result(env); 
  for(int i=0; i<coeffsize; i++) {
    result.add(coeff[i]*matrix[i][secondIndex]); 
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
        xjk[j][k] = IloIntVar(env, 0, 1, charcat("xjk_", j, k));
      }
    }

    IloIntVarArray Dk(env, nk);
    IloIntervalVarArray K(env, nk); // the batches
    IloIntervalVarArray J(env, nj); // the jobs

    for(int k=0; k < nk; k++) {
      Dk[k] = IloIntVar(env, 0, Dmax, charcat("Dk_", k, 0));
      K[k] = IloIntervalVar(env, charcat("Kinterval_", k, 0));
      K[k].setStartMin(0);
    }

    IloIntVar Lmax(env, -IloInfinity, IloInfinity, "Lmax");

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

    // 8. batches are as long as their longest job
    for(int k=0; k<nk; k++) {
      IloIntExprArray prodExpression = secondaryProdArray(env, pj, xjk, k, nj);
      //cout << prodExpression << endl;
      model.add( IloLengthOf(K[k]) == IloMax(prodExpression) );
    }


    // 9. batches are due as early as the earliest job
    for(int k=0; k<nk; k++) {
      model.add( MinNonzeroDuedate(Dk[k], xjk, dj, k));
    }

    // 10. Lmax definition
    //  Latenesses: { IloEndOf(K[k]) - Dk[k]}
    IloIntExprArray L(env, nk);
    for(int k=0; k<nk; k++) {
      L[k] = IloEndOf(K[k]) - Dk[k];
    }
    model.add( Lmax == IloMax(L) );

    // 11. Sequential setup
    for(int k=1; k<nk; k++) {
      model.add(IloEndAtStart(env, K[k-1], K[k], 0));
    }


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
    // model.add( Lmax <= 25); // this doesn't really help much.

    // 16. No batches later than necessary. This works because we sorted things.
    for(int j=0; j<nj; j++) {
      for(int k=j+1; k<nk; k++) {
        model.add( xjk[j][k] == 0 );
      }
    }

    // 17. No empty batches in the middle
    for(int k=0; k<nk-2; k++) {
      model.add(IloIfThen( env, IloEndOf(K[k+2]) > IloEndOf(K[k]),
            IloEndOf(K[k+1]) > IloEndOf(K[k])));
    } 

    /************** solve the model ************/
    //cout << model << endl;
    IloCP cp(model);

    // cp.setParameter(IloCP::OptimalityTolerance, 1);
    // cp.setParameter(IloCP::TimeLimit, 20);
    //cp.setParameter(IloCP::PropagationLog, IloCP::Verbose);
    cp.setParameter(IloCP::SearchType, IloCP::DepthFirst);
    cp.solve();

    /********** printing results ********/

    cout << "Lmax: " << cp.getValue(Lmax) << endl;
    cout << "Solution: " << endl << "  ";
    cout << "-----" << endl;
    cout << "Batch completion dates:" << endl;
    for(int k=0; k<nk; k++) {
      if(k<10) cout << " ";
      cout << cp.getValue(IloEndOf(K[k]));
    }
    cout << endl << "Batch due dates:" << endl;
    for(int k=0; k<nk; k++) {
      if(k<10) cout << " ";
      cout << cp.getValue((Dk[k]));
    }
    cout << endl << "Batch lateness:" << endl;
    for(int k=0; k<nk; k++) {
      if(k<10) cout << " ";
      cout << cp.getValue((L[k]));
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
