// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilosys.h
// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
//
// 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55 
// Copyright IBM Corp. 2000, 2011
//
// US Government Users Restricted Rights - Use, duplication or
// disclosure restricted by GSA ADP Schedule Contract with
// IBM Corp.
// ---------------------------------------------------------------------------

#ifndef __OPL_ilosysH
#define __OPL_ilosysH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#ifndef __CONCERT_ilosysH
#include <ilconcert/ilosys.h>
#endif
#ifndef __CONCERT_iloenvH
#include <ilconcert/iloenv.h>
#endif

#ifndef ILO_ADVANCED_CONCERT
#define ILO_ADVANCED_CONCERT
#endif
#define ILC_SHOW_FUNC_PARAM

#undef IloAssert
//#ifdef NDEBUG
//#define IloAssert(x,y)
//#else
#define IloAssert(x,y) if (!(x)) throw IloWrongUsage("ASSERT FAILED: " y);
//#endif

#ifdef ILO_WINDOWS
#define ILO_OPL_DEPRECATED __declspec(deprecated)
#else
#define ILO_OPL_DEPRECATED
#endif

#if defined(ILOOPL_DLL)
#pragma warning( disable: 4251 )
#if defined(ILO_BUILD_LIB)
#  define ILOOPL_EXPORTED ILO_BASEEXPORTED
#  define ILOOPL_EXPORTEDFUNCTION(type) ILO_BASEEXPORTEDFUNCTION(type)
#  define ILOOPL_EXPORTEDVAR(type) ILO_BASEEXPORTEDFUNCTION(type)
#else
#  define ILOOPL_EXPORTED ILO_BASEIMPORTED
#  define ILOOPL_EXPORTEDFUNCTION(type) ILO_BASEIMPORTEDFUNCTION(type)
#  define ILOOPL_EXPORTEDVAR(type) ILO_BASEIMPORTEDFUNCTION(type)
#endif
#else 
#  define ILOOPL_EXPORTED
#  define ILOOPL_EXPORTEDFUNCTION(type) type
#  define ILOOPL_EXPORTEDVAR(type) type
#endif


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif

