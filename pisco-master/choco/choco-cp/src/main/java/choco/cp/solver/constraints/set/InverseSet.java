package choco.cp.solver.constraints.set;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.set.AbstractLargeSetSConstraint;
import choco.kernel.solver.variables.set.SetVar;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 15 déc. 2010
 * Time: 12:59:12
 <p/>
 * X collection set-variable
 * Y collection set-variable
 *
 * Y should have enough slots to handle X domain size (ie. Y.length <= X.max)
 *
 * j in X[i]  <=> i in Y[j]
 *
 * cf. http://www.emn.fr/z-info/sdemasse/gccat/Cinverse_set.html
 *
 *
 * <p/>
 * */
public class InverseSet extends AbstractLargeSetSConstraint {

    int varoffset;
    SetVar[] x;
    SetVar[] y;

    public InverseSet(SetVar[] x, SetVar[] y) {
        super(ArrayUtils.append(x, y));
        varoffset = x.length;
        this.x = x;
        this.y = y;
    }

    @Override
    public void awake() throws ContradictionException {
        for (int idx = 0; idx < x.length ; idx++) {
           SetVar var = x[idx];
           for (int i = var.getEnveloppeInf() ; i<= var.getEnveloppeSup() ; i++) {
               if (var.isInDomainEnveloppe(i)) {
                   if (i >= y.length) {
                       var.remFromEnveloppe(i, this, false);
                   }
                   else if (! y[i].isInDomainEnveloppe(idx) ) {
                       var.remFromEnveloppe(i, this, false);
                   }
               }
           }

        }

        for (int idx = 0; idx < y.length ; idx++) {
           SetVar var = y[idx];
           for (int i = var.getEnveloppeInf() ; i<= var.getEnveloppeSup() ; i++) {
               if (var.isInDomainEnveloppe(i)) {
                   if (i >= x.length) {
                       var.remFromEnveloppe(i, this, false);
                   }
                   else if (! x[i].isInDomainEnveloppe(idx) ) {
                       var.remFromEnveloppe(i, this, false);
                   }
               }
           }

        }

        for (int idx = 0; idx < x.length ; idx++) {
            SetVar var = x[idx];
            DisposableIntIterator it = var.getDomain().getKernelIterator();
            while (it.hasNext()) {
                awakeOnKer(idx, it.next());
            }
        }

        for (int idx = 0; idx < y.length ; idx++) {
            SetVar var = y[idx];
            DisposableIntIterator it = var.getDomain().getKernelIterator();
            while (it.hasNext()) {
                awakeOnKer(idx + varoffset, it.next());
            }
        }
    }

    @Override
    public void propagate() throws ContradictionException {

        boolean allinstance = true;
        for (SetVar var : vars) {
            if (! var.isInstantiated()) {
                allinstance = false;
                break;
            }
        }

        if (allinstance && ! isSatisfied()) this.fail();
    }


    // X[varIdx] <- x
    // Y[x] <- varIdx

    @Override
    public void awakeOnKer(int varIdx, int x) throws ContradictionException {
        int var = (varIdx < varoffset) ? x + varoffset : x;
        int val = (varIdx < varoffset) ? varIdx : varIdx - varoffset;
        vars[var].addToKernel(val, this, false);
    }

    @Override
    public void awakeOnEnv(int varIdx, int x) throws ContradictionException {
        int var = (varIdx < varoffset) ? x + varoffset : x;
        int val = (varIdx < varoffset) ? varIdx : varIdx - varoffset;
        vars[var].remFromEnveloppe(val, this, false);
    }

    @Override
    public boolean isConsistent() {
        return isSatisfied();  //To change body of implemented methods use File | Settings | File Templates.
    }


    public boolean isSatisfied() {
        for (int i = 0; i < vars.length; i++) {
            SetVar var = vars[i];
            DisposableIntIterator itker = var.getDomain().getKernelIterator();

            while (itker.hasNext()) {
                int val = itker.next();
                int ov = (i < varoffset) ? val + varoffset : val;
                int v  = (i < varoffset) ? i : i - varoffset;

                if (! vars[ov].isInDomainKernel(v)) {
                    return false;
                }
            }
        }

        return true;
    }
}
