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

import org.apache.commons.math.analysis.QuinticFunction;
import org.apache.commons.math.analysis.SinFunction;
import org.apache.commons.math.analysis.UnivariateFunction;
import org.apache.commons.math.analysis.XMinus5Function;
import org.apache.commons.math.exception.NumberIsTooLargeException;
import org.apache.commons.math.exception.NoBracketingException;
import org.apache.commons.math.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

/**
 * Base class for root-finding algorithms tests derived from
 * {@link BaseSecantSolver}.
 *
 * @version $Id$
 */
public abstract class BaseSecantSolverAbstractTest {
    /** Returns the solver to use to perform the tests.
     * @return the solver to use to perform the tests
     */
    protected abstract UnivariateRealSolver getSolver();

    /** Returns the expected number of evaluations for the
     * {@link #testQuinticZero} unit test. A value of {@code -1} indicates that
     * the test should be skipped for that solver.
     * @return the expected number of evaluations for the
     * {@link #testQuinticZero} unit test
     */
    protected abstract int[] getQuinticEvalCounts();

    private double getSolution(UnivariateRealSolver solver, int maxEval, UnivariateFunction f,
                               double left, double right, AllowedSolution allowedSolution) {
        try {
            @SuppressWarnings("unchecked")
            BracketedUnivariateRealSolver<UnivariateFunction> bracketing =
            (BracketedUnivariateRealSolver<UnivariateFunction>) solver;
            return bracketing.solve(100, f, left, right, allowedSolution);
        } catch (ClassCastException cce) {
            double baseRoot = solver.solve(maxEval, f, left, right);
            if ((baseRoot <= left) || (baseRoot >= right)) {
                // the solution slipped out of interval
                return Double.NaN;
            }
            PegasusSolver bracketing =
                    new PegasusSolver(solver.getRelativeAccuracy(), solver.getAbsoluteAccuracy(),
                                      solver.getFunctionValueAccuracy());
            return UnivariateRealSolverUtils.forceSide(maxEval - solver.getEvaluations(),
                                                       f, bracketing, baseRoot, left, right,
                                                       allowedSolution);
        }
    }

}
