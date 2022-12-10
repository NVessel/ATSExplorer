package model;

import org.apache.commons.math3.util.Pair;

public class RowColumnToPolyCoeffs {
    private Pair<Integer, Integer> rowColumn;
    private double[] polyCoeffs;

    public RowColumnToPolyCoeffs(Pair<Integer, Integer> rowColumn, double[] polyCoeffs) {
        this.rowColumn = rowColumn;
        this.polyCoeffs = polyCoeffs;
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
}
