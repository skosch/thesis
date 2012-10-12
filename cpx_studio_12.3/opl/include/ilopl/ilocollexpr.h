// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilocollexpr.h
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

#ifndef __ADVANCED_ilocollexprH
#define __ADVANCED_ilocollexprH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>
#include <ilopl/ilomapi.h>

class IloGenerator;

class IloIntCollectionExprGenerator;
class IloNumCollectionExprGenerator;
class IloSymbolCollectionExprGenerator;

//-----------------------------------------------------------------

class IloIntCollectionExprGenerator : public IloGenerator {
public:
	
	IloIntCollectionExprGenerator();
	
	IloIntCollectionExprGenerator(IloIntCollectionExprGeneratorI* impl);
	
	IloIntCollectionExprGeneratorI* getImpl() const;
	
	IloIntIndex getIndex() const;
};

//--------------------------


class IloNumCollectionExprGenerator : public IloGenerator {
public:
	
	IloNumCollectionExprGenerator();
	
	IloNumCollectionExprGenerator(IloNumCollectionExprGeneratorI* impl);
	
	IloNumCollectionExprGeneratorI* getImpl() const;
	
	IloNumIndex getIndex() const;
};


class IloSymbolCollectionExprGenerator : public IloGenerator {
public:
	
	IloSymbolCollectionExprGenerator();
	
	IloSymbolCollectionExprGenerator(IloSymbolCollectionExprGeneratorI* impl);
	
	IloSymbolCollectionExprGeneratorI* getImpl() const;
	
	IloSymbolIndex getIndex() const;
};



IloConstraint IloMember(const IloEnv env,
						const IloSymbolExprArg exp,
						const IloSymbolCollectionExprArg arg);


IloConstraint IloMember(const IloEnv env,
						const IloNumExprArg exp,
						const IloNumCollectionExprArg arg);


IloConstraint IloMember(const IloEnv env,
						const IloIntExprArg exp,
						const IloIntCollectionExprArg arg);


IloConstraint IloNotMember(const IloEnv env,
						   const IloSymbolExprArg exp,
						   const IloSymbolCollectionExprArg arg);


IloConstraint IloNotMember(const IloEnv env,
						   const IloNumExprArg exp,
						   const IloNumCollectionExprArg arg);


IloConstraint IloNotMember(const IloEnv env,
						   const IloIntExprArg exp,
						   const IloIntCollectionExprArg arg);



IloIntExprArg IloCard(const IloIntCollectionExprArg e);


IloIntExprArg IloCard(const IloSymbolCollectionExprArg e);


IloIntExprArg IloCard(const IloNumCollectionExprArg e);



IloIntExprArg IloMin(const IloIntCollectionExprArg e);


IloNumExprArg IloMin(const IloNumCollectionExprArg e);


IloNumExprArg IloMax(const IloNumCollectionExprArg y);


IloIntExprArg IloMax(const IloIntCollectionExprArg y);






IloIntExprArg IloOrd (const IloIntCollectionExprArg coll, const IloIntExprArg x);

IloIntExprArg IloOrd (const IloNumCollectionExprArg coll, const IloNumExprArg y);

IloIntExprArg IloOrd (const IloSymbolCollectionExprArg coll, const IloSymbolExprArg y);



IloConstraint IloOrdered(const IloIntCollectionExprArg coll,
						 const IloIntExprArg exp1,
						 const IloIntExprArg exp2);


IloConstraint IloOrdered(const IloNumCollectionExprArg map,
						 const IloNumExprArg exp1,
						 const IloNumExprArg exp2);



IloConstraint IloOrdered(const IloSymbolCollectionExprArg map,
						 const IloSymbolExprArg exp1,
						 const IloSymbolExprArg exp2);


///////


IloIntCollectionExprArg IloUnion(const IloIntCollectionExprArg expr1, const IloIntCollectionExprArg expr2);


IloSymbolCollectionExprArg IloUnion(const IloSymbolCollectionExprArg expr1, const IloSymbolCollectionExprArg expr2);

//---


IloNumCollectionExprArg IloUnion(const IloNumCollectionExprArg expr1, const IloNumCollectionExprArg expr2);
//---



IloIntCollectionExprArg IloSymExclude(const IloIntCollectionExprArg expr1, const IloIntCollectionExprArg expr2);
//------------


IloSymbolCollectionExprArg IloSymExclude(const IloSymbolCollectionExprArg expr1, const IloSymbolCollectionExprArg expr2);


IloNumCollectionExprArg IloSymExclude(const IloNumCollectionExprArg expr1, const IloNumCollectionExprArg expr2);


IloIntCollectionExprArg IloExclude(const IloIntCollectionExprArg expr1, const IloIntCollectionExprArg expr2);


IloSymbolCollectionExprArg IloExclude(const IloSymbolCollectionExprArg expr1, const IloSymbolCollectionExprArg expr2);


IloNumCollectionExprArg IloExclude(const IloNumCollectionExprArg expr1, const IloNumCollectionExprArg expr2);


IloIntCollectionExprArg IloInter(const IloIntCollectionExprArg expr1, const IloIntCollectionExprArg expr2);


IloSymbolCollectionExprArg IloInter(const IloSymbolCollectionExprArg expr1, const IloSymbolCollectionExprArg expr2);


IloNumCollectionExprArg IloInter(const IloNumCollectionExprArg expr1, const IloNumCollectionExprArg expr2);
//

IloIntExprArg IloFirst(IloIntCollectionExprArg coll);

IloIntExprArg IloLast(IloIntCollectionExprArg coll);


IloNumExprArg IloFirst(IloNumCollectionExprArg coll);

IloNumExprArg IloLast(IloNumCollectionExprArg coll);


IloSymbolExprArg IloFirst(IloSymbolCollectionExprArg coll);

IloSymbolExprArg IloLast(IloSymbolCollectionExprArg coll);


IloTupleExprArg IloLast(IloTupleSetExprArg coll);

IloTupleExprArg IloLast(IloTupleSetExprArg coll);


////////////////////////////////////////////////////////
//ILOSUBMAPHANDLE(IloAnyCollection, IloAnyCollectionExprArg)


ILOSUBMAPHANDLE(IloSymbolCollection, IloSymbolCollectionExprArg)


ILOSUBMAPHANDLE(IloTupleSet, IloTupleSetExprArg)
ILOSUBMAPHANDLE(IloIntCollection, IloIntCollectionExprArg)
ILOSUBMAPHANDLE(IloNumCollection, IloNumCollectionExprArg)

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
