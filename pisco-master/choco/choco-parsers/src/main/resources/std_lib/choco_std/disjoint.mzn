%-----------------------------------------------------------------------------%
% Requires that sets 's1' and 's2' do not intersect.
%-----------------------------------------------------------------------------%
predicate disjoint(var set of int: s1, var set of int: s2) =
    global_setDisjoint(s1, s2);

%-----------------------------------------------------------------------------%

% The implementation of the constraint in the choco solver.
% This should not be called directly, instead the definition above should
% be used.
predicate global_setDisjoint(var set of int: s1, var set of int: s2);
