package model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AffectingParameterPolynomial {
    private int parameterNumber;
    private double[] polynomialCoefficients;
    private boolean isPositiveDependency;
}
