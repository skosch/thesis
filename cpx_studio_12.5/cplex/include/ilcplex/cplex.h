/* --------------------------------------------------------------------------
 * File: cpx.h
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

#ifndef CPX_H
#   define CPX_H 1
#include "cpxconst.h"

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#ifdef __cplusplus
extern "C" {
#endif

#ifndef CPX_MODERN
#define CPXoptimize CPXlpopt
#endif

#ifndef CPX_MODERN
#define CPXgetsbcnt CPXgetpsbcnt
#endif

#define CPXRegisterLicense CPXsetstaringsol

CPXLIBAPI
CPXLPptr CPXPUBLIC
   CPXcreateprob (CPXCENVptr env, int *status_p,
                  const char *probname_str);


CPXLIBAPI
CPXLPptr CPXPUBLIC
   CPXcloneprob (CPXCENVptr env, CPXCLPptr lp, int *status_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopylpwnames (CPXCENVptr env, CPXLPptr lp, int numcols,
                    int numrows, int objsense, const double *objective,
                    const double *rhs, const char *sense,
                    const int *matbeg, const int *matcnt,
                    const int *matind, const double *matval,
                    const double *lb, const double *ub,
                    const double *rngval, char **colname,
                    char **rowname);


CPXLIBAPI
int CPXPUBLIC
   CPXcopylp (CPXCENVptr env, CPXLPptr lp, int numcols, int numrows,
              int objsense, const double *objective, const double *rhs,
              const char *sense, const int *matbeg, const int *matcnt,
              const int *matind, const double *matval,
              const double *lb, const double *ub, const double *rngval);


CPXLIBAPI
int CPXPUBLIC
   CPXcopyobjname (CPXCENVptr env, CPXLPptr lp,
                   const char *objname_str);


CPXLIBAPI
int CPXPUBLIC
   CPXcopybase (CPXCENVptr env, CPXLPptr lp, const int *cstat,
                const int *rstat);


CPXLIBAPI
int CPXPUBLIC
   CPXcleanup (CPXCENVptr env, CPXLPptr lp, double eps);


CPXLIBAPI
int CPXPUBLIC
   CPXcopystart (CPXCENVptr env, CPXLPptr lp, const int *cstat,
                 const int *rstat, const double *cprim,
                 const double *rprim, const double *cdual,
                 const double *rdual);


CPXLIBAPI
int CPXPUBLIC
   CPXfreeprob (CPXCENVptr env, CPXLPptr *lp_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopynettolp (CPXCENVptr env, CPXLPptr lp, CPXCNETptr net);


CPXLIBAPI
int CPXPUBLIC
   CPXNETextract (CPXCENVptr env, CPXNETptr net, CPXCLPptr lp,
                  int *colmap, int *rowmap);


CPXLIBAPI
int CPXPUBLIC
   CPXlpopt (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXprimopt (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXdualopt (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXhybnetopt (CPXCENVptr env, CPXLPptr lp, int method);


CPXLIBAPI
int CPXPUBLIC
   CPXsiftopt (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXpratio (CPXCENVptr env, CPXLPptr lp, int *indices, int cnt,
              double *downratio, double *upratio, int *downleave,
              int *upleave, int *downleavestatus, int *upleavestatus,
              int *downstatus, int *upstatus);


CPXLIBAPI
int CPXPUBLIC
   CPXdratio (CPXCENVptr env, CPXLPptr lp, int *indices, int cnt,
              double *downratio, double *upratio, int *downenter,
              int *upenter, int *downstatus, int *upstatus);


CPXLIBAPI
int CPXPUBLIC
   CPXpivot (CPXCENVptr env, CPXLPptr lp, int jenter, int jleave,
             int leavestat);


CPXLIBAPI
int CPXPUBLIC
   CPXsetphase2 (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXcheckpfeas (CPXCENVptr env, CPXLPptr lp, int *infeas_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcheckdfeas (CPXCENVptr env, CPXLPptr lp, int *infeas_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsolution (CPXCENVptr env, CPXCLPptr lp, int *lpstat_p,
                double *objval_p, double *x, double *pi, double *slack,
                double *dj);


CPXLIBAPI
int CPXPUBLIC
   CPXsolninfo (CPXCENVptr env, CPXCLPptr lp, int *solnmethod_p,
                int *solntype_p, int *pfeasind_p, int *dfeasind_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetstat (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
CPXCHARptr CPXPUBLIC
   CPXgetstatstring (CPXCENVptr env, int statind, char *buffer_str);


CPXLIBAPI
int CPXPUBLIC
   CPXgetmethod (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetobjval (CPXCENVptr env, CPXCLPptr lp, double *objval_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetx (CPXCENVptr env, CPXCLPptr lp, double *x, int begin,
            int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetax (CPXCENVptr env, CPXCLPptr lp, double *x, int begin,
             int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetpi (CPXCENVptr env, CPXCLPptr lp, double *pi, int begin,
             int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetslack (CPXCENVptr env, CPXCLPptr lp, double *slack, int begin,
                int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetrowinfeas (CPXCENVptr env, CPXCLPptr lp, const double *x,
                    double *infeasout, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcolinfeas (CPXCENVptr env, CPXCLPptr lp, const double *x,
                    double *infeasout, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetdj (CPXCENVptr env, CPXCLPptr lp, double *dj, int begin,
             int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetgrad (CPXCENVptr env, CPXCLPptr lp, int j, int *head,
               double *y);


CPXLIBAPI
int CPXPUBLIC
   CPXgetijdiv (CPXCENVptr env, CPXCLPptr lp, int *idiv_p, int *jdiv_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetbase (CPXCENVptr env, CPXCLPptr lp, int *cstat, int *rstat);


CPXLIBAPI
int CPXPUBLIC
   CPXgetitcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetphase1cnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsiftitcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsiftphase1cnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetbaritcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcrossppushcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcrosspexchcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcrossdpushcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcrossdexchcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetpsbcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetdsbcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetdblquality (CPXCENVptr env, CPXCLPptr lp, double *quality_p,
                     int what);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpooldblquality (CPXCENVptr env, CPXCLPptr lp, int soln,
                             double *quality_p, int what);


CPXLIBAPI
int CPXPUBLIC
   CPXgetintquality (CPXCENVptr env, CPXCLPptr lp, int *quality_p,
                     int what);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolintquality (CPXCENVptr env, CPXCLPptr lp, int soln,
                             int *quality_p, int what);


CPXLIBAPI
int CPXPUBLIC
   CPXrhssa (CPXCENVptr env, CPXCLPptr lp, int begin, int end,
             double *lower, double *upper);


CPXLIBAPI
int CPXPUBLIC
   CPXboundsa (CPXCENVptr env, CPXCLPptr lp, int begin, int end,
               double *lblower, double *lbupper, double *ublower,
               double *ubupper);


CPXLIBAPI
int CPXPUBLIC
   CPXobjsa (CPXCENVptr env, CPXCLPptr lp, int begin, int end,
             double *lower, double *upper);


CPXLIBAPI
int CPXPUBLIC
   CPXrefineconflict (CPXCENVptr env, CPXLPptr lp, int *confnumrows_p,
                      int *confnumcols_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetconflict (CPXCENVptr env, CPXCLPptr lp, int *confstat_p,
                   int *rowind, int *rowbdstat, int *confnumrows_p,
                   int *colind, int *colbdstat, int *confnumcols_p);


CPXLIBAPI
int CPXPUBLIC
   CPXrefineconflictext (CPXCENVptr env, CPXLPptr lp, int grpcnt,
                         int concnt, const double *grppref,
                         const int *grpbeg, const int *grpind,
                         const char *grptype);


CPXLIBAPI
int CPXPUBLIC
   CPXgetconflictext (CPXCENVptr env, CPXCLPptr lp, int *grpstat,
                      int beg, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXclpwrite (CPXCENVptr env, CPXCLPptr lp, const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXrobustopt (CPXCENVptr env, CPXLPptr lp, CPXLPptr lblp,
                 CPXLPptr ublp, double objchg, const double *maxchg);


CPXLIBAPI
int CPXPUBLIC
   CPXfeasopt (CPXCENVptr env, CPXLPptr lp, const double *rhs,
               const double *rng, const double *lb, const double *ub);


CPXLIBAPI
int CPXPUBLIC
   CPXfeasoptext (CPXCENVptr env, CPXLPptr lp, int grpcnt, int concnt,
                  const double *grppref, const int *grpbeg,
                  const int *grpind, const char *grptype);


CPXLIBAPI
int CPXPUBLIC
   CPXnewrows (CPXCENVptr env, CPXLPptr lp, int rcnt,
               const double *rhs, const char *sense,
               const double *rngval, char **rowname);


CPXLIBAPI
int CPXPUBLIC
   CPXaddrows (CPXCENVptr env, CPXLPptr lp, int ccnt, int rcnt,
               int nzcnt, const double *rhs, const char *sense,
               const int *rmatbeg, const int *rmatind,
               const double *rmatval, char **colname, char **rowname);


CPXLIBAPI
int CPXPUBLIC
   CPXnewcols (CPXCENVptr env, CPXLPptr lp, int ccnt,
               const double *obj, const double *lb, const double *ub,
               const char *xctype, char **colname);


CPXLIBAPI
int CPXPUBLIC
   CPXaddcols (CPXCENVptr env, CPXLPptr lp, int ccnt, int nzcnt,
               const double *obj, const int *cmatbeg,
               const int *cmatind, const double *cmatval,
               const double *lb, const double *ub, char **colname);


CPXLIBAPI
int CPXPUBLIC
   CPXdelrows (CPXCENVptr env, CPXLPptr lp, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsetrows (CPXCENVptr env, CPXLPptr lp, int *delstat);


CPXLIBAPI
int CPXPUBLIC
   CPXdelcols (CPXCENVptr env, CPXLPptr lp, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsetcols (CPXCENVptr env, CPXLPptr lp, int *delstat);


CPXLIBAPI
int CPXPUBLIC
   CPXchgname (CPXCENVptr env, CPXLPptr lp, int key, int ij,
               const char *newname_str);


CPXLIBAPI
int CPXPUBLIC
   CPXchgrowname (CPXCENVptr env, CPXLPptr lp, int cnt,
                  const int *indices, char **newname);


CPXLIBAPI
int CPXPUBLIC
   CPXchgcolname (CPXCENVptr env, CPXLPptr lp, int cnt,
                  const int *indices, char **newname);


CPXLIBAPI
int CPXPUBLIC
   CPXdelnames (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXchgprobname (CPXCENVptr env, CPXLPptr lp, const char *probname);


CPXLIBAPI
int CPXPUBLIC
   CPXchgcoef (CPXCENVptr env, CPXLPptr lp, int i, int j,
               double newvalue);


CPXLIBAPI
int CPXPUBLIC
   CPXchgcoeflist (CPXCENVptr env, CPXLPptr lp, int numcoefs,
                   const int *rowlist, const int *collist,
                   const double *vallist);


CPXLIBAPI
int CPXPUBLIC
   CPXchgbds (CPXCENVptr env, CPXLPptr lp, int cnt, const int *indices,
              const char *lu, const double *bd);


CPXLIBAPI
int CPXPUBLIC
   CPXchgobj (CPXCENVptr env, CPXLPptr lp, int cnt, const int *indices,
              const double *values);


CPXLIBAPI
int CPXPUBLIC
   CPXchgrhs (CPXCENVptr env, CPXLPptr lp, int cnt, const int *indices,
              const double *values);


CPXLIBAPI
int CPXPUBLIC
   CPXchgrngval (CPXCENVptr env, CPXLPptr lp, int cnt,
                 const int *indices, const double *values);


CPXLIBAPI
int CPXPUBLIC
   CPXchgsense (CPXCENVptr env, CPXLPptr lp, int cnt,
                const int *indices, const char *sense);


CPXLIBAPI
int CPXPUBLIC
   CPXchgobjsen (CPXCENVptr env, CPXLPptr lp, int maxormin);


CPXLIBAPI
int CPXPUBLIC
   CPXchgprobtype (CPXCENVptr env, CPXLPptr lp, int type);


CPXLIBAPI
int CPXPUBLIC
   CPXchgprobtypesolnpool (CPXCENVptr env, CPXLPptr lp, int type,
                           int soln);


CPXLIBAPI
int CPXPUBLIC
   CPXcompletelp (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXpreaddrows (CPXCENVptr env, CPXLPptr lp, int rcnt, int nzcnt,
                  const double *rhs, const char *sense,
                  const int *rmatbeg, const int *rmatind,
                  const double *rmatval, char **rowname);


CPXLIBAPI
int CPXPUBLIC
   CPXprechgobj (CPXCENVptr env, CPXLPptr lp, int cnt,
                 const int *indices, const double *values);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnumcols (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnumrows (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnumnz (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetobjsen (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetobj (CPXCENVptr env, CPXCLPptr lp, double *obj, int begin,
              int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetrhs (CPXCENVptr env, CPXCLPptr lp, double *rhs, int begin,
              int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsense (CPXCENVptr env, CPXCLPptr lp, char *sense, int begin,
                int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcols (CPXCENVptr env, CPXCLPptr lp, int *nzcnt_p,
               int *cmatbeg, int *cmatind, double *cmatval,
               int cmatspace, int *surplus_p, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetrows (CPXCENVptr env, CPXCLPptr lp, int *nzcnt_p,
               int *rmatbeg, int *rmatind, double *rmatval,
               int rmatspace, int *surplus_p, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetlb (CPXCENVptr env, CPXCLPptr lp, double *lb, int begin,
             int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetub (CPXCENVptr env, CPXCLPptr lp, double *ub, int begin,
             int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetrngval (CPXCENVptr env, CPXCLPptr lp, double *rngval,
                 int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetprobname (CPXCENVptr env, CPXCLPptr lp, char *buf_str,
                   int bufspace, int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetobjname (CPXCENVptr env, CPXCLPptr lp, char *buf_str,
                  int bufspace, int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcolname (CPXCENVptr env, CPXCLPptr lp, char  **name,
                  char *namestore, int storespace, int *surplus_p,
                  int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetrowname (CPXCENVptr env, CPXCLPptr lp, char  **name,
                  char *namestore, int storespace, int *surplus_p,
                  int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcoef (CPXCENVptr env, CPXCLPptr lp, int i, int j,
               double *coef_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetrowindex (CPXCENVptr env, CPXCLPptr lp, const char *lname_str,
                   int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcolindex (CPXCENVptr env, CPXCLPptr lp, const char *lname_str,
                   int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetprobtype (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopyprob (CPXCENVptr env, CPXLPptr lp,
                    const char *filename_str, const char *filetype_str);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopybase (CPXCENVptr env, CPXLPptr lp,
                    const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopysol (CPXCENVptr env, CPXLPptr lp,
                   const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXwriteprob (CPXCENVptr env, CPXCLPptr lp,
                 const char *filename_str, const char *filetype_str);


CPXLIBAPI
int CPXPUBLIC
   CPXmbasewrite (CPXCENVptr env, CPXCLPptr lp,
                  const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXsolwrite (CPXCENVptr env, CPXCLPptr lp, const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXsolwritesolnpool (CPXCENVptr env, CPXCLPptr lp, int soln,
                        const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXsolwritesolnpoolall (CPXCENVptr env, CPXCLPptr lp,
                           const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXembwrite (CPXCENVptr env, CPXLPptr lp, const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXdperwrite (CPXCENVptr env, CPXLPptr lp, const char *filename_str,
                 double epsilon);


CPXLIBAPI
int CPXPUBLIC
   CPXpperwrite (CPXCENVptr env, CPXLPptr lp, const char *filename_str,
                 double epsilon);


CPXLIBAPI
int CPXPUBLIC
   CPXpreslvwrite (CPXCENVptr env, CPXLPptr lp,
                   const char *filename_str, double *objoff_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdualwrite (CPXCENVptr env, CPXCLPptr lp,
                 const char *filename_str, double *objshift_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsetdefaults (CPXENVptr env);


CPXLIBAPI
int CPXPUBLIC
   CPXsetintparam (CPXENVptr env, int whichparam, CPXINT newvalue);


CPXLIBAPI
int CPXPUBLIC
   CPXsetlongparam (CPXENVptr env, int whichparam, CPXLONG newvalue);


CPXLIBAPI
int CPXPUBLIC
   CPXsetdblparam (CPXENVptr env, int whichparam, double newvalue);


CPXLIBAPI
int CPXPUBLIC
   CPXsetstrparam (CPXENVptr env, int whichparam,
                   const char *newvalue_str);


CPXLIBAPI
int CPXPUBLIC
   CPXgetintparam (CPXCENVptr env, int whichparam, CPXINT *value_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetlongparam (CPXCENVptr env, int whichparam, CPXLONG *value_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetdblparam (CPXCENVptr env, int whichparam, double *value_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetstrparam (CPXCENVptr env, int whichparam, char *value_str);


CPXLIBAPI
int CPXPUBLIC
   CPXinfointparam (CPXCENVptr env, int whichparam, CPXINT *defvalue_p,
                    CPXINT *minvalue_p, CPXINT *maxvalue_p);


CPXLIBAPI
int CPXPUBLIC
   CPXinfolongparam (CPXCENVptr env, int whichparam,
                     CPXLONG *defvalue_p, CPXLONG *minvalue_p,
                     CPXLONG *maxvalue_p);


CPXLIBAPI
int CPXPUBLIC
   CPXinfodblparam (CPXCENVptr env, int whichparam, double *defvalue_p,
                    double *minvalue_p, double *maxvalue_p);


CPXLIBAPI
int CPXPUBLIC
   CPXinfostrparam (CPXCENVptr env, int whichparam, char *defvalue_str);


CPXLIBAPI
int CPXPUBLIC
   CPXgetparamname (CPXCENVptr env, int whichparam, char *name_str);


CPXLIBAPI
int CPXPUBLIC
   CPXgetparamnum (CPXCENVptr env, const char *name_str,
                   int *whichparam_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetparamtype (CPXCENVptr env, int whichparam, int *paramtype);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopyparam (CPXENVptr env, const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXwriteparam (CPXCENVptr env, const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXgetchgparam (CPXCENVptr env, int *cnt_p, int *paramnum,
                   int pspace, int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXtuneparam (CPXENVptr env, CPXLPptr lp, int intcnt,
                 const int *intnum, const int *intval, int dblcnt,
                 const int *dblnum, const double *dblval, int strcnt,
                 const int *strnum, char **strval, int *tunestat_p);


CPXLIBAPI
int CPXPUBLIC
   CPXtuneparamprobset (CPXENVptr env, int filecnt, char **filename,
                        char **filetype, int intcnt, const int *intnum,
                        const int *intval, int dblcnt,
                        const int *dblnum, const double *dblval,
                        int strcnt, const int *strnum, char **strval,
                        int *tunestat_p);


CPXLIBAPI
CPXCCHARptr CPXPUBLIC
   CPXversion (CPXCENVptr env);


CPXLIBAPI
CPXENVptr CPXPUBLIC
   CPXopenCPLEX (int *status_p);


CPXLIBAPI
CPXENVptr CPXPUBLIC
   CPXopenCPLEXruntime (int *status_p, int serialnum,
                        const char *licenvstring_str);


CPXLIBAPI
int CPXPUBLIC
   CPXcloseCPLEX (CPXENVptr *env_p);


CPXDEPRECATEDAPI(12040000)
int CPXPUBLIC
   CPXRegisterLicense (const char *ilm_license_str,
                       int ilm_license_signature);


CPXLIBAPI
CPXENVptr CPXPUBLIC
   CPXparenv (CPXENVptr env, int *status_p);


CPXLIBAPI
int CPXPUBLIC
   CPXfreeparenv (CPXENVptr env, CPXENVptr *child_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetchannels (CPXCENVptr env, CPXCHANNELptr *cpxresults_p,
                   CPXCHANNELptr *cpxwarning_p,
                   CPXCHANNELptr *cpxerror_p, CPXCHANNELptr *cpxlog_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsetlogfile (CPXENVptr env, CPXFILEptr lfile);


CPXLIBAPI
int CPXPUBLIC
   CPXgetlogfile (CPXCENVptr env, CPXFILEptr *logfile_p);


CPXLIBAPI
int CPXPUBVARARGS
   CPXmsg (CPXCHANNELptr channel, const char *format, ...) ;


CPXLIBAPI
int CPXPUBLIC
   CPXmsgstr (CPXCHANNELptr channel, const char *msg_str);


CPXLIBAPI
int CPXPUBLIC
   CPXflushchannel (CPXCENVptr env, CPXCHANNELptr channel);


CPXLIBAPI
int CPXPUBLIC
   CPXflushstdchannels (CPXCENVptr env);


CPXLIBAPI
CPXCHANNELptr CPXPUBLIC
   CPXaddchannel (CPXENVptr env);


CPXLIBAPI
int CPXPUBLIC
   CPXaddfpdest (CPXCENVptr env, CPXCHANNELptr channel,
                 CPXFILEptr fileptr);


CPXLIBAPI
int CPXPUBLIC
   CPXdelfpdest (CPXCENVptr env, CPXCHANNELptr channel,
                 CPXFILEptr fileptr);


CPXLIBAPI
int CPXPUBLIC
   CPXaddfuncdest (CPXCENVptr env, CPXCHANNELptr channel, void *handle,
                   void(CPXPUBLIC *msgfunction)(void *, const char *));


CPXLIBAPI
int CPXPUBLIC
   CPXdelfuncdest (CPXCENVptr env, CPXCHANNELptr channel, void *handle,
                   void(CPXPUBLIC *msgfunction)(void *, const char *));


CPXLIBAPI
int CPXPUBLIC
   CPXdelchannel (CPXENVptr env, CPXCHANNELptr *channel_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdisconnectchannel (CPXCENVptr env, CPXCHANNELptr channel);


CPXLIBAPI
CPXCCHARptr CPXPUBLIC
   CPXgeterrorstring (CPXCENVptr env, int errcode, char *buffer_str);


CPXLIBAPI
int CPXPUBLIC
   CPXsetlpcallbackfunc (CPXENVptr env,
                         int(CPXPUBLIC *callback)(CPXCENVptr, void *, int, void *),
                         void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXsetnetcallbackfunc (CPXENVptr env,
                          int(CPXPUBLIC *callback)(CPXCENVptr, void *, int, void *),
                          void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXsettuningcallbackfunc (CPXENVptr env,
                             int(CPXPUBLIC *callback)(CPXCENVptr, void *, int, void *),
                             void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcallbackinfo (CPXCENVptr env, void *cbdata, int wherefrom,
                       int whichinfo, void *result_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetlpcallbackfunc (CPXCENVptr env,
                         int(CPXPUBLIC **callback_p)(CPXCENVptr, void *, int, void *),
                         void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnetcallbackfunc (CPXCENVptr env,
                          int(CPXPUBLIC **callback_p)(CPXCENVptr, void *, int, void *),
                          void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgettuningcallbackfunc (CPXCENVptr env,
                             int(CPXPUBLIC **callback_p)(CPXCENVptr, void *, int, void *),
                             void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsetprofcallbackfunc (CPXENVptr env,
                           int(CPXPUBLIC *callback)(CPXCENVptr, int, void *),
                           void *cbhandle);


CPXLIBAPI
CPXFILEptr CPXPUBLIC
   CPXfopen (const char *filename_str, const char *type_str);


CPXLIBAPI
int CPXPUBLIC
   CPXfclose (CPXFILEptr stream);


CPXLIBAPI
int CPXPUBLIC
   CPXfputs (const char *s_str, CPXFILEptr stream);


CPXDEPRECATEDAPI(12040000)
CPXVOIDptr CPXPUBLIC
   CPXmalloc (size_t size);


CPXDEPRECATEDAPI(12040000)
CPXVOIDptr CPXPUBLIC
   CPXrealloc (void *ptr, size_t size);


CPXDEPRECATEDAPI(12040000)
CPXVOIDptr CPXPUBLIC
   CPXmemcpy (void *s1, void *s2, size_t n);


CPXDEPRECATEDAPI(12040000)
void CPXPUBLIC
   CPXfree (void *ptr);


CPXDEPRECATEDAPI(12040000)
size_t CPXPUBLIC
   CPXstrlen (const char *s_str);


CPXDEPRECATEDAPI(12040000)
CPXCHARptr CPXPUBLIC
   CPXstrcpy (char *dest_str, const char *src_str);


CPXLIBAPI
int CPXPUBLIC
   CPXputenv (char *envsetting_str);


CPXLIBAPI
int CPXPUBLIC
   CPXsetterminate (CPXENVptr env, volatile int *terminate_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetbhead (CPXCENVptr env, CPXCLPptr lp, int *head, double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXbinvcol (CPXCENVptr env, CPXCLPptr lp, int j, double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXbinvrow (CPXCENVptr env, CPXCLPptr lp, int i, double *y);


CPXLIBAPI
int CPXPUBLIC
   CPXbinvacol (CPXCENVptr env, CPXCLPptr lp, int j, double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXbinvarow (CPXCENVptr env, CPXCLPptr lp, int i, double *z);


CPXLIBAPI
int CPXPUBLIC
   CPXftran (CPXCENVptr env, CPXCLPptr lp, double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXbtran (CPXCENVptr env, CPXCLPptr lp, double *y);


CPXLIBAPI
int CPXPUBLIC
   CPXgetijrow (CPXCENVptr env, CPXCLPptr lp, int i, int j, int *row_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetray (CPXCENVptr env, CPXCLPptr lp, double *z);


CPXLIBAPI
int CPXPUBLIC
   CPXgetweight (CPXCENVptr env, CPXCLPptr lp, int rcnt,
                 const int *rmatbeg, const int *rmatind,
                 const double *rmatval, double *weight, int dpriind);


CPXLIBAPI
int CPXPUBLIC
   CPXmdleave (CPXCENVptr env, CPXLPptr lp, const int *indices,
               int cnt, double *downratio, double *upratio);


CPXLIBAPI
int CPXPUBLIC
   CPXstrongbranch (CPXCENVptr env, CPXLPptr lp, const int *indices,
                    int cnt, double *downobj, double *upobj, int itlim);


CPXLIBAPI
int CPXPUBLIC
   CPXdualfarkas (CPXCENVptr env, CPXCLPptr lp, double *y,
                  double *proof_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetobjoffset (CPXCENVptr env, CPXCLPptr lp, double *objoffset_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopypartialbase (CPXCENVptr env, CPXLPptr lp, int ccnt,
                       const int *cindices, const int *cstat, int rcnt,
                       const int *rindices, const int *rstat);


CPXLIBAPI
int CPXPUBLIC
   CPXgetbasednorms (CPXCENVptr env, CPXCLPptr lp, int *cstat,
                     int *rstat, double *dnorm);


CPXLIBAPI
int CPXPUBLIC
   CPXcopybasednorms (CPXCENVptr env, CPXLPptr lp, const int *cstat,
                      const int *rstat, const double *dnorm);


CPXLIBAPI
int CPXPUBLIC
   CPXgetdnorms (CPXCENVptr env, CPXCLPptr lp, double *norm, int *head,
                 int *len_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopydnorms (CPXCENVptr env, CPXLPptr lp, const double *norm,
                  const int *head, int len);


CPXLIBAPI
int CPXPUBLIC
   CPXkilldnorms (CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetpnorms (CPXCENVptr env, CPXCLPptr lp, double *cnorm,
                 double *rnorm, int *len_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopypnorms (CPXCENVptr env, CPXLPptr lp, const double *cnorm,
                  const double *rnorm, int len);


CPXLIBAPI
int CPXPUBLIC
   CPXkillpnorms (CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXpivotin (CPXCENVptr env, CPXLPptr lp, const int *rlist, int rlen);


CPXLIBAPI
int CPXPUBLIC
   CPXpivotout (CPXCENVptr env, CPXLPptr lp, const int *clist,
                int clen);


CPXLIBAPI
int CPXPUBLIC
   CPXchecksoln (CPXCENVptr env, CPXLPptr lp, int *lpstatus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXunscaleprob (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXtightenbds (CPXCENVptr env, CPXLPptr lp, int cnt,
                  const int *indices, const char *lu, const double *bd);


CPXLIBAPI
int CPXPUBLIC
   CPXpresolve (CPXCENVptr env, CPXLPptr lp, int method);


CPXLIBAPI
int CPXPUBLIC
   CPXbasicpresolve (CPXCENVptr env, CPXLPptr lp, double *redlb,
                     double *redub, int *rstat);


CPXLIBAPI
int CPXPUBLIC
   CPXslackfromx (CPXCENVptr env, CPXCLPptr lp, const double *x,
                  double *slack);


CPXLIBAPI
int CPXPUBLIC
   CPXdjfrompi (CPXCENVptr env, CPXCLPptr lp, const double *pi,
                double *dj);


CPXLIBAPI
int CPXPUBLIC
   CPXqpdjfrompi (CPXCENVptr env, CPXCLPptr lp, const double *pi,
                  const double *x, double *dj);


CPXLIBAPI
int CPXPUBLIC
   CPXfreepresolve (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetredlp (CPXCENVptr env, CPXCLPptr lp, CPXCLPptr *redlp_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcrushx (CPXCENVptr env, CPXCLPptr lp, const double *x,
              double *prex);


CPXLIBAPI
int CPXPUBLIC
   CPXuncrushx (CPXCENVptr env, CPXCLPptr lp, double *x,
                const double *prex);


CPXLIBAPI
int CPXPUBLIC
   CPXcrushpi (CPXCENVptr env, CPXCLPptr lp, const double *pi,
               double *prepi);


CPXLIBAPI
int CPXPUBLIC
   CPXuncrushpi (CPXCENVptr env, CPXCLPptr lp, double *pi,
                 const double *prepi);


CPXLIBAPI
int CPXPUBLIC
   CPXqpuncrushpi (CPXCENVptr env, CPXCLPptr lp, double *pi,
                   const double *prepi, const double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXcrushform (CPXCENVptr env, CPXCLPptr lp, int len, const int *ind,
                 const double *val, int *plen_p, double *poffset_p,
                 int *pind, double *pval);


CPXLIBAPI
int CPXPUBLIC
   CPXuncrushform (CPXCENVptr env, CPXCLPptr lp, int plen,
                   const int *pind, const double *pval, int *len_p,
                   double *offset_p, int *ind, double *val);


CPXLIBAPI
int CPXPUBLIC
   CPXgetprestat (CPXCENVptr env, CPXCLPptr lp, int *prestat_p,
                  int *pcstat, int *prstat, int *ocstat, int *orstat);


CPXLIBAPI
int CPXPUBLIC
   CPXcopyprotected (CPXCENVptr env, CPXLPptr lp, int cnt,
                     const int *indices);


CPXLIBAPI
int CPXPUBLIC
   CPXgetprotected (CPXCENVptr env, CPXCLPptr lp, int *cnt_p,
                    int *indices, int pspace, int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnumcores (CPXCENVptr env, int *numcores_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgettime (CPXCENVptr env, double *timestamp_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetdettime (CPXCENVptr env, double *dettimestamp_p);


CPXLIBAPI
int CPXPUBLIC
   CPXserializercreate (CPXSERIALIZERptr *ser_p);


CPXLIBAPI
void CPXPUBLIC
   CPXserializerdestroy (CPXSERIALIZERptr ser);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXserializerlength (CPXCSERIALIZERptr ser);


CPXLIBAPI
void const * CPXPUBLIC
   CPXserializerpayload (CPXCSERIALIZERptr ser);


CPXLIBAPI
int CPXPUBLIC
   CPXdeserializercreate (CPXDESERIALIZERptr *deser_p, CPXLONG size,
                          void const *buffer);


CPXLIBAPI
void CPXPUBLIC
   CPXdeserializerdestroy (CPXDESERIALIZERptr deser);


CPXLIBAPI
CPXLONG CPXPUBLIC
   CPXdeserializerleft (CPXCDESERIALIZERptr deser);


CPXLIBAPI
int CPXPUBLIC
   CPXlpwrite (CPXCENVptr env, CPXCLPptr lp, const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXlprewrite (CPXCENVptr env, CPXCLPptr lp,
                 const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXmpswrite (CPXCENVptr env, CPXCLPptr lp, const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXmpsrewrite (CPXCENVptr env, CPXCLPptr lp,
                  const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXsavwrite (CPXCENVptr env, CPXCLPptr lp, const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXsavwritedev (CPXCENVptr env, CPXCLPptr lp,
                   const char *filename_str, CPXIODEVICEptr dev);


CPXDEPRECATEDAPI(12040000)
int CPXPUBLIC
   CPXreadcopyvec (CPXCENVptr env, CPXLPptr lp,
                   const char *filename_str);


CPXDEPRECATEDAPI(12040000)
int CPXPUBLIC
   CPXvecwrite (CPXCENVptr env, CPXCLPptr lp, const char *filename_str);


CPXDEPRECATEDAPI(12040000)
int CPXPUBLIC
   CPXtxtsolwrite (CPXCENVptr env, CPXCLPptr lp,
                   const char *filename_str);


CPXDEPRECATEDAPI(12040000)
int CPXPUBLIC
   CPXgetiis (CPXCENVptr env, CPXCLPptr lp, int *iisstat_p,
              int *rowind, int *rowbdstat, int *iisnumrows_p,
              int *colind, int *colbdstat, int *iisnumcols_p);


CPXDEPRECATEDAPI(12040000)
int CPXPUBLIC
   CPXfindiis (CPXCENVptr env, CPXLPptr lp, int *iisnumrows_p,
               int *iisnumcols_p);


CPXLIBAPI
void CPXPUBLIC
   CPXinitialize (void);


CPXLIBAPI
void CPXPUBLIC
   CPXfinalize (void);



#ifdef __cplusplus
}
#endif

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif /* !CPX_H */
/* --------------------------------------------------------------------------
 * File: bar.h
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

#ifndef CPXBAR_H
#   define CPXBAR_H 1
#include "cpxconst.h"

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#ifdef __cplusplus
extern "C" {
#endif

CPXLIBAPI
int CPXPUBLIC
   CPXhybbaropt (CPXCENVptr env, CPXLPptr lp, int method);


CPXLIBAPI
int CPXPUBLIC
   CPXbaropt (CPXCENVptr env, CPXLPptr lp);



#ifdef __cplusplus
}
#endif

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif /* !CPXBAR_H */
/* --------------------------------------------------------------------------
 * File: mip.h
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

#ifndef CPXMIP_H
#   define CPXMIP_H 1

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#ifdef __cplusplus
extern "C" {
#endif

#define CPXgetmipobjval(env,lp,objval_p) CPXgetobjval(env, lp, objval_p)

#define CPXgetmipx(env,lp,x,begin,end) CPXgetx(env, lp, x, begin, end)

#define CPXgetmipslack(env,lp,slack,begin,end) CPXgetslack(env, lp, slack, begin, end)

#define CPXgetmipqconstrslack(env,lp,qcslack,begin,end) CPXgetqconstrslack(env, lp, qcslack, begin, end)

CPXLIBAPI
int CPXPUBLIC
   CPXcopyctype (CPXCENVptr env, CPXLPptr lp, const char *xctype);


CPXLIBAPI
int CPXPUBLIC
   CPXcopyorder (CPXCENVptr env, CPXLPptr lp, int cnt,
                 const int *indices, const int *priority,
                 const int *direction);


CPXLIBAPI
int CPXPUBLIC
   CPXcopysos (CPXCENVptr env, CPXLPptr lp, int numsos, int numsosnz,
               const char *sostype, const int *sosbeg,
               const int *sosind, const double *soswt, char **sosname);


CPXDEPRECATEDAPI(12040000)
int CPXPUBLIC
   CPXcopymipstart (CPXCENVptr env, CPXLPptr lp, int cnt,
                    const int *indices, const double *values);


CPXLIBAPI
int CPXPUBLIC
   CPXchgmipstarts (CPXCENVptr env, CPXLPptr lp, int mcnt,
                    const int *mipstartindices, int nzcnt,
                    const int *beg, const int *varindices,
                    const double *values, const int *effortlevel);


CPXLIBAPI
int CPXPUBLIC
   CPXaddmipstarts (CPXCENVptr env, CPXLPptr lp, int mcnt, int nzcnt,
                    const int *beg, const int *varindices,
                    const double *values, const int *effortlevel,
                    char **mipstartname);


CPXLIBAPI
int CPXPUBLIC
   CPXdelmipstarts (CPXCENVptr env, CPXLPptr lp, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsetmipstarts (CPXCENVptr env, CPXLPptr lp, int *delstat);


CPXLIBAPI
int CPXPUBLIC
   CPXrefinemipstartconflict (CPXCENVptr env, CPXLPptr lp,
                              int mipstartindex, int *confnumrows_p,
                              int *confnumcols_p);


CPXLIBAPI
int CPXPUBLIC
   CPXrefinemipstartconflictext (CPXCENVptr env, CPXLPptr lp,
                                 int mipstartindex, int grpcnt,
                                 int concnt, const double *grppref,
                                 const int *grpbeg, const int *grpind,
                                 const char *grptype);


CPXLIBAPI
int CPXPUBLIC
   CPXmipopt (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetmipitcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetbestobjval (CPXCENVptr env, CPXCLPptr lp, double *objval_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetmiprelgap (CPXCENVptr env, CPXCLPptr lp, double *gap_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcutoff (CPXCENVptr env, CPXCLPptr lp, double *cutoff_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnodecnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnodeleftcnt (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnodeint (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnumcuts (CPXCENVptr env, CPXCLPptr lp, int cuttype,
                  int *num_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnummipstarts (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetmipstarts (CPXCENVptr env, CPXCLPptr lp, int *nzcnt_p,
                    int *beg, int *varindices, double *values,
                    int *effortlevel, int startspace, int *surplus_p,
                    int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetmipstartname (CPXCENVptr env, CPXCLPptr lp, char  **name,
                       char *store, int storesz, int *surplus_p,
                       int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetmipstartindex (CPXCENVptr env, CPXCLPptr lp,
                        const char *lname_str, int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsubstat (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsubmethod (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXchgctype (CPXCENVptr env, CPXLPptr lp, int cnt,
                const int *indices, const char *xctype);


CPXLIBAPI
int CPXPUBLIC
   CPXaddsos (CPXCENVptr env, CPXLPptr lp, int numsos, int numsosnz,
              const char *sostype, const int *sosbeg,
              const int *sosind, const double *soswt, char **sosname);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsos (CPXCENVptr env, CPXLPptr lp, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsetsos (CPXCENVptr env, CPXLPptr lp, int *delset);


CPXLIBAPI
int CPXPUBLIC
   CPXgetctype (CPXCENVptr env, CPXCLPptr lp, char *xctype, int begin,
                int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnumsos (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsos (CPXCENVptr env, CPXCLPptr lp, int *numsosnz_p,
              char *sostype, int *sosbeg, int *sosind, double *soswt,
              int sosspace, int *surplus_p, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsosname (CPXCENVptr env, CPXCLPptr lp, char  **name,
                  char *namestore, int storespace, int *surplus_p,
                  int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsosindex (CPXCENVptr env, CPXCLPptr lp, const char *lname_str,
                   int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsosinfeas (CPXCENVptr env, CPXCLPptr lp, const double *x,
                    double *infeasout, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnumindconstrs (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetindconstr (CPXCENVptr env, CPXCLPptr lp, int *indvar_p,
                    int *complemented_p, int *nzcnt_p, double *rhs_p,
                    char *sense_p, int *linind, double *linval,
                    int space, int *surplus_p, int which);


CPXLIBAPI
int CPXPUBLIC
   CPXgetindconstrindex (CPXCENVptr env, CPXCLPptr lp,
                         const char *lname_str, int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetindconstrname (CPXCENVptr env, CPXCLPptr lp, char *buf_str,
                        int bufspace, int *surplus_p, int which);


CPXLIBAPI
int CPXPUBLIC
   CPXgetindconstrslack (CPXCENVptr env, CPXCLPptr lp,
                         double *indslack, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXindconstrslackfromx (CPXCENVptr env, CPXCLPptr lp,
                           const double *x, double *indslack);


CPXLIBAPI
int CPXPUBLIC
   CPXgetindconstrinfeas (CPXCENVptr env, CPXCLPptr lp,
                          const double *x, double *infeasout,
                          int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXdelindconstrs (CPXCENVptr env, CPXLPptr lp, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnumint (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnumbin (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnumsemicont (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnumsemiint (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetorder (CPXCENVptr env, CPXCLPptr lp, int *cnt_p, int *indices,
                int *priority, int *direction, int ordspace,
                int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXpopulate (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolnumfilters (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXaddsolnpooldivfilter (CPXCENVptr env, CPXLPptr lp,
                            double lower_bound, double upper_bound,
                            int nzcnt, const int *ind,
                            const double *weight, const double *refval,
                            const char *lname_str);


CPXLIBAPI
int CPXPUBLIC
   CPXaddsolnpoolrngfilter (CPXCENVptr env, CPXLPptr lp, double lb,
                            double ub, int nzcnt, const int *ind,
                            const double *val, const char *lname_str);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolfiltertype (CPXCENVptr env, CPXCLPptr lp,
                             int *ftype_p, int which);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpooldivfilter (CPXCENVptr env, CPXCLPptr lp,
                            double *lower_cutoff_p,
                            double *upper_cutoff_p, int *nzcnt_p,
                            int *ind, double *val, double *refval,
                            int space, int *surplus_p, int which);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolfilter (CPXCENVptr env, CPXCLPptr lp, int *ftype_p,
                         double *lowercutoff_p, double *upper_cutoff_p,
                         int *nzcnt_p, int *ind, double *val,
                         double *refval, int space, int *surplus_p,
                         int which);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolrngfilter (CPXCENVptr env, CPXCLPptr lp, double *lb_p,
                            double *ub_p, int *nzcnt_p, int *ind,
                            double *val, int space, int *surplus_p,
                            int which);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolfiltername (CPXCENVptr env, CPXCLPptr lp,
                             char *buf_str, int bufspace,
                             int *surplus_p, int which);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolfilterindex (CPXCENVptr env, CPXCLPptr lp,
                              const char *lname_str, int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsolnpoolfilters (CPXCENVptr env, CPXLPptr lp, int begin,
                          int end);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsetsolnpoolfilters (CPXCENVptr env, CPXLPptr lp, int *delstat);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolnumsolns (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolnumreplaced (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolmeanobjval (CPXCENVptr env, CPXCLPptr lp,
                             double *meanobjval_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolobjval (CPXCENVptr env, CPXCLPptr lp, int soln,
                         double *objval_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolx (CPXCENVptr env, CPXCLPptr lp, int soln, double *x,
                    int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolslack (CPXCENVptr env, CPXCLPptr lp, int soln,
                        double *slack, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolqconstrslack (CPXCENVptr env, CPXCLPptr lp, int soln,
                               double *qcslack, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolsolnname (CPXCENVptr env, CPXCLPptr lp, char *store,
                           int storesz, int *surplus_p, int which);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolsolnindex (CPXCENVptr env, CPXCLPptr lp,
                            const char *lname_str, int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsolnpoolsolns (CPXCENVptr env, CPXLPptr lp, int begin,
                        int end);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsetsolnpoolsolns (CPXCENVptr env, CPXLPptr lp, int *delstat);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopyorder (CPXCENVptr env, CPXLPptr lp,
                     const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopysolnpoolfilters (CPXCENVptr env, CPXLPptr lp,
                               const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopymipstarts (CPXCENVptr env, CPXLPptr lp,
                         const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXordwrite (CPXCENVptr env, CPXCLPptr lp, const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXwritemipstarts (CPXCENVptr env, CPXCLPptr lp,
                      const char *filename_str, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXfltwrite (CPXCENVptr env, CPXCLPptr lp, const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXsetinfocallbackfunc (CPXENVptr env,
                           int(CPXPUBLIC *callback)(CPXCENVptr, void *, int, void *),
                           void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXgetinfocallbackfunc (CPXCENVptr env,
                           int(CPXPUBLIC **callback_p)(CPXCENVptr, void *, int, void *),
                           void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsetmipcallbackfunc (CPXENVptr env,
                          int(CPXPUBLIC *callback)(CPXCENVptr, void *, int, void *),
                          void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXgetmipcallbackfunc (CPXCENVptr env,
                          int(CPXPUBLIC **callback_p)(CPXCENVptr, void *, int, void *),
                          void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsetbranchcallbackfunc (CPXENVptr env,
                             int(CPXPUBLIC *branchcallback)(CALLBACK_BRANCH_ARGS),
                             void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXsetbranchnosolncallbackfunc (CPXENVptr env,
                                   int(CPXPUBLIC *branchnosolncallback)(CALLBACK_BRANCH_ARGS),
                                   void *cbhandle);


CPXDEPRECATEDAPI(12040000)
int CPXPUBLIC
   CPXsetcutcallbackfunc (CPXENVptr env,
                          int(CPXPUBLIC *cutcallback)(CALLBACK_CUT_ARGS),
                          void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXsetlazyconstraintcallbackfunc (CPXENVptr env,
                                     int(CPXPUBLIC *lazyconcallback)(CALLBACK_CUT_ARGS),
                                     void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXsetusercutcallbackfunc (CPXENVptr env,
                              int(CPXPUBLIC *cutcallback)(CALLBACK_CUT_ARGS),
                              void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXsetnodecallbackfunc (CPXENVptr env,
                           int(CPXPUBLIC *nodecallback)(CALLBACK_NODE_ARGS),
                           void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXsetheuristiccallbackfunc (CPXENVptr env,
                                int(CPXPUBLIC *heuristiccallback)(CALLBACK_HEURISTIC_ARGS),
                                void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXsetincumbentcallbackfunc (CPXENVptr env,
                                int(CPXPUBLIC *incumbentcallback)(CALLBACK_INCUMBENT_ARGS),
                                void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXsetsolvecallbackfunc (CPXENVptr env,
                            int(CPXPUBLIC *solvecallback)(CALLBACK_SOLVE_ARGS),
                            void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXsetdeletenodecallbackfunc (CPXENVptr env,
                                 void(CPXPUBLIC *deletecallback)(CALLBACK_DELETENODE_ARGS),
                                 void *cbhandle);


CPXLIBAPI
int CPXPUBLIC
   CPXgetbranchcallbackfunc (CPXCENVptr env,
                             int(CPXPUBLIC **branchcallback_p)(CALLBACK_BRANCH_ARGS),
                             void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetbranchnosolncallbackfunc (CPXCENVptr env,
                                   int(CPXPUBLIC **branchnosolncallback_p)(CALLBACK_BRANCH_ARGS),
                                   void  **cbhandle_p);


CPXDEPRECATEDAPI(12040000)
int CPXPUBLIC
   CPXgetcutcallbackfunc (CPXCENVptr env,
                          int(CPXPUBLIC **cutcallback_p)(CALLBACK_CUT_ARGS),
                          void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetlazyconstraintcallbackfunc (CPXCENVptr env,
                                     int(CPXPUBLIC **cutcallback_p)(CALLBACK_CUT_ARGS),
                                     void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetusercutcallbackfunc (CPXCENVptr env,
                              int(CPXPUBLIC **cutcallback_p)(CALLBACK_CUT_ARGS),
                              void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnodecallbackfunc (CPXCENVptr env,
                           int(CPXPUBLIC **nodecallback_p)(CALLBACK_NODE_ARGS),
                           void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetheuristiccallbackfunc (CPXCENVptr env,
                                int(CPXPUBLIC **heuristiccallback_p)(CALLBACK_HEURISTIC_ARGS),
                                void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetincumbentcallbackfunc (CPXCENVptr env,
                                int(CPXPUBLIC **incumbentcallback_p)(CALLBACK_INCUMBENT_ARGS),
                                void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolvecallbackfunc (CPXCENVptr env,
                            int(CPXPUBLIC **solvecallback_p)(CALLBACK_SOLVE_ARGS),
                            void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetdeletenodecallbackfunc (CPXCENVptr env,
                                 void(CPXPUBLIC **deletecallback_p)(CALLBACK_DELETENODE_ARGS),
                                 void  **cbhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcallbacknodelp (CPXCENVptr env, void *cbdata, int wherefrom,
                         CPXLPptr *nodelp_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcallbacknodeinfo (CPXCENVptr env, void *cbdata, int wherefrom,
                           int nodeindex, int whichinfo,
                           void *result_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcallbackseqinfo (CPXCENVptr env, void *cbdata, int wherefrom,
                          int seqid, int whichinfo, void *result_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcallbacksosinfo (CPXCENVptr env, void *cbdata, int wherefrom,
                          int sosindex, int member, int whichinfo,
                          void *result_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcallbackindicatorinfo (CPXCENVptr env, void *cbdata,
                                int wherefrom, int iindex,
                                int whichinfo, void *result_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcutcallbackadd (CPXCENVptr env, void *cbdata, int wherefrom,
                      int nzcnt, double rhs, int sense,
                      const int *cutind, const double *cutval,
                      int purgeable);


CPXLIBAPI
int CPXPUBLIC
   CPXcutcallbackaddlocal (CPXCENVptr env, void *cbdata, int wherefrom,
                           int nzcnt, double rhs, int sense,
                           const int *cutind, const double *cutval);


CPXLIBAPI
int CPXPUBLIC
   CPXbranchcallbackbranchasCPLEX (CPXCENVptr env, void *cbdata,
                                   int wherefrom, int num,
                                   void *userhandle, int *seqnum_p);


CPXLIBAPI
int CPXPUBLIC
   CPXbranchcallbackbranchbds (CPXCENVptr env, void *cbdata,
                               int wherefrom, double nodeest, int cnt,
                               const int *indices, const char *lu,
                               const int *bd, void *userhandle,
                               int *seqnum_p);


CPXLIBAPI
int CPXPUBLIC
   CPXbranchcallbackbranchgeneral (CPXCENVptr env, void *cbdata,
                                   int wherefrom, double nodeest,
                                   int varcnt, const int *varind,
                                   const char *varlu, const int *varbd,
                                   int rcnt, int nzcnt,
                                   const double *rhs,
                                   const char *sense,
                                   const int *rmatbeg,
                                   const int *rmatind,
                                   const double *rmatval,
                                   void *userhandle, int *seqnum_p);


CPXLIBAPI
int CPXPUBLIC
   CPXbranchcallbackbranchconstraints (CPXCENVptr env, void *cbdata,
                                       int wherefrom, double nodeest,
                                       int rcnt, int nzcnt,
                                       const double *rhs,
                                       const char *sense,
                                       const int *rmatbeg,
                                       const int *rmatind,
                                       const double *rmatval,
                                       void *userhandle, int *seqnum_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcallbacknodex (CPXCENVptr env, void *cbdata, int wherefrom,
                        double *x, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcallbacknodeobjval (CPXCENVptr env, void *cbdata,
                             int wherefrom, double *objval_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcallbackctype (CPXCENVptr env, void *cbdata, int wherefrom,
                        char *xctype, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcallbackorder (CPXCENVptr env, void *cbdata, int wherefrom,
                        int *priority, int *direction, int begin,
                        int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcallbackpseudocosts (CPXCENVptr env, void *cbdata,
                              int wherefrom, double *uppc,
                              double *downpc, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcallbackincumbent (CPXCENVptr env, void *cbdata,
                            int wherefrom, double *x, int begin,
                            int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcallbacknodeintfeas (CPXCENVptr env, void *cbdata,
                              int wherefrom, int *feas, int begin,
                              int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcallbackgloballb (CPXCENVptr env, void *cbdata, int wherefrom,
                           double *lb, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcallbackglobalub (CPXCENVptr env, void *cbdata, int wherefrom,
                           double *ub, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcallbacknodelb (CPXCENVptr env, void *cbdata, int wherefrom,
                         double *lb, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcallbacknodeub (CPXCENVptr env, void *cbdata, int wherefrom,
                         double *ub, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcallbacklp (CPXCENVptr env, void *cbdata, int wherefrom,
                     CPXCLPptr *lp_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcallbacksetuserhandle (CPXCENVptr env, void *cbdata,
                             int wherefrom, void *userhandle,
                             void  **olduserhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcallbacksetnodeuserhandle (CPXCENVptr env, void *cbdata,
                                 int wherefrom, int nodeindex,
                                 void *userhandle,
                                 void  **olduserhandle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcallbacknodestat (CPXCENVptr env, void *cbdata, int wherefrom,
                           int *nodestat_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcallbackbranchconstraints (CPXCENVptr env, void *cbdata,
                                    int wherefrom, int which,
                                    int *cuts_p, int *nzcnt_p,
                                    double *rhs, char *sense,
                                    int *rmatbeg, int *rmatind,
                                    double *rmatval, int rmatsz,
                                    int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXaddusercuts (CPXCENVptr env, CPXLPptr lp, int rcnt, int nzcnt,
                   const double *rhs, const char *sense,
                   const int *rmatbeg, const int *rmatind,
                   const double *rmatval, char **rowname);


CPXLIBAPI
int CPXPUBLIC
   CPXaddlazyconstraints (CPXCENVptr env, CPXLPptr lp, int rcnt,
                          int nzcnt, const double *rhs,
                          const char *sense, const int *rmatbeg,
                          const int *rmatind, const double *rmatval,
                          char **rowname);


CPXLIBAPI
int CPXPUBLIC
   CPXfreeusercuts (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXfreelazyconstraints (CPXCENVptr env, CPXLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXordread (CPXCENVptr env, const char *filename_str, int numcols,
               char **colname, int *cnt_p, int *indices, int *priority,
               int *direction);



#ifdef __cplusplus
}
#endif

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif /* !CPXMIP_H */
/* --------------------------------------------------------------------------
 * File: gc.h
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

#ifndef CPXGC_H
#   define CPXGC_H 1

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#ifdef __cplusplus
extern "C" {
#endif

CPXLIBAPI
int CPXPUBLIC
   CPXaddindconstr (CPXCENVptr env, CPXLPptr lp, int indvar,
                    int complemented, int nzcnt, double rhs, int sense,
                    const int *linind, const double *linval,
                    const char *indname_str);



#ifdef __cplusplus
}
#endif

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif /* !CPXGC_H */
/* --------------------------------------------------------------------------
 * File: net.h
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

#ifndef CPXNET_H
#   define CPXNET_H 1

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#ifdef __cplusplus
extern "C" {
#endif

CPXLIBAPI
CPXNETptr CPXPUBLIC
   CPXNETcreateprob (CPXENVptr env, int *status_p,
                     const char *name_str);


CPXLIBAPI
int CPXPUBLIC
   CPXNETfreeprob (CPXENVptr env, CPXNETptr *net_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETcopynet (CPXCENVptr env, CPXNETptr net, int objsen,
                  int nnodes, const double *supply, char **nnames,
                  int narcs, const int *fromnode, const int *tonode,
                  const double *low, const double *up,
                  const double *obj, char **anames);


CPXLIBAPI
int CPXPUBLIC
   CPXNETcopybase (CPXCENVptr env, CPXNETptr net, const int *astat,
                   const int *nstat);


CPXLIBAPI
int CPXPUBLIC
   CPXNETaddnodes (CPXCENVptr env, CPXNETptr net, int nnodes,
                   const double *supply, char **name);


CPXLIBAPI
int CPXPUBLIC
   CPXNETaddarcs (CPXCENVptr env, CPXNETptr net, int narcs,
                  const int *fromnode, const int *tonode,
                  const double *low, const double *up,
                  const double *obj, char **anames);


CPXLIBAPI
int CPXPUBLIC
   CPXNETdelnodes (CPXCENVptr env, CPXNETptr net, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXNETdelarcs (CPXCENVptr env, CPXNETptr net, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXNETdelset (CPXCENVptr env, CPXNETptr net, int *whichnodes,
                 int *whicharcs);


CPXLIBAPI
int CPXPUBLIC
   CPXNETprimopt (CPXCENVptr env, CPXNETptr net);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetstat (CPXCENVptr env, CPXCNETptr net);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetobjval (CPXCENVptr env, CPXCNETptr net, double *objval_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetx (CPXCENVptr env, CPXCNETptr net, double *x, int begin,
               int end);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetpi (CPXCENVptr env, CPXCNETptr net, double *pi, int begin,
                int end);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetslack (CPXCENVptr env, CPXCNETptr net, double *slack,
                   int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetdj (CPXCENVptr env, CPXCNETptr net, double *dj, int begin,
                int end);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetitcnt (CPXCENVptr env, CPXCNETptr net);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetphase1cnt (CPXCENVptr env, CPXCNETptr net);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetbase (CPXCENVptr env, CPXCNETptr net, int *astat,
                  int *nstat);


CPXLIBAPI
int CPXPUBLIC
   CPXNETsolution (CPXCENVptr env, CPXCNETptr net, int *netstat_p,
                   double *objval_p, double *x, double *pi,
                   double *slack, double *dj);


CPXLIBAPI
int CPXPUBLIC
   CPXNETsolninfo (CPXCENVptr env, CPXCNETptr net, int *pfeasind_p,
                   int *dfeasind_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgname (CPXCENVptr env, CPXNETptr net, int key, int vindex,
                  const char *name_str);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgarcname (CPXCENVptr env, CPXNETptr net, int cnt,
                     const int *indices, char **newname);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgnodename (CPXCENVptr env, CPXNETptr net, int cnt,
                      const int *indices, char **newname);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgobjsen (CPXCENVptr env, CPXNETptr net, int maxormin);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgbds (CPXCENVptr env, CPXNETptr net, int cnt,
                 const int *indices, const char *lu, const double *bd);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgarcnodes (CPXCENVptr env, CPXNETptr net, int cnt,
                      const int *indices, const int *fromnode,
                      const int *tonode);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgobj (CPXCENVptr env, CPXNETptr net, int cnt,
                 const int *indices, const double *obj);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgsupply (CPXCENVptr env, CPXNETptr net, int cnt,
                    const int *indices, const double *supply);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetobjsen (CPXCENVptr env, CPXCNETptr net);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetsupply (CPXCENVptr env, CPXCNETptr net, double *supply,
                    int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetprobname (CPXCENVptr env, CPXCNETptr net, char *buf_str,
                      int bufspace, int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetnodename (CPXCENVptr env, CPXCNETptr net, char  **nnames,
                      char *namestore, int namespc, int *surplus_p,
                      int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetarcname (CPXCENVptr env, CPXCNETptr net, char  **nnames,
                     char *namestore, int namespc, int *surplus_p,
                     int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetlb (CPXCENVptr env, CPXCNETptr net, double *low, int begin,
                int end);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetub (CPXCENVptr env, CPXCNETptr net, double *up, int begin,
                int end);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetobj (CPXCENVptr env, CPXCNETptr net, double *obj,
                 int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetarcnodes (CPXCENVptr env, CPXCNETptr net, int *fromnode,
                      int *tonode, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetnodearcs (CPXCENVptr env, CPXCNETptr net, int *arccnt_p,
                      int *arcbeg, int *arc, int arcspace,
                      int *surplus_p, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetnumnodes (CPXCENVptr env, CPXCNETptr net);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetnumarcs (CPXCENVptr env, CPXCNETptr net);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetnodeindex (CPXCENVptr env, CPXCNETptr net,
                       const char *lname_str, int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetarcindex (CPXCENVptr env, CPXCNETptr net,
                      const char *lname_str, int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETreadcopyprob (CPXCENVptr env, CPXNETptr net,
                       const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXNETreadcopybase (CPXCENVptr env, CPXNETptr net,
                       const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXNETwriteprob (CPXCENVptr env, CPXCNETptr net,
                    const char *filename_str, const char *format_str);


CPXLIBAPI
int CPXPUBLIC
   CPXNETbasewrite (CPXCENVptr env, CPXCNETptr net,
                    const char *filename_str);



#ifdef __cplusplus
}
#endif

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif /* !CPXNET_H */
/* --------------------------------------------------------------------------
 * File: qp.h
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

#ifndef CPXQP_H
#   define CPXQP_H 1

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#ifdef __cplusplus
extern "C" {
#endif

CPXLIBAPI
int CPXPUBLIC
   CPXcopyquad (CPXCENVptr env, CPXLPptr lp, const int *qmatbeg,
                const int *qmatcnt, const int *qmatind,
                const double *qmatval);


CPXLIBAPI
int CPXPUBLIC
   CPXcopyqpsep (CPXCENVptr env, CPXLPptr lp, const double *qsepvec);


CPXLIBAPI
int CPXPUBLIC
   CPXchgqpcoef (CPXCENVptr env, CPXLPptr lp, int i, int j,
                 double newvalue);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnumqpnz (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnumquad (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqpcoef (CPXCENVptr env, CPXCLPptr lp, int rownum, int colnum,
                 double *coef_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetquad (CPXCENVptr env, CPXCLPptr lp, int *nzcnt_p,
               int *qmatbeg, int *qmatind, double *qmatval,
               int qmatspace, int *surplus_p, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXqpindefcertificate (CPXCENVptr env, CPXCLPptr lp, double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXqpopt (CPXCENVptr env, CPXLPptr lp);



#ifdef __cplusplus
}
#endif

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif /* !CPXQP_H */
/* --------------------------------------------------------------------------
 * File: socp.h
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

#ifndef CPXSOCP_H
#   define CPXSOCP_H 1

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#ifdef __cplusplus
extern "C" {
#endif

CPXLIBAPI
int CPXPUBLIC
   CPXaddqconstr (CPXCENVptr env, CPXLPptr lp, int linnzcnt,
                  int quadnzcnt, double rhs, int sense,
                  const int *linind, const double *linval,
                  const int *quadrow, const int *quadcol,
                  const double *quadval, const char *lname_str);


CPXLIBAPI
int CPXPUBLIC
   CPXdelqconstrs (CPXCENVptr env, CPXLPptr lp, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnumqconstrs (CPXCENVptr env, CPXCLPptr lp);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqconstrindex (CPXCENVptr env, CPXCLPptr lp,
                       const char *lname_str, int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqconstr (CPXCENVptr env, CPXCLPptr lp, int *linnzcnt_p,
                  int *quadnzcnt_p, double *rhs_p, char *sense_p,
                  int *linind, double *linval, int linspace,
                  int *linsurplus_p, int *quadrow, int *quadcol,
                  double *quadval, int quadspace, int *quadsurplus_p,
                  int which);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqconstrname (CPXCENVptr env, CPXCLPptr lp, char *buf_str,
                      int bufspace, int *surplus_p, int which);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqconstrslack (CPXCENVptr env, CPXCLPptr lp, double *qcslack,
                       int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXqconstrslackfromx (CPXCENVptr env, CPXCLPptr lp, const double *x,
                         double *qcslack);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqconstrinfeas (CPXCENVptr env, CPXCLPptr lp, const double *x,
                        double *infeasout, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetxqxax (CPXCENVptr env, CPXCLPptr lp, double *xqxax, int begin,
                int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqconstrdslack (CPXCENVptr env, CPXCLPptr lp, int qind,
                        int *nz_p, int *ind, double *val, int space,
                        int *surplus_p);



#ifdef __cplusplus
}
#endif

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif /* !CPXSOCP_H */
