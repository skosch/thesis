// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/iltuple/ilotuplebuffer.h
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



#ifndef __ADVANCED_ilotuplebufferH
#define __ADVANCED_ilotuplebufferH

#ifdef _WIN32
#pragma pack(push, 8)
#endif
#include <ilopl/ilosys.h>


#include <ilconcert/ilocollection.h>
#include <ilconcert/ilolinkedlist.h>

class IloTupleI;
class IloTupleCollectionI;
class IloTupleSchemaI;


class IloTuplePath : public IloEnvObjectI {
public:
	
	class IloTupleCell {
	public:
		union Value{
			IloInt _int;
			IloNum _num;
			IloAny _any;
		};
	private:
		IloDataCollection::IloDataType _type;
		Value _value;
	public:
		IloDataCollection::IloDataType getType() const { return _type; }
		Value getValue() const { return _value; }
		
		IloInt* getIntAddress() { return &(_value._int); }
		
		IloNum* getNumAddress() { return &(_value._num); }
		
		IloAny* getAnyAddress() { return &(_value._any); }

		
		IloBool isInt() const{ return (_type == IloDataCollection::IntDataColumn);	}
		
		IloBool isNum() const{ return (_type == IloDataCollection::NumDataColumn);	}
		
		IloBool isAny() const{ return (_type == IloDataCollection::AnyDataColumn); }

#ifdef ILO_WIN64
		IloInt getIntValue() const;
		
		IloNum getNumValue() const;
		
		IloAny getAnyValue() const;
#else
		
		IloInt getIntValue() const{
			if (this->isInt())
				return _value._int;
			else if (this->isNum() && IloNumIsInteger(_value._num))
				return IloNumToInt(_value._num);
			throw IloWrongUsage("IloTuplePath::IloTupleCell::getIntValue() type not int nor num");
			ILOUNREACHABLE(return 0;)
		}
		
		IloNum getNumValue() const{
			if (this->isNum())
				return _value._num;
			else if (this->isInt())
				return (IloNum)_value._int;
			throw IloWrongUsage("IloTuplePath::IloTupleCell::getNumValue() type not num nor int");
			ILOUNREACHABLE(return 0;)
		}
		
		IloAny getAnyValue() const{
			return _value._any;
		}
#endif

		
		IloTupleCell(IloInt val) {
			_type = IloDataCollection::IntDataColumn;
			_value._int = val;
		}
		
		IloTupleCell(IloNum val){
			_type = IloDataCollection::NumDataColumn;
			_value._num = val;
		}
		
		IloTupleCell(IloAny val){
			_type = IloDataCollection::AnyDataColumn;
			_value._any = val;
		}
		
		void setValue(IloInt val){
			_type = IloDataCollection::IntDataColumn;
			_value._int = val;
		}
		
		void setValue(IloNum val){
			_type = IloDataCollection::NumDataColumn;
			_value._num = val;
		}
		
		void setValue(IloAny val){
			_type = IloDataCollection::AnyDataColumn;
			_value._any = val;
		}
		~IloTupleCell(){}

		IloBool operator!=(const IloTupleCell& other) {
			if (_type != other.getType()) return IloTrue;
			switch(_type){
			case IloDataCollection::IntDataColumn:
				return _value._int != other._value._int;
			case IloDataCollection::NumDataColumn:
				return _value._num != other._value._num;
			default:
				return _value._any != other._value._any;
			}
		}
	};
public:
  enum IloTupleRequestType {
    Equality = 0,
    LowerBound = 32,
    UpperBound = 64
  };

private:
	IloTupleCell _cell;
	IloIntArray _path;
	IloTupleRequestType _reqType;
public:
		
	IloTupleCell& getCell() { return _cell; }
		
	IloIntArray getPath() const { return _path; }
public:
	~IloTuplePath(){ _path.end(); }
	void setValue(IloTupleCell cell){ _cell = cell;}
		
	void setValue(IloInt value){ getCell().setValue(value);	}
		
	void setValue(IloNum value){ getCell().setValue(value);	}
		
	void setValue(IloAny value){ getCell().setValue(value);	}
		
	void setValue(IloDiscreteDataCollection value){ getCell().setValue((IloAny)value.getImpl()); }
		
	IloTupleRequestType getRequestType() const {
		return _reqType;
	}
		
	IloBool isEquality() const {
		return _reqType==Equality;
	}
		
	IloBool isLowerBound() const {
		return _reqType==LowerBound;
	}
		
	IloBool isUpperBound() const {
		return _reqType==UpperBound;
	}

	IloTuplePath(IloIntArray path, IloTupleCell cell, IloTupleRequestType reqType = Equality):
	IloEnvObjectI(path.getEnv().getImpl()), _cell(cell), _path(path), _reqType(reqType) {
	}
		
	IloTuplePath(IloEnv env, IloIntArray path, IloAny value, IloTupleRequestType reqType = Equality):
	IloEnvObjectI(env.getImpl()), _cell(value), _path(path), _reqType(reqType) {
		if (!path.getImpl()) throw IloEmptyHandleException("IloTuplePath: path is empty");
	}
		
	IloTuplePath(IloEnv env, IloIntArray path, IloNum value, IloTupleRequestType reqType = Equality):
	IloEnvObjectI(env.getImpl()), _cell(value), _path(path), _reqType(reqType) {
		if (!path.getImpl()) throw IloEmptyHandleException( "IloTuplePath: path is empty");
	}
		
	IloTuplePath(IloEnv env, IloIntArray path, IloInt value, IloTupleRequestType reqType = Equality):
	IloEnvObjectI(env.getImpl()), _cell(value), _path(path), _reqType(reqType) {
		if (!path.getImpl()) throw IloEmptyHandleException( "IloTuplePath: path is empty");
	}
};

typedef IloArray<IloTuplePath*> IloTuplePathArray;

class IloTupleCellArrayI : public IloEnvObjectI {
	IloInt _size;
	IloTuplePath::IloTupleCell* _values;
public:
	IloTupleCellArrayI(IloEnvI* env, IloTupleCellArrayI* toCopy) : IloEnvObjectI(env), _size(toCopy->getSize()), _values((IloTuplePath::IloTupleCell*)env->alloc( sizeof(IloTuplePath::IloTupleCell)*toCopy->getSize())){
		memcpy((void*)_values, (const void*)toCopy->getValues(), _size*sizeof(IloTuplePath::IloTupleCell));
	}
	IloTupleCellArrayI(IloEnv env, IloInt size) : IloEnvObjectI(env.getImpl()), _size(size), _values(0){
		if (_size) {
			_values = (IloTuplePath::IloTupleCell*)getEnv()->alloc( sizeof(IloTuplePath::IloTupleCell)*size );
			memset(_values, 0, sizeof(IloTuplePath::IloTupleCell)*size);
		}
	}
	~IloTupleCellArrayI(){
		getEnv()->free(_values, sizeof(IloTuplePath::IloTupleCell)*_size);
		_values = 0;
		_size = 0;
	}
	const IloTuplePath::IloTupleCell& operator[] (IloInt i) const{
		IloAssert ( i>=0, "Index out of bounds operation: negative index");
		IloAssert ( i < _size, "X& IloArray::operator[] (IloInt i) : Out of bounds operation: index superior to size of array");
		return _values[i];
	}
	IloTuplePath::IloTupleCell& operator[] (IloInt i) {
		IloAssert ( i>=0, "Index out of bounds operation: negative index");
		IloAssert ( i < _size, "X& IloArray::operator[] (IloInt i) : Out of bounds operation: index superior to size of array");
		return _values[i];
	}
	inline void add(IloTuplePath::IloTupleCell i){
		IloInt ss = sizeof(IloTuplePath::IloTupleCell);
		IloInt oldsize = ss*_size;
		IloTuplePath::IloTupleCell* temp = (IloTuplePath::IloTupleCell*)getEnv()->alloc( ss+oldsize );
		memcpy(temp, _values, oldsize);
		temp[_size] = i;
		getEnv()->free(_values, ss*_size);
		_values = temp;
		_size++;
	}
	void add(IloTupleCellArrayI* array){
		IloInt ss = sizeof(IloTuplePath::IloTupleCell);
		IloInt oldsize = ss*_size;
		IloTuplePath::IloTupleCell* temp = (IloTuplePath::IloTupleCell*)getEnv()->alloc( ss*array->getSize()+oldsize );
		memcpy(temp, _values, oldsize);
		for (IloInt i=0; i< array->getSize(); i++){
			IloTuplePath::IloTupleCell cell = array->getValues()[i];
			temp[_size+i] = cell;
		}
		getEnv()->free(_values, oldsize);
		_values = temp;
		_size+=array->getSize();
	}
	inline IloInt getSize() const {
		return _size;
	}
	IloTupleCellArrayI* copy(){
		return new (getEnv()) IloTupleCellArrayI(getEnv(), this);
	}
	IloTupleCellArrayI* makeClone(IloEnvI* env){
		return new (env) IloTupleCellArrayI(env, this);
	}
	void setSize(IloInt size) {
		IloInt ss = sizeof(IloTuplePath::IloTupleCell);
		IloInt oldsize = ss*_size;
		IloInt newsize = ss*size;
		IloTuplePath::IloTupleCell* temp = (IloTuplePath::IloTupleCell*)getEnv()->alloc( newsize );
		if (size > _size)
			memcpy(temp, _values, oldsize);
		else
			memcpy(temp, _values, newsize);
		getEnv()->free(_values,oldsize);
		_values = temp;
		_size = size;
    }
    void clear() {
		getEnv()->free(_values, sizeof(IloTuplePath::IloTupleCell)*_size);
		_values = 0;
		_size = 0;
    }
	IloTuplePath::IloTupleCell* getValues(){ return _values; }
};

class IloTupleCellArray {
    IloTupleCellArrayI* _impl;
public:
	IloTupleCellArray(IloEnv env, IloInt size = 0) : _impl(new (env) IloTupleCellArrayI(env, size)){};
	IloTupleCellArray() : _impl(0){};
	IloTupleCellArray(IloTupleCellArrayI* impl) : _impl(impl){};
	IloTupleCellArrayI* getImpl() const{
		return _impl;
	}
	const IloTuplePath::IloTupleCell& operator[] (IloInt i) const {
		return _impl->operator[](i);
	}
	IloTuplePath::IloTupleCell& operator[] (IloInt i) {
		return _impl->operator[](i);
	}

	void add(IloTuplePath::IloTupleCell i){
		_impl->add(i);
	}
	void add(IloTupleCellArray array){
		_impl->add(array.getImpl());
	}
	void end(){
		if (_impl) delete _impl;
		_impl = 0;
	}
	IloInt getSize() const { return _impl->getSize(); }

	IloBool isHomogeneous() const;
	void convertTo(const IloTupleCollectionI* set, const IloTupleSchemaI* schema =0);
	void display(ILOSTD(ostream)& os) const;
	IloTupleCellArray makeSlice(IloInt index, IloInt size);
	IloTupleCellArray fillSlice(IloTupleCellArray array, IloInt index, IloInt size);
	void endAll();
	IloTupleCellArrayI* copy() const{
		return _impl->copy();
	}
	IloTupleCellArrayI* makeClone(IloEnvI* env) const{
		return _impl->makeClone(env);
	}
	IloEnv getEnv() const{ return _impl->getEnv(); }
    void setSize(IloInt size) {
		_impl->setSize(size);
    }
    void clear() {
		_impl->clear();
	}
};

IloBool operator==(const IloTupleCellArray cells1, const IloTupleCellArray cells2);


typedef IloSimpleLinkedList<IloTuplePath*> IloTuplePathListBase;


class IloTuplePathList : public IloSimpleLinkedList<IloTuplePath*> {
public:
		
	using IloSimpleLinkedList<IloTuplePath*>::add;
		
	IloTuplePathList(IloEnv env) : IloTuplePathListBase(env)  {}
	virtual ~IloTuplePathList();
};


//-------------------------------------


class IloTuplePathBufferI : public IloEnvObjectI{
	friend class IloTupleBufferI;
public:
	IloTuplePath* add(IloIntArray path, IloInt value);
	IloTuplePath* add(IloIntArray path, IloNum value);
	IloTuplePath* add(IloIntArray path, IloAny value);

	IloTuplePath* modify(IloTuplePath* tuple, IloInt value);
	IloTuplePath* modify(IloTuplePath* tuple, IloNum value);
	IloTuplePath* modify(IloTuplePath* tuple, IloAny value);

protected:
	IloBool _isOrd;
	IloTuplePathList _list;
	IloTuplePath* contains(IloIntArray array);
	IloTuplePath* contains(IloInt size, IloInt* path);
public:
	IloBool isOrd() const{ return _isOrd; }
	void setIsOrd(IloBool flag) { _isOrd = flag; }
		
	IloTuplePathBufferI(IloEnv env);
	virtual ~IloTuplePathBufferI();

		
	IloBool isEmpty() const { return _list.getSize() == 0; }
		
	IloBool isSingleton() const { return _list.getSize() == 1; }

		
	IloInt getSize() const { return _list.getSize(); }
		
	IloTuplePathList& getList() { return _list; }

		
	void clear() {
		for (IloInt i=0; i< _list.getSize(); i++){
			IloTuplePath* path = _list.getObject(i);
			delete path; path=0;
		}
		_list.clear();
	}
		
	void import(IloTuplePathBufferI* select, IloInt prefix =-1);

		
	void import(IloInt idx1, IloTupleI* value);
		
	void import(IloIntArray path, IloTupleI* value);
		
	void import(IloInt pathSize, IloInt* path, IloTupleI* value);

		
	IloTuplePath* addOnce(IloInt columnIndex, IloInt value);
		
	IloTuplePath* addOnce(IloInt columnIndex, IloNum value);
		
	IloTuplePath* addOnce(IloInt columnIndex, IloAny value);

		
	IloTuplePath* addOnce(IloIntArray path, IloBool isArrayInternal, IloInt value);
		
	IloTuplePath* addOnce(IloIntArray path, IloBool isArrayInternal, IloNum value);
		
	IloTuplePath* addOnce(IloIntArray path, IloBool isArrayInternal, IloAny value);

		
	IloTuplePath* addOnce(IloInt pathSize, IloInt* path, IloInt value);
		
	IloTuplePath* addOnce(IloInt pathSize, IloInt* path, IloNum value);
		
	IloTuplePath* addOnce(IloInt pathSize, IloInt* path, IloAny value);

		
	virtual void display(ILOSTD(ostream)& os) const;
};



class IloTuplePathBuffer {
protected:
	IloTuplePathBufferI* _impl;
public:
	
	IloEnv getEnv(){
		return _impl->getEnv();
	}

		
	IloTuplePathList& getList(){
		return _impl->getList();
	}

	
	IloTuplePathBuffer(IloTuplePathBufferI* impl) : _impl(impl){ }

	
	IloTuplePathBufferI* getImpl() const {
		return _impl;
	}

	
	void end(){
		if (_impl) {
			delete _impl; _impl = 0;
		}
	}
	
	IloTuplePathBuffer(IloEnv env);

	
	void display(ILOSTD(ostream)& os) const{
		IloAssert(getImpl() != 0, "IloTuplePathBuffer: Using empty handle");
		_impl->display(os);
	}
	
	IloInt getSize() const {
		IloAssert(getImpl() != 0, "IloTuplePathBuffer: Using empty handle");
		return _impl->getSize();
	}
};

#ifdef _WIN32
#pragma pack(pop)
#endif


#endif
