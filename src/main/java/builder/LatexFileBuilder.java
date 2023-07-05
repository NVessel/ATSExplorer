package builder;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import model.ParameterToDependencies;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import utils.PolyUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

@AllArgsConstructor
public class LatexFileBuilder {

    private List<ParameterToDependencies> parameterEquationInformation;

    @SneakyThrows
    public void writeSystemToLatex() {
        File latexFile = new File("equationSystem.tex");
        try (FileWriter fileWriter = new FileWriter(latexFile)) {
            fileWriter.write("\\documentclass[12pt, letterpaper]{article}\n");
            fileWriter.write("\\usepackage{amsmath}\n");
            fileWriter.write("\\usepackage[utf8]{inputenc}\n");
            fileWriter.write("\n");
            fileWriter.write("\\begin{document}\n");
            fileWriter.write("\\begin{equation*}\n");
            fileWriter.write("\\begin{cases}\n");
            for (ParameterToDependencies parameterToDependencies : parameterEquationInformation) {
                fileWriter.write(buildParameterRow(parameterToDependencies));
                fileWriter.write("\\\\");
            }
            fileWriter.write("\\end{cases}\n");
            fileWriter.write("\\end{equation*}\n");
            fileWriter.write("\\end{document}");
        }
    }

    private String buildParameterRow(ParameterToDependencies parameterToDependencies) {
        StringBuilder res = new StringBuilder();
        int elementCounter = 0;
        res.append("\\frac{X_{").append(parameterToDependencies.getParameterIndex() + 1).append("}(t)}{dt} = (");
        for (int i = 0; i < parameterToDependencies.getExternalFactorDependencies().size(); i++) {
            if (parameterToDependencies.getExternalFactorDependencies().get(i).isPositive()) {
                res.append(buildExternalFactorString(PolyUtils.truncPolyCoeffDigits(parameterToDependencies.getExternalFactorDependencies().get(i).getSlope()),
                        PolyUtils.truncPolyCoeffDigits(parameterToDependencies.getExternalFactorDependencies().get(i).getIntersection()), elementCounter));
                elementCounter++;
            }
        }
        //case when only one external factor or none - useless parenthesis is not needed
        if (elementCounter < 2) {
            res = new StringBuilder(res.substring(0, res.lastIndexOf("(")) + res.substring(res.lastIndexOf("(") + 1));
        } else {
            res.append(")");
        }
        for (int i = 0; i < parameterToDependencies.getPolynomialDependencies().size(); i++) {
            if (parameterToDependencies.getPolynomialDependencies().get(i).isPositive()) {
                res.append("(").append(buildPolyString(parameterToDependencies.getPolynomialDependencies().get(i).getParameterIndex(),
                        parameterToDependencies.getPolynomialDependencies().get(i).getPolyCoeffs())).append(")");
            }
        }
        res.append("\\\\");
        res.append(" - (");
        elementCounter = 0;
        for (int i = 0; i < parameterToDependencies.getExternalFactorDependencies().size(); i++) {
            if (!parameterToDependencies.getExternalFactorDependencies().get(i).isPositive()) {
                res.append(buildExternalFactorString(PolyUtils.truncPolyCoeffDigits(parameterToDependencies.getExternalFactorDependencies().get(i).getSlope()),
                        PolyUtils.truncPolyCoeffDigits(parameterToDependencies.getExternalFactorDependencies().get(i).getIntersection()), elementCounter));
                elementCounter++;
            }
        }
        //case when only one external factor or none - useless parenthesis is not needed
        if (elementCounter < 2) {
            res = new StringBuilder(res.substring(0, res.lastIndexOf("(")) + res.substring(res.lastIndexOf("(") + 1));
        } else {
            res.append(")");
        }
        for (int i = 0; i < parameterToDependencies.getPolynomialDependencies().size(); i++) {
            if (!parameterToDependencies.getPolynomialDependencies().get(i).isPositive()) {
                res.append("(").append(buildPolyString(parameterToDependencies.getPolynomialDependencies().get(i).getParameterIndex(),
                        parameterToDependencies.getPolynomialDependencies().get(i).getPolyCoeffs())).append(")");
            }
        }
        return res.toString();
    }

    private String buildExternalFactorString(double slope, double intercept, int elementCounter) {
        PolynomialFunction polynomialFunction = new PolynomialFunction(new double[] {intercept, slope});
        if (elementCounter > 0) {
            return " + (" + polynomialFunction.toString().replace("x", "t") + ")";
        }
        return "(" + polynomialFunction.toString().replace("x", "t") + ")";
    }

    private String buildPolyString(int parameterNumber, double[] coeffs) {
        PolynomialFunction polynomialFunction = new PolynomialFunction(coeffs);
        return polynomialFunction.toString().replace("x", "X_" + (parameterNumber + 1));
    }
}
