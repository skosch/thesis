%-----------------------------------------------------------------------------%
% Requires that 'c[i]' is the number of occurences of 'i' in 'x'.
%-----------------------------------------------------------------------------%
predicate global_cardinality(array[int] of var int: x,
                             array[int] of var int: c) =
    global_globalCardinality(x, c);


%-----------------------------------------------------------------------------%

% The implementation of the constraint in the choco solver.
% This should not be called directly, instead the definition above should
% be used.
predicate global_globalCardinality(array[int] of var int: x,
                             array[int] of var int: c);

