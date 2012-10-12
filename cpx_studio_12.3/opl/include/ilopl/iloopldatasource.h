// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/iloopldatasource.h
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

#ifndef __OPL_iloopldatasourceH
#define __OPL_iloopldatasourceH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#include <ilopl/ilooplelement.h>
#include <ilopl/iloopldatasourcei.h>

class IloOplDataHandler;
class IloOplDataSourceBaseI;
class IloOplSettings;
class IloOplScriptThunk;


class ILOOPL_EXPORTED IloOplDataSource {
	HANDLE_DECL_OPL(IloOplDataSource)
public:
    
    IloOplDataSource(IloOplDataSourceBaseI* impl);

    
    IloOplDataSource(IloEnv env, const char* filename):_impl(0) {
        _impl = new (env) IloOplDataFileReaderI(env.getImpl(),filename);
    }

    
    IloOplDataSource(IloEnv env, istream& ins, const char* name):_impl(0) {
        _impl = new (env) IloOplDataFileReaderI(env.getImpl(),ins,name);
    }

	// TODO why wrapped?
    
    void setDataHandler(IloOplDataHandler handler);

    
    IloOplDataHandler getDataHandler() const;

    
    void setErrorHandler(IloOplErrorHandler handler);

    
    IloOplErrorHandler getErrorHandler() const;

    
    void read() const {
        return impl().read();
    }

    
    const char* getDataSourceName() const {
        return impl().getDataSourceName();
    }

    
    IloBool hasPrepare() const {
        return impl().hasPrepare();
    }

    
    IloOplScriptThunk getPrepare() const;
};



typedef IloArray<IloOplDataSource> IloOplDataSourceArray;



class ILOOPL_EXPORTED IloOplDataHandlerBaseI: public IloOplDataHandlerI {
protected:
    
    IloOplDataHandlerBaseI(IloEnv env):IloOplDataHandlerI(env.getImpl()) {
    }
public:

    
    virtual ~IloOplDataHandlerBaseI() {
    }

    
    virtual void startElement(const char* name, IloBool) =0;

    
    virtual void endElement() =0;

    
    virtual void setElement(IloOplElementI& element) =0;

    
    virtual void invoke(const char* , const char* ) {
    }

    
    virtual void startArray() =0;

    
    virtual void endArray() =0;

    
    virtual void startIndexedArray() =0;

    
    virtual void endIndexedArray() =0;

    
    virtual void startTuple() =0;

    
    virtual void endTuple() =0;

    
    virtual void startNamedTuple() =0;

    
    virtual void endNamedTuple() =0;

    
    virtual void startSet() =0;

    
    virtual void endSet() =0;

    
    virtual void setItemIntIndex(IloInt  value)  =0;

    
    virtual void setItemNumIndex(IloNum value)  =0;

    
    virtual void setItemStringIndex(const char* value)  =0;

    
    virtual void startItemTupleIndex() =0;

    
    virtual void endItemTupleIndex() =0;

    
    virtual void setItemName(const char* name) =0;

    
    virtual void addIntItem(IloInt  value) =0;

    
    virtual void addNumItem(IloNum value) =0;

    
    virtual void addStringItem(const char* value) =0;

    
    virtual IloOplElementI& getElement(const char* name) const =0;

    
    virtual void executePrepare(const char* ) {
    }

    
    virtual void executePrepare(IloOplScriptThunkI& ) {
    }
};

class IloOplScriptThunk;



class ILOOPL_EXPORTED IloOplDataHandler {
    HANDLE_DECL_OPL(IloOplDataHandler)
public:
    
    ILO_OPL_DEPRECATED
    IloOplDataHandler(IloEnv env, IloOplSettings settings, ostream& outs);

    
    ILO_OPL_DEPRECATED
    IloOplDataHandler(IloEnv env, ostream& outs);

    
    IloOplDataHandler(IloOplDataHandlerBaseI* impl):_impl(impl) {
    }

    
    void startElement(const char* name) {
        impl().startElement(name);
    }

    
    void restartElement(const char* name) {
        impl().restartElement(name);
    }

	
    void endElement()  {
        impl().endElement();
    }

    
    void setElement(IloOplElement element)  {
        impl().setElement(element.impl());
    }

    
    void invoke(const char* name, const char* funcName)  {
        impl().invoke(name,funcName);
    }

    
    void startArray()  {
        impl().startArray();
    }

    
    void endArray()  {
        impl().endArray();
    }

    
    void startIndexedArray()  {
        impl().startIndexedArray();
    }

    
    void endIndexedArray()  {
        impl().endIndexedArray();
    }

    
    void startTuple()  {
        impl().startTuple();
    }
    
    void endTuple()  {
        impl().endTuple();
    }

    
    void startNamedTuple()  {
        impl().startNamedTuple();
    }

    
    void endNamedTuple()  {
        impl().endNamedTuple();
    }

    
    void startSet()  {
        impl().startSet();
    }

    
    void endSet()  {
        impl().endSet();
    }

    
    void setItemIntIndex(IloInt value)  {
        impl().setItemIntIndex(value);
    }

    
    void setItemNumIndex(IloNum value)  {
        impl().setItemNumIndex(value);
    }

    
    void setItemStringIndex(const char* value)  {
        impl().setItemStringIndex(value);
    }

    
    void startItemTupleIndex()  {
        impl().startItemTupleIndex();
    }

    
    void endItemTupleIndex()  {
        impl().endItemTupleIndex();
    }

    
    void setItemName(const char* name)  {
        impl().setItemName(name);
    }

    
    void addIntItem(IloInt value)  {
        impl().addIntItem(value);
    }

    
    void addNumItem(IloNum value)  {
        impl().addNumItem(value);
    }

    
    void addStringItem(const char* value)  {
        impl().addStringItem(value);
    }

    
    IloOplElement getElement(const char* name) const  {
        return &impl().getElement(name);
    }

    
	void executePrepare(const char* block) {
        impl().executePrepare(block);
	}

    
	void executePrepare(IloOplScriptThunk prepare);
};



class ILOOPL_EXPORTED IloOplDataSerializer: public IloOplDataHandler {
    HANDLE_DECL_SUB_OPL(IloOplDataSerializer,IloOplDataHandler)
public:
    
    IloOplDataSerializer(IloEnv env, IloOplSettings settings, ostream& outs);

    
    IloOplDataSerializer(IloEnv env, IloOplSettings settings, ostream& outs, IloBool header);

    
    IloOplDataSerializer(IloEnv env, IloOplSettings settings, ostream& outs, const char* title);

    
    IloOplDataSerializer(IloEnv env, ostream& outs);

    
    void printElement(IloOplElement element) {
        impl().printElement(element.impl());
    }

    
    void printArray(IloNumMap array) {
        impl().printArray(array);
    }

    
    void printArray(IloIntMap array) {
        impl().printArray(array);
    }

    
    void printArray(IloSymbolMap array) {
        impl().printArray(array);
    }

    
    void printArray(IloTupleMap array) {
        impl().printArray(array);
    }

    
    void printArray(IloNumSetMap array) {
        impl().printArray(array);
    }

    
    void printArray(IloIntSetMap array) {
        impl().printArray(array);
    }

    
    void printArray(IloSymbolSetMap array) {
        impl().printArray(array);
    }

    
    void printArray(IloTupleSetMap array) {
        impl().printArray(array);
    }

    
    void printSet(IloNumSet set) {
        impl().printSet(set);
    }

    
    void printSet(IloIntSet set) {
        impl().printSet(set);
    }

    
    void printSet(IloSymbolSet set) {
        impl().printSet(set);
    }

    
    void printSet(IloTupleSet set) {
        impl().printSet(set);
    }

    
    void printTuple(IloTuple tuple) {
        impl().printTuple(tuple);
    }

    
    void printTupleKey(IloTuple tuple) {
        impl().printTupleKey(tuple);
    }

    
	void printObject(IloOplObject obj) {
        impl().printObject(obj);
    }

    
	void flush() {
        impl().flush();
    }

    
    void setOutputLimit(IloInt limit) {
        impl().setOutputLimit(limit);
    }

    
    void setRawStrings(IloBool raw) {
        impl().setRawStrings(raw);
    }
};



class ILOOPL_EXPORTED IloOplDataSourceBaseI: public IloOplDataSourceI {
protected:
    
    IloOplDataSourceBaseI(IloEnv env):IloOplDataSourceI(env.getImpl()) {
    }
public:
    
    virtual ~IloOplDataSourceBaseI() {
    }

    
    IloOplDataHandler getDataHandler() const {
        return &IloOplDataSourceI::getDataHandler();
    }

    
    IloOplErrorHandler getErrorHandler() const {
        return &IloOplDataSourceI::getErrorHandler();
    }

    
    virtual void read() const =0;

    
    virtual const char* getDataSourceName() const {
        return IloOplDataSourceI::getDataSourceName();
    }
};



class IloOplModelDefinition;
class IloOplScriptExpression;


class ILOOPL_EXPORTED IloOplDataElements: public IloOplDataSource {
public:
    
    IloOplDataElements():IloOplDataSource() {
    }

    
    IloOplDataElements(IloOplDataElementsI* impl):IloOplDataSource(impl) {
    }

    
    IloOplDataElements(IloEnv env):IloOplDataSource(new (env) IloOplDataElementsI(env.getImpl())) {
    }

    
    IloOplDataElements(IloOplModelDefinition def, IloOplDataSource source);

    
    IloOplDataElements(IloOplModelDefinition def, IloOplDataSource source, IloBool enableScriptExpressions);

    
    IloOplDataElements(IloOplModelDefinition def, IloOplDataSourceArray sources);

    
    IloOplElementIterator makeElementIterator() {
        return static_cast<const IloOplDataElementsI&>(impl()).makeElementIterator();
    }

    
    IloOplElement makeElement(const char* name, IloInt value) const {
        return static_cast<const IloOplDataElementsI&>(impl()).makeElement(name,value);
    }

    
    IloOplElement makeElement(const char* name, IloNum value) const {
        return static_cast<const IloOplDataElementsI&>(impl()).makeElement(name,value);
    }

    
    IloOplElement makeElement(const char* name, const char* value) const {
        return static_cast<const IloOplDataElementsI&>(impl()).makeElement(name,value);
    }

    
    IloOplElement makeElement(const char* name, IloTuple value) const {
        return static_cast<const IloOplDataElementsI&>(impl()).makeElement(name,value.getImpl());
    }

    
    void addElement(IloOplElement element) {
        static_cast<IloOplDataElementsI&>(impl()).addElement(element.impl());
    }

    
    void addElementAs(const char* name, IloOplElement element) {
        static_cast<IloOplDataElementsI&>(impl()).addElementAs(name,element.impl());
    }

    
    void setElement(IloOplElement element) {
        static_cast<IloOplDataElementsI&>(impl()).setElement(element.impl());
    }

    
    IloOplElement getElement(const char* name) const {
        return IloOplElement(&(static_cast<const IloOplDataElementsI&>(impl()).getElement(name)));
    }

    
    IloOplScriptExpression makeScriptExpression(const char* name, IloStringArray paramNames, const char* code);
};


inline void IloOplDataSource::setDataHandler(IloOplDataHandler handler) {
    impl().setDataHandler(handler.impl());
}

inline IloOplDataSource::IloOplDataSource(IloOplDataSourceBaseI* impl) :_impl(impl) {
}

inline IloOplDataHandler IloOplDataSource::getDataHandler() const {
    return &impl().getDataHandler();
}

inline void IloOplDataSource::setErrorHandler(IloOplErrorHandler handler) {
    return impl().setErrorHandler(handler.impl());
}

inline IloOplErrorHandler IloOplDataSource::getErrorHandler() const {
    return &impl().getErrorHandler();
}


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif

