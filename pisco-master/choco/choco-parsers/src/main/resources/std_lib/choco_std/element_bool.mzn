%-----------------------------------------------------------------------------%
% Requires that 'y' is the ith element of the array 'x'.
%-----------------------------------------------------------------------------%
predicate element_bool(var int: i, array[int] of var bool: x, var bool: y) =
    global_element(i, x, y);

%-----------------------------------------------------------------------------%

% The implementation of the constraint in the choco solver.
% This should not be called directly, instead the definition above should
% be used.
predicate global_element(var int: i, array[int] of var bool: x, var bool: y);
