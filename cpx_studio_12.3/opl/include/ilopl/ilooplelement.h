// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilooplelement.h
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

#ifndef __OPL_ilooplelementH
#define __OPL_ilooplelementH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#ifndef __OPL_ilosysH
#include <ilopl/ilosys.h>
#endif


class IloOplElementType {
public:
    
    enum Type {
       UNKNOWN=0, 
       INT, 
       NUM, 
       STRING, 
       TUPLE,
       MAP_NUM, 
       MAP_INT, 
       MAP_SYMBOL, 
       MAP_TUPLE,
       MAP_SET_NUM, 
       MAP_SET_INT, 
       MAP_SET_SYMBOL, 
       MAP_SET_TUPLE,
       SET_NUM, 
       SET_INT, 
       SET_SYMBOL, 
       SET_TUPLE,
       RANGE_NUM, 
       RANGE_INT,
       TUPLE_SCHEMA,
       CONSTRAINT,
       MAP_CONSTRAINT,
       TEMPLATE, 
	   INTERVAL, 
	   SEQUENCE, 
	   PIECEWISE, 
	   CUMUL_FUNCTION, 
	   STATE_FUNCTION, 
	   MAP_INTERVAL,  
	   MAP_SEQUENCE,  
	   MAP_PIECEWISE, 
	   MAP_CUMUL_FUNCTION, 
	   MAP_STATE_FUNCTION  
    };
private:
    IloOplElementType();
};

#include <ilopl/ilooplerrorhandler.h>

#include <ilopl/ilomapi.h>
#include <ilopl/ilotuplemap.h>
#include <ilopl/ilooplelementi.h>

class IloOplModelDefinition;



class ILOOPL_EXPORTED IloOplElement {
    HANDLE_DECL_OPL(IloOplElement)
public:
    
    typedef IloOplElementType::Type Type;

    
    const char* getName() const;

    
    IloOplElement::Type getElementType() const;

    
     IloOplLocation getLocation() const;

    
    IloNum asNum() const;

    
    IloInt asInt() const;

    
    const char* asString() const;

    
    IloTuple asTuple() const;

    
    IloNumMap asNumMap() const;

    
    IloIntMap asIntMap() const;

    
    IloSymbolMap asSymbolMap() const;

    
    IloTupleMap asTupleMap() const;

    
    IloNumSetMap asNumSetMap() const;

    
    IloIntSetMap asIntSetMap() const;

    
    IloSymbolSetMap asSymbolSetMap() const;

    
    IloTupleSetMap asTupleSetMap() const;

    
    IloNumSet asNumSet() const;

    
    IloIntSet asIntSet() const;

    
    IloSymbolSet asSymbolSet() const;

    
    IloTupleSet asTupleSet() const;

    
    IloNumRange asNumRange() const;

    
    IloIntRange asIntRange() const;

    
    IloTupleSchema asTupleSchema() const;

    
    IloConstraint asConstraint() const;

    
    IloConstraintMap asConstraintMap() const;

    
    IloBool isDecisionVariable() const;

    
    IloBool isDecisionExpression() const;

    
    IloBool isExternalData() const;

    
    ILO_OPL_DEPRECATED
    IloBool isData() const;

    
    IloBool isInternalData() const;

    
    ILO_OPL_DEPRECATED
    IloBool isCalculated() const;

    
    IloBool isPostProcessing() const;

    
    IloNumVar asNumVar() const;

    
    IloNumExprArg asNumExpr() const;

    
    IloIntVar asIntVar() const;

    
    IloIntExprArg asIntExpr() const;
    
    IloNumVarMap asNumVarMap() const;
    
    IloIntVarMap asIntVarMap() const;
    
    IloNumDExprMap asNumExprMap() const;
    
    IloIntDExprMap asIntExprMap() const;
    
    IloOplModelDefinition asTemplateDefinition() const;

    
	IloIntervalVar asIntervalVar() const;

    
	IloIntervalVarMap asIntervalVarMap() const;

    
	IloIntervalSequenceVar asIntervalSequenceVar() const;

    
	IloIntervalSequenceVarMap asIntervalSequenceVarMap() const;

    
	IloPiecewiseFunctionExpr asPiecewiseFunctionExpr() const;

    
	IloPiecewiseFunctionExprMap asPiecewiseFunctionExprMap() const;

    
	IloCumulFunctionExpr asCumulFunctionExpr() const;

    
	IloCumulFunctionExprMap asCumulFunctionExprMap() const;

    
	IloStateFunctionExpr asStateFunctionExpr() const;

    
	IloStateFunctionExprMap asStateFunctionExprMap() const;
};


class IloOplElementIteratorI;


class ILOOPL_EXPORTED IloOplElementIterator {
    HANDLE_DECL_OPL(IloOplElementIterator)
public:
    
    IloBool ok() const;

    
    void operator++();

    
    IloOplElement operator*() const;
};

// ---------------------------------------------------------------------------
// inline implementations

inline
const char* IloOplElement::getName() const {
    return impl().getName();
}
inline
IloOplElementType::Type IloOplElement::getElementType() const {
    return impl().getElementType();
}
inline
IloOplLocation IloOplElement::getLocation() const {
    return &impl().getLocation();
}
inline
IloNum IloOplElement::asNum() const {
    return impl().asNum();
}
inline
IloInt IloOplElement::asInt() const {
    return impl().asInt();
}
inline
const char* IloOplElement::asString() const {
    return impl().asString();
}
inline
IloTuple IloOplElement::asTuple() const {
    return impl().asTuple();
}
inline
IloNumMap IloOplElement::asNumMap() const {
    return impl().asNumMap();
}
inline
IloIntMap IloOplElement::asIntMap() const {
    return impl().asIntMap();
}
inline
IloSymbolMap IloOplElement::asSymbolMap() const {
    return impl().asSymbolMap();
}
inline
IloTupleMap IloOplElement::asTupleMap() const {
    return impl().asTupleMap();
}
inline
IloNumSet IloOplElement::asNumSet() const {
    return impl().asNumSet();
}
inline
IloIntSet IloOplElement::asIntSet() const {
    return impl().asIntSet();
}
inline
IloSymbolSet IloOplElement::asSymbolSet() const {
    return impl().asSymbolSet();
}
inline
IloTupleSet IloOplElement::asTupleSet() const {
    return impl().asTupleSet();
}
inline
IloNumSetMap IloOplElement::asNumSetMap() const {
    return impl().asNumSetMap();
}
inline
IloIntSetMap IloOplElement::asIntSetMap() const {
    return impl().asIntSetMap();
}
inline
IloSymbolSetMap IloOplElement::asSymbolSetMap() const {
    return impl().asSymbolSetMap();
}
inline
IloTupleSetMap IloOplElement::asTupleSetMap() const {
    return impl().asTupleSetMap();
}
inline
IloNumRange IloOplElement::asNumRange() const {
    return impl().asNumRange();
}
inline
IloIntRange IloOplElement::asIntRange() const {
    return impl().asIntRange();
}
inline
IloTupleSchema IloOplElement::asTupleSchema() const {
    return impl().asTupleSchema();
}
inline
IloConstraint IloOplElement::asConstraint() const {
    return impl().asConstraint();
}
inline
IloConstraintMap IloOplElement::asConstraintMap() const {
    return impl().asConstraintMap();
}
inline
IloBool IloOplElement::isDecisionVariable() const {
    return impl().isDecisionVariable();
}
inline
IloBool IloOplElement::isDecisionExpression() const {
    return impl().isDecisionExpression();
}
inline
IloBool IloOplElement::isExternalData() const {
    return impl().isExternalData();
}
inline
IloBool IloOplElement::isData() const {
    return impl().isExternalData();
}
inline
IloBool IloOplElement::isInternalData() const {
    return impl().isInternalData();
}
inline
IloBool IloOplElement::isCalculated() const {
    return impl().isInternalData();
}
inline
IloBool IloOplElement::isPostProcessing() const {
    return impl().isPostProcessing();
}

inline
IloNumVar IloOplElement::asNumVar() const {
    return impl().asNumVar();
}
inline
IloNumExprArg IloOplElement::asNumExpr() const {
    return impl().asNumExpr();
}
inline
IloIntVar IloOplElement::asIntVar() const {
    return impl().asIntVar();
}
inline
IloIntExprArg IloOplElement::asIntExpr() const {
    return impl().asIntExpr();
}
inline
IloNumVarMap IloOplElement::asNumVarMap() const {
    return impl().asNumVarMap();
}
inline
IloIntVarMap IloOplElement::asIntVarMap() const {
    return impl().asIntVarMap();
}
inline
IloNumDExprMap IloOplElement::asNumExprMap() const {
    return impl().asNumExprMap();
}
inline
IloIntDExprMap IloOplElement::asIntExprMap() const {
    return impl().asIntExprMap();
}


inline IloIntervalVar IloOplElement::asIntervalVar() const {
	return impl().asIntervalVar();
}

inline IloIntervalVarMap IloOplElement::asIntervalVarMap() const {
	return impl().asIntervalVarMap();
}

inline IloIntervalSequenceVar IloOplElement::asIntervalSequenceVar() const {
	return impl().asIntervalSequenceVar();
}

inline IloIntervalSequenceVarMap IloOplElement::asIntervalSequenceVarMap() const {
	return impl().asIntervalSequenceVarMap();
}

inline IloPiecewiseFunctionExpr IloOplElement::asPiecewiseFunctionExpr() const {
	return impl().asPiecewiseFunctionExpr();
}

inline IloPiecewiseFunctionExprMap IloOplElement::asPiecewiseFunctionExprMap() const {
	return impl().asPiecewiseFunctionExprMap();
}

inline IloCumulFunctionExpr IloOplElement::asCumulFunctionExpr() const {
	return impl().asCumulFunctionExpr();
}

inline IloCumulFunctionExprMap IloOplElement::asCumulFunctionExprMap() const {
	return impl().asCumulFunctionExprMap();
}

inline IloStateFunctionExpr IloOplElement::asStateFunctionExpr() const {
	return impl().asStateFunctionExpr();
}

inline IloStateFunctionExprMap IloOplElement::asStateFunctionExprMap() const {
	return impl().asStateFunctionExprMap();
}

inline
IloBool IloOplElementIterator::ok() const {
    return impl().ok();
}
inline
void IloOplElementIterator::operator++() {
    impl().operator++();
}
inline
IloOplElement IloOplElementIterator::operator*() const {
    return &impl().operator*();
}


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif

