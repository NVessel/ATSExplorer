package utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PolyUtils {

    public static double[] truncPolyCoeffsDigits(double[] coeffs) {
        double[] result = new double[coeffs.length];
        for (int i = 0; i < coeffs.length; i++) {
            result[i] = BigDecimal.valueOf(coeffs[i])
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        return result;
    }

    public static double[] truncPolyCoeffsDigits(double[] coeffs, int digitCount) {
        double[] result = new double[coeffs.length];
        for (int i = 0; i < coeffs.length; i++) {
            result[i] = BigDecimal.valueOf(coeffs[i])
                    .setScale(digitCount, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        return result;
    }
}
