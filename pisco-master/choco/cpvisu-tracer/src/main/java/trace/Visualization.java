/**
 * Copyright (c) 1999-2010, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package trace;

import choco.kernel.memory.IStateLong;
import choco.kernel.solver.Solver;
import choco.kernel.solver.search.IntBranchingDecision;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static trace.CPVizConstant.*;

/**
 * A class to produce log files of a problem resolution.
 * <br/>
 * It creates 3 files:<br/>
 * - configuration.xml <br/>
 * - tree.xml <br/>
 * - visualization.xml <br/>
 * respecting the log formats defined by Helmut Simonis.
 * <br/>
 * These files are created at the root directory of the project (see logback.xml for more details).
 * <br/>
 * These files can be treaten with <a href="https://sourceforge.net/projects/cpviz/">cpviz<a/>
 * <p/>
 * <br/>
 * <p/>
 * Before calling the solve step of the program, one can create a new instance of <code>Visualization</code>.
 * <p/>
 * This object provides 3 main services:<br/>
 * - {@code createTree()}: declare the tree search visualizer<br/>
 * - {@code createViz()}: declare the constraint and variable visualizers container<br/>
 * - {@code addVisualizer(Visualizervisualizer)}: add a visualizer to the container<br/>
 * - {@code close()} : close the log files
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 9 déc. 2010
 */

// BEWARE: indices must start at 1
public class Visualization {

    private static String pbid;

    Logger configuration = org.slf4j.LoggerFactory.getLogger("configuration");

    Logger tree = org.slf4j.LoggerFactory.getLogger("tree");

    Logger visualization = org.slf4j.LoggerFactory.getLogger("visualization");

    public int trace_tools = 0;

    List<Visualizer> visualizers;

    private long node_id = -1;

    private IStateLong parent_id;

    private long state_id = 0;

    IntBranchingDecision currentDecision;

    final Solver solver;

    boolean hasFailed = false;

    /**
     * Build a new instance of <code>Visualization</code>.
     *
     * @param solver associated solver
     * @param dir    output directory
     */
    public Visualization(Solver solver, String dir) {
        this(Long.toString(new Random().nextLong()), solver, dir);
    }

    /**
     * Build a new instance of <code>Visualization</code>.
     *
     * @param pbname name of the treated problem (suffix of log files)
     * @param solver associated solver
     * @param dir    output directory
     */
    public Visualization(String pbname, Solver solver, String dir) {
        pbid = pbname;
        MDC.put("pbid", pbid);
        this.solver = solver;
        this.parent_id = this.solver.getEnvironment().makeLong();
        if (configuration.isInfoEnabled()) {
            configuration.info(C_CONF_TAG_IN, dir);
        }
    }

    /**
     * Declare the tree search visualization.<br/>
     * Append to the configuration xml file
     *
     * @param type    "layout", "graph" or "values"
     * @param display "compact" or "expanded"
     * @param repeat  "all", "final", "i" or "-i"
     * @param width   width of SVG canvas in screen pixels
     * @param height  height of SVG canvas in screen pixels
     */
    public void createTree(String type, String display, String repeat, int width, int height) {
        if (configuration.isInfoEnabled()) {
            if ((trace_tools & Show.TREE.mask) == 0) {
                trace_tools += Show.TREE.mask;
                if (tree.isInfoEnabled()) {
                    configuration.info(C_TOOL_TAG,
                            new Object[]{Show.TREE, type, display, repeat, Integer.toString(width), Integer.toString(height), "tree-" + pbid});
                    tree.info(HEADER);
                    tree.info(T_TREE_TAG_IN);
                } else {
                    tree.error("TREE : info level is disable!");
                }
            }
        } else {
            configuration.warn("Tool \"TREE\" already appended. This will not be added : [{} - {} - {} - {} - {}]",
                    new Object[]{type, display, repeat, Integer.toString(width), Integer.toString(height)});
        }

    }

    /**
     * Declare the tree search visualization, with default construction parameters.<br/>
     * - type : "layout" <br/>
     * - display : "compact" <br/>
     * - repeat: "all" <br/>
     * - width : 500 <br/>
     * - height : 500 <br/>
     * Append to the configuration xml file.
     */
    public void createTree() {
        createTree(LAYOUT, COMPACT, ALL, 500, 500);
    }

    /**
     * Declare the constraint and variable visualizers container.<br/>
     * Append to the configuration xml file
     *
     * @param type    "layout"
     * @param display "compact" or "expanded"
     * @param repeat  "all", "final", "i" or "-i"
     * @param width   width of SVG canvas in screen pixels
     * @param height  height of SVG canvas in screen pixels
     */
    public void createViz(String type, String display, String repeat, int width, int height) {
        visualizers = new ArrayList<Visualizer>(8);
        if (configuration.isInfoEnabled()) {
            if ((trace_tools & Show.VIZ.mask) == 0) {
                trace_tools += Show.VIZ.mask;
                if (visualization.isInfoEnabled()) {
                    configuration.info(C_TOOL_TAG,
                            new Object[]{Show.VIZ, type, display, repeat, Integer.toString(width), Integer.toString(height), "visualization-" + pbid});
                    visualization.info(HEADER);
                    visualization.info(V_VISUALIZATION_TAG_IN);
                } else {
                    visualization.error("VISUALIZATION : info level is disable!");
                }
            }
        } else {
            configuration.warn("Tool \"VISUALIZATION\" already appended. This will not be added : [{} - {} - {} - {} - {}]",
                    new Object[]{type, display, repeat, Integer.toString(width), Integer.toString(height)});
        }
    }

    /**
     * Declare the constraint and variable visualizers container, with default construction parameters.<br/>
     * - type : "layout" <br/>
     * - display : "compact" <br/>
     * - repeat: "all" <br/>
     * - width : 500 <br/>
     * - height : 500 <br/>
     * Append to the configuration xml file
     */
    public void createViz() {
        createViz(LAYOUT, COMPACT, ALL, 500, 500);
    }

    /**
     * Add a constraint/variable visualizer to the container
     *
     * @param visualizer the visualizer to add
     */
    public void addVisualizer(Visualizer visualizer) {
        visualizers.add(visualizer);
        visualizer.setId(visualizers.size());
        if (visualization.isInfoEnabled()) {
            visualization.info(V_VISUALIZER_TAG,
                    new Object[]{visualizer.getId(), visualizer.getType(), visualizer.getDisplay(),
                            visualizer.getWidth(), visualizer.getHeight(),
                            visualizer.options()});
        }
    }

    /**
     * Close the log files
     */
    public void close() {
        if (configuration.isInfoEnabled()) {
            configuration.info(C_CONF_TAG_OUT);
        }
        if (tree.isInfoEnabled()) {
            tree.info(T_TREE_TAG_OUT);
        }
        if (visualization.isInfoEnabled()) {
            visualization.info(V_VISUALIZATION_TAG_OUT);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //****************************************************************************************************************//
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void init() {
        node_id = 0;
        parent_id.set(0);
        state_id = 1;
    }

    void beforeInitialPropagation() {
        if (tree.isInfoEnabled()) {
            tree.info(T_ROOT_TAG);
        }
        if (visualization.isInfoEnabled()) {
            printVisualizerStat(state_id, -1, false);
        }
        state_id++;
    }

    void afterInitialPropagation() {
        if (visualization.isInfoEnabled()) {
            printVisualizerStat(state_id, 0, false);
        }
        state_id++;
    }

    void setBranchingDecision(IntBranchingDecision currentDecision) {
        this.currentDecision = currentDecision;
    }

    void tryNode() {
        node_id++;
        if (tree.isInfoEnabled()) {
            Object bo = currentDecision.getBranchingObject();
            String name = bo.toString();
            String dsize = "?";
            if (bo instanceof IntDomainVar) {
                IntDomainVar ivar = currentDecision.getBranchingIntVar();
                name = ivar.getName();
                dsize = Integer.toString(ivar.getDomainSize());
            } else if (bo instanceof SetVar) {
                SetVar svar = currentDecision.getBranchingSetVar();
                name = svar.getName();
                dsize = Integer.toString(svar.getEnveloppeDomainSize());
            }
            tree.info(T_TRY_TAG, new Object[]{node_id, parent_id.get(), name, dsize,
                    currentDecision.getBranchingValue()});
        }
        if (visualization.isInfoEnabled()) {
            printVisualizerStat(state_id, node_id, true);
        }
        parent_id.set(node_id);
        state_id++;
    }

    void hasFailed() {
        this.hasFailed = true;
    }

    void failNode() {
        if (hasFailed) {
            node_id++;
            hasFailed = false;
            if (tree.isInfoEnabled()) {
                Object bo = currentDecision.getBranchingObject();
                String name = bo.toString();
                String dsize = "?";
                if (bo instanceof IntDomainVar) {
                    IntDomainVar ivar = currentDecision.getBranchingIntVar();
                    name = ivar.getName();
                    dsize = Integer.toString(ivar.getDomainSize());
                } else if (bo instanceof SetVar) {
                    SetVar svar = currentDecision.getBranchingSetVar();
                    name = svar.getName();
                    dsize = Integer.toString(svar.getEnveloppeDomainSize());
                }
                tree.info(T_FAIL_TAG, new Object[]{node_id, parent_id.get(), name, dsize,
                        currentDecision.getBranchingValue()});
            }
            if (visualization.isInfoEnabled()) {
                printVisualizerStat(state_id, node_id, false);
            }
            state_id++;
        }
    }

    private void printVisualizerStat(long s_id, long n_id, Boolean focus) {
        if (visualization.isInfoEnabled() && visualizers != null) {
            visualization.info(V_STATE_TAG_IN, s_id, n_id);
            for (int i = 0; i < visualizers.size(); i++) {
                Visualizer vv = visualizers.get(i);
                visualization.info(V_VISUALIZER_STATE_TAG_IN, vv.getId());
                vv.print(visualization, focus, currentDecision);
                visualization.info(V_VISUALIZER_STATE_TAG_OUT);
            }
            visualization.info(V_STATE_TAG_OUT);
        }
    }

    void succNode() {
        if (tree.isInfoEnabled()) {
            tree.info(T_SUCC_TAG, node_id);
        }
    }

}
