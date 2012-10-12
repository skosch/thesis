// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/iltuple/ilotuplemapi.h
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

#ifndef __ADVANCED_ilotuplemapiH
#define __ADVANCED_ilotuplemapiH


#ifdef _WIN32
#pragma pack(push, 8)
#endif
#include <ilopl/ilosys.h>

#include <ilopl/ilomapextr.h>

class IloTupleSubMapExprI;

class IloAbstractTupleMapI : public IloAbstractMapI {
  ILORTTIDECL
protected:
  IloTupleCollectionI* _coll;
  IloAbstractTupleMapI(IloEnvI* env, IloDiscreteDataCollectionI* indexer, const IloTupleSchema& schema, IloInt size);
  IloAbstractTupleMapI(IloEnvI* env, IloDiscreteDataCollectionI* indexer, IloTupleCollectionI* coll);
public:
    IloTupleBufferI* getOrMakeSharedTupleBuffer(){
        return _coll->getOrMakeSharedTupleBuffer();
    }
  IloBool isDefaultValue(IloTuple);
	void setDefaultValue(IloTupleCellArray cells);
	void setDefaultValue(IloTupleBufferI* buf);

  virtual ~IloAbstractTupleMapI();
  virtual IloAbstractTupleMapI* getCopy() const = 0;
  virtual IloAbstractTupleMapI* makeClone(IloEnvI* env) const = 0;
  IloTupleSchema getSchema() const { return _coll->getSchema();}
  IloBool checkSchema(IloTupleI* t) const;
  IloBool checkKeySchema(IloTupleI* t) const;
  void setTupleCollection(IloTupleCollectionI* coll){
	  _coll = coll;
  }
  IloTupleCollectionI* getTupleCollection() const { return _coll; }
  IloTupleBuffer makeTupleBuffer() const;
  IloTuple makeTuple() const;
 

  IloInt getAbsoluteIndex(IloAny idx, IloInt dim = 0) const {
    IloAssert(getIndexer(dim),"Using IloTupleMap/IloTupleMap without indexer");
    return getIndexer(dim)->getIndex(idx);
  }
  IloInt getAbsoluteIndex(IloOplObject idx, IloInt dim = 0) const;
public:
  virtual void setAtAbsoluteIndex(IloInt index, IloOplObject value);
  virtual void setAtAbsoluteIndex(IloInt index, IloTuple value);
  virtual void setAtAbsoluteIndex(IloInt index, IloTupleBuffer value);

  virtual void setAtAbsoluteIndex(IloIntFixedArray indices, IloTuple value) = 0;
  virtual void setAtAbsoluteIndex(IloIntFixedArray indices, IloTupleBuffer value)=0;
  virtual void setAtAbsoluteIndex(IloIntFixedArray indices, IloOplObject value)=0;
  
  virtual void getAtAbsoluteIndex(IloInt index, IloTuple  buffer) const;
  virtual void getAtAbsoluteIndex(IloInt index, IloTupleBuffer buffer) const;
  
  virtual IloOplObject getAtAbsoluteIndex(IloInt index) const;
  virtual IloOplObject getAtAbsoluteIndex(IloIntFixedArray indices) const = 0;
  
  virtual void getAtAbsoluteIndex(IloIntFixedArray indices, IloTuple tuple) const=0;
  virtual void getAtAbsoluteIndex(IloIntFixedArray indices,IloTupleBuffer buffer) const =0;
  
  virtual void setAt(IloMapIndexArray indices, IloTuple value) = 0;
  virtual void setAt(IloMapIndexArray indices, IloTupleBuffer value) = 0;
  virtual void setAt(IloMapIndexArray indices, IloOplObject value) = 0;
  
  virtual IloOplObject getAt(IloMapIndexArray indices) const = 0;
  virtual void getAt(IloMapIndexArray indices, IloTuple tuple) const = 0;
  virtual void getAt(IloMapIndexArray indices, IloTupleBuffer buffer) const =0;
  virtual IloRttiEnvObjectI* makeOplClone(IloEnvI* env) const{
	  return (IloRttiEnvObjectI*) makeClone(env);
  }
};


class IloTupleMapI : public IloAbstractTupleMapI {
  ILORTTIDECL
protected:
  IloIntFixedArray _sharedIndexes;
  IloIntMap _indices;
private:
  void initIndices();
  IloInt initIndices(IloIntMap m, IloInt currentDim, IloInt value);
  IloOplObject getSubMapExpr(IloMapIndexArray indices) const;
public:
  IloMapI* getIndImpl() const { return _indices.getImpl(); }
  IloIntMap getIndices() const { return _indices; }
public:
  friend class IloTupleSubMapExprI;
  friend class IloTupleSubMapRootI;
  friend class IloTupleSubMapSubI;
  IloTupleMapI(IloEnvI*, const IloTupleMapI*);
  virtual void copyContent(const IloAbstractMapI*);
  virtual ~IloTupleMapI();
  virtual IloAbstractTupleMapI* getCopy() const;
  virtual IloAbstractTupleMapI* makeClone(IloEnvI* env) const;
  IloTupleMapI(IloEnvI* env, IloIntMap indices, const IloTupleSchema& schema);
  IloTupleMapI(IloEnvI* env, IloIntMap indices, IloTupleCollectionI* coll);

  virtual IloInt getNbDim() const { return getIndImpl()->getNbDim(); }
  virtual IloInt getTotalSize() { return getIndImpl()->getTotalSize(); } 
  virtual IloInt getSize() const { return getIndImpl()->getSize(); }
  virtual const char* getName() const { return getIndImpl()->getName(); }
  virtual void setName(const char* name) { getIndImpl()->setName(name); }
  using IloAbstractTupleMapI::getIndexer;
  virtual IloDiscreteDataCollectionI* getIndexer(IloInt i) const;

  using IloAbstractTupleMapI::getAbsoluteIndex;
  IloInt getAbsoluteIndex(IloIntFixedArray indices) const;
  IloIntFixedArray getOrMakeSharedAbsoluteIndexArray(IloMapIndexArray indices);

  void set(IloOplObject index, IloTuple value);
  void set(IloOplObject index, IloTupleBuffer value);
  void get(IloOplObject index, IloTuple tuple) const;
  void get(IloOplObject index, IloTupleBuffer buffer) const;

  void setAt(IloMapIndexArray indices, IloTuple value);
  void setAt(IloMapIndexArray indices, IloTupleBuffer value);
  virtual void setAt(IloMapIndexArray indices, IloOplObject value);
  virtual IloOplObject getAt(IloMapIndexArray indices) const;
  void getAt(IloMapIndexArray indices, IloTuple tuple) const;
  void getAt(IloMapIndexArray indices, IloTupleBuffer buffer) const;

  using IloAbstractTupleMapI::setAtAbsoluteIndex;
  using IloAbstractTupleMapI::getAtAbsoluteIndex;
  void setAtAbsoluteIndex(IloIntFixedArray indices, IloTuple value);
  void setAtAbsoluteIndex(IloIntFixedArray indices, IloTupleBuffer value);
  virtual void setAtAbsoluteIndex(IloIntFixedArray indices, IloOplObject value);
  virtual IloOplObject getAtAbsoluteIndex(IloIntFixedArray indices) const;
  void getAtAbsoluteIndex(IloIntFixedArray indices, IloTuple tuple) const;
  void getAtAbsoluteIndex(IloIntFixedArray indices, IloTupleBuffer buffer) const;
  virtual void display(ILOSTD(ostream)& out) const;
};

//---------------------------------------------------------------------------
//   Extractables for IloTupleMap
//---------------------------------------------------------------------------

class IloTupleSubMapExprI : public IloTupleExprI {
  ILOEXTRDECL
protected:
  IloIntSubMapExprI* _indices;
  IloTupleMapI* _map;
protected:
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloTupleSubMapExprI(IloEnvI* env, IloIntSubMapExprI* ind);
  virtual ~IloTupleSubMapExprI();
  IloTupleMapI* getMap() const { return _map; }
  
  virtual IloTupleSubMapExprI* makeSubMap(IloMapExtractIndexI* idx);
  IloIntSubMapExprI* getIndices() const { return _indices; }
  virtual IloMapExtractIndexI* getIndex() const { return getIndices()->getIndex(); }
  IloInt getCurrentDim() const { return getIndices()->getCurrentDim(); }
  IloBool isLastDimension() const { return getIndices()->isLastDimension(); }
  virtual IloDiscreteDataCollectionI* getIndexer() const {
    return getIndices()->getIndexer();
  }
  DEFINE_INDEX_UTILITIES_AUX()
};

class IloTupleSubMapRootI : public IloTupleSubMapExprI {
  ILOEXTRDECL
private:
  IloTupleSubMapRootI(IloEnvI* env,
		      IloIntSubMapRootI* index,
		      IloTupleMapI* m);
public:
  IloTupleSubMapRootI(IloEnvI* env,
		      IloMapExtractIndexI* index,
		      IloTupleMapI* m);
  virtual ~IloTupleSubMapRootI();
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual void display(ILOSTD(ostream)& out) const;
};

class IloTupleSubMapSubI : public IloTupleSubMapExprI {
  ILOEXTRDECL
private:
  IloTupleSubMapExprI* _owner;
protected:
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloTupleSubMapSubI(IloEnvI* env, IloIntSubMapExprI* iSub,
		     IloTupleSubMapExprI* owner);
  IloTupleSubMapSubI(IloEnvI* env, IloMapExtractIndexI* index,
		     IloTupleSubMapExprI* owner, IloInt dim);
  virtual ~IloTupleSubMapSubI();
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  virtual void display(ILOSTD(ostream)& out) const;
  IloTupleSubMapExprI* getOwner() const { return _owner; }
};

ILOSUBMAPHANDLE(IloTuple, IloTupleExprArg)

#ifdef _WIN32
#pragma pack(pop)
#endif


#endif
