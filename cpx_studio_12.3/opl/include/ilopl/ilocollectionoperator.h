// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilocollectionoperator.h
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

#ifndef __ADVANCED_ilocollectionoperatorH
#define __ADVANCED_ilocollectionoperatorH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>
#include <ilconcert/ilocollectionoperator.h>
#include <ilopl/ilotuplecollection.h>
#include <ilopl/ilosymbol.h>
#include <ilconcert/iloanyexpri.h>
#include <ilconcert/iloset.h>

class IloIntExprArg;
class IloNumExprArg;
class IloTupleExprArg;
class IloIntSetI;
class IloNumSetI;



IloBool operator == (const IloDiscreteDataCollection, const IloDiscreteDataCollection);
IloBool operator == (const IloAnyCollection, const IloAnyCollection);


class IloAdvIntSet : public IloIntSet {
public:
	IloAdvIntSet(const IloEnv env, IloDataCollection::SortSense sense);
	IloAdvIntSet(const IloEnv env, IloDataCollection::SortSense sense, IloIntArray array);
};
class IloAdvNumSet : public IloNumSet {
public:
	IloAdvNumSet(const IloEnv env, IloDataCollection::SortSense sense);
	IloAdvNumSet(const IloEnv env, IloDataCollection::SortSense sense, IloNumArray array);
};


class IloAdvCollectionHelper : public IloCollectionHelper {
public:
	static IloDiscreteDataCollectionI* newSet(IloDiscreteDataCollectionI* coll, IloDataCollection::SortSense sense);

	
	static IloSymbolSet asSymbolSet(IloDiscreteDataCollection);

	
	static IloTupleSet asTupleSet(IloDiscreteDataCollection);
};



IloSymbolSet IloUnion(IloSymbolSet set1, IloSymbolSet set2);

IloIntSetI* IloSortedAscUnion(IloIntSetI* set1, IloIntSetI* set2);
IloIntSetI* IloSortedDescUnion(IloIntSetI* set1, IloIntSetI* set2);

IloNumSetI* IloSortedAscUnion(IloNumSetI* set1, IloNumSetI* set2);
IloNumSetI* IloSortedDescUnion(IloNumSetI* set1, IloNumSetI* set2);

IloIntSetI* IloUnion(IloIntSetI* set1, IloIntSetI* set2, IloDataCollection::SortSense sense );
IloNumSetI* IloUnion(IloNumSetI* set1, IloNumSetI* set2, IloDataCollection::SortSense sense );

IloTupleSetI* IloSortedAscUnion(IloTupleSetI* set1, IloTupleSetI* set2);
IloTupleSetI* IloSortedDescUnion(IloTupleSetI* set1, IloTupleSetI* set2);
IloTupleSetI* IloUnion(IloTupleSetI* set1, IloTupleSetI* set2, IloDataCollection::SortSense sense );

IloIntSetI* IloSortedAscInter(IloIntSetI* set1, IloIntSetI* set2);
IloIntSetI* IloSortedDescInter(IloIntSetI* set1, IloIntSetI* set2);
IloNumSetI* IloSortedAscInter(IloNumSetI* set1, IloNumSetI* set2);
IloNumSetI* IloSortedDescInter(IloNumSetI* set1, IloNumSetI* set2);
IloIntSetI* IloInter(IloIntSetI* set1, IloIntSetI* set2, IloDataCollection::SortSense sense );
IloNumSetI* IloInter(IloNumSetI* set1, IloNumSetI* set2, IloDataCollection::SortSense sense );


IloIntSetI* IloSortedAscExclude(IloIntSetI* set1, IloIntSetI* set2);
IloIntSetI* IloSortedDescExclude(IloIntSetI* set1, IloIntSetI* set2);
IloNumSetI* IloSortedAscExclude(IloNumSetI* set1, IloNumSetI* set2);
IloNumSetI* IloSortedDescExclude(IloNumSetI* set1, IloNumSetI* set2);
IloIntSetI* IloExclude(IloIntSetI* set1, IloIntSetI* set2, IloDataCollection::SortSense sense);
IloNumSetI* IloExclude(IloNumSetI* set1, IloNumSetI* set2, IloDataCollection::SortSense sense);



IloIntSetI* IloSortedAscSymExclude(IloIntSetI* set1, IloIntSetI* set2);
IloIntSetI* IloSortedDescSymExclude(IloIntSetI* set1, IloIntSetI* set2);
IloNumSetI* IloSortedAscSymExclude(IloNumSetI* set1, IloNumSetI* set2);
IloNumSetI* IloSortedDescSymExclude(IloNumSetI* set1, IloNumSetI* set2);
IloIntSetI* IloSymExclude(IloIntSetI* set1, IloIntSetI* set2, IloDataCollection::SortSense sense);
IloNumSetI* IloSymExclude(IloNumSetI* set1, IloNumSetI* set2, IloDataCollection::SortSense sense);

IloSymbolSetI* IloUnion(IloSymbolSetI* set1, IloSymbolSetI* set2, IloDataCollection::SortSense sense);
IloSymbolSetI* IloSortedAscUnion(IloSymbolSetI* set1, IloSymbolSetI* set2);
IloSymbolSetI* IloSortedDescUnion(IloSymbolSetI* set1, IloSymbolSetI* set2);
IloSymbolSetI* IloSortedAscInter(IloSymbolSetI* set1, IloSymbolSetI* set2);
IloSymbolSetI* IloSortedDescInter(IloSymbolSetI* set1, IloSymbolSetI* set2);
IloSymbolSetI* IloInter(IloSymbolSetI* set1, IloSymbolSetI* set2, IloDataCollection::SortSense sense);
IloSymbolSetI* IloSortedAscExclude(IloSymbolSetI* set1, IloSymbolSetI* set2);
IloSymbolSetI* IloSortedDescExclude(IloSymbolSetI* set1, IloSymbolSetI* set2);
IloSymbolSetI* IloExclude(IloSymbolSetI* set1, IloSymbolSetI* set2, IloDataCollection::SortSense sense);
IloSymbolSetI* IloSortedAscSymExclude(IloSymbolSetI* set1, IloSymbolSetI* set2);
IloSymbolSetI* IloSortedDescSymExclude(IloSymbolSetI* set1, IloSymbolSetI* set2);
IloSymbolSetI* IloSymExclude(IloSymbolSetI* set1, IloSymbolSetI* set2, IloDataCollection::SortSense sense);



IloTupleSetI* IloSortedAscInter(IloTupleSetI* set1, IloTupleSetI* set2);
IloTupleSetI* IloSortedDescInter(IloTupleSetI* set1, IloTupleSetI* set2);
IloTupleSetI* IloInter(IloTupleSetI* set1, IloTupleSetI* set2, IloDataCollection::SortSense sense);
IloTupleSetI* IloSortedAscExclude(IloTupleSetI* set1, IloTupleSetI* set2);
IloTupleSetI* IloSortedDescExclude(IloTupleSetI* set1, IloTupleSetI* set2);
IloTupleSetI* IloExclude(IloTupleSetI* set1, IloTupleSetI* set2, IloDataCollection::SortSense sense);
IloTupleSetI* IloSortedAscSymExclude(IloTupleSetI* set1, IloTupleSetI* set2);
IloTupleSetI* IloSortedDescSymExclude(IloTupleSetI* set1, IloTupleSetI* set2);
IloTupleSetI* IloSymExclude(IloTupleSetI* set1, IloTupleSetI* set2, IloDataCollection::SortSense sense);

IloTupleSetI* IloSortedAscInter(IloTupleSetI* set1, IloTupleSetI* set2, IloIntArray idx, IloArray<IloDiscreteDataCollectionI*> references);
IloTupleSetI* IloSortedAscUnion(IloTupleSetI* set1, IloTupleSetI* set2, IloIntArray idx, IloArray<IloDiscreteDataCollectionI*> references);
IloTupleSetI* IloSortedAscExclude(IloTupleSetI* set1, IloTupleSetI* set2, IloIntArray idx, IloArray<IloDiscreteDataCollectionI*> references);
IloTupleSetI* IloSortedAscSymExclude(IloTupleSetI* set1, IloTupleSetI* set2, IloIntArray idx, IloArray<IloDiscreteDataCollectionI*> references);

IloTupleSetI* IloSortedDescInter(IloTupleSetI* set1, IloTupleSetI* set2, IloIntArray idx, IloArray<IloDiscreteDataCollectionI*> references);
IloTupleSetI* IloSortedDescUnion(IloTupleSetI* set1, IloTupleSetI* set2, IloIntArray idx, IloArray<IloDiscreteDataCollectionI*> references);
IloTupleSetI* IloSortedDescExclude(IloTupleSetI* set1, IloTupleSetI* set2, IloIntArray idx, IloArray<IloDiscreteDataCollectionI*> references);
IloTupleSetI* IloSortedDescSymExclude(IloTupleSetI* set1, IloTupleSetI* set2, IloIntArray idx, IloArray<IloDiscreteDataCollectionI*> references);

IloTupleSetI* IloSymExclude(IloTupleSetI* set1, IloTupleSetI* set2, IloIntArray idx, IloArray<IloDiscreteDataCollectionI*> references, IloDataCollection::SortSense sense);
IloTupleSetI* IloExclude(IloTupleSetI* set1, IloTupleSetI* set2, IloIntArray idx, IloArray<IloDiscreteDataCollectionI*> references, IloDataCollection::SortSense sense);
IloTupleSetI* IloInter(IloTupleSetI* set1, IloTupleSetI* set2, IloIntArray idx, IloArray<IloDiscreteDataCollectionI*> references, IloDataCollection::SortSense sense);
IloTupleSetI* IloUnion(IloTupleSetI* set1, IloTupleSetI* set2, IloIntArray idx, IloArray<IloDiscreteDataCollectionI*> references, IloDataCollection::SortSense sense);


IloTupleSet IloUnion(IloTupleSet set1, IloTupleSet set2);


IloSymbol IloFirst(IloSymbolSet set);


IloTuple IloFirst(IloTupleSet set);


IloSymbol IloLast(IloSymbolSet set);



IloTuple IloLast(IloTupleSet set);


IloSymbolSet IloExclude(IloSymbolSet set1, IloSymbolSet set2);


IloTupleSet IloExclude(IloTupleSet set1, IloTupleSet set2);


IloSymbolSet IloSymExclude(IloSymbolSet set1, IloSymbolSet set2);


IloTupleSet IloSymExclude(IloTupleSet set1, IloTupleSet set2);


IloSymbolSet IloInter(IloSymbolSet set1, IloSymbolSet set2);


IloTupleSet IloInter(IloTupleSet set1, IloTupleSet set2);

IloTupleSet IloInter(IloTupleSet set1, IloTupleSet set2, IloIntArray idx, IloArray<IloDiscreteDataCollectionI*> references);
IloTupleSet IloUnion(IloTupleSet set1, IloTupleSet set2, IloIntArray idx, IloArray<IloDiscreteDataCollectionI*> references);
IloTupleSet IloExclude(IloTupleSet set1, IloTupleSet set2, IloIntArray idx, IloArray<IloDiscreteDataCollectionI*> references);
IloTupleSet IloSymExclude(IloTupleSet set1, IloTupleSet set2, IloIntArray idx, IloArray<IloDiscreteDataCollectionI*> references);



IloInt IloNext(IloIntSet set, IloInt value, IloInt n=1);


IloNum IloNext(IloNumSet set, IloNum value, IloInt n=1);


IloAny IloNext(IloAnySet set, IloAny value, IloInt n=1);


IloTuple IloNext(IloTupleSet set, IloTuple value, IloInt n=1);


IloSymbol IloNext(IloSymbolSet set, IloSymbol value, IloInt n=1);



//=================================================


IloInt IloPrevious(IloIntSet set, IloInt value, IloInt n=1);


IloNum IloPrevious(IloNumSet set, IloNum value, IloInt n=1);


IloAny IloPrevious(IloAnySet set, IloAny value, IloInt n=1);


IloTuple IloPrevious(IloTupleSet set, IloTuple value, IloInt n=1);

IloSymbol IloPrevious(IloSymbolSet set, IloSymbol value, IloInt n=1);


IloInt IloNextC(IloIntSet set, IloInt value, IloInt n=1);


IloNum IloNextC(IloNumSet set, IloNum value, IloInt n=1);


IloAny IloNextC(IloAnySet set, IloAny value, IloInt n=1);


IloTuple IloNextC(IloTupleSet set, IloTuple value, IloInt n=1);


IloSymbol IloNextC(IloSymbolSet set, IloSymbol value, IloInt n=1);

//
//IloInt IloPreviousC(IloIntSet set, IloInt value, IloInt n=1);

//
//IloNum IloPreviousC(IloNumSet set, IloNum value, IloInt n=1);

//
//IloAny IloPreviousC(IloAnySet set, IloAny value, IloInt n=1);


IloTuple IloPreviousC(IloTupleSet set, IloTuple value, IloInt n=1);


IloSymbol IloPreviousC(IloSymbolSet set, IloSymbol value, IloInt n=1);


//====================================================


class IloIntCollectionExprArg;
class IloNumCollectionExprArg;
class IloSymbolCollectionExprArg;

IloIntExprArg IloNext(IloIntCollectionExprArg set, IloIntExprArg value);
IloIntExprArg IloNext(IloIntCollectionExprArg set, IloIntExprArg value, IloIntExprArg n);
IloIntExprArg IloPrevious(IloIntCollectionExprArg set, IloIntExprArg value);
IloIntExprArg IloPrevious(IloIntCollectionExprArg set, IloIntExprArg value, IloIntExprArg n);
IloIntExprArg IloNextC(IloIntCollectionExprArg set, IloIntExprArg value);
IloIntExprArg IloNextC(IloIntCollectionExprArg set, IloIntExprArg value, IloIntExprArg n);
IloIntExprArg IloPreviousC(IloIntCollectionExprArg set, IloIntExprArg value);
IloIntExprArg IloPreviousC(IloIntCollectionExprArg set, IloIntExprArg value, IloIntExprArg n);


IloNumExprArg IloNext(IloNumCollectionExprArg set, IloNumExprArg value);
IloNumExprArg IloNext(IloNumCollectionExprArg set, IloNumExprArg value, IloIntExprArg n);
IloNumExprArg IloPrevious(IloNumCollectionExprArg set, IloNumExprArg value);
IloNumExprArg IloPrevious(IloNumCollectionExprArg set, IloNumExprArg value, IloIntExprArg n);
IloNumExprArg IloNextC(IloNumCollectionExprArg set, IloNumExprArg value);
IloNumExprArg IloNextC(IloNumCollectionExprArg set, IloNumExprArg value, IloIntExprArg n);
IloNumExprArg IloPreviousC(IloNumCollectionExprArg set, IloNumExprArg value);
IloNumExprArg IloPreviousC(IloNumCollectionExprArg set, IloNumExprArg value, IloIntExprArg n);

IloSymbolExprArg IloNext(IloSymbolCollectionExprArg set, IloSymbolExprArg value);
IloSymbolExprArg IloNext(IloSymbolCollectionExprArg set, IloSymbolExprArg value, IloIntExprArg n);
IloSymbolExprArg IloPrevious(IloSymbolCollectionExprArg set, IloSymbolExprArg value);
IloSymbolExprArg IloPrevious(IloSymbolCollectionExprArg set, IloSymbolExprArg value, IloIntExprArg n);
IloSymbolExprArg IloNextC(IloSymbolCollectionExprArg set, IloSymbolExprArg value);
IloSymbolExprArg IloNextC(IloSymbolCollectionExprArg set, IloSymbolExprArg value, IloIntExprArg n);
IloSymbolExprArg IloPreviousC(IloSymbolCollectionExprArg set, IloSymbolExprArg value);
IloSymbolExprArg IloPreviousC(IloSymbolCollectionExprArg set, IloSymbolExprArg value, IloIntExprArg n);



class IloNumCollectionExprOperatorI : public IloNumExprI {
	ILOEXTRDECL
private:
	IloNumCollectionExprI* _coll;
	IloNumExprI* _expr;
	IloIntExprI* _offset;
	IloBool _circ;
	IloBool _sense;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloNumCollectionExprI* getNumCollectionExpr() const { return _coll; }
	IloNumExprI* getNumExpr() const { return _expr; }
	IloIntExprI* getOffSet() const { return _offset; }
	IloBool isCircular() const { return _circ; }
	IloBool isNext() const { return _sense; }
	IloNumCollectionExprOperatorI(IloEnvI* env,
		IloNumCollectionExprI* coll,
		IloNumExprI* index,
		IloIntExprI* offset = 0,
		IloBool sense = IloTrue,
		IloBool circ = IloTrue);
	virtual ~IloNumCollectionExprOperatorI();
	void display(ILOSTD(ostream)& out) const;
	virtual IloNum eval(const IloAlgorithm alg) const;
	virtual IloExtractableI* makeClone(IloEnvI*) const;
	virtual IloBool isInteger() const;
};



class IloIntCollectionExprOperatorI : public IloIntExprI {
	ILOEXTRDECL
private:
	IloIntCollectionExprI* _coll;
	IloIntExprI* _expr;
	IloIntExprI* _offset;
	IloBool _circ;
	IloBool _sense;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloIntCollectionExprI* getIntCollectionExpr() const { return _coll; }
	IloIntExprI* getIntExpr() const { return _expr; }
	IloIntExprI* getOffSet() const { return _offset; }
	IloBool isCircular() const { return _circ; }
	IloBool isNext() const { return _sense; }
	IloIntCollectionExprOperatorI(IloEnvI* env,
		IloIntCollectionExprI* coll,
		IloIntExprI* index,
		IloIntExprI* offset = 0,
		IloBool sense = IloTrue,
		IloBool circ = IloTrue);
	virtual ~IloIntCollectionExprOperatorI();
	void display(ILOSTD(ostream)& out) const;
	virtual IloNum eval(const IloAlgorithm alg) const;
	virtual IloExtractableI* makeClone(IloEnvI*) const;
};



class IloSymbolCollectionExprOperatorI : public IloSymbolExprI {
	ILOEXTRDECL
private:
	IloSymbolCollectionExprI* _coll;
	IloSymbolExprI* _expr;
	IloIntExprArgI* _offset;
	IloBool _circ;
	IloBool _sense;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloSymbolCollectionExprI* getSymbolCollectionExpr() const { return _coll; }
	IloSymbolExprI* getSymbolExpr() const { return _expr; }
	IloIntExprI* getOffSet() const { return _offset; }
	IloBool isCircular() const { return _circ; }
	IloBool isNext() const { return _sense; }
	IloSymbolCollectionExprOperatorI(IloEnvI* env,
		IloSymbolCollectionExprI* coll,
		IloSymbolExprI* index,
		IloIntExprI* offset = 0,
		IloBool sense = IloTrue,
		IloBool circ = IloTrue);
	virtual ~IloSymbolCollectionExprOperatorI();
	void display(ILOSTD(ostream)& out) const;
	virtual IloNum eval(const IloAlgorithm alg) const;
	virtual IloExtractableI* makeClone(IloEnvI*) const;
};

//============================


class IloNumCollectionExprItemI : public IloNumExprI {
	ILOEXTRDECL
private:
	IloNumCollectionExprI* _coll;
	IloIntExprI* _offset;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloNumCollectionExprI* getNumCollectionExpr() const { return _coll; }
	IloIntExprI* getOffSet() const { return _offset; }
	IloNumCollectionExprItemI(IloEnvI* env,
		IloNumCollectionExprI* coll,
		IloIntExprI* offset = 0);
	virtual ~IloNumCollectionExprItemI();
	void display(ILOSTD(ostream)& out) const;
	virtual IloNum eval(const IloAlgorithm alg) const;
	virtual IloExtractableI* makeClone(IloEnvI*) const;
	virtual IloBool isInteger() const;
};



class IloIntCollectionExprItemI : public IloIntExprI {
	ILOEXTRDECL
private:
	IloIntCollectionExprI* _coll;
	IloIntExprI* _offset;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloIntCollectionExprI* getIntCollectionExpr() const { return _coll; }
	IloIntExprI* getOffSet() const { return _offset; }
	IloIntCollectionExprItemI(IloEnvI* env,
		IloIntCollectionExprI* coll,
		IloIntExprI* offset = 0);
	virtual ~IloIntCollectionExprItemI();
	void display(ILOSTD(ostream)& out) const;
	virtual IloNum eval(const IloAlgorithm alg) const;
	virtual IloExtractableI* makeClone(IloEnvI*) const;
};



class IloSymbolCollectionExprItemI : public IloSymbolExprI {
	ILOEXTRDECL
private:
	IloSymbolCollectionExprI* _coll;
	IloIntExprArgI* _offset;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloSymbolCollectionExprI* getSymbolCollectionExpr() const { return _coll; }
	IloIntExprI* getOffSet() const { return _offset; }
	IloSymbolCollectionExprItemI(IloEnvI* env,
		IloSymbolCollectionExprI* coll,
		IloIntExprI* offset = 0);
	virtual ~IloSymbolCollectionExprItemI();
	void display(ILOSTD(ostream)& out) const;
	virtual IloNum eval(const IloAlgorithm alg) const;
	virtual IloExtractableI* makeClone(IloEnvI*) const;
};


IloIntExprArg IloItem(IloIntCollectionExprArg set, IloIntExprArg n);
IloNumExprArg IloItem(IloNumCollectionExprArg set, IloIntExprArg n);
IloSymbolExprArg IloItem(IloSymbolCollectionExprArg set, IloIntExprArg n);




IloConstraint IloSubset(IloEnv env, IloIntCollectionExprArg slice, IloIntCollectionExprArg set);
IloConstraint IloSubset(IloEnv env, IloNumCollectionExprArg slice, IloNumCollectionExprArg set);
IloConstraint IloSubset(IloEnv env, IloSymbolCollectionExprArg slice, IloSymbolCollectionExprArg set);

IloConstraint IloSubsetEq(IloEnv env, IloIntCollectionExprArg slice, IloIntCollectionExprArg set);
IloConstraint IloSubsetEq(IloEnv env, IloNumCollectionExprArg slice, IloNumCollectionExprArg set);
IloConstraint IloSubsetEq(IloEnv env, IloSymbolCollectionExprArg slice, IloSymbolCollectionExprArg set);


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
