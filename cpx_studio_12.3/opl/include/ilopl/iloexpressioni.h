// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/iloexpressioni.h
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


#ifndef __ADVANCED_iloexpressioniH
#define __ADVANCED_iloexpressioniH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>
#include <ilconcert/iloexpression.h>
#include <ilconcert/ilolinear.h>



template<class BaseClass, class FinalClass, IloBool isInterger >
class IloConditionalExprI : public BaseClass {

	IloConstraintI* _cond;
	BaseClass* _then;
	BaseClass* _else;
public:
	IloConditionalExprI(IloEnvI* env, IloConstraintI* cond, BaseClass* left, BaseClass* right ) 
		: BaseClass(env), 
		  _cond((IloConstraintI*)cond->lockExpr()), 
		  _then((BaseClass*)left->lockExpr()), 
		  _else((BaseClass*)right->lockExpr()) {
	}

	virtual ~IloConditionalExprI() {
		if (!this->getEnv()->isInDestructor()) {
			// no ref counting on IloConstraintI, _cond might have disappeares
			//_cond->release();
			this->getEnv()->release(_then);
			this->getEnv()->release(_else);
		}
	}

	virtual void display( ILOSTD(ostream)& out) const {
		_cond->display(out);
		out << " ? ";
		_then->display(out);
		out << " : ";
		_else->display(out);
	}

	virtual IloExtractableI* makeClone(IloEnvI* env) const {
		IloConstraintI* cond = (IloConstraintI*)env->getClone(_cond);
		BaseClass* left = (BaseClass*)env->getClone(_then);
		BaseClass* right = (BaseClass*)env->getClone(_else);
		return new (env) FinalClass(env, cond, left, right);
	}

	virtual IloBool isInteger() const { 
		return isInterger;
	}

	virtual void visitSubExtractables(IloExtractableVisitor* v) {
		v->beginVisit(this);
		v->visitChildren(this, _cond);
		v->visitChildren(this, _then);
		v->visitChildren(this, _else);
		v->endVisit(this);
	}
	
    IloConstraintI* getCond() const {return _cond;}
    BaseClass* getThen() const {return _then;}
	BaseClass* getElse() const {return _else;}
};

class IloConditionalIntExprI : public IloConditionalExprI< IloIntExprI, IloConditionalIntExprI, IloTrue > {	
	ILOEXTRDECL
public:
	IloConditionalIntExprI(IloEnvI* env, IloConstraintI* cond, IloIntExprI* left, IloIntExprI* right )
	  : IloConditionalExprI< IloIntExprI, IloConditionalIntExprI, IloTrue >( env, cond, left, right ) {}

	virtual ~IloConditionalIntExprI(){}
	virtual IloNum eval(const IloAlgorithm alg) const {
		if (getCond()->eval(alg) == 1) {
			return getThen()->eval(alg);
		} else {
			return getElse()->eval(alg);
		}
	} 
};

class IloConditionalNumExprI: public IloConditionalExprI< IloNumExprI, IloConditionalNumExprI, IloFalse > {
	ILOEXTRDECL
public:
	IloConditionalNumExprI(IloEnvI* env, IloConstraintI* cond, IloNumExprI* left, IloNumExprI* right ) 
		: IloConditionalExprI< IloNumExprI, IloConditionalNumExprI, IloFalse >( env, cond, left, right ) {}

	virtual ~IloConditionalNumExprI(){}
	virtual IloNum eval(const IloAlgorithm alg) const {
		if (getCond()->eval(alg) == 1) {
			return getThen()->eval(alg);
		} else {
			return getElse()->eval(alg);
		}
	}
};

class IloAdvModelEvaluatorI;


class IloRangeWithExprBoundsI : public IloRangeI {
  ILOEXTRDECL
  IloNumExprI* _left;
  IloNumExprI* _right;

  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloRangeWithExprBoundsI(IloEnvI* env,
            IloNumExprI* left,
	          IloNumExprI*  expr,
            IloNumExprI* right,
            const char* name=0);
  virtual ~IloRangeWithExprBoundsI ();

  ILOEXTROTHERDECL

  IloNumExprI* getLeft() const { return _left; }
  IloNumExprI* getRight() const { return _right; }

  IloBool updateBounds(IloAdvModelEvaluatorI*);
};

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
