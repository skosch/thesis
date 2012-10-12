// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilooplrun.h
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

#ifndef __OPL_ilooplrunH
#define __OPL_ilooplrunH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#ifndef __OPL_ilosysH
#include <ilopl/ilosys.h>
#endif
//#include <ilopl/ilosys.h>

class IloOplPrinter;
class IloModel;


int IloOplExpandAll(IloOplPrinter printer, ostream& os, IloModel model, IloBool forall, IloBool expr, int level);


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
