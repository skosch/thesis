// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/iloevaluator.h
// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
//
// 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55 
// Copyright IBM Corp. 2000, 2011
//
// US Government Users Restricted Rights - Use, duplication or
// disclosure restricted by GSA ADP Schedule Contract with
// IBM Corp.
// ---------------------------------------------------------------------------


#ifndef __ADVANCED_iloevaluatorH
#define __ADVANCED_iloevaluatorH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>
#include <ilopl/iloenv.h>
#include <ilconcert/iloevaluator.h>
#include <ilopl/iloforall.h>
#include <ilopl/ilocollexpr.h>


class IloModelEvaluatorI;
class IloAdvModelEvaluatorI;
class IloEvalExprI;
class IloEvalTupleExprI;
class IloEvalIteratorI;
class IloMapI;
class IloMapExtractIndexI;
class IloTupleCollectionI;
class IloTupleCollection;
class IloMapIndexArray;
class IloMapIndex;
class IloAdvPiecewiseExprFunctionI; // non ground
class IloAdvPiecewiseFunctionI;     // ground
class IloAggregateSegmentedFunctionI; // aggregate
class IloCumulAtomI;
class IloAdvCumulAtomI;
class IloCumulFunctionExprI;
class IloIntervalExprI;
class IloIntervalVarI;

class IloMismatchDExprSubstitution: public IloException {
private:
  IloExtractableI* _expr;
  IloMapIndexArray _indexes;
  IloMapIndexArray _values;
public:
	virtual void print(ILOSTD(ostream)& out) const{
		out << _message;
		if (_expr) _expr->display(out);
	}
  IloMismatchDExprSubstitution()
    : IloException("Can not mix pattern and index between declaration of dexpr and instanciation"), _expr(0), _indexes(), _values(){}
  void setInfo(IloExtractableI* extr, IloMapIndexArray indexes, IloMapIndexArray values){
	  _expr = extr;
	  _indexes = indexes;
	  _values = values;
  }
  IloExtractableI* getExtractable() const {return _expr;}
  IloMapIndexArray getIndexes() const {return _indexes;}
  IloMapIndexArray getValues() const {return _values;}
};


//----------------------------------------------------

class IloAdvModelEvaluator : public IloModelEvaluator {
private:
	IloAdvModelEvaluatorI* _impl;
public:
	using IloModelEvaluator::evaluate;

	
	IloAdvModelEvaluator():IloModelEvaluator(0) {}
	
	IloAdvModelEvaluator(IloAdvModelEvaluatorI* impl);

	
	IloAdvModelEvaluator(IloEnv env);
	
	IloEnv getEnv() const;


	
	IloInt getValue(IloIntExprArg exp) const;
	
	IloInt getMin(IloIntExprArg exp) const;
	
	IloInt getMax(IloIntExprArg exp) const;

	
	IloNum getValue(IloNumExprArg exp) const;
	
	IloNum getLB(IloNumExprArg exp) const;
	
	IloNum getUB(IloNumExprArg exp) const;

	
	const char* getValue(IloSymbolExprArg idx) const;

	IloTuple getValue(IloTupleExprArg exp) const;

	
	const char* evaluate(IloSymbolExprArg exp) const;

	
	IloInt evaluate(IloIntIndex exp) const;
	
	IloNum evaluate(IloNumIndex exp) const;
	
	const char* evaluate(IloSymbolIndex exp) const;
	
	IloTuple evaluate(IloTupleExprArg exp, IloTupleCollectionI* indexer) const;

  
	IloTuple evaluateAsBuffer(IloTuplePattern exp, IloTupleCollectionI* indexer) const;
  
private:
	IloTuplePattern evaluateAsConstPattern(IloTuplePattern exp) const;
public:
	
	IloIntCollection evaluate(IloIntCollectionIndex idx) const;
	
	IloNumCollection evaluate(IloNumCollectionIndex idx) const;
	
	IloAnyCollection evaluate(IloSymbolCollectionIndex idx) const;
	
	IloIntCollection evaluate(IloIntCollectionExprArg idx) const;
	
	IloDataCollection evaluate(IloNumCollectionExprArg idx) const;
	
	IloAnyCollection evaluate(IloSymbolCollectionExprArg idx) const;

	
	IloTupleSet evaluate(IloTupleSetExprArg exp) const;

	
	IloInt evaluateAbsoluteIndex(IloMapI* m, IloMapExtractIndexI* idx) const;
	void evaluate(const IloMapIndexArray& input, IloMapIndexArray& output) const;
	IloOplObject evaluate(const IloOplObject& input, const IloTupleCollectionI* indexer = 0) const;

	IloAdvPiecewiseFunctionI* evaluateToPiecewiseFunction(const IloAdvPiecewiseFunctionExprI* exp) const;
	IloIntervalVarI* evaluateToIntervalVar(const IloIntervalExprI* exp) const;
	IloIntervalSequenceVarI* evaluateToIntervalSequenceVar(const IloIntervalSequenceExprI* exp) const;
	IloCumulAtomI* evaluateToIloCumulAtom(const IloAdvCumulAtomI* exp) const;
	IloCumulFunctionExprI* evaluateToGroundCumulFunctionExpr(const IloCumulFunctionExprI* exp) const;

public:
	//-------------------------------------------------------------------------
	/// Iterator
	//-------------------------------------------------------------------------
	
	class Iterator {
		friend class IloModelEvaluatorI;
		friend class IloAdvModelEvaluatorI;
	private:
		IloBool _ended;
		IloEvalIteratorI* _eval;
		Iterator(const Iterator&) {
			throw IloWrongUsage("IloModelEvaluator::Iterator Inaccessible constructor called.");
		}
	public:
		
		Iterator(IloAdvModelEvaluatorI* e, IloComprehensionI* comp);
		virtual ~Iterator();
		
		void start();
		
		void reset();
		
		void end();
		
		IloBool ok() const;
		
		void operator++();
	private:
		IloBool isValid() const;
		void invalidate();
	};
public:
	//-------------------------------------------------------------------------
	/// Exceptions
	//-------------------------------------------------------------------------
	class Overflow : public IloException {
	public:
		Overflow():IloException("Evaluator had expression overflow.") {}
	};

	class AggregateFilter : public IloException {
		IloConstraintI* _filter;
	public:
		AggregateFilter(IloConstraintI* filter) :
		  IloException("Aggregate is currently not supported for filter expressions.."), _filter(filter) {}
		  IloConstraintI* getConstraint() const{ return _filter; }
	};


	class NotFoundTuple : public IloException {
		IloTupleCellArray _cells;
		IloTupleCollectionI* _coll;
	public:
		NotFoundTuple(IloTupleCellArray cells, IloTupleCollectionI* coll);
		virtual void print(ILOSTD(ostream)& out) const;
		virtual const char* getMessage() const;
		IloTupleCellArray getCells() const{ return _cells; }
		IloTupleBuffer makeTupleBuffer() const {
			IloTupleBuffer buf = new (_coll->getEnv()) IloTupleBufferI(_coll->getEnv(), _coll, _cells);
			return buf;
		}
		IloTupleCollectionI* getTupleCollection() const { return (IloTupleCollectionI*)_coll; }
	};

	class UnboundSlicer : public IloException {
		IloExtractableI* _exp;
	public:
		UnboundSlicer(IloExtractableI* exp);
		void print(ILOSTD(ostream)& out) const;
		virtual const char* getMessage() const;
	};

	class ElementNumIndex : public IloException {
		const IloExtractableI* _ctx;
		const IloNumExprI* _idx;
	public:
		ElementNumIndex(const IloExtractableI* ctx, const IloNumExprI* e);
		void print(ILOSTD(ostream)& out) const;
		virtual const char* getMessage() const;
	};

	class AggregateSetBody : public IloException {
	private:
		const IloExtractableI* _index;
		const IloExtractableI* _context;
	public:
		AggregateSetBody(const IloExtractableI* index);
		void print(ILOSTD(ostream)& out) const;
		virtual const char* getMessage() const;
		void setContext(const IloExtractableI* c) { _context = c; }
	};

public:
	enum ConstraintStatus {
		Violated,
		Satisfied,
		Unknown
	};

	static IloInt IloOverflowSum(IloNum a, IloNum b) {
		IloNum nr = a + b; 
		if (nr > (IloNum)IloIntMax || nr < (IloNum)IloIntMin){
			throw IloAdvModelEvaluator::Overflow();
		}
		return (IloInt)nr;
	}
	static IloInt IloOverflowSumHandleDown(IloNum a, IloNum b) {
		if (a==-IloIntMax || b==-IloIntMax)
			return -IloIntMax;
		return IloOverflowSum(a,b);
	}
	static IloInt IloOverflowSumHandleUp(IloNum a, IloNum b) {
		if (a==IloIntMax || b==IloIntMax)
			return IloIntMax;
		return IloOverflowSum(a,b);
	}
	static IloInt IloOverflowTimes(IloNum a, IloNum b) { 
		IloNum nr = a * b; 
		if (nr > IloIntMax || nr < -IloIntMax) throw IloAdvModelEvaluator::Overflow();
		return (IloInt)nr;
	}
	static IloInt IloOverflowSub(IloNum a, IloNum b) {
		IloNum nr = a - b; 
		if (nr > IloIntMax || nr < -IloIntMax) throw IloAdvModelEvaluator::Overflow();
		return (IloInt)nr;
	}
	static IloInt IloOverflowSubHandleUp(IloNum a, IloNum b) {
		if (a==IloIntMax || b==-IloIntMax)
			return IloIntMax;
		return IloOverflowSub(a,b);
	}
	static IloInt IloOverflowSubHandleDown(IloNum a, IloNum b) {
		if (a==-IloIntMax || b==IloIntMax)
			return -IloIntMax;
		return IloOverflowSub(a,b);
	}
	static IloInt IloOverflowDiv(IloNum a, IloNum b) {
		IloNum nr = a / b; 
		if (nr > IloIntMax || nr < -IloIntMax) throw IloAdvModelEvaluator::Overflow();
		return (IloInt)nr;
	}
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
