/* --------------------------------------------------------------------------
 * File: xadmipex3.c
 * Version 12.3
 * --------------------------------------------------------------------------
 * Licensed Materials - Property of IBM
 * 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55
 * Copyright IBM Corporation 1997, 2011. All Rights Reserved.
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with
 * IBM Corp.
 * --------------------------------------------------------------------------
 */

/* xadmipex3.c - Using the branch callback for optimizing a MIP 
                problem with Special Ordered Sets Type 1, with all 
                the variables binary, using cplexx.h */

/* To run this example, command line arguments are required:
       xadmipex3 [-r] filename
   where 
       filename  Name of the file, with .mps, .lp, or .sav
                 extension, and a possible additional .gz 
                 extension.
       -r        Indicates that callbacks will refer to the
                 presolved model.
   Example:
       admipex3  mexample.mps
       admipex3 -r mexample.mps */

/* Bring in the CPLEX function declarations and the C library 
   header file stdio.h with the following single include */

#include <ilcplex/cplexx.h>

/* Bring in the declarations for the string and character functions,
   malloc, floor, and fabs */

#include <ctype.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

/* Declarations for functions in this program */

static int CPXPUBLIC 
   usersetbranch (CPXCENVptr env, void *cbdata, int wherefrom,
                  void *userinfo, int brtype, CPXDIM sos, int nodes,
                  CPXDIM bdcnt, const double *nodeest, const CPXDIM *nodebeg,
                  const CPXDIM *indices, const char *lu,
                  const int *bd, int *useraction_p);

static void
   free_and_null (char **ptr),
   usage         (char *progname);



int
main (int  argc,
      char *argv[])
{
   int status = 0;

   /* Declare and allocate space for the variables and arrays where
      we will store the optimization results, including the status, 
      objective value, and variable values */
   
   int    solstat;
   double objval;
   double *x = NULL;
   
   CPXENVptr env = NULL;
   CPXLPptr  lp = NULL;
   
   CPXDIM     j;
   CPXDIM     cur_numcols;
   CPXFILEptr logfile = NULL;
   int        wantorig = 1;
   int        nameind = 1;

   /* Check the command line arguments */

   if ( argc != 2 ) {
      if ( argc != 3         ||
           argv[1][0] != '-' ||
           argv[1][1] != 'r'   ) {
         usage (argv[0]);
         goto TERMINATE;
      }
      wantorig = 0;
      nameind = 2;
   }

   /* Initialize the CPLEX environment */

   env = CPXXopenCPLEX (&status);

   /* If an error occurs, the status value indicates the reason for
      failure.  A call to CPXXgeterrorstring will produce the text of
      the error message.  Note that CPXXopenCPLEX produces no
      output, so the only way to see the cause of the error is to use
      CPXXgeterrorstring.  For other CPLEX routines, the errors will
      be seen if the CPX_PARAM_SCRIND parameter is set to CPX_ON */

   if ( env == NULL ) {
      char errmsg[CPXMESSAGEBUFSIZE];
      fprintf (stderr, "Could not open CPLEX environment.\n");
      CPXXgeterrorstring (env, status, errmsg);
      fprintf (stderr, "%s", errmsg);
      goto TERMINATE;
   }

   /* Turn on output to the screen */

   status = CPXXsetintparam (env, CPX_PARAM_SCRIND, CPX_ON);
   if ( status != 0 ) {
      fprintf (stderr, 
               "Failure to turn on screen indicator, error %d.\n",
               status);
      goto TERMINATE;
   }

   /* Set MIP parameters */

   status = CPXXsetcntparam (env, CPX_PARAM_NODELIM, 10000);
   if ( status )  goto TERMINATE;

   /* Open a logfile */

   logfile = CPXXfopen ("admipex3.log", "a");
   if ( logfile == NULL )  goto TERMINATE;
   status = CPXXsetlogfile (env, logfile);
   if ( status )  goto TERMINATE;

   /* Create the problem, using the filename as the problem name */

   lp = CPXXcreateprob (env, &status, argv[nameind]);

   /* A returned pointer of NULL may mean that not enough memory
      was available or there was some other problem.  In the case of
      failure, an error message will have been written to the error
      channel from inside CPLEX.  In this example, the setting of
      the parameter CPX_PARAM_SCRIND causes the error message to
      appear on stdout.  Note that most CPLEX routines return
      an error code to indicate the reason for failure */

   if ( lp == NULL ) {
      fprintf (stderr, "Failed to create LP.\n");
      goto TERMINATE;
   }

   /* Now read the file, and copy the data into the created lp */

   status = CPXXreadcopyprob (env, lp, argv[nameind], NULL);
   if ( status ) {
      fprintf (stderr,
               "Failed to read and copy the problem data.\n");
      goto TERMINATE;
   }

   /* Set up to use MIP callbacks */

   status = CPXXsetbranchcallbackfunc (env, usersetbranch, NULL);
   if ( status )  goto TERMINATE;

   if ( wantorig ) {
      /* Assure linear mappings between the presolved and original
         models */

      status = CPXXsetintparam (env, CPX_PARAM_PRELINEAR, 0);
      if ( status )  goto TERMINATE;

      /* Let MIP callbacks work on the original model */

      status = CPXXsetintparam (env, CPX_PARAM_MIPCBREDLP, CPX_OFF);
      if ( status )  goto TERMINATE;
   }


   /* Turn on traditional search for use with control callbacks */

   status = CPXXsetintparam (env, CPX_PARAM_MIPSEARCH, CPX_MIPSEARCH_TRADITIONAL);
   if ( status )  goto TERMINATE;

   /* Optimize the problem and obtain solution */

   status = CPXXmipopt (env, lp);

   if ( status ) {
      fprintf (stderr, "Failed to optimize MIP.\n");
      goto TERMINATE;
   }

   solstat = CPXXgetstat (env, lp);
   printf ("Solution status %d.\n", solstat);

   status = CPXXgetobjval (env, lp, &objval);

   if ( status ) {
      fprintf (stderr,"Failed to obtain objective value.\n");
      goto TERMINATE;
   }

   printf ("Objective value %.10g\n", objval);

   cur_numcols = CPXXgetnumcols (env, lp);

   /* Allocate space for solution */

   x = malloc (cur_numcols * sizeof (*x));

   if ( x == NULL ) {
      fprintf (stderr, "No memory for solution values.\n");
      goto TERMINATE;
   }

   status = CPXXgetx (env, lp, x, 0, cur_numcols-1);
   if ( status ) {
      fprintf (stderr, "Failed to obtain solution.\n");
      goto TERMINATE;
   }

   /* Write out the solution */

   for (j = 0; j < cur_numcols; j++) {
      if ( fabs (x[j]) > 1e-10 ) {
         printf ( "Column %d:  Value = %17.10g\n", j, x[j]);
      }
   }
   

TERMINATE:

   /* Free the solution vector */

   free_and_null ((char **) &x);

   /* Free the problem as allocated by CPXXcreateprob and
      CPXXreadcopyprob, if necessary */

   if ( lp != NULL ) {
      status = CPXXfreeprob (env, &lp);
      if ( status ) {
         fprintf (stderr, "CPXXfreeprob failed, error code %d.\n",
                  status);
      }
   }


   /* Free the CPLEX environment, if necessary */

   if ( env != NULL ) {
      status = CPXXcloseCPLEX (&env);

      /* Note that CPXXcloseCPLEX produces no output, so the only 
         way to see the cause of the error is to use
         CPXXgeterrorstring.  For other CPLEX routines, the errors 
         will be seen if the CPX_PARAM_SCRIND parameter is set to 
         CPX_ON */

      if ( status ) {
         char errmsg[CPXMESSAGEBUFSIZE];
         fprintf (stderr, "Could not close CPLEX environment.\n");
         CPXXgeterrorstring (env, status, errmsg);
         fprintf (stderr, "%s", errmsg);
      }
   }

   if ( logfile != NULL )  CPXXfclose (logfile);
     
   return (status);

} /* END main */


/* This simple routine frees up the pointer *ptr, and sets *ptr 
   to NULL */

static void
free_and_null (char **ptr)
{
   if ( *ptr != NULL ) {
      free (*ptr);
      *ptr = NULL;
   }
} /* END free_and_null */ 


static void
usage (char *progname)
{
   fprintf (stderr,
    "Usage: %s [-r] filename\n", progname);
   fprintf (stderr,
    "  filename   Name of a file, with .mps, .lp, or .sav\n");
   fprintf (stderr,
    "             extension, and a possible, additional .gz\n"); 
   fprintf (stderr,
    "             extension\n");
   fprintf (stderr,
    "  -r         Indicates that callbacks will refer to the\n");
   fprintf (stderr,
    "             presolved model\n");
} /* END usage */


static int CPXPUBLIC 
usersetbranch (CPXCENVptr   env,
               void         *cbdata,
               int          wherefrom,
               void         *userinfo,
               int          brtype,
               CPXDIM       sos,
               int          nodecnt,
               CPXDIM       bdcnt,
               const double *nodeest,
               const CPXDIM *nodebeg,
               const CPXDIM *idx,
               const char   *lu,
               const int    *bd,
               int          *useraction_p)
{
   int status = 0;
   
   int      isfeas;
   CPXDIM   i, j, k, l;
   CPXDIM   besti = -1;
   CPXDIM   bestj = -1;
   CPXDIM   bestk = -1;
   CPXDIM   cols;
   CPXDIM   sossize, numsos;
   double   maxfrac = 0.0;
   double   xj_frac;
   double   objval;
   double   epint;
   double   *x = NULL;

   CPXDIM   bdsz;
   CPXDIM   *varind = NULL;
   char     *varlu  = NULL;
   int      *varbd  = NULL;
   CPXCNT   seqnum;

   CPXCLPptr lp;

   /* Initialize useraction to indicate no user action taken */

   *useraction_p = CPX_CALLBACK_DEFAULT;

   /* If CPLEX is choosing an ordinary branch, take it */

   if ( sos < 0 )  goto TERMINATE;

   /* Get pointer to the problem */

   status = CPXXgetcallbacklp (env, cbdata, wherefrom, &lp);
   if ( status ) {
      fprintf (stdout, "Can't get LP pointer.\n");
      goto TERMINATE;
   }

   cols = CPXXgetnumcols (env, lp);
   if ( cols <= 0 ) {
      fprintf (stdout, "Can't number of columns.\n");
      status = CPXERR_CALLBACK;
      goto TERMINATE;
   }

   /* Get solution values and objective coefficients */

   x = malloc (cols * sizeof (*x));
   if ( x == NULL ) {
      fprintf (stdout, "Out of memory.");
      goto TERMINATE;
   }

   status = CPXXgetcallbacknodex (env, cbdata, wherefrom, x, 0,
                                 cols-1);
   if ( status ) {
      fprintf (stdout, "Can't get node solution.");
      goto TERMINATE;
   }

   status = CPXXgetcallbacknodeobjval (env, cbdata, wherefrom,
                                      &objval);
   if ( status ) {
      fprintf (stdout, "Can't get node objective value.");
      goto TERMINATE;
   }

   status = CPXXgetcallbacksosinfo (env, cbdata, wherefrom, 0, 0,
                                   CPX_CALLBACK_INFO_SOS_NUM,
                                   &numsos);
   if ( status )  goto TERMINATE;

   /* Branch on the set that has the variable closest to 1 */

   status = CPXXgetdblparam (env, CPX_PARAM_EPINT, &epint);

   for (i = 0; i < numsos; i++) {

      /* Check if the set is feasible */ 

      status = CPXXgetcallbacksosinfo (env, cbdata, wherefrom, i, 0,
                                   CPX_CALLBACK_INFO_SOS_IS_FEASIBLE,
                                   &isfeas);
      if ( status )  goto TERMINATE;

      /* Find variable with largest fraction in each infeasible
         set.  Select a set with largest largest fraction for
         branching */

      if ( !isfeas ) {
         status = CPXXgetcallbacksosinfo (env, cbdata, wherefrom, i,
                                         0,
                                         CPX_CALLBACK_INFO_SOS_SIZE,
                                         &sossize);
         if ( status )  goto TERMINATE;

         for (k = 0; k < sossize; k++) {
            status = CPXXgetcallbacksosinfo (env, cbdata, wherefrom,
                                  i, k,
                                  CPX_CALLBACK_INFO_SOS_MEMBER_INDEX,
                                  &j);
            if ( status )  goto TERMINATE;

            xj_frac = x[j] - floor (x[j] + epint);
            if ( xj_frac > maxfrac ) {
               bestj = j;
               bestk = k;
               besti = i;
               maxfrac = xj_frac;
           }
         }
      }
   }

   if ( bestj < 0 ) {
      status = CPX_CALLBACK_DEFAULT;
      goto TERMINATE;
   } 

   /* Now set up node descriptions */

   /* Check that arrays are big enough; if not, return, requesting
      more space */

   status = CPXXgetcallbacksosinfo (env, cbdata, wherefrom, besti, 0,
                                   CPX_CALLBACK_INFO_SOS_SIZE,
                                   &sossize);
   if ( status )  goto TERMINATE;

   bdsz = sossize + 1;

   varind = malloc (bdsz*sizeof(*varind));
   varlu  = malloc (bdsz*sizeof(*varlu));
   varbd  = malloc (bdsz*sizeof(*varbd));
   if ( varind == NULL ||
        varlu   == NULL ||
        varbd   == NULL   ) {
      status = CPXERR_NO_MEMORY;
      goto TERMINATE;
   }
   

   /* Create two nodes, one setting all but the selected variable 
      to 0 and the selected variable to 1, the other setting 
      the selected variable to 0 */

   /* First node */

   for (k = 0, l = 0; k < sossize; k++) {
      status = CPXXgetcallbacksosinfo (env, cbdata, wherefrom, besti,
                                 k,
                                 CPX_CALLBACK_INFO_SOS_MEMBER_INDEX,
                                 &j);
      if ( status )  goto TERMINATE;
      if ( k != bestk ) {
         varind[l] = j;
         varlu[l]  = 'U';
         varbd[l]  = 0;
         l++;
      }
      else {
         varind[l] = j;
         varlu[l]  = 'L';
         varbd[l]  = 1;
         l++;
      }
   }
   status = CPXXbranchcallbackbranchbds (env, cbdata, wherefrom,
                                        objval, l, varind, varlu, varbd,
                                        NULL, &seqnum);
   if ( status )  goto TERMINATE;

   /* Second node */

   varind[0] = bestj;
   varlu[0]  = 'U';
   varbd[0]  = 0;

   status = CPXXbranchcallbackbranchbds (env, cbdata, wherefrom,
                                        objval, 1, varind, varlu, varbd,
                                        NULL, &seqnum);
   if ( status )  goto TERMINATE;

   *useraction_p = CPX_CALLBACK_SET;

TERMINATE:

   free_and_null ((char **) &x);
   free_and_null ((char **) &varind);
   free_and_null ((char **) &varlu);
   free_and_null ((char **) &varbd);

   return (status);

} /* END usersetbranch */
