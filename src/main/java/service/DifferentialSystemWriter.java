package service;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
//TODO Refactor
public class DifferentialSystemWriter {

    private int symbolicViewPolynomialCounter;

    private static final String LATEX_NEW_LINE = "\\\\";

    @SneakyThrows
    public void writeSystemInNumericViewToLatex(List<PolynomialDependency> polynomialDependencies,
                                                String texFilename) {
        File latexFile = new File(texFilename);
        try (FileWriter fileWriter = new FileWriter(latexFile)) {
            writeBeginning(fileWriter);
            Map<Integer, List<PolynomialDependency>> derivativeParameterNumberToDependencies = buildDerivativeParameterNumberToItsDependencies(polynomialDependencies);
            for (Map.Entry<Integer, List<PolynomialDependency>> numberToDependencies : derivativeParameterNumberToDependencies.entrySet()) {
                fileWriter.write("\\begin{equation*}\n");
                fileWriter.write("\\begin{aligned}\n");
                fileWriter.write(buildParameterRow(numberToDependencies.getKey(),
                        buildSideOfParameterRowInNumericWay(numberToDependencies.getValue(), true),
                        buildSideOfParameterRowInNumericWay(numberToDependencies.getValue(), false)));
                fileWriter.write(LATEX_NEW_LINE);
                fileWriter.write("\n");
                fileWriter.write("\\end{aligned}\n");
                fileWriter.write("\\end{equation*}\n");
            }
            fileWriter.write("\\end{document}");
        }
    }

    @SneakyThrows
    public void writeSystemInSymbolicWayToLatex(List<PolynomialDependency> polynomialDependencies,
                                                String texFilename,
                                                int parametersQuantity) {
        File latexFile = new File(texFilename);
        try (FileWriter fileWriter = new FileWriter(latexFile)) {
            writeBeginning(fileWriter);
            Map<Integer, List<PolynomialDependency>> derivativeParameterNumberToDependencies = buildDerivativeParameterNumberToItsDependencies(polynomialDependencies);
            for (Map.Entry<Integer, List<PolynomialDependency>> numberToDependencies : derivativeParameterNumberToDependencies.entrySet()) {
                fileWriter.write("\\begin{equation*}\n");
                fileWriter.write("\\begin{aligned}\n");
                fileWriter.write(buildParameterRow(numberToDependencies.getKey(),
                        buildSideOfParameterRowInSymbolicWay(numberToDependencies.getValue(), parametersQuantity, true),
                        buildSideOfParameterRowInSymbolicWay(numberToDependencies.getValue(), parametersQuantity, false)));
                fileWriter.write(LATEX_NEW_LINE);
                fileWriter.write("\n");
                fileWriter.write("\\end{aligned}\n");
                fileWriter.write("\\end{equation*}\n");
            }
            fileWriter.write("\\end{document}");
        }
    }

    public void makePdfFromLatexFile(String texFilename) {
        File latexFile = new File(texFilename);
        if (latexFile.exists()) {
            startPdfCreationProcess(texFilename);
        } else {
            log.log(Level.WARNING, "Tex file wasn't found for pdf creation");
        }
    }

    private static void writeBeginning(FileWriter fileWriter) throws IOException {
        fileWriter.write("\\documentclass[10pt, letterpaper]{article}\n");
        fileWriter.write("\\usepackage[fleqn]{amsmath}     % Выравнивание формул по левому краю\n");
        fileWriter.write("\\setlength{\\mathindent}{0pt}");
        fileWriter.write("\\usepackage[utf8]{inputenc}\n");
        fileWriter.write("\\usepackage{geometry}\n" +
                "\\geometry{\n" +
                "  a4paper,\n" +
                "  left=3mm,\n" +
                "  top=15mm}\n");
        fileWriter.write("\n");
        fileWriter.write("\\begin{document}\n");
    }

    private static void startPdfCreationProcess(String texFilename) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("pdflatex", texFilename);
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

    private String buildParameterRow(Integer derivativeParameterNumber,
                                     String positiveSide,
                                     String negativeSide) {
        return "\\frac{dX_{" +
                (derivativeParameterNumber + 1) +
                "}(t)}{dt} &= " +
                positiveSide +
                LATEX_NEW_LINE +
                "&- \\; " +
                negativeSide;
    }

    private String buildSideOfParameterRowInNumericWay(List<PolynomialDependency> polynomialDependenciesForParameter,
                                                       boolean isPositiveSideNeeded) {
        StringBuilder sideToAdd = new StringBuilder("(");
        int externalFactorCounter = 0;
        for (PolynomialDependency polynomialDependency : polynomialDependenciesForParameter) {
            if (polynomialDependency.getRegressionMetrics().isEmpty() &&
                    polynomialDependency.isAffectingParameterPositiveDependency() == isPositiveSideNeeded) {
                sideToAdd.append(buildExternalFactorForParameterRowInNumericWay(PolynomialUtils.truncatePolynomialCoefficientsDigits(polynomialDependency.getPolynomialCoefficients()), externalFactorCounter));
                externalFactorCounter++;
            }
        }
        //case when only one external factor or none – useless parentheses is not needed
        if (externalFactorCounter == 0) {
            sideToAdd = new StringBuilder();
        }
        else if (externalFactorCounter == 1) {
            sideToAdd = new StringBuilder(sideToAdd.substring(1));
        } else {
            sideToAdd.append(")");
        }
        for (PolynomialDependency polynomialDependency : polynomialDependenciesForParameter) {
            if (!polynomialDependency.getRegressionMetrics().isEmpty() && polynomialDependency.isAffectingParameterPositiveDependency() == isPositiveSideNeeded) {
                sideToAdd.append("(").append(buildPolynomialForParameterRowInNumericWay(polynomialDependency.getPolynomialCoefficients(), polynomialDependency.getDerivativeParameterNumberToAffectingParameterNumber().getValue())).append(")");
            }
        }
        return sideToAdd.toString();
    }

    private String buildSideOfParameterRowInSymbolicWay(List<PolynomialDependency> polynomialDependenciesForParameter,
                                                        int parametersQuantity,
                                                        boolean isPositiveSideNeeded) {
        StringBuilder sideToAdd = new StringBuilder("(");

        int externalFactorCounter = 0;
        for (PolynomialDependency polynomialDependency : polynomialDependenciesForParameter) {
            if (polynomialDependency.getRegressionMetrics().isEmpty() &&
                    polynomialDependency.isAffectingParameterPositiveDependency() == isPositiveSideNeeded) {
                sideToAdd.append(buildExternalFactorForParameterRowInSymbolicWay(
                        polynomialDependency.getDerivativeParameterNumberToAffectingParameterNumber().getValue(),
                        externalFactorCounter, parametersQuantity));
                externalFactorCounter++;
            }
        }
        //case when only one external factor or none – useless parentheses is not needed
        if (externalFactorCounter == 0) {
            sideToAdd = new StringBuilder();
        }
        else if (externalFactorCounter == 1) {
            sideToAdd = new StringBuilder(sideToAdd.substring(1));
        } else {
            sideToAdd.append(")");
        }

        for (PolynomialDependency polynomialDependency : polynomialDependenciesForParameter) {
            if (!polynomialDependency.getRegressionMetrics().isEmpty() && polynomialDependency.isAffectingParameterPositiveDependency() == isPositiveSideNeeded) {
                sideToAdd.append(buildPolynomialForParameterRowInSymbolicWay(polynomialDependency.getDerivativeParameterNumberToAffectingParameterNumber().getValue()));
            }
        }
        return sideToAdd.toString();
    }

    private String buildExternalFactorForParameterRowInSymbolicWay(Integer affectingParameterNumber, int externalFactorCounter, int parametersQuantity) {
        if (externalFactorCounter > 0) {
            return " + F_{" + (affectingParameterNumber - parametersQuantity + 1) + "}(t)";
        }
        return "F_{" + (affectingParameterNumber - parametersQuantity + 1) + "}(t)";
    }

    private String buildPolynomialForParameterRowInSymbolicWay(int affectingParameterNumber) {
        return "f_{" + this.symbolicViewPolynomialCounter++
                + "}("
                + "X_{" + (affectingParameterNumber + 1)
                + "}(t))";
    }

    private String buildExternalFactorForParameterRowInNumericWay(double[] polynomialCoefficients, int elementCounter) {
        PolynomialFunction polynomialFunction = new PolynomialFunction(polynomialCoefficients);
        if (elementCounter > 0) {
            return " + (" + polynomialFunction.toString().replace("x", "t") + ")";
        }
        return "(" + polynomialFunction.toString().replace("x", "t") + ")";
    }

    private String buildPolynomialForParameterRowInNumericWay(double[] coefficients, int parameterNumber) {
        PolynomialFunction polynomialFunction = new PolynomialFunction(coefficients);
        return polynomialFunction.toString().replace("x", "X_{" + (parameterNumber + 1) + "}(t)");
    }

}
