
#include "MaxProcessingTimeI.h"

IlcMaxProcessingTimeI::IlcMaxProcessingTimeI(IloCP cp, IlcIntervalVar K, IlcIntVarArray
assignments, IlcIntArray pj, int k) : IlcConstraintI(cp), _K(K),
  _assignments(assignments), _pj(pj) {
    _k = k;

// constructor is otherwise empty, variables don't have to be added
// because we're working with engine objects that were extracted
// in the wrapper in the main file.
}

void IlcMaxProcessingTimeI::propagate() {
  if (_K.getOldLengthMax() > _K.getLengthMax()) {
    cout << "The max length of batch " << _k << " was just reduced from " <<
    _K.getOldLengthMax() << " to " << _K.getLengthMax() << endl;
  }
  IlcInt curMaxPTime = 0; // MAXINT
  IlcInt fixedJobs = 0;
  // loop through the matrix column k and find the job with the minimum duedate
  for(int j=0; j < _pj.getSize(); j++) {
    if(_assignments[j].isFixed()) {
      if(_assignments[j].getValue() == _k) {
      fixedJobs++;
      cout << "Job " << j << " fixed to batch " << _k << endl;
        if(_pj[j] > curMaxPTime) {
          curMaxPTime = _pj[j];
        }
      }
    }
  }
  cout << "Let's see how long this makes batch " << _k << "! MaxPTime is " <<
  curMaxPTime << endl;
  if(fixedJobs == 0) {
    _K.setLengthMin(0);
    _K.setLengthMax(IlcIntMax);
  }
  if(fixedJobs > 0) {
  cout << "Current domain:" << _K.getLengthMin() << " -- " << _K.getLengthMax()
  << endl;
  cout << "domain used to be" << _K.getOldLengthMin() << " -- " <<
  _K.getOldLengthMax() << endl;
    _K.setLengthMin(curMaxPTime);
    //_K.setLengthMax(IlcIntMax);
 cout << "K[" << _k << "] has a length of > " << _K.getLengthMin() << "(end=" <<
 _K.getEndMin() << "), was " << _K.getOldLengthMin() << " with fixedjobs=" <<
 fixedJobs << endl;
 cout << "K[" << _k << "] has a length of < " << _K.getLengthMax() << "(end=" <<
 _K.getEndMax() << "), was " << _K.getOldLengthMax() << " with fixedjobs=" <<
 fixedJobs << endl;
  }

 // setMin(IlcLengthOf(_K), curMaxPTime);
}

void IlcMaxProcessingTimeI::post() {
    //for(int k=0; k<_K.getSize(); k++) { // fix this!
 //   _K[k].whenIntervalDomain(this);
 // }
  _K.whenIntervalDomain(this);

  for(int j=0; j<_pj.getSize(); j++) {  
    _assignments[j].whenValue(this);
  }
} 


