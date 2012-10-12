// -------------------------------------------------------------- -*- C++ -*-
// File: ./include/ilopl/ilooplprinteri.h
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

#ifndef __OPL_ilooplprinteriH
#define __OPL_ilooplprinteriH

#ifdef _WIN32
#pragma pack(push, 8)
#endif


#include <ilconcert/iloenv.h>
#include <ilopl/iloforall.h>
#include <ilopl/ilomapi.h>
#include <ilopl/ilotuplemap.h>
#include <ilopl/ilomapextr.h>
#include <ilconcert/ilodiffi.h>
#include <ilopl/ilosortedcollectioni.h>

#include <iostream>
using namespace std;

// CB 7 april 2011
// Isomorphism & MultiplesSpan implementation
// this has to be checked by OPL-team
#define ILO_INTERVAL_MAP_CONSTRAINT

class IloTuplePatternI;
class IloAnyCollectionMemberI;
class IloExprPiecewiseLinearI;

class IloSymbolExprLengthI;
class IloSymbolExprToIntValueI;
class IloSymbolExprToFloatValueI;
class IloSymbolExprMatchAtI;
class IloEvaluableIntMapSlotInTuple;
class IloEvaluableNumMapSlotInTuple;

class IloIntCollectionExprGeneratorI;

class IloIntSetOperatorI;
class IloNumSetOperatorI;
class IloSymbolSetOperatorI;
class IloTupleSetOperatorI;

class IloIntCollectionExprOperatorI;
class IloNumCollectionExprOperatorI;
class IloSymbolCollectionExprOperatorI;
class IloTupleCollectionExprOperatorI;

class IloConditionalConstraintI;
class IloTableConstraintMapI;

class IloRealNumCollectedMapI;
class IloMapAsNumCollectedMapI;
class IloAllDiffCollectedMapI;
class IloAllMinDistanceCollectedMapI;
class IloInverseCollectedMapI;
class IloCountCollectedMapI;
class IloElementCollectedMapIntExprI;
class IloElementCollectedMapNumExprI;
class IloStandardDeviationCollectedMapI;
class IloPackCollectedMapI;

class IloIntSetItemI;
class IloNumSetItemI;
class IloSymbolSetItemI;
class IloTupleSetItemI;
class IloIntCollectionExprItemI;
class IloNumCollectionExprItemI;
class IloSymbolCollectionExprItemI;
class IloTupleSetExprItemI;

class IloIntSetByExtensionExprI;
class IloNumSetByExtensionExprI;
class IloSymbolSetByExtensionExprI;
class IloTupleSetByExtensionExprI;

class IloIntCollectionInterI;
class IloIntCollectionInterCstI;
class IloIntCollectionRecInterCstI;
class IloNumCollectionInterI;
class IloNumCollectionInterCstI;
class IloNumCollectionRecInterCstI;
class IloSymbolCollectionInterI;
class IloSymbolCollectionInterCstI;
class IloAnyCollectionRecInterCstI;
class IloTupleCollectionInterI;
class IloTupleCollectionInterCstI;
class IloTupleCollectionRecInterCstI;

class IloIntCollectionUnionI;
class IloIntCollectionUnionCstI;
class IloIntCollectionRecUnionCstI;
class IloNumCollectionUnionI;
class IloNumCollectionUnionCstI;
class IloNumCollectionRecUnionCstI;
class IloSymbolCollectionUnionI;
class IloSymbolCollectionUnionCstI;
class IloAnyCollectionRecUnionCstI;
class IloTupleCollectionUnionI;
class IloTupleCollectionUnionCstI;
class IloTupleCollectionRecUnionCstI;

class IloIntCollectionSymExcludeI;
class IloIntCollectionSymExcludeCstI;
class IloIntCollectionRecSymExcludeCstI;
class IloNumCollectionSymExcludeI;
class IloNumCollectionSymExcludeCstI;
class IloNumCollectionRecSymExcludeCstI;
class IloSymbolCollectionSymExcludeI;
class IloSymbolCollectionSymExcludeCstI;
class IloAnyCollectionRecSymExcludeCstI;
class IloTupleCollectionSymExcludeI;
class IloTupleCollectionSymExcludeCstI;
class IloTupleCollectionRecSymExcludeCstI;

class IloIntCollectionExcludeI;
class IloIntCollectionExcludeCstI;
class IloIntCollectionRecExcludeCstI;
class IloNumCollectionExcludeI;
class IloNumCollectionExcludeCstI;
class IloNumCollectionRecExcludeCstI;
class IloSymbolCollectionExcludeI;
class IloSymbolCollectionExcludeCstI;
class IloAnyCollectionRecExcludeCstI;
class IloTupleCollectionExcludeI;
class IloTupleCollectionExcludeCstI;
class IloTupleCollectionRecExcludeCstI;

class IloIntAggregateSetExprI;
class IloNumAggregateSetExprI;
class IloSymbolAggregateSetExprI;
class IloTupleAggregateSetExprI;

class IloIntDExprI;
class IloNumDExprI;

class IloIntRangeAsIntSetExprI;

class IloOplModelSourceI;
class IloOplModelDefinitionI;
class IloOplTemplateDefinitionI;
class IloOplModelI;
class IloOplDataSourceI;
class IloOplElementI;
class IloOplElementDefinitionI;
class IloOplArrayDefinitionI;
class IloOplSetDefinitionI;
class IloOplRangeDefinitionI;
class IloOplTupleDefinitionI;
class IloOplTupleSchemaDefinitionI;
class IloOplTemplateElementDefinitionI;
class IloOplConstraintDefinitionI;
class IloOplDecisionExprDefinitionI;

class IloAbstractMapI;

class IloAggregateConstraintI;
class IloIntAggregateUnionSetExprI;
class IloIntAggregateInterSetExprI;
class IloNumAggregateUnionSetExprI;
class IloNumAggregateInterSetExprI;
class IloSymbolAggregateUnionSetExprI;
class IloSymbolAggregateInterSetExprI;
class IloTupleAggregateUnionSetExprI;
class IloTupleAggregateInterSetExprI;


class IloExtendedComprehensionI;

class IloIntCollectionConstI;
class IloNumCollectionConstI;
class IloSymbolCollectionConstI;
class IloTupleSetConstI;

class IloLexCollectedMapI;
class IloAppendedNumCollectedMapI;

class IloIntExprMapLightI;
class IloNumExprMapLightI;

class IloIntervalVarSubMapExprI;
class IloIntervalVarSubMapRootI;
class IloIntervalVarSubMapSubI;

class IloIntervalSequenceVarSubMapRootI;
class IloIntervalSequenceVarSubMapSubI;

class IloExtractableCollectedMapI;
class IloRealExtractableCollectedMapI;
class IloMapAsExtractableCollectedMapI;
class IloAppendedExtractableCollectedMapI;
class IloAdvAlternativeI;
class IloAdvSpanI;
class IloAdvSynchronizeI;
class IloExecuteI;
class IloForbidTimesI;
class IloPrecedenceI;
class IloStatusRelationI;
class IloSpanI;
class IloSynchronizeI;
class IloAlternativeI;
class IloIntervalVarI;
class IloIntervalVarExprI;
class IloIntervalVarEvalI;
class IloIntervalSequenceVarI;

class IlosResourceI;
class IlosDemandCapacityExprI;
class IlosDemandI;
//class IloMapParamI; // base class
//class IloIntMapParamI;
//class IloNumMapParamI;
class IloAdvPiecewiseFunctionExprI;
class IloAdvPiecewiseFunctionI;
class IloAdvPiecewiseFunctionExprSubMapExprI; // base class.
class IloAdvPiecewiseFunctionSubMapRootI;
class IloAdvPiecewiseFunctionSubMapSubI;
class IloAdvPiecewiseExprFunctionI;
class IloAdvExprPiecewiseLinearI;
class IloAggregateStepFunctionI;
class IloAdvForbidTimesI;
class IloAdvSequenceExprI;
class IloAdvSequenceNextConstraintI;
class IloAdvSequenceBeforeConstraintI;
class IloAdvNoOverlapI;
class IlosCostExprI; // base class
class IlosLeafCostExprI;
class IlosCostOperationExprI;
#ifdef ILO_SCHED_OBJ
class IlosObjectiveI; // ifdef ILO_SCHED_OBJ
#endif
class IlosCostToNumExprI;
class IloFunctionExprI; // base class
class IloAdvCumulAtomI;
class IloCumulAtomI;
class IloAdvCumulVarAtomI;
class IloAdvCumulConstAtomI;
class IloExprAlwaysInI;
class IloAlwaysInIntervalI;
class IloAlwaysInIntervalVarI;
class IloAdvCumulHeightExprI;
class IloAggregateCumulExprI;
class IloCumulFunctionExprSubMapRootI;
class IloCumulFunctionExprSubMapSubI;
class IloAddCumulFunctionsI;
class IloNegateCumulFunctionI;
class IloCumulFunctionExprI;
class IloIntervalRangeI;
class IloStateFunctionI;
class IloStateFunctionExprSubMapRootI;
class IloStateFunctionExprSubMapSubI;
class IloAdvStateAlwaysConstI;
class IloAdvStateAlwaysVarI;
class IloOverlapConstExprI;
#ifdef ILO_INTERVAL_MAP_CONSTRAINT
class IloAdvIntervalMapConstraintI;
#endif

class IloNumExprArrayElementI;


class IloOplPrinterI: public IloRttiEnvObjectI {
    ILORTTIDECL
public:
    
    static const IloInt PRINT_MAX_TERMS;

    IloOplPrinterI(IloEnvI* env);
    virtual ~IloOplPrinterI();

	void print(ostream& os, const IloExtractableI* extr);
	void printOther(ostream& os, const IloRttiEnvObjectI* obj);
	void printOther(ostream& os, const IloOplObject& item);

    void printName(ostream& os, const IloExtractableI* extr);

    IloBool isWithConstraintNames() const;
    void setWithConstraintNames(IloBool value);

    IloBool isDecisionExprNamesOnly() const;
    void setDecisionExprNamesOnly(IloBool value);
protected:
    ostream& out() const {
        return *_out;
    }

	virtual void printFallback(const IloRttiEnvObjectI* extr);
	virtual void printNameFallback(const IloRttiEnvObjectI* obj);

private:
    IloBool _withConstraintNames;
    IloBool _dexprNamesOnly;

    typedef void (*Printer)(IloOplPrinterI* printer, const IloRttiEnvObjectI*);
	typedef IloArray<Printer> PrinterArray;
	PrinterArray _printers;

    ostream* _out;
    int _currentOperator;
    IloBool _isRangeUb;
    IloBool _noRangeName;

    typedef IloStringHashTable<IloConstraintArray> ExpandedForAllCache;
    ExpandedForAllCache _expandedForAllCache;

    void registerPrinters();
    void registerPrinter(IloTypeIndex index, Printer printer);
public: // AFC 20/04/06 This method is used by IDE to access temporarily the expanded constraints
    IloConstraintI* getExpandedForAllRange(const IloForAllRangeI* range);

protected:
    // utilities
	void print(IloExtractableArray array);
    void print(const IloRttiEnvObjectI* obj);
    void printName(const IloExtractableI* obj);
    void printConstraintLabel(const IloConstraintI* ct);
    void printName(const char* name, const IloRttiEnvObjectI* obj);
    void printAggregate(int op, const IloComprehensionI* comprehension, const IloExtractableI* extr);
    void printAggregateSet(const IloComprehensionI* comprehension, const IloExtractableI* extr);
    void printQualifier(const IloExtractableI* index, const IloRttiEnvObjectI* collection, const IloExtractableList& filters);
    void printBinary(int op, const IloExtractableI* left, const IloExtractableI* right, IloNum leftConst =0, IloNum rightConst =0);
    void printBinaryString(int op, const IloExtractableI* left, const IloExtractableI* right, const char* leftConst =0, const char* rightConst =0);
    void printBinaryTuple(int op, const IloExtractableI* left, const IloExtractableI* right, IloTupleI* leftConst =0, IloTupleI* rightConst =0);
    void printBinarySet(int op, const IloRttiEnvObjectI* left, const IloRttiEnvObjectI* right);
    void printArraySlot(const IloAbstractMapI* map, const IloExtractableI* owner, const IloExtractableI* index);
    void printArraySlot(const IloExtractableI* owner, const IloOplObject& index);    
    void printMember(const IloDiscreteDataCollectionI* collection, const IloExtractableI* expr);
    void printMemberExpr(const IloExtractableI* collection, const IloExtractableI* expr);
    void printFunctionCall(const char* name, const IloRttiEnvObjectI* arg);
    void printFunctionCall(const char* name, const IloRttiEnvObjectI* arg, const IloExtractableI* arg2);
    void printFunctionCall(const char* name, const IloRttiEnvObjectI* arg, const IloExtractableI* arg2, const IloExtractableI* arg3);    
    void printTupleCell(const IloExtractableI* tuple, const IloSymbolI* name);
    IloBool isExtensionPWL(const IloAggregateSlopesPwlExprI* e);
    void printSet(const IloDiscreteDataCollectionI* coll);
    void printMap(const IloAbstractMapI* mapI);
    void printFunctionCall(const char* name, const IloExtractableArray& args);
    void printExtensionSet(const IloMapIndexArray& items);
    void printDimensions(const IloAbstractMapI* mapI);
    void printElementEnd(const IloOplElementI& e, IloBool semi =IloFalse);
    void printElementEndSemi(const IloOplElementI& e) {
        printElementEnd(e,IloTrue);
    }
    void printElementDefinitionType(const IloOplElementDefinitionI& def);
    void printElementDefinitionName(const IloOplElementDefinitionI& def);
    void printDecisionExprInfo(const IloOplDecisionExprDefinitionI& def);

    IloBool printTerm(const IloNumLinExprTermI* term, int op);
    //void printItem(const IloTuplePatternItem& item);
    void printNumber(IloNum number);
    void printSymbol(IloSymbolI* symbol);
    void printString(const char* string);
    void printMapIndex(const IloOplObject& item);

    int pushOperator(int op);
    void popOperator(int op);

public:
    // extractable printers

    void printIloModelI(const IloModelI* e);
    void printIloObjectiveI(const IloObjectiveI* e);

    void printIloRangeI(const IloRangeI* e);
    void printIloNumVarI(const IloNumVarI* e);
    void printIloForAllI(const IloForAllI* e);
    void printIloComprehensionI(const IloComprehensionI* e);
    void printIloIntCollectionGeneratorI(const IloIntCollectionGeneratorI* e);
    void printIloNumCollectionGeneratorI(const IloNumCollectionGeneratorI* e);
    void printIloSymbolCollectionGeneratorI(const IloSymbolCollectionGeneratorI* e);

    void printIloNumEqI(const IloNumEqI* e);
    void printIloNumGeI(const IloNumGeI* e);
    void printIloNumLeI(const IloNumLeI* e);
    void printIloNumGTI(const IloNumGTI* e);
    void printIloNumLTI(const IloNumLTI* e);

    void printIloRealNumCollectedMapI(const IloRealNumCollectedMapI* e);
    void printIloMapAsNumCollectedMapI(const IloMapAsNumCollectedMapI* e);
    void printIloAllDiffCollectedMapI(const IloAllDiffCollectedMapI* e);
    void printIloAllMinDistanceCollectedMapI(const IloAllMinDistanceCollectedMapI* e);
    void printIloInverseCollectedMapI(const IloInverseCollectedMapI* e);
    void printIloCountCollectedMapI(const IloCountCollectedMapI* e);
    void printIloElementCollectedMapIntExprI(const IloElementCollectedMapIntExprI* e);
    void printIloElementCollectedMapNumExprI(const IloElementCollectedMapNumExprI* e);
    void printIloStandardDeviationCollectedMapI(const IloStandardDeviationCollectedMapI* e);
    void printIloPackCollectedMapI(const IloPackCollectedMapI* e);

    void printIloAllDiffI(const IloAllDiffI* e);
    void printIloDiffI(const IloDiffI* e);
    void printIloAllMinDistanceI(const IloAllMinDistanceI* e);
    void printIloNotI(const IloNotI* e);
    void printIloIfThenI(const IloIfThenI* e);    
    void printIloTableConstraintMapI(const IloTableConstraintMapI* e);

    void printIloNumLinTermI(const IloNumLinTermI* e);
    void printIloIntLinTermI(const IloIntLinTermI* e);

    void printIloMapIntExprIndexI(const IloMapIntExprIndexI* e);
    void printIloMapNumExprIndexI(const IloMapNumExprIndexI* e);
    void printIloMapAnyExprIndexI(const IloMapAnyExprIndexI* e);

    void printIloIntIndexI(const IloIntIndexI* e);
    void printIloNumIndexI(const IloNumIndexI* e);
    void printIloSymbolIndexI(const IloSymbolIndexI* e);

    void printIloIntAggregateExprI(const IloIntAggregateExprI* e);
    void printIloNumAggregateExprI(const IloNumAggregateExprI* e);

    void printIloIntDExprI(const IloIntDExprI* e);
    void printIloNumDExprI(const IloNumDExprI* e);

    void printIloIntTimesI(const IloIntTimesI* e);
    void printIloIntDivI(const IloIntDivI* e);

    void printIloNumTimesI(const IloNumTimesI* e);
    void printIloNumDivI(const IloNumDivI* e);

    void printIloNumTimesCstI(const IloNumTimesCstI* e);
    void printIloNumDivCstI(const IloNumDivCstI* e); 
    void printIloNumRecDivCstI(const IloNumRecDivCstI* e);

    void printIloIntTimesCstI(const IloIntTimesCstI* e);
    void printIloIntDivCstI(const IloIntDivCstI* e); 
    void printIloIntRecDivCstI(const IloIntRecDivCstI* e);

    void printIloConstConstraintI(const IloConstConstraintI* e);
    void printIloIntSubMapSubI(const IloIntSubMapSubI* e);
    void printIloIntSubMapRootI(const IloIntSubMapRootI* e);
    void printIloIntExprSubMapSubI(const IloIntExprSubMapSubI* e);
    void printIloIntExprSubMapRootI(const IloIntExprSubMapRootI* e);
    void printIloTupleSubMapSubI(const IloTupleSubMapSubI* e);
    void printIloTupleSubMapRootI(const IloTupleSubMapRootI* e);
    void printIloSymbolSubMapSubI(const IloSymbolSubMapSubI* e);
    void printIloSymbolSubMapRootI(const IloSymbolSubMapRootI* e);

    void printIloNumSubMapSubI(const IloNumSubMapSubI* e);
    void printIloNumSubMapRootI(const IloNumSubMapRootI* e);
    void printIloNumExprSubMapSubI(const IloNumExprSubMapSubI* e);
    void printIloNumExprSubMapRootI(const IloNumExprSubMapRootI* e);

    void printIloTupleSetGeneratorI(const IloTupleSetGeneratorI* e);

    void printIloTupleIndexI(const IloTupleIndexI* e);
    void printIloSymbolConstI(const IloSymbolConstI* e);
    void printIloTupleConstI(const IloTupleConstI* e);

    void printIloIntModuloCstI(const IloIntModuloCstI* e);
    void printIloIntModuloI(const IloIntModuloI* e);
    void printIloIntRecModuloCstI(const IloIntRecModuloCstI* e);

    void printIloTuplePatternI(const IloTuplePatternI* e);
    void printIloNumTupleCellExprI(const IloNumTupleCellExprI* e);
    void printIloIntTupleCellExprI(const IloIntTupleCellExprI* e);
    void printIloSymbolTupleCellExprI(const IloSymbolTupleCellExprI* e);
    void printIloTupleTupleCellExprI(const IloTupleTupleCellExprI* e);
    void printIloAndI(const IloAndI* e);
    void printIloOrI(const IloOrI* e);
    void printIloAggregateSlopesPwlExprI(const IloAggregateSlopesPwlExprI* e);
    void printIloAnyEqI(const IloAnyEqI* e);
    void printIloAnyNeqI(const IloAnyNeqI* e);
    void printIloAnyEqCstI(const IloAnyEqCstI* e);
    void printIloAnyNeqCstI(const IloAnyNeqCstI* e);

    void printIloTupleI(const IloTupleI* e);
    void printIloTupleBufferI(const IloTupleBufferI* e);

    void printIloMapIntIndexI(const IloMapIntIndexI* e);
    void printIloMapNumIndexI(const IloMapNumIndexI* e);
    void printIloMapTupleIndexI(const IloMapTupleIndexI* e);
    void printIloMapSymbolIndexI(const IloMapSymbolIndexI* e);
    void printIloMapAnyIndexI(const IloMapAnyIndexI* e);

    void printIloExprPiecewiseLinearI(const IloExprPiecewiseLinearI* e);

    void printIloNumPowerI(const IloNumPowerI* e);
    void printIloNumPowerCstI(const IloNumPowerCstI* e);
    void printIloNumRecPowerCstI(const IloNumRecPowerCstI* e);

    void printIloNumToIntI(const IloNumToIntI* e);
    void printIloSymbolExprLengthI(const IloSymbolExprLengthI* e);
    void printIloSymbolExprMatchAtI(const IloSymbolExprMatchAtI* e);
    void printIloSymbolExprToIntValueI(const IloSymbolExprToIntValueI* e);
    void printIloSymbolExprToFloatValueI(const IloSymbolExprToFloatValueI* e);

    void printIloConditionalIntExprI(const IloConditionalIntExprI* e);
    void printIloConditionalNumExprI(const IloConditionalNumExprI* e);
	void printIloConditionalSymbolExprI(const IloConditionalSymbolExprI* e);
	void printIloConditionalTupleExprI(const IloConditionalTupleExprI* e);
	void printIloConditionalIntSetExprI(const IloConditionalIntSetExprI* e);
	void printIloConditionalNumSetExprI(const IloConditionalNumSetExprI* e);
	void printIloConditionalSymbolSetExprI(const IloConditionalSymbolSetExprI* e);
	void printIloConditionalTupleSetExprI(const IloConditionalTupleSetExprI* e);

    void printIloIntRangeI(const IloIntRangeI* e);
    void printIloNumRangeI(const IloNumRangeI* e);
    void printIloDiscreteDataCollectionI(const IloDiscreteDataCollectionI* e);
    void printIloIntRangeExprI(const IloIntRangeExprI* e);

    void printIloIntSetI(const IloIntSetI* e);
    void printIloAscSortedIntSetI(const IloAscSortedIntSetI* e);
    void printIloDescSortedIntSetI(const IloDescSortedIntSetI* e);
    void printIloNumSetI(const IloNumSetI* e);
    void printIloAscSortedNumSetI(const IloAscSortedNumSetI* e);
    void printIloDescSortedNumSetI(const IloDescSortedNumSetI* e);
    void printIloAnySetI(const IloAnySetI* e);
    void printIloAscSortedSymbolSetI(const IloAscSortedSymbolSetI* e);
    void printIloDescSortedSymbolSetI(const IloDescSortedSymbolSetI* e);
    void printIloTupleSetI(const IloTupleSetI* e);
    void printIloTupleCollectionI(const IloTupleCollectionI* e);
    void printIloAscSortedTupleSetI(const IloAscSortedTupleSetI* e);
    void printIloDescSortedTupleSetI(const IloDescSortedTupleSetI* e);

    void printIloNumAbsI(const IloNumAbsI* e);
    void printIloNumCeilI(const IloNumCeilI* e);
    void printIloDistToIntI(const IloDistToIntI* e);
    void printIloExponentI(const IloExponentI* e);
    void printIloNumFloorI(const IloNumFloorI* e);
    void printIloNumFractI(const IloNumFractI* e);
    void printIloNumRoundI(const IloNumRoundI* e);
    void printIloLogI(const IloLogI* e);
    void printIloNumTruncI(const IloNumTruncI* e);
    void printIloSgnI(const IloSgnI* e);

    void printIloIntCollectionExprGeneratorI(const IloIntCollectionExprGeneratorI* e);
    void printIloNumCollectionExprGeneratorI(const IloNumCollectionExprGeneratorI* e);
    void printIloSymbolCollectionExprGeneratorI(const IloSymbolCollectionExprGeneratorI* e);
    void printIloTupleSetExprGeneratorI(const IloTupleSetExprGeneratorI* e);

    void printIloIntCollectionSubMapRootI(const IloIntCollectionSubMapRootI* e);
    void printIloIntCollectionSubMapSubI(const IloIntCollectionSubMapSubI* e);
    void printIloNumCollectionSubMapRootI(const IloNumCollectionSubMapRootI* e);
    void printIloNumCollectionSubMapSubI(const IloNumCollectionSubMapSubI* e);
    void printIloSymbolCollectionSubMapRootI(const IloSymbolCollectionSubMapRootI* e);
    void printIloSymbolCollectionSubMapSubI(const IloSymbolCollectionSubMapSubI* e);
    void printIloTupleSetSubMapRootI(const IloTupleSetSubMapRootI* e);
    void printIloTupleSetSubMapSubI(const IloTupleSetSubMapSubI* e);

    void printIloSymbolCollectionTupleCellExprI(const IloSymbolCollectionTupleCellExprI* e);
    void printIloNumCollectionTupleCellExprI(const IloNumCollectionTupleCellExprI* e);
    void printIloIntCollectionTupleCellExprI(const IloIntCollectionTupleCellExprI* e);

    void printIloEvaluableIntMapSlotInTuple(const IloEvaluableIntMapSlotInTuple* e);
    void printIloEvaluableNumMapSlotInTuple(const IloEvaluableNumMapSlotInTuple* e);

    void printIloForAllRangeI(const IloForAllRangeI* e);

    void printIloTupleSchemaI(const IloTupleSchemaI* e);
    void printIloColumnDefinitionI(const IloColumnDefinitionI* e);

    void printIloNumMaxArrayI(const IloNumMaxArrayI* e);
    void printIloNumMinArrayI(const IloNumMinArrayI* e);
    void printIloIntMaxArrayI(const IloIntMaxArrayI* e);
    void printIloIntMinArrayI(const IloIntMinArrayI* e);

    void printIloIntCollectionCardI(const IloIntCollectionCardI* e);
    void printIloNumCollectionCardI(const IloNumCollectionCardI* e);
    void printIloTupleCollectionCardI(const IloTupleCollectionCardI* e);
    void printIloSymbolCollectionCardI(const IloSymbolCollectionCardI* e);

    void printIloIntCollectionFirstI(const IloIntCollectionFirstI* e);
    void printIloNumCollectionFirstI(const IloNumCollectionFirstI* e);
    void printIloTupleCollectionFirstI(const IloTupleCollectionFirstI* e);
    void printIloSymbolCollectionFirstI(const IloSymbolCollectionFirstI* e);

    void printIloIntCollectionLastI(const IloIntCollectionLastI* e);
    void printIloNumCollectionLastI(const IloNumCollectionLastI* e);
    void printIloTupleCollectionLastI(const IloTupleCollectionLastI* e);
    void printIloSymbolCollectionLastI(const IloSymbolCollectionLastI* e);

    void printIloIntCollectionOrdI(const IloIntCollectionOrdI* e);
    void printIloNumCollectionOrdI(const IloNumCollectionOrdI* e);
    void printIloSymbolCollectionOrdI(const IloSymbolCollectionOrdI* e);
    void printIloTupleSetOrdI(const IloTupleSetOrdI* e);

    void printIloIntSetOperatorI(const IloIntSetOperatorI* e);
    void printIloNumSetOperatorI(const IloNumSetOperatorI* e);
    void printIloSymbolSetOperatorI(const IloSymbolSetOperatorI* e);
    void printIloTupleSetOperatorI(const IloTupleSetOperatorI* e);

    void printIloIntCollectionExprOperatorI(const IloIntCollectionExprOperatorI* e);
    void printIloNumCollectionExprOperatorI(const IloNumCollectionExprOperatorI* e);
    void printIloSymbolCollectionExprOperatorI(const IloSymbolCollectionExprOperatorI* e);
    void printIloTupleSetExprOperatorI(const IloTupleSetExprOperatorI* e);

    void printIloIntMapAsCollectionI(const IloIntMapAsCollectionI* e);
    void printIloNumMapAsCollectionI(const IloNumMapAsCollectionI* e);

    void printIloConversionI(const IloConversionI* e);

    void printIloConditionalConstraintI(const IloConditionalConstraintI* e);

    void printIloSymbolGTI(const IloSymbolGTI* e);
    void printIloSymbolLTI(const IloSymbolLTI* e);
    void printIloSymbolGeI(const IloSymbolGeI* e);
    void printIloSymbolLeI(const IloSymbolLeI* e);

    void printIloIntCollectionExprItemI(const IloIntCollectionExprItemI* e);
    void printIloNumCollectionExprItemI(const IloNumCollectionExprItemI* e);
    void printIloSymbolCollectionExprItemI(const IloSymbolCollectionExprItemI* e);
    void printIloTupleSetExprItemI(const IloTupleSetExprItemI* e);
    void printIloTupleSetExprItemByKeyI(const IloTupleSetExprItemByKeyI* e);

    void printIloRangeWithExprBoundsI(const IloRangeWithExprBoundsI* e);

    void printIloIntCollectionExprMemberI(const IloIntCollectionExprMemberI* e);
    void printIloNumCollectionExprMemberI(const IloNumCollectionExprMemberI* e);
    void printIloSymbolCollectionExprMemberI(const IloSymbolCollectionExprMemberI* e);
    void printIloTupleSetExprMemberI(const IloTupleSetExprMemberI* e);

    void printIloIntSetByExtensionExprI(const IloIntSetByExtensionExprI* e);
    void printIloNumSetByExtensionExprI(const IloNumSetByExtensionExprI* e);
    void printIloSymbolSetByExtensionExprI(const IloSymbolSetByExtensionExprI* e);
    void printIloTupleSetByExtensionExprI(const IloTupleSetByExtensionExprI* e);

    void printIloIntCollectionInterI(const IloIntCollectionInterI* e);
    void printIloNumCollectionInterI(const IloNumCollectionInterI* e);
    void printIloSymbolCollectionInterI(const IloSymbolCollectionInterI* e);
    void printIloTupleCollectionInterI(const IloTupleCollectionInterI* e);

    void printIloIntCollectionUnionI(const IloIntCollectionUnionI* e);
    void printIloNumCollectionUnionI(const IloNumCollectionUnionI* e);
    void printIloSymbolCollectionUnionI(const IloSymbolCollectionUnionI* e);
    void printIloTupleCollectionUnionI(const IloTupleCollectionUnionI* e);

    void printIloIntCollectionSymExcludeI(const IloIntCollectionSymExcludeI* e);
    void printIloSymbolCollectionSymExcludeI(const IloSymbolCollectionSymExcludeI* e);
    void printIloNumCollectionSymExcludeI(const IloNumCollectionSymExcludeI* e);
    void printIloTupleCollectionSymExcludeI(const IloTupleCollectionSymExcludeI* e);

    void printIloIntCollectionExcludeI(const IloIntCollectionExcludeI* e);
    void printIloNumCollectionExcludeI(const IloNumCollectionExcludeI* e);
    void printIloSymbolCollectionExcludeI(const IloSymbolCollectionExcludeI* e);
    void printIloTupleCollectionExcludeI(const IloTupleCollectionExcludeI* e);

    void printIloIntAggregateSetExprI(const IloIntAggregateSetExprI* e);
    void printIloNumAggregateSetExprI(const IloNumAggregateSetExprI* e);
    void printIloSymbolAggregateSetExprI(const IloSymbolAggregateSetExprI* e);
    void printIloTupleAggregateSetExprI(const IloTupleAggregateSetExprI* e);

    void printIloIntRangeAsIntSetExprI(const IloIntRangeAsIntSetExprI* e);

    void printIloOplModelSourceI(const IloOplModelSourceI* e);
    void printIloOplModelDefinitionI(const IloOplModelDefinitionI* e);
    void printIloOplTemplateDefinitionI(const IloOplTemplateDefinitionI* e);
    void printIloOplModelI(const IloOplModelI* e);
    void printIloOplDataSourceI(const IloOplDataSourceI* e);
    void printIloOplElementI(const IloOplElementI* e);
    void printIloOplElementDefinitionI(const IloOplElementDefinitionI* e);
    void printIloOplArrayDefinitionI(const IloOplArrayDefinitionI* e);
	void printIloOplSetDefinitionI(const IloOplSetDefinitionI* e);
	void printIloOplRangeDefinitionI(const IloOplRangeDefinitionI* e);
	void printIloOplTupleSchemaDefinitionI(const IloOplTupleSchemaDefinitionI* e);
	void printIloOplConstraintDefinitionI(const IloOplConstraintDefinitionI* e);
    void printIloOplDecisionExprDefinitionI(const IloOplDecisionExprDefinitionI* e);

    void printIloAbstractMapI(const IloAbstractMapI* e);

    void printIloAggregateConstraintI(const IloAggregateConstraintI* e);
    void printIloIntAggregateUnionSetExprI(const IloIntAggregateUnionSetExprI* e);
    void printIloIntAggregateInterSetExprI(const IloIntAggregateInterSetExprI* e);
    void printIloNumAggregateUnionSetExprI(const IloNumAggregateUnionSetExprI* e);
    void printIloNumAggregateInterSetExprI(const IloNumAggregateInterSetExprI* e);
    void printIloSymbolAggregateUnionSetExprI(const IloSymbolAggregateUnionSetExprI* e);
    void printIloSymbolAggregateInterSetExprI(const IloSymbolAggregateInterSetExprI* e);
    void printIloTupleAggregateUnionSetExprI(const IloTupleAggregateUnionSetExprI* e);
    void printIloTupleAggregateInterSetExprI(const IloTupleAggregateInterSetExprI* e);   

    void printIloExtendedComprehensionI(const IloExtendedComprehensionI* e);

    void printIloIntCollectionConstI(const IloIntCollectionConstI* e);
    void printIloNumCollectionConstI(const IloNumCollectionConstI* e);
    void printIloSymbolCollectionConstI(const IloSymbolCollectionConstI* e);
    void printIloTupleSetConstI(const IloTupleSetConstI* e);

    void printIloLexCollectedMapI(const IloLexCollectedMapI* e);
    void printIloAppendedNumCollectedMapI(const IloAppendedNumCollectedMapI* e);

    void printIloIntExprMapLightI(const IloIntExprMapLightI* e);
    void printIloNumExprMapLightI(const IloNumExprMapLightI* e);

    void printIloIntervalVarSubMapExprI(const IloIntervalVarSubMapExprI* e);
    void printIloIntervalVarSubMapRootI(const IloIntervalVarSubMapRootI* e);
    void printIloIntervalVarSubMapSubI(const IloIntervalVarSubMapSubI* e);

	void printIloIntervalSequenceVarSubMapRootI(const IloIntervalSequenceVarSubMapRootI* e);
	void printIloIntervalSequenceVarSubMapSubI(const IloIntervalSequenceVarSubMapSubI* e);

    void printIloExtractableCollectedMapI(const IloExtractableCollectedMapI* e);
    void printIloRealExtractableCollectedMapI(const IloRealExtractableCollectedMapI* e);
    void printIloMapAsExtractableCollectedMapI(const IloMapAsExtractableCollectedMapI* e);
    void printIloAppendedExtractableCollectedMapI(const IloAppendedExtractableCollectedMapI* e);

    void printIloAdvAlternativeI(const IloAdvAlternativeI* e);
    void printIloAdvSpanI(const IloAdvSpanI* e);
    void printIloAdvSynchronizeI(const IloAdvSynchronizeI* e);
    void printIloExecuteI(const IloExecuteI* e);
    void printIloForbidTimesI(const IloForbidTimesI* e);
    void printIloPrecedenceI(const IloPrecedenceI* e);
    void printIloStatusRelationI(const IloStatusRelationI* e);
    void printIloSpanI(const IloSpanI* e);
    void printIloSynchronizeI(const IloSynchronizeI* e);
    void printIloAlternativeI(const IloAlternativeI* e);

    void printIloIntervalVarI(const IloIntervalVarI* e);
    void printIloIntervalVarExprI(const IloIntervalVarExprI* e);
    void printIloIntervalVarEvalI(const IloIntervalVarEvalI* e);

	void printIloIntervalSequenceVarI(const IloIntervalSequenceVarI* e);

    void printIlosResourceI(const IlosResourceI* e);
    void printIlosDemandCapacityExprI(const IlosDemandCapacityExprI* e);
    void printIlosDemandI(const IlosDemandI* e);
//    void printIloIntMapParamI(const IloIntMapParamI* e);
//    void printIloNumMapParamI(const IloNumMapParamI* e);
    void printIloAdvPiecewiseFunctionExprI(const IloAdvPiecewiseFunctionExprI* e);
    void printIloAdvPiecewiseFunctionI(const IloAdvPiecewiseFunctionI* e);
    void printIloAdvPiecewiseFunctionSubMapRootI(const IloAdvPiecewiseFunctionSubMapRootI* e);
    void printIloAdvPiecewiseFunctionSubMapSubI(const IloAdvPiecewiseFunctionSubMapSubI* e);
	void printIloAdvPiecewiseExprFunctionI(const IloAdvPiecewiseExprFunctionI* e);
	void printIloAdvExprPiecewiseLinearI(const IloAdvExprPiecewiseLinearI* e);
	void printIloAggregateStepFunctionI( const IloAggregateStepFunctionI* e );
    void printIloAdvForbidTimesI(const IloAdvForbidTimesI* e);
    void printIloAdvSequenceExprI(const IloAdvSequenceExprI* e);
    void printIloAdvSequenceNextConstraintI(const IloAdvSequenceNextConstraintI* e);
    void printIloAdvSequenceBeforeConstraintI(const IloAdvSequenceBeforeConstraintI* e);
    void printIloAdvNoOverlapI(const IloAdvNoOverlapI* e);
    void printIlosLeafCostExprI(const IlosLeafCostExprI* e);
    void printIlosCostOperationExprI(const IlosCostOperationExprI* e);
    void printIlosCostToNumExprI(const IlosCostToNumExprI* e);
    void printIloAdvCumulAtomI(const IloAdvCumulAtomI* e);
	void printIloCumulAtomI(const IloCumulAtomI* e);
	void printIloAdvCumulVarAtomI(const IloAdvCumulVarAtomI* e);
	void printIloAdvCumulConstAtomI(const IloAdvCumulConstAtomI* e);
    void printIloCumulFunctionExprI(const IloCumulFunctionExprI* e);
	void printIloIntervalRangeI(const IloIntervalRangeI*e );
	void printIloExprAlwaysInI(const IloExprAlwaysInI*e );
	void printIloAlwaysInIntervalI(const IloAlwaysInIntervalI *e );
	void printIloAlwaysInIntervalVarI(const IloAlwaysInIntervalVarI *e );
	void printIloAdvCumulHeightExprI( const IloAdvCumulHeightExprI *e );
	void printIloAggregateCumulExprI( const IloAggregateCumulExprI *e );
	void printIloCumulFunctionExprSubMapRootI( const IloCumulFunctionExprSubMapRootI *e );
	void printIloCumulFunctionExprSubMapSubI( const IloCumulFunctionExprSubMapSubI *e );
	void printIloAddCumulFunctionsI( const IloAddCumulFunctionsI *e );
	void printIloNegateCumulFunctionI( const IloNegateCumulFunctionI *e );
	void printIloStateFunctionI( const IloStateFunctionI *e );
	void printIloStateFunctionExprSubMapRootI( const IloStateFunctionExprSubMapRootI *e );
	void printIloStateFunctionExprSubMapSubI( const IloStateFunctionExprSubMapSubI *e );
	void printIloAdvStateAlwaysVarI( const IloAdvStateAlwaysVarI *e );
	void printIloAdvStateAlwaysConstI( const IloAdvStateAlwaysConstI *e );
	void printIloOverlapConstExprI( const IloOverlapConstExprI *e );
	void printIloOverlapVarExprI( const IloOverlapVarExprI *e );
#ifdef ILO_INTERVAL_MAP_CONSTRAINT
	void printIloAdvIntervalMapConstraintI( const IloAdvIntervalMapConstraintI *e );
#endif
	void printIloNumExprArrayElementI(const IloNumExprArrayElementI* e);

};

inline IloBool IloOplPrinterI::isWithConstraintNames() const {
    return _withConstraintNames;
}

inline void IloOplPrinterI::setWithConstraintNames(IloBool with) {
    _withConstraintNames = with;
}

inline IloBool IloOplPrinterI::isDecisionExprNamesOnly() const {
    return _dexprNamesOnly;
}
inline void IloOplPrinterI::setDecisionExprNamesOnly(IloBool value) {
    _dexprNamesOnly = value;
}


#ifdef _WIN32
#pragma pack(pop)
#endif

#endif
