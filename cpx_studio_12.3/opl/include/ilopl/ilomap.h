// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilomap.h
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

#ifndef __ADVANCED_ilomapH
#define __ADVANCED_ilomapH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>
#include <ilopl/iloforall.h>
#include <ilopl/ilomapi.h>
#include <ilopl/ilocollexpr.h>

#define FIX_OPL_5226

#define IloIntExprMap IloIntVarMap
#define IloNumExprMap IloNumVarMap



// ----------------------------------------------------------------------
// IloXXXExprArg operator[](arg)
//     arg: IloIntExprArg, IloNumExprArg, IloSymbolExprArg,
//          IloTupleExprArg, IloTuplePattern
// ----------------------------------------------------------------------

#define DEFINE_EXPR_ACCESSORS(_exprType)\
   protected:\
   _exprType make(IloMapExtractIndexI* idx) const;\
   public:\
  _exprType subscriptOp(IloIntExprArg index) const{ return make( IloMapExtractIndexI::make(index) );}\
  _exprType subscriptOp(IloNumExprArg index) const{ return make( IloMapExtractIndexI::make(index) );}\
  _exprType subscriptOp(IloSymbolExprArg index) const{ return make( IloMapExtractIndexI::make(index) );}\
  _exprType subscriptOp(IloTupleExprArg index) const{ return make( IloMapExtractIndexI::make(index) );}



////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////
//
// Multidimensional Maps
//
////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////

/////////////////////////////////////
//
// MACROs for subscript operators
//
// ----------------------------------------------------------------------
// map[].elt(arg)
//     arg: IloInt, IloNum, IloSymbol, IloTuple, const char*
//          int, char short
// ----------------------------------------------------------------------
#define REDEFINE_ELT_MAP_ACCESSORS_BOUND(_eltType)\
  _eltType& eltAtAbsoluteIndex(IloInt i) const {\
      return (*static_cast<_eltType*>(&(super::eltAtAbsoluteIndex(i))));\
  } \
  _eltType& fastEltAtAbsoluteIndex(IloInt i) const {\
      return (*static_cast<_eltType*>(&(super::fastEltAtAbsoluteIndex(i))));\
  } \
   \
  _eltType getAt(IloMapIndexArray indices) const {\
    return _eltType(static_cast<name2(_eltType, I)*>(super::getAt(indices).getImpl()));\
  }\
   \
  const _eltType get(IloInt index) const{\
    return _eltType(static_cast<name2(_eltType, I)*>(super::get(index).getImpl()));\
  }\
   \
  const _eltType get(IloNum index) const{\
    return _eltType(static_cast<name2(_eltType, I)*>(super::get(index).getImpl()));\
  }\
   \
  const _eltType get(const char* index) const{\
    return _eltType(static_cast<name2(_eltType, I)*>(super::get(index).getImpl()));\
  }\
  const _eltType get(IloSymbol index) const{\
    return _eltType(static_cast<name2(_eltType, I)*>(super::get(index).getImpl()));\
  }\
   \
  const _eltType get(IloTuple index) const{\
    return _eltType(static_cast<name2(_eltType, I)*>(super::get(index).getImpl()));\
  }




#define REDEFINE_SIMPLE_ELT_MAP_ACCESSORS_BOUND(_eltType)\
   \
  const _eltType get(IloInt index) const{\
    return super::get(index);\
  }\
   \
  const _eltType get(IloNum index) const{\
    return super::get(index);\
  }\
   \
  const _eltType get(const char* index) const{\
    return super::get(index);\
  }\
  const _eltType get(IloSymbol index) const{\
    return super::get(index);\
  }\
   \
  const _eltType get(IloTuple index) const{\
    return super::get(index);\
  }



// ----------------------------------------------------------------------
// subMap map[arg]
//     arg: IloInt, IloNum, IloSymbol, IloTuple, const char*
//          int, char short
// ----------------------------------------------------------------------
#define REDEFINE_SUBMAP_ACCESSORS_BOUND(_subMapType)\
   \
  _subMapType getSub(IloNum index) const {\
    return _subMapType(super::getSub(index).getImpl());\
  }\
   \
  _subMapType getSub(IloInt index) const {\
    return _subMapType(super::getSub(index).getImpl());\
  }\
   \
  _subMapType getSub(IloTuple index) const {\
    return _subMapType(super::getSub(index).getImpl());\
  }\
   \
  _subMapType getSub(const char* index) const {\
    return _subMapType(super::getSub(index).getImpl());\
  }\
   \
  _subMapType operator[](IloInt index) const {\
    return _subMapType(super::operator[](index).getImpl());\
  }\
   \
  _subMapType operator[](IloNum index) const {\
    return _subMapType(super::operator[](index).getImpl());\
  }\
   \
  _subMapType operator[](const char* index) const {\
    return _subMapType(super::operator[](index).getImpl());\
  }\
   \
  _subMapType operator[](IloSymbol index) const {\
    return _subMapType(super::operator[](index).getImpl());\
  }\
   \
  _subMapType operator[](IloTuple index) const {\
    return _subMapType(super::operator[](index).getImpl());\
  }

#define REDEFINE_ACCESSORS_BOUND(_eltType, _subMapType)\
  REDEFINE_ELT_MAP_ACCESSORS_BOUND(_eltType)\
  REDEFINE_SUBMAP_ACCESSORS_BOUND(_subMapType)

#define REDEFINE_SIMPLE_ACCESSORS_BOUND(_eltType, _subMapType)\
  REDEFINE_SIMPLE_ELT_MAP_ACCESSORS_BOUND(_eltType)\
  REDEFINE_SUBMAP_ACCESSORS_BOUND(_subMapType)

/////////////////////////////////////
//
// IloMap - template part
//



template <class Elt> class IloMap  {
  typedef IloMapI ImplClass;
public:
  typedef IloMapI* IloSubMapI;
private:
  IloMapI* _impl;
  inline IloSubMapI* subArray(IloInt i) const {
    return &((IloSubMapI*)getImpl()->getValues()->getBase(i))[i&getImpl()->getValues()->getMod()];
  }
  inline Elt* data(IloInt i) const {
    return &((Elt*)getImpl()->getValues()->getBase(i))[i&getImpl()->getValues()->getMod()];
  }
  inline void setData(IloInt i, Elt value) {
    *data(i) = value;
  }
protected:
  Elt& at(IloInt index) const {
    IloAssert (_impl, "Empty Handle in IloMap::get/set/at");
    IloTestAndRaise(1==getNbDim(), "IloMap::get/set/at the map is not uni-dimensional.");
    IloInt i = _impl->getAbsoluteIndex(index);
    if (!(i>=0 && i<getSize()))
      throw IloMapOutOfBoundException(_impl, IloOplObject(index));
    return *data(i);
  }

  Elt& at(IloNum index) const {
    IloAssert (_impl, "Empty Handle in IloMap::get/set/at");
    IloTestAndRaise(1==getNbDim(), "IloMap::get/set/at the map is not uni-dimensional.");
    IloInt i = _impl->getAbsoluteIndex(index);
    if (!(i>=0 && i<_impl->getSize()))
      throw IloMapOutOfBoundException(_impl, IloOplObject(index));
    return *data(i);
  }

  Elt& at(const char* index) const {
    IloAssert (_impl, "Empty Handle in IloMap::get/set/at");
    IloTestAndRaise(1==getNbDim(), "IloMap::get/set/at the map is not uni-dimensional.");
    IloInt i = _impl->getAbsoluteIndex(this->getEnv().makeSymbol(index));
    if (!(i>=0 && i<_impl->getSize()))
      throw IloMapOutOfBoundException(_impl, IloOplObject(index));
    return *data(i);
  }

  Elt& at(IloTuple index) const {
    IloAssert (_impl, "Empty Handle in IloMap::get/set/at");
    IloTestAndRaise(1==getNbDim(), "IloMap::get/set/at the map is not uni-dimensional.");
    IloInt i = _impl->getAbsoluteIndex(index);
    if (!(i>=0 && i<_impl->getSize()))
      throw IloMapOutOfBoundException(_impl, IloOplObject(index));
    return *data(i);
  }

  Elt& at(IloSymbol index) const {
    IloAssert (_impl, "Empty Handle in IloMap::get/set/at");
    IloTestAndRaise(1==getNbDim(), "IloMap::get/set/at the map is not uni-dimensional.");
    IloInt i = _impl->getAbsoluteIndex(index);
    if (!(i>=0 && i<_impl->getSize()))
      throw IloMapOutOfBoundException(_impl, IloOplObject(index));
    return *data(i);
  }

  static IloSubMapI CreateData(IloEnvI* env,
			       IloBool validIdx,
			       IloInt currentDim,
			       IloMapIndexer indexer) {
    IloInt rootNbDim = indexer.getSize();
    IloDiscreteDataCollectionI* currIdx = indexer[currentDim].getImpl();
    IloInt size = currIdx->getSize();
    if (size <= 0) validIdx = IloFalse;
    if (!validIdx) size = 1;
    if (currentDim < rootNbDim-1) {
      IloArray<IloSubMapI> a(env, size);
      for (IloInt i=0; i<size; i++) {
	IloSubMapI elt = CreateData(env, validIdx, currentDim+1, indexer);
	a[i]= elt;
      }
      IloMapI* impl = new (env) IloMapI(env, rootNbDim-currentDim,
					currIdx, a.getImpl(), validIdx) ;
      impl->setType(IloOplObject::SubMap);
      return impl;
    } else {
      IloMap<Elt> a(env, currIdx, validIdx);
      return a.getImpl();
    }
  }

public:
  IloMap(IloMapI* impl = 0) : _impl(impl) {}
  IloMapI* getImpl() const { return _impl; }
#ifdef CPPREF_GENERATION
  
  IloMap(IloEnv env, IloDiscreteDataCollection indexer);
#endif
  IloMap(IloEnv env, IloDiscreteDataCollection indexer, IloBool validIdx = IloTrue) {
    _impl = new (env) IloMapI(env.getImpl(), 1,
			      indexer.getImpl(), (IloInt)sizeof(Elt), validIdx) ;
    _impl->setType(IloOplObject::GetType((Elt)0));
    IloInt size = _impl->getNbElt();
    for (IloInt i = 0; i<size; i++)
      setData(i, Elt());
  }
  IloMap(IloEnv env, IloMapIndexer indexer) {
    _impl = CreateData(env.getImpl(), IloTrue, 0, indexer);
  }


  void end() {
    IloAssert(_impl, "Empty Handle in IloMap::end");
    delete _impl;
    _impl = 0;
  }

  IloEnv getEnv() const {
    IloAssert(_impl, "Empty Handle in IloMap::getEnv");
    return _impl->getEnv();
  }

  
  IloInt getNbDim() const {
    IloAssert(_impl, "Empty Handle in IloMap::getNbDim");
    return _impl->getNbDim();
  }
  
  IloInt getSize() const {
    IloAssert(_impl, "Empty Handle in IloMap::getSize");
    return _impl->getSize();
  }
  
  IloInt getTotalSize() const {
    IloAssert(_impl, "Empty Handle in IloMap::getTotalSize");
    return _impl->getTotalSize();
  }
  
  IloDiscreteDataCollection getIndexer() const {
    IloAssert(_impl, "Empty Handle in IloMap::getIndexer");
    return IloDiscreteDataCollection(_impl->getIndexer());
  }

  
  IloDiscreteDataCollection getIndexer(IloInt i) const {
    IloAssert(_impl, "Empty Handle in IloMap::getIndexer");
    return IloDiscreteDataCollection(_impl->getIndexer(i));
  }

  
  IloMapIndexer makeMapIndexer() const {
    IloAssert(_impl, "Empty Handle in IloMap::getIndexer");
	return _impl->makeMapIndexer();
  }

  IloSubMapI fastGetAtAbsoluteIndex(IloInt i) const {
    return *subArray(i);
  }

  IloSubMapI getAtAbsoluteIndex(IloInt i) const {
    IloAssert (_impl, "Empty Handle in IloMap::getAtAbsoluteIndex");
    IloTestAndRaise(getNbDim()>1, "Wrong dimension");
    IloInt size = _impl->getValueSize();
    if (!(i>=0 && i<size))
		throw IloMapOutOfBoundException(_impl, getIndexer().getMapItem(i));
    IloSubMapI sub = *subArray(i);
    return sub;
  }

  
  IloMap<Elt> operator[](IloInt i) const {
    IloAssert (_impl, "Empty Handle in IloMap::operator[]");
    i = _impl->getAbsoluteIndex(i);
    if (!(i>=0 && i<getSize()))
      throw IloMapOutOfBoundException(_impl, getIndexer().getMapItem(i));
    IloMap<Elt> sub(this->getAtAbsoluteIndex(i));
    return sub;
  }

  
  IloMap<Elt> getSub(IloInt i) const {
    return this->operator[](i);
  }

  
  IloMap<Elt> getSub(IloNum n) const {
    IloAssert (_impl, "Empty Handle in IloMap::set");
    IloInt i = _impl->getAbsoluteIndex(n);
    IloMap<Elt> sub(this->getAtAbsoluteIndex(i));
    return sub;
  }

  
  IloMap<Elt> getSub(const char* a) const {
    IloAssert (_impl, "Empty Handle in IloMap::set");
    IloInt i = _impl->getAbsoluteIndex(this->getEnv().makeSymbol(a));
    IloMap<Elt> sub(this->getAtAbsoluteIndex(i));
    return sub;
  }

  
  IloMap<Elt> operator[](IloNum n) const {
    IloAssert (_impl, "Empty Handle in IloMap::operator[]");
    IloInt i = _impl->getAbsoluteIndex(n);
    IloMap<Elt> sub(this->getAtAbsoluteIndex(i));
    return sub;
  }
  
  IloMap<Elt> operator[](const char* a) const {
    IloAssert (_impl, "Empty Handle in IloMap::operator[]");
    IloInt i = _impl->getAbsoluteIndex(this->getEnv().makeSymbol(a));
    IloMap<Elt> sub(this->getAtAbsoluteIndex(i));
    return sub;
  }
  
  IloMap<Elt> operator[](IloSymbol s) const {
    IloAssert (_impl, "Empty Handle in IloMap::operator[]");
    IloInt i = _impl->getAbsoluteIndex(s);
    IloMap<Elt> sub(this->getAtAbsoluteIndex(i));
    return sub;
  }

  
  IloMap<Elt> getSub(IloTuple i) const {
    return this->operator[](i);
  }

  
  IloMap<Elt> operator[](IloTuple t) const {
    IloAssert (_impl, "Empty Handle in IloMap::operator[]");
    IloInt i = _impl->getAbsoluteIndex(t);
    IloMap<Elt> sub(this->getAtAbsoluteIndex(i));
    return sub;
  }

  
  const Elt get(IloInt idx) const{
    return this->at(idx);
  }
  
  const Elt get(IloNum idx) const{
    return this->at(idx);
  }
  
  const Elt get(const char* idx) const{
    return this->at(this->getEnv().makeSymbol(idx));
  }
  
  const Elt get(IloTuple idx) const{
    return this->at(idx);
  }
  
  const Elt get(IloSymbol idx) const{
    return this->at(idx);
  }

  
  void set(IloInt idx, Elt value) {
    this->at(idx) = value;
  }
  
  void set(IloNum idx, Elt value) {
    this->at(idx) = value;
  }
  
  void set(const char* idx, Elt value) {
    this->at(this->getEnv().makeSymbol(idx)) = value;
  }
  
  void set(IloTuple idx, Elt value) {
    this->at(idx) = value;
  }
  
  void set(IloSymbol idx, Elt value) {
    this->at(idx) = value;
  }

  Elt& fastEltAtAbsoluteIndex(IloInt i) const {
    return *data(i);
  }

  Elt& eltAtAbsoluteIndex(IloInt i) const {
    IloAssert(_impl, "Empty Handle in IloMap::eltAtAbsoluteIndex");
    IloTestAndRaise(1==getNbDim(), "IloMap::eltAtAbsoluteIndex the map is not uni-dimensional.");
    if (!(i>=0 && i<_impl->getValueSize()))
      throw IloMapOutOfBoundException(_impl, getIndexer().getMapItem(i));
    return *data(i);
  }


  
  void setAt(IloMapIndexArray indices, Elt value) {
    IloAssert (_impl, "Empty Handle in IloMap::setAt");
    _impl->setAt(indices, IloOplObject(value));
  }

  
  Elt getAt(IloMapIndexArray indices) const {
    IloAssert (_impl, "Empty Handle in IloMap::getAt");
    IloAssert(indices.isConstant(), "IloMap::getAt does not accept non constant indices.");
    IloOplObject item = _impl->getAt(indices);
    Elt res;
    item.getValue(res);
    return res;
  }
  IloMapI* copy() const {
    IloAssert (_impl, "Empty Handle in IloMap::copy");
    return _impl->getCopy();
  }

  
  const char * getName() const {
    if (getImpl() == 0)
      throw IloEmptyHandleException("IloMap::getName : Using empty handle");
    return getImpl()->getName();
  }
  
  void setName(const char * name) const {
    if (getImpl() == 0)
      throw IloEmptyHandleException("IloMap::setName : Using empty handle");
    getImpl()->setName(name);
  }

  class Iterator : public IloMapIterator {
  public:
    
    Iterator(const IloMapI* m)
      : IloMapIterator(m->getEnv()->getGeneralAllocator(), m) {
    }
    
    Iterator(const IloMap m)
      : IloMapIterator(m.getImpl()->getEnv()->getGeneralAllocator(),
		       m.getImpl()) {
    }

    
    Iterator(IloGenAlloc* heap, const IloMapI* m): IloMapIterator(heap, m) { }

    virtual ~Iterator(){}

    
    Elt& operator*() {
      IloArray<Elt> a(this->getCurrentEltArray());
      return a[this->getCurrentDeepestIndex()];
    }
  };

  Iterator* iterator(IloGenAlloc* heap) const {
    IloAssert(getImpl() != 0, "IloMap: Using empty handle");
	return new (getEnv()) Iterator(heap, _impl);

  }
  Iterator* iterator() const {
    IloAssert(getImpl() != 0, "IloMap: Using empty handle");
    return new (getEnv()) Iterator(_impl);
  }
};


template <class X>
inline ILOSTD(ostream)& operator<<(ILOSTD(ostream)& out, const IloMap<X>& m) {
  if (m.getImpl()) {
    IloInt nb = m.getNbDim();
    IloBool oneLine = (m.getTotalSize()<=15);
    if (nb>1) {
      IloMap<X> ma;
      out << '[';
      IloInt i, n=m.getSize()-1;
      for (i = 0; i < n; ++i) {
	ma = IloMap<X>(m.getAtAbsoluteIndex(i));
	out << ma << ',';
	if (oneLine) out << ' ';
	else         out << ILOSTD(endl);
      }
      if (n>=0) {
	ma = IloMap<X>(m.getAtAbsoluteIndex(i));
	out << ma;
      }
      out << ']';
      if (!oneLine)
	out << ILOSTD(endl);
    } else if (1==nb) {
      out << '[';
      IloInt i, n=m.getSize()-1;
      for (i = 0; i < n; ++i) {
	out << m.eltAtAbsoluteIndex(i);
	out << ',';
	if ( (i+1) % 10 ) out << ' ';
	else              out << ILOSTD(endl);
      }
      if (n>=0)
	out << m.eltAtAbsoluteIndex(n);
      out << ']';
    } else
      out << "[]";
  }
  return out;
}

#ifdef CPPREF_GENERATION
#define name2(x,y) x##y
#endif

#define ILOMAPCTORBASE(_typeMap, _base)\
public:\
  typedef _base super;\
  _typeMap(const _typeMap & cpy) : _base(cpy) {}\
  _typeMap(IloMapI* impl) : _base(impl) {}\
  _typeMap() : _base() {}\
private:

#define ILOMAPCTORDECL(_type,_base)\
  ILOMAPCTORBASE(name2(_type,Map),_base)\
public:\
                                       \
  name2(_type,Map)(IloEnv env, IloMapIndexer indexer);\
                                       \
  name2(_type,Map)(IloEnv env, IloDiscreteDataCollection indexer);\
private:


#define ILOMAPCTORIMPL_ALLTYPES(_type, _typeMap,_base, _item)\
  _typeMap::_typeMap(IloEnv env, IloMapIndexer indexer)\
                    : _base(env, indexer) {\
     getImpl()->setType(_item);\
  }\
  _typeMap::_typeMap(IloEnv env,\
		       IloDiscreteDataCollection indexer)\
                    : _base(env, indexer) {\
     getImpl()->setType(_item);\
  }


#define ILOMAPCTORIMPL(_type,_base,_item)\
ILOMAPCTORIMPL_ALLTYPES(_type, name2(_type,Map),_base,_item)

/////////////////////////////////////
//
// IloMap<base type>
//


class IloIntMap : public IloMap<IloInt> {
public:
  ILOMAPCTORDECL(IloInt, IloMap<IloInt>)
public:
  REDEFINE_SIMPLE_ACCESSORS_BOUND(IloInt, IloIntMap)
  DEFINE_EXPR_ACCESSORS(IloIntSubMapExpr)
  #ifdef CPPREF_GENERATION
  
  void set(const char* index, IloInt value);
  
   void set(IloInt index, IloInt value);
  
   void set(IloNum index, IloInt value);
  
  void set(IloTuple index, IloInt value);
  
  void set(IloSymbol index, IloInt value);
  #endif
};


class IloNumMap : public IloMap<IloNum> {
public:
  ILOMAPCTORDECL(IloNum, IloMap<IloNum>)
public:
  REDEFINE_SIMPLE_ACCESSORS_BOUND(IloNum, IloNumMap)
  DEFINE_EXPR_ACCESSORS(IloNumSubMapExpr)
  #ifdef CPPREF_GENERATION
  
  void set(const char* index, IloNum value);
  
   void set(IloInt index, IloNum value);
  
   void set(IloNum index, IloNum value);
  
  void set(IloTuple index, IloNum value);
  
  void set(IloSymbol index, IloNum value);
  #endif
  IloBool areElementsInteger() const;
  IloBool areElementsBoolean() const;
};


class IloSymbolMap : public IloMap<IloSymbol> {
public:
  ILOMAPCTORDECL(IloSymbol, IloMap<IloSymbol>)
public:
  REDEFINE_ACCESSORS_BOUND(IloSymbol, IloSymbolMap)
  DEFINE_EXPR_ACCESSORS(IloSymbolSubMapExpr)
  using IloMap<IloSymbol>::set;
  
  void set(IloInt index, const char* value) {
    set(index, getEnv().makeSymbol(value));
  }
  
  void set(IloNum index, const char* value) {
    set(index, getEnv().makeSymbol(value));
  }
  
  void set(const char* index, const char* value) {
    set(getEnv().makeSymbol(index), getEnv().makeSymbol(value));
  }
  
  void set(IloTuple index, const char* value) {
    set(index, getEnv().makeSymbol(value));
  }
  
  void set(IloSymbol idx, const char* value) {
    set(idx, getEnv().makeSymbol(value));
  }
  void init(IloSymbol elt){
    for (Iterator it(this->getImpl()); it.ok(); ++it) {
      *it = elt;
    }
  }
};


class IloAnyCollectionMap : public IloMap<IloAnyCollection> {
protected:
  ILOMAPCTORDECL(IloAnyCollection, IloMap<IloAnyCollection>)
public:
  REDEFINE_ACCESSORS_BOUND(IloAnyCollection, IloAnyCollectionMap)
  
  void endElements();
  void setAtAbsoluteIndex(IloInt idx, IloAnyCollection value);
  void setAt(IloMapIndexArray indices, IloAnyCollection value);
};


class IloSymbolSetMap : public IloAnyCollectionMap {
public:
  ILOMAPCTORDECL(IloSymbolSet, IloAnyCollectionMap)
public:
  REDEFINE_ACCESSORS_BOUND(IloSymbolSet, IloSymbolSetMap)
  DEFINE_EXPR_ACCESSORS(IloSymbolCollectionSubMapExpr)

  #ifdef CPPREF_GENERATION
  
  void set(const char* index, IloSymbolSet value);
  
   void set(IloInt index, IloSymbolSet value);
  
   void set(IloNum index, IloSymbolSet value);
  
  void set(IloTuple index, IloSymbolSet value);
  
  void set(IloSymbol index, IloSymbolSet value);
  #endif
  void initCollections();
};


class IloTupleSetMap : public IloAnyCollectionMap {
public:
  ILOMAPCTORBASE(IloTupleSetMap,IloAnyCollectionMap)
public:
   
  IloTupleSetMap(IloEnv env, IloTupleSchema schema, IloMapIndexer indexer);
public:
  REDEFINE_ACCESSORS_BOUND(IloTupleSet, IloTupleSetMap)
  DEFINE_EXPR_ACCESSORS(IloTupleSetSubMapExpr)
  
  IloTupleSchema getSchema() const {
    IloAssert(getImpl(), "IloTupleSetMap::getSchema using empty handle");
    return (IloTupleSchemaI*)getImpl()->getAuxData();
  }
  #ifdef CPPREF_GENERATION
  
  void set(const char* index, IloTupleSet value);
  
   void set(IloInt index, IloTupleSet value);
  
   void set(IloNum index, IloTupleSet value);
  
  void set(IloTuple index, IloTupleSet value);
  
  void set(IloSymbol index, IloTupleSet value);
  #endif
  void initCollections(IloDataCollection::SortSense sense);
};


class IloIntCollectionMap : public IloMap<IloIntCollection> {
public:
  ILOMAPCTORDECL(IloIntCollection, IloMap<IloIntCollection>)
public:
  REDEFINE_ACCESSORS_BOUND(IloIntCollection, IloMap<IloIntCollection>)
  DEFINE_EXPR_ACCESSORS(IloIntCollectionSubMapExpr)
  
  void endElements();
  void setAtAbsoluteIndex(IloInt idx, IloIntCollection value);
  void setAt(IloMapIndexArray indices, IloIntCollection value);
};


class IloIntSetMap : public IloIntCollectionMap {
public:
  ILOMAPCTORDECL(IloIntSet, IloIntCollectionMap)
public:
  REDEFINE_ACCESSORS_BOUND(IloIntSet, IloIntSetMap)
  DEFINE_EXPR_ACCESSORS(IloIntCollectionSubMapExpr)
  #ifdef CPPREF_GENERATION
  
  void set(const char* index, IloIntSet value);
  
   void set(IloInt index, IloIntSet value);
  
   void set(IloNum index, IloIntSet value);
  
  void set(IloTuple index, IloIntSet value);
  
  void set(IloSymbol index, IloIntSet value);
  #endif
  void initCollections(IloDataCollection::SortSense sense);
};


class IloNumCollectionMap : public IloMap<IloNumCollection> {
public:
  ILOMAPCTORDECL(IloNumCollection, IloMap<IloNumCollection>)
public:
  REDEFINE_ACCESSORS_BOUND(IloNumCollection, IloNumCollectionMap)
  DEFINE_EXPR_ACCESSORS(IloNumCollectionSubMapExpr)
  
  void endElements();
  void setAtAbsoluteIndex(IloInt idx, IloNumCollection value);
  void setAt(IloMapIndexArray indices, IloNumCollection value);
};


class IloNumSetMap : public IloNumCollectionMap {
public:
  ILOMAPCTORDECL(IloNumSet, IloNumCollectionMap)
public:
  REDEFINE_ACCESSORS_BOUND(IloNumSet, IloNumSetMap)
  DEFINE_EXPR_ACCESSORS(IloNumCollectionSubMapExpr)
  #ifdef CPPREF_GENERATION
  
  void set(const char* index, IloNumSet value);
  
   void set(IloInt index, IloNumSet value);
  
   void set(IloNum index, IloNumSet value);
  
  void set(IloTuple index, IloNumSet value);
  
  void set(IloSymbol index, IloNumSet value);
  #endif
  void initCollections(IloDataCollection::SortSense sense);
};


/////////////////////////////////////
//
// IloMap<extractable>
//
#define REDEFINEFROMSUPER_ALLTYPES(_eltType, _eltMap, _superMap)\
  ILOMAPCTORDECL(_eltType, _superMap)\
public:\
  REDEFINE_ACCESSORS_BOUND(_eltType, _eltMap)\
  _eltMap getClone(IloEnvI* env) const {\
    return _eltMap((_superMap::getClone(env)).getImpl());\
  }

#define REDEFINEFROMSUPER(_eltType, _superEltType)\
  REDEFINEFROMSUPER_ALLTYPES(_eltType,\
                             name2(_eltType, Map),\
                             name2(_superEltType, Map))

//typedef IloMap<IloExtractable> IloExtractableMapBase;

class IloExtractableMap : public IloMap<IloExtractable> {
private:
  IloSubMapI getClone(IloEnvI* env, IloInt dim, IloSubMapI srcI) const;
public:
  IloExtractableArray asNewExtractableArray() const;
  ILOMAPCTORDECL(IloExtractable, IloMap<IloExtractable>)
public:
  REDEFINE_ACCESSORS_BOUND(IloExtractable, IloExtractableMap)
  IloExtractableMap getClone(IloEnvI*) const;
  void replaceByClone(IloEnvI* env);
  
  void endElements();
  void visitSubExtractables(IloExtractableVisitor* v, IloExtractableI* e, IloMapI* m);
  void visitSubExtractables(IloExtractableVisitor* v, IloExtractableI* e){
	  IloAssert(getImpl(), "Empty Handle in IloExtractableMap::visitSubExtractables");
	  visitSubExtractables(v, e, getImpl());
  }
  void addSubExtractables(IloExtractableVisitor* v, IloExtractableI* p, IloExtractableMap m);
  void removeSubExtractables(IloExtractableVisitor* v, IloExtractableI* p, IloExtractableMap m);
};



class IloConstraintMap : public IloExtractableMap {
public:
// seems to be overlapping the REDEFINEFROMSUPER below ...
// does it break cpp doc if I comment it?
  REDEFINEFROMSUPER(IloConstraint, IloExtractable)
  
  IloConstraintArray asNewConstraintArray() const;
};


class IloIntVarMap : public IloExtractableMap {
public:
  REDEFINEFROMSUPER(IloIntVar, IloExtractable)
  DEFINE_EXPR_ACCESSORS(IloIntExprSubMapExpr)
  #ifdef CPPREF_GENERATION
  
  void set(const char* index, IloIntVar value);
  
   void set(IloInt index, IloIntVar value);
  
   void set(IloNum index, IloIntVar value);
  
  void set(IloTuple index, IloIntVar value);
  
  void set(IloSymbol index, IloIntVar value);
  #endif
  
  IloIntVarArray asNewIntVarArray() const;
};
//


class IloNumVarMap : public IloExtractableMap {
public:
  REDEFINEFROMSUPER(IloNumVar, IloExtractable)
  DEFINE_EXPR_ACCESSORS(IloNumExprSubMapExpr)
  #ifdef CPPREF_GENERATION
  
  void set(const char* index, IloNumVar value);
  
   void set(IloInt index, IloNumVar value);
  
   void set(IloNum index, IloNumVar value);
  
  void set(IloTuple index, IloNumVar value);
  
  void set(IloSymbol index, IloNumVar value);
  #endif
  
  IloNumVarArray asNewNumVarArray() const;
};


// OPL dexpr stuff

class IloTupleCellsDExprHash;
class IloTupleCellArrayI;


class IloIntExprMapLightI : public IloAbstractMapI {
	ILORTTIDECL
	IloIntExprI* _expr;
	IloMapIndexer _mapindexer;
	IloMapIndexArray _indexes;
	char* _name;
	IloMapIndexArray _shared;
	IloTupleCellsDExprHash* _hash;
	IloTupleCellArrayI* _sharedConstCells;
	//IloExtractableArray _array;
	IloIntLinTermI* _list;

public:
  IloIntExprMapLightI(IloEnvI* env, IloIntExprArg expr,
                      IloMapIndexer indexer, IloMapIndexArray indexes);
  virtual void copyContent(const IloAbstractMapI*);
  virtual ~IloIntExprMapLightI();
  using IloAbstractMapI::getIndexer;
  virtual IloDiscreteDataCollectionI* getIndexer(IloInt i) const;
  virtual const char* getName() const { return _name; }
  virtual void setName(const char* name);
  virtual IloInt getNbDim() const;
  virtual IloInt getTotalSize();
  virtual IloInt getSize() const;
  virtual IloOplObject getAt(IloMapIndexArray indices) const;
  virtual void setAt(IloMapIndexArray indices, IloOplObject value);
  virtual void setAtAbsoluteIndex(IloIntFixedArray indices, IloOplObject value);
  virtual IloOplObject getAtAbsoluteIndex(IloIntFixedArray indices) const;
  virtual void display(ILOSTD(ostream)& out) const;
  IloMapIndexArray getSharedIndex(IloIntFixedArray indices);
  IloIntExprArg getExpr() const { return _expr; };
  IloMapIndexer getOrMakeSharedMapIndexer() { return _mapindexer; }
  IloMapIndexArray getSharedUnaryMapIndexArray() { return _shared; }
};

class IloIntDExprI : public IloIntExprI{
	ILOEXTRDECL
		IloIntExprI* _expr;
	IloMapIndexArray _indexes;
	virtual void visitSubExtractables(IloExtractableVisitor* v);

public:
	IloIntDExprI(IloEnvI* env, IloIntExprI* expr, const char* name=0);
	IloIntDExprI(IloEnvI* env, IloIntExprI* expr, IloMapIndexArray indexes, const char* name=0);
	virtual ~IloIntDExprI();
	void display(ILOSTD(ostream)& out) const;
	virtual IloNum eval(const IloAlgorithm alg) const;
	virtual IloExtractableI* makeClone(IloEnvI*) const;
	IloIntExprI* getExpr() const { return _expr; }
	IloMapIndexArray getIndexes() const {return _indexes;}
};


class IloIntDExprMap {
  IloIntExprMapLightI* _impl;
public:
    IloIntDExprMap(IloIntExprMapLightI* impl):_impl(impl) {
    }
  
  IloIntDExprMap(IloEnv env, IloIntExprArg expr,
                      IloMapIndexer indexer, IloMapIndexArray indexes);
  
  IloIntExprMapLightI* getImpl() const {
    return _impl;
  }
  
  void end() {
    delete _impl;
    _impl = 0;
  }
 
  IloIntExprArg get(IloTuple index) const {
    IloAssert(_impl, "Using empty IloIntDExprMap handle");
    IloMapIndexArray a = _impl->getSharedUnaryMapIndexArray();
	a[0] = index;
    IloIntExprArg e = getAt(a);
    return e;
  }
 
  IloIntExprArg get(const char* index) const {
    IloAssert(_impl, "Using empty IloIntDExprMap handle");
    IloMapIndexArray a = _impl->getSharedUnaryMapIndexArray();
	a[0] = index;
    IloIntExprArg e = getAt(a);
    return e;
  }
 
  IloIntExprArg get(IloSymbol index) const {
    IloAssert(_impl, "Using empty IloIntDExprMap handle");
    IloMapIndexArray a = _impl->getSharedUnaryMapIndexArray();
	a[0] = index;
    IloIntExprArg e = getAt(a);
    return e;
  }
 
  IloIntExprArg get(IloNum index) const {
    IloAssert(_impl, "Using empty IloIntDExprMap handle");
    IloMapIndexArray a = _impl->getSharedUnaryMapIndexArray();
	a[0] = index;
    IloIntExprArg e = getAt(a);
    return e;
  }
 
  IloIntExprArg get(IloInt index) const {
    IloAssert(_impl, "Using empty IloIntDExprMap handle");
    IloMapIndexArray a = _impl->getSharedUnaryMapIndexArray();
	a[0] = index;
    IloIntExprArg e = getAt(a);
    return e;
  }
  IloIntExprArg getAt(IloMapIndexArray indices) const {
    IloAssert(_impl, "Using empty IloIntDExprMap handle");
    return _impl->getAt(indices).asIntExpr();
  }
 
  IloIntExprArg operator[](IloTuple index) const {
    return get(index);
  }
 
  IloIntExprArg operator[](const char* index) const {
    return get(index);
  }
 
  IloIntExprArg operator[](IloSymbol index) const {
    return get(index);
  }
 
  IloIntExprArg operator[](IloNum index) const {
    return get(index);
  }
 
  IloIntExprArg operator[](IloInt index) const {
    return get(index);
  }
};





class IloIntMapLightIterator {
protected:
  const IloIntExprMapLightI* _map;
  IloIntFixedArray _size;
  IloIntFixedArray _index;
  IloBool _ok;
  virtual IloBool next(IloInt);
  void resetIndex(IloInt i);
  void checkVectorIndex();
public:
	IloEnvI* getEnv() const{
		return _map->getEnv();
	}
  virtual IloBool next();
  virtual void reset();
  IloIntMapLightIterator(IloIntDExprMap m);
  IloIntMapLightIterator(const IloIntExprMapLightI* m);
  virtual ~IloIntMapLightIterator();
  const IloIntExprMapLightI* getIntMapI() const { return _map; }
  IloMapIndexArray getSharedIndex(){
	  return ((IloIntExprMapLightI*)getIntMapI())->getSharedIndex(_index);
  }

  IloMapIndexArray getSharedIndex(IloIntFixedArray indices){
	  return ((IloIntExprMapLightI*)getIntMapI())->getSharedIndex(indices);
  }

  
  IloBool ok() const {
	  return _ok;
  }
  //void setNotOk() { _ok = IloFalse; }

  
  void operator++() {
	  _ok = next();
  }

  void operator delete(void *p, size_t sz);
#ifdef ILODELETEOPERATOR
  void operator delete(void *p, const IloEnvI *);
  void operator delete(void *p, const IloEnv &);
#endif

  
  IloIntDExprI* operator*() {
	  IloOplObject item = _map->getAtAbsoluteIndex(_index);
	  return (IloIntDExprI*)item.asIntExpr().getImpl();
  }
};


class IloNumExprMapLightI : public IloAbstractMapI {
	ILORTTIDECL
	IloNumExprI* _expr;
	IloMapIndexer _mapindexer;
	IloMapIndexArray _indexes;
	IloMapIndexArray _shared;
	char* _name;
	IloTupleCellsDExprHash* _hash;
	IloTupleCellArrayI* _sharedConstCells;
	//IloExtractableArray _array;
	IloNumLinTermI* _list;

public:
  IloNumExprMapLightI(IloEnvI* env, IloNumExprArg expr,
                      IloMapIndexer indexer, IloMapIndexArray indexes);
  virtual void copyContent(const IloAbstractMapI*);
  virtual ~IloNumExprMapLightI();
  using IloAbstractMapI::getIndexer;
  virtual IloDiscreteDataCollectionI* getIndexer(IloInt i) const;
  virtual const char* getName() const { return _name; }
  virtual void setName(const char* name);
  virtual IloInt getNbDim() const;
  virtual IloInt getTotalSize();
  virtual IloInt getSize() const;
  virtual IloOplObject getAt(IloMapIndexArray indices) const;
  virtual void setAt(IloMapIndexArray indices, IloOplObject value);
  virtual void setAtAbsoluteIndex(IloIntFixedArray indices, IloOplObject value);
  virtual IloOplObject getAtAbsoluteIndex(IloIntFixedArray indices) const;
  virtual void display(ILOSTD(ostream)& out) const;
  IloMapIndexArray getSharedIndex(IloIntFixedArray indices);
  IloNumExprArg getExpr() const { return _expr; };
  IloMapIndexer getOrMakeSharedMapIndexer() { return _mapindexer; }
  IloMapIndexArray getSharedUnaryMapIndexArray() { return _shared; }
};

class IloNumDExprI : public IloNumExprI {
	ILOEXTRDECL
	IloNumExprI* _expr;
	IloMapIndexArray _indexes;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloNumDExprI(IloEnvI* env, IloNumExprI* expr, const char* name=0);
	IloNumDExprI(IloEnvI* env, IloNumExprI* expr, IloMapIndexArray indexes, const char* name=0);
	virtual ~IloNumDExprI();
	void display(ILOSTD(ostream)& out) const;
	virtual IloNum eval(const IloAlgorithm alg) const;
	virtual IloExtractableI* makeClone(IloEnvI*) const;
	IloNumExprI* getExpr() const { return _expr; }
	IloMapIndexArray getIndexes() const {return _indexes;}
};


class IloNumDExprMap {
  IloNumExprMapLightI* _impl;
public:
    IloNumDExprMap(IloNumExprMapLightI* impl):_impl(impl) {
    }
  
	IloNumDExprMap(IloEnv env, IloNumExprArg expr,
                      IloMapIndexer indexer, IloMapIndexArray indexes);

  
  IloNumExprMapLightI* getImpl() const {
    return _impl;
  }
  
  void end() {
    delete _impl;
    _impl = 0;
  }
 
  IloNumExprArg get(IloTuple index) const {
    IloAssert(_impl, "Using empty IloNumDExprMap handle");
    IloMapIndexArray a = _impl->getSharedUnaryMapIndexArray();
	a[0] = index;
    IloNumExprArg e = getAt(a);
    return e;
  }
 
  IloNumExprArg get(const char* index) const {
    IloAssert(_impl, "Using empty IloNumDExprMap handle");
    IloMapIndexArray a = _impl->getSharedUnaryMapIndexArray();
	a[0] = index;
    IloNumExprArg e = getAt(a);
    return e;
  }
 
  IloNumExprArg get(IloSymbol index) const {
    IloAssert(_impl, "Using empty IloNumDExprMap handle");
    IloMapIndexArray a = _impl->getSharedUnaryMapIndexArray();
	a[0] = index;
    IloNumExprArg e = getAt(a);
    return e;
  }
 
  IloNumExprArg get(IloNum index) const {
    IloAssert(_impl, "Using empty IloNumDExprMap handle");
    IloMapIndexArray a = _impl->getSharedUnaryMapIndexArray();
	a[0] = index;
    IloNumExprArg e = getAt(a);
    return e;
  }
 
  IloNumExprArg get(IloInt index) const {
    IloAssert(_impl, "Using empty IloNumDExprMap handle");
    IloMapIndexArray a = _impl->getSharedUnaryMapIndexArray();
	a[0] = index;
    IloNumExprArg e = getAt(a);
    return e;
  }
 
  IloNumExprArg getAt(IloMapIndexArray indices) const {
    IloAssert(_impl, "Using empty IloNumDExprMap handle");
    return _impl->getAt(indices).asNumExpr();
  }
 
  IloNumExprArg operator[](IloTuple index) const {
    return get(index);
  }
 
  IloNumExprArg operator[](const char* index) const {
    return get(index);
  }
 
  IloNumExprArg operator[](IloSymbol index) const {
    return get(index);
  }
 
  IloNumExprArg operator[](IloNum index) const {
    return get(index);
  }
 
  IloNumExprArg operator[](IloInt index) const {
    return get(index);
  }
};


//---------------------------------------------------------------------------
//   IloIntervalVarMap
//---------------------------------------------------------------------------
ILOSUBMAPHANDLE(IloIntervalVar, IloIntervalVar)


class IloIntervalVarMap : public IloExtractableMap {
private:
public:
  REDEFINEFROMSUPER(IloIntervalVar, IloExtractable)
  DEFINE_EXPR_ACCESSORS(IloIntervalVarSubMapExpr)
  
  IloIntervalVarArray asNewIntervalVarArray() const;
};


#define IloPiecewiseFunctionExprI IloAdvPiecewiseFunctionExprI
class IloPiecewiseFunctionExprSubMapExpr : public IloPiecewiseFunctionExprArg {
  ILOEXTR_RENAMED_HANDLE(IloPiecewiseFunctionExprSubMapExpr,
			 IloAdvPiecewiseFunctionExprSubMapExprI,
			 IloPiecewiseFunctionExprArg)
public:
  //IloPiecewiseFunctionExprSubMapExpr operator[](IloInt idx) const;
  //IloPiecewiseFunctionExprSubMapExpr operator[](IloNum idx) const;
  //IloPiecewiseFunctionExprSubMapExpr operator[](IloSymbol idx) const;
  //IloPiecewiseFunctionExprSubMapExpr operator[](IloTuple idx) const;
  IloPiecewiseFunctionExprSubMapExpr subscriptOp(IloIntExprArg idx) const;
  IloPiecewiseFunctionExprSubMapExpr subscriptOp(IloNumExprArg idx) const;
  IloPiecewiseFunctionExprSubMapExpr subscriptOp(IloSymbolExprArg idx) const;
  IloPiecewiseFunctionExprSubMapExpr subscriptOp(IloTupleExprArg idx) const;
};


class IloPiecewiseFunctionExprMap : public IloExtractableMap {
private:
public:
  REDEFINEFROMSUPER(IloPiecewiseFunctionExpr, IloExtractable)
  DEFINE_EXPR_ACCESSORS(IloPiecewiseFunctionExprSubMapExpr)

  void initCollections(IloEnvI* env, const IloBool isStepwise = IloFalse );
};

ILOSUBMAPHANDLE(IloIntervalSequenceVar, IloIntervalSequenceExprArg)


class IloIntervalSequenceVarMap : public IloExtractableMap {
private:
public:
  REDEFINEFROMSUPER(IloIntervalSequenceVar, IloExtractable)
  DEFINE_EXPR_ACCESSORS(IloIntervalSequenceVarSubMapExpr)
  
  IloIntervalSequenceVarArray asNewIntervalSequenceVarArray() const;
};

ILOSUBMAPHANDLE(IloCumulFunctionExpr, IloCumulFunctionExprArg)


class IloCumulFunctionExprMap : public IloExtractableMap {
private:
public:
  REDEFINEFROMSUPER(IloCumulFunctionExpr, IloExtractable)
  DEFINE_EXPR_ACCESSORS(IloCumulFunctionExprSubMapExpr)
  
  IloCumulFunctionExprArray asNewCumulFunctionExprArray() const;

  void initCollections(IloEnvI* env);
#ifdef FIX_OPL_5226
  void setAtAbsoluteIndex(IloInt idx, IloCumulFunctionExpr value);
  void setAt(IloMapIndexArray indices, IloCumulFunctionExpr value);
#endif
};

ILOSUBMAPHANDLE(IloStateFunctionExpr, IloStateFunctionExprArg)

class IloStateFunctionExprMap : public IloExtractableMap {
private:
public:
  REDEFINEFROMSUPER(IloStateFunctionExpr, IloExtractable)
  DEFINE_EXPR_ACCESSORS(IloStateFunctionExprSubMapExpr)
};


#undef IloPiecewiseFunctionExprI



class IloNumMapLightIterator {
protected:
  const IloNumExprMapLightI* _map;
  IloIntFixedArray _size;
  IloIntFixedArray _index;
  IloBool _ok;
  virtual IloBool next(IloInt);
  void resetIndex(IloInt i);
  void checkVectorIndex();
public:
	IloEnvI* getEnv() const{
		return _map->getEnv();
	}
  virtual IloBool next();
  virtual void reset();
  IloNumMapLightIterator(IloNumDExprMap m);
  IloNumMapLightIterator(const IloNumExprMapLightI* m);
  virtual ~IloNumMapLightIterator();
  const IloNumExprMapLightI* getNumMapI() const { return _map; }

  IloMapIndexArray getSharedIndex(){
	  return ((IloNumExprMapLightI*)getNumMapI())->getSharedIndex(_index);
  }

  IloMapIndexArray getSharedIndex(IloIntFixedArray indices){
	  return ((IloNumExprMapLightI*)getNumMapI())->getSharedIndex(indices);
  }

  
  IloBool ok() const {
    return _ok;
  }
  void setNotOk() { _ok = IloFalse; }

  
  void operator++() {
    _ok = next();
  }

  void operator delete(void *p, size_t sz);
#ifdef ILODELETEOPERATOR
  void operator delete(void *p, const IloEnvI *);
  void operator delete(void *p, const IloEnv &);
#endif

  
  IloNumDExprI* operator*() {
	  IloOplObject item = _map->getAtAbsoluteIndex(_index);
	  return (IloNumDExprI*)item.asNumExpr().getImpl();
  }
};

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif

