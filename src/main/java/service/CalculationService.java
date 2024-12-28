package service;

import derivative.DerivativeSystem;
import flanagan.integration.RungeKutta;
import lombok.Setter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import suppliers.ParametersDependenciesMatrixSupplier;
import suppliers.StatisticsSupplier;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Setter
public class CalculationService {

    private static final int ITERATIONS_COUNT = 12;
    private static final String[] ITERATIONS_MOMENTS = new String[]{"0", "0.1", "0.2", "0.3", "0.4",
            "0.5", "0.6", "0.7", "0.8", "0.9", "1", "1.1", "1.2"};
    private static final double MAX_ERROR_PERCENT_LIMIT = 0.1;
    private static final int GRAPHS_FIRST_ROW = 15;
    private static final int GRAPH_LENGTH = 29;

    private ParametersDependenciesMatrixSupplier parametersDependenciesMatrixSupplier;
    private StatisticsSupplier statisticsSupplier;

    public void calculateOnStatistics() throws IOException, InvalidFormatException {
        List<List<Integer>> dependenciesMatrix = parametersDependenciesMatrixSupplier.getExternalMatrix();
        List<List<Double>> statisticsMatrix = statisticsSupplier.getExternalStatistics();
        List<String> parametersNames = parametersDependenciesMatrixSupplier.getParametersNames();
        double[] y0 = extractInitialValues(statisticsMatrix, dependenciesMatrix.size());
        double[] y0copy = y0;
        double h = 0.01;
        double x0 = 0.0D;
        double xn = 0.1D;
        double[] yn;
        double[][] ynParts = new double[dependenciesMatrix.size()][ITERATIONS_COUNT];
        DerivativeSystem systemDerivative = new DerivativeSystem(dependenciesMatrix, statisticsMatrix, parametersNames);
        for (int j = 0; j < ITERATIONS_COUNT; j++) {
            yn = RungeKutta.fourthOrder(systemDerivative, x0, y0, xn, h);
         //   yn = correctEvaluations(yn, j, statisticsMatrix);
            for (int i = 0; i < dependenciesMatrix.size(); i++) {
                ynParts[i][j] = yn[i];
            }
            y0 = yn;
            x0 += 0.1;
            xn += 0.1;
        }
        showResults(y0copy, ynParts, statisticsMatrix, parametersNames);
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

    private void showResults(double[] y0copy, double[][] ynParts, List<List<Double>> statisticsMatrix, List<String> parametersNames) throws IOException {
        XSSFWorkbook resultBook = new XSSFWorkbook();
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
            cell.setCellValue(y0copy[i]);
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

    private double[] extractInitialValues(List<List<Double>> statisticsMatrix, int limitToStopAtExternalFactorsPart) {
        double[] initials = new double[limitToStopAtExternalFactorsPart];
        for (int i = 0; i < limitToStopAtExternalFactorsPart; i++) {
            initials[i] = statisticsMatrix.get(i).get(0);
        }
        return initials;
    }
}
