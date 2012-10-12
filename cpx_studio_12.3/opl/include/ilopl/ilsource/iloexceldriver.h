// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilsource/iloexceldriver.h
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


#ifndef __ADVANCED_iloexceldrvH
#define __ADVANCED_iloexceldrvH


#ifdef _WIN32
#pragma pack(push, 8)
#endif
#include <ilopl/ilosys.h>


#include <ilopl/ilsource/ilodataport.h>

#if defined(ILO_MSVC)
#if defined(ILO_WIN32) ||  (defined(ILO_MSVC8) && !defined(_IA64_))

//#pragma  warning( disable : 4146 )
#include <ilopl/ilsource/mso9.tlh>
#include <ilopl/ilsource/vbe6ext.tlh>
#include <ilopl/ilsource/EXCEL9.tlh>
//#import "ilconcert/ilsource/mso9.dll" no_namespace rename("DocumentProperties", "DocumentPropertiesXL") rename("RGB", "RBGXL")
//#import "ilconcert/ilsource/vbe6ext.olb" no_namespace rename("_Windows", "_WindowsXL") exclude()
//#import "ilconcert/ilsource/EXCEL9.OLB" rename("DialogBox", "DialogBoxXL") rename("RGB", "RBGXL") rename("DocumentProperties", "DocumentPropertiesXL") rename("CopyFile", "CopyFileXL") rename("ReplaceText", "ReplaceTextXL") no_auto_exclude


#if !defined(__ADVANCED_iloenvH)
#include <ilopl/iloenv.h>
#endif

class IloTupleCollectionI;
using namespace Excel;


class IloExcelConnectionError : public IloException {
public:
  IloExcelConnectionError();
  IloExcelConnectionError(const wchar_t* message);
public:
  virtual ~IloExcelConnectionError();
};


class IloExcelReadOnlyError : public IloException {
public:
  IloExcelReadOnlyError();
public:
  virtual ~IloExcelReadOnlyError();
};

class IloExcelInstanceError : public IloException {
  IloInt _code;
public:
  IloExcelInstanceError();
public:
  virtual ~IloExcelInstanceError();
};

class IloExcelConnection {
  enum {IloExcelErrSIZE = 1024};
  _ApplicationPtr _pXL;
  _WorkbookPtr _pBook;
  RangePtr _pRange;
  WorksheetFunctionPtr _pWsFunc;
  WorkbooksPtr _pWBooks;
  XlCalculation _calculation;
  IloBool _hasFormula;
private:
  IloExcelConnection(const IloExcelConnection&);
  void operator=(const IloExcelConnection&);
  void end();
public:
	IloBool hasFormula() const { return _hasFormula; }
//  IloExcelConnection(const char* xlsFile, const char* passwd, IloBool readOnly = IloFalse, IloBool visible = IloFalse);
  IloExcelConnection(const char* xlsFile, IloBool readOnly);
  ~IloExcelConnection();
  void setCurrentRange(const char* range);
  void setReference(long vertOffset, long horizOffset);
  IloInt getNumberOfRows();
  IloInt getNumberOfCols();

  IloInt getColNumber();
  IloInt getRowNumber();

  IloIntArray readIntArray(IloEnvI* env, long col, long line);
  IloNumArray readNumArray(IloEnvI* env, long col, long line);
  IloAnyArray readSymbolArray(IloEnvI* env, long col, long line);
  void fillTupleSet(IloTupleCollectionI* set, long i, long j);

	void write(IloIntArray ar, long i, long j);
	void write(IloNumArray ar, long i, long j);
	void write(IloAnyArray ar, long i, long j);
	void write(IloTupleCollectionI* ar, long i, long j);
  IloBool isNumber(long vertOffset, long horizOffset);
  IloBool isString(long vertOffset, long horizOffset);
  IloBool isError(long vertOffset, long horizOffset);
  IloBool isError(_variant_t& var) const;
  
  const char* getStringCell(long vertOffset, long horizOffset);//, char* leftValue);
  IloNum getFloatCell(long vertOffset, long horizOffset);
  IloInt getIntCell(long vertOffset, long horizOffset);

  void setStringCell(long vertOffset, long horizOffset, const char* value);
  void setFloatCell(long vertOffset, long horizOffset, IloNum value);
  void setIntCell(long vertOffset, long horizOffset, IloInt value);
  void save();

public:
  static char* convert(_bstr_t text) {
    char* str = 0;
    size_t length = strlen((const char*)text);
    ILOPROTECTNEW(str, new char[length+1]);
    IloStrncpy(str, length+1, (const char*)text,length);
    str[length] = '\0';
    return str;
  }

  static bool isEmptyWithoutExtraSpaces( _bstr_t str ) {
	char* s = convert( str );

	bool returnValue = IloExcelConnection::isEmptyWithoutExtraSpaces( s );

	ILOBADDELETE s;

	return returnValue;
  }

  static bool isEmptyWithoutExtraSpaces( const char* str ) {

	int strLength = strlen( str ) - 1;
	for( ; strLength >= 0; --strLength ) {
		if( str[strLength] != ' ') {
			break;
		}
	}

	return strLength == -1;
  }

};

#endif // ILO_WIN32
#endif // ILO_MSVC

class IloExcelException : public IloException {

public:

	IloExcelException( IloInt row, IloInt column )
		: _row( row ), _column( column ) {}

	IloExcelException( const IloExcelException& e )
		: _row( e._row ), _column( e._column ) {}

	virtual ~IloExcelException() {}

	IloInt getRow() const { return _row; }
	IloInt getColumn() const { return _column; }

	void addCollOffset( IloInt offset ) { _column += offset; }
	void addRowOffset( IloInt offset ) { _row += offset; }

protected:

	IloInt _row;
	IloInt _column;
};

class IloExcelBadFormatException : public IloExcelException {

public:

	enum CellType {
		LONG,
		FLOAT
	};

	IloExcelBadFormatException( CellType cellType, IloInt row, IloInt column )
		: IloExcelException( row, column ), _cellType( cellType ) {}

	virtual ~IloExcelBadFormatException() {}

	CellType getCellType() const { return _cellType; }
	const char* getCellTypeAsString() const;

	virtual void print(ILOSTD(ostream)& out) const;
	virtual const char* getMessage() const;

private:
	CellType _cellType;
};

class IloExcelCellErrorException : public IloExcelException {

public:
	IloExcelCellErrorException( IloInt row, IloInt column )
		: IloExcelException( row, column ) {}

	virtual void print(ILOSTD(ostream)& out) const;
	virtual const char* getMessage() const;

};

class IloExcelNotEnoughMemoryException : public IloExcelException {

public:
	IloExcelNotEnoughMemoryException( IloInt row, IloInt column )
		: IloExcelException( row, column ) {}

	virtual ~IloExcelNotEnoughMemoryException() {}

	virtual void print(ILOSTD(ostream)& out) const;
	virtual const char* getMessage() const;
};

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif




