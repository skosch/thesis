// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilosymbol.h
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

#ifndef __ADVANCED_ilosymbolH
#define __ADVANCED_ilosymbolH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>

#include <ilconcert/ilosymbol.h>

class IloAnyCollection;
class IloSymbolSet;

#ifndef ILO_WINDOWS
#include <locale.h>
#endif

#define IloSymbolSetI IloAnySetI

// --------------------------------------------------------------
// Symbol Expressions
// --------------------------------------------------------------


class IloSymbolCollectionExprI : public IloAnyExprI {
	ILOEXTRDECL
public:
	IloSymbolCollectionExprI(IloEnvI* env, const char* name=0) : IloAnyExprI(env, name) {}
	virtual ~IloSymbolCollectionExprI(){}
};

#define IloSymbolCollectionExprArgI IloSymbolCollectionExprI

//-----------------------------------------------------------------

class ILO_EXPORTED IloSymbolExprArg : public IloExtractable {
	ILOEXTRHANDLE(IloSymbolExprArg, IloExtractable)
};


// --------------------------------------------------------------
// Symbol Constraints
// --------------------------------------------------------------

//-----------------------------------------------------------------

IloConstraint operator==(const IloSymbolExprArg expr1, const IloSymbolExprArg expr2);


IloConstraint operator!=(const IloSymbolExprArg expr1, const IloSymbolExprArg expr2);



IloConstraint operator<(const IloSymbolExprArg expr1, const IloSymbolExprArg expr2);



IloConstraint operator>(const IloSymbolExprArg expr1, const IloSymbolExprArg expr2);



IloConstraint operator<=(const IloSymbolExprArg expr1, const IloSymbolExprArg expr2);



IloConstraint operator>=(const IloSymbolExprArg expr1, const IloSymbolExprArg expr2);



//--------------------------------------------------------------------
// Symbol arrays
//--------------------------------------------------------------------
typedef IloArray<IloSymbol> IloSymbolArrayBase;


class ILO_EXPORTED IloSymbolArray : public IloSymbolArrayBase {
public:
	typedef IloDefaultArrayI ImplClass;
	IloSymbolArray(IloDefaultArrayI* i=0) : IloSymbolArrayBase(i) {}
	IloSymbolArray(const IloSymbolArray& copy) : IloSymbolArrayBase(copy) {}
	IloSymbolArray(const IloEnv env, IloInt n = 0) : IloSymbolArrayBase(env, n) {}
	IloSymbolArray(const IloMemoryManager env, IloInt n = 0) : IloSymbolArrayBase(env, n) {}

	IloSymbolArray(const IloEnv env, IloInt n, const IloSymbol v0);
	IloSymbolArray(const IloEnv env, IloAnyArray from);
	IloAnyArray toAnyArray() const;

	using IloArray<IloSymbol>::add;
	void add(const char* name);
	IloSymbolArray(const IloEnv env, IloInt n, const char* v0);
	IloBool contains(const IloSymbol symbol) const;
	IloBool contains(const char* name) const;
	const IloSymbol& operator[] (IloInt i) const {
		return IloSymbolArrayBase::operator[](i);
	}
	IloSymbol& operator[] (IloInt i) {
		return IloSymbolArrayBase::operator[](i);
	}
	IloSymbolExprArg operator[] (IloIntExprArg intExp) const;
};




class IloSymbolArrayElementI : public IloSymbolExprI {
	ILOEXTRDECL
private:
	IloIntExprI* _index;
	IloSymbolArray  _array;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloSymbolArrayElementI(IloEnvI* env, IloIntExprI* index, IloSymbolArray array):
	  IloSymbolExprI(env), _index(index->intLockExpr()), _array(array) {}
	  virtual ~IloSymbolArrayElementI();
	  void display(ILOSTD(ostream)& out) const;
	  IloSymbolArray getArray() const { return _array; }
	  IloIntExprI* getIndex() const { return _index; }
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
};


typedef IloHandleSet<IloSymbol , IloSymbolI > IloSymbolSetBase;

class IloSymbolSet: public IloSymbolSetBase{
public:
	
	class Iterator : public IloAnyDefaultDataIterator {
	public:
		Iterator() : IloAnyDefaultDataIterator() {}

		
		Iterator(const IloSymbolSet coll): IloAnyDefaultDataIterator(coll.getImpl()->getEnv()->getGeneralAllocator(),
			coll.getImpl()) {
				reset();
		}
		virtual ~Iterator(){}

		
		const char* operator*() {
			return IloSymbol((IloSymbolI*)IloAnyDefaultDataIterator::operator*()).getImpl()->getString();
		}

#ifdef CPPREF_GENERATION
		
		IloBool ok() const {
			return _ok;
		}

		
		void operator++() {
			_ok = next();
		}
#endif
	};

public:
	IloSymbolSet(const IloEnv env,	const IloSymbolArray array, IloBool withIndex=IloFalse);

	IloSymbolSet(const IloEnv env,	const IloAnyArray array, IloBool withIndex=IloFalse);
	IloSymbolSet(const IloEnv env,	const IloSymbolSet set);
	
	IloSymbolSet(const IloEnv env, IloBool withIndex=IloFalse);
	IloSymbolSet(const IloEnv env, IloDataCollection::SortSense);

	IloSymbolSet(IloAnySetI* impl=0) : IloSymbolSetBase(impl) { if (impl) impl->setType(SymbolSet);}

	using IloSymbolSetBase::add;
	using IloSymbolSetBase::remove;
	using IloSymbolSetBase::contains;
	using IloSymbolSetBase::setIntersection;

	
	void add(const char* elt);

	
	void remove(const char* elt);

	
	void setIntersection(const char* elt);

	
	IloBool contains(const char* elt) const;

	IloBool isSymbolSet() const { return IloTrue; }

	using IloSymbolSetBase::getNext;

	
	IloSymbol getNext(const char* value, IloInt n=1) const{
		IloAssert(getImpl() != 0, "IloSet: Using empty handle");
		IloSymbol s = getImpl()->getEnv()->makeSymbol(value);
		return IloSymbol((IloSymbolI*)((IloAnySetI*)_impl)->getNext(s.getImpl(),n));
	}
	using IloSymbolSetBase::getPrevious;

	
	IloSymbol getPrevious(const char* value, IloInt n=1) const{
		IloAssert(getImpl() != 0, "IloSet: Using empty handle");
		IloSymbol s = getImpl()->getEnv()->makeSymbol(value);
		return IloSymbol((IloSymbolI*)((IloAnySetI*)_impl)->getPrevious(s.getImpl(),n));
	}
	using IloSymbolSetBase::getNextC;

	
	IloSymbol getNextC(const char* value, IloInt n=1) const{
		IloAssert(getImpl() != 0, "IloSet: Using empty handle");
		IloSymbol s = getImpl()->getEnv()->makeSymbol(value);
		return IloSymbol((IloSymbolI*)((IloAnySetI*)_impl)->getNextC(s.getImpl(),n));
	}
	using IloSymbolSetBase::getPreviousC;

	
	IloSymbol getPreviousC(const char* value, IloInt n=1) const{
		IloAssert(getImpl() != 0, "IloSet: Using empty handle");
		IloSymbol s = getImpl()->getEnv()->makeSymbol(value);
		return IloSymbol((IloSymbolI*)((IloAnySetI*)_impl)->getPreviousC(s.getImpl(),n));
	}
	void display(ILOSTD(ostream)& out) const{
		out << "[";
		for (IloInt i=0; i< getSize(); i++){
			IloSymbol s = getValue(i);
			out << s;
			if (i != getSize()-1) out << ", ";
		}
		out << "]";
	}
};



typedef IloSymbolSet::Iterator IloSymbolSetIterator;

ILOSTD(ostream)& operator<<(ILOSTD(ostream)& out, const IloSymbolSet& set);
#ifdef _WIN32
#pragma pack(pop)
#endif


#endif
