/*
 * job.cpp
 *
 *  Created on: Nov 16, 2012
 *      Author: sebastian
 */

#include "Job.h"

using namespace std;

Job::Job(double s, double p, double d) {
	this->s = s; this->p = p; this->d = d;
}

Job::~Job() {
	// nothing to do here, just for the STL stuff
}
