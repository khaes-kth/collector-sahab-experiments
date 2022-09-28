/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.math.optimization.general;

import java.io.Serializable;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.VectorialPointValuePair;

/**
 * <p>Some of the unit tests are re-implementations of the MINPACK <a
 * href="http://www.netlib.org/minpack/ex/file17">file17</a> and <a
 * href="http://www.netlib.org/minpack/ex/file22">file22</a> test files.
 * The redistribution policy for MINPACK is available <a
 * href="http://www.netlib.org/minpack/disclaimer">here</a>, for
 * convenience, it is reproduced below.</p>

 * <table border="0" width="80%" cellpadding="10" align="center" bgcolor="#E0E0E0">
 * <tr><td>
 *    Minpack Copyright Notice (1999) University of Chicago.
 *    All rights reserved
 * </td></tr>
 * <tr><td>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * <ol>
 *  <li>Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.</li>
 * <li>The end-user documentation included with the redistribution, if any,
 *     must include the following acknowledgment:
 *     <code>This product includes software developed by the University of
 *           Chicago, as Operator of Argonne National Laboratory.</code>
 *     Alternately, this acknowledgment may appear in the software itself,
 *     if and wherever such third-party acknowledgments normally appear.</li>
 * <li><strong>WARRANTY DISCLAIMER. THE SOFTWARE IS SUPPLIED "AS IS"
 *     WITHOUT WARRANTY OF ANY KIND. THE COPYRIGHT HOLDER, THE
 *     UNITED STATES, THE UNITED STATES DEPARTMENT OF ENERGY, AND
 *     THEIR EMPLOYEES: (1) DISCLAIM ANY WARRANTIES, EXPRESS OR
 *     IMPLIED, INCLUDING BUT NOT LIMITED TO ANY IMPLIED WARRANTIES
 *     OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, TITLE
 *     OR NON-INFRINGEMENT, (2) DO NOT ASSUME ANY LEGAL LIABILITY
 *     OR RESPONSIBILITY FOR THE ACCURACY, COMPLETENESS, OR
 *     USEFULNESS OF THE SOFTWARE, (3) DO NOT REPRESENT THAT USE OF
 *     THE SOFTWARE WOULD NOT INFRINGE PRIVATELY OWNED RIGHTS, (4)
 *     DO NOT WARRANT THAT THE SOFTWARE WILL FUNCTION
 *     UNINTERRUPTED, THAT IT IS ERROR-FREE OR THAT ANY ERRORS WILL
 *     BE CORRECTED.</strong></li>
 * <li><strong>LIMITATION OF LIABILITY. IN NO EVENT WILL THE COPYRIGHT
 *     HOLDER, THE UNITED STATES, THE UNITED STATES DEPARTMENT OF
 *     ENERGY, OR THEIR EMPLOYEES: BE LIABLE FOR ANY INDIRECT,
 *     INCIDENTAL, CONSEQUENTIAL, SPECIAL OR PUNITIVE DAMAGES OF
 *     ANY KIND OR NATURE, INCLUDING BUT NOT LIMITED TO LOSS OF
 *     PROFITS OR LOSS OF DATA, FOR ANY REASON WHATSOEVER, WHETHER
 *     SUCH LIABILITY IS ASSERTED ON THE BASIS OF CONTRACT, TORT
 *     (INCLUDING NEGLIGENCE OR STRICT LIABILITY), OR OTHERWISE,
 *     EVEN IF ANY OF SAID PARTIES HAS BEEN WARNED OF THE
 *     POSSIBILITY OF SUCH LOSS OR DAMAGES.</strong></li>
 * <ol></td></tr>
 * </table>

 * @author Argonne National Laboratory. MINPACK project. March 1980 (original fortran minpack tests)
 * @author Burton S. Garbow (original fortran minpack tests)
 * @author Kenneth E. Hillstrom (original fortran minpack tests)
 * @author Jorge J. More (original fortran minpack tests)
 * @author Luc Maisonobe (non-minpack tests and minpack tests Java translation)
 */
public class MinpackTest extends TestCase {

  public MinpackTest(String name) {
    super(name);
  }

  public void testMinpackJennrichSampson() {
    minpackTest(new JennrichSampsonFunction(10, new double[] { 0.3, 0.4 },
                                            64.5856498144943, 11.1517793413499,
                                            new double[] {
                                             0.2578330049, 0.257829976764542
                                            }), false);
  }

  private void minpackTest(MinpackFunction function, boolean exceptionExpected) {
      LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
      optimizer.setMaxIterations(100 * (function.getN() + 1));
      optimizer.setCostRelativeTolerance(Math.sqrt(2.22044604926e-16));
      optimizer.setParRelativeTolerance(Math.sqrt(2.22044604926e-16));
      optimizer.setOrthoTolerance(2.22044604926e-16);
//      assertTrue(function.checkTheoreticalStartCost(optimizer.getRMS()));
      try {
          VectorialPointValuePair optimum =
              optimizer.optimize(function,
                                 function.getTarget(), function.getWeight(),
                                 function.getStartPoint());
          assertFalse(exceptionExpected);
          function.checkTheoreticalMinCost(optimizer.getRMS());
          function.checkTheoreticalMinParams(optimum);
      } catch (OptimizationException lsse) {
          assertTrue(exceptionExpected);
      } catch (FunctionEvaluationException fe) {
          assertTrue(exceptionExpected);
      }
  }

  private static abstract class MinpackFunction
      implements DifferentiableMultivariateVectorialFunction, Serializable {

      private static final long serialVersionUID = -6209760235478794233L;
      protected int      n;
      protected int      m;
      protected double[] startParams;
      protected double   theoreticalMinCost;
      protected double[] theoreticalMinParams;
      protected double   costAccuracy;
      protected double   paramsAccuracy;

      protected MinpackFunction(int m, double[] startParams,
                                double theoreticalMinCost, double[] theoreticalMinParams) {
          this.m = m;
          this.n = startParams.length;
          this.startParams          = startParams.clone();
          this.theoreticalMinCost   = theoreticalMinCost;
          this.theoreticalMinParams = theoreticalMinParams;
          this.costAccuracy         = 1.0e-8;
          this.paramsAccuracy       = 1.0e-5;
      }

      protected static double[] buildArray(int n, double x) {
          double[] array = new double[n];
          Arrays.fill(array, x);
          return array;
      }

      public double[] getTarget() {
          return buildArray(m, 0.0);
      }

      public double[] getWeight() {
          return buildArray(m, 1.0);
      }

      public double[] getStartPoint() {
          return startParams.clone();
      }

      protected void setCostAccuracy(double costAccuracy) {
          this.costAccuracy = costAccuracy;
      }

      protected void setParamsAccuracy(double paramsAccuracy) {
          this.paramsAccuracy = paramsAccuracy;
      }

      public int getN() {
          return startParams.length;
      }

      public void checkTheoreticalMinCost(double rms) {
          double threshold = costAccuracy * (1.0 + theoreticalMinCost);
          assertEquals(theoreticalMinCost, Math.sqrt(m) * rms, threshold);
      }

      public void checkTheoreticalMinParams(VectorialPointValuePair optimum) {
          double[] params = optimum.getPointRef();
          if (theoreticalMinParams != null) {
              for (int i = 0; i < theoreticalMinParams.length; ++i) {
                  double mi = theoreticalMinParams[i];
                  double vi = params[i];
                  assertEquals(mi, vi, paramsAccuracy * (1.0 + Math.abs(mi)));
              }
          }
      }

      public MultivariateMatrixFunction jacobian() {
          return new MultivariateMatrixFunction() {
            private static final long serialVersionUID = -2435076097232923678L;
            public double[][] value(double[] point) {
                  return jacobian(point);
              }
          };
      }

      public abstract double[][] jacobian(double[] variables);

      public abstract double[] value(double[] variables);

  }

  private static class LinearFullRankFunction extends MinpackFunction {

    private static final long serialVersionUID = -9030323226268039536L;

    public LinearFullRankFunction(int m, int n, double x0,
                                  double theoreticalStartCost,
                                  double theoreticalMinCost) {
      super(m, buildArray(n, x0), theoreticalMinCost,
            buildArray(n, -1.0));
    }

    @Override
    public double[][] jacobian(double[] variables) {
      double t = 2.0 / m;
      double[][] jacobian = new double[m][];
      for (int i = 0; i < m; ++i) {
        jacobian[i] = new double[n];
        for (int j = 0; j < n; ++j) {
          jacobian[i][j] = (i == j) ? (1 - t) : -t;
        }
      }
      return jacobian;
    }

    @Override
    public double[] value(double[] variables) {
      double sum = 0;
      for (int i = 0; i < n; ++i) {
        sum += variables[i];
      }
      double t  = 1 + 2 * sum / m;
      double[] f = new double[m];
      for (int i = 0; i < n; ++i) {
        f[i] = variables[i] - t;
      }
      Arrays.fill(f, n, m, -t);
      return f;
    }

  }

  private static class LinearRank1Function extends MinpackFunction {

    private static final long serialVersionUID = 8494863245104608300L;

    public LinearRank1Function(int m, int n, double x0,
                                  double theoreticalStartCost,
                                  double theoreticalMinCost) {
      super(m, buildArray(n, x0), theoreticalMinCost, null);
    }

    @Override
    public double[][] jacobian(double[] variables) {
      double[][] jacobian = new double[m][];
      for (int i = 0; i < m; ++i) {
        jacobian[i] = new double[n];
        for (int j = 0; j < n; ++j) {
          jacobian[i][j] = (i + 1) * (j + 1);
        }
      }
      return jacobian;
    }

    @Override
    public double[] value(double[] variables) {
      double[] f = new double[m];
      double sum = 0;
      for (int i = 0; i < n; ++i) {
        sum += (i + 1) * variables[i];
      }
      for (int i = 0; i < m; ++i) {
        f[i] = (i + 1) * sum - 1;
      }
      return f;
    }

  }

  private static class LinearRank1ZeroColsAndRowsFunction extends MinpackFunction {

    private static final long serialVersionUID = -3316653043091995018L;

    public LinearRank1ZeroColsAndRowsFunction(int m, int n, double x0) {
      super(m, buildArray(n, x0),
            Math.sqrt((m * (m + 3) - 6) / (2.0 * (2 * m - 3))),
            null);
    }

    @Override
    public double[][] jacobian(double[] variables) {
      double[][] jacobian = new double[m][];
      for (int i = 0; i < m; ++i) {
        jacobian[i] = new double[n];
        jacobian[i][0] = 0;
        for (int j = 1; j < (n - 1); ++j) {
          if (i == 0) {
            jacobian[i][j] = 0;
          } else if (i != (m - 1)) {
            jacobian[i][j] = i * (j + 1);
          } else {
            jacobian[i][j] = 0;
          }
        }
        jacobian[i][n - 1] = 0;
      }
      return jacobian;
    }

    @Override
    public double[] value(double[] variables) {
      double[] f = new double[m];
      double sum = 0;
      for (int i = 1; i < (n - 1); ++i) {
        sum += (i + 1) * variables[i];
      }
      for (int i = 0; i < (m - 1); ++i) {
        f[i] = i * sum - 1;
      }
      f[m - 1] = -1;
      return f;
    }

  }

  private static class RosenbrockFunction extends MinpackFunction {

    private static final long serialVersionUID = 2893438180956569134L;

    public RosenbrockFunction(double[] startParams, double theoreticalStartCost) {
      super(2, startParams, 0.0, buildArray(2, 1.0));
    }

    @Override
    public double[][] jacobian(double[] variables) {
      double x1 = variables[0];
      return new double[][] { { -20 * x1, 10 }, { -1, 0 } };
    }

    @Override
    public double[] value(double[] variables) {
      double x1 = variables[0];
      double x2 = variables[1];
      return new double[] { 10 * (x2 - x1 * x1), 1 - x1 };
    }

  }

  private static class HelicalValleyFunction extends MinpackFunction {

    private static final long serialVersionUID = 220613787843200102L;

    public HelicalValleyFunction(double[] startParams,
                                 double theoreticalStartCost) {
      super(3, startParams, 0.0, new double[] { 1.0, 0.0, 0.0 });
    }

    @Override
    public double[][] jacobian(double[] variables) {
      double x1 = variables[0];
      double x2 = variables[1];
      double tmpSquare = x1 * x1 + x2 * x2;
      double tmp1 = twoPi * tmpSquare;
      double tmp2 = Math.sqrt(tmpSquare);
      return new double[][] {
        {  100 * x2 / tmp1, -100 * x1 / tmp1, 10 },
        { 10 * x1 / tmp2, 10 * x2 / tmp2, 0 },
        { 0, 0, 1 }
      };
    }

    @Override
    public double[] value(double[] variables) {
      double x1 = variables[0];
      double x2 = variables[1];
      double x3 = variables[2];
      double tmp1;
      if (x1 == 0) {
        tmp1 = (x2 >= 0) ? 0.25 : -0.25;
      } else {
        tmp1 = Math.atan(x2 / x1) / twoPi;
        if (x1 < 0) {
          tmp1 += 0.5;
        }
      }
      double tmp2 = Math.sqrt(x1 * x1 + x2 * x2);
      return new double[] {
        10.0 * (x3 - 10 * tmp1),
        10.0 * (tmp2 - 1),
        x3
      };
    }

    private static final double twoPi = 2.0 * Math.PI;

  }

  private static class PowellSingularFunction extends MinpackFunction {

    private static final long serialVersionUID = 7298364171208142405L;

    public PowellSingularFunction(double[] startParams,
                                  double theoreticalStartCost) {
      super(4, startParams, 0.0, buildArray(4, 0.0));
    }

    @Override
    public double[][] jacobian(double[] variables) {
      double x1 = variables[0];
      double x2 = variables[1];
      double x3 = variables[2];
      double x4 = variables[3];
      return new double[][] {
        { 1, 10, 0, 0 },
        { 0, 0, sqrt5, -sqrt5 },
        { 0, 2 * (x2 - 2 * x3), -4 * (x2 - 2 * x3), 0 },
        { 2 * sqrt10 * (x1 - x4), 0, 0, -2 * sqrt10 * (x1 - x4) }
      };
    }

    @Override
    public double[] value(double[] variables) {
      double x1 = variables[0];
      double x2 = variables[1];
      double x3 = variables[2];
      double x4 = variables[3];
      return new double[] {
        x1 + 10 * x2,
        sqrt5 * (x3 - x4),
        (x2 - 2 * x3) * (x2 - 2 * x3),
        sqrt10 * (x1 - x4) * (x1 - x4)
      };
    }

    private static final double sqrt5  = Math.sqrt( 5.0);
    private static final double sqrt10 = Math.sqrt(10.0);

  }

  private static class FreudensteinRothFunction extends MinpackFunction {

    private static final long serialVersionUID = 2892404999344244214L;

    public FreudensteinRothFunction(double[] startParams,
                                    double theoreticalStartCost,
                                    double theoreticalMinCost,
                                    double[] theoreticalMinParams) {
      super(2, startParams, theoreticalMinCost,
            theoreticalMinParams);
    }

    @Override
    public double[][] jacobian(double[] variables) {
      double x2 = variables[1];
      return new double[][] {
        { 1, x2 * (10 - 3 * x2) -  2 },
        { 1, x2 * ( 2 + 3 * x2) - 14, }
      };
    }

    @Override
    public double[] value(double[] variables) {
      double x1 = variables[0];
      double x2 = variables[1];
      return new double[] {
       -13.0 + x1 + ((5.0 - x2) * x2 -  2.0) * x2,
       -29.0 + x1 + ((1.0 + x2) * x2 - 14.0) * x2
      };
    }

  }

  private static class BardFunction extends MinpackFunction {

    private static final long serialVersionUID = 5990442612572087668L;

    public BardFunction(double x0,
                        double theoreticalStartCost,
                        double theoreticalMinCost,
                        double[] theoreticalMinParams) {
      super(15, buildArray(3, x0), theoreticalMinCost,
            theoreticalMinParams);
    }

    @Override
    public double[][] jacobian(double[] variables) {
      double   x2 = variables[1];
      double   x3 = variables[2];
      double[][] jacobian = new double[m][];
      for (int i = 0; i < m; ++i) {
        double tmp1 = i  + 1;
        double tmp2 = 15 - i;
        double tmp3 = (i <= 7) ? tmp1 : tmp2;
        double tmp4 = x2 * tmp2 + x3 * tmp3;
        tmp4 *= tmp4;
        jacobian[i] = new double[] { -1, tmp1 * tmp2 / tmp4, tmp1 * tmp3 / tmp4 };
      }
      return jacobian;
    }

    @Override
    public double[] value(double[] variables) {
      double   x1 = variables[0];
      double   x2 = variables[1];
      double   x3 = variables[2];
      double[] f = new double[m];
      for (int i = 0; i < m; ++i) {
        double tmp1 = i + 1;
        double tmp2 = 15 - i;
        double tmp3 = (i <= 7) ? tmp1 : tmp2;
        f[i] = y[i] - (x1 + tmp1 / (x2 * tmp2 + x3 * tmp3));
      }
      return f;
    }

    private static final double[] y = {
      0.14, 0.18, 0.22, 0.25, 0.29,
      0.32, 0.35, 0.39, 0.37, 0.58,
      0.73, 0.96, 1.34, 2.10, 4.39
    };

  }

  private static class KowalikOsborneFunction extends MinpackFunction {

    private static final long serialVersionUID = -4867445739880495801L;

    public KowalikOsborneFunction(double[] startParams,
                                  double theoreticalStartCost,
                                  double theoreticalMinCost,
                                  double[] theoreticalMinParams) {
      super(11, startParams, theoreticalMinCost,
            theoreticalMinParams);
      if (theoreticalStartCost > 20.0) {
        setCostAccuracy(2.0e-4);
        setParamsAccuracy(5.0e-3);
      }
    }

    @Override
    public double[][] jacobian(double[] variables) {
      double   x1 = variables[0];
      double   x2 = variables[1];
      double   x3 = variables[2];
      double   x4 = variables[3];
      double[][] jacobian = new double[m][];
      for (int i = 0; i < m; ++i) {
        double tmp = v[i] * (v[i] + x3) + x4;
        double j1  = -v[i] * (v[i] + x2) / tmp;
        double j2  = -v[i] * x1 / tmp;
        double j3  = j1 * j2;
        double j4  = j3 / v[i];
        jacobian[i] = new double[] { j1, j2, j3, j4 };
      }
      return jacobian;
    }

    @Override
    public double[] value(double[] variables) {
      double x1 = variables[0];
      double x2 = variables[1];
      double x3 = variables[2];
      double x4 = variables[3];
      double[] f = new double[m];
      for (int i = 0; i < m; ++i) {
        f[i] = y[i] - x1 * (v[i] * (v[i] + x2)) / (v[i] * (v[i] + x3) + x4);
      }
      return f;
    }

    private static final double[] v = {
      4.0, 2.0, 1.0, 0.5, 0.25, 0.167, 0.125, 0.1, 0.0833, 0.0714, 0.0625
    };

    private static final double[] y = {
      0.1957, 0.1947, 0.1735, 0.1600, 0.0844, 0.0627,
      0.0456, 0.0342, 0.0323, 0.0235, 0.0246
    };

  }

  private static class MeyerFunction extends MinpackFunction {

    private static final long serialVersionUID = -838060619150131027L;

    public MeyerFunction(double[] startParams,
                         double theoreticalStartCost,
                         double theoreticalMinCost,
                         double[] theoreticalMinParams) {
      super(16, startParams, theoreticalMinCost,
            theoreticalMinParams);
      if (theoreticalStartCost > 1.0e6) {
        setCostAccuracy(7.0e-3);
        setParamsAccuracy(2.0e-2);
      }
    }

    @Override
    public double[][] jacobian(double[] variables) {
      double   x1 = variables[0];
      double   x2 = variables[1];
      double   x3 = variables[2];
      double[][] jacobian = new double[m][];
      for (int i = 0; i < m; ++i) {
        double temp = 5.0 * (i + 1) + 45.0 + x3;
        double tmp1 = x2 / temp;
        double tmp2 = Math.exp(tmp1);
        double tmp3 = x1 * tmp2 / temp;
        jacobian[i] = new double[] { tmp2, tmp3, -tmp1 * tmp3 };
      }
      return jacobian;
    }

    @Override
    public double[] value(double[] variables) {
      double x1 = variables[0];
      double x2 = variables[1];
      double x3 = variables[2];
      double[] f = new double[m];
      for (int i = 0; i < m; ++i) {
        f[i] = x1 * Math.exp(x2 / (5.0 * (i + 1) + 45.0 + x3)) - y[i];
      }
     return f;
    }

    private static final double[] y = {
      34780.0, 28610.0, 23650.0, 19630.0,
      16370.0, 13720.0, 11540.0,  9744.0,
       8261.0,  7030.0,  6005.0,  5147.0,
       4427.0,  3820.0,  3307.0,  2872.0
    };

  }

  private static class WatsonFunction extends MinpackFunction {

    private static final long serialVersionUID = -9034759294980218927L;

    public WatsonFunction(int n, double x0,
                          double theoreticalStartCost,
                          double theoreticalMinCost,
                          double[] theoreticalMinParams) {
      super(31, buildArray(n, x0), theoreticalMinCost,
            theoreticalMinParams);
    }

    @Override
    public double[][] jacobian(double[] variables) {

      double[][] jacobian = new double[m][];

      for (int i = 0; i < (m - 2); ++i) {
        double div = (i + 1) / 29.0;
        double s2  = 0.0;
        double dx  = 1.0;
        for (int j = 0; j < n; ++j) {
          s2 += dx * variables[j];
          dx *= div;
        }
        double temp= 2 * div * s2;
        dx = 1.0 / div;
        jacobian[i] = new double[n];
        for (int j = 0; j < n; ++j) {
          jacobian[i][j] = dx * (j - temp);
          dx *= div;
        }
      }

      jacobian[m - 2]    = new double[n];
      jacobian[m - 2][0] = 1;

      jacobian[m - 1]   = new double[n];
      jacobian[m - 1][0]= -2 * variables[0];
      jacobian[m - 1][1]= 1;

      return jacobian;

    }

    @Override
    public double[] value(double[] variables) {
     double[] f = new double[m];
     for (int i = 0; i < (m - 2); ++i) {
       double div = (i + 1) / 29.0;
       double s1 = 0;
       double dx = 1;
       for (int j = 1; j < n; ++j) {
         s1 += j * dx * variables[j];
         dx *= div;
       }
       double s2 =0;
       dx =1;
       for (int j = 0; j < n; ++j) {
         s2 += dx * variables[j];
         dx *= div;
       }
       f[i] = s1 - s2 * s2 - 1;
     }

     double x1 = variables[0];
     double x2 = variables[1];
     f[m - 2] = x1;
     f[m - 1] = x2 - x1 * x1 - 1;

     return f;

    }

  }

  private static class Box3DimensionalFunction extends MinpackFunction {

    private static final long serialVersionUID = 5511403858142574493L;

    public Box3DimensionalFunction(int m, double[] startParams,
                                   double theoreticalStartCost) {
      super(m, startParams, 0.0,
            new double[] { 1.0, 10.0, 1.0 });
   }

    @Override
    public double[][] jacobian(double[] variables) {
      double   x1 = variables[0];
      double   x2 = variables[1];
      double[][] jacobian = new double[m][];
      for (int i = 0; i < m; ++i) {
        double tmp = (i + 1) / 10.0;
        jacobian[i] = new double[] {
          -tmp * Math.exp(-tmp * x1),
           tmp * Math.exp(-tmp * x2),
          Math.exp(-i - 1) - Math.exp(-tmp)
        };
      }
      return jacobian;
    }

    @Override
    public double[] value(double[] variables) {
      double x1 = variables[0];
      double x2 = variables[1];
      double x3 = variables[2];
      double[] f = new double[m];
      for (int i = 0; i < m; ++i) {
        double tmp = (i + 1) / 10.0;
        f[i] = Math.exp(-tmp * x1) - Math.exp(-tmp * x2)
             + (Math.exp(-i - 1) - Math.exp(-tmp)) * x3;
      }
      return f;
    }

  }

  private static class JennrichSampsonFunction extends MinpackFunction {

    private static final long serialVersionUID = -2489165190443352947L;

    public JennrichSampsonFunction(int m, double[] startParams,
                                   double theoreticalStartCost,
                                   double theoreticalMinCost,
                                   double[] theoreticalMinParams) {
      super(m, startParams, theoreticalMinCost,
            theoreticalMinParams);
    }

    @Override
    public double[][] jacobian(double[] variables) {
      double   x1 = variables[0];
      double   x2 = variables[1];
      double[][] jacobian = new double[m][];
      for (int i = 0; i < m; ++i) {
        double t = i + 1;
        jacobian[i] = new double[] { -t * Math.exp(t * x1), -t * Math.exp(t * x2) };
      }
      return jacobian;
    }

    @Override
    public double[] value(double[] variables) {
      double x1 = variables[0];
      double x2 = variables[1];
      double[] f = new double[m];
      for (int i = 0; i < m; ++i) {
        double temp = i + 1;
        f[i] = 2 + 2 * temp - Math.exp(temp * x1) - Math.exp(temp * x2);
      }
      return f;
    }

  }

  private static class BrownDennisFunction extends MinpackFunction {

    private static final long serialVersionUID = 8340018645694243910L;

    public BrownDennisFunction(int m, double[] startParams,
                               double theoreticalStartCost,
                               double theoreticalMinCost,
                               double[] theoreticalMinParams) {
      super(m, startParams, theoreticalMinCost,
            theoreticalMinParams);
      setCostAccuracy(2.5e-8);
    }

    @Override
    public double[][] jacobian(double[] variables) {
      double   x1 = variables[0];
      double   x2 = variables[1];
      double   x3 = variables[2];
      double   x4 = variables[3];
      double[][] jacobian = new double[m][];
      for (int i = 0; i < m; ++i) {
        double temp = (i + 1) / 5.0;
        double ti   = Math.sin(temp);
        double tmp1 = x1 + temp * x2 - Math.exp(temp);
        double tmp2 = x3 + ti   * x4 - Math.cos(temp);
        jacobian[i] = new double[] {
          2 * tmp1, 2 * temp * tmp1, 2 * tmp2, 2 * ti * tmp2
        };
      }
      return jacobian;
    }

    @Override
    public double[] value(double[] variables) {
      double x1 = variables[0];
      double x2 = variables[1];
      double x3 = variables[2];
      double x4 = variables[3];
      double[] f = new double[m];
      for (int i = 0; i < m; ++i) {
        double temp = (i + 1) / 5.0;
        double tmp1 = x1 + temp * x2 - Math.exp(temp);
        double tmp2 = x3 + Math.sin(temp) * x4 - Math.cos(temp);
        f[i] = tmp1 * tmp1 + tmp2 * tmp2;
      }
      return f;
    }

  }

  private static class ChebyquadFunction extends MinpackFunction {

    private static final long serialVersionUID = -2394877275028008594L;

    private static double[] buildChebyquadArray(int n, double factor) {
      double[] array = new double[n];
      double inv = factor / (n + 1);
      for (int i = 0; i < n; ++i) {
        array[i] = (i + 1) * inv;
      }
      return array;
    }

    public ChebyquadFunction(int n, int m, double factor,
                             double theoreticalStartCost,
                             double theoreticalMinCost,
                             double[] theoreticalMinParams) {
      super(m, buildChebyquadArray(n, factor), theoreticalMinCost,
            theoreticalMinParams);
    }

    @Override
    public double[][] jacobian(double[] variables) {

      double[][] jacobian = new double[m][];
      for (int i = 0; i < m; ++i) {
        jacobian[i] = new double[n];
      }

      double dx = 1.0 / n;
      for (int j = 0; j < n; ++j) {
        double tmp1 = 1;
        double tmp2 = 2 * variables[j] - 1;
        double temp = 2 * tmp2;
        double tmp3 = 0;
        double tmp4 = 2;
        for (int i = 0; i < m; ++i) {
          jacobian[i][j] = dx * tmp4;
          double ti = 4 * tmp2 + temp * tmp4 - tmp3;
          tmp3 = tmp4;
          tmp4 = ti;
          ti   = temp * tmp2 - tmp1;
          tmp1 = tmp2;
          tmp2 = ti;
        }
      }

      return jacobian;

    }

    @Override
    public double[] value(double[] variables) {

      double[] f = new double[m];

      for (int j = 0; j < n; ++j) {
        double tmp1 = 1;
        double tmp2 = 2 * variables[j] - 1;
        double temp = 2 * tmp2;
        for (int i = 0; i < m; ++i) {
          f[i] += tmp2;
          double ti = temp * tmp2 - tmp1;
          tmp1 = tmp2;
          tmp2 = ti;
        }
      }

      double dx = 1.0 / n;
      boolean iev = false;
      for (int i = 0; i < m; ++i) {
        f[i] *= dx;
        if (iev) {
          f[i] += 1.0 / (i * (i + 2));
        }
        iev = ! iev;
      }

      return f;

    }

  }

  private static class BrownAlmostLinearFunction extends MinpackFunction {

    private static final long serialVersionUID = 8239594490466964725L;

    public BrownAlmostLinearFunction(int m, double factor,
                                     double theoreticalStartCost,
                                     double theoreticalMinCost,
                                     double[] theoreticalMinParams) {
      super(m, buildArray(m, factor), theoreticalMinCost,
            theoreticalMinParams);
    }

    @Override
    public double[][] jacobian(double[] variables) {
      double[][] jacobian = new double[m][];
      for (int i = 0; i < m; ++i) {
        jacobian[i] = new double[n];
      }

      double prod = 1;
      for (int j = 0; j < n; ++j) {
        prod *= variables[j];
        for (int i = 0; i < n; ++i) {
          jacobian[i][j] = 1;
        }
        jacobian[j][j] = 2;
      }

      for (int j = 0; j < n; ++j) {
        double temp = variables[j];
        if (temp == 0) {
          temp = 1;
          prod = 1;
          for (int k = 0; k < n; ++k) {
            if (k != j) {
              prod *= variables[k];
            }
          }
        }
        jacobian[n - 1][j] = prod / temp;
      }

      return jacobian;

    }

    @Override
    public double[] value(double[] variables) {
      double[] f = new double[m];
      double sum  = -(n + 1);
      double prod = 1;
      for (int j = 0; j < n; ++j) {
        sum  += variables[j];
        prod *= variables[j];
      }
      for (int i = 0; i < n; ++i) {
        f[i] = variables[i] + sum;
      }
      f[n - 1] = prod - 1;
      return f;
    }

  }

  private static class Osborne1Function extends MinpackFunction {

    private static final long serialVersionUID = 4006743521149849494L;

    public Osborne1Function(double[] startParams,
                            double theoreticalStartCost,
                            double theoreticalMinCost,
                            double[] theoreticalMinParams) {
      super(33, startParams, theoreticalMinCost,
            theoreticalMinParams);
    }

    @Override
    public double[][] jacobian(double[] variables) {
      double   x2 = variables[1];
      double   x3 = variables[2];
      double   x4 = variables[3];
      double   x5 = variables[4];
      double[][] jacobian = new double[m][];
      for (int i = 0; i < m; ++i) {
        double temp = 10.0 * i;
        double tmp1 = Math.exp(-temp * x4);
        double tmp2 = Math.exp(-temp * x5);
        jacobian[i] = new double[] {
          -1, -tmp1, -tmp2, temp * x2 * tmp1, temp * x3 * tmp2
        };
      }
      return jacobian;
    }

    @Override
    public double[] value(double[] variables) {
      double x1 = variables[0];
      double x2 = variables[1];
      double x3 = variables[2];
      double x4 = variables[3];
      double x5 = variables[4];
      double[] f = new double[m];
      for (int i = 0; i < m; ++i) {
        double temp = 10.0 * i;
        double tmp1 = Math.exp(-temp * x4);
        double tmp2 = Math.exp(-temp * x5);
        f[i] = y[i] - (x1 + x2 * tmp1 + x3 * tmp2);
      }
      return f;
    }

    private static final double[] y = {
      0.844, 0.908, 0.932, 0.936, 0.925, 0.908, 0.881, 0.850, 0.818, 0.784, 0.751,
      0.718, 0.685, 0.658, 0.628, 0.603, 0.580, 0.558, 0.538, 0.522, 0.506, 0.490,
      0.478, 0.467, 0.457, 0.448, 0.438, 0.431, 0.424, 0.420, 0.414, 0.411, 0.406
    };

  }

  private static class Osborne2Function extends MinpackFunction {

    private static final long serialVersionUID = -8418268780389858746L;

    public Osborne2Function(double[] startParams,
                            double theoreticalStartCost,
                            double theoreticalMinCost,
                            double[] theoreticalMinParams) {
      super(65, startParams, theoreticalMinCost,
            theoreticalMinParams);
    }

    @Override
    public double[][] jacobian(double[] variables) {
      double   x01 = variables[0];
      double   x02 = variables[1];
      double   x03 = variables[2];
      double   x04 = variables[3];
      double   x05 = variables[4];
      double   x06 = variables[5];
      double   x07 = variables[6];
      double   x08 = variables[7];
      double   x09 = variables[8];
      double   x10 = variables[9];
      double   x11 = variables[10];
      double[][] jacobian = new double[m][];
      for (int i = 0; i < m; ++i) {
        double temp = i / 10.0;
        double tmp1 = Math.exp(-x05 * temp);
        double tmp2 = Math.exp(-x06 * (temp - x09) * (temp - x09));
        double tmp3 = Math.exp(-x07 * (temp - x10) * (temp - x10));
        double tmp4 = Math.exp(-x08 * (temp - x11) * (temp - x11));
        jacobian[i] = new double[] {
          -tmp1,
          -tmp2,
          -tmp3,
          -tmp4,
          temp * x01 * tmp1,
          x02 * (temp - x09) * (temp - x09) * tmp2,
          x03 * (temp - x10) * (temp - x10) * tmp3,
          x04 * (temp - x11) * (temp - x11) * tmp4,
          -2 * x02 * x06 * (temp - x09) * tmp2,
          -2 * x03 * x07 * (temp - x10) * tmp3,
          -2 * x04 * x08 * (temp - x11) * tmp4
        };
      }
      return jacobian;
    }

    @Override
    public double[] value(double[] variables) {
      double x01 = variables[0];
      double x02 = variables[1];
      double x03 = variables[2];
      double x04 = variables[3];
      double x05 = variables[4];
      double x06 = variables[5];
      double x07 = variables[6];
      double x08 = variables[7];
      double x09 = variables[8];
      double x10 = variables[9];
      double x11 = variables[10];
      double[] f = new double[m];
      for (int i = 0; i < m; ++i) {
        double temp = i / 10.0;
        double tmp1 = Math.exp(-x05 * temp);
        double tmp2 = Math.exp(-x06 * (temp - x09) * (temp - x09));
        double tmp3 = Math.exp(-x07 * (temp - x10) * (temp - x10));
        double tmp4 = Math.exp(-x08 * (temp - x11) * (temp - x11));
        f[i] = y[i] - (x01 * tmp1 + x02 * tmp2 + x03 * tmp3 + x04 * tmp4);
      }
      return f;
    }

    private static final double[] y = {
      1.366, 1.191, 1.112, 1.013, 0.991,
      0.885, 0.831, 0.847, 0.786, 0.725,
      0.746, 0.679, 0.608, 0.655, 0.616,
      0.606, 0.602, 0.626, 0.651, 0.724,
      0.649, 0.649, 0.694, 0.644, 0.624,
      0.661, 0.612, 0.558, 0.533, 0.495,
      0.500, 0.423, 0.395, 0.375, 0.372,
      0.391, 0.396, 0.405, 0.428, 0.429,
      0.523, 0.562, 0.607, 0.653, 0.672,
      0.708, 0.633, 0.668, 0.645, 0.632,
      0.591, 0.559, 0.597, 0.625, 0.739,
      0.710, 0.729, 0.720, 0.636, 0.581,
      0.428, 0.292, 0.162, 0.098, 0.054
    };

  }

}
