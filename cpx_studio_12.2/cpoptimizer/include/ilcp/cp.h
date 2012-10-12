// -------------------------------------------------------------- -*- C++ -*-
// File: ./ilcp/cp.h
// --------------------------------------------------------------------------
//
// Licensed Materials - Property of IBM
//
// 5724-Y48 5724-Y49 5724-Y54 5724-Y55 5725-A06 5725-A29
// Copyright IBM Corp. 1990, 2010 All Rights Reserved.
//
// US Government Users Restricted Rights - Use, duplication or
// disclosure restricted by GSA ADP Schedule Contract with
// IBM Corp.
//
// --------------------------------------------------------------------------

#ifndef __CP_cpH
#define __CP_cpH

#ifdef _MSC_VER
#pragma pack(push,8)
#endif

#ifndef CPPREF_GENERATION

#define IlcAbs IlcCPOAbs
#define IlcAllocationStack IlcCPOAllocationStack
#define IlcBranchSelectorI IlcCPOBranchSelectorI
#define IlcChooseIntVarI IlcCPOChooseIntVarI
#define IlcChooseIntVar IlcCPOChooseIntVar
#define IlcConstIntArray IlcCPOConstIntArray
#define IlcConstraintArray IlcCPOConstraintArray
#define IlcConstraintI IlcCPOConstraintI
#define IlcConstraint IlcCPOConstraint
#define IlcCPOFloatVarI IlcCPOFloatExpI
#define IlcDemonI IlcCPODemonI
#define IlcDemon IlcCPODemon
#define IlcExponent IlcCPOExponent
#define IlcExprI IlcCPOExprI
#define IlcFloatArrayI IlcCPOFloatArrayI
#define IlcFloatArray IlcCPOFloatArray
#define IlcFloatExpI IlcCPOFloatExpI
#define IlcFloatExp IlcCPOFloatExp
#define IlcFloatMax IlcCPOFloatMax
#define IlcFloatMin IlcCPOFloatMin
#define IlcFloatVarArrayI IlcCPOFloatVarArrayI
#define IlcFloatVarArray IlcCPOFloatVarArray
#define IlcFloatVarArrayIterator IlcCPOFloatVarArrayIterator
#define IlcFloatVarI IlcCPOFloatExpI
#define IlcFloatVar IlcCPOFloatVar
#define IlcGoalArray IlcCPOGoalArray
#define IlcGoalI IlcCPOGoalI
#define IlcGoal IlcCPOGoal
#define IlcIntArray IlcCPOIntArray
#define IlcIntExpI IlcCPOIntExpI
#define IlcIntExp IlcCPOIntExp
#define IlcIntExpIterator IlcCPOIntExpIterator
#define IlcIntPredicateI IlcCPOIntPredicateI
#define IlcIntPredicate IlcCPOIntPredicate
#define IlcIntSelectEvalI IlcCPOIntSelectEvalI
#define IlcIntSelectI IlcCPOIntSelectI
#define IlcIntSelect IlcCPOIntSelect
#define IlcIntSetArray IlcCPOIntSetArray
#define IlcIntSetI IlcCPOIntSetI
#define IlcIntSet IlcCPOIntSet
#define IlcIntSetIterator IlcCPOIntSetIterator
#define IlcIntSetVarArray IlcCPOIntSetVarArray
#define IlcIntSetVarDeltaIterator IlcCPOIntSetVarDeltaIterator
#define IlcIntSetVarI IlcCPOIntSetVarI
#define IlcIntSetVar IlcCPOIntSetVar
#define IlcIntSetVarIterator IlcCPOIntSetVarIterator
#define IlcIntTupleSet IlcCPOIntTupleSet
#define IlcIntTupleSetIterator IlcCPOIntTupleSetIterator
#define IlcIntVarArrayI IlcCPOIntVarArrayI
#define IlcIntVarArray IlcCPOIntVarArray
#define IlcIntVarDeltaIterator IlcCPOIntVarDeltaIterator
#define IlcIntVarI IlcCPOIntVarI
#define IlcIntVar IlcCPOIntVar
#define IlcLightIntExpIterator IlcCPOLightIntExpIterator
#define IlcLog IlcCPOLog
#define IlcManagerI IlcCPOManagerI
#define IlcManager IlcCPOManager
#define IlcPower IlcCPOPower
#define IlcRandomI IlcCPORandomI
#define IlcRandom IlcCPORandom
#define IlcRevAny IlcCPORevAny
#define IlcRevBool IlcCPORevBool
#define IlcRevFloat IlcCPORevFloat
#define IlcRevInt IlcCPORevInt
#define IlcSearchI IlcCPOSearchI
#define IlcSearchLimitI IlcCPOSearchLimitI
#define IlcSearchMonitorI IlcCPOSearchMonitorI
#define IloCPConstraintI IloCPOCPConstraintI
#define IloFailLimit IloCPOFailLimit
#define IloGoalFail IloCPOGoalFail
#define IloGoalI IloCPOGoalI
#define IloGoal IloCPOGoal
#define IloGoalTrue IloCPOGoalTrue
#define IloOrLimit IloCPOOrLimit
#define IloSearchLimitI IloCPOSearchLimitI
#define IloSearchLimit IloCPOSearchLimit
#define IloSolver IloCPOSolver
#define IloTimeLimit IloCPOTimeLimit
#define IlcIntSetIteratorI IlcCPOIntSetIteratorI

#endif

#if !defined(__CONCERT_iloalgH)
# include <ilconcert/iloalg.h>
#endif
#if !defined(__CONCERT_ilomodelH)
# include <ilconcert/ilomodel.h>
#endif
#if !defined(__CONCERT_ilotuplesetH)
# include <ilconcert/ilotupleset.h>
#endif
#if !defined(__CONCERT_ilosmodelH)
# include <ilconcert/ilosmodel.h>
#endif
#if !defined(__CONCERT_ilosatomiH)
# include <ilconcert/ilsched/ilosatomi.h>
#endif

//----------------------------------------------------------------------

#define ILOCPHANDLEMINI(Hname, Iname)                              \
public:                                                            \
  Hname(Iname* impl = 0) : _impl(impl) { }                         \
  Iname* getImpl() const { return _impl; }                         \
protected:                                                         \
  Iname* _impl;

#define ILOCPHANDLE(Hname, Iname)                                  \
  ILOCPHANDLEMINI(Hname, Iname)                                    \
private:                                                           \
  const char *  _getName() const;                                  \
  IloAny        _getObject() const;                                \
  void          _setName(const char * name) const;                 \
  void          _setObject(IloAny obj) const;                      \
public:                                                            \
  const char * getName() const {                                   \
    IloAssert(_impl != 0, ILO_STRINGIZE(hname) ": empty handle");  \
    return _getName();                                             \
  }                                                                \
  IloAny getObject() const {                                       \
    IloAssert(_impl != 0, ILO_STRINGIZE(hname) ": empty handle");  \
    return _getObject();                                           \
  }                                                                \
  void setName(const char * name) const {                          \
    IloAssert(_impl != 0, ILO_STRINGIZE(hname) ": empty handle");  \
    _setName(name);                                                \
  }                                                                \
  void setObject(IloAny obj) const {                               \
    IloAssert(_impl != 0, ILO_STRINGIZE(hname) ": empty handle");  \
    _setObject(obj);                                               \
  }                                                                \

#define ILOCPHANDLEINLINE(Hname, Iname)                            \
  ILOCPHANDLEMINI(Hname, Iname)                                    \
public:                                                            \
  const char * getName() const {                                   \
    IloAssert(_impl != 0, ILO_STRINGIZE(hname) ": empty handle");  \
    return _impl->getName();                                       \
  }                                                                \
  IloAny getObject() const {                                       \
    IloAssert(_impl != 0, ILO_STRINGIZE(hname) ": empty handle");  \
    return _impl->getObject();                                     \
  }                                                                \
  void setName(const char * name) const {                          \
    IloAssert(_impl != 0, ILO_STRINGIZE(hname) ": empty handle");  \
    _impl->setName(name);                                                \
  }                                                                \
  void setObject(IloAny obj) const {                               \
    IloAssert(_impl != 0, ILO_STRINGIZE(hname) ": empty handle");  \
    _impl->setObject(obj);                                               \
  }                                                                \

//----------------------------------------------------------------------

class IloIntArray;
class IloIntExp;
class IloIntSet;
class IloIntSetVar;
class IloIntSetVarArray;
class IloIntVar;
class IloIntVarArray;
//class IloDiff;
  
class IlcAllocationStack;
class IlcConstraint;
class IlcConstraintArray;
class IlcExprI;
class IlcRecomputeExprI;
class IlcFloatArray;
class IlcFloatExp;
class IlcFloatVar;
class IlcFloatArray;
class IlcFloatVarArray;
class IlcIntArray;
class IlcIntExp;
class IlcIntSet;
class IlcIntSetVar;
class IlcIntSetVarArray;
class IlcIntTupleSet;
class IlcIntVar;
class IlcIntervalVar;
class IlcCumulElementVar;
class IlcIntervalSequenceVar;  
class IlcIntVarArray;
class IlcGoal;
class IlcRandom;
class IloPropagatorI;
class IloGoalI;
class IloCP;
class IloCPI;
class IloSolver;
class IlcStrategyManagerI;
class IlcManagerI;
class IloGoal;
class IloCPHookI;
class IlcFloatExp;
class IlcsSchedule;
class IlcsIntervalVar;
class IlcsSpan;
class IlcsAlternative;
class IloStateFunctionI;

////////////////////////////////////////////////////////////////////////
//
// CUSTOM SEARCH
//
////////////////////////////////////////////////////////////////////////

class IloIntVarEvalI : public IloExtensibleRttiEnvObjectI {
 public:
  IloIntVarEvalI(IloEnv env):
    IloExtensibleRttiEnvObjectI(env.getImpl()){}
  virtual IloNum eval(IloCP cp, IloIntVar x) = 0;
  virtual ~IloIntVarEvalI();
  ILORTTIDECL
};

class IloIntVarEval {
  ILOCPHANDLEMINI(IloIntVarEval, IloIntVarEvalI)
public:
  void end();
};

class IloIntValueEvalI : public IloExtensibleRttiEnvObjectI {
 public:
  IloIntValueEvalI(IloEnv env) :
    IloExtensibleRttiEnvObjectI(env.getImpl()){}
  virtual IloNum eval(IloCP cp, IloIntVar x, IloInt value) = 0;
  virtual ~IloIntValueEvalI();
  ILORTTIDECL
};

class IloIntValueEval {
  ILOCPHANDLEMINI(IloIntValueEval, IloIntValueEvalI)
public:
  void end();
};

class IloVarSelectorI;
class IloValueSelectorI;
class IloVarSelector {
  ILOCPHANDLEMINI(IloVarSelector, IloVarSelectorI)
public:
  void end();
};

typedef IloArray<IloVarSelector> IloVarSelectorArray;

IloVarSelector IloSelectSmallest(IloIntVarEval eval);

IloVarSelector IloSelectSmallest(IloNum minNumber, IloIntVarEval eval);

IloVarSelector IloSelectSmallest(IloIntVarEval eval, IloNum tol);

IloVarSelector IloSelectLargest(IloIntVarEval eval);

IloVarSelector IloSelectLargest(IloNum minNumber, IloIntVarEval eval);

IloVarSelector IloSelectLargest(IloIntVarEval eval, IloNum tol);

IloVarSelector IloSelectRandomVar(IloEnv env);

class IloValueSelector {
  ILOCPHANDLEMINI(IloValueSelector, IloValueSelectorI)
public:
  void end();
};

typedef IloArray<IloValueSelector> IloValueSelectorArray;

IloValueSelector IloSelectSmallest(IloIntValueEval eval);

IloValueSelector IloSelectSmallest(IloNum minNumber, IloIntValueEval eval);

IloValueSelector IloSelectSmallest(IloIntValueEval eval, IloNum tol);

IloValueSelector IloSelectLargest(IloIntValueEval eval);

IloValueSelector IloSelectLargest(IloNum minNumber, IloIntValueEval eval);

IloValueSelector IloSelectLargest(IloIntValueEval eval, IloNum tol);

IloValueSelector IloSelectRandomValue(IloEnv env);

class IloIntVarChooserI : public IloExtensibleRttiEnvObjectI {
 public:
  IloIntVarChooserI(IloEnv env) :
    IloExtensibleRttiEnvObjectI(env.getImpl()){}
  virtual IloInt choose(IloCP cp, IloIntVarArray x) = 0;
  virtual ~IloIntVarChooserI();
  ILORTTIDECL
};

class IloIntVarChooser {
  ILOCPHANDLEMINI(IloIntVarChooser, IloIntVarChooserI)
public:
  IloIntVarChooser(IloVarSelector varSel);
  IloIntVarChooser(IloVarSelectorArray varSelArray);
  IloIntVarChooser(IloEnv env, IloVarSelector varSel);
  IloIntVarChooser(IloEnv env, IloVarSelectorArray varSelArray);
  void end();
};

class IloIntValueChooserI : public IloExtensibleRttiEnvObjectI {
 public:
  IloIntValueChooserI(IloEnv env) :
    IloExtensibleRttiEnvObjectI(env.getImpl()){}
  virtual IloInt choose(IloCP cp, IloIntVarArray x, IloInt index) = 0;
  virtual ~IloIntValueChooserI();
  ILORTTIDECL
};

class IloIntValueChooser {
  ILOCPHANDLEMINI(IloIntValueChooser, IloIntValueChooserI)
public:
  IloIntValueChooser(IloValueSelector valueSel);
  IloIntValueChooser(IloValueSelectorArray valueSelArray);
  IloIntValueChooser(IloEnv env, IloValueSelector valueSel);
  IloIntValueChooser(IloEnv env, IloValueSelectorArray valueSelArray);
  void end();
};

////////////////////////////////////
//  IloSearchPhaseI

class IloSearchPhaseI;
class IloSearchPhase {
  friend class  IlcStrategyManagerI;
  ILOCPHANDLE(IloSearchPhase, IloSearchPhaseI)
 public:
  void end();
  IloSearchPhase(IloEnv env,
                 IloIntVarArray vars,
                 IloIntVarChooser varChooser,
                 IloIntValueChooser valueChooser);

  IloSearchPhase(IloEnv env,
                 IloIntVarArray vars);
  IloSearchPhase(IloEnv env,
                 IloIntVarChooser varChooser,
                 IloIntValueChooser valueChooser);

  IloSearchPhase(IloEnv env, IloIntervalVarArray intervalVars);

  IloSearchPhase(IloEnv env, IloIntervalSequenceVarArray sequenceVars);

};

// Undocumented:
IloSearchPhase IloFixPresenceSearchPhase(IloEnv env, IloIntervalVarArray intervalVars);

typedef IloArray<IloSearchPhase> IloSearchPhaseArray;

IloIntValueEval IloExplicitValueEval(IloEnv env,
                                     IloIntArray valueArray,
                                     IloIntArray evalArray,
                                     IloNum defaultEval = 0);
IloIntValueEval IloExplicitValueEval(IloEnv env,
                                     IloIntArray valueArray,
                                     IloNumArray evalArray,
                                     IloNum defaultValue = 0);
IloIntValueEval IloValueIndex(IloEnv env,
                              IloIntArray valueArray,
                              IloInt defaultEval = -1);
IloIntValueEval IloValue(IloEnv env);
IloIntValueEval IloValueImpact(IloEnv env);
IloIntValueEval IloValueSuccessRate(IloEnv env);
IloIntValueEval IloValueLocalImpact(IloEnv env);

IloIntValueEval IloValueLowerObjVariation(IloEnv env);
IloIntValueEval IloValueUpperObjVariation(IloEnv env);
IloIntVarEval IloVarIndex(IloEnv env, IloIntVarArray x, IloInt defaultEval = -1);
IloIntVarEval IloExplicitVarEval(IloEnv env, IloIntVarArray x, IloIntArray evalArray, IloNum defaultEval = 0);
IloIntVarEval IloExplicitVarEval(IloEnv env, IloIntVarArray x, IloNumArray evalArray, IloNum defaultEval = 0);
IloIntVarEval IloDomainMin(IloEnv env);
IloIntVarEval IloDomainMax(IloEnv env);
IloIntVarEval IloDomainSize(IloEnv env);
IloIntVarEval IloVarSuccessRate(IloEnv env);
IloIntVarEval IloVarImpact(IloEnv env);
IloIntVarEval IloVarLocalImpact(IloEnv env, IloInt effort = -1);
IloIntVarEval IloImpactOfLastBranch(IloEnv env);

IloIntVarEval IloRegretOnMin(IloEnv env);
IloIntVarEval IloRegretOnMax(IloEnv env);
IloIntVarEval IloVarLowerObjVariation(IloEnv env);
IloIntVarEval IloVarUpperObjVariation(IloEnv env);

////////////////////////////////////////////////////////////////////////
//
// ILOCP
//
////////////////////////////////////////////////////////////////////////
  
class IlcIntExpI;
class IlcFloatExpI;
 
class IloCP : public IloAlgorithm {
private:
  void    _ctor(const IloModel model);
  void    _ctor(const IloEnv env);
  void    _abortSearch() const;
  void    _clearAbort() const;
  void    _exitSearch() const;
  void    _fail(IloAny label) const;
  void    _freeze() const;
  void    _unfreeze() const;
  void    _getBounds(const IloIntVar var, IloInt& min, IloInt& max) const;
  IloInt  _getDegree(const IloIntVar var) const;
  IlcAllocationStack * _getHeap() const;
  IloNum  _getImpactOfLastAssignment(const IloIntVar var) const;
  IloNum  _getImpact(const IloIntVar var) const;
  IloNum  _getImpact(const IloIntVar var, IloInt value) const;
  IloNum  _getSuccessRate(const IloIntVar var) const;
  IloNum  _getSuccessRate(const IloIntVar var, IloInt value) const;
  IloNum  _getNumberOfFails(const IloIntVar var, IloInt value) const;
  IloNum  _getNumberOfInstantiations(const IloIntVar var, IloInt value) const;
  IloNum  _getLocalImpact(const IloIntVar var, IloInt value) const;
  IloNum  _getLocalVarImpact(const IloIntVar var, IloInt depth) const;
  IlcFloatArray _getFloatArray(IloNumArray arg) const;
  IlcIntArray _getIntArray(IloIntArray arg) const;
  IlcIntArray _getIntArray(IloNumArray arg) const;
  IloInt  _getMax(const IloIntVar var) const;
  IloNum  _getMax(const IloNumVar var) const;
  IloMemoryManager _getReversibleAllocator() const;
  IloMemoryManager _getSolveAllocator() const;
  IloMemoryManager _getPersistentAllocator() const;
  IloInt  _getMin(const IloIntVar var) const;
  IloNum  _getMin(const IloNumVar var) const;
  IlcAllocationStack * _getPersistentHeap() const;
  IlcAllocationStack * _getSolveHeap() const;
  IloInt  _getRandomInt(IloInt n) const;
  IloNum  _getRandomNum() const;
  IloInt  _getReduction(const IloIntVar var) const;
  IloNum  _getValue(const IloNumVar var) const;
  IloInt  _getValue(const IloIntVar var) const;
  IloIntSet  _getValue(const IloIntSetVar var) const;
  IloIntSet  _getRequired(const IloIntSetVar var) const;
  IloIntSet  _getPossible(const IloIntSetVar var) const;
  const char * _getVersion() const;
  IloBool _isFixed(const IloIntVar var) const;
  IloBool _isFixed(const IloNumVar var) const;
  IloBool _isFixed(const IloIntSetVar var) const;
  IloBool _isAllFixed() const;
  IloInt  _getDomainSize(const IloNumVar var) const;
  IloBool _isInDomain(const IloIntVar var, IloInt value) const;
  void     _printInformation() const;
  void     _printInformation(ILOSTD(ostream) & o) const;
  void     _printPortableInformation() const;
  void     _printPortableInformation(ILOSTD(ostream) & o) const;
  IloBool _propagate(const IloConstraint ct) const;
  void    _removeValueBuffered(IloNumVarI * var, IloInt value) const;
  void    _setMinBuffered(IloNumVarI * var, IloNum min) const;
  void    _setMaxBuffered(IloNumVarI * var, IloNum max) const;
  void    _setInferenceLevel(IloConstraint ct, IloInt level) const;
  IloInt  _getInferenceLevel(IloConstraint ct) const;
  void    _resetConstraintInferenceLevels() const;
  void    _setSearchPhases() const;
  void    _setSearchPhases(const IloSearchPhase phase) const;
  void    _setSearchPhases(const IloSearchPhaseArray phaseArray) const;
  void    _setStartingPoint(const IloSolution ws) const;
  void    _clearStartingPoint() const;
  IloBool _solve(const IloSearchPhaseArray phaseArray) const;
  IloBool _solve(const IloSearchPhase phase) const;
  IloBool _solve() const;
  void    _startNewSearch(const IloSearchPhaseArray phaseArray) const;
  void    _startNewSearch(const IloSearchPhase phase) const;
  void    _startNewSearch() const;
  IloBool  _next() const;
  void    _endSearch() const;
  IloArray<IloConstraintArray> _findDisjointConflicts(IloInt conflictLimit) const;
  IloBool _isInReplay() const;
  void    _store(IloSolution solution) const;
  IloBool _restore(IloSolution solution) const;
  void _saveValue(IloAny * ptr) const;
  void _saveValue(IloNum * ptr) const;
  void _setNodeHook(IloCPHookI * hook) const;

  void _printDomain(const IloNumVar var) const;
  void _printDomain(const IloNumVarArray vars) const;
  void _printDomain(const IloIntVarArray vars) const;
  void _printDomain(const IloIntSetVar var) const;
  void _printDomain(const IloIntSetVarArray vars) const;

  IloBool _isFixed(const IloCumulFunctionExpr cumul) const;
  IloInt _getNumberOfSegments(const IloCumulFunctionExpr cumul) const;
  IloBool _isValidSegment(const IloCumulFunctionExpr cumul, IloInt s) const;
  IloInt _getSegmentStart(const IloCumulFunctionExpr cumul, IloInt s) const;
  IloInt _getSegmentEnd(const IloCumulFunctionExpr cumul, IloInt s) const;
  IloInt _getSegmentValue(const IloCumulFunctionExpr cumul, IloInt s) const;
  IloBool _isValidAbscissa(const IloCumulFunctionExpr cumul, IloInt a) const;
  IloInt _getValue(const IloCumulFunctionExpr cumul, IloInt a) const;

  IloNum _getNumberOfSegmentsAsNum(const IloCumulFunctionExpr cumul) const;
  IloNum _getSegmentStartAsNum(const IloCumulFunctionExpr cumul, IloInt s) const;
  IloNum _getSegmentEndAsNum(const IloCumulFunctionExpr cumul, IloInt s) const;
  IloNum _getSegmentValueAsNum(const IloCumulFunctionExpr cumul, IloInt s) const;
  IloNum _getValueAsNum(const IloCumulFunctionExpr cumul, IloInt a) const;
  
  IloBool _isFixed(const IloStateFunction f) const;
  IloInt _getNumberOfSegments(const IloStateFunction f) const;
  IloBool _isValidSegment(const IloStateFunction f, IloInt s) const;
  IloInt _getSegmentStart(const IloStateFunction f, IloInt s) const;
  IloInt _getSegmentEnd(const IloStateFunction f, IloInt s) const;
  IloInt _getSegmentValue(const IloStateFunction f, IloInt s) const;
  IloBool _isValidAbscissa(const IloStateFunction f, IloInt a) const;
  IloInt _getValue(const IloStateFunction f, IloInt a) const;

  IloNum _getNumberOfSegmentsAsNum(const IloStateFunction f) const;
  IloNum _getSegmentStartAsNum(const IloStateFunction f, IloInt s) const;
  IloNum _getSegmentEndAsNum(const IloStateFunction f, IloInt s) const;
  IloNum _getSegmentValueAsNum(const IloStateFunction f, IloInt s) const;
  IloNum _getValueAsNum(const IloStateFunction f, IloInt a) const;
  
  IloInt _getNumberOfSegments(const IloStateFunctionExpr expr) const;
  IloInt _getSegmentStart(const IloStateFunctionExpr expr, IloInt s) const;
  IloInt _getSegmentEnd(const IloStateFunctionExpr expr, IloInt s) const;
  IloInt _getSegmentValue(const IloStateFunctionExpr expr, IloInt s) const;
  IloInt _getValue(const IloStateFunctionExpr expr, IloInt a) const;

  IloBool _isAllExtracted(const IloExtractableArray ex) const;
  IloBool _isAllValid(const IloExtractableArray ex) const;
  IloBool _hasObjective() const;

  IlcManagerI * _getManagerI() const;

  static void _RegisterXML(IloEnv env);
public:
  ///////////////////////////////////////////////////////////////////////////
  // Parameters
  ///////////////////////////////////////////////////////////////////////////
  enum IntParam {
    DefaultInferenceLevel = 1,
    AllDiffInferenceLevel = 2,
    DistributeInferenceLevel = 3,
    CountInferenceLevel = 4,
    SequenceInferenceLevel = 5,
    AllMinDistanceInferenceLevel = 6,
    ElementInferenceLevel = 7,

    ConstraintAggregation = 8,

    FailLimit = 9,
    ChoicePointLimit = 10,

    LogVerbosity = 11,
    LogPeriod = 12,

     SearchType = 13,

    RandomSeed = 14,
    RestartFailLimit = 15,

    MultiPointNumberOfSearchPoints = 16,

    MultiPointEncodingPercentage = 17,
    ImpactMeasures = 18,

    // Hole of size 3: can be filled later

    PackApproximationSize = 22,
    StrictNumericalDivision = 23,
    FloatDisplay = 24,

    Workers = 25,
    ParallelPolicy = 26,

    PropagationLog = 27,

    BranchLimit = 28,


    AutomaticReplay = 29,

    SeedRandomOnSolve = 30,
    TraceExtraction = 31,
    DynamicProbing = 32,

    ConflictLimit = 33,
    TimeDisplay = 34,


    SolutionLimit = 35,

    PresolveLevel = 36,
    ObjectiveLimit = 37,

    PrecedenceInferenceLevel = 38,

    IntervalSequenceInferenceLevel = 39,

    NoOverlapInferenceLevel = 40,

    CumulFunctionInferenceLevel = 41,

     StateFunctionInferenceLevel = 42,

    TimeMode = 43,

    TemporalRelaxation = 44,

    // Undocumented
    TemporalRelaxationLevel = 45,
    TemporalRelaxationRowLimit = 46,
    TemporalRelaxationIterationLimit = 47,
    IncrementalDisjunctive = 48,
    SetTimesDominanceRule = 49,
    SequenceExpressionInferenceLevel = 50,
    StateFunctionTriangularInequalityCheck = 51
  };


  enum NumParam {
    OptimalityTolerance = 1001,
    RelativeOptimalityTolerance,
    TimeLimit,
    RestartGrowthFactor,
    DynamicProbingStrength
  };

  enum IntInfo {
    NumberOfChoicePoints = 1,
    NumberOfFails = 2,
    NumberOfBranches = 3,
    NumberOfModelVariables = 4,
    NumberOfAuxiliaryVariables = 5,
    NumberOfVariables = 6,
    NumberOfConstraints = 7,
    MemoryUsage = 8,
    NumberOfConstraintsAggregated = 9,
    NumberOfConstraintsGenerated = 10,
    FailStatus = 11,
    NumberOfIntegerVariables = 12,
    NumberOfIntervalVariables = 13,
    NumberOfSequenceVariables = 14,
    NumberOfSolutions = 15
  };
  enum NumInfo {
    SolveTime = 1001,
    ExtractionTime,
    TotalTime,
    EffectiveOptimalityTolerance
  };

  enum ParameterValues {
    // Auto
    Auto     = -1,

    // On / Off
    Off      = 0,
    On       = 1,

    // Inference levels
    Default,
    Low,
    Basic,
    Medium,
    Extended,

    // Float display
    Standard,
    IntScientific,
    IntFixed,
    BasScientific,
    BasFixed,

    // Failure status
    SearchHasNotFailed,
    SearchHasFailedNormally,
    SearchStoppedByLimit,
    SearchStoppedByLabel,
    SearchStoppedByExit,
    SearchStoppedByAbort,
    UnknownFailureStatus,

    // Log verbosity
    Quiet,
    Terse,
    Normal,
    Verbose,

    // Search type
    DepthFirst,
    Restart,
    MultiPoint,

    // Parallelism type
    Diverse,
    Focused,
    Intensive,

    // Time display format
    Seconds,
    HMS,
    NoTime,

    // Time mode
    CPUTime,
    ElapsedTime
  };

private:
  void   _setParameter(IloCP::IntParam param, IloInt value) const;
  void   _setParameter(IloCP::NumParam param, IloNum value) const;
  IloInt _getParameter(IloCP::IntParam param) const;
  IloNum _getParameter(IloCP::NumParam param) const;
  IloInt _getParameterDefault(IloCP::IntParam param) const;
  IloNum _getParameterDefault(IloCP::NumParam param) const;
  IloInt _getInfo(IloCP::IntInfo info) const;
  IloNum _getInfo(IloCP::NumInfo info) const;

  void   _setParameter(const char * name, IloNum value) const;
  void   _setParameter(const char * name, const char * value) const;
  IloNum _getParameter(const char * name) const;
  IloNum _getParameterDefault(const char * name) const;
  IloNum _getInfo(const char * name) const;

public:
  void setParameter(IloCP::IntParam param, IloInt value) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    _setParameter(param, value);
  }
  IloInt getParameter(IloCP::IntParam param) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    return _getParameter(param);
  }
  IloInt getParameterDefault(IloCP::IntParam param) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    return _getParameterDefault(param);
  }
  void setParameter(IloCP::NumParam param, IloNum value) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    _setParameter(param, value);
  }
  void setParameter(const char * name, IloNum value) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(name  != 0, "IloCP::setParameter - empty name");
    _setParameter(name, value);
  }
  void setParameter(const char * name, const char * value) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(name  != 0, "IloCP::setParameter - empty name");
    IloAssert(value != 0, "IloCP::setParameter - empty value");
    _setParameter(name, value);
  }
  IloNum getParameter(IloCP::NumParam param) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    return _getParameter(param);
  }
  IloNum getParameter(const char * name) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(name  != 0, "IloCP::getParameter - empty name");
    return _getParameter(name);
  }
  IloNum getParameterDefault(IloCP::NumParam param) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    return _getParameterDefault(param);
  }
  IloNum getParameterDefault(const char * name) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(name  != 0, "IloCP::getParameterDefault - empty name");
    return _getParameterDefault(name);
  }

  IloInt getInfo(IloCP::IntInfo info) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    return _getInfo(info);
  }
  IloNum getInfo(IloCP::NumInfo info) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    return _getInfo(info);
  }
  IloNum getInfo(const char * name) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(name  != 0, "IloCP::getNumInfo - empty name");
    return _getInfo(name);
  }

  ///////////////////////////////////////////////////////////////////////////
  // Constructors, extraction and related methods
  ///////////////////////////////////////////////////////////////////////////
  IloCP(const IloEnv env) {
    IloAssert(env.getImpl() != 0, "IloEnv: empty handle");
    _ctor(env);
  }
  IloCP(const IloModel model) {
    IloAssert(model.getImpl() != 0, "IloModel: empty handle");
    _ctor(model);
  }
#ifdef CPPREF_GENERATION
void extract (const IloModel model) const;
 IloBool isExtracted(const IloExtractable ext) const;
 void end();
#endif
  IloCPI * getImpl() const {
    return (IloCPI*)_impl;
  }
  IloCP(IloCPI * impl=0) : IloAlgorithm((IloAlgorithmI *)impl) { }

  IloBool isAllExtracted(const IloExtractableArray ext) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(ext.getImpl() != 0, "IloExtractableArray: empty handle");
    return _isAllExtracted(ext);
  }
  IloBool isAllValid(const IloExtractableArray ext) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(ext.getImpl() != 0, "IloExtractableArray: empty handle");
    return _isAllValid(ext);
  }
  IloBool hasObjective() const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    return _hasObjective();
  }
  ///////////////////////////////////////////////////////////////////////////
  // Solving
  ///////////////////////////////////////////////////////////////////////////
  void setSearchPhases(){
    IloAssert(_impl != 0, "IloCP: empty handle");
    return _setSearchPhases();
  }
  void setSearchPhases(IloSearchPhase phase){
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(phase.getImpl() != 0, "IloSearchPhase: empty handle");
    return _setSearchPhases(phase);
  }
  void setSearchPhases(IloSearchPhaseArray phaseArray){
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(phaseArray.getImpl() != 0, "IloSearchPhaseArray: empty handle");
    return _setSearchPhases(phaseArray);
  }

  void setStartingPoint(const IloSolution sp) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(sp.getImpl() != 0, "IloSolution: empty handle");
    _setStartingPoint(sp);
  }

  void clearStartingPoint() const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    _clearStartingPoint();
  }

  IloBool solve(const IloGoal goal) const;
  IloBool solve() const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    return _solve();
  }
  IloBool solve(const IloSearchPhaseArray phaseArray) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(phaseArray.getImpl() != 0, "IloSearchPhaseArray: empty handle");
    return _solve(phaseArray);
  }
  IloBool solve(const IloSearchPhase phase) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(phase.getImpl() != 0, "IloSearchPhase: empty handle");
    return _solve(phase);
  }
  void startNewSearch(const IloGoal goal) const;
  void startNewSearch() const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    _startNewSearch();
  }
  void startNewSearch(const IloSearchPhaseArray phaseArray) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(phaseArray.getImpl() != 0, "IloSearchPhaseArray: empty handle");
    _startNewSearch(phaseArray);
  }
  void startNewSearch(const IloSearchPhase phase) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(phase.getImpl() != 0, "IloSearchPhase: empty handle");
    _startNewSearch(phase);
  }
  IloBool next() const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    return _next();
  }
  IloBool isInReplay() const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    return _isInReplay();
  }
  void endSearch() const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    _endSearch();
  }
  IloArray<IloConstraintArray> findDisjointConflicts(IloInt limit = IloIntMax) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(limit >= 0, "IloCP::findDisjointConflicts - conflict limit is negative");
    return _findDisjointConflicts(IloMax(limit, 1));
  }
  IloBool propagate(const IloConstraint constraint = 0) {
    IloAssert(_impl != 0, "IloCP: empty handle");
    return _propagate(constraint);
  }
  void store(IloSolution solution) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(solution.getImpl() != 0, "IloSolution: empty handle");
    _store(solution);
  }
  IloBool restore(IloSolution solution) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(solution.getImpl() != 0, "IloSolution: empty handle");
    return _restore(solution);
  }
  void printInformation() const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    _printInformation();
  }
  void printInformation(ILOSTD(ostream)& stream) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    _printInformation(stream);
  }

  void printPortableInformation() const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    _printPortableInformation();
  }
  void printPortableInformation(ILOSTD(ostream)& stream) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    _printPortableInformation(stream);
  }

  void printDomain(const IloNumVar var) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(var.getImpl() != 0, "IloNumVar: empty handle");
    IloAssert(isExtracted(var), "IloNumVar: not extracted");
    _printDomain(var);
  }
  void printDomain(const IloNumVarArray vars) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(vars.getImpl() != 0, "IloNumVarArray: empty handle");
    IloAssert(isAllValid(vars), "IloNumVarArray: empty element handle");
    IloAssert(isAllExtracted(vars), "IloNumVarArray: element not extracted");
    _printDomain(vars);
  }
  void printDomain(const IloIntVarArray vars) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(vars.getImpl() != 0, "IloIntVarArray: empty handle");
    IloAssert(isAllValid(vars), "IloIntVarArray: empty element handle");
    IloAssert(isAllExtracted(vars), "IloIntVarArray: element not extracted");
    _printDomain(vars);
  }
  void printDomain(const IloIntSetVar var) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(var.getImpl() != 0, "IloIntSetVar: empty handle");
    IloAssert(isExtracted(var), "IloIntSetVar: not extracted");
    _printDomain(var);
  }
  void printDomain(const IloIntSetVarArray vars) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(vars.getImpl() != 0, "IloIntSetVarArray: empty handle");
    IloAssert(isAllValid(vars), "IloIntSetVarArray: empty element handle");
    IloAssert(isAllExtracted(vars), "IloIntSetVarArray: element not extracted");
    _printDomain(vars);
  }

  class PrintDomains {
  protected:
    const IloCPI *     _cp;
    IloInt             _n;
    IloExtractableI ** _var;

    PrintDomains() { }
    PrintDomains(const IloCP cp, const IloExtractable ext);
    PrintDomains(const IloCP cp, const IloExtractableArray ext);
    PrintDomains(const PrintDomains &);
  public:
    ~PrintDomains();
  };

  class PrintNumVarDomains : public PrintDomains {
    friend class IloCP;
  private:
    void operator = (const PrintNumVarDomains &);

    PrintNumVarDomains(const IloCP cp, const IloNumVar var);
    PrintNumVarDomains(const IloCP cp, const IloNumVarArray var);
    PrintNumVarDomains(const IloCP cp, const IloIntVarArray var);
  public:
    void display(ILOSTD(ostream)& o) const;
  };

  class PrintIntSetVarDomains : public PrintDomains {
    friend class IloCP;
  private:
    void operator = (const PrintIntSetVarDomains &);

    PrintIntSetVarDomains(const IloCP cp, const IloIntSetVar var);
    PrintIntSetVarDomains(const IloCP cp, const IloIntSetVarArray var);
  public:
    void display(ILOSTD(ostream)& o) const;
  };

    PrintNumVarDomains domain(const IloNumVar var) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(var.getImpl() != 0, "IloNumVar: empty handle");
    IloAssert(isExtracted(var), "IloNumVar: not extracted");
    return PrintNumVarDomains(*this, var);
  }
    PrintNumVarDomains domain(const IloNumVarArray vars) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(vars.getImpl() != 0, "IloNumVarArray: empty handle");
    IloAssert(isAllValid(vars), "IloNumVarArray: empty element handle");
    IloAssert(isAllExtracted(vars), "IloNumVarArray: element not extracted");
    return PrintNumVarDomains(*this, vars);
  }
  PrintNumVarDomains domain(const IloIntVarArray vars) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(vars.getImpl() != 0, "IloIntVarArray: empty handle");
    IloAssert(isAllValid(vars), "IloIntVarArray: empty element handle");
    IloAssert(isAllExtracted(vars), "IloIntVarArray: element not extracted");
    return PrintNumVarDomains(*this, vars);
  }
  // NO DOC
  PrintIntSetVarDomains domain(const IloIntSetVar var) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(var.getImpl() != 0, "IloIntSetVar: empty handle");
    IloAssert(isExtracted(var), "IloIntSetVar: not extracted");
    return PrintIntSetVarDomains(*this, var);
  }
  // NO DOC
  PrintIntSetVarDomains domain(const IloIntSetVarArray vars) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(vars.getImpl() != 0, "IloIntSetVarArray: empty handle");
    IloAssert(isAllValid(vars), "IloIntSetVarArray: empty element handle");
    IloAssert(isAllExtracted(vars), "IloIntSetVarArray: element not extracted");
    return PrintIntSetVarDomains(*this, vars);
  }

  ///////////////////////////////////////////////////////////////////////////
  // Hooks
  ///////////////////////////////////////////////////////////////////////////
  void setNodeHook(IloCPHookI * hook = 0) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    _setNodeHook(hook);
  }

  ///////////////////////////////////////////////////////////////////////////
  // Getting solution information
  ///////////////////////////////////////////////////////////////////////////
  // Mimic IloAlgorithm as CP Optimizer has its own getValue functions
  IloNum getValue(const IloObjective obj) const {
    return IloAlgorithm::getValue(obj);
  }
  IloNum getValue(const IloNumExprArg expr) const {
    return IloAlgorithm::getValue(expr);
  }

  IloNum getValue(const IloNumVar v) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(v.getImpl() != 0, "IloNumVar: empty handle");
    IloAssert(isExtracted(v), "IloNumVar: not extracted");
    IloAssert(isFixed(v), "IloNumVar: not fixed");
    return _getValue(v);
  }
  IloInt getValue(const IloIntVar v) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(v.getImpl() != 0, "IloIntVar: empty handle");
    IloAssert(isExtracted(v), "IloIntVar: not extracted");
    IloAssert(isFixed(v), "IloIntVar: not fixed");
    return _getValue(v);
  }
  // 2.0b1
  IloAny getAnyValue(const IloIntVar v) const { return (IloAny)getValue(v); }
  IloNum getMin(const IloNumVar v) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(v.getImpl() != 0, "IloNumVar: empty handle");
    IloAssert(isExtracted(v), "IloNumVar: not extracted");
    return _getMin(v);
  }
  IloNum            getMax(const IloNumVar v) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(v.getImpl() != 0, "IloNumVar: empty handle");
    IloAssert(isExtracted(v), "IloNumVar: not extracted");
    return _getMax(v);
  }
  IloInt            getMax(const IloIntVar var) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(var.getImpl() != 0, "IloIntVar: empty handle");
    IloAssert(isExtracted(var), "IloIntVar: not extracted");
    return _getMax(var);
  }
  IloInt            getMin(const IloIntVar var) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(var.getImpl() != 0, "IloIntVar: empty handle");
    IloAssert(isExtracted(var), "IloIntVar: not extracted");
    return _getMin(var);
  }
  void getBounds(const IloIntVar var, IloInt& min, IloInt& max) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(var.getImpl() != 0, "IloIntVar: empty handle");
    IloAssert(isExtracted(var), "IloIntVar: not extracted");
    _getBounds(var, min, max);
  }
  IloBool isInDomain(const IloNumVar var, IloInt value) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(var.getImpl() != 0, "IloNumVar: empty handle");
    IloAssert(isExtracted(var), "IloNumVar: not extracted");
    IloAssert(var.getType() != ILOFLOAT, "IloNumVar: not integer");
    return _isInDomain(IloIntVar(var.getImpl()), value);
  }
  IloBool isInDomain(const IloIntVar var, IloInt value) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(var.getImpl() != 0, "IloIntVar: empty handle");
    IloAssert(isExtracted(var), "IloIntVar: not extracted");
    return _isInDomain(var, value);
  }
  IloInt getDomainSize(const IloNumVar var) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(var.getImpl() != 0, "IloNUmVar: empty handle");
    IloAssert(isExtracted(var), "IloNumVar: not extracted");
    IloAssert(var.getType() != ILOFLOAT, "IloNumVar: not integer");
    return _getDomainSize(var);
  }
  IloBool isFixed(const IloNumVar var) const  {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(var.getImpl() != 0, "IloNumVar: empty handle");
    IloAssert(isExtracted(var), "IloNumVar: not extracted");
    return _isFixed(var);
  }
  IloBool isFixed(const IloIntVar var) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(var.getImpl() != 0, "IloIntVar: empty handle");
    IloAssert(isExtracted(var), "IloIntVar: not extracted");
    return _isFixed(var);
  }
  IloBool isAllFixed() const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    return _isAllFixed();
  }
  IloBool isFixed(const IloIntSetVar var) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(var.getImpl() != 0, "IloIntSetVar: empty handle");
    IloAssert(isExtracted(var), "IloIntSetVar: not extracted");
    return _isFixed(var);
  }
  IloIntSet getValue(const IloIntSetVar var) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(var.getImpl() != 0, "IloIntSetVar: empty handle");
    IloAssert(isExtracted(var), "IloIntSetVar: not extracted");
    IloAssert(isFixed(var), "IloIntSetVar: not extracted");
    return _getValue(var);
  }
  IloIntSet getRequired(const IloIntSetVar var) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(var.getImpl() != 0, "IloIntSetVar: empty handle");
    IloAssert(isExtracted(var), "IloIntSetVar: not extracted");
    return _getRequired(var);
  }
  IloIntSet getPossible(const IloIntSetVar var) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(var.getImpl() != 0, "IloIntSetVar: empty handle");
    IloAssert(isExtracted(var), "IloIntSetVar: not extracted");
    return _getPossible(var);
  }
  class IntVarIterator {
  private:
    class IlcIntVarI * _var;
    IloInt       _curr;
    IloBool      _ok;

    void _init(IloCP cp, IloIntVar var);
  public:
    IntVarIterator() : _var(0) {}
    IntVarIterator(IloCP cp, IloIntVar var) {
      IloAssert(cp.getImpl() != 0, "IloCP: empty handle");
      IloAssert(var.getImpl() != 0, "IloIntVar: empty handle");
      _init(cp, var);
    }
    IntVarIterator(IloCP cp, IloNumVar var) {
      IloAssert(cp.getImpl() != 0, "IloCP: empty handle");
      IloAssert(var.getImpl() != 0, "IloNumVar: empty handle");
      IloAssert(var.getType() != ILOFLOAT, "IloNumVar: not integer");
      _init(cp, IloIntVar(var.getImpl()));
    }
    IntVarIterator& operator++();
    IloInt operator*() const { return _curr; }
    // 2.0b1
    IloAny getAnyValue() const { return (IloAny)_curr; }
    IloBool ok() const { return _ok; }
  };
  IloCP::IntVarIterator iterator(IloIntVar var) {
    IloAssert(var.getImpl() != 0, "IloIntVar: empty handle");
    return IloCP::IntVarIterator(*this, var);
  }
  IloCP::IntVarIterator iterator(IloNumVar var) {
    IloAssert(var.getImpl() != 0, "IloNumVar: empty handle");
    IloAssert(var.getType() != ILOFLOAT, "IloNumVar: not integer");
    return IloCP::IntVarIterator(*this, var);
  }

  ///////////////////////////////////////////////////////////////////////////
  // Search information
  ///////////////////////////////////////////////////////////////////////////
  IloInt getReduction(const IloIntVar x) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(x.getImpl() != 0, "IloIntVar: empty handle");
    IloAssert(isExtracted(x), "IloIntVar: not extracted");
    return _getReduction(x);
  }
  IloNum getImpactOfLastAssignment(const IloIntVar x) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(x.getImpl() != 0, "IloIntVar: empty handle");
    IloAssert(isExtracted(x), "IloIntVar: not extracted");
    return _getImpactOfLastAssignment(x);
  }
  IloNum getImpact(const IloIntVar x) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(x.getImpl() != 0, "IloIntVar: empty handle");
    IloAssert(isExtracted(x), "IloIntVar: not extracted");
    return _getImpact(x);
  }
  IloNum getImpact(const IloIntVar x, IloInt value) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(x.getImpl() != 0, "IloIntVar: empty handle");
    IloAssert(isExtracted(x), "IloIntVar: not extracted");
    return _getImpact(x, value);
  }
  IloNum getSuccessRate(const IloIntVar x) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(x.getImpl() != 0, "IloIntVar: empty handle");
    IloAssert(isExtracted(x), "IloIntVar: not extracted");
    return _getSuccessRate(x);
  }
  IloNum getSuccessRate(const IloIntVar x, IloInt value) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(x.getImpl() != 0, "IloIntVar: empty handle");
    IloAssert(isExtracted(x), "IloIntVar: not extracted");
    return _getSuccessRate(x, value);
  }
  IloNum getNumberOfFails(const IloIntVar x, IloInt value) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(x.getImpl() != 0, "IloIntVar: empty handle");
    IloAssert(isExtracted(x), "IloIntVar: not extracted");
    return _getNumberOfFails(x, value);
  }
  IloNum getNumberOfInstantiations(const IloIntVar x, IloInt value) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(x.getImpl() != 0, "IloIntVar: empty handle");
    IloAssert(isExtracted(x), "IloIntVar: not extracted");
    return _getNumberOfInstantiations(x, value);
  }
  IloNum getLocalImpact(const IloIntVar x, IloInt value) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(x.getImpl() != 0, "IloIntVar: empty handle");
    IloAssert(isExtracted(x), "IloIntVar: not extracted");
    return _getLocalImpact(x, value);
  }
  IloNum getLocalVarImpact(const IloIntVar x, IloInt depth) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(x.getImpl() != 0, "IloIntVar: empty handle");
    IloAssert(isExtracted(x), "IloIntVar: not extracted");
    return _getLocalVarImpact(x, depth);
  }

  ///////////////////////////////////////////////////////////////////////////
  // Services
  ///////////////////////////////////////////////////////////////////////////
  IloInt getRandomInt(IloInt n) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(n >= 1, "IloCP::getRandomInt(n): n < 1");
    return _getRandomInt(n);
  }
  IloNum getRandomNum() const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    return _getRandomNum();
  }
  const char* getVersion() const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    return _getVersion();
  }
  static const char* GetVersion();

  ///////////////////////////////////////////////////////////////////////////
  // Advanced: Ilc mapping
  //           No inlining here to avoid Ilc/Ilo world crossover
  ///////////////////////////////////////////////////////////////////////////
  IlcIntVar getIntVar(const IloNumVar var) const;
  IlcIntVar getIntVar(const IloIntVar var) const;
  IlcIntVar getIntVar(const IloNumVarI* var) const;
  IlcIntervalVar getInterval(const IloIntervalVar var) const;
  IlcCumulElementVar getCumulElement(const IloCumulFunctionExpr f) const;
  IlcIntervalSequenceVar getIntervalSequence(const IloIntervalSequenceVar s) const;
  IlcIntArray getIntArray(const IloNumArray arg) const;
  IlcIntArray getIntArray(const IloIntArray arg) const;
  IlcFloatArray getFloatArray(const IloNumArray arg) const;
  IlcIntSet getIntSet(const IloIntSet arg) const;
  IlcIntSet getIntSet(const IloNumSet arg) const;
  IlcFloatVar getFloatVar(const IloNumVar var) const;
  IlcIntVarArray getIntVarArray(const IloIntVarArray vars) const;
  IlcIntVarArray getIntVarArray(const IloNumVarArray vars) const;
  IlcIntVarArray getIntVarArray(const IloIntExprArray exps) const;
  IlcFloatVarArray getFloatVarArray(const IloNumVarArray vars) const;
  IlcIntSetVar getIntSetVar(const IloIntSetVar var) const;
  IlcIntSetVarArray getIntSetVarArray(const IloIntSetVarArray vars) const;
  IlcIntExp getIntExp(const IloIntExprArg expr) const;
  IlcFloatExp getFloatExp(const IloNumExprArg expr) const;
  IlcIntTupleSet getIntTupleSet(const IloIntTupleSet ts) const;

  ///////////////////////////////////////////////////////////////////////////
  // Advanced
  ///////////////////////////////////////////////////////////////////////////
  void startNewSearch(const IlcGoal goal) const;
  void fail(IloAny label=0) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    _fail(label);
  }
  void freeze() const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    _freeze();
  }
  void unfreeze() const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    _unfreeze();
  }
  IloBool solve(const IlcGoal goal, IloBool restore = IloFalse) const;
  void add(const IlcConstraint constraint) const;
  void add(const IlcConstraintArray constraints) const;
  IlcAllocationStack * getHeap() const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    return _getHeap();
  }
  IlcRandom getRandom() const;
  void setInferenceLevel(IloConstraint ct, IloInt level) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(ct.getImpl() != 0, "IloConstraint: empty handle");
    _setInferenceLevel(ct, level);
  }
  IloInt getInferenceLevel(IloConstraint ct) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    return _getInferenceLevel(ct);
  }
  void resetInferenceLevels() const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    _resetConstraintInferenceLevels();
  }

  ///////////////////////////////////////////////////////////////////////////
  // Low-level, advanced
  ///////////////////////////////////////////////////////////////////////////
  // No wrapping
  void saveValue(IloAny * ptr) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(ptr != 0, "IloCP: saveValue must receive non-null pointer");
    _saveValue(ptr);
  }
  // No wrapping
  void saveValue(IloInt * ptr) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(ptr != 0, "IloCP: saveValue must receive non-null pointer");
    _saveValue((IloAny*)ptr);
  }
  // No wrapping
  void saveValue(IloNum * ptr) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(ptr != 0, "IloCP: saveValue must receive non-null pointer");
    _saveValue(ptr);
  }
  void abortSearch() const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    _abortSearch();
  }
  void clearAbort() const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    _clearAbort();
  }
  void exitSearch() const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    _exitSearch();
  }
  IlcAllocationStack* getPersistentHeap() const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    return _getPersistentHeap();
  }
  void addReversibleAction(const IlcGoal goal) const;

  // For propagator
  void removeValueBuffered(IloNumVarI * var, IloInt value) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(var != 0, "IloNumVar: empty handle");
    IloAssert(var->getType() != ILOFLOAT, "IloNumVar: not integer");
    _removeValueBuffered(var, value);
  }
  void setMinBuffered(IloNumVarI * var, IloNum min) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(var != 0, "IloNumVar: empty handle");
    _setMinBuffered(var, min);
  }
  void setMaxBuffered(IloNumVarI * var, IloNum max) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(var != 0, "IloNumVar: empty handle");
    _setMaxBuffered(var, max);
  }
  IloBool isInteger(IloNumVar var) const {
    IloAssert(var.getImpl() != 0, "IloNumVar: empty handle");
    return var.getType() != ILOFLOAT;
  }

  ///////////////////////////////////////////////////////////////////////////
  // Exceptions
  ///////////////////////////////////////////////////////////////////////////
 class Exception : public IloAlgorithm::Exception {
    IloInt _status;
  public:
    Exception(int status, const char* str);
    IloInt getStatus() const { return _status; }
  };
  class NoLicense : public Exception {
  public:
    NoLicense(const char* msg)
      : IloCP::Exception(-1, msg) { }
  };

  class MultipleObjException : public Exception {
    IloObjective _obj;
  public:
    MultipleObjException(IloObjective obj)
      :IloCP::Exception(-1, "IloCP can not handle multiple objectives")
      , _obj(obj) {}
    IloObjective getObj() const { return _obj; }
  };

  class MultipleSearchException : public Exception {
  public:
    MultipleSearchException()
      : IloCP::Exception(-1, "IloCP can not handle multiple searchs"){
    }
  };

  class InvalidDiffException : public Exception {
    IloDiff _diff;
  public:
    InvalidDiffException(IloDiff diff)
      : IloCP::Exception(-1, "Invalid IloDiff constraint for IloCP (can only uses integer expressions)")
      , _diff(diff) {}
    IloDiff getDiff() const { return _diff; }
  };

  class SolverErrorException : public Exception {
    const char* _function;
    IloInt _errorType;
  public:
    SolverErrorException(const char* function, const char* str, IloInt er)
      : IloCP::Exception(-1, str), _function(function),
      _errorType(er) {}
    virtual void print(ILOSTD(ostream)& o) const;
    const char* getFunction() const { return _function; }
    IloInt getErrorType() const { return _errorType; }
  };

  class SolverErrorExceptionInt : public SolverErrorException {
    const IloInt _value;
  public:
    SolverErrorExceptionInt(const char* function,
                            const char* str,
                            IloInt e,
                            IloInt value) :
      IloCP::SolverErrorException(function, str, e), _value(value) {}

    virtual void print(ILOSTD(ostream)& o) const;
    IloInt getValue() const { return _value; }
  };
  class SolverErrorExceptionIntInt : public SolverErrorExceptionInt {
    const IloInt _value2;
  public:
    SolverErrorExceptionIntInt(const char* function,
                               const char* str,
                               IloInt e,
                               IloInt value,
                               IloInt value2) :
      IloCP::SolverErrorExceptionInt(function, str, e, value),
      _value2(value2) {}

    virtual void print(ILOSTD(ostream)& o) const;
    IloInt getValue2() const { return _value2; }
  };

  class SolverErrorExceptionFloat : public SolverErrorException {
    const IloNum _value;
  public:
    SolverErrorExceptionFloat(const char* function,
                            const char* str,
                            IloInt e,
                            IloNum value) :
      IloCP::SolverErrorException(function, str, e), _value(value) {}

    virtual void print(ILOSTD(ostream)& o) const;
    IloNum  getValue() const { return _value; }
  };

  class SolverErrorExceptionAny : public SolverErrorException {
    const IloAny _value;
  public:
    SolverErrorExceptionAny(const char* function,
                            const char* str,
                            IloInt e,
                            IloAny value) :
      IloCP::SolverErrorException(function, str, e), _value(value) {}

    virtual void print(ILOSTD(ostream)& o) const;
    IloAny getValue() const { return _value; }
  };

  class SolverErrorExceptionExprI : public SolverErrorException {
    const IlcExprI* _exprI;
  public:
    SolverErrorExceptionExprI(const char* function,
                              const char* str,
                              IloInt e,
                              const IlcExprI* expr) :
      IloCP::SolverErrorException(function, str, e), _exprI(expr) {}

    virtual void print(ILOSTD(ostream)& o) const;
    const IlcExprI* getExprI() const { return _exprI; }
  };

  class SolverErrorExceptionExprsI : public SolverErrorExceptionExprI {
    const IlcExprI* _exprI2;
  public:
    SolverErrorExceptionExprsI(const char* function,
                              const char* str,
                              IloInt e,
                              const IlcExprI* expr,
                              const IlcExprI* expr2) :
      IloCP::SolverErrorExceptionExprI(function, str, e, expr),
      _exprI2(expr2){}

   virtual void print(ILOSTD(ostream)& o) const;
   const IlcExprI* getExprI2() const { return _exprI2; }
  };

  class UnimplementedFeature : public Exception {
  public:
    UnimplementedFeature(const char* message) :
      IloCP::Exception(-1, message) {}
  };

  class ObjectNotExtracted : public Exception {
  public:
    ObjectNotExtracted(const char* message) :
      IloCP::Exception(-1, message) {}
    virtual void print(ILOSTD(ostream)&) const;
  };

  class ModelNotExtracted : public Exception {
  public:
    ModelNotExtracted() : IloCP::Exception(-1, "Model is not loaded") {}
  };

  class BadParameterType : public Exception {
  public:
    BadParameterType(const char* message) :IloCP::Exception(-1, message) {}
  };

  class NumIsNotInteger : public Exception {
  public:
    NumIsNotInteger() :IloCP::Exception(-1, "IloNum is not integer") {}
  };

  class NumIsNotBoolean : public Exception {
  public:
    NumIsNotBoolean() :IloCP::Exception(-1, "IloNum is not boolean") {}
  };

  class IntegerOverflow : public Exception {
  public:
    IntegerOverflow() :IloCP::Exception(-1, "IloNum is out of integer range") {}
  };

  class MixedTypeVariableArray : public Exception {
  public:
    MixedTypeVariableArray(const char* message) :IloCP::Exception(-1, message) {}
  };

  class VariableShouldBeInteger : public Exception {
  public:
    VariableShouldBeInteger(const char* message) :
      IloCP::Exception(-1, message) {}
    virtual void print(ILOSTD(ostream)&) const;
  };

  class VariableShouldBeFloat : public Exception {
  public:
    VariableShouldBeFloat(const char* message) :
      IloCP::Exception(-1, message) {}
    virtual void print(ILOSTD(ostream)&) const;
  };

  class WrongContext : public Exception {
  public:
    WrongContext(const char* message) :
      IloCP::Exception(-1, message) {}
  };
  class WrongType : public Exception {
  public:
    WrongType(const char* message) :
      IloCP::Exception(-1, message) {}
  };
  class WrongUsage : public Exception {
  public:
    WrongUsage(const char* message) :
      IloCP::Exception(-1, message) {}
  };

  class EmptyHandle : public Exception {
  public:
    EmptyHandle(const char* message) :
      IloCP::Exception(-1, message) {}
  };

  class SizeMustBePositive : public Exception {
  public:
    SizeMustBePositive(const char* message) : IloCP::Exception(-1, message) {}
  };

  class ModelInconsistent : public Exception {
    IloExtractableI* _extractable;
  public:
    ModelInconsistent(IloExtractableI* ext) :IloCP::Exception(-1, "The loaded model is inconsistent"), _extractable(ext) {}
    IloExtractable getExtractable() const { return _extractable; }
    virtual void print(ILOSTD(ostream)&) const;
    virtual const char* getInconsistencyReason() const;
  };

  class IntervalInconsistent : public ModelInconsistent {
  public:
    enum Reason {
      StartRange,
      EndRange,
      SizeRange,
      LengthRange,
      Window
    };
    IntervalInconsistent(IloExtractableI* ext, Reason r) :IloCP::ModelInconsistent(ext), _reason(r) {}
    virtual const char* getInconsistencyReason() const;
  private:
    Reason _reason;
  };

  class StateFunctionNoTriangularInequality : public Exception {
    const IloStateFunctionI* _sf;
    IloInt _i;								
    IloInt _j;								
    IloInt _k;
  public:
    StateFunctionNoTriangularInequality(const IloStateFunctionI* sf, 
                                        IloInt i =-1, IloInt j =-1, IloInt k =-1)
      :IloCP::Exception(-1, "Transition distance matrix does not satisfy the triangular inequality")
      ,_sf(sf), _i(i), _j(j), _k(k) {}
    virtual void print(ILOSTD(ostream)& o) const;
    const IloStateFunctionI* getStateFunction() const { return _sf; }				
    IloInt getI() const { return _i; }
    IloInt getJ() const { return _j; }
    IloInt getK() const { return _k; }
  };
  
  class UndefinedFunctionValue : public Exception {
    const IlcFloatExpI* _exprI;
  public:
    UndefinedFunctionValue(const IlcFloatExpI* exprI) 
      :IloCP::Exception(-1, "Accessing function outside its definition interval"), 
       _exprI(exprI) {}
    virtual void print(ILOSTD(ostream)& o) const;
  };
  
  class PropagatorException : public Exception {
  public:
    PropagatorException(const char* message) :
      IloCP::Exception(-1, message) {}
  };

  class ParameterCannotBeSetHereException : public Exception {
  public:
    ParameterCannotBeSetHereException(const char* message) :
      IloCP::Exception(-1, message) {}
  };

  class IncompatibleMemoryManagerException : public Exception {
    public: IncompatibleMemoryManagerException();
  };

  class NoSuchXException : public Exception {
    private:
      const char * _x;
      void _ctor(const char *);
    public:
      NoSuchXException(const NoSuchXException &ex)
        : Exception(ex) { _ctor(ex._x); }
      NoSuchXException& operator = (const NoSuchXException & ex) {
        *((Exception*)this) = (const Exception&)ex;
        _ctor(ex._x);
        return *this;
      }
      NoSuchXException(const char * what, const char * x)
        : Exception(-1, what) { _ctor(x); }
      ~NoSuchXException();
      void print(ILOSTD(ostream)&) const;
  };

  class NoSuchParameterException : public NoSuchXException {
    public:
      NoSuchParameterException(const char * param)
        : NoSuchXException("parameter", param) { }
  };
  class NoSuchParameterValueException : public NoSuchXException {
    public:
      NoSuchParameterValueException(const char * paramValue)
        : NoSuchXException("parameter value", paramValue) { }
  };
  class NoSuchInfoException : public NoSuchXException {
    public:
      NoSuchInfoException(const char * info)
        : NoSuchXException("info", info) { }
  };

  ///////////////////////////////////////////////////////////////////////////
  // Unclassified
  ///////////////////////////////////////////////////////////////////////////
  IloCP(IloMemoryManager memoryManager);
  IloMemoryManager getReversibleAllocator() const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    return _getReversibleAllocator();
  }
  IloMemoryManager getSolveAllocator() const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    return _getSolveAllocator();
  }
  IloMemoryManager getPersistentAllocator() const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    return _getPersistentAllocator();
  }
  operator IloSolver() const;
  IlcManagerI * getManagerI() const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    return _getManagerI();
  }
  ////////////////////////////////////////////////////////////////////////
  // Run-time license
  ////////////////////////////////////////////////////////////////////////
  static IloBool RegisterLicense(const char *, int);

  ////////////////////////////////////////////////////////////////////////
  // XML registration
  ////////////////////////////////////////////////////////////////////////
  static void RegisterXML(IloEnv env) {
    IloAssert(env.getImpl() != 0, "IloEnv: empty handle");
    _RegisterXML(env);
  }
  static void UseStandardCPLEX();

   IloBool isFixed(const IloIntervalVar a) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    IloAssert(a.getImpl(), "IloIntervalVar: empty handle.");
    IloAssert(isExtracted(a), "IloIntervalVar: not extracted.");
    return _isFixed(a);
  }
  IloBool isPresent(const IloIntervalVar a) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    IloAssert(a.getImpl(), "IloIntervalVar: empty handle.");
    IloAssert(isExtracted(a), "IloIntervalVar: not extracted.");
    return _isPresent(a);
  }
  IloBool isAbsent(const IloIntervalVar a) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    IloAssert(a.getImpl(), "IloIntervalVar: empty handle.");
    IloAssert(isExtracted(a), "IloIntervalVar: not extracted.");
    return _isAbsent(a);
  }
  IloInt  getStartMin(const IloIntervalVar a) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    IloAssert(a.getImpl(), "IloIntervalVar: empty handle.");
    IloAssert(isExtracted(a), "IloIntervalVar: not extracted.");
    return _getStartMin(a);
  }
  IloInt  getStartMax(const IloIntervalVar a) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    IloAssert(a.getImpl(), "IloIntervalVar: empty handle.");
    IloAssert(isExtracted(a), "IloIntervalVar: not extracted.");
    return _getStartMax(a);
  }
  IloInt  getStart(const IloIntervalVar a) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    IloAssert(a.getImpl(), "IloIntervalVar: empty handle.");
    IloAssert(isExtracted(a), "IloIntervalVar: not extracted.");
    IloAssert(isFixed(a), "IloIntervalVar: not fixed.");
    return _getStart(a);
  }
  IloInt  getEndMin(const IloIntervalVar a) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    IloAssert(a.getImpl(), "IloIntervalVar: empty handle.");
    IloAssert(isExtracted(a), "IloIntervalVar: not extracted.");
    return _getEndMin(a);
  }
  IloInt  getEndMax(const IloIntervalVar a) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    IloAssert(a.getImpl(), "IloIntervalVar: empty handle.");
    IloAssert(isExtracted(a), "IloIntervalVar: not extracted.");
    return _getEndMax(a);
  }
  IloInt  getEnd(const IloIntervalVar a) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    IloAssert(a.getImpl(), "IloIntervalVar: empty handle.");
    IloAssert(isExtracted(a), "IloIntervalVar: not extracted.");
    IloAssert(isFixed(a), "IloIntervalVar: not fixed.");
    return _getEnd(a);
  }
  IloInt  getSizeMin(const IloIntervalVar a) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    IloAssert(a.getImpl(), "IloIntervalVar: empty handle.");
    IloAssert(isExtracted(a), "IloIntervalVar: not extracted.");
    return _getSizeMin(a);
  }
  IloInt  getSizeMax(const IloIntervalVar a) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    IloAssert(a.getImpl(), "IloIntervalVar: empty handle.");
    IloAssert(isExtracted(a), "IloIntervalVar: not extracted.");
    return _getSizeMax(a);
  }
  IloInt  getSize(const IloIntervalVar a) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    IloAssert(a.getImpl(), "IloIntervalVar: empty handle.");
    IloAssert(isExtracted(a), "IloIntervalVar: not extracted.");
    IloAssert(isFixed(a), "IloIntervalVar: not fixed.");
    return _getSize(a);
  }
  IloInt  getLengthMin(const IloIntervalVar a) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    IloAssert(a.getImpl(), "IloIntervalVar: empty handle.");
    IloAssert(isExtracted(a), "IloIntervalVar: not extracted.");
    return _getLengthMin(a);
  }
  IloInt  getLengthMax(const IloIntervalVar a) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    IloAssert(a.getImpl(), "IloIntervalVar: empty handle.");
    IloAssert(isExtracted(a), "IloIntervalVar: not extracted.");
    return _getLengthMax(a);
  }
  IloInt  getLength(const IloIntervalVar a) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    IloAssert(a.getImpl(), "IloIntervalVar: empty handle.");
    IloAssert(isExtracted(a), "IloIntervalVar: not extracted.");
    IloAssert(isFixed(a), "IloIntervalVar: not fixed.");
    return _getLength(a);
  }
  void printDomain(const IloIntervalVar a) const {
    IloAssert(_impl != 0, "IloCP: empty handle.");
    IloAssert(a.getImpl() != 0, "IloIntervalVar: empty handle.");
    IloAssert(isExtracted(a), "IloIntervalVar: not extracted.");
    _printDomain(a);
  }
  IloBool isFixed(const IloIntervalSequenceVar seq) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    IloAssert(seq.getImpl(), "IloIntervalSequenceVar: empty handle.");
    IloAssert(isExtracted(seq), "IloIntervalSequenceVar: not extracted.");
    return _isFixed(seq);
  }
  IloIntervalVar getFirst(const IloIntervalSequenceVar seq) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    IloAssert(seq.getImpl(), "IloIntervalSequenceVar: empty handle.");
    IloAssert(isExtracted(seq), "IloIntervalSequenceVar: not extracted.");
    IloAssert(isFixed(seq), "IloIntervalSequenceVar: not fixed.");
    return _getFirst(seq);
  }
  IloIntervalVar getLast (const IloIntervalSequenceVar seq) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    IloAssert(seq.getImpl(), "IloIntervalSequenceVar: empty handle.");
    IloAssert(isExtracted(seq), "IloIntervalSequenceVar: not extracted.");
    IloAssert(isFixed(seq), "IloIntervalSequenceVar: not fixed.");
    return _getLast(seq);
  }
  IloIntervalVar getNext(const IloIntervalSequenceVar seq, const IloIntervalVar a) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    IloAssert(seq.getImpl(), "IloIntervalSequenceVar: empty handle.");
    IloAssert(isExtracted(seq), "IloIntervalSequenceVar: not extracted.");
    IloAssert(isFixed(seq), "IloIntervalSequenceVar: not fixed.");
    IloAssert(a.getImpl(), "IloIntervalVar: empty handle.");
    IloAssert(isExtracted(a), "IloIntervalVar: not extracted.");
    IloAssert(isPresent(a), "IloIntervalVar: not present.");
    IloAssert(isInSequence(seq, a), "IloIntervalVar: not in sequence variable.");
     return _getNext(seq, a);
  }
  IloIntervalVar getPrev (const IloIntervalSequenceVar seq, const IloIntervalVar a) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    IloAssert(seq.getImpl(), "IloIntervalSequenceVar: empty handle.");
    IloAssert(isExtracted(seq), "IloIntervalSequenceVar: not extracted.");
    IloAssert(isFixed(seq), "IloIntervalSequenceVar: not fixed.");
    IloAssert(a.getImpl(), "IloIntervalVar: empty handle.");
    IloAssert(isExtracted(a), "IloIntervalVar: not extracted.");
    IloAssert(isPresent(a), "IloIntervalVar: not present.");
    IloAssert(isInSequence(seq, a), "IloIntervalVar: not in sequence variable.");
    return _getPrev(seq, a);
  }

  IloBool isFixed(const IloCumulFunctionExpr f) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloCumulFunctionExpr: empty handle");
    IloAssert(isExtracted(f), "IloCumulFunctionExpr: not extracted");
    return _isFixed(f);
  }

  IloInt getNumberOfSegments(const IloCumulFunctionExpr f) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloCumulFunctionExpr: empty handle");
    IloAssert(isExtracted(f), "IloCP: cumul function expression not extracted");
    IloAssert(_isFixed(f), "IloCP: cumul function expression not fixed");
    return _getNumberOfSegments(f);
  }

  IloNum getNumberOfSegmentsAsNum(const IloCumulFunctionExpr f) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloCumulFunctionExpr: empty handle");
    IloAssert(isExtracted(f), "IloCP: cumul function expression not extracted");
    IloAssert(_isFixed(f), "IloCP: cumul function expression not fixed");
    return _getNumberOfSegmentsAsNum(f);
  }

  IloInt getSegmentStart(const IloCumulFunctionExpr f, IloInt i) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloCumulFunctionExpr: empty handle");
    IloAssert(isExtracted(f), "IloCP: cumul function expression not extracted");
    IloAssert(_isFixed(f), "IloCP: cumul function expression not fixed");
    IloAssert(_isValidSegment(f, i), "IloCP: invalid cumul function expression segment");
    return _getSegmentStart(f, i);
  }
  IloNum getSegmentStartAsNum(const IloCumulFunctionExpr f, IloInt i) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloCumulFunctionExpr: empty handle");
    IloAssert(isExtracted(f), "IloCP: cumul function expression not extracted");
    IloAssert(_isFixed(f), "IloCP: cumul function expression not fixed");
    IloAssert(_isValidSegment(f, i), "IloCP: invalid cumul function expression segment");
    return _getSegmentStartAsNum(f, i);
  }

  IloInt getSegmentEnd(const IloCumulFunctionExpr f, IloInt i) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloCumulFunctionExpr: empty handle");
    IloAssert(isExtracted(f), "IloCP: cumul function expression not extracted");
    IloAssert(_isFixed(f), "IloCP: cumul function expression not fixed");
    IloAssert(_isValidSegment(f, i), "IloCP: invalid cumul function expression segment");
    return _getSegmentEnd(f, i);
  }
  IloNum getSegmentEndAsNum(const IloCumulFunctionExpr f, IloInt i) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloCumulFunctionExpr: empty handle");
    IloAssert(isExtracted(f), "IloCP: cumul function expression not extracted");
    IloAssert(_isFixed(f), "IloCP: cumul function expression not fixed");
    IloAssert(_isValidSegment(f, i), "IloCP: invalid cumul function expression segment");
    return _getSegmentEndAsNum(f, i);
  }

  IloInt getSegmentValue(const IloCumulFunctionExpr f, IloInt i) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloCumulFunctionExpr: empty handle");
    IloAssert(isExtracted(f), "IloCP: cumul function expression not extracted");
    IloAssert(_isFixed(f), "IloCP: cumul function expression not fixed");
    IloAssert(_isValidSegment(f, i), "IloCP: invalid cumul function expression segment");
    return _getSegmentValue(f, i);
  }
  IloNum getSegmentValueAsNum(const IloCumulFunctionExpr f, IloInt i) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloCumulFunctionExpr: empty handle");
    IloAssert(isExtracted(f), "IloCP: cumul function expression not extracted");
    IloAssert(_isFixed(f), "IloCP: cumul function expression not fixed");
    IloAssert(_isValidSegment(f, i), "IloCP: invalid cumul function expression segment");
    return _getSegmentValueAsNum(f, i);
  }

  IloInt getValue(const IloCumulFunctionExpr f, IloInt t) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloCumulFunctionExpr: empty handle");
    IloAssert(isExtracted(f), "IloCP: cumul function expression not extracted");
    IloAssert(_isFixed(f), "IloCP: cumul function expression not fixed");
    IloAssert(_isValidAbscissa(f, t), "IloCP: cumul function expression evaluated on invalid point");
    return _getValue(f, t);
  }
  IloNum getValueAsNum(const IloCumulFunctionExpr f, IloInt t) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloCumulFunctionExpr: empty handle");
    IloAssert(isExtracted(f), "IloCP: cumul function expression not extracted");
    IloAssert(_isFixed(f), "IloCP: cumul function expression not fixed");
    IloAssert(_isValidAbscissa(f, t), "IloCP: cumul function expression evaluated on invalid point");
    return _getValueAsNum(f, t);
  }

  ////////////////////////////////////////////////////////////////////////
  // Reading State Functions at solution
  ////////////////////////////////////////////////////////////////////////


  enum FunctionValues {
        NoState = -1
  };

  IloBool isFixed(const IloStateFunction f) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloStateFunction: empty handle");
    IloAssert(isExtracted(f), "IloStateFunctionFunction: not extracted");
    return _isFixed(f);
  }

  IloInt getNumberOfSegments(const IloStateFunction f) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloStateFunction: empty handle");
    IloAssert(isExtracted(f), "IloCP: state function  not extracted");
    IloAssert(_isFixed(f), "IloCP: state function not fixed");
    return _getNumberOfSegments(f);
  }


  IloInt getSegmentStart(const IloStateFunction f, IloInt s) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloStateFunction: empty handle");
    IloAssert(isExtracted(f), "IloCP: state function  not extracted");
    IloAssert(_isFixed(f), "IloCP: state function not fixed");
    IloAssert(_isValidSegment(f, s), "IloCP: invalid state function segment");
    return _getSegmentStart(f, s);
  }
  IloNum getSegmentStartAsNum(const IloStateFunction f, IloInt s) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloStateFunction: empty handle");
    IloAssert(isExtracted(f), "IloCP: state function  not extracted");
    IloAssert(_isFixed(f), "IloCP: state function not fixed");
    IloAssert(_isValidSegment(f, s), "IloCP: invalid state function segment");
    return _getSegmentStartAsNum(f, s);
  }
  
  IloInt getSegmentEnd(const IloStateFunction f, IloInt s) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloStateFunction: empty handle");
    IloAssert(isExtracted(f), "IloCP: state function  not extracted");
    IloAssert(_isFixed(f), "IloCP: state function not fixed");
    IloAssert(_isValidSegment(f, s), "IloCP: invalid state function segment");
    return _getSegmentEnd(f, s);
  }
  IloNum getSegmentEndAsNum(const IloStateFunction f, IloInt s) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloStateFunction: empty handle");
    IloAssert(isExtracted(f), "IloCP: state function  not extracted");
    IloAssert(_isFixed(f), "IloCP: state function not fixed");
    IloAssert(_isValidSegment(f, s), "IloCP: invalid state function segment");
    return _getSegmentEndAsNum(f, s);
  }
 
  IloInt getSegmentValue(const IloStateFunction f, IloInt s) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloStateFunction: empty handle");
    IloAssert(isExtracted(f), "IloCP: state function  not extracted");
    IloAssert(_isFixed(f), "IloCP: state function not fixed");
    IloAssert(_isValidSegment(f, s), "IloCP: invalid state function segment");
    return _getSegmentValue(f, s);
  }
  IloNum getSegmentValueAsNum(const IloStateFunction f, IloInt s) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloStateFunction: empty handle");
    IloAssert(isExtracted(f), "IloCP: state function  not extracted");
    IloAssert(_isFixed(f), "IloCP: state function not fixed");
    IloAssert(_isValidSegment(f, s), "IloCP: invalid state function segment");
    return _getSegmentValueAsNum(f, s);
  }
 


  IloInt getValue(const IloStateFunction f, IloInt t) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloStateFunction: empty handle");
    IloAssert(isExtracted(f), "IloCP: state function not extracted");
    IloAssert(_isFixed(f), "IloCP: state function not fixed");
    IloAssert(_isValidAbscissa(f, t), "IloCP: state function evaluated on invalid point");
    return _getValue(f, t);
  }
  IloNum getValueAsNum(const IloStateFunction f, IloInt t) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    IloAssert(f.getImpl() != 0, "IloStateFunction: empty handle");
    IloAssert(isExtracted(f), "IloCP: state function not extracted");
    IloAssert(_isFixed(f), "IloCP: state function not fixed");
    IloAssert(_isValidAbscissa(f, t), "IloCP: state function evaluated on invalid point");
    return _getValueAsNum(f, t);
  }
  
  ///////////////////////////////////////////////////////////////////////////
  // class IloStateFunctionExpr
  ///////////////////////////////////////////////////////////////////////////

  IloInt getNumberOfSegments(const IloStateFunctionExpr expr) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    return _getNumberOfSegments(expr);
  }


  IloInt getSegmentStart(const IloStateFunctionExpr expr, IloInt i) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    return _getSegmentStart(expr, i);
  }



  IloInt getSegmentEnd(const IloStateFunctionExpr expr, IloInt i) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    return _getSegmentEnd(expr, i);
  }


  IloInt getSegmentValue(const IloStateFunctionExpr expr, IloInt i) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    return _getSegmentValue(expr, i);
  }

  IloInt getValue(const IloStateFunctionExpr expr, IloInt t) const {
    IloAssert(getImpl() != 0, "IloCP: empty handle");
    return _getValue(expr, t);
  }

  ///////////////////////////////////////////////////////////////////////////
  // Printing interval domains
  ///////////////////////////////////////////////////////////////////////////

  class PrintIntervalVarDomains : public PrintDomains {
    friend class IloCP;
  private:
    void operator = (const PrintIntervalVarDomains&);
    PrintIntervalVarDomains(const IloCP cp, const IloIntervalVar var);
  public:
    void display(ILOSTD(ostream)& o) const;
  };
  PrintIntervalVarDomains domain(const IloIntervalVar a) const {
    IloAssert(_impl != 0, "IloCP: empty handle");
    IloAssert(a.getImpl() != 0, "IloIntervalVar: empty handle");
    IloAssert(isExtracted(a), "IloIntervalVar: not extracted");
    return PrintIntervalVarDomains(*this, a);
  }

  // ------------------------------------------------------------------------
  // Advanced
  // ------------------------------------------------------------------------
  IloBool isInSequence (const IloIntervalSequenceVar seq,
			const IloIntervalVar a) const;
  void prettyPrintSchedule(ILOSTD(ostream)& s) const {
    IloAssert(getImpl(), "IloCP: empty handle.");
    _prettyPrintSchedule(s);
  }
 private:
  IloBool _isFixed      (const IloIntervalVar a) const;
  IloBool _isPresent    (const IloIntervalVar a) const;
  IloBool _isAbsent     (const IloIntervalVar a) const;
  IloInt  _getStartMin  (const IloIntervalVar a) const;
  IloInt  _getStartMax  (const IloIntervalVar a) const;
  IloInt  _getStart     (const IloIntervalVar a) const;
  IloInt  _getEndMin    (const IloIntervalVar a) const;
  IloInt  _getEndMax    (const IloIntervalVar a) const;
  IloInt  _getEnd       (const IloIntervalVar a) const;
  IloInt  _getSizeMin   (const IloIntervalVar a) const;
  IloInt  _getSizeMax   (const IloIntervalVar a) const;
  IloInt  _getSize      (const IloIntervalVar a) const;
  IloInt  _getLengthMin (const IloIntervalVar a) const;
  IloInt  _getLengthMax (const IloIntervalVar a) const;
  IloInt  _getLength    (const IloIntervalVar a) const;
  void    _printDomain  (const IloIntervalVar a) const;
  IloBool _isFixed      (const IloIntervalSequenceVar seq) const;
  IloIntervalVar _getFirst(const IloIntervalSequenceVar seq) const;
  IloIntervalVar _getLast (const IloIntervalSequenceVar seq) const;
  IloIntervalVar _getNext (const IloIntervalSequenceVar seq, const IloIntervalVar a) const;
  IloIntervalVar _getPrev (const IloIntervalSequenceVar seq, const IloIntervalVar a) const;
  void _prettyPrintSchedule(ILOSTD(ostream)& s) const;

public:
  void setJNIEnv(void* env);
};

ILOSTD(ostream)& operator << (ILOSTD(ostream) & o,
                              const IloCP::PrintNumVarDomains& doms);
ILOSTD(ostream)& operator << (ILOSTD(ostream) & o,
                              IloCP::PrintIntSetVarDomains& doms);
ILOSTD(ostream)& operator << (ILOSTD(ostream) & o,
                              const IloCP::PrintIntervalVarDomains& doms);

////////////////////////////////////////////////////////////////////////
//
// ILOCPHOOK
//
////////////////////////////////////////////////////////////////////////

class IloCPHookI : public IloEnvObjectI {
public:
  IloCPHookI(IloEnv env) : IloEnvObjectI(env.getImpl()) { }
  virtual void execute(IloCP cp) = 0;
};

////////////////////////////////////////////////////////////////////////
//
// IloSolver compat
//
////////////////////////////////////////////////////////////////////////

typedef enum {
  IlcLow=0L,
  IloLowLevel=0L,
  IlcBasic=1L,
  IloBasicLevel=1L,
  IlcMedium=2L,
  IloMediumLevel=2L,
  IlcExtended=3L,
  IloExtendedLevel=3L
} IlcFilterLevel;

typedef enum {
  IlcAllDiffCt=0L,
  IloAllDiffCt=0L,
  IlcDistributeCt=1L,
  IloDistributeCt=1L,
  IlcSequenceCt=2L,
  IloSequenceCt=2L,
  IlcAllMinDistanceCt=3L,
  IloAllMinDistanceCt=3L,
  IlcPartitionCt=4L,
  IloPartitionCt=4L,
  IlcAllNullIntersectCt=5L,
  IloAllNullIntersectCt=5L,
  IlcEqUnionCt=6L,
  IloEqUnionCt=6L,
  IlcCountCt=8L,
  IloCountCt=8L
} IlcFilterLevelConstraint;

typedef enum {
  IlcStandardDisplay = 0,
  IlcIntScientific,
  IlcIntFixed,
  IlcBasScientific,
  IlcBasFixed
} IlcFloatDisplay;

class IloSolver : public IloCP {
private:
  void _restartSearch() const;
  void _setPropagationControl(IloNumVar var) const;
  void _setPropagationControl(IloIntVar var) const;
  void _setMin(IloNumVar var, IloNum min) const;
  void _setMax(IloNumVar var, IloNum max) const;
  void _setValue(IloNumVar var, IloNum value) const;

  void _ctor(const IloModel model);
  void _ctor(const IloEnv env);
  void _setSolverConfig() const;
public:
  IloSolver(const IloModel model) : IloCP() {
    IloAssert(model.getImpl() != 0, "IloModel: empty handle");
    _ctor(model);
  }
  IloSolver(const IloEnv env) : IloCP() {
    IloAssert(env.getImpl() != 0, "IloEnv: empty handle");
    _ctor(env);
  }
  IloSolver(IloCPI * impl = 0) : IloCP(impl) { }

  enum SearchState {
    IloBeforeSearch = 0,
    IloDuringSearch,
    IloAfterSearch
  };
  typedef enum {
    searchHasNotFailed = 0,
    searchHasFailedNormally,
    searchStoppedByLimit,
    searchStoppedByLabel,
    searchStoppedByExit,
    searchStoppedByAbort,
    unknownFailureStatus
  } FailureStatus;
  FailureStatus convertFailureStatus(IloInt f) const {
    switch (f) {
      case IloCP::SearchHasNotFailed:      return searchHasNotFailed;
      case IloCP::SearchHasFailedNormally: return searchHasFailedNormally;
      case IloCP::SearchStoppedByLimit:    return searchStoppedByLimit;
      case IloCP::SearchStoppedByLabel:    return searchStoppedByLabel;
      case IloCP::SearchStoppedByExit:     return searchStoppedByExit;
      case IloCP::SearchStoppedByAbort:    return searchStoppedByAbort;
      default:                             return unknownFailureStatus;
    }
    return unknownFailureStatus;
  }

  IloCP::ParameterValues filterToInferenceLevel(IlcFilterLevel fl) const {
    switch (fl) {
      case IlcLow :     return IloCP::Low;
      case IlcBasic:    return IloCP::Basic;
      case IlcMedium:   return IloCP::Medium;
      case IlcExtended: return IloCP::Extended;
    }
    IloAssert(0, "IloSolver: Invalid filter level");
    return IloCP::Basic;
  }
  IloCP::IntParam filterToInferenceCt(IlcFilterLevelConstraint fl) const {
    switch (fl) {
      case IloAllDiffCt:          return IloCP::AllDiffInferenceLevel;
      case IloDistributeCt:       return IloCP::DistributeInferenceLevel;
      case IloSequenceCt:         return IloCP::SequenceInferenceLevel;
      case IloAllMinDistanceCt:   return IloCP::AllMinDistanceInferenceLevel;
      case IloCountCt:            return IloCP::CountInferenceLevel;
      default: IloAssert(0, "IloSolver: Invalid filter constraint");
    }
    return IloCP::AllDiffInferenceLevel;
  }
  void setFilterLevel(IloConstraint ct, IlcFilterLevel fl) const {
    setInferenceLevel(ct, filterToInferenceLevel(fl));
  }
  void setDefaultFilterLevel(IlcFilterLevelConstraint ct,
                             IlcFilterLevel fl) const {
    setParameter(filterToInferenceCt(ct), filterToInferenceLevel(fl));
  }
  void setDefaultFilterLevel(IlcFilterLevel fl) const {
    setParameter(IloCP::DefaultInferenceLevel, filterToInferenceLevel(fl));
  }
  void setFilterLevel(IlcConstraint ct, IlcFilterLevel fl) const;
  IlcConstraint getConstraint(IloConstraint ct) const;
  IlcConstraintArray getConstraintArray(IloConstraintArray ct) const;
  IloBool isInSearch() const;
  void restartSearch() const {
    IloAssert(getImpl() != 0, "IloSolver: empty handle");
    _restartSearch();
  }
  void setObjMin(const IlcIntVar& obj, IloInt step=1, IloNum r = 0.0) const;
  void setObjMin(const IlcFloatVar& obj, IloNum step, IloNum r = 0.0) const;
  FailureStatus getFailureStatus() const {
    IloAssert(getImpl() != 0, "IloSolver: empty handle");
    return convertFailureStatus(getInfo(IloCP::FailStatus));
  }
  void setOptimizationStep(IloNum step) const {
    setParameter(IloCP::OptimalityTolerance, step);
  }
  void setRelativeOptimizationStep(IloNum step) const {
    setParameter(IloCP::RelativeOptimalityTolerance, step);
  }
  IloNum getOptimizationStep() const {
    return getParameter(IloCP::OptimalityTolerance);
  }
  IloNum getRelativeOptimizationStep() const {
    return getParameter(IloCP::RelativeOptimalityTolerance);
  }
  void setFailLimit(IloInt fails) const {
    setParameter(IloCP::FailLimit, fails);
  }
  void setOrLimit(IloInt cps) const {
    setParameter(IloCP::ChoicePointLimit, cps);
  }
  void setTimeLimit(IloNum time) const {
    setParameter(IloCP::TimeLimit, time);
  }
  void unsetLimit() const {
    setFailLimit(IloIntMax);
    setOrLimit(IloIntMax);
    setTimeLimit(IloInfinity);
  }
  void setFloatDisplay(IlcFloatDisplay display) const {
    setParameter(IloCP::FloatDisplay,
      display - IlcStandardDisplay + IloCP::Standard
    );
  }
  IlcFloatDisplay getFloatDisplay() const {
    return IlcFloatDisplay(getParameter(IloCP::FloatDisplay)
                         - IloCP::Standard
                         + IlcStandardDisplay);
  }
  IloIntSet getIntSetValue(IloIntSetVar s) const {
    return IloCP::getValue(s);
  }
  IloNum getTime() const { return getInfo(IloCP::SolveTime); }
  IloUInt getMemoryUsage() const { return getInfo(IloCP::MemoryUsage); }
  IloNum getDefaultPrecision() const;
  void setFastRestartMode(IloBool mode) const;
  IloInt getNumberOfChoicePoints() const {
    return getInfo(IloCP::NumberOfChoicePoints);
  }
  IloInt getNumberOfConstraints() const {
    return getInfo(IloCP::NumberOfConstraints);
  }
  using IloCP::getNumberOfFails;
  IloInt getNumberOfFails() const {
    return getInfo(IloCP::NumberOfFails);
  }
  IloInt getNumberOfVariables() const {
    return getInfo(IloCP::NumberOfVariables);
  }
  void setDefaultPrecision(IloNum precision) const;
  void setPackApproximationSize(IloInt size) const {
    setParameter(IloCP::PackApproximationSize, size);
  }
  void setPropagationControl(IloNumVar var) const {
    IloAssert(getImpl() != 0, "IloSolver: empty handle");
    IloAssert(var.getImpl() != 0, "IloNumVar: empty handle");
    _setPropagationControl(var);
  }
  void setPropagationControl(IloIntVar var) const {
    IloAssert(getImpl() != 0, "IloSolver: empty handle");
    IloAssert(var.getImpl() != 0, "IloIntVar: empty handle");
    _setPropagationControl(var);
  }
  void setMin(IloNumVar var, IloNum min) const {
    IloAssert(getImpl() != 0, "IloSolver: empty handle");
    IloAssert(var.getImpl() != 0, "IloNumVar: empty handle");
    _setMin(var, min);
  }
  void setMax(IloNumVar var, IloNum max) const {
    IloAssert(getImpl() != 0, "IloSolver: empty handle");
    IloAssert(var.getImpl() != 0, "IloNumVar: empty handle");
    _setMax(var, max);
  }
  void setValue(IloNumVar var, IloNum value) const {
    IloAssert(getImpl() != 0, "IloSolver: empty handle");
    IloAssert(var.getImpl() != 0, "IloNumVar: empty handle");
    _setValue(var, value);
  }
};

////////////////////////////////////////////////////////////////////////
//
// PROPAGATORS
//
////////////////////////////////////////////////////////////////////////
class IloPropagatorI : public IloEnvObjectI {
private:
  IloExtractableArray _vars;
  IloCP               _cp;

  void _addVar(IloNumVar var);
public:
  IloPropagatorI(IloEnv env);
  virtual ~IloPropagatorI();

  IloExtractable getVar(IloInt i) const { return _vars[i]; }
  IloInt getNumVars() const { return _vars.getSize(); }

  void addVar(IloNumVar var) {
    IloAssert(var.getImpl() != 0, "IloNumVar: empty handle");
    _addVar(var);
  }

  void setMax(IloCP cp, IloNumVar var, IloNum max) {
    cp.setMaxBuffered(var.getImpl(), max);
  }
  void setMax(IloCP cp, IloIntVar var, IloInt max) {
    cp.setMaxBuffered(var.getImpl(), (IloNum)max);
  }
  void setMax(IloNumVar var, IloNum max) {
    setMax(_cp, var, max);
  }
  void setMax(IloIntVar var, IloInt max) {
    setMax(_cp, var, max);
  }

  void setMin(IloCP cp, IloNumVar var, IloNum min) {
    cp.setMinBuffered(var.getImpl(), min);
  }
  void setMin(IloCP cp, IloIntVar var, IloInt min) {
    cp.setMinBuffered(var.getImpl(), (IloNum)min);
  }
  void setMin(IloNumVar var, IloNum min) {
    setMin(_cp, var, min);
  }

  void setRange(IloCP cp, IloNumVar var, IloNum min, IloNum max) {
    setMin(cp, var, min);
    setMax(cp, var, max);
  }
  void setRange(IloCP cp, IloIntVar var, IloInt min, IloInt max) {
    setMin(cp, var, min);
    setMax(cp, var, max);
  }
  void setRange(IloNumVar var, IloNum min, IloNum max) {
    setRange(_cp, var, min, max);
  }
  void setRange(IloIntVar var, IloInt min, IloInt max) {
    setRange(_cp, var, min, max);
  }

  void setValue(IloCP cp, IloNumVar var, IloNum value) {
    setRange(cp, var, value, value);
  }
  void setValue(IloCP cp, IloIntVar var, IloInt value) {
    setRange(cp, var, value, value);
  }
  void setValue(IloNumVar var, IloNum value) {
    setValue(_cp, var, value);
  }
  void setValue(IloIntVar var, IloInt value) {
    setValue(_cp, var, value);
  }

  void removeValue(IloCP cp, IloIntVar var, IloInt value) {
    cp.removeValueBuffered(var.getImpl(), value);
  }
  void removeValue(IloIntVar var, IloInt value) {
    removeValue(_cp, var, value);
  }

  IloNum getMin(IloCP cp, IloNumVar var) const {
    return cp.getMin(var);
  }
  IloInt getMin(IloCP cp, IloIntVar var) const {
    return cp.getMin(var);
  }
  IloNum getMin(IloNumVar var) const {
    return getMin(_cp, var);
  }
  IloInt getMin(IloIntVar var) const {
    return getMin(_cp, var);
  }

  IloNum getMax(IloCP cp, IloNumVar var) const  {
    return cp.getMax(var);
  }
  IloInt getMax(IloCP cp, IloIntVar var) const  {
    return cp.getMax(var);
  }
  IloNum getMax(IloNumVar var) const { return getMax(_cp, var); }
  IloInt getMax(IloIntVar var) const { return getMax(_cp, var); }

  IloNum getValue(IloCP cp, IloNumVar var) const { return cp.getValue(var); }
  IloInt getValue(IloCP cp, IloIntVar var) const { return cp.getValue(var); }
  IloNum getValue(IloNumVar var) const { return getValue(_cp, var); }
  IloInt getValue(IloIntVar var) const { return getValue(_cp, var); }

  IloInt getDomainSize(IloCP cp, IloNumVar var) const {
    return cp.getDomainSize(var);
  }
  IloInt getDomainSize(IloNumVar var) const {
    return getDomainSize(_cp, var);
  }
  IloBool isInDomain(IloCP cp, IloNumVar var, IloInt value) const {
    return cp.isInDomain(var, value);
  }
  IloBool isInDomain(IloNumVar var, IloInt value) const {
    return isInDomain(_cp, var, value);
  }
  IloBool isFixed(IloCP cp, IloNumVar var) const {
    return cp.isFixed(var);
  }
  IloBool isFixed(IloCP cp, IloIntVar var) const {
    return cp.isFixed(var);
  }
  IloBool isFixed(IloNumVar var) const { return isFixed(_cp, var); }
  IloBool isFixed(IloIntVar var) const { return isFixed(_cp, var); }
  IloCP::IntVarIterator iterator(IloCP cp, IloNumVar var) {
    return IloCP::IntVarIterator(cp, var);
  }
  IloCP::IntVarIterator iterator(IloNumVar var) {
    return iterator(_cp, var);
  }

  virtual void execute() = 0;
  virtual IloPropagatorI* makeClone(IloEnv env) const=0;
  void setCP(IloCP cp);
  friend class IlcPropagatorConstraintI;
  friend class IntVarIterator;
};

IloConstraint IloCustomConstraint(IloEnv env, IloPropagatorI * prop);

////////////////////////////////////////////////////////////////////////
//
// ILOGOAL
//
////////////////////////////////////////////////////////////////////////

class IloGoalI : public IloExtensibleRttiEnvObjectI {
public:
  IloGoalI(IloEnvI*);
  virtual ~IloGoalI();
  virtual IlcGoal extract(const IloCP cp) const=0;
  virtual void display(ILOSTD(ostream&)) const;
  ILORTTIDECL
};

class IloGoal {
  ILOCPHANDLEINLINE(IloGoal, IloGoalI)
public:
  typedef IloGoalI ImplClass;
  IloGoal(IloEnv env, IloIntVarArray vars);
  IloGoal(IloEnv env, IloIntVarArray vars,
                      IloIntVarChooser varChooser,
                      IloIntValueChooser valueChooser);
  IloEnv getEnv() const;
  void end() const;
};

ILOSTD(ostream&) operator << (ILOSTD(ostream&), const IloGoal&);

typedef IloArray<IloGoal> IloGoalArray;

IloGoal IloGoalTrue(const IloEnv);

IloGoal IloGoalFail(const IloEnv);

IloGoal operator && (const IloGoal g1, const IloGoal g2);
IloGoal IloAndGoal(const IloEnv env, const IloGoal, const IloGoal);
IloGoal operator||(const IloGoal g1, const IloGoal g2);
IloGoal IloOrGoal(const IloEnv env, const IloGoal, const IloGoal);

#ifdef ILCENABLEUSINGCPO
using namespace CPOptimizer;
#endif

#ifdef _MSC_VER
#pragma pack(pop)
#endif

#endif
