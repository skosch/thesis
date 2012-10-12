// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilomapextr.h
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

#ifndef __ADVANCED_ilomapextrH
#define __ADVANCED_ilomapextrH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>
#include <ilopl/ilomap.h>
#include <ilopl/ilocollexpr/iloanycollexpri.h>

/////////////////////////////////////////////////////////////////////////////
//   Macros
/////////////////////////////////////////////////////////////////////////////
#define DEFINE_INDEX_UTILITIES_AUX()\
  IloBool isExtractableIndex() const { return getIndex()->isExtractableIndex(); }\
  IloBool isIntIndex() const { return getIndex()->isIntIndex(); }\
  IloBool isIntExprIndex() const { return getIndex()->isIntExprIndex(); }
  


#define DEFINE_MAP_UTILITIES()\
  public :\
  IloMapExtractIndexI* getIndex() const { return _index; }\
  DEFINE_INDEX_UTILITIES_AUX() \
  IloInt getCurrentDim() const { return _currentDim; }\
  virtual void visitSubExtractables(IloExtractableVisitor* v);\
  virtual IloDiscreteDataCollectionI* getIndexer() const {\
    return getMap().getImpl()->getIndexer(_currentDim -1);\
  }\
  IloBool isLastDimension() const { return _currentDim == getMap().getNbDim(); }




/////////////////////////////////////////////////////////////////////////////
//   IloMap
/////////////////////////////////////////////////////////////////////////////

//---------------------------------------------------------------------------
//   IloIntMap
//---------------------------------------------------------------------------

class IloIntSubMapExprI : public IloIntExprI {
  ILOEXTRDECL
protected:
  IloMapExtractIndexI* _index;
  IloInt _currentDim;
  IloMapI* _map;
public:
  IloIntSubMapExprI(IloEnvI* env, IloMapExtractIndexI* index, IloInt dim);
  virtual ~IloIntSubMapExprI();
  virtual IloNum eval(const IloAlgorithm alg) const;
  virtual IloIntMap evalMap(const IloAlgorithm alg) const;
  IloIntMap getMap() const{ return _map; }
  virtual IloIntMap getEvaluatedMap(const IloAlgorithm alg) const = 0;
  IloIntSubMapExprI* makeSubMap(IloMapExtractIndexI* idx);
  DEFINE_MAP_UTILITIES()
};


class IloIntSubMapRootI : public IloIntSubMapExprI {
  ILOEXTRDECL
public:
  IloIntSubMapRootI(IloEnvI* env,
		    IloMapExtractIndexI* index,
		    IloIntMap m);
  virtual ~IloIntSubMapRootI();
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual void display(ILOSTD(ostream)& out) const;
  virtual IloIntMap getEvaluatedMap(const IloAlgorithm alg) const;
  virtual IloDiscreteDataCollectionI* getIndexer() const {
    return _map->getIndexer();
  }
};


class IloIntSubMapSubI : public IloIntSubMapExprI {
  ILOEXTRDECL
private:
  IloIntSubMapExprI* _owner;
protected:
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloIntSubMapSubI(IloEnvI* env, IloMapExtractIndexI* index,
		   IloIntSubMapExprI* owner, IloInt dim);
  virtual ~IloIntSubMapSubI();
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual void display(ILOSTD(ostream)& out) const;
  IloIntSubMapExprI* getOwner() const { return _owner; }
  virtual IloIntMap getEvaluatedMap(const IloAlgorithm alg) const;
};


//---------------------------------------------------------------------------
//   IloIntExprMap
//---------------------------------------------------------------------------

class IloIntExprSubMapExprI : public IloIntExprI {
  ILOEXTRDECL
protected:
  IloMapExtractIndexI* _index;
  IloInt _currentDim;
  IloMapI* _map;
public:
  IloIntExprSubMapExprI(IloEnvI* env, IloMapExtractIndexI* index, IloInt dim);
  virtual ~IloIntExprSubMapExprI();
  virtual IloNum eval(const IloAlgorithm alg) const;
  virtual IloIntVarMap evalMap(const IloAlgorithm alg) const;
  virtual IloIntVarMap getMap() const{ return _map; }
  virtual IloIntVarMap getEvaluatedMap(const IloAlgorithm alg) const = 0;
  IloIntExprSubMapExprI* makeSubMap(IloMapExtractIndexI* idx);
  DEFINE_MAP_UTILITIES()
};


class IloIntExprSubMapRootI : public IloIntExprSubMapExprI {
  ILOEXTRDECL
protected:
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloIntExprSubMapRootI(IloEnvI* env,
			IloMapExtractIndexI* index,
			IloIntVarMap m);
  virtual ~IloIntExprSubMapRootI();
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual void display(ILOSTD(ostream)& out) const;
  virtual IloIntVarMap getEvaluatedMap(const IloAlgorithm alg) const;
  virtual IloDiscreteDataCollectionI* getIndexer() const {
    return _map->getIndexer();
  }
};


class IloIntExprSubMapSubI : public IloIntExprSubMapExprI {
  ILOEXTRDECL
private:
  IloIntExprSubMapExprI* _owner;
protected:
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloIntExprSubMapSubI(IloEnvI* env, IloMapExtractIndexI* index,
		       IloIntExprSubMapExprI* owner, IloInt dim);
  virtual ~IloIntExprSubMapSubI();
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual void display(ILOSTD(ostream)& out) const;
  IloIntExprSubMapExprI* getOwner() const { return _owner; }
  //virtual IloIntVarMap getMap() const { return _owner->getMap(); }
  virtual IloIntVarMap getEvaluatedMap(const IloAlgorithm alg) const;
};

//---------------------------------------------------------------------------
//   IloNumMap
//---------------------------------------------------------------------------


class IloNumSubMapExprI : public IloNumExprI {
  ILOEXTRDECL
protected:
  IloMapExtractIndexI* _index;
  IloInt _currentDim;
  IloMapI* _map;
public:
  IloNumSubMapExprI(IloEnvI* env, IloMapExtractIndexI* index, IloInt dim);
  virtual ~IloNumSubMapExprI();
  virtual IloNum eval(const IloAlgorithm alg) const;
  virtual IloNumMap evalMap(const IloAlgorithm alg) const;
  IloNumMap getMap() const { return _map; }
  virtual IloNumMap getEvaluatedMap(const IloAlgorithm alg) const = 0;
  IloNumSubMapExprI* makeSubMap(IloMapExtractIndexI* idx);
  DEFINE_MAP_UTILITIES()
};


class IloNumSubMapRootI : public IloNumSubMapExprI {
  ILOEXTRDECL
public:
  IloNumSubMapRootI(IloEnvI* env,
		    IloMapExtractIndexI* index,
		    IloNumMap m);
  virtual ~IloNumSubMapRootI();
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual void display(ILOSTD(ostream)& out) const;
  virtual IloNumMap getEvaluatedMap(const IloAlgorithm alg) const;
  virtual IloDiscreteDataCollectionI* getIndexer() const {
    return _map->getIndexer();
  }
};


class IloNumSubMapSubI : public IloNumSubMapExprI {
  ILOEXTRDECL
private:
  IloNumSubMapExprI* _owner;
protected:
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloNumSubMapSubI(IloEnvI* env, IloMapExtractIndexI* index,
		   IloNumSubMapExprI* owner, IloInt dim);
  virtual ~IloNumSubMapSubI();
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual void display(ILOSTD(ostream)& out) const;
  IloNumSubMapExprI* getOwner() const { return _owner; }
  virtual IloNumMap getEvaluatedMap(const IloAlgorithm alg) const;
};


//---------------------------------------------------------------------------
//   IloNumExprMap
//---------------------------------------------------------------------------

class IloNumExprSubMapExprI : public IloNumExprI {
  ILOEXTRDECL
protected:
  IloMapExtractIndexI* _index;
  IloInt _currentDim;
  IloMapI* _map;
public:
  IloNumExprSubMapExprI(IloEnvI* env, IloMapExtractIndexI* index, IloInt dim);
  virtual ~IloNumExprSubMapExprI();
  virtual IloNum eval(const IloAlgorithm alg) const;
  virtual IloNumVarMap evalMap(const IloAlgorithm alg) const;
  IloNumVarMap getMap() const { return _map; }
  virtual IloNumVarMap getEvaluatedMap(const IloAlgorithm alg) const = 0;
  IloNumExprSubMapExprI* makeSubMap(IloMapExtractIndexI* idx);
  DEFINE_MAP_UTILITIES()
};


class IloNumExprSubMapRootI : public IloNumExprSubMapExprI {
  ILOEXTRDECL
protected:
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloNumExprSubMapRootI(IloEnvI* env,
			IloMapExtractIndexI* index,
			IloNumVarMap m);
  virtual ~IloNumExprSubMapRootI();
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual void display(ILOSTD(ostream)& out) const;
  virtual IloNumVarMap getEvaluatedMap(const IloAlgorithm alg) const;
  virtual IloDiscreteDataCollectionI* getIndexer() const {
    return _map->getIndexer();
  }
};


class IloNumExprSubMapSubI : public IloNumExprSubMapExprI {
  ILOEXTRDECL
private:
  IloNumExprSubMapExprI* _owner;
protected:
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloNumExprSubMapSubI(IloEnvI* env, IloMapExtractIndexI* index,
		       IloNumExprSubMapExprI* owner, IloInt dim);
  virtual ~IloNumExprSubMapSubI();
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual void display(ILOSTD(ostream)& out) const;
  IloNumExprSubMapExprI* getOwner() const { return _owner; }
  virtual IloNumVarMap getEvaluatedMap(const IloAlgorithm alg) const;
};

//---------------------------------------------------------------------------
//   IloSymbolMap
//---------------------------------------------------------------------------

class IloSymbolSubMapExprI : public IloSymbolExprI {
  ILOEXTRDECL
protected:
  IloMapExtractIndexI* _index;
  IloInt _currentDim;
  IloMapI* _map;
public:
  IloSymbolSubMapExprI(IloEnvI* env, IloMapExtractIndexI* index, IloInt dim);
  virtual ~IloSymbolSubMapExprI();
  virtual IloSymbolMap evalMap(const IloAlgorithm alg) const;
  IloSymbolMap getMap() const { return _map; }
  virtual IloSymbolMap getEvaluatedMap(const IloAlgorithm alg) const = 0;
  IloSymbolSubMapExprI* makeSubMap(IloMapExtractIndexI* idx);
  DEFINE_MAP_UTILITIES()
};


class IloSymbolSubMapRootI : public IloSymbolSubMapExprI {
  ILOEXTRDECL
public:
  IloSymbolSubMapRootI(IloEnvI* env,
		       IloMapExtractIndexI* index,
		       IloSymbolMap m);
  virtual ~IloSymbolSubMapRootI();
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual void display(ILOSTD(ostream)& out) const;
  virtual IloSymbolMap getEvaluatedMap(const IloAlgorithm alg) const;
  virtual IloDiscreteDataCollectionI* getIndexer() const {
    return _map->getIndexer();
  }
};


class IloSymbolSubMapSubI : public IloSymbolSubMapExprI {
  ILOEXTRDECL
private:
  IloSymbolSubMapExprI* _owner;
protected:
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloSymbolSubMapSubI(IloEnvI* env, IloMapExtractIndexI* index,
		      IloSymbolSubMapExprI* owner, IloInt dim);
  virtual ~IloSymbolSubMapSubI();
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual void display(ILOSTD(ostream)& out) const;
  IloSymbolSubMapExprI* getOwner() const { return _owner; }
  virtual IloSymbolMap getEvaluatedMap(const IloAlgorithm alg) const;
};

////////////////////////////////////////////////////////////
//  Extractables for IloMap[IloXXXIndex]
//
#define SUBMAP_ACCESSOR_IMPL(_subMapType) \
  _subMapType _subMapType::subscriptOp(IloIntExprArg idx) const { \
    IloEnvI* env = idx.getEnv().getImpl(); \
    IloMapIntExprIndexI* idxE = new (env) IloMapIntExprIndexI(env, idx.getImpl()); \
    return _subMapType(getImpl()->makeSubMap(idxE)); \
  } \
  _subMapType _subMapType::subscriptOp(IloNumExprArg idx) const { \
    IloEnvI* env = idx.getEnv().getImpl(); \
    IloMapNumExprIndexI* idxE = new (env) IloMapNumExprIndexI(env, idx.getImpl()); \
    return _subMapType(getImpl()->makeSubMap(idxE)); \
  } \
  _subMapType _subMapType::subscriptOp(IloSymbolExprArg idx) const { \
    IloEnvI* env = idx.getEnv().getImpl(); \
    IloMapAnyExprIndexI* idxE = new (env) IloMapAnyExprIndexI(env, idx.getImpl()); \
    return _subMapType(getImpl()->makeSubMap(idxE)); \
  } \
  _subMapType _subMapType::subscriptOp(IloTupleExprArg idx) const { \
    IloEnvI* env = idx.getEnv().getImpl(); \
    IloMapAnyExprIndexI* idxE = new (env) IloMapAnyExprIndexI(env, idx.getImpl()); \
    return _subMapType(getImpl()->makeSubMap(idxE)); \
  }




//---------------------------------------------------------------------------
//   IloAnyCollectionMap
//---------------------------------------------------------------------------

class IloSymbolCollectionSubMapExprI : public IloSymbolCollectionExprI {
  ILOEXTRDECL
protected:
  IloMapExtractIndexI* _index;
  IloInt _currentDim;
  IloMapI* _map;
public:
  IloSymbolCollectionSubMapExprI(IloEnvI* env,
			      IloMapExtractIndexI* index,
			      IloInt dim);
  virtual ~IloSymbolCollectionSubMapExprI();
  virtual IloSymbolSetMap evalMap(const IloAlgorithm alg) const;
  IloSymbolSetMap getMap() const { return _map; }
  virtual IloSymbolSetMap getEvaluatedMap(const IloAlgorithm alg) const = 0;
  IloSymbolCollectionSubMapExprI* makeSubMap(IloMapExtractIndexI* idx);
  DEFINE_MAP_UTILITIES()
};


class IloSymbolCollectionSubMapRootI : public IloSymbolCollectionSubMapExprI {
  ILOEXTRDECL
public:
  IloSymbolCollectionSubMapRootI(IloEnvI* env,
			      IloMapExtractIndexI* index,
			      IloSymbolSetMap m);
  virtual ~IloSymbolCollectionSubMapRootI();
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual void display(ILOSTD(ostream)& out) const;
  virtual IloSymbolSetMap getEvaluatedMap(const IloAlgorithm alg) const;
  virtual IloDiscreteDataCollectionI* getIndexer() const {
    return _map->getIndexer();
  }
};


class IloSymbolCollectionSubMapSubI : public IloSymbolCollectionSubMapExprI {
  ILOEXTRDECL
private:
  IloSymbolCollectionSubMapExprI* _owner;
protected:
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloSymbolCollectionSubMapSubI(IloEnvI* env, IloMapExtractIndexI* index,
			     IloSymbolCollectionSubMapExprI* owner,
			     IloInt dim);
  virtual ~IloSymbolCollectionSubMapSubI();
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual void display(ILOSTD(ostream)& out) const;
  IloSymbolCollectionSubMapExprI* getOwner() const { return _owner; }
  virtual IloSymbolSetMap getEvaluatedMap(const IloAlgorithm alg) const;
};


class IloTupleSetSubMapExprI : public IloTupleSetExprI {
  ILOEXTRDECL
protected:
  IloMapExtractIndexI* _index;
  IloInt _currentDim;
  IloMapI* _map;
public:
  IloTupleSetSubMapExprI(IloEnvI* env,
			      IloMapExtractIndexI* index,
			      IloInt dim);
  virtual ~IloTupleSetSubMapExprI();
  virtual IloTupleSetMap evalMap(const IloAlgorithm alg) const;
  IloTupleSetMap getMap() const { return _map; }
  virtual IloTupleSetMap getEvaluatedMap(const IloAlgorithm alg) const = 0;
  IloTupleSetSubMapExprI* makeSubMap(IloMapExtractIndexI* idx);
  DEFINE_MAP_UTILITIES()
};


class IloTupleSetSubMapRootI : public IloTupleSetSubMapExprI {
  ILOEXTRDECL
public:
  IloTupleSetSubMapRootI(IloEnvI* env,
			      IloMapExtractIndexI* index,
			      IloTupleSetMap m);
  virtual ~IloTupleSetSubMapRootI();
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual void display(ILOSTD(ostream)& out) const;
  virtual IloTupleSetMap getEvaluatedMap(const IloAlgorithm alg) const;
  virtual IloDiscreteDataCollectionI* getIndexer() const {
    return _map->getIndexer();
  }
};


class IloTupleSetSubMapSubI : public IloTupleSetSubMapExprI {
  ILOEXTRDECL
private:
  IloTupleSetSubMapExprI* _owner;
protected:
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloTupleSetSubMapSubI(IloEnvI* env, IloMapExtractIndexI* index,
			     IloTupleSetSubMapExprI* owner,
			     IloInt dim);
  virtual ~IloTupleSetSubMapSubI();
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual void display(ILOSTD(ostream)& out) const;
  IloTupleSetSubMapExprI* getOwner() const { return _owner; }
  virtual IloTupleSetMap getEvaluatedMap(const IloAlgorithm alg) const;
};

//---------------------------------------------------------------------------
//   IloIntCollectionMap
//---------------------------------------------------------------------------

class IloIntCollectionSubMapExprI : public IloIntCollectionExprI {
  ILOEXTRDECL
protected:
  IloMapExtractIndexI* _index;
  IloInt _currentDim;
  IloMapI* _map;
public:
  IloNum eval(const IloAlgorithm) const;

  IloIntCollectionSubMapExprI(IloEnvI* env,
			      IloMapExtractIndexI* index,
			      IloInt dim);
  virtual ~IloIntCollectionSubMapExprI();
  virtual IloIntCollectionMap evalMap(const IloAlgorithm alg) const;
  IloIntCollectionMap getMap() const { return _map; }
  virtual IloIntCollectionMap getEvaluatedMap(const IloAlgorithm alg) const = 0;
  IloIntCollectionSubMapExprI* makeSubMap(IloMapExtractIndexI* idx);
  DEFINE_MAP_UTILITIES()
};


class IloIntCollectionSubMapRootI : public IloIntCollectionSubMapExprI {
  ILOEXTRDECL
public:
  IloIntCollectionSubMapRootI(IloEnvI* env,
			      IloMapExtractIndexI* index,
			      IloIntCollectionMap m);
  virtual ~IloIntCollectionSubMapRootI();
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual void display(ILOSTD(ostream)& out) const;
  virtual IloIntCollectionMap getEvaluatedMap(const IloAlgorithm alg) const;
  virtual IloDiscreteDataCollectionI* getIndexer() const {
    return _map->getIndexer();
  }
};


class IloIntCollectionSubMapSubI : public IloIntCollectionSubMapExprI {
  ILOEXTRDECL
private:
  IloIntCollectionSubMapExprI* _owner;
protected:
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloIntCollectionSubMapSubI(IloEnvI* env, IloMapExtractIndexI* index,
			     IloIntCollectionSubMapExprI* owner,
			     IloInt dim);
  virtual ~IloIntCollectionSubMapSubI();
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual void display(ILOSTD(ostream)& out) const;
  IloIntCollectionSubMapExprI* getOwner() const { return _owner; }
  virtual IloIntCollectionMap getEvaluatedMap(const IloAlgorithm alg) const;
  virtual IloBool isInteger() const{ return IloTrue;}
};
//-----------------------------------

//---------------------------------------------------------------------------
//   IloNumCollectionMap
//---------------------------------------------------------------------------

class IloNumCollectionSubMapExprI : public IloNumCollectionExprI {
  ILOEXTRDECL
protected:
  IloMapExtractIndexI* _index;
  IloInt _currentDim;
  IloMapI* _map;
public:
	IloNum eval(const IloAlgorithm) const;

  IloNumCollectionSubMapExprI(IloEnvI* env, IloMapExtractIndexI* index, IloInt dim);
  virtual ~IloNumCollectionSubMapExprI();
  virtual IloNumCollectionMap evalMap(const IloAlgorithm alg) const;
  IloNumCollectionMap getMap() const { return _map; }
  virtual IloNumCollectionMap getEvaluatedMap(const IloAlgorithm alg) const = 0;
  IloNumCollectionSubMapExprI* makeSubMap(IloMapExtractIndexI* idx);
  DEFINE_MAP_UTILITIES()
};


class IloNumCollectionSubMapRootI : public IloNumCollectionSubMapExprI {
  ILOEXTRDECL
public:
  IloNumCollectionSubMapRootI(IloEnvI* env,
			      IloMapExtractIndexI* index,
			      IloNumCollectionMap m);
  virtual ~IloNumCollectionSubMapRootI();
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual void display(ILOSTD(ostream)& out) const;
  virtual IloNumCollectionMap getEvaluatedMap(const IloAlgorithm alg) const;
  virtual IloDiscreteDataCollectionI* getIndexer() const {
    return _map->getIndexer();
  }
};


class IloNumCollectionSubMapSubI : public IloNumCollectionSubMapExprI {
  ILOEXTRDECL
private:
  IloNumCollectionSubMapExprI* _owner;
protected:
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloNumCollectionSubMapSubI(IloEnvI* env, IloMapExtractIndexI* index,
			     IloNumCollectionSubMapExprI* owner,
			     IloInt dim);
  virtual ~IloNumCollectionSubMapSubI();
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual void display(ILOSTD(ostream)& out) const;
  IloNumCollectionSubMapExprI* getOwner() const { return _owner; }
  virtual IloNumCollectionMap getEvaluatedMap(const IloAlgorithm alg) const;
};

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
