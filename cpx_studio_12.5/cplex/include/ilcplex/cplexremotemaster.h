/* --------------------------------------------------------------------------
 * File: cplexremotemaster.h
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

#ifndef CPX_CPLEX_REMOTEMASTER_H
#   define CPX_CPLEX_REMOTEMASTER_H 1
#include "ilcplex/cplexremote.h"

#ifdef _WIN32
#pragma pack(push, 8)
#endif

#ifdef __cplusplus
extern "C" {
#endif

CPXLIBAPI
CPXENVptr CPXPUBLIC
   CPXopenCPLEXremote (char const *transport, int argc,
                       char const *const *argv, int *status_p);


CPXLIBAPI
int CPXPUBLIC
   CPXuserfunction (CPXENVptr env, int id, CPXLONG insize,
                    void const *indata, CPXLONG outspace,
                    CPXLONG *outsize_p, void *outdata);


CPXLIBAPI
int CPXPUBLIC
   CPXcreateenvgroup (CPXENVGROUPptr *group_p, int nenvs,
                      CPXENVptr const *envs);


CPXLIBAPI
void CPXPUBLIC
   CPXfreeenvgroup (CPXENVGROUPptr *group_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetenvgroupsize (CPXCENVGROUPptr group);


CPXLIBAPI
CPXENVptr CPXPUBLIC
   CPXgetenvgroupenv (CPXCENVGROUPptr group, int idx);


CPXLIBAPI
int CPXPUBLIC
   CPXsetinfohandler (CPXENVptr env, CPXINFOHANDLER *infohandler,
                      void *handle);


CPXLIBAPI
int CPXPUBLIC
   CPXgetinfohandler (CPXCENVptr env, CPXINFOHANDLER  **infohandler_p,
                      void  **handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetreconnectinfolen (CPXCENVptr env);


CPXLIBAPI
int CPXPUBLIC
   CPXcloseCPLEXdisconnect (CPXENVptr *env_p, void *buffer);


CPXLIBAPI
CPXENVptr CPXPUBLIC
   CPXopenCPLEXreconnect (int *status_p, void const *buffer);


CPXLIBAPI
int CPXPUBLIC
   CPXgetlps (CPXCENVptr env, int capacity, CPXLPptr *lps,
              int *count_p, int *total_p);


CPXLIBAPI
int CPXPUBLIC
   CPXasynctest (CPXASYNCptr asynchandle, int *running_p);


CPXLIBAPI
int CPXPUBLIC
   CPXasynckill (CPXASYNCptr asynchandle);


CPXLIBAPI
int CPXPUBLIC
   CPXgetasynchandlesize (CPXCASYNCptr asynchandle);


CPXLIBAPI
void CPXPUBLIC
   CPXasynchandlesave (CPXCASYNCptr asynchandle, void *buffer);


CPXLIBAPI
int CPXPUBLIC
   CPXasynchandleload (CPXENVptr env, CPXASYNCptr *asynchandle_p,
                       void const *buffer);


CPXLIBAPI
void CPXPUBLIC
   CPXasynchandledel (CPXASYNCptr *asynchandle_p);













CPXLIBAPI
int CPXPUBLIC
   CPXhybbaropt_async (CPXCENVptr env, CPXLPptr lp, int method,
                       CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXhybbaropt_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXhybbaropt_multicast (CPXENVGROUPptr group, int method);


CPXLIBAPI
int CPXPUBLIC
   CPXbaropt_async (CPXCENVptr env, CPXLPptr lp, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXbaropt_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXbaropt_multicast (CPXENVGROUPptr group);


CPXLIBAPI
int CPXPUBLIC
   CPXcopylpwnames_async (CPXCENVptr env, CPXLPptr lp, int numcols,
                          int numrows, int objsense,
                          const double *objective, const double *rhs,
                          const char *sense, const int *matbeg,
                          const int *matcnt, const int *matind,
                          const double *matval, const double *lb,
                          const double *ub, const double *rngval,
                          char **colname, char **rowname,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopylpwnames_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopylp_async (CPXCENVptr env, CPXLPptr lp, int numcols,
                    int numrows, int objsense, const double *objective,
                    const double *rhs, const char *sense,
                    const int *matbeg, const int *matcnt,
                    const int *matind, const double *matval,
                    const double *lb, const double *ub,
                    const double *rngval, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopylp_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopylp_multicast (CPXENVGROUPptr group, int numcols, int numrows,
                        int objsense, const double *objective,
                        const double *rhs, const char *sense,
                        const int *matbeg, const int *matcnt,
                        const int *matind, const double *matval,
                        const double *lb, const double *ub,
                        const double *rngval);


CPXLIBAPI
int CPXPUBLIC
   CPXcopyobjname_async (CPXCENVptr env, CPXLPptr lp,
                         const char *objname_str,
                         CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopyobjname_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopybase_async (CPXCENVptr env, CPXLPptr lp, const int *cstat,
                      const int *rstat, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopybase_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcleanup_async (CPXCENVptr env, CPXLPptr lp, double eps,
                     CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcleanup_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcleanup_multicast (CPXENVGROUPptr group, double eps);


CPXLIBAPI
int CPXPUBLIC
   CPXcopystart_async (CPXCENVptr env, CPXLPptr lp, const int *cstat,
                       const int *rstat, const double *cprim,
                       const double *rprim, const double *cdual,
                       const double *rdual, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopystart_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopynettolp_async (CPXCENVptr env, CPXLPptr lp, CPXCNETptr net,
                         CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopynettolp_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopynettolp_multicast (CPXENVGROUPptr group);


CPXLIBAPI
int CPXPUBLIC
   CPXNETextract_async (CPXCENVptr env, CPXNETptr net, CPXCLPptr lp,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETextract_join (CPXASYNCptr *handle_p, int *colmap, int *rowmap);


CPXLIBAPI
int CPXPUBLIC
   CPXlpopt_async (CPXCENVptr env, CPXLPptr lp, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXlpopt_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXlpopt_multicast (CPXENVGROUPptr group);


CPXLIBAPI
int CPXPUBLIC
   CPXprimopt_async (CPXCENVptr env, CPXLPptr lp,
                     CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXprimopt_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXprimopt_multicast (CPXENVGROUPptr group);


CPXLIBAPI
int CPXPUBLIC
   CPXdualopt_async (CPXCENVptr env, CPXLPptr lp,
                     CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdualopt_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdualopt_multicast (CPXENVGROUPptr group);


CPXLIBAPI
int CPXPUBLIC
   CPXhybnetopt_async (CPXCENVptr env, CPXLPptr lp, int method,
                       CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXhybnetopt_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXhybnetopt_multicast (CPXENVGROUPptr group, int method);


CPXLIBAPI
int CPXPUBLIC
   CPXsiftopt_async (CPXCENVptr env, CPXLPptr lp,
                     CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsiftopt_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsiftopt_multicast (CPXENVGROUPptr group);


CPXLIBAPI
int CPXPUBLIC
   CPXpratio_async (CPXCENVptr env, CPXLPptr lp, int *indices, int cnt,
                    CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXpratio_join (CPXASYNCptr *handle_p, int *indices,
                   double *downratio, double *upratio, int *downleave,
                   int *upleave, int *downleavestatus,
                   int *upleavestatus, int *downstatus, int *upstatus);


CPXLIBAPI
int CPXPUBLIC
   CPXdratio_async (CPXCENVptr env, CPXLPptr lp, int *indices, int cnt,
                    CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdratio_join (CPXASYNCptr *handle_p, int *indices,
                   double *downratio, double *upratio, int *downenter,
                   int *upenter, int *downstatus, int *upstatus);


CPXLIBAPI
int CPXPUBLIC
   CPXpivot_async (CPXCENVptr env, CPXLPptr lp, int jenter, int jleave,
                   int leavestat, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXpivot_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXpivot_multicast (CPXENVGROUPptr group, int jenter, int jleave,
                       int leavestat);


CPXLIBAPI
int CPXPUBLIC
   CPXsetphase2_async (CPXCENVptr env, CPXLPptr lp,
                       CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsetphase2_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsetphase2_multicast (CPXENVGROUPptr group);


CPXLIBAPI
int CPXPUBLIC
   CPXcheckpfeas_async (CPXCENVptr env, CPXLPptr lp,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcheckpfeas_join (CPXASYNCptr *handle_p, int *infeas_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcheckdfeas_async (CPXCENVptr env, CPXLPptr lp,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcheckdfeas_join (CPXASYNCptr *handle_p, int *infeas_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsolution_async (CPXCENVptr env, CPXCLPptr lp,
                      CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsolution_join (CPXASYNCptr *handle_p, int *lpstat_p,
                     double *objval_p, double *x, double *pi,
                     double *slack, double *dj);


CPXLIBAPI
int CPXPUBLIC
   CPXsolninfo_async (CPXCENVptr env, CPXCLPptr lp,
                      CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsolninfo_join (CPXASYNCptr *handle_p, int *solnmethod_p,
                     int *solntype_p, int *pfeasind_p, int *dfeasind_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetmethod_async (CPXCENVptr env, CPXCLPptr lp,
                       CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetmethod_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetobjval_async (CPXCENVptr env, CPXCLPptr lp,
                       CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetobjval_join (CPXASYNCptr *handle_p, double *objval_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetx_async (CPXCENVptr env, CPXCLPptr lp, int begin, int end,
                  CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetx_join (CPXASYNCptr *handle_p, double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXgetax_async (CPXCENVptr env, CPXCLPptr lp, int begin, int end,
                   CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetax_join (CPXASYNCptr *handle_p, double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXgetpi_async (CPXCENVptr env, CPXCLPptr lp, int begin, int end,
                   CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetpi_join (CPXASYNCptr *handle_p, double *pi);


CPXLIBAPI
int CPXPUBLIC
   CPXgetslack_async (CPXCENVptr env, CPXCLPptr lp, int begin, int end,
                      CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetslack_join (CPXASYNCptr *handle_p, double *slack);


CPXLIBAPI
int CPXPUBLIC
   CPXgetrowinfeas_async (CPXCENVptr env, CPXCLPptr lp,
                          const double *x, int begin, int end,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetrowinfeas_join (CPXASYNCptr *handle_p, double *infeasout);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcolinfeas_async (CPXCENVptr env, CPXCLPptr lp,
                          const double *x, int begin, int end,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcolinfeas_join (CPXASYNCptr *handle_p, double *infeasout);


CPXLIBAPI
int CPXPUBLIC
   CPXgetdj_async (CPXCENVptr env, CPXCLPptr lp, int begin, int end,
                   CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetdj_join (CPXASYNCptr *handle_p, double *dj);


CPXLIBAPI
int CPXPUBLIC
   CPXgetgrad_async (CPXCENVptr env, CPXCLPptr lp, int j,
                     CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetgrad_join (CPXASYNCptr *handle_p, int *head, double *y);


CPXLIBAPI
int CPXPUBLIC
   CPXgetijdiv_async (CPXCENVptr env, CPXCLPptr lp,
                      CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetijdiv_join (CPXASYNCptr *handle_p, int *idiv_p, int *jdiv_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetbase_async (CPXCENVptr env, CPXCLPptr lp,
                     CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetbase_join (CPXASYNCptr *handle_p, int *cstat, int *rstat);


CPXLIBAPI
int CPXPUBLIC
   CPXgetdblquality_async (CPXCENVptr env, CPXCLPptr lp, int what,
                           CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetdblquality_join (CPXASYNCptr *handle_p, double *quality_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpooldblquality_async (CPXCENVptr env, CPXCLPptr lp,
                                   int soln, int what,
                                   CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpooldblquality_join (CPXASYNCptr *handle_p,
                                  double *quality_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetintquality_async (CPXCENVptr env, CPXCLPptr lp, int what,
                           CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetintquality_join (CPXASYNCptr *handle_p, int *quality_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolintquality_async (CPXCENVptr env, CPXCLPptr lp,
                                   int soln, int what,
                                   CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolintquality_join (CPXASYNCptr *handle_p,
                                  int *quality_p);


CPXLIBAPI
int CPXPUBLIC
   CPXrhssa_async (CPXCENVptr env, CPXCLPptr lp, int begin, int end,
                   CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXrhssa_join (CPXASYNCptr *handle_p, double *lower, double *upper);


CPXLIBAPI
int CPXPUBLIC
   CPXboundsa_async (CPXCENVptr env, CPXCLPptr lp, int begin, int end,
                     CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXboundsa_join (CPXASYNCptr *handle_p, double *lblower,
                    double *lbupper, double *ublower, double *ubupper);


CPXLIBAPI
int CPXPUBLIC
   CPXobjsa_async (CPXCENVptr env, CPXCLPptr lp, int begin, int end,
                   CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXobjsa_join (CPXASYNCptr *handle_p, double *lower, double *upper);


CPXLIBAPI
int CPXPUBLIC
   CPXrefineconflict_async (CPXCENVptr env, CPXLPptr lp,
                            CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXrefineconflict_join (CPXASYNCptr *handle_p, int *confnumrows_p,
                           int *confnumcols_p);


CPXLIBAPI
int CPXPUBLIC
   CPXrefineconflictext_async (CPXCENVptr env, CPXLPptr lp, int grpcnt,
                               int concnt, const double *grppref,
                               const int *grpbeg, const int *grpind,
                               const char *grptype,
                               CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXrefineconflictext_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXrefineconflictext_multicast (CPXENVGROUPptr group, int grpcnt,
                                   int concnt, const double *grppref,
                                   const int *grpbeg,
                                   const int *grpind,
                                   const char *grptype);


CPXLIBAPI
int CPXPUBLIC
   CPXgetconflictext_async (CPXCENVptr env, CPXCLPptr lp, int beg,
                            int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetconflictext_join (CPXASYNCptr *handle_p, int *grpstat);


CPXLIBAPI
int CPXPUBLIC
   CPXclpwrite_async (CPXCENVptr env, CPXCLPptr lp,
                      const char *filename_str, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXclpwrite_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXclpwrite_multicast (CPXENVGROUPptr group,
                          const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXfeasopt_async (CPXCENVptr env, CPXLPptr lp, const double *rhs,
                     const double *rng, const double *lb,
                     const double *ub, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXfeasopt_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXfeasoptext_async (CPXCENVptr env, CPXLPptr lp, int grpcnt,
                        int concnt, const double *grppref,
                        const int *grpbeg, const int *grpind,
                        const char *grptype, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXfeasoptext_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXfeasoptext_multicast (CPXENVGROUPptr group, int grpcnt,
                            int concnt, const double *grppref,
                            const int *grpbeg, const int *grpind,
                            const char *grptype);


CPXLIBAPI
int CPXPUBLIC
   CPXnewrows_async (CPXCENVptr env, CPXLPptr lp, int rcnt,
                     const double *rhs, const char *sense,
                     const double *rngval, char **rowname,
                     CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXnewrows_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXaddrows_async (CPXCENVptr env, CPXLPptr lp, int ccnt, int rcnt,
                     int nzcnt, const double *rhs, const char *sense,
                     const int *rmatbeg, const int *rmatind,
                     const double *rmatval, char **colname,
                     char **rowname, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXaddrows_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXnewcols_async (CPXCENVptr env, CPXLPptr lp, int ccnt,
                     const double *obj, const double *lb,
                     const double *ub, const char *xctype,
                     char **colname, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXnewcols_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXaddcols_async (CPXCENVptr env, CPXLPptr lp, int ccnt, int nzcnt,
                     const double *obj, const int *cmatbeg,
                     const int *cmatind, const double *cmatval,
                     const double *lb, const double *ub,
                     char **colname, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXaddcols_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelrows_async (CPXCENVptr env, CPXLPptr lp, int begin, int end,
                     CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelrows_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelrows_multicast (CPXENVGROUPptr group, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsetrows_async (CPXCENVptr env, CPXLPptr lp, int *delstat,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsetrows_join (CPXASYNCptr *handle_p, int *delstat);


CPXLIBAPI
int CPXPUBLIC
   CPXdelcols_async (CPXCENVptr env, CPXLPptr lp, int begin, int end,
                     CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelcols_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelcols_multicast (CPXENVGROUPptr group, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsetcols_async (CPXCENVptr env, CPXLPptr lp, int *delstat,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsetcols_join (CPXASYNCptr *handle_p, int *delstat);


CPXLIBAPI
int CPXPUBLIC
   CPXchgname_async (CPXCENVptr env, CPXLPptr lp, int key, int ij,
                     const char *newname_str, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgname_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgrowname_async (CPXCENVptr env, CPXLPptr lp, int cnt,
                        const int *indices, char **newname,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgrowname_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgcolname_async (CPXCENVptr env, CPXLPptr lp, int cnt,
                        const int *indices, char **newname,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgcolname_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelnames_async (CPXCENVptr env, CPXLPptr lp,
                      CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelnames_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelnames_multicast (CPXENVGROUPptr group);


CPXLIBAPI
int CPXPUBLIC
   CPXchgprobname_async (CPXCENVptr env, CPXLPptr lp,
                         const char *probname, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgprobname_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgcoef_async (CPXCENVptr env, CPXLPptr lp, int i, int j,
                     double newvalue, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgcoef_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgcoef_multicast (CPXENVGROUPptr group, int i, int j,
                         double newvalue);


CPXLIBAPI
int CPXPUBLIC
   CPXchgcoeflist_async (CPXCENVptr env, CPXLPptr lp, int numcoefs,
                         const int *rowlist, const int *collist,
                         const double *vallist, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgcoeflist_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgcoeflist_multicast (CPXENVGROUPptr group, int numcoefs,
                             const int *rowlist, const int *collist,
                             const double *vallist);


CPXLIBAPI
int CPXPUBLIC
   CPXchgbds_async (CPXCENVptr env, CPXLPptr lp, int cnt,
                    const int *indices, const char *lu,
                    const double *bd, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgbds_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgbds_multicast (CPXENVGROUPptr group, int cnt,
                        const int *indices, const char *lu,
                        const double *bd);


CPXLIBAPI
int CPXPUBLIC
   CPXchgobj_async (CPXCENVptr env, CPXLPptr lp, int cnt,
                    const int *indices, const double *values,
                    CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgobj_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgobj_multicast (CPXENVGROUPptr group, int cnt,
                        const int *indices, const double *values);


CPXLIBAPI
int CPXPUBLIC
   CPXchgrhs_async (CPXCENVptr env, CPXLPptr lp, int cnt,
                    const int *indices, const double *values,
                    CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgrhs_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgrhs_multicast (CPXENVGROUPptr group, int cnt,
                        const int *indices, const double *values);


CPXLIBAPI
int CPXPUBLIC
   CPXchgrngval_async (CPXCENVptr env, CPXLPptr lp, int cnt,
                       const int *indices, const double *values,
                       CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgrngval_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgrngval_multicast (CPXENVGROUPptr group, int cnt,
                           const int *indices, const double *values);


CPXLIBAPI
int CPXPUBLIC
   CPXchgsense_async (CPXCENVptr env, CPXLPptr lp, int cnt,
                      const int *indices, const char *sense,
                      CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgsense_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgsense_multicast (CPXENVGROUPptr group, int cnt,
                          const int *indices, const char *sense);


CPXLIBAPI
int CPXPUBLIC
   CPXchgobjsen_async (CPXCENVptr env, CPXLPptr lp, int maxormin,
                       CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgobjsen_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgobjsen_multicast (CPXENVGROUPptr group, int maxormin);


CPXLIBAPI
int CPXPUBLIC
   CPXchgprobtype_async (CPXCENVptr env, CPXLPptr lp, int type,
                         CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgprobtype_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgprobtype_multicast (CPXENVGROUPptr group, int type);


CPXLIBAPI
int CPXPUBLIC
   CPXchgprobtypesolnpool_async (CPXCENVptr env, CPXLPptr lp, int type,
                                 int soln, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgprobtypesolnpool_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgprobtypesolnpool_multicast (CPXENVGROUPptr group, int type,
                                     int soln);


CPXLIBAPI
int CPXPUBLIC
   CPXcompletelp_async (CPXCENVptr env, CPXLPptr lp,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcompletelp_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcompletelp_multicast (CPXENVGROUPptr group);


CPXLIBAPI
int CPXPUBLIC
   CPXpreaddrows_async (CPXCENVptr env, CPXLPptr lp, int rcnt,
                        int nzcnt, const double *rhs,
                        const char *sense, const int *rmatbeg,
                        const int *rmatind, const double *rmatval,
                        char **rowname, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXpreaddrows_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXprechgobj_async (CPXCENVptr env, CPXLPptr lp, int cnt,
                       const int *indices, const double *values,
                       CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXprechgobj_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXprechgobj_multicast (CPXENVGROUPptr group, int cnt,
                           const int *indices, const double *values);


CPXLIBAPI
int CPXPUBLIC
   CPXgetobj_async (CPXCENVptr env, CPXCLPptr lp, int begin, int end,
                    CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetobj_join (CPXASYNCptr *handle_p, double *obj);


CPXLIBAPI
int CPXPUBLIC
   CPXgetrhs_async (CPXCENVptr env, CPXCLPptr lp, int begin, int end,
                    CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetrhs_join (CPXASYNCptr *handle_p, double *rhs);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsense_async (CPXCENVptr env, CPXCLPptr lp, int begin, int end,
                      CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsense_join (CPXASYNCptr *handle_p, char *sense);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcols_async (CPXCENVptr env, CPXCLPptr lp, int cmatspace,
                     int begin, int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcols_join (CPXASYNCptr *handle_p, int *nzcnt_p, int *cmatbeg,
                    int *cmatind, double *cmatval, int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetrows_async (CPXCENVptr env, CPXCLPptr lp, int rmatspace,
                     int begin, int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetrows_join (CPXASYNCptr *handle_p, int *nzcnt_p, int *rmatbeg,
                    int *rmatind, double *rmatval, int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetlb_async (CPXCENVptr env, CPXCLPptr lp, int begin, int end,
                   CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetlb_join (CPXASYNCptr *handle_p, double *lb);


CPXLIBAPI
int CPXPUBLIC
   CPXgetub_async (CPXCENVptr env, CPXCLPptr lp, int begin, int end,
                   CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetub_join (CPXASYNCptr *handle_p, double *ub);


CPXLIBAPI
int CPXPUBLIC
   CPXgetrngval_async (CPXCENVptr env, CPXCLPptr lp, int begin,
                       int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetrngval_join (CPXASYNCptr *handle_p, double *rngval);


CPXLIBAPI
int CPXPUBLIC
   CPXgetprobname_async (CPXCENVptr env, CPXCLPptr lp, int bufspace,
                         CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetprobname_join (CPXASYNCptr *handle_p, char *buf_str,
                        int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetobjname_async (CPXCENVptr env, CPXCLPptr lp, int bufspace,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetobjname_join (CPXASYNCptr *handle_p, char *buf_str,
                       int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcolname_async (CPXCENVptr env, CPXCLPptr lp, char *namestore,
                        int storespace, int begin, int end,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcolname_join (CPXASYNCptr *handle_p, char  **name,
                       int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetrowname_async (CPXCENVptr env, CPXCLPptr lp, char *namestore,
                        int storespace, int begin, int end,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetrowname_join (CPXASYNCptr *handle_p, char  **name,
                       int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcoef_async (CPXCENVptr env, CPXCLPptr lp, int i, int j,
                     CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcoef_join (CPXASYNCptr *handle_p, double *coef_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetrowindex_async (CPXCENVptr env, CPXCLPptr lp,
                         const char *lname_str, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetrowindex_join (CPXASYNCptr *handle_p, int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcolindex_async (CPXCENVptr env, CPXCLPptr lp,
                         const char *lname_str, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcolindex_join (CPXASYNCptr *handle_p, int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopyprob_async (CPXCENVptr env, CPXLPptr lp,
                          const char *filename_str,
                          const char *filetype_str,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopyprob_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopyprob_multicast (CPXENVGROUPptr group,
                              const char *filename_str,
                              const char *filetype_str);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopybase_async (CPXCENVptr env, CPXLPptr lp,
                          const char *filename_str,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopybase_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopybase_multicast (CPXENVGROUPptr group,
                              const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopysol_async (CPXCENVptr env, CPXLPptr lp,
                         const char *filename_str,
                         CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopysol_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopysol_multicast (CPXENVGROUPptr group,
                             const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXwriteprob_async (CPXCENVptr env, CPXCLPptr lp,
                       const char *filename_str,
                       const char *filetype_str, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXwriteprob_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXwriteprob_multicast (CPXENVGROUPptr group,
                           const char *filename_str,
                           const char *filetype_str);


CPXLIBAPI
int CPXPUBLIC
   CPXmbasewrite_async (CPXCENVptr env, CPXCLPptr lp,
                        const char *filename_str,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXmbasewrite_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXmbasewrite_multicast (CPXENVGROUPptr group,
                            const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXsolwrite_async (CPXCENVptr env, CPXCLPptr lp,
                      const char *filename_str, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsolwrite_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsolwrite_multicast (CPXENVGROUPptr group,
                          const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXsolwritesolnpool_async (CPXCENVptr env, CPXCLPptr lp, int soln,
                              const char *filename_str,
                              CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsolwritesolnpool_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsolwritesolnpool_multicast (CPXENVGROUPptr group, int soln,
                                  const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXsolwritesolnpoolall_async (CPXCENVptr env, CPXCLPptr lp,
                                 const char *filename_str,
                                 CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsolwritesolnpoolall_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsolwritesolnpoolall_multicast (CPXENVGROUPptr group,
                                     const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXembwrite_async (CPXCENVptr env, CPXLPptr lp,
                      const char *filename_str, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXembwrite_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXembwrite_multicast (CPXENVGROUPptr group,
                          const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXdperwrite_async (CPXCENVptr env, CPXLPptr lp,
                       const char *filename_str, double epsilon,
                       CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdperwrite_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdperwrite_multicast (CPXENVGROUPptr group,
                           const char *filename_str, double epsilon);


CPXLIBAPI
int CPXPUBLIC
   CPXpperwrite_async (CPXCENVptr env, CPXLPptr lp,
                       const char *filename_str, double epsilon,
                       CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXpperwrite_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXpperwrite_multicast (CPXENVGROUPptr group,
                           const char *filename_str, double epsilon);


CPXLIBAPI
int CPXPUBLIC
   CPXpreslvwrite_async (CPXCENVptr env, CPXLPptr lp,
                         const char *filename_str,
                         CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXpreslvwrite_join (CPXASYNCptr *handle_p, double *objoff_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdualwrite_async (CPXCENVptr env, CPXCLPptr lp,
                       const char *filename_str, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdualwrite_join (CPXASYNCptr *handle_p, double *objshift_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsetdefaults_async (CPXENVptr env, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsetdefaults_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsetdefaults_multicast (CPXENVGROUPptr group);


CPXLIBAPI
int CPXPUBLIC
   CPXsetintparam_async (CPXENVptr env, int whichparam,
                         CPXINT newvalue, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsetintparam_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsetintparam_multicast (CPXENVGROUPptr group, int whichparam,
                             CPXINT newvalue);


CPXLIBAPI
int CPXPUBLIC
   CPXsetlongparam_async (CPXENVptr env, int whichparam,
                          CPXLONG newvalue, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsetlongparam_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsetlongparam_multicast (CPXENVGROUPptr group, int whichparam,
                              CPXLONG newvalue);


CPXLIBAPI
int CPXPUBLIC
   CPXsetdblparam_async (CPXENVptr env, int whichparam,
                         double newvalue, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsetdblparam_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsetdblparam_multicast (CPXENVGROUPptr group, int whichparam,
                             double newvalue);


CPXLIBAPI
int CPXPUBLIC
   CPXgetintparam_async (CPXCENVptr env, int whichparam,
                         CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetintparam_join (CPXASYNCptr *handle_p, CPXINT *value_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetlongparam_async (CPXCENVptr env, int whichparam,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetlongparam_join (CPXASYNCptr *handle_p, CPXLONG *value_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetdblparam_async (CPXCENVptr env, int whichparam,
                         CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetdblparam_join (CPXASYNCptr *handle_p, double *value_p);


CPXLIBAPI
int CPXPUBLIC
   CPXinfointparam_async (CPXCENVptr env, int whichparam,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXinfointparam_join (CPXASYNCptr *handle_p, CPXINT *defvalue_p,
                         CPXINT *minvalue_p, CPXINT *maxvalue_p);


CPXLIBAPI
int CPXPUBLIC
   CPXinfolongparam_async (CPXCENVptr env, int whichparam,
                           CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXinfolongparam_join (CPXASYNCptr *handle_p, CPXLONG *defvalue_p,
                          CPXLONG *minvalue_p, CPXLONG *maxvalue_p);


CPXLIBAPI
int CPXPUBLIC
   CPXinfodblparam_async (CPXCENVptr env, int whichparam,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXinfodblparam_join (CPXASYNCptr *handle_p, double *defvalue_p,
                         double *minvalue_p, double *maxvalue_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetparamname_async (CPXCENVptr env, int whichparam,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetparamname_join (CPXASYNCptr *handle_p, char *name_str);


CPXLIBAPI
int CPXPUBLIC
   CPXgetparamnum_async (CPXCENVptr env, const char *name_str,
                         CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetparamnum_join (CPXASYNCptr *handle_p, int *whichparam_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetparamtype_async (CPXCENVptr env, int whichparam,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetparamtype_join (CPXASYNCptr *handle_p, int *paramtype);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopyparam_async (CPXENVptr env, const char *filename_str,
                           CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopyparam_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopyparam_multicast (CPXENVGROUPptr group,
                               const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXwriteparam_async (CPXCENVptr env, const char *filename_str,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXwriteparam_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXwriteparam_multicast (CPXENVGROUPptr group,
                            const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXgetchgparam_async (CPXCENVptr env, int pspace,
                         CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetchgparam_join (CPXASYNCptr *handle_p, int *cnt_p,
                        int *paramnum, int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXtuneparam_async (CPXENVptr env, CPXLPptr lp, int intcnt,
                       const int *intnum, const int *intval,
                       int dblcnt, const int *dblnum,
                       const double *dblval, int strcnt,
                       const int *strnum, char **strval,
                       CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXtuneparam_join (CPXASYNCptr *handle_p, int *tunestat_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetbhead_async (CPXCENVptr env, CPXCLPptr lp,
                      CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetbhead_join (CPXASYNCptr *handle_p, int *head, double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXbinvcol_async (CPXCENVptr env, CPXCLPptr lp, int j,
                     CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXbinvcol_join (CPXASYNCptr *handle_p, double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXbinvrow_async (CPXCENVptr env, CPXCLPptr lp, int i,
                     CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXbinvrow_join (CPXASYNCptr *handle_p, double *y);


CPXLIBAPI
int CPXPUBLIC
   CPXbinvacol_async (CPXCENVptr env, CPXCLPptr lp, int j,
                      CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXbinvacol_join (CPXASYNCptr *handle_p, double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXbinvarow_async (CPXCENVptr env, CPXCLPptr lp, int i,
                      CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXbinvarow_join (CPXASYNCptr *handle_p, double *z);


CPXLIBAPI
int CPXPUBLIC
   CPXftran_async (CPXCENVptr env, CPXCLPptr lp, double *x,
                   CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXftran_join (CPXASYNCptr *handle_p, double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXbtran_async (CPXCENVptr env, CPXCLPptr lp, double *y,
                   CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXbtran_join (CPXASYNCptr *handle_p, double *y);


CPXLIBAPI
int CPXPUBLIC
   CPXgetijrow_async (CPXCENVptr env, CPXCLPptr lp, int i, int j,
                      CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetijrow_join (CPXASYNCptr *handle_p, int *row_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetray_async (CPXCENVptr env, CPXCLPptr lp,
                    CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetray_join (CPXASYNCptr *handle_p, double *z);


CPXLIBAPI
int CPXPUBLIC
   CPXmdleave_async (CPXCENVptr env, CPXLPptr lp, const int *indices,
                     int cnt, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXmdleave_join (CPXASYNCptr *handle_p, double *downratio,
                    double *upratio);


CPXLIBAPI
int CPXPUBLIC
   CPXstrongbranch_async (CPXCENVptr env, CPXLPptr lp,
                          const int *indices, int cnt, int itlim,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXstrongbranch_join (CPXASYNCptr *handle_p, double *downobj,
                         double *upobj);


CPXLIBAPI
int CPXPUBLIC
   CPXdualfarkas_async (CPXCENVptr env, CPXCLPptr lp,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdualfarkas_join (CPXASYNCptr *handle_p, double *y,
                       double *proof_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetobjoffset_async (CPXCENVptr env, CPXCLPptr lp,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetobjoffset_join (CPXASYNCptr *handle_p, double *objoffset_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopypartialbase_async (CPXCENVptr env, CPXLPptr lp, int ccnt,
                             const int *cindices, const int *cstat,
                             int rcnt, const int *rindices,
                             const int *rstat, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopypartialbase_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopypartialbase_multicast (CPXENVGROUPptr group, int ccnt,
                                 const int *cindices, const int *cstat,
                                 int rcnt, const int *rindices,
                                 const int *rstat);


CPXLIBAPI
int CPXPUBLIC
   CPXgetbasednorms_async (CPXCENVptr env, CPXCLPptr lp,
                           CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetbasednorms_join (CPXASYNCptr *handle_p, int *cstat,
                          int *rstat, double *dnorm);


CPXLIBAPI
int CPXPUBLIC
   CPXcopybasednorms_async (CPXCENVptr env, CPXLPptr lp,
                            const int *cstat, const int *rstat,
                            const double *dnorm, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopybasednorms_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopydnorms_async (CPXCENVptr env, CPXLPptr lp,
                        const double *norm, const int *head, int len,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopydnorms_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopydnorms_multicast (CPXENVGROUPptr group, const double *norm,
                            const int *head, int len);


CPXLIBAPI
int CPXPUBLIC
   CPXpivotin_async (CPXCENVptr env, CPXLPptr lp, const int *rlist,
                     int rlen, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXpivotin_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXpivotin_multicast (CPXENVGROUPptr group, const int *rlist,
                         int rlen);


CPXLIBAPI
int CPXPUBLIC
   CPXpivotout_async (CPXCENVptr env, CPXLPptr lp, const int *clist,
                      int clen, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXpivotout_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXpivotout_multicast (CPXENVGROUPptr group, const int *clist,
                          int clen);


CPXLIBAPI
int CPXPUBLIC
   CPXchecksoln_async (CPXCENVptr env, CPXLPptr lp,
                       CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchecksoln_join (CPXASYNCptr *handle_p, int *lpstatus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXunscaleprob_async (CPXCENVptr env, CPXLPptr lp,
                         CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXunscaleprob_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXunscaleprob_multicast (CPXENVGROUPptr group);


CPXLIBAPI
int CPXPUBLIC
   CPXtightenbds_async (CPXCENVptr env, CPXLPptr lp, int cnt,
                        const int *indices, const char *lu,
                        const double *bd, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXtightenbds_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXtightenbds_multicast (CPXENVGROUPptr group, int cnt,
                            const int *indices, const char *lu,
                            const double *bd);


CPXLIBAPI
int CPXPUBLIC
   CPXpresolve_async (CPXCENVptr env, CPXLPptr lp, int method,
                      CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXpresolve_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXpresolve_multicast (CPXENVGROUPptr group, int method);


CPXLIBAPI
int CPXPUBLIC
   CPXbasicpresolve_async (CPXCENVptr env, CPXLPptr lp,
                           CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXbasicpresolve_join (CPXASYNCptr *handle_p, double *redlb,
                          double *redub, int *rstat);


CPXLIBAPI
int CPXPUBLIC
   CPXslackfromx_async (CPXCENVptr env, CPXCLPptr lp, const double *x,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXslackfromx_join (CPXASYNCptr *handle_p, double *slack);


CPXLIBAPI
int CPXPUBLIC
   CPXdjfrompi_async (CPXCENVptr env, CPXCLPptr lp, const double *pi,
                      CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdjfrompi_join (CPXASYNCptr *handle_p, double *dj);


CPXLIBAPI
int CPXPUBLIC
   CPXqpdjfrompi_async (CPXCENVptr env, CPXCLPptr lp, const double *pi,
                        const double *x, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXqpdjfrompi_join (CPXASYNCptr *handle_p, double *dj);


CPXLIBAPI
int CPXPUBLIC
   CPXfreepresolve_async (CPXCENVptr env, CPXLPptr lp,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXfreepresolve_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXfreepresolve_multicast (CPXENVGROUPptr group);


CPXLIBAPI
int CPXPUBLIC
   CPXcrushx_async (CPXCENVptr env, CPXCLPptr lp, const double *x,
                    CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcrushx_join (CPXASYNCptr *handle_p, double *prex);


CPXLIBAPI
int CPXPUBLIC
   CPXuncrushx_async (CPXCENVptr env, CPXCLPptr lp, const double *prex,
                      CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXuncrushx_join (CPXASYNCptr *handle_p, double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXcrushpi_async (CPXCENVptr env, CPXCLPptr lp, const double *pi,
                     CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcrushpi_join (CPXASYNCptr *handle_p, double *prepi);


CPXLIBAPI
int CPXPUBLIC
   CPXuncrushpi_async (CPXCENVptr env, CPXCLPptr lp,
                       const double *prepi, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXuncrushpi_join (CPXASYNCptr *handle_p, double *pi);


CPXLIBAPI
int CPXPUBLIC
   CPXqpuncrushpi_async (CPXCENVptr env, CPXCLPptr lp,
                         const double *prepi, const double *x,
                         CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXqpuncrushpi_join (CPXASYNCptr *handle_p, double *pi);


CPXLIBAPI
int CPXPUBLIC
   CPXcrushform_async (CPXCENVptr env, CPXCLPptr lp, int len,
                       const int *ind, const double *val,
                       CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcrushform_join (CPXASYNCptr *handle_p, int *plen_p,
                      double *poffset_p, int *pind, double *pval);


CPXLIBAPI
int CPXPUBLIC
   CPXgetprestat_async (CPXCENVptr env, CPXCLPptr lp,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetprestat_join (CPXASYNCptr *handle_p, int *prestat_p,
                       int *pcstat, int *prstat, int *ocstat,
                       int *orstat);


CPXLIBAPI
int CPXPUBLIC
   CPXcopyprotected_async (CPXCENVptr env, CPXLPptr lp, int cnt,
                           const int *indices, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopyprotected_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopyprotected_multicast (CPXENVGROUPptr group, int cnt,
                               const int *indices);


CPXLIBAPI
int CPXPUBLIC
   CPXgetprotected_async (CPXCENVptr env, CPXCLPptr lp, int pspace,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetprotected_join (CPXASYNCptr *handle_p, int *cnt_p,
                         int *indices, int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnumcores_async (CPXCENVptr env, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnumcores_join (CPXASYNCptr *handle_p, int *numcores_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgettime_async (CPXCENVptr env, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgettime_join (CPXASYNCptr *handle_p, double *timestamp_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetdettime_async (CPXCENVptr env, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetdettime_join (CPXASYNCptr *handle_p, double *dettimestamp_p);


CPXLIBAPI
int CPXPUBLIC
   CPXlpwrite_async (CPXCENVptr env, CPXCLPptr lp,
                     const char *filename_str, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXlpwrite_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXlpwrite_multicast (CPXENVGROUPptr group,
                         const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXlprewrite_async (CPXCENVptr env, CPXCLPptr lp,
                       const char *filename_str, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXlprewrite_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXlprewrite_multicast (CPXENVGROUPptr group,
                           const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXmpswrite_async (CPXCENVptr env, CPXCLPptr lp,
                      const char *filename_str, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXmpswrite_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXmpswrite_multicast (CPXENVGROUPptr group,
                          const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXmpsrewrite_async (CPXCENVptr env, CPXCLPptr lp,
                        const char *filename_str,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXmpsrewrite_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXmpsrewrite_multicast (CPXENVGROUPptr group,
                            const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXsavwrite_async (CPXCENVptr env, CPXCLPptr lp,
                      const char *filename_str, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsavwrite_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXsavwrite_multicast (CPXENVGROUPptr group,
                          const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXaddindconstr_async (CPXCENVptr env, CPXLPptr lp, int indvar,
                          int complemented, int nzcnt, double rhs,
                          int sense, const int *linind,
                          const double *linval,
                          const char *indname_str,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXaddindconstr_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopyctype_async (CPXCENVptr env, CPXLPptr lp, const char *xctype,
                       CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopyctype_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopyorder_async (CPXCENVptr env, CPXLPptr lp, int cnt,
                       const int *indices, const int *priority,
                       const int *direction, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopyorder_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopyorder_multicast (CPXENVGROUPptr group, int cnt,
                           const int *indices, const int *priority,
                           const int *direction);


CPXLIBAPI
int CPXPUBLIC
   CPXcopysos_async (CPXCENVptr env, CPXLPptr lp, int numsos,
                     int numsosnz, const char *sostype,
                     const int *sosbeg, const int *sosind,
                     const double *soswt, char **sosname,
                     CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopysos_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgmipstarts_async (CPXCENVptr env, CPXLPptr lp, int mcnt,
                          const int *mipstartindices, int nzcnt,
                          const int *beg, const int *varindices,
                          const double *values, const int *effortlevel,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgmipstarts_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgmipstarts_multicast (CPXENVGROUPptr group, int mcnt,
                              const int *mipstartindices, int nzcnt,
                              const int *beg, const int *varindices,
                              const double *values,
                              const int *effortlevel);


CPXLIBAPI
int CPXPUBLIC
   CPXaddmipstarts_async (CPXCENVptr env, CPXLPptr lp, int mcnt,
                          int nzcnt, const int *beg,
                          const int *varindices, const double *values,
                          const int *effortlevel, char **mipstartname,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXaddmipstarts_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelmipstarts_async (CPXCENVptr env, CPXLPptr lp, int begin,
                          int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelmipstarts_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelmipstarts_multicast (CPXENVGROUPptr group, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsetmipstarts_async (CPXCENVptr env, CPXLPptr lp, int *delstat,
                             CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsetmipstarts_join (CPXASYNCptr *handle_p, int *delstat);


CPXLIBAPI
int CPXPUBLIC
   CPXrefinemipstartconflict_async (CPXCENVptr env, CPXLPptr lp,
                                    int mipstartindex,
                                    CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXrefinemipstartconflict_join (CPXASYNCptr *handle_p,
                                   int *confnumrows_p,
                                   int *confnumcols_p);


CPXLIBAPI
int CPXPUBLIC
   CPXrefinemipstartconflictext_async (CPXCENVptr env, CPXLPptr lp,
                                       int mipstartindex, int grpcnt,
                                       int concnt,
                                       const double *grppref,
                                       const int *grpbeg,
                                       const int *grpind,
                                       const char *grptype,
                                       CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXrefinemipstartconflictext_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXrefinemipstartconflictext_multicast (CPXENVGROUPptr group,
                                           int mipstartindex,
                                           int grpcnt, int concnt,
                                           const double *grppref,
                                           const int *grpbeg,
                                           const int *grpind,
                                           const char *grptype);


CPXLIBAPI
int CPXPUBLIC
   CPXmipopt_async (CPXCENVptr env, CPXLPptr lp, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXmipopt_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXmipopt_multicast (CPXENVGROUPptr group);


CPXLIBAPI
int CPXPUBLIC
   CPXgetbestobjval_async (CPXCENVptr env, CPXCLPptr lp,
                           CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetbestobjval_join (CPXASYNCptr *handle_p, double *objval_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetmiprelgap_async (CPXCENVptr env, CPXCLPptr lp,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetmiprelgap_join (CPXASYNCptr *handle_p, double *gap_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcutoff_async (CPXCENVptr env, CPXCLPptr lp,
                       CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetcutoff_join (CPXASYNCptr *handle_p, double *cutoff_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnumcuts_async (CPXCENVptr env, CPXCLPptr lp, int cuttype,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnumcuts_join (CPXASYNCptr *handle_p, int *num_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnummipstarts_async (CPXCENVptr env, CPXCLPptr lp,
                             CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetnummipstarts_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetmipstarts_async (CPXCENVptr env, CPXCLPptr lp, int startspace,
                          int begin, int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetmipstarts_join (CPXASYNCptr *handle_p, int *nzcnt_p, int *beg,
                         int *varindices, double *values,
                         int *effortlevel, int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetmipstartname_async (CPXCENVptr env, CPXCLPptr lp, char *store,
                             int storesz, int begin, int end,
                             CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetmipstartname_join (CPXASYNCptr *handle_p, char  **name,
                            int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetmipstartindex_async (CPXCENVptr env, CPXCLPptr lp,
                              const char *lname_str,
                              CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetmipstartindex_join (CPXASYNCptr *handle_p, int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgctype_async (CPXCENVptr env, CPXLPptr lp, int cnt,
                      const int *indices, const char *xctype,
                      CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgctype_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgctype_multicast (CPXENVGROUPptr group, int cnt,
                          const int *indices, const char *xctype);


CPXLIBAPI
int CPXPUBLIC
   CPXaddsos_async (CPXCENVptr env, CPXLPptr lp, int numsos,
                    int numsosnz, const char *sostype,
                    const int *sosbeg, const int *sosind,
                    const double *soswt, char **sosname,
                    CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXaddsos_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsos_async (CPXCENVptr env, CPXLPptr lp, int begin, int end,
                    CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsos_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsos_multicast (CPXENVGROUPptr group, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsetsos_async (CPXCENVptr env, CPXLPptr lp, int *delset,
                       CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsetsos_join (CPXASYNCptr *handle_p, int *delset);


CPXLIBAPI
int CPXPUBLIC
   CPXgetctype_async (CPXCENVptr env, CPXCLPptr lp, int begin, int end,
                      CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetctype_join (CPXASYNCptr *handle_p, char *xctype);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsos_async (CPXCENVptr env, CPXCLPptr lp, int sosspace,
                    int begin, int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsos_join (CPXASYNCptr *handle_p, int *numsosnz_p,
                   char *sostype, int *sosbeg, int *sosind,
                   double *soswt, int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsosname_async (CPXCENVptr env, CPXCLPptr lp, char *namestore,
                        int storespace, int begin, int end,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsosname_join (CPXASYNCptr *handle_p, char  **name,
                       int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsosindex_async (CPXCENVptr env, CPXCLPptr lp,
                         const char *lname_str, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsosindex_join (CPXASYNCptr *handle_p, int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsosinfeas_async (CPXCENVptr env, CPXCLPptr lp,
                          const double *x, int begin, int end,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsosinfeas_join (CPXASYNCptr *handle_p, double *infeasout);


CPXLIBAPI
int CPXPUBLIC
   CPXgetindconstr_async (CPXCENVptr env, CPXCLPptr lp, int space,
                          int which, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetindconstr_join (CPXASYNCptr *handle_p, int *indvar_p,
                         int *complemented_p, int *nzcnt_p,
                         double *rhs_p, char *sense_p, int *linind,
                         double *linval, int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetindconstrindex_async (CPXCENVptr env, CPXCLPptr lp,
                               const char *lname_str,
                               CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetindconstrindex_join (CPXASYNCptr *handle_p, int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetindconstrname_async (CPXCENVptr env, CPXCLPptr lp,
                              int bufspace, int which,
                              CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetindconstrname_join (CPXASYNCptr *handle_p, char *buf_str,
                             int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetindconstrslack_async (CPXCENVptr env, CPXCLPptr lp, int begin,
                               int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetindconstrslack_join (CPXASYNCptr *handle_p, double *indslack);


CPXLIBAPI
int CPXPUBLIC
   CPXindconstrslackfromx_async (CPXCENVptr env, CPXCLPptr lp,
                                 const double *x,
                                 CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXindconstrslackfromx_join (CPXASYNCptr *handle_p,
                                double *indslack);


CPXLIBAPI
int CPXPUBLIC
   CPXgetindconstrinfeas_async (CPXCENVptr env, CPXCLPptr lp,
                                const double *x, int begin, int end,
                                CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetindconstrinfeas_join (CPXASYNCptr *handle_p,
                               double *infeasout);


CPXLIBAPI
int CPXPUBLIC
   CPXdelindconstrs_async (CPXCENVptr env, CPXLPptr lp, int begin,
                           int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelindconstrs_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelindconstrs_multicast (CPXENVGROUPptr group, int begin,
                               int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetorder_async (CPXCENVptr env, CPXCLPptr lp, int ordspace,
                      CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetorder_join (CPXASYNCptr *handle_p, int *cnt_p, int *indices,
                     int *priority, int *direction, int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXpopulate_async (CPXCENVptr env, CPXLPptr lp,
                      CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXpopulate_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXpopulate_multicast (CPXENVGROUPptr group);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolnumfilters_async (CPXCENVptr env, CPXCLPptr lp,
                                   CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolnumfilters_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXaddsolnpooldivfilter_async (CPXCENVptr env, CPXLPptr lp,
                                  double lower_bound,
                                  double upper_bound, int nzcnt,
                                  const int *ind, const double *weight,
                                  const double *refval,
                                  const char *lname_str,
                                  CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXaddsolnpooldivfilter_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXaddsolnpoolrngfilter_async (CPXCENVptr env, CPXLPptr lp,
                                  double lb, double ub, int nzcnt,
                                  const int *ind, const double *val,
                                  const char *lname_str,
                                  CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXaddsolnpoolrngfilter_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolfiltertype_async (CPXCENVptr env, CPXCLPptr lp,
                                   int which, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolfiltertype_join (CPXASYNCptr *handle_p, int *ftype_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpooldivfilter_async (CPXCENVptr env, CPXCLPptr lp,
                                  int space, int which,
                                  CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpooldivfilter_join (CPXASYNCptr *handle_p,
                                 double *lower_cutoff_p,
                                 double *upper_cutoff_p, int *nzcnt_p,
                                 int *ind, double *val, double *refval,
                                 int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolfilter_async (CPXCENVptr env, CPXCLPptr lp, int space,
                               int which, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolfilter_join (CPXASYNCptr *handle_p, int *ftype_p,
                              double *lowercutoff_p,
                              double *upper_cutoff_p, int *nzcnt_p,
                              int *ind, double *val, double *refval,
                              int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolrngfilter_async (CPXCENVptr env, CPXCLPptr lp,
                                  int space, int which,
                                  CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolrngfilter_join (CPXASYNCptr *handle_p, double *lb_p,
                                 double *ub_p, int *nzcnt_p, int *ind,
                                 double *val, int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolfiltername_async (CPXCENVptr env, CPXCLPptr lp,
                                   int bufspace, int which,
                                   CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolfiltername_join (CPXASYNCptr *handle_p, char *buf_str,
                                  int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolfilterindex_async (CPXCENVptr env, CPXCLPptr lp,
                                    const char *lname_str,
                                    CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolfilterindex_join (CPXASYNCptr *handle_p, int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsolnpoolfilters_async (CPXCENVptr env, CPXLPptr lp, int begin,
                                int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsolnpoolfilters_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsolnpoolfilters_multicast (CPXENVGROUPptr group, int begin,
                                    int end);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsetsolnpoolfilters_async (CPXCENVptr env, CPXLPptr lp,
                                   int *delstat, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsetsolnpoolfilters_join (CPXASYNCptr *handle_p, int *delstat);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolnumsolns_async (CPXCENVptr env, CPXCLPptr lp,
                                 CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolnumsolns_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolnumreplaced_async (CPXCENVptr env, CPXCLPptr lp,
                                    CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolnumreplaced_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolmeanobjval_async (CPXCENVptr env, CPXCLPptr lp,
                                   CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolmeanobjval_join (CPXASYNCptr *handle_p,
                                  double *meanobjval_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolobjval_async (CPXCENVptr env, CPXCLPptr lp, int soln,
                               CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolobjval_join (CPXASYNCptr *handle_p, double *objval_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolx_async (CPXCENVptr env, CPXCLPptr lp, int soln,
                          int begin, int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolx_join (CPXASYNCptr *handle_p, double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolslack_async (CPXCENVptr env, CPXCLPptr lp, int soln,
                              int begin, int end,
                              CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolslack_join (CPXASYNCptr *handle_p, double *slack);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolqconstrslack_async (CPXCENVptr env, CPXCLPptr lp,
                                     int soln, int begin, int end,
                                     CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolqconstrslack_join (CPXASYNCptr *handle_p,
                                    double *qcslack);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolsolnname_async (CPXCENVptr env, CPXCLPptr lp,
                                 int storesz, int which,
                                 CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolsolnname_join (CPXASYNCptr *handle_p, char *store,
                                int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolsolnindex_async (CPXCENVptr env, CPXCLPptr lp,
                                  const char *lname_str,
                                  CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetsolnpoolsolnindex_join (CPXASYNCptr *handle_p, int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsolnpoolsolns_async (CPXCENVptr env, CPXLPptr lp, int begin,
                              int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsolnpoolsolns_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsolnpoolsolns_multicast (CPXENVGROUPptr group, int begin,
                                  int end);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsetsolnpoolsolns_async (CPXCENVptr env, CPXLPptr lp,
                                 int *delstat, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelsetsolnpoolsolns_join (CPXASYNCptr *handle_p, int *delstat);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopyorder_async (CPXCENVptr env, CPXLPptr lp,
                           const char *filename_str,
                           CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopyorder_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopyorder_multicast (CPXENVGROUPptr group,
                               const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopysolnpoolfilters_async (CPXCENVptr env, CPXLPptr lp,
                                     const char *filename_str,
                                     CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopysolnpoolfilters_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopysolnpoolfilters_multicast (CPXENVGROUPptr group,
                                         const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopymipstarts_async (CPXCENVptr env, CPXLPptr lp,
                               const char *filename_str,
                               CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopymipstarts_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXreadcopymipstarts_multicast (CPXENVGROUPptr group,
                                   const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXordwrite_async (CPXCENVptr env, CPXCLPptr lp,
                      const char *filename_str, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXordwrite_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXordwrite_multicast (CPXENVGROUPptr group,
                          const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXwritemipstarts_async (CPXCENVptr env, CPXCLPptr lp,
                            const char *filename_str, int begin,
                            int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXwritemipstarts_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXwritemipstarts_multicast (CPXENVGROUPptr group,
                                const char *filename_str, int begin,
                                int end);


CPXLIBAPI
int CPXPUBLIC
   CPXfltwrite_async (CPXCENVptr env, CPXCLPptr lp,
                      const char *filename_str, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXfltwrite_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXfltwrite_multicast (CPXENVGROUPptr group,
                          const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXaddusercuts_async (CPXCENVptr env, CPXLPptr lp, int rcnt,
                         int nzcnt, const double *rhs,
                         const char *sense, const int *rmatbeg,
                         const int *rmatind, const double *rmatval,
                         char **rowname, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXaddusercuts_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXaddlazyconstraints_async (CPXCENVptr env, CPXLPptr lp, int rcnt,
                                int nzcnt, const double *rhs,
                                const char *sense, const int *rmatbeg,
                                const int *rmatind,
                                const double *rmatval, char **rowname,
                                CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXaddlazyconstraints_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXfreeusercuts_async (CPXCENVptr env, CPXLPptr lp,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXfreeusercuts_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXfreeusercuts_multicast (CPXENVGROUPptr group);


CPXLIBAPI
int CPXPUBLIC
   CPXfreelazyconstraints_async (CPXCENVptr env, CPXLPptr lp,
                                 CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXfreelazyconstraints_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXfreelazyconstraints_multicast (CPXENVGROUPptr group);


CPXLIBAPI
int CPXPUBLIC
   CPXordread_async (CPXCENVptr env, const char *filename_str,
                     int numcols, char **colname,
                     CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXordread_join (CPXASYNCptr *handle_p, int *cnt_p, int *indices,
                    int *priority, int *direction);


CPXLIBAPI
int CPXPUBLIC
   CPXNETcopynet_async (CPXCENVptr env, CPXNETptr net, int objsen,
                        int nnodes, const double *supply,
                        char **nnames, int narcs, const int *fromnode,
                        const int *tonode, const double *low,
                        const double *up, const double *obj,
                        char **anames, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETcopynet_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETcopybase_async (CPXCENVptr env, CPXNETptr net,
                         const int *astat, const int *nstat,
                         CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETcopybase_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETaddnodes_async (CPXCENVptr env, CPXNETptr net, int nnodes,
                         const double *supply, char **name,
                         CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETaddnodes_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETaddarcs_async (CPXCENVptr env, CPXNETptr net, int narcs,
                        const int *fromnode, const int *tonode,
                        const double *low, const double *up,
                        const double *obj, char **anames,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETaddarcs_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETdelnodes_async (CPXCENVptr env, CPXNETptr net, int begin,
                         int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETdelnodes_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETdelarcs_async (CPXCENVptr env, CPXNETptr net, int begin,
                        int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETdelarcs_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETdelset_async (CPXCENVptr env, CPXNETptr net, int *whichnodes,
                       int *whicharcs, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETdelset_join (CPXASYNCptr *handle_p, int *whichnodes,
                      int *whicharcs);


CPXLIBAPI
int CPXPUBLIC
   CPXNETprimopt_async (CPXCENVptr env, CPXNETptr net,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETprimopt_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETprimopt_multicast (CPXENVGROUPptr group);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetobjval_async (CPXCENVptr env, CPXCNETptr net,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetobjval_join (CPXASYNCptr *handle_p, double *objval_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetx_async (CPXCENVptr env, CPXCNETptr net, int begin,
                     int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetx_join (CPXASYNCptr *handle_p, double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetpi_async (CPXCENVptr env, CPXCNETptr net, int begin,
                      int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetpi_join (CPXASYNCptr *handle_p, double *pi);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetslack_async (CPXCENVptr env, CPXCNETptr net, int begin,
                         int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetslack_join (CPXASYNCptr *handle_p, double *slack);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetdj_async (CPXCENVptr env, CPXCNETptr net, int begin,
                      int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetdj_join (CPXASYNCptr *handle_p, double *dj);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetbase_async (CPXCENVptr env, CPXCNETptr net,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetbase_join (CPXASYNCptr *handle_p, int *astat, int *nstat);


CPXLIBAPI
int CPXPUBLIC
   CPXNETsolution_async (CPXCENVptr env, CPXCNETptr net,
                         CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETsolution_join (CPXASYNCptr *handle_p, int *netstat_p,
                        double *objval_p, double *x, double *pi,
                        double *slack, double *dj);


CPXLIBAPI
int CPXPUBLIC
   CPXNETsolninfo_async (CPXCENVptr env, CPXCNETptr net,
                         CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETsolninfo_join (CPXASYNCptr *handle_p, int *pfeasind_p,
                        int *dfeasind_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgname_async (CPXCENVptr env, CPXNETptr net, int key,
                        int vindex, const char *name_str,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgname_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgarcname_async (CPXCENVptr env, CPXNETptr net, int cnt,
                           const int *indices, char **newname,
                           CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgarcname_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgnodename_async (CPXCENVptr env, CPXNETptr net, int cnt,
                            const int *indices, char **newname,
                            CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgnodename_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgobjsen_async (CPXCENVptr env, CPXNETptr net, int maxormin,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgobjsen_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgobjsen_multicast (CPXENVGROUPptr group, int maxormin);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgbds_async (CPXCENVptr env, CPXNETptr net, int cnt,
                       const int *indices, const char *lu,
                       const double *bd, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgbds_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgbds_multicast (CPXENVGROUPptr group, int cnt,
                           const int *indices, const char *lu,
                           const double *bd);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgarcnodes_async (CPXCENVptr env, CPXNETptr net, int cnt,
                            const int *indices, const int *fromnode,
                            const int *tonode, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgarcnodes_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgarcnodes_multicast (CPXENVGROUPptr group, int cnt,
                                const int *indices,
                                const int *fromnode, const int *tonode);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgobj_async (CPXCENVptr env, CPXNETptr net, int cnt,
                       const int *indices, const double *obj,
                       CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgobj_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgobj_multicast (CPXENVGROUPptr group, int cnt,
                           const int *indices, const double *obj);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgsupply_async (CPXCENVptr env, CPXNETptr net, int cnt,
                          const int *indices, const double *supply,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgsupply_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETchgsupply_multicast (CPXENVGROUPptr group, int cnt,
                              const int *indices, const double *supply);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetsupply_async (CPXCENVptr env, CPXCNETptr net, int begin,
                          int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetsupply_join (CPXASYNCptr *handle_p, double *supply);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetprobname_async (CPXCENVptr env, CPXCNETptr net,
                            int bufspace, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetprobname_join (CPXASYNCptr *handle_p, char *buf_str,
                           int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetnodename_async (CPXCENVptr env, CPXCNETptr net,
                            char *namestore, int namespc, int begin,
                            int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetnodename_join (CPXASYNCptr *handle_p, char  **nnames,
                           int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetarcname_async (CPXCENVptr env, CPXCNETptr net,
                           char *namestore, int namespc, int begin,
                           int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetarcname_join (CPXASYNCptr *handle_p, char  **nnames,
                          int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetlb_async (CPXCENVptr env, CPXCNETptr net, int begin,
                      int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetlb_join (CPXASYNCptr *handle_p, double *low);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetub_async (CPXCENVptr env, CPXCNETptr net, int begin,
                      int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetub_join (CPXASYNCptr *handle_p, double *up);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetobj_async (CPXCENVptr env, CPXCNETptr net, int begin,
                       int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetobj_join (CPXASYNCptr *handle_p, double *obj);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetarcnodes_async (CPXCENVptr env, CPXCNETptr net, int begin,
                            int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetarcnodes_join (CPXASYNCptr *handle_p, int *fromnode,
                           int *tonode);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetnodearcs_async (CPXCENVptr env, CPXCNETptr net,
                            int arcspace, int begin, int end,
                            CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetnodearcs_join (CPXASYNCptr *handle_p, int *arccnt_p,
                           int *arcbeg, int *arc, int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetnodeindex_async (CPXCENVptr env, CPXCNETptr net,
                             const char *lname_str,
                             CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetnodeindex_join (CPXASYNCptr *handle_p, int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetarcindex_async (CPXCENVptr env, CPXCNETptr net,
                            const char *lname_str,
                            CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETgetarcindex_join (CPXASYNCptr *handle_p, int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETreadcopyprob_async (CPXCENVptr env, CPXNETptr net,
                             const char *filename_str,
                             CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETreadcopyprob_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETreadcopyprob_multicast (CPXENVGROUPptr group,
                                 const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXNETreadcopybase_async (CPXCENVptr env, CPXNETptr net,
                             const char *filename_str,
                             CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETreadcopybase_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETreadcopybase_multicast (CPXENVGROUPptr group,
                                 const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXNETwriteprob_async (CPXCENVptr env, CPXCNETptr net,
                          const char *filename_str,
                          const char *format_str,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETwriteprob_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETwriteprob_multicast (CPXENVGROUPptr group,
                              const char *filename_str,
                              const char *format_str);


CPXLIBAPI
int CPXPUBLIC
   CPXNETbasewrite_async (CPXCENVptr env, CPXCNETptr net,
                          const char *filename_str,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETbasewrite_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXNETbasewrite_multicast (CPXENVGROUPptr group,
                              const char *filename_str);


CPXLIBAPI
int CPXPUBLIC
   CPXcopyquad_async (CPXCENVptr env, CPXLPptr lp, const int *qmatbeg,
                      const int *qmatcnt, const int *qmatind,
                      const double *qmatval, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopyquad_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopyqpsep_async (CPXCENVptr env, CPXLPptr lp,
                       const double *qsepvec, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopyqpsep_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgqpcoef_async (CPXCENVptr env, CPXLPptr lp, int i, int j,
                       double newvalue, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgqpcoef_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXchgqpcoef_multicast (CPXENVGROUPptr group, int i, int j,
                           double newvalue);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqpcoef_async (CPXCENVptr env, CPXCLPptr lp, int rownum,
                       int colnum, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqpcoef_join (CPXASYNCptr *handle_p, double *coef_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetquad_async (CPXCENVptr env, CPXCLPptr lp, int qmatspace,
                     int begin, int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetquad_join (CPXASYNCptr *handle_p, int *nzcnt_p, int *qmatbeg,
                    int *qmatind, double *qmatval, int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXqpindefcertificate_async (CPXCENVptr env, CPXCLPptr lp,
                                CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXqpindefcertificate_join (CPXASYNCptr *handle_p, double *x);


CPXLIBAPI
int CPXPUBLIC
   CPXqpopt_async (CPXCENVptr env, CPXLPptr lp, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXqpopt_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXqpopt_multicast (CPXENVGROUPptr group);


CPXLIBAPI
int CPXPUBLIC
   CPXaddqconstr_async (CPXCENVptr env, CPXLPptr lp, int linnzcnt,
                        int quadnzcnt, double rhs, int sense,
                        const int *linind, const double *linval,
                        const int *quadrow, const int *quadcol,
                        const double *quadval, const char *lname_str,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXaddqconstr_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelqconstrs_async (CPXCENVptr env, CPXLPptr lp, int begin,
                         int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelqconstrs_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXdelqconstrs_multicast (CPXENVGROUPptr group, int begin, int end);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqconstrindex_async (CPXCENVptr env, CPXCLPptr lp,
                             const char *lname_str,
                             CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqconstrindex_join (CPXASYNCptr *handle_p, int *index_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqconstr_async (CPXCENVptr env, CPXCLPptr lp, int linspace,
                        int quadspace, int which,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqconstr_join (CPXASYNCptr *handle_p, int *linnzcnt_p,
                       int *quadnzcnt_p, double *rhs_p, char *sense_p,
                       int *linind, double *linval, int *linsurplus_p,
                       int *quadrow, int *quadcol, double *quadval,
                       int *quadsurplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqconstrname_async (CPXCENVptr env, CPXCLPptr lp, int bufspace,
                            int which, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqconstrname_join (CPXASYNCptr *handle_p, char *buf_str,
                           int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqconstrslack_async (CPXCENVptr env, CPXCLPptr lp, int begin,
                             int end, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqconstrslack_join (CPXASYNCptr *handle_p, double *qcslack);


CPXLIBAPI
int CPXPUBLIC
   CPXqconstrslackfromx_async (CPXCENVptr env, CPXCLPptr lp,
                               const double *x, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXqconstrslackfromx_join (CPXASYNCptr *handle_p, double *qcslack);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqconstrinfeas_async (CPXCENVptr env, CPXCLPptr lp,
                              const double *x, int begin, int end,
                              CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqconstrinfeas_join (CPXASYNCptr *handle_p, double *infeasout);


CPXLIBAPI
int CPXPUBLIC
   CPXgetxqxax_async (CPXCENVptr env, CPXCLPptr lp, int begin, int end,
                      CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetxqxax_join (CPXASYNCptr *handle_p, double *xqxax);


CPXLIBAPI
int CPXPUBLIC
   CPXgetdnorms_async (CPXCENVptr env, CPXCLPptr lp,
                       CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetdnorms_join (CPXASYNCptr *handle_p, double *norm, int *head,
                      int *len_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetpnorms_async (CPXCENVptr env, CPXCLPptr lp,
                       CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetpnorms_join (CPXASYNCptr *handle_p, double *cnorm,
                      double *rnorm, int *len_p);


CPXLIBAPI
int CPXPUBLIC
   CPXkilldnorms_async (CPXENVptr env, CPXLPptr lp,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXkilldnorms_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXkilldnorms_multicast (CPXENVGROUPptr group);


CPXLIBAPI
int CPXPUBLIC
   CPXkillpnorms_async (CPXENVptr env, CPXLPptr lp,
                        CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXkillpnorms_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXkillpnorms_multicast (CPXENVGROUPptr group);


CPXLIBAPI
int CPXPUBLIC
   CPXcopypnorms_async (CPXCENVptr env, CPXLPptr lp,
                        const double *cnorm, const double *rnorm,
                        int len, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXcopypnorms_join (CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqconstrdslack_async (CPXCENVptr env, CPXCLPptr lp, int qind,
                              int space, CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXgetqconstrdslack_join (CPXASYNCptr *handle_p, int *nz_p,
                             int *ind, double *val, int *surplus_p);


CPXLIBAPI
int CPXPUBLIC
   CPXuserfunction_async (CPXENVptr env, int id, CPXLONG insize,
                          void const *indata, CPXLONG outspace,
                          CPXASYNCptr *handle_p);


CPXLIBAPI
int CPXPUBLIC
   CPXuserfunction_join (CPXASYNCptr *handle_p, CPXLONG *outsize_p,
                         void *outdata);



#ifdef __cplusplus
}
#endif

#ifdef _WIN32
#pragma pack(pop)
#endif

#endif /* !CPX_CPLEX_REMOTEMASTER_H */
