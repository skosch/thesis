// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilconcert/ilotypedlist.h
// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
//
// 5725-A06 5725-A29 5724-Y47 5724-Y48 5724-Y49 5724-Y54 5724-Y55
// Copyright IBM Corp. 2000, 2010
//
// US Government Users Restricted Rights - Use, duplication or
// disclosure restricted by GSA ADP Schedule Contract with
// IBM Corp.
// --------------------------------------------------------------------------- 


#ifndef __CONCERT_ilotypedlistH
#define __CONCERT_ilotypedlistH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilconcert/iloextractable.h>


/* special instance of IloHashTable, for <IloAny, IloAny> pair of key/value */

typedef enum {
	IloHashStatusOk=0,
	IloHashStatusNotFound=1,
	IloHashStatusDuplicatedKey=2
} IloHashStatus;


class IloSimpleHashTable {
protected:
	struct Item {
		IloAny _key;
		IloAny _value;
		Item*  _next;
		Item(IloAny k, IloAny v, Item* next=0);
	};
	IloEnvI* _env;
	Item*    _last;
	IloInt   _hSize;
	Item **  _buckets;
	Item     _nil;
	IloInt   _entries;
	inline IloInt hashFunction(IloAny key) const {
		return (((unsigned long)key)>>3)&(_hSize-1);
	}
	IloHashStatus checkDuplicatedKey();

public:
	IloSimpleHashTable(const IloEnv env, IloInt size=64);
	~IloSimpleHashTable();
	void clear();
	void end();
	IloBool isEnded() const { return (_buckets==0); }
	IloInt getSize() const { return _entries; }

	/* Add value under given key, without checking duplication of key */
	void add(IloAny key, IloAny value);
	/* Add value under given key, checking duplication of key */
	IloHashStatus addWithCheck(IloAny key, IloAny value);
	/* Remove key and its values */
	IloHashStatus remove(IloAny key);
	/* Test whether key has a value */
	IloBool isIn(IloAny key);
	/* Get value of key */
	IloHashStatus getValue(IloAny key, IloAny& value);
	IloAny operator[](IloAny key);

	class Iterator;
	friend class Iterator; // necessary to compile on linux + HP

	class Iterator {
	private:
		IloInt _index;
		Item* const * _table;
		const Item* _nil;
		Item* _item;
	public:
		Iterator(const IloSimpleHashTable& table);
		IloBool ok() const;
		void operator++();
		IloAny operator* ();
	};
};


class IloSimpleHashTable2 : public IloSimpleHashTable {
protected:
	IloInt _nextResize;
	void reHash();
public:
	IloSimpleHashTable2(const IloEnv env,
		IloInt size=64, IloInt AverageHashedListSize=32)
		: IloSimpleHashTable(env,size)
		, _nextResize(_hSize*AverageHashedListSize) {}

	IloInt getHashTableSize() const { return _hSize; }

	void add(IloAny key, IloAny value);
	IloHashStatus addWithCheck(IloAny key, IloAny value);
};

class IloTypedList {
	friend class IloTypedListManagerI;
private:
	/* The type of the extractables in the list */
	IloTypeInfo   _type;
	/* A list of extractables of the direct mother-class of the list. */
	IloTypedList*       _father;
	/* The first sub-list (direct sub-classes) */
	IloTypedList*       _firstSubType;
	/* next direct sub-class of the mother list. */
	IloTypedList*       _next;
	/* The list of extractables with same IloTypeInfo */
	IloExtractableList  _extractables;

public:
	IloTypeInfo getType() const { return _type; }

private:
	/* Constructor of a new empty list with the given type */
	IloTypedList(IloTypeInfo);
	~IloTypedList();

	/* Sets the father list. Called by setFirstSubType and setNext */
	void setFather(IloTypedList* father);
	/* Sets the first son-list. */
	void setFirstSubType(IloTypedList* sub);
	/* Sets the sibling of a list. */
	void setNext(IloTypedList* next);

public:
	/* Adds an extractable to the list */
	void add(IloExtractableI* ext);
	/* Removes an extractable to the list, or does nothing if the
	extractable is not in the list. */
	void remove(IloExtractableI* ext);

	const IloExtractableList& getExtractables() const { return _extractables; }
	IloTypedList* getFather() const { return _father; }
	IloTypedList* getFirstSubType() const { return _firstSubType; }
	IloTypedList* getNext() const { return _next; }
	void addSubList(IloTypedList*);
};



class IloTypedListManagerI {
private:
	IloEnvI* _env;

	/* IloTrue if the manager must classify all the extractables of the model */
	IloBool    _cleared;

	IloExtractableList _unclassified; /* not-yet classified extractables */
	IloSimpleHashTable _classified;

private:

	friend class IloTypedListManager;

	IloTypedListManagerI(IloEnvI*);
	~IloTypedListManagerI();

	void classifyAll();
	void classify(); /* only classifies the new extractables. */
	void insert(IloExtractableI*);  /* put in the right place */

	IloTypedList* getOrBuildList(IloTypeInfo); /* new list for the given type */

	void add(IloExtractableI* ext);
	void remove(IloExtractableI* ext);


public:
	void clear(); /* PMTODO: this is public only for bench "iterators.cpp:220".
				  Fix this... */
	class IloTypedList* getList(IloTypeInfo);
};

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif  /* __CONCERT_ilotypedlistH */
