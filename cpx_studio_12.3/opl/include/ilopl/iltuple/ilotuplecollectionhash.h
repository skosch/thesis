// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/iltuple/ilotuplecollectionhash.h
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


#ifndef __ADVANCED_ilotuplecollectionhashH
#define __ADVANCED_ilotuplecollectionhashH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>

#include <ilconcert/ilohash.h>
#include <ilopl/iltuple/ilotuplerequest.h>
#include <ilconcert/iloset.h>
#include <ilconcert/iloanyset.h>

#define ILO_MEMORY_OBJECT IloMemoryManagerObjectI

class IloRequestSignatureI;
class IloTupleRequestI;


template <class KeyType>
class IloTupleSetHash : public IloEnvHashTable<KeyType, IloIntArrayI*>{
public:
	typedef IloInt (*IloHashFunction) (KeyType, IloInt);
	typedef IloBool (*IloCompFunction) (KeyType, KeyType);

	
	IloTupleSetHash(IloEnv env,
		IloHashFunction hashFct,
		IloCompFunction compFct,
		IloInt size=IloDefaultHashSize)
		:IloEnvHashTable<KeyType, IloIntArrayI*>(env, hashFct,compFct, size) {}
		~IloTupleSetHash(){
		}
};


extern IloBool IloAnyCollectionCompFunction(IloAny key1, IloAny key2);

extern IloBool IloIntCollectionCompFunction(IloAny key1, IloAny key2);

extern IloBool IloNumCollectionCompFunction(IloAny key1, IloAny key2);



class IloIntDataTableHash : public IloTupleSetHash<IloInt> {
public:
	
	IloIntDataTableHash(IloEnv env, IloInt size=IloDefaultHashSize)
		:IloTupleSetHash<IloInt>(env,
		IloIntDataTableHashFunction,
		IloIntDataTableCompFunction,
		size) {}
		~IloIntDataTableHash() {}
};




class IloNumDataTableHash : public IloTupleSetHash<IloNum> {
public:
	
	IloNumDataTableHash(IloEnv env, IloInt size=IloDefaultHashSize)
		:IloTupleSetHash<IloNum>(env,
		IloNumDataTableHashFunction,
		IloNumDataTableCompFunction,
		size) {}
		~IloNumDataTableHash() {}
};


class IloAnyDataTableHash : public IloTupleSetHash<IloAny> {
public:
	typedef IloInt (*IloHashFunction) (IloAny, IloInt);
	typedef IloBool (*IloCompFunction) (IloAny, IloAny);
	
	IloAnyDataTableHash(IloEnv env, IloHashFunction fct1, IloCompFunction fct2, IloInt size=IloDefaultHashSize)
		:IloTupleSetHash<IloAny>(env,
		fct1,
		fct2,
		size) {}
		~IloAnyDataTableHash() {}
};


extern IloInt  IloVBHashFunction(IloRequestSignatureI*, IloInt size);
extern IloBool IloVBCompFunction(IloRequestSignatureI* key1, IloRequestSignatureI* key2);


class IloVBHash : public IloEnvHashTable<IloRequestSignatureI*, ILO_MEMORY_OBJECT*>{
public:
	
	IloVBHash(IloEnv env, IloInt size=IloDefaultHashSize)
		:IloEnvHashTable<IloRequestSignatureI*, ILO_MEMORY_OBJECT*>(env,
		IloVBHashFunction,
		IloVBCompFunction,
		size) {}
		~IloVBHash() {}
};



//extern IloInt  IloTupleRequestHashFunction(IloTupleRequestI*, IloInt size);
//extern IloBool IloTupleRequestCompFunction(IloTupleRequestI* key1,IloTupleRequestI* key2);

typedef IloInt (*IloTupleCellsHashFunction) (IloTupleCellArrayI*, IloInt);

extern IloInt  IloMixedTupleCellsHashFunction(IloTupleCellArrayI*, IloInt size);
extern IloInt  IloMixedBinaryTupleCellsHashFunction(IloTupleCellArrayI*, IloInt size);

extern IloBool IloMixedTupleCellsCompFunction(IloTupleCellArrayI* key1,IloTupleCellArrayI* key2);

extern IloInt  IloMixedTupleCellsNoCollectionHashFunction(IloTupleCellArrayI*, IloInt size);
extern IloInt  IloMixedBinaryTupleCellsNoCollectionHashFunction(IloTupleCellArrayI*, IloInt size);

extern IloBool IloMixedTupleCellsNoCollectionCompFunction(IloTupleCellArrayI* key1,IloTupleCellArrayI* key2);

extern IloInt  IloIntTupleCellsHashFunction(IloTupleCellArrayI*, IloInt size);
extern IloInt  IloIntBinaryTupleCellsHashFunction(IloTupleCellArrayI*, IloInt size);
extern IloInt  IloIntUnaryTupleCellsHashFunction(IloTupleCellArrayI*, IloInt size);

extern IloInt  IloNumTupleCellsHashFunction(IloTupleCellArrayI*, IloInt size);
extern IloInt  IloNumBinaryTupleCellsHashFunction(IloTupleCellArrayI*, IloInt size);
extern IloInt  IloNumUnaryTupleCellsHashFunction(IloTupleCellArrayI*, IloInt size);

extern IloInt  IloAnyTupleCellsHashFunction(IloTupleCellArrayI*, IloInt size);
extern IloInt  IloAnyBinaryTupleCellsHashFunction(IloTupleCellArrayI*, IloInt size);
extern IloInt  IloAnyTupleCellsNoCollectionHashFunction(IloTupleCellArrayI*, IloInt size);
extern IloInt  IloAnyUnaryTupleCellsNoCollectionHashFunction(IloTupleCellArrayI*, IloInt size);


typedef IloBool (*IloTupleCellsCompFunction) (IloTupleCellArrayI*, IloTupleCellArrayI*);

extern IloBool IloIntTupleCellsCompFunction(IloTupleCellArrayI* key1,IloTupleCellArrayI* key2);
extern IloBool IloNumTupleCellsCompFunction(IloTupleCellArrayI* key1,IloTupleCellArrayI* key2);
extern IloBool IloAnyTupleCellsCompFunction(IloTupleCellArrayI* key1,IloTupleCellArrayI* key2);
extern IloBool  IloAnyTupleCellsNoCollectionCompFunction(IloTupleCellArrayI*, IloTupleCellArrayI* key2);

extern IloBool IloIntBinaryTupleCellsCompFunction(IloTupleCellArrayI* key1,IloTupleCellArrayI* key2);
extern IloBool IloNumBinaryTupleCellsCompFunction(IloTupleCellArrayI* key1,IloTupleCellArrayI* key2);
extern IloBool IloAnyBinaryTupleCellsCompFunction(IloTupleCellArrayI* key1,IloTupleCellArrayI* key2);
extern IloBool  IloAnyBinaryTupleCellsNoCollectionCompFunction(IloTupleCellArrayI*, IloTupleCellArrayI* key2);

extern IloBool IloIntUnaryTupleCellsCompFunction(IloTupleCellArrayI* key1,IloTupleCellArrayI* key2);
extern IloBool IloNumUnaryTupleCellsCompFunction(IloTupleCellArrayI* key1,IloTupleCellArrayI* key2);
extern IloBool  IloAnyUnaryTupleCellsNoCollectionCompFunction(IloTupleCellArrayI*, IloTupleCellArrayI* key2);




class IloQueryDataTableHash : public IloTupleSetHash<IloTupleCellArrayI*> {
public:
	
	IloQueryDataTableHash(IloEnv env, IloInt size=IloDefaultHashSize)
		:IloTupleSetHash<IloTupleCellArrayI*>(env,
		IloMixedTupleCellsNoCollectionHashFunction,
		IloMixedTupleCellsNoCollectionCompFunction,
		size) {}
		~IloQueryDataTableHash();
};



template <class KeyType>
class IloTupleCellsHashBase : public IloEnvHashTable<KeyType, IloInt>{
public:
	typedef IloInt (*IloHashFunction) (KeyType, IloInt);
	typedef IloBool (*IloCompFunction) (KeyType, KeyType);

	
	IloTupleCellsHashBase(IloEnv env,
		IloHashFunction hashFct,
		IloCompFunction compFct,
		IloInt size=IloDefaultHashSize)
		:IloEnvHashTable<KeyType, IloInt>(env, hashFct,compFct, size) {}
		~IloTupleCellsHashBase(){}
};


class IloTupleCells2IndexHash : public IloTupleCellsHashBase<IloTupleCellArrayI*> {
public:
	
	IloTupleCells2IndexHash(IloEnv env, IloTupleCellsHashFunction hashFct, IloTupleCellsCompFunction compFct, IloInt size = IloDefaultHashSize)
		:IloTupleCellsHashBase<IloTupleCellArrayI*>(env,
		hashFct,
		compFct,
		size) {}
		~IloTupleCells2IndexHash(){}
};


class IloIndex2KeyHash : public IloEnvHashTable<IloInt, IloTupleCellArrayI*>{
public:
	typedef IloInt (*IloHashFunction) (IloInt, IloInt);
	typedef IloBool (*IloCompFunction) (IloInt, IloInt);

	
	IloIndex2KeyHash(IloEnv env,
		IloHashFunction hashFct,
		IloCompFunction compFct,
		IloInt size=IloDefaultHashSize)
		:IloEnvHashTable<IloInt, IloTupleCellArrayI*>(env, hashFct,compFct, size) {}
		~IloIndex2KeyHash(){}
};




template <class KeyType>
class IloTupleCellsTupleCollectionHashBase : public IloEnvHashTable<KeyType, IloIntArrayI*>{
public:
	typedef IloInt (*IloHashFunction) (KeyType, IloInt);
	typedef IloBool (*IloCompFunction) (KeyType, KeyType);

	
	IloTupleCellsTupleCollectionHashBase(IloEnv env,
		IloHashFunction hashFct,
		IloCompFunction compFct,
		IloInt size=IloDefaultHashSize)
		:IloEnvHashTable<KeyType, IloIntArrayI*>(env, hashFct,compFct, size) {}
		~IloTupleCellsTupleCollectionHashBase(){}
};


class IloTupleCells2ArrayIndexHash : public IloTupleCellsTupleCollectionHashBase<IloTupleCellArrayI*> {
public:
	
	IloTupleCells2ArrayIndexHash(IloEnv env, IloTupleCellsHashFunction hashFct, IloTupleCellsCompFunction compFct)
		:IloTupleCellsTupleCollectionHashBase<IloTupleCellArrayI*>(env,
		hashFct,
		compFct,
		IloDefaultHashSize) {}
		~IloTupleCells2ArrayIndexHash(){}
};



class IloSymbol2IndexHashTable : public IloSymbolHashTable<IloInt> {
public:
	
	IloSymbol2IndexHashTable(IloEnv env, IloInt size=IloDefaultHashSize)
		: IloSymbolHashTable<IloInt>(env, size) {}
		virtual ~IloSymbol2IndexHashTable() {}
};

#ifdef _WIN32
#pragma pack(pop)
#endif



#endif


