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

package choco.kernel.model.variables;

/* 
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 2 avr. 2008
 * Since : Choco 2.0.0
 *
 */
public enum Operator {

  ABS("abs", "operator.abs", 1),
  COS("cos", "operator.cos", 1),
  DISTANCEEQ("distanceEQ"),
  DISTANCEGT("distanceGT"),
  DISTANCELT("distanceLT"),
  DISTANCENEQ("distanceEQ"),
  DIV("div", "operator.div", 2),
  IFTHENELSE("ifthenelse", "operator.ifthenelse", 2),
  MINUS("minus", "operator.minus", 2),
  MAX("max", "operator.max", 0),
  MIN("min", "operator.min", 0),
  MOD("mod", "operator.mod", 2),
  MULT("mult", "operator.mult", 2),
  NEG("neg", "operator.neg", 1),
  NONE("none"),
  PLUS("plus", "operator.plus", 2),
  POWER("power", "operator.power", 2),
  SCALAR("scalar", "operator.scalar", 0),
  SIN("sin", "operator.sin", 1),
  SUM("sum", "operator.sum", 0),
  ;

  public final String name;
  public final String property;
  public final int parameters; // 0 means 1 or more parameters

    Operator(String name, String property, int paramaters) {
    this.name = name;
      this.property = property;
      this.parameters = paramaters;
  }

    Operator(String name) {
        this(name, null, -1);
    }
}
