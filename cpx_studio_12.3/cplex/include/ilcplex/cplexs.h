/* --------------------------------------------------------------------------
 * File: cplexs.h
 * Version 12.3
 * --------------------------------------------------------------------------
 * Licensed Materials - Property of IBM
 * 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55
 * Copyright IBM Corporation 1988, 2011. All Rights Reserved.
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with
 * IBM Corp.
 *---------------------------------------------------------------------------
 */

#ifndef CPX_CPLEXS_H
#define CPX_CPLEXS_H

#include "cpxconst.h"

/* Argument lists for callbacks */
#define CPXS_CALLBACK_ARGS CPXCENVptr env, void *cbdata, int wherefrom, \
      void *cbhandle
#define CPXS_CALLBACK_PROF_ARGS CPXCENVptr env, int wherefrom, void *cbhandle
#define CPXS_CALLBACK_BRANCH_ARGS  CPXCENVptr xenv, void *cbdata,       \
      int wherefrom, void *cbhandle, int brtype, CPXINT brset,          \
      int nodecnt, CPXINT bdcnt, const double *nodeest,                 \
      const CPXINT *nodebeg, const CPXINT *xindex, const char *lu,      \
      const int *bd, int *useraction_p
#define CPXS_CALLBACK_NODE_ARGS  CPXCENVptr xenv, void *cbdata,         \
      int wherefrom, void *cbhandle, CPXLONG *nodeindex, int *useraction
#define CPXS_CALLBACK_HEURISTIC_ARGS  CPXCENVptr xenv, void *cbdata,    \
      int wherefrom, void *cbhandle, double *objval_p, double *x,       \
      int *checkfeas_p, int *useraction_p
#define CPXS_CALLBACK_SOLVE_ARGS  CPXCENVptr xenv, void *cbdata,        \
      int wherefrom, void *cbhandle, int *useraction
#define CPXS_CALLBACK_CUT_ARGS  CPXCENVptr xenv, void *cbdata,  \
      int wherefrom, void *cbhandle, int *useraction_p
#define CPXS_CALLBACK_INCUMBENT_ARGS  CPXCENVptr xenv, void *cbdata,    \
      int wherefrom, void *cbhandle, double objval, double *x,          \
      int *isfeas_p, int *useraction_p
#define CPXS_CALLBACK_DELETENODE_ARGS  CPXCENVptr xenv,         \
   int wherefrom, void *cbhandle, CPXLONG seqnum, void *handle

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#ifdef __cplusplus
extern "C" {
#endif

typedef int (CPXPUBLIC CPXS_CALLBACK) (CPXS_CALLBACK_ARGS);
typedef int (CPXPUBLIC CPXS_CALLBACK_PROF)(CPXS_CALLBACK_PROF_ARGS);
typedef int (CPXPUBLIC CPXS_CALLBACK_BRANCH) (CPXS_CALLBACK_BRANCH_ARGS);
typedef int (CPXPUBLIC CPXS_CALLBACK_NODE) (CPXS_CALLBACK_NODE_ARGS);
typedef int (CPXPUBLIC CPXS_CALLBACK_HEURISTIC) (CPXS_CALLBACK_HEURISTIC_ARGS);
typedef int (CPXPUBLIC CPXS_CALLBACK_SOLVE) (CPXS_CALLBACK_SOLVE_ARGS);
typedef int (CPXPUBLIC CPXS_CALLBACK_INCUMBENT) (CPXS_CALLBACK_INCUMBENT_ARGS);
typedef void (CPXPUBLIC CPXS_CALLBACK_DELETENODE) (CPXS_CALLBACK_DELETENODE_ARGS);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetdeletenodecallbackfunc (CPXENVptr env,
                                  CPXS_CALLBACK_DELETENODE *callback,
                                  void *cbhandle);

CPXLIBAPI
void CPXPUBLIC
   CPXSgetdeletenodecallbackfunc (CPXCENVptr env,
                                  CPXS_CALLBACK_DELETENODE **callback_p,
                                  void **cbhandle_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetnodecallbackfunc (CPXENVptr env,
                            CPXS_CALLBACK_NODE *callback,
                            void *cbhandle);

CPXLIBAPI
void CPXPUBLIC
   CPXSgetnodecallbackfunc (CPXCENVptr env,
                            CPXS_CALLBACK_NODE **callback_p,
                            void **cbhandle_p);
CPXLIBAPI
int CPXPUBLIC
   CPXShybbaropt (CPXCENVptr env, CPXLPptr lp, int method);

CPXLIBAPI
int CPXPUBLIC
   CPXSbaropt (CPXCENVptr env, CPXLPptr lp);

CPXLIBAPI
CPXLPptr CPXPUBLIC
   CPXScreateprob (CPXCENVptr cenv, int *status_p, const char *probname_str);

CPXLIBAPI
CPXLPptr CPXPUBLIC
   CPXScloneprob (CPXCENVptr env, CPXCLPptr lp, int *status_p);

CPXLIBAPI
int CPXPUBLIC
   CPXScopylpwnames (CPXCENVptr env, CPXLPptr lp, CPXINT numcols, CPXINT numrows, int objsen, const double *obj, const double *rhs, const char *sense, const CPXINT *matbeg, const CPXINT *matcnt, const CPXINT *matind, const double *matval, const double *lb, const double *ub, const double *rngval, char const *const *colname, char const *const *rowname);

CPXLIBAPI
int CPXPUBLIC
   CPXScopylp (CPXCENVptr env, CPXLPptr lp, CPXINT numcols, CPXINT numrows, int objsen, const double *obj, const double *rhs, const char *sense, const CPXINT *matbeg, const CPXINT *matcnt, const CPXINT *matind, const double *matval, const double *lb, const double *ub, const double *rngval);

CPXLIBAPI
int CPXPUBLIC
   CPXScopyobjname (CPXCENVptr env, CPXLPptr lp, const char *objname_str);

CPXLIBAPI
int CPXPUBLIC
   CPXScopybase (CPXCENVptr env, CPXLPptr lp, const int *cstat, const int *rstat);

CPXLIBAPI
int CPXPUBLIC
   CPXScleanup (CPXCENVptr env, CPXLPptr lp, double eps);

CPXLIBAPI
int CPXPUBLIC
   CPXScopystart (CPXCENVptr env, CPXLPptr lp, const int *cstat, const int *rstat, const double *cprim, const double *rprim, const double *cdual, const double *rdual);

CPXLIBAPI
int CPXPUBLIC
   CPXSfreeprob (CPXCENVptr env, CPXLPptr *lp_p);

CPXLIBAPI
int CPXPUBLIC
   CPXScopynettolp (CPXCENVptr env, CPXLPptr lp, CPXCNETptr net);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETextract (CPXCENVptr env, CPXNETptr net, CPXCLPptr lp, CPXINT *colmap, CPXINT *rowmap);

CPXLIBAPI
int CPXPUBLIC
   CPXSlpopt (CPXCENVptr env, CPXLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSprimopt (CPXCENVptr env, CPXLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSdualopt (CPXCENVptr env, CPXLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXShybnetopt (CPXCENVptr env, CPXLPptr lp, int method);

CPXLIBAPI
int CPXPUBLIC
   CPXSsiftopt (CPXCENVptr env, CPXLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSpratio (CPXCENVptr env, CPXLPptr lp, CPXINT *goodlist, CPXINT goodlen, double *downratio, double *upratio, CPXINT *downleave, CPXINT *upleave, int *downleavestatus, int *upleavestatus, int *downstatus, int *upstatus);

CPXLIBAPI
int CPXPUBLIC
   CPXSdratio (CPXCENVptr env, CPXLPptr lp, CPXINT *goodlist, CPXINT goodlen, double *downratio, double *upratio, CPXINT *downenter, CPXINT *upenter, int *downstatus, int *upstatus);

CPXLIBAPI
int CPXPUBLIC
   CPXSpivot (CPXCENVptr env, CPXLPptr lp, CPXINT jenter, CPXINT ileave, int leavestat);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetphase2 (CPXCENVptr env, CPXLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXScheckpfeas (CPXCENVptr env, CPXLPptr lp, CPXINT *infeas_p);

CPXLIBAPI
int CPXPUBLIC
   CPXScheckdfeas (CPXCENVptr env, CPXLPptr lp, CPXINT *infeas_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSchecksoln (CPXCENVptr env, CPXLPptr lp, int *lpstatus_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSsolution (CPXCENVptr env, CPXCLPptr lp, int *lpstat_p, double *objval_p, double *x, double *pi, double *slack, double *dj);

CPXLIBAPI
int CPXPUBLIC
   CPXSsolninfo (CPXCENVptr env, CPXCLPptr lp, int *solnmethod_p, int *solntype_p, int *pfeasind_p, int *dfeasind_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetstat (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
char * CPXPUBLIC
   CPXSgetstatstring (CPXCENVptr env, int statind, char *buffer_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetmethod (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetobjval (CPXCENVptr env, CPXCLPptr lp, double *objval_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetx (CPXCENVptr env, CPXCLPptr lp, double *xout, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetax (CPXCENVptr env, CPXCLPptr lp, double *axout, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetpi (CPXCENVptr env, CPXCLPptr lp, double *piout, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetslack (CPXCENVptr env, CPXCLPptr lp, double *slackout, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetrowinfeas (CPXCENVptr env, CPXCLPptr lp, const double *x, double *infeasout, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcolinfeas (CPXCENVptr env, CPXCLPptr lp, const double *xin, double *infeasout, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetdj (CPXCENVptr env, CPXCLPptr lp, double *djout, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetgrad (CPXCENVptr env, CPXCLPptr lp, CPXINT j, CPXINT *head, double *x);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetijdiv (CPXCENVptr env, CPXCLPptr lp, CPXINT *idiv_p, CPXINT *jdiv_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetbase (CPXCENVptr env, CPXCLPptr lp, int *xcstat, int *xrstat);

CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXSgetitcnt (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXSgetphase1cnt (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXSgetsiftitcnt (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXSgetsiftphase1cnt (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXSgetbaritcnt (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXSgetcrossppushcnt (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXSgetcrosspexchcnt (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXSgetcrossdpushcnt (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXSgetcrossdexchcnt (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
CPXINT CPXPUBLIC
   CPXSgetpsbcnt (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
CPXINT CPXPUBLIC
   CPXSgetdsbcnt (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetdblquality (CPXCENVptr env, CPXCLPptr lp, double *quality_p, int what);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsolnpooldblquality (CPXCENVptr env, CPXCLPptr lp, int soln, double *quality_p, int what);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetintquality (CPXCENVptr env, CPXCLPptr lp, CPXINT *quality_p, int what);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsolnpoolintquality (CPXCENVptr env, CPXCLPptr lp, int soln, CPXINT *quality_p, int what);

CPXLIBAPI
int CPXPUBLIC
   CPXSrhssa (CPXCENVptr env, CPXCLPptr lp, CPXINT begin, CPXINT end, double *lower, double *upper);

CPXLIBAPI
int CPXPUBLIC
   CPXSboundsa (CPXCENVptr env, CPXCLPptr lp, CPXINT begin, CPXINT end, double *lblower, double *lbupper, double *ublower, double *ubupper);

CPXLIBAPI
int CPXPUBLIC
   CPXSobjsa (CPXCENVptr env, CPXCLPptr lp, CPXINT begin, CPXINT end, double *lower, double *upper);

CPXLIBAPI
int CPXPUBLIC
   CPXSrefineconflict (CPXCENVptr env, CPXLPptr lp, CPXINT *confnumrows_p, CPXINT *confnumcols_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetconflict (CPXCENVptr env, CPXCLPptr lp, int *confstat_p, CPXINT *rowind, int *rowstat, CPXINT *confnumrows_p, CPXINT *colind, int *colstat, CPXINT *confnumcols_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSrefineconflictext (CPXCENVptr env, CPXLPptr lp, CPXINT grpcnt, CPXINT concnt, const double *grppri, const CPXINT *grpbeg, const CPXINT *grpind, const char *grptype);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetconflictext (CPXCENVptr env, CPXCLPptr lp, int *grpstat, CPXINT beg, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSclpwrite (CPXCENVptr env, CPXCLPptr lp, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSrobustopt (CPXCENVptr env, CPXLPptr lp, CPXLPptr lblp, CPXLPptr ublp, double objchg, const double *maxchg_ext);

CPXLIBAPI
int CPXPUBLIC
   CPXSfeasopt (CPXCENVptr env, CPXLPptr lp, const double *rhs, const double *rng, const double *lb, const double *ub);

CPXLIBAPI
int CPXPUBLIC
   CPXSfeasoptext (CPXCENVptr env, CPXLPptr lp, CPXINT grpcnt, CPXINT concnt, const double *grppri, const CPXINT *grpbeg, const CPXINT *grpind, const char *grptype);

CPXLIBAPI
int CPXPUBLIC
   CPXSnewrows (CPXCENVptr env, CPXLPptr lp, CPXINT rcnt, const double *zrhs, const char *zsense, const double *zrngval, char const *const *zrname);

CPXLIBAPI
int CPXPUBLIC
   CPXSaddrows (CPXCENVptr env, CPXLPptr lp, CPXINT ccnt, CPXINT rcnt, CPXINT nzcnt, const double *zrhs, const char *zsense, const CPXINT *rmatbeg, const CPXINT *rmatind, const double *rmatval, char const *const *zcname, char const *const *zrname);

CPXLIBAPI
int CPXPUBLIC
   CPXSnewcols (CPXCENVptr env, CPXLPptr lp, CPXINT ccnt, const double *zobj, const double *zlb, const double *zub, const char *zctype, char const *const *zcname);

CPXLIBAPI
int CPXPUBLIC
   CPXSaddcols (CPXCENVptr env, CPXLPptr lp, CPXINT ccnt, CPXINT nzcnt, const double *zobj, const CPXINT *cmatbeg, const CPXINT *cmatind, const double *cmatval, const double *zlb, const double *zub, char const *const *zcname);

CPXLIBAPI
int CPXPUBLIC
   CPXSdelrows (CPXCENVptr env, CPXLPptr lp, CPXINT index1, CPXINT index2);

CPXLIBAPI
int CPXPUBLIC
   CPXSdelsetrows (CPXCENVptr env, CPXLPptr lp, CPXINT *mask);

CPXLIBAPI
int CPXPUBLIC
   CPXSdelcols (CPXCENVptr env, CPXLPptr lp, CPXINT index1, CPXINT index2);

CPXLIBAPI
int CPXPUBLIC
   CPXSdelsetcols (CPXCENVptr env, CPXLPptr lp, CPXINT *mask);

CPXLIBAPI
int CPXPUBLIC
   CPXSchgname (CPXCENVptr env, CPXLPptr lp, int key, CPXINT oldindex, const char *newname_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSchgrowname (CPXCENVptr env, CPXLPptr lp, CPXINT cnt, const CPXINT *indices, char const *const *newname);

CPXLIBAPI
int CPXPUBLIC
   CPXSchgcolname (CPXCENVptr env, CPXLPptr lp, CPXINT cnt, const CPXINT *indices, char const *const *newname);

CPXLIBAPI
int CPXPUBLIC
   CPXSdelnames (CPXCENVptr env, CPXLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSchgprobname (CPXCENVptr env, CPXLPptr lp, const char *probname_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSchgcoef (CPXCENVptr env, CPXLPptr lp, CPXINT i, CPXINT j, double newvalue);

CPXLIBAPI
int CPXPUBLIC
   CPXSchgcoeflist (CPXCENVptr env, CPXLPptr lp, CPXINT numcoefs, const CPXINT *rowlist, const CPXINT *collist, const double *vallist);

CPXLIBAPI
int CPXPUBLIC
   CPXSchgbds (CPXCENVptr env, CPXLPptr lp, CPXINT cnt, const CPXINT *idx, const char *lu, const double *bd);

CPXLIBAPI
int CPXPUBLIC
   CPXSchgobj (CPXCENVptr env, CPXLPptr lp, CPXINT cnt, const CPXINT *idx, const double *values);

CPXLIBAPI
int CPXPUBLIC
   CPXSchgrhs (CPXCENVptr env, CPXLPptr lp, CPXINT cnt, const CPXINT *idx, const double *values);

CPXLIBAPI
int CPXPUBLIC
   CPXSchgrngval (CPXCENVptr env, CPXLPptr lp, CPXINT cnt, const CPXINT *idx, const double *values);

CPXLIBAPI
int CPXPUBLIC
   CPXSchgsense (CPXCENVptr env, CPXLPptr lp, CPXINT cnt, const CPXINT *idx, const char *zsense);

CPXLIBAPI
void CPXPUBLIC
   CPXSchgobjsen (CPXCENVptr env, CPXLPptr lp, int maxormin);

CPXLIBAPI
int CPXPUBLIC
   CPXSchgprobtype (CPXCENVptr env, CPXLPptr lp, int type);

CPXLIBAPI
int CPXPUBLIC
   CPXSchgprobtypesolnpool (CPXCENVptr env, CPXLPptr lp, int type, int soln);

CPXLIBAPI
int CPXPUBLIC
   CPXScompletelp (CPXCENVptr env, CPXLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSpreaddrows (CPXCENVptr env, CPXLPptr lp, CPXINT rcnt, CPXINT nzcnt, const double *zrhs, const char *zsense, const CPXINT *rmatbeg, const CPXINT *rmatind, const double *rmatval, char const *const *zrname);

CPXLIBAPI
int CPXPUBLIC
   CPXSprechgobj (CPXCENVptr env, CPXLPptr lp, CPXINT cnt, const CPXINT *idx, const double *values);

CPXLIBAPI
CPXINT CPXPUBLIC
   CPXSgetnumcols (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
CPXINT CPXPUBLIC
   CPXSgetnumrows (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
CPXINT CPXPUBLIC
   CPXSgetnumnz (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetobjsen (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetobj (CPXCENVptr env, CPXCLPptr lp, double *xobj, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetrhs (CPXCENVptr env, CPXCLPptr lp, double *xrhs, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsense (CPXCENVptr env, CPXCLPptr lp, char *xsense, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcols (CPXCENVptr env, CPXCLPptr lp, CPXINT *nzcnt_p, CPXINT *cmatbeg, CPXINT *cmatind, double *cmatval, CPXINT cmatsz, CPXINT *surplus_p, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetrows (CPXCENVptr env, CPXCLPptr lp, CPXINT *nzcnt_p, CPXINT *rmatbeg, CPXINT *rmatind, double *rmatval, CPXINT rmatsz, CPXINT *surplus_p, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetlb (CPXCENVptr env, CPXCLPptr lp, double *xlb, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetub (CPXCENVptr env, CPXCLPptr lp, double *xub, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetrngval (CPXCENVptr env, CPXCLPptr lp, double *xrngval, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetprobname (CPXCENVptr env, CPXCLPptr lp, char *buf_str, CPXSIZE storesz, CPXSIZE *surplus_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetobjname (CPXCENVptr env, CPXCLPptr lp, char *buf_str, CPXSIZE storesz, CPXSIZE *surplus_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcolname (CPXCENVptr env, CPXCLPptr lp, char **name, char *store, CPXSIZE storesz, CPXSIZE *surplus_p, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetrowname (CPXCENVptr env, CPXCLPptr lp, char **name, char *store, CPXSIZE storesz, CPXSIZE *surplus_p, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcoef (CPXCENVptr env, CPXCLPptr lp, CPXINT i, CPXINT j, double *coef_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetrowindex (CPXCENVptr env, CPXCLPptr lp, const char *lname_str, CPXINT *index_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcolindex (CPXCENVptr env, CPXCLPptr lp, const char *lname_str, CPXINT *index_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetprobtype (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSreadcopyprob (CPXCENVptr env, CPXLPptr lp, const char *filename_str, const char *filetype_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSreadcopybase (CPXCENVptr env, CPXLPptr lp, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSreadcopysol (CPXCENVptr env, CPXLPptr lp, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSwriteprob (CPXCENVptr env, CPXCLPptr lp, const char *filename_str, const char *filetype_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSmbasewrite (CPXCENVptr env, CPXCLPptr lp, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSsolwrite (CPXCENVptr env, CPXCLPptr lp, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSsolwritesolnpool (CPXCENVptr env, CPXCLPptr lp, int soln, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSsolwritesolnpoolall (CPXCENVptr env, CPXCLPptr lp, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSembwrite (CPXCENVptr env, CPXLPptr lp, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSdperwrite (CPXCENVptr env, CPXLPptr lp, const char *filename_str, double epsilon);

CPXLIBAPI
int CPXPUBLIC
   CPXSpperwrite (CPXCENVptr env, CPXLPptr lp, const char *filename_str, double epsilon);

CPXLIBAPI
int CPXPUBLIC
   CPXSpreslvwrite (CPXCENVptr env, CPXLPptr lp, const char *filename_str, double *objoff_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSdualwrite (CPXCENVptr env, CPXCLPptr lp, const char *filename_str, double *objshift_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetdefaults (CPXENVptr env);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetintparam (CPXENVptr env, int whichparam, CPXINT newvalue);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetlongparam (CPXENVptr env, int whichparam, CPXLONG newvalue);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetdblparam (CPXENVptr env, int whichparam, double newvalue);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetstrparam (CPXENVptr env, int whichparam, const char *newvalue_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetintparam (CPXCENVptr env, int whichparam, CPXINT *value_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetlongparam (CPXCENVptr env, int whichparam, CPXLONG *value_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetdblparam (CPXCENVptr env, int whichparam, double *value_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetstrparam (CPXCENVptr env, int whichparam, char *value_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSinfointparam (CPXCENVptr env, int whichparam, CPXINT *defvalue_p, CPXINT *minvalue_p, CPXINT *maxvalue_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSinfolongparam (CPXCENVptr env, int whichparam, CPXLONG *defvalue_p, CPXLONG *minvalue_p, CPXLONG *maxvalue_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSinfodblparam (CPXCENVptr env, int whichparam, double *defvalue_p, double *minvalue_p, double *maxvalue_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSinfostrparam (CPXCENVptr env, int whichparam, char *defvalue_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetparamname (CPXCENVptr env, int whichparam, char *name_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetparamnum (CPXCENVptr env, const char *name_str, int *whichparam_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetparamtype (CPXCENVptr env, int whichparam, int *paramtype);

CPXLIBAPI
int CPXPUBLIC
   CPXSreadcopyparam (CPXENVptr env, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSwriteparam (CPXCENVptr env, const char *filename);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetchgparam (CPXCENVptr env, int *cnt_p, int *paramnum, int pspace, int *surplus_p);

CPXLIBAPI
int CPXPUBLIC
   CPXStuneparam (CPXENVptr env, CPXLPptr lp, int icnt, const int *inum, const CPXINT *ival, int dcnt, const int *dnum, const double *dval, int scnt, const int *snum, char const *const *sval, int *tunestat_p);

CPXLIBAPI
int CPXPUBLIC
   CPXStuneparamprobset (CPXENVptr env, int filecnt, char const *const *filename, char const *const *filetype, int icnt, const int *inum, const CPXINT *ival, int dcnt, const int *dnum, const double *dval, int scnt, const int *snum, char const *const *sval, int *tunestat_p);

CPXLIBAPI
CPXCCHARptr CPXPUBLIC
   CPXSversion (CPXCENVptr env);

CPXLIBAPI
CPXENVptr CPXPUBLIC
   CPXSopenCPLEX (int *status_p);

CPXLIBAPI
CPXENVptr CPXPUBLIC
   CPXSopenCPLEXruntime (int *status_p, int serialnum, const char *licenvstring_str);

CPXLIBAPI
int CPXPUBLIC
   CPXScloseCPLEX (CPXENVptr *env_p);

CPXLIBAPI
CPXENVptr CPXPUBLIC
   CPXSparenv (CPXENVptr env, int *status_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSfreeparenv (CPXENVptr env, CPXENVptr *child_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetchannels (CPXCENVptr env, CPXCHANNELptr *cpxresults_p, CPXCHANNELptr *cpxwarning_p, CPXCHANNELptr *cpxerror_p, CPXCHANNELptr *cpxlog_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetlogfile (CPXENVptr env, CPXFILEptr lfile);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetlogfile (CPXCENVptr env, CPXFILEptr *logfile_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSmsgstr (CPXCHANNELptr channel, const char *msg_str);

CPXLIBAPI
void CPXPUBLIC
   CPXSflushchannel (CPXCENVptr env, CPXCHANNELptr channel);

CPXLIBAPI
int CPXPUBLIC
   CPXSflushstdchannels (CPXCENVptr env);

CPXLIBAPI
CPXCHANNELptr CPXPUBLIC
   CPXSaddchannel (CPXENVptr env);

CPXLIBAPI
int CPXPUBLIC
   CPXSaddfpdest (CPXCENVptr env, CPXCHANNELptr channel, CPXFILEptr fileptr);

CPXLIBAPI
int CPXPUBLIC
   CPXSdelfpdest (CPXCENVptr env, CPXCHANNELptr channel, CPXFILEptr fileptr);

CPXLIBAPI
int CPXPUBLIC
   CPXSaddfuncdest (CPXCENVptr env, CPXCHANNELptr channel, void *handle, void(CPXPUBLIC*msgfunction)(void *, const char *));

CPXLIBAPI
int CPXPUBLIC
   CPXSdelfuncdest (CPXCENVptr env, CPXCHANNELptr channel, void *handle, void(CPXPUBLIC*msgfunction)(void *, const char *));

CPXLIBAPI
void CPXPUBLIC
   CPXSdelchannel (CPXENVptr env, CPXCHANNELptr *channel_p);

CPXLIBAPI
void CPXPUBLIC
   CPXSdisconnectchannel (CPXCENVptr env, CPXCHANNELptr channel);

CPXLIBAPI
CPXCCHARptr CPXPUBLIC
   CPXSgeterrorstring (CPXCENVptr env, int errcode, char *buffer_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetlpcallbackfunc (CPXENVptr env, int(CPXPUBLIC*callback)(CPXCENVptr, void *, int, void *), void *cbhandle);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetnetcallbackfunc (CPXENVptr env, int(CPXPUBLIC*callback)(CPXCENVptr, void *, int, void *), void *cbhandle);

CPXLIBAPI
int CPXPUBLIC
   CPXSsettuningcallbackfunc (CPXENVptr env, int(CPXPUBLIC*callback)(CPXCENVptr, void *, int, void *), void *cbhandle);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcallbackinfo (CPXCENVptr env, void *cbdata, int wherefrom, int whichinfo, void *result_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetlpcallbackfunc (CPXCENVptr env, int(CPXPUBLIC**callback_p)(CPXCENVptr, void *, int, void *), void **cbhandle_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetnetcallbackfunc (CPXCENVptr env, int(CPXPUBLIC**callback_p)(CPXCENVptr, void *, int, void *), void **cbhandle_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgettuningcallbackfunc (CPXCENVptr env, int(CPXPUBLIC**callback_p)(CPXCENVptr, void *, int, void *), void **cbhandle_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetprofcallbackfunc (CPXENVptr env, int(CPXPUBLIC*callback)(CPXCENVptr, int, void *), void *cbhandle);

CPXLIBAPI
CPXFILEptr CPXPUBLIC
   CPXSfopen (const char *filename_str, const char *type_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSfclose (CPXFILEptr stream);

CPXLIBAPI
int CPXPUBLIC
   CPXSfputs (const char *s_str, CPXFILEptr stream);

CPXLIBAPI
CPXVOIDptr CPXPUBLIC
   CPXSmalloc (size_t size);

CPXLIBAPI
CPXVOIDptr CPXPUBLIC
   CPXSrealloc (void *ptr, size_t size);

CPXLIBAPI
CPXVOIDptr CPXPUBLIC
   CPXSmemcpy (void *s1, void *s2, size_t n);

CPXLIBAPI
void CPXPUBLIC
   CPXSfree (void *ptr);

CPXLIBAPI
size_t CPXPUBLIC
   CPXSstrlen (const char *s_str);

CPXLIBAPI
CPXCHARptr CPXPUBLIC
   CPXSstrcpy (char *dest_str, const char *src_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSputenv (const char *envsetting_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetterminate (CPXENVptr env, volatile int *terminate_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetbhead (CPXCENVptr env, CPXCLPptr lp, CPXINT *head, double *x);

CPXLIBAPI
int CPXPUBLIC
   CPXSbinvcol (CPXCENVptr env, CPXCLPptr lp, CPXINT j, double *x);

CPXLIBAPI
int CPXPUBLIC
   CPXSbinvrow (CPXCENVptr env, CPXCLPptr lp, CPXINT i, double *y);

CPXLIBAPI
int CPXPUBLIC
   CPXSbinvacol (CPXCENVptr env, CPXCLPptr lp, CPXINT j, double *x);

CPXLIBAPI
int CPXPUBLIC
   CPXSbinvarow (CPXCENVptr env, CPXCLPptr lp, CPXINT i, double *z);

CPXLIBAPI
int CPXPUBLIC
   CPXSftran (CPXCENVptr env, CPXCLPptr lp, double *x);

CPXLIBAPI
int CPXPUBLIC
   CPXSbtran (CPXCENVptr env, CPXCLPptr lp, double *y);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetijrow (CPXCENVptr env, CPXCLPptr lp, CPXINT i, CPXINT j, CPXINT *row_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetray (CPXCENVptr env, CPXCLPptr lp, double *z);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetweight (CPXCENVptr env, CPXCLPptr lp, CPXINT rcnt, const CPXINT *rmatbeg, const CPXINT *rmatind, const double *rmatval, double *weight, int dpriind);

CPXLIBAPI
int CPXPUBLIC
   CPXSmdleave (CPXCENVptr env, CPXLPptr lp, const CPXINT *goodlist, CPXINT goodlen, double *downratio, double *upratio);

CPXLIBAPI
int CPXPUBLIC
   CPXSstrongbranch (CPXCENVptr env, CPXLPptr lp, const CPXINT *goodlist, CPXINT goodlen, double *downpen, double *uppen, CPXLONG itlim);

CPXLIBAPI
int CPXPUBLIC
   CPXSdualfarkas (CPXCENVptr env, CPXCLPptr lp, double *y, double *proof_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetobjoffset (CPXCENVptr env, CPXCLPptr lp, double *objoffset_p);

CPXLIBAPI
int CPXPUBLIC
   CPXScopypartialbase (CPXCENVptr env, CPXLPptr lp, CPXINT rcnt, const CPXINT *rindices, const int *xrstat, CPXINT ccnt, const CPXINT *cindices, const int *xcstat);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetbasednorms (CPXCENVptr env, CPXCLPptr lp, int *cstat, int *rstat, double *dnorm);

CPXLIBAPI
int CPXPUBLIC
   CPXScopybasednorms (CPXCENVptr env, CPXLPptr lp, const int *cstat, const int *rstat, const double *dnorm);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetdnorms (CPXCENVptr env, CPXCLPptr lp, double *norm, CPXINT *head, CPXINT *len_p);

CPXLIBAPI
int CPXPUBLIC
   CPXScopydnorms (CPXCENVptr env, CPXLPptr lp, const double *norm, const CPXINT *head, CPXINT len);

CPXLIBAPI
void CPXPUBLIC
   CPXSkilldnorms (CPXLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetpnorms (CPXCENVptr env, CPXCLPptr lp, double *cnorm, double *rnorm, CPXINT *len_p);

CPXLIBAPI
int CPXPUBLIC
   CPXScopypnorms (CPXCENVptr env, CPXLPptr lp, const double *cnorm, const double *rnorm, CPXINT len);

CPXLIBAPI
void CPXPUBLIC
   CPXSkillpnorms (CPXLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSpivotin (CPXCENVptr env, CPXLPptr lp, const CPXINT *rlist, CPXINT rlen);

CPXLIBAPI
int CPXPUBLIC
   CPXSpivotout (CPXCENVptr env, CPXLPptr lp, const CPXINT *clist, CPXINT clen);

CPXLIBAPI
int CPXPUBLIC
   CPXSunscaleprob (CPXCENVptr env, CPXLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXStightenbds (CPXCENVptr env, CPXLPptr lp, CPXINT cnt, const CPXINT *idx, const char *lu, const double *bd);

CPXLIBAPI
int CPXPUBLIC
   CPXSpresolve (CPXCENVptr env, CPXLPptr lp, int method);

CPXLIBAPI
int CPXPUBLIC
   CPXSbasicpresolve (CPXCENVptr env, CPXLPptr lp, double *redlb, double *redub, int *rstat);

CPXLIBAPI
int CPXPUBLIC
   CPXSslackfromx (CPXCENVptr env, CPXCLPptr lp, const double *x, double *slack);

CPXLIBAPI
int CPXPUBLIC
   CPXSdjfrompi (CPXCENVptr env, CPXCLPptr lp, const double *pi, double *dj);

CPXLIBAPI
int CPXPUBLIC
   CPXSqpdjfrompi (CPXCENVptr env, CPXCLPptr lp, const double *pi, const double *x, double *dj);

CPXLIBAPI
int CPXPUBLIC
   CPXSfreepresolve (CPXCENVptr env, CPXLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetredlp (CPXCENVptr env, CPXCLPptr lp, CPXCLPptr *redlp_p);

CPXLIBAPI
int CPXPUBLIC
   CPXScrushx (CPXCENVptr env, CPXCLPptr lp, const double *x, double *prex);

CPXLIBAPI
int CPXPUBLIC
   CPXSuncrushx (CPXCENVptr env, CPXCLPptr lp, double *x, const double *prex);

CPXLIBAPI
int CPXPUBLIC
   CPXScrushpi (CPXCENVptr env, CPXCLPptr lp, const double *pi, double *prepi);

CPXLIBAPI
int CPXPUBLIC
   CPXSuncrushpi (CPXCENVptr env, CPXCLPptr lp, double *pi, const double *prepi);

CPXLIBAPI
int CPXPUBLIC
   CPXSqpuncrushpi (CPXCENVptr env, CPXCLPptr lp, double *pi, const double *prepi, const double *x);

CPXLIBAPI
int CPXPUBLIC
   CPXScrushform (CPXCENVptr env, CPXCLPptr lp, CPXINT len, const CPXINT *ind, const double *val, CPXINT *plen_p, double *poffset_p, CPXINT *pind, double *pval);

CPXLIBAPI
int CPXPUBLIC
   CPXSuncrushform (CPXCENVptr env, CPXCLPptr lp, CPXINT plen, const CPXINT *pind, const double *pval, CPXINT *len_p, double *const_p, CPXINT *ind, double *val);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetprestat (CPXCENVptr env, CPXCLPptr lp, int *prestat_p, CPXINT *pcstat, CPXINT *prstat, CPXINT *ocstat, CPXINT *orstat);

CPXLIBAPI
int CPXPUBLIC
   CPXScopyprotected (CPXCENVptr env, CPXLPptr lp, CPXINT xcnt, const CPXINT *xindices);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetprotected (CPXCENVptr env, CPXCLPptr lp, CPXINT *xcnt_p, CPXINT *xindices, CPXINT pspace, CPXINT *surplus_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgettime (CPXCENVptr env, double *timestamp);

CPXLIBAPI
int CPXPUBLIC
   CPXSlpwrite (CPXCENVptr env, CPXCLPptr lp, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSlprewrite (CPXCENVptr env, CPXCLPptr lp, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSmpswrite (CPXCENVptr env, CPXCLPptr lp, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSmpsrewrite (CPXCENVptr env, CPXCLPptr lp, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSsavwrite (CPXCENVptr env, CPXCLPptr lp, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSaddindconstr (CPXCENVptr env, CPXLPptr lp, CPXINT indvar, int complemented, CPXINT nzcnt, double rhs, int sense, const CPXINT *linind, const double *linval, const char *indname_str);

CPXLIBAPI
int CPXPUBLIC
   CPXScopyctype (CPXCENVptr env, CPXLPptr lp, const char *xctype);

CPXLIBAPI
int CPXPUBLIC
   CPXScopyorder (CPXCENVptr env, CPXLPptr lp, CPXINT xcnt, const CPXINT *xindices, const CPXINT *xpriority, const int *xdirection);

CPXLIBAPI
int CPXPUBLIC
   CPXScopysos (CPXCENVptr env, CPXLPptr lp, CPXINT nsos, CPXINT lastsosnz, const char *sostype, const CPXINT *sosbeg, const CPXINT *sosind, const double *soswt, char const *const *sosname);

CPXLIBAPI
int CPXPUBLIC
   CPXSchgmipstarts (CPXCENVptr env, CPXLPptr lp, int mcnt, const int *mipstartindices, CPXINT nzcnt, const CPXINT *beg, const CPXINT *varindices, const double *values, const int *effortlevel);

CPXLIBAPI
int CPXPUBLIC
   CPXSaddmipstarts (CPXCENVptr env, CPXLPptr lp, int mcnt, CPXINT nzcnt, const CPXINT *beg, const CPXINT *varindices, const double *values, const int *effortlevel, char const *const *mipstartname);

CPXLIBAPI
int CPXPUBLIC
   CPXSdelmipstarts (CPXCENVptr env, CPXLPptr lp, int begin, int end);

CPXLIBAPI
int CPXPUBLIC
   CPXSdelsetmipstarts (CPXCENVptr env, CPXLPptr lp, int *delstat);

CPXLIBAPI
int CPXPUBLIC
   CPXSrefinemipstartconflict (CPXCENVptr env, CPXLPptr lp, int mipstartindex, CPXINT *confnumrows_p, CPXINT *confnumcols_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSrefinemipstartconflictext (CPXCENVptr env, CPXLPptr lp, int mipstartindex, CPXINT grpcnt, CPXINT concnt, const double *grppref, const CPXINT *grpbeg, const CPXINT *grpind, const char *grptype);

CPXLIBAPI
int CPXPUBLIC
   CPXSmipopt (CPXCENVptr env, CPXLPptr lp);

CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXSgetmipitcnt (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetbestobjval (CPXCENVptr env, CPXCLPptr lp, double *objval_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetmiprelgap (CPXCENVptr env, CPXCLPptr lp, double *gap_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcutoff (CPXCENVptr env, CPXCLPptr lp, double *cutoff_p);

CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXSgetnodecnt (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXSgetnodeleftcnt (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXSgetnodeint (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetnumcuts (CPXCENVptr env, CPXCLPptr lp, int which, CPXINT *num_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetnummipstarts (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetmipstarts (CPXCENVptr env, CPXCLPptr lp, CPXINT *nzcnt_p, CPXINT *beg, CPXINT *varindices, double *values, int *effortlevel, CPXINT startspace, CPXINT *surplus_p, int begin, int end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetmipstartname (CPXCENVptr env, CPXCLPptr lp, char **name, char *store, CPXSIZE storesz, CPXSIZE *surplus_p, int begin, int end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetmipstartindex (CPXCENVptr env, CPXCLPptr lp, const char *lname_str, int *idx_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsubstat (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsubmethod (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSchgctype (CPXCENVptr env, CPXLPptr lp, CPXINT cnt, const CPXINT *indices, const char *zctype);

CPXLIBAPI
int CPXPUBLIC
   CPXSaddsos (CPXCENVptr env, CPXLPptr lp, CPXINT nsos, CPXINT lastsosnz, const char *sostype, const CPXINT *sosbeg, const CPXINT *sosind, const double *soswt, char const *const *sosname);

CPXLIBAPI
int CPXPUBLIC
   CPXSdelsetsos (CPXCENVptr env, CPXLPptr lp, CPXINT *delset);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetctype (CPXCENVptr env, CPXCLPptr lp, char *xctype, CPXINT begin, CPXINT end);

CPXLIBAPI
CPXINT CPXPUBLIC
   CPXSgetnumsos (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsos (CPXCENVptr env, CPXCLPptr lp, CPXINT *numsosnz_p, char *xsostype, CPXINT *xsosbeg, CPXINT *xsosind, double *xsoswt, CPXINT sosspace, CPXINT *surplus_p, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsosname (CPXCENVptr env, CPXCLPptr lp, char **name, char *store, CPXSIZE storesz, CPXSIZE *surplus_p, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsosindex (CPXCENVptr env, CPXCLPptr lp, const char *lname_str, CPXINT *index_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsosinfeas (CPXCENVptr env, CPXCLPptr lp, const double *xin, double *infeasout, CPXINT begin, CPXINT end);

CPXLIBAPI
CPXINT CPXPUBLIC
   CPXSgetnumindconstrs (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetindconstr (CPXCENVptr env, CPXCLPptr lp, CPXINT *indvar_p, int *complemented_p, CPXINT *nzcnt_p, double *rhs_p, char *sense_p, CPXINT *ind, double *val, CPXINT space, CPXINT *surplus_p, CPXINT which);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetindconstrindex (CPXCENVptr env, CPXCLPptr lp, const char *lname_str, CPXINT *idx_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetindconstrname (CPXCENVptr env, CPXCLPptr lp, char *store, CPXSIZE storesz, CPXSIZE *surplus_p, CPXINT which);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetindconstrslack (CPXCENVptr env, CPXCLPptr lp, double *indslack, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSindconstrslackfromx (CPXCENVptr env, CPXCLPptr lp, const double *x, double *indslack);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetindconstrinfeas (CPXCENVptr env, CPXCLPptr lp, const double *xin, double *infeasout, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSdelindconstrs (CPXCENVptr env, CPXLPptr lp, CPXINT begin, CPXINT end);

CPXLIBAPI
CPXINT CPXPUBLIC
   CPXSgetnumint (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
CPXINT CPXPUBLIC
   CPXSgetnumbin (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
CPXINT CPXPUBLIC
   CPXSgetnumsemicont (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
CPXINT CPXPUBLIC
   CPXSgetnumsemiint (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetorder (CPXCENVptr env, CPXCLPptr lp, CPXINT *xcnt_p, CPXINT *xindices, CPXINT *xpriority, int *xdirection, CPXINT ordspace, CPXINT *surplus_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSpopulate (CPXCENVptr env, CPXLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsolnpoolnumfilters (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSaddsolnpooldivfilter (CPXCENVptr env, CPXLPptr lp, double lower_cutoff, double upper_cutoff, CPXINT num, const CPXINT *ind, const double *weights, const double *refval, const char *fname);

CPXLIBAPI
int CPXPUBLIC
   CPXSaddsolnpoolrngfilter (CPXCENVptr env, CPXLPptr lp, double lb, double ub, CPXINT num, const CPXINT *ind, const double *val, const char *fname);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsolnpoolfiltertype (CPXCENVptr env, CPXCLPptr lp, int *ftype_p, int which);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsolnpooldivfilter (CPXCENVptr env, CPXCLPptr lp, double *limit1_p, double *limit2_p, CPXINT *num_p, CPXINT *ind, double *val, double *refval, CPXINT space, CPXINT *surplus_p, int which);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsolnpoolfilter (CPXCENVptr env, CPXCLPptr lp, int *ftype_p, double *lowercutoff_p, double *upper_cutoff_p, int *nzcnt_p, int *ind, double *val, double *refval, int space, int *surplus_p, int which);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsolnpoolrngfilter (CPXCENVptr env, CPXCLPptr lp, double *limit1_p, double *limit2_p, CPXINT *num_p, CPXINT *ind, double *val, CPXINT space, CPXINT *surplus_p, int which);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsolnpoolfiltername (CPXCENVptr env, CPXCLPptr lp, char *store, CPXSIZE storesz, CPXSIZE *surplus_p, int which);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsolnpoolfilterindex (CPXCENVptr env, CPXCLPptr lp, const char *lname_str, int *index_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSdelsolnpoolfilters (CPXCENVptr env, CPXLPptr lp, int begin, int end);

CPXLIBAPI
int CPXPUBLIC
   CPXSdelsetsolnpoolfilters (CPXCENVptr env, CPXLPptr lp, int *delstat);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsolnpoolnumsolns (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsolnpoolnumreplaced (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsolnpoolmeanobjval (CPXCENVptr env, CPXCLPptr lp, double *meanobjval_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsolnpoolobjval (CPXCENVptr env, CPXCLPptr lp, int soln, double *objval_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsolnpoolx (CPXCENVptr env, CPXCLPptr lp, int soln, double *x, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsolnpoolslack (CPXCENVptr env, CPXCLPptr lp, int num, double *slack, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsolnpoolqconstrslack (CPXCENVptr env, CPXCLPptr lp, int num, double *slack, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsolnpoolsolnname (CPXCENVptr env, CPXCLPptr lp, char *store, CPXSIZE storesz, CPXSIZE *surplus_p, int which);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetsolnpoolsolnindex (CPXCENVptr env, CPXCLPptr lp, const char *lname_str, int *index_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSdelsolnpoolsolns (CPXCENVptr env, CPXLPptr lp, int begin, int end);

CPXLIBAPI
int CPXPUBLIC
   CPXSdelsetsolnpoolsolns (CPXCENVptr env, CPXLPptr lp, int *delstat);

CPXLIBAPI
int CPXPUBLIC
   CPXSreadcopyorder (CPXCENVptr env, CPXLPptr lp, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSreadcopysolnpoolfilters (CPXCENVptr env, CPXLPptr lp, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSreadcopymipstarts (CPXCENVptr env, CPXLPptr lp, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSordwrite (CPXCENVptr env, CPXCLPptr lp, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSwritemipstarts (CPXCENVptr env, CPXCLPptr lp, const char *filename_str, int begin, int end);

CPXLIBAPI
int CPXPUBLIC
   CPXSfltwrite (CPXCENVptr env, CPXCLPptr lp, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetinfocallbackfunc (CPXENVptr env, int(CPXPUBLIC*callback)(CPXCENVptr, void *, int, void *), void *cbhandle);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetinfocallbackfunc (CPXCENVptr env, int(CPXPUBLIC**callback_p)(CPXCENVptr, void *, int, void *), void **cbhandle_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetmipcallbackfunc (CPXENVptr env, int(CPXPUBLIC*callback)(CPXCENVptr, void *, int, void *), void *cbhandle);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetmipcallbackfunc (CPXCENVptr env, int(CPXPUBLIC**callback_p)(CPXCENVptr, void *, int, void *), void **cbhandle_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetbranchcallbackfunc (CPXENVptr env, int(CPXPUBLIC*branchcallback)(CALLBACK_BRANCH_ARGS), void *cbhandle);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetbranchnosolncallbackfunc (CPXENVptr env, int(CPXPUBLIC*branchnosolncallback)(CALLBACK_BRANCH_ARGS), void *cbhandle);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetlazyconstraintcallbackfunc (CPXENVptr env, int(CPXPUBLIC*lazyconcallback)(CALLBACK_CUT_ARGS), void *cbhandle);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetusercutcallbackfunc (CPXENVptr env, int(CPXPUBLIC*cutcallback)(CALLBACK_CUT_ARGS), void *cbhandle);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetheuristiccallbackfunc (CPXENVptr env, int(CPXPUBLIC*heuristiccallback)(CALLBACK_HEURISTIC_ARGS), void *cbhandle);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetincumbentcallbackfunc (CPXENVptr env, int(CPXPUBLIC*incumbentcallback)(CALLBACK_INCUMBENT_ARGS), void *cbhandle);

CPXLIBAPI
int CPXPUBLIC
   CPXSsetsolvecallbackfunc (CPXENVptr env, int(CPXPUBLIC*solvecallback)(CALLBACK_SOLVE_ARGS), void *cbhandle);

CPXLIBAPI
void CPXPUBLIC
   CPXSgetbranchcallbackfunc (CPXCENVptr env, int(CPXPUBLIC**branchcallback_p)(CALLBACK_BRANCH_ARGS), void **cbhandle_p);

CPXLIBAPI
void CPXPUBLIC
   CPXSgetbranchnosolncallbackfunc (CPXCENVptr env, int(CPXPUBLIC**branchnosolncallback_p)(CALLBACK_BRANCH_ARGS), void **cbhandle_p);

CPXLIBAPI
void CPXPUBLIC
   CPXSgetlazyconstraintcallbackfunc (CPXCENVptr env, int(CPXPUBLIC**cutcallback_p)(CALLBACK_CUT_ARGS), void **cbhandle_p);

CPXLIBAPI
void CPXPUBLIC
   CPXSgetusercutcallbackfunc (CPXCENVptr env, int(CPXPUBLIC**cutcallback_p)(CALLBACK_CUT_ARGS), void **cbhandle_p);

CPXLIBAPI
void CPXPUBLIC
   CPXSgetheuristiccallbackfunc (CPXCENVptr env, int(CPXPUBLIC**heuristiccallback_p)(CALLBACK_HEURISTIC_ARGS), void **cbhandle_p);

CPXLIBAPI
void CPXPUBLIC
   CPXSgetincumbentcallbackfunc (CPXCENVptr env, int(CPXPUBLIC**incumbentcallback_p)(CALLBACK_INCUMBENT_ARGS), void **cbhandle_p);

CPXLIBAPI
void CPXPUBLIC
   CPXSgetsolvecallbackfunc (CPXCENVptr env, int(CPXPUBLIC**solvecallback_p)(CALLBACK_SOLVE_ARGS), void **cbhandle_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcallbacknodelp (CPXCENVptr env, void *cbdata, int wherefrom, CPXLPptr *nodelp_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcallbacknodeinfo (CPXCENVptr env, void *cbdata, int wherefrom, CPXLONG nodenum, int whichinfo, void *result_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcallbackseqinfo (CPXCENVptr env, void *cbdata, int wherefrom, CPXLONG seqid, int whichinfo, void *result_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcallbacksosinfo (CPXCENVptr env, void *cbdata, int wherefrom, CPXINT sosindex, CPXINT member, int whichinfo, void *result_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcallbackindicatorinfo (CPXCENVptr env, void *cbdata, int wherefrom, CPXINT iindex, int whichinfo, void *result_p);

CPXLIBAPI
int CPXPUBLIC
   CPXScutcallbackadd (CPXCENVptr env, void *cbdata, int wherefrom, CPXINT nzcnt, double rhs, int sense, const CPXINT *cutind, const double *cutval, int purgeable);

CPXLIBAPI
int CPXPUBLIC
   CPXScutcallbackaddlocal (CPXCENVptr env, void *cbdata, int wherefrom, CPXINT nzcnt, double rhs, int sense, const CPXINT *cutind, const double *cutval);

CPXLIBAPI
int CPXPUBLIC
   CPXSbranchcallbackbranchbds (CPXCENVptr env, void *cbdata, int wherefrom, double xnodeest, CPXINT varcnt, const CPXINT *varind, const char *varlu, const int *varbd, void *userhandle, CPXLONG *nodeid_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcallbacknodex (CPXCENVptr env, void *cbdata, int wherefrom, double *x, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcallbacknodeobjval (CPXCENVptr env, void *cbdata, int wherefrom, double *objval_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcallbackctype (CPXCENVptr env, void *cbdata, int wherefrom, char *xctype, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcallbackorder (CPXCENVptr env, void *cbdata, int wherefrom, CPXINT *xpri, int *xdir, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcallbackpseudocosts (CPXCENVptr env, void *cbdata, int wherefrom, double *uppc, double *downpc, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcallbackincumbent (CPXCENVptr env, void *cbdata, int wherefrom, double *x, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcallbacknodeintfeas (CPXCENVptr env, void *cbdata, int wherefrom, int *feas, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcallbackgloballb (CPXCENVptr env, void *cbdata, int wherefrom, double *xlb, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcallbackglobalub (CPXCENVptr env, void *cbdata, int wherefrom, double *xub, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcallbacknodelb (CPXCENVptr env, void *cbdata, int wherefrom, double *xlb, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcallbacknodeub (CPXCENVptr env, void *cbdata, int wherefrom, double *xub, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcallbacklp (CPXCENVptr env, void *cbdata, int wherefrom, CPXCLPptr *lp_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcallbacknodestat (CPXCENVptr env, void *cbdata, int wherefrom, int *nodestat_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetcallbackbranchconstraints (CPXCENVptr env, void *cbdata, int wherefrom, int which, CPXINT *cuts_p, CPXINT *nzcnt_p, double *rhs, char *sense, CPXINT *rmatbeg, CPXINT *rmatind, double *rmatval, CPXINT rmatsz, CPXINT *surplus_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSaddusercuts (CPXCENVptr env, CPXLPptr lp, CPXINT rcnt, CPXINT lastnz, const double *rhs, const char *sense, const CPXINT *rmatbeg, const CPXINT *rmatind, const double *rmatval, char const *const *rowname);

CPXLIBAPI
int CPXPUBLIC
   CPXSaddlazyconstraints (CPXCENVptr env, CPXLPptr lp, CPXINT rcnt, CPXINT lastnz, const double *rhs, const char *sense, const CPXINT *rmatbeg, const CPXINT *rmatind, const double *rmatval, char const *const *rowname);

CPXLIBAPI
int CPXPUBLIC
   CPXSfreeusercuts (CPXCENVptr env, CPXLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSfreelazyconstraints (CPXCENVptr env, CPXLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSordread (CPXCENVptr env, const char *filename, CPXINT cols, char const *const *cname, CPXINT *cnt, CPXINT *indices, CPXINT *priority, int *direction);

CPXLIBAPI
CPXNETptr CPXPUBLIC
   CPXSNETcreateprob (CPXENVptr env, int *status_p, const char *name_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETfreeprob (CPXENVptr env, CPXNETptr *net_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETcopynet (CPXCENVptr env, CPXNETptr net, int objsen, CPXINT nnodes, const double *supply, char const *const *nnames, CPXINT narcs, const CPXINT *fromnode, const CPXINT *tonode, const double *low, const double *up, const double *obj, char const *const *anames);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETcopybase (CPXCENVptr env, CPXNETptr net, const int *astat, const int *nstat);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETaddnodes (CPXCENVptr env, CPXNETptr net, CPXINT n, const double *supply, char const *const *name);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETaddarcs (CPXCENVptr env, CPXNETptr net, CPXINT n, const CPXINT *from, const CPXINT *to, const double *low, const double *up, const double *cost, char const *const *name);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETdelnodes (CPXCENVptr env, CPXNETptr net, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETdelarcs (CPXCENVptr env, CPXNETptr net, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETdelset (CPXCENVptr env, CPXNETptr net, CPXINT *whichnodes, CPXINT *whicharcs);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETprimopt (CPXCENVptr env, CPXNETptr net);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETgetstat (CPXCENVptr env, CPXCNETptr net);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETgetobjval (CPXCENVptr env, CPXCNETptr net, double *objval_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETgetx (CPXCENVptr env, CPXCNETptr net, double *x, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETgetpi (CPXCENVptr env, CPXCNETptr net, double *pi, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETgetslack (CPXCENVptr env, CPXCNETptr net, double *slack, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETgetdj (CPXCENVptr env, CPXCNETptr net, double *dj, CPXINT begin, CPXINT end);

CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXSNETgetitcnt (CPXCENVptr env, CPXCNETptr net);

CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXSNETgetphase1cnt (CPXCENVptr env, CPXCNETptr net);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETgetbase (CPXCENVptr env, CPXCNETptr net, int *astat, int *nstat);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETsolution (CPXCENVptr env, CPXCNETptr net, int *netstat_p, double *objval_p, double *x, double *pi, double *slack, double *dj);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETsolninfo (CPXCENVptr env, CPXCNETptr net, int *pfeasind_p, int *dfeasind_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETchgname (CPXCENVptr env, CPXNETptr net, int key, CPXINT idx, const char *name_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETchgarcname (CPXCENVptr env, CPXNETptr net, CPXINT cnt, const CPXINT *indices, char const *const *name);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETchgnodename (CPXCENVptr env, CPXNETptr net, CPXINT cnt, const CPXINT *indices, char const *const *name);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETchgobjsen (CPXCENVptr env, CPXNETptr net, int maxormin);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETchgbds (CPXCENVptr env, CPXNETptr net, CPXINT cnt, const CPXINT *indices, const char *lu, const double *bd);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETchgarcnodes (CPXCENVptr env, CPXNETptr net, CPXINT cnt, const CPXINT *indices, const CPXINT *fromnode, const CPXINT *tonode);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETchgobj (CPXCENVptr env, CPXNETptr net, CPXINT cnt, const CPXINT *indices, const double *obj);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETchgsupply (CPXCENVptr env, CPXNETptr net, CPXINT cnt, const CPXINT *indices, const double *supply);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETgetobjsen (CPXCENVptr env, CPXCNETptr net);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETgetsupply (CPXCENVptr env, CPXCNETptr net, double *supply, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETgetprobname (CPXCENVptr env, CPXCNETptr net, char *buf_str, int bufspace, int *surplus_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETgetnodename (CPXCENVptr env, CPXCNETptr net, char **names, char *namestore, CPXSIZE xnamespace, CPXSIZE *surplus_p, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETgetarcname (CPXCENVptr env, CPXCNETptr net, char **names, char *namestore, CPXSIZE xnamespace, CPXSIZE *surplus_p, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETgetlb (CPXCENVptr env, CPXCNETptr net, double *low, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETgetub (CPXCENVptr env, CPXCNETptr net, double *up, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETgetobj (CPXCENVptr env, CPXCNETptr net, double *obj, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETgetarcnodes (CPXCENVptr env, CPXCNETptr net, CPXINT *fromnode, CPXINT *tonode, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETgetnodearcs (CPXCENVptr env, CPXCNETptr net, CPXINT *arccnt_p, CPXINT *arcbeg, CPXINT *arc, CPXINT arcspace, CPXINT *surplus_p, CPXINT begin, CPXINT end);

CPXLIBAPI
CPXINT CPXPUBLIC
   CPXSNETgetnumnodes (CPXCENVptr env, CPXCNETptr net);

CPXLIBAPI
CPXINT CPXPUBLIC
   CPXSNETgetnumarcs (CPXCENVptr env, CPXCNETptr net);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETgetnodeindex (CPXCENVptr env, CPXCNETptr net, const char *lname_str, CPXINT *index_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETgetarcindex (CPXCENVptr env, CPXCNETptr net, const char *lname_str, CPXINT *index_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETreadcopyprob (CPXCENVptr env, CPXNETptr net, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETreadcopybase (CPXCENVptr env, CPXNETptr net, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETwriteprob (CPXCENVptr env, CPXCNETptr net, const char *filename_str, const char *format_str);

CPXLIBAPI
int CPXPUBLIC
   CPXSNETbasewrite (CPXCENVptr env, CPXCNETptr net, const char *filename_str);

CPXLIBAPI
int CPXPUBLIC
   CPXScopyquad (CPXCENVptr env, CPXLPptr lp, const CPXINT *qmatbeg, const CPXINT *qmatcnt, const CPXINT *qmatind, const double *qmatval);

CPXLIBAPI
int CPXPUBLIC
   CPXScopyqpsep (CPXCENVptr env, CPXLPptr lp, const double *qsepvec);

CPXLIBAPI
int CPXPUBLIC
   CPXSchgqpcoef (CPXCENVptr env, CPXLPptr lp, CPXINT i, CPXINT j, double newvalue);

CPXLIBAPI
CPXINT CPXPUBLIC
   CPXSgetnumqpnz (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
CPXINT CPXPUBLIC
   CPXSgetnumquad (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetqpcoef (CPXCENVptr env, CPXCLPptr lp, CPXINT rownum, CPXINT colnum, double *pcoef);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetquad (CPXCENVptr env, CPXCLPptr lp, CPXINT *nzcnt_p, CPXINT *xqmatbeg, CPXINT *xqmatind, double *xqmatval, CPXINT xqmatspace, CPXINT *surplus_p, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSqpindefcertificate (CPXCENVptr env, CPXCLPptr lp, double *x);

CPXLIBAPI
int CPXPUBLIC
   CPXSqpopt (CPXCENVptr env, CPXLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSaddqconstr (CPXCENVptr env, CPXLPptr lp, CPXINT linnzcnt, CPXINT quadnzcnt, double rhs, int sense, const CPXINT *linind, const double *linval, const CPXINT *quadrow, const CPXINT *quadcol, const double *quadval, const char *constrname);

CPXLIBAPI
int CPXPUBLIC
   CPXSdelqconstrs (CPXCENVptr env, CPXLPptr lp, CPXINT begin, CPXINT end);

CPXLIBAPI
CPXINT CPXPUBLIC
   CPXSgetnumqconstrs (CPXCENVptr env, CPXCLPptr lp);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetqconstrindex (CPXCENVptr env, CPXCLPptr lp, const char *lname_str, CPXINT *index_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetqconstr (CPXCENVptr env, CPXCLPptr lp, CPXINT *linnzcnt_p, CPXINT *quadnzcnt_p, double *rhs_p, char *sense_p, CPXINT *linind, double *linval, CPXINT linsz, CPXINT *linsurplus_p, CPXINT *quadrow, CPXINT *quadcol, double *quadval, CPXINT quadsz, CPXINT *quadsurplus_p, CPXINT which);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetqconstrname (CPXCENVptr env, CPXCLPptr lp, char *store, CPXSIZE storesz, CPXSIZE *surplus_p, CPXINT which);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetqconstrslack (CPXCENVptr env, CPXCLPptr lp, double *qcslack, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSqconstrslackfromx (CPXCENVptr env, CPXCLPptr lp, const double *x, double *qcslack);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetqconstrinfeas (CPXCENVptr env, CPXCLPptr lp, const double *xin, double *infeasout, CPXINT begin, CPXINT end);

CPXLIBAPI
int CPXPUBLIC
   CPXSgetxqxax (CPXCENVptr env, CPXCLPptr lp, double *qxout, CPXINT begin, CPXINT end);



CPXLIBAPI
int CPXPUBVARARGS
   CPXSmsg (CPXCHANNELptr channel, const char *format, ...);

CPXLIBAPI
int CPXPUBLIC
   CPXSbranchcallbackbranchgeneral (CPXCENVptr env, void *cbdata,
                                    int wherefrom, double xnodeest,
                                    CPXINT varcnt,
                                    const CPXINT *varind, const char *varlu,
                                    const int *varbd, CPXINT rcnt,
                                    CPXINT nzcnt, const double *rhs,
                                    const char *sense,
                                    const CPXINT *rmatbeg,
                                    const CPXINT *rmatind,
                                    const double *rmatval,
                                    void *userhandle,
                                    CPXLONG *nodeid_p);

CPXLIBAPI
int CPXPUBLIC
   CPXSbranchcallbackbranchconstraints (CPXCENVptr env, void *cbdata,
                                        int wherefrom, double xnodeest,
                                        CPXINT rcnt, CPXINT nzcnt,
                                        const double *rhs,
                                        const char *sense,
                                        const CPXINT *rmatbeg,
                                        const CPXINT *rmatind,
                                        const double *rmatval,
                                        void *userhandle,
                                        CPXLONG *nodeid_p);


#ifdef __cplusplus
}
#endif

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif /* CPX_CPLEXS_H */
