package choco.cp.solver.constraints.set;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.set.AbstractBinSetSConstraint;
import choco.kernel.solver.variables.set.SetVar;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 14 déc. 2010
 * Time: 11:06:17
 *
 * <p/>
 * X set-variable
 * Y set-variable
 *
 * X <=lex Y
 *
 *
 * <p/>
 * */
public class SetLexicographicOrdering extends AbstractBinSetSConstraint{

    public SetLexicographicOrdering(SetVar v0, SetVar v1) {
        super(v0, v1);
    }

    public String toString() {
        return v0 + " <=lex " + v1;
    }

    public String pretty() {
        return v0.pretty() + " <=lex " + v1.pretty();
    }

    @Override
    public void propagate() throws ContradictionException {
        if (v0.isInstantiated() && v1.isInstantiated()) {
            if (! isSatisfied()) {
                this.fail();
            }
        } else { // on ne peut pas faire grand-chose à part toujours s'assurer qu'il reste des valeurs dispos dans
            // l'enveloppe quand une différence est constatée sur les noyaux
            if (v0.isInstantiated()) {
                if (v0.getKernelDomainSize() > 0 && v1.getKernelDomainSize() > 0) {
                    // avancer conjointement sur les noyaux et s'arrêter dès qu'il y a un problème
                    DisposableIntIterator itker0 = v0.getDomain().getKernelIterator();
                    DisposableIntIterator itker1 = v1.getDomain().getKernelIterator();
                    int elem0, elem1 ;
                    do {
                        elem0 = itker0.next();
                        elem1 = itker1.next();
                    } while (itker0.hasNext() && itker1.hasNext() && (elem0 == elem1));

                }
            }

            if (v0.getKernelDomainSize() > 0 && v1.getKernelDomainSize() > 0) {
                // avancer conjointement sur les noyaux et s'arrêter dès qu'il y a un problème
                DisposableIntIterator itker0 = v0.getDomain().getKernelIterator();
                DisposableIntIterator itker1 = v1.getDomain().getKernelIterator();
                int elem0, elem1 ;
                do {
                    elem0 = itker0.next();
                    elem1 = itker1.next();
                } while (itker0.hasNext() && itker1.hasNext() && (elem0 == elem1));

                if (elem0 != elem1) { // il y a une différence sur les noyaux
                    if (elem0 > elem1) { // il faut s'assurer qu'il y a une valeur correcte dispo en face
                        DisposableIntIterator itenv0 = v0.getDomain().getEnveloppeIterator();
                        boolean safe = false;
                        while (!safe && itenv0.hasNext())  {
                            int val = itenv0.next();
                            if (! v0.isInDomainKernel(val) && (val <= elem1)) {
                                safe = true;
                            }
                        }
                        if (! safe) {
                            this.fail();
                        }
                    }
                }
            }


       }
    }

    @Override
    public boolean isConsistent() {
        return isSatisfied();
    }

    public boolean isSatisfied() {
        if (v0.getKernelDomainSize() > 0 && v1.getKernelDomainSize() > 0) {
            DisposableIntIterator itker0 = v0.getDomain().getKernelIterator();
            DisposableIntIterator itker1 = v1.getDomain().getKernelIterator();
            int elem0, elem1 ;
            do {
                elem0 = itker0.next();
                elem1 = itker1.next();
            } while (itker0.hasNext() && itker1.hasNext() && (elem0 == elem1));

            if (elem0 != elem1) {
                return elem0 <= elem1;
            } else {
                if (! itker1.hasNext() ) {
                    return ! itker0.hasNext(); // il faut que ker0 soit fini aussi, sinon problème
                }
                else {
                    return true; // ker1 pas fini et ker0 fini ... pas de problème
                }
            }
        } else {
            if (v0.getKernelDomainSize() == 0){
                return true; // anything can work for v1
            }
            return false;
        }
    }
}
