// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/iltuple/ilodatacolumni.h
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


#ifndef __ADVANCED_ilodatacolumniH
#define __ADVANCED_ilodatacolumniH

#ifdef _WIN32
#pragma pack(push, 8)
#endif
#include <ilopl/ilosys.h>

//#include <ilopl/iltuple/ilodatacolumn.h>
#include <ilopl/iloexpressioni.h>
#include <ilconcert/ilocollectioni.h>
#include <ilopl/iltuple/ilotuplecollectionhash.h>


class IloIntDataColumnI : public IloIntAbstractDataColumnI {
public:
	class UnknownReference : public IloException {
	private:
		IloDiscreteDataCollectionI* _set;
		IloDiscreteDataCollectionI* _reference;
		IloInt _value;
	public:
		UnknownReference(IloDiscreteDataCollectionI* set, IloDiscreteDataCollectionI* ref, IloInt val);
		virtual const char* getMessage() const;
		void print(ILOSTD(ostream)& out) const;
		IloDiscreteDataCollectionI* getReference() const { return _reference; }
		IloInt getValue() const { return _value; }
		IloDiscreteDataCollectionI* getDataCollection() const { return _set; }
	};

	ILORTTIDECL
protected:
	IloIntArray _array;
	IloIntDataTableHash* _hashForSelect;
	IloInt _defaultValue;
	IloDiscreteDataCollectionI* _refered;
	IloIntDataTableHash::Status add(IloInt value, IloIntArrayI* res){
		return _hashForSelect->add(value, res);
	}

	void updateHashForSelect(IloInt index, IloInt value, IloBool addIndex = IloTrue);

protected:
	void discard(IloInt value);
public:
	void checkReferences() const;
	void setReference(IloDiscreteDataCollectionI* refered);
	virtual IloBool hasReference() const {  return _refered != 0; } 
	IloDiscreteDataCollectionI* getReference() const {return _refered; }
	void addWithoutCheck(IloInt value, IloIntArrayI* res){
		_hashForSelect->addWithoutCheck(value, res);
	}
	virtual IloDataCollectionI* copy() const;
	virtual IloDataCollectionI* makeClone(IloEnvI*) const;

	virtual void remove(IloInt index);

	void setValue(IloInt index, IloInt value);
	void createSelectIndexes();
	void clearSelectIndexes();
	IloIntArray find(IloInt value, IloBool& arrayCreated);
	IloIntArray findGe(IloInt value, IloBool& arrayCreated);
	IloIntArray findLe(IloInt value, IloBool& arrayCreated);
	void enableSelectIndexes() {
		if (!_hashForSelect) _hashForSelect = new (getEnv()) IloIntDataTableHash(getEnv());
	}
	void disableSelectIndexes() {
		if (_hashForSelect){
			_hashForSelect->clear();
			delete _hashForSelect; _hashForSelect = 0;
		}
	}
	IloBool isSelectIndexEnabled() const { return (_hashForSelect != 0); }
	IloIntDataColumnI(IloEnvI* env, const IloIntArray array, IloInt defaultValue = 0);
	IloIntDataColumnI(IloEnvI* env, IloInt n=0);
	virtual ~IloIntDataColumnI();
	inline IloIntArray getArray() const {return _array;}
	virtual IloIntArray makeIndexArray(IloInt value) const;
	using IloIntAbstractDataColumnI::getIndex;
	IloInt getIndex(IloInt value) const;
	IloInt getIntValue(IloInt idx) const;
	IloNum getNumValue(IloInt idx) const;
	IloInt getSize() const{
		return _array.getImpl() ? _array.getSize() : 0;
	}
	virtual IloObjectBase getMapItem(IloInt idx) const;
	void add(IloInt elt);
	void add(IloIntDataColumnI* set);
	virtual void empty();
	IloBool contains(IloInt elt) const;
	IloInt getValue(IloInt index) const;
	inline virtual IloDataCollection::IloDataType getDataType() const{
		return IloDataCollection::IntDataColumn;
	}
	IloBool isIntDataColumn() const;
	void display(ILOSTD(ostream)& os) const;
	virtual IloInt getLB();
	virtual IloInt getUB();
	IloInt getDefaultValue() const { return _defaultValue; }
	void setDefaultValue(const IloInt val);
	void fillWithDefaultValue(const IloInt val);
	void clear();
};


class IloNumDataColumnI : public IloNumAbstractDataColumnI {
public:
	class UnknownReference : public IloException {
	private:
		IloDiscreteDataCollectionI* _set;
		IloDiscreteDataCollectionI* _reference;
		IloNum _value;
	public:
		UnknownReference(IloDiscreteDataCollectionI* set, IloDiscreteDataCollectionI* ref, IloNum val);
		virtual const char* getMessage() const;
		void print(ILOSTD(ostream)& out) const;
		IloDiscreteDataCollectionI* getReference() const { return _reference; }
		IloNum getValue() const { return _value; }
		IloDiscreteDataCollectionI* getDataCollection() const { return _set; }
	};

	ILORTTIDECL
private:
	IloNumArray _array;
	IloNumDataTableHash* _hashForSelect;
	IloNum _defaultValue;
	IloDiscreteDataCollectionI* _refered;

	IloNumDataTableHash::Status add(IloNum value, IloIntArrayI* res) {
		return _hashForSelect->add(value, res);
	}

	void updateHashForSelect(IloInt index, IloNum value, IloBool addIndex = IloTrue);

protected:
	void discard(IloNum value);

public:
	void checkReferences() const;
	void setReference(IloDiscreteDataCollectionI* refered);
	virtual IloBool hasReference() const {  return _refered != 0; } 
	IloDiscreteDataCollectionI* getReference() const { return _refered; }
	void addWithoutCheck(IloNum value, IloIntArrayI* res){
		_hashForSelect->addWithoutCheck(value, res);
	}
	virtual IloDataCollectionI* copy() const;
	virtual IloDataCollectionI* makeClone(IloEnvI*) const;

	void remove(IloInt index);
	void setValue(IloInt index, IloNum value);
	void createSelectIndexes();
	void clearSelectIndexes();
	IloIntArray find(IloNum value, IloBool& arrayCreated);
	IloIntArray findGe(IloNum value, IloBool& arrayCreated);
	IloIntArray findLe(IloNum value, IloBool& arrayCreated);
	void enableSelectIndexes() {
		if (!_hashForSelect) _hashForSelect = new (getEnv()) IloNumDataTableHash(getEnv());
	}
	void disableSelectIndexes() {
		if (_hashForSelect){
			_hashForSelect->clear();
			delete _hashForSelect; _hashForSelect = 0;
		}
	}
	IloBool isSelectIndexEnabled() const { return (_hashForSelect != 0); }
	IloNumDataColumnI(IloEnvI* env, const IloNumArray array, IloNum defaultValue = 0.0);
	IloNumDataColumnI(IloEnvI* env, IloInt n=0);
	virtual ~IloNumDataColumnI();
	inline IloNumArray getArray() const {return _array;}
	IloInt getSize() const{
		return _array.getImpl() ? _array.getSize() : 0;
	}
	virtual IloObjectBase getMapItem(IloInt idx) const;

	using IloNumAbstractDataColumnI::getIndex;

	IloInt getIndex(IloNum value) const;

	IloNum getNumValue(IloInt idx) const;
	IloInt getIntValue(IloInt idx) const;
	virtual IloIntArray makeIndexArray(IloNum value) const;
	void add(IloNum elt);
	void add(IloNumDataColumnI* set);
	void empty();
	IloBool contains(IloNum elt) const;
	IloNum getValue(IloInt index) const;
	inline virtual IloDataCollection::IloDataType getDataType() const{
		return IloDataCollection::NumDataColumn;
	}
	IloBool isNumDataColumn() const;
	void display(ILOSTD(ostream)& os) const;
	IloNum getDefaultValue() const { return _defaultValue; }
	void setDefaultValue(const IloNum coll);
	void fillWithDefaultValue(const IloNum val);
	void clear();
};


class IloAnyDataColumnI : public IloAnyAbstractDataColumnI {
public:
	class UnknownReference : public IloException {
	private:
		IloDiscreteDataCollectionI* _set;
		IloDiscreteDataCollectionI* _reference;
		IloAny _value;
	public:
		UnknownReference(IloDiscreteDataCollectionI* set, IloDiscreteDataCollectionI* ref, IloAny val);
		virtual const char* getMessage() const;
		void print(ILOSTD(ostream)& out) const;
		IloDiscreteDataCollectionI* getReference() const { return _reference; }
		IloAny getValue() const { return _value; }
		IloDiscreteDataCollectionI* getDataCollection() const { return _set; }
	};

	ILORTTIDECL
protected:
	IloAnyArray _array;
	IloAnyDataTableHash* _hashForSelect;
	IloDiscreteDataCollectionI* _refered;

	virtual void updateHashForSelect(IloInt index, IloAny value, IloBool addIndex = IloTrue);

protected:
	IloAny _defaultValue;
	virtual void discard(IloAny value);

public:
	void checkReferences() const;
	void setReference(IloDiscreteDataCollectionI* refered);
	virtual IloBool hasReference() const {  return _refered != 0; } 
	IloDiscreteDataCollectionI* getReference() const { return _refered; }
	void addWithoutCheck(IloAny value, IloIntArrayI* res){
		_hashForSelect->addWithoutCheck(value, res);
	}
	virtual IloDataCollectionI* copy() const;
	virtual IloDataCollectionI* makeClone(IloEnvI*) const;

	virtual void remove(IloInt index);

	virtual void setValue(IloInt index, IloAny value);
	void createSelectIndexes();
	void clearSelectIndexes();
	IloIntArray find(IloAny value, IloBool& arrayCreated);
	virtual void enableSelectIndexes() {
		if (!_hashForSelect) _hashForSelect = new (getEnv()) IloAnyDataTableHash(getEnv(), IloAddressHashFunction, IloAddressCompFunction);
	}
	void disableSelectIndexes() {
		if (_hashForSelect){
			_hashForSelect->clear();
			delete _hashForSelect; _hashForSelect = 0;
		}
	}
	IloBool isSelectIndexEnabled() const { return (_hashForSelect != 0); }
	IloAnyDataColumnI(IloEnvI* env, const IloAnyArray array, IloAny defaultValue = 0);
	IloAnyDataColumnI(IloEnvI* env, IloInt n=0);
	virtual ~IloAnyDataColumnI();
	inline IloAnyArray getArray() const {return _array;}
	IloInt getSize() const{
		return _array.getImpl() ? _array.getSize() : 0;
	}
	virtual IloObjectBase getMapItem(IloInt idx) const;

	using IloAnyAbstractDataColumnI::getIndex;
	virtual IloInt getIndex(IloAny val) const;
	IloAny getAnyValue(IloInt idx) const;
	virtual IloIntArray makeIndexArray(IloAny value) const;
	virtual void add(IloAny elt);
	virtual void add(IloAnyDataColumnI* set);
	virtual void empty();
	IloBool contains(IloAny elt) const;
	IloAny getValue(IloInt index) const;

	inline virtual IloDataCollection::IloDataType getDataType() const{
		return IloDataCollection::AnyDataColumn;
	}
	IloBool isAnyDataColumn() const;
	void display(ILOSTD(ostream)& os) const;
	IloAny getDefaultValue() const { return _defaultValue; }
	virtual void setDefaultValue(const IloAny val);
	void fillWithDefaultValue(const IloAny val);
	void clear();
};


#ifdef _WIN32
#pragma pack(pop)
#endif



#endif
