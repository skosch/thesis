/*
 * Job.h
 *
 *  Created on: Nov 16, 2012
 *      Author: sebastian
 */

#ifndef JOB_H_
#define JOB_H_

#include <ilcplex/ilocplex.h>
#include <ilconcert/ilocsvreader.h>
#include <ilconcert/iloexpression.h>



class Job {
public:
	Job(double s, double p, double d);
	IloNum s, p, d;
	virtual ~Job();
};


// comparison functor -- preferred over overloaded operator<
class CompareByDuedate : public std::binary_function<Job, Job, bool>
{
public: bool operator()(Job a, Job b) {
		return a.d < b.d;
	}
};

#endif /* JOB_H_ */
