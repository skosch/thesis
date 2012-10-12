// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilocollexprbase.h
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

#ifndef __ADVANCED_ilocollexprbaseH
#define __ADVANCED_ilocollexprbaseH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>
#include <ilconcert/iloany.h>
#include <ilconcert/iloanyexpri.h>
#include <ilconcert/ilocollection.h>
#include <ilopl/iloforall.h>
#include <ilopl/ilocollexpr/ilocollexpri.h>

class IloIntCollectionExprGenerator;
class IloNumCollectionExprGenerator;
class IloSymbolCollectionExprGenerator;



//-----------------------------------------------------------------

class ILO_EXPORTED IloIntCollectionExprArg : public IloExtractable {
	ILOEXTRHANDLE(IloIntCollectionExprArg, IloExtractable)
public:
	
	IloIntCollectionExprArg(IloIntSubMapExpr handle);
	
	IloIntCollectionExprArg(IloIntExprArg lb, IloIntExprArg ub);
	
	IloIntCollectionExprArg(IloIntExprArg lb, IloInt ub);
	
	IloIntCollectionExprArg(IloInt lb, IloIntExprArg ub);
};


class ILO_EXPORTED IloNumCollectionExprArg : public IloExtractable {
	ILOEXTRHANDLE(IloNumCollectionExprArg, IloExtractable)
public:
	
	IloNumCollectionExprArg(IloNumSubMapExpr handle);
	IloNumCollectionExprArg(IloNumExprArg lb, IloNumExprArg ub);
	IloNumCollectionExprArg(IloNumExprArg lb, IloNum ub);
	IloNumCollectionExprArg(IloNum lb, IloNumExprArg ub);
};



class ILO_EXPORTED IloSymbolCollectionExprArg : public IloExtractable {
	ILOEXTRHANDLE(IloSymbolCollectionExprArg, IloExtractable)
};


class IloIntCollectionIndex : public IloIntCollectionExprArg {
	ILOEXTRHANDLE(IloIntCollectionIndex, IloIntCollectionExprArg)
public:
	
	IloIntCollectionIndex(IloEnv env, const char* name=0);

	class NotSubstituted : public IloException {
	private:
		const IloIntCollectionIndexI* _idx;
	public:
		NotSubstituted(const IloIntCollectionIndexI* idx);
		NotSubstituted(const IloIntCollectionIndex idx);
		virtual const char* getMessage() const;
		void print(ILOSTD(ostream)& out) const;
	};
	class TupleCellGenerator : public IloException {
	private:
		const IloIntCollectionIndexI* _idx;
	public:
		TupleCellGenerator(const IloIntCollectionIndexI* idx);
		virtual const char* getMessage() const;
		void print(ILOSTD(ostream)& out) const;
	};
};
//--------------------------


class IloNumCollectionIndex : public IloNumCollectionExprArg {
	ILOEXTRHANDLE(IloNumCollectionIndex, IloNumCollectionExprArg)
public:
	
	IloNumCollectionIndex(IloEnv env, const char* name=0);

	class NotSubstituted : public IloException {
	private:
		const IloNumCollectionIndexI* _idx;
	public:
		NotSubstituted(const IloNumCollectionIndexI* idx);
		NotSubstituted(const IloNumCollectionIndex idx);
		virtual const char* getMessage() const;
		void print(ILOSTD(ostream)& out) const;
	};
	class TupleCellGenerator : public IloException {
	private:
		const IloNumCollectionIndexI* _idx;
	public:
		TupleCellGenerator(const IloNumCollectionIndexI* idx);
		virtual const char* getMessage() const;
		void print(ILOSTD(ostream)& out) const;
	};
};



class IloSymbolCollectionIndex : public IloSymbolCollectionExprArg {
	ILOEXTRHANDLE(IloSymbolCollectionIndex, IloSymbolCollectionExprArg)
public:
	
	IloSymbolCollectionIndex(IloEnv env, const char* name=0);

	class NotSubstituted : public IloException {
	private:
		const IloSymbolCollectionIndexI* _idx;
	public:
		NotSubstituted(const IloSymbolCollectionIndexI* idx);
		NotSubstituted(const IloSymbolCollectionIndex idx);
		virtual const char* getMessage() const;
		void print(ILOSTD(ostream)& out) const;
	};
	class TupleCellGenerator : public IloException {
	private:
		const IloSymbolCollectionIndexI* _idx;
	public:
		TupleCellGenerator(const IloSymbolCollectionIndexI* idx);
		virtual const char* getMessage() const;
		void print(ILOSTD(ostream)& out) const;
	};
};

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
