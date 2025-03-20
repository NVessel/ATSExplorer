package service;

import derivative.DerivativeSystem;
import flanagan.integration.RungeKutta;
import model.PolynomialDependency;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import suppliers.ParametersDependenciesMatrixSupplier;
import suppliers.PolynomialDependenciesSupplier;
import suppliers.StatisticsSupplier;

import java.io.IOException;
import java.util.List;

public class CalculationService {

    private static final int ITERATIONS_COUNT = 12;
    private static final double MAX_ERROR_PERCENT_LIMIT = 0.1;

    public void calculateOnStatistics(ParametersDependenciesMatrixSupplier dependenciesMatrixSupplier,
                                      StatisticsSupplier statisticsSupplier) throws IOException, InvalidFormatException {
        List<List<Integer>> dependenciesMatrix = dependenciesMatrixSupplier.getExternalMatrix();
        List<List<Double>> statisticsMatrix = statisticsSupplier.getExternalStatistics();
        List<String> parametersNames = dependenciesMatrixSupplier.getParametersNames();

        List<PolynomialDependency> polynomialDependencies = new PolynomialDependenciesSupplier(dependenciesMatrix, statisticsMatrix)
                .getPolynomialDependencies();
        ExcelDrawingService excelDrawingService = new ExcelDrawingService(dependenciesMatrix, statisticsMatrix,
                parametersNames, polynomialDependencies);
        excelDrawingService.drawPolynomials();
        writeSystemToFiles(polynomialDependencies);

        double[] derivativeParametersValues = extractInitialValues(statisticsMatrix, dependenciesMatrix.size());
        double h = 0.01;
        double t0 = 0.0D;
        double t1 = 0.1D;
        double[][] derivativeParametersValuesForTimeMoments = new double[dependenciesMatrix.size()][ITERATIONS_COUNT];
        DerivativeSystem systemDerivative = new DerivativeSystem(dependenciesMatrix, polynomialDependencies);
        for (int j = 0; j < ITERATIONS_COUNT; j++) {
            double[] derivativeParametersValuesInGivenTimeMoment = RungeKutta.fourthOrder(systemDerivative, t0, derivativeParametersValues, t1, h);
            for (int i = 0; i < dependenciesMatrix.size(); i++) {
                derivativeParametersValuesForTimeMoments[i][j] = derivativeParametersValuesInGivenTimeMoment[i];
            }
            derivativeParametersValues = derivativeParametersValuesInGivenTimeMoment;
            t0 += 0.1;
            t1 += 0.1;
        }
        excelDrawingService.drawResults(extractInitialValues(statisticsMatrix, dependenciesMatrix.size()), derivativeParametersValuesForTimeMoments);
    }

    public void calculateOnManualSettings(ParametersDependenciesMatrixSupplier parametersDependenciesMatrixSupplier,
                                          List<PolynomialDependency> polynomialDependencies, double[] initialValues) {

    }

    private void writeSystemToFiles(List<PolynomialDependency> polynomialDependencies) {
        DifferentialSystemWriter differentialSystemWriter = new DifferentialSystemWriter();
        differentialSystemWriter.writeSystemToLatex(polynomialDependencies);
        differentialSystemWriter.makePdfFromLatexFile();
    }

    private double[] correctEvaluations(double[] yn, int timeCount, List<List<Double>> statisticsMatrix) {
        double[] correctedYn = new double[yn.length];
        for (int i = 0; i < yn.length; i++) {
            if ((statisticsMatrix.get(i).get(timeCount + 1) / yn[i] > 1 + MAX_ERROR_PERCENT_LIMIT) || (statisticsMatrix.get(i).get(timeCount + 1) / yn[i] < 1 - MAX_ERROR_PERCENT_LIMIT)) {
                correctedYn[i] = yn[i] * (statisticsMatrix.get(i).get(timeCount + 1) / yn[i]);
            } else {
                correctedYn[i] = yn[i];
            }
        }
        return correctedYn;
    }

    private double[] extractInitialValues(List<List<Double>> statisticsMatrix, int limitToStopAtExternalFactorsPart) {
        double[] initials = new double[limitToStopAtExternalFactorsPart];
        for (int i = 0; i < limitToStopAtExternalFactorsPart; i++) {
            initials[i] = statisticsMatrix.get(i).get(0);
        }
        return initials;
    }
}
