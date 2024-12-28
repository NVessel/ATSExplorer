package utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PolynomialUtils {

    public static double truncatePolynomialCoefficientDigits(double coefficient) {
        return BigDecimal.valueOf(coefficient)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static double[] truncatePolynomialCoefficientsDigits(double[] coefficients) {
        double[] result = new double[coefficients.length];
        for (int i = 0; i < coefficients.length; i++) {
            result[i] = BigDecimal.valueOf(coefficients[i])
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        return result;
    }

    public static double[] truncatePolynomialCoefficientsDigits(double[] coefficients, int digitScale) {
        double[] result = new double[coefficients.length];
        for (int i = 0; i < coefficients.length; i++) {
            result[i] = BigDecimal.valueOf(coefficients[i])
                    .setScale(digitScale, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        return result;
    }

    public static String generateSubscriptTitle(int number) {
        StringBuilder sb = new StringBuilder();
        for (char ch : String.valueOf(number).toCharArray()) {
            sb.append((char) ('\u2080' + (ch - '0')));
        }
        return sb.toString();
    }

    public static String generateSuperscriptTitle(int number) {
        StringBuilder sb = new StringBuilder();
        for (char ch : String.valueOf(number).toCharArray()) {
            if (ch == '2' || ch == '3') {
                sb.append((char) ('\u00B0' + (ch - '0')));
            } else {
                sb.append((char) ('\u2070' + (ch - '0')));
            }
        }
        return sb.toString();
    }
}
