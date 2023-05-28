package model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ParameterToDependencies {

    private int parameterIndex;
    private List<ParameterPolynomial> polynomialDependencies;
    private List<ExternalFactorPolynomial> externalFactorDependencies;
}
