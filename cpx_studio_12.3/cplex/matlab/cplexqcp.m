function [x,fval,exitflag,output,lambda]=cplexqcp(H, f, Aineq, bineq, Aeq, beq, l, Q, r, lb, ub, x0, options)
%%
% cplexqcp
% Solve quadratically constrained linear/quadratic programming problems.
%
% x = cplexqcp(H,f,Aineq,bineq) solves the quadratically constrained
% linear/quadratic programming problem min 1/2*x'*H*x + f*x subject to
% Aineq*x <= bineq. If no quadratic objective term exists, set H=[].
%
% x = cplexqcp(H,f,Aineq,bineq,Aeq,beq) solves the preceding problem while
% additionally satisfying the equality constraints Aeq*x = beq. If no
% inequalities exist, set Aineq=[] and bineq=[].
%
% x = cplexqcp(H,f,Aineq,bineq,Aeq,beq,l,Q,r) solves the preceding problem
% while additionally satisfying the quadratic inequality constraints
% l*x + x'*Q*x <= r. If no equalities exist, set Aeq=[] and beq=[].
%
% x = cplexqcp(H,f,Aineq,bineq,Aeq,beq,l,Q,r,lb,ub) defines a set of lower
% and upper bounds on the design variables, x, so that the solution is in
% the range lb <= x <= ub. If no quadratic inequalities exist, set l=[],
% Q=[] and r=[].
%
% x = cplexqcp(H,f,Aineq,bineq,Aeq,beq,l,Q,r,lb,ub,x0) sets the starting
% point to x0. If no bounds exist, set lb=[] and ub=[].
%
% x = cplexqcp(H,f,Aineq,bineq,Aeq,beq,l,Q,r,lb,ub,x0,options) minimizes
% with the default optimization options replaced by values in the structure
% options, which can be created using the function cplexoptimset. If you do
% not wish to give an initial point, set x0=[].
%
% x = cplexqcp(problem) where problem is a structure.
%
% [x,fval] = cplexqcp(...) returns the value of the objective function at
% the solution x: fval = 0.5*x'*H*x + f*x.
%
% [x,fval,exitflag] = cplexqcp(...) returns a value exitflag that describes
% the exit condition of cplexqcp.
%
% [x,fval,exitflag,output] = cplexqcp(...) returns a structure output that
% contains information about the optimization.
%
% [x,fval,exitflag,output,lambda] = cplexqcp(...) returns a structure
% lambda whose fields contain the Lagrange multipliers at the solution x.
%
%  See also cplexoptimset
%

% ---------------------------------------------------------------------------
% File: cplexqcp.m
% ---------------------------------------------------------------------------
% Licensed Materials - Property of IBM
% 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55
% Copyright IBM Corporation 2008, 2011. All Rights Reserved.
%
% US Government Users Restricted Rights - Use, duplication or
% disclosure restricted by GSA ADP Schedule Contract with
% IBM Corp.
% ---------------------------------------------------------------------------
