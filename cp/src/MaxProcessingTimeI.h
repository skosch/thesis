#ifndef MAXPROCESSINGTIMEI_H
#define MAXPROCESSINGTIMEI_H

#include <iostream>
#include <ilcplex/ilocplex.h>
#include <ilcp/cp.h>
#include <ilcp/cpext.h>
#include <ilconcert/ilocsvreader.h>
#include <ilconcert/iloexpression.h>

using namespace std;

class IlcMaxProcessingTimeI : public IlcConstraintI {
  private:
    // data members for the constraint
    IlcIntervalVar _K;
    IlcIntVarArray _assignments;
    IlcIntArray _pj;
    int _k;
  public:
    IlcMaxProcessingTimeI(IloCP cp, IlcIntervalVar K, IlcIntVarArray assignments,
    IlcIntArray pj, int k);
    ~IlcMaxProcessingTimeI(){}; // empty constructor
    virtual void propagate();
    virtual void post();
};

#endif /* MAXPROCESSINGTIMEI_H */
