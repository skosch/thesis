// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilocollexpr/ilocollexpri.h
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


#ifndef __ADVANCED_ilocollexpriH
#define __ADVANCED_ilocollexpriH

#ifdef _WIN32
#pragma pack(push, 8)
#endif
#include <ilopl/ilosys.h>

#include <ilopl/iloforallbase.h>
#include <ilopl/ilotuple.h>
#include <ilopl/iloforalli.h>
#include <ilopl/ilocollexpr/ilointcollexpri.h>
#include <ilopl/ilocollexpr/ilonumcollexpri.h>
#include <ilopl/ilocollexpr/iloanycollexpri.h>


//--------------------------------------------------------------

class IloIntCollectionCardI : public IloIntExprI {
	ILOEXTRDECL
		IloIntCollectionExprArgI* _expr;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	
	IloIntCollectionCardI(IloEnvI* env, IloIntCollectionExprArgI* expr) :
	  IloIntExprI(env), _expr(expr->intLockExpr()) {}
	  virtual ~IloIntCollectionCardI();
	
	  void display(ILOSTD(ostream)& out) const;
	
	  virtual IloNum eval(const IloAlgorithm alg) const;
	
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	
	  IloIntExprI* getExpr() const { return _expr; }
};


class IloIntCollectionMinI : public IloIntExprI {
	ILOEXTRDECL
		IloIntCollectionExprArgI* _expr;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	
	IloIntCollectionMinI(IloEnvI* env, IloIntCollectionExprArgI* expr) :
	  IloIntExprI(env), _expr(expr->intLockExpr()) {}
	  virtual ~IloIntCollectionMinI();
	
	  void display(ILOSTD(ostream)& out) const;
	
	  virtual IloNum eval(const IloAlgorithm alg) const;
	
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	
	  IloIntExprI* getExpr() const { return _expr; }
};


class IloIntCollectionMaxI : public IloIntExprI {
	ILOEXTRDECL
		IloIntCollectionExprArgI* _expr;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	
	IloIntCollectionMaxI(IloEnvI* env, IloIntCollectionExprArgI* expr) :
	  IloIntExprI(env), _expr(expr->intLockExpr()) {}
	  virtual ~IloIntCollectionMaxI();
	
	  void display(ILOSTD(ostream)& out) const;
	
	  virtual IloNum eval(const IloAlgorithm alg) const;
	
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	
	  IloIntExprI* getExpr() const { return _expr; }
};

//-----------------------------------------------------------------------


class IloNumCollectionCardI : public IloIntExprI {
	ILOEXTRDECL
		IloNumCollectionExprArgI* _expr;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	
	IloNumCollectionCardI(IloEnvI* env, IloNumCollectionExprArgI* expr) :
	  IloIntExprI(env), _expr(expr->lockExpr()) {}
	  virtual ~IloNumCollectionCardI();
	
	  void display(ILOSTD(ostream)& out) const;
	
	  virtual IloNum eval(const IloAlgorithm alg) const;
	
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	
	  IloNumExprI* getExpr() const { return _expr; }
};


class IloNumCollectionMinI : public IloNumExprI {
	ILOEXTRDECL
		IloNumCollectionExprArgI* _expr;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	
	IloNumCollectionMinI(IloEnvI* env, IloNumCollectionExprArgI* expr) :
	  IloNumExprI(env), _expr(expr->lockExpr()) {}
	  virtual ~IloNumCollectionMinI();
	
	  void display(ILOSTD(ostream)& out) const;
	
	  virtual IloNum eval(const IloAlgorithm alg) const;
	
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	
	  IloNumExprI* getExpr() const { return _expr; }
};


class IloNumCollectionMaxI : public IloNumExprI {
	ILOEXTRDECL
		IloNumCollectionExprArgI* _expr;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	
	IloNumCollectionMaxI(IloEnvI* env, IloNumCollectionExprArgI* expr) :
	  IloNumExprI(env), _expr(expr->lockExpr()) {}
	  virtual ~IloNumCollectionMaxI();
	
	  void display(ILOSTD(ostream)& out) const;
	
	  virtual IloNum eval(const IloAlgorithm alg) const;
	
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	
	  IloNumExprI* getExpr() const { return _expr; }
};


//------------------------------------------


class IloSymbolCollectionCardI : public IloIntExprI {
	ILOEXTRDECL
		IloSymbolCollectionExprArgI* _expr;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	
	IloSymbolCollectionCardI(IloEnvI* env, IloSymbolCollectionExprArgI* expr) :
	  IloIntExprI(env), _expr((IloSymbolCollectionExprArgI*)expr->lockExpr()) {}
	  virtual ~IloSymbolCollectionCardI();
	
	  void display(ILOSTD(ostream)& out) const;
	
	  virtual IloNum eval(const IloAlgorithm alg) const;
	
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	
	  IloSymbolCollectionExprArgI* getExpr() const { return _expr; }
};




class IloIntCollectionExprMemberI : public IloConstraintI {
	ILOEXTRDECL
private:
	IloIntExprI* _exp;
	IloIntCollectionExprArgI* _coll;
public:
	
	IloIntCollectionExprMemberI(IloEnvI* env, IloIntExprI* exp, IloIntCollectionExprArgI* coll);
	
	virtual ~IloIntCollectionExprMemberI();
	
	void visitSubExtractables(IloExtractableVisitor* v);
	
	IloIntExprI* getExpr() const { return _exp; }
	
	IloIntCollectionExprArgI* getCollection() const { return _coll; }
	ILOEXTROTHERDECL
};


class IloNumCollectionExprMemberI : public IloConstraintI {
	ILOEXTRDECL
private:
	IloNumExprI* _exp;
	IloNumCollectionExprArgI* _coll;
public:
	
	IloNumCollectionExprMemberI(IloEnvI* env, IloNumExprI* exp, IloNumCollectionExprArgI* coll);
	
	virtual ~IloNumCollectionExprMemberI();
	
	void visitSubExtractables(IloExtractableVisitor* v);
	
	IloNumExprI* getExpr() const { return _exp; }
	
	IloNumCollectionExprArgI* getCollection() const { return _coll; }
	ILOEXTROTHERDECL
};



class IloSymbolCollectionExprMemberI : public IloConstraintI {
	ILOEXTRDECL
private:
	IloSymbolExprI* _exp;
	IloSymbolCollectionExprArgI* _coll;
public:
	
	IloSymbolCollectionExprMemberI(IloEnvI* env, IloSymbolExprI* exp, IloSymbolCollectionExprArgI* coll);
	virtual ~IloSymbolCollectionExprMemberI();
	
	void visitSubExtractables(IloExtractableVisitor* v);
	
	IloAnyExprI* getExpr() const { return _exp; }
	
	IloSymbolCollectionExprArgI* getCollection() const { return _coll; }
	ILOEXTROTHERDECL
};


//

class IloIntCollectionOrdI : public IloIntExprI {
  ILOEXTRDECL
  IloIntCollectionExprI* _coll;
  IloIntExprI* _expr;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloIntCollectionOrdI(IloEnvI* env, IloIntCollectionExprI* coll, IloIntExprI* expr) :
    IloIntExprI(env), _coll(coll->intLockExpr()), _expr(expr->intLockExpr()) {}
  virtual ~IloIntCollectionOrdI();
  void display(ILOSTD(ostream)& out) const;
  virtual IloNum eval(const IloAlgorithm alg) const;
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  IloIntCollectionExprI* getCollection() const { return _coll; }
  IloIntExprI* getExpr() const { return _expr; }
};


class IloNumCollectionOrdI : public IloIntExprI {
  ILOEXTRDECL
  IloNumCollectionExprI* _coll;
  IloNumExprI* _expr;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloNumCollectionOrdI(IloEnvI* env, IloNumCollectionExprI* coll, IloNumExprI* expr) :
    IloIntExprI(env), _coll(coll->lockExpr()), _expr(expr->lockExpr()) {}
  virtual ~IloNumCollectionOrdI();
  void display(ILOSTD(ostream)& out) const;
  virtual IloNum eval(const IloAlgorithm alg) const;
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  IloNumCollectionExprI* getCollection() const { return _coll; }
  IloNumExprI* getExpr() const { return _expr; }
};



class IloSymbolCollectionOrdI : public IloIntExprI {
  ILOEXTRDECL
  IloSymbolCollectionExprI* _coll;
  IloSymbolExprI* _expr;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloSymbolCollectionOrdI(IloEnvI* env, IloSymbolCollectionExprI* coll, IloAnyExprI* expr) :
    IloIntExprI(env), _coll((IloSymbolCollectionExprI*)coll->lockExpr()), _expr((IloSymbolExprI*)expr->lockExpr()) {}
  virtual ~IloSymbolCollectionOrdI();
  void display(ILOSTD(ostream)& out) const;
  virtual IloNum eval(const IloAlgorithm alg) const;
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  IloSymbolCollectionExprI* getCollection() const { return _coll; }
  IloSymbolExprI* getExpr() const { return _expr; }
};





class IloIntCollectionFirstI : public IloIntExprI {
	ILOEXTRDECL
	IloIntCollectionExprI* _expr;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	
	IloIntCollectionFirstI(IloEnvI* env, IloIntCollectionExprI* expr) :
	  IloIntExprI(env), _expr(expr->intLockExpr()) {}
	  virtual ~IloIntCollectionFirstI();
	
	  void display(ILOSTD(ostream)& out) const;
	
	  virtual IloNum eval(const IloAlgorithm alg) const;
	
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	
	  IloIntCollectionExprArgI* getExpr() const { return _expr; }
};


class IloNumCollectionFirstI : public IloNumExprI {
	ILOEXTRDECL
	IloNumCollectionExprI* _expr;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	
	IloNumCollectionFirstI(IloEnvI* env, IloNumCollectionExprI* expr) :
	  IloNumExprI(env), _expr(expr->lockExpr()) {}
	  virtual ~IloNumCollectionFirstI();
	
	  void display(ILOSTD(ostream)& out) const;
	
	  virtual IloNum eval(const IloAlgorithm alg) const;
	
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	
	  IloNumCollectionExprArgI* getExpr() const { return _expr; }
};






class IloIntCollectionLastI : public IloIntExprI {
	ILOEXTRDECL
	IloIntCollectionExprI* _expr;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	
	IloIntCollectionLastI(IloEnvI* env, IloIntCollectionExprI* expr) :
	  IloIntExprI(env), _expr(expr->intLockExpr()) {}
	  virtual ~IloIntCollectionLastI();
	
	  void display(ILOSTD(ostream)& out) const;
	
	  virtual IloNum eval(const IloAlgorithm alg) const;
	
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	
	  IloIntCollectionExprArgI* getExpr() const { return _expr; }
};


class IloNumCollectionLastI : public IloNumExprI {
	ILOEXTRDECL
	IloNumCollectionExprI* _expr;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	
	IloNumCollectionLastI(IloEnvI* env, IloNumCollectionExprI* expr) :
	  IloNumExprI(env), _expr(expr->lockExpr()) {}
	  virtual ~IloNumCollectionLastI();
	
	  void display(ILOSTD(ostream)& out) const;
	
	  virtual IloNum eval(const IloAlgorithm alg) const;
	
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
	
	  IloNumCollectionExprArgI* getExpr() const { return _expr; }
};




class IloSymbolCollectionFirstI : public IloSymbolExprI {
	ILOEXTRDECL
private:
	IloSymbolCollectionExprI* _expr;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloSymbolCollectionFirstI(IloEnvI* env, IloSymbolCollectionExprI* expr):
	  IloSymbolExprI(env), _expr((IloSymbolCollectionExprI*)expr->lockExpr()) {}
	  virtual ~IloSymbolCollectionFirstI();
	  void display(ILOSTD(ostream)& out) const;
	  IloSymbolCollectionExprI* getExpr() const { return _expr; }
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
};




class IloSymbolCollectionLastI : public IloSymbolExprI {
	ILOEXTRDECL
private:
	IloSymbolCollectionExprI* _expr;
	virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
	IloSymbolCollectionLastI(IloEnvI* env, IloSymbolCollectionExprI* expr):
	  IloSymbolExprI(env), _expr((IloSymbolCollectionExprI*)expr->lockExpr()) {}
	  virtual ~IloSymbolCollectionLastI();
	  void display(ILOSTD(ostream)& out) const;
	  IloSymbolCollectionExprI* getExpr() const { return _expr; }
	  virtual IloExtractableI* makeClone(IloEnvI*) const;
};





//---------------------------------------------------------------------------
//   IloIntCollectionUnion
//---------------------------------------------------------------------------

class IloIntCollectionUnionI : public IloIntCollectionExprI {
  ILOEXTRDECL
private:
  IloIntCollectionExprI* _left;
  IloIntCollectionExprI* _right;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloIntCollectionUnionI(IloEnvI* env, IloIntCollectionExprI* left, IloIntCollectionExprI* right);
  virtual ~IloIntCollectionUnionI();
  virtual IloNum eval(const IloAlgorithm alg) const;
  IloIntCollectionExprI* getLeft() const { return _left;}
  IloIntCollectionExprI* getRight() const { return _right;}
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  void display(ILOSTD(ostream)& out) const;
};

//---------------------------------------------------------------------------
//   IloNumCollectionUnion
//---------------------------------------------------------------------------

class IloNumCollectionUnionI : public IloNumCollectionExprI {
  ILOEXTRDECL
private:
  IloNumCollectionExprI* _left;
  IloNumCollectionExprI* _right;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloNumCollectionUnionI(IloEnvI* env, IloNumCollectionExprI* left, IloNumCollectionExprI* right);
  virtual ~IloNumCollectionUnionI();
  virtual IloNum eval(const IloAlgorithm alg) const;
  IloNumCollectionExprI* getLeft() const { return _left;}
  IloNumCollectionExprI* getRight() const { return _right;}
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  void display(ILOSTD(ostream)& out) const;
};
//---------------------------------------------------------------------------
//   IloSymbolCollectionUnion
//---------------------------------------------------------------------------

class IloSymbolCollectionUnionI : public IloSymbolCollectionExprI {
  ILOEXTRDECL
private:
  IloSymbolCollectionExprI* _left;
  IloSymbolCollectionExprI* _right;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloSymbolCollectionUnionI(IloEnvI* env, IloSymbolCollectionExprI* left, IloSymbolCollectionExprI* right);
  virtual ~IloSymbolCollectionUnionI();
  IloSymbolCollectionExprI* getLeft() const { return _left;}
  IloSymbolCollectionExprI* getRight() const { return _right;}
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  void display(ILOSTD(ostream)& out) const;
};



//---------------------------------------------------------------------------
//   IloIntCollectionSymExclude
//---------------------------------------------------------------------------

class IloIntCollectionSymExcludeI : public IloIntCollectionExprI {
  ILOEXTRDECL
private:
  IloIntCollectionExprI* _left;
  IloIntCollectionExprI* _right;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloIntCollectionSymExcludeI(IloEnvI* env, IloIntCollectionExprI* left, IloIntCollectionExprI* right);
  virtual ~IloIntCollectionSymExcludeI();
  virtual IloNum eval(const IloAlgorithm alg) const;
  IloIntCollectionExprI* getLeft() const { return _left;}
  IloIntCollectionExprI* getRight() const { return _right;}
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  void display(ILOSTD(ostream)& out) const;
};

//---------------------------------------------------------------------------
//   IloNumCollectionSymExclude
//---------------------------------------------------------------------------

class IloNumCollectionSymExcludeI : public IloNumCollectionExprI {
  ILOEXTRDECL
private:
  IloNumCollectionExprI* _left;
  IloNumCollectionExprI* _right;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloNumCollectionSymExcludeI(IloEnvI* env, IloNumCollectionExprI* left, IloNumCollectionExprI* right);
  virtual ~IloNumCollectionSymExcludeI();
  virtual IloNum eval(const IloAlgorithm alg) const;
  IloNumCollectionExprI* getLeft() const { return _left;}
  IloNumCollectionExprI* getRight() const { return _right;}
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  void display(ILOSTD(ostream)& out) const;
};
//---------------------------------------------------------------------------
//   IloSymbolCollectionSymExclude
//---------------------------------------------------------------------------

class IloSymbolCollectionSymExcludeI : public IloSymbolCollectionExprI {
  ILOEXTRDECL
private:
  IloSymbolCollectionExprI* _left;
  IloSymbolCollectionExprI* _right;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloSymbolCollectionSymExcludeI(IloEnvI* env, IloSymbolCollectionExprI* left, IloSymbolCollectionExprI* right);
  virtual ~IloSymbolCollectionSymExcludeI();
  IloSymbolCollectionExprI* getLeft() const { return _left;}
  IloSymbolCollectionExprI* getRight() const { return _right;}
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  void display(ILOSTD(ostream)& out) const;
};


//---------------------------------------------------------------------------
//   IloIntCollectionExclude
//---------------------------------------------------------------------------

class IloIntCollectionExcludeI : public IloIntCollectionExprI {
  ILOEXTRDECL
private:
  IloIntCollectionExprI* _left;
  IloIntCollectionExprI* _right;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloIntCollectionExcludeI(IloEnvI* env, IloIntCollectionExprI* left, IloIntCollectionExprI* right);
  virtual ~IloIntCollectionExcludeI();
  virtual IloNum eval(const IloAlgorithm alg) const;
  IloIntCollectionExprI* getLeft() const { return _left;}
  IloIntCollectionExprI* getRight() const { return _right;}
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  void display(ILOSTD(ostream)& out) const;
};

//---------------------------------------------------------------------------
//   IloNumCollectionExclude
//---------------------------------------------------------------------------

class IloNumCollectionExcludeI : public IloNumCollectionExprI {
  ILOEXTRDECL
private:
  IloNumCollectionExprI* _left;
  IloNumCollectionExprI* _right;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloNumCollectionExcludeI(IloEnvI* env, IloNumCollectionExprI* left, IloNumCollectionExprI* right);
  virtual ~IloNumCollectionExcludeI();
  virtual IloNum eval(const IloAlgorithm alg) const;
  IloNumCollectionExprI* getLeft() const { return _left;}
  IloNumCollectionExprI* getRight() const { return _right;}
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  void display(ILOSTD(ostream)& out) const;
};
//---------------------------------------------------------------------------
//   IloSymbolCollectionExclude
//---------------------------------------------------------------------------

class IloSymbolCollectionExcludeI : public IloSymbolCollectionExprI {
  ILOEXTRDECL
private:
  IloSymbolCollectionExprI* _left;
  IloSymbolCollectionExprI* _right;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloSymbolCollectionExcludeI(IloEnvI* env, IloSymbolCollectionExprI* left, IloSymbolCollectionExprI* right);
  virtual ~IloSymbolCollectionExcludeI();
  IloSymbolCollectionExprI* getLeft() const { return _left;}
  IloSymbolCollectionExprI* getRight() const { return _right;}
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  void display(ILOSTD(ostream)& out) const;
};



//---------------------------------------------------------------------------
//   IloIntCollectionInter
//---------------------------------------------------------------------------

class IloIntCollectionInterI : public IloIntCollectionExprI {
  ILOEXTRDECL
private:
  IloIntCollectionExprI* _left;
  IloIntCollectionExprI* _right;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloIntCollectionInterI(IloEnvI* env, IloIntCollectionExprI* left, IloIntCollectionExprI* right);
  virtual ~IloIntCollectionInterI();
  virtual IloNum eval(const IloAlgorithm alg) const;
  IloIntCollectionExprI* getLeft() const { return _left;}
  IloIntCollectionExprI* getRight() const { return _right;}
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  void display(ILOSTD(ostream)& out) const;
};

//---------------------------------------------------------------------------
//   IloNumCollectionInter
//---------------------------------------------------------------------------

class IloNumCollectionInterI : public IloNumCollectionExprI {
  ILOEXTRDECL
private:
  IloNumCollectionExprI* _left;
  IloNumCollectionExprI* _right;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloNumCollectionInterI(IloEnvI* env, IloNumCollectionExprI* left, IloNumCollectionExprI* right);
  virtual ~IloNumCollectionInterI();
  virtual IloNum eval(const IloAlgorithm alg) const;
  IloNumCollectionExprI* getLeft() const { return _left;}
  IloNumCollectionExprI* getRight() const { return _right;}
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  void display(ILOSTD(ostream)& out) const;
};
//---------------------------------------------------------------------------
//   IloSymbolCollectionInter
//---------------------------------------------------------------------------

class IloSymbolCollectionInterI : public IloSymbolCollectionExprI {
  ILOEXTRDECL
private:
  IloSymbolCollectionExprI* _left;
  IloSymbolCollectionExprI* _right;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloSymbolCollectionInterI(IloEnvI* env, IloSymbolCollectionExprI* left, IloSymbolCollectionExprI* right);
  virtual ~IloSymbolCollectionInterI();
  IloSymbolCollectionExprI* getLeft() const { return _left;}
  IloSymbolCollectionExprI* getRight() const { return _right;}
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  void display(ILOSTD(ostream)& out) const;
};




class IloIntCollectionAsNumCollectionI : public IloNumCollectionExprI {
  ILOEXTRDECL
private:
  IloIntCollectionExprI* _expr;
  virtual void visitSubExtractables(IloExtractableVisitor* v);
public:
  IloIntCollectionAsNumCollectionI(IloEnvI* env, IloIntCollectionExprI* left);
  virtual ~IloIntCollectionAsNumCollectionI();
  virtual IloNum eval(const IloAlgorithm alg) const;
  IloIntCollectionExprI* getExpr() const { return _expr;}
  virtual IloExtractableI* makeClone(IloEnvI*) const;
  void display(ILOSTD(ostream)& out) const;
};





class IloIntCollectionExprSubsetI : public IloConstraintI {
	ILOEXTRDECL
private:
	IloIntCollectionExprArgI* _slice;
	IloIntCollectionExprArgI* _coll;
	IloBool _eq;
public:
	
	IloIntCollectionExprSubsetI(IloEnvI* env, IloIntCollectionExprArgI* exp, IloIntCollectionExprArgI* coll, IloBool eq);
	
	virtual ~IloIntCollectionExprSubsetI();
	
	void visitSubExtractables(IloExtractableVisitor* v);
	
	IloIntCollectionExprArgI* getSlice() const { return _slice; }
	
	IloIntCollectionExprArgI* getCollection() const { return _coll; }
	IloBool isSubSetEq() const { return _eq; }
	ILOEXTROTHERDECL
};


class IloNumCollectionExprSubsetI : public IloConstraintI {
	ILOEXTRDECL
private:
	IloNumCollectionExprArgI* _slice;
	IloNumCollectionExprArgI* _coll;
	IloBool _eq;
public:
	
	IloNumCollectionExprSubsetI(IloEnvI* env, IloNumCollectionExprArgI* exp, IloNumCollectionExprArgI* coll, IloBool eq);
	
	virtual ~IloNumCollectionExprSubsetI();
	
	void visitSubExtractables(IloExtractableVisitor* v);
	
	IloNumCollectionExprArgI* getSlice() const { return _slice; }
	
	IloNumCollectionExprArgI* getCollection() const { return _coll; }
	IloBool isSubSetEq() const { return _eq; }
	ILOEXTROTHERDECL
};



class IloSymbolCollectionExprSubsetI : public IloConstraintI {
	ILOEXTRDECL
private:
	IloSymbolCollectionExprArgI* _slice;
	IloSymbolCollectionExprArgI* _coll;
	IloBool _eq;
public:
	
	IloSymbolCollectionExprSubsetI(IloEnvI* env, IloSymbolCollectionExprArgI* exp, IloSymbolCollectionExprArgI* coll, IloBool eq);
	virtual ~IloSymbolCollectionExprSubsetI();
	
	void visitSubExtractables(IloExtractableVisitor* v);
	
	IloSymbolCollectionExprArgI* getSlice() const { return _slice; }
	
	IloSymbolCollectionExprArgI* getCollection() const { return _coll; }
	IloBool isSubSetEq() const { return _eq; }
	ILOEXTROTHERDECL
};


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
