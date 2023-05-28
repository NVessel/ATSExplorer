package model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParameterPolynomial {
    private int parameterIndex;
    private double[] polyCoeffs;
    private boolean isPositive;
}
