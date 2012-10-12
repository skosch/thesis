// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilconcert/ilxml/ilomacros.h
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

#ifndef __XML_ilomacrosH
#define __XML_ilomacrosH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


/* Version is 1.0 */
#if !defined(IlogLibVersion)
#define IlogLibVersion 100
#endif /* !IloXmlLibVersion */

#if defined(SYSV) && !defined(__SVR4)
#define __SVR4
#endif /* SYSV && !__SVR4 */

#if !defined(ILOXML_NOPRAGMA) && defined(_MSC_VER) && (_MSC_VER >= 1200)
#pragma warning( disable : 4291 )
#endif /* VC 6 */

#if defined(__hpux) && defined(__HP_aCC) && defined(_HP_NAMESPACE_STD) && !defined(IL_STD)
# define IL_STD
#endif /* HP aC++ && _HP_NAMESPACE_STD */
#if defined(linux) && (__GNUC__ >= 3) && !defined(IL_STD)
#define IL_STD
#endif /* linux gcc3 */

/* Windows 32 flags */
#if defined(_WIN32) || defined(__FLAT__)
/* MS/Windows 32 bits */
#  if !defined(WIN32)
#    define WIN32
#  endif /* !WIN32 */
#  if !defined(WINDOWS)
#    define WINDOWS
#  endif /* !WINDOWS */
#  if !defined(ILOXML_FATFILOXMLENAMES)
#    define ILOXML_FATFILOXMLENAMES
#  endif /* ILOXML_FATFILOXMLENAMES */
#endif /* _WIN32 || __FLAT__*/

#if defined(_WIN64)
/* MS/Windows 64 bits */
#  if !defined(WIN64)
#    define WIN64
#  endif /* !WIN64 */
#  if !defined(WINDOWS)
#    define WINDOWS
#  endif /* !WINDOWS */
#  if !defined(ILOXML_FATFILOXMLENAMES)
#    define ILOXML_FATFILOXMLENAMES
#  endif /* ILOXML_FATFILOXMLENAMES */
#endif /* _WIN64 */

#if defined(ILOXML_MAINWIN) && defined(ILOXML_FATFILOXMLENAMES)
#undef ILOXML_FATFILOXMLENAMES
#endif /* ILOXML_MAINWIN && ILOXML_FATFILOXMLENAMES */

#if !defined(ILOXML_CALLBACK)
#  define ILOXML_CALLBACK
#endif /* !ILOXML_CALLBACK */
#if !defined(ILOXML_HUGE)
#  define ILOXML_HUGE
#endif /* !ILOXML_HUGE */

/* --------------------------------------------------------------------------
// Debug macros
// --------------------------------------------------------------------------*/
#if defined(ILOXML_DEBUG)
#define ILOXML_ASSERT(cond, expr) { if (!(cond)) { expr; } }
#define ILOXML_TRACE(expr) { expr; }
#else  /* !ILOXML_DEBUG */
#define ILOXML_ASSERT(cond, expr)
#define ILOXML_TRACE(expr)
#endif /* !ILOXML_DEBUG */

#if (defined(__alpha) && !defined(WIN32)) || (defined(__hpux) && defined(__LP64__)) || (defined(__sparc) && defined(__sparcv9))
typedef int IloXmlInt;
typedef unsigned int IloXmlUInt;
#else  /* !(alphaosf || hp64 || ultrasparc64) */
typedef long IloXmlInt;
typedef unsigned long IloXmlUInt;
#endif /* !(alphaosf || hp64) */

typedef void* IloXmlAny;
typedef short IloXmlShort;
typedef unsigned short IloXmlUShort;
typedef unsigned char IloXmlUChar;

#if !defined(__IloXmlBOOLEAN)
#define __IloXmlBOOLEAN
#if defined(__GNUC__) || defined(WINDOWS)
typedef bool IloXmlBoolean;
#define IloXmlFalse false
#define IloXmlTrue	true
#else  /* !__GNUC__ && !WIN32 */
#if (defined(__alpha) && !defined(WINDOWS)) || (defined(__hpux) && defined(__LP64__) || (defined(__sparc) && defined(__sparcv9)))
typedef short IloXmlBoolean;
#else  /* !(alphaosf || hp64 || ultrasparc64) */
typedef int IloXmlBoolean;
#endif /* !(alphaosf || hp64 || ultrasparc64) */
#define IloXmlFalse ((IloXmlBoolean)0)
#define IloXmlTrue  ((IloXmlBoolean)1)
#endif /* !__GNUC__ && !WINDOWS */
#endif /* !__IloXmlBOOLEAN */
#if !defined(__IloXmlFloat)
#define __IloXmlFloat
typedef float IloXmlFloat;
typedef double IloXmlDouble;
#endif /* __IloXmlFloat */

#if !defined(__IloXmlMIN)
#define __IloXmlMIN
#define DEFIloXmlMinMax(T) inline T IloXmlMin(T x, T y) { return (x >= y) ? y : x; }\
	inline T IloXmlMax(T x, T y) { return (x >= y) ? x : y; }
#define DEFIloXmlAbs(T)	inline T IloXmlAbs(T x) { return (x >= 0) ? x : (T)(-x); }

DEFIloXmlMinMax(int)
DEFIloXmlAbs(int)
DEFIloXmlMinMax(unsigned int)

DEFIloXmlMinMax(long)
DEFIloXmlAbs(long)
DEFIloXmlMinMax(unsigned long)
DEFIloXmlMinMax(short)
DEFIloXmlMinMax(unsigned short)
DEFIloXmlAbs(short)
DEFIloXmlMinMax(IloXmlFloat)
DEFIloXmlAbs(IloXmlFloat)
DEFIloXmlMinMax(IloXmlDouble)
DEFIloXmlAbs(IloXmlDouble)
#endif /* !__IloXmlMIN */

#if defined(sun) && defined(ILOXML_SUNCC2_1)
#define ILOXML_VOLATILOXMLE
#endif /* Sun C++ 2.1 */
#if !defined(ILOXML_VOLATILOXMLE)
#define ILOXML_VOLATILOXMLE volatile
#endif /* !ILOXML_VOLATILOXMLE */

/* ILOXML_STRING */
#if defined(__BORLANDC__) || defined(__GNUC__) || defined(_MSC_VER)
#define ILOXML_STRING(name) ""#name""
#endif /* defined(__BORLANDC__) || defined(__GNUC__) || defined(_MSC_VER) */
#if defined(sun) && defined(ILOXML_SUNCC2_1)
#define ILOXML_STRING(name) "name"
#endif /* sparc_4_2.1 */
#if !defined(ILOXML_STRING)
#define ILOXML_STRING(name) #name
#endif /* !defined(ILOXML_STRING) */

/* --------------------------------------------------------------------------
// Handling casts
// --------------------------------------------------------------------------*/
#if (defined(__hpux) && (__cplusplus >= 199707L)) || defined(_CPPRTTI)
#define ILOXML_RTTI
#endif /* HP aC++ || _CPPRTTI */
#if defined(__BORLANDC__) && (__BORLANDC__ >= 0x0530) && !defined(IL_STD)
#define IL_STD
#endif /* Borland C++ >= 3.0 */
#if defined(__SUNPRO_CC_COMPAT) && (__SUNPRO_CC_COMPAT >= 5) && !defined(IL_STD)
#define IL_STD
#endif /* Sun C++ >= 5.0 */
#if defined(IL_STD)
#define ILOXML_CAST(type, expr) static_cast<type>(expr)
#define ILOXML_UNSAFECAST(type, expr) ((type)(IloXmlAny)(expr))
#if defined(_MSC_VER) && (_MSC_VER >= 1200) && !defined(_CPPRTTI)
#define ILOXML_DYNAMICCAST(type, expr) ILOXML_CAST(type, expr)
#else  /* !VisualC++ 6 */
#define ILOXML_DYNAMICCAST(type, expr) dynamic_cast<type>(expr)
#endif /* !VisualC++ 6 */
#define ILOXML_CONSTCAST(type, expr) const_cast<type>(expr)
#define ILOXML_REINTERPRETCAST(type, expr) reinterpret_cast<type>(expr)
#elif defined(ILOXML_RTTI) /* !IL_STD */
#define ILOXML_CAST(type, expr) ((type)(expr))
#define ILOXML_UNSAFECAST(type, expr) ((type)(IloXmlAny)(expr))
#define ILOXML_DYNAMICCAST(type, expr) dynamic_cast<type>(expr)
#define ILOXML_CONSTCAST(type, expr) ((type)(expr))
#define ILOXML_REINTERPRETCAST(type, expr) ILOXML_CAST(type, expr)
#else  /* !IL_STD && !ILOXML_RTTI */
#define ILOXML_CAST(type, expr) ((type)(expr))
#define ILOXML_UNSAFECAST(type, expr) ((type)(expr))
#define ILOXML_DYNAMICCAST(type, expr) ((type)(expr))
#define ILOXML_CONSTCAST(type, expr) ((type)(expr))
#define ILOXML_REINTERPRETCAST(type, expr) ILOXML_CAST(type, expr)
#endif /* !IL_STD && !ILOXML_RTTI */

#if (defined(_MSC_VER) && (_MSC_VER >= 1100)) || defined(__BORLANDC__) || (defined(__hpux) && (__cplusplus >= 199707L))
#define ILOXML_EXPLICIT explicit
#define ILOXML_MUTABLE  mutable
#define ILOXML_ACCESSMUTABLE(class, field) field
#else  /* !_MSC_VER && !__BORLANDC__ && !(__hpux && __cplusplus) */
#define ILOXML_EXPLICIT
#define ILOXML_MUTABLE
#define ILOXML_ACCESSMUTABLE(class, field) ILOXML_CONSTCAST(class*, this)->field
#endif /* !_MSC_VER && !__BORLANDC__ && !(__hpux && __cplusplus) */

#if defined(__BORLANDC__) && (__BORLANDC__ >= 0x0530)
#define __USELOCALES__
#endif

/* For new C++ standard (3.0). */
#if defined(IL_STD)
#  define IL_STDPREF std::
#  define IL_STDUSE using namespace std;
#else
#  if defined(__SUNPRO_CC_COMPAT) && (__SUNPRO_CC_COMPAT >= 5)
#    define IL_STDPREF
#  else
#    define IL_STDPREF ::
#  endif
#  define IL_STDUSE
#endif

#define ILOXML_EMPTYMACRO

#if defined(__SUNPRO_CC_COMPAT) && (__SUNPRO_CC_COMPAT >= 5)
#define ILOXML_DEFINEFPTRTOANYCAST(FUNCTYPE) \
struct FUNCTYPE##Cast {				\
	FUNCTYPE##Cast(IloXmlAny a)    { v.a = a; }	\
	FUNCTYPE##Cast(FUNCTYPE f) { v.f = f; }	\
union { FUNCTYPE f; IloXmlAny a; } v;		\
	operator IloXmlAny()    const { return v.a; }	\
	operator FUNCTYPE() const { return v.f; } }
#else
#define ILOXML_DEFINEFPTRTOANYCAST(FUNCTYPE) \
	typedef FUNCTYPE FUNCTYPE##Cast
#endif
#define ILOXML_FPTRTOANYCAST(FUNCTYPE) FUNCTYPE##Cast

/* --------------------------------------------------------------------------
// Macros for static initializations                                         */
#if defined(ILOXML_BUILOXMLDLIBS)
#define ILOXML_MODULEINIT(m, e) class e ILOXML_ICDECL(m)\
{ public: ILOXML_ICDECL(m)(); static int c; }; \
	extern "C" ILO_EXPORTFUNCTION(void, ILOXML_EMPTYMACRO e) ILOXML_MODINIT(m)()
#define ILOXML_MODULETERM(m, e) extern "C" {\
	ILO_EXPORTFUNCTION(void, ILOXML_EMPTYMACRO e) ILOXML_MODINIT(m)();\
	ILO_EXPORTFUNCTION(void, ILOXML_EMPTYMACRO e) ILOXML_MODTERM(m)();\
}\
class e ILOXML_ICDECL(m){\
public: ILOXML_ICDECL(m)(); ~ILOXML_ICDECL(m)(); static int c;}
#else  /* !ILOXML_BUILOXMLDLIBS */
#define ILOXML_MODULEINIT(m, e) \
	extern "C" ILO_EXPORTFUNCTION(void, ILOXML_EMPTYMACRO e) ILOXML_MODINIT(m)();\
	static class e ILOXML_ICDECL(m)\
{\
public:\
	ILOXML_ICDECL(m)();\
	static int c;\
} ILOXML_MODPREFIX(m)
#define ILOXML_MODULETERM(m, e) extern "C" {\
	ILO_EXPORTFUNCTION(void, ILOXML_EMPTYMACRO e) ILOXML_MODINIT(m)();\
	ILO_EXPORTFUNCTION(void, ILOXML_EMPTYMACRO e) ILOXML_MODTERM(m)();\
};\
	static class e ILOXML_ICDECL(m)\
{\
public:\
	ILOXML_ICDECL(m)();\
	~ILOXML_ICDECL(m)();\
	static int c;\
} ILOXML_MODPREFIX(m)
#endif /* !ILOXML_BUILOXMLDLIBS */

#define ILOXML_BEGINMODULEINITDEF(mod)	\
	void ILOXML_MODINIT(mod)(){if(!ILOXML_ICDECL(mod)::c++){
#define ILOXML_ENDMODULEINITDEF(mod)	\
}}int ILOXML_ICDECL(mod)::c=0;ILOXML_ICDECL(mod)::ILOXML_ICDECL(mod)(){ILOXML_MODINIT(mod)();}

#define ILOXML_BEGINMODULETERMDEF(mod)	\
	void ILOXML_MODTERM(mod)(){if(!--ILOXML_ICDECL(mod)::c){
#define ILOXML_ENDMODULETERMDEF(mod)	\
}}ILOXML_ICDECL(mod)::~ILOXML_ICDECL(mod)(){ILOXML_MODTERM(mod)();}

#if defined(WINDOWS)
#  if !defined(ILOXML_FATFILOXMLENAMES)
#    define ILOXML_FATFILOXMLENAMES
#  endif /* ILOXML_FATFILOXMLENAMES */
#endif

// for dll macros
#ifndef __CONCERT_ilosysH
#include <ilconcert/ilosys.h>
#endif

/* Static initialization macros */
#define ILOGXMLMODULEINIT(m) ILOXML_MODULEINIT(m, ILO_EXPORTED)
#define ILOGXMLMODULETERM(m) ILOXML_MODULETERM(m, ILO_EXPORTED)

#if !defined(ILOXML_PTRCASTS) && defined(__cplusplus)
#  define ILOXML_PTRCASTS
#  if defined(WIN64) || (defined(_MSC_VER) && (_MSC_VER >= 1300)) /* intptr_t */
#    if defined(IL_STD)
#    include <cstddef>
#    else  /* !IL_STD */
#    include <stddef.h>
#    endif /* !IL_STD */
typedef intptr_t	IloXmlIntPtr;
typedef uintptr_t	IloXmlUIntPtr;
#  else /* !intptr_t */
typedef long		IloXmlIntPtr;
typedef unsigned long	IloXmlUIntPtr;
#  endif /* !intptr_t */

inline IloXmlIntPtr
IloXmlCastIloXmlAnyToIloXmlIntPtr(IloXmlAny v) 
{
	return ILOXML_REINTERPRETCAST(IloXmlIntPtr, v);
}

inline IloXmlUIntPtr
IloXmlCastIloXmlAnyToIloXmlUIntPtr(IloXmlAny v) 
{
	return ILOXML_REINTERPRETCAST(IloXmlUIntPtr, v);
}

inline IloXmlAny
IloXmlCastIloXmlIntPtrToIloXmlAny(IloXmlIntPtr v) 
{
	return ILOXML_REINTERPRETCAST(IloXmlAny, v);
}

inline IloXmlAny
IloXmlCastIloXmlUIntPtrToIloXmlAny(IloXmlUIntPtr v) 
{
	return ILOXML_REINTERPRETCAST(IloXmlAny, v);
}

inline char
IloXmlCastIloXmlAnyToChar(IloXmlAny v)
{
	return ILOXML_CAST(char, IloXmlCastIloXmlAnyToIloXmlIntPtr(v));
}

inline IloXmlUChar
IloXmlCastIloXmlAnyToIloXmlUChar(IloXmlAny v)
{
	return ILOXML_CAST(IloXmlUChar, IloXmlCastIloXmlAnyToIloXmlUIntPtr(v));
}

inline IloXmlShort
IloXmlCastIloXmlAnyToIloXmlShort(IloXmlAny v)
{
	return ILOXML_CAST(IloXmlShort, IloXmlCastIloXmlAnyToIloXmlIntPtr(v));
}

inline IloXmlUShort
IloXmlCastIloXmlAnyToIloXmlUShort(IloXmlAny v)
{
	return ILOXML_CAST(IloXmlUShort, IloXmlCastIloXmlAnyToIloXmlUIntPtr(v));
}

inline int
IloXmlCastIloXmlAnyToInt(IloXmlAny v)
{
	return ILOXML_CAST(int, IloXmlCastIloXmlAnyToIloXmlIntPtr(v));
}

inline IloXmlInt
IloXmlCastIloXmlAnyToIloXmlInt(IloXmlAny v)
{
	return ILOXML_CAST(IloXmlInt, IloXmlCastIloXmlAnyToIloXmlIntPtr(v));
}

inline IloXmlUInt
IloXmlCastIloXmlAnyToIloXmlUInt(IloXmlAny v)
{
	return ILOXML_CAST(IloXmlUInt, IloXmlCastIloXmlAnyToIloXmlUIntPtr(v));
}

inline IloXmlAny
IloXmlCastCharToIloXmlAny(char v)
{
	return IloXmlCastIloXmlIntPtrToIloXmlAny(ILOXML_CAST(IloXmlIntPtr, v));
}

inline IloXmlAny
IloXmlCastIloXmlUCharToIloXmlAny(IloXmlUChar v)
{
	return IloXmlCastIloXmlUIntPtrToIloXmlAny(ILOXML_CAST(IloXmlUIntPtr, v));
}

inline IloXmlAny
IloXmlCastIloXmlShortToIloXmlAny(IloXmlShort v)
{
	return IloXmlCastIloXmlIntPtrToIloXmlAny(ILOXML_CAST(IloXmlIntPtr, v));
}

inline IloXmlAny
IloXmlCastIloXmlUShortToIloXmlAny(IloXmlUShort v)
{
	return IloXmlCastIloXmlUIntPtrToIloXmlAny(ILOXML_CAST(IloXmlUIntPtr, v));
}

inline IloXmlAny
IloXmlCastIloXmlIntToIloXmlAny(IloXmlInt v)
{
	return IloXmlCastIloXmlIntPtrToIloXmlAny(ILOXML_CAST(IloXmlIntPtr, v));
}

inline IloXmlAny
IloXmlCastIntToIloXmlAny(int v)
{
	return IloXmlCastIloXmlIntPtrToIloXmlAny(ILOXML_CAST(IloXmlIntPtr, v));
}

inline IloXmlAny
IloXmlCastIloXmlUIntToIloXmlAny(IloXmlUInt v)
{
	return IloXmlCastIloXmlUIntPtrToIloXmlAny(ILOXML_CAST(IloXmlUIntPtr, v));
}

#endif /* ILOXML_PTRCASTS && __cplusplus */

#ifdef _WIN32
#pragma pack(pop)
#endif


#endif  /* __XML_ilomacrosH */
