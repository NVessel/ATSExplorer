package deriv;

import builder.LatexFileBuilder;
import flanagan.integration.DerivnFunction;
import model.ExternalFactorPolynomial;
import model.ParameterPolynomial;
import model.ParameterToDependencies;
import model.RowColumnToRegressionModel;
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
import java.util.logging.Logger;

public class DerivSystemV2 implements DerivnFunction {

    private final Logger logger = Logger.getLogger(DerivSystemV2.class.getName());

    private static final int REGRESSION_DEGREE = 1;

    private static final int GRAPH_WIDTH = 10;
    private static final int GRAPH_LENGTH = 20;
    private static final boolean IS_ENABLED_POLY_DRAWING = true;

    private List<ParameterPolynomial> parameterPolynomialList;
    private List<ExternalFactorPolynomial> externalFactorPolynomialList;
    private final List<List<Integer>> posNegMatrix;
    private final List<List<Double>> statMatrix;
    private final List<String> parametersNames;
    private final List<RowColumnToRegressionModel> rowColumnToRegressionModelCache = new LinkedList<>();
    private final List<ParameterToDependencies> parameterToDependenciesCache = new LinkedList<>();

    public DerivSystemV2(List<List<Integer>> posNegMatrix, List<List<Double>> statMatrix, List<String> parametersNames) {
        this.posNegMatrix = posNegMatrix;
        this.statMatrix = statMatrix;
        this.parametersNames = parametersNames;
    }

    @Override
    public double[] derivn(double t, double[] x) {
        double[] dxdt = new double[this.posNegMatrix.size()];
        for (int i = 0; i < this.posNegMatrix.size(); i++) {
            double posPolynomMultiplication = 1;
            double negPolynomMultiplication = 1;
            this.parameterPolynomialList = new ArrayList<>();
            this.externalFactorPolynomialList = new ArrayList<>();
            for (int j = 0; j < this.posNegMatrix.size(); j++) {
                if (posNegMatrix.get(i).get(j) == 1) {
                    posPolynomMultiplication *= buildAndSavePoly(i, j, x[j], true);
                }
                else if (posNegMatrix.get(i).get(j) == -1) {
                    negPolynomMultiplication *= buildAndSavePoly(i, j, x[j], false);
                }
            }
            dxdt[i] = calculateExternalFactorsSum(i, t, true) * posPolynomMultiplication
                    - calculateExternalFactorsSum(i, t, false) * negPolynomMultiplication;
            parameterToDependenciesCache.add(new ParameterToDependencies(i, this.parameterPolynomialList, this.externalFactorPolynomialList));
        }
        //useless multiple drawings during one traverse, it can be drowned only once
        if (IS_ENABLED_POLY_DRAWING) {
            showPolys();
        }
        LatexFileBuilder latexFileBuilder = new LatexFileBuilder(parameterToDependenciesCache);
        latexFileBuilder.writeSystemToLatex();
        parameterToDependenciesCache.clear();
        rowColumnToRegressionModelCache.clear();
        return dxdt;
    }

    //useless work, need tp build polys and log in separate provider, then inject polys and evaluate to save performance
    private double buildAndSavePoly(int rowNumber, int columnNumber, double usedValue, boolean isPositiveSide) {
        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        double[] sampleY = new double[statMatrix.get(0).size()];
        double[][] sampleX = new double[statMatrix.get(0).size()][REGRESSION_DEGREE];
        for (int k = 0; k < statMatrix.get(0).size(); k++) {
            sampleY[k] = statMatrix.get(rowNumber).get(k);
            sampleX[k][0] = statMatrix.get(columnNumber).get(k);
    //        sampleX[k][1] = Math.pow(statMatrix.get(columnNumber).get(k), 2);
         //   sampleX[k][2] = Math.pow(statMatrix.get(columnNumber).get(k), 3);
         //   sampleX[k][3] = Math.pow(statMatrix.get(columnNumber).get(k), 4);
            regression.newSampleData(sampleY, sampleX);
        }
        double[] coeffsOfPoly = regression.estimateRegressionParameters();
        coeffsOfPoly = PolyUtils.truncPolyCoeffsDigits(coeffsOfPoly);
        Map<String, Double> regressionStatParams = collectRegressionStatParams(regression);
        this.parameterPolynomialList.add(new ParameterPolynomial(columnNumber, coeffsOfPoly, isPositiveSide));
        this.rowColumnToRegressionModelCache.add(new RowColumnToRegressionModel(new Pair<>(rowNumber, columnNumber), coeffsOfPoly, regressionStatParams));
        return calcPoly(coeffsOfPoly, usedValue);
    }

    private double buildAndSaveExternalFactor(int j, double t, boolean isPositiveSide) {
        SimpleRegression simpleRegression = new SimpleRegression();
        simpleRegression.addData(0, statMatrix.get(j).get(0));
        simpleRegression.addData(1, statMatrix.get(j).get(statMatrix.get(j).size() - 1));
        this.externalFactorPolynomialList.add(new ExternalFactorPolynomial(j, simpleRegression.getSlope(), simpleRegression.getIntercept(), isPositiveSide));
        return simpleRegression.predict(t);
    }

    private Map<String, Double> collectRegressionStatParams(OLSMultipleLinearRegression regression) {
        Map<String, Double> statParams = new HashMap<>();
        statParams.put("Дисперсия регрессии", BigDecimal.valueOf(regression.estimateRegressandVariance())
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue());
        double fisher = (regression.calculateRSquared() / (1 - regression.calculateRSquared() + 0.001)) * ((statMatrix.get(0).size() - REGRESSION_DEGREE - 1) / REGRESSION_DEGREE);
        statParams.put("Критерий Фишера", BigDecimal.valueOf(fisher)
                        .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue());
        statParams.put("Дисперсия ошибки", BigDecimal.valueOf(regression.estimateErrorVariance())
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue());
        statParams.put("Среднеквадратическое отклонение ошибки", BigDecimal.valueOf(regression.estimateRegressionStandardError())
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue());
        return statParams;
    }

    private double calcPoly(double[] coeffs, double usedValue) {
        double resultSum = 0;
        for (int k = 0; k < coeffs.length; k++) {
            resultSum += coeffs[k] * Math.pow(usedValue, k);
        }
        return resultSum;
    }

    private Double[] calcPoly(double[] coeffs, Double[] usedValues) {
        Double[] resultArray = new Double[usedValues.length];
        for (int i = 0; i < resultArray.length; i++) {
            resultArray[i] = calcPoly(coeffs, usedValues[i]);
        }
        return resultArray;
    }

    private double calculateExternalFactorsSum(int rowNumber, double t, boolean isPositiveSide) {
        double resultSum = 1;
        for (int j = this.posNegMatrix.size(); j < this.posNegMatrix.get(0).size(); j++) {
            if ((posNegMatrix.get(rowNumber).get(j) == 1) && isPositiveSide) {
                resultSum += buildAndSaveExternalFactor(j, t, true);
            } else if ((posNegMatrix.get(rowNumber).get(j) == -1) && !isPositiveSide) {
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
                    k / posNegMatrix.size(), k % posNegMatrix.size());
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream("resultPolyBook.xlsx")) {
            resultBook.write(fileOutputStream);
            resultBook.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "resultPolyBook file is broken");
            e.printStackTrace();
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
                aggregator.getRowColumn().getSecond(), aggregator.getPolyCoeffs(),
                aggregator.getRegressionMetrics(), data);
        chart.plot(data);
    }

    private void enrichChartDataWithSeries(int rowNumber, int columnNumber, double[] coeffs,
                                           Map<String, Double> regressionMetrics, XDDFLineChartData data) {
        XDDFNumericalDataSource<Double> independentStatisticParameterValues = XDDFDataSourcesFactory.fromArray(statMatrix.get(columnNumber).toArray(new Double[0]));
        XDDFNumericalDataSource<Double> dependentStatisticParameterValues = XDDFDataSourcesFactory.fromArray(statMatrix.get(rowNumber).toArray(new Double[0]));
        XDDFNumericalDataSource<Double> dependentCalculatedParameterValues = XDDFDataSourcesFactory.fromArray(calcPoly(coeffs, statMatrix.get(columnNumber).toArray(new Double[0])));
        XDDFLineChartData.Series series = (XDDFLineChartData.Series) data.addSeries(independentStatisticParameterValues, dependentStatisticParameterValues);
        XDDFLineChartData.Series series2 = (XDDFLineChartData.Series) data.addSeries(independentStatisticParameterValues, dependentCalculatedParameterValues);
        series.setTitle("Зависимость из статистики");
        series.setSmooth(true);
        series.setMarkerStyle(MarkerStyle.STAR);
        series2.setTitle("Построенный многочлен: " + buildPolyTitle(rowNumber, columnNumber, coeffs) + "\n" + buildMetricsTitle(regressionMetrics));
    }

    //degrees of regression model can't be big
    private String buildPolyTitle(int rowNumber, int columnNumber, double[] coeffs) {
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
