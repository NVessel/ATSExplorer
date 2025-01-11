package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.math3.util.Pair;

import java.util.Map;

@Data
@AllArgsConstructor
public class PolynomialDependency {
    private Pair<Integer, Integer> derivativeParameterNumberToAffectingParameterNumber;
    private boolean isAffectingParameterPositiveDependency;
    private double[] polynomialCoefficients;
    private Map<String, Double> regressionMetrics;
}
