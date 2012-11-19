
#include "MinNonzeroDuedateI.h"

MinNonzeroDuedateI::MinNonzeroDuedateI(IloIntVar Dk, IloIntVarArray assignments, IloIntArray dj, int k) : IloPropagatorI(Dk.getEnv()), _Dk(Dk),
  _assignments(assignments), _dj(dj) {
    _k = k;
    addVar(Dk); // this is the object that we want cpoptimizer to propagate on
    // also add the matrix variables to this
    for(int j=0; j<assignments.getSize(); j++) {
        addVar(assignments[j]);
    }
}

void MinNonzeroDuedateI::execute() {
  IloInt curMinDuedate = IloIntMax; // MAXINT
  // loop through the matrix column k and find the job with the minimum duedate
  for(int j=0; j < _dj.getSize(); j++) {
    if(isFixed(_assignments[j])) {
      if(getValue(_assignments[j]) == _k) {
        if(_dj[j] < curMinDuedate) {
          curMinDuedate = _dj[j];
        }
      }
    }
  }
  
    setMax(_Dk, curMinDuedate);
}

IloPropagatorI* MinNonzeroDuedateI::makeClone(IloEnv env) const {
  return new (env) MinNonzeroDuedateI(_Dk, _assignments, _dj, _k);
}
