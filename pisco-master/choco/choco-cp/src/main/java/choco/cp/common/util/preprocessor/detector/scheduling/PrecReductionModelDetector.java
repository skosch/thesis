package choco.cp.common.util.preprocessor.detector.scheduling;

import java.util.BitSet;

import choco.cp.model.CPModel;
import choco.kernel.model.constraints.TemporalConstraint;

public class PrecReductionModelDetector extends AbstractSchedulingDectector {

	public PrecReductionModelDetector(CPModel model, DisjunctiveModel disjMod) {
		super(model, disjMod);
	}

	@Override
	public void apply() {
		final BitSet[] reduction = disjMod.copyPrecGraph(); //get prec closure
		DisjunctiveGraph.floydMarshallReduction(reduction);
		disjMod.unsetPrecClosure();
		final BitSet[] transitive = disjMod.copyPrecGraph(); // get prec graph
		DisjunctiveGraph.andNot(transitive, reduction);
		for (int i = 0; i < transitive.length; i++) {
			for (int j = transitive[i].nextSetBit(0); j >= 0; j = transitive[i]
					.nextSetBit(j + 1)) {
				if(disjMod.containsConstraint(i, j)) {
					final TemporalConstraint ct = disjMod.getConstraint(i, j);
					if(ct.isInPreprocess() && 
							ct.backwardSetup() == 0 && 
							ct.forwardSetup() == 0) {
						//a transitive precedence without setup time can be deleted
						disjMod.deleteArc(i, j);
						delete(ct);
					}
				}
			}
		}
	}

}
