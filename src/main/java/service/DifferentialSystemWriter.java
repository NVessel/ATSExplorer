package service;

import lombok.SneakyThrows;
import lombok.extern.java.Log;
import model.PolynomialDependency;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import utils.PolynomialUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Log
public class DifferentialSystemWriter {

    private static final String TEX_FILENAME = "equationSystem.tex";

    @SneakyThrows
    public void writeSystemToLatex(List<PolynomialDependency> polynomialDependencies) {
        File latexFile = new File(TEX_FILENAME);
        try (FileWriter fileWriter = new FileWriter(latexFile)) {
            fileWriter.write("\\documentclass[12pt, letterpaper]{article}\n");
            fileWriter.write("\\usepackage{amsmath}\n");
            fileWriter.write("\\usepackage[utf8]{inputenc}\n");
            fileWriter.write("\n");
            fileWriter.write("\\begin{document}\n");
            fileWriter.write("\\begin{equation*}\n");
            fileWriter.write("\\begin{cases}\n");
            Map<Integer, List<PolynomialDependency>> derivativeParameterNumberToDependencies = buildDerivativeParameterNumberToItsDependencies(polynomialDependencies);
            for (Map.Entry<Integer, List<PolynomialDependency>> numberToDependencies : derivativeParameterNumberToDependencies.entrySet()) {
                fileWriter.write(buildParameterRow(numberToDependencies.getKey(), numberToDependencies.getValue()));
                fileWriter.write("\\\\");
            }
            fileWriter.write("\\end{cases}\n");
            fileWriter.write("\\end{equation*}\n");
            fileWriter.write("\\end{document}");
        }
    }

    public void makePdfFromLatexFile() {
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

    private Map<Integer, List<PolynomialDependency>> buildDerivativeParameterNumberToItsDependencies(List<PolynomialDependency> polynomialDependencies) {
        return polynomialDependencies
                .stream()
                .collect(Collectors.groupingBy(polynomialDependency -> polynomialDependency.getDerivativeParameterNumberToAffectingParameterNumber().getKey()));
    }

    private String buildParameterRow(Integer derivativeParameterNumber, List<PolynomialDependency> polynomialDependencies) {
        StringBuilder parameterRow = new StringBuilder();
        parameterRow.append("\\frac{dX_{")
                .append(derivativeParameterNumber + 1)
                .append("}(t)}{dt} = (");
        parameterRow = buildSideOfParameterRow(polynomialDependencies, parameterRow, true);
        parameterRow.append("\\\\");
        parameterRow.append("\\quad");
        parameterRow.append(" - (");
        parameterRow = buildSideOfParameterRow(polynomialDependencies, parameterRow, false);
        return parameterRow.toString();
    }

    private StringBuilder buildSideOfParameterRow(List<PolynomialDependency> polynomialDependencies,
                                                  StringBuilder parameterRowToAddPart,
                                                  boolean isPositiveSideNeeded) {
        int elementCounter = 0;
        for (PolynomialDependency polynomialDependency : polynomialDependencies) {
            if (polynomialDependency.getRegressionMetrics().isEmpty() &&
                    polynomialDependency.isAffectingParameterPositiveDependency() == isPositiveSideNeeded) {
                parameterRowToAddPart.append(buildExternalFactorString(PolynomialUtils.truncatePolynomialCoefficientsDigits(polynomialDependency.getPolynomialCoefficients()), elementCounter));
                elementCounter++;
            }
        }
        //case when only one external factor or none – useless parentheses is not needed
        if (elementCounter < 2) {
            parameterRowToAddPart = new StringBuilder(parameterRowToAddPart.substring(0,
                    parameterRowToAddPart.lastIndexOf("(")) + parameterRowToAddPart.substring(parameterRowToAddPart.lastIndexOf("(") + 1));
        } else {
            parameterRowToAddPart.append(")");
        }
        for (PolynomialDependency polynomialDependency : polynomialDependencies) {
            if (!polynomialDependency.getRegressionMetrics().isEmpty() && polynomialDependency.isAffectingParameterPositiveDependency() == isPositiveSideNeeded) {
                parameterRowToAddPart.append("(").append(buildPolynomialAsString(polynomialDependency.getPolynomialCoefficients(), polynomialDependency.getDerivativeParameterNumberToAffectingParameterNumber().getValue())).append(")");
            }
        }
        return parameterRowToAddPart;
    }

    private String buildExternalFactorString(double[] polynomialCoefficients, int elementCounter) {
        PolynomialFunction polynomialFunction = new PolynomialFunction(polynomialCoefficients);
        if (elementCounter > 0) {
            return " + (" + polynomialFunction.toString().replace("x", "t") + ")";
        }
        return "(" + polynomialFunction.toString().replace("x", "t") + ")";
    }

    private String buildPolynomialAsString(double[] coefficients, int parameterNumber) {
        PolynomialFunction polynomialFunction = new PolynomialFunction(coefficients);
        return polynomialFunction.toString().replace("x", "X_" + (parameterNumber + 1));
    }
}
