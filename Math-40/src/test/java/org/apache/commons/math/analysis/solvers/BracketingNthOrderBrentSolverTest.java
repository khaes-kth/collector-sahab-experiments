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

package org.apache.commons.math.analysis.solvers;

import org.apache.commons.math.analysis.DifferentiableUnivariateFunction;
import org.apache.commons.math.analysis.QuinticFunction;
import org.apache.commons.math.analysis.UnivariateFunction;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.exception.TooManyEvaluationsException;
import org.apache.commons.math.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for {@link BracketingNthOrderBrentSolver bracketing n<sup>th</sup> order Brent} solver.
 *
 * @version $Id$
 */
public final class BracketingNthOrderBrentSolverTest extends BaseSecantSolverAbstractTest {
    /** {@inheritDoc} */
    @Override
    protected UnivariateRealSolver getSolver() {
        return new BracketingNthOrderBrentSolver();
    }

    /** {@inheritDoc} */
    @Override
    protected int[] getQuinticEvalCounts() {
        return new int[] {1, 3, 8, 1, 9, 4, 8, 1, 12, 1, 16};
    }

    @Test
    public void testIssue716() {
        BracketingNthOrderBrentSolver solver =
                new BracketingNthOrderBrentSolver(1.0e-12, 1.0e-10, 1.0e-22, 5);
        UnivariateFunction sharpTurn = new UnivariateFunction() {
            public double value(double x) {
                return (2 * x + 1) / (1.0e9 * (x + 1));
            }
        };
        double result = solver.solve(100, sharpTurn, -0.9999999, 30, 15, AllowedSolution.RIGHT_SIDE);
        Assert.assertEquals(0, sharpTurn.value(result), solver.getFunctionValueAccuracy());
        Assert.assertTrue(sharpTurn.value(result) >= 0);
        Assert.assertEquals(-0.5, result, 1.0e-10);
    }

    private void compare(TestFunction f) {
        compare(f, f.getRoot(), f.getMin(), f.getMax());
    }

    private void compare(DifferentiableUnivariateFunction f,
                         double root, double min, double max) {
        NewtonSolver newton = new NewtonSolver(1.0e-12);
        BracketingNthOrderBrentSolver bracketing =
                new BracketingNthOrderBrentSolver(1.0e-12, 1.0e-12, 1.0e-18, 5);
        double resultN;
        try {
            resultN = newton.solve(100, f, min, max);
        } catch (TooManyEvaluationsException tmee) {
            resultN = Double.NaN;
        }
        double resultB;
        try {
            resultB = bracketing.solve(100, f, min, max);
        } catch (TooManyEvaluationsException tmee) {
            resultB = Double.NaN;
        }
        Assert.assertEquals(root, resultN, newton.getAbsoluteAccuracy());
        Assert.assertEquals(root, resultB, bracketing.getAbsoluteAccuracy());
        Assert.assertTrue(bracketing.getEvaluations() < newton.getEvaluations());
    }

    private static abstract class TestFunction implements DifferentiableUnivariateFunction {

        private final double root;
        private final double min;
        private final double max;

        protected TestFunction(final double root, final double min, final double max) {
            this.root = root;
            this.min  = min;
            this.max  = max;
        }

        public double getRoot() {
            return root;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }

        public abstract double value(double x);

        public abstract double derivative(double x);

        public UnivariateFunction derivative() {
            return new UnivariateFunction() {
                public double value(double x) {
                     return derivative(x);
                }
            };
        }

    }

}
