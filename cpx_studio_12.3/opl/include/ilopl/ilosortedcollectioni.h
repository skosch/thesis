// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilosortedcollectioni.h
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

#ifndef __ADVANCED_ilosortedcollectioniH
#define __ADVANCED_ilosortedcollectioniH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>
#include <ilopl/ilotuplecollectioni.h>

//--------------------------------------
IloIntArray intersectAscSortedIndex(IloEnv env, IloIntArray set1, IloIntArray set2);
IloNumArray intersectAscSortedIndex(IloEnv env, IloNumArray set1, IloNumArray set2);
IloAnyArray intersectAscSortedIndex(IloEnv env, IloAnyArray set1, IloAnyArray set2);

IloIntArray intersectDescSortedIndex(IloEnv env, IloIntArray set1, IloIntArray set2);
IloNumArray intersectDescSortedIndex(IloEnv env, IloNumArray set1, IloNumArray set2);
IloAnyArray intersectDescSortedIndex(IloEnv env, IloAnyArray set1, IloAnyArray set2);

//--------------------------------------
class IloSortedIntSetI : public IloIntSetI {
	ILORTTIDECL
public:
	
	virtual ~IloSortedIntSetI() {
		if( _oldIndexPositions.getImpl() ) {
			_oldIndexPositions.end();
		}
	}
protected:
	IloSortedIntSetI(IloEnvI* env);
	IloSortedIntSetI(IloEnvI* env, IloSortedIntSetI* S) : IloIntSetI(env, S), _oldIndexPositions( 0 ), _canSort( IloTrue ) {}
public:
	virtual void processBeforeFill() {
		_canSort = IloFalse;
	}
	virtual void processAfterFill( IloBool generateOldIndex = IloFalse ) { 
		_canSort = IloTrue;
		if( generateOldIndex ) {
			initOldIndexes();
		}
		sort(); 
	}
	virtual void addWithoutSort(IloInt elt){ IloIntSetI::add(elt); }

	virtual const IloIntArray getOldIndexPositions() { return _oldIndexPositions; }

	virtual void endOldIndexes() {
		if( _oldIndexPositions.getImpl() ) {
			_oldIndexPositions.end();
		}
	}
protected:
	IloBool canSort() const {
		return _canSort;
	}
	void initOldIndexes();
	IloIntArray _oldIndexPositions;
	IloBool _canSort;
};

class IloAscSortedIntSetI : public IloSortedIntSetI {
	ILORTTIDECL
public:
	
	virtual ~IloAscSortedIntSetI() {}

	IloAscSortedIntSetI(IloEnvI* env);
	IloAscSortedIntSetI(IloEnvI* env, IloAscSortedIntSetI* S) : IloSortedIntSetI(env, S){}

	virtual IloBool isSortedAsc() const{ return IloTrue; }
	virtual IloInt getLB() const{
		return getFirst();
	}
	virtual IloInt getUB() const{
		return getLast();
	}

	virtual void add(IloInt elt);
	virtual void add(IloIntSetI* set);
	virtual void setIntersection(IloIntSetI* set);
	virtual IloDataCollectionI* copy() const;
	virtual IloDataCollectionI* makeClone(IloEnvI* env) const;
	virtual void sort(IloBool updateHash = IloTrue);
};




class IloDescSortedIntSetI : public IloSortedIntSetI {
	ILORTTIDECL
public:
	
	virtual ~IloDescSortedIntSetI(){}

	IloDescSortedIntSetI(IloEnvI* env);
	IloDescSortedIntSetI(IloEnvI* env, IloDescSortedIntSetI* S) : IloSortedIntSetI(env, S){}

	virtual IloBool isSortedDesc() const{ return IloTrue; }
	virtual IloInt getLB() const{
		return getFirst();
	}
	virtual IloInt getUB() const{
		return getLast();
	}

	virtual void add(IloInt elt);
	virtual void add(IloIntSetI* set);
	virtual void setIntersection(IloIntSetI* set);
	virtual IloDataCollectionI* copy() const;
	virtual IloDataCollectionI* makeClone(IloEnvI* env) const;
	virtual void sort(IloBool updateHash = IloTrue);
};



class IloSortedNumSetI : public IloNumSetI {
	ILORTTIDECL
public:
	
	virtual ~IloSortedNumSetI(){
		if( _oldIndexPositions.getImpl() ) {
			_oldIndexPositions.end();
		}
	}
protected:
	IloSortedNumSetI(IloEnvI* env);
	IloSortedNumSetI(IloEnvI* env, IloSortedNumSetI* S) : IloNumSetI(env, S), _oldIndexPositions( 0 ), _canSort( IloTrue ) {}
public:
	virtual void processBeforeFill() {
		_canSort = IloFalse;
	}
	virtual void processAfterFill( IloBool generateOldIndex = IloFalse ) { 
		_canSort = IloTrue;
		if( generateOldIndex ) {
			initOldIndexes();
		}
		sort(); 
	}
	virtual void addWithoutSort(IloNum elt){ IloNumSetI::add(elt); }

	virtual void endOldIndexes() {
		if( _oldIndexPositions.getImpl() ) {
			_oldIndexPositions.end();
		}
	}
protected:
	IloBool canSort() const {
		return _canSort;
	}

	void initOldIndexes();
	IloIntArray _oldIndexPositions;
	IloBool _canSort;
};

class IloAscSortedNumSetI : public IloSortedNumSetI {
	ILORTTIDECL
public:
	
	virtual ~IloAscSortedNumSetI(){}

	IloAscSortedNumSetI(IloEnvI* env);
	IloAscSortedNumSetI(IloEnvI* env, IloAscSortedNumSetI* S) : IloSortedNumSetI(env, S){}

	virtual IloBool isSortedAsc() const{ return IloTrue; }
	virtual IloNum getLB() const{
		return getFirst();
	}
	virtual IloNum getUB() const{
		return getLast();
	}

	virtual void add(IloNum elt);
	virtual void add(IloNumSetI* set);
	virtual void setIntersection(IloNumSetI* set);
	virtual IloDataCollectionI* copy() const;
	virtual IloDataCollectionI* makeClone(IloEnvI* env) const;
	virtual void sort(IloBool updateHash = IloTrue);
};



class IloDescSortedNumSetI : public IloSortedNumSetI {
	ILORTTIDECL
public:
	
	virtual ~IloDescSortedNumSetI(){}

	IloDescSortedNumSetI(IloEnvI* env);
	IloDescSortedNumSetI(IloEnvI* env, IloDescSortedNumSetI* S) : IloSortedNumSetI(env, S){}

	virtual IloBool isSortedDesc() const{ return IloTrue; }
	virtual IloNum getLB() const{
		return getFirst();
	}
	virtual IloNum getUB() const{
		return getLast();
	}

	virtual void add(IloNum elt);
	virtual void add(IloNumSetI* set);
	virtual void setIntersection(IloNumSetI* set);
	virtual IloDataCollectionI* copy() const;
	virtual IloDataCollectionI* makeClone(IloEnvI* env) const;
	virtual void sort(IloBool updateHash = IloTrue);
};


class IloSortElement;
class IloSortedSymbolSetI : public IloSymbolSetI {
	ILORTTIDECL
public:
	
	virtual ~IloSortedSymbolSetI(){}
protected:
	IloSortedSymbolSetI(IloEnvI* env);
	IloSortedSymbolSetI(IloEnvI* env, IloSortedSymbolSetI* S) : IloSymbolSetI(env, S), _canSort( IloTrue ) {
		setType(IloDataCollection::SymbolSet);
	}
	IloArray<IloSortElement> makeSort();
	IloBool _canSort;

	IloBool canSort() const {
		return _canSort;
	}
public:
	virtual void processBeforeFill() {
		_canSort = IloFalse;
	}
	virtual void processAfterFill( IloBool  = IloFalse  ) { 
		_canSort = IloTrue;
		sort();	
	}
	virtual void addWithoutSort(IloAny elt){ IloSymbolSetI::add(elt); }
	virtual void addWithoutSort(IloSymbol elt){ addWithoutSort(elt.getImpl()); }
	virtual void sort(IloBool updateHash = IloTrue);
	virtual IloAnyArray makeSortedIndexes(IloArray<IloSortElement> tmp );
};

class IloAscSortedSymbolSetI : public IloSortedSymbolSetI {
	ILORTTIDECL
public:
	
	virtual ~IloAscSortedSymbolSetI(){}

	IloAscSortedSymbolSetI(IloEnvI* env);
	IloAscSortedSymbolSetI(IloEnvI* env, IloAscSortedSymbolSetI* S) : IloSortedSymbolSetI(env, S){}

	virtual IloBool isSortedAsc() const{ return IloTrue; }

	virtual void add(IloAny elt);
	virtual void add(IloAnySetI* set);
	virtual void setIntersection(IloAnySetI* set);
	virtual IloDataCollectionI* copy() const;
	virtual IloDataCollectionI* makeClone(IloEnvI* env) const;
	virtual IloAnyArray makeSortedIndexes(IloArray<IloSortElement> tmp );
};



class IloDescSortedSymbolSetI : public IloSortedSymbolSetI {
	ILORTTIDECL
public:
	
	virtual ~IloDescSortedSymbolSetI(){}

	IloDescSortedSymbolSetI(IloEnvI* env);
	IloDescSortedSymbolSetI(IloEnvI* env, IloDescSortedSymbolSetI* S) : IloSortedSymbolSetI(env, S){}

	virtual IloBool isSortedDesc() const{ return IloTrue; }

	virtual void add(IloAny elt);
	virtual void add(IloAnySetI* set);
	virtual void setIntersection(IloAnySetI* set);
	virtual IloDataCollectionI* copy() const;
	virtual IloDataCollectionI* makeClone(IloEnvI*) const;
	virtual IloAnyArray makeSortedIndexes(IloArray<IloSortElement> tmp );
};



class IloSortedTupleSetI : public IloTupleSetI {
	ILORTTIDECL
protected:
	IloIntArray _sortedIdxes;
	IloIntArray _absIdxes;
	IloBool _canSort;
	IloSortedTupleSetI(IloEnv env, IloTupleSchema schema) 
		: IloTupleSetI(env, schema), _sortedIdxes(env), _absIdxes(env), _canSort( IloTrue ) {
	}
	IloSortedTupleSetI(IloEnvI* env, IloSortedTupleSetI* S) 
		: IloTupleSetI(env, S), _sortedIdxes(S->getSortedIndexes().copy()), _absIdxes(S->getAbsoluteIndexes().copy()), _canSort( IloTrue ) {
	}
	IloBool canSort() const {
		return _canSort;
	}
public:
	IloArray<IloSortElement> makeSort();
	virtual void fillSortedIndexes(IloArray<IloSortElement> tmp );
	virtual void sort(IloBool var = IloTrue);

	IloIntArray getSortedIndexes() const{ return _sortedIdxes; }
	IloIntArray getAbsoluteIndexes() const{ return _absIdxes; }
	virtual ~IloSortedTupleSetI(){
		_sortedIdxes.end();
		_absIdxes.end();
	}
	virtual void processBeforeFill() {
		_canSort = IloFalse;
	}
	virtual void processAfterFill( IloBool  = IloFalse ) { 
		_canSort = IloTrue;
		sort();	
	}
	virtual IloInt commitWithoutSort(IloTupleCellArray line, IloBool check){
		return IloTupleSetI::commit(line, check);
	}
	virtual void display(ILOSTD(ostream)& out) const;
	virtual IloInt getTupleIndexFromAbsoluteIndex(IloInt idx) const{ 
		return idx >=0 && idx < _absIdxes.getSize() ? _absIdxes[idx] : -1; 
	}
	virtual IloInt getAbsoluteIndexFromTupleIndex(IloInt idx) const{ 
		return idx >=0 && idx < _sortedIdxes.getSize() ? _sortedIdxes[idx] : -1; 
	}
};

class IloAscSortedTupleSetI : public IloSortedTupleSetI {
using IloSortedTupleSetI::commit;
	ILORTTIDECL
public:
	
	IloAscSortedTupleSetI(IloEnv env, IloTupleSchema schema) : IloSortedTupleSetI(env, schema){}
	IloAscSortedTupleSetI(IloEnvI* env, IloAscSortedTupleSetI* S) : IloSortedTupleSetI(env, S){}

	virtual ~IloAscSortedTupleSetI(){
	}
	virtual IloBool isSortedAsc() const{ return IloTrue; }

	virtual IloInt commit(IloTupleCellArray line, IloBool check);
	virtual IloInt setLine(IloInt index, IloTupleCellArray line, IloBool check);
	virtual IloDataCollectionI* copy() const;
	virtual IloDataCollectionI* makeClone(IloEnvI*) const;
	virtual void fillSortedIndexes(IloArray<IloSortElement> tmp );
};



class IloDescSortedTupleSetI : public IloSortedTupleSetI {
using IloSortedTupleSetI::commit;
	ILORTTIDECL
public:
	
	IloDescSortedTupleSetI(IloEnv env, IloTupleSchema schema) : IloSortedTupleSetI(env, schema){}
	IloDescSortedTupleSetI(IloEnvI* env, IloDescSortedTupleSetI* S) : IloSortedTupleSetI(env, S){}

	virtual ~IloDescSortedTupleSetI(){
	}
	virtual IloBool isSortedDesc() const{ return IloTrue; }

	virtual IloInt commit(IloTupleCellArray line, IloBool check);
	virtual IloInt setLine(IloInt index, IloTupleCellArray line, IloBool check);
	virtual IloDataCollectionI* copy() const;
	virtual IloDataCollectionI* makeClone(IloEnvI*) const;
	virtual void fillSortedIndexes(IloArray<IloSortElement> tmp );
};


#ifdef _WIN32
#pragma pack(pop)
#endif


#endif
