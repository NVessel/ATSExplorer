import flanagan.integration.DerivnFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import java.util.Arrays;
import java.util.List;

public class DerivSystemV2 implements DerivnFunction {

    private static final int PRECISION_DEGREE = 3;

    private final List<List<Integer>> posNegMatrix;
    private final List<List<Double>> statMatrix;

    public DerivSystemV2(List<List<Integer>> posNegMatrix, List<List<Double>> statMatrix) {
        this.posNegMatrix = posNegMatrix;
        this.statMatrix = statMatrix;
    }

    @Override
    public double[] derivn(double t, double[] x) {
        double[] dxdt = new double[this.posNegMatrix.size()];
        for (int i = 0; i < this.posNegMatrix.size(); i++) {
            double posPolynomMultiplication = 1;
            double negPolynomMultiplication = 1;
            for (int j = 0; j < this.posNegMatrix.size(); j++) {
                if (posNegMatrix.get(i).get(j) == 1) {
                    posPolynomMultiplication *= calculatePoli(i, j, x[j]);
                }
                else if (posNegMatrix.get(i).get(j) == -1) {
                    negPolynomMultiplication *= calculatePoli(i, j, x[j]);
                }
            }
            dxdt[i] = calculateExternalFactorsSum(t, true) * posPolynomMultiplication - calculateExternalFactorsSum(t, false) * negPolynomMultiplication;
        }
        return dxdt;
    }

    private double calculatePoli(int rowNumber, int columnNumber, double usedValue) {
        WeightedObservedPoints weightedObservedPoints = new WeightedObservedPoints();
        for (int k = 0; k < statMatrix.get(0).size(); k++) { //might be easier
            weightedObservedPoints.add(statMatrix.get(columnNumber).get(k), statMatrix.get(rowNumber).get(k));
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(PRECISION_DEGREE);
        double[] coeffs = fitter.fit(weightedObservedPoints.toList());
        return buildPoli(coeffs, usedValue);
    }

    private double buildPoli(double[] coeffs, double usedValue) {
        double resultSum = 0;
        for (int k = 0; k < coeffs.length; k++) {
            resultSum += coeffs[k] * Math.pow(usedValue, k);
        }
        return resultSum;
    }

    //Turned off for now
    private double calculateExternalFactorsSum(double t, boolean isPositiveSide) {
        return 1;
    }
}
