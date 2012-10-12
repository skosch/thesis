// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilooplerrorhandleri.h
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

#ifndef __OPL_ilooplerrorhandleriH
#define __OPL_ilooplerrorhandleriH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#ifndef __OPL_ilosysH
#include <ilopl/ilosys.h>
#endif

#include <ilopl/ilooplexception.h>

#include <ilconcert/iloenv.h>
//#include <ilconcert/ilosymbol.h>
#include <ilconcert/iloalg.h>
#include <ilopl/iloevaluator.h>
#include <ilopl/iloforallbase.h>
#include <ilopl/ilooplcpi.h>
#include <ilopl/ilooplcpexceptions.h>

#include <ilcplex/ilocplexi.h>
#include <ilcp/cp.h>

#include <iostream>
using namespace std;

class IloOplLocationI;
class IloMapOutOfBoundException;
class IloMapsDimensionException;
class IloNumCollectedMapOutOfBoundException;
class IloOrdValueNotFoundException;
class IloCollectionElementNotFoundException;
class IloOperatorNextElementNotFoundException;
class IloFirstLastElementNotFoundException;
class IloInvalidExpression;
class IloHomomorphismErrorHandlerI;
class IljRuntimeError;
class IloExcelBadFormatException;
class IloExcelCellErrorException;
class IloExcelNotEnoughMemoryException;
class IloNumCollectedMapRangeException;
class IloNumCollectedMapMismatchSizeException;
class IloIndexOutOfBoundsException;

class IloAdvLabelCallbackI;
class IloTransitionDistanceTupleException;

class IloOplSettingsI;


class ILOOPL_EXPORTED IloOplErrorHandlerI: public IloRttiEnvObjectI {
    ILORTTIDECL
private:
    IloInt _countErrors;
    IloInt _countWarnings;
    IloInt _maxErrors;
    IloInt _maxWarnings;

    short _aborted;
    void throwAborted();

    typedef IloArray<IloHomomorphismErrorHandlerI*> EnvBridges;
    EnvBridges _envBridges;

	IloInt getMaxErrors() const {
		return _maxErrors;
	}

	IloInt getMaxWarnings() const {
		return _maxWarnings;
	}

	IloBool incrementErrors();
    IloBool incrementWarnings();

public:
    virtual ~IloOplErrorHandlerI();

    void setWithWarnings(IloBool flag) {
		if ( (!flag && _maxWarnings>0) || (flag && _maxWarnings<0) ) {
			_maxWarnings = -_maxWarnings;
		}
    }
    void setMaxErrors(IloInt max) {
        _maxErrors = max;
    }
    void setMaxWarnings(IloInt max) {
        _maxWarnings = max;
    }

    IloInt getErrorsLeft() const {
        return getMaxErrors() - _countErrors;
    }
    IloInt getWarningsLeft() const {
        return getMaxWarnings() - _countWarnings;
    }

    void fatal( const IloOplMessage&  message, const IloOplLocationI& location );
    void error( const IloOplMessage&  message, const IloOplLocationI& location );
    void warning( const IloOplMessage&  message, const IloOplLocationI& location );

    virtual IloBool ok() const;

    void abort();
    inline IloBool isAborted() const;
    inline void checkAborted();

    // convenience for known exceptions types
    void error(const IloExtractableI* extractable, const char* algorithmName, IloBool asWarning =IloFalse);
    void error(const IloExtractable& extractable, const char* algorithmName, IloBool asWarning =IloFalse) {
        error(extractable.getImpl(),algorithmName,asWarning);
    }
    void error(IloAlgorithm::CannotExtractException& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(IloAlgorithm::CannotChangeException& e, IloBool asWarning =IloFalse);
    void error(IloNumCollectedMapOutOfBoundException& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(IloMapOutOfBoundException& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(IloMapsDimensionException& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(const IloOrdValueNotFoundException& e, const IloOplLocationI& location, IloBool asWarning );
    void error(const IloIntExpr::IloOverflowOccurred& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(const IloAdvModelEvaluator::Overflow& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(const IloAdvModelEvaluator::AggregateFilter& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(const IloAdvModelEvaluator::NotFoundTuple& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(const IloAdvModelEvaluator::Unbound& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(const IloGenerator::Exception& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(const IloCplex::Exception& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(const IloCP::ModelInconsistent& exception, const IloOplLocationI& location, IloBool asWarning=IloFalse);
    void error(const IloCP::StateFunctionNoTriangularInequality& exception, const IloOplLocationI& location, IloBool asWarning=IloFalse);
    void error(const IloMetaConstraintNotAllowedException& exception, const IloOplLocationI& location, IloBool asWarning=IloFalse);
    void error(const IloInvalidExpression& e, IloBool asWarning =IloFalse);
    void error(const IloTupleCollection::DuplicatedKey& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(const IloTupleCollection::DuplicatedTuple& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(const IloDataCollection::ImmutableException& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(const IloTupleCollection::UnknownReference& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(const IloIntDataColumnI::UnknownReference& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(const IloNumDataColumnI::UnknownReference& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(const IloAnyDataColumnI::UnknownReference& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(const IloTupleSetI::OPL3213& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
	void error(const IloCollectionElementNotFoundException& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
	void error(const IloOperatorNextElementNotFoundException& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
	void error(const IloFirstLastElementNotFoundException& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(const IloMismatchDExprSubstitution& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(const IloNumCollectedMapRangeException& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(const IloParsingNumberMismatch& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
	void error(const IloExcelBadFormatException& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
	void error(const IloExcelCellErrorException& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
	void error(const IloExcelNotEnoughMemoryException& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
	void error(const IloNumCollectedMapMismatchSizeException& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
	void error(const IloEncryptedModelException& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
	void error(IloTransitionDistanceTupleException& e, const IloOplLocationI& location, IloBool asWarning);
	void error(const IloNumCollectedMapUnboundException & e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
	void error(const IloExtractableCollectedMapUnboundException  & e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
	void error(const IloIndexOutOfBoundsException& e, const IloOplLocationI& location, IloBool asWarning =IloFalse);
    void error(IljRuntimeError& e);

    void assertFail(const char* label, const IloStringArray& names, const IloMapIndexArray& values, const IloOplLocationI& location);

    void createBridgeTo(IloEnvI* env);

    class TryCatchAction: public IloEnvObjectI {
    protected:
        friend class IloOplErrorHandlerI;
        IloBool _didThrow;
        TryCatchAction(IloEnvI* env);
    public:
        virtual ~TryCatchAction();
        virtual void doTry();
        virtual void doFinally(IloOplErrorHandlerI& handler);
        virtual IloBool handleAsWarning() const;
        virtual IloBool handleAsFatal() const;
        virtual const IloOplLocationI& getLocation() const;
    };

    IloBool tryCatch(TryCatchAction* tryCatchAction);

protected:
	explicit IloOplErrorHandlerI(IloEnvI* env);

    IloBool getErrors() const {
        return _countErrors;
    }
    IloBool getWarnings() const {
        return _countWarnings;
    }

    virtual IloBool handleFatal( const IloOplMessage&  message, const IloOplLocationI& location ) = 0;
    virtual IloBool handleError( const IloOplMessage&  message, const IloOplLocationI& location ) = 0;
    virtual IloBool handleWarning( const IloOplMessage&  message, const IloOplLocationI& location ) = 0;
    // use IloMapIndexArray instead of IloMapIndexArray for wrappers
    virtual IloBool handleAssertFail( const char* label, const IloStringArray& names, const IloMapIndexArray& values, const IloOplLocationI& location);

private:
    // copy protection
    IloOplErrorHandlerI(const IloOplErrorHandlerI& other);
    IloOplErrorHandlerI& operator=(const IloOplErrorHandlerI& other);
};

inline void IloOplErrorHandlerI::abort() {
    _aborted++;
}

inline IloBool IloOplErrorHandlerI::isAborted() const {
    return _aborted>0;
}

inline void IloOplErrorHandlerI::checkAborted() {
    if ( isAborted() ) {
        throwAborted();
    }
}

class ILOOPL_EXPORTED IloOplDefaultErrorHandlerI: public IloOplErrorHandlerI {
    ILORTTIDECL
protected:
    ostream& _os;

public:
	IloOplDefaultErrorHandlerI(IloEnvI* env);
    IloOplDefaultErrorHandlerI(IloEnvI* env, ostream& os);

protected:
    virtual IloBool handleFatal( const IloOplMessage& message, const IloOplLocationI& location );
    virtual IloBool handleError( const IloOplMessage& message, const IloOplLocationI& location );
    virtual IloBool handleWarning( const IloOplMessage& message, const IloOplLocationI& location );

protected:
    virtual void print( const char* level,
                        const IloOplMessage&  message, const IloOplLocationI& location );
};

class ILOOPL_EXPORTED IloOplLocationI: public IloRttiEnvObjectI {
    ILORTTIDECL
private:
    int _l0;
    int _c0;
    int _l1;
    int _c1;
    const char* _source;

public:
    static void PushLocations(IloEnvI* env, const char* source);
    static void PopLocations(IloEnvI* env, const char* source);
	static const IloOplLocationI& GetLocation(IloEnvI* env, int l0, int c0, int l1, int c1, const char* source);
	static const IloOplLocationI& CopyLocation(const IloOplLocationI&, const char* source);

    static const IloOplLocationI UNKNOWN;

    IloBool isUnknown() const;

    int getLine() const {
      return _l0;
    }

    int getColumn() const {
      return _c0;
    }

    int getEndLine() const {
      return _l1;
    }

    int getEndColumn() const {
      return _c1;
    }

    const char* getSource() const {
       return _source;
    }

    ostream& printOn( ostream& os ) const;

private:
	friend class IloOplLocationFactory;
    IloOplLocationI(IloEnvI* env, int l0, int c0, int l1, int c1, const char* source);
    IloOplLocationI(const IloOplLocationI& other);
    IloOplLocationI& operator=(const IloOplLocationI& other);
	virtual ~IloOplLocationI();

protected:
    void set(int l0, int c0, int l1, int c1, const char* source);

private:
    IloOplLocationI():IloRttiEnvObjectI(0) {
        _l0 = _l1 = _c0 = _c1 = -1;
        _source = 0;
    }
};

ILOOPL_EXPORTEDFUNCTION(ostream&) operator<<( ostream&  os, const IloOplLocationI&  location);


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif

