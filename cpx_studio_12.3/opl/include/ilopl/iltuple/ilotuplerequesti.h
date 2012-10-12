// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/iltuple/ilotuplerequesti.h
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

#ifndef __ADVANCED_ilotuplerequestiH
#define __ADVANCED_ilotuplerequestiH

#ifdef _WIN32
#pragma pack(push, 8)
#endif
#include <ilopl/ilosys.h>


#include <ilconcert/ilostring.h>
#include <ilopl/ilosymbol.h>
#include <ilopl/iltuple/ilotuplebuffer.h>
#include <ilopl/iltuple/ilorequestcache.h>


class IloTupleRequest;
class IloTupleRequestI;
class IloTupleSetI;
class IloTupleSet;
class IloTupleSchemaI;
class IloTupleSchema;
class IloTupleBuffer;
class IloTupleRequestChangeListenerI;


class IloFixedReqTypeArrayI : public IloEnvObjectI {
	IloInt _size;
	IloTuplePath::IloTupleRequestType* _values;
public:
	IloFixedReqTypeArrayI(IloEnv env, IloInt size) : IloEnvObjectI(env.getImpl()), _size(size), _values(0){
		if (_size) _values = (IloTuplePath::IloTupleRequestType*)getEnv()->alloc( sizeof(IloTuplePath::IloTupleRequestType)*size );
	}
	~IloFixedReqTypeArrayI(){
		getEnv()->free(_values, sizeof(IloTuplePath::IloTupleRequestType)*_size);
	}
	IloTuplePath::IloTupleRequestType operator[] (IloInt i) const {
		IloAssert ( i>=0, "Index out of bounds operation: negative index");
		IloAssert ( i < _size, "X& IloArray::operator[] (IloInt i) : Out of bounds operation: index superior to size of array");
		return _values[i];
	}
	void setValue(IloInt index, IloTuplePath::IloTupleRequestType value){
		IloAssert ( index>=0, "Index out of bounds operation: negative index");
		IloAssert ( index < _size, "X& IloArray::operator[] (IloInt i) : Out of bounds operation: index superior to size of array");
		_values[index] = value;
	}

	IloInt getSize() const { return _size;}
	IloFixedReqTypeArrayI* copy();
	IloTuplePath::IloTupleRequestType* getValues(){ return _values; }
};

class IloFixedReqTypeArray {
    IloFixedReqTypeArrayI* _impl;
public:
	IloFixedReqTypeArray(IloEnv env, IloInt size = 0) : _impl(new (env) IloFixedReqTypeArrayI(env, size)){};
	IloFixedReqTypeArray() : _impl(0){};
	IloFixedReqTypeArray(IloFixedReqTypeArrayI* impl) : _impl(impl){};
	IloFixedReqTypeArrayI* getImpl() const{
		return _impl;
	}
	IloTuplePath::IloTupleRequestType operator[] (IloInt i) const {
		return _impl->operator[](i);
	}

	void end(){
		if (_impl) delete _impl;
		_impl = 0;
	}
	IloInt getSize() const { return _impl->getSize(); }
	IloFixedReqTypeArrayI* copy() const{
		return _impl->copy();
	}
	IloEnv getEnv() const{ return _impl->getEnv(); }
	void setValue(IloInt index, IloTuplePath::IloTupleRequestType value){
		_impl->setValue(index, value);
	}
};


class IloRequestSignatureI : public IloEnvObjectI {
private:
	IloIntFixedArray _signature;
    IloFixedReqTypeArray _reqTypes;
public:
	
	IloInt getSize() const { return _signature.getSize(); }
	IloIntFixedArray getArray() const { return _signature; }
	
	IloFixedReqTypeArray getReqTypes() const { return _reqTypes; }
	
	IloRequestSignatureI(IloTupleRequestI* req);
	~IloRequestSignatureI();
	
	IloBool equal(IloRequestSignatureI* sign) const;
};



class IloTupleRequestI : public IloEnvObjectI {
	friend class IloTupleRequestChangeListenerI;
private:
	enum RequestType{
		IntRequest,
		NumRequest,
		AnyRequest,
		IntAnyRequest,
		MixedRequest,
		Undefined
	};
	enum Check{
		ToBeChecked = -1,
		CheckedTrue = 0,
		CheckedInverseTrue = 1,
		CheckedFalse = 2
	};
	RequestType _reqType;
	Check  _findRequest;
	Check  _subTupleRequest;
	Check _containsAllKeys;
	Check _isOrd;
	IloBool _isSingleColumn;
	IloBool _mustClear;
	IloTupleSchemaI* _schema;
	IloTupleSetI* _coll;
	IloTupleRequestCacheI* _cache;
	IloTupleRequestUnaryCacheI* _unaryCache;
	IloTupleRequestI* _subRequest;
	IloTupleRequestI* _keyRequest;
	IloTupleRequestI* _nonKeyRequest;
	IloTupleRequestChangeListenerI* _changeListener;
	IloTuplePathList _list;
public:
	void  setMustClear(IloBool flag) { _mustClear = flag; }
	IloBool mustClear() const { return _mustClear; }
	void checkRequest();
	RequestType getRequestType() const { return _reqType; }
	void setRequestType(RequestType type){ _reqType = type; }
	void setRequestType(IloTupleRequestI* req){
		if (_reqType == Undefined || _reqType == MixedRequest) return;
		req->setRequestType(_reqType);
	}
	IloInt getColumnIndex(const char* name) const;
	IloIntArray makeColumnIndexArray(IloStringArray name) const;

	IloTuplePath* addWithoutCopy(IloIntArray path, IloInt value, IloTuplePath::IloTupleRequestType = IloTuplePath::Equality);
	IloTuplePath* addWithoutCopy(IloIntArray path, IloNum value, IloTuplePath::IloTupleRequestType = IloTuplePath::Equality);
	IloTuplePath* addWithoutCopy(IloIntArray path, IloAny value);

	IloBool contains(IloInt size, IloInt* path, IloInt value);
	IloBool contains(IloInt size, IloInt* path, IloNum value);
	IloBool contains(IloInt size, IloInt* path, IloAny value);

	void buildIntCaches();
	void buildNumCaches();
	void buildAnyCaches();
	void buildCommonCaches();

	void buildCache();
private:
	IloMultipleIntHashTable* createIntCacheHashTable(IloInt size);
	IloMultipleNumHashTable* createNumCacheHashTable(IloInt size);
	IloMultipleAnyHashTable* createAnyCacheHashTable(IloInt size);
	IloMultipleUnaryIntHashTable* createIntCacheUnaryHashTable(IloInt size);
	IloMultipleUnaryNumHashTable* createNumCacheUnaryHashTable(IloInt size);
	IloMultipleUnaryAnyHashTable* createAnyCacheUnaryHashTable(IloInt size);
protected:

	friend class IloTupleIterator;
	void setCollection(IloTupleSetI* coll) {
		if (_coll && _coll!=coll){
			this->deleteAllCaches();
		}
		if (_subRequest) _subRequest->setCollection(coll);
		if (_keyRequest) _keyRequest->setCollection(coll);
		if (_nonKeyRequest) _nonKeyRequest->setCollection(coll);
		_coll = coll;
	}
	void deleteAllCaches();

public:
	IloTupleSchemaI* getSchema() const{
		return _schema;
	}
	IloInt getInternalId(IloIntArray path) const;
	void checkIsFindRequest();
	void checkIsOrd();
	IloBool isOrd() const {
		return (_isOrd == CheckedTrue) ? IloTrue : IloFalse;
	}
	IloBool isInverseOrd() const {
		return (_isOrd == CheckedInverseTrue) ? IloTrue : IloFalse;
	}
	void  setIsOrd(Check flag) { _isOrd = flag; }
	IloBool isFindRequest() const {
		return (_findRequest == CheckedTrue) ? IloTrue : IloFalse;
	}
	void checkIsSubTupleRequest();
	IloBool isSubTupleRequest() const {
		return (_subTupleRequest == CheckedTrue) ? IloTrue : IloFalse;
	}

	void checkContainsAllKeys();
	IloBool containsAllKeys() const {
		return (_containsAllKeys == CheckedTrue) ? IloTrue : IloFalse;
	}
	void setContainsAllKeys(Check flag){ _containsAllKeys = flag;}

	IloTupleRequestI* getOrMakeKeyRequest();
	IloTupleRequestI* getOrMakeNonKeyRequest();

	IloTupleRequestI* getOrMakeSubRequest();
	
	void createSelectIndexes();
	
	void clearSelectIndexes();

	
	IloTuplePathList& getList();

	
	void clear();
	
	void import(IloTupleRequestI* select);


	
	IloTuplePath* add(IloIntArray path, IloInt value, IloTuplePath::IloTupleRequestType = IloTuplePath::Equality);
	
	IloTuplePath* add(IloIntArray path, IloNum value, IloTuplePath::IloTupleRequestType = IloTuplePath::Equality);
	
	IloTuplePath* add(IloIntArray path, IloAny value);
	
	IloTuplePath* add(IloIntArray path, IloSymbol value);

	
	IloTuplePath* add(IloIntArray path, IloIntCollectionI* value);
	
	IloTuplePath* add(IloIntArray path, IloNumCollectionI* value);
	
	IloTuplePath* add(IloIntArray path, IloAnyCollectionI* value);

	
	IloTuplePath* add(IloInt size, IloInt* path, IloInt value, IloTuplePath::IloTupleRequestType = IloTuplePath::Equality);
	
	IloTuplePath* add(IloInt size, IloInt* path, IloNum value, IloTuplePath::IloTupleRequestType = IloTuplePath::Equality);
	
	IloTuplePath* add(IloInt size, IloInt* path, IloAny value);
	
	IloTuplePath* add(IloInt size, IloInt* path, IloSymbol value);
	
	IloTuplePath* add(IloInt size, IloInt* path, IloIntCollectionI* value);
	
	IloTuplePath* add(IloInt size, IloInt* path, IloNumCollectionI* value);
	
	IloTuplePath* add(IloInt size, IloInt* path, IloAnyCollectionI* value);

	
	IloTuplePathArray addRecursive(IloTupleSchemaI* schema, IloIntArray path);
	IloTuplePathArray addRecursive(IloTupleSchemaI* schema, IloInt size, IloInt* path);

	
	IloInt getSize() const;

	
	IloTupleSetI* getCollection() const { return _coll; }
	virtual ~IloTupleRequestI();

	//IloBool isEmpty() const { return _list.getSize() == 0; }
	
	IloBool isSingleColumn() const { return _isSingleColumn; }

	
	IloBool isIntQuery() const;
	
	IloBool isNumQuery() const;
	
	IloBool isAnyQuery() const;
	IloBool isIntAnyQuery() const;

	
	IloTuplePath* getFirst() const { return _list.firstObject(); }

	
	IloBool storeCache(IloIntArray slice);
	
	IloIntArrayI* getCache() {
		return ((_cache == 0) ? (IloIntArrayI*)0 : _cache->execute());
	}

	IloInt getUnaryCache() {
		return ((_unaryCache == 0) ? (IloInt)-1 : _unaryCache->execute());
	}
	
	IloTupleRequestI(IloEnv env, IloTupleSetI* coll);
	
	IloTupleRequestI(IloEnv env, IloTupleSchemaI* schema);

	
	virtual void display(ILOSTD(ostream)& os) const;
	
	IloBool contains(IloInt value);
	
	IloBool contains(IloNum value);
	
	IloBool contains(IloAny value);
};

#ifdef _WIN32
#pragma pack(pop)
#endif


#endif

