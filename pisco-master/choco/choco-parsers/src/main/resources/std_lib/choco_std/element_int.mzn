%-----------------------------------------------------------------------------%
% Requires that 'y' is the ith element of the array 'x'.
%-----------------------------------------------------------------------------%

predicate element_int(var int: i, array[int] of var int: x, var int: y) =
    global_element(i, x, y);

%-----------------------------------------------------------------------------%

% The implementation of the constraint in the choco solver.
% This should not be called directly, instead the definition above should
% be used.
predicate global_element(var int: i, array[int] of var int: x, var int: y);
