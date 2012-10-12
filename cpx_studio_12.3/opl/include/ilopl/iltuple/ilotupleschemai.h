// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/iltuple/ilotupleschemai.h
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


#ifndef __ADVANCED_ilotupleschemaiH
#define __ADVANCED_ilotupleschemaiH

#ifdef _WIN32
#pragma pack(push, 8)
#endif
#include <ilopl/ilosys.h>


#include <ilopl/iltuple/ilodatacolumni.h>

class IloTupleSchemaI;
class IloTupleSchema;

class IloTuplePattern;



class IloColumnDefinitionI : public IloEnvObjectI{
protected:
	IloDataCollection::IloDataType _type;
	const char* _name;
	IloBool _isKey;
	IloAny _data;
	void setName(const char* name);
public:
	
	IloColumnDefinitionI(IloEnv env, IloDataCollection::IloDataType type, const char* name = 0);

	
	IloDataCollection::IloDataType getDataType() const {
		return _type;
	}

	void setKeyProperty(IloBool type){
		_isKey = type;
	}

	IloBool getKeyProperty() const{
		return _isKey;
	}

	
	void setData(IloAny data){
		_data = data;
	}
	
	IloAny getData(){
		return _data;
	}

	
	const char* getName() const {
		return _name;
	}

	virtual ~IloColumnDefinitionI(){
		if (_name!=0){
			getEnv()->free((char *)_name, 1+strlen(_name) );
		}
	}

	
	IloBool isInt() const { return _type == IloDataCollection::IntDataColumn; }
	
	IloBool isNum() const { return _type == IloDataCollection::NumDataColumn; }
	
	IloBool isSymbol() const { return _type == IloDataCollection::SymbolDataColumn; }
	
	IloBool isAny() const { return _type == IloDataCollection::AnyDataColumn; }

	
	IloBool isIntCollection() const { return _type == IloDataCollection::IntCollectionColumn; }
	
	IloBool isNumCollection() const { return _type == IloDataCollection::NumCollectionColumn; }
	
	IloBool isAnyCollection() const { return _type == IloDataCollection::AnyCollectionColumn; }

	
	virtual IloBool isTuple() const;
	
	virtual void display(ILOSTD(ostream)& out) const;
};



class IloColumnDefinition {
	IloColumnDefinitionI* _impl;
public:
	
	IloColumnDefinition():_impl(0){}
	
	IloColumnDefinition(IloColumnDefinitionI* impl):_impl(impl){}
	
	IloColumnDefinitionI* getImpl() const { return _impl; }
	
	void end(){
		if (_impl){
			delete _impl; _impl = 0;
		}
	}
};



typedef IloArray<IloColumnDefinition> IloColumnDefinitionArrayBase;


class IloColumnDefinitionArray : public IloColumnDefinitionArrayBase {
public:
	typedef IloDefaultArrayI ImplClass;
	
	IloColumnDefinitionArray(IloDefaultArrayI* i=0) : IloColumnDefinitionArrayBase(i){}
	
	IloColumnDefinitionArray(const IloColumnDefinitionArray& copy) : IloColumnDefinitionArrayBase(copy){}
	
	IloColumnDefinitionArray(const IloEnv env, IloInt n = 0) : IloColumnDefinitionArrayBase(env, n){}
};



class IloTupleSchemaI : public IloRttiEnvObjectI {
	ILORTTIDECL
private:
	IloBool _hasKey;
	IloTupleSchemaI* _keySchema;
	IloIntArray _keyTotalIdx;
	IloIntArray _keyIdx;
	IloBool _hasSubTuple;
	const char* _name;
	IloColumnDefinitionArray _array;
	IloColumnDefinitionArray _totalArray;
	IloIntArray2 _sharedPaths;
	IloIntArray _simpleColumnsIds;
	IloIntArray _intColsAbsIdx;
	IloIntArray _numColsAbsIdx;
	IloIntArray _symbolColsAbsIdx;
	IloIntArray _intColsKeyAbsKeyIdx;
	IloIntArray _numColsKeyAbsKeyIdx;
	IloIntArray _symbolColsKeyAbsKeyIdx;

	IloDataCollection::IloDataType getColumnType(IloIntArray path);
	void makeSharedPaths();
	IloIntArray makePathFromAbsolutePosition(IloInt position) const;
	void buildSharedIntColsAbsIdx();
	void buildSharedNumColsAbsIdx();
	void buildSharedSymbolColsAbsIdx();
public:
	void makeShared(){
		((IloTupleSchemaI*)this)->makeSharedPaths();
		((IloTupleSchemaI*)this)->buildSharedIntColsAbsIdx();
		((IloTupleSchemaI*)this)->buildSharedNumColsAbsIdx();
		((IloTupleSchemaI*)this)->buildSharedSymbolColsAbsIdx();
	}
	IloIntArray getSharedIntColsAbsIdx() const{
		return _intColsAbsIdx;
	}
	IloIntArray getSharedNumColsAbsIdx() const{
		return _numColsAbsIdx;
	}
	IloIntArray getSharedSymbolColsAbsIdx() const{
		return _symbolColsAbsIdx;
	}
	IloIntArray getSharedIntColsKeyAbsKeyIdx() const{
		return _intColsKeyAbsKeyIdx;
	}
	IloIntArray getSharedNumColsKeyAbsKeyIdx() const{
		return _numColsKeyAbsKeyIdx;
	}
	IloIntArray getSharedSymbolColsKeyAbsKeyIdx() const{
		return _symbolColsKeyAbsKeyIdx;
	}

	IloIntArray getSharedPathFromAbsolutePosition(IloInt position) const;
    IloInt getInternalId(IloIntArray path);
    IloInt getInternalSubId(IloIntArray path);
	IloTuplePattern makeTuplePattern() const;
	IloBool hasSubTuple() const { return _hasSubTuple; }
	void setSubTuple(IloBool flag){ _hasSubTuple = flag; }
	void setName(const char* name);
	const char* getName() const {
		return _name;
	}

	IloColumnDefinitionArray getOrMakeTotalColumnDefinitionArray();

    IloBool isCompatibleWith(IloTupleSchemaI*) const;

	
	IloBool isSimpleTypedSchema() const;
	virtual ~IloTupleSchemaI();

	void setKeyProperty(const char* col);
	void setKeyProperty(IloSymbolI* col);
	void setKeyProperty(IloInt idx);

	IloBool hasKeyProperty(const char* col) const;
	IloBool hasKeyProperty(IloSymbolI* col) const;
	IloBool hasKeyProperty(IloInt idx) const;

	IloBool hasKey() const { return _hasKey; }

	IloTupleSchemaI* getOrMakeSharedKeySchema();
	IloIntArray getOrMakeTotalKeyIndexes();
	IloIntArray getOrMakeKeyIndexes();

	IloInt getTotalIndexFromKey(IloInt key) const;
	IloInt getIndexFromKey(IloInt key) const;

	IloInt getIndexFromTotalIndex( IloInt totalIndex ) const;

	
	IloTupleSchemaI(IloEnv env, const char* name = 0);

	
	IloInt getSize() const{
		return _array.getImpl()==NULL ? 0 : _array.getSize();
	}


	
	IloInt getTotalColumnNumber() const {
		if (!_hasSubTuple) return getArray().getSize();
		return ((IloTupleSchemaI*)this)->getOrMakeTotalColumnDefinitionArray().getSize();
	}

	
	IloColumnDefinitionI* getColumn(IloInt i) const {
		return _array[i].getImpl();
	}

	IloInt getColumnIndex(const char* name) const;
	IloInt getColumnIndex(const IloSymbolI* name) const;
	const char* getColumnName(IloInt idx) const;

	
	IloColumnDefinitionI* getColumn(IloIntArray path) const;
	//for opl TupleCellArrayFiller.
	IloColumnDefinitionI* getColumn(IloIntArray path, IloInt size) const;
	IloColumnDefinitionI* getColumn(IloInt size, IloInt* path) const;

	
	IloColumnDefinitionArray getArray() const{
		return _array;
	}


	
	void addIntColumn(const char* name =0);

	
	void addNumColumn(const char* name =0);

	void addAnyColumn(const char* name =0);

	
	void addSymbolColumn(const char* name =0);

	
	void addIntCollectionColumn(const char* name =0);
	
	void addNumCollectionColumn(const char* name =0);
	
	void addAnyCollectionColumn(const char* name =0);

	
	void addTupleColumn(IloTupleSchema ax, const char* name=0);
	
	IloTupleSchemaI* getTupleColumn(IloInt colIndex) const;
	
	IloTupleSchemaI* getTupleColumn(IloIntArray path) const;
	IloTupleSchemaI* getTupleColumn(IloInt size, IloInt* path) const;

	
	void clear();

	
	void display(ILOSTD(ostream)& out) const;

	
	IloBool isInt(IloInt index);
	
	IloBool isNum(IloInt index);
	
	IloBool isAny(IloInt index);
	
	IloBool isSymbol(IloInt index);
	
	IloBool isTuple(IloInt index);

	
	IloBool isIntCollection(IloInt index);
	
	IloBool isNumCollection(IloInt index);
	
	IloBool isAnyCollection(IloInt index);

	
	IloBool isInt(IloIntArray path);
	
	IloBool isNum(IloIntArray path);
	
	IloBool isAny(IloIntArray path);

	
	IloBool isIntCollection(IloIntArray path);
	
	IloBool isNumCollection(IloIntArray path);
	
	IloBool isAnyCollection(IloIntArray path);

	
	IloBool isSymbol(IloIntArray path);
	
	IloBool isTuple(IloIntArray path);

	
	void setData(IloInt index, IloAny data);
	
	void setData(IloIntArray path, IloAny data);
	
	IloAny getData(IloInt index);
	
	IloAny getData(IloIntArray path);

	IloTupleSchemaI* copy() const;
    virtual IloBool isOplRefCounted() const{ return IloTrue; }
	IloBool isNamed() const;
	virtual IloRttiEnvObjectI* makeOplClone(IloEnvI* env) const;
};

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif

