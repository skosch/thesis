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
#include "MaxProcessingTimeI.h"

using namespace std;


// explicit data structure makes sorting easier
struct job{
  IloInt s;
  IloInt p;
  IloInt d;
};
bool operator<(const job &a, const job &b)  {return (a.d < b.d and a.p <= b.p);}

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
IloConstraint MinNonzeroDuedate(IloIntVar Dk, IloIntVarArray assignments, IloIntArray
    dj, int k) {
  return IloCustomConstraint(Dk.getEnv(), new (Dk.getEnv())
      MinNonzeroDuedateI(Dk, assignments, dj, k));
}

IlcConstraint IlcMaxProcessingTime(IloCP cp, IlcIntervalVar K, IlcIntVarArray assignments,
IlcIntArray pj, int k) {
  //IloCP cp = K.getCP();
  return new (cp.getHeap()) IlcMaxProcessingTimeI(cp, K, assignments, pj, k);
}

ILOCPCONSTRAINTWRAPPER4(MaxProcessingTime, cp, IloIntervalVar, K,
IloIntVarArray, assignments, IloIntArray, pj, IloInt, k) {
  use(cp, K);
  use(cp, assignments);
  return IlcMaxProcessingTime(cp, cp.getInterval(K),
  cp.getIntVarArray(assignments), cp.getIntArray(pj),k);
};

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
    bool verboseoutput = false;
    string datafilename;

    if(argc >= 2) {
      nj = atoi(argv[1]);
    } else {
      nj = 10;
    }

    if(argc >= 3 && !strcmp(argv[2], "v")) {
        verboseoutput=true;
    }

    if(argc >= 4) {
      datafilename = argv[3];
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



    IloIntVar Lmax(env, -IloInfinity, IloInfinity, "Lmax");

    /*************** constants *************/

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

    IloCumulFunctionExpr cumulResource(env);
    for(int j=0; j<nj; j++) {
      J[j] = IloIntervalVar(env, (IloInt)pj[j], (IloInt)IloMax(pj), false, 0, charcat("Jobinterval_", j, 0));
    }

    // number of non-empty batches
    IloIntVar batchesUsed(env, 0, nk, "batchesused");


    IloIntVarArray batchloads(env, nk, 0, capacity);
    IloIntVarArray assignments(env, nj, 0, nk);

    for(int j=0; j < nj; j++) {
      assignments[j] = IloIntVar(env, 0, nk, charcat("assignment_j", j, 0));
    }

    // instantiate Dk and Pk
    for(int k=0; k < nk; k++) {
      Dk[k] = IloIntVar(env, 0, Dmax, charcat("Dk_", k, 0));
      batchloads[k] = IloIntVar(env, 0, capacity, charcat("batchload_k", k, 0));

      //Pk[k] = IloIntVar(env, 0, 0, charcat("Pk_", k, 0));
      //Pk[k] = 0;
      K[k] = IloIntervalVar(env, charcat("Kinterval_", k, 0));
      K[k].setStartMin(0);
    }

    // calculate a lower bound on the number of batches

    // calculate some global cardinality constraints

    /*********** objective function ************/

    model.add(IloMinimize(env, Lmax));

    /*********** constraints *****************/

    // use bin packing to ensure no capacity overloada
    model.add(IloPack(env, batchloads, assignments, sj, batchesUsed));

    // use a cumulative constraint from time 0 through sum(pj) that keeps the
    // sum of sj's between 0 and capacity
    model.add(IloAlwaysIn(env, cumulResource, 0, IloSum(pj), 0, capacity));

    // Now make sure the J[j]'s coincide with the batches
    for(int j=0; j<nj; j++) {
      for(int k=0; k<nk; k++) {
        model.add(IloIfThen(env, assignments[j]==k, IloStartOf(J[j]) ==
        IloStartOf(K[k])));
      }
      cumulResource += IloPulse(J[j],(IloInt) sj[j]);
    }

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
      for(int j=0; j<nj; j++) {
        model.add( IloLengthOf(J[j]) == IloLengthOf(K[k]) * xjk[j][k] +
        IloLengthOf(J[j])*(1-xjk[j][k]));
      }
    }


    // 9. batches are due as early as the earliest job
    for(int k=0; k<nk; k++) {
      model.add( MinNonzeroDuedate(Dk[k], assignments, dj, k));
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
        model.add( assignments[j] <= j );
        for(int k=nj+1; k<nk; k++) {
       //   model.add(xjk[j][k] == 0);
        }
    }

    // 17. No empty batches in the middle
    for(int k=0; k<nk-2; k++) {
      model.add(IloIfThen( env, IloEndOf(K[k+2]) > IloEndOf(K[k]),
            IloEndOf(K[k+1]) > IloEndOf(K[k])));
    } 

    // 18. globalCardinality on batch lengths
    IloIntExprArray allBatchLengths(env);
    for(int k=0; k < nk; k++) {
      allBatchLengths.add(IloLengthOf(K[k]));
    }
    model.add( IloCount( allBatchLengths, IloMax(pj)) == 1);


    /************** solve the model ************/
    //cout << model << endl;
    IloCP cp(model);

    // cp.setParameter(IloCP::OptimalityTolerance, 1);
    // cp.setParameter(IloCP::TimeLimit, 20);
    if(verboseoutput) {
      cp.setParameter(IloCP::PropagationLog, IloCP::Verbose);
    }
    cp.setParameter(IloCP::SearchType, IloCP::DepthFirst);
    cp.setParameter(IloCP::Workers, 1);
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

      // Lastly, print the solve time
    }
      cout << cp.getInfo(IloCP::SolveTime) << endl;
  }
  catch (IloException& ex) {
    env.out() << "Error: " << ex << " " << ex.getMessage() << std::endl;
  }
  env.end();
  return 0;
}
