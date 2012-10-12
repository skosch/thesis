// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilomember.h
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

#ifndef __ADVANCED_ilomemberH
#define __ADVANCED_ilomemberH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>
#include <ilopl/iloenv.h>
#include <ilconcert/ilocollection.h>
#include <ilopl/ilotuplecollection.h>
#include <ilopl/ilotuple.h>

/////////////////
//
//  Constraints
//
class IloSymbolExprArg;
class IloTupleExprArg;
class IloTupleSet;
class IloAnyExprI;


IloConstraint IloMember(const IloEnv env, const IloTupleExprArg exp, const IloTupleSetExprArg s);


IloConstraint IloMember(const IloEnv env, const IloSymbolExprArg expr, const IloSymbolArray elements);


IloConstraint IloNotMember(const IloEnv env, const IloTupleExprArg exp, const IloTupleSetExprArg s);


IloConstraint IloNotMember(const IloEnv env, const IloIntExprArg expr, const IloIntArray elements);


IloConstraint IloNotMember(const IloEnv env, const IloNumExprArg expr, const IloNumArray elements);


IloConstraint IloNotMember(const IloEnv env, const IloSymbolExprArg expr, const IloSymbolArray elements);



class IloTupleSetExprMemberI : public IloConstraintI {
	ILOEXTRDECL
private:
	IloTupleExprArgI* _exp;
	IloTupleSetExprArgI* _coll;
public:
	IloTupleSetExprMemberI(IloEnvI* env, IloTupleExprArgI* exp, IloTupleSetExprArgI* coll);
    virtual ~IloTupleSetExprMemberI();
	void visitSubExtractables(IloExtractableVisitor* v);
	IloTupleExprArgI* getTupleExpr() const { return _exp; }
	IloTupleSetExprArgI* getTupleSetExpr() const { return _coll; }
	ILOEXTROTHERDECL
};



class IloAnyArrayMemberI : public IloConstraintI {
	ILOEXTRMEMBERS2DECL(IloAnyArrayMemberI,IloConstraintI,
		IloAnyExprI*, _expr,
		IloAnyArray, _elements)
		virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	virtual ~IloAnyArrayMemberI();
	IloAnyExprI* getExpr() const {return _expr;}
	IloAnyArray getElements() const {return _elements;}
};

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif


