// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/iltuple/ilotuplerequest.h
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


#ifndef __ADVANCED_ilotuplerequestH
#define __ADVANCED_ilotuplerequestH

#ifdef _WIN32
#pragma pack(push, 8)
#endif
#include <ilopl/ilosys.h>


#include <ilopl/iltuple/ilotuplerequesti.h>


class IloTupleRequest {
protected:
	IloTupleRequestI* _impl;
public:
	
	void end(){
		if (_impl) {
			delete _impl; _impl = 0;
		}
	}

	
	void display(ILOSTD(ostream)& os) const{
		IloAssert(getImpl() != 0, "IloTupleRequest: Using empty handle");
		_impl->display(os);
	}

	
	IloEnv getEnv(){
		return _impl->getEnv();
	}

	
	IloTupleRequest(IloTupleRequestI* impl) : _impl(impl){ }

	
	IloTupleRequestI* getImpl() const {
		return (IloTupleRequestI*)_impl;
	}

	
	IloTupleRequest(IloEnv env, IloTupleSet coll);
};

#ifdef _WIN32
#pragma pack(pop)
#endif


#endif

