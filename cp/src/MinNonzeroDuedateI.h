#ifndef MINNONZERODUEDATEI_H
#define MINNONZERODUEDATEI_H

#include <iostream>
#include <ilcplex/ilocplex.h>
#include <ilcp/cp.h>
#include <ilconcert/ilocsvreader.h>
#include <ilconcert/iloexpression.h>

using namespace std;

class MinNonzeroDuedateI : public IloPropagatorI {
  private:
    // data members for the constraint
    IloIntVar _Dk;
    IloIntVarArray _assignments;
    IloIntArray _dj;
    int _k;
  public:
    MinNonzeroDuedateI(IloIntVar Dk, IloIntVarArray assignments, IloIntArray
    dj, int k);
    void execute();
    IloPropagatorI * makeClone(IloEnv env) const;
};

#endif /* MINNONZERODUEDATEI_H */
