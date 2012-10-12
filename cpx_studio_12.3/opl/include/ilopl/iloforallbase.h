// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/iloforallbase.h
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

#ifndef __ADVANCED_iloforallbaseH
#define __ADVANCED_iloforallbaseH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>
#include <ilconcert/iloextractable.h>
#include <ilconcert/iloexpression.h>
#include <ilconcert/ilocollection.h>


class IloIntIndex;
class IloNumIndex;
class IloSymbolIndex;

class IloIntCollectionIndex;
class IloNumCollectionIndex;
class IloSymbolCollectionIndex;

class IloTupleIndex;
class IloTuplePattern;


class ILO_EXPORTED IloIndex {
private:
	IloExtractableI* _impl;
public:
	
	IloIndex():_impl(0) {}
	
	IloIndex(IloIntIndex x);
	
	IloIndex(IloNumIndex x);
	
	IloIndex(IloSymbolIndex x);

	
	IloIndex(IloIntCollectionIndex x);
	
	IloIndex(IloNumCollectionIndex x);
	
	IloIndex(IloSymbolCollectionIndex x);

	
	IloIndex(IloTupleIndex x);
	
	IloIndex(IloTuplePattern x);
	~IloIndex() {}
	
	IloExtractableI* getImpl() const { return _impl; }
	
	IloEnv getEnv() const {
		IloAssert(_impl != 0, "Access empty IloIndex handle.");
		return _impl->getEnv();
	}
	class NotSubstituted : public IloException {
	private:
		const IloExtractableI* _idx;
	public:
		NotSubstituted(const IloExtractableI* idx);
		virtual const char* getMessage() const;
		void print(ILOSTD(ostream)& out) const;
	};
};


#ifdef _WIN32
#pragma pack(pop)
#endif


#endif
