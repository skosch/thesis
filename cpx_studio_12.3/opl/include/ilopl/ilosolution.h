// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilosolution.h
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


#ifndef __ADVANCED_ilosolutionH
#define __ADVANCED_ilosolutionH

#ifdef _WIN32
#pragma pack(push, 8)
#endif
//
#include <ilopl/ilosys.h>
#include <ilconcert/ilosolution.h>
#include <ilopl/ilomap.h>
#include <ilopl/ilotuplemap.h>


/////////////////////////////////////////////////////////////////////////////
// Iterators
/////////////////////////////////////////////////////////////////////////////


class IloOplSolution : public IloSolution {
public:
  using IloSolution::setValue;
  using IloSolution::add;
  using IloSolution::setMin;
  using IloSolution::setMax;
  //using IloSolution::setOptionality;
  using IloSolution::setEnd;
  using IloSolution::setEndMin;
  using IloSolution::setEndMax;
  //using IloSolution::setPresence;
  using IloSolution::setStart;
  using IloSolution::setStartMin;
  using IloSolution::setStartMax;


  IloOplSolution() : IloSolution(0) { }

  IloOplSolution(IloSolutionI *impl) : IloSolution(impl) { }

  IloOplSolution(const IloOplSolution& solution) : IloSolution(solution._impl) { }



  IloOplSolution(IloEnv mem, const char* name = 0) : IloSolution(mem, name){}


  void add(const IloIntVarMap map) const;


  void add(const IloIntervalVarMap map) const;


  void setValue(const IloIntVarMap var, const IloIntMap value) const;


  void setMin(const IloIntVarMap var, const IloIntMap min) const;


  void setMax(const IloIntVarMap var, const IloIntMap max) const;


  void setOptionality(const IloIntervalVarMap var, const IloIntMap value) const; 


  void setStart(const IloIntervalVarMap var, const IloIntMap value) const; 


  void setStartMin(const IloIntervalVarMap var, const IloIntMap min) const ;


  void setStartMax(const IloIntervalVarMap var, const IloIntMap max) const ;

  void setEnd(const IloIntervalVarMap var, const IloIntMap value) const ;


  void setEndMin(const IloIntervalVarMap var, const IloIntMap min) const ;


  void setEndMax(const IloIntervalVarMap var, const IloIntMap max) const ;


  void setPresence(const IloIntervalVarMap var, const IloTupleMap value) const ;


  void setStart(const IloIntervalVarMap var, const IloTupleMap value) const ;


  void setStartMin(const IloIntervalVarMap var, const IloTupleMap min) const ;


  void setStartMax(const IloIntervalVarMap var, const IloTupleMap max) const ;


  void setEnd(const IloIntervalVarMap var, const IloTupleMap value) const ;


  void setEndMin(const IloIntervalVarMap var, const IloTupleMap min) const ;


  void setEndMax(const IloIntervalVarMap var, const IloTupleMap max) const ;


  void setValue(const IloIntervalVarMap var, const IloTupleMap value) const; 


  void setPresence(const IloIntervalVar var, const IloTuple value) const; 


  void setStart(const IloIntervalVar var, const IloTuple value) const; 


  void setStartMin(const IloIntervalVar var, const IloTuple min) const; 


  void setStartMax(const IloIntervalVar var, const IloTuple max) const; 


  void setEnd(const IloIntervalVar var, const IloTuple value) const; 


  void setEndMin(const IloIntervalVar var, const IloTuple min) const; 

  void setEndMax(const IloIntervalVar var, const IloTuple max) const; 

  void setValue(const IloIntervalVar var, const IloTuple value) const; 
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
