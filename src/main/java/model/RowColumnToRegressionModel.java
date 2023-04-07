package model;

import org.apache.commons.math3.util.Pair;

import java.util.Map;

public class RowColumnToRegressionModel {
    private Pair<Integer, Integer> rowColumn;
    private double[] polyCoeffs;
    private Map<String, Double> regressionMetrics;

    public RowColumnToRegressionModel(Pair<Integer, Integer> rowColumn,
                                      double[] polyCoeffs, Map<String, Double> regressionMetrics) {
        this.rowColumn = rowColumn;
        this.polyCoeffs = polyCoeffs;
        this.regressionMetrics = regressionMetrics;
    }

    public Pair<Integer, Integer> getRowColumn() {
        return rowColumn;
    }

    public void setRowColumn(Pair<Integer, Integer> rowColumn) {
        this.rowColumn = rowColumn;
    }

    public double[] getPolyCoeffs() {
        return polyCoeffs;
    }

    public void setPolyCoeffs(double[] polyCoeffs) {
        this.polyCoeffs = polyCoeffs;
    }

    public Map<String, Double> getRegressionMetrics() {
        return regressionMetrics;
    }

    public void setRegressionMetrics(Map<String, Double> regressionMetrics) {
        this.regressionMetrics = regressionMetrics;
    }
}
