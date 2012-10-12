// -------------------------------------------------------------- -*- C++ -*-
// File: ilocarray.h
// Version 12.3
// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
// 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55
// Copyright IBM Corporation 2000, 2011. All Rights Reserved.
//
// US Government Users Restricted Rights - Use, duplication or
// disclosure restricted by GSA ADP Schedule Contract with
// IBM Corp.
// --------------------------------------------------------------------------

#ifndef __CONCERT_ilocarrayH
#define __CONCERT_ilocarrayH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <assert.h>
#include <stdlib.h>
#include <string.h>


// --------------------------------------------------------------------------
//
// C-style array for simple classes without constructor or destructor
// kept in memory in purely linear order; just to ease up malloc, realloc,
// and free.  The destructor will delete the data, thus they work fine with
// exceptions.
//

template <class X>
class IloCarray {

#ifdef ILOBADTEMPLATE
  typedef IloCarray<X> Myself;
#endif

  int _max;   // length of array _data
  int _num;   // used part of array
  X*  _data;  // the array

public:
  void setMemory(X* x) {
    assert(!_data);
    _max = 1;
    _num = 1;
    _data = x;
  }

  void remax (int newsize) {
    _max = newsize;
    if (_max > 0 ) {
      X* x;
      if (_data)
#ifdef ILOREALLOCCHAR
        x = (X*) realloc ((char*)_data, _max * sizeof (X));
#else
        x = (X*) realloc ((void*)_data, _max * sizeof (X));
#endif
      else
        x = (X*) malloc (_max * sizeof (X));
      if ( x == NULL ) throw IloMemoryException();
      _data = x;
    }
  }

  void setMinSize(int minsize) {
    if ( _num < minsize ) {
      _num = minsize;
      if ( _max < minsize ) {
        int newmax = 2 * _max;
        if ( _num > newmax ) newmax = _num;
        remax (newmax);
      }
    }
  }

#ifdef ILOREALLOCCHAR
  ~IloCarray ()               { free ((char**)_data); }
#else
  ~IloCarray ()               { free (_data); }
#endif

  IloCarray(int maxi=0)
    : _max  (maxi)
    , _num  (0)
    , _data (0)
  {
    assert (_max >= 0);
    if ( maxi > 0 )  remax (maxi);
  }

  IloCarray(const
#ifdef ILOBADTEMPLATE
	    Myself
#else
	    IloCarray
#endif
	    & cpy)
    : _max  (cpy._max)
    , _num  (cpy._num)
    , _data (0)
  {
    remax (_max);
    memcpy ((void*)_data, (void*)cpy._data, _num * sizeof (X));
  }

  int getSize() const                { return (_num); }

  void addNum (int more) {
    _num += more;
    if ( _num > _max ) {
      int newmax = 2 * _max;
      if ( _num > newmax ) newmax = _num;
      remax (newmax);
    }
  }
  void add (X x) {
    addNum (1);
    _data[_num-1] = x;
  }
  void add (int num, X x) {
    int i = getSize();
    addNum (num);
    int e = getSize();
    for (; i < e; i++) _data[i] = x;
  }
  void add (int num, const X* x) {
    int j = 0;
    int i = getSize();
    addNum (num);
    int e = getSize();
    for (; i < e; i++, j++) _data[i] = x[j];
  }
  void remove(int first, int num = 1) {
    if ( first >= 0 && first < _num ) {
      int i;
      int j = first + num;
      int e = _num;
      for(i = first; j < e; i++, j++) _data[i] = _data[j];
      _num = i;
    }
  }

  X&       operator[] (IloInt i) {
    assert (i >= 0 && i < _max);
    return (_data[i]);
  }
  const X& operator[] (IloInt i) const {
    assert (i >= 0 && i < _max);
    return (_data[i]);
  }

  X&       operator[] (int i) {
    assert (i >= 0 && i < _max);
    return (_data[i]);
  }
  const X& operator[] (int i) const {
    assert (i >= 0 && i < _max);
    return (_data[i]);
  }
  operator const X* () const        { return (_data); }
  operator       X* ()              { return (_data); }

  IloCarray<X>& operator= (const
#ifdef ILOBADTEMPLATE
			   Myself
#else
			   IloCarray<X>
#endif
			   & rhs) {
    if ( _max < rhs._num ) addNum (rhs._num - _max);
    int      num  = _num = rhs._num;
    X*       to   = _data;
    const X* from = rhs._data;
    for (int i = 0; i < num; i++) to[i] = from[i];
    return (*this);
  }

  void clear ()            { _num = 0; }
};


// --------------------------------------------------------------------------
//
// an array that keeps track of changes.  However when using the inherited
// subscript operator for writing invalidate() must be called by hand
//

template<class X>
class IloCachedArray : public IloCarray<X> {
  IloBool _valid;
public:
  IloCachedArray(int maxi=0)
    : IloCarray<X>(maxi), _valid(IloFalse) {
  }
  IloBool isValid() const       { return (_valid); }
  void    validate()            { _valid = IloTrue; }
  void    invalidate()          { _valid = IloFalse; }
  void add(X x)                 { IloCarray<X>::add(x);      invalidate(); }
  void add(int num, X x)        { IloCarray<X>::add(num, x); invalidate(); }
  void add(int num, const X* x) { IloCarray<X>::add(num, x); invalidate(); }
  void clear ()                 { IloCarray<X>::clear();     invalidate(); }
  void remove(int first, int num = 1) {
    IloCarray<X>::remove(first, num);
    invalidate();
  }
};

template<class X>
class IloCachedMultiArray : public IloCachedArray<X> {
  IloBool _valid;
  IloInt  _which;
public:
  IloCachedMultiArray(int maxi=0)
    : IloCachedArray<X>(maxi), _valid(IloFalse), _which(-1) {
  }
  IloBool isValid(IloInt i) const       { return (_valid && _which == i); }
  void    validate(IloInt i)            { _valid = IloTrue; _which = i; }
  void    invalidate()                  { _valid = IloFalse; _which = -1; }

  void add(X x, IloInt i) {
     assert (_which == i);
     IloCachedArray<X>::add(x);
  }
  void add(int num, X x, IloInt i) {
     assert (_which == i);
     IloCachedArray<X>::add(num, x);
  }
  void add(int num, const X* x, IloInt i) {
     assert (_which == i);
     IloCachedArray<X>::add(num, x);
  }
  void clear () {
     IloCachedArray<X>::clear();
      _which=-1;
  }
};

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
