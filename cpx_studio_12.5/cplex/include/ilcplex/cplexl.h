/* --------------------------------------------------------------------------
 * File: cplexl.h
 * Version 12.5
 * --------------------------------------------------------------------------
 * Licensed Materials - Property of IBM
 * 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55 5655-Y21
 * Copyright IBM Corporation 1988, 2012. All Rights Reserved.
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with
 * IBM Corp.
 *---------------------------------------------------------------------------
 */

#ifndef CPX_CPLEXL_H
#   define CPX_CPLEXL_H 1
#include "cpxconst.h"

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#ifdef __cplusplus
extern "C" {
#endif


#if defined(__x86_64__) || defined(__ia64) || defined(_WIN64) || defined(__powerpc64__) || defined(__64BIT__) || defined(__sparcv9) || defined(__LP64__)

#define CPX_CPLEX_L_API_DEFINED 1

/* Argument lists for callbacks */
#define CPXL_CALLBACK_ARGS CPXCENVptr env, void *cbdata, int wherefrom, \
      void *cbhandle
#define CPXL_CALLBACK_PROF_ARGS CPXCENVptr env, int wherefrom, void *cbhandle
#define CPXL_CALLBACK_BRANCH_ARGS  CPXCENVptr xenv, void *cbdata,       \
      int wherefrom, void *cbhandle, int brtype, CPXINT brset,          \
      int nodecnt, CPXINT bdcnt, const double *nodeest,                 \
      const CPXINT *nodebeg, const CPXINT *xindex, const char *lu,      \
      const int *bd, int *useraction_p
#define CPXL_CALLBACK_NODE_ARGS  CPXCENVptr xenv, void *cbdata,         \
      int wherefrom, void *cbhandle, CPXLONG *nodeindex, int *useraction
#define CPXL_CALLBACK_HEURISTIC_ARGS  CPXCENVptr xenv, void *cbdata,    \
      int wherefrom, void *cbhandle, double *objval_p, double *x,       \
      int *checkfeas_p, int *useraction_p
#define CPXL_CALLBACK_SOLVE_ARGS  CPXCENVptr xenv, void *cbdata,        \
      int wherefrom, void *cbhandle, int *useraction
#define CPXL_CALLBACK_CUT_ARGS  CPXCENVptr xenv, void *cbdata,  \
      int wherefrom, void *cbhandle, int *useraction_p
#define CPXL_CALLBACK_INCUMBENT_ARGS  CPXCENVptr xenv, void *cbdata,    \
      int wherefrom, void *cbhandle, double objval, double *x,          \
      int *isfeas_p, int *useraction_p
#define CPXL_CALLBACK_DELETENODE_ARGS  CPXCENVptr xenv,         \
   int wherefrom, void *cbhandle, CPXLONG seqnum, void *handle

typedef int (CPXPUBLIC CPXL_CALLBACK) (CPXL_CALLBACK_ARGS);
typedef int (CPXPUBLIC CPXL_CALLBACK_PROF)(CPXL_CALLBACK_PROF_ARGS);
typedef int (CPXPUBLIC CPXL_CALLBACK_BRANCH) (CPXL_CALLBACK_BRANCH_ARGS);
typedef int (CPXPUBLIC CPXL_CALLBACK_NODE) (CPXL_CALLBACK_NODE_ARGS);
typedef int (CPXPUBLIC CPXL_CALLBACK_HEURISTIC) (CPXL_CALLBACK_HEURISTIC_ARGS);
typedef int (CPXPUBLIC CPXL_CALLBACK_SOLVE) (CPXL_CALLBACK_SOLVE_ARGS);
typedef int (CPXPUBLIC CPXL_CALLBACK_CUT) (CPXL_CALLBACK_CUT_ARGS);
typedef int (CPXPUBLIC CPXL_CALLBACK_INCUMBENT) (CPXL_CALLBACK_INCUMBENT_ARGS);
typedef void (CPXPUBLIC CPXL_CALLBACK_DELETENODE) (CPXL_CALLBACK_DELETENODE_ARGS);







CPXLIBAPI
CPXLPptr CPXPUBLIC
   CPXLcreateprob (CPXCENVptr env, int *status_p,
                   const char *probname_str);


CPXLIBAPI
CPXLPptr CPXPUBLIC
   CPXLcloneprob (CPXCENVptr env, CPXCLPptr lp, int *status_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLcopylpwnames (CPXCENVptr env, CPXLPptr lp, CPXINT numcols,
                     CPXINT numrows, int objsense,
                     const double *objective, const double *rhs,
                     const char *sense, const CPXLONG *matbeg,
                     const CPXINT *matcnt, const CPXINT *matind,
                     const double *matval, const double *lb,
                     const double *ub, const double *rngval,
                     char const *const *colname,
                     char const *const *rowname);


CPXLIBAPI
int CPXPUBLIC
   CPXLcopylp (CPXCENVptr env, CPXLPptr lp, CPXINT numcols,
               CPXINT numrows, int objsense, const double *objective,
               const double *rhs, const char *sense,
               const CPXLONG *matbeg, const CPXINT *matcnt,
               const CPXINT *matind, const double *matval,
               const double *lb, const double *ub,
               const double *rngval);


CPXLIBAPI
int CPXPUBLIC
   CPXLcopyobjname (CPXCENVptr env, CPXLPptr lp,
                    const char *objname_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLcopybase (CPXCENVptr env, CPXLPptr lp, const int *cstat,
                 const int *rstat);


CPXLIBAPI
int CPXPUBLIC
   CPXLcleanup (CPXCENVptr env, CPXLPptr lp, double eps);


CPXLIBAPI
int CPXPUBLIC
   CPXLcopystart (CPXCENVptr env, CPXLPptr lp, const int *cstat,
                  const int *rstat, const double *cprim,
                  const double *rprim, const double *cdual,
                  const double *rdual);


CPXLIBAPI
int CPXPUBLIC
   CPXLfreeprob (CPXCENVptr env, CPXLPptr *lp_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLcopynettolp (CPXCENVptr env, CPXLPptr lp, CPXCNETptr net);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETextract (CPXCENVptr env, CPXNETptr net, CPXCLPptr lp,
                   CPXINT *colmap, CPXINT *rowmap);


CPXLIBAPI
int CPXPUBLIC
   CPXLlpopt (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLprimopt (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLdualopt (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLhybnetopt (CPXCENVptr env, CPXLPptr lp, int method);


CPXLIBAPI
int CPXPUBLIC
   CPXLsiftopt (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLpratio (CPXCENVptr env, CPXLPptr lp, CPXINT *indices,
               CPXINT cnt, double *downratio, double *upratio,
               CPXINT *downleave, CPXINT *upleave,
               int *downleavestatus, int *upleavestatus,
               int *downstatus, int *upstatus);


CPXLIBAPI
int CPXPUBLIC
   CPXLdratio (CPXCENVptr env, CPXLPptr lp, CPXINT *indices,
               CPXINT cnt, double *downratio, double *upratio,
               CPXINT *downenter, CPXINT *upenter, int *downstatus,
               int *upstatus);


CPXLIBAPI
int CPXPUBLIC
   CPXLpivot (CPXCENVptr env, CPXLPptr lp, CPXINT jenter,
              CPXINT jleave, int leavestat);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetphase2 (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLcheckpfeas (CPXCENVptr env, CPXLPptr lp, CPXINT *infeas_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLcheckdfeas (CPXCENVptr env, CPXLPptr lp, CPXINT *infeas_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLsolution (CPXCENVptr env, CPXCLPptr lp, int *lpstat_p,
                 double *objval_p, double *x, double *pi,
                 double *slack, double *dj);


CPXLIBAPI
int CPXPUBLIC
   CPXLsolninfo (CPXCENVptr env, CPXCLPptr lp, int *solnmethod_p,
                 int *solntype_p, int *pfeasind_p, int *dfeasind_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetstat (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
CPXCHARptr CPXPUBLIC
   CPXLgetstatstring (CPXCENVptr env, int statind, char *buffer_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetmethod (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetobjval (CPXCENVptr env, CPXCLPptr lp, double *objval_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetx (CPXCENVptr env, CPXCLPptr lp, double *x, CPXINT begin,
             CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetax (CPXCENVptr env, CPXCLPptr lp, double *x, CPXINT begin,
              CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetpi (CPXCENVptr env, CPXCLPptr lp, double *pi, CPXINT begin,
              CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetslack (CPXCENVptr env, CPXCLPptr lp, double *slack,
                 CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetrowinfeas (CPXCENVptr env, CPXCLPptr lp, const double *x,
                     double *infeasout, CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcolinfeas (CPXCENVptr env, CPXCLPptr lp, const double *x,
                     double *infeasout, CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetdj (CPXCENVptr env, CPXCLPptr lp, double *dj, CPXINT begin,
              CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetgrad (CPXCENVptr env, CPXCLPptr lp, CPXINT j, CPXINT *head,
                double *y);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetijdiv (CPXCENVptr env, CPXCLPptr lp, CPXINT *idiv_p,
                 CPXINT *jdiv_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetbase (CPXCENVptr env, CPXCLPptr lp, int *cstat, int *rstat);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXLgetitcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXLgetphase1cnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXLgetsiftitcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXLgetsiftphase1cnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXLgetbaritcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXLgetcrossppushcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXLgetcrosspexchcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXLgetcrossdpushcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXLgetcrossdexchcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
CPXINT CPXPUBLIC
   CPXLgetpsbcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
CPXINT CPXPUBLIC
   CPXLgetdsbcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetdblquality (CPXCENVptr env, CPXCLPptr lp, double *quality_p,
                      int what);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsolnpooldblquality (CPXCENVptr env, CPXCLPptr lp, int soln,
                              double *quality_p, int what);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetintquality (CPXCENVptr env, CPXCLPptr lp, CPXINT *quality_p,
                      int what);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsolnpoolintquality (CPXCENVptr env, CPXCLPptr lp, int soln,
                              CPXINT *quality_p, int what);


CPXLIBAPI
int CPXPUBLIC
   CPXLrhssa (CPXCENVptr env, CPXCLPptr lp, CPXINT begin, CPXINT end,
              double *lower, double *upper);


CPXLIBAPI
int CPXPUBLIC
   CPXLboundsa (CPXCENVptr env, CPXCLPptr lp, CPXINT begin, CPXINT end,
                double *lblower, double *lbupper, double *ublower,
                double *ubupper);


CPXLIBAPI
int CPXPUBLIC
   CPXLobjsa (CPXCENVptr env, CPXCLPptr lp, CPXINT begin, CPXINT end,
              double *lower, double *upper);


CPXLIBAPI
int CPXPUBLIC
   CPXLrefineconflict (CPXCENVptr env, CPXLPptr lp,
                       CPXINT *confnumrows_p, CPXINT *confnumcols_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetconflict (CPXCENVptr env, CPXCLPptr lp, int *confstat_p,
                    CPXINT *rowind, int *rowbdstat,
                    CPXINT *confnumrows_p, CPXINT *colind,
                    int *colbdstat, CPXINT *confnumcols_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLrefineconflictext (CPXCENVptr env, CPXLPptr lp, CPXLONG grpcnt,
                          CPXLONG concnt, const double *grppref,
                          const CPXLONG *grpbeg, const CPXINT *grpind,
                          const char *grptype);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetconflictext (CPXCENVptr env, CPXCLPptr lp, int *grpstat,
                       CPXLONG beg, CPXLONG end);


CPXLIBAPI
int CPXPUBLIC
   CPXLclpwrite (CPXCENVptr env, CPXCLPptr lp,
                 const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLrobustopt (CPXCENVptr env, CPXLPptr lp, CPXLPptr lblp,
                  CPXLPptr ublp, double objchg, const double *maxchg);


CPXLIBAPI
int CPXPUBLIC
   CPXLfeasopt (CPXCENVptr env, CPXLPptr lp, const double *rhs,
                const double *rng, const double *lb, const double *ub);


CPXLIBAPI
int CPXPUBLIC
   CPXLfeasoptext (CPXCENVptr env, CPXLPptr lp, CPXINT grpcnt,
                   CPXLONG concnt, const double *grppref,
                   const CPXLONG *grpbeg, const CPXINT *grpind,
                   const char *grptype);


CPXLIBAPI
int CPXPUBLIC
   CPXLnewrows (CPXCENVptr env, CPXLPptr lp, CPXINT rcnt,
                const double *rhs, const char *sense,
                const double *rngval, char const *const *rowname);


CPXLIBAPI
int CPXPUBLIC
   CPXLaddrows (CPXCENVptr env, CPXLPptr lp, CPXINT ccnt, CPXINT rcnt,
                CPXLONG nzcnt, const double *rhs, const char *sense,
                const CPXLONG *rmatbeg, const CPXINT *rmatind,
                const double *rmatval, char const *const *colname,
                char const *const *rowname);


CPXLIBAPI
int CPXPUBLIC
   CPXLnewcols (CPXCENVptr env, CPXLPptr lp, CPXINT ccnt,
                const double *obj, const double *lb, const double *ub,
                const char *xctype, char const *const *colname);


CPXLIBAPI
int CPXPUBLIC
   CPXLaddcols (CPXCENVptr env, CPXLPptr lp, CPXINT ccnt,
                CPXLONG nzcnt, const double *obj,
                const CPXLONG *cmatbeg, const CPXINT *cmatind,
                const double *cmatval, const double *lb,
                const double *ub, char const *const *colname);


CPXLIBAPI
int CPXPUBLIC
   CPXLdelrows (CPXCENVptr env, CPXLPptr lp, CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLdelsetrows (CPXCENVptr env, CPXLPptr lp, CPXINT *delstat);


CPXLIBAPI
int CPXPUBLIC
   CPXLdelcols (CPXCENVptr env, CPXLPptr lp, CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLdelsetcols (CPXCENVptr env, CPXLPptr lp, CPXINT *delstat);


CPXLIBAPI
int CPXPUBLIC
   CPXLchgname (CPXCENVptr env, CPXLPptr lp, int key, CPXINT ij,
                const char *newname_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLchgrowname (CPXCENVptr env, CPXLPptr lp, CPXINT cnt,
                   const CPXINT *indices, char const *const *newname);


CPXLIBAPI
int CPXPUBLIC
   CPXLchgcolname (CPXCENVptr env, CPXLPptr lp, CPXINT cnt,
                   const CPXINT *indices, char const *const *newname);


CPXLIBAPI
int CPXPUBLIC
   CPXLdelnames (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLchgprobname (CPXCENVptr env, CPXLPptr lp, const char *probname);


CPXLIBAPI
int CPXPUBLIC
   CPXLchgcoef (CPXCENVptr env, CPXLPptr lp, CPXINT i, CPXINT j,
                double newvalue);


CPXLIBAPI
int CPXPUBLIC
   CPXLchgcoeflist (CPXCENVptr env, CPXLPptr lp, CPXLONG numcoefs,
                    const CPXINT *rowlist, const CPXINT *collist,
                    const double *vallist);


CPXLIBAPI
int CPXPUBLIC
   CPXLchgbds (CPXCENVptr env, CPXLPptr lp, CPXINT cnt,
               const CPXINT *indices, const char *lu, const double *bd);


CPXLIBAPI
int CPXPUBLIC
   CPXLchgobj (CPXCENVptr env, CPXLPptr lp, CPXINT cnt,
               const CPXINT *indices, const double *values);


CPXLIBAPI
int CPXPUBLIC
   CPXLchgrhs (CPXCENVptr env, CPXLPptr lp, CPXINT cnt,
               const CPXINT *indices, const double *values);


CPXLIBAPI
int CPXPUBLIC
   CPXLchgrngval (CPXCENVptr env, CPXLPptr lp, CPXINT cnt,
                  const CPXINT *indices, const double *values);


CPXLIBAPI
int CPXPUBLIC
   CPXLchgsense (CPXCENVptr env, CPXLPptr lp, CPXINT cnt,
                 const CPXINT *indices, const char *sense);


CPXLIBAPI
int CPXPUBLIC
   CPXLchgobjsen (CPXCENVptr env, CPXLPptr lp, int maxormin);


CPXLIBAPI
int CPXPUBLIC
   CPXLchgprobtype (CPXCENVptr env, CPXLPptr lp, int type);


CPXLIBAPI
int CPXPUBLIC
   CPXLchgprobtypesolnpool (CPXCENVptr env, CPXLPptr lp, int type,
                            int soln);


CPXLIBAPI
int CPXPUBLIC
   CPXLcompletelp (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLpreaddrows (CPXCENVptr env, CPXLPptr lp, CPXINT rcnt,
                   CPXLONG nzcnt, const double *rhs, const char *sense,
                   const CPXLONG *rmatbeg, const CPXINT *rmatind,
                   const double *rmatval, char const *const *rowname);


CPXLIBAPI
int CPXPUBLIC
   CPXLprechgobj (CPXCENVptr env, CPXLPptr lp, CPXINT cnt,
                  const CPXINT *indices, const double *values);


CPXLIBAPI
CPXINT CPXPUBLIC
   CPXLgetnumcols (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
CPXINT CPXPUBLIC
   CPXLgetnumrows (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXLgetnumnz (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetobjsen (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetobj (CPXCENVptr env, CPXCLPptr lp, double *obj, CPXINT begin,
               CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetrhs (CPXCENVptr env, CPXCLPptr lp, double *rhs, CPXINT begin,
               CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsense (CPXCENVptr env, CPXCLPptr lp, char *sense,
                 CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcols (CPXCENVptr env, CPXCLPptr lp, CPXLONG *nzcnt_p,
                CPXLONG *cmatbeg, CPXINT *cmatind, double *cmatval,
                CPXLONG cmatspace, CPXLONG *surplus_p, CPXINT begin,
                CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetrows (CPXCENVptr env, CPXCLPptr lp, CPXLONG *nzcnt_p,
                CPXLONG *rmatbeg, CPXINT *rmatind, double *rmatval,
                CPXLONG rmatspace, CPXLONG *surplus_p, CPXINT begin,
                CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetlb (CPXCENVptr env, CPXCLPptr lp, double *lb, CPXINT begin,
              CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetub (CPXCENVptr env, CPXCLPptr lp, double *ub, CPXINT begin,
              CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetrngval (CPXCENVptr env, CPXCLPptr lp, double *rngval,
                  CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetprobname (CPXCENVptr env, CPXCLPptr lp, char *buf_str,
                    CPXSIZE bufspace, CPXSIZE *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetobjname (CPXCENVptr env, CPXCLPptr lp, char *buf_str,
                   CPXSIZE bufspace, CPXSIZE *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcolname (CPXCENVptr env, CPXCLPptr lp, char  **name,
                   char *namestore, CPXSIZE storespace,
                   CPXSIZE *surplus_p, CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetrowname (CPXCENVptr env, CPXCLPptr lp, char  **name,
                   char *namestore, CPXSIZE storespace,
                   CPXSIZE *surplus_p, CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcoef (CPXCENVptr env, CPXCLPptr lp, CPXINT i, CPXINT j,
                double *coef_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetrowindex (CPXCENVptr env, CPXCLPptr lp,
                    const char *lname_str, CPXINT *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcolindex (CPXCENVptr env, CPXCLPptr lp,
                    const char *lname_str, CPXINT *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetprobtype (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLreadcopyprob (CPXCENVptr env, CPXLPptr lp,
                     const char *filename_str,
                     const char *filetype_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLreadcopybase (CPXCENVptr env, CPXLPptr lp,
                     const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLreadcopysol (CPXCENVptr env, CPXLPptr lp,
                    const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLwriteprob (CPXCENVptr env, CPXCLPptr lp,
                  const char *filename_str, const char *filetype_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLmbasewrite (CPXCENVptr env, CPXCLPptr lp,
                   const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLsolwrite (CPXCENVptr env, CPXCLPptr lp,
                 const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLsolwritesolnpool (CPXCENVptr env, CPXCLPptr lp, int soln,
                         const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLsolwritesolnpoolall (CPXCENVptr env, CPXCLPptr lp,
                            const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLembwrite (CPXCENVptr env, CPXLPptr lp, const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLdperwrite (CPXCENVptr env, CPXLPptr lp,
                  const char *filename_str, double epsilon);


CPXLIBAPI
int CPXPUBLIC
   CPXLpperwrite (CPXCENVptr env, CPXLPptr lp,
                  const char *filename_str, double epsilon);


CPXLIBAPI
int CPXPUBLIC
   CPXLpreslvwrite (CPXCENVptr env, CPXLPptr lp,
                    const char *filename_str, double *objoff_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLdualwrite (CPXCENVptr env, CPXCLPptr lp,
                  const char *filename_str, double *objshift_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetdefaults (CPXENVptr env);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetintparam (CPXENVptr env, int whichparam, CPXINT newvalue);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetlongparam (CPXENVptr env, int whichparam, CPXLONG newvalue);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetdblparam (CPXENVptr env, int whichparam, double newvalue);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetstrparam (CPXENVptr env, int whichparam,
                    const char *newvalue_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetintparam (CPXCENVptr env, int whichparam, CPXINT *value_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetlongparam (CPXCENVptr env, int whichparam, CPXLONG *value_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetdblparam (CPXCENVptr env, int whichparam, double *value_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetstrparam (CPXCENVptr env, int whichparam, char *value_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLinfointparam (CPXCENVptr env, int whichparam,
                     CPXINT *defvalue_p, CPXINT *minvalue_p,
                     CPXINT *maxvalue_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLinfolongparam (CPXCENVptr env, int whichparam,
                      CPXLONG *defvalue_p, CPXLONG *minvalue_p,
                      CPXLONG *maxvalue_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLinfodblparam (CPXCENVptr env, int whichparam,
                     double *defvalue_p, double *minvalue_p,
                     double *maxvalue_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLinfostrparam (CPXCENVptr env, int whichparam,
                     char *defvalue_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetparamname (CPXCENVptr env, int whichparam, char *name_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetparamnum (CPXCENVptr env, const char *name_str,
                    int *whichparam_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetparamtype (CPXCENVptr env, int whichparam, int *paramtype);


CPXLIBAPI
int CPXPUBLIC
   CPXLreadcopyparam (CPXENVptr env, const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLwriteparam (CPXCENVptr env, const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetchgparam (CPXCENVptr env, int *cnt_p, int *paramnum,
                    int pspace, int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLtuneparam (CPXENVptr env, CPXLPptr lp, int intcnt,
                  const int *intnum, const int *intval, int dblcnt,
                  const int *dblnum, const double *dblval, int strcnt,
                  const int *strnum, char const *const *strval,
                  int *tunestat_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLtuneparamprobset (CPXENVptr env, int filecnt,
                         char const *const *filename,
                         char const *const *filetype, int intcnt,
                         const int *intnum, const int *intval,
                         int dblcnt, const int *dblnum,
                         const double *dblval, int strcnt,
                         const int *strnum, char const *const *strval,
                         int *tunestat_p);


CPXLIBAPI
CPXCCHARptr CPXPUBLIC
   CPXLversion (CPXCENVptr env);


CPXLIBAPI
CPXENVptr CPXPUBLIC
   CPXLopenCPLEX (int *status_p);


CPXLIBAPI
CPXENVptr CPXPUBLIC
   CPXLopenCPLEXruntime (int *status_p, int serialnum,
                         const char *licenvstring_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLcloseCPLEX (CPXENVptr *env_p);



CPXLIBAPI
CPXENVptr CPXPUBLIC
   CPXLparenv (CPXENVptr env, int *status_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLfreeparenv (CPXENVptr env, CPXENVptr *child_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetchannels (CPXCENVptr env, CPXCHANNELptr *cpxresults_p,
                    CPXCHANNELptr *cpxwarning_p,
                    CPXCHANNELptr *cpxerror_p, CPXCHANNELptr *cpxlog_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetlogfile (CPXENVptr env, CPXFILEptr lfile);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetlogfile (CPXCENVptr env, CPXFILEptr *logfile_p);


CPXLIBAPI
int CPXPUBVARARGS
   CPXLmsg (CPXCHANNELptr channel, const char *format, ...);


CPXLIBAPI
int CPXPUBLIC
   CPXLmsgstr (CPXCHANNELptr channel, const char *msg_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLflushchannel (CPXCENVptr env, CPXCHANNELptr channel);


CPXLIBAPI
int CPXPUBLIC
   CPXLflushstdchannels (CPXCENVptr env);


CPXLIBAPI
CPXCHANNELptr CPXPUBLIC
   CPXLaddchannel (CPXENVptr env);


CPXLIBAPI
int CPXPUBLIC
   CPXLaddfpdest (CPXCENVptr env, CPXCHANNELptr channel,
                  CPXFILEptr fileptr);


CPXLIBAPI
int CPXPUBLIC
   CPXLdelfpdest (CPXCENVptr env, CPXCHANNELptr channel,
                  CPXFILEptr fileptr);


CPXLIBAPI
int CPXPUBLIC
   CPXLaddfuncdest (CPXCENVptr env, CPXCHANNELptr channel,
                    void *handle,
                    void (CPXPUBLIC *msgfunction)(void *, char const *));


CPXLIBAPI
int CPXPUBLIC
   CPXLdelfuncdest (CPXCENVptr env, CPXCHANNELptr channel,
                    void *handle,
                    void (CPXPUBLIC *msgfunction)(void *, char const *));


CPXLIBAPI
int CPXPUBLIC
   CPXLdelchannel (CPXENVptr env, CPXCHANNELptr *channel_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLdisconnectchannel (CPXCENVptr env, CPXCHANNELptr channel);


CPXLIBAPI
CPXCCHARptr CPXPUBLIC
   CPXLgeterrorstring (CPXCENVptr env, int errcode, char *buffer_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetlpcallbackfunc (CPXENVptr env, CPXL_CALLBACK *callback,
                          void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetnetcallbackfunc (CPXENVptr env, CPXL_CALLBACK *callback,
                           void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXLsettuningcallbackfunc (CPXENVptr env, CPXL_CALLBACK *callback,
                              void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcallbackinfo (CPXCENVptr env, void *cbdata, int wherefrom,
                        int whichinfo, void *result_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetlpcallbackfunc (CPXCENVptr env, CPXL_CALLBACK **callback_p,
                          void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetnetcallbackfunc (CPXCENVptr env, CPXL_CALLBACK **callback_p,
                           void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgettuningcallbackfunc (CPXCENVptr env,
                              CPXL_CALLBACK **callback_p,
                              void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetprofcallbackfunc (CPXENVptr env,
                            CPXL_CALLBACK_PROF *callback,
                            void *cbhandle);


CPXLIBAPI
CPXFILEptr CPXPUBLIC
   CPXLfopen (const char *filename_str, const char *type_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLfclose (CPXFILEptr stream);


CPXLIBAPI
int CPXPUBLIC
   CPXLfputs (const char *s_str, CPXFILEptr stream);


CPXLIBAPI
CPXVOIDptr CPXPUBLIC
   CPXLmalloc (size_t size);


CPXLIBAPI
CPXVOIDptr CPXPUBLIC
   CPXLrealloc (void *ptr, size_t size);


CPXLIBAPI
CPXVOIDptr CPXPUBLIC
   CPXLmemcpy (void *s1, void *s2, size_t n);


CPXLIBAPI
void CPXPUBLIC
   CPXLfree (void *ptr);


CPXLIBAPI
size_t CPXPUBLIC
   CPXLstrlen (const char *s_str);


CPXLIBAPI
CPXCHARptr CPXPUBLIC
   CPXLstrcpy (char *dest_str, const char *src_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLputenv (char *envsetting_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetterminate (CPXENVptr env, volatile int *terminate_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetbhead (CPXCENVptr env, CPXCLPptr lp, CPXINT *head, double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXLbinvcol (CPXCENVptr env, CPXCLPptr lp, CPXINT j, double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXLbinvrow (CPXCENVptr env, CPXCLPptr lp, CPXINT i, double *y);


CPXLIBAPI
int CPXPUBLIC
   CPXLbinvacol (CPXCENVptr env, CPXCLPptr lp, CPXINT j, double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXLbinvarow (CPXCENVptr env, CPXCLPptr lp, CPXINT i, double *z);


CPXLIBAPI
int CPXPUBLIC
   CPXLftran (CPXCENVptr env, CPXCLPptr lp, double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXLbtran (CPXCENVptr env, CPXCLPptr lp, double *y);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetijrow (CPXCENVptr env, CPXCLPptr lp, CPXINT i, CPXINT j,
                 CPXINT *row_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetray (CPXCENVptr env, CPXCLPptr lp, double *z);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetweight (CPXCENVptr env, CPXCLPptr lp, CPXINT rcnt,
                  const CPXLONG *rmatbeg, const CPXINT *rmatind,
                  const double *rmatval, double *weight, int dpriind);


CPXLIBAPI
int CPXPUBLIC
   CPXLmdleave (CPXCENVptr env, CPXLPptr lp, const CPXINT *indices,
                CPXINT cnt, double *downratio, double *upratio);


CPXLIBAPI
int CPXPUBLIC
   CPXLstrongbranch (CPXCENVptr env, CPXLPptr lp,
                     const CPXINT *indices, CPXINT cnt,
                     double *downobj, double *upobj, CPXLONG itlim);


CPXLIBAPI
int CPXPUBLIC
   CPXLdualfarkas (CPXCENVptr env, CPXCLPptr lp, double *y,
                   double *proof_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetobjoffset (CPXCENVptr env, CPXCLPptr lp, double *objoffset_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLcopypartialbase (CPXCENVptr env, CPXLPptr lp, CPXINT ccnt,
                        const CPXINT *cindices, const int *cstat,
                        CPXINT rcnt, const CPXINT *rindices,
                        const int *rstat);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetbasednorms (CPXCENVptr env, CPXCLPptr lp, int *cstat,
                      int *rstat, double *dnorm);


CPXLIBAPI
int CPXPUBLIC
   CPXLcopybasednorms (CPXCENVptr env, CPXLPptr lp, const int *cstat,
                       const int *rstat, const double *dnorm);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetdnorms (CPXCENVptr env, CPXCLPptr lp, double *norm,
                  CPXINT *head, CPXINT *len_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLcopydnorms (CPXCENVptr env, CPXLPptr lp, const double *norm,
                   const CPXINT *head, CPXINT len);


CPXLIBAPI
int CPXPUBLIC
   CPXLkilldnorms (CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetpnorms (CPXCENVptr env, CPXCLPptr lp, double *cnorm,
                  double *rnorm, CPXINT *len_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLcopypnorms (CPXCENVptr env, CPXLPptr lp, const double *cnorm,
                   const double *rnorm, CPXINT len);


CPXLIBAPI
int CPXPUBLIC
   CPXLkillpnorms (CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLpivotin (CPXCENVptr env, CPXLPptr lp, const CPXINT *rlist,
                CPXINT rlen);


CPXLIBAPI
int CPXPUBLIC
   CPXLpivotout (CPXCENVptr env, CPXLPptr lp, const CPXINT *clist,
                 CPXINT clen);


CPXLIBAPI
int CPXPUBLIC
   CPXLchecksoln (CPXCENVptr env, CPXLPptr lp, int *lpstatus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLunscaleprob (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLtightenbds (CPXCENVptr env, CPXLPptr lp, CPXINT cnt,
                   const CPXINT *indices, const char *lu,
                   const double *bd);


CPXLIBAPI
int CPXPUBLIC
   CPXLpresolve (CPXCENVptr env, CPXLPptr lp, int method);


CPXLIBAPI
int CPXPUBLIC
   CPXLbasicpresolve (CPXCENVptr env, CPXLPptr lp, double *redlb,
                      double *redub, int *rstat);


CPXLIBAPI
int CPXPUBLIC
   CPXLslackfromx (CPXCENVptr env, CPXCLPptr lp, const double *x,
                   double *slack);


CPXLIBAPI
int CPXPUBLIC
   CPXLdjfrompi (CPXCENVptr env, CPXCLPptr lp, const double *pi,
                 double *dj);


CPXLIBAPI
int CPXPUBLIC
   CPXLqpdjfrompi (CPXCENVptr env, CPXCLPptr lp, const double *pi,
                   const double *x, double *dj);


CPXLIBAPI
int CPXPUBLIC
   CPXLfreepresolve (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetredlp (CPXCENVptr env, CPXCLPptr lp, CPXCLPptr *redlp_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLcrushx (CPXCENVptr env, CPXCLPptr lp, const double *x,
               double *prex);


CPXLIBAPI
int CPXPUBLIC
   CPXLuncrushx (CPXCENVptr env, CPXCLPptr lp, double *x,
                 const double *prex);


CPXLIBAPI
int CPXPUBLIC
   CPXLcrushpi (CPXCENVptr env, CPXCLPptr lp, const double *pi,
                double *prepi);


CPXLIBAPI
int CPXPUBLIC
   CPXLuncrushpi (CPXCENVptr env, CPXCLPptr lp, double *pi,
                  const double *prepi);


CPXLIBAPI
int CPXPUBLIC
   CPXLqpuncrushpi (CPXCENVptr env, CPXCLPptr lp, double *pi,
                    const double *prepi, const double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXLcrushform (CPXCENVptr env, CPXCLPptr lp, CPXINT len,
                  const CPXINT *ind, const double *val, CPXINT *plen_p,
                  double *poffset_p, CPXINT *pind, double *pval);


CPXLIBAPI
int CPXPUBLIC
   CPXLuncrushform (CPXCENVptr env, CPXCLPptr lp, CPXINT plen,
                    const CPXINT *pind, const double *pval,
                    CPXINT *len_p, double *offset_p, CPXINT *ind,
                    double *val);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetprestat (CPXCENVptr env, CPXCLPptr lp, int *prestat_p,
                   CPXINT *pcstat, CPXINT *prstat, CPXINT *ocstat,
                   CPXINT *orstat);


CPXLIBAPI
int CPXPUBLIC
   CPXLcopyprotected (CPXCENVptr env, CPXLPptr lp, CPXINT cnt,
                      const CPXINT *indices);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetprotected (CPXCENVptr env, CPXCLPptr lp, CPXINT *cnt_p,
                     CPXINT *indices, CPXINT pspace, CPXINT *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetnumcores (CPXCENVptr env, int *numcores_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgettime (CPXCENVptr env, double *timestamp_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetdettime (CPXCENVptr env, double *dettimestamp_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLserializercreate (CPXSERIALIZERptr *ser_p);


CPXLIBAPI
void CPXPUBLIC
   CPXLserializerdestroy (CPXSERIALIZERptr ser);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXLserializerlength (CPXCSERIALIZERptr ser);


CPXLIBAPI
void const * CPXPUBLIC
   CPXLserializerpayload (CPXCSERIALIZERptr ser);


CPXLIBAPI
int CPXPUBLIC
   CPXLdeserializercreate (CPXDESERIALIZERptr *deser_p, CPXLONG size,
                           void const *buffer);


CPXLIBAPI
void CPXPUBLIC
   CPXLdeserializerdestroy (CPXDESERIALIZERptr deser);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXLdeserializerleft (CPXCDESERIALIZERptr deser);


CPXLIBAPI
int CPXPUBLIC
   CPXLlpwrite (CPXCENVptr env, CPXCLPptr lp, const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLlprewrite (CPXCENVptr env, CPXCLPptr lp,
                  const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLmpswrite (CPXCENVptr env, CPXCLPptr lp,
                 const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLmpsrewrite (CPXCENVptr env, CPXCLPptr lp,
                   const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLsavwrite (CPXCENVptr env, CPXCLPptr lp,
                 const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLsavwritedev (CPXCENVptr env, CPXCLPptr lp,
                    const char *filename_str, CPXIODEVICEptr dev);







CPXLIBAPI
void CPXPUBLIC
   CPXLinitialize (void);


CPXLIBAPI
void CPXPUBLIC
   CPXLfinalize (void);


CPXLIBAPI
int CPXPUBLIC
   CPXLhybbaropt (CPXCENVptr env, CPXLPptr lp, int method);


CPXLIBAPI
int CPXPUBLIC
   CPXLbaropt (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLaddindconstr (CPXCENVptr env, CPXLPptr lp, CPXINT indvar,
                     int complemented, CPXINT nzcnt, double rhs,
                     int sense, const CPXINT *linind,
                     const double *linval, const char *indname_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLcopyquad (CPXCENVptr env, CPXLPptr lp, const CPXLONG *qmatbeg,
                 const CPXINT *qmatcnt, const CPXINT *qmatind,
                 const double *qmatval);


CPXLIBAPI
int CPXPUBLIC
   CPXLcopyqpsep (CPXCENVptr env, CPXLPptr lp, const double *qsepvec);


CPXLIBAPI
int CPXPUBLIC
   CPXLchgqpcoef (CPXCENVptr env, CPXLPptr lp, CPXINT i, CPXINT j,
                  double newvalue);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXLgetnumqpnz (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
CPXINT CPXPUBLIC
   CPXLgetnumquad (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetqpcoef (CPXCENVptr env, CPXCLPptr lp, CPXINT rownum,
                  CPXINT colnum, double *coef_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetquad (CPXCENVptr env, CPXCLPptr lp, CPXLONG *nzcnt_p,
                CPXLONG *qmatbeg, CPXINT *qmatind, double *qmatval,
                CPXLONG qmatspace, CPXLONG *surplus_p, CPXINT begin,
                CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLqpindefcertificate (CPXCENVptr env, CPXCLPptr lp, double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXLqpopt (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLaddqconstr (CPXCENVptr env, CPXLPptr lp, CPXINT linnzcnt,
                   CPXLONG quadnzcnt, double rhs, int sense,
                   const CPXINT *linind, const double *linval,
                   const CPXINT *quadrow, const CPXINT *quadcol,
                   const double *quadval, const char *lname_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLdelqconstrs (CPXCENVptr env, CPXLPptr lp, CPXINT begin,
                    CPXINT end);


CPXLIBAPI
CPXINT CPXPUBLIC
   CPXLgetnumqconstrs (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetqconstrindex (CPXCENVptr env, CPXCLPptr lp,
                        const char *lname_str, CPXINT *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetqconstr (CPXCENVptr env, CPXCLPptr lp, CPXINT *linnzcnt_p,
                   CPXLONG *quadnzcnt_p, double *rhs_p, char *sense_p,
                   CPXINT *linind, double *linval, CPXINT linspace,
                   CPXINT *linsurplus_p, CPXINT *quadrow,
                   CPXINT *quadcol, double *quadval, CPXLONG quadspace,
                   CPXLONG *quadsurplus_p, CPXINT which);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetqconstrname (CPXCENVptr env, CPXCLPptr lp, char *buf_str,
                       CPXSIZE bufspace, CPXSIZE *surplus_p,
                       CPXINT which);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetqconstrslack (CPXCENVptr env, CPXCLPptr lp, double *qcslack,
                        CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLqconstrslackfromx (CPXCENVptr env, CPXCLPptr lp,
                          const double *x, double *qcslack);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetqconstrinfeas (CPXCENVptr env, CPXCLPptr lp, const double *x,
                         double *infeasout, CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetxqxax (CPXCENVptr env, CPXCLPptr lp, double *xqxax,
                 CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetqconstrdslack (CPXCENVptr env, CPXCLPptr lp, CPXINT qind,
                         CPXINT *nz_p, CPXINT *ind, double *val,
                         CPXINT space, CPXINT *surplus_p);


CPXLIBAPI
CPXNETptr CPXPUBLIC
   CPXLNETcreateprob (CPXENVptr env, int *status_p,
                      const char *name_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETfreeprob (CPXENVptr env, CPXNETptr *net_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETcopynet (CPXCENVptr env, CPXNETptr net, int objsen,
                   CPXINT nnodes, const double *supply,
                   char const *const *nnames, CPXINT narcs,
                   const CPXINT *fromnode, const CPXINT *tonode,
                   const double *low, const double *up,
                   const double *obj, char const *const *anames);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETcopybase (CPXCENVptr env, CPXNETptr net, const int *astat,
                    const int *nstat);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETaddnodes (CPXCENVptr env, CPXNETptr net, CPXINT nnodes,
                    const double *supply, char const *const *name);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETaddarcs (CPXCENVptr env, CPXNETptr net, CPXINT narcs,
                   const CPXINT *fromnode, const CPXINT *tonode,
                   const double *low, const double *up,
                   const double *obj, char const *const *anames);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETdelnodes (CPXCENVptr env, CPXNETptr net, CPXINT begin,
                    CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETdelarcs (CPXCENVptr env, CPXNETptr net, CPXINT begin,
                   CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETdelset (CPXCENVptr env, CPXNETptr net, CPXINT *whichnodes,
                  CPXINT *whicharcs);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETprimopt (CPXCENVptr env, CPXNETptr net);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETgetstat (CPXCENVptr env, CPXCNETptr net);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETgetobjval (CPXCENVptr env, CPXCNETptr net, double *objval_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETgetx (CPXCENVptr env, CPXCNETptr net, double *x,
                CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETgetpi (CPXCENVptr env, CPXCNETptr net, double *pi,
                 CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETgetslack (CPXCENVptr env, CPXCNETptr net, double *slack,
                    CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETgetdj (CPXCENVptr env, CPXCNETptr net, double *dj,
                 CPXINT begin, CPXINT end);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXLNETgetitcnt (CPXCENVptr env, CPXCNETptr net);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXLNETgetphase1cnt (CPXCENVptr env, CPXCNETptr net);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETgetbase (CPXCENVptr env, CPXCNETptr net, int *astat,
                   int *nstat);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETsolution (CPXCENVptr env, CPXCNETptr net, int *netstat_p,
                    double *objval_p, double *x, double *pi,
                    double *slack, double *dj);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETsolninfo (CPXCENVptr env, CPXCNETptr net, int *pfeasind_p,
                    int *dfeasind_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETchgname (CPXCENVptr env, CPXNETptr net, int key,
                   CPXINT vindex, const char *name_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETchgarcname (CPXCENVptr env, CPXNETptr net, CPXINT cnt,
                      const CPXINT *indices,
                      char const *const *newname);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETchgnodename (CPXCENVptr env, CPXNETptr net, CPXINT cnt,
                       const CPXINT *indices,
                       char const *const *newname);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETchgobjsen (CPXCENVptr env, CPXNETptr net, int maxormin);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETchgbds (CPXCENVptr env, CPXNETptr net, CPXINT cnt,
                  const CPXINT *indices, const char *lu,
                  const double *bd);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETchgarcnodes (CPXCENVptr env, CPXNETptr net, CPXINT cnt,
                       const CPXINT *indices, const CPXINT *fromnode,
                       const CPXINT *tonode);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETchgobj (CPXCENVptr env, CPXNETptr net, CPXINT cnt,
                  const CPXINT *indices, const double *obj);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETchgsupply (CPXCENVptr env, CPXNETptr net, CPXINT cnt,
                     const CPXINT *indices, const double *supply);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETgetobjsen (CPXCENVptr env, CPXCNETptr net);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETgetsupply (CPXCENVptr env, CPXCNETptr net, double *supply,
                     CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETgetprobname (CPXCENVptr env, CPXCNETptr net, char *buf_str,
                       CPXSIZE bufspace, CPXSIZE *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETgetnodename (CPXCENVptr env, CPXCNETptr net, char  **nnames,
                       char *namestore, CPXSIZE namespc,
                       CPXSIZE *surplus_p, CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETgetarcname (CPXCENVptr env, CPXCNETptr net, char  **nnames,
                      char *namestore, CPXSIZE namespc,
                      CPXSIZE *surplus_p, CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETgetlb (CPXCENVptr env, CPXCNETptr net, double *low,
                 CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETgetub (CPXCENVptr env, CPXCNETptr net, double *up,
                 CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETgetobj (CPXCENVptr env, CPXCNETptr net, double *obj,
                  CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETgetarcnodes (CPXCENVptr env, CPXCNETptr net,
                       CPXINT *fromnode, CPXINT *tonode, CPXINT begin,
                       CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETgetnodearcs (CPXCENVptr env, CPXCNETptr net,
                       CPXINT *arccnt_p, CPXINT *arcbeg, CPXINT *arc,
                       CPXINT arcspace, CPXINT *surplus_p,
                       CPXINT begin, CPXINT end);


CPXLIBAPI
CPXINT CPXPUBLIC
   CPXLNETgetnumnodes (CPXCENVptr env, CPXCNETptr net);


CPXLIBAPI
CPXINT CPXPUBLIC
   CPXLNETgetnumarcs (CPXCENVptr env, CPXCNETptr net);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETgetnodeindex (CPXCENVptr env, CPXCNETptr net,
                        const char *lname_str, CPXINT *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETgetarcindex (CPXCENVptr env, CPXCNETptr net,
                       const char *lname_str, CPXINT *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETreadcopyprob (CPXCENVptr env, CPXNETptr net,
                        const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETreadcopybase (CPXCENVptr env, CPXNETptr net,
                        const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETwriteprob (CPXCENVptr env, CPXCNETptr net,
                     const char *filename_str, const char *format_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLNETbasewrite (CPXCENVptr env, CPXCNETptr net,
                     const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLcopyctype (CPXCENVptr env, CPXLPptr lp, const char *xctype);


CPXLIBAPI
int CPXPUBLIC
   CPXLcopyorder (CPXCENVptr env, CPXLPptr lp, CPXINT cnt,
                  const CPXINT *indices, const CPXINT *priority,
                  const int *direction);


CPXLIBAPI
int CPXPUBLIC
   CPXLcopysos (CPXCENVptr env, CPXLPptr lp, CPXINT numsos,
                CPXLONG numsosnz, const char *sostype,
                const CPXLONG *sosbeg, const CPXINT *sosind,
                const double *soswt, char const *const *sosname);



CPXLIBAPI
int CPXPUBLIC
   CPXLchgmipstarts (CPXCENVptr env, CPXLPptr lp, int mcnt,
                     const int *mipstartindices, CPXLONG nzcnt,
                     const CPXLONG *beg, const CPXINT *varindices,
                     const double *values, const int *effortlevel);


CPXLIBAPI
int CPXPUBLIC
   CPXLaddmipstarts (CPXCENVptr env, CPXLPptr lp, int mcnt,
                     CPXLONG nzcnt, const CPXLONG *beg,
                     const CPXINT *varindices, const double *values,
                     const int *effortlevel,
                     char const *const *mipstartname);


CPXLIBAPI
int CPXPUBLIC
   CPXLdelmipstarts (CPXCENVptr env, CPXLPptr lp, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXLdelsetmipstarts (CPXCENVptr env, CPXLPptr lp, int *delstat);


CPXLIBAPI
int CPXPUBLIC
   CPXLrefinemipstartconflict (CPXCENVptr env, CPXLPptr lp,
                               int mipstartindex,
                               CPXINT *confnumrows_p,
                               CPXINT *confnumcols_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLrefinemipstartconflictext (CPXCENVptr env, CPXLPptr lp,
                                  int mipstartindex, CPXLONG grpcnt,
                                  CPXLONG concnt,
                                  const double *grppref,
                                  const CPXLONG *grpbeg,
                                  const CPXINT *grpind,
                                  const char *grptype);


CPXLIBAPI
int CPXPUBLIC
   CPXLmipopt (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXLgetmipitcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetbestobjval (CPXCENVptr env, CPXCLPptr lp, double *objval_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetmiprelgap (CPXCENVptr env, CPXCLPptr lp, double *gap_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcutoff (CPXCENVptr env, CPXCLPptr lp, double *cutoff_p);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXLgetnodecnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXLgetnodeleftcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXLgetnodeint (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetnumcuts (CPXCENVptr env, CPXCLPptr lp, int cuttype,
                   CPXINT *num_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetnummipstarts (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetmipstarts (CPXCENVptr env, CPXCLPptr lp, CPXLONG *nzcnt_p,
                     CPXLONG *beg, CPXINT *varindices, double *values,
                     int *effortlevel, CPXLONG startspace,
                     CPXLONG *surplus_p, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetmipstartname (CPXCENVptr env, CPXCLPptr lp, char  **name,
                        char *store, CPXSIZE storesz,
                        CPXSIZE *surplus_p, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetmipstartindex (CPXCENVptr env, CPXCLPptr lp,
                         const char *lname_str, int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsubstat (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsubmethod (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLchgctype (CPXCENVptr env, CPXLPptr lp, CPXINT cnt,
                 const CPXINT *indices, const char *xctype);


CPXLIBAPI
int CPXPUBLIC
   CPXLaddsos (CPXCENVptr env, CPXLPptr lp, CPXINT numsos,
               CPXLONG numsosnz, const char *sostype,
               const CPXLONG *sosbeg, const CPXINT *sosind,
               const double *soswt, char const *const *sosname);


CPXLIBAPI
int CPXPUBLIC
   CPXLdelsos (CPXCENVptr env, CPXLPptr lp, CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLdelsetsos (CPXCENVptr env, CPXLPptr lp, CPXINT *delset);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetctype (CPXCENVptr env, CPXCLPptr lp, char *xctype,
                 CPXINT begin, CPXINT end);


CPXLIBAPI
CPXINT CPXPUBLIC
   CPXLgetnumsos (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsos (CPXCENVptr env, CPXCLPptr lp, CPXLONG *numsosnz_p,
               char *sostype, CPXLONG *sosbeg, CPXINT *sosind,
               double *soswt, CPXLONG sosspace, CPXLONG *surplus_p,
               CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsosname (CPXCENVptr env, CPXCLPptr lp, char  **name,
                   char *namestore, CPXSIZE storespace,
                   CPXSIZE *surplus_p, CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsosindex (CPXCENVptr env, CPXCLPptr lp,
                    const char *lname_str, CPXINT *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsosinfeas (CPXCENVptr env, CPXCLPptr lp, const double *x,
                     double *infeasout, CPXINT begin, CPXINT end);


CPXLIBAPI
CPXINT CPXPUBLIC
   CPXLgetnumindconstrs (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetindconstr (CPXCENVptr env, CPXCLPptr lp, CPXINT *indvar_p,
                     int *complemented_p, CPXINT *nzcnt_p,
                     double *rhs_p, char *sense_p, CPXINT *linind,
                     double *linval, CPXINT space, CPXINT *surplus_p,
                     CPXINT which);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetindconstrindex (CPXCENVptr env, CPXCLPptr lp,
                          const char *lname_str, CPXINT *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetindconstrname (CPXCENVptr env, CPXCLPptr lp, char *buf_str,
                         CPXSIZE bufspace, CPXSIZE *surplus_p,
                         CPXINT which);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetindconstrslack (CPXCENVptr env, CPXCLPptr lp,
                          double *indslack, CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLindconstrslackfromx (CPXCENVptr env, CPXCLPptr lp,
                            const double *x, double *indslack);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetindconstrinfeas (CPXCENVptr env, CPXCLPptr lp,
                           const double *x, double *infeasout,
                           CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLdelindconstrs (CPXCENVptr env, CPXLPptr lp, CPXINT begin,
                      CPXINT end);


CPXLIBAPI
CPXINT CPXPUBLIC
   CPXLgetnumint (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
CPXINT CPXPUBLIC
   CPXLgetnumbin (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
CPXINT CPXPUBLIC
   CPXLgetnumsemicont (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
CPXINT CPXPUBLIC
   CPXLgetnumsemiint (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetorder (CPXCENVptr env, CPXCLPptr lp, CPXINT *cnt_p,
                 CPXINT *indices, CPXINT *priority, int *direction,
                 CPXINT ordspace, CPXINT *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLpopulate (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsolnpoolnumfilters (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLaddsolnpooldivfilter (CPXCENVptr env, CPXLPptr lp,
                             double lower_bound, double upper_bound,
                             CPXINT nzcnt, const CPXINT *ind,
                             const double *weight,
                             const double *refval,
                             const char *lname_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLaddsolnpoolrngfilter (CPXCENVptr env, CPXLPptr lp, double lb,
                             double ub, CPXINT nzcnt,
                             const CPXINT *ind, const double *val,
                             const char *lname_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsolnpoolfiltertype (CPXCENVptr env, CPXCLPptr lp,
                              int *ftype_p, int which);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsolnpooldivfilter (CPXCENVptr env, CPXCLPptr lp,
                             double *lower_cutoff_p,
                             double *upper_cutoff_p, CPXINT *nzcnt_p,
                             CPXINT *ind, double *val, double *refval,
                             CPXINT space, CPXINT *surplus_p,
                             int which);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsolnpoolfilter (CPXCENVptr env, CPXCLPptr lp, int *ftype_p,
                          double *lowercutoff_p,
                          double *upper_cutoff_p, CPXINT *nzcnt_p,
                          CPXINT *ind, double *val, double *refval,
                          CPXINT space, CPXINT *surplus_p, int which);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsolnpoolrngfilter (CPXCENVptr env, CPXCLPptr lp,
                             double *lb_p, double *ub_p,
                             CPXINT *nzcnt_p, CPXINT *ind, double *val,
                             CPXINT space, CPXINT *surplus_p,
                             int which);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsolnpoolfiltername (CPXCENVptr env, CPXCLPptr lp,
                              char *buf_str, CPXSIZE bufspace,
                              CPXSIZE *surplus_p, int which);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsolnpoolfilterindex (CPXCENVptr env, CPXCLPptr lp,
                               const char *lname_str, int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLdelsolnpoolfilters (CPXCENVptr env, CPXLPptr lp, int begin,
                           int end);


CPXLIBAPI
int CPXPUBLIC
   CPXLdelsetsolnpoolfilters (CPXCENVptr env, CPXLPptr lp,
                              int *delstat);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsolnpoolnumsolns (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsolnpoolnumreplaced (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsolnpoolmeanobjval (CPXCENVptr env, CPXCLPptr lp,
                              double *meanobjval_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsolnpoolobjval (CPXCENVptr env, CPXCLPptr lp, int soln,
                          double *objval_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsolnpoolx (CPXCENVptr env, CPXCLPptr lp, int soln, double *x,
                     CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsolnpoolslack (CPXCENVptr env, CPXCLPptr lp, int soln,
                         double *slack, CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsolnpoolqconstrslack (CPXCENVptr env, CPXCLPptr lp, int soln,
                                double *qcslack, CPXINT begin,
                                CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsolnpoolsolnname (CPXCENVptr env, CPXCLPptr lp, char *store,
                            CPXSIZE storesz, CPXSIZE *surplus_p,
                            int which);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsolnpoolsolnindex (CPXCENVptr env, CPXCLPptr lp,
                             const char *lname_str, int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLdelsolnpoolsolns (CPXCENVptr env, CPXLPptr lp, int begin,
                         int end);


CPXLIBAPI
int CPXPUBLIC
   CPXLdelsetsolnpoolsolns (CPXCENVptr env, CPXLPptr lp, int *delstat);


CPXLIBAPI
int CPXPUBLIC
   CPXLreadcopyorder (CPXCENVptr env, CPXLPptr lp,
                      const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLreadcopysolnpoolfilters (CPXCENVptr env, CPXLPptr lp,
                                const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLreadcopymipstarts (CPXCENVptr env, CPXLPptr lp,
                          const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLordwrite (CPXCENVptr env, CPXCLPptr lp,
                 const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLwritemipstarts (CPXCENVptr env, CPXCLPptr lp,
                       const char *filename_str, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXLfltwrite (CPXCENVptr env, CPXCLPptr lp,
                 const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetinfocallbackfunc (CPXENVptr env, CPXL_CALLBACK *callback,
                            void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetinfocallbackfunc (CPXCENVptr env, CPXL_CALLBACK **callback_p,
                            void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetmipcallbackfunc (CPXENVptr env, CPXL_CALLBACK *callback,
                           void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetmipcallbackfunc (CPXCENVptr env, CPXL_CALLBACK **callback_p,
                           void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetbranchcallbackfunc (CPXENVptr env,
                              CPXL_CALLBACK_BRANCH *branchcallback,
                              void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetbranchnosolncallbackfunc (CPXENVptr env,
                                    CPXL_CALLBACK_BRANCH *branchnosolncallback,
                                    void *cbhandle);



CPXLIBAPI
int CPXPUBLIC
   CPXLsetlazyconstraintcallbackfunc (CPXENVptr env,
                                      CPXL_CALLBACK_CUT *lazyconcallback,
                                      void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetusercutcallbackfunc (CPXENVptr env,
                               CPXL_CALLBACK_CUT *cutcallback,
                               void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetnodecallbackfunc (CPXENVptr env,
                            CPXL_CALLBACK_NODE *nodecallback,
                            void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetheuristiccallbackfunc (CPXENVptr env,
                                 CPXL_CALLBACK_HEURISTIC *heuristiccallback,
                                 void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetincumbentcallbackfunc (CPXENVptr env,
                                 CPXL_CALLBACK_INCUMBENT *incumbentcallback,
                                 void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetsolvecallbackfunc (CPXENVptr env,
                             CPXL_CALLBACK_SOLVE *solvecallback,
                             void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXLsetdeletenodecallbackfunc (CPXENVptr env,
                                  CPXL_CALLBACK_DELETENODE *deletecallback,
                                  void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetbranchcallbackfunc (CPXCENVptr env,
                              CPXL_CALLBACK_BRANCH **branchcallback_p,
                              void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetbranchnosolncallbackfunc (CPXCENVptr env,
                                    CPXL_CALLBACK_BRANCH **branchnosolncallback_p,
                                    void  **cbhandle_p);



CPXLIBAPI
int CPXPUBLIC
   CPXLgetlazyconstraintcallbackfunc (CPXCENVptr env,
                                      CPXL_CALLBACK_CUT **cutcallback_p,
                                      void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetusercutcallbackfunc (CPXCENVptr env,
                               CPXL_CALLBACK_CUT **cutcallback_p,
                               void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetnodecallbackfunc (CPXCENVptr env,
                            CPXL_CALLBACK_NODE **nodecallback_p,
                            void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetheuristiccallbackfunc (CPXCENVptr env,
                                 CPXL_CALLBACK_HEURISTIC **heuristiccallback_p,
                                 void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetincumbentcallbackfunc (CPXCENVptr env,
                                 CPXL_CALLBACK_INCUMBENT **incumbentcallback_p,
                                 void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetsolvecallbackfunc (CPXCENVptr env,
                             CPXL_CALLBACK_SOLVE **solvecallback_p,
                             void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetdeletenodecallbackfunc (CPXCENVptr env,
                                  CPXL_CALLBACK_DELETENODE **deletecallback_p,
                                  void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcallbacknodelp (CPXCENVptr env, void *cbdata, int wherefrom,
                          CPXLPptr *nodelp_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcallbacknodeinfo (CPXCENVptr env, void *cbdata,
                            int wherefrom, CPXLONG nodeindex,
                            int whichinfo, void *result_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcallbackseqinfo (CPXCENVptr env, void *cbdata, int wherefrom,
                           CPXLONG seqid, int whichinfo,
                           void *result_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcallbacksosinfo (CPXCENVptr env, void *cbdata, int wherefrom,
                           CPXINT sosindex, CPXINT member,
                           int whichinfo, void *result_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcallbackindicatorinfo (CPXCENVptr env, void *cbdata,
                                 int wherefrom, CPXINT iindex,
                                 int whichinfo, void *result_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLcutcallbackadd (CPXCENVptr env, void *cbdata, int wherefrom,
                       CPXINT nzcnt, double rhs, int sense,
                       const CPXINT *cutind, const double *cutval,
                       int purgeable);


CPXLIBAPI
int CPXPUBLIC
   CPXLcutcallbackaddlocal (CPXCENVptr env, void *cbdata,
                            int wherefrom, CPXINT nzcnt, double rhs,
                            int sense, const CPXINT *cutind,
                            const double *cutval);


CPXLIBAPI
int CPXPUBLIC
   CPXLbranchcallbackbranchasCPLEX (CPXCENVptr env, void *cbdata,
                                    int wherefrom, int num,
                                    void *userhandle,
                                    CPXLONG *seqnum_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLbranchcallbackbranchbds (CPXCENVptr env, void *cbdata,
                                int wherefrom, double nodeest,
                                CPXINT cnt, const CPXINT *indices,
                                const char *lu, const int *bd,
                                void *userhandle, CPXLONG *seqnum_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLbranchcallbackbranchgeneral (CPXCENVptr env, void *cbdata,
                                    int wherefrom, double nodeest,
                                    CPXINT varcnt,
                                    const CPXINT *varind,
                                    const char *varlu,
                                    const int *varbd, CPXINT rcnt,
                                    CPXLONG nzcnt, const double *rhs,
                                    const char *sense,
                                    const CPXLONG *rmatbeg,
                                    const CPXINT *rmatind,
                                    const double *rmatval,
                                    void *userhandle,
                                    CPXLONG *seqnum_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLbranchcallbackbranchconstraints (CPXCENVptr env, void *cbdata,
                                        int wherefrom, double nodeest,
                                        CPXINT rcnt, CPXLONG nzcnt,
                                        const double *rhs,
                                        const char *sense,
                                        const CPXLONG *rmatbeg,
                                        const CPXINT *rmatind,
                                        const double *rmatval,
                                        void *userhandle,
                                        CPXLONG *seqnum_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcallbacknodex (CPXCENVptr env, void *cbdata, int wherefrom,
                         double *x, CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcallbacknodeobjval (CPXCENVptr env, void *cbdata,
                              int wherefrom, double *objval_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcallbackctype (CPXCENVptr env, void *cbdata, int wherefrom,
                         char *xctype, CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcallbackorder (CPXCENVptr env, void *cbdata, int wherefrom,
                         CPXINT *priority, int *direction,
                         CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcallbackpseudocosts (CPXCENVptr env, void *cbdata,
                               int wherefrom, double *uppc,
                               double *downpc, CPXINT begin,
                               CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcallbackincumbent (CPXCENVptr env, void *cbdata,
                             int wherefrom, double *x, CPXINT begin,
                             CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcallbacknodeintfeas (CPXCENVptr env, void *cbdata,
                               int wherefrom, int *feas, CPXINT begin,
                               CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcallbackgloballb (CPXCENVptr env, void *cbdata,
                            int wherefrom, double *lb, CPXINT begin,
                            CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcallbackglobalub (CPXCENVptr env, void *cbdata,
                            int wherefrom, double *ub, CPXINT begin,
                            CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcallbacknodelb (CPXCENVptr env, void *cbdata, int wherefrom,
                          double *lb, CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcallbacknodeub (CPXCENVptr env, void *cbdata, int wherefrom,
                          double *ub, CPXINT begin, CPXINT end);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcallbacklp (CPXCENVptr env, void *cbdata, int wherefrom,
                      CPXCLPptr *lp_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLcallbacksetuserhandle (CPXCENVptr env, void *cbdata,
                              int wherefrom, void *userhandle,
                              void  **olduserhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLcallbacksetnodeuserhandle (CPXCENVptr env, void *cbdata,
                                  int wherefrom, CPXLONG nodeindex,
                                  void *userhandle,
                                  void  **olduserhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcallbacknodestat (CPXCENVptr env, void *cbdata,
                            int wherefrom, int *nodestat_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLgetcallbackbranchconstraints (CPXCENVptr env, void *cbdata,
                                     int wherefrom, int which,
                                     CPXINT *cuts_p, CPXLONG *nzcnt_p,
                                     double *rhs, char *sense,
                                     CPXLONG *rmatbeg, CPXINT *rmatind,
                                     double *rmatval, CPXLONG rmatsz,
                                     CPXLONG *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXLaddusercuts (CPXCENVptr env, CPXLPptr lp, CPXINT rcnt,
                    CPXLONG nzcnt, const double *rhs,
                    const char *sense, const CPXLONG *rmatbeg,
                    const CPXINT *rmatind, const double *rmatval,
                    char const *const *rowname);


CPXLIBAPI
int CPXPUBLIC
   CPXLaddlazyconstraints (CPXCENVptr env, CPXLPptr lp, CPXINT rcnt,
                           CPXLONG nzcnt, const double *rhs,
                           const char *sense, const CPXLONG *rmatbeg,
                           const CPXINT *rmatind,
                           const double *rmatval,
                           char const *const *rowname);


CPXLIBAPI
int CPXPUBLIC
   CPXLfreeusercuts (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLfreelazyconstraints (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXLordread (CPXCENVptr env, const char *filename_str,
                CPXINT numcols, char const *const *colname,
                CPXINT *cnt_p, CPXINT *indices, CPXINT *priority,
                int *direction);



#endif /* 64 bit port */

#ifdef __cplusplus
}
#endif

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif /* !CPX_CPLEXL_H */
