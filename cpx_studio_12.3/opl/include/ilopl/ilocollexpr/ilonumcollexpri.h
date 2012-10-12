// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilocollexpr/ilonumcollexpri.h
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


#ifndef __ADVANCED_ilonumcollexpriH
#define __ADVANCED_ilonumcollexpriH

#ifdef _WIN32
#pragma pack(push, 8)
#endif
#include <ilopl/ilosys.h>

#include <ilopl/iloforallbase.h>

#define IloNumCollectionExprArgI IloNumExprI
#define IloNumCollectionExprI IloNumExprI

class IloTupleIndexI;

class IloNumCollectionExprGeneratorI;


class IloNumCollectionIndexI : public IloNumExprI {
	ILOEXTRDECL
public:
	IloNumCollectionIndexI(IloEnvI* env, const char* name=0);
	virtual ~IloNumCollectionIndexI();
	IloNum eval(const IloAlgorithm alg) const;
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


class IloNumCollectionTupleCellExprI : public IloNumCollectionExprI {
	ILOEXTRDECL
private:
	IloTupleExprI* _tuple;
	IloSymbolI* _colName;
	void visitSubExtractables(IloExtractableVisitor* v);
public:
	
	IloNumCollectionTupleCellExprI(IloEnvI* env, IloTupleExprI* tuple, IloSymbolI* colName);
	
  virtual ~IloNumCollectionTupleCellExprI();
	
	IloTupleExprI* getTuple() const { return _tuple; }
	
	IloSymbolI* getColumnName() const { return _colName; }
	
	IloNum eval(const IloAlgorithm alg) const;

	ILOEXTROTHERDECL
};


class IloNumCollectionExprGeneratorI : public IloNumGeneratorI {
	ILOEXTRDECL
private:
	IloNumCollectionExprArgI* _coll;
	void visitSubExtractables(IloExtractableVisitor* v);
public:
	
	IloNumCollectionExprGeneratorI(IloEnvI* env, IloNumIndexI* x, IloNumCollectionExprArgI* expr);
	virtual ~IloNumCollectionExprGeneratorI();
	
	IloNumCollectionExprArgI* getCollection() const { return _coll; }
	
	virtual IloBool generatesDuplicates() const;
	ILOEXTROTHERDECL
  virtual IloDiscreteDataCollectionI* getDiscreteDataCollection() const;
  virtual IloBool hasDiscreteDataCollection() const;
};



class IloEvalFloatCollectionExprI;
class IloEvalNumCollectionExprIIterator : public IloNumDefaultDataIterator {
	IloEvalFloatCollectionExprI*  _expr;
public:
	
	IloEvalNumCollectionExprIIterator(IloGenAlloc* heap,
		IloEvalFloatCollectionExprI* expr);
	
	virtual ~IloEvalNumCollectionExprIIterator();
	virtual void reset(IloBool catchInvalidCollection = IloFalse);
	virtual void reset(IloNum numLB, IloNum numUB, IloBool catchInvalidCollection = IloFalse);
};



class IloNumRangeExprI : public IloNumCollectionExprArgI {
	ILOEXTRDECL
	IloNumExprI* _lb;
	IloNumExprI* _ub;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	
	IloNumRangeExprI(IloEnvI* env, IloNumExprI* lb, IloNumExprI* ub);
	  virtual ~IloNumRangeExprI();
	
	  void display(ILOSTD(ostream)& out) const;
	
	  virtual IloNum eval(const IloAlgorithm alg) const;
	
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	
	  IloNumExprI* getLB() const { return _lb; }
	
	  IloNumExprI* getUB() const { return _ub; }
};


class IloAdvModelEvaluatorI;


class IloNumRangeExprIIterator : public IloNumDataIterator {
	IloNumRangeExprI*  _rangeexpr;
	IloAdvModelEvaluatorI* _evaluator;
public:
	
	IloNumRangeExprIIterator(IloGenAlloc* heap,
		const IloNumRangeExprI* rangeexpr,
		const IloAdvModelEvaluatorI* eval);
	
	virtual ~IloNumRangeExprIIterator();
	
	virtual IloBool next();
	
	virtual void reset(IloBool catchInvalidCollection = IloFalse);
	
	virtual void reset(IloNum min, IloNum max, IloBool catchInvalidCollection = IloFalse);
	
	virtual IloNum recomputeMin() const;
	
	virtual IloNum recomputeMax() const;
};




class IloNumCollectionTupleCellExprIIterator : public IloNumDefaultDataIterator {
	IloNumCollectionTupleCellExprI*  _expr;
	IloAdvModelEvaluatorI* _evaluator;
public:
	
	IloNumCollectionTupleCellExprIIterator(IloGenAlloc* heap,
		const IloNumCollectionTupleCellExprI* rangeexpr,
		const IloAdvModelEvaluatorI* eval);
	
	virtual ~IloNumCollectionTupleCellExprIIterator();
	virtual void reset(IloBool catchInvalidCollection = IloFalse);
	virtual void reset(IloNum numLB, IloNum numUB, IloBool catchInvalidCollection = IloFalse);
};




class IloNumCollectionIndexIIterator : public IloNumDefaultDataIterator {
	IloNumCollectionIndexI*  _expr;
	IloAdvModelEvaluatorI* _evaluator;
public:
	
	IloNumCollectionIndexIIterator(IloGenAlloc* heap,
		const IloNumCollectionIndexI* rangeexpr,
		const IloAdvModelEvaluatorI* eval);
	
	virtual ~IloNumCollectionIndexIIterator();
	virtual void reset(IloBool catchInvalidCollection = IloFalse);
	virtual void reset(IloNum numLB, IloNum numUB, IloBool catchInvalidCollection = IloFalse);
};



class IloNumCollectionSubMapExprI;

class IloNumCollectionSubMapExprIIterator : public IloNumDefaultDataIterator {
	const IloNumCollectionSubMapExprI*  _expr;
	IloAdvModelEvaluatorI* _evaluator;
public:
	
	IloNumCollectionSubMapExprIIterator(IloGenAlloc* heap,
		const IloNumCollectionSubMapExprI* map,
		const IloAdvModelEvaluatorI* eval);
	
	virtual ~IloNumCollectionSubMapExprIIterator();
	virtual void reset(IloBool catchInvalidCollection = IloFalse);
	virtual void reset(IloNum numLB, IloNum numUB, IloBool catchInvalidCollection = IloFalse);
};



class IloNumAggregateSetExprI : public IloNumCollectionExprArgI {
	ILOEXTRDECL
	IloExtendedComprehensionI* _comp;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloNumAggregateSetExprI(IloExtendedComprehensionI* comp);

	virtual ~IloNumAggregateSetExprI();

	  IloExtendedComprehensionI* getComprehension() const { return _comp; }

	
	  void display(ILOSTD(ostream)& out) const;
	
	  virtual IloNum eval(const IloAlgorithm alg) const;
	
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	  IloNumExprI* getBody() const { return (IloNumExprI*)_comp->getExtent(); }
};
//





class IloNumAggregateUnionSetExprI : public IloNumCollectionExprArgI {
	ILOEXTRDECL
	IloExtendedComprehensionI* _comp;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloNumAggregateUnionSetExprI(IloExtendedComprehensionI* comp);

	virtual ~IloNumAggregateUnionSetExprI();

	  IloExtendedComprehensionI* getComprehension() const { return _comp; }

	
	  void display(ILOSTD(ostream)& out) const;
	
	  virtual IloNum eval(const IloAlgorithm alg) const;
	
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	  IloNumExprI* getBody() const { return (IloNumExprI*)_comp->getExtent(); }
};
//




class IloNumAggregateInterSetExprI : public IloNumCollectionExprArgI {
	ILOEXTRDECL
	IloExtendedComprehensionI* _comp;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloNumAggregateInterSetExprI(IloExtendedComprehensionI* comp);

	virtual ~IloNumAggregateInterSetExprI();

	  IloExtendedComprehensionI* getComprehension() const { return _comp; }

	
	  void display(ILOSTD(ostream)& out) const;
	
	  virtual IloNum eval(const IloAlgorithm alg) const;
	
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	  IloNumCollectionExprI* getBody() const { return (IloNumCollectionExprI*)_comp->getExtent(); }
};
//


class IloNumCollectionConstI : public IloNumCollectionExprI {
	ILOEXTRDECL
private:
	IloNumCollectionI* _coll;
  IloBool _ownsColl;
	void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloNumCollectionConstI(IloEnvI* env, IloNumCollectionI* coll, IloBool ownsColl=IloFalse);
	virtual ~IloNumCollectionConstI();
	IloNumCollectionI* getCollection() const { return _coll; }
	IloNum eval(const IloAlgorithm) const{
		throw IloWrongUsage("can not evaluate");
		ILOUNREACHABLE(return 0;)
	}
	ILOEXTROTHERDECL
};

//-------------------------------------------------------------
class IloConditionalNumSetExprI
  : public IloConditionalExprI< IloNumCollectionExprI, IloConditionalNumSetExprI, IloFalse > {
	ILOEXTRDECL
public:
	IloConditionalNumSetExprI(IloEnvI* env, IloConstraintI* cond, IloNumCollectionExprI* left, IloNumCollectionExprI* right )
		: IloConditionalExprI< IloNumCollectionExprI, IloConditionalNumSetExprI, IloFalse >( env, cond, left, right ) {}

	virtual IloNum eval(const IloAlgorithm) const {
		throw IloWrongUsage("IloConditionalNumSetExprI::eval : a collection expression cannot be evaluated to a number.");
		ILOUNREACHABLE(return 0;)
	}
	virtual ~IloConditionalNumSetExprI(){}
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
