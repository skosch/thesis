import java.util.Vector;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.geost.Constants;
import choco.cp.solver.constraints.global.geost.externalConstraints.ExternalConstraint;
import choco.cp.solver.constraints.global.geost.externalConstraints.NonOverlapping;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.geost.GeostObject;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;

public class GeostExp {

	// The data
	public static int dim = 2;

	public static int[][] domOrigins = { { 0, 1, 0, 1 }, { 0, 1, 0, 1 },
			{ 0, 1, 0, 3 }, };

	public static int[][] domShapes = { { 0, 1 }, { 2, 2 }, { 3, 3 } };

	public static int[][] shBoxes = { { 0, 0, 0, 1, 3 }, { 0, 0, 0, 2, 1 },
			{ 1, 0, 0, 2, 1 }, { 1, 1, 0, 1, 3 }, { 2, 0, 0, 2, 1 },
			{ 2, 1, 0, 1, 3 }, { 2, 0, 2, 2, 1 }, { 3, 0, 0, 2, 1 }, };

	public static void exp2D() {

		int nbOfObj = 3;

		// create the choco problem
		Model m = new CPModel();

		// Create Objects
		Vector<GeostObject> objects = new Vector<GeostObject>();

		for (int i = 0; i < nbOfObj; i++) {
			IntegerVariable shapeId = Choco.makeIntVar("sid_" +i,  domShapes[i][0], domShapes[i][1]);
			IntegerVariable coords[] = new IntegerVariable[dim];
			coords[0] = Choco.makeIntVar("x_" +i, domOrigins[i][0],
					domOrigins[i][1]);
			coords[1] = Choco.makeIntVar("y_" +i, domOrigins[i][2],
					domOrigins[i][3]);
			

			IntegerVariable start = Choco.makeIntVar("start", 0, 0);
			IntegerVariable duration = Choco.makeIntVar("duration", 1, 1);
			IntegerVariable end = Choco.makeIntVar("end", 1, 1);
			objects.add(new GeostObject(dim, i, shapeId, coords, start,
					duration, end));

		}

		// create shiftedboxes and add them to corresponding shapes
		Vector<choco.cp.solver.constraints.global.geost.geometricPrim.ShiftedBox> sb = new Vector<choco.cp.solver.constraints.global.geost.geometricPrim.ShiftedBox>();
		for (int i = 0; i < shBoxes.length; i++) {
			int[] offset = { shBoxes[i][1], shBoxes[i][2] };
			int[] sizes = { shBoxes[i][3], shBoxes[i][4] };
			sb
					.add(new choco.cp.solver.constraints.global.geost.geometricPrim.ShiftedBox(
							shBoxes[i][0], offset, sizes));
		}

		// Create the external constraints vecotr
		Vector<ExternalConstraint> ectr = new Vector<ExternalConstraint>();
		
		// create the list of dimensions for the external constraint
		int[] ectrDim = new int[dim];
		for (int d = 0; d < dim; d++)
			ectrDim[d] = d;

		// create the list of object ids for the external constraint
		int[] objOfEctr = new int[nbOfObj];
		for (int d = 0; d < nbOfObj; d++) {
			objOfEctr[d] = objects.elementAt(d).getObjectId();
		}

		// create the external constraint of non overlapping type
		NonOverlapping n = new NonOverlapping(Constants.NON_OVERLAPPING,
				ectrDim, objOfEctr);
		// add the external constraint to the ectr vector
		ectr.add(n);

		// create the geost constraint
		Constraint geost = Choco.geost(dim, objects, sb, ectr);

		// post the geost constraint to the choco problem
		m.addConstraint(geost);

		// build a solver
		Solver s = new CPSolver();
		// read the problem
		s.read(m);

		// solve the probem
		s.solve();

		// print the solution
		System.out.println(s.pretty());
	}

	public static void main(String args[]) {
		exp2D();
	}
}