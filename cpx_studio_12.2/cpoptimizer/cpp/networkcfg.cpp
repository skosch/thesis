// -------------------------------------------------------------- -*- C++ -*-
// File: ./examples/src/cpp/networkcfg.cpp
// --------------------------------------------------------------------------
// Licensed Materials - Property of IBM
//
// 5724-Y48 5724-Y49 5724-Y54 5724-Y55 5725-A06 5725-A29
// Copyright IBM Corporation 1990, 2010. All Rights Reserved.
//
// Note to U.S. Government Users Restricted Rights:
// Use, duplication or disclosure restricted by GSA ADP Schedule
// Contract with IBM Corp.
// --------------------------------------------------------------------------

/* ------------------------------------------------------------

Problem Description
-------------------
The problem is to configure a network such that:
- a node is either a supplier or a normal node
- k suppliers are needed
- an arc between nodes i and j can exist only when exactly one of i or j is a supplier
- the network is connected
- the network need to respect an upper bound on the distances between some nodes
- the network must have exactly numArcs arcs

Only some arcs are possible and the cost associated to each arc are known.
Some additional constraints have the form "arc1 or arc2", and force at least one arc to 
be present in the solution.

------------------------------------------------------------ */

#include <ilcp/cp.h>
#include <ilcp/cpext.h>

#define costRescalingRatio 6400
#define numSkipped 500
#define AUTO 0
#define MANUAL 1
#define CONSTRAINT 2

typedef IloArray<IloIntVarArray> IloIntVarArray2;

class FileError: public IloException {
public:
  FileError() : IloException("Cannot open data file") {}
};


//------ A custom inferencer for maintaining the distance constraint -----------

// A custom inferencer is a subclass of IlcCustomInferencerI

class IlcMinDistanceInferencerI: public IlcCustomInferencerI {
  //ensure that the graph given by aa satisfies for all i,j dist[i][j] = min distance between i and j
 
  IloIntVarArray2 a;
  IloIntVarArray2 dist;
  IloCP cp;
  IloIntArray2 d;
  IloInt n;
  IloIntArray2 alreadyChecked;
public:
  IlcMinDistanceInferencerI(IloCP solver, IloIntVarArray2 aa, IloIntVarArray2 ddist, 
                            IloBool manual, IloInt skipped): 
      IlcCustomInferencerI(solver, manual, skipped), a(aa), dist(ddist), cp(solver) {
    n = aa.getSize();
    d = IloIntArray2(cp.getReversibleAllocator(), n);
    for (IloInt i = 0; i < n; i++) {
      d[i]= IloIntArray(cp.getReversibleAllocator(), n);
    }
    alreadyChecked = IloIntArray2(cp.getReversibleAllocator(), n);
    for (IloInt i = 0; i < n; i++) {
      alreadyChecked[i]= IloIntArray(cp.getReversibleAllocator(), n);
    }
  }
  ~IlcMinDistanceInferencerI(){};
  void initialiseDistance() {
    for (IloInt i = 0; i < n; i++) {
      for (IloInt j = 0; j < n; j++) {
        if (i==j) {
          d[i][j]= 0;
        } else if (cp.getMax(a[i][j])==0) {
          d[i][j]= n+1;
        } else {
          d[i][j]= 1;
        }
      }
    }
  }

  void computeDistance() {
    for (IloInt k = 0; k < n; k++)
      for (IloInt i = 0; i < n; i++)
        for (IloInt j = 0; j < n; j++)
          d[i][j]=IloMin(d[i][j], d[i][k]+d[k][j]);
  }
  void constrainDistance() {
    for (IloInt i = 0; i < n; i++) {
      for (IloInt j = 0; j < n; j++)  {
        cp.getIntVar(dist[i][j]).setMin(d[i][j]);
      }
    }
  }
  IloBool isCompatibleDistance() {
    for (IloInt i = 0; i < n; i++) 
      for (IloInt j = 0; j < n; j++) 
        if (d[i][j] > cp.getIntVar(dist[i][j]).getMax())
          return IloFalse;
    return IloTrue;
  }

  virtual void execute() {
    initialiseDistance();
    computeDistance();
    constrainDistance();
    // try to remove each arc and force those arcs that lead to a distance greater than 
    // the max allowed to be present
    for (IloInt t = 0; t < n; t++) {
      for (IloInt v = 0; v < n; v++) {
        if (v!=t && cp.getMin(a[t][v])==0 && cp.getMax(a[t][v])==1) {
          initialiseDistance();
          d[t][v]=n+1;
          d[v][t]=n+1;
          computeDistance();
          // the CPU time spent in the execute() must be told to the solver. 
          // This can be done thanks to the addPropagationCost() function.
          addPropagationCost(n*n*n/costRescalingRatio);
          if (!isCompatibleDistance()) {
            cp.getIntVar(a[t][v]).setValue(1);
          }
        }
      }
    }
  }

  // This function is used in the automatic mode to estimate the cost of a call to execute()
  // The unity in which the cost is expressed should roughly correspond to the cost of an average propagation.
  // Here, it is given by the constant costRescalingRatio. Indeed, this constant allows the automatic
  // mode to be tuned by trying different values for the constant.
  virtual IloNum estimateCost(IloNum bound){
    IloNum c=n*n*n / costRescalingRatio;
    if (c >= bound)
      return bound;
    for (IloInt t = 0; t < n; t++) {
      for (IloInt v = 0; v < n; v++) {
        if (v!=t && cp.getMin(a[t][v])==0 && cp.getMax(a[t][v])==1) {
          c += 1+ n*n*n / costRescalingRatio;
          if (c >= bound)
            return bound;
        }
      }
    }
    return c;
  }
};

// The function that returns the custom inferencer in a handle class
IlcCustomInferencer IlcMinDistanceInferencer(IloCP cp, IloIntVarArray2 a, IloIntVarArray2 dist, 
                                             IloBool manual, IloInt skipped){
  return new (cp.getHeap()) IlcMinDistanceInferencerI(cp, a, dist, manual, skipped);
}

// The custom inferencer is an object of the solver level, i.e. an "Ilc" object.
// Its representation in the model is an IloConstraint. 
// We use the same macro for building an IloConstraint from an IlcCustomInferencer or from an IlcConstraint.
ILOCPCONSTRAINTWRAPPER4(IloMyDistanceConstraint, cp, IloIntVarArray2, a, 
                        IloIntVarArray2, dist, IloBool, manual, IloInt, skipped) {
  for (IloInt i = 0; i < a.getSize(); i++) {
    use(cp, a[i]);
  }
  for (IloInt i = 0; i < dist.getSize(); i++) {
    use(cp, dist[i]);
  }
  return IlcMinDistanceInferencer(cp, a, dist, manual, skipped);
}


//------------- Maintaining distance constraint with a real constraint --------------

// this is simply a constraint that is pushed and that calls the execute() function of
// the above custom inferencer

class IlcMinDistanceConstraintI : public IlcConstraintI {
protected:
  IloIntVarArray2 a;
  IloIntVarArray2 dist;
  IlcCustomInferencer custInf;
 public:
  IlcMinDistanceConstraintI(IloCP cp, IloIntVarArray2 aa, IloIntVarArray2 ddist)
    : IlcConstraintI(cp), a(aa),dist(ddist) {
      custInf = IlcMinDistanceInferencer(cp, a, dist, IloTrue, 0);
  }
  ~IlcMinDistanceConstraintI() {}
  virtual void post();
  virtual void propagate();
  void varDemon();
};

ILCCTDEMON0(DistanceDemon, IlcMinDistanceConstraintI, varDemon)

void IlcMinDistanceConstraintI::post () {
  IlcInt i,j;
  for (i = 0; i < a.getSize(); i++)
    for (j = 0; j < a[i].getSize(); j++)
      getCP().getIntVar(a[i][j]).whenValue(DistanceDemon(getCP(), this));
  for (i = 0; i < dist.getSize(); i++)
    for (j = 0; j < dist[i].getSize(); j++)
      getCP().getIntVar(dist[i][j]).whenRange(DistanceDemon(getCP(), this));
}
void IlcMinDistanceConstraintI::propagate () {
  custInf.getImpl()->execute();
}
void IlcMinDistanceConstraintI::varDemon () {
  push();
}
IlcConstraint IlcMinDistanceConstraint(IloCP cp, IloIntVarArray2 a, IloIntVarArray2 dist){
  return new (cp.getHeap()) IlcMinDistanceConstraintI(cp, a, dist);
}

ILOCPCONSTRAINTWRAPPER2(IloMinDistanceConstraint, cp, IloIntVarArray2, a, IloIntVarArray2, dist) {
  for (IloInt i = 0; i < a.getSize(); i++) {
    use(cp, a[i]);
  }
  for (IloInt i = 0; i < dist.getSize(); i++) {
    use(cp, dist[i]);
  }
  return IlcMinDistanceConstraint(cp, a, dist);
}



//-------------------------- Model ---------------------------------------

void networkOptim(const char* filename, IloInt mode){
  IloEnv env;
  std::ifstream file(filename);
  if (!file){
    env.out() << "file not found" << std::endl;
    throw FileError();
  }

  IloIntArray2 possibleArcs;
  IloIntArray2 cost;
  IloIntArray2 maxDistances;
  IloIntArray2 additionalConstraints;
  IloInt n; 
  IloInt k; // number of suppliers
  IloInt numArcs; //number of arcs in the solution
  IloInt nbPossibleArcs;
  IloInt nbMaxDistances;
  IloInt nbAdditionalConstraints;

  file >> n;
  file >> k;
  file >> numArcs;
  file >> nbPossibleArcs;
  possibleArcs = IloIntArray2(env, n);
  for (IloInt i = 0; i < n; i++) {
    possibleArcs[i] = IloIntArray(env, n);
    for (IloInt j = 0; j < n; j++) {
      possibleArcs[i][j]=0;
    }
    possibleArcs[i][i]=1;
  }
  cost = IloIntArray2(env, n);
  for (IloInt i = 0; i < n; i++) {
    cost[i] = IloIntArray(env, n);
    for (IloInt j = 0; j < n; j++) {
      cost[i][j]=IloIntMax;
    }
  }
  for(IloInt i = 0; i < nbPossibleArcs; i++){
    IloInt a, b, c;
    file >> a;  
    file >> b;
    file >> c;
    possibleArcs[a][b]=1;
    possibleArcs[b][a]=1;
    cost[a][b]=c;
    cost[b][a]=c;
  }
  file >> nbMaxDistances;
  maxDistances = IloIntArray2(env, n);
  for (IloInt i = 0; i < n; i++) {
    maxDistances[i] = IloIntArray(env, n);
    for (IloInt j = 0; j < n; j++) {
      maxDistances[i][j]=n-1;
    }
  }
  for(IloInt i = 0; i < nbMaxDistances; i++){
    IloInt a, b, c;
    file >> a;  
    file >> b;
    file >> c;
    maxDistances[a][b]=c;
    maxDistances[b][a]=c;
  }

  file >> nbAdditionalConstraints;
  additionalConstraints = IloIntArray2(env, nbAdditionalConstraints);
  for (IloInt i = 0; i < nbAdditionalConstraints; i++) {
    additionalConstraints[i] = IloIntArray(env, 4);
    file>>additionalConstraints[i][0];
    file>>additionalConstraints[i][1];
    file>>additionalConstraints[i][2];
    file>>additionalConstraints[i][3];
  }

  try {
    IloModel mdl(env);
    IloIntVarArray2 a(env, n);
    for (IloInt i = 0; i < n; i++) {
      a[i]  = IloIntVarArray(env);
      for (IloInt j=0; j<n; j++) {
        a[i].add(IloIntVar(env,0,possibleArcs[i][j]));
      }
    }

    IloIntVarArray2 dist(env, n);
    for (IloInt i = 0; i < n; i++) {
      dist[i]  = IloIntVarArray(env);
      for (IloInt j=0; j<n; j++) {
        if (i==j)
          dist[i].add(IloIntVar(env,0,0));
        else
          dist[i].add(IloIntVar(env,0,maxDistances[i][j]));
        mdl.add(dist[i][j]);
      }
    }

    IloIntVarArray supplier(env, n,0,1);

    // undirected graph
    for (IloInt i = 0; i < n-1; i++) {
      for (IloInt j = i+1; j < n; j++) {
        mdl.add(a[i][j]==a[j][i]);  
      }
    }
    for (IloInt i = 0; i < n; i++) {
      mdl.add(a[i][i]==1);  
    }

    // at least one connection for each node
    IloIntVarArray nbNeighbors(env,n,1, n-1);
    for (IloInt i = 0; i < n; i++) {
      mdl.add(nbNeighbors[i]==IloSum(a[i])-1); //a[i][i] is = 1
    }

    // exactly one supplier in each connection
    for (IloInt i = 0; i < n; i++) {
      for (IloInt j = 0; j < n; j++) {
        if (i!=j)
          mdl.add(a[i][j] <= (supplier[i]!=supplier[j]));  
      }
    }

    // Exactly k suppliers
    mdl.add(IloSum(supplier)==k);

    // a redundant constraint can be infered from the problem. As in each arc, one node is a supplier,
    // a simple path have at least k supliers and thus 2*k arcs. Moreover, when one (resp. two) edge(s) 
    // of the path is a supplier, its length is at most 2*k-1 (resp. 2*k-2)
    for (IloInt i = 0; i < n; i++) {
      for (IloInt j = 0; j < n; j++) {
        if (i!=j) {
          mdl.add(dist[i][j] <= 2*k);
          mdl.add( (supplier[i]==0 && supplier[j] ==0) || dist[i][j] <=2*k-1);
          mdl.add( supplier[i]==0 || supplier[j] ==0 || dist[i][j] <=2*k-2);
        }
      }
    }

    //exactly numArcs undirected arcs
    mdl.add(IloSum(nbNeighbors) == numArcs); 

    // Three different ways for ensuring the properties on distances:
    // 1: a constraint, 
    if (mode==CONSTRAINT)
      mdl.add(IloMinDistanceConstraint(env,a,dist));
    else if (mode==MANUAL)
      // 2: a custom inferencer in a manual mode specifying the number
      // of nodes to explore between two invocation of the custom inferencer   
      mdl.add(IloMyDistanceConstraint(env,a,dist, IloTrue, numSkipped));
    else // mode==AUTO
      // 3: a custom inferencer in automatic mode
      mdl.add(IloMyDistanceConstraint(env,a,dist, IloFalse, 0));

    //Additional constraints
    for (IloInt i = 0; i < nbAdditionalConstraints; i++) {
      mdl.add(
        a[additionalConstraints[i][0]][additionalConstraints[i][1]]==1 || 
        a[additionalConstraints[i][2]][additionalConstraints[i][3]]==1);
    }
 
    // The objective is to minimize the cost of arcs.
    IloExpr objExp = IloIntExpr(env,0);
    for (IloInt i = 0; i < n; i++) {
      for (IloInt j = i+1; j < n; j++) {
        objExp+= a[i][j]*cost[i][j];
      }
    }
    IloObjective obj = IloMinimize(env, objExp);
    mdl.add(obj);

    IloCP cp(mdl);
    cp.setParameter(IloCP::TimeLimit, 20); 

    IloIntVarArray decisionVars(env);
    for (IloInt i = 0; i < n; i++) {
      decisionVars.add(a[i]);
      decisionVars.add(supplier[i]);
    }
    IloSearchPhase userGoal(0);
    // There is no need to assign the variables representing the distances
    userGoal = IloSearchPhase(env, decisionVars);
    if (cp.solve(userGoal)) {
      cp.out() << "Cost= "<<cp.getValue(objExp) << std::endl;
      cp.out() << "Suppliers: ";
      for (IloInt i =0; i < n; i++) {
        if (cp.getValue(supplier[i])==1)
          cp.out() << i<< " ";
      }
      cp.out()  << std::endl << "Arcs: ";
      for (IloInt i = 0; i < n-1; i++) {    
        for (IloInt j = i+1; j < n; j++) {
          if (cp.getValue(a[i][j])==1)
            cp.out() << "("<<i<<" "<<j<<") ";
        }
      }
      cp.out() << std::endl;
    } else {
      cp.out() << "No solution found" << std::endl;
    }
    cp.endSearch();
  }
  catch (IloException& ex) {
    env.out() << "Error: " << ex << std::endl;
  }
  env.end();
}

int main(int argc, const char* argv[]){
  IloInt mode= AUTO;
  if (argc > 1)
    mode = atoi(argv[1]);
  const char* filename = "../../../examples/data/networkcfg.data";
  if (argc > 2)
    filename = argv[2];
  networkOptim(filename, mode);
  return 0;
}
