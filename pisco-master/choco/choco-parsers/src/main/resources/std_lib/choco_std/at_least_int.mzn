%-----------------------------------------------------------------------------%
% Requires at least 'n' variables in 'x' to take the value 'v'.
%-----------------------------------------------------------------------------%

predicate at_least_int(int: n, array[int] of var int: x, int: v) =
    global_at_least(n,x,v);

%-----------------------------------------------------------------------------%

% The implementation of the constraint in the choco solver.
% This should not be called directly, instead the definition above should
% be used.
predicate global_at_least(int: n, array[int] of var int: x, int: v);

%-----------------------------------------------------------------------------%

