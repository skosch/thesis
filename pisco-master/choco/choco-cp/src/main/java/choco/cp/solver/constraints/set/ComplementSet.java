package choco.cp.solver.constraints.set;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.set.AbstractBinSetSConstraint;
import choco.kernel.solver.variables.set.SetVar;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 15 déc. 2010
 * Time: 09:20:33
 <p/>
 * X set-variable
 * Y set-variable
 *
 * complement(X) == Y
 * i.e.
 * let min = minimum possible value for x or y
 * and max = maximum possible value for x or y
 *
 * forall i in min .. max,
 *      i in x <=> i notin y
 *
 *
 * <p/>
 * */
public class ComplementSet extends AbstractBinSetSConstraint {

    int minvalue, maxvalue;

    public ComplementSet(SetVar v0, SetVar v1) {
        super(v0, v1);
        minvalue = Math.min(v0.getEnveloppeInf(), v1.getEnveloppeInf());
        maxvalue = Math.max(v0.getEnveloppeSup(), v1.getEnveloppeSup());
    }

    @Override
    public void propagate() throws ContradictionException {
        if (v0.isInstantiated() && v1.isInstantiated()) {
            if (! isSatisfied())  this.fail();
        }
        else {
            // ce qui est dans le noyau de l'un doit disparaître de l'enveloppe de l'autre
            DisposableIntIterator itker = v0.getDomain().getKernelIterator();
            while (itker.hasNext()) {
                int val = itker.next();
                v1.remFromEnveloppe(val, this, true);
            }

            itker = v1.getDomain().getKernelIterator();
            while (itker.hasNext()) {
                int val = itker.next();
                v0.remFromEnveloppe(val, this, true);
            }

            // make sure that all values are still available
            for (int val = minvalue ; val <= maxvalue ; val++) {
                if (! v0.isInDomainEnveloppe(val) && ! v1.isInDomainEnveloppe(val)) {
                    this.fail();
                }
            }
        }
    }

    @Override
    public void awakeOnKer(int varIdx, int x) throws ContradictionException {
        SetVar ov = (varIdx == 0) ? v1 : v0;
        ov.remFromEnveloppe(x, this, true);
    }

    @Override
    public void awakeOnEnv(int varIdx, int x) throws ContradictionException {
        SetVar ov = (varIdx == 0) ? v1 : v0;
        ov.addToKernel(x, this, true);
    }

    @Override
    public boolean isConsistent() {
        return isSatisfied();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isSatisfied() {
        for (int val = minvalue ; val <= maxvalue ; val++) {
            if ( (v0.isInDomainKernel(val) && v1.isInDomainKernel(val))
                    || (! v0.isInDomainKernel(val) && ! v1.isInDomainKernel(val))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "complement(" + v0 + ") == " + v1;
    }

    @Override
    public String pretty() {
        return "complement(" + v0.pretty() + ") == " + v1.pretty();
    }
}
