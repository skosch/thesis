// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilooplelementdefinitioni.h
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

#ifndef __OPL_ilooplelementdefinitioniH
#define __OPL_ilooplelementdefinitioniH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#ifndef __OPL_ilosysH
#include <ilopl/ilosys.h>
#endif

class IloOplLocationI;
class IloOplModelDefinitionI;
class IloOplAst;

class IloOplArrayDefinitionI;
class IloOplSetDefinitionI;
class IloOplRangeDefinitionI;
class IloOplTupleDefinitionI;
class IloOplTupleSchemaDefinitionI;
class IloOplTemplateElementDefinitionI;
class IloOplConstraintDefinitionI;
class IloOplDecisionExprDefinitionI;

class IloOplElementDefinitionTable;


class ILOOPL_EXPORTED IloOplElementDefinitionI: public IloRttiEnvObjectI {
    ILORTTIDECL

public:
	typedef IloOplElementDefinitionType::Type Type;
	typedef IloArray<const IloOplElementDefinitionI*> ElementDefinitionArray;
	typedef IloStringHashTable<const IloOplElementDefinitionI*> ElementDefinitionMap;

private:
	const char* _name;
	Type _defType;
	const IloOplLocationI* _location;
	IloBool _isExternalData;
	IloBool _isPost;
	IloBool _isInternalData;
    void* _internalType;

protected:
	IloOplElementDefinitionI(IloEnvI* env, const char* name, Type defType, const IloOplLocationI& location, IloBool isExternalData, IloBool isPost)
		:IloRttiEnvObjectI(env), _name(name), _defType(defType), _location(&location), _isExternalData(isExternalData), _isPost(isPost), _isInternalData(!isExternalData), _internalType(0) {
    }

	void setInternalData(IloBool isInternalData) {
		_isInternalData = isInternalData;
	}

public:
	const char* getName() const {
		return _name;
	}

    void setInternalType(void* type) {
        _internalType = type;
    }
    void displayType(ostream& outs) const;

	IloOplElementDefinitionType::Type getElementDefinitionType() const {
		return _defType;
	}

	const IloOplLocationI& getLocation() const {
		return *_location;
	}

	const IloOplArrayDefinitionI& asArray() const;
	const IloOplSetDefinitionI& asSet() const;
	const IloOplRangeDefinitionI& asRange() const;
	const IloOplTupleDefinitionI& asTuple() const;
	const IloOplTupleSchemaDefinitionI& asTupleSchema() const;
	const IloOplTemplateElementDefinitionI& asTemplate() const;
	const IloOplConstraintDefinitionI& asConstraint() const;
    const IloOplDecisionExprDefinitionI& asDecisionExpression() const;

	virtual IloBool isDecisionVariable() const {
		return IloFalse;
	}
	virtual IloBool isDecisionExpression() const {
        //return isDecisionVariable() && isCalculated();
        return IloFalse;
	}
	IloBool isExternalData() const {
		return _isExternalData;
	}
	IloBool isInternalData() const {
		return _isInternalData;
	}
	IloBool isPostProcessing() const {
		return _isPost;		
	}

	virtual const IloOplElementDefinitionI& getLeaf() const {
		return *this;
	}

private:
	DONT_COPY_OPL(IloOplElementDefinitionI)
};

class ILOOPL_EXPORTED IloOplSimpleDefinitionI: public IloOplElementDefinitionI {
    ILORTTIDECL

	IloBool _isDvar;

public:
	IloOplSimpleDefinitionI(IloEnvI* env, const char* name, Type defType, IloBool isDvar, const IloOplLocationI& location, IloBool isExternalData, IloBool isPost)
		:IloOplElementDefinitionI(env,name,defType,location,isExternalData,isPost), _isDvar(isDvar) {
			setInternalData( !isExternalData && !isDvar);
    }
	virtual ~IloOplSimpleDefinitionI(){
	}

	virtual IloBool isDecisionVariable() const {
		return _isDvar;
	}
};

class ILOOPL_EXPORTED IloOplArrayDefinitionI: public IloOplElementDefinitionI {
	ILORTTIDECL

public:
	typedef ElementDefinitionArray IndexerArray;
	typedef ElementDefinitionMap References;
	
private:
	ElementDefinitionArray _indexers;
	const IloOplElementDefinitionI* _itemDef;
	References* _references;
	IloBool _ownItemDef;
    IloBoolArray _ownIndexerDef;

public:
	IloOplArrayDefinitionI(IloEnvI* env, const char* name, const IndexerArray& indexers, const IloOplElementDefinitionI& itemDef, const IloOplLocationI& location, IloBool isExternalData, IloBool isPost)
		:IloOplElementDefinitionI(env,name,IloOplElementDefinitionType::ARRAY,location,isExternalData,isPost), _indexers(indexers), _itemDef(&itemDef), _references(0), _ownItemDef(IloFalse), _ownIndexerDef(0) {
			setInternalData( !isExternalData 
				&& !itemDef.isDecisionVariable() 
				&& itemDef.getElementDefinitionType()!=IloOplElementDefinitionType::CONSTRAINT );
	}
	IloOplArrayDefinitionI(IloEnvI* env, const char* name, const IndexerArray& indexers, IloOplElementDefinitionI* itemDef, const IloOplLocationI& location, IloBool isExternalData, IloBool isPost)
		:IloOplElementDefinitionI(env,name,IloOplElementDefinitionType::ARRAY,location,isExternalData,isPost), _indexers(indexers), _itemDef(itemDef), _references(0), _ownItemDef(IloTrue), _ownIndexerDef(0) {
			setInternalData( !isExternalData 
				&& !itemDef->isDecisionVariable() 
				&& itemDef->getElementDefinitionType()!=IloOplElementDefinitionType::CONSTRAINT );
	}
	virtual ~IloOplArrayDefinitionI();

	IloInt getDimensions() const {
		return _indexers.getSize();
	}
	const IloOplElementDefinitionI& getIndexer(IloInt i) const {
		return *_indexers[i];
	}
	const IloOplElementDefinitionI& getItem() const {
		return *_itemDef;
	}

    void replaceItem(const IloOplElementDefinitionI* itemDef);
    void replaceIndexer(IloInt i, const IloOplElementDefinitionI* indexerDef);

	virtual IloBool isDecisionVariable() const {
		return getItem().isDecisionVariable();
	}

	virtual IloBool isDecisionExpression() const {
		return getItem().isDecisionExpression();
	}

    virtual const IloOplElementDefinitionI& getLeaf() const {
		return getItem().getLeaf();
	}

	void addReference(const char* name, const IloOplElementDefinitionI& reference);
	IloBool hasReferences() const;
	IloBool isReference(const IloOplElementDefinitionI& component) const;
	const IloOplElementDefinitionI& getReference(const IloOplElementDefinitionI& component) const;
};

class ILOOPL_EXPORTED IloOplSetDefinitionI: public IloOplElementDefinitionI {
	ILORTTIDECL

	const IloOplElementDefinitionI* _itemDef;
public:
	typedef ElementDefinitionMap References;
    enum SortSense { NONE=-1, ORDERED, SORTED, REVERSED };
	
private:
	References* _references;
    SortSense _sense;

public:
	IloOplSetDefinitionI(IloEnvI* env, const char* name, SortSense sense, const IloOplElementDefinitionI& itemDef, const IloOplLocationI& location, IloBool isData, IloBool isPost)
	:IloOplElementDefinitionI(env,name,IloOplElementDefinitionType::SET,location,isData,isPost), _itemDef(&itemDef), _references(0), _sense(sense) {
	}
	~IloOplSetDefinitionI() {
		delete _references;
	}

	const IloOplElementDefinitionI& getItem() const {
		return *_itemDef;
	}

	virtual const IloOplElementDefinitionI& getLeaf() const {
		return getItem().getLeaf();
	}

	void addReference(const char* name, const IloOplElementDefinitionI& reference);
	IloBool hasReferences() const;
	IloBool isReference(const IloOplElementDefinitionI& component) const;
	const IloOplElementDefinitionI& getReference(const IloOplElementDefinitionI& component) const;

    IloBool isOrdered() const {
        return _sense==ORDERED;
    }
    IloBool isSorted() const {
        return _sense==SORTED;
    }
    IloBool isReversed() const {
        return _sense==REVERSED;
    }
};

class ILOOPL_EXPORTED IloOplRangeDefinitionI: public IloOplElementDefinitionI {
	ILORTTIDECL

	const IloOplElementDefinitionI* _target;

    const char* _simpleLB;
    const IloOplElementDefinitionI* _simpleLBel;
    const char* _simpleUB;
    const IloOplElementDefinitionI* _simpleUBel;

private:
    friend class SimpleRangeFinder;
    void setSimpleRange(const char* lb, const IloOplElementDefinitionI* lbel, const char* ub, const IloOplElementDefinitionI* ubel);

public:
	IloOplRangeDefinitionI(IloEnvI* env, const char* name, const IloOplElementDefinitionI& target, const IloOplLocationI& location, IloBool isPost)
	:IloOplElementDefinitionI(env,name,IloOplElementDefinitionType::RANGE,location,IloFalse,isPost), _target(&target), _simpleLB(0), _simpleLBel(0), _simpleUB(0), _simpleUBel(0) {
		setInternalData( IloTrue );
	}
	virtual ~IloOplRangeDefinitionI(){
	}
	
	IloBool isFloat() const {
		return _target->getElementDefinitionType()==IloOplElementDefinitionType::FLOAT;
	}

    IloBool isSimpleRange() const {
        return _simpleLB!=0 || _simpleUB!=0 || _simpleLBel!=0 || _simpleUBel!=0;
	}
    IloBool isSimpleLB() const {
        return _simpleLB!=0;
	}
    IloBool isSimpleLBElement() const {
        return _simpleLBel!=0;
	}
    IloBool isSimpleUB() const {
        return _simpleUB!=0;
	}
    IloBool isSimpleUBElement() const {
        return _simpleUBel!=0;
	}

    const char* getSimpleLB() const;
	const char* getSimpleUB() const;
    const IloOplElementDefinitionI& getSimpleLBElement() const;
	const IloOplElementDefinitionI& getSimpleUBElement() const;

	virtual const IloOplElementDefinitionI& getLeaf() const {
		return *_target;
	}
};

class ILOOPL_EXPORTED IloOplTupleDefinitionI: public IloOplElementDefinitionI {
	ILORTTIDECL

	const IloOplTupleSchemaDefinitionI* _schemaDef;

public:
	IloOplTupleDefinitionI(IloEnvI* env, const char* name, const IloOplTupleSchemaDefinitionI& schemaDef, const IloOplLocationI& location, IloBool isData, IloBool isPost)
	:IloOplElementDefinitionI(env,name,IloOplElementDefinitionType::TUPLE,location,isData,isPost), _schemaDef(&schemaDef) {
    }

	const IloOplTupleSchemaDefinitionI& getTupleSchema() const {
		return *_schemaDef;
	}
};

class ILOOPL_EXPORTED IloOplTupleSchemaDefinitionI: public IloOplElementDefinitionI {
	ILORTTIDECL
public:
	typedef ElementDefinitionArray ComponentArray;

private:
	ComponentArray _components;
	const IloOplTupleSchemaDefinitionI* _key;

public:
	IloOplTupleSchemaDefinitionI(IloEnvI* env, const char* name, const ComponentArray& components, const IloOplLocationI& location, IloBool isPost)
	:IloOplElementDefinitionI(env,name,IloOplElementDefinitionType::TUPLE_SCHEMA,location,IloFalse,isPost), _components(components), _key(0) {
		setInternalData( IloFalse );
	}
	virtual ~IloOplTupleSchemaDefinitionI() {
		_components.end();
	}
	
	void setKeyTupleSchema(const IloOplTupleSchemaDefinitionI& key) {
		_key = &key;
	}

	ComponentArray getComponentArray() const {
		return _components;
	}

	IloBool isKey(const IloOplElementDefinitionI& component) const;

	IloBool hasKey() const {
		return _key!=0;
	}

	const IloOplTupleSchemaDefinitionI& getKeyTupleSchema() const {
		return _key ? *_key : *this;
	}
};

class ILOOPL_EXPORTED IloOplTemplateElementDefinitionI: public IloOplElementDefinitionI {
	ILORTTIDECL

	const IloOplAst* _templateAst;

public:
    IloOplTemplateElementDefinitionI(IloEnvI* env, const char* name, const IloOplLocationI& location, const IloOplAst* templateAst)
	:IloOplElementDefinitionI(env,name,IloOplElementDefinitionType::TEMPLATE,location,IloFalse,IloFalse), _templateAst(templateAst) {
		setInternalData( IloFalse );
	}
	virtual ~IloOplTemplateElementDefinitionI();

	const IloOplAst& getTemplateAst() const {
		return *_templateAst;
	}
};

class ILOOPL_EXPORTED IloOplConstraintDefinitionI: public IloOplElementDefinitionI {
	ILORTTIDECL

public:
	typedef ElementDefinitionArray TypeArray;
	typedef ElementDefinitionArray CollectionArray;

    enum Kind { KIND_UNKNOWN=-1, KIND_OTHER, KIND_RANGE, KIND_BINARY, KIND_FORALL, KIND_LOGICAL };

private:
	IloStringArray _indexNames;
	TypeArray _indexTypes;
	CollectionArray _indexCollections;    
    Kind _kind;

public:
	IloOplConstraintDefinitionI(IloEnvI* env, const char* name, const IloOplLocationI& location)
		:IloOplElementDefinitionI(env,name,IloOplElementDefinitionType::CONSTRAINT,location,IloFalse,IloFalse), _kind(KIND_UNKNOWN) {
			setInternalData(IloFalse);
	}
	virtual ~IloOplConstraintDefinitionI();

	void setIndexInfo(IloStringArray indexNames, TypeArray indexTypes, CollectionArray indexCollections);

    IloBool hasKind() const {
        return _kind != KIND_UNKNOWN;
    }

    void setKind(Kind kind) {
        _kind = kind;
    }

    IloBool hasIndexInfo() const {
        return _indexNames.getImpl()!=0;
    }

	IloInt getIndexCount() const {
		return _indexNames.getImpl()!=0 ? _indexNames.getSize() : 0;
	}

	const char* getIndexName(int i) const {
		return _indexNames[i];
	}

	const IloOplElementDefinitionI& getIndexType(int i) const {
		IloTestAndRaise( _indexTypes[i], "no index type available" );
		return *_indexTypes[i];
	}

	IloBool hasIndexCollection(int i) const {
		return _indexCollections[i]!=0;
	}

	const IloOplElementDefinitionI& getIndexCollection(int i) const {
		IloTestAndRaise( hasIndexCollection(i), "no index collection available" );
		return *_indexCollections[i];
	}

    IloBool hasMid() const {
        return _kind == KIND_RANGE;
    }

    IloBool hasBounds() const {
        return _kind == KIND_BINARY || _kind == KIND_RANGE;
    }

    IloBool isForall() const {
        return _kind == KIND_FORALL;
    }

    IloBool isLogical() const {
        return _kind == KIND_LOGICAL;
    }
};

class ILOOPL_EXPORTED IloOplDecisionExprDefinitionI: public IloOplElementDefinitionI {
	ILORTTIDECL

public:
    typedef ElementDefinitionArray TypeArray;
	typedef ElementDefinitionArray CollectionArray;
	typedef ElementDefinitionArray UsedArray;

private:
    IloBool _usedInObjective;
    IloInt _simpleGoal;
    const char* _simpleGoalCoef;
    IloBool _simpleGoalIsMinimize;

    IloStringArray _expandIndexNames;
	TypeArray _expandIndexTypes;
	CollectionArray _expandIndexCollections;
	UsedArray _used;

    IloBool _isNonLinear;

private:
    friend class IloOplTypeChecker;
    void setUsedInObjective(IloBool used) {
		_usedInObjective = used;
	}
    void setNonLinear(IloBool nonLinear) {
		_isNonLinear = nonLinear;
	}

private:
    friend class SimpleGoalFinder;
    void setSimpleGoal(const char* coef, IloBool minimize);
	IloInt getSimpleGoalUse() const {
		return _simpleGoal;
	}
	void addSimpleGoalUse() {
		_simpleGoal++;
	}

private:
    friend class IloOplElementDefinitionTable;
    void setExpandIndexInfo(IloStringArray indexNames, TypeArray indexTypes, CollectionArray indexCollections);
    void setUsed(UsedArray used);

public:
	IloOplDecisionExprDefinitionI(IloEnvI* env, const char* name, Type defType, const IloOplLocationI& location, IloBool isPost)
        :IloOplElementDefinitionI(env,name,defType,location,IloFalse,isPost), _usedInObjective(IloFalse), _simpleGoal(0), _simpleGoalCoef(0), _simpleGoalIsMinimize(IloFalse), _isNonLinear(IloFalse) {
			setInternalData(IloTrue);
	}
	virtual ~IloOplDecisionExprDefinitionI();

    virtual IloBool isDecisionExpression() const {
		return IloTrue;
	}

    IloBool isUsedInObjective() const {
		return _usedInObjective;
	}

    IloBool isSimpleGoal() const {
        return _simpleGoal==1;
	}
	const char* getSimpleGoalCoef() const {
		//ASSERT_DEBUG_YOPL( _simpleGoal==1 );
        return _simpleGoalCoef;
	}
	IloBool isSimpleGoalMinimize() const {
		//ASSERT_DEBUG_YOPL( _simpleGoal==1 );
        return _simpleGoalIsMinimize;
	}

	IloInt getExpandIndexCount() const {
		return _expandIndexNames.getImpl()!=0 ? _expandIndexNames.getSize() : 0;
	}
	const char* getExpandIndexName(int i) const {
		return _expandIndexNames[i];
	}
	const IloOplElementDefinitionI& getExpandIndexType(int i) const {
		IloTestAndRaise( _expandIndexTypes[i], "no expand index type available" );
		return *_expandIndexTypes[i];
	}
	IloBool hasExpandIndexCollection(int i) const {
		return _expandIndexCollections[i]!=0;
	}
	const IloOplElementDefinitionI& getExpandIndexCollection(int i) const {
		IloTestAndRaise( hasExpandIndexCollection(i), "no expand index collection available" );
		return *_expandIndexCollections[i];
	}

	IloInt getUsedDecisionExprCount() const {
		return _used.getImpl()!=0 ? _used.getSize() : 0;
	}
	const IloOplElementDefinitionI& getUsedDecisionExpr(int i) const {
		return *_used[i];
	}

    IloBool isNonLinear() const {
        return _isNonLinear;
    }
};

class ILOOPL_EXPORTED IloOplElementDefinitionIteratorI: public IloRttiEnvObjectI {
	ILORTTIDECL

	const IloOplElementDefinitionTable* _table;
	IloInt _size;
	IloInt _current;

public:
	explicit IloOplElementDefinitionIteratorI(const IloOplElementDefinitionTable& table);

	IloBool ok() const;
    void operator++();
    const IloOplElementDefinitionI& operator*() const;
};

class ILOOPL_EXPORTED IloOplConstraintOutlineI: public IloRttiEnvObjectI {
	ILORTTIDECL

	const IloOplModelDefinitionI* _modelDef;

public:
	explicit IloOplConstraintOutlineI(const IloOplModelDefinitionI& modelDef);

	void generateOutline(IloBool verbose =IloFalse);

	virtual void pushNode(const char* name, const IloOplLocationI& location) =0;
	virtual void popNode() =0;
};


class ILOOPL_EXPORTED IloOplOutlineListenerI: public IloRttiEnvObjectI {
	ILORTTIDECL

protected:
	IloOplOutlineListenerI(IloEnvI* env):IloRttiEnvObjectI(env) {
	}

public:	
	virtual IloBool isVerbose() const {
		return IloFalse;
	}

	virtual void notifyElementDefinition(const IloOplElementDefinitionI& def) =0;
	virtual void notifyClosePreProcessing() =0;

	virtual void notifyPushConstraint(const char* name, const IloOplLocationI& location) =0;
	virtual void notifyPopConstraint() =0;

	virtual void notifyExecute(const char* name, const IloOplLocationI& location) =0;
	virtual void notifyAssert(const char* name, const IloOplLocationI& location) =0;
	virtual void notifyObjective(IloBool simple, IloBool minimize, const IloOplLocationI& location) =0;
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
