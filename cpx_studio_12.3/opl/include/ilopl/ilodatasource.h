// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilodatasource.h
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

#ifndef __ADVANCED_ilodatasourceH
#define __ADVANCED_ilodatasourceH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>
#include <ilopl/ilsource/ilodatasourcei.h>




class IloStringDataSource {
private:
	IloStringDataSourceI* _impl;
public:
	
	IloStringDataSource(IloEnv env);
	
	IloStringDataSource(): _impl(0) {}

	
	IloStringDataSourceI* getImpl() const { return _impl; }
	
	IloStringDataSource(IloStringDataSourceI* impl): _impl(impl) {}

	
	void end(){
		if (_impl){
			delete _impl;
			_impl = 0;
		}
	}

	
	IloIntArray getIntArray(const char* str);

	
	IloNumArray getNumArray(const char* str);
	
	IloSymbolArray getSymbolArray(const char* str);

	
	IloIntSet getIntSet(const char* str);

	
	IloNumSet getNumSet(const char* str);
	
	IloSymbolSet getSymbolSet(const char* str);

	IloInt getInt(const char* str);
	IloNum getNum(const char* str);
	IloSymbol getSymbol(const char* str);
};


class IloCsvDataSource {
private:
	IloCsvDataSourceI* _impl;
public:
	
	IloCsvDataSource(IloEnv env, const char* fileName);
	
	IloCsvDataSource(): _impl(0) {}

	IloCsvDataSourceI* getImpl() const { return _impl; }
	
	IloCsvDataSource(IloCsvDataSourceI* impl): _impl(impl) {}


	
	void end(){
		if (_impl){
			delete _impl;
			_impl = 0;
		}
	}

	
	IloIntSet getIntSetFromLine(const char* tableName, IloInt line = 0);
	
	IloIntSet getIntSetFromColumn(const char* tableName, IloInt column = 1);

	
	IloNumSet getNumSetFromLine(const char* tableName, IloInt line = 0);
	
	IloNumSet getNumSetFromColumn(const char* tableName, IloInt column = 1);
	
	IloSymbolSet getSymbolSetFromLine(const char* tableName, IloInt line = 0);
	
	IloSymbolSet getSymbolSetFromColumn(const char* tableName, IloInt column = 1);


	
	IloIntArray getIntArrayFromLine(const char* tableName, IloInt line = 0);
	
	IloIntArray getIntArrayFromColumn(const char* tableName, IloInt column = 1);

	
	IloNumArray getNumArrayFromLine(const char* tableName, IloInt line = 0);
	
	IloNumArray getNumArrayFromColumn(const char* tableName, IloInt column = 1);
	
	IloSymbolArray getSymbolArrayFromLine(const char* tableName, IloInt line = 0);
	
	IloSymbolArray getSymbolArrayFromColumn(const char* tableName, IloInt column = 1);


	
	IloInt getIntFromTable(const char* tableName, IloInt x, IloInt y);

	
	IloNum getNumFromTable(const char* tableName, IloInt x, IloInt y);
	
	IloSymbol getSymbolFromTable(const char* tableName, IloInt x, IloInt y);

	
	IloTupleSet getTupleSet(IloTupleSchema schema, const char* tableName);
};


#ifdef ILO_MSVC
#if defined(ILO_WIN32) || defined(ILO_MSVC8)
/////////////////


class IloExcelDataSource {
private:
	IloExcelDataSourceI* _impl;
public:
	
	IloExcelDataSource(IloEnv env, const char* fileName, IloBool readBolny = IloFalse);
	
	IloExcelDataSource(): _impl(0) {}

	IloExcelDataSourceI* getImpl() const { return _impl; }
	
	IloExcelDataSource(IloExcelDataSourceI* impl): _impl(impl) {}


	
	void end(){
		if (_impl){
			delete _impl;
			_impl = 0;
		}
	}

	
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


