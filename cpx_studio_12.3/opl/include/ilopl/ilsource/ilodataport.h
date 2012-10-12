// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilsource/ilodataport.h
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


#ifndef __ADVANCED_ilodataportH
#define __ADVANCED_ilodataportH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#include <ilopl/ilosys.h> //CG

#include <new>
#include <stdlib.h>

class data_bad_alloc {};

#define WORD_SIZE    32
#define WORD_SHIFT  (WORD_SIZE - 1)

#if defined(ILO_MSVC)
#define NOTHROW 
#define REINTERPRET_CAST(type,expression) (reinterpret_cast<type>(expression))
#define CONST_CAST(type,expression) (const_cast<type>(expression))
#define STATIC_CAST(type,expression) (static_cast<type>(expression))
#define TYPENAME typename
#endif


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif

