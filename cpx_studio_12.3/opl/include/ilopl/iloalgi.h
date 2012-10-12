// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/iloalgi.h
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


#ifndef __ADVANCED_iloalgiH
#define __ADVANCED_iloalgiH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>

//
// temporary stuff for compiling with new selectors
//
#include <ilopl/ilomodeli.h>
#include <ilconcert/ilolinear.h>
#include <ilopl/iloalg.h>

class IloModelEvaluatorSolutionGetterBaseI : public IloRttiEnvObjectI {
	ILORTTIDECL
		IloAlgorithmI* _alg;
public:
	IloModelEvaluatorSolutionGetterBaseI(IloEnvI* env, IloAlgorithm alg) : IloRttiEnvObjectI(env), _alg(alg.getImpl()) {}
	IloAlgorithmI* getAlgorithmI() const { return _alg; }
	IloAlgorithm getAlgorithm() const { return _alg; }
	enum IntervalField {
		BitPresent,
		BitAbsent,
		Start,
		End,
		Length,
		Size
	};	
	enum SequenceDirection {
		LeftToRight,
		RightToLeft
	};
	enum FunctionField {
		NumberOfSegments,
		SegmentStart,
		SegmentEnd,
		SegmentValue,
		FunctionValue,
		_Internal_Concert
	};
};
#ifdef _WIN32
#pragma pack(pop)
#endif

#endif

