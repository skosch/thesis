%-----------------------------------------------------------------------------%
% Requires that the array 'x' is strictly lexicographically less than array 'y'.
% Compares them from first to last element, regardless of indices
%-----------------------------------------------------------------------------%

predicate lex_less_int(array[int] of var int: x,
                   array[int] of var int: y) =
	global_lex(x, y);
%-----------------------------------------------------------------------------%

% The implementation of the constraint in the choco solver.
% This should not be called directly, instead the definition above should
% be used.
predicate global_lex(array[int] of var int: x,
                   array[int] of var int: y);
