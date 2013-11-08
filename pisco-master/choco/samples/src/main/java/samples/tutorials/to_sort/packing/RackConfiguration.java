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

package samples.tutorials.to_sort.packing;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.model.constraints.pack.PackModel;
import choco.kernel.model.variables.integer.IntegerConstantVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;
import org.kohsuke.args4j.Option;
import samples.tutorials.PatternExample;

import java.util.logging.Level;

import static choco.Choco.*;
import static choco.Options.*;
import static choco.visu.components.chart.ChocoChartFactory.createAndShowGUI;
import static choco.visu.components.chart.ChocoChartFactory.createPackChart;


/**
 * <b>CSPLib n°31 : Rack Configuration Problem</b>
 *
 * @author Arnaud Malapert</br>
 * @version 2.0.1</br>
 * @since 3 déc. 2008 <b>version</b> 2.0.1</br>
 */
public class RackConfiguration extends PatternExample {
    //DATA
    @Option(name = "-n", usage = "Number of racks (default : 10)", required = false)
    private int nbRacks;

    private int nbCards, nbRackModels, nbCardTypes;

    @Option(name = "-rpow", usage = "Power of each rack (default : {0,50,150,200})", required = false)
    private int[] rackPower = {0, 50, 150, 200};

    @Option(name = "-rcon", usage = "Number of connector on each rack (default : {0, 2, 5, 9})", required = false)
    private int[] rackConnector = {0, 2, 5, 9};

    @Option(name = "-rpri", usage = "Price of each rack (default : {0, 35, 140, 200})", required = false)
    private int[] rackPrice = {0, 35, 140, 200};

    @Option(name = "-cpow", usage = "Power of each card (default : {75, 50, 40, 20})", required = false)
    private int[] _cardPower = {0, 50, 150, 200};

    @Option(name = "-cdem", usage = "Demand of each card (default : {3, 4, 6, 8})", required = false)
    private int[] cardDemand = {3, 4, 6, 8};

    //MODEL
    private SetVariable[] racks;

    private IntegerVariable[] cardRacks;

    private IntegerConstantVariable[] cardMPower;

    private IntegerConstantVariable[] cardPower;

    private IntegerVariable[] rackLoads, rackTypes, rackMaxLoads, rackMaxConnectors, rackCosts;

    private IntegerVariable objective;

    private PackModel packMod;

    @Override
    public void buildModel() {
        cardMPower = constantArray(_cardPower);
        nbCardTypes = cardDemand.length;
        nbRackModels = rackPrice.length;
        nbCards = MathUtils.sum(cardDemand);


        model = new CPModel();
        //VARIABLES
        cardRacks = new IntegerVariable[nbCards];
        cardPower = new IntegerConstantVariable[nbCards];
        int offset = 0;
        for (int i = 0; i < nbCardTypes; i++) {
            for (int j = offset; j < offset + cardDemand[i]; j++) {
                cardRacks[j] = makeIntVar("cardRack_" + i + "_" + (j - offset), 0, nbRacks - 1, V_ENUM);
                cardPower[j] = cardMPower[i];
            }
            offset += cardDemand[i];
        }
        racks = makeSetVarArray("rack", nbRacks, 0, nbCards - 1, V_NO_DECISION);
        rackLoads = makeIntVarArray("rackLoad", nbRacks, 0, MathUtils.max(rackPower), V_BOUND, V_NO_DECISION);
        rackTypes = makeIntVarArray("rackType", nbRacks, 0, nbRackModels - 1, V_ENUM);
        rackCosts = makeIntVarArray("rackCost", nbRacks, rackPrice, V_ENUM);
        rackMaxLoads = makeIntVarArray("rackMaxLoad", nbRacks, rackPower, V_ENUM, V_NO_DECISION);
        rackMaxConnectors = makeIntVarArray("rackMaxConnector", nbRacks, rackConnector, V_NO_DECISION);
        objective = makeIntVar("obj", 0, nbRacks * MathUtils.max(rackPrice), V_OBJECTIVE, V_NO_DECISION);

        //CONSTRAINTS
        //post packing constraints
        packMod = new PackModel(cardRacks, cardPower, racks, rackLoads);
        model.addConstraints(pack(packMod, C_PACK_AR));
        //post connector,power and price constraints
        for (int i = 0; i < nbRacks; i++) {
            model.addConstraints(
                    leqCard(racks[i], rackMaxConnectors[i]),
                    leq(rackLoads[i], rackMaxLoads[i]),
                    nth(rackTypes[i], rackPower, rackMaxLoads[i]),
                    nth(rackTypes[i], rackPrice, rackCosts[i]),
                    nth(rackTypes[i], rackConnector, rackMaxConnectors[i])
            );
        }
        //post objective
        model.addConstraint(eq(objective, sum(rackCosts)));
        //post symmetry breaking constraints
        model.addConstraints(packMod.orderEqualSizedItems(0));
        for (int i = 1; i < nbRacks; i++) {
            model.addConstraint(geq(rackTypes[i - 1], rackTypes[i]));
        }

    }


    @Override
    public void buildSolver() {
        solver = new CPSolver();
        solver.read(model);
    }


    @Override
    public void solve() {
        solver.minimize(false);
    }


    @Override
    public void prettyOut() {
        if (LOGGER.isLoggable(Level.INFO) && solver.existsSolution()) {
            final StringBuilder b = new StringBuilder();
            for (int i = 0; i < nbRacks; i++) {
                b.append(solver.getVar(rackTypes[i]));
                b.append(" --> ").append(solver.getVar(racks[i]));
                b.append('\n');
            }
            b.append("total cost: ").append(solver.getObjectiveValue());
            LOGGER.info(b.toString());
            createAndShowGUI("Rack Configuration", createPackChart(null, solver, packMod));
        }
    }


    public static void main(String[] args) {
        (new RackConfiguration()).execute(args);
    }


}
