%-----------------------------------------------------------------------------%
% Requires that for all `i', the value `cover[i]' appears at least `lbound[i]'
% and at most `ubound[i]' times in the array `x'.
%-----------------------------------------------------------------------------%

predicate global_cardinality_low_up(array[int] of var int: x,
                                    array[int] of int: cover,
				    array[int] of int: lbound,
                                    array[int] of int: ubound) =
    global_globalCardinalityLowUp(x, cover, lbound, ubound);


%-----------------------------------------------------------------------------%

% The implementation of the constraint in the choco solver.
% This should not be called directly, instead the definition above should
% be used.
predicate global_globalCardinalityLowUp(array[int] of var int: x,
                             array[int] of var int: c,
			     array[int] of int: lbound,
                             array[int] of int: ubound);
