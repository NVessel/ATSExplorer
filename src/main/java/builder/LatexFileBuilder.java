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
        latexFile.createNewFile();
        try (FileWriter fileWriter = new FileWriter(latexFile)) {
            fileWriter.write("\\documentclass[12pt, letterpaper]{article}\n");
            fileWriter.write("\\usepackage{amsmath}\n");
            fileWriter.write("\\usepackage[utf8]{inputenc}\n");
            fileWriter.write("\n");
            fileWriter.write("\\begin{document}\n");
            fileWriter.write("\\begin{equation*}\n");
            fileWriter.write("\\begin{cases}\n");
            for (int i = 0; i < parameterEquationInformation.size(); i++) {
                fileWriter.write(buildParameterRow(parameterEquationInformation.get(i)));
                fileWriter.write("\\\\");
            }
            fileWriter.write("\\end{cases}\n");
            fileWriter.write("\\end{equation*}\n");
            fileWriter.write("\\end{document}");
        }
    }

    private String buildParameterRow(ParameterToDependencies parameterToDependencies) {
        String res = "";
        res += "\\frac{X_{" + (parameterToDependencies.getParameterIndex() + 1) + "}(t)}{dt} = (";
        for (int i = 0; i < parameterToDependencies.getExternalFactorDependencies().size(); i++) {
            if (parameterToDependencies.getExternalFactorDependencies().get(i).isPositive()) {
                res += PolyUtils.truncPolyCoeffDigits(parameterToDependencies.getExternalFactorDependencies().get(i).getSlope()) + "(t)" + "+ ("
                        + PolyUtils.truncPolyCoeffDigits(parameterToDependencies.getExternalFactorDependencies().get(i).getIntersection()) + ")";
            }
        }
        res += ")";
        for (int i = 0; i < parameterToDependencies.getPolynomialDependencies().size(); i++) {
            if (parameterToDependencies.getPolynomialDependencies().get(i).isPositive()) {
                res += "(" + buildPolyString(parameterToDependencies.getPolynomialDependencies().get(i).getParameterIndex(),
                        parameterToDependencies.getPolynomialDependencies().get(i).getPolyCoeffs()) + ")";
            }
        }
        res += "\\\\";
        res += " - (";
        for (int i = 0; i < parameterToDependencies.getExternalFactorDependencies().size(); i++) {
            if (!parameterToDependencies.getExternalFactorDependencies().get(i).isPositive()) {
                res += PolyUtils.truncPolyCoeffDigits(parameterToDependencies.getExternalFactorDependencies().get(i).getSlope()) + "(t)" + "("
                        + PolyUtils.truncPolyCoeffDigits(parameterToDependencies.getExternalFactorDependencies().get(i).getIntersection()) + ")";
            }
        }
        res += ")";
        for (int i = 0; i < parameterToDependencies.getPolynomialDependencies().size(); i++) {
            if (!parameterToDependencies.getPolynomialDependencies().get(i).isPositive()) {
                res += "(" + buildPolyString(parameterToDependencies.getPolynomialDependencies().get(i).getParameterIndex(),
                        parameterToDependencies.getPolynomialDependencies().get(i).getPolyCoeffs()) + ")";
            }
        }
        return res;
    }

    private String buildPolyString(int parameterNumber, double[] coeffs) {
        PolynomialFunction polynomialFunction = new PolynomialFunction(coeffs);
        String polyPart = polynomialFunction.toString().replace("x", "X_" + (parameterNumber + 1));
        return polyPart;
    }
}
