// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilconcert/iloalg.h
// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
//
// 5725-A06 5725-A29 5724-Y47 5724-Y48 5724-Y49 5724-Y54 5724-Y55
// Copyright IBM Corp. 2000, 2010
//
// US Government Users Restricted Rights - Use, duplication or
// disclosure restricted by GSA ADP Schedule Contract with
// IBM Corp.
// ---------------------------------------------------------------------------

#ifndef __CONCERT_iloalgH
#define __CONCERT_iloalgH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilconcert/iloenv.h>
#include <ilconcert/iloextractable.h>

class IloSolutionOperationsI;
class IloSolutionManagerI;
class IloAlgorithmI;
class IloNumExprArg;
class IloNumExprArray;
class IloNumVar;
class IloNumVarArray;
class IloExtractable;
class IloExtractableArray;
#if !defined(IloNumSetVar)
# define IloNumSetVar IloIntSetVar
#endif
class IloObjective;
class IloAnySetVar;
class IloIntSetVar;

class IloAlgorithm {
  IloBool callSolve() const;
protected:
  IloAlgorithmI* _impl;
public:
  IloAlgorithm(IloAlgorithmI* impl = 0) : _impl(impl) {}

  IloAlgorithmI* getImpl() const        { return (_impl); }

  IloEnv getEnv() const;
  IloModel getModel() const;

  void end();

  enum Status {
    Unknown,
    Feasible,
    Optimal,
    Infeasible,
    Unbounded,
    InfeasibleOrUnbounded,
    Error
  };

  ILOSTD(ostream)& out() const;
  void setOut(ILOSTD(ostream)&);
  ILOSTD(ostream)& warning() const;
  void setWarning(ILOSTD(ostream)&);
  ILOSTD(ostream)& error() const;
  void setError(ILOSTD(ostream)&);
  IloNum getTime() const;
  void   printTime() const;
  void   resetTime() const;

  void extract(const IloModel) const;


  IloBool solve() const;

  void clear() const;

  Status getStatus() const;

  IloNum getObjValue() const;

  IloNum getValue(const IloNumVar) const;
  IloNum getValue(const IloIntVar) const;
  IloInt getIntValue(const IloIntVar) const;
  IloNum getValue(const IloObjective) const;
  void getValues(const IloNumVarArray, IloNumArray) const;
  void getValues(const IloIntVarArray, IloNumArray) const;
  void getIntValues(const IloIntVarArray, IloIntArray) const;
  IloNum getValue(const IloNumExprArg) const;
  IloBool isExtracted(const IloExtractable) const;

  IloSolutionOperationsI* getSolutionOperations(const IloExtractableI *e)const;
  IloSolutionOperationsI* getSolutionOperations(IloExtractable e) const;

  class Exception : public IloException {
  public:
    Exception(const char* str) : IloException(str)      {}
  };

  class CannotExtractException : public Exception {
    const IloAlgorithmI* _alg;
    IloExtractableArray  _obj;
  public:
    CannotExtractException(const IloAlgorithmI* alg);
    CannotExtractException(const IloAlgorithmI* alg, IloExtractableI* obj);
    void print(ILOSTD(ostream)& out) const;
    const IloAlgorithmI* getAlgorithm() const       { return (_alg); }
    IloExtractableArray& getExtractables()          { return (_obj); }
    void end();
  };

  class CannotRemoveException : public Exception {
    const IloAlgorithmI* _alg;
    IloExtractableArray  _obj;
  public:
    CannotRemoveException(const IloAlgorithmI* alg);
    void print(ILOSTD(ostream)& out) const;
    const IloAlgorithmI* getAlgorithm() const    { return (_alg); }
    IloExtractableArray& getExtractables()       { return (_obj); }
    void end();
  };

  class CannotChangeException : public Exception {
    const IloAlgorithmI* _alg;
    IloExtractableArray  _obj;
  public:
    CannotChangeException(const IloAlgorithmI* alg);
    void print(ILOSTD(ostream)& out) const;
    const IloAlgorithmI* getAlgorithm() const    { return (_alg); }
    IloExtractableArray& getExtractables()       { return (_obj); }
    void end();

  };

  class NotExtractedException : public Exception {
    const IloAlgorithmI* _alg;
    const IloExtractable _obj;
  public:
    NotExtractedException(const IloAlgorithmI*, const IloExtractable);
    void print(ILOSTD(ostream)& out) const;
    const IloAlgorithmI*  getAlgorithm() const   { return (_alg); }
    const IloExtractable& getExtractable()       { return (_obj); }
  };

  class NoObjectiveException : public Exception {
    const IloAlgorithmI* _alg;
  public:
    NoObjectiveException(const IloAlgorithmI*);
    void print(ILOSTD(ostream)& out) const;
    const IloAlgorithmI*  getAlgorithm() const   { return (_alg); }
  };

  class ExtractFailure : public Exception {
  public:
    ExtractFailure(const char* s = 0) : Exception(s) {}
    virtual ~ExtractFailure();
  };

  class RemoveFailure : public Exception {
  public:
    RemoveFailure(const char* s = 0) : Exception(s) {}
    virtual ~RemoveFailure();
  };
  ILOEXTENSIONHNONINLINE(IloAlgorithm)
};

ILOSTD(ostream)& operator<<(ILOSTD(ostream)& out, IloAlgorithm::Status st);

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
