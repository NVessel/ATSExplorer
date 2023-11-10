package utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PolyUtils {

    public static double trunkPolyCoefficientDigits(double coef) {
        return BigDecimal.valueOf(coef)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    public static double[] trunkPolyCoefficientsDigits(double[] coeffs) {
        double[] result = new double[coeffs.length];
        for (int i = 0; i < coeffs.length; i++) {
            result[i] = BigDecimal.valueOf(coeffs[i])
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        return result;
    }

    public static double[] trunkPolyCoefficientsDigits(double[] coeffs, int digitCount) {
        double[] result = new double[coeffs.length];
        for (int i = 0; i < coeffs.length; i++) {
            result[i] = BigDecimal.valueOf(coeffs[i])
                    .setScale(digitCount, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        return result;
    }

    public static String generateSubscript(int i) {
        StringBuilder sb = new StringBuilder();
        for (char ch : String.valueOf(i).toCharArray()) {
            sb.append((char) ('\u2080' + (ch - '0')));
        }
        return sb.toString();
    }

    public static String generateSuperscript(int i) {
        StringBuilder sb = new StringBuilder();
        for (char ch : String.valueOf(i).toCharArray()) {
            if (ch == '2' || ch == '3') {
                sb.append((char) ('\u00B0' + (ch - '0')));
            } else {
                sb.append((char) ('\u2070' + (ch - '0')));
            }
        }
        return sb.toString();
    }
}
