%-----------------------------------------------------------------------------%
% Constrains 'm' to be the minimum of the values in 'x'.
% Assumptions: |x| > 0.
%-----------------------------------------------------------------------------%

predicate minimum_int(var int: m, array[int] of var int: x) =
	global_min(x, m);

%-----------------------------------------------------------------------------%

% The implementation of the constraint in the choco solver.
% This should not be called directly, instead the definition above should
% be used.
predicate global_min(array[int] of var int: x, var int: m);
