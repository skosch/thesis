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

package choco.cp.solver.constraints.global.geost.layers;

import choco.cp.solver.constraints.global.geost.Setup;
import choco.cp.solver.constraints.global.geost.externalConstraints.DistGeq;
import choco.cp.solver.constraints.global.geost.externalConstraints.DistLeq;
import choco.cp.solver.constraints.global.geost.externalConstraints.DistLinear;
import choco.cp.solver.constraints.global.geost.externalConstraints.ExternalConstraint;
import choco.cp.solver.constraints.global.geost.geometricPrim.Obj;
import choco.cp.solver.constraints.global.geost.geometricPrim.Region;
import choco.cp.solver.constraints.global.geost.internalConstraints.InternalConstraint;
import choco.cp.solver.constraints.global.geost.layers.continuousSolver.Quimper;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import static java.text.MessageFormat.format;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: szampelli
 * Date: 14 ao�t 2009
 * Time: 16:27:20
 * To change this template use File | Settings | File Templates.
 */
public final class GeostNumeric {

    private static final Logger LOGGER = ChocoLogging.getEngineLogger();

    private Setup stp = null;
    private Quimper engine = null;
    private int paramid = 0; //each detected new param gets a param id which is the id of the param in the extengine
    private int varid = 0;    //each detected new variable gets a variable id which is the id of the var in the extengine
    // (extengine=external engine=numeric solver here)
    private int ctrid = 0; //each added new constraint gets a cstr id
    private String listVars = "";
    private String listParams = "";
    private String listCstrs = "";
    private String listContractors = "";


    private Map<Obj, String> contractorName = new HashMap<Obj, String>();
    //list of var ids in the ext engine corresponding to domain variable
    private Map<IntDomainVar, List<Integer>> VarVarId = new HashMap<IntDomainVar, List<Integer>>();
    //list of param ids in the ext engine corresponding to domain variable
    private Map<IntDomainVar, List<Integer>> varParamId = new HashMap<IntDomainVar, List<Integer>>();
    //list of domain variables that exist in the external engine
    private Map<IntDomainVar, Boolean> listOfVars = new HashMap<IntDomainVar, Boolean>();

    private Map<IntDomainVar, HashMap<ExternalConstraint, Integer>> VarParamId = new HashMap<IntDomainVar, HashMap<ExternalConstraint, Integer>>();
    private Map<Obj, HashMap<ExternalConstraint, String>> ObjParamIdText = new HashMap<Obj, HashMap<ExternalConstraint, String>>();
    private Map<Obj, HashMap<ExternalConstraint, String>> ObjCstrName = new HashMap<Obj, HashMap<ExternalConstraint, String>>();


    private long cr = -1; //conversion rate between domain variable and external engine
    private double isThick = 0.0;

    public GeostNumeric(Setup stp_, int maxNbrOfBoxes) {
        stp = stp_;

        cr = computeConversionRate();

        isThick = computeIsThick(maxNbrOfBoxes);


        for (int oid = 0; oid < stp.getObjectKeySet().size(); oid++) { //fixed order iteration
            Obj o = stp.getObject(oid);
            addObj(o);
        }

        for (int oid = 0; oid < stp.getObjectKeySet().size(); oid++) { //fixed order iteration
            Obj o = stp.getObject(oid);
            for (ExternalConstraint ectr : o.getRelatedExternalConstraints()) {
                addCstr(o, ectr); //write constraint and params at the same time
            }
            writeContractor(o);
        }

        writeFile("/tmp/quimper.qpr");

        engine = new Quimper("/tmp/quimper.qpr");
    }

    private void addObj(Obj o) {
        listVars += strObj(o);
        int k = o.getCoordinates().length;
        for (int d = 0; d < k; d++) {
            IntDomainVar v = o.getCoord(d);
            addVar(v);
        }
    }

    private void addVar(IntDomainVar v) {
        if (!VarVarId.containsKey(v)) {
            VarVarId.put(v, new ArrayList<Integer>());
        }
        VarVarId.get(v).add(varid++);
        listOfVars.put(v, true);
    }


    private String strObj(Obj o) {
        StringBuilder r = new StringBuilder();
        int k = o.getCoordinates().length;
        r.append("o").append(o.getObjectId()).append("[").append(k).append("] in [");
        for (int d = 0; d < k; d++) {
            r.append("[");
            r.append(coordToExtEngine(o.getCoord(d).getInf())).append("/*").append(o.getCoord(d).getInf()).append("*/,");
            r.append(coordToExtEngine(o.getCoord(d).getSup())).append("/*").append(o.getCoord(d).getSup()).append("*/");
            if (d == k - 1) {
                r.append("]");
            } else {
                r.append("];");
            }
        }
        r.append("];\n");
        return r.toString();
    }


    private String strParam(Obj o, ExternalConstraint ectr) {
        StringBuilder r = new StringBuilder();
        int k = o.getCoordinates().length;
        r.append(ObjParamIdText.get(o).get(ectr)).append("[").append(k).append("] in [");
        for (int d = 0; d < k; d++) {
            r.append("[");
            r.append(coordToExtEngine(o.getCoord(d).getInf())).append("/*").append(o.getCoord(d).getInf()).append("*/" + ",");
            r.append(coordToExtEngine(o.getCoord(d).getSup())).append("/*").append(o.getCoord(d).getSup()).append("*/");
            if (d == k - 1) {
                r.append("]");
            } else {
                r.append("];");
            }
        }
        r.append("];\n");
        return r.toString();
    }


    private void addParam(Obj o, ExternalConstraint ectr) {
        //Add all obj to the system which are different from o and included in ectr
        if (ectr instanceof DistLeq) {
            DistLeq dl = (DistLeq) ectr;
            int oid = o.getObjectId();
            int toAdd = oid;
            if (dl.o1 == oid) {
                toAdd = dl.o2;
            } else {
                toAdd = dl.o1;
            }
            addObjParamText(stp.getObject(toAdd), ectr);
            listParams += strParam(stp.getObject(toAdd), ectr);
            if (dl.hasDistanceVar()) {
                addVarParam(dl.getDistanceVar(), ectr);
                listParams += strParam(dl.getDistanceVar(), ectr);
            }
        } else if (ectr instanceof DistGeq) {
            DistGeq dl = (DistGeq) ectr;
            int oid = o.getObjectId();
            int toAdd = oid;
            if (dl.o1 == oid) {
                toAdd = dl.o2;
            } else {
                toAdd = dl.o1;
            }
            addObjParamText(stp.getObject(toAdd), ectr);
            listParams += strParam(stp.getObject(toAdd), ectr);
            if (dl.hasDistanceVar()) {
                addVarParam(dl.getDistanceVar(), ectr);
                listParams += strParam(dl.getDistanceVar(), ectr);
            }
        } else if (ectr instanceof DistLinear) {
            //no parameter
        } else {
            throw new SolverException("choco.cp.solver.constraints.global.geost.layers.GeostNumeric:addParam():External Constraint " + ectr + " not supported yet.");
        }
        ctrid++;
    }

    private String strParam(IntDomainVar v, ExternalConstraint ectr) {
        String name = format("p{0}", VarParamId.get(v).get(ectr));
        StringBuilder r = new StringBuilder(format("{0} in ", name));
        r.append("[");
        r.append(coordToExtEngine(v.getInf())).append(",");
        r.append(coordToExtEngine(v.getSup()));
        r.append("];\n");
        return r.toString();
    }


    private void addObjParamText(Obj o, ExternalConstraint ectr) {
        if (ObjParamIdText.get(o) == null) {
            ObjParamIdText.put(o, new HashMap<ExternalConstraint, String>());
        }
        ObjParamIdText.get(o).put(ectr, strObjName(o));

        for (IntDomainVar v : o.getCoordinates()) {
            addVarParam(v, ectr);
            paramid++;
        }
    }

    private String strObjName(Obj o) {
        //return the name of the param of obj o and constraint ectr
        return "o" + o.getObjectId() + "_ctr" + ctrid;
    }

    private void addVarParam(IntDomainVar v, ExternalConstraint ectr) {
        if (VarParamId.get(v) == null) {
            VarParamId.put(v, new HashMap<ExternalConstraint, Integer>());
        }
        VarParamId.get(v).put(ectr, paramid);
        if (varParamId.get(v) == null) {
            varParamId.put(v, new ArrayList<Integer>());
        }
        varParamId.get(v).add(paramid);
        listOfVars.put(v, true);
        paramid++;
    }


    private void addCstr(Obj o, ExternalConstraint ectr) {
        addParam(o, ectr);
        listCstrs += strCstr(o, ectr); //return the cstr string and associate o with the name of the constraint
        addCstrName(o, ectr); //asociate o and the constraint name
        ctrid++;
    }

    private void addCstrName(Obj o, ExternalConstraint ectr) {
        String name = strCstrName(o);
        if (ObjCstrName.get(o) == null) {
            ObjCstrName.put(o, new HashMap<ExternalConstraint, String>());
        }
        ObjCstrName.get(o).put(ectr, name);
    }

    private String strCstr(Obj o, ExternalConstraint ectr) {
        StringBuilder r = new StringBuilder();
        if (ectr instanceof DistLeq) {
            DistLeq dl = (DistLeq) ectr;
            int oid = o.getObjectId();
            r.append(format("constraint {0}\n", strCstrName(o)));
            if (dl.hasDistanceVar()) {
                r.append(" distance(o").append(oid).append(",").append(getObjectParamIdText(stp.getObject(dl.o2), ectr))
                        .append(")<=").append(getVarParamIdText(dl.getDistanceVar(), ectr)).append(";\n");
            } else {
                r.append(" distance(o").append(oid).append(",")
                        .append(getObjectParamIdText(stp.getObject(dl.o2), ectr)).append(")<=")
                        .append(coordToExtEngine(dl.D / 2)).append("/*").append(dl.D).append("*/" + ";\n");
            }
            r.append("end\n");
        } else if (ectr instanceof DistGeq) {
            DistGeq dl = (DistGeq) ectr;
            int oid = o.getObjectId();
            r.append("constraint ").append(strCstrName(o)).append("\n");
            if (dl.hasDistanceVar()) {
                r.append(" distance(o").append(oid).append(",")
                        .append(getObjectParamIdText(stp.getObject(dl.o2), ectr)).append(")>=")
                        .append(getVarParamIdText(dl.getDistanceVar(), ectr)).append(";\n");
            } else {
                r.append(" distance(o").append(oid).append(",")
                        .append(getObjectParamIdText(stp.getObject(dl.o2), ectr)).append(")>=")
                        .append(coordToExtEngine(dl.D)).append("/*").append(dl.D).append("*/" + ";\n");
            }
            r.append("end\n");
        } else if (ectr instanceof DistLinear) {
            throw new SolverException("choco.cp.solver.constraints.global.geost.layers.GeostNumeric:strCstr():External Constraint " + ectr + " not supported yet.");
        }

        return r.toString();
    }

    private String getObjectParamIdText(Obj o, ExternalConstraint ectr) {
        return ObjParamIdText.get(o).get(ectr);
    }

    private String strCstrName(Obj o) {
        return "obj" + o.getObjectId() + "_c" + ctrid;
    }

    private String getVarParamIdText(IntDomainVar v, ExternalConstraint ectr) {
        return "p" + VarParamId.get(v).get(ectr);
    }


    private double computeIsThick(int maxNbrOfBoxes) {
        //The goal of 'computeIsThick' is to compute the parameter isThick such that
        //the max. nbr of boxes generated by the numeric engine os 'maxNbrOfBoxes'.
        //PRE: cr has been computed

        if (cr == -1) {
            throw new SolverException("choco.cp.solver.constraints.global.geost.layers.GeostNumeric:computeIsThick:conversion ratio cr must be computed before isThick can be compured");
        }

        double maxNbrOfBoxesDouble = (double) maxNbrOfBoxes;

        int k = stp.getObject(0).getCoordinates().length;
        double volume = (double) volume();
        LOGGER.info("volume:" + String.format("%f", volume));

        isThick = 1.0;
        double inverse_k = 1.0 / ((double) k);
        if (inverse_k <= 0) {
            throw new SolverException("choco.cp.solver.constraints.global.geost.layers.GeostNumeric:computeIsThick:unable to compute isThick because of 1/k:" + inverse_k);
        }
        return Math.pow((volume / maxNbrOfBoxesDouble), inverse_k) / cr;
    }

    private long volume() {
        int k = stp.getObject(0).getCoordinates().length;
        long volume = 1;

        for (int dim = 0; dim < k; dim++) {
            //determine min and max for dim i
            int min = stp.getObject(0).getCoord(dim).getInf();
            int max = stp.getObject(0).getCoord(dim).getSup();
            for (int i : stp.getObjectKeySet()) {
                Obj o = stp.getObject(i);
                min = Math.min(min, o.getCoord(dim).getInf());
                max = Math.max(max, o.getCoord(dim).getSup());
            }

            volume *= Math.abs(max - min);
        }

        return volume;

    }


    private long computeConversionRate() {
        //compute conversion rate (of the form 10^k) on min and max
        //determine min and max
        int min = stp.getObject(0).getCoord(0).getInf();
        int max = stp.getObject(0).getCoord(0).getSup();
        for (int i : stp.getObjectKeySet()) {
            Obj o = stp.getObject(i);
            for (int j = 0; j < o.getCoordinates().length; j++) {
                min = Math.min(min, o.getCoord(j).getInf());
                max = Math.max(max, o.getCoord(j).getSup());
            }
        }
        //determine conversion rate of min
        long cr_min = 1;
        while ((min / cr_min) > 0) {
            cr_min *= 10;
            if (cr_min < 0) {
                throw new SolverException("choco.cp.solver.constraints.global.geost.layers.GeostNumeric:computeConversionRate:long limit cr_min exceeded");
            }
        }
        //determine conversion rate of max
        long cr_max = 1;
        while ((max / cr_max) > 0) {
            cr_max *= 10;
            if (cr_max < 0) {
                throw new SolverException("choco.cp.solver.constraints.global.geost.layers.GeostNumeric:computeConversionRate:long limit cr_max exceeded");
            }
        }

        //return the max of the two
        return Math.max(cr_min, cr_max);
    }

    private double coordToExtEngine(int v) {
        double vd = (double) v;
        return (vd / cr);
    }

    private void writeContractor(Obj o) {
        List<ExternalConstraint> vectr = o.getRelatedExternalConstraints();
        if (vectr.size() > 0) {
            listContractors += "contractor object" + o.getObjectId() + "\npropag(";

            for (int i = 0; i < vectr.size(); i++) {
                String ctrname = ObjCstrName.get(o).get(vectr.get(i));
                if (i == vectr.size() - 1) {
                    listContractors += ctrname + ");";
                } else {
                    listContractors += ctrname + ";";
                }
            }
            listContractors += "\nend/*listContractors*/\n";
            contractorName.put(o, "object" + o.getObjectId());
        }


    }

    private void writeFile(String filename) {
        try {
            File f = new File(filename);
            PrintWriter pw = new PrintWriter(new FileWriter(f));
            pw.println("Variables");
            pw.println(listVars);
            pw.println("Parameters");
            pw.println(listParams);
            pw.println("function d=distance(x[2],y[2])\n\td=sqrt((x[1]-y[1])^2+(x[2]-y[2])^2);\nend");
            pw.println(listCstrs);
            pw.println(listContractors);
            //DecimalFormat df = new DecimalFormat("###.#######################");
            // df.format(isThick)
            pw.println("contractor isthick\nmaxdiamGT(" + String.format("%f", isThick).replace(",", ".") + ");\nend\n");

            pw.close();
        }
        catch (Exception e) {
            throw new SolverException("choco.cp.solver.constraints.global.geost.layers.GeostNumeric:writeFile():could not write file");
        }
    }

    public void synchronize() {
//        for (int i : stp.getObjectKeySet()) {
//            Obj o=stp.getObject(i);
//            synchronize(o);
//        }

        for (IntDomainVar v : listOfVars.keySet()) {
            synchronize(v);
        }
    }

    public void synchronize(Obj o) {
        for (int i = 0; i < o.getCoordinates().length; i++) {
            IntDomainVar v = o.getCoord(i);
            for (int id : VarVarId.get(v)) {
                double inf = coordToExtEngine(v.getInf());
                double sup = coordToExtEngine(v.getSup());
                engine.set_var_domain(id, inf, sup);
            }
        }
    }

    public void synchronize(IntDomainVar v) {

        List<Integer> a = VarVarId.get(v);
        List<Integer> b = varParamId.get(v);
        if (a != null) {
            for (int id : VarVarId.get(v)) {
                double inf = coordToExtEngine(v.getInf());
                double sup = coordToExtEngine(v.getSup());
                engine.set_var_domain(id, inf, sup);
            }
        } else if (b != null) {
            List<Integer> vint = varParamId.get(v);
            for (int i = 0; i < vint.size(); i++) {
                int id = vint.get(i);
                double inf = coordToExtEngine(v.getInf());
                double sup = coordToExtEngine(v.getSup());
                engine.set_var_domain(id, inf, sup);
            }

        } else {
            throw new SolverException("choco.cp.solver.constraints.global.geost.layers.GeostNumeric:synchronize:IntDomainVar not found");

        }


    }

    public void synchronize(ExternalConstraint ectr) {
        throw new SolverException("choco.cp.solver.constraints.global.geost.layers.GeostNumeric:synchronize:External Constraints are not supported yet.");

    }

    public Region propagate(Obj o) {
        //Returns a region /*containing*/ the solution
        LOGGER.info("--Entering GeostNumeric:propagate()");

        int k = o.getCoordinates().length;
        if (contractorName.get(o) == null) {
            return new Region(k, o);
        }
        LOGGER.info("calling contract(object" + o.getObjectId() + ") because of contractorName.get(" + o + ")=" + contractorName.get(o));

        engine.contract("object" + o.getObjectId());
        Region r = new Region(k, o.getObjectId());
        for (int i = 0; i < k; i++) {
            //Probleme de conversion inverse ici
            String id = format("o{0}[{1}]", o.getObjectId(), i);
            double lb = engine.get_lb(id);
            double ub = engine.get_ub(id);
            int lb_int = (int) Math.floor(lb * cr);
            int ub_int = (int) Math.ceil(ub * cr);
            r.setMinimumBoundary(i, lb_int);
            r.setMaximumBoundary(i, ub_int);
        }
        LOGGER.info("--Exiting GeostNumeric:propagate()");
        return r;
    }


    public void prune(Obj o, int k, List<InternalConstraint> ictrs) throws ContradictionException {
        LOGGER.info("Entering Prune:" + o + "," + k + "," + ictrs);
        //returns no value, but throws a contradiction exception if failure occurs
        //call engine to propagate
        synchronize();
        Region box = propagate(o);
        //update o with box; set b to true if o is modified.
        for (int i = 0; i < k; i++) {
            int min = box.getMinimumBoundary(i);
            int max = box.getMaximumBoundary(i);
            int min_ori = o.getCoord(i).getInf();
            int max_ori = o.getCoord(i).getSup();
            LOGGER.info("Prune():" + o + "[" + i + "] updated to [" + min + "," + max + "]");

            boolean var_was_modified = false;
            if (min > min_ori) {
                var_was_modified = true;
                //Detect failure to update b
                o.getCoord(i).setInf(min);
            }
            if (max < max_ori) {
                var_was_modified = true;
                o.getCoord(i).setSup(max);
            }

            //TODO: synchronize variables only when they are modified
            if (var_was_modified) {
                synchronize(o.getCoord(i)); //(A)
            }
            LOGGER.info("Prune():synchronize o[" + i + "]=[" + o.getCoord(i).getInf() + "," + o.getCoord(i).getSup() + "]");
        }
        //The following line is an alternative to the synchronization in line (A)
        //if (b) synchronize(o); //(B)
        //(B) is simply more computational-expensive than (A)
        LOGGER.info("Exiting Prune()");
    }


}


//        for (ExternalConstraint ectr : stp.getConstraints()) {
//        }
//
//
//
//
//        //new ComponentConstraint(ConstraintType.GEOST, new Object[]{dim, shiftedBoxes, eCtrs, objects, ctrlVs, opt}, vars);
//            Object[] params = (Object[])parameters;
//            int dim = (Integer)params[0];
//            Vector<ShiftedBox> shiftedBoxes = (Vector<ShiftedBox>)params[1];
//            Vector<IExternalConstraint> ectr = (Vector<IExternalConstraint>)params[2];
//            Vector<GeostObject> vgo = (Vector<GeostObject>)params[3];
//            Vector<int[]> ctrlVs = (Vector<int[]>)params[4];
//            GeostOptions opt = (GeostOptions) params[5];
//            if (opt==null) { opt=new GeostOptions(); }
//
//            CPSolver solver = (CPSolver) stp.getObject(0).getCoord(0).getSolver();
//
//            //Transformation of Geost Objects (model) to interval geost object (constraint)
//            Vector<Obj> vo = new Vector<Obj>(vgo.size());
//            for (int i = 0; i < vgo.size(); i++) {
//                GeostObject g = vgo.elementAt(i);
//                vo.add(i, new Obj(g.getDim(),
//                        g.getObjectId(),
//                        solver.getVar(g.getShapeId()),
//                        solver.getVar(g.getCoordinates()),
//                        solver.getVar(g.getStartTime()),
//                        solver.getVar(g.getDurationTime()),
//                        solver.getVar(g.getEndTime()),
//                        g.getRadius())
//                        );
//            }
//
//
//        String vars="";
//        int k=stp.getObject(0).getCoordinates().length;
//        int index=0;
//        //for each object i
//        for (int i=0; i<stp.getObjectKeySet().size(); i++) {
//            Obj o = stp.getObject(i);
//            //declare variable domain
//            cvars.put(i,index);
//            vars+="o"+i+"["+k+"] in ";
//            vars+="[";
//            for (int d=0; d<k; d++) {
//                vars+="[";
//                vars+=o.getCoord(d).getInf()+",";
//                vars+=o.getCoord(d).getSup();
//                if (d==k-1) vars+="]"; else vars+="];";
//            }
//            vars+="];\n";
//            index+=2;
//
//
//            //Assign a parameter for all variables related to o_i
//           for (ExternalConstraint ect : stp.getConstraints() ) {
//                if (ect instanceof DistLeq) {
//                    DistLeq dl = (DistLeq) ect;
//                    //pars[i][dl.o2]=
//
//                }
//
//            }
//
//            String constraints="";
//            int nextCstr=0;
//            //for each constraint attached to object i
//            for (ExternalConstraint ect : stp.getConstraints() ) {
//                if (ect instanceof DistLeq) {
//                    DistLeq dl = (DistLeq) ect;
//                    constraints+="constraint obj"+i+"_c"+nextCstr+"\n";
//                    constraints+=" distance(o"+i+",p"+pars.get(dl.getObjectIds()[1])+")<=p"+pars.getVarCstr(dl.getCstrId())+";\n";
//                    constraints+="end\n";
//
//                }
//            }
//
//
//
//
//
//        }
//

