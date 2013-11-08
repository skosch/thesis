package choco.cp.common.util.preprocessor.detector.scheduling;

import static choco.Choco.clause;

import java.util.ArrayList;

import choco.cp.model.CPModel;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.TemporalConstraint;
import choco.kernel.model.variables.integer.IntegerVariable;

public class ClauseFromDisjointModelDetector extends
AbstractSchedulingDectector {


	private ArrayList<IntegerVariable> posvars = new ArrayList<IntegerVariable>(2);
	private ArrayList<IntegerVariable> negvars = new ArrayList<IntegerVariable>(2);

	public ClauseFromDisjointModelDetector(CPModel model,
			DisjunctiveModel disjMod) {
		super(model, disjMod);
	}

	private void clearLits() {
		posvars.clear();
		negvars.clear();
	}

	private void addPosLit(int o, int d, TemporalConstraint ct) {
		( ct.getOrigin().getHook() == o ? posvars : negvars).add(ct.getDirection());
	}

	private void addNegLit(int o, int d, TemporalConstraint ct) {
		( ct.getOrigin().getHook() == o ? negvars : posvars).add(ct.getDirection());
	}

	private  void addClause() {
		add(clause(
				posvars.toArray(new IntegerVariable[posvars.size()]), 
				negvars.toArray(new IntegerVariable[negvars.size()])
				));
		clearLits();
	}

	@Override
	public void apply() {
		final int n = disjMod.getNbNodes();
		//always based on floyd marshall algorithm
		for (int k = 0; k < n; k++) {
			for (int i = 0; i < n; i++) {
				if(disjMod.containsEdge(i, k)) {
					final TemporalConstraint cik = disjMod.getEdgeConstraint(i, k);
					if(cik.isInPreprocess()) {
						for (int j = 0; j < n; j++) {
							if(disjMod.containsEdge(k, j)) {
								final TemporalConstraint ckj = disjMod.getEdgeConstraint(k, j);
								if(ckj.isInPreprocess()) {
									if(disjMod.containsEdge(i , j)) {
										final TemporalConstraint cij = disjMod.getEdgeConstraint(i, j);
										if(cik.isInPreprocess()) {
											//clause 1
											addPosLit(i, j, cij);
											addNegLit(i, k, cik);
											addNegLit(k, j, ckj);
											addClause();
											//clause 2
											addNegLit(i, j, cij);
											addPosLit(i, k, cik);
											addPosLit(k, j, ckj);
											addClause();
										}
									} else if(disjMod.containsArc(i , j)) {
										addPosLit(i, k, cik);
										addPosLit(k, j, ckj);
										addClause();
									} else if(disjMod.containsArc(j , i)) {
										addNegLit(i, k, cik);
										addNegLit(k, j, ckj);
										addClause();
									}
								}
							}
						}
					}

				}
			}
		}
	}
}