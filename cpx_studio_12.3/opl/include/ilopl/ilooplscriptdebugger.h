// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilooplscriptdebugger.h
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

#ifndef __OPL_ilooplscriptdebuggerH
#define __OPL_ilooplscriptdebuggerH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#include <ilopl/ilosys.h>
#include <ilopl/ilooplexception.h>
#include <ilconcert/iloenv.h>

#include <iostream>
using namespace std;

class IloOplScripting;
class IloOplModel;
class IloOplLocation;
class IloOplScriptDebugIde;


class ILOOPL_EXPORTED IloOplScriptDebuggerBaseI: public IloDestroyableI {
    ILORTTIDECL
private:
    IloOplScriptDebugIde* _ide;
    ostream* _os;

    void init(const IloOplModel& opl);

protected:
    
    IloOplScriptDebuggerBaseI(IloEnv env, const IloOplModel& opl);

public:
    
    virtual ~IloOplScriptDebuggerBaseI();

    
    ostream& getOut() const {
        return *_os;
    }

    
    void setOut(ostream& outs) {
        _os = &outs;
    }

    
    virtual void handleInterruption() =0;

    
    virtual void handleScriptResult(const char* value) =0;
    
    IloInt getCurrentBreakpointId() const;

    
    void registerPause();
    
    void unregisterPause();
    
    void registerBreakpoint(IloInt id, const IloOplLocation& loc);
    
    void unregisterBreakpoint(IloInt id, const IloOplLocation& loc);
    
    void registerStepOver();
    
    void registerStepIn();
    
    void registerStepOut();
    
    void registerStepToCursor(const IloOplLocation& loc);

    
    void registerScript(const char* code);
    
    void keepAlive();

    
    const char* evalScript(const char* code);
};

typedef IloOplScriptDebuggerBaseI IloOplScriptDebuggerI;


class IloStringArray;


class ILOOPL_EXPORTED IloOplScriptDebugger {
	HANDLE_DECL_OPL(IloOplScriptDebugger)

public:
    //%typemap("javapackage") IloStringArray, IloStringArray *, IloStringArray &, const IloStringArray & "ilog.concert.cppimpl";

    
    static void GetOplKeywordsModel(IloStringArray kw);
    
    static void GetOplKeywordsModelCP(IloStringArray kw);
    
    static void GetOplKeywordsData(IloStringArray kw);

    
    static void GetOplKeywordsScript(IloStringArray kw);

    
    static void GetOplFunctionNames(IloStringArray fn);
    
    static void GetOplFunctionNamesCP(IloStringArray fn);

	
    static void GetOplScriptScopes(IloStringArray scopes);
    
    static void GetOplScriptScopeInfos(const char* scope, IloStringArray infos);
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
