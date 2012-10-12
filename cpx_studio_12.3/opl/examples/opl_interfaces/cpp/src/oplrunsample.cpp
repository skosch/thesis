// -------------------------------------------------------------- -*- C++ -*-
// File: oplrunsample.cpp
// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
//
// 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55
// Copyright IBM Corporation 1998, 2011. All Rights Reserved.
//
// Note to U.S. Government Users Restricted Rights:
// Use, duplication or disclosure restricted by GSA ADP Schedule
// Contract with IBM Corp.
///////////////////////////////////////////////////////////////////////////////

#include <ilopl/iloopl.h>
#include <ilopl/ilooplprofiler.h>


#ifndef DATADIR
#ifdef ILO_WINDOWS
#define DIRSEP "\\"
#else
#define DIRSEP "/"
#endif
#define DATADIR ".." DIRSEP ".."  DIRSEP ".." DIRSEP ".." DIRSEP "opl" DIRSEP
#endif

static const char* DEFAULT_mod = DATADIR "mulprod" DIRSEP "mulprod.mod";
static const int DEFAULT_ndat = 1;
static const char* DEFAULT_dat[] = { DATADIR "mulprod" DIRSEP "mulprod.dat" };


class CommandLine;

class OplRunSample {
    CommandLine& _cl;
    IloEnv& _env;
    IloTimer _timer;

    void trace(const char* title, const char* info =0);

public:
    OplRunSample(IloEnv& env, CommandLine& cl);
    int run();
};

class CommandLine {
    const char* _myName;
    IloBool _verbose;
    IloBool _forceUsage;
    IloBool _isRelax;
    IloBool _isConflict;

    IloBool _project;
    const char* _modelFileName;
    int _nDataFiles;
    const char** _dataFileNames;

    const char* _exportName;
    const char* _compileName;
    const char* _externalDataName;
    const char* _internalDataName;

public:
    CommandLine(int argc, char* argv[]);
    ~CommandLine();

    void usage() const;

    const char* getModelFileName() const;
    int getNumberOfDataFiles() const;
    const char** getDataFileNames() const;

    IloBool isProject() const;
    const char* getProjectPath() const;
    const char* getRunConfigurationName() const;

    IloBool isVerbose() const;
    const char* getExportName() const;
    const char* getCompileName() const;
    const char* getExternalDataName() const;
    const char* getInternalDataName() const;
    IloBool isForceElementUsage() const;
    IloBool isRelaxation() const;
    IloBool isConflict() const;
};

int main(int argc,char* argv[]) {
    int status = -1;
    IloEnv env;

    try {
        CommandLine cl(argc,argv);
        OplRunSample oplRun(env,cl);
        status = oplRun.run();
    } catch( IloOplException & e ) {
        cout << "### OPL exception: " << e.getMessage() << endl;
    } catch( IloException & e ) {
        cout << "### CONCERT exception: ";
        e.print(cout);
        status = 2;
        cout << endl;
    } catch (...) {
        cout << "### UNEXPECTED ERROR ..." << endl;
        status = 3;
    }

    env.end();

    cout << endl << "--Press <Enter> to exit--" << endl;
    getchar();

    return status;
}

int OplRunSample::run() {
    locale loc("");
    locale::global(loc);
#ifdef ILO_WINDOWS
    static char buffer[1024];
    cout.rdbuf()->pubsetbuf(buffer,1024);
#endif

    if ( _cl.getCompileName() ) {
        IloOplCompiler compiler(_env);
        ofstream ofs(_cl.getCompileName(),ios::binary);
        IloOplModelSource modelSource(_env,_cl.getModelFileName());
        compiler.compile(modelSource,ofs);
        ofs.close();

        trace("compile");
        return 0;
    }

    if ( _cl.getModelFileName()==0 && !_cl.isProject()) {
        return 0;
    }

    trace("initial");

    IloOplRunConfiguration rc;
    if ( _cl.isProject() ) {
        IloOplProject prj(_env,_cl.getProjectPath());
        rc = prj.makeRunConfiguration(_cl.getRunConfigurationName());
    } else {
        if ( _cl.getNumberOfDataFiles()==0 ) {
            rc = IloOplRunConfiguration(_env,_cl.getModelFileName());
        } else {
            IloStringArray dataFileNames(_env,_cl.getNumberOfDataFiles());
            for(int i=0; i<dataFileNames.getSize(); i++) {
                dataFileNames[i] = _cl.getDataFileNames()[i];
            }
            rc = IloOplRunConfiguration(_env,_cl.getModelFileName(),dataFileNames);
        }
    }

    IloOplErrorHandler handler = rc.getErrorHandler();
    IloOplModel opl = rc.getOplModel();

    IloOplSettings settings = opl.getSettings();
    settings.setWithLocations(IloTrue);
    settings.setWithNames(IloTrue);
    settings.setForceElementUsage(_cl.isForceElementUsage());

    IloInt status = 9;
    if ( opl.getModelDefinition().hasMain() ) {
        status = opl.main();
        cout << "main returns " << status << endl;
        trace("main");
    } else if ( handler.ok() ) {
        opl.generate();
        trace("generate model");

        if ( opl.hasCplex() ) {
            if ( _cl.getExportName() ) {
                opl.getCplex().exportModel( _cl.getExportName() );
                trace("export model",_cl.getExportName());
            }

            if ( _cl.isRelaxation() ) {
                cout << "RELAXATIONS to obtain a feasible problem: " << endl;
                opl.printRelaxation(cout);
                cout << "RELAXATIONS done." << endl << endl;
            }
            if ( _cl.isConflict() ) {
                cout << "CONFLICT in the infeasible problem: " << endl;
                opl.printConflict(cout);
                cout << "CONFLICT done." << endl << endl;
            }
            if (!_cl.isRelaxation() && !_cl.isConflict()) {
                int result = 0;
                try {
                    result = opl.getCplex().solve();
                } catch( IloException & e ) {
                    cout << "### ENGINE exception: ";
                    e.print(cout);
                    cout << endl;
                }

                if ( result ) {
                    trace("solve");
                    cout << endl << endl << "OBJECTIVE: " << fixed << setprecision(2) << opl.getCplex().getObjValue() << endl;

                    opl.postProcess();
                    trace("post process");

                    if ( _cl.isVerbose() ) {
                        opl.printSolution(cout);
                    }
                    status = 0;
                } else {
                    trace("no solution");
                    status = 1;
                }
            }
        } else { // opl.hasCP()
            int result = 0;
            try {
                result = opl.getCP().solve();
            } catch( IloException & e ) {
                cout << "### ENGINE exception: ";
                e.print(cout);
                cout << endl;
            }

            if ( result ) {
                trace("solve");
                if ( opl.getCP().hasObjective() ) {
                    cout << endl << endl << "OBJECTIVE: " << fixed << setprecision(2) << opl.getCP().getObjValue() << endl;
                } else {
                    cout << endl << endl << "OBJECTIVE: no objective" << endl;
                }
                opl.postProcess();
                trace("post process");

                if ( _cl.isVerbose() ) {
                    opl.printSolution(cout);
                }
                status = 0;
            } else {
                trace("no solution");
                status = 1;
            }
        }
    }

    if ( _cl.getExternalDataName() ) {
        ofstream ofs(_cl.getExternalDataName());
        opl.printExternalData(ofs);
        ofs.close();
        trace("write external data",_cl.getExternalDataName());
    }

    if ( _cl.getInternalDataName() ) {
        ofstream ofs(_cl.getInternalDataName());
        opl.printInternalData(ofs);
        ofs.close();
        trace("write internal data",_cl.getInternalDataName());
    }

    trace("done");
    return status;
}

void CommandLine::usage() const {
    cerr << endl;
    cerr << "Usage: " << endl;
    cerr << _myName << " [options] model-file [data-file ...]" << endl;
    cerr << _myName << " [options] -p project-path [run-configuration]" << endl;
    cerr << "  options " << endl;
    cerr << "    -h               " << "this help message" << endl;
    cerr << "    -v               " << "verbose" << endl;
    cerr << "    -e [export-file] " << "export model" << endl;
    cerr << "    -de dat-file     " << "write external data" << endl;
    cerr << "    -di dat-file     " << "write internal data" << endl;
    cerr << "    -o output-file   " << "compile model" << endl;
    cerr << "    -f               " << "force element usage" << endl;
    cerr << "    -relax           " << "calculate relaxations needed for feasible model" << endl;
    cerr << "    -conflict        " << "calculate a conflict for infeasible model" << endl;
    cerr << endl;
    exit(0);
}

static IloBool FileExists(const char* path);

CommandLine::CommandLine(int argc, char* argv[]) {
    _myName = argv[0];
    _verbose = IloFalse;
    _exportName = 0;
    _compileName = 0;
    _externalDataName = 0;
    _internalDataName = 0;
    _project = IloFalse;
    _modelFileName = 0;
    _nDataFiles = 0;
    _dataFileNames = 0;
    _forceUsage = 0;
    _isRelax = IloFalse;
    _isConflict = IloFalse;

    int i=0;
    for (i=1; i<argc; i++) {
        if ( strcmp("-h",argv[i])==0 ) {
            usage();
        } else if ( strcmp("-p",argv[i])==0 ) {
            _project = IloTrue;
        } else if ( strcmp("-v",argv[i])==0 ) {
            _verbose = IloTrue;
        } else if ( strcmp("-e",argv[i])==0 ) {
            i++;
            if ( i<argc && argv[i][0]!='-' && argv[i][0]!='\0' ) {
                _exportName = argv[i];
            } else {
                _exportName = "oplRunSample.lp";
                i--;
            }
        } else if ( strcmp("-o",argv[i])==0 ) {
            i++;
            if ( i<argc && argv[i][0]!='-' && argv[i][0]!='\0' ) {
                _compileName = argv[i];
            } else {
                usage();
            }
        } else if ( strcmp("-de",argv[i])==0 ) {
            i++;
            if ( i<argc && argv[i][0]!='-' && argv[i][0]!='\0' ) {
                _externalDataName = argv[i];
            } else {
                usage();
            }
        } else if ( strcmp("-di",argv[i])==0 ) {
            i++;
            if ( i<argc && argv[i][0]!='-' && argv[i][0]!='\0' ) {
                _internalDataName = argv[i];
            } else {
                usage();
            }
        } else if ( strcmp("-f",argv[i])==0 ) {
            _forceUsage = IloTrue;
        } else if ( strcmp("-relax",argv[i])==0 ) {
            _isRelax = IloTrue;
        } else if ( strcmp("-conflict",argv[i])==0 ) {
            _isConflict = IloTrue;
        } else if ( strncmp("-",argv[i],1)==0 ) {
            cerr << "Unknown option: " << argv[i] << endl;
            usage();
        } else {
            break;
        }
    }

    if ( i<argc ) {
        _modelFileName= argv[i];
        _dataFileNames = 0;
        _nDataFiles = 0;
        i++;
        if ( i<argc ) {
            _dataFileNames = (const char**)&argv[i];
            _nDataFiles = argc-i;
        }
    }

    if ( _modelFileName==0 && FileExists(DEFAULT_mod) ) {
        _modelFileName = DEFAULT_mod;
        _nDataFiles = DEFAULT_ndat;
        _dataFileNames = DEFAULT_dat;
    }

    if ( _project && _nDataFiles>1 ) {
        usage();
    }
}

CommandLine::~CommandLine() {
}

IloBool CommandLine::isVerbose() const {
    return _verbose;
}

const char* CommandLine::getExportName() const {
    return _exportName;
}

const char* CommandLine::getCompileName() const {
    return _compileName;
}

const char* CommandLine::getExternalDataName() const {
    return _externalDataName;
}

const char* CommandLine::getInternalDataName() const {
    return _internalDataName;
}

const char* CommandLine::getModelFileName() const {
    return _modelFileName;
}

int CommandLine::getNumberOfDataFiles() const {
    return _nDataFiles;
}

const char** CommandLine::getDataFileNames() const {
    return _dataFileNames;
}

IloBool CommandLine::isProject() const {
    return _project;
}

const char* CommandLine::getProjectPath() const {
    return _project ? _modelFileName : 0;
}

const char* CommandLine::getRunConfigurationName() const {
    return ( _project && _nDataFiles==1 ) ? _dataFileNames[0] : 0;
}

IloBool CommandLine::isForceElementUsage() const {
    return _forceUsage;
}

IloBool CommandLine::isRelaxation() const {
    return _isRelax;
}

IloBool CommandLine::isConflict() const {
    return _isConflict;
}

OplRunSample::OplRunSample(IloEnv& env, CommandLine& cl)
:_cl(cl), _env(env), _timer(env) {
    _timer.restart();
}

void OplRunSample::trace(const char* title, const char* info) {
    cout << endl << "<<< " << title;
    if ( info ) {
        cout << ": " << info;
    }
    if ( _cl.isVerbose() ) {
        cout << ", at " << _env.getTime() << "s"
            << ", took " << _timer.getTime() << "s";
        _timer.restart();
    }
    cout << endl << endl;
}

static IloBool FileExists(const char* path) {
    FILE* exists = fopen(path,"r");
    if ( exists ) fclose(exists);
    return exists!=NULL;
}

