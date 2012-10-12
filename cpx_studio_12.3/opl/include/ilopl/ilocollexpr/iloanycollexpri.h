// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilocollexpr/iloanycollexpri.h
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


#ifndef __ADVANCED_iloanycollexpriH
#define __ADVANCED_iloanycollexpriH

#ifdef _WIN32
#pragma pack(push, 8)
#endif
#include <ilopl/ilosys.h>


#include <ilopl/iloforallbase.h>


class IloTupleIndexI;

class IloSymbolCollectionExprGeneratorI;


class IloSymbolCollectionIndexI : public IloSymbolCollectionExprI {
	ILOEXTRDECL
public:
	IloSymbolCollectionIndexI(IloEnvI* env, const char* name=0);
	virtual ~IloSymbolCollectionIndexI();
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


class IloSymbolCollectionTupleCellExprI : public IloSymbolCollectionExprI {
	ILOEXTRDECL
private:
	IloTupleExprI* _tuple;
	IloSymbolI* _colName;
	void visitSubExtractables(IloExtractableVisitor* v);
public:
	
	IloSymbolCollectionTupleCellExprI(IloEnvI* env, IloTupleExprI* tuple, IloSymbolI* colName);
	
  virtual ~IloSymbolCollectionTupleCellExprI();
	
	IloTupleExprI* getTuple() const { return _tuple; }
	
	IloSymbolI* getColumnName() const { return _colName; }
	ILOEXTROTHERDECL
};


class IloSymbolCollectionExprGeneratorI : public IloSymbolGeneratorI {
	ILOEXTRDECL
private:
	IloSymbolCollectionExprArgI* _coll;
	void visitSubExtractables(IloExtractableVisitor* v);
public:
	
	IloSymbolCollectionExprGeneratorI(IloEnvI* env, IloSymbolIndexI* x, IloSymbolCollectionExprArgI* expr);
	virtual ~IloSymbolCollectionExprGeneratorI();
	
	IloSymbolCollectionExprArgI* getCollection() const { return _coll; }
	
	virtual IloBool generatesDuplicates() const;
	ILOEXTROTHERDECL
  virtual IloDiscreteDataCollectionI* getDiscreteDataCollection() const;
  virtual IloBool hasDiscreteDataCollection() const;
};


class IloEvalSymbolCollectionExprI;
class IloEvalSymbolCollectionExprIIterator : public IloAnyDefaultDataIterator {
	IloEvalSymbolCollectionExprI*  _expr;
public:
	
	IloEvalSymbolCollectionExprIIterator(IloGenAlloc* heap,
		IloEvalSymbolCollectionExprI* expr);
	
	virtual ~IloEvalSymbolCollectionExprIIterator();
	virtual void reset(IloBool catchInvalidCollection = IloFalse);
	virtual void reset(IloAny value, IloBool catchInvalidCollection = IloFalse);
};


class IloSymbolCollectionTupleCellExprIIterator : public IloAnyDefaultDataIterator {
	IloSymbolCollectionTupleCellExprI*  _expr;
	IloAdvModelEvaluatorI* _evaluator;
public:
	
	IloSymbolCollectionTupleCellExprIIterator(IloGenAlloc* heap,
		const IloSymbolCollectionTupleCellExprI* rangeexpr,
		const IloAdvModelEvaluatorI* eval);
	
	virtual ~IloSymbolCollectionTupleCellExprIIterator();
	virtual void reset(IloBool catchInvalidCollection = IloFalse);
	virtual void reset(IloAny value, IloBool catchInvalidCollection = IloFalse);
};




class IloSymbolCollectionIndexIIterator : public IloAnyDefaultDataIterator {
	IloSymbolCollectionIndexI*  _expr;
	IloAdvModelEvaluatorI* _evaluator;
public:
	
	IloSymbolCollectionIndexIIterator(IloGenAlloc* heap,
		const IloSymbolCollectionIndexI* rangeexpr,
		const IloAdvModelEvaluatorI* eval);
	
	virtual ~IloSymbolCollectionIndexIIterator();
	virtual void reset(IloBool catchInvalidCollection = IloFalse);
	virtual void reset(IloAny value, IloBool catchInvalidCollection = IloFalse);
};



class IloSymbolCollectionSubMapExprI;

class IloSymbolCollectionSubMapExprIIterator : public IloAnyDefaultDataIterator {
	const IloSymbolCollectionSubMapExprI*  _expr;
	IloAdvModelEvaluatorI* _evaluator;
public:
	
	IloSymbolCollectionSubMapExprIIterator(IloGenAlloc* heap,
		const IloSymbolCollectionSubMapExprI* map,
		const IloAdvModelEvaluatorI* eval);
	
	virtual ~IloSymbolCollectionSubMapExprIIterator();
	virtual void reset(IloBool catchInvalidCollection = IloFalse);
	virtual void reset(IloAny value, IloBool catchInvalidCollection = IloFalse);
};




class IloSymbolAggregateSetExprI : public IloSymbolCollectionExprArgI {
	ILOEXTRDECL
		IloExtendedComprehensionI* _comp;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	// no need to lock e, this is done in the comprehension.
	IloSymbolAggregateSetExprI(IloExtendedComprehensionI* comp);

	virtual ~IloSymbolAggregateSetExprI();

	IloExtendedComprehensionI* getComprehension() const { return _comp; }

	IloSymbolExprI* getBody() const { return (IloSymbolExprI*)_comp->getExtent(); }
	ILOEXTROTHERDECL
};
//



class IloTupleAggregateSetExprI : public IloTupleSetExprArgI {
	ILOEXTRDECL
		IloExtendedComprehensionI* _comp;

	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	// no need to lock e, this is done in the comprehension.
	IloTupleAggregateSetExprI(IloTupleSchemaI* schema, IloExtendedComprehensionI* comp);

	virtual ~IloTupleAggregateSetExprI();

	IloExtendedComprehensionI* getComprehension() const { return _comp; }

	IloTupleExprI* getBody() const { return (IloTupleExprI*)_comp->getExtent(); }
	ILOEXTROTHERDECL
};







class IloSymbolAggregateUnionSetExprI : public IloSymbolCollectionExprArgI {
	ILOEXTRDECL
	IloExtendedComprehensionI* _comp;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	// no need to lock e, this is done in the comprehension.
	IloSymbolAggregateUnionSetExprI(IloExtendedComprehensionI* comp);

	virtual ~IloSymbolAggregateUnionSetExprI();

	  IloExtendedComprehensionI* getComprehension() const { return _comp; }

	  IloSymbolCollectionExprI* getBody() const { return (IloSymbolCollectionExprI*)_comp->getExtent(); }
	ILOEXTROTHERDECL
};
//



class IloTupleAggregateUnionSetExprI : public IloTupleSetExprArgI {
	ILOEXTRDECL
	IloExtendedComprehensionI* _comp;

	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	// no need to lock e, this is done in the comprehension.
	IloTupleAggregateUnionSetExprI(IloExtendedComprehensionI* comp);

	virtual ~IloTupleAggregateUnionSetExprI();

	  IloExtendedComprehensionI* getComprehension() const { return _comp; }

	  IloTupleSetExprI* getBody() const { return (IloTupleSetExprI*)_comp->getExtent(); }
	ILOEXTROTHERDECL
};








class IloSymbolAggregateInterSetExprI : public IloSymbolCollectionExprArgI {
	ILOEXTRDECL
	IloExtendedComprehensionI* _comp;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	// no need to lock e, this is done in the comprehension.
	IloSymbolAggregateInterSetExprI(IloExtendedComprehensionI* comp);

	virtual ~IloSymbolAggregateInterSetExprI();

	  IloExtendedComprehensionI* getComprehension() const { return _comp; }

	  IloSymbolCollectionExprI* getBody() const { return (IloSymbolCollectionExprI*)_comp->getExtent(); }
	ILOEXTROTHERDECL
};
//



class IloTupleAggregateInterSetExprI : public IloTupleSetExprArgI {
	ILOEXTRDECL
	IloExtendedComprehensionI* _comp;

	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	// no need to lock e, this is done in the comprehension.
	IloTupleAggregateInterSetExprI(IloExtendedComprehensionI* comp);

	virtual ~IloTupleAggregateInterSetExprI();

	  IloExtendedComprehensionI* getComprehension() const { return _comp; }

	  IloTupleSetExprI* getBody() const { return (IloTupleSetExprI*)_comp->getExtent(); }
	ILOEXTROTHERDECL
};



class IloSymbolCollectionConstI : public IloSymbolCollectionExprI {
	ILOEXTRDECL
private:
	IloAnyCollectionI* _coll;
  IloBool _ownsColl;
	void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloSymbolCollectionConstI(IloEnvI* env, IloAnyCollectionI* coll, IloBool ownsColl=IloFalse);
	virtual ~IloSymbolCollectionConstI();
	IloAnyCollectionI* getCollection() const { return _coll; }
	ILOEXTROTHERDECL
};

//-------------------------------------------------------------
class IloConditionalSymbolSetExprI : public IloConditionalExprI< IloSymbolCollectionExprI, IloConditionalSymbolSetExprI, IloFalse > {
	ILOEXTRDECL

public:
	IloConditionalSymbolSetExprI(IloEnvI* env, IloConstraintI* cond, IloSymbolCollectionExprI* left, IloSymbolCollectionExprI* right )
		: IloConditionalExprI< IloSymbolCollectionExprI, IloConditionalSymbolSetExprI, IloFalse >( env, cond, left, right ) {}
	virtual ~IloConditionalSymbolSetExprI(){}
};

//-------------------------------------------------------------
class IloConditionalTupleSetExprI : public IloConditionalExprI< IloTupleSetExprI, IloConditionalTupleSetExprI, IloFalse > {

	ILOEXTRDECL
public:
	IloConditionalTupleSetExprI(IloEnvI* env, IloConstraintI* cond, IloTupleSetExprI* left, IloTupleSetExprI* right )
		: IloConditionalExprI< IloTupleSetExprI, IloConditionalTupleSetExprI, IloFalse >( env, cond, left, right ) {}
};

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
