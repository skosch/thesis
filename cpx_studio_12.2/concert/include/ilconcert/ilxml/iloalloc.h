// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilconcert/ilxml/iloalloc.h
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

#ifndef __XML_iloallocH
#define __XML_iloallocH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#include <ilconcert/ilxml/ilomacros.h>

#if !defined(ILOXML_SIZE_T)
#  if defined(IL_STD)
#    include <cstddef>
#  else  /* ! IL_STD */
#    include <stddef.h>
#  endif /* ! IL_STD */
#  if defined(_MSC_VER) && ((_MSC_VER < 1300) || defined(WIN64)) /* msvc6. */
#    define ILOXML_SIZE_T size_t
#else  /* !msvc6 */
#    define ILOXML_SIZE_T IL_STDPREF size_t
#endif /* !msvc6. */
#endif /* !ILOXML_SIZE_T */


#if defined(__alpha) && !defined(VMS)
#define ILOXML_MLK_ALIGNMENT        16
#define ILOXML_MLK_THRESHOLD        8
#else
#define ILOXML_MLK_ALIGNMENT        8
#define ILOXML_MLK_THRESHOLD        8
#endif

// number of multiples of the system alignment, specifying the threshold past
// which memory management is delegated to malloc


/*
* size of an elementary memory block to allocate for small objects, in number
* of small object bytes packets. Every time a block of memory is full,
* another block is allocated whose size is
* ILOXML_MLK_BLOCK*ILOXML_MLK_THRESHOLD*ILOXML_MLK_ALIGNMENT*ILOXML_MLK_MULT
*/
#define ILOXML_MLK_BLOCK            64
#define ILOXML_MLK_MULT             2

#if defined(ILOXML_MLK_STATS)
#  if defined(IL_STD)
#    include <ostream>
#  else  /* !IL_STD */
class ostream;
#  endif /* !IL_STD */
#endif /* ILOXML_MLK_STATS */

class ILO_EXPORTED IloXmlMemoryManager
{
public:
	IloXmlMemoryManager();
#if defined(WINDOWS)
	~IloXmlMemoryManager();
#endif /* WINDOWS */
	void* alloc(ILOXML_SIZE_T);
	void release(void*, ILOXML_SIZE_T);
#ifdef ILOXML_MLK_STATS
	void status(IL_STDPREF ostream &) const;
#endif
	static IloXmlMemoryManager& GetInstance();
private:
	static ILOXML_SIZE_T category(ILOXML_SIZE_T size) {
		return ((size%ILOXML_MLK_ALIGNMENT)
			? (size/ILOXML_MLK_ALIGNMENT)
			: ((size/ILOXML_MLK_ALIGNMENT)-1));
	}
	static ILOXML_SIZE_T alignedSize(ILOXML_SIZE_T size) {
		return ((size%ILOXML_MLK_ALIGNMENT)
			? ((size/ILOXML_MLK_ALIGNMENT+1)*ILOXML_MLK_ALIGNMENT)
			: size);
	}
#define ILOXML_MLK_FLOC_SIZE (ILOXML_MLK_THRESHOLD+1)
	void* _floc[ILOXML_MLK_FLOC_SIZE];
	void* _scratch;
	void* _border;
	unsigned int _blocks;
#ifdef ILOXML_MLK_STATS
	unsigned long _heap;
	unsigned long _used;
	int _totalAllocated;
	int _totalFreed;
	int _currentlyUsed;
	int _maxUsed;
#endif
	// forbidden
	void operator=(const IloXmlMemoryManager &);
	IloXmlMemoryManager(const IloXmlMemoryManager &);
};

/* static before operator delete is optional */
#define ILOXML_MLK						\
	void* operator new(ILOXML_SIZE_T s)				\
{ return IloXmlMemoryManager::GetInstance().alloc(s); }	\
	void operator delete(void* a, ILOXML_SIZE_T s)		\
{ IloXmlMemoryManager::GetInstance().release(a, s); }

#define ILOXML_MLK_DECL()				\
	void* operator new(ILOXML_SIZE_T);			\
	static void operator delete(void*, ILOXML_SIZE_T)

#define ILOXML_MLK_DEF(cl)					\
	void* cl::operator new(ILOXML_SIZE_T size)			\
{ return IloXmlMemoryManager::GetInstance().alloc(size); }	\
	void cl::operator delete(void* a, ILOXML_SIZE_T s)		\
{ IloXmlMemoryManager::GetInstance().release(a, s); }

#ifdef _WIN32
#pragma pack(pop)
#endif


#endif /* __XML_iloallocH */
