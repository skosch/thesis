// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilooplelementi.h
// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
//
// 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55 
// Copyright IBM Corp. 1998, 2011
//
// US Government Users Restricted Rights - Use, duplication or
// disclosure restricted by GSA ADP Schedule Contract with
// IBM Corp.
// ---------------------------------------------------------------------------

#ifndef __OPL_ilooplelementiH
#define __OPL_ilooplelementiH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#ifndef __OPL_ilosysH
#include <ilopl/ilosys.h>
#endif

#include <ilopl/ilooplexception.h>

#include <ilconcert/iloenv.h>

#include <ilconcert/ilomodel.h>
#include <ilconcert/ilocollection.h>
#include <ilopl/iloforall.h>
#include <ilopl/ilomapi.h>
#include <ilopl/ilotuplemap.h>

class IloOplLocationI;

class IloNumSet;
class IloIntSet;
class IloTupleSet;
class IloNumRange;
class IloIntRange;
class IloTupleSchema;
class IloConstraint;
class IloOplModelDefinitionI;
class IloCplexI;

class IloOplDataSerializerI;


class ILOOPL_EXPORTED IloOplElementI: public IloRttiEnvObjectI {
    ILORTTIDECL
public:
    virtual ~IloOplElementI() {
    }

    virtual const char* getName() const =0;
    virtual IloOplElementType::Type getElementType() const =0;
    virtual const IloOplLocationI& getLocation() const =0;

    virtual IloNum asNum() const =0;
    virtual IloInt asInt() const =0;
    virtual const char* asString() const =0;
    virtual IloTuple asTuple() const =0;

    virtual IloNumMap asNumMap() const =0;
    virtual IloIntMap asIntMap() const =0;
    virtual IloSymbolMap asSymbolMap() const =0;
    virtual IloTupleMap asTupleMap() const =0;

    virtual IloNumSetMap asNumSetMap() const =0;
    virtual IloIntSetMap asIntSetMap() const =0;
    virtual IloSymbolSetMap asSymbolSetMap() const =0;
    virtual IloTupleSetMap asTupleSetMap() const =0;

    virtual IloNumSet asNumSet() const =0;
    virtual IloIntSet asIntSet() const =0;
    virtual IloSymbolSet asSymbolSet() const =0;
    virtual IloTupleSet asTupleSet() const =0;

    virtual IloNumRange asNumRange() const =0;
    virtual IloIntRange asIntRange() const =0;
    virtual IloTupleSchema asTupleSchema() const =0;

    virtual IloConstraint asConstraint() const =0;
    virtual IloConstraintMap asConstraintMap() const =0;

    virtual IloBool isDecisionVariable() const =0;
    virtual IloBool isDecisionExpression() const =0;
    virtual IloBool isExternalData() const =0;
    virtual IloBool isInternalData() const =0;
    virtual IloBool isPostProcessing() const =0;

    virtual IloNumVar asNumVar() const =0;
    virtual IloIntVar asIntVar() const =0;
    virtual IloNumExprArg asNumExpr() const =0;
    virtual IloIntExprArg asIntExpr() const =0;

    virtual IloNumVarMap asNumVarMap() const =0;
    virtual IloIntVarMap asIntVarMap() const =0;
    virtual IloNumDExprMap asNumExprMap() const =0;
    virtual IloIntDExprMap asIntExprMap() const =0;

    virtual IloOplModelDefinitionI* asTemplateDefinition() const =0;

    virtual IloNumVarArray asNumVarArray() const =0;

	virtual IloIntervalVar asIntervalVar() const = 0;
	virtual IloIntervalVarMap asIntervalVarMap() const = 0;

	virtual IloIntervalSequenceVar asIntervalSequenceVar() const = 0;
	virtual IloIntervalSequenceVarMap asIntervalSequenceVarMap() const = 0;

	virtual IloPiecewiseFunctionExpr asPiecewiseFunctionExpr() const = 0;
	virtual IloPiecewiseFunctionExprMap asPiecewiseFunctionExprMap() const = 0;

	virtual IloCumulFunctionExpr asCumulFunctionExpr() const = 0;
	virtual IloCumulFunctionExprMap asCumulFunctionExprMap() const = 0;

	virtual IloStateFunctionExpr asStateFunctionExpr() const = 0;
	virtual IloStateFunctionExprMap asStateFunctionExprMap() const = 0;

	virtual void* getInternalValue() const;

    void displayValue(ostream& outs) const;

protected:
    explicit IloOplElementI(IloEnvI* env):IloRttiEnvObjectI(env) {}

    virtual void printValue(IloOplDataSerializerI&) const;

private:
    DONT_COPY_OPL(IloOplElementI)
};


class ILOOPL_EXPORTED IloOplElementIteratorI: public IloDestroyableI {
    ILORTTIDECL
public:
    virtual IloBool ok() const = 0;
    virtual void operator++() =0;
    virtual IloOplElementI& operator*() const =0;
        
protected:
    IloOplElementIteratorI(IloEnvI* env) :IloDestroyableI(env) {}
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif

