%-----------------------------------------------------------------------------%
% Requires that the multiset of values in 'x' are the same as the
% multiset of values in 'y' but 'y' is in sorted order.
%-----------------------------------------------------------------------------%
predicate sort(array[int] of var int: x, array[int] of var int: y) =
	global_sorting(x, y);

%-----------------------------------------------------------------------------%

% The implementation of the constraint in the choco solver.
% This should not be called directly, instead the definition above should
% be used.
predicate global_sorting(array[int] of var int: x, array[int] of var int: y);

