// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/iloforall.h
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

#ifndef __ADVANCED_iloforallH
#define __ADVANCED_iloforallH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>
#include <ilconcert/iloset.h>
#include <ilopl/ilosymbol.h>
#include <ilopl/ilotuple.h>
#include <ilconcert/ilocollection.h>
#include <ilopl/ilomember.h>
#include <ilopl/iloord.h>
#include <ilopl/iloforalli.h>

class IloIntGenerator;
class IloNumGenerator;
class IloSymbolGenerator;
class IloTupleGenerator;
class IloTuplePattern;


class ILO_EXPORTED IloIntIndex : public IloIntExprArg {
	ILOEXTRHANDLE(IloIntIndex, IloIntExprArg)
public:
	
	IloIntIndex(IloEnv env, const char* name=0);
};


class ILO_EXPORTED IloNumIndex : public IloNumExprArg {
	ILOEXTRHANDLE(IloNumIndex, IloNumExprArg)
public:
	
	IloNumIndex(IloEnv env, const char* name=0);
};


class ILO_EXPORTED IloSymbolIndex : public IloSymbolExprArg {
	ILOEXTRHANDLE(IloSymbolIndex, IloSymbolExprArg)
public:
	
	IloSymbolIndex(IloEnv env, const char* name=0);
};


class ILO_EXPORTED IloTupleIndex : public IloTupleExprArg {
	ILOEXTRHANDLE(IloTupleIndex, IloTupleExprArg)
public:
	
	IloTupleIndex(IloEnv env, const char* name=0);
};

class ILO_EXPORTED IloTuplePattern : public IloTupleIndex {
	ILOEXTRHANDLE(IloTuplePattern, IloTupleIndex)
public:
	
	IloTuplePattern(IloEnv env,
		IloTuplePatternItem a1);
	
	IloTuplePattern(IloEnv env,
		IloTuplePatternItemArray args);

public:
	//-------------------------------------------
	class NoArityMatching : public IloException {
	private:
		const IloTuplePatternI* _pattern;
		IloTupleSchema _schema;
	public:
		NoArityMatching(const IloTuplePatternI* p, IloTupleSchema s);
		virtual const char* getMessage() const;
		void print(ILOSTD(ostream)& out) const;
	};

	class NoColumnMatching : public IloException {
	private:
		const IloTuplePatternI* _pattern;
		const IloTuplePatternItem* _item;
		const IloColumnDefinitionI* _column;
		IloInt _pos;
	public:
		NoColumnMatching(const IloTuplePatternI*, const IloTuplePatternItem* item, const IloColumnDefinitionI*, IloInt);
		virtual const char* getMessage() const;
		void print(ILOSTD(ostream)& out) const;
	};

	class InvalidPatternItem : public IloException {
	private:
		const IloTuplePatternI* _pattern;
		const IloTuplePatternItem* _item;
	public:
		InvalidPatternItem(const IloTuplePatternI*, const IloTuplePatternItem*);
		virtual const char* getMessage() const;
		void print(ILOSTD(ostream)& out) const;
	};

	class SchemaGeneration : public IloException {
	private:
		const IloTuplePatternI* _pattern;
		const IloTuplePatternItem* _item;
	public:
		SchemaGeneration(const IloTuplePatternI*, const IloTuplePatternItem*);
		virtual const char* getMessage() const;
		void print(ILOSTD(ostream)& out) const;
	};
};

///////////////////////////////////////////////////////////////////////////////
//// Generators
///////////////////////////////////////////////////////////////////////////////


class ILO_EXPORTED IloIntGenerator : public IloGenerator {
public:
	
	IloIntGenerator():IloGenerator() {}
	
	IloIntGenerator(IloIntGeneratorI* impl):IloGenerator(impl) {}
	
	IloIntGeneratorI* getImpl() const {
		return (IloIntGeneratorI*)IloGenerator::getImpl();
	}
	
	IloIntIndex getIndex() const {
		IloAssert(getImpl(), "Using empty IloIntGenerator handle.");
		return (getImpl()->getIndex());
	}
};

class ILO_EXPORTED IloNumGenerator : public IloGenerator {
public:
	
	IloNumGenerator():IloGenerator() {}
	
	IloNumGenerator(IloNumGeneratorI* impl):IloGenerator(impl) {}
	
	IloNumGeneratorI* getImpl() const {
		return (IloNumGeneratorI*)IloGenerator::getImpl();
	}
	
	IloNumIndex getIndex() const {
		IloAssert(getImpl(), "Using empty IloNumGenerator handle.");
		return (getImpl()->getIndex());
	}
};

class ILO_EXPORTED IloSymbolGenerator : public IloGenerator {
public:
	
	IloSymbolGenerator():IloGenerator() {}
	
	IloSymbolGenerator(IloSymbolGeneratorI* impl):IloGenerator(impl) {}
	
	IloSymbolGeneratorI* getImpl() const {
		return (IloSymbolGeneratorI*)IloGenerator::getImpl();
	}
	
	IloSymbolIndex getIndex() const {
		IloAssert(getImpl(), "Using empty IloSymbolGenerator handle.");
		return (IloSymbolIndexI*)getImpl()->getIndex();
	}
};

class ILO_EXPORTED IloTupleGenerator : public IloGenerator {
public:
	
	IloTupleGenerator():IloGenerator() {}
	
	IloTupleGenerator(IloTupleGeneratorI* impl):IloGenerator(impl) {}
	
	IloTupleGeneratorI* getImpl() const {
		return (IloTupleGeneratorI*)IloGenerator::getImpl();
	}
	
	IloTupleIndex getIndex() const {
		IloAssert(getImpl(), "Using empty IloTupleGenerator handle.");
		return (IloTupleIndexI*)getImpl()->getIndex();
	}
};


ILO_EXPORTEDFUNCTION(IloConstraint) IloForAll(IloComprehensionI* comp, IloConstraint ct, const char* name=0);

/////////////////////////////////////////////////////////////////////////////
// Aggregation operators
/////////////////////////////////////////////////////////////////////////////
ILO_EXPORTEDFUNCTION(IloIntExprArg) IloAggregatedSum(IloComprehensionI* comp, IloIntExprArg e);
ILO_EXPORTEDFUNCTION(IloNumExprArg) IloAggregatedSum(IloComprehensionI* comp, IloNumExprArg e);

ILO_EXPORTEDFUNCTION(IloIntExprArg) IloAggregatedMin(IloComprehensionI* comp,IloIntExprArg e);
ILO_EXPORTEDFUNCTION(IloNumExprArg) IloAggregatedMin(IloComprehensionI* comp, IloNumExprArg e);

ILO_EXPORTEDFUNCTION(IloNumExprArg) IloAggregatedProd(IloComprehensionI* comp, IloNumExprArg e);
ILO_EXPORTEDFUNCTION(IloIntExprArg) IloAggregatedProd(IloComprehensionI* comp, IloIntExprArg e);

ILO_EXPORTEDFUNCTION(IloNumExprArg) IloAggregatedMax(IloComprehensionI* comp, IloNumExprArg e);
ILO_EXPORTEDFUNCTION(IloIntExprArg) IloAggregatedMax(IloComprehensionI* comp, IloIntExprArg e);

ILO_EXPORTEDFUNCTION(IloConstraint) IloAggregatedOr(IloComprehensionI* comp, IloConstraint e);
ILO_EXPORTEDFUNCTION(IloConstraint) IloAggregatedAnd(IloComprehensionI* comp, IloConstraint e);


/////////////////////////////////////////////////////////////////////////////
// Aggregate Piecewise linear function
/////////////////////////////////////////////////////////////////////////////

ILO_EXPORTEDFUNCTION(IloNumExprArg) IloPiecewiseLinear(const IloNumExprArg node,
													   IloComprehensionI* comp,
													   const IloNumExprArg point,
													   const IloNumExprArg slope,
													   const IloNumExprArg lastSlope,
													   const IloNumExprArg a,
													   const IloNumExprArg fa);



ILO_EXPORTEDFUNCTION(IloNumExprArg) IloPiecewiseLinear(const IloNumExprArg node,
													   const IloNumExprArg firstSlope,
													   IloComprehensionI* comp,
													   const IloNumExprArg x,
													   const IloNumExprArg y,
													   const IloNumExprArg lastSlope);

ILO_EXPORTEDFUNCTION(IloNumExprArg) IloSlack(const IloConstraint ct);
ILO_EXPORTEDFUNCTION(IloNumExprArg) IloDual(const IloConstraint ct);
ILO_EXPORTEDFUNCTION(IloNumExprArg) IloReducedCost(const IloNumVar var);


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
