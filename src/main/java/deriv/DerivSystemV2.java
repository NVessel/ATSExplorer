package deriv;

import flanagan.integration.DerivnFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class DerivSystemV2 implements DerivnFunction {

    private static final int GRAPH_WIDTH = 12;
    private static final int GRAPH_LENGTH = 29;

    private static final int PRECISION_DEGREE = 3;

    private static final boolean IS_ENABLED_POLY_DRAWING = true;

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
                    posPolynomMultiplication *= calculatePoly(i, j, x[j]);
                }
                else if (posNegMatrix.get(i).get(j) == -1) {
                    negPolynomMultiplication *= calculatePoly(i, j, x[j]);
                }
            }
            dxdt[i] = calculateExternalFactorsSum(t, true) * posPolynomMultiplication - calculateExternalFactorsSum(t, false) * negPolynomMultiplication;
        }
        return dxdt;
    }

    private double calculatePoly(int rowNumber, int columnNumber, double usedValue) {
        WeightedObservedPoints weightedObservedPoints = new WeightedObservedPoints();
        for (int k = 0; k < statMatrix.get(0).size(); k++) {
            weightedObservedPoints.add(statMatrix.get(columnNumber).get(k), statMatrix.get(rowNumber).get(k));
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(PRECISION_DEGREE);
        double[] coeffs = fitter.fit(weightedObservedPoints.toList());
        if (IS_ENABLED_POLY_DRAWING) {
            showPoly(rowNumber, columnNumber, coeffs);
        }
        return buildPoly(coeffs, usedValue);
    }

    private double buildPoly(double[] coeffs, double usedValue) {
        double resultSum = 0;
        for (int k = 0; k < coeffs.length; k++) {
            resultSum += coeffs[k] * Math.pow(usedValue, k);
        }
        return resultSum;
    }

    private Double[] buildPoly(double[] coeffs, Double[] usedValues) {
        Double[] resultArray = new Double[usedValues.length];
        for (int i = 0; i < resultArray.length; i++) {
            resultArray[i] = buildPoly(coeffs, usedValues[i]);
        }
        return resultArray;
    }

    //Turned off for now
    private double calculateExternalFactorsSum(double t, boolean isPositiveSide) {
        return 1;
    }

    //depends on quantities of polys, can be big
    //probably bad performance
    //double[] need to support
    private void showPoly(int rowNumber, int columnNumber, double[] coeffs) {
        XSSFWorkbook poliBook = new XSSFWorkbook();
        XSSFSheet poliSheet = poliBook.createSheet("poliResultSheet");
        XSSFDrawing drawingPatriarch = poliSheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawingPatriarch.createAnchor(0, 0, 0, 0,
                (GRAPH_WIDTH + 3) * columnNumber, (GRAPH_LENGTH + 3) * rowNumber,
                GRAPH_WIDTH + (GRAPH_WIDTH + 3) * columnNumber, GRAPH_LENGTH + (GRAPH_LENGTH + 3) * rowNumber);
        XSSFChart chart = drawingPatriarch.createChart(anchor);
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);
        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("Значение независимого параметра номер " + columnNumber);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle("Значение зависимого параметра номер " + rowNumber);
        XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
        data.setVaryColors(false);
        XDDFNumericalDataSource<Double> independentStatisticParameterValues = XDDFDataSourcesFactory.fromArray(statMatrix.get(columnNumber).toArray(new Double[0]));
        XDDFNumericalDataSource<Double> dependentStatisticParameterValues = XDDFDataSourcesFactory.fromArray(statMatrix.get(rowNumber).toArray(new Double[0]));
        XDDFNumericalDataSource<Double> dependentCalculatedParameterValues = XDDFDataSourcesFactory.fromArray(buildPoly(coeffs, statMatrix.get(columnNumber).toArray(new Double[0])));
        XDDFLineChartData.Series series = (XDDFLineChartData.Series) data.addSeries(independentStatisticParameterValues, dependentStatisticParameterValues);
        XDDFLineChartData.Series series2 = (XDDFLineChartData.Series) data.addSeries(independentStatisticParameterValues, dependentCalculatedParameterValues);
        series.setTitle("Зависимость из статистики");
        series.setSmooth(true);
        series.setMarkerStyle(MarkerStyle.STAR);
        series2.setTitle("Построенный многочлен");
        chart.plot(data);
        try (FileOutputStream fileOutputStream = new FileOutputStream("resultPoliBook.xlsx")) {
            poliBook.write(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
