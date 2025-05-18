package derivative;

import lombok.AllArgsConstructor;
import model.PolynomialDependency;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math3.util.Pair;
import org.apache.commons.math3.util.Precision;

import java.util.List;

@AllArgsConstructor
public class ApacheMathDerivativeSystem implements FirstOrderDifferentialEquations {

    private final List<List<Integer>> dependencyMatrix;
    private final List<PolynomialDependency> polynomialDependencies;

    @Override
    public int getDimension() {
        return dependencyMatrix.size();
    }

    @Override
    public void computeDerivatives(double t, double[] y, double[] yDot)
            throws MaxCountExceededException, DimensionMismatchException {
        double[] dxdt = new double[this.dependencyMatrix.size()];
        for (int derivativeParameterNumber = 0; derivativeParameterNumber < this.dependencyMatrix.size(); derivativeParameterNumber++) {
            double positiveSideOfParameterMultiplication = 1;
            double negativeSideOfParameterMultiplication = 1;

            for (int affectingOnDerivativeParameterNumber = 0; affectingOnDerivativeParameterNumber < this.dependencyMatrix.size(); affectingOnDerivativeParameterNumber++) {
                if (dependencyMatrix.get(derivativeParameterNumber).get(affectingOnDerivativeParameterNumber) == 1) {
                    positiveSideOfParameterMultiplication *= calculatePartOfForDerivativeParameterUsingExactAffecting(derivativeParameterNumber, affectingOnDerivativeParameterNumber, y[affectingOnDerivativeParameterNumber]);
                } else if (dependencyMatrix.get(derivativeParameterNumber).get(affectingOnDerivativeParameterNumber) == -1) {
                    negativeSideOfParameterMultiplication *= calculatePartOfForDerivativeParameterUsingExactAffecting(derivativeParameterNumber, affectingOnDerivativeParameterNumber, y[affectingOnDerivativeParameterNumber]);
                }
            }

            double positiveSideOfExternalFactorsSum = 0;
            double negativeSideOfExternalFactorsSum = 0;

            for (int externalFactorNumber = this.dependencyMatrix.size(); externalFactorNumber < this.dependencyMatrix.get(0).size(); externalFactorNumber++) {
                if (dependencyMatrix.get(derivativeParameterNumber).get(externalFactorNumber) == 1) {
                    positiveSideOfExternalFactorsSum += calculatePartOfForDerivativeParameterUsingExactAffecting(derivativeParameterNumber, externalFactorNumber, t);
                } else if (dependencyMatrix.get(derivativeParameterNumber).get(externalFactorNumber) == -1) {
                    negativeSideOfExternalFactorsSum += calculatePartOfForDerivativeParameterUsingExactAffecting(derivativeParameterNumber, externalFactorNumber, t);
                }
            }
            dxdt[derivativeParameterNumber] = setOneIfZero(positiveSideOfExternalFactorsSum) * positiveSideOfParameterMultiplication
                    - setOneIfZero(negativeSideOfExternalFactorsSum) * negativeSideOfParameterMultiplication;
        }
        System.arraycopy(dxdt, 0, yDot, 0, yDot.length);
    }

    private double setOneIfZero(double factorsSum) {
        if (Precision.equals(factorsSum, 0, 0.000001)) {
            return 1;
        }
        return factorsSum;
    }

    private double calculatePartOfForDerivativeParameterUsingExactAffecting(int derivativeParameterNumber, int externalOrAffectingParameterNumber, double affectingParameterFunctionValueInGivenTime) {
        for (PolynomialDependency polynomialDependency : polynomialDependencies) {
            if (polynomialDependency.getDerivativeParameterNumberToAffectingParameterNumber().equals(new Pair<>(derivativeParameterNumber, externalOrAffectingParameterNumber))) {
                return evaluatePolynomialForValueUsingCoefficients(polynomialDependency.getPolynomialCoefficients(), affectingParameterFunctionValueInGivenTime);
            }
        }
        throw new IllegalStateException("Polynomial wasn't found for numbers");
    }

    private double evaluatePolynomialForValueUsingCoefficients(double[] regressionPolynomialCoefficients, double affectingParameterValue) {
        double resultSum = 0;
        for (int k = 0; k < regressionPolynomialCoefficients.length; k++) {
            resultSum += regressionPolynomialCoefficients[k] * Math.pow(affectingParameterValue, k);
        }
        return resultSum;
    }
}
