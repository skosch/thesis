// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/iloord.h
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


#ifndef __ADVANCED_iloordH
#define __ADVANCED_iloordH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>
#include <ilopl/iloexpressioni.h>
#include <ilconcert/iloanyexpri.h>


class IloIntSet;
class IloNumSet;
class IloSymbolSet;
class IloTupleSet;
class IloTuple;

class IloIntSetI;
class IloNumSetI;
class IloAnySetI;
class IloTupleSetI;

class IloSymbolExprArg;
class IloNumExprArg;
class IloSymbolExprArg;
class IloTupleExprArg;

class IloIntExprArg;


IloConstraint IloOrdered(IloNumSet set, IloNumExprArg base, IloNumExprArg base2);


IloConstraint IloOrdered(IloIntSet set, IloIntExprArg base, IloIntExprArg base2);


class IloTupleI;
class IloOrdValueNotFoundException : public IloException {
	IloInt _intValue;
  IloNum _numValue;
  IloSymbolI* _symbolValue;
  IloTupleI* _tupleValue;
  IloInt _type;
public:
	IloOrdValueNotFoundException(IloInt elt);
  IloOrdValueNotFoundException(IloNum elt);
  IloOrdValueNotFoundException(IloSymbolI* elt);
  IloOrdValueNotFoundException(IloTupleI* elt);
	virtual void print(ILOSTD(ostream)& out) const;
	virtual const char* getMessage() const;
  IloBool isInt() const {return _type == 0;}
  IloInt getInt() const {return _intValue;}
  IloBool isNum() const {return _type == 1;}
  IloNum getNum() const {return _numValue;}
  IloBool isSymbol() const {return _type == 2;}
  IloSymbol getSymbol() const;
  IloBool isTuple() const {return _type == 3;}
  IloTuple getTuple() const;
};


IloInt IloOrd(const IloIntSet x, IloInt y);


IloInt IloOrd(const IloNumSet x, IloNum y);


IloInt IloOrd(const IloSymbolSet x, IloSymbol y);


IloInt IloOrd(const IloTupleSet x, IloTuple y);


IloConstraint IloOrdered(IloTupleSet set, IloTupleExprArg base, IloTupleExprArg base2);



IloConstraint IloOrdered(IloSymbolSet set, const IloSymbolExprArg expr1, const IloSymbolExprArg expr2);


#ifdef _WIN32
#pragma pack(pop)
#endif


#endif
