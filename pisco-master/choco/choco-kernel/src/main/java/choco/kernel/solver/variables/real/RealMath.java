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

package choco.kernel.solver.variables.real;

import choco.kernel.common.logging.ChocoLogging;

import java.util.logging.Logger;

/**
 * Some tools for float computing.
 * Inspired from IAMath : interval.sourceforge.net
 */
public class RealMath {
    protected final static Logger LOGGER = ChocoLogging.getEngineLogger();

  public static final double ZERO = 0.0;
  public static final double NEG_ZER0 = 0.0 * -1.0;

  public static double nextFloat(double x) {
    if (x < 0)
      return Double.longBitsToDouble(Double.doubleToLongBits(x) - 1);
    else if (x == 0)
      return Double.longBitsToDouble(1);
    else if (x < Double.POSITIVE_INFINITY)
      return Double.longBitsToDouble(Double.doubleToLongBits(x) + 1);
    else
      return x; // nextFloat(infty) = infty
  }

  public static double prevFloat(double x) {
    if (x == 0.0)
      return -nextFloat(0.0);
    else
      return -nextFloat(-x);
  }

  public static RealInterval add(RealInterval x, RealInterval y) {
    return new RealIntervalConstant(prevFloat(x.getInf() + y.getInf()), nextFloat(x.getSup() + y.getSup()));
  }

  public static RealInterval sub(RealInterval x, RealInterval y) {
    return new RealIntervalConstant(prevFloat(x.getInf() - y.getSup()), nextFloat(x.getSup() - y.getInf()));
  }

  public static RealInterval mul(RealInterval x, RealInterval y) {
    double i, s;

    if ((x.getInf() == 0.0 && x.getSup() == 0.0) || (y.getInf() == 0.0 && y.getSup() == 0.0)) {
      i = 0.0;
      s = NEG_ZER0; // Ca peut etre utile pour rejoindre des intervalles : si on veut aller de -5 a 0,
      // ca sera 0-.
    } else {
      if (x.getInf() >= 0.0) {
        if (y.getInf() >= 0.0) {
          i = Math.max(ZERO, prevFloat(x.getInf() * y.getInf())); // Si x et y positifs, on ne veut pas etre n?gatif !
          s = nextFloat(x.getSup() * y.getSup());
        } else if (y.getSup() <= 0.0) {
          i = prevFloat(x.getSup() * y.getInf());
          s = Math.min(ZERO, nextFloat(x.getInf() * y.getSup()));
        } else {
          i = prevFloat(x.getSup() * y.getInf());
          s = nextFloat(x.getSup() * y.getSup());
        }
      } else if (x.getSup() <= 0.0) {
        if (y.getInf() >= 0.0) {
          i = prevFloat(x.getInf() * y.getSup());
          s = Math.min(ZERO, nextFloat(x.getSup() * y.getInf()));
        } else if (y.getSup() <= 0.0) {
          i = Math.max(ZERO, prevFloat(x.getSup() * y.getSup()));
          s = nextFloat(x.getInf() * y.getInf());
        } else {
          i = prevFloat(x.getInf() * y.getSup());
          s = nextFloat(x.getInf() * y.getInf());
        }
      } else {
        if (y.getInf() >= 0.0) {
          i = prevFloat(x.getInf() * y.getSup());
          s = nextFloat(x.getSup() * y.getSup());
        } else if (y.getSup() <= 0.0) {
          i = prevFloat(x.getSup() * y.getInf());
          s = nextFloat(x.getInf() * y.getInf());
        } else {
          i = Math.min(prevFloat(x.getInf() * y.getSup()),
              prevFloat(x.getSup() * y.getInf()));
          s = Math.max(nextFloat(x.getInf() * y.getInf()),
              nextFloat(x.getSup() * y.getSup()));
        }
      }
    }

    return new RealIntervalConstant(i, s);
  }

  /**
   * y should not contain 0 !
   *
   * @param x
   * @param y
   * @return TODO
   */
  public static RealInterval odiv(RealInterval x, RealInterval y) {
    if (y.getInf() <= 0.0 && y.getSup() >= 0.0) {
      throw new UnsupportedOperationException();
    } else {
      double yl = y.getInf();
      double yh = y.getSup();
      double i, s;
      if (yh == 0.0) yh = NEG_ZER0;

      if (x.getInf() >= 0.0) {
        if (yl >= 0.0) {
          i = Math.max(ZERO, prevFloat(x.getInf() / yh));
          s = nextFloat(x.getSup() / yl);
        } else { // yh <= 0
          i = prevFloat(x.getSup() / yh);
          s = Math.min(ZERO, nextFloat(x.getInf() / yl));
        }
      } else if (x.getSup() <= 0.0) {
        if (yl >= 0.0) {
          i = prevFloat(x.getInf() / yl);
          s = Math.min(ZERO, nextFloat(x.getSup() / yh));
        } else {
          i = Math.max(ZERO, prevFloat(x.getSup() / yl));
          s = nextFloat(x.getInf() / yh);
        }
      } else {
        if (yl >= 0.0) {
          i = prevFloat(x.getInf() / yl);
          s = nextFloat(x.getSup() / yl);
        } else {
          i = prevFloat(x.getSup() / yh);
          s = nextFloat(x.getInf() / yh);
        }
      }
      return new RealIntervalConstant(i, s);
    }
  }

  public static RealInterval odiv_wrt(RealInterval x, RealInterval y, RealInterval res) {
    if (y.getInf() > 0.0 || y.getSup() < 0.0) {  // y != 0
      return odiv(x, y);
    } else {
      double resl = res.getInf();
      double resh = res.getSup();

      if (x.getInf() >= 0.0) {
        double tmp_neg = nextFloat(x.getInf() / y.getInf()); // la plus grande valeur negative
        double tmp_pos = prevFloat(x.getInf() / y.getSup()); // la plus petite valeur positive

        if ((resl > tmp_neg || resl == 0.0) && resl < tmp_pos) resl = tmp_pos;
        if ((resh < tmp_pos || resh == 0.0) && resh > tmp_neg) resh = tmp_neg;
      } else if (x.getSup() <= 0.0) {
        double tmp_neg = nextFloat(x.getSup() / y.getSup());
        double tmp_pos = nextFloat(x.getSup() / y.getInf());

        if ((resl > tmp_neg || resl == 0.0) && resl < tmp_pos) resl = tmp_pos;
        if ((resh < tmp_pos || resh == 0.0) && resh > tmp_neg) resh = tmp_neg;
      }

      return new RealIntervalConstant(resl, resh);
    }
  }

  public static boolean isCanonical(RealInterval i, double precision) {
    double inf = i.getInf();
    double sup = i.getSup();
    if (sup - inf < precision) return true;
    if (nextFloat(inf) >= sup) return true;
    return false;
  }

  public static RealInterval firstHalf(RealInterval i) {
    double inf = i.getInf();
    if (inf == Double.NEGATIVE_INFINITY) inf = -Double.MAX_VALUE;
    double sup = i.getSup();
    if (sup == Double.POSITIVE_INFINITY) sup = Double.MAX_VALUE;
    return new RealIntervalConstant(i.getInf(), inf + sup / 2.0 - inf / 2.0);
  }

  public static RealInterval secondHalf(RealInterval i) {
    double inf = i.getInf();
    if (inf == Double.NEGATIVE_INFINITY) inf = -Double.MAX_VALUE;
    double sup = i.getSup();
    if (sup == Double.POSITIVE_INFINITY) sup = Double.MAX_VALUE;
    return new RealIntervalConstant(inf + sup / 2.0 - inf / 2.0, i.getSup());
  }

  public static double iPower_lo(double x, int p) {   // TODO : to check !
    // x >= 0 et p > 1 entier
    if (x == 0) return 0;
    if (x == 1) return 1;
    return prevFloat(Math.exp(prevFloat(p * prevFloat(Math.log(x)))));
  }

  public static double iPower_up(double x, int p) {
    if (x == 0) return 0;
    if (x == 1) return 1;
    return nextFloat(Math.exp(nextFloat(p * nextFloat(Math.log(x)))));
  }

  private static RealInterval evenIPower(RealInterval i, int p) {
    double inf, sup;
    if (i.getInf() >= 0.0) {
      if (i.getInf() == Double.POSITIVE_INFINITY) {
        inf = Double.POSITIVE_INFINITY;
        sup = Double.POSITIVE_INFINITY;
      } else {
        inf = iPower_lo(i.getInf(), p);
        if (i.getSup() == Double.POSITIVE_INFINITY) {
          sup = Double.POSITIVE_INFINITY;
        } else {
          sup = iPower_up(i.getSup(), p);
        }
      }
    } else if (i.getSup() <= 0.0) {
      if (i.getSup() == Double.NEGATIVE_INFINITY) {
        inf = Double.POSITIVE_INFINITY;
        sup = Double.POSITIVE_INFINITY;
      } else {
        inf = iPower_lo(-i.getSup(), p);
        if (i.getInf() == Double.NEGATIVE_INFINITY) {
          sup = Double.POSITIVE_INFINITY;
        } else {
          sup = iPower_up(-i.getInf(), p);
        }
      }
    } else {
      inf = 0;
      if (i.getInf() == Double.NEGATIVE_INFINITY ||
          i.getSup() == Double.POSITIVE_INFINITY) {
        sup = Double.POSITIVE_INFINITY;
      } else {
        sup = Math.max(iPower_up(-i.getInf(), p),
            iPower_up(i.getSup(), p));
      }
    }
    return new RealIntervalConstant(inf, sup);
  }

  public static RealInterval oddIPower(RealInterval i, int p) {
    double inf, sup;
    if (i.getInf() >= 0.0) {
      if (i.getInf() == Double.POSITIVE_INFINITY) {
        inf = Double.POSITIVE_INFINITY;
        sup = Double.POSITIVE_INFINITY;
      } else {
        inf = iPower_lo(i.getInf(), p);
        if (i.getSup() == Double.POSITIVE_INFINITY) {
          sup = Double.POSITIVE_INFINITY;
        } else {
          sup = iPower_up(i.getSup(), p);
        }
      }
    } else if (i.getSup() <= 0.0) {
      if (i.getSup() == Double.NEGATIVE_INFINITY) {
        inf = Double.NEGATIVE_INFINITY;
        sup = Double.NEGATIVE_INFINITY;
      } else {
        sup = -iPower_lo(-i.getSup(), p);
        if (i.getInf() == Double.NEGATIVE_INFINITY) {
          inf = Double.NEGATIVE_INFINITY;
        } else {
          inf = -iPower_up(-i.getInf(), p);
        }
      }
    } else {
      if (i.getInf() == Double.NEGATIVE_INFINITY) {
        inf = Double.NEGATIVE_INFINITY;
      } else {
        inf = -iPower_up(-i.getInf(), p);
      }
      if (i.getSup() == Double.POSITIVE_INFINITY) {
        sup = Double.POSITIVE_INFINITY;
      } else {
        sup = iPower_up(i.getSup(), p);
      }
    }
    return new RealIntervalConstant(inf, sup);
  }

  public static RealInterval iPower(RealInterval i, int p) {
    if (p <= 1) {
      throw new UnsupportedOperationException();
    }
    if (p % 2 == 0) { // pair
      return evenIPower(i, p);
    } else { // impair
      return oddIPower(i, p);
    }
  }

  public static double iRoot_lo(double x, int p) { // TODO : to check !!
    double d_lo = prevFloat(1.0 / (double) p);
    double d_hi = nextFloat(1.0 / (double) p);
    if (x == Double.POSITIVE_INFINITY) {
      return Double.POSITIVE_INFINITY;
    } else if (x == 0)
      return 0;
    else if (x == 1)
      return 1;
    else if (x < 1)
      return prevFloat(Math.exp(prevFloat(d_hi * prevFloat(Math.log(x)))));
    else
      return prevFloat(Math.exp(prevFloat(d_lo * prevFloat(Math.log(x)))));
  }

  public static double iRoot_up(double x, int p) {
    double d_lo = prevFloat(1.0 / (double) p);
    double d_hi = nextFloat(1.0 / (double) p);
    if (x == Double.POSITIVE_INFINITY) {
      return Double.POSITIVE_INFINITY;
    } else if (x == 0)
      return 0;
    else if (x == 1)
      return 1;
    else if (x < 1)
      return nextFloat(Math.exp(nextFloat(d_lo * nextFloat(Math.log(x)))));
    else
      return nextFloat(Math.exp(nextFloat(d_hi * nextFloat(Math.log(x)))));
  }

  public static RealInterval evenIRoot(RealInterval i, int p, RealInterval res) {
    if (i.getSup() < 0) {
      LOGGER.severe("Erreur !!");
    }
    double inf, sup;
    if (i.getInf() < 0)
      inf = 0;
    else
      inf = iRoot_lo(i.getInf(), p);
    sup = iRoot_up(i.getSup(), p);

    if (res.getSup() < inf)
      return new RealIntervalConstant(-sup, -inf);
    else if (res.getInf() > sup)
      return new RealIntervalConstant(inf, sup);
    else
      return new RealIntervalConstant(-sup, sup);
  }

  public static RealInterval oddIRoot(RealInterval i, int p) {
    double inf, sup;
    if (i.getInf() >= 0)
      inf = iRoot_lo(i.getInf(), p);
    else
      inf = -iRoot_up(-i.getInf(), p);

    if (i.getSup() >= 0)
      sup = iRoot_up(i.getSup(), p);
    else
      sup = -iRoot_lo(-i.getSup(), p);
    return new RealIntervalConstant(inf, sup);
  }

  public static RealInterval iRoot(RealInterval i, int p, RealInterval res) {
    if (p <= 1) {
      throw new UnsupportedOperationException();
    }
    if (p % 2 == 0) {
      return evenIRoot(i, p, res);
    } else {
      return oddIRoot(i, p);
    }
  }

  public static RealInterval sinRange(int a, int b) {
    switch (4 * a + b) {
      case 0:
        LOGGER.severe("Erreur !");
        return null;
      case 1:
        return new RealIntervalConstant(1.0, 1.0);
      case 2:
        return new RealIntervalConstant(0.0, 1.0);
      case 3:
        LOGGER.severe("Erreur !");
        return null;
      case 4:
        LOGGER.severe("Erreur !");
        return null;
      case 5:
        LOGGER.severe("Erreur !");
        return null;
      case 6:
        return new RealIntervalConstant(0.0, 0.0);
      case 7:
        return new RealIntervalConstant(-1.0, 0.0);
      case 8:
        return new RealIntervalConstant(-1.0, 0.0);
      case 9:
        LOGGER.severe("Erreur !");
        return null;
      case 10:
        LOGGER.severe("Erreur !");
        return null;
      case 11:
        return new RealIntervalConstant(-1.0, -1.0);
      case 12:
        return new RealIntervalConstant(0.0, 0.0);
      case 13:
        return new RealIntervalConstant(0.0, 1.0);
      case 14:
        LOGGER.severe("Erreur !");
        return null;
      case 15:
        LOGGER.severe("Erreur !");
        return null;
    }
    throw new UnsupportedOperationException();
  }

  public static RealInterval cos(RealInterval interval) {
    if (interval.getSup() - interval.getInf() > prevFloat(1.5 * prevFloat(Math.PI))) {
      return new RealIntervalConstant(-1.0, 1.0);
    }
    int nlo, nup;
    if (interval.getInf() >= 0)
      nlo = (int) Math.floor(prevFloat(prevFloat(interval.getInf() * 2.0) / nextFloat(Math.PI)));
    else
      nlo = (int) Math.floor(prevFloat(prevFloat(interval.getInf() * 2.0) / prevFloat(Math.PI)));
    if (interval.getSup() >= 0)
      nup = (int) Math.floor(nextFloat(nextFloat(interval.getSup() * 2.0) / prevFloat(Math.PI)));
    else
      nup = (int) Math.floor(nextFloat(nextFloat(interval.getSup() * 2.0) / nextFloat(Math.PI)));

    if ((((nup - nlo) % 4) + 4) % 4 == 3) return new RealIntervalConstant(-1.0, 1.0);

    double clo = Math.min(prevFloat(Math.cos(interval.getInf())), prevFloat(Math.cos(interval.getSup())));
    double cup = Math.max(nextFloat(Math.cos(interval.getInf())), nextFloat(Math.cos(interval.getSup())));

    if ((((nup - nlo) % 4) + 4) % 4 == 0) return new RealIntervalConstant(clo, cup);

    RealInterval mask = sinRange((((nlo + 1) % 4) + 4) % 4, (((nup + 1) % 4) + 4) % 4);
    if (mask.getInf() < clo) clo = mask.getInf();
    if (mask.getSup() > cup) cup = mask.getSup();

    return new RealIntervalConstant(clo, cup);
  }

  public static RealInterval sin(RealInterval interval) {
    if (interval.getSup() - interval.getInf() > prevFloat(1.5 * prevFloat(Math.PI))) {
      return new RealIntervalConstant(-1.0, 1.0);
    }
    int nlo, nup;
    if (interval.getInf() >= 0)
      nlo = (int) Math.floor(prevFloat(prevFloat(interval.getInf() * 2.0) / nextFloat(Math.PI)));
    else
      nlo = (int) Math.floor(prevFloat(prevFloat(interval.getInf() * 2.0) / prevFloat(Math.PI)));
    if (interval.getSup() >= 0)
      nup = (int) Math.floor(nextFloat(nextFloat(interval.getSup() * 2.0) / prevFloat(Math.PI)));
    else
      nup = (int) Math.floor(nextFloat(nextFloat(interval.getSup() * 2.0) / nextFloat(Math.PI)));

    if ((((nup - nlo) % 4) + 4) % 4 == 3) return new RealIntervalConstant(-1.0, 1.0);

    double clo = Math.min(prevFloat(Math.sin(interval.getInf())), prevFloat(Math.sin(interval.getSup())));
    double cup = Math.max(nextFloat(Math.sin(interval.getInf())), nextFloat(Math.sin(interval.getSup())));

    if ((((nup - nlo) % 4) + 4) % 4 == 0) return new RealIntervalConstant(clo, cup);

    RealInterval mask = sinRange(((nlo % 4) + 4) % 4, ((nup % 4) + 4) % 4);
    if (mask.getInf() < clo) clo = mask.getInf();
    if (mask.getSup() > cup) cup = mask.getSup();

    return new RealIntervalConstant(clo, cup);
  }

  public static RealInterval asin_wrt(RealInterval interval, RealInterval res) {
    double retSup = Double.POSITIVE_INFINITY, retInf = Double.NEGATIVE_INFINITY;
    double asinl = prevFloat(Math.asin(interval.getInf()));
    double asinu = nextFloat(Math.asin(interval.getSup()));

    // Lower bound
    int modSup = (int) Math.floor((res.getInf() + nextFloat(Math.PI)) / prevFloat(2 * Math.PI));
    double decSup, decInf;

    if (modSup < 0) {
      decSup = nextFloat(2 * modSup * prevFloat(Math.PI));
      decInf = prevFloat(2 * modSup * nextFloat(Math.PI));
    } else if (modSup > 0) {
      decSup = nextFloat(2 * modSup * nextFloat(Math.PI));
      decInf = prevFloat(2 * modSup * prevFloat(Math.PI));
    } else {
      decSup = 0.0;
      decInf = 0.0;
    }

    if (interval.getInf() > -1.0) {
      if ((res.getInf() > nextFloat(nextFloat(-Math.PI) - asinl + decSup)) &&
          (res.getInf() < prevFloat(asinl + decInf))) {
        retInf = prevFloat(asinl + decInf);
      }
      if ((res.getInf() > nextFloat(nextFloat(Math.PI) - asinl + decSup)) &&
          (res.getInf() < prevFloat(asinl + 2 * prevFloat(Math.PI) + decInf))) {
        retInf = prevFloat(asinl + 2 * prevFloat(Math.PI) + decInf);
      }
    }

    if (interval.getSup() < 1.0) {
      if ((res.getInf() > asinu + decSup) &&
          (res.getInf() < prevFloat(prevFloat(Math.PI) - asinu) + decInf)) {
        retInf = prevFloat(prevFloat(Math.PI) - asinu) + decInf;
      }
    }

    // Upper bound
    modSup = (int) Math.floor((res.getSup() + nextFloat(Math.PI)) / prevFloat(2 * Math.PI));

    if (modSup < 0) {
      decSup = nextFloat(2 * modSup * prevFloat(Math.PI));
      decInf = prevFloat(2 * modSup * nextFloat(Math.PI));
    } else if (modSup > 0) {
      decSup = nextFloat(2 * modSup * nextFloat(Math.PI));
      decInf = prevFloat(2 * modSup * prevFloat(Math.PI));
    } else {
      decSup = 0.0;
      decInf = 0.0;
    }

    if (interval.getInf() > -1.0) {
      if ((res.getSup() > nextFloat(nextFloat(-Math.PI) - asinl + decSup)) &&
          (res.getSup() < prevFloat(asinl + decInf))) {
        retSup = nextFloat(nextFloat(-Math.PI) - asinl + decSup);
      }
      if ((res.getSup() > nextFloat(nextFloat(Math.PI) - asinl + decSup)) &&
          (res.getSup() < prevFloat(asinl + 2 * prevFloat(Math.PI) + decInf))) {
        retSup = nextFloat(nextFloat(Math.PI) - asinl + decSup);
      }
    }

    if (interval.getSup() < 1.0) {
      if ((res.getSup() > asinu + decSup) &&
          (res.getSup() < prevFloat(prevFloat(Math.PI) - asinu) + decInf)) {
        retSup = asinu + decSup;
      }
    }

    return new RealIntervalConstant(retInf, retSup);
  }

  public static RealInterval acos_wrt(RealInterval interval, RealInterval res) {
    double retSup = Double.POSITIVE_INFINITY, retInf = Double.NEGATIVE_INFINITY;
    double acosl = prevFloat(Math.acos(interval.getSup()));
    double acosu = nextFloat(Math.acos(interval.getInf()));

    // Lower bound
    int modSup = (int) Math.floor(res.getInf() / prevFloat(2 * Math.PI));
    double decSup, decInf;

    if (modSup < 0) {
      decSup = nextFloat(2 * modSup * prevFloat(Math.PI));
      decInf = prevFloat(2 * modSup * nextFloat(Math.PI));
    } else if (modSup > 0) {
      decSup = nextFloat(2 * modSup * nextFloat(Math.PI));
      decInf = prevFloat(2 * modSup * prevFloat(Math.PI));
    } else {
      decSup = 0.0;
      decInf = 0.0;
    }

    if (interval.getSup() < 1.0) {
      if ((res.getInf() > nextFloat(decSup - acosl)) &&
          (res.getInf() < prevFloat(decInf + acosl))) {
        retInf = prevFloat(decInf + acosl);
      }
      if ((res.getInf() > nextFloat(2 * nextFloat(Math.PI) - acosl + decSup)) &&
          (res.getInf() < prevFloat(2 * prevFloat(Math.PI) + acosl + decInf))) {
        retInf = prevFloat(2 * prevFloat(Math.PI) + acosl + decInf);
      }
    }

    if (interval.getInf() > -1.0) {
      if ((res.getInf() > nextFloat(acosu + decSup)) &&
          (res.getInf() < prevFloat(2 * prevFloat(Math.PI) - acosu + decInf))) {
        retInf = prevFloat(2 * prevFloat(Math.PI) - acosu + decInf);
      }
    }

    // Upper bound
    modSup = (int) Math.floor(res.getSup() / prevFloat(2 * Math.PI));

    if (modSup < 0) {
      decSup = nextFloat(2 * modSup * prevFloat(Math.PI));
      decInf = prevFloat(2 * modSup * nextFloat(Math.PI));
    } else if (modSup > 0) {
      decSup = nextFloat(2 * modSup * nextFloat(Math.PI));
      decInf = prevFloat(2 * modSup * prevFloat(Math.PI));
    } else {
      decSup = 0.0;
      decInf = 0.0;
    }

    if (interval.getSup() < 1.0) {
      if ((res.getSup() > nextFloat(decSup - acosl)) &&
          (res.getSup() < prevFloat(decInf + acosl))) {
        retSup = nextFloat(decSup - acosl);
      }
      if ((res.getSup() > nextFloat(2 * nextFloat(Math.PI) - acosl + decSup)) &&
          (res.getSup() < prevFloat(2 * prevFloat(Math.PI) + acosl + decInf))) {
        retSup = nextFloat(2 * nextFloat(Math.PI) - acosl + decSup);
      }
    }

    if (interval.getInf() > -1.0) {
      if ((res.getSup() > nextFloat(acosu + decSup)) &&
          (res.getSup() < prevFloat(2 * prevFloat(Math.PI) - acosu + decInf))) {
        retSup = nextFloat(acosu + decSup);
      }
    }

    return new RealIntervalConstant(retInf, retSup);
  }

}