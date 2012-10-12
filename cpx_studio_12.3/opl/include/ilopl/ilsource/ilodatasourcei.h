// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilsource/ilodatasourcei.h
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


#ifndef __ADVANCED_ilodatasourceiH
#define __ADVANCED_ilodatasourceiH

#ifdef _WIN32
#pragma pack(push, 8)
#endif
#include <ilopl/ilosys.h>


#include <ilconcert/ilocsvreader.h>
#include <ilconcert/ilosymbol.h>
#include <ilconcert/iloanyset.h>
#include <ilopl/ilotuplecollection.h>
#include <ilopl/ilotuple.h>


#ifdef ILO_MSVC
#if defined(ILO_WIN32) ||  (defined(ILO_MSVC8) && !defined(_IA64_))
#include <ilopl/ilsource/ilodrvsheet.h>

//#include <stdio.h>
//#include <wtypes.h>
//#include <comdef.h>
#endif
#endif

class IloCsvTableReaderI;
class IloCsvTableReader;

class IloCsvLineI;

class IloAbstractTupleMapI;




class IloStringDataSourceI : public IloRttiEnvObjectI{
	ILORTTIDECL
public:
	
	IloStringDataSourceI(IloEnv env);
	~IloStringDataSourceI();

	
	IloIntArray getIntArray(const char* str);
	
	IloNumArray getNumArray(const char* str);
	
	IloSymbolArray getSymbolArray(const char* str);

	
	IloIntSet getIntSet(const char* str);
	
	IloNumSet getNumSet(const char* str);
	
	IloSymbolSet getSymbolSet(const char* str);

	
	IloInt getInt(const char* str);
	
	IloNum getNum(const char* str);
	
	IloSymbol getSymbol(const char* str);

	
	IloIntArray2 getIntArray2(const char* str);
	
	IloNumArray2 getNumArray2(const char* str);
};


class IloCsvDataSourceI : public IloRttiEnvObjectI{
	ILORTTIDECL
private:
	void closeConnexion();
	IloCsvReader _reader;
public:
	
	IloCsvDataSourceI(IloEnv env, const char* fileName);
	~IloCsvDataSourceI();

	
	IloIntArray getIntArrayFromLine(const char* tableName, IloInt line);
	
	IloIntArray getIntArrayFromColumn(const char* tableName, IloInt column);
	
	IloIntSet getIntSetFromLine(const char* tableName, IloInt line);
	
	IloIntSet getIntSetFromColumn(const char* tableName, IloInt column);

	
	IloNumArray getNumArrayFromLine(const char* tableName, IloInt line);
	
	IloNumArray getNumArrayFromColumn(const char* tableName, IloInt column);
	
	IloNumSet getNumSetFromLine(const char* tableName, IloInt line);
	
	IloNumSet getNumSetFromColumn(const char* tableName, IloInt column);

	
	IloSymbolArray getSymbolArrayFromLine(const char* tableName, IloInt line);
	
	IloSymbolArray getSymbolArrayFromColumn(const char* tableName, IloInt column);
	
	IloSymbolSet getSymbolSetFromLine(const char* tableName, IloInt line);
	
	IloSymbolSet getSymbolSetFromColumn(const char* tableName, IloInt column);

	
	IloInt getIntFromTable(const char* tableName, IloInt x, IloInt y);
	
	IloNum getNumFromTable(const char* tableName, IloInt x, IloInt y);
	
	IloSymbol getSymbolFromTable(const char* tableName, IloInt x, IloInt y);

	
	IloTupleSet getTupleSet(IloTupleSchema schema, const char* tableName);

};

#ifdef ILO_MSVC
#if defined(ILO_WIN32) || defined(ILO_MSVC8)

class IloCOMInitializer {
public:
	IloCOMInitializer();
	~IloCOMInitializer();
};


class IloExcelDataSourceI : public IloRttiEnvObjectI{
	ILORTTIDECL
private:
	void closeConnexion();
	IloExcelSpreadsheet _reader;
	IloInt getIntCell(IloInt x, IloInt y);
	IloIntArray readIntArray(IloEnvI* env, IloInt i, IloInt j);
	IloNumArray readNumArray(IloEnvI* env, IloInt i, IloInt j);
	IloAnyArray readSymbolArray(IloEnvI* env, IloInt i, IloInt j);
    void fillTupleSet(IloTupleCollectionI* set, IloInt i, IloInt j);

	void write(IloIntArray ar, IloInt i, IloInt j);
	void write(IloNumArray ar, IloInt i, IloInt j);
	void write(IloAnyArray ar, IloInt i, IloInt j);
	void write(IloTupleCollectionI* ar, IloInt i, IloInt j);
	IloNum getFloatCell(IloInt x, IloInt y);
	const char* getStringCell(IloInt x, IloInt y);

	IloCOMInitializer _comInitializer;
public:
	IloExcelDataSourceI(IloEnv env, const char* fileName, IloBool readOnly = IloFalse);
	~IloExcelDataSourceI();

	IloInt getIntFromTable(const char* range, IloInt x, IloInt y);
	IloNum getNumFromTable(const char* range, IloInt x, IloInt y);
	IloSymbol getSymbolFromTable(const char* range, IloInt x, IloInt y);

    
    void readIntMap(IloMapI* result, const char* range);
    
    void readNumMap(IloMapI* result, const char* range);
    
    void readSymbolMap(IloMapI* result, const char* range);
    
    void readTupleMap(IloAbstractTupleMapI* result, const char* range);

    
    void readIntSet(IloIntSetI* result, const char* range);

    
    void readNumSet(IloNumSetI* result, const char* range);

    
    void readSymbolSet(IloAnySetI* result, const char* range);

    
    void readTupleSet(IloTupleSetI* result, const char* range);


    
    void writeIntSet(IloIntSetI* set, const char* range);

    
    void writeNumSet(IloNumSetI* set, const char* range);

    
    void writeSymbolSet(IloAnySetI* set, const char* range);

    
    void writeTupleSet(IloTupleSetI* set, const char* range);



    
    void writeInt(IloInt val, const char* range);

    
    void writeNum(IloNum val, const char* range);

    
    void writeSymbol(IloSymbolI* val, const char* range);

    
    void writeTuple(IloTupleI* tuple, const char* range);


    
    template <class MapClass, class ArrayClass>
    void writeMap_helper(IloMapI* set, const char* range);
    
    void writeIntMap(IloMapI* set, const char* range);
    
    void writeNumMap(IloMapI* set, const char* range);
    
    void writeSymbolMap(IloMapI* set, const char* range);
    
    void writeTupleMap(IloAbstractTupleMapI* set, const char* range);

	void save();
};

#endif //ILO_WIN32
#endif //ILO_MSVC


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif


