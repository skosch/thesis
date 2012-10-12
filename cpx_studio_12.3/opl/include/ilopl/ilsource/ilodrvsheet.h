// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilsource/ilodrvsheet.h
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


#ifndef __ADVANCED_ilodrvsheetH
#define __ADVANCED_ilodrvsheetH

#ifdef _WIN32
#pragma pack(push, 8)
#endif
#include <ilopl/ilosys.h>


class IloExcelSheetFactory;
class IloExcelSpreadsheet;
class IloTupleSetI;
class IloTupleCollectionI;

class IloIntArray;
class IloNumArray;
class IloAnyArray;


class IloExcelSpreadsheetI {
  friend class IloExcelSpreadsheet;
  long _ref;
  void lock()  { _ref++;}
  long unlock() { return --_ref;}
public:
  IloExcelSpreadsheetI() : _ref(0) {}
  virtual ~IloExcelSpreadsheetI()  {}
  virtual void setCurrentRange(const char* range) = 0;
  virtual void setReference(long row,long col) = 0;
  virtual long  getNumberOfRows() = 0;
  virtual long  getNumberOfCols() = 0;

  virtual long getColNumber() = 0;
  virtual long getRowNumber() = 0;

  virtual long isNumber(long row,long col) = 0;
  virtual long isString(long row,long col) = 0;
  virtual long isError(long row,long col) = 0;
  virtual const char* getStringCell(long row,long col) = 0;
  virtual IloNum getFloatCell(long row,long col) = 0; 
  virtual long   getIntCell(long row,long col) = 0;

	virtual IloIntArray readIntArray(IloEnvI* env, IloInt col, IloInt line) = 0;
	virtual IloNumArray readNumArray(IloEnvI* env, IloInt col, IloInt line) = 0;
	virtual IloAnyArray readSymbolArray(IloEnvI* env, IloInt col, IloInt line) = 0;
	virtual void fillTupleSet(IloTupleCollectionI* set, IloInt i, IloInt j) = 0;

	virtual void write(IloIntArray ar, IloInt i, IloInt j) = 0;
	virtual void write(IloNumArray ar, IloInt i, IloInt j) = 0;
	virtual void write(IloAnyArray ar, IloInt i, IloInt j) = 0;
	virtual void write(IloTupleCollectionI* ar, IloInt i, IloInt j) = 0;

  virtual void setStringCell(long row,long col, const char* val) = 0;
  virtual void setFloatCell(long row,long col,IloNum val) = 0;
  virtual void setIntCell(long row,long col,long val) = 0;
  virtual void save() = 0;
};



class IloExcelSpreadsheet {
  IloExcelSpreadsheetI* _sheet;
public:
  void save() { _sheet->save(); }
  void release();
  IloExcelSpreadsheet(IloExcelSpreadsheetI* s=0);
  IloExcelSpreadsheet(const IloExcelSpreadsheet& s);
  ~IloExcelSpreadsheet(void);
  IloExcelSpreadsheet& operator=(const IloExcelSpreadsheet& s);
  IloExcelSpreadsheetI* operator->() { return _sheet;}
  const IloExcelSpreadsheetI* operator->() const { return _sheet;}
  operator long() const { return _sheet !=0;}
};


class IloExcelSheetFactory {
public:
  IloExcelSheetFactory() {}
  virtual ~IloExcelSheetFactory() {}
  IloExcelSpreadsheet openSheet(const char* name, IloBool readOnly);
};


class IloExcelSSConnectionError {
  long     _code;
  char* _message;
  void setMessage(const char* message){
    if (_message!=0) {
      delete [] _message;
    }
    if (message!=0) {
      IloInt length = strlen(message);
      _message = new char[length+1];
      memcpy((char *)_message, message, length+1);
    }
    else
      _message = 0;
  }
public:
  IloExcelSSConnectionError(long c,const char* msg) : _code(c),_message(0){
    setMessage(msg);
  }
  long getCode() const               { return _code;}
  const char* getMessage() const { return _message;}
};

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
