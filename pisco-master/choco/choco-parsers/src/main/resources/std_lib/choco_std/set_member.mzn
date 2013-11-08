%-----------------------------------------------------------------------------%
% Requires that 'y' occurs in the array or set 'x'.
%-----------------------------------------------------------------------------%

predicate set_member(var set of int: x, var int: y) =
	global_member(x, y);

%-----------------------------------------------------------------------------%

% The implementation of the constraint in the choco solver.
% This should not be called directly, instead the definition above should
% be used.
predicate global_member(var set of int: x, var int: y);
