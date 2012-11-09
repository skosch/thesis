
#include "MinNonzeroDuedateI.h"

MinNonzeroDuedateI::MinNonzeroDuedateI(IloIntVar Dk, IloArray<IloIntVarArray>
    matrix, IloIntArray dj, int k) : IloPropagatorI(Dk.getEnv()), _Dk(Dk),
  _matrix(matrix), _dj(dj) {
    _k = k;
    addVar(Dk); // this is the object that we want cpoptimizer to propagate on
    // also add the matrix variables to this
    for(int j=0; j<matrix.getSize(); j++) {
      for(int ik=0; ik<matrix[0].getSize(); ik++) {
        addVar(matrix[j][ik]);
      }
    }
  }

void MinNonzeroDuedateI::execute() {
  IloInt curMinDuedate = IloIntMax; // MAXINT
  int jobsfixed = 0;
  // loop through the matrix column k and find the job with the minimum duedate
  for(int j=0; j < _dj.getSize(); j++) {
    if(isFixed(_matrix[j][_k])) {
    jobsfixed++;
      if(getValue(_matrix[j][_k]) > 0) {
        if(_dj[j] < curMinDuedate) {
          curMinDuedate = _dj[j];
        }
      }
    }
  }
  
  //if(getMax(_Dk) > curMinDuedate) {
    setMax(_Dk, curMinDuedate);
  //}
 /* if(jobsfixed == _dj.getSize()) {
    cout << "Set D[" << _k << "] <= " << curMinDuedate << "with " << jobsfixed
    << "/" << _dj.getSize() << " fixed" << endl;
  }*/
}

IloPropagatorI* MinNonzeroDuedateI::makeClone(IloEnv env) const {
  return new (env) MinNonzeroDuedateI(_Dk, _matrix, _dj, _k);
}
