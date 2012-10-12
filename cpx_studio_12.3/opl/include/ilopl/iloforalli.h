// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/iloforalli.h
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

#ifndef __ADVANCED_iloforalliH
#define __ADVANCED_iloforalliH


#ifdef _WIN32
#pragma pack(push, 8)
#endif
#include <ilopl/ilosys.h>

#include <ilopl/iloforallbase.h>
#include <ilconcert/ilocollection.h>
#include <ilopl/ilotuplecollectioni.h>
#include <ilopl/ilotuple.h>
#include <ilopl/iloexpressioni.h>
#include <ilopl/ilosymbolexpri.h>

class IloAdvModelEvaluatorI;

class IloIntCollectionExprArg;
class IloNumCollectionExprArg;
class IloSymbolCollectionExprArg;

class IloIntGeneratorI;
class IloNumGeneratorI;
//TUPLE_EXPR
class IloSymbolGeneratorI;
class IloTupleGeneratorI;
class IloModelEvaluatorI;
class IloIntIndex;
class IloNumIndex;
class IloSymbolIndex;
class IloTupleIndex;
class IloTuplePattern;
class IloTuplePatternI;
class IloGeneratorI;

/////////////////////////////////////////////////////////////////////////////
//   Formal Indexes
/////////////////////////////////////////////////////////////////////////////

class IloIntSubMapExpr;
class IloIntSubMapExprI;
class IloNumSubMapExpr;
class IloNumSubMapExprI;
class IloSymbolSubMapExpr;
class IloSymbolSubMapExprI;
class IloTupleSchemaI;
//



//-------------------------------------------
class IloIntIndexI : public IloIntExprI {
	ILOEXTRDECL

public:
	virtual ~IloIntIndexI();
	IloIntGeneratorI* within(IloIntCollectionI* coll) const;

	IloIntGeneratorI* within(IloIntCollectionExprArgI* expr) const;

	IloNum eval(const IloAlgorithm alg) const;
	virtual void visitSubExtractables(IloExtractableVisitor* v);

	ILOEXTROTHERDECL
	virtual IloBool hasNongroundType() const;

	IloInt getKey() const{ 
		return (IloInt)this; 
	}
	IloIntIndexI(IloEnvI* env, const char* name=0);
	void errorNotSubstituted() const{
		throw IloIndex::NotSubstituted(this);
	}
	virtual IloBool isGround() const;
};

//-------------------------------------------
class IloNumIndexI : public IloNumExprI {
	ILOEXTRDECL
public:
	IloNumIndexI(IloEnvI* env, const char* name=0);
	virtual ~IloNumIndexI();
	IloNumGeneratorI* within(IloNumCollectionI* coll) const;

	IloNumGeneratorI* within(IloNumCollectionExprArgI* expr) const;

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
	virtual IloBool isGround() const;
};

//-------------------------------------------
class IloSymbolIndexI : public IloSymbolExprI{
	ILOEXTRDECL
		IloExtractableI* makeClone(IloEnvI* env) const;
public:
	IloSymbolIndexI(IloEnvI* env, const char* name=0);
	IloSymbolGeneratorI* within(IloAnyCollectionI* coll) const;
	IloSymbolGeneratorI* within(IloSymbolCollectionExprArgI* expr) const;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
	virtual IloBool hasNongroundType() const;

	IloInt getKey() const{ 
		return (IloInt)this; 
	}
	void errorNotSubstituted() const{
		throw IloIndex::NotSubstituted(this);
	}
	virtual void display(ILOSTD(ostream)& out) const;
};

//-------------------------------------------
class IloTupleIndexI : public IloTupleExprI {
protected:
	IloBool _isKey;
	ILOEXTRDECL
		IloExtractableI* makeClone(IloEnvI* env) const;
public:
	IloTupleIndexI(IloEnvI* env, const char* name=0);
	IloTupleGeneratorI* within(IloTupleSetI* coll) const;
	IloTupleGeneratorI* within(IloTupleSetExprArgI* expr) const;

	virtual void checkCompatibility(IloTupleSchemaI*) const;
	virtual void setKeyProperty(IloBool flag) { _isKey = flag; }
	IloBool isKey() const{ return _isKey; }
	virtual void display(ILOSTD(ostream)& out) const;
	virtual IloBool isTupleIndex() const{ return IloTrue;}
	virtual void visitSubExtractables(IloExtractableVisitor* v);
	virtual IloBool hasNongroundType() const;

	IloInt getKey() const{ 
		return (IloInt)this; 
	}
	void errorNotSubstituted() const{
		throw IloIndex::NotSubstituted(this);
	}
	virtual ~IloTupleIndexI(){
	}
};



//-------------------------------------------
class IloIntTupleCellExprI : public IloIntExprI {
  ILOEXTRDECL
private:
  IloTupleExprI* _tuple;
  IloSymbolI* _colName;
  void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloIntTupleCellExprI(IloEnvI* env,
			IloTupleExprI* tuple, IloSymbolI* colName);
  virtual ~IloIntTupleCellExprI();
  IloTupleExprI* getTuple() const { return _tuple; }
  IloSymbolI* getColumnName() const { return _colName; }
  IloNum eval(const IloAlgorithm alg) const;

  ILOEXTROTHERDECL
};

//-------------------------------------------
class IloNumTupleCellExprI : public IloNumExprI {
  ILOEXTRDECL
private:
  IloTupleExprI* _tuple;
  IloSymbolI* _colName;
  void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloNumTupleCellExprI(IloEnvI* env,
			IloTupleExprI* tuple, IloSymbolI* colName);
  virtual ~IloNumTupleCellExprI();
  IloTupleExprI* getTuple() const { return _tuple; }
  IloSymbolI* getColumnName() const { return _colName; }
  IloNum eval(const IloAlgorithm alg) const;

  ILOEXTROTHERDECL
};

//-------------------------------------------
class IloSymbolTupleCellExprI : public IloSymbolExprI {
  ILOEXTRDECL
private:
  IloTupleExprI* _tuple;
  IloSymbolI* _colName;
  void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloSymbolTupleCellExprI(IloEnvI* env,
			   IloTupleExprI* tuple, IloSymbolI* colName);
  virtual ~IloSymbolTupleCellExprI();
  IloTupleExprI* getTuple() const { return _tuple; }
  IloSymbolI* getColumnName() const { return _colName; }

  ILOEXTROTHERDECL
};

//-------------------------------------------
class IloTupleTupleCellExprI : public IloTupleExprI {
  ILOEXTRDECL
private:
  IloBool _isKey;
  IloTupleExprI* _tuple;
  IloSymbolI* _colName;
  void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloTupleTupleCellExprI(IloEnvI* env,
			  IloTupleExprI* tuple, IloSymbolI* colName);
  virtual ~IloTupleTupleCellExprI();
  IloTupleExprI* getTuple() const { return _tuple; }
  IloSymbolI* getColumnName() const { return _colName; }
  virtual void setKeyProperty(IloBool flag) { _isKey = flag; }
  IloBool isKey() const{ return _isKey; }
  ILOEXTROTHERDECL
};

//-------------------------------------------
#define IloTuplePatternItem IloOplObject
#define IloTuplePatternItemArray IloMapIndexArray

//-------------------------------------------
class IloMapIndexArray;
class IloModelEvaluator;
class IloTuplePatternI : public IloTupleIndexI {
  friend class IloTupleSet;
  ILOEXTRDECL
public:
  enum Status {
    ConstPattern=0,
    ExprPattern=1,
    UndefPattern=2
  };
private:
  Status _status;
private:
	IloInt _arity;
	IloTuplePatternItem* _items;
	void visitSubExtractables(IloExtractableVisitor* v);
	IloBool isPatternConst();
public:
	IloInt getTotalArityForConstPattern() const;

	IloBool hasSchema() const{ return getSchema()!=0; }

	virtual void checkCompatibility(IloTupleSchemaI* schema) const;
	void checkColumnCompatibility(IloTuplePatternItem* item,
		IloColumnDefinitionI* col, IloInt pos) const;
	void lockSubExpr();
	void releaseSubExpr(IloEnvI* env);
	IloTuplePatternI(IloEnvI* env, IloInt size, IloTuplePatternItem* items);
	virtual ~IloTuplePatternI();  
	IloInt getArity() const { return _arity; }
	IloTuplePatternItem* getItems() const { return _items; }
	IloTuplePatternItem* getItem(IloInt i) const;
	IloExtractableI* getExtractable(IloInt i) const;
	IloInt getIndexKey(IloInt i) const;
	IloTuplePatternI* copy() const;
	IloTuplePatternI(IloEnvI* env, Status status, IloInt size, IloTuplePatternItem* items);
	Status getStatus() const { 
		return _status; 
	};
	IloBool isConst() const { 
		return !_status; 
	}
	IloBool isExpr() const { return (_status & 1); }
	ILOEXTROTHERDECL
		virtual void setKeyProperty(IloBool flag);
	virtual IloBool isTupleIndex() const{ return IloFalse;}
	virtual IloBool isTuplePattern() const{ return IloTrue;}
	void getItemNames(IloStringArray arr);
	void flattenPattern(IloAdvModelEvaluatorI* e, IloMapIndexArray arr);
	virtual void setSchema(IloTupleSchemaI* schema);
};

/////////////////////////////////////////////////////////////////////////////
//   Generators
/////////////////////////////////////////////////////////////////////////////

//-------------------------------------------
class IloIntGeneratorI : public IloGeneratorI {
  ILOEXTRDECL
private:
  IloIntIndexI* _x;
protected:
  void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloIntGeneratorI(IloEnvI* env, IloIntIndexI* x);
  virtual ~IloIntGeneratorI();
  IloInt getIndexKey() const;
  IloIntIndexI* getIndex() const { return _x; }
  virtual IloDataIterator* iterator() const;
  virtual IloDiscreteDataCollectionI* getDiscreteDataCollection() const;
};

//-------------------------------------------
class IloIntCollectionGeneratorI : public IloIntGeneratorI {
  ILOEXTRDECL
private:
  IloIntCollectionI* _coll;
  void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloIntCollectionGeneratorI(IloEnvI* env,
			     IloIntIndexI* x,
			     IloIntCollectionI* coll);
  virtual ~IloIntCollectionGeneratorI();
  virtual IloDataIterator* iterator() const;
  virtual IloDiscreteDataCollectionI* getDiscreteDataCollection() const;
  virtual IloBool hasDiscreteDataCollection() const;
  virtual IloBool generatesDuplicates() const;
  ILOEXTROTHERDECL
};

//-------------------------------------------
class IloNumGeneratorI : public IloGeneratorI {
  ILOEXTRDECL
private:
  IloNumIndexI* _x;
protected:
  void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloNumGeneratorI(IloEnvI* env, IloNumIndexI* x);
  virtual ~IloNumGeneratorI();
  IloInt getIndexKey() const;
  IloNumIndexI* getIndex() const { return _x; }
  virtual IloDataIterator* iterator() const;
  virtual IloDiscreteDataCollectionI* getDiscreteDataCollection() const;
};

//-------------------------------------------
class IloNumCollectionGeneratorI : public IloNumGeneratorI {
  ILOEXTRDECL
private:
  IloNumCollectionI* _coll;
  void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloNumCollectionGeneratorI(IloEnvI* env,
			     IloNumIndexI* x,
			     IloNumCollectionI* coll);
  virtual ~IloNumCollectionGeneratorI();
  virtual IloDataIterator* iterator() const;
  virtual IloBool hasDiscreteDataCollection() const;
  virtual IloDiscreteDataCollectionI* getDiscreteDataCollection() const;
  virtual IloBool generatesDuplicates() const;
  ILOEXTROTHERDECL
};

//-------------------------------------------

// TUPLE_EXPR
class IloSymbolGeneratorI : public IloGeneratorI {
  ILOEXTRDECL
private:
  IloSymbolIndexI* _x;
public:
	IloSymbolGeneratorI(IloEnvI* env, IloSymbolIndexI* x);
	virtual ~IloSymbolGeneratorI();
	virtual IloDataIterator* iterator() const;
	virtual IloDiscreteDataCollectionI* getDiscreteDataCollection() const;
	IloInt getIndexKey() const{
		return _x->getKey();
	}
	virtual void visitSubExtractables(IloExtractableVisitor* v);
	IloExtractableI* getIndex() const { 
		return _x;
	}
};

class IloTupleGeneratorI : public IloGeneratorI {
	ILOEXTRDECL
private:
  IloTupleIndexI* _x;
public:
	IloTupleGeneratorI(IloEnvI* env, IloTupleIndexI* x);
	virtual ~IloTupleGeneratorI();
	virtual IloDataIterator* iterator() const;
	virtual IloDiscreteDataCollectionI* getDiscreteDataCollection() const;
	IloInt getIndexKey() const{
		return _x->getKey();
	}
	virtual void visitSubExtractables(IloExtractableVisitor* v);
	IloExtractableI* getIndex() const { 
		return _x;
	}
};


//-------------------------------------------
class IloSymbolCollectionGeneratorI : public IloSymbolGeneratorI {
  ILOEXTRDECL
private:
  IloAnyCollectionI* _coll;
  void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloSymbolCollectionGeneratorI(IloEnvI* env,
				IloSymbolIndexI* x,
				IloAnyCollectionI* coll);
	virtual ~IloSymbolCollectionGeneratorI();
  virtual IloBool generatesDuplicates() const;
  virtual IloDataIterator* iterator() const;
  virtual IloBool hasDiscreteDataCollection() const;
  virtual IloDiscreteDataCollectionI* getDiscreteDataCollection() const;
  ILOEXTROTHERDECL
};

//-------------------------------------------
class IloTupleSetGeneratorI : public IloTupleGeneratorI {
  ILOEXTRDECL
private:
  IloTupleSetI* _coll;
  void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloTupleSetGeneratorI(IloEnvI* env,
			       IloTupleIndexI* x,
			       IloTupleSetI* coll);
	virtual ~IloTupleSetGeneratorI();
  virtual IloDataIterator* iterator() const;
  virtual IloBool generatesDuplicates() const;
  virtual IloBool hasDiscreteDataCollection() const;
  virtual IloDiscreteDataCollectionI* getDiscreteDataCollection() const;
  ILOEXTROTHERDECL
};

class IloTupleSetExprGeneratorI : public IloTupleGeneratorI {
  ILOEXTRDECL
private:
  IloTupleSetExprI* _coll;
  void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloTupleSetExprGeneratorI(IloEnvI* env,
			       IloTupleIndexI* x,
			       IloTupleSetExprI* coll);
	virtual ~IloTupleSetExprGeneratorI();
  IloTupleSetExprI* getCollection() const { return _coll; }
  virtual IloBool generatesDuplicates() const;
  virtual IloBool hasDiscreteDataCollection() const;
  virtual IloDiscreteDataCollectionI* getDiscreteDataCollection() const;
  ILOEXTROTHERDECL
};

/////////////////////////////////////////////////////////////////////////////
//   Comprehension
/////////////////////////////////////////////////////////////////////////////
class IloMapIndexArray;

class IloExtendedComprehensionI : public IloComprehensionI {
  ILOEXTRDECL
private:
  IloExtractableI* _extent;
  enum ExtentType {
    Other,
    IntExpr,
    NumExpr,
    AnyExpr
  };
  ExtentType _extentType;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
  void lockExtentExpr(IloExtractableI* e);
  void releaseExtentExpr(IloExtractableI* e);
public:
  IloExtendedComprehensionI(IloEnvI* env, IloGeneratorArray gen, IloExtractableI* e);
  IloExtendedComprehensionI(IloEnvI* env, IloInt n,IloGeneratorI** gen,IloExtractableI* e);
  virtual ~IloExtendedComprehensionI();
  IloExtractableI* getExtent() const { return _extent; }
  ILOEXTROTHERDECL
};

/////////////////////////////////////////////////////////////////////////////
// For-all
/////////////////////////////////////////////////////////////////////////////



enum IloAggregateOperator {
  IloAggregateSum,
  IloAggregateProd,
  IloAggregateMin,
  IloAggregateMax, 
  IloAggregateOr,
  IloAggregateAnd
};
const char* IloPrintOperator(IloAggregateOperator);

class IloAggregateConstraintI : public IloConstraintI {
  ILOEXTRDECL
private:
  IloComprehensionI*   _comp;
  IloConstraintI*   _ct;
  IloAggregateOperator  _op;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
  virtual IloBool hasNongroundType() const;
public:
  IloAggregateConstraintI(IloEnvI* env, IloComprehensionI* comp, IloConstraintI* ct, IloAggregateOperator op, 
	  const char* name=0);
  virtual ~IloAggregateConstraintI();
  virtual IloInt expandTo(IloModelI* m, const char* propertyName=0, const char* propertyValue=0) const;
  IloComprehensionI* getComprehension() const { return _comp; }
  IloInt getNbGenerators() const { return _comp->getNbGenerators(); }
  IloGeneratorI** getGenerators() const { return _comp->getGenerators(); }
  IloConstraintI* getConstraint() const { return _ct; }
  IloAggregateOperator getOperator() const { return _op; }
  ILOEXTROTHERDECL
	virtual IloBool isAggregate() const{ return IloTrue; }
};


/////////////////////////////////////////////////////////////////////////////
// Aggregation operators
/////////////////////////////////////////////////////////////////////////////

class IloIntAggregateExprI : public IloIntExprI {
  ILOEXTRDECL
private:
  IloComprehensionI* _comp;
  IloAggregateOperator _op;
  IloIntExprI* _expr;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
  virtual IloBool hasNongroundType() const;
public:
  IloIntAggregateExprI(IloEnvI* env,
		       IloComprehensionI* comp,
		       IloAggregateOperator op,
		       IloIntExprI* expr);
  virtual ~IloIntAggregateExprI();
  virtual IloInt expandTo(IloModelI* m, const char* propertyName=0, const char* propertyValue=0) const {
     return IloIntExprI::expandTo(m, propertyName, propertyValue);
  }
  virtual IloInt expandTo(IloNumExprI* e) const;
  IloComprehensionI* getComprehension() const { return _comp; }
  IloInt getNbGenerators() const { return _comp->getNbGenerators(); }
  IloGeneratorI** getGenerators() const { return _comp->getGenerators(); }
  IloAggregateOperator getOperator() const { return _op; }
  IloIntExprI* getExpr() const { return _expr; }
  IloNum eval(const IloAlgorithm alg) const;
  IloInt getDefaultValue() const;
  ILOEXTROTHERDECL
	virtual IloBool isAggregate() const{ return IloTrue; }
};

class IloNumAggregateExprI : public IloNumExprI {
  ILOEXTRDECL
private:
  IloComprehensionI* _comp;
  IloAggregateOperator _op;
  IloNumExprI* _expr;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
  virtual IloBool hasNongroundType() const;
public:
  IloNumAggregateExprI(IloEnvI* env,
		       IloComprehensionI* comp,
		       IloAggregateOperator op,
		       IloNumExprI* expr);
  virtual ~IloNumAggregateExprI();
  virtual IloInt expandTo(IloModelI* m, const char* propertyName=0, const char* propertyValue=0) const {
     return IloNumExprI::expandTo(m, propertyName, propertyValue);
  }
  virtual IloInt expandTo(IloNumExprI* e) const;
  IloComprehensionI* getComprehension() const { return _comp; }
  IloInt getNbGenerators() const { return _comp->getNbGenerators(); }
  IloGeneratorI** getGenerators() const { return _comp->getGenerators(); }
  IloAggregateOperator getOperator() const { return _op; }
  IloNumExprI* getExpr() const { return _expr; }
  IloNum eval(const IloAlgorithm alg) const;
  IloNum getDefaultValue() const;
  ILOEXTROTHERDECL
	virtual IloBool isAggregate() const{ return IloTrue; }
};

/////////////////////////////////////////////////////////////////////////////
// Aggregate piecewise linear function
/////////////////////////////////////////////////////////////////////////////
class IloAggregatePwlExprI : public IloNumExprI {
  ILOEXTRDECL
private:
  IloNumExprI* _node;
  IloComprehensionI* _comp;
  virtual IloBool hasNongroundType() const;
public:
  IloAggregatePwlExprI(IloEnvI* env,
		       IloNumExprI* node, IloComprehensionI* comp);
  virtual ~IloAggregatePwlExprI();
  virtual IloInt expandTo(IloModelI* m, const char* propertyName=0, const char* propertyValue=0) const {
     return IloNumExprI::expandTo(m, propertyName, propertyValue);
  }
  virtual IloInt expandTo(IloNumExprI* e) const = 0;
  IloNumExprI* getNode() const { return _node; }
  IloComprehensionI* getComprehension() const { return _comp; }
  IloInt getNbGenerators() const { return _comp->getNbGenerators(); }
  IloGeneratorI** getGenerators() const { return _comp->getGenerators(); }
	virtual IloBool isAggregate() const{ return IloTrue; }
};

class IloAggregateSlopesPwlExprI : public IloAggregatePwlExprI {
  ILOEXTRDECL
private:
  IloNumExprI* _point;
  IloNumExprI* _slope;
  IloNumExprI* _lastSlope;
  IloNumExprI* _x;
  IloNumExprI* _y;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloAggregateSlopesPwlExprI(IloEnvI* env,
			     IloNumExprI* node,
			     IloComprehensionI* comp,
			     IloNumExprI* point,
			     IloNumExprI* slope,
			     IloNumExprI* lastSlope,
			     IloNumExprI* x,
			     IloNumExprI* y);
  virtual ~IloAggregateSlopesPwlExprI();
  virtual IloInt expandTo(IloModelI* m, const char* propertyName=0, const char* propertyValue=0) const {
     return IloAggregatePwlExprI::expandTo(m, propertyName, propertyValue);
  }
  virtual IloInt expandTo(IloNumExprI* e) const;
  IloNumExprI* getPoint() const { return _point; }
  IloNumExprI* getSlope() const { return _slope; }
  IloNumExprI* getLastSlope() const { return _lastSlope; }
  IloNumExprI* getX() const { return _x; }
  IloNumExprI* getY() const { return _y; }
  IloNum eval(const IloAlgorithm alg) const;
  ILOEXTROTHERDECL
};

class IloAggregatePointsPwlExprI : public IloAggregatePwlExprI {
  ILOEXTRDECL
private:
  IloNumExprI* _x;
  IloNumExprI* _y;
  IloNumExprI* _firstSlope;
  IloNumExprI* _lastSlope;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloAggregatePointsPwlExprI(IloEnvI* env,
			     IloNumExprI* node,
			     IloNumExprI* firstSlope,
			     IloComprehensionI* comp,
			     IloNumExprI* x, IloNumExprI* y,
			     IloNumExprI* lastSlope);
  virtual ~IloAggregatePointsPwlExprI();
  virtual IloInt expandTo(IloModelI* m, const char* propertyName=0, const char* propertyValue=0) const {
     return IloAggregatePwlExprI::expandTo(m, propertyName, propertyValue);
  }
  virtual IloInt expandTo(IloNumExprI* e) const;
  IloNumExprI* getX() const { return _x; }
  IloNumExprI* getY() const { return _y; }
  IloNumExprI* getFirstSlope() const { return _firstSlope; }
  IloNumExprI* getLastSlope()  const { return _lastSlope; }
  IloNum eval(const IloAlgorithm alg) const;
  ILOEXTROTHERDECL
};



//-------------------------------------------------------------
class IloSlackExprI : public IloNumExprI {
  ILOEXTRDECL
  IloConstraintI* _arg;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloSlackExprI(IloEnvI* env, IloConstraintI* ct);
  virtual ~IloSlackExprI();
  IloConstraintI* getConstraint() const { return _arg; }
  void display(ILOSTD(ostream)& out) const;
  virtual IloNum eval(const IloAlgorithm alg) const;
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual IloBool isInteger() const;
};

class IloDualExprI : public IloNumExprI {
  ILOEXTRDECL
  IloConstraintI* _arg;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloDualExprI(IloEnvI* env, IloConstraintI* ct);
  virtual ~IloDualExprI();
  IloConstraintI* getConstraint() const { return _arg; }
  void display(ILOSTD(ostream)& out) const;
  virtual IloNum eval(const IloAlgorithm alg) const;
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual IloBool isInteger() const;
};

class IloReducedCostExprI : public IloNumExprI {
  ILOEXTRDECL
  IloNumVarI* _arg;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloReducedCostExprI(IloEnvI* env, IloNumVarI* ct);
  virtual ~IloReducedCostExprI();
  IloNumVarI* getVar() const { return _arg; }
  void display(ILOSTD(ostream)& out) const;
  virtual IloNum eval(const IloAlgorithm alg) const;
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual IloBool isInteger() const;
};

class IloAdvModelEvaluatorI;
class IloComprehensionBuilder{
public:
	static void build(IloIntSetI* source, IloExtendedComprehensionI* comp, IloAdvModelEvaluatorI* eval=0);
	static void build(IloNumSetI* source, IloExtendedComprehensionI* comp, IloAdvModelEvaluatorI* eval=0);
	static void build(IloAnySetI* source, IloExtendedComprehensionI* comp, IloAdvModelEvaluatorI* eval=0);
	static void build(IloTupleSetI* source, IloExtendedComprehensionI* comp, IloAdvModelEvaluatorI* eval=0);
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
