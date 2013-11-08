%-----------------------------------------------------------------------------%
% Constrains 'm' to be the maximum of the values in 'x'.
% Assumptions: |x| > 0.
%-----------------------------------------------------------------------------%

predicate maximum_int(var int: m, array[int] of var int: x) =
	global_max(x, m);

%-----------------------------------------------------------------------------%

% The implementation of the constraint in the choco solver.
% This should not be called directly, instead the definition above should
% be used.
predicate global_max(array[int] of var int: x, var int: m);
