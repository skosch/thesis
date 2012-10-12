// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilsched/ilosoplcpi.h
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


#ifndef __CONCERT_ilosoplcpiH
#define __CONCERT_ilosoplcpiH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>

# include <ilconcert/ilosmodel.h>
# include <ilconcert/ilsched/ilostimei.h>
# include <ilconcert/ilsched/ilossequencei.h>
#include <ilconcert/ilsched/ilosatomi.h>
# include <ilopl/ilomapextr.h>
# include <ilopl/ilooplcpi.h>

//---------------------------------------------------------------------------
//   IloIntervalVarMap
//---------------------------------------------------------------------------


class IloIntervalVarSubMapExprI : public IloIntervalExprI {
	ILOEXTRDECL
protected:
	IloMapExtractIndexI* _index;
	IloInt _currentDim;
protected:
	virtual void atRemove(IloExtractableI* sub = 0, IloAny info = 0);
public:
	IloIntervalVarSubMapExprI(IloEnvI* env, IloMapExtractIndexI* index,
		IloInt dim);
	virtual ~IloIntervalVarSubMapExprI();
	virtual IloIntervalVarMap evalMap(const IloAlgorithm alg) const;
	virtual IloIntervalVarMap getMap() const = 0;
	virtual IloIntervalVarMap getEvaluatedMap(const IloAlgorithm alg) const = 0;
	IloIntervalVarSubMapExprI* makeSubMap(IloMapExtractIndexI* idx);
	DEFINE_MAP_UTILITIES()
};

//---------------------------------------------------------------------------
//   Conditional Exepression for functions
//---------------------------------------------------------------------------
template<class BaseClass, class FinalClass >
class IloConditionalFunctionExprI : public BaseClass {
	IloConstraintI* _cond;
	BaseClass* _then;
	BaseClass* _else;
public:
	IloConditionalFunctionExprI(IloEnvI* env,
		IloConstraintI* cond,
		BaseClass* left,
		BaseClass* right )
		: BaseClass(env),
		_cond((IloConstraintI*)cond->lockExpr()),
		_then(left),
		_else(right) {
	}

	virtual ~IloConditionalFunctionExprI() {}

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

//----------------------------------------------------------------------
//   conditional expression
//----------------------------------------------------------------------
class IloConditionalPiecewiseFunctionExprI:
	public IloConditionalFunctionExprI<IloAdvPiecewiseFunctionExprI,
	IloConditionalPiecewiseFunctionExprI> {
		ILOEXTRDECL
public:
	IloConditionalPiecewiseFunctionExprI(IloEnvI* env,
		IloConstraintI* cond,
		IloAdvPiecewiseFunctionExprI* left,
		IloAdvPiecewiseFunctionExprI* right);
	virtual ~IloConditionalPiecewiseFunctionExprI(){}
	virtual IloBool isStepwise() const { return getThen()->isStepwise(); }
};

//----------------------------------------------------------------------
class IloAdvPiecewiseFunctionExprSubMapExprI : public IloAdvPiecewiseFunctionExprI {
	ILOEXTRDECL
protected:
	IloMapExtractIndexI* _index;
	IloInt _currentDim;
public:
	IloAdvPiecewiseFunctionExprSubMapExprI(IloEnvI* env, IloMapExtractIndexI* index,
		IloInt dim);
	virtual ~IloAdvPiecewiseFunctionExprSubMapExprI();
	virtual IloPiecewiseFunctionExprMap evalMap(const IloAlgorithm alg) const;
	virtual IloPiecewiseFunctionExprMap getMap() const = 0;
	virtual IloPiecewiseFunctionExprMap getEvaluatedMap(const IloAlgorithm alg) const = 0;
	IloAdvPiecewiseFunctionExprSubMapExprI* makeSubMap(IloMapExtractIndexI* idx);
	DEFINE_MAP_UTILITIES()
};

class IloIntervalSequenceVarSubMapExprI : public IloIntervalSequenceExprI {
	ILOEXTRDECL
protected:
	IloMapExtractIndexI* _index;
	IloInt _currentDim;
public:
	IloIntervalSequenceVarSubMapExprI(IloEnvI* env, IloMapExtractIndexI* index,
		IloInt dim);
	virtual ~IloIntervalSequenceVarSubMapExprI();
	virtual IloIntervalSequenceVarMap evalMap(const IloAlgorithm alg) const;
	virtual IloIntervalSequenceVarMap getMap() const = 0;
	virtual IloIntervalSequenceVarMap getEvaluatedMap(const IloAlgorithm alg) const = 0;
	IloIntervalSequenceVarSubMapExprI* makeSubMap(IloMapExtractIndexI* idx);
	DEFINE_MAP_UTILITIES()
};

//---------------------------------------------------------------------------
//   IloCumulAtom Expressions
//---------------------------------------------------------------------------

class IloAdvCumulAtomI : public IloCumulFunctionExprI {
	ILOS_CPEXTR_DECL(IloAdvCumulAtomI, IloCumulFunctionExprI)
protected:
	IloIntExprI*  _levelMin; // non-ground - value for some cases
	// for makeClone and subclasses
	IloAdvCumulAtomI(IloEnvI* env, IloIntExprI*  levelMin, const char* name=0);
public:
	virtual ~IloAdvCumulAtomI();
	virtual IloBool isPulse() const=0;
	virtual IloBool isStep() const=0;
	IloIntExprArg getLevelMin() const { return _levelMin; }
	virtual IloIntExprArg getValue() const=0;
	virtual IloBool isAtomic() const { return IloTrue; }
protected:
	void visitAtoms(IloCumulFunctionExprI::AtomVisitor * visitor,
		IloCumulFunctionExprI::AtomVisitorContext * ctx);
};

//---------------------------------------------------------------------------
//   IloStateFunctionExprMap
//---------------------------------------------------------------------------
class IloStateFunctionExprSubMapExprI : public IloStateFunctionExprI {
	ILOEXTRDECL
protected:
	IloMapExtractIndexI* _index;
	IloInt _currentDim;
public:
	IloStateFunctionExprSubMapExprI(IloEnvI* env,
		IloMapExtractIndexI* index,
		IloInt dim);
	virtual ~IloStateFunctionExprSubMapExprI();
	IloStateFunctionExprSubMapExprI* makeSubMap(IloMapExtractIndexI* idx);
	virtual IloStateFunctionExprMap evalMap(const IloAlgorithm alg) const;
	virtual IloStateFunctionExprMap getMap() const = 0;
	virtual IloStateFunctionExprMap getEvaluatedMap(const IloAlgorithm alg) const = 0;
	DEFINE_MAP_UTILITIES()
};

//---------------------------------------------------------------------------
//   aggregate form
//---------------------------------------------------------------------------
// sum(i in ...) pulse(t[i], q);
class IloAggregateCumulExprI : public IloCumulFunctionExprI {
	ILOS_CPEXTR_DECL(IloAggregateCumulExprI, IloCumulFunctionExprI)
		void visitAtoms(IloCumulFunctionExprI::AtomVisitor * visitor,
		IloCumulFunctionExprI::AtomVisitorContext * ctx);
private:
	IloComprehensionI* _comprehension;
	IloCumulFunctionExprI* _body;
	virtual IloExtractableI* makeClone(IloEnvI*) const;
public:
	IloAggregateCumulExprI(IloEnvI* env, IloComprehensionI* comp,
		IloCumulFunctionExprI* expr, const char* name=0);
	virtual ~IloAggregateCumulExprI();
	IloComprehensionI* getComprehension() const { return _comprehension; }
	IloCumulFunctionExprI* getExpr() const { return _body; }
	virtual IloBool isAtomic() const { return IloFalse; }
protected:
	virtual void atRemove(IloExtractableI* sub = 0, IloAny info = 0);
public:
	virtual void display(ILOSTD(ostream)& out) const;
};

//---------------------------------------------------------------------------
//   conditional expression
//---------------------------------------------------------------------------
// cumulFunction c=(i== XXX)?(pulse(...):pulse(...);
class IloConditionalCumulFunctionExprI:
	public IloConditionalFunctionExprI<IloCumulFunctionExprI,
	IloConditionalCumulFunctionExprI> {
		ILOEXTRDECL
public:
	IloConditionalCumulFunctionExprI(IloEnvI* env,
		IloConstraintI* cond,
		IloCumulFunctionExprI* left,
		IloCumulFunctionExprI* right)
		: IloConditionalFunctionExprI<IloCumulFunctionExprI,
		IloConditionalCumulFunctionExprI>(env,
		cond,
		left,
		right) {}
	virtual ~IloConditionalCumulFunctionExprI(){}
	virtual IloBool isAtomic() const;
	virtual void visitAtoms(AtomVisitor * visitor, AtomVisitorContext * ctx);
};
//---------------------------------------------------------------------------


class IloIntervalVarSubMapRootI : public IloIntervalVarSubMapExprI {
	ILOEXTRDECL
private:
	IloIntervalVarMap _map;
protected:
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloIntervalVarSubMapRootI(IloEnvI* env,
		IloMapExtractIndexI* index,
		IloIntervalVarMap m);
	virtual ~IloIntervalVarSubMapRootI();
	virtual IloExtractableI* makeClone(IloEnvI*) const;
	virtual void display(ILOSTD(ostream)& out) const;
	virtual IloIntervalVarMap getMap() const { return _map; }
	virtual IloIntervalVarMap getEvaluatedMap(const IloAlgorithm alg) const;
	virtual IloBool isRoot() const { return IloTrue; }
	virtual IloDiscreteDataCollectionI* getIndexer() const {
		return _map.getImpl()->getIndexer();
	}
};


class IloIntervalVarSubMapSubI : public IloIntervalVarSubMapExprI {
	ILOEXTRDECL
private:
	IloIntervalVarSubMapExprI* _owner;
protected:
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloIntervalVarSubMapSubI(IloEnvI* env, IloMapExtractIndexI* index,
		IloIntervalVarSubMapExprI* owner, IloInt dim);
	virtual ~IloIntervalVarSubMapSubI();
	virtual IloExtractableI* makeClone(IloEnvI*) const;
	virtual void display(ILOSTD(ostream)& out) const;
	IloIntervalVarSubMapExprI* getOwner() const { return _owner; }
	virtual IloIntervalVarMap getMap() const { return _owner->getMap(); }
	virtual IloIntervalVarMap getEvaluatedMap(const IloAlgorithm alg) const;
};


class IloAdvPiecewiseFunctionSubMapRootI : public IloAdvPiecewiseFunctionExprSubMapExprI {
	ILOEXTRDECL
private:
	IloPiecewiseFunctionExprMap _map;
protected:
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloAdvPiecewiseFunctionSubMapRootI(IloEnvI* env,
		IloMapExtractIndexI* index,
		IloPiecewiseFunctionExprMap m);
	virtual ~IloAdvPiecewiseFunctionSubMapRootI();
	virtual IloExtractableI* makeClone(IloEnvI*) const;
	virtual void display(ILOSTD(ostream)& out) const;
	virtual IloPiecewiseFunctionExprMap getMap() const { return _map; }
	virtual IloPiecewiseFunctionExprMap getEvaluatedMap(const IloAlgorithm alg) const;
	virtual IloBool isRoot() const { return IloTrue; }
	virtual IloDiscreteDataCollectionI* getIndexer() const {
		return _map.getImpl()->getIndexer();
	}
};

class IloAdvPiecewiseFunctionSubMapSubI : public IloAdvPiecewiseFunctionExprSubMapExprI {
	ILOEXTRDECL
private:
	IloAdvPiecewiseFunctionExprSubMapExprI* _owner;
protected:
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloAdvPiecewiseFunctionSubMapSubI(IloEnvI* env, IloMapExtractIndexI* index,
		IloAdvPiecewiseFunctionExprSubMapExprI* owner, IloInt dim);
	virtual ~IloAdvPiecewiseFunctionSubMapSubI();
	virtual IloExtractableI* makeClone(IloEnvI*) const;
	virtual void display(ILOSTD(ostream)& out) const;
	IloAdvPiecewiseFunctionExprSubMapExprI* getOwner() const { return _owner; }
	virtual IloPiecewiseFunctionExprMap getMap() const { return _owner->getMap(); }
	virtual IloPiecewiseFunctionExprMap getEvaluatedMap(const IloAlgorithm alg) const;
};


class IloIntervalSequenceVarSubMapRootI : public IloIntervalSequenceVarSubMapExprI {
	ILOEXTRDECL
private:
	IloIntervalSequenceVarMap _map;
protected:
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloIntervalSequenceVarSubMapRootI(IloEnvI* env,
		IloMapExtractIndexI* index,
		IloIntervalSequenceVarMap m);
	virtual ~IloIntervalSequenceVarSubMapRootI();
	virtual IloExtractableI* makeClone(IloEnvI*) const;
	virtual void display(ILOSTD(ostream)& out) const;
	virtual IloIntervalSequenceVarMap getMap() const { return _map; }
	virtual IloIntervalSequenceVarMap getEvaluatedMap(const IloAlgorithm alg) const;
	virtual IloBool isRoot() const { return IloTrue; }
	virtual IloDiscreteDataCollectionI* getIndexer() const {
		return _map.getImpl()->getIndexer();
	}
};

class IloIntervalSequenceVarSubMapSubI : public IloIntervalSequenceVarSubMapExprI {
	ILOEXTRDECL
private:
	IloIntervalSequenceVarSubMapExprI* _owner;
protected:
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloIntervalSequenceVarSubMapSubI(IloEnvI* env, IloMapExtractIndexI* index,
		IloIntervalSequenceVarSubMapExprI* owner,
		IloInt dim);
	virtual ~IloIntervalSequenceVarSubMapSubI();
	virtual IloExtractableI* makeClone(IloEnvI*) const;
	virtual void display(ILOSTD(ostream)& out) const;
	IloIntervalSequenceVarSubMapExprI* getOwner() const { return _owner; }
	virtual IloIntervalSequenceVarMap getMap() const { return _owner->getMap(); }
	virtual IloIntervalSequenceVarMap getEvaluatedMap(const IloAlgorithm alg) const;
};


class IloCumulFunctionExprSubMapExprI : public IloCumulFunctionExprI {
	ILOEXTRDECL
protected:
	IloMapExtractIndexI* _index;
	IloInt _currentDim;
	void visitAtoms(IloCumulFunctionExprI::AtomVisitor * visitor,
		IloCumulFunctionExprI::AtomVisitorContext * ctx);
public:
	IloCumulFunctionExprSubMapExprI(IloEnvI* env, IloMapExtractIndexI* index,
		IloInt dim);
	virtual ~IloCumulFunctionExprSubMapExprI();
	virtual IloBool isAtomic() const { return IloFalse; }
	virtual IloCumulFunctionExprMap evalMap(const IloAlgorithm alg) const;
	virtual IloCumulFunctionExprMap getMap() const = 0;
	virtual IloCumulFunctionExprMap getEvaluatedMap(const IloAlgorithm alg) const = 0;
	IloCumulFunctionExprSubMapExprI* makeSubMap(IloMapExtractIndexI* idx);
	DEFINE_MAP_UTILITIES()
};

class IloCumulFunctionExprSubMapRootI : public IloCumulFunctionExprSubMapExprI {
	ILOEXTRDECL
private:
	IloCumulFunctionExprMap _map;
protected:
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloCumulFunctionExprSubMapRootI(IloEnvI* env,
		IloMapExtractIndexI* index,
		IloCumulFunctionExprMap m);
	virtual ~IloCumulFunctionExprSubMapRootI();
	virtual IloExtractableI* makeClone(IloEnvI*) const;
	virtual void display(ILOSTD(ostream)& out) const;
	virtual IloCumulFunctionExprMap getMap() const { return _map; }
	virtual IloCumulFunctionExprMap getEvaluatedMap(const IloAlgorithm alg) const;
	virtual IloBool isRoot() const { return IloTrue; }
	virtual IloDiscreteDataCollectionI* getIndexer() const {
		return _map.getImpl()->getIndexer();
	}
};

class IloCumulFunctionExprSubMapSubI : public IloCumulFunctionExprSubMapExprI {
	ILOEXTRDECL
private:
	IloCumulFunctionExprSubMapExprI* _owner;
protected:
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloCumulFunctionExprSubMapSubI(IloEnvI* env, IloMapExtractIndexI* index,
		IloCumulFunctionExprSubMapExprI* owner, IloInt dim);
	virtual ~IloCumulFunctionExprSubMapSubI();
	virtual IloExtractableI* makeClone(IloEnvI*) const;
	virtual void display(ILOSTD(ostream)& out) const;
	IloCumulFunctionExprSubMapExprI* getOwner() const { return _owner; }
	virtual IloCumulFunctionExprMap getMap() const { return _owner->getMap(); }
	virtual IloCumulFunctionExprMap getEvaluatedMap(const IloAlgorithm alg) const;
};


class IloStateFunctionExprSubMapRootI :
	public IloStateFunctionExprSubMapExprI {
		ILOEXTRDECL
private:
	IloStateFunctionExprMap _map;
protected:
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloStateFunctionExprSubMapRootI(IloEnvI* env,
		IloMapExtractIndexI* index,
		IloStateFunctionExprMap m);
	virtual ~IloStateFunctionExprSubMapRootI();
	virtual IloExtractableI* makeClone(IloEnvI*) const;
	virtual void display(ILOSTD(ostream)& out) const;
	virtual IloStateFunctionExprMap getMap() const { return _map; }
	virtual IloStateFunctionExprMap getEvaluatedMap(const IloAlgorithm alg) const;
	virtual IloBool isRoot() const { return IloTrue; }
	virtual IloDiscreteDataCollectionI* getIndexer() const {
		return _map.getImpl()->getIndexer();
	}
};

class IloStateFunctionExprSubMapSubI :
	public IloStateFunctionExprSubMapExprI {
		ILOEXTRDECL
protected:
	virtual void visitSubExtractables(IloExtractableVisitor* v);
private:
	IloStateFunctionExprSubMapExprI* _owner;
public:
	IloStateFunctionExprSubMapSubI(IloEnvI* env,
		IloMapExtractIndexI* index,
		IloStateFunctionExprSubMapExprI* owner,
		IloInt dim);
	virtual ~IloStateFunctionExprSubMapSubI();
	IloStateFunctionExprSubMapExprI* getOwner() const { return _owner; }
	virtual IloExtractableI* makeClone(IloEnvI*) const;
	virtual void display(ILOSTD(ostream)& out) const;
	virtual IloStateFunctionExprMap getMap() const { return _owner->getMap(); }
	virtual IloStateFunctionExprMap getEvaluatedMap(const IloAlgorithm alg) const;
};



inline IloAdvPiecewiseFunctionI* MakeAdvStepFunction(IloEnvI* env, const IloNumArray x, const IloNumArray y, const char* name =0) {
	return new (env) IloAdvPiecewiseFunctionI(env, x, y, name);
}
inline IloAdvPiecewiseFunctionI* MakeAdvPiecewiseFunction(IloEnvI* env, const IloNumArray x, const IloNumArray s, IloNum x0, IloNum y0, const char* name=0) {
	return new (env) IloAdvPiecewiseFunctionI(env, x, s, x0, y0, name);
}




#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
