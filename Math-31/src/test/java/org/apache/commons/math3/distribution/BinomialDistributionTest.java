/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.apache.commons.math3.distribution;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases for BinomialDistribution. Extends IntegerDistributionAbstractTest.
 * See class javadoc for IntegerDistributionAbstractTest for details.
 *
 * @version $Id$
 *          2009) $
 */
public class BinomialDistributionTest extends IntegerDistributionAbstractTest {

    // -------------- Implementations for abstract methods
    // -----------------------

    /** Creates the default discrete distribution instance to use in tests. */
    @Override
    public IntegerDistribution makeDistribution() {
        return new BinomialDistribution(10, 0.70);
    }

    /** Creates the default probability density test input values */
    @Override
    public int[] makeDensityTestPoints() {
        return new int[] { -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    }

    /** Creates the default probability density test expected values */
    @Override
    public double[] makeDensityTestValues() {
        return new double[] { 0d, 0.0000059049d, 0.000137781d, 0.0014467d,
                0.00900169d, 0.0367569d, 0.102919d, 0.200121d, 0.266828d,
                0.233474d, 0.121061d, 0.0282475d, 0d };
    }

    /** Creates the default cumulative probability density test input values */
    @Override
    public int[] makeCumulativeTestPoints() {
        return makeDensityTestPoints();
    }

    /** Creates the default cumulative probability density test expected values */
    @Override
    public double[] makeCumulativeTestValues() {
        return new double[] { 0d, 0.0000d, 0.0001d, 0.0016d, 0.0106d, 0.0473d,
                0.1503d, 0.3504d, 0.6172d, 0.8507d, 0.9718d, 1d, 1d };
    }

    /** Creates the default inverse cumulative probability test input values */
    @Override
    public double[] makeInverseCumulativeTestPoints() {
        return new double[] { 0, 0.001d, 0.010d, 0.025d, 0.050d, 0.100d,
                0.999d, 0.990d, 0.975d, 0.950d, 0.900d, 1 };
    }

    /**
     * Creates the default inverse cumulative probability density test expected
     * values
     */
    @Override
    public int[] makeInverseCumulativeTestValues() {
        return new int[] { 0, 2, 3, 4, 5, 5, 10, 10, 10, 9, 9, 10 };
    }

    // ----------------- Additional test cases ---------------------------------

    @Test
    public void testMath718() {
        // for large trials the evaluation of ContinuedFraction was inaccurate
        // do a sweep over several large trials to test if the current implementation is
        // numerically stable.

        for (int trials = 500000; trials < 20000000; trials += 100000) {
            BinomialDistribution dist = new BinomialDistribution(trials, 0.5);
            int p = dist.inverseCumulativeProbability(0.5);
            Assert.assertEquals(trials / 2, p);
        }

    }
}
