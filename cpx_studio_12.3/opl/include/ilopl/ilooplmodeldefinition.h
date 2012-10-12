// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilooplmodeldefinition.h
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

#ifndef __OPL_ilooplmodeldefinitionH
#define __OPL_ilooplmodeldefinitionH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#include <ilopl/ilooplerrorhandler.h>
#include <ilopl/ilooplelementdefinition.h>

#include <ilopl/ilooplmodeldefinitioni.h>


class ILOOPL_EXPORTED IloOplModelSource {
    HANDLE_DECL_OPL(IloOplModelSource)
public:
    
    ILO_OPL_DEPRECATED
    IloOplModelSource(IloEnv env, const char* filename, IloBool ) :_impl(0) {
        _impl = new (env) IloOplModelSourceI(env.getImpl(),filename);
    }

    
    ILO_OPL_DEPRECATED
    IloOplModelSource(IloEnv env, istream& ins, const char* name, IloBool ) :_impl(0) {
        _impl = new (env) IloOplModelSourceI(env.getImpl(),ins,name);
    }

    
    IloOplModelSource(IloEnv env, const char* filename) :_impl(0) {
        _impl = new (env) IloOplModelSourceI(env.getImpl(),filename);
    }

    
    IloOplModelSource(IloEnv env, istream& ins, const char* name) :_impl(0) {
        _impl = new (env) IloOplModelSourceI(env.getImpl(),ins,name);
    }

    
    const char* getName() const {
        return impl().getName();
    }

    
    istream& getStream() const {
        return impl().getStream();
    }

    
    void close() const {
        impl().close();
    }

    
    IloBool isCompiled() const {
        return impl().isCompiled();
    }
};



class ILOOPL_EXPORTED IloOplModelDefinition {
    HANDLE_DECL_OPL(IloOplModelDefinition)
public:
    
    ILO_OPL_DEPRECATED
    IloOplModelDefinition(IloOplModelSource source, IloOplErrorHandler handler):_impl(0) {
        _impl = new (source.getEnv()) IloOplModelDefinitionI(source.impl(),handler.impl());
    }

    
    IloOplModelDefinition(IloOplModelSource source, IloOplSettings settings):_impl(0) {
        _impl = new (source.getEnv()) IloOplModelDefinitionI(source.impl(),settings.impl());
    }

    
    IloOplModelSource getModelSource() const {
        return &impl().getModelSource();
    }

    
    const char* getName() const {
        return impl().getName();
    }

    
    IloBool read() {
        return impl().read();
    }

    
    IloBool hasMain() const {
        return impl().hasMain();
    }

    
    IloBool isNonLinear() const {
        return impl().isNonLinear();
    }

    
    IloBool hasObjective() const {
        return impl().hasObjective();
    }

    
    IloBool isSimpleObjective() const {
        return impl().isSimpleObjective();
    }

    
    IloOplLocation getObjectiveLocation() const {
        return &impl().getObjectiveLocation();
    }

    
    typedef IloOplModelDefinitionI::AlgorithmType AlgorithmType;
    //enum AlgorithmType { ALGORITHM_DEFAULT=0, ALGORITHM_CPLEX, ALGORITHM_CP };

    
    AlgorithmType getAlgorithmType() const {
        return impl().getAlgorithmType();
    }

    
    const char* getAlgorithmName() const {
        return impl().getAlgorithmName();
    }

    
	IloOplElementDefinitionIterator makeElementDefinitionIterator() const {
		return impl().makeElementDefinitionIterator();
	}

    
	IloOplElementDefinition getElementDefinition(const char* name) const {
		return &impl().getElementDefinition(name);
	}

    
	IloBool hasElementDefinition(const char* name) const {
		return impl().hasElementDefinition(name);
	}

    
    IloBool isUsingCplex() const {
        return impl().isUsingCplex();
    }

    
    IloBool isUsingCP() const {
        return impl().isUsingCP();
    }
};



class ILOOPL_EXPORTED IloOplCompiler {
    HANDLE_DECL_OPL(IloOplCompiler)
public:
    
    IloOplCompiler(IloEnv env):_impl(0) {
        _impl = new (env) IloOplCompilerI(env.getImpl());
    }

    
    void compile(IloOplModelSource source, ostream& outs) {
        impl().compile(source.impl(),outs);
    }
};

#ifdef _WIN32
#pragma pack(pop)
#endif


#endif
