package service;

import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import model.PolynomialDependency;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import utils.PolynomialUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

@AllArgsConstructor
@Log
public class ExcelDrawingService {

    private static final int GRAPH_WIDTH = 10;
    private static final int GRAPH_LENGTH = 20;

    private final List<List<Integer>> dependencyMatrix;
    private final List<List<Double>> statisticMatrix;
    private final List<String> parametersNames;
    private final List<PolynomialDependency> polynomialDependencies;

    public void drawPolynomials() {
        XSSFWorkbook resultBook = new XSSFWorkbook();
        XSSFSheet resultPolySheet = resultBook.createSheet("resultPolySheet");
        XSSFDrawing drawingPatriarch = resultPolySheet.createDrawingPatriarch();
        for (int dependencyNumber = 0; dependencyNumber < polynomialDependencies.size(); dependencyNumber++) {
            //TODO it only draws parameter dependencies now, filtering external factors
            if (!polynomialDependencies.get(dependencyNumber).getRegressionMetrics().isEmpty()) {
                drawPolynomial(polynomialDependencies.get(dependencyNumber), drawingPatriarch,
                        dependencyNumber / dependencyMatrix.size(), dependencyNumber % dependencyMatrix.size());
            }
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream("resultPolyBook.xlsx")) {
            resultBook.write(fileOutputStream);
            resultBook.close();
        } catch (IOException e) {
            log.log(Level.SEVERE, "resultPolyBook file is broken", e);
        }
    }

    private void drawPolynomial(PolynomialDependency polynomialDependency, XSSFDrawing drawingPatriarch,
                                int offsetRow, int offsetColumn) {
        XSSFClientAnchor anchor = drawingPatriarch.createAnchor(0, 0, 0, 0,
                (GRAPH_WIDTH + 3) * offsetColumn, (GRAPH_LENGTH + 3) * offsetRow,
                GRAPH_WIDTH + (GRAPH_WIDTH + 3) * offsetColumn, GRAPH_LENGTH + (GRAPH_LENGTH + 3) * offsetRow);
        XSSFChart chart = drawingPatriarch.createChart(anchor);
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);
        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle(parametersNames.get(polynomialDependency.getDerivativeParameterNumberToAffectingParameterNumber().getSecond()));
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle(parametersNames.get(polynomialDependency.getDerivativeParameterNumberToAffectingParameterNumber().getFirst()));

        XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
        data.setVaryColors(false);
        enrichChartDataWithSeries(polynomialDependency.getDerivativeParameterNumberToAffectingParameterNumber().getFirst(),
                polynomialDependency.getDerivativeParameterNumberToAffectingParameterNumber().getSecond(), polynomialDependency.getPolynomialCoefficients(),
                polynomialDependency.getRegressionMetrics(), data);
        chart.plot(data);
    }

    private void enrichChartDataWithSeries(int derivativeParameterNumber, int affectingParameterNumber, double[] dependencyPolynomialCoefficients,
                                           Map<String, Double> regressionMetrics, XDDFLineChartData lineChartData) {
        List<Pair<Double, Double>> affectingParameterStatisticValuesToDerivativeParameterStaticValuesPairs = collectAffectingParameterStatisticValuesToDerivativeParameterValues(affectingParameterNumber, statisticMatrix.get(derivativeParameterNumber));
        List<Pair<Double, Double>> pairForCalculatedLineChart = collectAffectingParameterStatisticValuesToDerivativeParameterValues(affectingParameterNumber,
                calculateDerivativeParameterValuesUsingRegressionFunction(dependencyPolynomialCoefficients, statisticMatrix.get(affectingParameterNumber)));
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

    private List<Double> calculateDerivativeParameterValuesUsingRegressionFunction(double[] regressionPolynomialCoefficients, List<Double> affectingParameterValues) {
        Double[] resultArray = new Double[affectingParameterValues.size()];
        for (int i = 0; i < resultArray.length; i++) {
            resultArray[i] = evaluatePolynomialForValueUsingCoefficients(regressionPolynomialCoefficients, affectingParameterValues.get(i));
        }
        return Arrays.asList(resultArray);
    }

    private double evaluatePolynomialForValueUsingCoefficients(double[] regressionPolynomialCoefficients, double affectingParameterValue) {
        double resultSum = 0;
        for (int k = 0; k < regressionPolynomialCoefficients.length; k++) {
            resultSum += regressionPolynomialCoefficients[k] * Math.pow(affectingParameterValue, k);
        }
        return resultSum;
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

    private String buildPolynomialTitle(int derivativeParameterNumber, int affectingParameterNumber, double[] dependencyPolynomialCoefficients) {
        PolynomialFunction polynomialFunction = new PolynomialFunction(dependencyPolynomialCoefficients);
        String polyPart = polynomialFunction.toString().replace("x", "X" + PolynomialUtils.generateSubscriptTitle(affectingParameterNumber + 1));
        while (polyPart.contains("^")) {
            int caretIndex = polyPart.indexOf("^");
            String degreePartSubstring = polyPart.substring(caretIndex + 1, caretIndex + 2);
            String generatedSuperscript = PolynomialUtils.generateSuperscriptTitle(Integer.parseInt(degreePartSubstring));
            polyPart = polyPart.replace("^" + degreePartSubstring, generatedSuperscript);
        }
        return "X" + PolynomialUtils.generateSubscriptTitle(derivativeParameterNumber + 1) + " = " + polyPart;
    }

    private String buildMetricsTitle(Map<String, Double> regressionMetrics) {
        StringBuilder metrics = new StringBuilder();
        for (Map.Entry<String, Double> pairs: regressionMetrics.entrySet()) {
            metrics.append(pairs.getKey()).append(" = ").append(pairs.getValue()).append("\n");
        }
        return metrics.toString();
    }
}
