/* --------------------------------------------------------------------------
 * File: xadmipex5.c
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

/* Example xadmipex5.c solves the MIPLIB 3.0 model noswot.mps by adding
   user cuts via a user cut callback during the branch-and-cut process.

   Then it modifies the problem by sepcifying a lazy constraint generator that
   tests against one single cut that the original optimal solution does not
   satisfy.

   Finally the modified problem is solved again, this time without user cuts
   (but the lazy constraint generator is still active).

   This example uses cplexx.h

   To run this example, no command line arguments are required.
   This program reads a problem from a file named "noswot.mps" */

/* Bring in the CPLEX function declarations and the C library 
   header file stdio.h with the following single include */

#include <ilcplex/cplexx.h>

/* Bring in the declarations for the string and character functions, 
   malloc, and fabs */

#include <ctype.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>

/* The following structure will hold the information we need to 
   pass to the cut callback function */

struct cutinfo {
   CPXLPptr lp;
   CPXDIM   numcols;
   CPXDIM   num;
   double   *x;
   CPXNNZ   *beg;
   CPXDIM   *ind; 
   double   *val;
   double   *rhs;
};
typedef struct cutinfo CUTINFO, *CUTINFOptr;

/* Declarations for functions in this program */

static int
   myoptimize         (CPXENVptr env, CPXLPptr lp, double true_objval);

static int CPXPUBLIC
   mycutcallback      (CPXCENVptr env, void *cbdata, int wherefrom,
                       void *cbhandle, int *useraction_p);

static int
   makeusercuts       (CPXENVptr env, CPXLPptr lp, CUTINFOptr cutinfo);

static int
   makelazyconstraint (CPXENVptr env, CPXLPptr lp, CUTINFOptr cutinfo);

static void
   free_and_null      (char **ptr);


int
main ()
{
   int status = 0;

   /* Declare and allocate space for the variables and arrays where
      we will store the optimization results, including the status, 
      objective value, and variable values */
   
   CPXENVptr env = NULL;
   CPXLPptr  lp = NULL;

   CUTINFO usercutinfo;
   CUTINFO lazyconinfo;

   usercutinfo.x   = NULL;
   usercutinfo.beg = NULL;
   usercutinfo.ind = NULL; 
   usercutinfo.val = NULL;
   usercutinfo.rhs = NULL;

   lazyconinfo.x   = NULL;
   lazyconinfo.beg = NULL;
   lazyconinfo.ind = NULL; 
   lazyconinfo.val = NULL;
   lazyconinfo.rhs = NULL;

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
   CPXXsetcntparam (env, CPX_PARAM_MIPINTERVAL, 1000);

   /* The problem will be solved several times, so turn off advanced start */
   
   status = CPXXsetintparam (env, CPX_PARAM_ADVIND, CPX_OFF);
   if ( status )  goto TERMINATE;

   /* Create the problem, using the filename as the problem name */

   lp = CPXXcreateprob (env, &status, "noswot");

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

   status = CPXXreadcopyprob (env, lp, "../../data/noswot.mps", NULL);
   if ( status ) {
      fprintf (stderr,
               "Failed to read and copy the problem data.\n");
      goto TERMINATE;
   }

   /* Set parameters */

   /* Assure linear mappings between the presolved and original
      models */

   status = CPXXsetintparam (env, CPX_PARAM_PRELINEAR, 0);
   if ( status )  goto TERMINATE;


   /* Turn on traditional search for use with control callbacks */

   status = CPXXsetintparam (env, CPX_PARAM_MIPSEARCH, CPX_MIPSEARCH_TRADITIONAL);
   if ( status )  goto TERMINATE;

   /* Let MIP callbacks work on the original model */

   status = CPXXsetintparam (env, CPX_PARAM_MIPCBREDLP, CPX_OFF);
   if ( status )  goto TERMINATE;

   /* Create user cuts for noswot problem */

   status = makeusercuts (env, lp, &usercutinfo);
   if ( status )  goto TERMINATE;

   /* Set up to use MIP usercut callback */

   status = CPXXsetusercutcallbackfunc (env, mycutcallback, &usercutinfo);
   if ( status )  goto TERMINATE;

   status = myoptimize (env, lp, -41.0);
   if ( status )  goto TERMINATE;

   /*=======================================================================*/

   /* Create a lazy constraint to alter the optimum */

   status = makelazyconstraint (env, lp, &lazyconinfo);
   if ( status )  goto TERMINATE;

   /* Set up to use MIP lazyconstraint callback. The callback funtion
    * registered is the same, but the data will be different. */

   status = CPXXsetlazyconstraintcallbackfunc (env, mycutcallback, &lazyconinfo);
   if ( status )  goto TERMINATE;

   status = myoptimize (env, lp, -39.0);
   if ( status )  goto TERMINATE;

   /*=======================================================================*/

   /* Now solve the problem without usercut callback */
   
   status = CPXXsetusercutcallbackfunc (env, NULL, NULL);
   if ( status )  goto TERMINATE;

   status = myoptimize (env, lp, -39.0);
   if ( status )  goto TERMINATE;

   /*=======================================================================*/


TERMINATE:

   /* Free the allocated vectors */

   free_and_null ((char **) &usercutinfo.x);
   free_and_null ((char **) &usercutinfo.beg);
   free_and_null ((char **) &usercutinfo.ind);
   free_and_null ((char **) &usercutinfo.val);
   free_and_null ((char **) &usercutinfo.rhs);
   free_and_null ((char **) &lazyconinfo.x);
   free_and_null ((char **) &lazyconinfo.beg);
   free_and_null ((char **) &lazyconinfo.ind);
   free_and_null ((char **) &lazyconinfo.val);
   free_and_null ((char **) &lazyconinfo.rhs);

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
     
   return (status);

} /* END main */

int
myoptimize (CPXENVptr env, CPXLPptr lp, double true_objval)
{
   int status = 0;
   int solstat = 0;

   CPXDIM j;
   double objval;
   double *x = NULL;
   CPXDIM cur_numcols = CPXXgetnumcols (env, lp);

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
         char *colname[1];
         char namestore[6];
         CPXSIZE surplus = 0;
         status = CPXXgetcolname (env, lp, colname, namestore, 6,
                                 &surplus, j, j);
         if ( status ) {
            namestore[0] = 0;
            colname[0] = namestore;
         }
         printf ( "Column %3d (%5s):  Value = %17.10g\n", j, colname[0], x[j]);
      }
   }


TERMINATE:

   free_and_null ((char **) &x);

   return (status);
}

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


static int CPXPUBLIC 
mycutcallback (CPXCENVptr env,
               void       *cbdata,
               int        wherefrom,
               void       *cbhandle,
               int        *useraction_p)
{
   int status = 0;

   CUTINFOptr cutinfo = (CUTINFOptr) cbhandle;

   CPXDIM   numcols  = cutinfo->numcols;
   CPXDIM   numcuts  = cutinfo->num;
   double   *x       = cutinfo->x;
   CPXNNZ   *beg     = cutinfo->beg;
   CPXDIM   *ind     = cutinfo->ind;
   double   *val     = cutinfo->val;
   double   *rhs     = cutinfo->rhs;
   CPXDIM   *cutind  = NULL;
   double   *cutval  = NULL;
   double   cutvio;
   int      addcuts = 0;
   CPXDIM   i, j;
   CPXNNZ   k;
   CPXDIM   cutnz;

   *useraction_p = CPX_CALLBACK_DEFAULT; 

   status = CPXXgetcallbacknodex (env, cbdata, wherefrom, x,
                                 0, numcols-1); 
   if ( status ) {
      fprintf(stderr, "Failed to get node solution.\n");
      goto TERMINATE;
   }

   for (i = 0; i < numcuts; i++) {
      cutvio = -rhs[i];
      k = beg[i];
      cutnz = beg[i+1] - k;
      cutind = ind + k;
      cutval = val + k;
      for (j = 0; j < cutnz; j++) {
         cutvio += x[cutind[j]] * cutval[j];
      }

      /* Use a cut violation tolerance of 0.01 */

      if ( cutvio > 0.01 ) { 
         status = CPXXcutcallbackadd (env, cbdata, wherefrom,
                                     cutnz, rhs[i], 'L',
                                     cutind, cutval, 1);
         if ( status ) {
            fprintf (stderr, "Failed to add cut.\n");
            goto TERMINATE;
         }
         addcuts++;
      }
   }

   /* Tell CPLEX that cuts have been created */ 
   if ( addcuts > 0 ) {
      *useraction_p = CPX_CALLBACK_SET; 
   }

TERMINATE:

   return (status);

} /* END mycutcallback */


/* Valid cuts for noswot 
   cut1: X21 - X22 <= 0
   cut2: X22 - X23 <= 0
   cut3: X23 - X24 <= 0
   cut4: 2.08 X11 + 2.98 X21 + 3.47 X31 + 2.24 X41 + 2.08 X51 
         + 0.25 W11 + 0.25 W21 + 0.25 W31 + 0.25 W41 + 0.25 W51
         <= 20.25
   cut5: 2.08 X12 + 2.98 X22 + 3.47 X32 + 2.24 X42 + 2.08 X52
         + 0.25 W12 + 0.25 W22 + 0.25 W32 + 0.25 W42 + 0.25 W52
         <= 20.25
   cut6: 2.08 X13 + 2.98 X23 + 3.4722 X33 + 2.24 X43 + 2.08 X53
         + 0.25 W13 + 0.25 W23 + 0.25 W33 + 0.25 W43 + 0.25 W53
         <= 20.25
   cut7: 2.08 X14 + 2.98 X24 + 3.47 X34 + 2.24 X44 + 2.08 X54
         + 0.25 W14 + 0.25 W24 + 0.25 W34 + 0.25 W44 + 0.25 W54
         <= 20.25
   cut8: 2.08 X15 + 2.98 X25 + 3.47 X35 + 2.24 X45 + 2.08 X55
         + 0.25 W15 + 0.25 W25 + 0.25 W35 + 0.25 W45 + 0.25 W55
         <= 16.25
*/

static int
makeusercuts (CPXENVptr  env,
              CPXLPptr   lp,
              CUTINFOptr usercutinfo)
{
   int status = 0;

   CPXNNZ beg[] = {0, 2, 4, 6, 16, 26, 36, 46, 56};

   double val[] = 
   {1, -1, 
    1, -1, 
    1, -1, 
    2.08, 2.98, 3.47, 2.24, 2.08, 0.25, 0.25, 0.25, 0.25, 0.25,
    2.08, 2.98, 3.47, 2.24, 2.08, 0.25, 0.25, 0.25, 0.25, 0.25,
    2.08, 2.98, 3.47, 2.24, 2.08, 0.25, 0.25, 0.25, 0.25, 0.25,
    2.08, 2.98, 3.47, 2.24, 2.08, 0.25, 0.25, 0.25, 0.25, 0.25,
    2.08, 2.98, 3.47, 2.24, 2.08, 0.25, 0.25, 0.25, 0.25, 0.25};

   char const *varname[] = 
   {"X21", "X22", 
    "X22", "X23", 
    "X23", "X24",
    "X11", "X21", "X31", "X41", "X51",
    "W11", "W21", "W31", "W41", "W51",
    "X12", "X22", "X32", "X42", "X52",
    "W12", "W22", "W32", "W42", "W52",
    "X13", "X23", "X33", "X43", "X53",
    "W13", "W23", "W33", "W43", "W53",
    "X14", "X24", "X34", "X44", "X54",
    "W14", "W24", "W34", "W44", "W54",
    "X15", "X25", "X35", "X45", "X55",
    "W15", "W25", "W35", "W45", "W55"};

   double rhs[] = {0, 0, 0, 20.25, 20.25, 20.25, 20.25, 16.25};

   CPXNNZ *cutbeg = NULL;
   CPXDIM *cutind = NULL;
   double *cutval = NULL;
   double *cutrhs = NULL; 

   CPXDIM i, varind;
   CPXNNZ nz   = 56;
   CPXDIM cuts = 8;

   CPXDIM cur_numcols = CPXXgetnumcols (env, lp);

   usercutinfo->lp = lp;
   usercutinfo->numcols = cur_numcols;

   usercutinfo->x = malloc (cur_numcols * sizeof (*usercutinfo->x));
   if ( usercutinfo->x == NULL ) {
      fprintf (stderr, "No memory for solution values.\n");
      goto TERMINATE;
   }

   cutbeg = malloc ((cuts+1) * sizeof (*cutbeg));
   cutind = malloc (nz * sizeof (*cutind));
   cutval = malloc (nz * sizeof (*cutval));
   cutrhs = malloc (cuts * sizeof (*cutrhs));

   if ( cutbeg == NULL ||
        cutind == NULL ||
        cutval == NULL ||
        cutrhs == NULL   ) {
      fprintf (stderr, "No memory.\n");
      status = CPXERR_NO_MEMORY;
      goto TERMINATE;
   } 
      
   for (i = 0; i < nz; i++) {
      status = CPXXgetcolindex (env, lp, varname[i], &varind);
      if ( status )  {
         fprintf (stderr,
                  "Failed to get index from variable name.\n");
         goto TERMINATE;
      }
      cutind[i] = varind;
      cutval[i] = val[i];
   }

   for (i = 0; i < cuts; i++) {
      cutbeg[i] = beg[i];
      cutrhs[i] = rhs[i];
   }
   cutbeg[cuts] = beg[cuts];

   usercutinfo->num      = cuts;
   usercutinfo->beg      = cutbeg;
   usercutinfo->ind      = cutind;
   usercutinfo->val      = cutval;
   usercutinfo->rhs      = cutrhs;

TERMINATE:

   if ( status ) {
      free_and_null ((char **) &cutbeg);
      free_and_null ((char **) &cutind);
      free_and_null ((char **) &cutval);
      free_and_null ((char **) &cutrhs);
   }
 
   return (status);

} /* END makeusercuts */

/* A constraint that cuts off the optimal solution of noswot:
   W11 + W12 + W13 + W14 + W15 <= 3
 */

static int
makelazyconstraint (CPXENVptr  env,
                    CPXLPptr   lp,
                    CUTINFOptr lazyconinfo)
{
   int status = 0;

   CPXNNZ beg[] = {0, 5};
   double val[] = {1, 1, 1, 1, 1};
   char const *varname[] = { "W11", "W12", "W13", "W14", "W15" };
   double rhs[] = {3};

   CPXNNZ *cutbeg = NULL;
   CPXDIM *cutind = NULL;
   double *cutval = NULL;
   double *cutrhs = NULL; 

   CPXDIM i, varind;
   CPXNNZ nz   = 5;
   CPXDIM cuts = 1;

   CPXDIM cur_numcols = CPXXgetnumcols (env, lp);

   lazyconinfo->lp = lp;
   lazyconinfo->numcols = cur_numcols;

   lazyconinfo->x = malloc (cur_numcols * sizeof (*lazyconinfo->x));
   if ( lazyconinfo->x == NULL ) {
      fprintf (stderr, "No memory for solution values.\n");
      goto TERMINATE;
   }

   cutbeg = malloc ((cuts+1) * sizeof (*cutbeg));
   cutind = malloc (nz * sizeof (*cutind));
   cutval = malloc (nz * sizeof (*cutval));
   cutrhs = malloc (cuts * sizeof (*cutrhs));

   if ( cutbeg == NULL ||
        cutind == NULL ||
        cutval == NULL ||
        cutrhs == NULL   ) {
      fprintf (stderr, "No memory.\n");
      status = CPXERR_NO_MEMORY;
      goto TERMINATE;
   } 
      
   for (i = 0; i < nz; i++) {
      status = CPXXgetcolindex (env, lp, varname[i], &varind);
      if ( status )  {
         fprintf (stderr,
                  "Failed to get index from variable name.\n");
         goto TERMINATE;
      }
      cutind[i] = varind;
      cutval[i] = val[i];
   }

   for (i = 0; i < cuts; i++) {
      cutbeg[i] = beg[i];
      cutrhs[i] = rhs[i];
   }
   cutbeg[cuts] = beg[cuts];

   lazyconinfo->num      = cuts;
   lazyconinfo->beg      = cutbeg;
   lazyconinfo->ind      = cutind;
   lazyconinfo->val      = cutval;
   lazyconinfo->rhs      = cutrhs;

TERMINATE:

   if ( status ) {
      free_and_null ((char **) &cutbeg);
      free_and_null ((char **) &cutind);
      free_and_null ((char **) &cutval);
      free_and_null ((char **) &cutrhs);
   }
 
   return (status);

} /* END makelazyconstraint */

