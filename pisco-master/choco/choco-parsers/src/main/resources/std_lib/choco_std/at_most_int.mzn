%-----------------------------------------------------------------------------%
% Requires at most 'n' variables in 'x' to take the value 'v'.
%-----------------------------------------------------------------------------%

predicate at_most_int(int: n, array[int] of var int: x, int: v) =
    global_at_most(n,x,v);

%-----------------------------------------------------------------------------%

% The implementation of the constraint in the choco solver.
% This should not be called directly, instead the definition above should
% be used.
predicate global_at_most(int: n, array[int] of var int: x, int: v);

%-----------------------------------------------------------------------------%

