// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/iltuple/ilorequestcache.h
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


#ifndef __ADVANCED_ilorequestcacheH
#define __ADVANCED_ilorequestcacheH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>

#include <ilopl/iloenv.h>
#include <ilconcert/ilostring.h>
#include <ilopl/iltuple/ilotuplebuffer.h>
#include <ilconcert/ilohash.h>
#include <ilopl/iltuple/ilotuplecollectionhash.h>

class IloQueryDataTableHash;
class IloTupleRequest;
class IloTupleRequestI;
class IloTupleSetI;
class IloTupleSet;

class IloIntDataColumnI;
class IloNumDataColumnI;
class IloAnyDataColumnI;



class IloTupleRequestCacheI : public IloEnvObjectI {
protected:
	IloIntArray _empty;
public:
	
	IloTupleRequestCacheI(IloEnv env);
	virtual ~IloTupleRequestCacheI();
	
	virtual IloIntArrayI* execute()=0;
	
	virtual IloBool store(IloIntArrayI* slice)=0;
};

class IloTupleRequestUnaryCacheI : public IloEnvObjectI {
public:
	
	IloTupleRequestUnaryCacheI(IloEnv env);
	virtual ~IloTupleRequestUnaryCacheI();
	
	virtual IloInt execute()=0;
	
	virtual IloBool store(IloInt slice)=0;
};

IloBool IloMultipleIntCompFunction(IloInt** key1, IloInt** key2);
IloInt IloMultipleIntHashFunction(IloInt** key, IloInt size);


#define ILO_NB_HASHFUNCTIONH(Type, x) \
	IloBool name2(name2(Ilo,x),name2(Type,CompFunction))( name2(Ilo,Type)** key1, name2(Ilo,Type)** key2); \
	IloInt name2(name2(Ilo,x),name2(Type,HashFunction))(name2(Ilo,Type)** key, IloInt size);

#define ALL_ILO_NB_HASHFUNCTIONH(x) \
	ILO_NB_HASHFUNCTIONH(Int, x) \
	ILO_NB_HASHFUNCTIONH(Num, x) \
	ILO_NB_HASHFUNCTIONH(Any, x)

ALL_ILO_NB_HASHFUNCTIONH(2)
ALL_ILO_NB_HASHFUNCTIONH(3)
ALL_ILO_NB_HASHFUNCTIONH(4)
ALL_ILO_NB_HASHFUNCTIONH(5)
ALL_ILO_NB_HASHFUNCTIONH(6)
ALL_ILO_NB_HASHFUNCTIONH(7)
ALL_ILO_NB_HASHFUNCTIONH(8)
ALL_ILO_NB_HASHFUNCTIONH(9)
ALL_ILO_NB_HASHFUNCTIONH(10)
ALL_ILO_NB_HASHFUNCTIONH(11)
ALL_ILO_NB_HASHFUNCTIONH(12)
ALL_ILO_NB_HASHFUNCTIONH(13)
ALL_ILO_NB_HASHFUNCTIONH(14)
ALL_ILO_NB_HASHFUNCTIONH(15)
ALL_ILO_NB_HASHFUNCTIONH(16)
ALL_ILO_NB_HASHFUNCTIONH(17)
ALL_ILO_NB_HASHFUNCTIONH(18)
ALL_ILO_NB_HASHFUNCTIONH(19)
ALL_ILO_NB_HASHFUNCTIONH(20)
ALL_ILO_NB_HASHFUNCTIONH(21)
ALL_ILO_NB_HASHFUNCTIONH(22)
ALL_ILO_NB_HASHFUNCTIONH(23)
ALL_ILO_NB_HASHFUNCTIONH(24)
ALL_ILO_NB_HASHFUNCTIONH(25)
ALL_ILO_NB_HASHFUNCTIONH(26)
ALL_ILO_NB_HASHFUNCTIONH(27)
ALL_ILO_NB_HASHFUNCTIONH(28)
ALL_ILO_NB_HASHFUNCTIONH(29)
ALL_ILO_NB_HASHFUNCTIONH(30)
ALL_ILO_NB_HASHFUNCTIONH(31)
ALL_ILO_NB_HASHFUNCTIONH(32)

#define MY_ARITY 32


class IloMultipleIntHashTable : public IloEnvHashTable<IloInt**, IloIntArrayI*> {
  typedef IloInt (*IloMultipleIntHashFunction) (IloInt**, IloInt);
  typedef IloBool (*IloMultipleIntCompFunction) (IloInt**, IloInt**);
private:
	IloInt _size;
public:
	
	IloMultipleIntHashTable(IloEnv env, IloInt size, IloMultipleIntHashFunction, IloMultipleIntCompFunction);
	virtual ~IloMultipleIntHashTable();
};




class IloNaryIntCacheI : public IloTupleRequestCacheI {
private:
	IloInt _keySize;
	IloInt** _key;
	IloMultipleIntHashTable* _hash;
public:
	
	IloNaryIntCacheI(IloEnv env,
		IloInt keySize, IloInt** key,
		IloMultipleIntHashTable* h);
	virtual ~IloNaryIntCacheI();
	
	virtual IloIntArrayI* execute();
	
	virtual IloBool store(IloIntArrayI* slice);
};


class IloSimplifiedNaryIntCacheI : public IloTupleRequestCacheI {
private:
	IloInt _size;
	IloInt** _key;
	IloMultipleIntHashTable* _hash;
public:
	
	IloSimplifiedNaryIntCacheI(IloEnv env, IloInt size, IloInt** key, IloMultipleIntHashTable* h);
	virtual ~IloSimplifiedNaryIntCacheI();
	
	virtual IloIntArrayI* execute();
	
	virtual IloBool store(IloIntArrayI* slice);
};

class IloMultipleUnaryIntHashTable : public IloEnvHashTable<IloInt**, IloInt> {
  typedef IloInt (*IloMultipleIntHashFunction) (IloInt**, IloInt);
  typedef IloBool (*IloMultipleIntCompFunction) (IloInt**, IloInt**);
private:
	IloInt _size;
public:
	
	IloMultipleUnaryIntHashTable(IloEnv env, IloInt size, IloMultipleIntHashFunction, IloMultipleIntCompFunction);
	virtual ~IloMultipleUnaryIntHashTable();
};


class IloNaryIntUnaryCacheI : public IloTupleRequestUnaryCacheI {
private:
	IloInt _keySize;
	IloInt** _key;
	IloMultipleUnaryIntHashTable* _hash;
public:
	
	IloNaryIntUnaryCacheI(IloEnv env,
		IloInt keySize, IloInt** key,
		IloMultipleUnaryIntHashTable* h);
	virtual ~IloNaryIntUnaryCacheI();
	
	virtual IloInt execute();
	
	virtual IloBool store(IloInt slice);
};

class IloSimplifiedNaryIntUnaryCacheI : public IloTupleRequestUnaryCacheI {
private:
	IloInt _size;
	IloInt** _key;
	IloMultipleUnaryIntHashTable* _hash;
public:
	
	IloSimplifiedNaryIntUnaryCacheI(IloEnv env, IloInt size, IloInt** key, IloMultipleUnaryIntHashTable* h);
	virtual ~IloSimplifiedNaryIntUnaryCacheI();
	
	virtual IloInt execute();
	
	virtual IloBool store(IloInt slice);
};

IloBool IloMultipleNumCompFunction(IloNum** key1, IloNum** key2);
IloInt IloMultipleNumHashFunction(IloNum** key, IloInt size);


class IloMultipleNumHashTable : public IloEnvHashTable<IloNum**, IloIntArrayI*> {
  typedef IloInt (*IloMultipleNumHashFunction) (IloNum**, IloInt);
  typedef IloBool (*IloMultipleNumCompFunction) (IloNum**, IloNum**);
private:
	IloInt _size;
public:
	
	IloMultipleNumHashTable(IloEnv env, IloInt size, IloMultipleNumHashFunction, IloMultipleNumCompFunction);
	virtual ~IloMultipleNumHashTable();
};



class IloNaryNumCacheI : public IloTupleRequestCacheI {
private:
	IloInt ILO_MAY_ALIAS _keySize;
	IloNum** _key;
	IloMultipleNumHashTable* _hash;
public:
	
	IloNaryNumCacheI(IloEnv env,
		IloInt keySize, IloNum** key,
		IloMultipleNumHashTable* h);
	virtual ~IloNaryNumCacheI();
	
	virtual IloIntArrayI* execute();
	
	virtual IloBool store(IloIntArrayI* slice);
};

class IloSimplifiedNaryNumCacheI : public IloTupleRequestCacheI {
private:
	IloInt _size;
	IloNum** _key;
	IloMultipleNumHashTable* _hash;
public:
	
	IloSimplifiedNaryNumCacheI(IloEnv env, IloInt size, IloNum** key, IloMultipleNumHashTable* h);
	virtual ~IloSimplifiedNaryNumCacheI();
	
	virtual IloIntArrayI* execute();
	
	virtual IloBool store(IloIntArrayI* slice);
};

class IloMultipleUnaryNumHashTable : public IloEnvHashTable<IloNum**, IloInt> {
  typedef IloInt (*IloMultipleNumHashFunction) (IloNum**, IloInt);
  typedef IloBool (*IloMultipleNumCompFunction) (IloNum**, IloNum**);
private:
	IloInt _size;
public:
	
	IloMultipleUnaryNumHashTable(IloEnv env, IloInt size, IloHashFunction, IloCompFunction);
	virtual ~IloMultipleUnaryNumHashTable();
};


class IloNaryNumUnaryCacheI : public IloTupleRequestUnaryCacheI {
private:
	IloInt ILO_MAY_ALIAS _keySize;
	IloNum** _key;
	IloMultipleUnaryNumHashTable* _hash;
public:
	
	IloNaryNumUnaryCacheI(IloEnv env,
		IloInt keySize, IloNum** key,
		IloMultipleUnaryNumHashTable* h);
	virtual ~IloNaryNumUnaryCacheI();
	virtual IloInt execute();
	virtual IloBool store(IloInt slice);
};

class IloSimplifiedNaryNumUnaryCacheI : public IloTupleRequestUnaryCacheI {
private:
	IloInt _size;
	IloNum** _key;
	IloMultipleUnaryNumHashTable* _hash;
public:
	
	IloSimplifiedNaryNumUnaryCacheI(IloEnv env, IloInt size, IloNum** key, IloMultipleUnaryNumHashTable* h);
	virtual ~IloSimplifiedNaryNumUnaryCacheI();
	virtual IloInt execute();
	virtual IloBool store(IloInt slice);
};

IloBool IloMultipleAnyCompFunction(IloAny** key1, IloAny** key2);
IloInt IloMultipleAnyHashFunction(IloAny** key, IloInt size);



class IloMultipleAnyHashTable : public IloEnvHashTable<IloAny**, IloIntArrayI*> {
  typedef IloInt (*IloMultipleAnyHashFunction) (IloAny**, IloInt);
  typedef IloBool (*IloMultipleAnyCompFunction) (IloAny**, IloAny**);
private:
	IloInt _size;
public:
	
	IloMultipleAnyHashTable(IloEnv env, IloInt size, IloMultipleAnyHashFunction, IloMultipleAnyCompFunction);
	virtual ~IloMultipleAnyHashTable();
};



class IloNaryAnyCacheI : public IloTupleRequestCacheI {
private:
	IloInt _keySize;
	IloAny** _key;
	IloMultipleAnyHashTable* _hash;
public:
	
	IloNaryAnyCacheI(IloEnv env,
		IloInt keySize,
		IloAny** key,
		IloMultipleAnyHashTable* h);
	virtual ~IloNaryAnyCacheI();
	
	virtual IloIntArrayI* execute();
	
	virtual IloBool store(IloIntArrayI* slice);
};

class IloSimplifiedNaryAnyCacheI : public IloTupleRequestCacheI {
private:
	IloInt _size;
	IloAny** _key;
	IloMultipleAnyHashTable* _hash;
public:
	
	IloSimplifiedNaryAnyCacheI(IloEnv env, IloInt size, IloAny** key, IloMultipleAnyHashTable* h);
	virtual ~IloSimplifiedNaryAnyCacheI();
	
	virtual IloIntArrayI* execute();
	
	virtual IloBool store(IloIntArrayI* slice);
};


class IloMultipleUnaryAnyHashTable : public IloEnvHashTable<IloAny**, IloInt> {
  typedef IloInt (*IloMultipleAnyHashFunction) (IloAny**, IloInt);
  typedef IloBool (*IloMultipleAnyCompFunction) (IloAny**, IloAny**);
private:
	IloInt _size;
public:
	
	IloMultipleUnaryAnyHashTable(IloEnv env, IloInt size, IloMultipleAnyHashFunction, IloMultipleAnyCompFunction);
	virtual ~IloMultipleUnaryAnyHashTable();
};

class IloNaryAnyUnaryCacheI : public IloTupleRequestUnaryCacheI {
private:
	IloInt _keySize;
	IloAny** _key;
	IloMultipleUnaryAnyHashTable* _hash;
public:
	IloNaryAnyUnaryCacheI(IloEnv env,
		IloInt keySize,
		IloAny** key,
		IloMultipleUnaryAnyHashTable* h);
	virtual ~IloNaryAnyUnaryCacheI();
	virtual IloInt execute();
	virtual IloBool store(IloInt slice);
};


class IloSimplifiedNaryAnyUnaryCacheI : public IloTupleRequestUnaryCacheI {
private:
	IloInt _size;
	IloAny** _key;
	IloMultipleUnaryAnyHashTable* _hash;
public:
	IloSimplifiedNaryAnyUnaryCacheI(IloEnv env, IloInt size, IloAny** key, IloMultipleUnaryAnyHashTable* h);
	virtual ~IloSimplifiedNaryAnyUnaryCacheI();
	virtual IloInt execute();
	virtual IloBool store(IloInt slice);
};


class IloNaryCellCacheI : public IloTupleRequestCacheI {
private:
	IloTupleRequestI* _key;
	IloTupleCellArray _keys;
	IloQueryDataTableHash* _hash;
public:
	
	IloNaryCellCacheI(IloEnv env,
		IloTupleRequestI* key,
		IloQueryDataTableHash* h);
	virtual ~IloNaryCellCacheI();
	
	virtual IloIntArrayI* execute();
	
	virtual IloBool store(IloIntArrayI* slice);
	IloTupleCellArray getOrMakeKeys();
	IloTupleCellArray makeKeys();
};

class IloQueryUnaryDataTableHash : public IloEnvHashTable<IloTupleCellArrayI*, IloInt>{
public:
	IloQueryUnaryDataTableHash(IloEnv env,IloInt size=IloDefaultHashSize);
		~IloQueryUnaryDataTableHash(){
			for (IloQueryUnaryDataTableHash::Iterator it(this); it.ok(); ++it){
				IloTupleCellArray tca(it.getKey());
				tca.end();
			}
		}
};

class IloNaryCellUnaryCacheI : public IloTupleRequestUnaryCacheI {
private:
	IloTupleRequestI* _key;
	IloTupleCellArray _keys;
	IloQueryUnaryDataTableHash* _hash;
public:
	
	IloNaryCellUnaryCacheI(IloEnv env,
		IloTupleRequestI* key,
		IloQueryUnaryDataTableHash* h);
	virtual ~IloNaryCellUnaryCacheI();
	
	virtual IloInt execute();
	
	virtual IloBool store(IloInt slice);
	IloTupleCellArray getOrMakeKeys();
	IloTupleCellArray makeKeys();
};

#ifdef _WIN32
#pragma pack(pop)
#endif


#endif

