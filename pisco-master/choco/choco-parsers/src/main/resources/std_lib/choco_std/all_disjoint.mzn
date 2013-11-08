%-----------------------------------------------------------------------------%
% Ensures that every pair of sets in the array 'S' is disjoint.
%-----------------------------------------------------------------------------%
predicate all_disjoint(array[int] of var set of int: S) =
    global_allDisjoint(S);

%-----------------------------------------------------------------------------%

% The implementation of the constraint in the choco solver.
% This should not be called directly, instead the definition above should
% be used.
predicate global_allDisjoint(array[int] of var set of int: S); 

%-----------------------------------------------------------------------------%