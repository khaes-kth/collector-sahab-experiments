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
package org.apache.commons.math3.optimization.direct;

import java.util.Arrays;
import java.util.Random;

import org.apache.commons.math3.Retry;
import org.apache.commons.math3.RetryRunner;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;

/**
 * Test for {@link CMAESOptimizer}.
 */
@RunWith(RetryRunner.class)
public class CMAESOptimizerTest {

    static final int DIM = 13;
    static final int LAMBDA = 4 + (int)(3.*Math.log(DIM));

    /**
     * Cf. MATH-867
     */
    @Test
    public void testFitAccuracyDependsOnBoundary() {
        final CMAESOptimizer optimizer = new CMAESOptimizer();
        final MultivariateFunction fitnessFunction = new MultivariateFunction() {
                public double value(double[] parameters) {
                    final double target = 11.1;
                    final double error = target - parameters[0];
                    return error * error;
                }
            };

        final double[] start = { 1 };
 
        // No bounds.
        PointValuePair result = optimizer.optimize(100000, fitnessFunction, GoalType.MINIMIZE,
                                                   start);
        final double resNoBound = result.getPoint()[0];

        // Optimum is near the lower bound.
        final double[] lower = { -20 };
        final double[] upper = { 5e16 };
        result = optimizer.optimize(100000, fitnessFunction, GoalType.MINIMIZE,
                                    start, lower, upper);
        final double resNearLo = result.getPoint()[0];

        // Optimum is near the upper bound.
        lower[0] = -5e16;
        upper[0] = 20;
        result = optimizer.optimize(100000, fitnessFunction, GoalType.MINIMIZE,
                                    start, lower, upper);
        final double resNearHi = result.getPoint()[0];

        // System.out.println("resNoBound=" + resNoBound +
        //                    " resNearLo=" + resNearLo +
        //                    " resNearHi=" + resNearHi);

        // The two values currently differ by a substantial amount, indicating that
        // the bounds definition can prevent reaching the optimum.
        Assert.assertEquals(resNoBound, resNearLo, 1e-3);
        Assert.assertEquals(resNoBound, resNearHi, 1e-3);
    }
 
    /**
     * @param func Function to optimize.
     * @param startPoint Starting point.
     * @param inSigma Individual input sigma.
     * @param boundaries Upper / lower point limit.
     * @param goal Minimization or maximization.
     * @param lambda Population size used for offspring.
     * @param isActive Covariance update mechanism.
     * @param diagonalOnly Simplified covariance update.
     * @param stopValue Termination criteria for optimization.
     * @param fTol Tolerance relative error on the objective function.
     * @param pointTol Tolerance for checking that the optimum is correct.
     * @param maxEvaluations Maximum number of evaluations.
     * @param expected Expected point / value.
     */
    private void doTest(MultivariateFunction func,
            double[] startPoint,
            double[] inSigma,
            double[][] boundaries,
            GoalType goal,
            int lambda,
            boolean isActive,
            int diagonalOnly, 
            double stopValue,
            double fTol,
            double pointTol,
            int maxEvaluations,
            PointValuePair expected) {
        int dim = startPoint.length;
        // test diagonalOnly = 0 - slow but normally fewer feval#
        CMAESOptimizer optim = new CMAESOptimizer(lambda, inSigma, 30000,
                                                  stopValue, isActive, diagonalOnly,
                                                  0, new MersenneTwister(), false);
        final double[] lB = boundaries == null ? null : boundaries[0];
        final double[] uB = boundaries == null ? null : boundaries[1];
        PointValuePair result = optim.optimize(maxEvaluations, func, goal, startPoint, lB, uB);
        // System.out.println("sol=" + Arrays.toString(result.getPoint()));
        Assert.assertEquals(expected.getValue(), result.getValue(), fTol);
        for (int i = 0; i < dim; i++) {
            Assert.assertEquals(expected.getPoint()[i], result.getPoint()[i], pointTol);
        }
    }

    private static double[] point(int n, double value) {
        double[] ds = new double[n];
        Arrays.fill(ds, value);
        return ds;
    }

    private static double[][] boundaries(int dim,
            double lower, double upper) {
        double[][] boundaries = new double[2][dim];
        for (int i = 0; i < dim; i++)
            boundaries[0][i] = lower;
        for (int i = 0; i < dim; i++)
            boundaries[1][i] = upper;
        return boundaries;
    }

    private static class Sphere implements MultivariateFunction {

        public double value(double[] x) {
            double f = 0;
            for (int i = 0; i < x.length; ++i)
                f += x[i] * x[i];
            return f;
        }
    }

    private static class Cigar implements MultivariateFunction {
        private double factor;

        Cigar() {
            this(1e3);
        }

        Cigar(double axisratio) {
            factor = axisratio * axisratio;
        }

        public double value(double[] x) {
            double f = x[0] * x[0];
            for (int i = 1; i < x.length; ++i)
                f += factor * x[i] * x[i];
            return f;
        }
    }

    private static class Tablet implements MultivariateFunction {
        private double factor;

        Tablet() {
            this(1e3);
        }

        Tablet(double axisratio) {
            factor = axisratio * axisratio;
        }

        public double value(double[] x) {
            double f = factor * x[0] * x[0];
            for (int i = 1; i < x.length; ++i)
                f += x[i] * x[i];
            return f;
        }
    }

    private static class CigTab implements MultivariateFunction {
        private double factor;

        CigTab() {
            this(1e4);
        }

        CigTab(double axisratio) {
            factor = axisratio;
        }

        public double value(double[] x) {
            int end = x.length - 1;
            double f = x[0] * x[0] / factor + factor * x[end] * x[end];
            for (int i = 1; i < end; ++i)
                f += x[i] * x[i];
            return f;
        }
    }

    private static class TwoAxes implements MultivariateFunction {

        private double factor;

        TwoAxes() {
            this(1e6);
        }

        TwoAxes(double axisratio) {
            factor = axisratio * axisratio;
        }

        public double value(double[] x) {
            double f = 0;
            for (int i = 0; i < x.length; ++i)
                f += (i < x.length / 2 ? factor : 1) * x[i] * x[i];
            return f;
        }
    }

    private static class ElliRotated implements MultivariateFunction {
        private Basis B = new Basis();
        private double factor;

        ElliRotated() {
            this(1e3);
        }

        ElliRotated(double axisratio) {
            factor = axisratio * axisratio;
        }

        public double value(double[] x) {
            double f = 0;
            x = B.Rotate(x);
            for (int i = 0; i < x.length; ++i)
                f += Math.pow(factor, i / (x.length - 1.)) * x[i] * x[i];
            return f;
        }
    }

    private static class Elli implements MultivariateFunction {

        private double factor;

        Elli() {
            this(1e3);
        }

        Elli(double axisratio) {
            factor = axisratio * axisratio;
        }

        public double value(double[] x) {
            double f = 0;
            for (int i = 0; i < x.length; ++i)
                f += Math.pow(factor, i / (x.length - 1.)) * x[i] * x[i];
            return f;
        }
    }

    private static class MinusElli implements MultivariateFunction {

        public double value(double[] x) {
            return 1.0-(new Elli().value(x));
        }
    }

    private static class DiffPow implements MultivariateFunction {

        public double value(double[] x) {
            double f = 0;
            for (int i = 0; i < x.length; ++i)
                f += Math.pow(Math.abs(x[i]), 2. + 10 * (double) i
                        / (x.length - 1.));
            return f;
        }
    }

    private static class SsDiffPow implements MultivariateFunction {

        public double value(double[] x) {
            double f = Math.pow(new DiffPow().value(x), 0.25);
            return f;
        }
    }

    private static class Rosen implements MultivariateFunction {

        public double value(double[] x) {
            double f = 0;
            for (int i = 0; i < x.length - 1; ++i)
                f += 1e2 * (x[i] * x[i] - x[i + 1]) * (x[i] * x[i] - x[i + 1])
                + (x[i] - 1.) * (x[i] - 1.);
            return f;
        }
    }

    private static class Ackley implements MultivariateFunction {
        private double axisratio;

        Ackley(double axra) {
            axisratio = axra;
        }

        public Ackley() {
            this(1);
        }

        public double value(double[] x) {
            double f = 0;
            double res2 = 0;
            double fac = 0;
            for (int i = 0; i < x.length; ++i) {
                fac = Math.pow(axisratio, (i - 1.) / (x.length - 1.));
                f += fac * fac * x[i] * x[i];
                res2 += Math.cos(2. * Math.PI * fac * x[i]);
            }
            f = (20. - 20. * Math.exp(-0.2 * Math.sqrt(f / x.length))
                    + Math.exp(1.) - Math.exp(res2 / x.length));
            return f;
        }
    }

    private static class Rastrigin implements MultivariateFunction {

        private double axisratio;
        private double amplitude;

        Rastrigin() {
            this(1, 10);
        }

        Rastrigin(double axisratio, double amplitude) {
            this.axisratio = axisratio;
            this.amplitude = amplitude;
        }

        public double value(double[] x) {
            double f = 0;
            double fac;
            for (int i = 0; i < x.length; ++i) {
                fac = Math.pow(axisratio, (i - 1.) / (x.length - 1.));
                if (i == 0 && x[i] < 0)
                    fac *= 1.;
                f += fac * fac * x[i] * x[i] + amplitude
                * (1. - Math.cos(2. * Math.PI * fac * x[i]));
            }
            return f;
        }
    }

    private static class Basis {
        double[][] basis;
        Random rand = new Random(2); // use not always the same basis

        double[] Rotate(double[] x) {
            GenBasis(x.length);
            double[] y = new double[x.length];
            for (int i = 0; i < x.length; ++i) {
                y[i] = 0;
                for (int j = 0; j < x.length; ++j)
                    y[i] += basis[i][j] * x[j];
            }
            return y;
        }

        void GenBasis(int DIM) {
            if (basis != null ? basis.length == DIM : false)
                return;

            double sp;
            int i, j, k;

            /* generate orthogonal basis */
            basis = new double[DIM][DIM];
            for (i = 0; i < DIM; ++i) {
                /* sample components gaussian */
                for (j = 0; j < DIM; ++j)
                    basis[i][j] = rand.nextGaussian();
                /* substract projection of previous vectors */
                for (j = i - 1; j >= 0; --j) {
                    for (sp = 0., k = 0; k < DIM; ++k)
                        sp += basis[i][k] * basis[j][k]; /* scalar product */
                    for (k = 0; k < DIM; ++k)
                        basis[i][k] -= sp * basis[j][k]; /* substract */
                }
                /* normalize */
                for (sp = 0., k = 0; k < DIM; ++k)
                    sp += basis[i][k] * basis[i][k]; /* squared norm */
                for (k = 0; k < DIM; ++k)
                    basis[i][k] /= Math.sqrt(sp);
            }
        }
    }
}
