/*
 * Job.h
 *
 *  Created on: Nov 16, 2012
 *      Author: sebastian
 */

#ifndef JOB_H_
#define JOB_H_




class Job {
	public:
	Job() {};
	Job(double s, double p, double d);
	IloNum s, p, d;
    bool operator<(Job const &other) const {
        return (d < other.d) || (d == other.d && p <= other.p); //sort by single-EDD
    }
	virtual ~Job();
};

// comparison functor -- preferred over overloaded operator<
class CompareByLength : public std::binary_function<Job, Job, bool>
{
public: bool operator()(const Job& a, const Job& b) {
		return (a.p >= b.p);
	}
};

// comparison functor -- preferred over overloaded operator<
class CompareByDuedate : public std::binary_function<Job, Job, bool>
{
public: bool operator()(const Job& a, const Job& b) {
		return (a.d < b.d && a.p <= b.p);
	}
};

#endif /* JOB_H_ */
