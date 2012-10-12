// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilooplprofileri.h
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

#ifndef __OPL_ilooplprofileriH
#define __OPL_ilooplprofileriH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#ifndef __OPL_ilosysH
#include <ilopl/ilosys.h>
#endif

#include <ilopl/ilooplexception.h>
#include <ilconcert/iloenv.h>

#include <iostream>
using namespace std;


class IloOplLocationI;
class IloCplexI;
class IloCPI;

class ILOOPL_EXPORTED IloOplProfilerI: public IloRttiEnvObjectI {
    ILORTTIDECL

    IloBool _ignoreUserSection;
    void* _attachedTo;
	void* _attachedToCP;

public:
    class InfoProvider {
    public:
        virtual ~InfoProvider() {
        }
        virtual IloNum getCurrentTime() =0;
        virtual void getCurrentMemory(IloInt& peak, IloInt& used) =0;
        virtual void pause() =0;
        virtual void resume() =0;

		virtual IloInt getTotalPausedMemory() const {
			return 0;
		}
    protected:
        InfoProvider() {
        }
    };

private:
    typedef IloArray<InfoProvider*> ProviderStack;
    ProviderStack _providerStack;
    IloInt _providerStackTop;

protected:
    explicit IloOplProfilerI(IloEnvI* env):IloRttiEnvObjectI(env), _ignoreUserSection(IloFalse), _providerStack(env,3), _providerStackTop(-1) {
        _attachedTo = 0;
        _attachedToCP = 0;
    }

    IloBool isIgnoreSection(int section) const {
        return _ignoreUserSection && section==SECTION_USER;
    }

    virtual IloBool isInSection(int section) const =0;

    void attachToAlgorithm(IloAlgorithmI& algorithm);
    void detachFromAlgorithm(IloAlgorithmI& algorithm);

public:
    enum {
       SECTION_READ_DEFINITION,
       SECTION_LOAD_MODEL,
       SECTION_LOAD_DATA,
       SECTION_PRE_PROCESSING,
       SECTION_ASSERT,
       SECTION_EXTRACT,
       SECTION_OBJECTIVE,
       SECTION_POST_PROCESSING,
       SECTION_PUBLISH_RESULTS,
       SECTION_FORCE_USAGE,
       SECTION_USER,
       SECTION_CPLEX,
       SECTION_CP,
       SECTION_ODM,
       SECTION_END,
       SECTION_OTHER,
       INIT,
       EXECUTE,
       ROOT
    };

    virtual ~IloOplProfilerI() {
        _providerStack.end();
    }

    void setIgnoreUserSection(IloBool ignore) {
        _ignoreUserSection = ignore;
    }
    IloBool isIgnoreUserSection() const {
        return _ignoreUserSection;
    }

    virtual void printReport(ostream& os) =0;
    
    virtual void enterSection(int section, const char* name) =0;
    virtual void reenterSection(int section, const char* name) =0;
    virtual void exitSection(int section) =0;

    virtual void enterInit(const char* nam) =0;
    virtual void exitInit(const char* name) =0;

    virtual void enterExecute(const char* name) =0;
    virtual void exitExecute(const char* name) =0;
    
    virtual void setLocation(const IloOplLocationI& location) =0;

    void enterSectionIfNeeded(int section, const char* name);
    void exitSectionIfNeeded(int section);

    void attachTo(IloCplexI& cplex);
    void detachFrom(IloCplexI& cplex);

    void attachTo(IloCPI& cp);
    void detachFrom(IloCPI& cp);

    IloBool isAttachedTo(void* other) {
      return _attachedTo && _attachedTo == other;
    }

    void pushProvider(InfoProvider& provider);
    void popProvider(InfoProvider& provider);
    InfoProvider& getProvider() const {
        return *_providerStack[_providerStackTop];
    }

	void pauseProvider() const;
    void resumeProvider() const;

    static IloInt GetCurrentProcessMemory();
    static IloNum GetCurrentProcessTime();
    static IloNum GetCurrentUserTime();

private:
    DONT_COPY_OPL(IloOplProfilerI)
};


class ILOOPL_EXPORTED IloOplDefaultProfilerI: public IloOplProfilerI {
    ILORTTIDECL

public:
    class Node: public IloEnvObjectI {
    protected:
        Node(IloEnvI* env):IloEnvObjectI(env) {
        }
        virtual ~Node() {
        }
    public:
        virtual int getSection() const =0;
        virtual const char* getName() const =0;

        virtual IloBool hasLocation() const =0;
        virtual const IloOplLocationI& getLocation() const =0;

        virtual IloNum getTime() const =0;
        virtual IloNum getSelfTime() const =0;
        virtual IloInt getPeakMemory() const =0;
        virtual IloInt getLocalMemory() const =0;
        virtual IloInt getCount() const =0;
        virtual IloInt getNodes() const =0;

        virtual IloBool isRoot() const =0;
        virtual const Node& getParent() const =0;
        virtual IloBool isLeaf() const =0;
        virtual const Node& getFirstChild() const =0;
        virtual IloBool isLast() const =0;
        virtual const Node& getNext() const =0;
    };

private:
    Node* _root;
    Node* _current;
    ostream* _traceStream;
    InfoProvider* _envProvider;

    void open();
    void close();

    void traceEnter(int section, const char* name) const;
    void traceExit(int section) const;

protected:
    virtual IloBool isInSection(int section) const;

public:
    explicit IloOplDefaultProfilerI(IloEnvI* env);
    IloOplDefaultProfilerI(IloEnvI* env, ostream& traceStream);
    virtual ~IloOplDefaultProfilerI();

    virtual void printReport(ostream& os);
    IloBool hasRoot() const;
    const Node& getRoot() const;

    virtual void enterSection(int section, const char* name);
    virtual void reenterSection(int section, const char* name);
    virtual void exitSection(int section);

    virtual void enterInit(const char* name);
    virtual void exitInit(const char* name);

    virtual void enterExecute(const char* name);
    virtual void exitExecute(const char* name);

    virtual void setLocation(const IloOplLocationI& location);
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif

