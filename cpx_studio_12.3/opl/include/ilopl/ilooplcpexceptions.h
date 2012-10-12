// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilooplcpexceptions.h
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

#ifndef __ADVANCED_ilooplcpexceptionsH
#define __ADVANCED_ilooplcpexceptionsH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


//---------- Exceptions
class IloMetaConstraintNotAllowedException  : public IloException {
  const IloExtractableI* _obj;
  const IloExtractableI* _context;
public:
  IloMetaConstraintNotAllowedException(const IloExtractableI* e,
                                       const IloExtractableI* context=0);
  virtual void print(ILOSTD(ostream)& out) const;
  const IloExtractableI* getExtractable() const { return (_obj); }
  const IloExtractableI* getContext() const { return (_context); }
  void setContext(const IloExtractableI* context) { _context = context; }
};


class IloNumCollectedMapI;
//---------- Exceptions
class IloNumCollectedMapException  : public IloException {
  const IloNumCollectedMapI* _map;
  const IloExtractableI* _obj;
public:
  IloNumCollectedMapException (const IloNumCollectedMapI*, const IloExtractableI*);
  virtual void print(ILOSTD(ostream)& out) const;
  const IloNumCollectedMapI* getCollectedMap() const { return (_map); }
  const IloExtractableI* getExtractable() const { return (_obj); }
  void setContext(const IloExtractableI* context) { _obj = context; }
};

class IloNumCollectedMapOutOfBoundException  : public IloNumCollectedMapException {
  const IloObjectBase _index;
public:
  IloNumCollectedMapOutOfBoundException (const IloNumCollectedMapI*, const IloObjectBase, const IloExtractableI*);
  void print(ILOSTD(ostream)& out) const;
  const IloObjectBase getMapItem() const { return (_index); }
};

class IloNumCollectedMapUnboundException  : public IloNumCollectedMapException {
  const IloNumExprI* _subexpr;
public:
  IloNumCollectedMapUnboundException (const IloNumCollectedMapI*,
                                      const IloNumExprI* sub,
                                      const IloExtractableI*);
  void print(ILOSTD(ostream)& out) const;
  const IloNumExprI* getSubExpression() const { return (_subexpr); }
};

class IloWrongNumCollectedMapDimensionException  : public IloNumCollectedMapException {
  const IloInt _expectedNbDim;
public:
  IloWrongNumCollectedMapDimensionException (const IloNumCollectedMapI*, const IloInt, const IloExtractableI*);
  const IloInt getExpectedNbOfDimension() const { return (_expectedNbDim); }
  void print(ILOSTD(ostream)& out) const;
};

class IloNumCollectedMapRangeException  : public IloNumCollectedMapException {
public:
  IloNumCollectedMapRangeException (const IloNumCollectedMapI*);
  void print(ILOSTD(ostream)& out) const;
};

class IloNumCollectedMapMismatchSizeException : public IloNumCollectedMapException {
  const IloNumCollectedMapI* _map2;
  IloInt _size1;
  IloInt _size2;
public:
  IloNumCollectedMapMismatchSizeException (const IloNumCollectedMapI*,
					   const IloNumCollectedMapI*,
					   const IloExtractableI*);
  IloNumCollectedMapMismatchSizeException (const IloNumCollectedMapI*,
					   IloInt dim1,
					   const IloNumCollectedMapI*,
					   IloInt dim2,
					   const IloExtractableI*);
  const IloNumCollectedMapI* getSecondCollectedMap() const { return (_map2); }
  const IloInt getSize1() const { return _size1; }
  const IloInt getSize2() const { return _size2; }
  void print(ILOSTD(ostream)& out) const;
};

class IloNumCollectedMapWrongSizeException : public IloNumCollectedMapException {
  IloInt _expectedSize;
  IloInt _size;
public:
  IloNumCollectedMapWrongSizeException (const IloNumCollectedMapI*,
                                        IloInt expectedSize,
                                        const IloExtractableI*);
  IloNumCollectedMapWrongSizeException (const IloNumCollectedMapI*,
                                        IloInt expectedSize,
                                        IloInt size,
                                        const IloExtractableI*);
  const IloInt getExpectedSize() const { return _expectedSize; }
  const IloInt getSize() const { return _size; }
  void print(ILOSTD(ostream)& out) const;
};


//---------- Exceptions
class IloExtractableCollectedMapI;
class IloExtractableCollectedMapException  : public IloException {
  const IloExtractableCollectedMapI* _map;
  const IloExtractableI* _obj;
public:
  IloExtractableCollectedMapException (const IloExtractableCollectedMapI*,
				       const IloExtractableI*);
  virtual void print(ILOSTD(ostream)& out) const;
  const IloExtractableCollectedMapI* getCollectedMap() const { return (_map); }
  const IloExtractableI* getExtractable() const { return (_obj); }
  void setContext(const IloExtractableI* context) { _obj = context; }
};

class IloExtractableCollectedMapOutOfBoundException  : public IloExtractableCollectedMapException {
  const IloObjectBase _index;
public:
  IloExtractableCollectedMapOutOfBoundException(const IloExtractableCollectedMapI*,
						const IloObjectBase,
						const IloExtractableI*);
  void print(ILOSTD(ostream)& out) const;
  const IloObjectBase getMapItem() const { return (_index); }
};

class IloExtractableCollectedMapUnboundException  : public IloExtractableCollectedMapException {
  const IloExtractableI* _subexpr;
public:
  IloExtractableCollectedMapUnboundException (
                                      const IloExtractableCollectedMapI*,
                                      const IloExtractableI* sub,
                                      const IloExtractableI*);
  void print(ILOSTD(ostream)& out) const;
  const IloExtractableI* getSubExpression() const { return (_subexpr); }
};

class IloWrongExtractableCollectedMapDimensionException  : public IloExtractableCollectedMapException {
  const IloInt _expectedNbDim;
public:
  IloWrongExtractableCollectedMapDimensionException (const IloExtractableCollectedMapI*,
						     const IloInt,
						     const IloExtractableI*);
  const IloInt getExpectedNbOfDimension() const { return (_expectedNbDim); }
  void print(ILOSTD(ostream)& out) const;
};

class IloExtractableCollectedMapRangeException  : public IloExtractableCollectedMapException {
public:
  IloExtractableCollectedMapRangeException (const IloExtractableCollectedMapI*);
  void print(ILOSTD(ostream)& out) const;
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
