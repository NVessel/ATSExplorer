package deriv;

import flanagan.integration.DerivnFunction;
import model.RowColumnToPolyCoeffs;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class DerivSystemV2 implements DerivnFunction {

    private static final int GRAPH_WIDTH = 10;
    private static final int GRAPH_LENGTH = 20;

    private static final int PRECISION_DEGREE = 2;

    private static final boolean IS_ENABLED_POLY_DRAWING = true;

    private final List<List<Integer>> posNegMatrix;
    private final List<List<Double>> statMatrix;
    private final List<RowColumnToPolyCoeffs> rowColumnToPolyCoeffsCache = new LinkedList<>();

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
                    posPolynomMultiplication *= buildAndSavePoly(i, j, x[j]);
                }
                else if (posNegMatrix.get(i).get(j) == -1) {
                    negPolynomMultiplication *= buildAndSavePoly(i, j, x[j]);
                }
            }
            dxdt[i] = calculateExternalFactorsSum(t, true) * posPolynomMultiplication - calculateExternalFactorsSum(t, false) * negPolynomMultiplication;
        }
        if (IS_ENABLED_POLY_DRAWING) {
            showPolys();
        }
        rowColumnToPolyCoeffsCache.clear();
        return dxdt;
    }

    //useless work, need tp build polys and log in separate provider, then inject polys and evaluate to save perfomance
    private double buildAndSavePoly(int rowNumber, int columnNumber, double usedValue) {
        WeightedObservedPoints weightedObservedPoints = new WeightedObservedPoints();
        for (int k = 0; k < statMatrix.get(0).size(); k++) {
            weightedObservedPoints.add(statMatrix.get(columnNumber).get(k), statMatrix.get(rowNumber).get(k));
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(PRECISION_DEGREE);
        double[] coeffs = fitter.fit(weightedObservedPoints.toList());
        this.rowColumnToPolyCoeffsCache.add(new RowColumnToPolyCoeffs(new Pair<>(rowNumber, columnNumber), coeffs));
        return calcPoly(coeffs, usedValue);
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

    //Turned off for now
    private double calculateExternalFactorsSum(double t, boolean isPositiveSide) {
        return 1;
    }

    private void showPolys() {
        if (rowColumnToPolyCoeffsCache.isEmpty()) {
            throw new IllegalStateException("Polys list is empty, check your matrix");
        }
        XSSFWorkbook resultBook = new XSSFWorkbook();
        XSSFSheet resultPolySheet = resultBook.createSheet("resultPolySheet");
        XSSFDrawing drawingPatriarch = resultPolySheet.createDrawingPatriarch();
        for (int k = 0; k < rowColumnToPolyCoeffsCache.size(); k++) {
            showPoly(rowColumnToPolyCoeffsCache.get(k).getRowColumn().getFirst(), rowColumnToPolyCoeffsCache.get(k).getRowColumn().getSecond(),
                    rowColumnToPolyCoeffsCache.get(k).getPolyCoeffs(), drawingPatriarch, k / posNegMatrix.size(), k % posNegMatrix.size());
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream("resultPolyBook.xlsx")) {
            resultBook.write(fileOutputStream);
            resultBook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //double[] need to support, not Double[]
    private void showPoly(int rowNumber, int columnNumber, double[] coeffs, XSSFDrawing drawingPatriarch, int offsetRow, int offsetColumn) {
        XSSFClientAnchor anchor = drawingPatriarch.createAnchor(0, 0, 0, 0,
                (GRAPH_WIDTH + 3) * offsetColumn, (GRAPH_LENGTH + 3) * offsetRow,
                GRAPH_WIDTH + (GRAPH_WIDTH + 3) * offsetColumn, GRAPH_LENGTH + (GRAPH_LENGTH + 3) * offsetRow);
        XSSFChart chart = drawingPatriarch.createChart(anchor);
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);
        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        //because of 0-index
        bottomAxis.setTitle("Значение независимого параметра X " + (columnNumber + 1));
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("Значение зависимого параметра X " + (rowNumber + 1));
        XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
        data.setVaryColors(false);
        XDDFNumericalDataSource<Double> independentStatisticParameterValues = XDDFDataSourcesFactory.fromArray(statMatrix.get(columnNumber).toArray(new Double[0]));
        XDDFNumericalDataSource<Double> dependentStatisticParameterValues = XDDFDataSourcesFactory.fromArray(statMatrix.get(rowNumber).toArray(new Double[0]));
        XDDFNumericalDataSource<Double> dependentCalculatedParameterValues = XDDFDataSourcesFactory.fromArray(calcPoly(coeffs, statMatrix.get(columnNumber).toArray(new Double[0])));
        XDDFLineChartData.Series series = (XDDFLineChartData.Series) data.addSeries(independentStatisticParameterValues, dependentStatisticParameterValues);
        XDDFLineChartData.Series series2 = (XDDFLineChartData.Series) data.addSeries(independentStatisticParameterValues, dependentCalculatedParameterValues);
        series.setTitle("Зависимость из статистики");
        series.setSmooth(true);
        series.setMarkerStyle(MarkerStyle.STAR);
        series2.setTitle("Построенный многочлен");
        chart.plot(data);
    }
}
