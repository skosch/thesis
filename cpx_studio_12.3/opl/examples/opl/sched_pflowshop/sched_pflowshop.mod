// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
// (c) Copyright IBM Corporation 1998, 2011. All Rights Reserved.
//
// Note to U.S. Government Users Restricted Rights:
// Use, duplication or disclosure restricted by GSA ADP Schedule
// Contract with IBM Corp.
// --------------------------------------------------------------------------

using CP;

int nbJobs = ...;
int nbMchs = ...;

range Jobs = 0..nbJobs-1;
range Mchs = 0..nbMchs-1; 

int OpDurations[j in Jobs][m in Mchs] = ...;

dvar interval itvs[j in Jobs][m in Mchs] size OpDurations[j][m];
dvar sequence mchs[m in Mchs] in all(j in Jobs) itvs[j][m] types all(j in Jobs) j;

execute {
  cp.param.FailLimit = 10000;
  }

minimize max(j in Jobs) endOf(itvs[j][nbMchs-1]);
subject to {
  forall (m in Mchs)
    noOverlap(mchs[m]);
  forall (j in Jobs, o in 0..nbMchs-2)
    endBeforeStart(itvs[j][o], itvs[j][o+1]);
  forall (m in Mchs, j in Jobs: 0<m) 
    typeOfPrev(mchs[m],itvs[j][m],nbJobs)==typeOfPrev(mchs[0],itvs[j][0],nbJobs);
}

execute {
  for (var j = 0; j <= nbJobs-1; j++) {
    for (var o = 0; o <= nbMchs-1; o++) {
      write(itvs[j][o].start + " ");
    }
    writeln("");
  }
}
