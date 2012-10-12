// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/iltuple/ilocollectioncolumni.h
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


#ifndef __ADVANCED_ilocollectioncolumniH
#define __ADVANCED_ilocollectioncolumniH

#ifdef _WIN32
#pragma pack(push, 8)
#endif
#include <ilopl/ilosys.h>


#include <ilopl/iltuple/ilodatacolumni.h>

class IloIntMap;
class IloNumMap;
class IloAnyMap;

class IloMapI;
class IloObjectBase;

class IloIntMapAsCollectionI : public IloIntCollectionI {
	ILORTTIDECL
private:
	IloMapI* _map;
public:
	virtual IloDataCollectionI* copy() const;
	virtual IloDataCollectionI* makeClone(IloEnvI* env) const;
	IloIntMapAsCollectionI(IloEnvI* env, const IloIntMap map);
	IloIntMap getMap() const;
	IloDiscreteDataCollectionI* getIndexer() const;
	virtual ~IloIntMapAsCollectionI();
	virtual IloDataCollection::IloDataType getDataType() const;
	virtual IloInt getSize() const;
	virtual IloObjectBase getMapItem(IloInt idx) const;
	virtual IloIntArray getArray() const;
	virtual IloBool contains(IloInt e) const;
	virtual IloInt getValue(IloInt index) const;
	virtual IloDataIterator* iterator(IloGenAlloc* heap) const;
	virtual void display(ILOSTD(ostream)& os) const;
	virtual IloBool isMapAsCollection() const { return IloTrue; }
};

class IloNumMapAsCollectionI : public IloNumCollectionI {
	ILORTTIDECL
private:
	IloMapI* _map;
public:
	virtual IloDataCollectionI* copy() const;
	virtual IloDataCollectionI* makeClone(IloEnvI* env) const;
	IloNumMapAsCollectionI(IloEnvI* env, const IloNumMap map);
	IloNumMap getMap() const;
	IloDiscreteDataCollectionI* getIndexer() const;
	virtual ~IloNumMapAsCollectionI();
	virtual IloDataCollection::IloDataType getDataType() const;
	virtual IloInt getSize() const;
	virtual IloObjectBase getMapItem(IloInt idx) const;
	virtual IloNumArray getArray() const;
	virtual IloBool contains(IloNum e) const;
	virtual IloNum getValue(IloInt index) const;
	virtual IloDataIterator* iterator(IloGenAlloc* heap) const;
	virtual void display(ILOSTD(ostream)& os) const;
	virtual IloBool isMapAsCollection() const { return IloTrue; }
};


class IloCollectionColumnI : public IloAnyDataColumnI {
	IloBool _mustDelete;
protected:
	IloDiscreteDataCollectionI* _indexer; // at this moment we only accept 1 dimension map in tuples.
	ILORTTIDECL
protected:
	virtual void updateHashForSelect(IloInt index, IloAny value, IloBool addIndex = IloTrue);
public:
	using IloAnyDataColumnI::getIndex;
	// for RS6000 because getArray is const.
	IloCollectionColumnI(IloEnvI* env, IloDiscreteDataCollectionI* indexer, const IloAnyArray array, IloDiscreteDataCollectionI* defaultValue = 0);
	IloCollectionColumnI(IloEnvI* env, IloDiscreteDataCollectionI* indexer): IloAnyDataColumnI(env), _mustDelete(IloTrue), _indexer(indexer) {}
	virtual ~IloCollectionColumnI();
	virtual void add(IloAny elt);
	virtual void add(IloAnyDataColumnI* set);
	virtual void discard(IloAny value);
	virtual void remove(IloInt index);
	virtual void setValue(IloInt index, IloAny value);
	virtual void empty();
	virtual void setDefaultValue(const IloAny coll);
	void mustDelete(IloBool flag){ _mustDelete = flag; }
	IloBool mustDelete() const{ return _mustDelete;}
	virtual IloInt getIndex(IloAny val) const;
	virtual IloIntArray makeIndexArray(IloAny value) const;
	virtual IloDataCollection::IloDataType getDataType() const{
		throw IloWrongUsage("IloCollectionColumnI does not have a Data Type");
		ILOUNREACHABLE(return IloDataCollection::IloDataType(0);)
	}
	IloBool isSetColumn() const { return _indexer==0;}
	IloBool compareIndexer(IloDiscreteDataCollectionI* coll) const;
	virtual void checkBeforeUsing(IloDiscreteDataCollectionI* coll) const = 0;
};


typedef IloArray<IloTupleCellArrayI*> IloTrial;

class IloTupleRefDataColumnI : public IloIntDataColumnI {
	ILORTTIDECL
private:
	IloBool _checkReference;
	IloTrial _hashForKeys; 
public:
	using IloIntDataColumnI::getIndex;
	virtual IloDataCollectionI* copy() const;
	virtual IloDataCollectionI* makeClone(IloEnvI* env) const;

	IloTupleRefDataColumnI(IloEnvI* env, IloTupleSetI* refered, IloInt n, IloBool checkReferences);
	virtual IloObjectBase getMapItem(IloInt idx) const;

	IloBool checkReferences() const { return _checkReference;};
	void setCheckReferences(IloBool flag) { _checkReference = flag; };
	IloInt commit(IloTupleCellArray line);
	IloBool setLine(IloInt, IloTupleCellArray line);

	IloTupleCellArray getOrMakeSharedTupleCells(IloTuplePathBuffer);
	IloTupleCellArray getOrMakeEmptySharedTupleCells();
	IloTupleCellArray getOrMakeSharedTupleCells(IloInt line);
	IloTupleCellArray getOrMakeSharedKeyCells(IloInt line);
	IloTupleCellArray getOrMakeSharedKeyCells(IloTupleCellArray);
	IloTupleCellArray getOrMakeEmptySharedKeyCells();
	void addTupleCells(IloTupleCellArray array, IloInt line) const;

	//IloInt getTupleIndex(IloTupleCellArray array);

	IloInt getTupleIndex(IloTupleCellArray array);
	inline IloTupleSetI* getTupleCollection() const{return (IloTupleSetI*)(void*)_refered;}
	IloTrial getHashForKeys() const{ return _hashForKeys;}

	IloInt getWidth() const; 
	virtual ~IloTupleRefDataColumnI();
	virtual void empty();
	virtual IloDataCollection::IloDataType getDataType() const{
		return IloDataCollection::TupleRefColumn;
	}
	virtual IloBool isIntDataColumn() const{ return IloFalse;}
	virtual IloBool isTupleRefColumn() const{ return IloTrue;}
	virtual void display(ILOSTD(ostream)& os) const;
	virtual void displayKeys(ILOSTD(ostream)& os) const;
	virtual void remove(IloInt index);
	virtual void remove(IloTupleCellArray cells);
};


class IloIntCollectionColumnI : public IloCollectionColumnI {
	ILORTTIDECL
public:
	IloIntCollectionColumnI(IloEnvI* env, IloDiscreteDataCollectionI* indexer, const IloAnyArray array, IloDiscreteDataCollectionI* defaultFalue = 0);
	IloIntCollectionColumnI(IloEnvI* env, IloDiscreteDataCollectionI* indexer, IloInt n=0);
	virtual IloBool isIntCollectionColumn() const;
	virtual IloDataCollectionI* copy() const;
	virtual IloDataCollectionI* makeClone(IloEnvI* env) const;
	virtual void enableSelectIndexes() {}
	virtual IloDataCollection::IloDataType getDataType() const{
		return IloDataCollection::IntCollectionColumn;
	}
	virtual IloObjectBase getMapItem(IloInt idx) const;
	virtual void checkBeforeUsing(IloDiscreteDataCollectionI* coll) const;
	virtual IloAny getValue(IloInt index) const;
};

class IloNumCollectionColumnI : public IloCollectionColumnI {
	ILORTTIDECL
public:
	IloNumCollectionColumnI(IloEnvI* env, IloDiscreteDataCollectionI* indexer, const IloAnyArray array, IloDiscreteDataCollectionI* defaultFalue = 0);
	IloNumCollectionColumnI(IloEnvI* env, IloDiscreteDataCollectionI* indexer, IloInt n=0);
	virtual IloBool isNumCollectionColumn() const;
	virtual IloDataCollectionI* copy() const;
	virtual IloDataCollectionI* makeClone(IloEnvI* env) const;
	virtual void enableSelectIndexes() {}
	virtual IloDataCollection::IloDataType getDataType() const{
		return IloDataCollection::NumCollectionColumn;
	}
	virtual IloObjectBase getMapItem(IloInt idx) const;
	virtual void checkBeforeUsing(IloDiscreteDataCollectionI* coll) const;
	virtual IloAny getValue(IloInt index) const;
};

class IloAnyCollectionColumnI : public IloCollectionColumnI {
	ILORTTIDECL
public:
	IloAnyCollectionColumnI(IloEnvI* env, const IloAnyArray array, IloDiscreteDataCollectionI* defaultValue = 0);
	IloAnyCollectionColumnI(IloEnvI* env, IloInt n=0);
	virtual IloBool isAnyCollectionColumn() const;
	virtual IloDataCollectionI* copy() const;
	virtual IloDataCollectionI* makeClone(IloEnvI* env) const;
	virtual void enableSelectIndexes() {}
	virtual IloDataCollection::IloDataType getDataType() const{
		return IloDataCollection::AnyCollectionColumn;
	}
	virtual IloObjectBase getMapItem(IloInt idx) const;
	virtual void checkBeforeUsing(IloDiscreteDataCollectionI* ) const{}
	virtual IloAny getValue(IloInt index) const;
};


class IloCollectionUtil {
public:
	static IloIntMap getMap(IloIntCollection coll);
	static IloNumMap getMap(IloNumCollection coll);
};

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
