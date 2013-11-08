#include <iostream>
#include <sstream>
#include <cmath>
#include <string>
#include <cstdlib>
#include <algorithm>
#include <vector>
#include <deque>

#include "Job.h"
#include "RootNode.h"
#include "BBNode.h"

using namespace std;



vector<Job> getSortedJobs(IloEnv env, string datafilename, int nj) {
	stringstream filename;
	if(datafilename.length() > 0) {
		filename << datafilename;
	} else {
		filename << "../../data/data_" << nj;
	}
	cout << "Loading file: " << filename.str() << endl;
	IloCsvReader csvr(env, filename.str().c_str());

    if(csvr.getNumberOfItems()-1 != nj) {
      cout << "Error: Expected " << nj << " jobs, but found " << csvr.getNumberOfItems() << " in CSV file. Aborting.\n";
      exit(0);
    }

    vector<Job> jc; // this structure is necessary to sort the jobs.

    IloCsvLine curLine;
    for(int j=0; j<nj; j++) {
      curLine = csvr.getLineByNumber(j+1);

      Job curLineJob((IloNum) curLine.getIntByPosition(0), (IloNum) curLine.getIntByPosition(1), (IloNum) curLine.getIntByPosition(2));
      jc.push_back(curLineJob);
    }

    // sort the jobs by non-decreasing due date
    sort(jc.begin(), jc.end(), CompareByDuedate());
    return jc;
}

int main(int argc, char *argv[]) {

	// define Ilog stuff

	IloEnv env;

	// read in job lists
	int nj, nk;
	string datafilename;

	int capacity = 20;
	int Dmax = 2000;

    if(argc >= 2) {
      nj = atoi(argv[1]);
      nk = nj;
    } else {
      nj = 10;
    }



    if(argc >= 3) {
      datafilename = argv[2];
    }

    vector<Job> jc = getSortedJobs(env, datafilename, nj);


	// create root node
	RootNode root(&jc, capacity, Dmax);

	// let root node create children
	root.run();

	cout << "We're done here." << endl;
	return 0;
}
