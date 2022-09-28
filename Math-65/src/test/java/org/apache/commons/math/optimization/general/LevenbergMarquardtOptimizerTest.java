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

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.linear.BlockRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.SimpleVectorialValueChecker;
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
public class LevenbergMarquardtOptimizerTest
  extends TestCase {

    public LevenbergMarquardtOptimizerTest(String name) {
        super(name);
    }

    public void testCircleFitting() throws FunctionEvaluationException, OptimizationException {
        Circle circle = new Circle();
        circle.addPoint( 30.0,  68.0);
        circle.addPoint( 50.0,  -6.0);
        circle.addPoint(110.0, -20.0);
        circle.addPoint( 35.0,  15.0);
        circle.addPoint( 45.0,  97.0);
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialPointValuePair optimum =
            optimizer.optimize(circle, new double[] { 0, 0, 0, 0, 0 }, new double[] { 1, 1, 1, 1, 1 },
                               new double[] { 98.680, 47.345 });
        assertTrue(optimizer.getEvaluations() < 10);
        assertTrue(optimizer.getJacobianEvaluations() < 10);
        double rms = optimizer.getRMS();
        assertEquals(1.768262623567235,  Math.sqrt(circle.getN()) * rms,  1.0e-10);
        Point2D.Double center = new Point2D.Double(optimum.getPointRef()[0], optimum.getPointRef()[1]);
        assertEquals(69.96016176931406, circle.getRadius(center), 1.0e-10);
        assertEquals(96.07590211815305, center.x,      1.0e-10);
        assertEquals(48.13516790438953, center.y,      1.0e-10);
        double[][] cov = optimizer.getCovariances();
        assertEquals(1.839, cov[0][0], 0.001);
        assertEquals(0.731, cov[0][1], 0.001);
        assertEquals(cov[0][1], cov[1][0], 1.0e-14);
        assertEquals(0.786, cov[1][1], 0.001);
        double[] errors = optimizer.guessParametersErrors();
        assertEquals(1.384, errors[0], 0.001);
        assertEquals(0.905, errors[1], 0.001);

        // add perfect measurements and check errors are reduced
        double  r = circle.getRadius(center);
        for (double d= 0; d < 2 * Math.PI; d += 0.01) {
            circle.addPoint(center.x + r * Math.cos(d), center.y + r * Math.sin(d));
        }
        double[] target = new double[circle.getN()];
        Arrays.fill(target, 0.0);
        double[] weights = new double[circle.getN()];
        Arrays.fill(weights, 2.0);
        optimizer.optimize(circle, target, weights, new double[] { 98.680, 47.345 });
        cov = optimizer.getCovariances();
        assertEquals(0.0016, cov[0][0], 0.001);
        assertEquals(3.2e-7, cov[0][1], 1.0e-9);
        assertEquals(cov[0][1], cov[1][0], 1.0e-14);
        assertEquals(0.0016, cov[1][1], 0.001);
        errors = optimizer.guessParametersErrors();
        assertEquals(0.004, errors[0], 0.001);
        assertEquals(0.004, errors[1], 0.001);

    }


    private static class LinearProblem implements DifferentiableMultivariateVectorialFunction, Serializable {

        private static final long serialVersionUID = 703247177355019415L;
        final RealMatrix factors;
        final double[] target;
        public LinearProblem(double[][] factors, double[] target) {
            this.factors = new BlockRealMatrix(factors);
            this.target  = target;
        }

        public double[] value(double[] variables) {
            return factors.operate(variables);
        }

        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                private static final long serialVersionUID = 556396458721526234L;
                public double[][] value(double[] point) {
                    return factors.getData();
                }
            };
        }

    }

    private static class Circle implements DifferentiableMultivariateVectorialFunction, Serializable {

        private static final long serialVersionUID = -4711170319243817874L;

        private ArrayList<Point2D.Double> points;

        public Circle() {
            points  = new ArrayList<Point2D.Double>();
        }

        public void addPoint(double px, double py) {
            points.add(new Point2D.Double(px, py));
        }

        public int getN() {
            return points.size();
        }

        public double getRadius(Point2D.Double center) {
            double r = 0;
            for (Point2D.Double point : points) {
                r += point.distance(center);
            }
            return r / points.size();
        }

        private double[][] jacobian(double[] point) {

            int n = points.size();
            Point2D.Double center = new Point2D.Double(point[0], point[1]);

            // gradient of the optimal radius
            double dRdX = 0;
            double dRdY = 0;
            for (Point2D.Double pk : points) {
                double dk = pk.distance(center);
                dRdX += (center.x - pk.x) / dk;
                dRdY += (center.y - pk.y) / dk;
            }
            dRdX /= n;
            dRdY /= n;

            // jacobian of the radius residuals
            double[][] jacobian = new double[n][2];
            for (int i = 0; i < n; ++i) {
                Point2D.Double pi = points.get(i);
                double di   = pi.distance(center);
                jacobian[i][0] = (center.x - pi.x) / di - dRdX;
                jacobian[i][1] = (center.y - pi.y) / di - dRdY;
            }

            return jacobian;

        }

        public double[] value(double[] variables)
        throws FunctionEvaluationException, IllegalArgumentException {

            Point2D.Double center = new Point2D.Double(variables[0], variables[1]);
            double radius = getRadius(center);

            double[] residuals = new double[points.size()];
            for (int i = 0; i < residuals.length; ++i) {
                residuals[i] = points.get(i).distance(center) - radius;
            }

            return residuals;

        }

        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                private static final long serialVersionUID = -4340046230875165095L;
                public double[][] value(double[] point) {
                    return jacobian(point);
                }
            };
        }

    }

    private static class QuadraticProblem implements DifferentiableMultivariateVectorialFunction, Serializable {

        private static final long serialVersionUID = 7072187082052755854L;
        private List<Double> x;
        private List<Double> y;

        public QuadraticProblem() {
            x = new ArrayList<Double>();
            y = new ArrayList<Double>();
        }

        public void addPoint(double x, double y) {
            this.x.add(x);
            this.y.add(y);
        }

        private double[][] jacobian(double[] variables) {
            double[][] jacobian = new double[x.size()][3];
            for (int i = 0; i < jacobian.length; ++i) {
                jacobian[i][0] = x.get(i) * x.get(i);
                jacobian[i][1] = x.get(i);
                jacobian[i][2] = 1.0;
            }
            return jacobian;
        }

        public double[] value(double[] variables) {
            double[] values = new double[x.size()];
            for (int i = 0; i < values.length; ++i) {
                values[i] = (variables[0] * x.get(i) + variables[1]) * x.get(i) + variables[2];
            }
            return values;
        }

        public MultivariateMatrixFunction jacobian() {
            return new MultivariateMatrixFunction() {
                private static final long serialVersionUID = -8673650298627399464L;
                public double[][] value(double[] point) {
                    return jacobian(point);
                }
            };
        }

    }

}
