// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/iloenv.h
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


#ifndef __ADVANCED_iloenvH
#define __ADVANCED_iloenvH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>
#include <ilconcert/iloenv.h>

#ifdef ILO_LINUX
#include <cstring>
#endif

#define IloSymbolSetI IloAnySetI

class IloDataCollectionI;
class IloEndCollectionCallbackI : public IloDestroyableI {
	ILORTTIDECL
public:
	IloEndCollectionCallbackI(IloEnvI* env) : IloDestroyableI(env) {}
	virtual ~IloEndCollectionCallbackI(){}

	virtual void shareCollection(IloRttiEnvObjectI* coll) = 0;
	virtual void endCollection(IloRttiEnvObjectI*) = 0;
	virtual IloBool isSharing(IloRttiEnvObjectI*) const = 0;

	static void Register(IloEnvI* env, IloEndCollectionCallbackI* cb);
	static void Unregister(IloEnvI* env, IloEndCollectionCallbackI* cb);
	static IloBool HasInstance(IloEnvI* env);
	static IloEndCollectionCallbackI* GetInstance(IloEnvI* env);
};


class IloIntFixedArrayI : public IloEnvObjectI {
	IloInt _size;
	IloInt* _values;
public:
	IloIntFixedArrayI(IloIntFixedArrayI* toCopy) : IloEnvObjectI(toCopy->getEnv()), _size(toCopy->getSize()), _values((IloInt*)getEnv()->alloc( sizeof(IloInt)*toCopy->getSize() )){
		memcpy((void*)_values, (const void*)toCopy->getValues(), _size*sizeof(IloInt));
	}

	IloIntFixedArrayI(IloEnv env, IloInt size) : IloEnvObjectI(env.getImpl()), _size(size), _values(0){
		if (_size) _values = (IloInt*)getEnv()->alloc( sizeof(IloInt)*size );
	}
	~IloIntFixedArrayI(){
		getEnv()->free(_values, sizeof(IloInt)*_size);
		_values = 0;
		_size = 0;
	}
	IloInt operator[] (IloInt i) const{
		IloAssert ( i>=0, "Index out of bounds operation: negative index");
		IloAssert ( i < _size, "X& IloArray::operator[] (IloInt i) : Out of bounds operation: index superior to size of array");
		return _values[i];
	}
	IloInt& operator[] (IloInt i){
		IloAssert ( i>=0, "Index out of bounds operation: negative index");
		IloAssert ( i < _size, "X& IloArray::operator[] (IloInt i) : Out of bounds operation: index superior to size of array");
		return _values[i];
	}
	void setValue(IloInt index, IloInt value){
		IloAssert ( index>=0, "Index out of bounds operation: negative index");
		IloAssert ( index < _size, "X& IloArray::operator[] (IloInt i) : Out of bounds operation: index superior to size of array");
		_values[index] = value;
	}
	IloInt getSize() const { return _size;}
	IloIntFixedArrayI* copy(){
		return new (getEnv()) IloIntFixedArrayI(this);
	}
	IloInt* getValues(){ return _values; }

	void zeroData(){
		memset(_values, 0, sizeof(IloInt)*_size);
	}
	void add(IloInt i){
		IloInt* temp = (IloInt*)getEnv()->alloc( sizeof(IloInt)*(_size+1) );
		for (IloInt j=0; j< _size; j++){
			IloInt cell = _values[j];
			temp[j] = cell;
		}
		temp[_size] = i;
		getEnv()->free(_values, sizeof(IloInt)*_size);
		_values = temp;
		_size++;
	}
	void add(IloIntFixedArrayI* array){
		IloInt* temp = (IloInt*)getEnv()->alloc( sizeof(IloInt)*(_size+ array->getSize()) );
		for (IloInt j=0; j< _size; j++){
			IloInt cell = _values[j];
			temp[j] = cell;
		}
		for (IloInt i=0; i< array->getSize(); i++){
			IloInt cell = array->getValues()[i];
			temp[_size+i] = cell;
		}
		getEnv()->free(_values, sizeof(IloInt)*_size);
		_values = temp;
		_size+=array->getSize();
	}
};

class IloIntFixedArray {
    IloIntFixedArrayI* _impl;
public:
	IloIntFixedArray(IloEnv env, IloInt size = 0) : _impl(new (env) IloIntFixedArrayI(env, size)){};
	IloIntFixedArray() : _impl(0){};
	IloIntFixedArray(IloIntFixedArrayI* impl) : _impl(impl){};
	IloIntFixedArrayI* getImpl() const{
		return _impl;
	}
	IloInt operator[] (IloInt i) const {
		return _impl->operator[](i);
	}
	IloInt& operator[] (IloInt i){
		return _impl->operator[](i);
	}
	void setValue(IloInt index, IloInt value){
		_impl->setValue(index, value);
	}

	void end(){
		if (_impl) delete _impl;
		_impl = 0;
	}
	IloInt getSize() const { return _impl->getSize(); }
	IloIntFixedArrayI* copy() const{
		return _impl->copy();
	}
	IloEnv getEnv() const{ return _impl->getEnv(); }
	void add(IloInt i){ _impl->add(i); }
	void add(IloIntFixedArray i){ _impl->add(i.getImpl()); }
};


#if defined(ILOUSESTL) && !defined(ILO_HP)
class IloOplStringHelper {
public:
    IloOplStringHelper() {
    }
    virtual ~IloOplStringHelper() {
    }
    void printEscaped(std::ostream& os, char c) const;
    virtual void printEscaped(std::ostream& os, const char* chunk) const;
};

class IloOplMultiByteHelper: public IloOplStringHelper {
    int _mbCurMax;
public:
    static IloOplMultiByteHelper* NewOrNull(IloEnvI* env);

    explicit IloOplMultiByteHelper(size_t _mbCurMax);
    int getMax() const {
        return _mbCurMax;
    }

    int lastCharOffset(const char* chunk, int len) const;
    int lastCharOffsetAndFixColumn(const char* chunk, int len, int& col) const;
    int charOffset(int charIndex, const char* chunk, int len) const;
    bool isMB(const char* chunk) const;
    int countChars(const char* chunk, int len =-1) const;
    bool hasMB(const char* chunk) const;
	using IloOplStringHelper::printEscaped;
    virtual void printEscaped(std::ostream& os, const char* chunk) const;
};
#endif


IloIntArray intersectAscSortedIndex(IloEnv env, IloIntArray set1, IloIntArray set2);
IloNumArray intersectAscSortedIndex(IloEnv env, IloNumArray set1, IloNumArray set2);
IloAnyArray intersectAscSortedIndex(IloEnv env, IloAnyArray set1, IloAnyArray set2);

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
