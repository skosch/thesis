// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilooplelementdefinition.h
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

#ifndef __OPL_ilooplelementdefinitionH
#define __OPL_ilooplelementdefinitionH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#ifndef __OPL_ilosysH
#include <ilopl/ilosys.h>
#endif


class IloOplElementDefinitionType {
public:
    enum Type {
       UNKNOWN=0,
       INTEGER, FLOAT, STRING, BOOLEAN,
       ARRAY,
       SET,
	   CONSTRAINT,
       RANGE,
	   TUPLE, 
       TUPLE_SCHEMA,
       TEMPLATE,
       INTERVAL,
       SEQUENCE,
       FUNCTION,
       CUMUL_FUNCTION,
       STATE_FUNCTION,
       MAX
    };
	static const char* ToString(Type);
private:
    IloOplElementDefinitionType();
};

#include <ilopl/ilooplerrorhandler.h>
#include <ilopl/ilooplelementdefinitioni.h>

class IloOplModelDefinition;
class IloOplArrayDefinition;
class IloOplSetDefinition;
class IloOplRangeDefinition;
class IloOplTupleDefinition;
class IloOplTupleSchemaDefinition;
class IloOplTemplateElementDefinition;
class IloOplConstraintDefinition;
class IloOplDecisionExprDefinition;


class ILOOPL_EXPORTED IloOplElementDefinition {
    HANDLE_DECL_OPL(IloOplElementDefinition)
public:
    
    typedef IloOplElementDefinitionType::Type Type;
    
	const char* getName() const {
		return impl().getName();
	}
    
	IloOplElementDefinitionType::Type getElementDefinitionType() const {
		return impl().getElementDefinitionType();
	}
    
    IloOplLocation getLocation() const {
		return &impl().getLocation();
	}

    
    IloOplArrayDefinition asArray() const;
    
    IloOplSetDefinition asSet() const;
    
    IloOplRangeDefinition asRange() const;
    
    IloOplTupleDefinition asTuple() const;
    
    IloOplTupleSchemaDefinition asTupleSchema() const;
	
    IloOplTemplateElementDefinition asTemplate() const;
	
    IloOplConstraintDefinition asConstraint() const;
	
    IloOplDecisionExprDefinition asDecisionExpression() const;

	
	IloBool isDecisionVariable() const {
		return impl().isDecisionVariable();
	}
	
	IloBool isDecisionExpression() const {
		return impl().isDecisionExpression();
	}
    
    IloBool isExternalData() const {
		return impl().isExternalData();
	}
    
    IloBool isInternalData() const {
		return impl().isInternalData();
	}
    
    ILO_OPL_DEPRECATED IloBool isData() const {
		return impl().isExternalData();
	}
    
    ILO_OPL_DEPRECATED IloBool isCalculated() const {
		return impl().isInternalData();
	}
    
    IloBool isPostProcessing() const {
		return impl().isPostProcessing();
	}

	
	IloOplElementDefinition getLeaf() const;
};


class ILOOPL_EXPORTED IloOplArrayDefinition: public IloOplElementDefinition {
public:
	
	IloOplArrayDefinition(const IloOplArrayDefinitionI* impl):IloOplElementDefinition(impl) {
	}

	
	IloInt getDimensions() const {
		return static_cast<const IloOplArrayDefinitionI&>(impl()).getDimensions();
	}
	
	IloOplElementDefinition getIndexer(IloInt i) const {
		return &static_cast<const IloOplArrayDefinitionI&>(impl()).getIndexer(i);
	}
    
	IloOplElementDefinition getItem() const {
		return &static_cast<const IloOplArrayDefinitionI&>(impl()).getItem();
	}
    
	IloBool hasReferences() const {
		return static_cast<const IloOplArrayDefinitionI&>(impl()).hasReferences();
	}
    
	IloBool isReference(IloOplElementDefinition component) const {
		return static_cast<const IloOplArrayDefinitionI&>(impl()).isReference(component.impl());
	}
    
	IloOplElementDefinition getReference(IloOplElementDefinition component) const {
		return &static_cast<const IloOplArrayDefinitionI&>(impl()).getReference(component.impl());
	}
};


class ILOOPL_EXPORTED IloOplSetDefinition: public IloOplElementDefinition {
public:
	
	IloOplSetDefinition(const IloOplSetDefinitionI* impl):IloOplElementDefinition(impl) {
	}
    
	IloOplElementDefinition getItem() const {
		return &static_cast<const IloOplSetDefinitionI&>(impl()).getItem();
	}
    
	IloBool hasReferences() const {
		return static_cast<const IloOplSetDefinitionI&>(impl()).hasReferences();
	}
    
	IloBool isReference(IloOplElementDefinition component) const {
		return static_cast<const IloOplSetDefinitionI&>(impl()).isReference(component.impl());
	}
    
	IloOplElementDefinition getReference(IloOplElementDefinition component) const {
		return &static_cast<const IloOplSetDefinitionI&>(impl()).getReference(component.impl());
	}

    
	IloBool isOrdered() const {
		return static_cast<const IloOplSetDefinitionI&>(impl()).isOrdered();
	}
    
	IloBool isSorted() const {
		return static_cast<const IloOplSetDefinitionI&>(impl()).isSorted();
	}
    
	IloBool isReversed() const {
		return static_cast<const IloOplSetDefinitionI&>(impl()).isReversed();
	}
};


class ILOOPL_EXPORTED IloOplRangeDefinition: public IloOplElementDefinition {
public:
	
	IloOplRangeDefinition(const IloOplRangeDefinitionI* impl):IloOplElementDefinition(impl) {
	}
    
	IloBool isFloat() const {
		return static_cast<const IloOplRangeDefinitionI&>(impl()).isFloat();
	}

    
    IloBool isSimpleRange() const {
		return static_cast<const IloOplRangeDefinitionI&>(impl()).isSimpleRange();
	}
    
    IloBool isSimpleLB() const {
		return static_cast<const IloOplRangeDefinitionI&>(impl()).isSimpleLB();
	}
    
    IloBool isSimpleLBElement() const {
		return static_cast<const IloOplRangeDefinitionI&>(impl()).isSimpleLBElement();
	}
    
    IloBool isSimpleUB() const {
		return static_cast<const IloOplRangeDefinitionI&>(impl()).isSimpleUB();
	}
    
    IloBool isSimpleUBElement() const {
		return static_cast<const IloOplRangeDefinitionI&>(impl()).isSimpleUBElement();
	}
    
	const char* getSimpleLB() const {
		return static_cast<const IloOplRangeDefinitionI&>(impl()).getSimpleLB();
	}
    
	IloOplElementDefinition getSimpleLBElement() const {
		return &static_cast<const IloOplRangeDefinitionI&>(impl()).getSimpleLBElement();
	}
    
	const char* getSimpleUB() const {
		return static_cast<const IloOplRangeDefinitionI&>(impl()).getSimpleUB();
	}
    
	IloOplElementDefinition getSimpleUBElement() const {
		return &static_cast<const IloOplRangeDefinitionI&>(impl()).getSimpleUBElement();
	}
};


class ILOOPL_EXPORTED IloOplTupleSchemaDefinition: public IloOplElementDefinition {
public:
 	
	IloOplTupleSchemaDefinition(const IloOplTupleSchemaDefinitionI* impl):IloOplElementDefinition(impl) {
	}
   
	IloInt getSize() const {
		return static_cast<const IloOplTupleSchemaDefinitionI&>(impl()).getComponentArray().getSize();
	}
    
	IloOplElementDefinition getComponent(IloInt i) const {
		return static_cast<const IloOplTupleSchemaDefinitionI&>(impl()).getComponentArray()[i];
	}
    
	IloOplTupleSchemaDefinition getKeyTupleSchema() const {
		return &static_cast<const IloOplTupleSchemaDefinitionI&>(impl()).getKeyTupleSchema();
	}
    
	IloBool hasKey() const {
		return static_cast<const IloOplTupleSchemaDefinitionI&>(impl()).hasKey();
	}
    
	IloBool isKey(IloOplElementDefinition component) const {
		return static_cast<const IloOplTupleSchemaDefinitionI&>(impl()).isKey(component.impl());
	}
};


class ILOOPL_EXPORTED IloOplTupleDefinition: public IloOplElementDefinition {
public:
	
	IloOplTupleDefinition(const IloOplTupleDefinitionI* impl):IloOplElementDefinition(impl) {
	}
    
	IloOplTupleSchemaDefinition getTupleSchema() const {
		return &static_cast<const IloOplTupleDefinitionI&>(impl()).getTupleSchema();
	}
};


class ILOOPL_EXPORTED IloOplTemplateElementDefinition: public IloOplElementDefinition {
public:
	
	IloOplTemplateElementDefinition(const IloOplTemplateElementDefinitionI* impl):IloOplElementDefinition(impl) {
	}
};


class ILOOPL_EXPORTED IloOplConstraintDefinition: public IloOplElementDefinition {
public:
	
	IloOplConstraintDefinition(const IloOplConstraintDefinitionI* impl): IloOplElementDefinition(impl){
	}

    
	IloInt getIndexCount() const {
		return static_cast<const IloOplConstraintDefinitionI&>(impl()).getIndexCount();
	}

    
	const char* getIndexName(int i) const {
		return static_cast<const IloOplConstraintDefinitionI&>(impl()).getIndexName(i);
	}

    
	IloBool hasIndexCollection(int i) const {
		return static_cast<const IloOplConstraintDefinitionI&>(impl()).hasIndexCollection(i);
	}

    
	IloOplElementDefinition getIndexCollection(int i) const {
		return &static_cast<const IloOplConstraintDefinitionI&>(impl()).getIndexCollection(i);
	}

    
	IloOplElementDefinition getIndexType(int i) const {
		return &static_cast<const IloOplConstraintDefinitionI&>(impl()).getIndexType(i);
	}

    
	IloBool hasMid() const {
		return static_cast<const IloOplConstraintDefinitionI&>(impl()).hasMid();
	}

    
	IloBool hasBounds() const {
		return static_cast<const IloOplConstraintDefinitionI&>(impl()).hasBounds();
	}

    
	IloBool isForall() const {
		return static_cast<const IloOplConstraintDefinitionI&>(impl()).isForall();
	}

    
	IloBool isLogical() const {
		return static_cast<const IloOplConstraintDefinitionI&>(impl()).isLogical();
	}
};



class ILOOPL_EXPORTED IloOplDecisionExprDefinition: public IloOplElementDefinition {
public:
	
	IloOplDecisionExprDefinition(const IloOplDecisionExprDefinitionI* impl):IloOplElementDefinition(impl) {
	}

    
	IloBool isUsedInObjective() const {
		return static_cast<const IloOplDecisionExprDefinitionI&>(impl()).isUsedInObjective();
	}

    
    IloBool isSimpleGoal() const {
		return static_cast<const IloOplDecisionExprDefinitionI&>(impl()).isSimpleGoal();
	}
    
	const char* getSimpleGoalCoef() const {
		return static_cast<const IloOplDecisionExprDefinitionI&>(impl()).getSimpleGoalCoef();
	}
    
	IloBool isSimpleGoalMinimize() const {
		return static_cast<const IloOplDecisionExprDefinitionI&>(impl()).isSimpleGoalMinimize();
	}

    
    IloBool isNonLinear() const {
		return static_cast<const IloOplDecisionExprDefinitionI&>(impl()).isNonLinear();
    }

    
	IloInt getExpandIndexCount() const {
		return static_cast<const IloOplDecisionExprDefinitionI&>(impl()).getExpandIndexCount();
	}
    
	const char* getExpandIndexName(int i) const {
		return static_cast<const IloOplDecisionExprDefinitionI&>(impl()).getExpandIndexName(i);
	}
    
	IloBool hasExpandIndexCollection(int i) const {
		return static_cast<const IloOplDecisionExprDefinitionI&>(impl()).hasExpandIndexCollection(i);
	}
    
	IloOplElementDefinition getExpandIndexCollection(int i) const {
		return &static_cast<const IloOplDecisionExprDefinitionI&>(impl()).getExpandIndexCollection(i);
	}
    
	IloOplElementDefinition getExpandIndexType(int i) const {
		return &static_cast<const IloOplDecisionExprDefinitionI&>(impl()).getExpandIndexType(i);
	}

    
	IloInt getUsedDecisionExprCount() const {
		return static_cast<const IloOplDecisionExprDefinitionI&>(impl()).getUsedDecisionExprCount();
	}
    
	IloOplElementDefinition getUsedDecisionExpr(int i) const {
		return &static_cast<const IloOplDecisionExprDefinitionI&>(impl()).getUsedDecisionExpr(i);
	}
};

// ---------------------------------------------------------------------------

inline IloOplArrayDefinition IloOplElementDefinition::asArray() const {
	return &impl().asArray();
}

inline IloOplSetDefinition IloOplElementDefinition::asSet() const {
	return &impl().asSet();
}

inline IloOplRangeDefinition IloOplElementDefinition::asRange() const {
	return &impl().asRange();
}

inline IloOplTupleDefinition IloOplElementDefinition::asTuple() const {
	return &impl().asTuple();
}

inline IloOplTupleSchemaDefinition IloOplElementDefinition::asTupleSchema() const {
	return &impl().asTupleSchema();
}

inline IloOplTemplateElementDefinition IloOplElementDefinition::asTemplate() const {
	return &impl().asTemplate();
}

inline IloOplConstraintDefinition IloOplElementDefinition::asConstraint() const {
	return &impl().asConstraint();
}

inline IloOplDecisionExprDefinition IloOplElementDefinition::asDecisionExpression() const {
	return &impl().asDecisionExpression();
}

inline IloOplElementDefinition IloOplElementDefinition::getLeaf() const {
	return &impl().getLeaf();
}

// ---------------------------------------------------------------------------

class IloOplElementDefinitionIteratorI;


class ILOOPL_EXPORTED IloOplElementDefinitionIterator {
    HANDLE_DECL_OPL(IloOplElementDefinitionIterator)
public:
    
    IloBool ok() const {
		return impl().ok();
	}
    
    void operator++() {
		return impl().operator++();
	}
    
    IloOplElementDefinition operator*() const {
		return &impl().operator*();
	}
};

// ---------------------------------------------------------------------------


class ILOOPL_EXPORTED IloOplOutlineListenerBaseI: public IloOplOutlineListenerI {
	ILORTTIDECL

    IloOplModelDefinitionI* _def;
    IloBool _verbose;

protected:
    
	IloOplOutlineListenerBaseI(IloEnv env, IloOplModelDefinition def, IloBool verbose);


    virtual void notifyElementDefinition(const IloOplElementDefinitionI& def) {
        notifyElementDefinition(IloOplElementDefinition(&def));
    }

    virtual void notifyPushConstraint(const char* name, const IloOplLocationI& location) {
        notifyPushConstraint(name,IloOplLocation(&location));
    }

    virtual void notifyExecute(const char* name, const IloOplLocationI& location) {
        notifyExecute(name,IloOplLocation(&location));
    }

    virtual void notifyAssert(const char* name, const IloOplLocationI& location) {
        notifyAssert(name,IloOplLocation(&location));
    }

	virtual void notifyObjective(IloBool simple, IloBool minimize, const IloOplLocationI& location) {
        notifyObjective(simple, minimize, IloOplLocation(&location));
    }
    
	virtual IloBool isVerbose() const {
		return _verbose;
	}

public:	
    
    virtual ~IloOplOutlineListenerBaseI();

    
	virtual void notifyElementDefinition(IloOplElementDefinition def) =0;
    
	virtual void notifyClosePreProcessing() =0;

    
	virtual void notifyPushConstraint(const char* name, IloOplLocation location) =0;
    
	virtual void notifyPopConstraint() =0;

    
	virtual void notifyExecute(const char* name, IloOplLocation location) =0;

    
	virtual void notifyAssert(const char* name, IloOplLocation location) =0;
	
	    
	virtual void notifyObjective(IloBool simple, IloBool minimize, IloOplLocation location) =0;
};


class ILOOPL_EXPORTED IloOplOutlineListener {
	HANDLE_DECL_OPL(IloOplOutlineListener)
public:
    
    IloOplOutlineListener(IloOplOutlineListenerBaseI* impl):_impl(impl) {
    }
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif

