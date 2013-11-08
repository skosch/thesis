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

//     ___  ___         PaLM library for Choco system
//    /__ \/ __\        (c) 2001 - 2004 -- Narendra Jussien
//   ____\ |/_____
//  /_____\/_____ \     PalmExplanation based constraint programming tool
// |/   (_)(_)   \|
//       \ /            Version 0.1
//       \ /            January 2004
//       \ /
// ______\_/_______     Contibutors: Fran�ois Laburthe, Hadrien Cambazard, Guillaume Rochart...

package choco.ecp.solver.variables.real;

import choco.ecp.solver.PalmSolver;
import choco.ecp.solver.constraints.real.exp.PalmRealIntervalConstant;
import choco.ecp.solver.explanations.Explanation;
import choco.kernel.solver.variables.real.RealInterval;
import choco.kernel.solver.variables.real.RealMath;

/**
 * Explained interval arithmetic.
 */
public class PalmRealMath extends RealMath {
  public static PalmRealInterval add(PalmSolver pb, RealInterval x, RealInterval y) {
    Explanation expOnInf = pb.makeExplanation();
    Explanation expOnSup = pb.makeExplanation();
    ((PalmRealInterval) x).self_explain(PalmRealInterval.INF, expOnInf);
    ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnInf);

    ((PalmRealInterval) x).self_explain(PalmRealInterval.SUP, expOnSup);
    ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnSup);
    return new PalmRealIntervalConstant(RealMath.prevFloat(x.getInf() + y.getInf()),
        RealMath.nextFloat(x.getSup() + y.getSup()), expOnInf, expOnSup);
  }

  public static PalmRealInterval sub(PalmSolver pb, RealInterval x, RealInterval y) {
    Explanation expOnInf = pb.makeExplanation();
    Explanation expOnSup = pb.makeExplanation();
    ((PalmRealInterval) x).self_explain(PalmRealInterval.INF, expOnInf);
    ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnInf);

    ((PalmRealInterval) x).self_explain(PalmRealInterval.SUP, expOnSup);
    ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnSup);
    return new PalmRealIntervalConstant(RealMath.prevFloat(x.getInf() - y.getSup()),
        RealMath.nextFloat(x.getSup() - y.getInf()), expOnInf, expOnSup);
  }

  public static PalmRealInterval mul(PalmSolver pb, RealInterval x, RealInterval y) {
    double i, s;
    Explanation expOnInf = pb.makeExplanation();
    Explanation expOnSup = pb.makeExplanation();

    /*((PalmRealInterval)x).self_explain(PalmRealInterval.DOM, expOnInf);
    ((PalmRealInterval)x).self_explain(PalmRealInterval.DOM, expOnSup);
    ((PalmRealInterval)y).self_explain(PalmRealInterval.DOM, expOnInf);
    ((PalmRealInterval)y).self_explain(PalmRealInterval.DOM, expOnSup);*/

    if ((x.getInf() == 0.0 && x.getSup() == 0.0)) {
      i = 0.0;
      s = RealMath.NEG_ZER0; // Ca peut etre utile pour rejoindre des intervalles : si on veut aller de -5 a 0,
      // ca sera 0-.
      ((PalmRealInterval) x).self_explain(PalmRealInterval.DOM, expOnInf);
      ((PalmRealInterval) x).self_explain(PalmRealInterval.DOM, expOnSup);
    } else if ((y.getInf() == 0.0 && y.getSup() == 0.0)) {
      i = 0.0;
      s = RealMath.NEG_ZER0;
      ((PalmRealInterval) y).self_explain(PalmRealInterval.DOM, expOnInf);
      ((PalmRealInterval) y).self_explain(PalmRealInterval.DOM, expOnSup);
    } else {
      if (x.getInf() >= 0.0) {
        ((PalmRealInterval) x).self_explain(PalmRealInterval.INF, expOnInf);
        ((PalmRealInterval) x).self_explain(PalmRealInterval.INF, expOnSup);
        if (y.getInf() >= 0.0) {
          ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnInf);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnSup);
          // Inf Bound
          // No more explanations
          i = Math.max(RealMath.ZERO, RealMath.prevFloat(x.getInf() * y.getInf())); // Si x et y positifs, on ne veut pas etre n?gatif !
          // Sup Bound
          // Upper bounds of x and y explanations
          ((PalmRealInterval) x).self_explain(PalmRealInterval.SUP, expOnSup);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnSup);
          s = RealMath.nextFloat(x.getSup() * y.getSup());
        } else if (y.getSup() <= 0.0) {
          ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnInf);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnSup);
          // Inf Bound
          // X sup and Y inf
          ((PalmRealInterval) x).self_explain(PalmRealInterval.SUP, expOnInf);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnInf);
          i = RealMath.prevFloat(x.getSup() * y.getInf());
          // Sup Bound
          s = Math.min(RealMath.ZERO, RealMath.nextFloat(x.getInf() * y.getSup()));
        } else {
          // Inf Bound
          // X sup and Y inf
          ((PalmRealInterval) x).self_explain(PalmRealInterval.SUP, expOnInf);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnInf);
          i = RealMath.prevFloat(x.getSup() * y.getInf());
          // Sup Bound
          // X sup and Y sup
          ((PalmRealInterval) x).self_explain(PalmRealInterval.SUP, expOnSup);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnSup);
          s = RealMath.nextFloat(x.getSup() * y.getSup());
        }
      } else if (x.getSup() <= 0.0) {
        ((PalmRealInterval) x).self_explain(PalmRealInterval.SUP, expOnInf);
        ((PalmRealInterval) x).self_explain(PalmRealInterval.SUP, expOnSup);
        if (y.getInf() >= 0.0) {
          ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnInf);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnSup);
          // Inf Bound
          // X inf and Y sup
          ((PalmRealInterval) x).self_explain(PalmRealInterval.INF, expOnInf);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnInf);
          i = RealMath.prevFloat(x.getInf() * y.getSup());
          // Sup Bound
          // -
          s = Math.min(RealMath.ZERO, RealMath.nextFloat(x.getSup() * y.getInf()));
        } else if (y.getSup() <= 0.0) {
          ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnInf);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnSup);
          // Inf Bound
          // -
          i = Math.max(RealMath.ZERO, RealMath.prevFloat(x.getSup() * y.getSup()));
          // Sup Bound
          // X inf and Y inf
          ((PalmRealInterval) x).self_explain(PalmRealInterval.INF, expOnSup);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnSup);
          s = RealMath.nextFloat(x.getInf() * y.getInf());
        } else {
          // Inf Bound
          // X inf and Y sup
          ((PalmRealInterval) x).self_explain(PalmRealInterval.INF, expOnInf);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnInf);
          i = RealMath.prevFloat(x.getInf() * y.getSup());
          // Sup Bound
          // X inf and Y inf
          ((PalmRealInterval) x).self_explain(PalmRealInterval.INF, expOnSup);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnSup);
          s = RealMath.nextFloat(x.getInf() * y.getInf());
        }
      } else {
        if (y.getInf() >= 0.0) {
          ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnInf);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnSup);
          // Inf Bound
          // X inf and Y sup
          ((PalmRealInterval) x).self_explain(PalmRealInterval.INF, expOnInf);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnInf);
          i = RealMath.prevFloat(x.getInf() * y.getSup());
          // Sup Bound
          // X sup and Y sup
          ((PalmRealInterval) x).self_explain(PalmRealInterval.SUP, expOnSup);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnSup);
          s = RealMath.nextFloat(x.getSup() * y.getSup());
        } else if (y.getSup() <= 0.0) {
          ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnInf);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnSup);
          // Inf Bound
          // X sup and Y inf
          ((PalmRealInterval) x).self_explain(PalmRealInterval.SUP, expOnInf);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnInf);
          i = RealMath.prevFloat(x.getSup() * y.getInf());
          // Sup Bound
          // X inf and Y inf
          ((PalmRealInterval) x).self_explain(PalmRealInterval.INF, expOnSup);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnSup);
          s = RealMath.nextFloat(x.getInf() * y.getInf());
        } else {
          ((PalmRealInterval) x).self_explain(PalmRealInterval.DOM, expOnInf);
          ((PalmRealInterval) x).self_explain(PalmRealInterval.DOM, expOnSup);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.DOM, expOnInf);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.DOM, expOnSup);
          i = Math.min(RealMath.prevFloat(x.getInf() * y.getSup()),
              RealMath.prevFloat(x.getSup() * y.getInf()));
          s = Math.max(RealMath.nextFloat(x.getInf() * y.getInf()),
              RealMath.nextFloat(x.getSup() * y.getSup()));
        }
      }
    }

    return new PalmRealIntervalConstant(i, s, expOnInf, expOnSup);
  }

  /**
   * y should not contain 0 !
   *
   * @param x
   * @param y
   * @return TODO
   */
  public static PalmRealInterval odiv(PalmSolver pb, RealInterval x, RealInterval y) {
    Explanation expOnInf = pb.makeExplanation();
    Explanation expOnSup = pb.makeExplanation();

    /*((PalmRealInterval)x).self_explain(PalmRealInterval.DOM, expOnInf);
    ((PalmRealInterval)x).self_explain(PalmRealInterval.DOM, expOnSup);
    ((PalmRealInterval)y).self_explain(PalmRealInterval.DOM, expOnInf);
    ((PalmRealInterval)y).self_explain(PalmRealInterval.DOM, expOnSup);*/

    if (y.getInf() <= 0.0 && y.getSup() >= 0.0) {
      throw new UnsupportedOperationException();
    } else {
      double yl = y.getInf();
      double yh = y.getSup();
      double i, s;
      if (yh == 0.0) yh = RealMath.NEG_ZER0;

      if (x.getInf() >= 0.0) {
        ((PalmRealInterval) x).self_explain(PalmRealInterval.INF, expOnInf);
        ((PalmRealInterval) x).self_explain(PalmRealInterval.INF, expOnSup);
        if (yl >= 0.0) {
          ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnInf);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnSup);
          // Inf Bound
          // Y sup
          ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnInf);
          i = Math.max(RealMath.ZERO, RealMath.prevFloat(x.getInf() / yh));
          // Sup Bound
          // X sup
          ((PalmRealInterval) x).self_explain(PalmRealInterval.SUP, expOnSup);
          s = RealMath.nextFloat(x.getSup() / yl);
        } else { // yh <= 0
          ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnInf);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnSup);
          // Inf Bound
          // X sup
          ((PalmRealInterval) x).self_explain(PalmRealInterval.SUP, expOnInf);
          i = RealMath.prevFloat(x.getSup() / yh);
          // Sup Bound
          // Y inf
          ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnSup);
          s = Math.min(RealMath.ZERO, RealMath.nextFloat(x.getInf() / yl));
        }
      } else if (x.getSup() <= 0.0) {
        ((PalmRealInterval) x).self_explain(PalmRealInterval.SUP, expOnInf);
        ((PalmRealInterval) x).self_explain(PalmRealInterval.SUP, expOnSup);
        if (yl >= 0.0) {
          ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnInf);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnSup);
          // Inf Bound
          // X inf
          ((PalmRealInterval) x).self_explain(PalmRealInterval.INF, expOnInf);
          i = RealMath.prevFloat(x.getInf() / yl);
          // Sup Bound
          // Y sup
          ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnSup);
          s = Math.min(RealMath.ZERO, RealMath.nextFloat(x.getSup() / yh));
        } else {
          ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnInf);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnSup);
          // Inf Bound
          // Y inf
          ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnInf);
          i = Math.max(RealMath.ZERO, RealMath.prevFloat(x.getSup() / yl));
          // Sup Bound
          // X inf
          ((PalmRealInterval) x).self_explain(PalmRealInterval.INF, expOnSup);
          s = RealMath.nextFloat(x.getInf() / yh);
        }
      } else {
        if (yl >= 0.0) {
          ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnInf);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.INF, expOnSup);
          // Inf Bound
          // X inf
          ((PalmRealInterval) x).self_explain(PalmRealInterval.INF, expOnInf);
          i = RealMath.prevFloat(x.getInf() / yl);
          // Sup Bound
          // X sup
          ((PalmRealInterval) x).self_explain(PalmRealInterval.SUP, expOnSup);
          s = RealMath.nextFloat(x.getSup() / yl);
        } else {
          ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnInf);
          ((PalmRealInterval) y).self_explain(PalmRealInterval.SUP, expOnSup);
          // Inf Bound
          // X sup
          ((PalmRealInterval) x).self_explain(PalmRealInterval.SUP, expOnInf);
          i = RealMath.prevFloat(x.getSup() / yh);
          // Sup Bound
          // X inf
          ((PalmRealInterval) x).self_explain(PalmRealInterval.INF, expOnSup);
          s = RealMath.nextFloat(x.getInf() / yh);
        }
      }
      return new PalmRealIntervalConstant(i, s, expOnInf, expOnSup);
    }
  }

  public static PalmRealInterval odiv_wrt(PalmSolver pb, RealInterval x, RealInterval y, RealInterval res) {
    if (y.getInf() > 0.0 || y.getSup() < 0.0) {  // y != 0
      return odiv(pb, x, y);
    } else {
      double resl = res.getInf();
      double resh = res.getSup();
      Explanation expOnInf = pb.makeExplanation();
      Explanation expOnSup = pb.makeExplanation();
      // TODO : voir si on peut faire mieux !
      ((PalmRealInterval) x).self_explain(PalmRealInterval.DOM, expOnInf);
      ((PalmRealInterval) x).self_explain(PalmRealInterval.DOM, expOnSup);
      ((PalmRealInterval) y).self_explain(PalmRealInterval.DOM, expOnInf);
      ((PalmRealInterval) y).self_explain(PalmRealInterval.DOM, expOnSup);
      ((PalmRealInterval) res).self_explain(PalmRealInterval.DOM, expOnInf);
      ((PalmRealInterval) res).self_explain(PalmRealInterval.DOM, expOnSup);

      if (x.getInf() >= 0.0) {
        double tmp_neg = RealMath.nextFloat(x.getInf() / y.getInf()); // la plus grande valeur negative
        double tmp_pos = RealMath.prevFloat(x.getInf() / y.getSup()); // la plus petite valeur positive

        if ((resl > tmp_neg || resl == 0.0) && resl < tmp_pos) resl = tmp_pos;
        if ((resh < tmp_pos || resh == 0.0) && resh > tmp_neg) resh = tmp_neg;
      } else if (x.getSup() <= 0.0) {
        double tmp_neg = RealMath.nextFloat(x.getSup() / y.getSup());
        double tmp_pos = RealMath.nextFloat(x.getSup() / y.getInf());

        if ((resl > tmp_neg || resl == 0.0) && resl < tmp_pos) resl = tmp_pos;
        if ((resh < tmp_pos || resh == 0.0) && resh > tmp_neg) resh = tmp_neg;
      }

      return new PalmRealIntervalConstant(resl, resh, expOnInf, expOnSup);
    }
  }
}
