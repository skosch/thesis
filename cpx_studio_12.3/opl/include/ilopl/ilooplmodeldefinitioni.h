// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilooplmodeldefinitioni.h
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

#ifndef __OPL_ilooplmodeldefinitioniH
#define __OPL_ilooplmodeldefinitioniH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#include <ilconcert/iloenv.h>
#include <ilopl/ilooplexception.h>

#include <iostream>
using namespace std;

class IloOplErrorHandlerI;
class IloOplAst;
class IloOplModelSourceI;
class IloOplElementDefinitionI;
class IloOplElementDefinitionTable;
class IloOplElementDefinitionIteratorI;
class IloOplOutlineListenerI;
class IloOplSettingsI;
class IloOplLocationI;

class ILOOPL_EXPORTED IloOplModelDefinitionI: public IloRttiEnvObjectI {
    ILORTTIDECL
private:
    const IloOplModelSourceI* _modelSource;
	const char* _modelSourceName;
    IloOplSettingsI* _settings;
    IloOplErrorHandlerI* _handler; //TODO deprecated
    IloOplAst* _ownAst;
    const IloOplAst* _ast;
    IloBool _isChecked;
    IloBool _isOk;
	IloOplElementDefinitionTable* _table;

	IloOplOutlineListenerI* _outlineListener;

protected:
    IloOplModelDefinitionI(const IloOplModelDefinitionI& outer, const IloOplAst& templateAst);

public:
    IloOplModelDefinitionI(const IloOplModelSourceI& source, IloOplSettingsI& settings);
    IloOplModelDefinitionI(const IloOplModelSourceI& source, IloOplErrorHandlerI& handler);
    virtual ~IloOplModelDefinitionI();
    
    virtual const IloOplModelSourceI& getModelSource() const {
        return *_modelSource;
    }

    IloBool hasSettings() const {
        return _settings!=0;
    }
    IloOplSettingsI& getSettings() const {
        return *_settings;
    }

    IloOplErrorHandlerI& getErrorHandler() const {
        return *_handler;
    }

    virtual const char* getName() const;
    virtual IloBool read();

	IloOplElementDefinitionIteratorI* makeElementDefinitionIterator() const;
	const IloOplElementDefinitionI& getElementDefinition(const char* name) const;
	IloBool hasElementDefinition(const char* name) const;

	void addOutlineListener(IloOplOutlineListenerI& listener);
	void removeOutlineListener(IloOplOutlineListenerI& listener);

    enum AlgorithmType { ALGORITHM_NONE=0, ALGORITHM_DEFAULT, ALGORITHM_CPLEX, ALGORITHM_CP };
    AlgorithmType getAlgorithmType() const;
    const char* getAlgorithmName() const;

    IloBool hasMain() const;
    IloBool isNonLinear() const;

    enum ObjectiveType { OBJECTIVE_NONE=0, OBJECTIVE_SIMPLE, OBJECTIVE_OTHER };
    ObjectiveType getObjectiveType() const;
    IloBool hasObjective() const {
        return getObjectiveType()!=OBJECTIVE_NONE;
    }
    IloBool isSimpleObjective() const {
        return getObjectiveType()==OBJECTIVE_SIMPLE;
    }

    // convenience
    IloBool isUsingCplex() const;
    IloBool isUsingCP() const;
    void checkUsing(AlgorithmType expected) const;

    const IloOplLocationI& getObjectiveLocation() const;

    // more convenience
    static const char* GetAlgorithmName(AlgorithmType type);
    static IloBool IsEqual(AlgorithmType type, AlgorithmType type2);
    static IloBool IsCplex(AlgorithmType type);
    static IloBool IsCP(AlgorithmType type);

    virtual void parse();
    virtual void check();

    const IloOplAst& getAst() const;
	IloOplElementDefinitionTable& getElementDefinitionTable() const;
    
    DONT_COPY_OPL(IloOplModelDefinitionI)
};


class IloOplModelI;

class ILOOPL_EXPORTED IloOplTemplateDefinitionI: public IloOplModelDefinitionI {
    ILORTTIDECL
private:
    const IloOplModelI* _outerModel;
    const char* _name;

public:
	IloOplTemplateDefinitionI(const IloOplModelI& outer, const IloOplAst& templateAst, const char* name);
    
    virtual const char* getName() const;

    const IloOplModelI& getOuterOplModel() const {
        return *_outerModel;
    }

    virtual IloBool read() {
        return IloTrue;
    }

    virtual const IloOplModelSourceI& getModelSource() const;
    virtual void parse();
    virtual void check();
    
    DONT_COPY_OPL(IloOplTemplateDefinitionI)
};


class ILOOPL_EXPORTED IloOplInputSourceI: public IloDestroyableI {
    ILORTTIDECL
private:
    const char* _name;
    istream* _is;
    ifstream* _own;
    istream* _ownOther;

protected:
    virtual istream& openStream();
    virtual void closeStream();

public:
    IloOplInputSourceI(IloEnvI* env, const char* filename);
    IloOplInputSourceI(IloEnvI* env, istream& ins, const char* name);
    ~IloOplInputSourceI();

    void setStream(istream* ins);

    const char* getName() const;
    istream& getStream() const;
    void close() const;

    DONT_COPY_OPL(IloOplInputSourceI)
};

class ILOOPL_EXPORTED IloOplModelSourceI: public IloOplInputSourceI {
    ILORTTIDECL
	istream* _input;
    void* _aux;

	enum State {
		UNINITILIZED,
		COMPILED,
		NOT_COMPILED
	};

	State _state;

protected:
    virtual istream& openStream();
    virtual void closeStream();
public:
    IloOplModelSourceI(IloEnvI* env, const char* filename);
    IloOplModelSourceI(IloEnvI* env, istream& ins, const char* name);
    IloBool isCompiled() const;
};


class ILOOPL_EXPORTED IloOplCompilerI: public IloRttiEnvObjectI {
    ILORTTIDECL
public:
    IloOplCompilerI(IloEnvI* env);    
    void compile(const IloOplModelSourceI& input, ostream& outs);

    static void MarkCompiled(ostream& outs);
    static IloBool IsCompiled(istream& ins);
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif

