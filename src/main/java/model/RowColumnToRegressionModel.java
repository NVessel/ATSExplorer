package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.math3.util.Pair;

import java.util.Map;

@Data
@AllArgsConstructor
public class RowColumnToRegressionModel {
    private Pair<Integer, Integer> rowColumn;
    private double[] polynomialCoefficients;
    private Map<String, Double> regressionMetrics;
}
