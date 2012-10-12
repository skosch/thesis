// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/iloopldatasourcei.h
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

#ifndef __OPL_iloopldatasourceiH
#define __OPL_iloopldatasourceiH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#include <ilopl/ilosys.h>
#include <ilopl/ilooplmodeli.h>

class IloOplElementI;
class IloOplErrorHandlerI;
class IloOplInputSourceI;
class IloOplDataSourceI;
class IloOplDataFileReaderI;
class IloPiecewiseFunctionMap;

//TODO avoid
#include <ilopl/ilooplelement.h>

class ILOOPL_EXPORTED IloOplDataHandlerI: public IloRttiEnvObjectI {
    ILORTTIDECL
protected:
    IloOplDataHandlerI(IloEnvI* env);
        
public:
    virtual ~IloOplDataHandlerI();

    virtual void startElement(const char* name, IloBool restart =IloFalse) =0;  
	void restartElement(const char* name) {
		startElement(name,IloTrue);
	}
    virtual void endElement() =0;    

    virtual void setElement(IloOplElementI& element) =0;
    virtual void invoke(const char* name, const char* funcName);

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

    virtual void setItemName(const char* name) =0;    

    virtual void startItemTupleIndex() =0;    
    virtual void endItemTupleIndex() =0;    

    virtual void addIntItem(IloInt  value) =0;    
    virtual void addNumItem(IloNum value) =0;    
    virtual void addStringItem(const char* value) =0; 

    virtual IloOplElementI& getElement(const char* name) const =0;

    virtual void executePrepare(const char* block);
    virtual void executePrepare(IloOplScriptThunkI& prepare);

	virtual void endOfDataSource(const IloOplDataSourceI& ds);
};


class ILOOPL_EXPORTED IloOplDataConnectionHandlerI: public IloOplDataHandlerI {
    ILORTTIDECL
protected:
    IloOplDataConnectionHandlerI(IloEnvI* env);
        
public:
    virtual ~IloOplDataConnectionHandlerI();

    virtual void dbConnection(const char* dbId, const char* rdbms, const char* dbName) =0;
    virtual void dbAddParameter(const char* name) =0;
    virtual void dbReadElement(const char* name, const char* dbId, const char* select) =0;
    virtual void dbReadElements(IloStringArray names, const char* dbId, const char* select) =0;
    virtual void dbUpdateElement(const char* name, const char* dbId, const char* update) =0;
    virtual void dbExecute(const char* dbId, const char* execute) =0;

    virtual void sheetConnection(const char* sheetId, const char* sheetName) =0;
    virtual void sheetReadElement(const char* name, const char* sheetId, const char* spec) =0;
    virtual void sheetWriteElement(const char* name, const char* sheetId, const char* spec) =0;

	virtual void setPublishingLocation(const IloOplLocationI& location);

    virtual IloBool handlesCustom(const char* customId) =0;
    virtual void customConnection(const char* connId, const char* subId, const char* spec, const char* customId) =0;
    virtual void customReadElement(const char* name, const char* spec, const char* connId, const char* customId) =0;
    virtual void customPublishElement( const char* name, const char* connId, const char* spec, const char* customId) =0;
};


class ILOOPL_EXPORTED IloOplDataSourceI: public IloRttiEnvObjectI {
    ILORTTIDECL
private:
    IloOplErrorHandlerI* _errorHandler;
    IloOplDataHandlerI* _dataHandler;
	IloOplScriptThunkI* _prepare;

public:
    IloOplDataSourceI(IloEnvI* env);
    virtual ~IloOplDataSourceI();

    void setDataHandler(IloOplDataHandlerI& handler);
    IloOplDataHandlerI& getDataHandler() const;

    void setErrorHandler(IloOplErrorHandlerI& handler);
    IloOplErrorHandlerI& getErrorHandler() const;

    virtual void read() const =0;
    virtual const char* getDataSourceName() const;
    virtual IloBool isOverride() const;

	virtual const IloOplLocationI& getCurrentLocation() const;

	void setPrepare(IloOplScriptThunkI* prepare);
	IloBool hasPrepare() const;
	IloOplScriptThunkI& getPrepare() const;
};


class ILOOPL_EXPORTED IloOplResultPublisherI: public IloRttiEnvObjectI {
    ILORTTIDECL
private:
    IloOplErrorHandlerI* _errorHandler;
    const IloOplModelI* _model;

protected:
    const char* resolveString(const char* name) const;

public:
    IloOplResultPublisherI(IloEnvI* env);
    virtual ~IloOplResultPublisherI();

    void setOplModel(const IloOplModelI& model);
    const IloOplModelI& getOplModel() const;

    const IloOplElementI& getElement(const char* name) const;

    void setErrorHandler(IloOplErrorHandlerI& handler);
    IloOplErrorHandlerI& getErrorHandler() const;

    virtual void publish() =0;
    virtual const char* getResultPublisherName() const =0;
};

class IloOplDataParser;


class ILOOPL_EXPORTED IloOplDataFileReaderI: public IloOplDataSourceI { 
    ILORTTIDECL
private:
    IloOplInputSourceI* _input;
	IloOplDataParser* _parser;

public:
    IloOplDataFileReaderI(IloEnvI* env, const char* filename);
    IloOplDataFileReaderI(IloEnvI* env, istream& ins, const char* name);
    ~IloOplDataFileReaderI();

    void setStream(istream* ins) {
        _input->setStream(ins);
    }

    void read() const;
    const char* getDataSourceName() const;
	const IloOplLocationI& getCurrentLocation() const;
};


class IloOplDataElementI;

class ILOOPL_EXPORTED IloOplDataElementsI: public IloOplDataSourceI {
    ILORTTIDECL
private:
    typedef IloStringHashTable<const IloOplElementI*> DataElements;
    DataElements _dataElements;

public:
    typedef IloArray<const char*> Names;
private:
    Names _names;
    IloOplNopModelI* _nop;

public:
    explicit IloOplDataElementsI(IloEnvI* env);
    IloOplDataElementsI(IloOplModelDefinitionI& def, IloOplDataSourceI& sources, IloBool enableScriptExpressions);
    virtual ~IloOplDataElementsI();

    void addElement(const IloOplElementI& element);
    void addElementAs(const char* name, const IloOplElementI& element);
    void setElement(const IloOplElementI& element);

    const IloOplElementI* getElementOrNull(const char* name) const;
    const IloOplElementI& getElement(const char* name) const;

    const Names& getElementNames() const {
        return _names;
    }
    IloOplElementIteratorI* makeElementIterator() const;

    IloOplElementI* makeElement(const char* name, IloInt value) const;
    IloOplElementI* makeElement(const char* name, IloNum value) const;
    IloOplElementI* makeElement(const char* name, const char* value) const;
    IloOplElementI* makeElement(const char* name, IloTupleI* tuple) const;
   
    IloOplElementI* makeElement(const char* name, void* internal) const;

    void read() const;

    IloOplScriptExpressionI* makeScriptExpression(const char* name, const IloStringArray& paramNames, const char* code);

private:
  void addElement_internal(IloOplDataElementI* dataElement);

};


class IloOplDataSerializerI;

class ILOOPL_EXPORTED IloOplExtractableSerializerI: public IloRttiEnvObjectI {
    ILORTTIDECL
protected:
    IloOplExtractableSerializerI(IloEnvI* env);

public:
    virtual void printExtractable(const IloExtractable& extractable, IloOplDataSerializerI& serializer) =0;
    virtual void printExtractable(const IloExtractable& extractable, ostream& serializer) =0;
};


class IloOplScriptThunkI;

class ILOOPL_EXPORTED IloOplDataSerializerI: public IloOplDataHandlerI {
    ILORTTIDECL
private:
    const IloOplSettingsI* _settings;

    IloInt  _indent;
    IloInt  _newlinePos;
    IloBool _prefix;
    IloBool _compact;
    IloInt  _outputOffset;
    IloInt  _outputLimit;
    IloBool _rawStrings;
        
    ostream& newline(IloInt  pos);
    ostream& item();
    ostream& prefix();
    ostream& open();
    ostream& close();
    IloInt availableWidth(IloInt  pos) const;

	void nextItemNoSpace() {
		_prefix=IloTrue;
	}

    void init(const char* title);
    void printArray(const IloTupleMap& arry, IloIntFixedArray& indices, IloInt dim);

    void checkOutputLimit(IloInt pos) const;

protected:
    ostream* _os;
    locale _osOrigImbue;
    IloBool _ownStream;
	IloBool _stringStream;
    streambuf* _rdbuf;
	IloInt _defaultPrecision;

    void printTupleItem(const IloTuple& tuple, IloInt index, IloTupleSchemaI* schema);
    void printTupleItems(const IloTuple& tuple, IloBool isIndex, IloBool seperate =IloTrue);
    void printIndexTuple(const IloTuple& tuple);
    void printIndex(const IloDiscreteDataCollection& indexer, IloInt index);

    IloInt outPos();

public:
    IloOplDataSerializerI(IloEnvI* env, ostream& os, const char* title =0);
    IloOplDataSerializerI(IloEnvI* env, const IloOplSettingsI& settings, ostream& os, const char* title =0);
    IloOplDataSerializerI(IloEnvI* env, ostream* os, IloInt defaultPrecision =4);
    IloOplDataSerializerI(IloEnvI* env, IloInt defaultPrecision);
    ~IloOplDataSerializerI();

    ostream& out() {
        return *_os;
    }

    void setOutputLimit(IloInt limit);

    void setRawStrings(IloBool raw) {
        _rawStrings = raw;
    }

    void flush() {
        out().flush(); 
    }

    ostream& getStream() const {
        return *_os;
    }

	IloBool isStringStream() const {
		return _stringStream;
	}
    ostringstream& getStringStream() const;

	void setStream(ostream& newos) {
        flush();
        _os = &newos;
        _outputOffset = outPos();
        _newlinePos = _outputOffset;
    }

    void printComment(const char* text);
    void printElement(const IloOplElementI& element);
    void printElementValue(const IloOplElementI& element);

    void printArray(const IloNumMap& arry);
    void printArray(const IloIntMap& arry);
    void printArray(const IloSymbolMap& arry);
    void printArray(const IloTupleMap& arry);

    void printArray(const IloNumSetMap& arry);
    void printArray(const IloIntSetMap& arry);
    void printArray(const IloSymbolSetMap& arry);
    void printArray(const IloTupleSetMap& arry);

    void printArray(const IloConstraintMap& arry);
    void printArray(const IloExtractableMap& arry, IloOplExtractableSerializerI& serializer);

    void printSet(const IloNumSet& set);
    void printSet(const IloIntSet& set);
    void printSet(const IloSymbolSet& set);
    void printSet(const IloTupleSet& set);

    void printTuple(const IloTuple& tuple);
    void printTupleKey(const IloTuple& tuple);
    void printTupleItem(const IloTuple& tuple, IloInt index);
    void printTupleItem(const IloTupleCellArray& cells, IloInt cell, const IloTupleCollectionI& coll);

    void printIndexValue(const IloDiscreteDataCollection& indexer, IloInt index);
	
	void printObject(const IloOplObject& obj);
	void printConstraint(const IloConstraint& ct);

    void printString(const char* text);
    void printNum(IloNum value);
    void printInt(IloInt value);

    static void PrintString(ostream& os, const char* text, IloBool raw, IloBool escaped);
    static void PrintNum(ostream& os, IloNum value, IloInt precision =-1);
    static void PrintNumRange(ostream& os, IloNum lb, IloNum ub, IloInt precision =-1);

	void printPiecewiseFunction( const IloPiecewiseFunctionExpr& value );
	void printArray(const IloPiecewiseFunctionExprMap& arry);

    void printAccess(void* obj, IloOplExtractableSerializerI* serializer =0);

public:
    // IloOplDataHandlerI implementation
    virtual void startElement(const char* name, IloBool restart =IloFalse);    
    virtual void endElement();    

    virtual void setElement(IloOplElementI& element);
	virtual void invoke(const char* name, const char* funcName);

    virtual void startArray();    
    virtual void endArray();    

    virtual void startIndexedArray();    
    virtual void endIndexedArray();    

    virtual void startTuple();    
    virtual void endTuple();    

    virtual void startNamedTuple();    
    virtual void endNamedTuple();    

    virtual void startSet();    
    virtual void endSet();    

    virtual void setItemIntIndex(IloInt  value);    
    virtual void setItemNumIndex(IloNum value);    
    virtual void setItemStringIndex(const char* value);    

    virtual void setItemName(const char* name);    

    virtual void startItemTupleIndex();    
    virtual void endItemTupleIndex();    
    void endItemTupleIndex(IloBool separate);

    virtual void addIntItem(IloInt  value);    
    virtual void addNumItem(IloNum value);    
    virtual void addStringItem(const char* value);    

    virtual IloOplElementI& getElement(const char* name) const;

	virtual void executePrepare(const char* block);
	virtual void executePrepare(IloOplScriptThunkI& prepare);
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif

