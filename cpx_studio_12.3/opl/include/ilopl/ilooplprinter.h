// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilooplprinter.h
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

#ifndef __OPL_ilooplprinterH
#define __OPL_ilooplprinterH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#include <ilopl/ilooplprinteri.h>


class ILOOPL_EXPORTED IloOplPrinter {
    HANDLE_DECL_OPL(IloOplPrinter)
public:
    explicit IloOplPrinter(IloEnv env):_impl(0) {
        _impl = new (env) IloOplPrinterI(env.getImpl());
    }

    void print(ostream& os, IloExtractable extr) {
        impl().print(os,extr.getImpl());
    }
};


#ifdef _WIN32
#pragma pack(pop)
#endif


#endif
