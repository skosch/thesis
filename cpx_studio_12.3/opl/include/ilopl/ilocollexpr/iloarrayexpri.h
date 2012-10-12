// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilocollexpr/iloarrayexpri.h
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

#ifndef __ADVANCED_iloarrayexpriH
#define __ADVANCED_iloarrayexpriH

#ifdef _WIN32
#pragma pack(push, 8)
#endif
#include <ilopl/ilosys.h>

#include <ilopl/iloforallbase.h>
#include <ilopl/ilomapi.h>




class IloIntSetByExtensionExprI : public IloIntCollectionExprArgI {
	ILOEXTRDECL
	IloMapIndexArray _comp;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	// no need to lock e, this is done in the comprehension.
	IloIntSetByExtensionExprI(IloEnvI* env, IloMapIndexArray e);
	virtual ~IloIntSetByExtensionExprI();

	IloMapIndexArray getArray() const { return _comp; }

	
	void display(ILOSTD(ostream)& out) const;
	
	virtual IloNum eval(const IloAlgorithm alg) const;
	
	virtual IloExtractableI* makeClone(IloEnvI*) const;
	virtual IloBool isInteger() const;
};
//




class IloSymbolSetByExtensionExprI : public IloSymbolCollectionExprArgI {
	ILOEXTRDECL
		IloMapIndexArray _comp;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	// no need to lock e, this is done in the comprehension.
	IloSymbolSetByExtensionExprI(IloEnvI* env, IloMapIndexArray e);

	virtual ~IloSymbolSetByExtensionExprI();

	IloMapIndexArray getArray() const { return _comp; }
	ILOEXTROTHERDECL
};
//




class IloTupleSetByExtensionExprI : public IloTupleSetExprArgI {
	ILOEXTRDECL
	IloTupleSchemaI* _schema;
	IloMapIndexArray _comp;

	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	// no need to lock e, this is done in the comprehension.
	IloTupleSetByExtensionExprI(IloEnvI* env, IloTupleSchemaI* schema, IloMapIndexArray e);
	virtual ~IloTupleSetByExtensionExprI();
	IloTupleSchemaI* getSchema() const { return _schema; }
	IloMapIndexArray getArray() const { return _comp; }
	ILOEXTROTHERDECL
};





class IloNumSetByExtensionExprI : public IloNumCollectionExprArgI {
	ILOEXTRDECL
		IloMapIndexArray _comp;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	// no need to lock e, this is done in the comprehension.
	IloNumSetByExtensionExprI(IloEnvI* env, IloMapIndexArray e);
	virtual ~IloNumSetByExtensionExprI();

	IloMapIndexArray getArray() const { return _comp; }

	
	void display(ILOSTD(ostream)& out) const;
	
	virtual IloNum eval(const IloAlgorithm alg) const;
	
	virtual IloExtractableI* makeClone(IloEnvI*) const;
};
//


class IloIntSetByExtensionExprIIterator : public IloIntDataIterator {
	IloIntSetByExtensionExprI*  _expr;
	IloAdvModelEvaluatorI* _evaluator;
public:
	
	IloIntSetByExtensionExprIIterator(IloGenAlloc* heap,
		const IloIntSetByExtensionExprI* expr,
		const IloAdvModelEvaluatorI* eval);
	
	virtual ~IloIntSetByExtensionExprIIterator();
	
	virtual IloBool next();
	
	virtual IloInt recomputeMin() const;
	
	virtual IloInt recomputeMax() const;
	void recomputeBounds(IloInt& min, IloInt& max) const;
	virtual void reset(IloBool catchInvalidCollection = IloFalse);
	virtual void reset(IloInt intMin, IloInt intMax, IloBool catchInvalidCollection = IloFalse);
};




class IloNumSetByExtensionExprIIterator : public IloNumDataIterator {
	IloNumSetByExtensionExprI*  _expr;
	IloAdvModelEvaluatorI* _evaluator;
public:
	
	IloNumSetByExtensionExprIIterator(IloGenAlloc* heap,
		const IloNumSetByExtensionExprI* expr,
		const IloAdvModelEvaluatorI* eval);
	
	virtual ~IloNumSetByExtensionExprIIterator();
	
	virtual IloBool next();
	
	virtual IloNum recomputeLB() const;
	
	virtual IloNum recomputeUB() const;
	void recomputeBounds(IloNum& min, IloNum& max) const;
	virtual void reset(IloBool catchInvalidCollection = IloFalse);
	virtual void reset(IloNum numLB, IloNum numUB, IloBool catchInvalidCollection = IloFalse);
};



class IloSymbolSetByExtensionExprIIterator : public IloAnyDefaultDataIterator {
	IloSymbolSetByExtensionExprI*  _expr;
	IloAdvModelEvaluatorI* _evaluator;
public:
	
	IloSymbolSetByExtensionExprIIterator(IloGenAlloc* heap,
		const IloSymbolSetByExtensionExprI* expr,
		const IloAdvModelEvaluatorI* eval);
	
	virtual ~IloSymbolSetByExtensionExprIIterator();
	
	virtual IloBool next();
	virtual void reset(IloBool catchInvalidCollection = IloFalse);
	virtual void reset(IloAny value, IloBool catchInvalidCollection = IloFalse);
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
