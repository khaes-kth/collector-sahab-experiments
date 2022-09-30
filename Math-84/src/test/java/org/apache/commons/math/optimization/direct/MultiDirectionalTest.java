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

package org.apache.commons.math.optimization.direct;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.SimpleScalarValueChecker;
import org.junit.Assert;
import org.junit.Test;

public class MultiDirectionalTest {


  @Test
  public void testMinimizeMaximize()
      throws FunctionEvaluationException, ConvergenceException {

      // the following function has 4 local extrema:
      final double xM        = -3.841947088256863675365;
      final double yM        = -1.391745200270734924416;
      final double xP        =  0.2286682237349059125691;
      final double yP        = -yM;
      final double valueXmYm =  0.2373295333134216789769; // local  maximum
      final double valueXmYp = -valueXmYm;                // local  minimum
      final double valueXpYm = -0.7290400707055187115322; // global minimum
      final double valueXpYp = -valueXpYm;                // global maximum
      MultivariateRealFunction fourExtrema = new MultivariateRealFunction() {
          private static final long serialVersionUID = -7039124064449091152L;
          public double value(double[] variables) throws FunctionEvaluationException {
              final double x = variables[0];
              final double y = variables[1];
              return ((x == 0) || (y == 0)) ? 0 : (Math.atan(x) * Math.atan(x + 2) * Math.atan(y) * Math.atan(y) / (x * y));
          }
      };

      MultiDirectional optimizer = new MultiDirectional();
      optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-11, 1.0e-30));
      optimizer.setMaxIterations(200);
      optimizer.setStartConfiguration(new double[] { 0.2, 0.2 });
      RealPointValuePair optimum;

      // minimization
      optimum = optimizer.optimize(fourExtrema, GoalType.MINIMIZE, new double[] { -3.0, 0 });
      Assert.assertEquals(xM,        optimum.getPoint()[0], 4.0e-6);
      Assert.assertEquals(yP,        optimum.getPoint()[1], 3.0e-6);
      Assert.assertEquals(valueXmYp, optimum.getValue(),    8.0e-13);
      Assert.assertTrue(optimizer.getEvaluations() > 120);
      Assert.assertTrue(optimizer.getEvaluations() < 150);

      optimum = optimizer.optimize(fourExtrema, GoalType.MINIMIZE, new double[] { +1, 0 });
      Assert.assertEquals(xP,        optimum.getPoint()[0], 2.0e-8);
      Assert.assertEquals(yM,        optimum.getPoint()[1], 3.0e-6);
      Assert.assertEquals(valueXpYm, optimum.getValue(),    2.0e-12);              
      Assert.assertTrue(optimizer.getEvaluations() > 120);
      Assert.assertTrue(optimizer.getEvaluations() < 150);

      // maximization
      optimum = optimizer.optimize(fourExtrema, GoalType.MAXIMIZE, new double[] { -3.0, 0.0 });
      Assert.assertEquals(xM,        optimum.getPoint()[0], 7.0e-7);
      Assert.assertEquals(yM,        optimum.getPoint()[1], 3.0e-7);
      Assert.assertEquals(valueXmYm, optimum.getValue(),    2.0e-14);
      Assert.assertTrue(optimizer.getEvaluations() > 120);
      Assert.assertTrue(optimizer.getEvaluations() < 150);

      optimizer.setConvergenceChecker(new SimpleScalarValueChecker(1.0e-15, 1.0e-30));
      optimum = optimizer.optimize(fourExtrema, GoalType.MAXIMIZE, new double[] { +1, 0 });
      Assert.assertEquals(xP,        optimum.getPoint()[0], 2.0e-8);
      Assert.assertEquals(yP,        optimum.getPoint()[1], 3.0e-6);
      Assert.assertEquals(valueXpYp, optimum.getValue(),    2.0e-12);
      Assert.assertTrue(optimizer.getEvaluations() > 180);
      Assert.assertTrue(optimizer.getEvaluations() < 220);

  }


  private static class Gaussian2D implements MultivariateRealFunction {

      private final double[] maximumPosition;

      private final double std;
      
      public Gaussian2D(double xOpt, double yOpt, double std) {
          maximumPosition = new double[] { xOpt, yOpt };
          this.std = std;
      }

      public double getMaximum() {
          return value(maximumPosition);
      }

      public double[] getMaximumPosition() {
          return maximumPosition.clone();
      }

      public double value(double[] point) {
          final double x = point[0], y = point[1];
          final double twoS2 = 2.0 * std * std;
          return 1.0 / (twoS2 * Math.PI) * Math.exp(-(x * x + y * y) / twoS2);
      }
  }

  private int count;

}
