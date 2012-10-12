function [x,fval,exitflag,output]=cplexmiqcp(H, f, Aineq, bineq, Aeq, beq, l, Q, r, sostype, sosind, soswt, lb, ub, ctype, x0, options)
%%
% cplexmiqcp
% Solve quadratically constrained linear or quadratic integer programming
% problems.
%
% x = cplexmiqcp(H,f,Aineq,bineq) solves the mixed integer programming
% problem min 1/2*x'*H*x + f*x subject to Aineq*x <= bineq. If no
% quadratic objective term exists, set H=[].
%
% x = cplexmiqcp(H,f,Aineq,bineq,Aeq,beq) solves the preceding problem
% while additionally satisfying the equality constraints Aeq*x = beq. If no
% inequalities exist, set Aineq=[] and bineq=[].
%
% x = cplexmiqcp(H,f,Aineq,bineq,Aeq,beq,l,Q,r) solves the preceding
% problem while additionally satisfying the quadratic inequality
% constraints l*x + x'*Q*x <= r. If no equalities exist, set Aeq=[] and beq=[].
%
% x = cplexmiqcp(f,Aineq,bineq,Aeq,beq,l,Q,r,sostype,sosind,soswt) solves
% the preceding problem with the additional requirement that the SOS
% constraints are satisfied. If no quadratic inequalities exist, set l=[],
% Q=[] and r=[].
%
% x = cplexmiqcp(H,f,Aineq,bineq,Aeq,beq,l,Q,r,sostype,sosind,soswt,lb,ub)
% defines a set of lower and upper bounds on the design variables, x, so
% that the solution is in the range lb <= x <= ub. If no SOS constraints
% exist, set sostype=[], sosind=[] and soswt=[].
%
% x =
% cplexmiqcp(H,f,Aineq,bineq,Aeq,beq,l,Q,r,sostype,sosind,soswt,lb,ub,ctype
% ) defines the types for each of the design variables. If no bounds exist,
% set lb=[] and ub=[].
%
% x =
% cplexmiqcp(H,f,Aineq,bineq,Aeq,beq,l,Q,r,sostype,sosind,soswt,lb,ub,x0)
% sets the starting point to x0. If all design variables are continuous,
% set ctype=[].
%
% x = cplexmiqcp(H,f,Aineq,bineq,Aeq,beq,l,Q,r,lb,ub,ctype,x0,options) minimizes
% with the optimization options specified in the structure options, which
% can be created using the function cplexoptimset If you do not wish to
% give an initial point, set x0=[].
%
% x = cplexmiqcp(problem) where problem is a structure.
%
% [x,fval] = cplexmiqcp(...) returns the value of the objective function at
% the solution x: fval = 0.5*x'*H*x + f*x.
%
% [x,fval,exitflag] = cplexmiqcp(...) returns a value exitflag that
% describes the exit condition of cplexmiqcp.
%
% [x,fval,exitflag,output] = cplexmiqcp(...) returns a structure output
% that contains information about the optimization.
%
%  See also cplexoptimset
%

% ---------------------------------------------------------------------------
% File: cplexmiqcp.m
% ---------------------------------------------------------------------------
% Licensed Materials - Property of IBM
% 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55
% Copyright IBM Corporation 2008, 2011. All Rights Reserved.
%
% US Government Users Restricted Rights - Use, duplication or
% disclosure restricted by GSA ADP Schedule Contract with
% IBM Corp.
% ---------------------------------------------------------------------------
