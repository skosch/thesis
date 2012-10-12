// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilooplprojecti.h
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

#ifndef __OPL_ilooplprojectiH
#define __OPL_ilooplprojectiH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#ifndef __OPL_ilosysH
#include <ilopl/ilosys.h>
#endif

#include <ilopl/ilooplexception.h>
#include <ilopl/iloopldatasource.h>

#include <ilconcert/iloenv.h>
#include <ilconcert/ilostring.h>

class IloOplErrorHandlerI;
class IloOplModelSourceI;
class IloOplModelDefinitionI;
class IloCplexI;
class IloOplDataElementsI;
class IloOplModelI;

class IloCPI;
#include <ilcp/cp.h>

class ILOOPL_EXPORTED IloOplRunConfigurationI: public IloDestroyableI {
    ILORTTIDECL

    const char* _modPath;
    IloStringArray _datPaths;
    const char* _base;

    IloOplErrorHandlerI* _handler;
    IloOplErrorHandlerI* _handlerOwn;

    IloOplSettingsI* _settings;

    IloOplModelSourceI* _modelSource;

    IloOplModelDefinitionI* _def;
    IloOplModelDefinitionI* _defOwn;

    IloCplexI* _cplex;
    IloCplexI* _cplexOwn;
    IloCPI* _cp;
    IloCPI* _cpOwn;
    IloOplDataElementsI* _data;
    IloOplDataSourceArray _dataSources;

    IloOplModelI* _opl;

    void init();
    void checkOpen() const;

protected:
    IloOplRunConfigurationI(IloEnvI* env);

    const char* symbolString(const char* path) const {
        return _env->makeSymbol(path)->getString();
    }

    void setModPath(const char* modPath);
    void setDatPaths(const IloStringArray& datPaths);

    virtual IloOplModelI* makeOplModel();

public:
    IloOplRunConfigurationI(IloEnvI* env, const char* modPath);
    IloOplRunConfigurationI(IloEnvI* env, const char* modPath, const char* datPath);
    IloOplRunConfigurationI(IloEnvI* env, const char* modPath, const IloStringArray& datPaths);
    explicit IloOplRunConfigurationI(IloOplModelDefinitionI& def);
    IloOplRunConfigurationI(IloOplModelDefinitionI& def, IloOplDataElementsI& dataElements);

    ~IloOplRunConfigurationI();

    void end();

    IloBool isCompiledModel() const;

    void setErrorHandler(IloOplErrorHandlerI& handler);
    void setSettings(IloOplSettingsI& settings);
    void setCplex(IloCplexI& cplex);
    void setOwnCplex(IloBool own);

    void setBase(const char* base) {
        _base = base;
    }

    IloOplErrorHandlerI& getErrorHandler();
    IloOplSettingsI& getSettings();
    IloCplexI& getCplex();

    void setCP(IloCPI& cp);
    void setOwnCP(IloBool own);
    IloCPI& getCP();

    IloOplModelI& getOplModel();
	IloBool isOplModelAvailable() const;

    IloBool hasModelDefinition() const;
    IloOplModelDefinitionI& getModelDefinition() const;

private:
    DONT_COPY_OPL(IloOplRunConfigurationI)
};


class XmlProjectConstI;

class ILOOPL_EXPORTED IloOplProjectI: public IloRttiEnvObjectI {
    ILORTTIDECL

    const char* _name;

    const char* _filePath;
    IloInt _format;

    istream* _stream;
    XmlProjectConstI* _xml;

    XmlProjectConstI* getXml();
    void end();

    const char* symbolString(const char* path) const {
        return _env->makeSymbol(path)->getString();
    }

public:
    IloOplProjectI(IloEnvI* env, const char* prjPath);
    IloOplProjectI(IloEnvI* env, istream* stream, const char* name);
    ~IloOplProjectI();

    const char* getName() const {
        return _name;
    }

    const char* getFilePath() const;

    IloStringArray makeRunConfigurationNames(const char* categoryName =0) const;

    IloOplRunConfigurationI* makeRunConfiguration(const char* name =0);
    IloBool tuneParam(IloCplex::ParameterSetI& fixedSet, IloCplex::ParameterSetI& resultSet, IloStringArray runConfigNames, IloOplSettingsI* settings, IloOplTuningCallbackI* cb =0);

    static const char* TranscodeReadValue(IloEnvI*, const char* source);
    static const char* TranscodeWriteValue(IloEnvI*, const char* source);

    static void ApplyProjectSettings(const IloStringArray& opsPaths, const IloOplModelI& model, const char* base);
    static void ApplyProjectSettings(istream& opsStream, const char* name, const IloOplModelI& model);

    static void ReadProjectSettings(istream& opsStream, const char* name, IloCplexI& cplex, IloCplex::ParameterSet parameterSet, IloOplSettingsI& settings);
    static void WriteProjectSettings(IloCplexI& cplex, IloCplex::ParameterSet parameterSet, ostream& opsStream, const char* name);

private:
    friend class IloOplProjectRunConfigurationI;

    const char* getModelPath(const char* rcName);
    IloStringArray makeDataPaths(const char* rcName);
    IloStringArray makeSettingsPaths(const char* rcName);

private:
    DONT_COPY_OPL(IloOplProjectI)
};

class ILOOPL_EXPORTED IloOplProjectRunConfigurationI: public IloOplRunConfigurationI {
    ILORTTIDECL

    IloOplProjectI* _prj;
    const char* _name;

    IloStringArray _opsPaths;

protected:
    void setOpsPaths( const IloStringArray& opsPaths );
    void applyCplexParameters( IloCplexI& cplex );
    void applyCPParameters( IloCPI& cp );
    void applyOplSettings( IloOplSettingsI& settings );

    virtual IloOplModelI* makeOplModel();

public:
    explicit IloOplProjectRunConfigurationI(IloOplProjectI& prj, const char* name);
    ~IloOplProjectRunConfigurationI();
};

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif

