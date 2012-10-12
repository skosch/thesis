// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilooplutils.h
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

#ifndef __OPL_ilooplutilsH
#define __OPL_ilooplutilsH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#ifndef __OPL_ilosysH
#include <ilopl/ilosys.h>
#endif

#include <jni.h>

#include <list>
#include <map>
#include <string>
ILOSTLBEGIN




#define ODMS_JAVA_ARGS_ENV_VAR_NAME "ODMS_JAVA_ARGS"
#define ODMROOT_OVERRIDE_ENV_VAR_NAME "ODMROOT_OVERRIDE"
#define OPLROOT_OVERRIDE_ENV_VAR_NAME "OPLROOT_OVERRIDE"
#define ODMS_JVM_LIBRARY_OVERRIDE_ENV_VAR "ODMS_JVM_LIBRARY_OVERRIDE"
#define ODMS_JVM_NO_INHERITED_LIBPATH_APPEND "ODMS_JVM_NO_INHERITED_LIBPATH_APPEND"

#define COMMAND_LINE_REDIRECT_OUTPUT_ON true
#define COMMAND_LINE_REDIRECT_OUTPUT_OFF false
#define COMMAND_LINE_USE_IBM_GENERATIONAL_GC true
#define COMMAND_LINE_USE_DEFAULT_GC false


class IloOplMessageHandler {
public:
	virtual ~IloOplMessageHandler() {};
	// report message by message id and args:
	virtual void info(const char*,const char* =0,const char* =0) = 0;
	virtual void warning(const char*,const char* =0,const char* =0) = 0;
	virtual void error(const char*,const char* =0,const char* =0) = 0;
	// report error as an exception, stopping the processing:
	virtual void fatal(const char*,const char* =0,const char* =0) = 0;

};


bool IloOplHasValidOPLRoot(IloOplMessageHandler& reportHandler);


std::string IloOplGetOPLRootDir(IloOplMessageHandler& reportHandler);


bool IloOplIsODMEnabled(IloOplMessageHandler& reportHandler);


std::string IloOplGetODMRootDir(IloOplMessageHandler& reportHandler);


bool IloOplIsRunningAsSharedLib();


std::string IloOplGetCurrentModuleDirPath();


void IloOplAddToSharedLibLoadingPath(const std::string& item);


JNIEnv* IloOplGetJVM(IloOplMessageHandler& reportHandler);


bool IloOplCreateJVM(IloOplMessageHandler& reportHandler,
                     bool redirectStdStreamsOnDebug = COMMAND_LINE_REDIRECT_OUTPUT_ON,
                     bool useGenerationalConcurrentGC = COMMAND_LINE_USE_DEFAULT_GC);


std::string IloCatchAndThrowFromJVM();


bool IloOplLaunchJavaMain(string programName,
              string className,
              IloOplMessageHandler& reportHandler,
              const list<string>& args,
              bool redirectStdStreamsOnDebug = COMMAND_LINE_REDIRECT_OUTPUT_ON,
              bool useGenerationalConcurrentGC = COMMAND_LINE_USE_DEFAULT_GC);


char** IloOplCommandLineToArgv(const char* cmdLine,int* argc);


void parseArgumentsAndExec(int argc, char* argv[], const char* programName, const char* mainJavaClassName,
                           bool redirectStdStreamsOnDebug = COMMAND_LINE_REDIRECT_OUTPUT_ON,
                           bool useGenerationalConcurrentGC = COMMAND_LINE_USE_DEFAULT_GC);


class IloOplDefaultMessageHandler : public IloOplMessageHandler
{
private:
	bool _messagesCatalogInited;
	map<string,string> _messagesCatalog;
	list<string> _reportedMessages;
public:
	IloOplDefaultMessageHandler();
	virtual void info(const char* msgId,const char* arg1=0,const char* arg2=0);
	virtual void warning(const char* msgId,const char* arg1=0,const char* arg2=0);
	virtual void error(const char* msgId,const char* arg1=0,const char* arg2=0);
	virtual void fatal(const char* msgId,const char* arg1=0,const char* arg2=0);
	string localize(const char* msgId,const char* arg1=0,const char* arg2=0);
	const list<string>& getReportedMessages();
protected:
	map<string,string>& getMessageCatalog();
	virtual void report(const char* level,const char* localizedMessage);
	static string fixPlaceHolders(string text);
};

class IloOplNullMessageHandler : public IloOplMessageHandler
{
public:
	IloOplNullMessageHandler() { }
	void info(const char* msgId,const char* arg1=0,const char* arg2=0) { }
	void warning(const char* msgId,const char* arg1=0,const char* arg2=0) { }
	void error(const char* msgId,const char* arg1=0,const char* arg2=0) { }
	void fatal(const char* msgId,const char* arg1=0,const char* arg2=0) { }
};


class IloOplLogger {
private:
	static bool _enabled;
	static std::string _currentIndent;
public:
	// true if log are enabled
	static void setLogEnabled(bool enabled) { _enabled=enabled; }
	// true if log are enabled
	static bool isLogEnabled() { return _enabled; }
	// increase indent for next messages
	static void increaseIndent() { _currentIndent+="--"; }
	// increase indent for next messages
	static void decreaseIndent() { _currentIndent.resize(_currentIndent.size()-2); }
    // report log message: not a message id, since logs are not i18n
	static void log(const char* msgPart1,const char* msgPart2=0,const char* msgPart3=0,const char* msgPart4=0,const char* msgPart5=0);
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
