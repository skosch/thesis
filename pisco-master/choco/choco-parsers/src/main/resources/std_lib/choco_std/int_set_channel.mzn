%-----------------------------------------------------------------------------%
% Requires that array of int variables 'x' and array of set variables 'y'
% are related such that '(x[i] = j) <-> (i in y[j])'.
%-----------------------------------------------------------------------------%
predicate int_set_channel(array[int] of var int: x,
                          array[int] of var set of int: y) =
    global_inverseSet(x, y);
%-----------------------------------------------------------------------------%

% The implementation of the constraint in the choco solver.
% This should not be called directly, instead the definition above should
% be used.
predicate global_inverseSet(array[int] of var int: x,
                          array[int] of var set of int: y);
