// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilotuple.h
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


#ifndef __ADVANCED_ilotupleH
#define __ADVANCED_ilotupleH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>
#include <ilopl/ilotuplecollection.h>
#include <ilconcert/ilostring.h>
#include <ilopl/ilosymbol.h>
#include <ilopl/iloexpressioni.h>

class IloTuple;

class IloTupleCollectionI;
class IloTupleDataColumnI;
class IloTupleBufferI;
class IloTuplePatternI;

class IloIntCollectionExprArg;
class IloNumCollectionExprArg;
class IloSymbolCollectionExprArg;

class IloIntMap;
class IloNumMap;

class IloOplObject;


//-------------------------------------------------
// Tuple knows the collection + a row index
//-------------------------------------------------

class IloTupleI : public IloRttiEnvObjectI {
  ILORTTIDECL
protected:
  IloTupleCollectionI* _coll;
  IloInt _index;
  IloIntArray makeColumnIndexArray(IloStringArray name) const;

  friend class IloTupleIterator;
  void reuse(IloTupleCollectionI* coll) { _coll = coll; _index = 0; }
public:
  friend class IloTupleBufferI;
  virtual ~IloTupleI();

  IloTuplePatternI* makeConstPattern() const;


  
  IloTupleBufferI* makeTupleBuffer() const;
  
  IloBool equal(IloTupleI*);

  
  IloTupleI(IloEnvI* env, IloTupleCollectionI* coll, IloInt i=-1);

  void setCollection(IloTupleCollectionI* coll){ _coll = coll;}
  
  const IloTupleCollectionI* getCollection() const { return _coll; }

  
  IloTupleSchemaI* getSchema() const;

  
  IloInt getColumnIndex(const char* name) const;

  
  IloInt getColumnIndex(const IloSymbolI* s) const;

  
  IloInt getIndex() const { return _index; }

  
  void setIndex(IloInt i);

  
  virtual IloInt getIntValue(const char* col) const;

  
  virtual IloNum getNumValue(const char* col) const;

  
  virtual IloAny getAnyValue(const char* col) const;

  
  virtual IloSymbol getSymbolValue(const char* col) const;

  
  virtual const char* getStringValue(const char* col) const;

  
  virtual IloTupleI* makeTupleValue(const char* col) const;

  
  virtual IloIntCollectionI* getIntCollectionValue(const char* col) const;

  
  virtual IloNumCollectionI* getNumCollectionValue(const char* col) const;

  
  virtual IloAnyCollectionI* getAnyCollectionValue(const char* col) const;

  
  virtual IloInt getIntValue(IloInt index) const;

  
  virtual IloNum getNumValue(IloInt index) const;

  
  virtual IloAny getAnyValue(IloInt index) const;

  
  virtual IloIntCollectionI* getIntCollectionValue(IloInt index) const;

  
  virtual IloNumCollectionI* getNumCollectionValue(IloInt index) const;

  
  virtual IloAnyCollectionI* getAnyCollectionValue(IloInt index) const;

  
  virtual IloSymbol getSymbolValue(IloInt index) const;

  
  virtual const char* getStringValue(IloInt index) const;

  
  virtual IloTupleI* makeTupleValue(IloInt index) const;

  //
  
  virtual IloInt getIntValue(IloIntArray path) const;

  
  virtual IloNum getNumValue(IloIntArray path) const;

  
  virtual IloAny getAnyValue(IloIntArray path) const;

  
  virtual IloIntCollectionI* getIntCollectionValue(IloIntArray path) const;

  
  virtual IloNumCollectionI* getNumCollectionValue(IloIntArray path) const;

  
  virtual IloAnyCollectionI* getAnyCollectionValue(IloIntArray path) const;

  
  virtual IloSymbol getSymbolValue(IloIntArray path) const;

  
  virtual const char* getStringValue(IloIntArray path) const;

  
  virtual IloTupleI* makeTupleValue(IloIntArray path) const;


  virtual void display(ILOSTD(ostream)& out) const;
  virtual IloBool isTupleBuffer() const { return IloFalse; }
  virtual IloOplObject getMapItem(IloIntArray path) const;
  virtual IloBool isOplRefCounted() const{ return IloTrue; }
  virtual IloTupleCellArray makeTupleCells();
  virtual IloTupleI* makeClone(IloEnvI* env) const;
  virtual IloRttiEnvObjectI* makeOplClone(IloEnvI* env) const{
	  return (IloRttiEnvObjectI*)makeClone(env);
  }
};

class IloTupleExprI : public IloSchemaExprI {
	ILOEXTRDECL
public:
	IloTupleExprI(IloEnvI* env, const char* name=0) : IloSchemaExprI(env, name) {}
	virtual IloBool isTupleIndex() const{ return IloFalse;}
	virtual IloBool isTuplePattern() const{ return IloFalse;}
	virtual ~IloTupleExprI(){}
};

//-------------------------------------------
class IloTupleConstI : public IloTupleExprI {
  ILOEXTRDECL
private:
  IloTupleI* _tuple;
  void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloTupleConstI(IloEnvI* env, IloTupleI* tuple);
  virtual ~IloTupleConstI();
  IloTupleI* getTuple() const { return _tuple; }
  ILOEXTROTHERDECL
};



class ILO_EXPORTED IloTuple {
protected:
   IloTupleI* _impl;
 public:

   virtual ~IloTuple(){}
   IloTuple():_impl(0){}
   IloTuple(IloTupleI* impl):_impl(impl){}
   IloTupleI* getImpl() const { return _impl; }
   
   void end(){
     if (_impl){
       delete _impl; _impl = 0;
     }
   }

  
   IloTupleBuffer makeTupleBuffer() const;

   
   IloEnv getEnv() const;

   
    void display(ILOSTD(ostream)& outs) const;

   
   IloInt getIndex() const;

   
   IloTupleCollection getCollection() const;

   
   IloTupleSchema getSchema() const {
     IloAssert(getImpl(), "Using empty IloTuple handle");
     return getImpl()->getSchema();
   }

   
   void setIndex(IloInt i);

   
   IloInt getIntValue(IloInt index) const;

   

   IloNum getNumValue(IloInt index) const;

   
   IloAny getAnyValue(IloInt index) const;

   
   IloIntCollection getIntCollectionValue(IloInt index) const;

   
   IloNumCollection getNumCollectionValue(IloInt index) const;

   
   IloIntSet getIntSetValue(IloInt index) const;

   
   IloNumSet getNumSetValue(IloInt index) const;

   
   IloSymbolSet getSymbolSetValue(IloInt index) const;

   
   IloIntMap getIntMapValue(IloInt index) const;

   
   IloNumMap getNumMapValue(IloInt index) const;

   
   IloAnyCollection getAnyCollectionValue(IloInt index) const;

   
   IloSymbol getSymbolValue(IloInt index) const;

   
   const char* getStringValue(IloInt index) const;

   
   IloTuple makeTupleValue(IloInt index) const;

   
   IloInt getIntValue(IloIntArray path) const;

   
   IloNum getNumValue(IloIntArray path) const;

   
   IloAny getAnyValue(IloIntArray path) const;

   
   IloIntCollection getIntCollectionValue(IloIntArray path) const;

   
   IloIntSet getIntSetValue(IloIntArray path) const;

   
   IloNumSet getNumSetValue(IloIntArray path) const;

   
   IloSymbolSet getSymbolSetValue(IloIntArray path) const;

   
   IloIntMap getIntMapValue(IloIntArray path) const;

   
   IloNumCollection getNumCollectionValue(IloIntArray path) const;

   
   IloNumMap getNumMapValue(IloIntArray path) const;

   
   IloAnyCollection getAnyCollectionValue(IloIntArray path) const;

   
   IloSymbol getSymbolValue(IloIntArray path) const;

  
   const char* getStringValue(IloIntArray path) const;

  
   IloTuple makeTupleValue(IloIntArray path) const;

   
   IloInt getIntValue(const char* col) const;

   
   IloNum getNumValue(const char* col) const;

   
   IloAny getAnyValue(const char* col) const;

   
   IloSymbol getSymbolValue(const char* col) const;

   
   const char* getStringValue(const char* col) const;

  
   IloTuple makeTupleValue(const char* col) const;

  
   IloIntCollection getIntCollectionValue(const char* col) const;

   
   IloIntSet getIntSetValue(const char* col) const;

   
   IloNumSet getNumSetValue(const char* col) const;

   
   IloSymbolSet getSymbolSetValue(const char* col) const;

   
   IloIntMap getIntMapValue(const char* col) const;

   
   IloNumCollection getNumCollectionValue(const char* col) const;

   
   IloNumMap getNumMapValue(const char* col) const;

   
   IloAnyCollection getAnyCollectionValue(const char* col) const;


   
   IloOplObject getMapItem(IloIntArray path) const;
 };

inline ILOSTD(ostream)& operator<<(ILOSTD(ostream)& out, const IloTuple& t){
  IloAssert(t.getImpl(), "Use empty handle.");
  t.getImpl()->display(out);
  return (out);
}


//-------------------------------------------------
// Tuple knows the collection + a row index
//-------------------------------------------------

class IloTupleBufferI : public IloTupleI {
  ILORTTIDECL
private:
  IloTuplePathBuffer _buffer;
public:
  virtual ~IloTupleBufferI(){
    _buffer.end();
  }

  
  void endAll();

  
  void display(ILOSTD(ostream)& out) const;

  
  void import(IloTupleI* tuple);
  void add(IloTupleBufferI* tuple);

  IloTupleBufferI(IloEnvI* env, IloTupleCollectionI* coll, IloTupleCellArray values);

  
  IloTupleBufferI(IloEnvI* env, IloTupleCollectionI* coll, IloInt i=-1);

  
  void setIntValue(IloInt columnIndex, IloInt value);

  
  void setNumValue(IloInt columnIndex, IloNum value);

  
  void setAnyValue(IloInt columnIndex, IloAny value);

  
  void setSymbolValue(IloInt columnIndex, IloSymbol value);

  
  void setSymbolValue(IloInt columnIndex, const char* value){
		setSymbolValue(columnIndex, getEnv()->makeSymbol(value));
  }

  
  void setTupleValue(IloInt columnIndex, IloTuple value);

  
  void setIntCollectionValue(IloInt columnIndex, IloIntCollectionI* value);

  
  void setNumCollectionValue(IloInt columnIndex, IloNumCollectionI* value);

  
  void setAnyCollectionValue(IloInt columnIndex, IloAnyCollectionI* value);

  
  void setIntValue(const char* col, IloInt value);

  
  void setNumValue(const char* col, IloNum value);

  
  void setAnyValue(const char* col, IloAny value);

  
  void setSymbolValue(const char* col, IloSymbol value);
  void setSymbolValue(const char* col, const char* value){
       setSymbolValue(col, getEnv()->makeSymbol(value));
  }

  
  void setTupleValue(const char* col, IloTuple value);

  
  void setIntCollectionValue(const char* col, IloIntCollectionI* value);

  
  void setNumCollectionValue(const char* col, IloNumCollectionI* value);

  
  void setAnyCollectionValue(const char* col, IloAnyCollectionI* value);


  //
  
  void setIntValue(IloIntArray path, IloInt value);

  
  void setNumValue(IloIntArray path, IloNum value);

  
  void setAnyValue(IloIntArray path, IloAny value);

  
  void setIntCollectionValue(IloIntArray path, IloIntCollectionI* value);

  
  void setNumCollectionValue(IloIntArray path, IloNumCollectionI* value);

  
  void setAnyCollectionValue(IloIntArray path, IloAnyCollectionI* value);

  
  void setSymbolValue(IloIntArray path, IloSymbol value);
  void setSymbolValue(IloIntArray path, const char* value){
	  setSymbolValue(path, getEnv()->makeSymbol(value));
  }

  
  void setTupleValue(IloIntArray path, IloTuple value);


  
  void setIntValue(IloInt pathSize, IloInt* path, IloInt value);

  
  void setNumValue(IloInt pathSize, IloInt* path, IloNum value);

  
  void setAnyValue(IloInt pathSize, IloInt* path, IloAny value);

  
  void setSymbolValue(IloInt pathSize, IloInt* path, IloSymbol value);
  void setSymbolValue(IloInt pathSize, IloInt* path, const char* value){
	  setSymbolValue(pathSize, path, getEnv()->makeSymbol(value));
  }

  
  void setTupleValue(IloInt pathSize, IloInt* path, IloTuple value);

  void setIntCollectionValue(IloInt pathSize, IloInt* path, IloIntCollectionI* value);
  void setNumCollectionValue(IloInt pathSize, IloInt* path, IloNumCollectionI* value);
  void setAnyCollectionValue(IloInt pathSize, IloInt* path, IloAnyCollectionI* value);

  
  virtual IloInt getIntValue(const char* col) const;

  
  virtual IloNum getNumValue(const char* col) const;

  
  virtual IloAny getAnyValue(const char* col) const;

  
  virtual IloSymbol getSymbolValue(const char* col) const;

  
  virtual const char* getStringValue(const char* col) const;

  
  virtual IloTupleI* makeTupleValue(const char* col) const;

  
  virtual IloIntCollectionI* getIntCollectionValue(const char* col) const;

  
  virtual IloNumCollectionI* getNumCollectionValue(const char* col) const;

  
  virtual IloAnyCollectionI* getAnyCollectionValue(const char* col) const;

  
  virtual IloInt getIntValue(IloInt index) const;

  
  virtual IloNum getNumValue(IloInt index) const;

  
  virtual IloAny getAnyValue(IloInt index) const;

  
  virtual IloIntCollectionI* getIntCollectionValue(IloInt index) const;

  
  virtual IloNumCollectionI* getNumCollectionValue(IloInt index) const;

  
  virtual IloAnyCollectionI* getAnyCollectionValue(IloInt index) const;

  
  virtual IloSymbol getSymbolValue(IloInt index) const;

  
  virtual const char* getStringValue(IloInt index) const;

  
  virtual IloTupleI* makeTupleValue(IloInt index) const;

  //
  
  virtual IloInt getIntValue(IloIntArray path) const;

  
  virtual IloNum getNumValue(IloIntArray path) const;

  
  virtual IloAny getAnyValue(IloIntArray path) const;

  
  virtual IloIntCollectionI* getIntCollectionValue(IloIntArray path) const;

  
  virtual IloNumCollectionI* getNumCollectionValue(IloIntArray path) const;

  
  virtual IloAnyCollectionI* getAnyCollectionValue(IloIntArray path) const;

  
  virtual IloSymbol getSymbolValue(IloIntArray path) const;

  
  virtual const char* getStringValue(IloIntArray path) const;

  
  virtual IloTupleI* makeTupleValue(IloIntArray path) const;

  //
  
  IloTuplePathBuffer getLocalBuffer();

  
  IloInt commit();

  
  void clear();
  virtual IloBool isTupleBuffer() const { return IloTrue; }
  virtual IloOplObject getMapItem(IloIntArray path) const;
  virtual IloTupleCellArray makeTupleCells();
  virtual IloTupleI* makeClone(IloEnvI* env) const;
};



class ILO_EXPORTED IloTupleBuffer : public IloTuple {
public:
 
  void clear();

 
  void display(ILOSTD(ostream)& outs) const;

  IloTupleBuffer():IloTuple(0){}

  IloTupleBuffer(IloTupleBufferI* impl):IloTuple(impl){}

  IloTupleBufferI* getImpl() const {
    return (IloTupleBufferI*)IloTuple::getImpl();
  }

 
  void setIntValue(IloInt columnIndex, IloInt value);

 
  void setNumValue(IloInt columnIndex, IloNum value);

 
  void setAnyValue(IloInt columnIndex, IloAny value);

  
  void setIntCollectionValue(IloInt columnIndex, IloIntCollection value);

 
  void setNumCollectionValue(IloInt columnIndex, IloNumCollection value);

 
  void setAnyCollectionValue(IloInt columnIndex, IloAnyCollection value);

 
  void setIntMapValue(IloInt columnIndex, IloIntMap value);

 
  void setNumMapValue(IloInt columnIndex, IloNumMap value);

 
  void setSymbolValue(IloInt columnIndex, const char* value);

 
  void setSymbolValue(IloInt columnIndex, IloSymbol value);

 
  void setTupleValue(IloInt columnIndex, IloTuple value);

 
  void setIntValue(const char* col, IloInt value);

 
  void setNumValue(const char* col, IloNum value);

  
  void setAnyValue(const char* col, IloAny value);
 
  void setSymbolValue(const char* col, const char* value);
 
  void setSymbolValue(const char* col, IloSymbol value);
 
  void setTupleValue(const char* col, IloTuple value);

 
  void setIntCollectionValue(const char* col, IloIntCollection value);

 
  void setNumCollectionValue(const char* col, IloNumCollection value);

 
  void setAnyCollectionValue(const char* col, IloAnyCollection value);

 
  void setIntMapValue(const char* col, IloIntMap value);

 
  void setNumMapValue(const char* col, IloNumMap value);

 
  void setIntCollectionValue(IloIntArray path, IloIntCollection value);

 
  void setNumCollectionValue(IloIntArray path, IloNumCollection value);
 
  void setAnyCollectionValue(IloIntArray path, IloAnyCollection value);

 
  void setIntMapValue(IloIntArray path, IloIntMap value);

 
  void setNumMapValue(IloIntArray path, IloNumMap value);

 
  void setIntValue(IloIntArray path, IloInt value);

 
  void setNumValue(IloIntArray path, IloNum value);

  
  void setAnyValue(IloIntArray path, IloAny value);

 
  void setSymbolValue(IloIntArray path, const char* value);

 
  void setSymbolValue(IloIntArray path, IloSymbol value);

 
  void setTupleValue(IloIntArray path, IloTuple value);

  
  IloTuplePathBuffer getLocalBuffer();

  
  IloInt commit();

#ifdef CPPREF_GENERATION
 
  void end();
#endif
};


// --------------------------------------------------------------
// Tuple Expressions
// --------------------------------------------------------------
#define IloTupleExprArgI IloTupleExprI

//-----------------------------------------------------------------

class ILO_EXPORTED IloTupleExprArg : public IloExtractable {
  ILOEXTRHANDLE(IloTupleExprArg, IloExtractable)
public:
	
	IloIntExprArg getInt(const char* colName) const;
	
	IloNumExprArg getNum(const char* colName) const;
	
	IloSymbolExprArg getSymbol(const char* colName) const;
	
	IloTupleExprArg getTuple(const char* colName) const;

	
	IloIntCollectionExprArg getIntCollection(const char* colName) const;
	
	IloNumCollectionExprArg getNumCollection(const char* colName) const;
	
	IloSymbolCollectionExprArg getSymbolCollection(const char* colName) const;
};

// --------------------------------------------------------------
// Tuple Constraints
// --------------------------------------------------------------

//-----------------------------------------------------------------

IloConstraint operator==(const IloTupleExprArg expr1,
			 const IloTupleExprArg expr2);


IloConstraint operator==(const IloTupleExprArg expr, IloTuple value);


inline IloConstraint operator==(IloTuple val, const IloTupleExprArg expr){
  return (expr == val);
}


IloConstraint operator!=(const IloTupleExprArg expr1,
			 const IloTupleExprArg expr2);


IloConstraint operator!=(const IloTupleExprArg expr, IloTuple value);


inline IloConstraint operator!=(IloTuple val, const IloTupleExprArg expr){
  return (expr != val);
}

// --------------------------------------------------------------
// HashTable with IloTuple keys
// --------------------------------------------------------------


extern IloBool IloTupleCompFunction(IloTuple key1, IloTuple key2);

extern IloInt IloTupleHashCodeFunction(IloTuple key);


//-------------------------------------------------------------
class IloConditionalTupleExprI 
	: public IloConditionalExprI< IloTupleExprI, IloConditionalTupleExprI, IloFalse >  {

	ILOEXTRDECL

public:
	IloConditionalTupleExprI(IloEnvI* env, IloConstraintI* cond, IloTupleExprI* left, IloTupleExprI* right )
	  : IloConditionalExprI< IloTupleExprI, IloConditionalTupleExprI, IloFalse >( env, cond, left, right ) {
		setSchema( left->getSchema() );
	}
};

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
