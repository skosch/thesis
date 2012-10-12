// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilooplcpi.h
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


#ifndef __CONCERT_ilooplcpiH
#define __CONCERT_ilooplcpiH

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#include <ilopl/ilosys.h>
# include <ilconcert/ilomodel.h>
# include <ilopl/ilomapi.h>
# include <ilconcert/iloevaluator.h>

//-------------------------------------------------------------
// IloPiecewiseLinear build with segmented functions

typedef IloNum (*IloEvalCP)(const IloAlgorithm alg,
			    const IloExtractableI* ext);


IloNumExprArg IloPiecewiseLinear(const IloNumExprArg node,
				 IloAdvPiecewiseFunctionExprI* pwf);

IloNumExprArg IloStepwiseLinear(const IloNumExprArg node,
				IloAdvPiecewiseFunctionExprI* swf);

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif

