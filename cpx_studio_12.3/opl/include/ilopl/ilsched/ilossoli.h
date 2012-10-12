// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilsched/ilossoli.h
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


#ifndef __ADVANCED_ilossoliH
#define __ADVANCED_ilossoliH

#ifdef _WIN32
#pragma pack(push, 8)
#endif
#include <ilopl/ilosys.h>

#include <ilopl/ilosolution.h>
#include <ilopl/ilsched/ilosstructi.h>
#include <ilopl/ilsched/ilostimei.h>
#include <ilopl/ilsched/ilossequencei.h>
#include <ilopl/ilsched/ilosatomi.h>



// --------------------------------------------------------------------------
// class IloSavedFunctionExprI
// --------------------------------------------------------------------------

class IloSavedFunctionExprI: public IloSolutionElementI {
 public:
  IloUInt              _restoreFields;
  IloSavedPrecGraphI*  _pg;
 public:
  IloSavedFunctionExprI(IloMemoryManager m, IloExtractableI* f, IloUInt restore);
  virtual ~IloSavedFunctionExprI();

  // Accessors
  void addArc(IloExtractableI* source, IloExtractableI* target);  
  void removeAllIncoming(IloExtractableI* atom);
  void removeAllOutgoing(IloExtractableI* atom);
  void removeAllPrecedences();
  void mark(IloExtractableI* atom);
  void relink();
  IloSavedPrecGraphI* getSavedPG() const { return _pg; }
  IloBool hasEmptyPG() const { return (0==_pg) || _pg->isEmpty(); }

  // Restore fields
  IloBool isToBeRestored(IloUInt flag) const { return (_restoreFields & flag); }
  IloUInt getRestoreFields()           const { return _restoreFields; }
  IloUInt getRestoreFields(IloExtractableI* atom) const;
  void setRestoreFields(IloUInt fields);
  void setRestoreFields(IloExtractableI* atom, IloUInt fields);

  // Virtuals of IloSolutionElementI
  virtual void copy(const IloSolutionElementI *e);
  virtual IloSolutionElementI* makeClone(IloMemoryManagerI* env) const;
  virtual IloBool isEquivalent(const IloSolutionElementI* element) const;
  
  virtual void display(ILOSTD(ostream) &s) const;
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif

