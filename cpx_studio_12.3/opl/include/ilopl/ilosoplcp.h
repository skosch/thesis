// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilosoplcp.h
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


#ifndef __CONCERT_ilosoplcpH
#define __CONCERT_ilosoplcpH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>
# include <ilconcert/ilosmodel.h>


// Implementation classes
class IloAdvPiecewiseFunctionExprI;
class IloAdvPiecewiseFunctionI;
class IloIntervalSequenceExprI;
class IloAdvPiecewiseFunctionExprSubMapExprI;
class IloStateFunctionExprI;
class IloStateFunctionExprSubMapExprI;
//---------------------------------------------------------------------------
//   IloAdvPiecewiseFunction
//---------------------------------------------------------------------------

#define IloAdvPiecewiseFunctionExprArgI IloAdvPiecewiseFunctionExprI

#define ILOEXTR_RENAMED_HANDLE(Hname, Iname, RHname)\
public: \
  typedef Iname ImplClass; \
  \
  Hname():RHname(){} \
  \
  Hname(Iname* impl):RHname((RHname::ImplClass*)(void*)impl){} \
  \
  Iname* getImpl() const { \
    return (ImplClass*)(void*)_impl; \
  } \
  Hname getClone() const { \
    return (ImplClass*)(void*)getEnv().getImpl()->getClone(_impl); \
  } \
  void setClone(Hname clone) const { \
    (getEnv().getImpl())->setClone(_impl, clone._impl); \
  } \
  Hname getClone(const IloEnv env) const { \
    return (ImplClass*)(void*)(env.getImpl() ? env.getImpl() : getEnv().getImpl())->getClone(_impl); \
  } \
  void setClone(Hname clone, const IloEnv env) const { \
    (env.getImpl() ? env.getImpl() : getEnv().getImpl())->setClone(_impl, clone._impl); \
  } \
private:



class IloPiecewiseFunctionExpr : public IloExtractable {
  ILOEXTR_RENAMED_HANDLE(IloPiecewiseFunctionExpr,
			 IloAdvPiecewiseFunctionExprI, IloExtractable)
public:
  // Used by IloObjectBase
  static IloBool MatchTypeInfo(IloTypeInfo);
};

// handle classes needed only for maps
class IloPiecewiseFunctionExprArg : public IloPiecewiseFunctionExpr {
  ILOEXTR_RENAMED_HANDLE(IloPiecewiseFunctionExprArg,
			 IloAdvPiecewiseFunctionExprI,
			 IloPiecewiseFunctionExpr)
public:
  // Used by IloObjectBase
  static IloBool MatchTypeInfo(IloTypeInfo);
};


class IloAdvPiecewiseFunction : public IloPiecewiseFunctionExpr {
  ILOEXTRHANDLE(IloAdvPiecewiseFunction, IloPiecewiseFunctionExpr)
public:
  // Used by IloObjectBase
  static IloBool MatchTypeInfo(IloTypeInfo);
};

//typedef IloArray<IloAdvPiecewiseFunction> IloAdvPiecewiseFunctionArray;
//typedef IloArray<IloPiecewiseFunctionExpr> IloPiecewiseFunctionExprArray;

#define IloIntervalSequenceExprArgI IloIntervalSequenceExprI

// handle classes needed only for maps
class IloIntervalSequenceExprArg : public IloExtractable {
  ILOEXTRHANDLE(IloIntervalSequenceExprArg, IloExtractable)
public:
  // Used by IloObjectBase
  static IloBool MatchTypeInfo(IloTypeInfo);
};


#define IloCumulFunctionExprArgI IloCumulFunctionExprI

// handle classes needed only for maps
class IloCumulFunctionExprArg : public IloCumulFunctionExpr {
  ILOEXTRHANDLE(IloCumulFunctionExprArg, IloCumulFunctionExpr)
public:
  // Used by IloObjectBase
  static IloBool MatchTypeInfo(IloTypeInfo);
};


// handle classes needed only for maps
class IloStateFunctionExprArg : public IloStateFunctionExpr {
  ILOEXTRHANDLE(IloStateFunctionExprArg, IloStateFunctionExpr)
public:
  // Used by IloObjectBase
  static IloBool MatchTypeInfo(IloTypeInfo);
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
