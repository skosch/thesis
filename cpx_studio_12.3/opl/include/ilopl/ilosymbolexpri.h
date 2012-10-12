// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilosymbolexpri.h
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

#ifndef __ADVANCED_ilosymbolexpriH
#define __ADVANCED_ilosymbolexpriH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>
#include <ilconcert/ilosymbol.h>


//-------------------------------------------------------------
class IloConditionalSymbolExprI : public IloConditionalExprI< IloSymbolExprI, IloConditionalSymbolExprI, IloFalse >  {

	ILOEXTRDECL
public:
	IloConditionalSymbolExprI(IloEnvI* env, IloConstraintI* cond, IloSymbolExprI* left, IloSymbolExprI* right )
	  : IloConditionalExprI< IloSymbolExprI, IloConditionalSymbolExprI, IloFalse >( env, cond, left, right ) {}
	virtual ~IloConditionalSymbolExprI(){}
};

//-------------------------

class IloSymbolLTI : public IloConstraintI {
  ILOEXTRDECL
private:
  IloSymbolExprI* _expr1;
  IloSymbolExprI* _expr2;
public:
  IloSymbolLTI(IloEnvI* env, IloSymbolExprI* expr1, IloSymbolExprI* expr2, const char* name=0);
  virtual ~IloSymbolLTI();
  virtual void visitSubExtractables(IloExtractableVisitor* v);
  IloSymbolExprI* getExpr1() const { return _expr1; } 
  IloSymbolExprI* getExpr2() const { return _expr2; }
  IloNum eval(const IloAlgorithm alg) const;
  ILOEXTROTHERDECL
};

class IloSymbolGTI : public IloConstraintI {
  ILOEXTRDECL
private:
  IloSymbolExprI* _expr1;
  IloSymbolExprI* _expr2;
public:
  IloSymbolGTI(IloEnvI* env, IloSymbolExprI* expr1, IloSymbolExprI* expr2, const char* name=0);
  virtual ~IloSymbolGTI();
  virtual void visitSubExtractables(IloExtractableVisitor* v);
  IloAnyExprI* getExpr1() const { return _expr1; } 
  IloAnyExprI* getExpr2() const { return _expr2; }
  IloNum eval(const IloAlgorithm alg) const;
  ILOEXTROTHERDECL
};
//

class IloSymbolLeI : public IloConstraintI {
  ILOEXTRDECL
private:
  IloSymbolExprI* _expr1;
  IloSymbolExprI* _expr2;
public:
  IloSymbolLeI(IloEnvI* env, IloSymbolExprI* expr1, IloSymbolExprI* expr2, const char* name=0);
  virtual ~IloSymbolLeI();
  virtual void visitSubExtractables(IloExtractableVisitor* v);
  IloSymbolExprI* getExpr1() const { return _expr1; } 
  IloSymbolExprI* getExpr2() const { return _expr2; }
  IloNum eval(const IloAlgorithm alg) const;
  ILOEXTROTHERDECL
};
//

class IloSymbolGeI : public IloConstraintI {
  ILOEXTRDECL
private:
  IloSymbolExprI* _expr1;
  IloSymbolExprI* _expr2;
public:
  IloSymbolGeI(IloEnvI* env, IloSymbolExprI* expr1, IloSymbolExprI* expr2, const char* name=0);
  virtual ~IloSymbolGeI();
  virtual void visitSubExtractables(IloExtractableVisitor* v);
  IloSymbolExprI* getExpr1() const { return _expr1; } 
  IloSymbolExprI* getExpr2() const { return _expr2; }
  IloNum eval(const IloAlgorithm alg) const;
  ILOEXTROTHERDECL
};
//

#ifdef _WIN32
#pragma pack(pop)
#endif


#endif
