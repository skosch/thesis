// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilooplprofiler.h
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

#ifndef __OPL_ilooplprofilerH
#define __OPL_ilooplprofilerH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#include <ilopl/ilooplprofileri.h>



class ILOOPL_EXPORTED IloOplProfiler {
    HANDLE_DECL_OPL(IloOplProfiler)
public:
    
    IloOplProfiler(IloEnv env):_impl(0) {
        _impl = new (env) IloOplDefaultProfilerI(env.getImpl());
    }

    
    IloOplProfiler(IloEnv env, ostream& traceStream):_impl(0) {
        _impl = new (env) IloOplDefaultProfilerI(env.getImpl(),traceStream);
    }

    
    void setIgnoreUserSection(IloBool ignore) {
        impl().setIgnoreUserSection(ignore);
    }

    
    void printReport(ostream& os) {
        impl().printReport(os);
    }

    
    void enterSectionOther(const char* name) {
        impl().enterSection(IloOplProfilerI::SECTION_OTHER,name);
    }

    
    void reenterSectionOther(const char* name) {
        impl().reenterSection(IloOplProfilerI::SECTION_OTHER,name);
    }

    
    void exitSectionOther() {
        impl().exitSection(IloOplProfilerI::SECTION_OTHER);
    }

    
    void enterSectionODM(const char* name) {
        impl().enterSection(IloOplProfilerI::SECTION_ODM,name);
    }

    
    void reenterSectionODM(const char* name) {
        impl().reenterSection(IloOplProfilerI::SECTION_ODM,name);
    }

    
    void exitSectionODM() {
        impl().exitSection(IloOplProfilerI::SECTION_ODM);
    }
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
