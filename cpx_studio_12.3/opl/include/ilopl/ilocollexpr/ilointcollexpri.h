// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilocollexpr/ilointcollexpri.h
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


#ifndef __ADVANCED_ilointcollexpriH
#define __ADVANCED_ilointcollexpriH

#ifdef _WIN32
#pragma pack(push, 8)
#endif
#include <ilopl/ilosys.h>

#include <ilopl/iloforallbase.h>



class IloTupleIndexI;

class IloIntCollectionExprGeneratorI;
class IloIntCollectionExprI;
class IloAdvModelEvaluatorI;


class IloIntCollectionIndexI : public IloIntExprI {
	ILOEXTRDECL
public:
	IloIntCollectionIndexI(IloEnvI* env, const char* name=0);
	virtual ~IloIntCollectionIndexI();
	IloNum eval(const IloAlgorithm alg) const;
	virtual IloBool isInteger() const;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
	ILOEXTROTHERDECL
	virtual IloBool hasNongroundType() const;
	IloInt getKey() const{
		return (IloInt)this;
	}
	void errorNotSubstituted() const{
		throw IloIndex::NotSubstituted(this);
	}
};


class IloIntCollectionTupleCellExprI : public IloIntCollectionExprI {
	ILOEXTRDECL
private:
	IloTupleExprI* _tuple;
	IloSymbolI* _colName;
	void visitSubExtractables(IloExtractableVisitor* v);
public:
	
	IloIntCollectionTupleCellExprI(IloEnvI* env, IloTupleExprI* tuple, IloSymbolI* colName);
	
  virtual ~IloIntCollectionTupleCellExprI();
	
	IloTupleExprI* getTuple() const { return _tuple; }
	
	IloSymbolI* getColumnName() const { return _colName; }
	
	virtual IloNum eval(const IloAlgorithm alg) const;
	virtual IloBool isInteger() const;
	ILOEXTROTHERDECL
};


class IloIntCollectionExprGeneratorI : public IloIntGeneratorI {
	ILOEXTRDECL
private:
	IloIntCollectionExprArgI* _coll;
	void visitSubExtractables(IloExtractableVisitor* v);
public:
	
	IloIntCollectionExprGeneratorI(IloEnvI* env, IloIntIndexI* x, IloIntCollectionExprArgI* expr);
	virtual ~IloIntCollectionExprGeneratorI();
	
	IloIntCollectionExprArgI* getCollection() const { return _coll; }
	
	virtual IloBool generatesDuplicates() const;
	ILOEXTROTHERDECL
		virtual IloDiscreteDataCollectionI* getDiscreteDataCollection() const;
	virtual IloBool hasDiscreteDataCollection() const;
};


class IloEvalIntCollectionExprI;
class IloEvalIntCollectionExprIIterator : public IloIntDefaultDataIterator {
	IloEvalIntCollectionExprI*  _expr;
public:
	
	IloEvalIntCollectionExprIIterator(IloGenAlloc* heap,
		IloEvalIntCollectionExprI* expr);
	
	virtual ~IloEvalIntCollectionExprIIterator();
	virtual IloInt recomputeMin() const;
	
	virtual IloInt recomputeMax() const;
	virtual void reset(IloBool catchInvalidCollection = IloFalse);
	virtual void reset(IloInt min, IloInt max, IloBool catchInvalidCollection = IloFalse);
};


class IloIntRangeExprI : public IloIntCollectionExprArgI {
	ILOEXTRDECL
	IloIntExprI* _lb;
	IloIntExprI* _ub;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	
	IloIntRangeExprI(IloEnvI* env, IloIntExprI* lb, IloIntExprI* ub);
	  virtual ~IloIntRangeExprI();
	
	  void display(ILOSTD(ostream)& out) const;
	
	  virtual IloNum eval(const IloAlgorithm alg) const;
	
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	
	  IloIntExprI* getLB() const { return _lb; }
	
	  IloIntExprI* getUB() const { return _ub; }
	  virtual IloBool isInteger() const;
};





class IloIntRangeAsIntSetExprI : public IloIntRangeExprI {
	// temporary for frank.
	IloIntRangeExprI* _expr;
	ILOEXTRDECL
public:
	// temporary for frank.
	IloIntRangeAsIntSetExprI(IloEnvI* env, IloIntRangeExprI* R);
	  virtual ~IloIntRangeAsIntSetExprI();
	
	  void display(ILOSTD(ostream)& out) const;
	
	  virtual IloExtractableI* makeClone(IloEnvI*) const;

  	  // temporary for frank.
	  IloIntRangeExprI* getRange() const{
		  return _expr;
	  }
	  IloIntRangeExprI* makeRange() const{
		  return new (getEnv()) IloIntRangeExprI(getEnv(), getLB(), getUB());
	  }
};



class IloIntRangeExprIIterator : public IloIntDataIterator {
	IloIntRangeExprI*  _rangeexpr;
	IloAdvModelEvaluatorI* _evaluator;
public:
	
	IloIntRangeExprIIterator(IloGenAlloc* heap,
		const IloIntRangeExprI* rangeexpr,
		const IloAdvModelEvaluatorI* eval);
	
	virtual ~IloIntRangeExprIIterator();
	
	virtual IloBool next();
	
	virtual void reset(IloBool catchInvalidCollection = IloFalse);
	
	virtual void reset(IloInt min, IloInt max, IloBool catchInvalidCollection = IloFalse);
	
	virtual IloInt recomputeMin() const;
	
	virtual IloInt recomputeMax() const;
	void recomputeBounds(IloInt& min, IloInt& max) const;
};



class IloIntCollectionTupleCellExprIIterator : public IloIntDefaultDataIterator {
	IloIntCollectionTupleCellExprI*  _expr;
	IloAdvModelEvaluatorI* _evaluator;
public:
	
	IloIntCollectionTupleCellExprIIterator(IloGenAlloc* heap,
		const IloIntCollectionTupleCellExprI* rangeexpr,
		const IloAdvModelEvaluatorI* eval);
	
	virtual ~IloIntCollectionTupleCellExprIIterator();
	
	virtual IloInt recomputeMin() const;
	virtual IloInt recomputeMax() const;
	virtual void reset(IloBool catchInvalidCollection = IloFalse);
	virtual void reset(IloInt min, IloInt max, IloBool catchInvalidCollection = IloFalse);
};



class IloIntCollectionIndexIIterator : public IloIntDefaultDataIterator {
	IloIntCollectionIndexI*  _expr;
	IloAdvModelEvaluatorI* _evaluator;
public:
	
	IloIntCollectionIndexIIterator(IloGenAlloc* heap,
		const IloIntCollectionIndexI* rangeexpr,
		const IloAdvModelEvaluatorI* eval);
	
	virtual ~IloIntCollectionIndexIIterator();
	
	virtual IloInt recomputeMin() const;
	
	virtual IloInt recomputeMax() const;
	virtual void reset(IloBool catchInvalidCollection = IloFalse);
	virtual void reset(IloInt min, IloInt max, IloBool catchInvalidCollection = IloFalse);
};



class IloIntCollectionSubMapExprI;

class IloIntCollectionSubMapExprIIterator : public IloIntDefaultDataIterator {
	const IloIntCollectionSubMapExprI*  _expr;
	IloAdvModelEvaluatorI* _evaluator;
public:
	
	IloIntCollectionSubMapExprIIterator(IloGenAlloc* heap,
		const IloIntCollectionSubMapExprI* map,
		const IloAdvModelEvaluatorI* eval);
	
	virtual ~IloIntCollectionSubMapExprIIterator();
	virtual IloInt recomputeMin() const;
	
	virtual IloInt recomputeMax() const;
	virtual void reset(IloBool catchInvalidCollection = IloFalse);
	virtual void reset(IloInt min, IloInt max, IloBool catchInvalidCollection = IloFalse);
};




class IloIntAggregateSetExprI : public IloIntCollectionExprArgI {
	ILOEXTRDECL
	IloExtendedComprehensionI* _comp;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	// no need to lock e, this is done in the comprehension.
	IloIntAggregateSetExprI(IloExtendedComprehensionI* comp);

	virtual ~IloIntAggregateSetExprI();

	  IloExtendedComprehensionI* getComprehension() const { return _comp; }

	
	  void display(ILOSTD(ostream)& out) const;
	
	  virtual IloNum eval(const IloAlgorithm alg) const;
	
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	  virtual IloBool isInteger() const;
	  IloIntExprI* getBody() const { return (IloIntExprI*)_comp->getExtent(); }
};
//





class IloIntAggregateUnionSetExprI : public IloIntCollectionExprArgI {
	ILOEXTRDECL
	IloExtendedComprehensionI* _comp;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloIntAggregateUnionSetExprI(IloExtendedComprehensionI* comp);

	virtual ~IloIntAggregateUnionSetExprI();

	  IloExtendedComprehensionI* getComprehension() const { return _comp; }

	
	  void display(ILOSTD(ostream)& out) const;
	
	  virtual IloNum eval(const IloAlgorithm alg) const;
	
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	  virtual IloBool isInteger() const;
	  IloIntExprI* getBody() const { return (IloIntExprI*)_comp->getExtent(); }
};
//





class IloIntAggregateInterSetExprI : public IloIntCollectionExprArgI {
	ILOEXTRDECL
	IloExtendedComprehensionI* _comp;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloIntAggregateInterSetExprI(IloExtendedComprehensionI* comp);

	virtual ~IloIntAggregateInterSetExprI();

	  IloExtendedComprehensionI* getComprehension() const { return _comp; }

	
	  void display(ILOSTD(ostream)& out) const;
	
	  virtual IloNum eval(const IloAlgorithm alg) const;
	
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	  virtual IloBool isInteger() const;
	  IloIntCollectionExprI* getBody() const { return (IloIntCollectionExprI*)_comp->getExtent(); }
};
//



class IloIntCollectionConstI : public IloIntCollectionExprI {
	ILOEXTRDECL
private:
	IloIntCollectionI* _coll;
  IloBool _ownsColl;
	void visitSubExtractables(IloExtractableVisitor* v);
public:
	
	IloIntCollectionConstI(IloEnvI* env, IloIntCollectionI* coll, IloBool ownsColl=IloFalse);
	
	virtual ~IloIntCollectionConstI();
	
	IloIntCollectionI* getCollection() const { return _coll; }
	
	virtual IloNum eval(const IloAlgorithm) const{
		throw IloWrongUsage("can not evaluate");
		ILOUNREACHABLE(return 0;)
	}
	virtual IloBool isInteger() const{ return IloTrue;}
	ILOEXTROTHERDECL
};


//-------------------------------------------------------------
class IloConditionalIntSetExprI
  : public IloConditionalExprI< IloIntCollectionExprI, IloConditionalIntSetExprI, IloFalse > {
	ILOEXTRDECL

public:
	IloConditionalIntSetExprI(IloEnvI* env, IloConstraintI* cond, IloIntCollectionExprI* left, IloIntCollectionExprI* right )
	  : IloConditionalExprI< IloIntCollectionExprI, IloConditionalIntSetExprI, IloFalse >( env, cond, left, right ) {}

	virtual ~IloConditionalIntSetExprI(){}
	virtual IloNum eval(const IloAlgorithm) const {
		throw IloWrongUsage("IloConditionalIntSetExprI::eval : a collection expression cannot be evaluated to a number.");
		ILOUNREACHABLE(return 0;)
	}
};

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
