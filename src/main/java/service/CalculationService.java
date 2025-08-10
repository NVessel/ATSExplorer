package service;

import derivative.ApacheMathDerivativeSystem;
import model.PolynomialDependency;
import org.apache.commons.math3.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import suppliers.ParametersDependenciesMatrixSupplier;
import suppliers.PolynomialDependenciesSupplier;
import suppliers.StatisticsSupplier;

import java.io.IOException;
import java.util.List;

public class CalculationService {

    private static final int ITERATIONS_COUNT = 10;
    private static final double MAX_ERROR_PERCENT_LIMIT = 0.15;
    private static final String SYMBOLIC_TEX_FILENAME = "symbolicSystem.tex";
    private static final String TEX_FILENAME = "equationSystem.tex";
    private static final String POLYNOMIALS_APART_TEX_FILENAME = "polynomialsApart.tex";

    public void calculateOnStatistics(ParametersDependenciesMatrixSupplier dependenciesMatrixSupplier,
                                      StatisticsSupplier statisticsSupplier,
                                      List<Double> limitValues) throws IOException, InvalidFormatException {
        List<List<Integer>> dependenciesMatrix = dependenciesMatrixSupplier.getExternalMatrix();
        List<List<Double>> statisticsMatrix = statisticsSupplier.getExternalStatistics();
        List<String> parametersNames = dependenciesMatrixSupplier.getParametersNames();

        List<PolynomialDependency> polynomialDependencies = new PolynomialDependenciesSupplier(dependenciesMatrix, statisticsMatrix)
                .getPolynomialDependencies();
        ExcelDrawingService excelDrawingService = new ExcelDrawingService(dependenciesMatrix, statisticsMatrix,
                parametersNames, polynomialDependencies, limitValues);
        excelDrawingService.drawPolynomials();
        writeSystemToFiles(polynomialDependencies, dependenciesMatrix.size());

        double h = 0.1;
        double t0 = 0;
        double t1 = t0 + h;
        double[] derivativeParametersValues = extractInitialValues(statisticsMatrix, dependenciesMatrix.size());
        double[][] derivativeParametersValuesForTimeMoments = new double[dependenciesMatrix.size()][ITERATIONS_COUNT];
        for (int i = 0; i < dependenciesMatrix.size(); i++) {
            derivativeParametersValuesForTimeMoments[i][0] = derivativeParametersValues[i];
        }
        ClassicalRungeKuttaIntegrator classicalRungeKuttaIntegrator = new ClassicalRungeKuttaIntegrator(h);
        ApacheMathDerivativeSystem apacheMathDerivativeSystem = new ApacheMathDerivativeSystem(dependenciesMatrix, polynomialDependencies);
        for (int j = 1; j < ITERATIONS_COUNT; j++) {
            double[] derivativeParametersValuesInGivenTimeMoment = classicalRungeKuttaIntegrator.singleStep(apacheMathDerivativeSystem, t0, derivativeParametersValues, t1);
            for (int i = 0; i < dependenciesMatrix.size(); i++) {
                derivativeParametersValuesForTimeMoments[i][j] = derivativeParametersValuesInGivenTimeMoment[i];
            }
            derivativeParametersValues = derivativeParametersValuesInGivenTimeMoment;
            t0 += h;
            t1 += h;
        }
        excelDrawingService.drawResults(derivativeParametersValuesForTimeMoments);
    }

    public void calculateOnManualSettings(ParametersDependenciesMatrixSupplier parametersDependenciesMatrixSupplier,
                                          List<PolynomialDependency> polynomialDependencies, double[] initialValues) {

    }

    private void writeSystemToFiles(List<PolynomialDependency> polynomialDependencies, int parametersQuantity) {
        DifferentialSystemWriter differentialSystemWriter = new DifferentialSystemWriter(1);
        differentialSystemWriter.writeSystemInNumericViewToLatex(polynomialDependencies, TEX_FILENAME);
        differentialSystemWriter.writeSystemInSymbolicWayToLatex(polynomialDependencies, SYMBOLIC_TEX_FILENAME, parametersQuantity);
        differentialSystemWriter.setPolynomialCounterInFiles(1);
        differentialSystemWriter.writePolynomialDependenciesPartiallyToLatex(polynomialDependencies, POLYNOMIALS_APART_TEX_FILENAME, parametersQuantity);
        differentialSystemWriter.makePdfFromLatexFile(TEX_FILENAME);
        differentialSystemWriter.makePdfFromLatexFile(SYMBOLIC_TEX_FILENAME);
        differentialSystemWriter.makePdfFromLatexFile(POLYNOMIALS_APART_TEX_FILENAME);
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
