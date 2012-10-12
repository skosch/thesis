// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilooplerrorhandler.h
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

#ifndef __OPL_ilooplerrorhandlerH
#define __OPL_ilooplerrorhandlerH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#ifndef __OPL_ilosysH
#include <ilopl/ilosys.h>
#endif

#include <ilopl/ilooplerrorhandleri.h>



class ILOOPL_EXPORTED IloOplLocation {
    HANDLE_DECL_BASE_WITHOUTEND_OPL(IloOplLocation,IloOplLocationI,_impl)
	const IloOplLocationI* _impl;
public:
    
    IloOplLocation(IloEnv env, int l0, int c0, int l1, int c1, const char* source) :_impl(0) {
		_impl = &IloOplLocationI::GetLocation(env.getImpl(),l0,c0,l1,c1,source);
    }

	
    void end() { 
		_impl=0; //delete by factory
	}

    
    static const IloOplLocation UNKNOWN;

    
    int getLine() const {
        return impl().getLine();
    }

    
    int getColumn() const {
        return impl().getColumn();
    }

    
    int getEndLine() const {
        return impl().getEndLine();
    }

    
    int getEndColumn() const {
        return impl().getEndColumn();
    }

    
    const char* getSource() const {
        return impl().getSource();
    }

    
    IloBool isUnknown() const {
        return impl().isUnknown();
    }

    
    ostream& printOn( ostream& os ) const {
        return impl().printOn(os);
    }
};

class IloOplErrorHandlerBaseI;



class ILOOPL_EXPORTED IloOplErrorHandler {
    HANDLE_DECL_OPL(IloOplErrorHandler)
public:
    
    IloOplErrorHandler(IloOplErrorHandlerBaseI* impl);

    
    IloOplErrorHandler(IloEnv env):_impl(0) {
        _impl = new (env) IloOplDefaultErrorHandlerI(env.getImpl());
    }

    
    IloOplErrorHandler(IloEnv env, ostream& outs):_impl(0) {
        _impl = new (env) IloOplDefaultErrorHandlerI(env.getImpl(),outs);
    }

    
    void fatal( const IloOplMessage& message, IloOplLocation location );

    
    void error( const IloOplMessage& message, IloOplLocation location ) {
        impl().error(message,location.impl());
    }

    
    void warning( const IloOplMessage& message, IloOplLocation location ) {
        impl().warning(message,location.impl());
    }

    
    IloBool ok() const {
        return impl().ok();
    }

    
    void abort() {
        return impl().abort();
    }
};



class ILOOPL_EXPORTED IloOplErrorHandlerBaseI: public IloOplErrorHandlerI {
protected:
    
    IloOplErrorHandlerBaseI(IloEnv env):IloOplErrorHandlerI(env.getImpl()) {
    }

    
    virtual IloBool handleFatal( const IloOplMessage& message, const IloOplLocationI& location ) {
        IloOplLocation loc(&location);
        return handleFatal(message,IloOplLocation(&location));
    }

    
    virtual IloBool handleError( const IloOplMessage& message, const IloOplLocationI& location ) {
        IloOplLocation loc(&location);
        return handleError(message,loc);
    }

    
    virtual IloBool handleWarning( const IloOplMessage& message, const IloOplLocationI& location ) {
        return handleWarning(message,IloOplLocation(&location));
    }

    
    virtual IloBool handleAssertFail( const char* label, const IloStringArray& names, const IloMapIndexArray& values, const IloOplLocationI& location) {
        return handleAssertFail(label,names,values,IloOplLocation(&location));
    }

public:
    
    virtual ~IloOplErrorHandlerBaseI() {
    }

    
    virtual IloBool handleFatal( const IloOplMessage& message, IloOplLocation location ) =0;

    
    virtual IloBool handleError( const IloOplMessage& message, IloOplLocation location ) =0;

    
    virtual IloBool handleWarning( const IloOplMessage& message, IloOplLocation location ) =0;

    
    virtual IloBool handleAssertFail( const char* label, const IloStringArray& names, const IloMapIndexArray& values, IloOplLocation location);

    
    virtual IloBool ok() const {
        return IloOplErrorHandlerI::ok();
    }
};

inline IloOplErrorHandler::IloOplErrorHandler(IloOplErrorHandlerBaseI* impl) :_impl(impl) {
}

inline IloBool IloOplErrorHandlerBaseI::handleAssertFail( const char* label, const IloStringArray& names, const IloMapIndexArray& values, IloOplLocation location) {
    return IloOplErrorHandlerI::handleAssertFail(label,names,values,location.impl());
}

inline
ILOOPL_EXPORTEDFUNCTION(ostream&) operator<<( ostream&  os, const IloOplLocation&  location) {
    return location.printOn(os);
}


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif

