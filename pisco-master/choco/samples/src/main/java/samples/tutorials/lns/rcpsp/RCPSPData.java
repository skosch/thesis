package samples.tutorials.lns.rcpsp;/*
 * Created by IntelliJ IDEA.
 * User: sofdem - sophie.demassey{at}mines-nantes.fr
 * Date: 11/01/11 - 16:49
 */

import choco.kernel.common.util.tools.ArrayUtils;

/**
 * An instance of the Resource-Constrained Proect Scheduling Problem (RCPSP)
 * @author Sophie Demassey
 */
public class RCPSPData {

/** number of real non-preemptive activities (indexed from 0 to nAct-1) */
final int nAct;
/** number of cumulative renewable resources */
final int nRes;
/**
 * simple precedences between activities
 * pred[i][j]=1 iff activity i cannot finish after activity j starts
 * pred[i][j]=0 otherwise
 */
int[][] pred;
/** duration[i] the duration of activity i */
int[] duration;
/** request[i][r] the amount of resource r consumed by activity i between its starting time (included) and its ending time (excluded) */
int[][] request;
/** capacity[r] the amount of resource r available at each moment */
int[] capacity;
/** the latest finishing time of the schedule */
int horizon;

public RCPSPData(int nAct, int nRes)
{
	this.nAct = nAct;
	this.nRes = nRes;
	pred = new int[nAct][nAct];
	duration = new int[nAct];
	request = new int[nAct][nRes];
	capacity = new int[nRes];
	horizon = -1;
}

public void setPrecedence(int actPred, int actSucc)
{
	this.pred[actPred][actSucc] = 1;
}

public void setDuration(int act, int duration)
{
	this.duration[act] = duration;
}

public void setRequest(int act, int res, int request)
{
	this.request[act][res] = request;
}

public void setCapacity(int res, int capacity)
{
	this.capacity[res] = capacity;
}

public int nAct()
{
	return nAct;
}

public int nRes()
{
	return nRes;
}

public boolean isPrecedence(int act1, int act2)
{
	return pred[act1][act2] > 0;
}

public int[] getDurations()
{
	return duration;
}

public int getRequest(int act, int res)
{
	return request[act][res];
}

public int[] getRequests(int res)
{
	return ArrayUtils.transpose(request)[res];
}

public int getCapacity(int res)
{
	return capacity[res];
}

public int getHorizon()
{
	if (horizon < 0) {
		for (int d : duration) { horizon += d; }
	}
	return horizon;
}

@Override
public String toString()
{
	return "RCPSPData{" + "nAct=" + nAct + ", nRes=" + nRes + '}';
}

}
