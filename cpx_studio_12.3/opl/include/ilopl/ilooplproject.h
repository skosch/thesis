// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilooplproject.h
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

#ifndef __OPL_ilooplprojectH
#define __OPL_ilooplprojectH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#include <ilopl/ilooplprojecti.h>


class ILOOPL_EXPORTED IloOplRunConfiguration {
    HANDLE_DECL_OPL(IloOplRunConfiguration)
public:
    
    IloOplRunConfiguration(IloEnv env, const char* modPath):_impl(0) {
        _impl = new (env.getImpl()) IloOplRunConfigurationI(env.getImpl(),modPath);
    }

    
    IloOplRunConfiguration(IloEnv env, const char* modPath, const char* datPath):_impl(0) {
        _impl = new (env.getImpl()) IloOplRunConfigurationI(env.getImpl(),modPath,datPath);
    }

    
    IloOplRunConfiguration(IloEnv env, const char* modPath, IloStringArray datPaths):_impl(0) {
        _impl = new (env.getImpl()) IloOplRunConfigurationI(env.getImpl(),modPath,datPaths);
    }

    
    IloOplRunConfiguration(IloOplModelDefinition def):_impl(0) {
        _impl = new (def.getEnv().getImpl()) IloOplRunConfigurationI(def.impl());
    }

    
    IloOplRunConfiguration(IloOplModelDefinition def, IloOplDataElements dataElements):_impl(0) {
        _impl = new (def.getEnv().getImpl()) IloOplRunConfigurationI(def.impl(),static_cast<IloOplDataElementsI&>(dataElements.impl()));
    }

    
    ILO_OPL_DEPRECATED
    void endAll() {
        end();
    }

    
    ILO_OPL_DEPRECATED
    void setCompiledModel(IloBool ) {
    }

    
    IloBool isCompiledModel() const {
        return impl().isCompiledModel();
    }

    
    void setErrorHandler(IloOplErrorHandler handler) {
        impl().setErrorHandler(handler.impl());
    }

    
    void setSettings(IloOplSettings settings) {
        impl().setSettings(settings.impl());
    }

    
    void setCplex(IloCplex cplex) {
        impl().setCplex(*cplex.getImpl());
    }

    
    void setOwnCplex(IloBool own) {
        impl().setOwnCplex(own);
    }

    
    IloOplErrorHandler getErrorHandler() {
        return &impl().getErrorHandler();
    }

    
    IloOplSettings getSettings() {
        return &impl().getSettings();
    }

    
    IloCplex getCplex() {
        return &impl().getCplex();
    }

    
    IloOplModel getOplModel() {
        return &impl().getOplModel();
    }

    
    void setCP(IloCP cp) {
        impl().setCP(*cp.getImpl());
    }
    
    void setOwnCP(IloBool own) {
        impl().setOwnCP(own);
    }
    
    IloCP getCP() {
        return &impl().getCP();
    }

    
	IloBool isOplModelAvailable() const {
		return impl().isOplModelAvailable();
	}

};



class ILOOPL_EXPORTED IloOplProject {
    HANDLE_DECL_OPL(IloOplProject)
public:
    
    IloOplProject(IloEnv env, const char* prjPath):_impl(0) {
        _impl = new (env.getImpl()) IloOplProjectI(env.getImpl(),prjPath);
    }

    
    IloOplProject(IloEnv env, istream* ins, const char* name):_impl(0) {
        _impl = new (env.getImpl()) IloOplProjectI(env.getImpl(),ins,name);
    }

    
    IloOplRunConfiguration makeRunConfiguration() {
        return impl().makeRunConfiguration();
    }

    
    IloStringArray makeRunConfigurationNames() const {
        return impl().makeRunConfigurationNames();
    }

    
    IloBool tuneParam(IloCplex::ParameterSet fixedSet, IloCplex::ParameterSet resultSet, IloStringArray runConfigNames) {
        ASSERT_IMPL_OBJ(fixedSet.getImpl());
        ASSERT_IMPL_OBJ(resultSet.getImpl());
        return impl().tuneParam(*fixedSet.getImpl(), *resultSet.getImpl(), runConfigNames, 0);
    }

    
    IloBool tuneParam(IloCplex::ParameterSet fixedSet, IloCplex::ParameterSet resultSet, IloStringArray runConfigNames, IloOplSettings settings) {
        ASSERT_IMPL_OBJ(fixedSet.getImpl());
        ASSERT_IMPL_OBJ(resultSet.getImpl());
        return impl().tuneParam(*fixedSet.getImpl(), *resultSet.getImpl(), runConfigNames, settings.getImpl());
    }

    
    IloBool tuneParam(IloCplex::ParameterSet fixedSet, IloCplex::ParameterSet resultSet, IloStringArray runConfigNames, IloOplSettings settings, IloOplTuningCallback cb) {
        ASSERT_IMPL_OBJ(fixedSet.getImpl());
        ASSERT_IMPL_OBJ(resultSet.getImpl());
        return impl().tuneParam(*fixedSet.getImpl(), *resultSet.getImpl(), runConfigNames, settings.getImpl(), cb.getImpl());
    }

    
    IloOplRunConfiguration makeRunConfiguration(const char* name) {
        return impl().makeRunConfiguration(name);
    }

    
    static void ApplyProjectSettings(IloStringArray opsPaths, IloOplModel model) {
        IloOplProjectI::ApplyProjectSettings(opsPaths,model.impl(),model.getSourceName());
    }

    
    static void ApplyProjectSettings(istream& opsStream, const char* name, IloOplModel model) {
        IloOplProjectI::ApplyProjectSettings(opsStream,name,model.impl());
    }

    
    static void ReadProjectSettings(istream& opsStream, const char* name, IloCplex cplex, IloCplex::ParameterSet parameterSet, IloOplSettings settings) {
        IloOplProjectI::ReadProjectSettings(opsStream,name,*cplex.getImpl(),parameterSet,settings.impl());
    }
    
    static void WriteProjectSettings(IloCplex cplex, IloCplex::ParameterSet parameterSet, ostream& opsStream, const char* name) {
        IloOplProjectI::WriteProjectSettings(*cplex.getImpl(),parameterSet,opsStream,name);
    }
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
