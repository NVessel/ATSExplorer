package service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import model.PolynomialDependency;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
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

    private static final int ITERATIONS_COUNT = 12;
    private static final String[] ITERATIONS_MOMENTS = new String[]{"0", "0.1", "0.2", "0.3", "0.4",
            "0.5", "0.6", "0.7", "0.8", "0.9", "1", "1.1", "1.2"};
    private static final int GRAPHS_FIRST_ROW = 15;
    private static final int GRAPH_LENGTH = 29;
    private static final int GRAPH_WIDTH = 10;

    private final List<List<Integer>> dependencyMatrix;
    private final List<List<Double>> statisticMatrix;
    private final List<String> parametersNames;
    private final List<PolynomialDependency> polynomialDependencies;

    public void drawPolynomials() {
        XSSFWorkbook resultBook = new XSSFWorkbook();
        XSSFSheet resultPolySheet = resultBook.createSheet("resultPolySheet");
        XSSFDrawing drawingPatriarch = resultPolySheet.createDrawingPatriarch();
        for (int dependencyNumber = 0; dependencyNumber < polynomialDependencies.size(); dependencyNumber++) {
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

    @SneakyThrows
    public void drawResults(double[] initialYValues, double[][] ynParts, List<List<Double>> statisticsMatrix, List<String> parametersNames) {
        try (XSSFWorkbook resultBook = new XSSFWorkbook()) {
            XSSFSheet resultSheet = resultBook.createSheet("resultSheet");
            Row firstRow = resultSheet.createRow(0);
            int shiftForColumnNamings = 1;
            int shiftForRowNamings = 1;
            for (int k = 0; k <= ITERATIONS_COUNT; k++) {
                Cell cell = firstRow.createCell(k + shiftForColumnNamings);
                CellStyle cellStyle = resultBook.createCellStyle();
                Font headerFont = resultBook.createFont();
                headerFont.setBold(true);
                cellStyle.setFont(headerFont);
                cell.setCellStyle(cellStyle);
                double timeStep = (double) k / ITERATIONS_COUNT;
                cell.setCellValue("t = " + timeStep);
            }

            for (int i = 0; i < ynParts.length; i++) {
                //i+1 j+1 because of place for naming
                XSSFRow newRow = resultSheet.createRow(i + 1);
                for (int j = 0; j < ITERATIONS_COUNT; j++) {
                    XSSFCell cell = newRow.createCell(j + 1 + shiftForColumnNamings);
                    cell.setCellValue(ynParts[i][j]);
                }
                XSSFCell cell = newRow.createCell(shiftForColumnNamings); //because of beginning
                cell.setCellValue(initialYValues[i]);
                cell = newRow.createCell(0); //because it's edge
                cell.setCellValue(parametersNames.get(i));
            }

            for (int i = 0; i < ynParts.length; i++) {
                XSSFDrawing drawing = resultSheet.createDrawingPatriarch();
                XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0,
                        GRAPHS_FIRST_ROW + (GRAPH_LENGTH + 3) * i, 12, GRAPHS_FIRST_ROW + GRAPH_LENGTH + (GRAPH_LENGTH + 3) * i);
                XSSFChart chart = drawing.createChart(anchor);
                XDDFChartLegend legend = chart.getOrAddLegend();
                legend.setPosition(LegendPosition.TOP_RIGHT);
                XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
                bottomAxis.setTitle("Время");
                XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
                leftAxis.setTitle("Значение");
                XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
                data.setVaryColors(false);
                XDDFCategoryDataSource timeArgument = XDDFDataSourcesFactory.fromArray(ITERATIONS_MOMENTS);
                XDDFNumericalDataSource<Double> resultParameterValues = XDDFDataSourcesFactory.fromNumericCellRange(resultSheet,
                        new CellRangeAddress(i + shiftForRowNamings, i + shiftForRowNamings,
                                shiftForColumnNamings, shiftForColumnNamings + ITERATIONS_COUNT));
                XDDFNumericalDataSource<Double> realParameterValues = XDDFDataSourcesFactory.fromArray(statisticsMatrix.get(i).toArray(new Double[0]));
                XDDFLineChartData.Series series1 = (XDDFLineChartData.Series) data.addSeries(timeArgument, resultParameterValues);
                XDDFLineChartData.Series series2 = (XDDFLineChartData.Series) data.addSeries(timeArgument, realParameterValues);
                series1.setTitle("Данные модели для " + parametersNames.get(i));
                series1.setSmooth(true);
                series1.setMarkerStyle(MarkerStyle.STAR);
                series2.setTitle("Значение статистики для " + parametersNames.get(i));
                chart.plot(data);
            }
            try (FileOutputStream fileOutputStream = new FileOutputStream("resultBook.xlsx")) {
                resultBook.write(fileOutputStream);
            }
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
