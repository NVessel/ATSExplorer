package model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ExternalFactorPolynomial {
    private int factorNumber;
    private double[] polynomialCoefficients;
    private boolean isPositiveDependency;
}
