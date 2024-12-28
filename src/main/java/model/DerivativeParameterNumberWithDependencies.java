package model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DerivativeParameterNumberWithDependencies {
    private int derivativeParameterNumber;
    private List<AffectingParameterPolynomial> polynomialDependencies;
    private List<ExternalFactorPolynomial> externalFactorDependencies;
}
