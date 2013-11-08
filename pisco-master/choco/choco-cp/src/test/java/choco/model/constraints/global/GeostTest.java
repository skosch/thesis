/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package choco.model.constraints.global;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.geost.Constants;
import choco.cp.solver.constraints.global.geost.util.InputParser;
import choco.cp.solver.constraints.global.geost.util.RandomProblemGenerator;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.geost.GeostOptions;
import choco.kernel.model.constraints.geost.externalConstraints.IExternalConstraint;
import choco.kernel.model.constraints.geost.externalConstraints.NonOverlappingModel;
import choco.kernel.model.variables.geost.GeostObject;
import choco.kernel.model.variables.geost.ShiftedBox;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

import static choco.Choco.*;


public class GeostTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    int dim;
    int mode;


    @Before
    public void before() {
        dim = 3;
        mode = 0;
    }


    @Test
    public void testCustomProblem() {

        int lengths[] = {5, 3, 2};
        int widths[] = {2, 2, 1};
        int heights[] = {1, 1, 1};

        int nbOfObj = 3;

        for (int seed = 0; seed < 20; seed++) {
            //create the choco problem
            Model m = new CPModel();

            //Create Objects
            List<GeostObject> obj2 = new ArrayList<GeostObject>();

            for (int i = 0; i < nbOfObj; i++) {
                IntegerVariable shapeId = makeIntVar("sid", i, i);
                IntegerVariable coords[] = new IntegerVariable[this.dim];
                for (int j = 0; j < coords.length; j++) {
                    coords[j] = makeIntVar("x" + j, 0, 2);
                }
                IntegerVariable start = makeIntVar("start", 1, 1);
                IntegerVariable duration = makeIntVar("duration", 1, 1);
                IntegerVariable end = makeIntVar("end", 1, 1);
                obj2.add(new GeostObject(dim, i, shapeId, coords, start, duration, end));
            }

            //create shiftedboxes and add them to corresponding shapes
            List<ShiftedBox> sb2 = new ArrayList<ShiftedBox>();
            int h = 0;
            while (h < nbOfObj) {

                int[] l = {lengths[h], heights[h], widths[h]};
                int[] t = {0, 0, 0};


                sb2.add(new ShiftedBox(h, t, l));
                h++;
            }

            //Create the external constraints vecotr
            List<IExternalConstraint> ectr2 = new ArrayList<IExternalConstraint>();
            //create the list od dimensions for the external constraint
            int[] ectrDim2 = new int[this.dim];
            for (int d = 0; d < 3; d++)
                ectrDim2[d] = d;

            //create the list of object ids for the external constraint
            int[] objOfEctr2 = new int[nbOfObj];
            for (int d = 0; d < nbOfObj; d++) {
                objOfEctr2[d] = obj2.get(d).getObjectId();
            }

            //create the external constraint of type non overlapping
            NonOverlappingModel n2 = new NonOverlappingModel(Constants.NON_OVERLAPPING, ectrDim2, objOfEctr2);
            //add the external constraint to the vector
            ectr2.add(n2);

            //create the geost constraint object
            Constraint geost = geost(this.dim, obj2, sb2, ectr2);
            m.addConstraint(geost);
            //post the geost constraint to the choco problem
            Solver s = new CPSolver();
            s.read(m);
            s.setValIntSelector(new RandomIntValSelector(seed));
            s.setVarIntSelector(new RandomIntVarSelector(s, seed));
            s.solveAll();
            Assert.assertEquals("number of solutions", 9828, s.getNbSolutions());
        }

    }

    @Test
    @Ignore
    public void RandomProblemGeneration() {

        for (int seed = 0; seed < /*20*/5; seed++) {
            //nb of objects, shapes, shifted boxes and maxLength respectively
            //The nb of Obj should be equal to nb Of shapes for NOW. as For the number of the shifted Boxes it should be greater or equal to thhe nb of Objects

            RandomProblemGenerator rp = new RandomProblemGenerator(this.dim, 7, 7, 9, 25);

            rp.generateProb();

            Model m = rp.getModel();

            List<IExternalConstraint> ectr = new ArrayList<IExternalConstraint>();
            int[] ectrDim = new int[this.dim];
            for (int i = 0; i < this.dim; i++)
                ectrDim[i] = i;

            int[] objOfEctr = new int[rp.getObjects().size()];
            for (int i = 0; i < rp.getObjects().size(); i++) {
                objOfEctr[i] = rp.getObjects().get(i).getObjectId();
            }

            NonOverlappingModel n = new NonOverlappingModel(Constants.NON_OVERLAPPING, ectrDim, objOfEctr);
            ectr.add(n);


            Constraint geost = geost(this.dim, rp.getObjects(), rp.getSBoxes(), ectr);
            m.addConstraint(geost);
            Solver s = new CPSolver();
            s.read(m);
            s.setValIntSelector(new RandomIntValSelector(seed));
            s.setVarIntSelector(new RandomIntVarSelector(s, seed));
            s.solveAll();
            Assert.assertEquals("number of solutions", 0, s.getNbSolutions());
        }
    }

    @Test
    public void PolyMorphicTest() {
        int[][] objects = new int[][]{
                {0, 0, 1, 0, 3, 0, 4, 1, 1, 1, 1, 1, 1},
                {1, 0, 1, 0, 3, 0, 4, 1, 1, 1, 1, 1, 1},
                {2, 0, 1, 0, 3, 0, 4, 1, 1, 1, 1, 1, 1},
                {3, 0, 1, 0, 3, 0, 4, 1, 1, 1, 1, 1, 1},
                {4, 0, 1, 0, 3, 0, 4, 1, 1, 1, 1, 1, 1},
                {5, 2, 2, 0, 0, 6, 6, 1, 1, 1, 1, 1, 1}
        };
        int[] shapes = new int[]{2, 1, 0};
        int[][] shiftedBoxes = new int[][]{
                {0, 0, 0, 3, 2},
                {1, 0, 0, 2, 3},
                {2, 0, 0, 5, 1},
                {2, 5, -6, 1, 7},
        };

        this.dim = 2;

        InputParser parser;
        new InputParser();

        InputParser.GeostProblem gp = new InputParser.GeostProblem(objects, shapes, shiftedBoxes);
        for (int seed = 0; seed < 20; seed++) {
            parser = new InputParser(gp, dim);
            try {
                parser.parse();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Model m = new CPModel();

            // create a vector to hold in it all the external constraints we want to add to geost
            List<IExternalConstraint> ectr = new ArrayList<IExternalConstraint>();

            // ////////////Create the needed external constraints//////////////

            // first of all create a array of intergers containing all the dimensions where the constraint will be active
            int[] ectrDim = new int[dim];
            for (int i = 0; i < dim; i++)
                ectrDim[i] = i;

            // Create an array of object ids representing all the objects that the external constraint will be applied to
            int[] objOfEctr = new int[parser.getObjects().size()];
            for (int i = 0; i < parser.getObjects().size(); i++) {
                objOfEctr[i] = parser.getObjects().get(i).getObjectId();
            }

            // Create the external constraint, in our case it is the NonOverlappingModel
            // constraint (it is the only one implemented for now)
            NonOverlappingModel n = new NonOverlappingModel(Constants.NON_OVERLAPPING, ectrDim, objOfEctr);

            // add the created external constraint to the vector we created
            ectr.add(n);

            // /////////////Create the array of variables to make choco happy//////////////
            // vars will be stored as follows: object 1 coords(so k coordinates), sid, start, duration, end,
            //                                 object 2 coords(so k coordinates), sid, start, duration, end and so on ........
            // To retrieve the index of a certain variable, the formula is (nb of the object in question = objId assuming objIds are consecutive and
            // start from 0) * (k + 4) + number of the variable wanted the number of the variable wanted is decided as follows: 0 ... k-1
            // (the coords), k (the sid), k+1 (start), k+2 (duration), k+3 (end)

            int originOfObjects = parser.getObjects().size() * dim;
            // Number of domain  variables  to  represent the origin of all  objects
            int otherVariables = parser.getObjects().size() * 4; // each object  has 4 other variables: shapeId, start,  duration; end
            IntegerVariable[] vars = new IntegerVariable[originOfObjects + otherVariables];

            for (int i = 0; i < parser.getObjects().size(); i++) {
                for (int j = 0; j < dim; j++) {
                    vars[(i * (dim + 4)) + j] = parser.getObjects().get(i).getCoordinates()[j];
                }
                vars[(i * (dim + 4)) + dim] = parser.getObjects().get(i).getShapeId();
                vars[(i * (dim + 4)) + dim + 1] = parser.getObjects().get(i).getStartTime();
                vars[(i * (dim + 4)) + dim + 2] = parser.getObjects().get(i).getDurationTime();
                vars[(i * (dim + 4)) + dim + 3] = parser.getObjects().get(i).getEndTime();
            }


            Constraint geost = geost(dim, parser.getObjects(), parser.getShiftedBoxes(), ectr);

            // /////////////Add the constraint to the choco problem//////////////
            m.addConstraint(geost);

            for (int i = 0; i < parser.getObjects().size() - 2; i++) {
                m.addConstraint(lex(parser.getObjects().get(i).getCoordinates(), parser.getObjects().get(i + 1).getCoordinates()));
            }

            Solver s = new CPSolver();
            s.read(m);


            s.setValIntSelector(new RandomIntValSelector(seed));
            s.setVarIntSelector(new RandomIntVarSelector(s, seed));

            s.solveAll();
            Assert.assertEquals(s.getNbSolutions(), 2);
        }

    }

    @Test
    public void testOfSharingShape() {

        int lengths[] = {5, 3, 6};
        int widths[] = {2, 4, 1};
        int heights[] = {1, 2, 4};

        int nbOfObj = 3;
        Model m = new CPModel();

        //Create Objects
        List<GeostObject> obj2 = new ArrayList<GeostObject>();

        for (int i = 0; i < nbOfObj; i++) {
            IntegerVariable shapeId = makeIntVar("sid", 0, 0);
            IntegerVariable coords[] = new IntegerVariable[this.dim];
            for (int j = 0; j < coords.length; j++) {
                coords[j] = makeIntVar("x" + j, 0, 2);
            }
            IntegerVariable start = makeIntVar("start", 1, 1);
            IntegerVariable duration = makeIntVar("duration", 1, 1);
            IntegerVariable end = makeIntVar("end", 1, 1);
            obj2.add(new GeostObject(dim, i, shapeId, coords, start, duration, end));
        }
        for (int i = 0; i < obj2.size(); i++) {
            for (int d = 0; d < this.dim; d++) {
                LOGGER.info("" + obj2.get(i).getCoordinates()[d].getLowB() + "    " + obj2.get(i).getCoordinates()[d].getUppB());
            }

        }

        //create shiftedboxes and add them to corresponding shapes
        List<ShiftedBox> sb2 = new ArrayList<ShiftedBox>();

        int[] l = {lengths[0], heights[0], widths[0]};
        int[] t = {0, 0, 0};


        sb2.add(new ShiftedBox(0, t, l));


        List<IExternalConstraint> ectr2 = new ArrayList<IExternalConstraint>();
        int[] ectrDim2 = new int[this.dim];
        for (int d = 0; d < 3; d++)
            ectrDim2[d] = d;


        int[] objOfEctr2 = new int[nbOfObj];
        for (int d = 0; d < nbOfObj; d++) {
            objOfEctr2[d] = obj2.get(d).getObjectId();
        }

        NonOverlappingModel n2 = new NonOverlappingModel(Constants.NON_OVERLAPPING, ectrDim2, objOfEctr2);
        ectr2.add(n2);

        Constraint geost2 = geost(this.dim, obj2, sb2, ectr2);
        m.addConstraint(geost2);
        Solver s = new CPSolver();
        s.read(m);
        //Here the solve will only do a test for the first constraint and not the second.
        //However for our purposes this is not important. If it is just change the code
        //of solve to take 2 constraints as parameters and then run the two solution testers
        s.solveAll();
        Assert.assertEquals(s.getNbSolutions(), 7290);
    }


    @Test
    public void exp2DTest() {

        //The data
        int dim = 2;
        int[][] domOrigins = {
                {0, 5, 0, 3},
                {0, 5, 0, 5},
                {0, 6, 0, 4},
                {0, 6, 0, 5},
                {0, 5, 0, 5},
                {0, 7, 0, 4},
                {0, 6, 0, 5},
                {0, 6, 0, 5},
                {0, 5, 0, 6},
                {0, 7, 0, 5}
        };


        int[][] shBoxes = {
                {0, 0, 0, 2, 3},
                {0, 1, 2, 2, 2},
                {1, 0, 0, 3, 2},
                {2, 0, 0, 2, 3},
                {3, 0, 0, 2, 2},
                {4, 0, 0, 3, 1},
                {4, 1, 0, 1, 2},
                {5, 0, 0, 1, 3},
                {6, 0, 0, 1, 2},
                {6, 0, 1, 2, 1},
                {7, 0, 0, 2, 1},
                {7, 1, 0, 1, 2},
                {8, 0, 0, 3, 1},
                {9, 0, 0, 1, 2}
        };
        int[] v0 = {-1, -2, -3};
        int[] v1 = {-1, 2, 3};
        int[] v2 = {-1, 2, -3};
        int[] v3 = {-1, 3, -2};

        int nbOfObj = 10;

        //create the choco problem
        Model m = new CPModel();

        //Create Objects
        List<GeostObject> obj = new ArrayList<GeostObject>();

        for (int i = 0; i < nbOfObj; i++) {
            IntegerVariable shapeId = makeIntVar("sid", i, i);
            IntegerVariable coords[] = new IntegerVariable[dim];
            coords[0] = makeIntVar("x", domOrigins[i][0], domOrigins[i][1]);
            coords[1] = makeIntVar("y", domOrigins[i][2], domOrigins[i][3]);

            IntegerVariable start = makeIntVar("start", 1, 1);
            IntegerVariable duration = makeIntVar("duration", 1, 1);
            IntegerVariable end = makeIntVar("end", 1, 1);
            obj.add(new GeostObject(dim, i, shapeId, coords, start, duration, end));


        }

        //create shiftedboxes and add them to corresponding shapes
        List<ShiftedBox> sb = new ArrayList<ShiftedBox>();
        for (int i = 0; i < shBoxes.length; i++) {
            int[] offset = {shBoxes[i][1], shBoxes[i][2]};
            int[] sizes = {shBoxes[i][3], shBoxes[i][4]};
            sb.add(new ShiftedBox(shBoxes[i][0], offset, sizes));
        }

        //Create the external constraints vecotr
        List<IExternalConstraint> ectr = new ArrayList<IExternalConstraint>();
        //create the list of dimensions for the external constraint
        int[] ectrDim = new int[dim];
        for (int d = 0; d < dim; d++)
            ectrDim[d] = d;

        //create the list of object ids for the external constraint
        int[] objOfEctr = new int[nbOfObj];
        for (int d = 0; d < nbOfObj; d++) {
            objOfEctr[d] = obj.get(d).getObjectId();
        }

//		create the external constraint of type non overlapping
        NonOverlappingModel n = new NonOverlappingModel(Constants.NON_OVERLAPPING, ectrDim, objOfEctr);
        //add the external constraint to the vector
        ectr.add(n);

        //create the list of controlling vectors
        List<int[]> ctrlVs = new ArrayList<int[]>();
        ctrlVs.add(v0);
        //ctrlVs.add(v1);
        //ctrlVs.add(v2);
        //ctrlVs.add(v3);

        //create the geost constraint
        Constraint geost = geost(dim, obj, sb, ectr, ctrlVs);

        //NOTA: you can choose to not take into account of the greedy mode by creating the geost constraint as follows:
        //Geost_Constraint geost = new Geost_Constraint(vars, dim, obj, sb, ectr);

        //post the geost constraint to the choco problem
        m.addConstraint(geost);

        Solver s = new CPSolver();
        s.read(m);

        // solve the probem
        s.solve();

        for (int i = 0; i < obj.size(); i++) {
            GeostObject o = obj.get(i);
            StringBuffer st = new StringBuffer();
            st.append(MessageFormat.format("Object {0}: ", o.getObjectId()));
            for (int j = 0; j < dim; j++)
                st.append(MessageFormat.format("{0} ", s.getVar(o.getCoordinates()[j])));
            LOGGER.info(st.toString());
        }
    }

    public static int[][] domOrigins = {{0, 1, 0, 1}, {0, 1, 0, 1},
            {0, 1, 0, 3},};

    public static int[][] domShapes = {{0, 1}, {2, 2}, {3, 3}};

    public static int[][] shBoxes = {{0, 0, 0, 1, 3}, {0, 0, 0, 2, 1},
            {1, 0, 0, 2, 1}, {1, 1, 0, 1, 3}, {2, 0, 0, 2, 1},
            {2, 1, 0, 1, 3}, {2, 0, 2, 2, 1}, {3, 0, 0, 2, 1},};

    @Test
    public void exp2D2Test() {

        dim = 2;
        int nbOfObj = 3;

        // create the choco problem
        Model m = new CPModel();

        // Create Objects
        List<GeostObject> objects = new ArrayList<GeostObject>();

        for (int i = 0; i < nbOfObj; i++) {
            IntegerVariable shapeId = makeIntVar("sid_" + i, domShapes[i][0], domShapes[i][1]);
            IntegerVariable coords[] = new IntegerVariable[dim];
            coords[0] = makeIntVar("x_" + i, domOrigins[i][0], domOrigins[i][1]);
            coords[1] = makeIntVar("y_" + i, domOrigins[i][2], domOrigins[i][3]);

            // ++ Modification
            // Additional Constraint
            m.addConstraint(geq(coords[0], 1));
            // -- Modification

            IntegerVariable start = makeIntVar("start", 0, 0);
            IntegerVariable duration = makeIntVar("duration", 1, 1);
            IntegerVariable end = makeIntVar("end", 1, 1);
            objects.add(new GeostObject(dim, i, shapeId, coords, start, duration, end));
        }

        // create shiftedboxes and add them to corresponding shapes
        List<ShiftedBox> sb =
                new ArrayList<ShiftedBox>();

        for (int i = 0; i < shBoxes.length; i++) {
            int[] offset = {shBoxes[i][1], shBoxes[i][2]};
            int[] sizes = {shBoxes[i][3], shBoxes[i][4]};
            sb.add(new ShiftedBox(shBoxes[i][0], offset, sizes));
        }

        // Create the external constraints vecotr
        List<IExternalConstraint> ectr = new ArrayList<IExternalConstraint>();

        // create the list of dimensions for the external constraint
        int[] ectrDim = new int[dim];
        for (int d = 0; d < dim; d++)
            ectrDim[d] = d;

        // create the list of object ids for the external constraint
        int[] objOfEctr = new int[nbOfObj];
        for (int d = 0; d < nbOfObj; d++) {
            objOfEctr[d] = objects.get(d).getObjectId();
        }

        // create the external constraint of non overlapping type
        NonOverlappingModel n = new NonOverlappingModel(Constants.NON_OVERLAPPING,
                ectrDim, objOfEctr);
        // add the external constraint to the ectr vector
        ectr.add(n);

        // create the geost constraint
        Constraint geost = geost(dim, objects, sb, ectr);

        // post the geost constraint to the choco problem
        m.addConstraint(geost);

        // build a solver
        Solver s = new CPSolver();
        // read the problem
        s.read(m);

        // solve the probem
        s.solve();

        Assert.assertSame("No solution expected", Boolean.FALSE, s.isFeasible());

        // print the solution
        LOGGER.info(s.pretty());
    }

    @Test
    public void test_ajdvries() {
        for (int nbO = 4; nbO < 257; nbO *= 4) {
            boolean inc = true;
            do {
                int width = nbO * 5;
                int height = nbO * 5;
                int maxX = width - 5;
                int maxY = height - 5;

                CPModel m = new CPModel();

                List<GeostObject> geosts = new Vector<GeostObject>();
                List<ShiftedBox> sb = new Vector<ShiftedBox>();

                List<IntegerVariable> x = new ArrayList<IntegerVariable>();
                List<IntegerVariable> y = new ArrayList<IntegerVariable>();
                for (int a = 0; a < 16; a++) {
                    IntegerVariable varX = makeIntVar("img_" + a + "_x", 0, maxX);
                    IntegerVariable varY = makeIntVar("img_" + a + "_y", 0, maxY);
                    x.add(varX);

                    y.add(varY);
                    IntegerVariable coordinates[] = new IntegerVariable[]{varX, varY};

                    geosts.add(new GeostObject(2, a, constant(a), coordinates, constant(0), constant(1),
                            constant(1)));

                    sb.add(new ShiftedBox(a, new int[]{0, 0}, new int[]{5, 5}));
                }

                Vector<IExternalConstraint> ectr = new Vector();

                int[] ectrDim = new int[]{0, 1};
                int[] objOfEctr = new int[geosts.size()];
                for (int i = 0; i < geosts.size(); i++) {
                    objOfEctr[i] = geosts.get(i).getObjectId();
                }

                NonOverlappingModel n = new NonOverlappingModel(Constants.NON_OVERLAPPING, ectrDim, objOfEctr);
                ectr.add(n);

                Vector<int[]> ctrlVs = new Vector<int[]>();
                int[] v0 = {1, -3, -2};
                ctrlVs.add(v0);

                // Definition of the GEOST constraint
                GeostOptions.increment = inc;
                Constraint geost = geost(2, geosts, sb, ectr, ctrlVs);
                m.addConstraint(geost);

                Solver solver = new CPSolver();
                solver.read(m);
                Assert.assertTrue(solver.solve());
                inc ^= true;
            } while (!inc);
        }

    }

    @Test
    public void testNonOverlap() {
        CPModel model = new CPModel();

        List<GeostObject> geosts = new ArrayList<GeostObject>();
        Map<Integer, ShiftedBox> boxesById = new HashMap<Integer, ShiftedBox>();

        // Blocks to be placed
        int currentShapeId = 0;
        ShiftedBox block = new ShiftedBox(currentShapeId, new int[]{0, 0}, new int[]{20, 1});
        boxesById.put(currentShapeId, block);

        IntegerVariable[] fixedCoordinates = new IntegerVariable[]{constant(0), constant(0)};
        IntegerVariable[] variableCoordinates = new IntegerVariable[]{makeIntVar("variable", 0, 0), constant(0)};


        geosts.add(new GeostObject(2, 0, constant(currentShapeId), fixedCoordinates, constant(0),
                constant(1), constant(1)));

        geosts.add(new GeostObject(2, 1, constant(currentShapeId), fixedCoordinates, constant(0),
                constant(1), constant(1)));

        List<IExternalConstraint> ectr = new ArrayList<IExternalConstraint>();

        int[] ectrDim = new int[]{0, 1};
        int[] objOfEctr = new int[geosts.size()];

        NonOverlappingModel n = new NonOverlappingModel(Constants.NON_OVERLAPPING, ectrDim, objOfEctr);
        ectr.add(n);

        List<int[]> ctrlVs = new ArrayList<int[]>();
        int[] v0 = {1, -3, -2};
        // int[] v1 = { 1, -2, 2 };
        // ctrlVs.add(v1);
        ctrlVs.add(v0);

        // Definition of the GEOST constraint
        Constraint geost = geost(2, geosts, new ArrayList<ShiftedBox>(boxesById.values()), ectr, ctrlVs);
        model.addConstraint(geost);

        CPSolver solver = new CPSolver();
        solver.read(model);

        Assert.assertFalse(solver.solve());
    }

    @Test
    public void testSerge() {
        // On this very first experiment, we'll consider only a single shape
        //  for each object
        // Labels to be placed are:
        // - a label of size (2,2) near (6,3) : at (7,4) or at (4,1)
        // - a label of size (2,4) near (3,2) : at (4,3) or at (0,3)
        // Graphically, this gives (a and b are alternatives for label 1, c and d for 2):
        //
        //        5                               a   a
        //        4   d   d   d   d   c   c   c   ac  a
        //        3   d   d   d   d   c   c   1c  c
        //        2               2   b   b
        //        1                   b   b
        //        0
        //            0   1   2   3   4   5   6   7   8
        //
        // Expected solutions are: "1=a,2=d", "1=b,2=c" and "1=b,2=d"

        // Both labels 1 and 2 will have a single shape each

        IntegerVariable X1 = makeIntVar("X1", 4, 7);
        IntegerVariable Y1 = makeIntVar("Y1", 1, 4);
        IntegerVariable[] p1 = new IntegerVariable[]{X1, Y1};
        GeostObject lab1 = new GeostObject(2, 1, constant(1), p1,
                constant(0), constant(1), constant(1));
        ShiftedBox sb1 = new ShiftedBox(1, new int[]{0, 0}, new int[]{2, 2});

        IntegerVariable X2 = makeIntVar("X2", 0, 4);
        IntegerVariable Y2 = makeIntVar("Y2", 3, 3);
        IntegerVariable[] p2 = new IntegerVariable[]{X2, Y2};
        GeostObject lab2 = new GeostObject(2, 2, constant(2), p2,
                constant(0), constant(1), constant(1));
        ShiftedBox sb2 = new ShiftedBox(2, new int[]{0, 0}, new int[]{4, 2});

        List<GeostObject> gos = new ArrayList<GeostObject>();
        gos.add(lab1);
        gos.add(lab2);
        List<ShiftedBox> SBs = new ArrayList<ShiftedBox>();
        SBs.add(sb1);
        SBs.add(sb2);

        int[] ectrDim = new int[]{0, 1};
        int[] objOfEctr = new int[]{1, 2}; // IDs of labels
        List<IExternalConstraint> ectr = new ArrayList<IExternalConstraint>();
        ectr.add(new NonOverlappingModel(Constants.NON_OVERLAPPING,
                ectrDim, objOfEctr));

        Model m = new CPModel();
        m.addConstraint(geost(2, gos, SBs, ectr));
        Solver s = new CPSolver();
        System.out.println("Avant 'Solver.read'");
        s.read(m);
        System.out.println("Avant 'Solver.solve'");
        s.solve();
        System.out.println("Après 'Solver.solve'");
    }

}

