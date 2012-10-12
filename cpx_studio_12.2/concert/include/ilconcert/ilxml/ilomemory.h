// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilconcert/ilxml/ilomemory.h
// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
//
// 5725-A06 5725-A29 5724-Y47 5724-Y48 5724-Y49 5724-Y54 5724-Y55
// Copyright IBM Corp. 2000, 2010
//
// US Government Users Restricted Rights - Use, duplication or
// disclosure restricted by GSA ADP Schedule Contract with
// IBM Corp.
// ---------------------------------------------------------------------------

#ifndef __XML_ilomemoryH
#define __XML_ilomemoryH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#include <ilconcert/ilxml/ilomacros.h>

#if defined(IL_STD)
#include <cstddef>
#else  /* ! IL_STD */
#include <stddef.h>
#endif /* ! IL_STD */

#if defined(_MSC_VER) && ((_MSC_VER < 1300) || defined(WIN64))/* msvc6. */
ILO_EXPORTEDFUNCTION(void*) IloXmlMalloc(size_t nBytes);
ILO_EXPORTEDFUNCTION(void*) IloXmlRealloc(void* block, size_t);
ILO_EXPORTEDFUNCTION(void*) IloXmlMemMove(void*, const void*, size_t);
#else  /* !msvc6 */
ILO_EXPORTEDFUNCTION(void*) IloXmlMalloc(IL_STDPREF size_t nBytes);
ILO_EXPORTEDFUNCTION(void*) IloXmlRealloc(void* block, IL_STDPREF size_t);
ILO_EXPORTEDFUNCTION(void*) IloXmlMemMove(void*, const void*, IL_STDPREF size_t);
#endif /* !msvc6. */
ILO_EXPORTEDFUNCTION(void)  IloXmlFree(void* block);
#define ILOXML_MALLOC(type, n)  ((type*)IloXmlMalloc((n)*sizeof(type)))
#define ILOXML_REALLOC(type, a, n) ((type*)IloXmlRealloc((IloXmlAny)(a), (n)*sizeof(type)))

#if defined(WINDOWS)
# define ILOXML_STRCASECMP(s1, s2) stricmp(s1, s2)
# define ILOXML_STRNCASECMP(s1, s2, n) strnicmp(s1, s2, n)
#else  /* !WINDOWS */
# if defined(sun) && !defined(__SVR4)
#  if defined(ILOXML_SUNCC2_1)
#   define ILOXML_STRCASECMP(s1, s2) strcasecmp((char*)(s1), (char*)(s2))
#   define ILOXML_STRNCASECMP(s1, s2, n) strncasecmp((char*)(s1), (char*)(s2), n)
#  else
#   define ILOXML_STRCASECMP(s1, s2) strcasecmp((char*)(s1), s2)
#   define ILOXML_STRNCASECMP(s1, s2, n) strncasecmp((char*)(s1), s2, n)
#  endif /* !ILOXML_SUNCC2_1 */
# else
#  define ILOXML_STRCASECMP(s1, s2) strcasecmp(s1, s2)
#  define ILOXML_STRNCASECMP(s1, s2, n) strncasecmp(s1, s2, n)
# endif /* ! (sun && !__SVR4) */
#endif /* !WINDOWS */
#if defined(_AIX) || defined(sinix)
#include <sys/types.h>
extern "C" {
	int strcasecmp(const char *, const char *);
	int strncasecmp(const char *, const char *, size_t);
}
#endif /* _AIX || sinix */

#ifdef _WIN32
#pragma pack(pop)
#endif


#endif /* !__XML_ilomemoryH */
