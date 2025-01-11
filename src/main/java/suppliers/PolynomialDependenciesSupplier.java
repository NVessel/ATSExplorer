package suppliers;

import lombok.AllArgsConstructor;
import model.PolynomialDependency;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.util.Pair;
import utils.PolynomialUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class PolynomialDependenciesSupplier {

    private static final int PARAMETERS_REGRESSION_DEGREE = 1;
    private static final int EXTERNAL_FACTOR_REGRESSION_DEGREE = 1;

    private final List<List<Integer>> dependencyMatrix;
    private final List<List<Double>> statisticMatrix;

    public List<PolynomialDependency> getPolynomialDependencies() {
        List<PolynomialDependency> polynomialDependencies = new ArrayList<>();
        for (int derivativeParameterNumber = 0; derivativeParameterNumber < this.dependencyMatrix.size(); derivativeParameterNumber++) {
            for (int affectingOnDerivativeParameterNumber = 0; affectingOnDerivativeParameterNumber < this.dependencyMatrix.size(); affectingOnDerivativeParameterNumber++) {
                if (dependencyMatrix.get(derivativeParameterNumber).get(affectingOnDerivativeParameterNumber) != 0) {
                    polynomialDependencies.add(buildParameterPolynomialDependency(derivativeParameterNumber, affectingOnDerivativeParameterNumber));
                }
            }

            for (int externalFactorNumber = this.dependencyMatrix.size(); externalFactorNumber < this.dependencyMatrix.get(0).size(); externalFactorNumber++) {
                if (dependencyMatrix.get(derivativeParameterNumber).get(externalFactorNumber) != 0) {
                    polynomialDependencies.add(buildExternalFactorPolynomialDependency(derivativeParameterNumber, externalFactorNumber));
                }
            }
        }
        return polynomialDependencies;
    }

    private PolynomialDependency buildExternalFactorPolynomialDependency(int derivativeParameterNumber, int externalFactorNumber) {
        OLSMultipleLinearRegression regressionBetweenExternalFactorToTime = findRegressionBetweenExternalFactorToTime(externalFactorNumber);
        double[] coefficientsOfExternalFactorPolynomial = PolynomialUtils.truncatePolynomialCoefficientsDigits(regressionBetweenExternalFactorToTime.estimateRegressionParameters());
        return new PolynomialDependency(
                new Pair<>(derivativeParameterNumber, externalFactorNumber),
                findDependencyPositiveness(derivativeParameterNumber, externalFactorNumber),
                coefficientsOfExternalFactorPolynomial,
                new HashMap<>()
        );
    }

    private PolynomialDependency buildParameterPolynomialDependency(int derivativeParameterNumber, int affectingOnDerivativeParameterNumber) {
        OLSMultipleLinearRegression regression = findRegressionBetweenParameters(derivativeParameterNumber, affectingOnDerivativeParameterNumber);
        double[] coefficientsOfDependencyPolynomial = PolynomialUtils.truncatePolynomialCoefficientsDigits(regression.estimateRegressionParameters());
        Map<String, Double> regressionStatisticParams = collectRegressionStatisticParams(regression);
        return new PolynomialDependency(
                new Pair<>(derivativeParameterNumber, affectingOnDerivativeParameterNumber),
                findDependencyPositiveness(derivativeParameterNumber, affectingOnDerivativeParameterNumber),
                coefficientsOfDependencyPolynomial,
                regressionStatisticParams);
    }

    private boolean findDependencyPositiveness(int derivativeParameterNumber, int affectingOnDerivativeParameterOrExternalNumber) {
        return dependencyMatrix.get(derivativeParameterNumber).get(affectingOnDerivativeParameterOrExternalNumber) == 1;
    }

    private OLSMultipleLinearRegression findRegressionBetweenParameters(int derivativeParameterNumber, int affectingParameterNumber) {
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        double[] sampleY = new double[statisticMatrix.get(0).size()];
        double[][] sampleX = new double[statisticMatrix.get(0).size()][PARAMETERS_REGRESSION_DEGREE];
        for (int k = 0; k < statisticMatrix.get(0).size(); k++) {
            sampleY[k] = statisticMatrix.get(derivativeParameterNumber).get(k);
            sampleX[k][0] = statisticMatrix.get(affectingParameterNumber).get(k);
            regression.newSampleData(sampleY, sampleX);
        }
        return regression;
    }

    private OLSMultipleLinearRegression findRegressionBetweenExternalFactorToTime(int externalFactorNumber) {
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        double[] sampleY = new double[statisticMatrix.get(0).size()];
        double[][] sampleX = new double[statisticMatrix.get(0).size()][EXTERNAL_FACTOR_REGRESSION_DEGREE];
        for (int k = 0; k < statisticMatrix.get(0).size(); k++) {
            sampleY[k] = statisticMatrix.get(externalFactorNumber).get(k);
            sampleX[k][0] = (double) k/statisticMatrix.get(0).size();
            regression.newSampleData(sampleY, sampleX);
        }
        return regression;
    }

    private Map<String, Double> collectRegressionStatisticParams(OLSMultipleLinearRegression regression) {
        Map<String, Double> statisticParams = new HashMap<>();
        statisticParams.put("Дисперсия регрессии", PolynomialUtils.truncatePolynomialCoefficientDigits(regression.estimateRegressandVariance()));
        double fisher = (regression.calculateRSquared() / (1 - regression.calculateRSquared() + 0.001))
                * ((statisticMatrix.get(0).size() - PARAMETERS_REGRESSION_DEGREE - 1) / (double) PARAMETERS_REGRESSION_DEGREE);
        statisticParams.put("Критерий Фишера", PolynomialUtils.truncatePolynomialCoefficientDigits(fisher));
        statisticParams.put("Дисперсия ошибки", PolynomialUtils.truncatePolynomialCoefficientDigits(regression.estimateErrorVariance()));
        statisticParams.put("Среднеквадратическое отклонение ошибки", PolynomialUtils.truncatePolynomialCoefficientDigits(regression.estimateRegressionStandardError()));
        return statisticParams;
    }
}
