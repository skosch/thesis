// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilotuplecollection.h
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

#ifndef __ADVANCED_ilotuplecollectionH
#define __ADVANCED_ilotuplecollectionH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>
#include <ilconcert/iloextractable.h>
#include <ilopl/iltuple/ilodatacolumni.h>
#include <ilopl/iltuple/ilotuplebuffer.h>
#include <ilconcert/iloanyexpri.h>

class IloTuple;
class IloTupleBuffer;
class IloTupleSetI;
class IloTupleCollectionI;
class IloTupleSchemaI;
class IloTupleRequestI;
class IloTupleRequest;
class IloTupleSchema;
class IloTupleIndex;
class IloTuplePattern;
class IloGenerator;
class IloTupleIterator;
class IloTupleExprArg;
class IloTupleSetMap;



class IloColumnDefinitionI;
class IloColumnDefinition;
class IloColumnDefinitionArray;
class IloTupleSetArray;
class IloSymbolArray;


class IloTupleSchema  {
private:
  IloTupleSchemaI* _impl;
public:
  
  IloTupleSchema(IloEnv env ,const char* name = 0);

  
  IloTupleSchema(IloTupleSchemaI* impl);

  
  IloTupleSchemaI* getImpl() const {
    return _impl;
  }

  
  void end();

 
  IloInt getSize() const;

 
  IloColumnDefinition getColumn(IloInt i) const;

 
  const char* getColumnName(IloInt idx) const;

 
  IloEnv getEnv() const;

 
  void addIntColumn(const char* name =0);

 
  void addNumColumn(const char* name =0);

 
  void addAnyColumn(const char* name =0);

 
  void addSymbolColumn(const char* name =0);

 
  void addIntCollectionColumn(const char* name =0);

 
  void addNumCollectionColumn(const char* name =0);

 
  void addAnyCollectionColumn(const char* name =0);

 
  void addTupleColumn(IloTupleSchema ax, const char* name=0);

 
  void clear();

 
  void setName(const char* name);

 
  const char* getName();

 
  IloBool isSimpleTypedSchema() const;

 
  IloInt getTotalColumnNumber() const;

 
  IloIntArray getSharedPathFromAbsolutePosition(IloInt position) const;


 
  IloInt getColumnIndex(const char* name) const;

 
  IloInt getColumnIndex(const IloSymbolI* name) const;

  
  IloColumnDefinitionI* getColumn(IloIntArray path);

  
  IloColumnDefinitionArray getArray() const;

  
  IloTupleSchema getTupleColumn(IloInt colIndex) const;

 
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
};

ILOSTD(ostream)& operator<<(ILOSTD(ostream)& out, const IloTupleSchema& s);

IloBool operator==(const IloTupleSchema schema1, const IloTupleSchema schema2);
IloBool operator!=(const IloTupleSchema schema1, const IloTupleSchema schema2);



class IloTupleCollection : public IloAnyCollection {
public:
	
	class DuplicatedException : public IloException {
	private:
		IloTupleCollectionI* _set;
		IloInt _idx;
		IloTupleCellArray _duplicate;
	public:
		DuplicatedException(const char* message, IloTupleCollectionI* set, IloTupleCellArray duplicate, IloInt index);

		DuplicatedException( const DuplicatedException& dup );

		virtual const char* getMessage() const;
		
		void print(ILOSTD(ostream)& out) const;
		~DuplicatedException();
		
		IloTupleCollectionI* getTupleCollection() const { return _set; }
		IloInt getIndex() const { return _idx; }
		
		IloTupleCellArray getTupleCellArray() const { return _duplicate; }
	};

	
	class DuplicatedKey : public DuplicatedException {
	public:
		DuplicatedKey(IloTupleCollectionI* set, IloTupleCellArray duplicate, IloInt index);
		~DuplicatedKey(){}
	};

	
	class DuplicatedTuple : public DuplicatedException {
	public:
		DuplicatedTuple(IloTupleCollectionI* set, IloTupleCellArray duplicate, IloInt index);
		~DuplicatedTuple(){}
	};


	
	class UnknownReference : public IloException {
	private:
		IloTupleCollectionI* _set;
		IloTupleCollectionI* _reference;
		IloTupleCellArray _cells;
	public:
		UnknownReference(IloTupleCollectionI* set, IloTupleCollectionI* ref, IloTupleCellArray duplicate);
		virtual const char* getMessage() const;
		
		void print(ILOSTD(ostream)& out) const;
		~UnknownReference();
		
		IloTupleCollectionI* getTupleCollection() const { return _set; }
		
		IloTupleCollectionI* getReference() const { return _reference; }
		IloTupleCellArray getTupleCellArray() const { return _cells; }
	};

public:
	
	IloTupleCollection(IloTupleCollectionI* impl);
	
	IloTupleCollectionI* getImpl() const;
	IloTupleCollection(){ _impl = 0; }
};



class IloTupleSet : public IloTupleCollection {
public:
 
	IloTupleSchema getSchema() const;

 
	void setName(const char* name);

 
	const char* getName() const;

	IloTupleSet(){ _impl = 0; }

 
	IloTupleSet(IloTupleSetI* impl);


 
	IloTupleSet(IloEnv env, const IloTupleSchema schema);
	IloTupleSet(IloEnv env, const IloTupleSchema schema, IloDataCollection::SortSense sense);

 
	IloTuple makeNext(IloTuple value, IloInt n=0) const;

 
	IloTuple makePrevious(IloTuple value, IloInt n=0) const;

 
	IloTuple makeNextC(IloTuple value, IloInt n=0) const;

 
	IloTuple makePreviousC(IloTuple value, IloInt n=0) const;

 
	IloTuple makeFirst() const;

 
	IloTuple makeLast() const;

 
	IloTupleSetI* getImpl() const;

 
	const char* getColumnName(IloInt index) const;

 
	const char* getColumnName(IloIntArray path) const;

 
	void setColumnName(IloInt index, const char* name);

 
	void setColumnName(IloIntArray path, const char* name);

 
	IloTuple makeTuple(IloInt index) const;

 
	IloTupleBuffer makeTupleBuffer(IloInt index = -1) const;

 
	IloTuplePathBuffer makeLine(IloInt index) const;

 
	IloInt commit(IloTupleBuffer line, IloBool check = IloTrue);

 
	IloInt getLength() const;

 
	IloInt getSize() const;

 
	IloTupleIterator* iterator(IloGenAlloc* heap);

	
	IloTupleIterator* iterator();

 
	void clearSelectIndexes();

 
	void createSelectIndexes();

 
	void displayRow(IloInt i, ILOSTD(ostream)& out) const;

 
	IloBool isIn(IloTupleBuffer buffer);
 
	IloTuple find(IloTupleBuffer buffer);
 
	IloInt getIndex(IloTuple tuple) const;
};



class IloTupleIterator : public IloAnyDataIterator {
private:
	IloTupleRequestI* _request;
	IloIntArrayI* _reqResult;
  IloBool _ownsReqResult;
	IloInt _index;
	void initSlice();
public:
	virtual void setCollection(const IloDiscreteDataCollectionI* coll);
   
	IloInt getIndex() const {
		return _index;
	}
protected:
   
	IloTupleIterator(IloGenAlloc* heap);

   
        void initCollection(IloTupleSetI*);
public:
	virtual ~IloTupleIterator();

   
	IloTupleIterator(IloGenAlloc* heap, IloTupleSet coll);

   
	IloTupleIterator(IloTupleSet coll);

   
	IloTupleIterator(IloGenAlloc* heap, IloTupleSet coll, IloTupleRequest req, IloBool computeSlice=IloTrue);

   
	IloTupleIterator(IloTupleSet coll, IloTupleRequest req, IloBool computeSlice=IloTrue);

  
	IloTupleSet getTupleSet() const;

   
	virtual IloTupleSchemaI* getSchema() const;

   
	void initRequest(IloTupleRequest req, IloBool computeSlice=IloTrue);

   
	void clearReqResult();

   
	virtual IloBool next();

   
	virtual void reset(IloBool catchInvalidCollection = IloFalse);

   
	virtual void reset(IloAny val, IloBool catchInvalidCollection = IloFalse);

#define AFC_FIX_TUPLEITERATOR
#ifdef AFC_FIX_TUPLEITERATOR
  
	IloTuple operator*();
#endif


#ifdef CPPREF_GENERATION
  
  IloBool ok() const {
    return _ok;
  }

  
  void operator++() {
    _ok = next();
  }
#endif
};
//
IloConstraint IloSubset(IloEnv env, IloTupleSetExprArg slice, IloTupleSetExprArg set);
IloConstraint IloSubsetEq(IloEnv env, IloTupleSetExprArg slice, IloTupleSetExprArg set);



IloIntExprArg IloOrd (IloTupleSetExprArg map, IloTupleExprArg y);



IloConstraint IloOrdered(IloTupleSetExprArg coll, IloTupleExprArg exp1, IloTupleExprArg exp2);


IloTupleExprArg IloPreviousC(IloTupleSetExprArg set, IloTupleExprArg value, IloIntExprArg n);

IloTupleExprArg IloNextC(IloTupleSetExprArg set, IloTupleExprArg value, IloIntExprArg n);


IloTupleExprArg IloPrevious(IloTupleSetExprArg set, IloTupleExprArg value, IloIntExprArg n);


IloTupleExprArg IloNext(IloTupleSetExprArg set, IloTupleExprArg value, IloIntExprArg n);


IloTupleExprArg IloItem(IloTupleSetExprArg set, IloIntExprArg n);


IloTupleExprArg IloItem(IloTupleSetExprArg set, IloTupleExprArg n);


IloTupleExprArg IloPreviousC(IloTupleSetExprArg set, IloTupleExprArg value);


IloTupleExprArg IloNextC(IloTupleSetExprArg set, IloTupleExprArg value);


IloTupleExprArg IloPrevious(IloTupleSetExprArg set, IloTupleExprArg value);


IloTupleExprArg IloNext(IloTupleSetExprArg set, IloTupleExprArg value);


IloTupleExprArg IloFirst(IloTupleSetExprArg set);


IloTupleExprArg IloLast(IloTupleSetExprArg set);


IloTupleSetExprArg IloSymExclude(IloTupleSetExprArg expr1,IloTupleSetExprArg expr2);


IloTupleSetExprArg IloUnion(IloTupleSetExprArg expr1, IloTupleSetExprArg expr2);


IloTupleSetExprArg IloExclude(IloTupleSetExprArg expr1, IloTupleSetExprArg expr2);


IloTupleSetExprArg IloInter(IloTupleSetExprArg expr1, IloTupleSetExprArg expr2);



IloIntExprArg IloCard(const IloTupleSetExprArg e);



#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
