// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilooplexception.h
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

#ifndef __OPL_ilooplexceptionH
#define __OPL_ilooplexceptionH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#ifndef __OPL_ilosysH
#include <ilopl/ilosys.h>
#endif

class IloOplLocation;

#include <ilopl/ilooplmessagecatalog.h>


class ILOOPL_EXPORTED IloOplMessage {
public:
    virtual ~IloOplMessage() {
    }

    
    virtual const char* getLocalized() const =0;

    
    virtual IloOplMessageCatalog::MessageId getMessageCatalogId() const =0;
};



class ILOOPL_EXPORTED IloOplException: public IloException {
public:
    virtual ~IloOplException() {
    }
    
    virtual const char* getMessage() const;

    
    virtual const IloOplMessage& getOplMessage() const =0;

    
    virtual IloOplLocation getLocation() const;
};



class ILOOPL_EXPORTED IloOplAbort: public IloOplException {
public:
    IloOplAbort();
    const IloOplMessage& getOplMessage() const;
};



class ILOOPL_EXPORTED IloOplImplMissing: public IloOplException {
    IloOplMessage* _oplMessage;
public:
    IloOplImplMissing(const char* file, int line);
    IloOplImplMissing(const IloOplImplMissing& other);
    virtual ~IloOplImplMissing();
    const char* getMessage() const;
    const IloOplMessage& getOplMessage() const;
};


// ---
// macros for handles
#define ASSERT_IMPL_OBJ(impl) \
    if ( impl==0 ) { throw IloOplImplMissing(__FILE__,__LINE__); }

#define ASSERT_IMPL \
    ASSERT_IMPL_OBJ(_impl)

#define HANDLE_DECL_BASE_WITHOUTEND_OPL(name,nameImpl,ctorImpl) \
    public: \
        typedef nameImpl ImplClass; \
        name() :ctorImpl() { _impl=0; } \
        name(ImplClass* impl) :ctorImpl(impl) {} \
        name(const ImplClass* impl) :ctorImpl(const_cast<ImplClass*>(impl)) {} \
		 \
        IloEnv getEnv() const { return impl().getEnv(); } \
        ImplClass* getImpl() const { return (ImplClass*)_impl; } \
        ImplClass& impl() { ASSERT_IMPL; return *getImpl(); } \
        const ImplClass& impl() const { ASSERT_IMPL; return *getImpl(); } \
		 \
        void setImpl(ImplClass* impl) { _impl=impl; }

#define HANDLE_DECL_BASE_OPL(name,nameImpl,ctorImpl) \
    HANDLE_DECL_BASE_WITHOUTEND_OPL(name,name##I,ctorImpl) \
		 \
        void end() { delete _impl; _impl=0; }

#define HANDLE_DECL_ROOT_OPL(name,nameImpl) \
    HANDLE_DECL_BASE_OPL(name,name##I,_impl) \
    protected: \
        ImplClass* _impl;

#define HANDLE_DECL_OPL(name) \
    HANDLE_DECL_ROOT_OPL(name,name##I)

#define HANDLE_DECL_SUB_OPL(name,superName) \
    HANDLE_DECL_BASE_OPL(name,name##I,superName)

// ---
// macros for impls

#define DONT_COPY_OPL( classname ) \
    private: \
        classname (const classname& other); \
        classname& operator=(const classname& other);



class IloEncryptedModelException : public IloException {
public:
  IloEncryptedModelException();
  virtual ~IloEncryptedModelException(){}
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif

