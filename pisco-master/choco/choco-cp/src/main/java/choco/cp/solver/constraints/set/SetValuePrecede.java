package choco.cp.solver.constraints.set;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.set.AbstractLargeSetSConstraint;
import choco.kernel.solver.variables.set.SetVar;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 17 déc. 2010
 * Time: 10:45:58
 *
 * <p/>
 * S int
 * T int
 * VARIABLES set-variables
 *
 * If there exists a set variable v1 of VARIABLES such that S does not belong to v1 and T does,
 * then there also exists a set variable v2 preceding v1 such that S belongs to v2 and T does not.
 *
 * <p/>
 *
 * based on the paper  
 * Y. C. Law, J. H. M. Lee,
 * Global Constraints for Integer and Set Value Precedence
 * Principles and Practice of Constraint Programming (CP'2004) LNCS 3258 Springer -Verlag M. G. Wallace, 362–376 2004
 *
 * propagation algorithm given on figure 4
 */
public class SetValuePrecede extends AbstractLargeSetSConstraint {
    int s;
    int t;
    int n;

    IStateInt alpha;
    IStateInt beta;
    IStateInt gamma;

    IEnvironment environment;

    public SetValuePrecede(int s, int t, SetVar[] svars, IEnvironment environment) {
        super(svars);
        this.s = s;
        this.t = t;
        this.n = vars.length;
        this.environment = environment;
    }


    private boolean checkKerEnv(SetVar v) {
        return ! v.isInDomainEnveloppe(s) || v.isInDomainKernel(t);
    }

    private void initialize() throws ContradictionException {
        alpha = environment.makeInt(0);

        while ( (alpha.get() < n) && checkKerEnv(vars[alpha.get()])) {
            updateVar(vars[alpha.get()]);
            alpha.add(1);
        }

        beta = environment.makeInt(alpha.get());
        gamma = environment.makeInt(alpha.get());

        if (alpha.get() < n ){
            do {
                gamma.add(1);
            }
            while ((gamma.get()) < n && (vars[gamma.get()].isInDomainEnveloppe(s) || ! vars[gamma.get()].isInDomainKernel(t)));
            updateBeta();
        }
    }

    private void updateBeta() throws ContradictionException {
        do {
            beta.add(1);
        }
        while (beta.get() < n && checkKerEnv(vars[beta.get()]));
        if (beta.get() > gamma.get()) {
            vars[alpha.get()].remFromEnveloppe(t, this, true);
            vars[alpha.get()].addToKernel(s, this, true);
        }
    }


    private void updateVar(SetVar v) throws ContradictionException {
        if (! v.isInDomainEnveloppe(s)) {
            v.remFromEnveloppe(t, this, true);
        }
        else {
            v.addToKernel(s, this, true);
        }
    }

    private void propagate(int idx) throws ContradictionException {

        if (beta.get() <= gamma.get()) {
            SetVar var = vars[idx];
            if ( (idx == alpha.get()) && checkKerEnv(var)) {
                updateVar(var);
                alpha.add(1);

                while (alpha.get() < beta.get()) {
                    updateVar(vars[alpha.get()]);
                    alpha.add(1);
                }
                while (alpha.get() < n && checkKerEnv(vars[alpha.get()])) {
                    updateVar(vars[alpha.get()]);
                    alpha.add(1);
                }
                beta.set(alpha.get());
                if (alpha.get() < n) updateBeta();
            }
            else if (idx == beta.get() && checkKerEnv(var)) {
                updateBeta();
            }
            checkGamma(idx);
        }
    }

    private void checkGamma(int idx) throws ContradictionException {
        if (beta.get() < gamma.get() && idx < gamma.get() && ! vars[idx].isInDomainEnveloppe(s) && vars[idx].isInDomainKernel(t)) {
            gamma.set(idx);
            if (beta.get() > idx) {
                vars[alpha.get()].remFromEnveloppe(t, this, true);
                vars[alpha.get()].addToKernel(s, this, true);
            }
        }
    }

    @Override
    public void awake() throws ContradictionException {              // première initialisation
        initialize();
        propagate();
    }

    @Override
    public void propagate() throws ContradictionException {         // autres appels
        for (int i = 0; i < n ; i++) {
            propagate(i);
        }
    }

    @Override
    public void awakeOnKer(int varIdx, int x) throws ContradictionException {
        propagate(varIdx);
    }

    @Override
    public void awakeOnEnv(int varIdx, int x) throws ContradictionException {
        propagate(varIdx);
    }

    @Override
    public void awakeOnInst(int varIdx) throws ContradictionException {
        propagate(varIdx);
    }

    @Override
    public boolean isConsistent() {
        return isSatisfied();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isSatisfied() {
        boolean check = vars[0].isInDomainKernel(s) || ! vars[0].isInDomainKernel(t);
        for (int j = 1; j < vars.length ; j++) {
            boolean tree = vars[0].isInDomainKernel(s) && ! vars[0].isInDomainKernel(t);
            for (int i = 0 ; i < j ; i++) {
                tree |= (vars[i].isInDomainKernel(s) && ! vars[i].isInDomainKernel(t));
            }
            check &= !(! vars[j].isInDomainKernel(s) && vars[j].isInDomainKernel(t)) || tree;
        }
        return check;
    }

    @Override
    public String pretty() {
        StringBuilder sb = new StringBuilder();
        sb.append("ValuePrecede("+ s + "," + t +",{");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) sb.append(", ");
            SetVar var = vars[i];
            sb.append(var.pretty());
        }
        sb.append("})");
        return sb.toString();
    }


    @Override
    public String toString() {
        String autstring = "ValuePrecede("+ s + "," + t +",[ ";
        for (int i = 0; i < vars.length; i++) {
            autstring += vars[i] + "]";
        }
        return autstring;

    }
}
