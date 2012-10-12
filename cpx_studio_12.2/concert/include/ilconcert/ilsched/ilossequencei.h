// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilconcert/ilsched/ilossequencei.h
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

#ifndef __CONCERT_ilossequenceiH
#define __CONCERT_ilossequenceiH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilconcert/iloenv.h>
#include <ilconcert/iloexpression.h>
#include <ilconcert/ilsched/ilosbasic.h>
#include <ilconcert/ilsched/ilostimei.h>
#include <ilconcert/iloparami.h>

// --------------------------------------------------------------------------
// CONSTRAINTS ON SEQUENCE VARIABLES
// --------------------------------------------------------------------------

class IloSequenceNextConstraintI: public IloConstraintI {
  ILOS_CPEXTR_DECL(IloSequenceNextConstraintI, IloConstraintI)
    public:
  IloSequenceNextConstraintI(IloEnvI* env,
                             IloIntervalSequenceVarI* seq, 
                             IloIntervalVarI* prev, 
                             IloIntervalVarI* next);
  virtual ~IloSequenceNextConstraintI(){}
  virtual void display(ILOSTD(ostream)& out) const;
  IloIntervalSequenceVarI* getSequenceVar() const { return _seq; }
  IloIntervalVarI* getPrev() const { return _prev; } 
  IloIntervalVarI* getNext() const { return _next; }

protected:
  virtual void atRemove(IloExtractableI* sub = 0, IloAny info = 0);

private:
  virtual IloExtractableI* makeClone(IloEnvI*) const;

private:
  IloIntervalSequenceVarI* _seq;
  IloIntervalVarI*         _prev; 
  IloIntervalVarI*         _next;
};

class IloSequenceBeforeConstraintI: public IloConstraintI {
  ILOS_CPEXTR_DECL(IloSequenceBeforeConstraintI, IloConstraintI)
    public:
  IloSequenceBeforeConstraintI(IloEnvI* env,
                               IloIntervalSequenceVarI* seq, 
                               IloIntervalVarI* pred, 
                               IloIntervalVarI* succ);
  virtual ~IloSequenceBeforeConstraintI(){}
  virtual void display(ILOSTD(ostream)& out) const;
  IloIntervalSequenceVarI* getSequenceVar() const { return _seq; }
  IloIntervalVarI* getPredecessor() const { return _pred; } 
  IloIntervalVarI* getSuccessor()   const { return _succ; }
protected:
  virtual void atRemove(IloExtractableI* sub = 0, IloAny info = 0);

private:
  virtual IloExtractableI* makeClone(IloEnvI*) const;

private:
  IloIntervalSequenceVarI* _seq;
  IloIntervalVarI*         _pred; 
  IloIntervalVarI*         _succ;
};

// --------------------------------------------------------------------------
// EXPRESSIONS ON SEQUENCE VARIABLES
// --------------------------------------------------------------------------

class IloSequenceExprI: public IloIntExprI {
  ILOS_CPEXTR_DECL(IloSequenceExprI, IloIntExprI)
  public:
  enum Type { 
    TypeOfNext, 
    StartOfNext, 
    EndOfNext, 
    SizeOfNext, 
    LengthOfNext,
    TypeOfPrev, 
    StartOfPrev, 
    EndOfPrev, 
    SizeOfPrev, 
    LengthOfPrev
  };
  IloSequenceExprI(Type type, 
                   IloIntervalSequenceVarI* seq, 
                   IloIntervalVarI* a,
                   IloInt posVal,
                   IloInt absVal);
  virtual ~IloSequenceExprI(){}
  IloSequenceExprI::Type   getType()        const { return _type; }
  IloIntervalSequenceVarI* getSequenceVar() const { return _seq; }
  IloIntervalVarI*         getIntervalVar() const { return _a; }
  IloInt getExtremePositionValue() const { return _posVal; }
  IloInt getAbsenceValue()         const { return _absVal; }

public:
  virtual void display(ILOSTD(ostream)& out) const;
  virtual IloNum eval(const IloAlgorithm algo) const;
  static IloEvalCP _evalCP;
protected:
  virtual void atRemove(IloExtractableI* sub = 0, IloAny info = 0);
private:
  virtual IloExtractableI* makeClone(IloEnvI*) const;

private:
  Type                      _type;
  IloIntervalSequenceVarI*  _seq;
  IloIntervalVarI*          _a;
  IloInt                    _posVal;
  IloInt                    _absVal;
};

// --------------------------------------------------------------------------
// NO-OVERLAP CONSTRAINT ON SEQUENCE
// --------------------------------------------------------------------------

class IloNoOverlapI: public IloConstraintI {
  ILOS_CPEXTR_DECL(IloNoOverlapI, IloConstraintI)
    public:
  enum ParamId {
    TransitionDistance =0
  };
public:
  IloNoOverlapI(IloEnvI* env,
		IloIntervalSequenceVarI* seq, 
		IloTransitionDistanceI* tdist,
                IloBool direct,
		const char* name);
  virtual ~IloNoOverlapI(){}
  virtual void display(ILOSTD(ostream)& out) const;
  virtual void changeParameter(const IloParameterExtractableLinkI* param);
  virtual void endParameter(const IloParameterExtractableLinkI* link);
  IloIntervalSequenceVarI* getSequenceVar() const { return _seq; }
  IloTransitionDistanceI* getTransitionDistanceParam() const { 
    if (0 != _tdistParamLink)
      return (IloTransitionDistanceI*)_tdistParamLink->getParameter();
    return NULL;
  }
  IloBool transitionDistanceOnDirectSuccessors() const { 
    return _direct; 
  }
protected:
  virtual void atRemove(IloExtractableI* sub = 0, IloAny info = 0);

private:
  virtual IloExtractableI* makeClone(IloEnvI*) const;

private:
  IloIntervalSequenceVarI*  _seq;
  IloParameterExtractableLinkI* _tdistParamLink;
  IloBool _direct;
};

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
