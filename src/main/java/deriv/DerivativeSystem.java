package deriv;

import model.*;
import service.DifferentialSystemWriter;
import flanagan.integration.DerivnFunction;
import lombok.extern.java.Log;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import utils.PolyUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.logging.Level;

@Log
public class DerivativeSystem implements DerivnFunction {

    private static final int REGRESSION_DEGREE = 1;

    private static final int GRAPH_WIDTH = 10;
    private static final int GRAPH_LENGTH = 20;
    private static final boolean IS_ENABLED_POLY_DRAWING = true;

    private List<ParameterPolynomial> parameterPolynomialList;
    private List<ExternalFactorPolynomial> externalFactorPolynomialList;
    private final List<List<Integer>> dependencyMatrix;
    private final List<List<Double>> statisticMatrix;
    private final List<String> parametersNames;
    private final List<RowColumnToRegressionModel> rowColumnToRegressionModelCache = new LinkedList<>();
    private final List<ParameterToDependencies> parameterToDependenciesCache = new LinkedList<>();

    public DerivativeSystem(List<List<Integer>> dependencyMatrix, List<List<Double>> statisticMatrix, List<String> parametersNames) {
        this.dependencyMatrix = dependencyMatrix;
        this.statisticMatrix = statisticMatrix;
        this.parametersNames = parametersNames;
    }

    @Override
    public double[] derivn(double t, double[] x) {
        double[] dxdt = new double[this.dependencyMatrix.size()];
        for (int derivatedParameterNumber = 0; derivatedParameterNumber < this.dependencyMatrix.size(); derivatedParameterNumber++) {
            double positiveSideOfParameterMultiplication = 1;
            double negativeSideOfParameterMultiplication = 1;
            this.parameterPolynomialList = new ArrayList<>();
            this.externalFactorPolynomialList = new ArrayList<>();
            for (int affectingOnDerivatedParameterNumber = 0; affectingOnDerivatedParameterNumber < this.dependencyMatrix.size(); affectingOnDerivatedParameterNumber++) {
                if (dependencyMatrix.get(derivatedParameterNumber).get(affectingOnDerivatedParameterNumber) == 1) {
                    positiveSideOfParameterMultiplication *= createFunctionDependencyAndCalculatePartOfSideForDerivatedParameter(derivatedParameterNumber, affectingOnDerivatedParameterNumber, x[affectingOnDerivatedParameterNumber], true);
                }
                else if (dependencyMatrix.get(derivatedParameterNumber).get(affectingOnDerivatedParameterNumber) == -1) {
                    negativeSideOfParameterMultiplication *= createFunctionDependencyAndCalculatePartOfSideForDerivatedParameter(derivatedParameterNumber, affectingOnDerivatedParameterNumber, x[affectingOnDerivatedParameterNumber], false);
                }
            }
            dxdt[derivatedParameterNumber] = calculateExternalFactorsSum(derivatedParameterNumber, t, true) * positiveSideOfParameterMultiplication
                    - calculateExternalFactorsSum(derivatedParameterNumber, t, false) * negativeSideOfParameterMultiplication;
            parameterToDependenciesCache.add(new ParameterToDependencies(derivatedParameterNumber, this.parameterPolynomialList, this.externalFactorPolynomialList));
        }
        //TODO useless multiple drawings during one traverse, it can be drowned only once
        if (IS_ENABLED_POLY_DRAWING) {
            showPolys();
        }
        DifferentialSystemWriter differentialSystemWriter = new DifferentialSystemWriter(parameterToDependenciesCache);
        differentialSystemWriter.writeSystemToLatex();
        differentialSystemWriter.writeSystemToPdf();
        parameterToDependenciesCache.clear();
        rowColumnToRegressionModelCache.clear();
        return dxdt;
    }

    //TODO useless work, need tp build polys and log in separate provider, then inject polys and evaluate to save performance
    private double createFunctionDependencyAndCalculatePartOfSideForDerivatedParameter(int derivativeParameterNumber, int affectingParameterNumber, double affectingParameterFunctionValueInGivenTime, boolean isPositiveSide) {
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        double[] sampleY = new double[statisticMatrix.get(0).size()];
        double[][] sampleX = new double[statisticMatrix.get(0).size()][REGRESSION_DEGREE];
        for (int k = 0; k < statisticMatrix.get(0).size(); k++) {
            sampleY[k] = statisticMatrix.get(derivativeParameterNumber).get(k);
            sampleX[k][0] = statisticMatrix.get(affectingParameterNumber).get(k);
            regression.newSampleData(sampleY, sampleX);
        }
        double[] coefficientsOfDependencyPolynomial = regression.estimateRegressionParameters();
        coefficientsOfDependencyPolynomial = PolyUtils.trunkPolyCoefficientsDigits(coefficientsOfDependencyPolynomial);
        Map<String, Double> regressionStatParams = collectRegressionStatParams(regression);
        this.parameterPolynomialList.add(new ParameterPolynomial(affectingParameterNumber, coefficientsOfDependencyPolynomial, isPositiveSide));
        this.rowColumnToRegressionModelCache.add(new RowColumnToRegressionModel(new Pair<>(derivativeParameterNumber, affectingParameterNumber), coefficientsOfDependencyPolynomial, regressionStatParams));
        return calculateDerivativeParameterValueUsingRegressionFunction(coefficientsOfDependencyPolynomial, affectingParameterFunctionValueInGivenTime);
    }

    private double buildAndSaveExternalFactor(int j, double t, boolean isPositiveSide) {
        SimpleRegression simpleRegression = new SimpleRegression();
        simpleRegression.addData(0, statisticMatrix.get(j).get(0));
        simpleRegression.addData(1, statisticMatrix.get(j).get(statisticMatrix.get(j).size() - 1));
        this.externalFactorPolynomialList.add(new ExternalFactorPolynomial(j, simpleRegression.getSlope(), simpleRegression.getIntercept(), isPositiveSide));
        return simpleRegression.predict(t);
    }

    private Map<String, Double> collectRegressionStatParams(OLSMultipleLinearRegression regression) {
        Map<String, Double> statisticParams = new HashMap<>();
        statisticParams.put("Дисперсия регрессии", BigDecimal.valueOf(regression.estimateRegressandVariance())
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue());
        double fisher = (regression.calculateRSquared() / (1 - regression.calculateRSquared() + 0.001)) * ((statisticMatrix.get(0).size() - REGRESSION_DEGREE - 1) / REGRESSION_DEGREE);
        statisticParams.put("Критерий Фишера", BigDecimal.valueOf(fisher)
                        .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue());
        statisticParams.put("Дисперсия ошибки", BigDecimal.valueOf(regression.estimateErrorVariance())
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue());
        statisticParams.put("Среднеквадратическое отклонение ошибки", BigDecimal.valueOf(regression.estimateRegressionStandardError())
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue());
        return statisticParams;
    }

    private double calculateDerivativeParameterValueUsingRegressionFunction(double[] regressionPolynomialCoefficients, double affectingParameterValue) {
        double resultSum = 0;
        for (int k = 0; k < regressionPolynomialCoefficients.length; k++) {
            resultSum += regressionPolynomialCoefficients[k] * Math.pow(affectingParameterValue, k);
        }
        return resultSum;
    }

    private List<Double> calculateDerivativeParameterValuesUsingRegressionFunction(double[] regressionPolynomialCoefficients, List<Double> affectingParameterValues) {
        Double[] resultArray = new Double[affectingParameterValues.size()];
        for (int i = 0; i < resultArray.length; i++) {
            resultArray[i] = calculateDerivativeParameterValueUsingRegressionFunction(regressionPolynomialCoefficients, affectingParameterValues.get(i));
        }
        return Arrays.asList(resultArray);
    }

    private double calculateExternalFactorsSum(int rowNumber, double t, boolean isPositiveSide) {
        double resultSum = 1;
        for (int j = this.dependencyMatrix.size(); j < this.dependencyMatrix.get(0).size(); j++) {
            if ((dependencyMatrix.get(rowNumber).get(j) == 1) && isPositiveSide) {
                resultSum += buildAndSaveExternalFactor(j, t, true);
            } else if ((dependencyMatrix.get(rowNumber).get(j) == -1) && !isPositiveSide) {
                resultSum += buildAndSaveExternalFactor(j, t, false);
            }
        }
        return resultSum;
    }

    private void showPolys() {
        if (rowColumnToRegressionModelCache.isEmpty()) {
            throw new IllegalStateException("Polys list is empty, check your matrix");
        }
        XSSFWorkbook resultBook = new XSSFWorkbook();
        XSSFSheet resultPolySheet = resultBook.createSheet("resultPolySheet");
        XSSFDrawing drawingPatriarch = resultPolySheet.createDrawingPatriarch();
        for (int k = 0; k < rowColumnToRegressionModelCache.size(); k++) {
            showPoly(rowColumnToRegressionModelCache.get(k), drawingPatriarch,
                    k / dependencyMatrix.size(), k % dependencyMatrix.size());
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream("resultPolyBook.xlsx")) {
            resultBook.write(fileOutputStream);
            resultBook.close();
        } catch (IOException e) {
            log.log(Level.SEVERE, "resultPolyBook file is broken", e);
        }
    }

    private void showPoly(RowColumnToRegressionModel aggregator, XSSFDrawing drawingPatriarch,
                          int offsetRow, int offsetColumn) {
        XSSFClientAnchor anchor = drawingPatriarch.createAnchor(0, 0, 0, 0,
                (GRAPH_WIDTH + 3) * offsetColumn, (GRAPH_LENGTH + 3) * offsetRow,
                GRAPH_WIDTH + (GRAPH_WIDTH + 3) * offsetColumn, GRAPH_LENGTH + (GRAPH_LENGTH + 3) * offsetRow);
        XSSFChart chart = drawingPatriarch.createChart(anchor);
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);
        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle(parametersNames.get(aggregator.getRowColumn().getSecond()));
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle(parametersNames.get(aggregator.getRowColumn().getFirst()));

        XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
        data.setVaryColors(false);
        enrichChartDataWithSeries(aggregator.getRowColumn().getFirst(),
                aggregator.getRowColumn().getSecond(), aggregator.getPolynomialCoefficients(),
                aggregator.getRegressionMetrics(), data);
        chart.plot(data);
    }

    private void enrichChartDataWithSeries(int derivativeParameterNumber, int affectingParameterNumber, double[] dependencyPolynomialCoefficients,
                                           Map<String, Double> regressionMetrics, XDDFLineChartData lineChartData) {
        List<Pair<Double, Double>> affectingParameterStatisticValuesToDerivativeParameterStaticValuesPairs = collectAffectingParameterStatisticValuesToDerivativeParameterValues(affectingParameterNumber, statisticMatrix.get(derivativeParameterNumber));
        List<Pair<Double, Double>> pairForCalculatedLineChart = collectAffectingParameterStatisticValuesToDerivativeParameterValues(affectingParameterNumber, calculateDerivativeParameterValuesUsingRegressionFunction(dependencyPolynomialCoefficients, statisticMatrix.get(affectingParameterNumber)));
        XDDFNumericalDataSource<Double> independentParameterStatisticValues = XDDFDataSourcesFactory.fromArray(affectingParameterStatisticValuesToDerivativeParameterStaticValuesPairs.stream()
                .map(Pair::getKey)
                .toArray(Double[]::new));
        XDDFNumericalDataSource<Double> dependentParameterStatisticValues = XDDFDataSourcesFactory.fromArray(affectingParameterStatisticValuesToDerivativeParameterStaticValuesPairs.stream()
                .map(Pair::getValue)
                .toArray(Double[]::new));
        XDDFNumericalDataSource<Double> dependentParameterCalculatedValues = XDDFDataSourcesFactory.fromArray(pairForCalculatedLineChart.stream()
                .map(Pair::getValue)
                .toArray(Double[]::new));
        XDDFLineChartData.Series statisticDependencySeries = (XDDFLineChartData.Series) lineChartData.addSeries(independentParameterStatisticValues, dependentParameterStatisticValues);
        XDDFLineChartData.Series modelDependencySeries = (XDDFLineChartData.Series) lineChartData.addSeries(independentParameterStatisticValues, dependentParameterCalculatedValues);
        statisticDependencySeries.setTitle("Данные статистики");
        statisticDependencySeries.setSmooth(true);
        statisticDependencySeries.setMarkerStyle(MarkerStyle.STAR);
        modelDependencySeries.setTitle("Аппроксимирующий многочлен: " + buildPolynomialTitle(derivativeParameterNumber, affectingParameterNumber, dependencyPolynomialCoefficients) + "\n" + buildMetricsTitle(regressionMetrics));
    }

    private List<Pair<Double, Double>> collectAffectingParameterStatisticValuesToDerivativeParameterValues(int affectingParameterNumber, List<Double> derivativeParameterValuesToMakePairs) {
        List<Pair<Double, Double>> affectingParameterStatisticValuesToDerivativeParameterValues = new ArrayList<>();
        Set<Double> seenStatisticValues = new HashSet<>();
        for (int position = 0; position < statisticMatrix.get(affectingParameterNumber).size(); position++) {
            if (!seenStatisticValues.contains(statisticMatrix.get(affectingParameterNumber).get(position))) {
                seenStatisticValues.add(statisticMatrix.get(affectingParameterNumber).get(position));
                affectingParameterStatisticValuesToDerivativeParameterValues.add(new Pair<>(statisticMatrix.get(affectingParameterNumber).get(position), derivativeParameterValuesToMakePairs.get(position)));
            }
        }
        affectingParameterStatisticValuesToDerivativeParameterValues.sort(Comparator.comparingDouble(Pair::getKey));
        return affectingParameterStatisticValuesToDerivativeParameterValues;
    }

    private String buildPolynomialTitle(int rowNumber, int columnNumber, double[] coeffs) {
        PolynomialFunction polynomialFunction = new PolynomialFunction(coeffs);
        String polyPart = polynomialFunction.toString().replace("x", "X" + PolyUtils.generateSubscript(columnNumber + 1));
        while (polyPart.contains("^")) {
            int caretIndex = polyPart.indexOf("^");
            String degreePartSubstring = polyPart.substring(caretIndex + 1, caretIndex + 2);
            String generatedSuperscript = PolyUtils.generateSuperscript(Integer.parseInt(degreePartSubstring));
            polyPart = polyPart.replace("^" + degreePartSubstring, generatedSuperscript);
        }
        return "X" + PolyUtils.generateSubscript(rowNumber + 1) + " = " + polyPart;
    }

    private String buildMetricsTitle(Map<String, Double> regressionMetrics) {
        StringBuilder metrics = new StringBuilder();
        for (Map.Entry<String, Double> pairs: regressionMetrics.entrySet()) {
            metrics.append(pairs.getKey()).append(" = ").append(pairs.getValue()).append("\n");
        }
        return metrics.toString();
    }
}
