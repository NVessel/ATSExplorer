package model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ExternalFactorPolynomial {
    private int factorNumber;
    private double slope;
    private double intersection;
    private boolean isPositiveDependency;
}
