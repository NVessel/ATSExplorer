package service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import model.DerivativeParameterNumberWithDependencies;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import utils.PolynomialUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

@Log
@AllArgsConstructor
public class DifferentialSystemWriter {

    private static final String TEX_FILENAME = "equationSystem.tex";

    private List<DerivativeParameterNumberWithDependencies> parametersEquationInformation;

    @SneakyThrows
    public void writeSystemToLatex() {
        File latexFile = new File(TEX_FILENAME);
        try (FileWriter fileWriter = new FileWriter(latexFile)) {
            fileWriter.write("\\documentclass[12pt, letterpaper]{article}\n");
            fileWriter.write("\\usepackage{amsmath}\n");
            fileWriter.write("\\usepackage[utf8]{inputenc}\n");
            fileWriter.write("\n");
            fileWriter.write("\\begin{document}\n");
            fileWriter.write("\\begin{equation*}\n");
            fileWriter.write("\\begin{cases}\n");
            for (DerivativeParameterNumberWithDependencies derivativeParameterNumberWithDependencies : parametersEquationInformation) {
                fileWriter.write(buildParameterRow(derivativeParameterNumberWithDependencies));
                fileWriter.write("\\\\");
            }
            fileWriter.write("\\end{cases}\n");
            fileWriter.write("\\end{equation*}\n");
            fileWriter.write("\\end{document}");
        }
    }

    public void writeSystemToPdf() {
        File latexFile = new File(TEX_FILENAME);
        if (latexFile.exists()) {
            startPdfCreationProcess();
        } else {
            log.log(Level.WARNING, "Tex file wasn't found for pdf creation");
        }
    }

    private static void startPdfCreationProcess() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("pdflatex", TEX_FILENAME);
            processBuilder.directory(new File("."));
            processBuilder.start();
        } catch (IOException e) {
            log.log(Level.SEVERE, "Can not run pdflatex tool", e);
        }
    }

    private String buildParameterRow(DerivativeParameterNumberWithDependencies derivativeParameterNumberWithDependencies) {
        StringBuilder parameterRow = new StringBuilder();
        parameterRow.append("\\frac{X_{")
                .append(derivativeParameterNumberWithDependencies.getDerivativeParameterNumber() + 1)
                .append("}(t)}{dt} = (");
        parameterRow = buildSideOfParameterRow(derivativeParameterNumberWithDependencies, parameterRow, true);
        parameterRow.append("\\\\");
        parameterRow.append("\\quad");
        parameterRow.append(" - (");
        parameterRow = buildSideOfParameterRow(derivativeParameterNumberWithDependencies, parameterRow, false);
        return parameterRow.toString();
    }

    private StringBuilder buildSideOfParameterRow(DerivativeParameterNumberWithDependencies derivativeParameterNumberToDependencies, StringBuilder parameterRowToAddPart, boolean isPositiveSideNeeded) {
        int elementCounter = 0;
        for (int i = 0; i < derivativeParameterNumberToDependencies.getExternalFactorDependencies().size(); i++) {
            if (derivativeParameterNumberToDependencies.getExternalFactorDependencies().get(i).isPositiveDependency() == isPositiveSideNeeded) {
                parameterRowToAddPart.append(buildExternalFactorString(PolynomialUtils.truncatePolynomialCoefficientDigits(derivativeParameterNumberToDependencies.getExternalFactorDependencies().get(i).getSlope()),
                        PolynomialUtils.truncatePolynomialCoefficientDigits(derivativeParameterNumberToDependencies.getExternalFactorDependencies().get(i).getIntersection()), elementCounter));
                elementCounter++;
            }
        }
        //case when only one external factor or none – useless parentheses is not needed
        if (elementCounter < 2) {
            parameterRowToAddPart = new StringBuilder(parameterRowToAddPart.substring(0, parameterRowToAddPart.lastIndexOf("(")) + parameterRowToAddPart.substring(parameterRowToAddPart.lastIndexOf("(") + 1));
        } else {
            parameterRowToAddPart.append(")");
        }
        for (int i = 0; i < derivativeParameterNumberToDependencies.getPolynomialDependencies().size(); i++) {
            if (derivativeParameterNumberToDependencies.getPolynomialDependencies().get(i).isPositiveDependency() == isPositiveSideNeeded) {
                parameterRowToAddPart.append("(").append(buildPolynomialAsString(derivativeParameterNumberToDependencies.getPolynomialDependencies().get(i).getParameterNumber(),
                        derivativeParameterNumberToDependencies.getPolynomialDependencies().get(i).getPolynomialCoefficients())).append(")");
            }
        }
        return parameterRowToAddPart;
    }

    private String buildExternalFactorString(double slope, double intercept, int elementCounter) {
        PolynomialFunction polynomialFunction = new PolynomialFunction(new double[] {intercept, slope});
        if (elementCounter > 0) {
            return " + (" + polynomialFunction.toString().replace("x", "t") + ")";
        }
        return "(" + polynomialFunction.toString().replace("x", "t") + ")";
    }

    private String buildPolynomialAsString(int parameterNumber, double[] coefficients) {
        PolynomialFunction polynomialFunction = new PolynomialFunction(coefficients);
        return polynomialFunction.toString().replace("x", "X_" + (parameterNumber + 1));
    }
}
