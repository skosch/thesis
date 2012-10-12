// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilotuplecollectioni.h
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


#ifndef __ADVANCED_ilotuplecollectioniH
#define __ADVANCED_ilotuplecollectioniH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>

#include <ilopl/ilotuplecollection.h>
#include <ilopl/ilotuple.h>
#include <ilopl/iltuple/ilocollectioncolumni.h>
#include <ilconcert/iloset.h>
#include <ilopl/iltuple/ilotuplecollectionhash.h>
#include <ilopl/iltuple/ilotuplerequest.h>
#include <ilopl/iltuple/ilotupleschemai.h>


class IloTupleSet;
class IloTupleIterator;
class IloTupleSetArray;
class IloTupleI;
class IloTuple;
class IloTupleBuffer;
class IloTupleBufferI;
class IloTupleSchema;
class IloIntArray;

class IloTuplePattern;
class IloTuplePatternI;

class IloIntCollectionColumnI;
class IloNumCollectionColumnI;
class IloAnyCollectionColumnI;
class IloExtendedComprehensionI;
class IloTupleSetArray;

class IloTupleDefinitionI : public IloColumnDefinitionI{
private:
	IloTupleSchema _schema;
public:
	IloTupleDefinitionI(IloEnv env, IloTupleSchema schema, const char* name = 0);
	virtual IloBool isTuple() const;
	IloTupleSchema getSchema() const;
	virtual void display(ILOSTD(ostream)& out) const;
};


class IloAbstractTupleChangeListenerI : public IloDataChangeListenerI {
	ILORTTIDECL
		friend class IloTupleCollectionChangeNotifierI;
protected :
	virtual void change() const = 0;
	virtual void changeTuple(IloInt, IloTupleCellArray) const = 0;
	virtual void addTuple(IloInt) const = 0;
	virtual void removeTuple(IloInt) const = 0;
	virtual void removeCollection(IloTupleCollectionI* coll) const = 0;
public:
	IloAbstractTupleChangeListenerI(IloEnvI* env) : IloDataChangeListenerI(env) {}
	virtual ~IloAbstractTupleChangeListenerI() {};
};

class IloTupleCollectionChangeNotifierI : public IloDataChangeNotifierI {
	ILORTTIDECL
		friend class IloTupleSetI;
	friend class IloAscSortedTupleSetI;
	friend class IloTupleCollectionI;
	IloTupleSetI* _tupleSet;
protected:
	virtual void change() const;
	virtual void changeTuple(IloInt, IloTupleCellArray) const;
	virtual void addTuple(IloInt) const;
	virtual void removeTuple(IloInt) const;
	virtual void removeCollection(IloTupleCollectionI* coll) const;
public:
	IloTupleCollectionChangeNotifierI(IloEnvI*, IloTupleSetI*);
};


class IloTupleSetChangeListenerI : public IloAbstractTupleChangeListenerI {
	ILORTTIDECL
		IloTupleSetI* _set;
public:
	IloTupleSetChangeListenerI(IloEnvI* env, IloTupleSetI* request);
	virtual ~IloTupleSetChangeListenerI();
public:
	virtual void change() const;
	virtual void changeTuple(IloInt, IloTupleCellArray) const;
	virtual void addTuple(IloInt) const;
	virtual void removeTuple(IloInt) const;
	virtual void removeCollection(IloTupleCollectionI* coll) const;
private:
	IloEnvI* getEnv() const;
	IloTupleSetI* getSet() const;
};


class IloTupleCollectionI : public IloAnyCollectionI {
	ILORTTIDECL
protected:
	IloBool _opl3213;
	IloInt _size;
	IloIntArray _empty;
	IloDataCollectionArray _array;
	IloTupleSchema _schema;

	IloTupleBufferI* _sharedBuffer;
	IloTupleCellArray _sharedTupleCells;
	IloTupleCellArray _sharedKeyCells;
	IloTupleCellArray _sharedRefSliceCells;
	IloTupleCellArray _sharedRefCells;

	// for internal use to avoid conflicts with sharedxxxCells
	IloTupleCellArray _internalTupleCells;

	// for reference resolution
	IloTupleCellArray _referenceCells;

	IloTupleCells2ArrayIndexHash* _hashForTupleCollection;
	IloAnyArray _pointers;
	IloIntArray _collectionColumnIdx;
	IloBool _hasReference;
	IloIntArray _referencedColumns;
	IloIntArray _referenceKeyColumns;
	IloTupleCellArray getNormalizedCommitCells(IloTupleCellArray line);
	IloTupleCellArray getNormalizedFindCells(IloTupleCellArray line);
protected :
	IloTupleCollectionChangeNotifierI* _changeNotifier;
public:
	IloBool isOPL3213() const{ return _opl3213; }
	virtual IloTupleCollectionChangeNotifierI* getChangeNotifier();
	IloTupleCollectionChangeNotifierI* getChangeNotifier(IloTupleSetI* set);
	IloTupleCellArray makeDefaultValue() const;
	void freeNormalizedCells(IloTupleCellArray line);
	void checkReferences() const;

protected:
	void setListeners(IloTupleSetI*);
	void init(IloEnv env, IloTupleSchema schema, IloInt n = 0);
	void disableSelectIndexesOnColumn();
	void enableSelectIndexesOnColumn();
	IloBool isSelectIndexEnabledOnColumn() const;

	void removeValuesOnly(IloInt line);
private:
	void initReferencedColumns();
	IloTuplePattern makeTuplePattern(IloInt idx) const;

public:
	virtual IloObjectBase getMapItem(IloInt idx) const;

	void setLine(IloInt, IloTupleCellArray line);
	IloDiscreteDataCollectionI* makeColumnAsSet(IloInt idx) const;
	IloInt getRealIndex(IloInt columnIdx, IloInt idx);
	IloBool hasReference() const { return _hasReference;}

	IloIntArray getReferencedColumns() const{
		IloTestAndRaise(_referencedColumns.getImpl(), "IloTupleCollectionI::getReferencedColumns should not be called");
		return _referencedColumns;
	}

	IloInt getReferenceKeyIndexesCount() const {
		return _referenceKeyColumns.getImpl() == 0 ? 0 : _referenceKeyColumns.getSize();
	}

	IloIntArray getReferenceKeyIndexes() const{
		IloTestAndRaise(_referenceKeyColumns.getImpl(), "IloTupleCollectionI::getReferenceKeyIndexes should not be called");
		return _referenceKeyColumns;
	}
	void setReferences(IloIntArray idx, IloArray<IloDiscreteDataCollectionI*> references);

	IloAnyArray makePointersToColumns() const;
	IloIntArray getCollectionColumnIndexes() const{ return _collectionColumnIdx;}

	void fillWithDefaultValue(IloTupleCellArray cells);
	void fillWithDefaultValue(IloTupleBufferI* buf);
	void setDefaultValue(IloTupleCellArray cells);
	void setDefaultValue(IloTupleBufferI* buf);

	IloInt getInternalId(IloIntArray path) const;
	IloInt getInternalSubId(IloIntArray path) const;
	IloInt getReferenceKeyedInternalId(IloIntArray path) const;
	void fillHashForTupleCollection();
	void createHashForTupleCollection();
	void deleteHashForTupleCollection();

	IloTupleCells2ArrayIndexHash* getHashForTupleCollection() {
		if(!_hashForTupleCollection) {
			createHashForTupleCollection();
		}
		return _hashForTupleCollection;
	}

	IloAnyArray getSharedPointersToColumns() const{ return _pointers;}
	void setSize(IloInt size);
	friend class IloTupleIterator;
	IloTupleCollectionI(IloEnv env, IloTupleSchema schema, IloInt numberOfLine, IloTupleCellArray defaultValue);
	IloTupleCollectionI(IloEnv env, IloTupleSchema schema, IloInt numberOfLine); 
	IloTupleCollectionI(IloEnvI* env, IloTupleCollectionI* set); 
	IloDataCollectionArray getColumnArray() const { return _array; }
	virtual IloDataCollectionI* copy() const;
	virtual IloDataCollectionI* makeClone(IloEnvI*) const;

	void clearSelectIndexes();
	void createSelectIndexes();
	void createSelectIndexes(IloInt idxCol);
	void createSelectIndexes(IloIntArray idxCol);
private:
	IloDiscreteDataCollectionI* findColumn(const char* name) const;

private:
	void setValue(IloDiscreteDataCollection column, IloInt index, IloIntCollectionI* value);
	void setValue(IloDiscreteDataCollection column, IloInt index, IloNumCollectionI* value);
	void setValue(IloDiscreteDataCollection column, IloInt index, IloAnyCollectionI* value);

	void setValue(IloDiscreteDataCollection column, IloInt index, IloInt value);
	void setValue(IloDiscreteDataCollection column, IloInt index, IloNum value);
	void setValue(IloDiscreteDataCollection column, IloInt index, IloAny value);
	void setValue(IloIntArray path, IloInt index, IloInt value){
		setValue(getColumn(path), index, value);
	}
	void setValue(IloIntArray path, IloInt index, IloNum value){
		setValue(getColumn(path), index, value);
	}
	void setValue(IloIntArray path, IloInt index, IloAny value){
		setValue(getColumn(path), index, value);
	}
	void setValue(IloIntArray path, IloInt index, IloIntCollectionI* value){
		setValue(getColumn(path), index, value);
	}
	void setValue(IloIntArray path, IloInt index, IloNumCollectionI* value){
		setValue(getColumn(path), index, value);
	}
	void setValue(IloIntArray path, IloInt index, IloAnyCollectionI* value){
		setValue(getColumn(path), index, value);
	}

public:
	virtual void setLockable(IloBool flag);
	IloBool isKeyEnabled() const {
		return (this->getDataType() != IloDataCollection::TupleCollection)
			&&
			getSchema().getImpl()->getTotalColumnNumber() != getSchema().getImpl()->getOrMakeTotalKeyIndexes().getSize();
	}

protected:
	IloTupleCellArray makeKeyCells(IloInt line);
	IloTupleCellArray makeKeyCells(IloTupleCellArray);
public:
	IloTupleCellArray getOrMakeSharedTupleCells(IloTuplePatternI*);
protected:
	inline IloTupleCellArray getOrMakeInternalTupleCells(IloInt line){
		if (!_internalTupleCells.getImpl()){
			_internalTupleCells = makeTupleCells(line);
			return _internalTupleCells;
		}
		_internalTupleCells.end();
		_internalTupleCells = makeTupleCells(line);
		return _internalTupleCells;
	}

public:
	IloTupleBufferI* getOrMakeSharedTupleBuffer(){
		if ( !_sharedBuffer ) {
			delete _sharedBuffer;
			_sharedBuffer = makeTupleBuffer().getImpl();
		}
		_sharedBuffer->setIndex(-1);
		return _sharedBuffer;
	}

	IloTupleCellArray getOrMakeSharedTupleCells(IloTuplePathBuffer);
	//IloTupleCellArray getOrMakeEmptySharedTupleCells();

//utilisee par setI::makeTheQuery et les evalXxx.cpp
//ok with references.
	inline IloTupleCellArray getOrMakeEmptySharedTupleCells() {
		if (!_sharedTupleCells.getImpl()){
			_sharedTupleCells = IloTupleCellArray(getEnv(), getSchema().getImpl()->getTotalColumnNumber());
			return _sharedTupleCells;
		}
		IloInt size = getSchema().getImpl()->getTotalColumnNumber();
		if (_sharedTupleCells.getSize() == size) return _sharedTupleCells;
		_sharedTupleCells.getImpl()->setSize(size);  
		return _sharedTupleCells;
	}

	inline IloTupleCellArray getOrMakeSharedKeyCells(IloInt line){	
		if (!_sharedKeyCells.getImpl()){
			_sharedKeyCells = makeKeyCells(line);
			return _sharedKeyCells;
		}
		_sharedKeyCells.end();
		_sharedKeyCells = makeKeyCells(line);
		return _sharedKeyCells;
	}

	IloTupleCellArray getOrMakeSharedKeyCells(IloTupleCellArray);
	IloTupleCellArray getOrMakeSharedReferenceCells(IloTuplePathBuffer);

	inline IloTupleCellArray getOrMakeSharedTupleCells(IloInt line) {
		if (!_sharedTupleCells.getImpl()){
			_sharedTupleCells = makeTupleCells(line);
			return _sharedTupleCells;
		}
		_sharedTupleCells.end();
		_sharedTupleCells = makeTupleCells(line);
		return _sharedTupleCells;
	}

	//ok with references.
	inline IloTupleCellArray getOrMakeEmptySharedKeyCells() {
		if (!_sharedKeyCells.getImpl()){
			_sharedKeyCells = IloTupleCellArray(getEnv(), getSchema().getImpl()->getOrMakeTotalKeyIndexes().getSize());
			return _sharedKeyCells;
		}
		IloInt size = getSchema().getImpl()->getOrMakeTotalKeyIndexes().getSize();
		if (_sharedKeyCells.getSize() == size) return _sharedKeyCells;
		_sharedKeyCells.getImpl()->setSize(size);
		return _sharedKeyCells;
	}
	//ok with references.
	inline IloTupleCellArray getOrMakeEmptyRefSliceCells() {
		if (!_sharedRefSliceCells.getImpl())
			_sharedRefSliceCells = IloTupleCellArray(getEnv());
		else _sharedRefSliceCells.clear();
		return _sharedRefSliceCells;
	}

	inline IloTupleCellArray getOrMakeEmptyReferenceCells() {
		if (!_sharedRefCells.getImpl()){
			IloInt size = 0;
			for (IloInt i=0; i< _array.getSize(); i++){
				IloDiscreteDataCollectionI* coll = (IloDiscreteDataCollectionI*)_array[i].getImpl();
				if (coll->isTupleCollection()){
					size += ((IloTupleCollectionI*)coll)->getSchema().getImpl()->getTotalColumnNumber();
				}
				else if (coll->isTupleRefColumn()){
					IloTupleCollectionI* col = (IloTupleCollectionI*)((IloTupleRefDataColumnI*)coll)->getTupleCollection();
					size += col->getSchema().getImpl()->getOrMakeTotalKeyIndexes().getSize();
				}
				else{
					size+=1;
				}
			}
			_sharedRefCells = IloTupleCellArray(getEnv(), size);
		}
		return _sharedRefCells;
	}




public:

	IloInt getAbsoluteIndex(IloTupleCellArray cells, IloInt pos) const;

	IloIntArray getSharedPathFromAbsolutePosition(IloInt position) const;
	virtual IloInt commit(IloTuplePatternI* exp);
	void commitWithoutSize(IloTupleCellArray array, IloInt startIndex);
public:
	virtual IloInt commit(IloTupleCellArray array);
	virtual void remove(IloInt index);
	void remove(IloTuple buffer);
	virtual void remove(IloTupleCellArray buffer);

	void fillTupleCellsFromIndex(IloTupleCellArray array, IloInt line, IloInt index = 0) const;
	void fillTupleKeysFromIndex(IloTupleCellArray array, IloInt line, IloInt index = 0) const;
	void addTupleCells(IloTupleCellArray array, IloInt line) const;
	void addTupleKeys(IloTupleCellArray array, IloInt line) const;
	IloTupleCellArray makeTupleCells(IloTuplePathBuffer buf) const;
	IloTupleCellArray makeTupleCells(IloInt line) const;

	const char* getColumnName(IloInt index) const{
		return this->getColumn(index).getImpl()->getName();
	}

	IloDiscreteDataCollection getColumn(IloInt index) const{
		return (IloDiscreteDataCollectionI*)_array[index].getImpl();
	}

	IloTupleCollectionI* getTupleColumn(IloInt colIndex) const;
	IloTupleCollectionI* getTupleColumn(IloIntArray path) const;
	IloTupleCollectionI* getTupleColumn(IloInt size, IloInt* path) const;

	IloInt getColumnIndex(const char* name) const;
	IloInt getColumnIndex(const IloSymbolI* s1) const;
	virtual IloBool supportDuplicates() const{ return IloTrue; }
	IloTupleSchema getSchema() const { return _schema;}
	void setSchema(IloTupleSchema schema);
	IloInt getLength() const {
		return _array.getSize();
	}

	virtual IloDataCollection::IloDataType getDataType() const{
		return IloDataCollection::TupleCollection;
	}
	virtual IloAnyArray getArray() const;
	virtual IloBool isTupleCollection() const;
	inline IloInt getSize() const{
		return _size;
	}

	virtual ~IloTupleCollectionI();
	IloDiscreteDataCollection getColumn(IloIntArray path) const{
		IloInt colIdx = this->getInternalId(path);
		return (IloDiscreteDataCollectionI*)_pointers[colIdx];
	}
	IloTuple makeTuple(IloInt index) const{
		return new (getEnv()) IloTupleI(getEnv(), (IloTupleCollectionI*)this, index);
	}
	IloTupleBuffer makeTupleBuffer(IloInt index = -1) const{
		return new (getEnv()) IloTupleBufferI(getEnv(), (IloTupleCollectionI*)this, index);
	}
	IloTuplePathBuffer makeLine(IloInt index) const;
	void fillLine(IloTuplePathBuffer buf, IloInt index) const;
	virtual IloInt commit(IloTupleBuffer line);
	virtual void display(ILOSTD(ostream)& os) const;
	IloDataIterator* iterator(IloGenAlloc* heap) const;

	virtual IloInt getIndex(IloSymbol value) const;
	virtual IloInt getIndex(IloInt value) const;
	virtual IloInt getIndex(IloNum value) const;
	virtual IloInt getIndex(IloAny value) const; 
	virtual IloBool contains(IloAny elt) const;
	virtual IloAny getValue(IloInt index) const;
	virtual IloBool areEquivalents(IloInt index1, IloInt index2) const;
	IloTupleSetI* makeKeySet() const;

	virtual IloInt getTupleIndexFromAbsoluteIndex(IloInt idx) const{ return idx; }
	virtual IloInt getAbsoluteIndexFromTupleIndex(IloInt idx) const{ return idx; }
private:
	IloTupleSetChangeListenerI* _changeListener;
public:
	void setCollectionMustDelete(IloBool flag);
};


class IloTupleSetI : public IloTupleCollectionI {
public:
	class OPL3213 : public IloException {
	private:
		IloTupleSetI* _set;
	public:
		OPL3213(IloTupleSetI* set);
		virtual const char* getMessage() const;
		void print(ILOSTD(ostream)& out) const;
		IloTupleSetI* getSet() const { return _set; }
	};

	ILORTTIDECL
		virtual IloInt commit(IloTupleCellArray array){
			return commit(array, IloTrue);
	}
public:
	IloInt commit2HashTable(IloTupleCellArray array, IloBool check);
	void fillColumns();

public:
	friend class IloTupleIterator;
public:

	IloAnyArray makePointersToKeyColumns() const;

protected:
	IloVBHash* _VBhash;
	IloVBHash* _UnaryVBhash;
	IloTupleCells2IndexHash* _tupleHash;

private:
	IloDiscreteDataCollectionI* findColumn(const char* name) const;
	IloIntArray makeTheQuery(IloTupleRequest query, IloBool& arrayCreated);

public:
	virtual IloInt getMapItemIndex(IloObjectBase value) const;

	virtual IloTupleCollectionChangeNotifierI* getChangeNotifier();
	IloTupleSetI* makeTupleSetKey() const;
	IloTupleCells2IndexHash* getTupleCellsHash() const {
		if (_tupleHash)
			return _tupleHash;
		((IloTupleSetI*)this)->createTupleHash();
		return _tupleHash;
	}
	IloIntArray select(IloTupleRequest query, IloBool& arrayCreated);

	ILO_MEMORY_OBJECT* findVB(IloRequestSignatureI* req);
	ILO_MEMORY_OBJECT* findUnaryVB(IloRequestSignatureI* req);
	void addVB(IloRequestSignatureI* req, ILO_MEMORY_OBJECT* a);
	void addUnaryVB(IloRequestSignatureI* req, ILO_MEMORY_OBJECT* a);
	IloTupleBuffer checkUnicityOfTuple(IloTupleRequest query);

private:
	IloIntArray select(IloInt value, IloDiscreteDataCollection column, IloBool& arrayCreated, IloTuplePath::IloTupleRequestType reqType){
		IloDiscreteDataCollectionI* coll = column.getImpl();
		if (reqType == IloTuplePath::Equality) {
			return ((IloIntDataColumnI*)coll)->find(value, arrayCreated);
		} else if (reqType == IloTuplePath::LowerBound) {
			return ((IloIntDataColumnI*)coll)->findGe(value, arrayCreated);
		} else if (reqType == IloTuplePath::UpperBound) {
			return ((IloIntDataColumnI*)coll)->findLe(value, arrayCreated);
		}
		throw IloWrongUsage("IloIntArray IloTupleSetI::select");
		ILOUNREACHABLE(return 0;)
	}

	IloIntArray select(IloNum value, IloDiscreteDataCollection column, IloBool& arrayCreated, IloTuplePath::IloTupleRequestType reqType){
		IloDiscreteDataCollectionI* coll = column.getImpl();
		if (reqType == IloTuplePath::Equality) {
			return ((IloNumDataColumnI*)coll)->find(value, arrayCreated);
		} else if (reqType == IloTuplePath::LowerBound) {
			return ((IloNumDataColumnI*)coll)->findGe(value, arrayCreated);
		} else if (reqType == IloTuplePath::UpperBound) {
			return ((IloNumDataColumnI*)coll)->findLe(value, arrayCreated);
		}
		throw IloWrongUsage("IloIntArray IloTupleSetI::select");
		ILOUNREACHABLE(return 0;)
	}

	IloIntArray select(IloAny value, IloDiscreteDataCollection column, IloBool& arrayCreated){
		IloDiscreteDataCollectionI* coll = column.getImpl();
		return ((IloAnyDataColumnI*)coll)->find(value, arrayCreated);
	}


private:
	IloIntArray select(IloTuplePath* path, IloBool& arrayCreated);
	IloIntArray select(IloInt value, IloIntArray path, IloBool& arrayCreated, IloTuplePath::IloTupleRequestType reqType) {
		return select(value, getColumn(path), arrayCreated, reqType);
	}
	IloIntArray select(IloNum value, IloIntArray path, IloBool& arrayCreated, IloTuplePath::IloTupleRequestType reqType) {
		return select(value, getColumn(path), arrayCreated, reqType);
	}
	IloIntArray select(IloAny value, IloIntArray path, IloBool& arrayCreated) {
		return select(value, getColumn(path), arrayCreated);
	}

	void updateTupleHashAfterRemove( IloInt index );

public:
	using IloTupleCollectionI::setLine;
	virtual IloInt setLine(IloInt index, IloTupleCellArray line, IloBool check);
	IloTupleSetI(IloEnvI* env, IloTupleSetI* set);
	virtual IloTuple makeNext(IloTuple value, IloInt n=0) const;
	virtual IloTuple makePrevious(IloTuple value, IloInt n=0) const;
	virtual IloTuple makeNextC(IloTuple value, IloInt n=0) const;
	virtual IloTuple makePreviousC(IloTuple value, IloInt n=0) const;
	virtual IloTuple makeFirst() const;
	virtual IloTuple makeLast() const;
	virtual IloBool supportDuplicates() const;
	IloBool isIn(IloTupleBuffer buffer);
	IloBool isIn(IloTuplePathBuffer buffer);
	IloInt getIndex(IloTupleCellArray array);
	IloInt getIndex(IloTuplePatternI* exp);
	IloTupleI* find(IloTupleBufferI* buffer);
	IloTupleI* find(IloTupleBuffer buffer);
	IloTupleI* find(IloTuplePathBuffer buffer);

	void fillTupleHash();
	void createTupleHash();
	void clearTupleHash();
	virtual void clearAllCaches();
	virtual void empty(){ clear(); }
	void clear();
	void createHashForSelect();

	void clearHashForSelect(){
		if (_VBhash){
			for (IloVBHash::Iterator it(_VBhash); it.ok(); ++it){
				// Request signature are shared with the other hash -> we delete it in the other hash.
				//IloRequestSignatureI* sel = it.getKey();
				//delete sel; sel=0;
				ILO_MEMORY_OBJECT* hash = (ILO_MEMORY_OBJECT*)*(it);
				delete hash; hash = 0;
			}
			_VBhash->clear();
		}
		if (_UnaryVBhash){
			for (IloVBHash::Iterator it(_UnaryVBhash); it.ok(); ++it){
				IloRequestSignatureI* sel = it.getKey();
				delete sel; sel=0;
				ILO_MEMORY_OBJECT* hash = (ILO_MEMORY_OBJECT*)*(it);
				delete hash; hash = 0;
			}
			_UnaryVBhash->clear();
		}
	}

	void deleteHashForSelect(){
		clearHashForSelect();
		if (_VBhash){
			delete _VBhash; _VBhash = 0;
		}
		if (_UnaryVBhash){
			delete _UnaryVBhash; _UnaryVBhash = 0;
		}
	}
	IloBool isHashForSelectCreated() const {
		return (_VBhash != 0 && _UnaryVBhash != 0);
	}

	IloIntDataColumnI* getIntColumn(IloInt colIndex) const;
	IloIntDataColumnI* getIntColumn(const char* name) const;
	IloNumDataColumnI* getNumColumn(IloInt colIndex) const;
	IloNumDataColumnI* getNumColumn(const char* name) const;
	IloAnyDataColumnI* getAnyColumn(IloInt colIndex) const;
	IloAnyDataColumnI* getAnyColumn(const char* name) const;
	IloIntCollectionColumnI* getIntCollectionColumn(IloInt colIndex) const;
	IloIntCollectionColumnI* getIntCollectionColumn(const char* name) const;
	IloNumCollectionColumnI* getNumCollectionColumn(IloInt colIndex) const;
	IloNumCollectionColumnI* getNumCollectionColumn(const char* name) const;
	IloAnyCollectionColumnI* getAnyCollectionColumn(IloInt colIndex) const;
	IloAnyCollectionColumnI* getAnyCollectionColumn(const char* name) const;
	virtual IloDataCollection::IloDataType getDataType() const{
		return IloDataCollection::TupleSet;
	}
	virtual IloBool isTupleSet() const;
	IloTupleSetI(IloEnv env, IloTupleSchema schema);
	IloTupleSetI(IloEnv env, IloTupleSchema schema, IloTupleCellArray defaultValue);

	virtual ~IloTupleSetI();

	void enableSelectIndexes(){
		enableSelectIndexesOnColumn();
	}

	void disableSelectIndexes(){
		disableSelectIndexesOnColumn();
	}

	void setColumnName(IloIntArray path, const char* name){
		getColumn(path).getImpl()->setName(name);
	}
	const char* getColumnName(IloIntArray path) const{
		return this->getColumn(path).getImpl()->getName();
	}
	void setColumnName(IloInt index, const char* name){
		this->getColumn(index).getImpl()->setName(name);
	}
	const char* getColumnName(IloInt index) const{
		return this->getColumn(index).getImpl()->getName();
	}

	virtual IloInt commit(IloTupleBuffer line);
	virtual IloInt commit(IloTupleCellArray line, IloBool check);
	virtual IloInt commitWithoutSort(IloTupleCellArray line, IloBool check){
		return commit(line, check);
	}

	IloInt commit(IloTupleBuffer line, IloBool check);
	IloInt commit(IloInt index, IloTuplePathBuffer line, IloBool check);

	virtual IloDataIterator* iterator(IloGenAlloc* heap) const;

	virtual IloInt getIndex(IloInt value) const;
	virtual IloInt getIndex(IloNum value) const;
	virtual IloInt getIndex(IloSymbol value) const;
	virtual IloInt getIndex(IloAny value) const;
	IloInt getIndex(IloTupleI* tuple) const;
	IloInt getIndex(IloTuple tuple) const;

	virtual IloInt commit(IloTuplePatternI*);
	IloInt commit(IloTuplePatternI* exp, IloBool check);
	virtual IloDataCollectionI* copy() const;
	virtual IloDataCollectionI* makeClone(IloEnvI* env) const;
	virtual void remove(IloInt index);
	virtual void remove(IloTupleCellArray buffer);
	virtual IloBool areEquivalents(IloInt index1, IloInt index2) const;
	IloDiscreteDataCollectionI* smartCopy() const;

public:
	virtual void sort(IloBool  = IloTrue){}
};


//------------------


class IloTupleSetExprOperatorI : public IloTupleExprI {
	ILOEXTRDECL
private:
	IloTupleSetExprI* _coll;
	IloTupleExprI* _expr;
	IloIntExprI* _offset;
	IloBool _circ;
	IloBool _sense;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloTupleSetExprI* getTupleCollection() const { return _coll; }
	IloTupleExprI* getTupleExpr() const { return _expr; }
	IloIntExprI* getOffSet() const { return _offset; }
	IloBool isCircular() const { return _circ; }
	IloBool isNext() const { return _sense; }
	IloTupleSetExprOperatorI(IloEnvI* env,
		IloTupleSetExprI* coll,
		IloTupleExprI* index,
		IloIntExprI* offset = 0,
		IloBool sense = IloTrue,
		IloBool circ = IloTrue);
	virtual ~IloTupleSetExprOperatorI();
	virtual void display(ILOSTD(ostream)& out) const;
	virtual IloNum eval(const IloAlgorithm alg) const;
	virtual IloExtractableI* makeClone(IloEnvI*) const;
};

class IloTupleSetExprItemI : public IloTupleExprI {
	ILOEXTRDECL
private:
	IloTupleSetExprI* _coll;
	IloIntExprI* _offset;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloTupleSetExprI* getTupleCollection() const { return _coll; }
	IloIntExprI* getOffSet() const { return _offset; }
	IloTupleSetExprItemI(IloEnvI* env,
		IloTupleSetExprI* coll,
		IloIntExprI* offset = 0);
	virtual ~IloTupleSetExprItemI();
	void display(ILOSTD(ostream)& out) const;
	virtual IloNum eval(const IloAlgorithm alg) const;
	virtual IloExtractableI* makeClone(IloEnvI*) const;
};

class IloTupleSetExprItemByKeyI : public IloTupleExprI {
	ILOEXTRDECL
private:
	IloTupleSetExprI* _coll;
	IloTupleExprI* _offset;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloTupleSetExprI* getTupleCollection() const { return _coll; }
	IloTupleExprI* getOffSet() const { return _offset; }
	IloTupleSetExprItemByKeyI(IloEnvI* env,
		IloTupleSetExprI* coll,
		IloTupleExprI* offset);
	virtual ~IloTupleSetExprItemByKeyI();
	void display(ILOSTD(ostream)& out) const;
	virtual IloNum eval(const IloAlgorithm alg) const;
	virtual IloExtractableI* makeClone(IloEnvI*) const;
};


class IloTupleSetOrdI : public IloIntExprI {
	ILOEXTRDECL
		IloTupleSetExprI* _coll;
	IloTupleExprI* _expr;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloTupleSetOrdI(IloEnvI* env, IloTupleSetExprI* coll, IloTupleExprI* expr) :
	  IloIntExprI(env), _coll((IloTupleSetExprI*)coll->lockExpr()), _expr((IloTupleExprI*)expr->lockExpr()) {}
	  virtual ~IloTupleSetOrdI();
	  void display(ILOSTD(ostream)& out) const;
	  virtual IloNum eval(const IloAlgorithm alg) const;
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	  IloTupleSetExprI* getCollection() const { return _coll; }
	  IloTupleExprI* getExpr() const { return _expr; }
};


class IloTupleCollectionFirstI : public IloTupleExprI {
	ILOEXTRDECL
private:
	IloTupleSetExprI* _coll;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloTupleSetExprI* getExpr() const { return _coll; }
	IloTupleCollectionFirstI(IloEnvI* env, IloTupleSetExprI* coll);
	virtual ~IloTupleCollectionFirstI();
	void display(ILOSTD(ostream)& out) const;
	virtual IloNum eval(const IloAlgorithm alg) const;
	virtual IloExtractableI* makeClone(IloEnvI*) const;
};

class IloTupleCollectionLastI : public IloTupleExprI {
	ILOEXTRDECL
private:
	IloTupleSetExprI* _coll;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloTupleSetExprI* getExpr() const { return _coll; }
	IloTupleCollectionLastI(IloEnvI* env, IloTupleSetExprI* coll);
	virtual ~IloTupleCollectionLastI();
	void display(ILOSTD(ostream)& out) const;
	virtual IloNum eval(const IloAlgorithm alg) const;
	virtual IloExtractableI* makeClone(IloEnvI*) const;
};

class IloTupleCollectionInterI : public IloTupleSetExprI {
	ILOEXTRDECL
private:
	IloTupleSetExprI* _left;
	IloTupleSetExprI* _right;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloTupleSetExprI* getLeft() const { return _left; }
	IloTupleSetExprI* getRight() const { return _right; }
	IloTupleCollectionInterI(IloEnvI* env, IloTupleSetExprI* left, IloTupleSetExprI* right);
	virtual ~IloTupleCollectionInterI();
	void display(ILOSTD(ostream)& out) const;
	virtual IloNum eval(const IloAlgorithm alg) const;
	virtual IloExtractableI* makeClone(IloEnvI*) const;
};

class IloTupleCollectionUnionI : public IloTupleSetExprI {
	ILOEXTRDECL
private:
	IloTupleSetExprI* _left;
	IloTupleSetExprI* _right;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloTupleSetExprI* getLeft() const { return _left; }
	IloTupleSetExprI* getRight() const { return _right; }
	IloTupleCollectionUnionI(IloEnvI* env, IloTupleSetExprI* left, IloTupleSetExprI* right);
	virtual ~IloTupleCollectionUnionI();
	void display(ILOSTD(ostream)& out) const;
	virtual IloNum eval(const IloAlgorithm alg) const;
	virtual IloExtractableI* makeClone(IloEnvI*) const;
};

class IloTupleCollectionExcludeI : public IloTupleSetExprI {
	ILOEXTRDECL
private:
	IloTupleSetExprI* _left;
	IloTupleSetExprI* _right;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloTupleSetExprI* getLeft() const { return _left; }
	IloTupleSetExprI* getRight() const { return _right; }
	IloTupleCollectionExcludeI(IloEnvI* env, IloTupleSetExprI* left, IloTupleSetExprI* right);
	virtual ~IloTupleCollectionExcludeI();
	void display(ILOSTD(ostream)& out) const;
	virtual IloNum eval(const IloAlgorithm alg) const;
	virtual IloExtractableI* makeClone(IloEnvI*) const;
};

class IloTupleCollectionSymExcludeI : public IloTupleSetExprI {
	ILOEXTRDECL
private:
	IloTupleSetExprI* _left;
	IloTupleSetExprI* _right;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloTupleSetExprI* getLeft() const { return _left; }
	IloTupleSetExprI* getRight() const { return _right; }
	IloTupleCollectionSymExcludeI(IloEnvI* env, IloTupleSetExprI* left, IloTupleSetExprI* right);
	virtual ~IloTupleCollectionSymExcludeI();
	void display(ILOSTD(ostream)& out) const;
	virtual IloNum eval(const IloAlgorithm alg) const;
	virtual IloExtractableI* makeClone(IloEnvI*) const;
};

class IloTupleCollectionCardI : public IloIntExprI {
	ILOEXTRDECL
		IloTupleSetExprI* _expr;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloTupleCollectionCardI(IloEnvI* env, IloTupleSetExprI* expr) :
	  IloIntExprI(env), _expr((IloTupleSetExprI*)expr->lockExpr()) {}
	  virtual ~IloTupleCollectionCardI();
	  void display(ILOSTD(ostream)& out) const;
	  virtual IloNum eval(const IloAlgorithm alg) const;
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	  IloTupleSetExprI* getExpr() const { return _expr; }
};

class IloTupleSetConstI : public IloTupleSetExprI {
	ILOEXTRDECL
private:
	IloTupleSetI* _tuple;
	IloBool _ownsColl;
	void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloTupleSetConstI(IloEnvI* env, IloTupleSetI* tuple, IloBool ownsTuple=IloFalse);
	virtual ~IloTupleSetConstI();
	IloTupleSetI* getTupleSet() const { return _tuple; }
	ILOEXTROTHERDECL
};



class IloTupleSetExprSubsetI : public IloConstraintI {
	ILOEXTRDECL
private:
	IloTupleSetExprArgI* _slice;
	IloTupleSetExprArgI* _coll;
	IloBool _eq;
public:
	IloTupleSetExprSubsetI(IloEnvI* env, IloTupleSetExprArgI* exp, IloTupleSetExprArgI* coll, IloBool eq);
    virtual ~IloTupleSetExprSubsetI();
	void visitSubExtractables(IloExtractableVisitor* v);
	IloTupleSetExprArgI* getSlice() const { return _slice; }
	IloTupleSetExprArgI* getCollection() const { return _coll; }
	IloBool isSubSetEq() const { return _eq; }
	ILOEXTROTHERDECL
};

#ifdef _WIN32
#pragma pack(pop)
#endif


#endif
