import flanagan.integration.DerivnFunction;

public class DerivSystem implements DerivnFunction {

    int[][] cons;
    String[][] coefs;

    @Override
    public double[] derivn(double t, double[] x) {
        double[] dxdt = new double[15];

        double pos_mult = 1;
        double neg_mult = 1;
        for (int j = 0; j < 15; j++) {
            if (cons[0][j] == 1) {
                String coef = coefs[0][j];
                String[] split = coef.split(",");
                pos_mult *= calculatePolinomial(split, x[j]);
            }
        }
        for (int j = 0; j < 15; j++) {
            if (cons[0][j] == -1) {
                String coef = coefs[0][j];
                String[] split = coef.split(",");
                neg_mult *= calculatePolinomial(split, x[j]);
            }
        }
        dxdt[0] = ((t - 0.5 * t) * pos_mult) - ((Math.sin(t) + Math.sin(t) + t) * neg_mult);

        pos_mult = 1;
        neg_mult = 1;
        for (int j = 0; j < 15; j++) {
            if (cons[1][j] == 1) {
                String coef = coefs[1][j];
                String[] split = coef.split(",");
                pos_mult *= calculatePolinomial(split, x[j]);
            }
        }
        for (int j = 0; j < 15; j++) {
            if (cons[1][j] == -1) {
                String coef = coefs[1][j];
                String[] split = coef.split(",");
                neg_mult *= calculatePolinomial(split, x[j]);
            }
        }
        dxdt[1] = (t * pos_mult) - ((0) * neg_mult);

        pos_mult = 1;
        neg_mult = 1;
        for (int j = 0; j < 15; j++) {
            if (cons[2][j] == 1) {
                String coef = coefs[2][j];
                String[] split = coef.split(",");
                pos_mult *= calculatePolinomial(split, x[j]);
            }
        }
        for (int j = 0; j < 15; j++) {
            if (cons[2][j] == -1) {
                String coef = coefs[2][j];
                String[] split = coef.split(",");
                neg_mult *= calculatePolinomial(split, x[j]);
            }
        }
        dxdt[2] = ((t + t) * pos_mult) - ((Math.sin(t) + Math.sin(t-1) + t + 0.5*t - t) * neg_mult);

        pos_mult = 1;
        neg_mult = 1;
        for (int j = 0; j < 15; j++) {
            if (cons[3][j] == 1) {
                String coef = coefs[3][j];
                String[] split = coef.split(",");
                pos_mult *= calculatePolinomial(split, x[j]);
            }
        }
        for (int j = 0; j < 15; j++) {
            if (cons[3][j] == -1) {
                String coef = coefs[3][j];
                String[] split = coef.split(",");
                neg_mult *= calculatePolinomial(split, x[j]);
            }
        }
        dxdt[3] = (t * pos_mult) - ((Math.sin(t) + Math.sin(t-1)) * neg_mult);

        pos_mult = 1;
        neg_mult = 1;
        for (int j = 0; j < 15; j++) {
            if (cons[4][j] == 1) {
                String coef = coefs[4][j];
                String[] split = coef.split(",");
                pos_mult *= calculatePolinomial(split, x[j]);
            }
        }
        for (int j = 0; j < 15; j++) {
            if (cons[4][j] == -1) {
                String coef = coefs[4][j];
                String[] split = coef.split(",");
                neg_mult *= calculatePolinomial(split, x[j]);
            }
        }
        dxdt[4] = ((0.5*t) * pos_mult) - ((Math.sin(t) + Math.sin(t-1) + t) * neg_mult);

        pos_mult = 1;
        neg_mult = 1;
        for (int j = 0; j < 15; j++) {
            if (cons[5][j] == 1) {
                String coef = coefs[5][j];
                String[] split = coef.split(",");
                pos_mult *= calculatePolinomial(split, x[j]);
            }
        }
        for (int j = 0; j < 15; j++) {
            if (cons[5][j] == -1) {
                String coef = coefs[5][j];
                String[] split = coef.split(",");
                neg_mult *= calculatePolinomial(split, x[j]);
            }
        }
        dxdt[5] = ((0.5*t) * pos_mult) - ((Math.sin(t) + Math.sin(t-1) + t) * neg_mult);

        pos_mult = 1;
        neg_mult = 1;
        for (int j = 0; j < 15; j++) {
            if (cons[6][j] == 1) {
                String coef = coefs[6][j];
                String[] split = coef.split(",");
                pos_mult *= calculatePolinomial(split, x[j]);
            }
        }
        for (int j = 0; j < 15; j++) {
            if (cons[6][j] == -1) {
                String coef = coefs[6][j];
                String[] split = coef.split(",");
                neg_mult *= calculatePolinomial(split, x[j]);
            }
        }
        dxdt[6] = ((0) * pos_mult) - ((Math.sin(t) + Math.sin(t-1)) * neg_mult);

        pos_mult = 1;
        neg_mult = 1;
        for (int j = 0; j < 15; j++) {
            if (cons[7][j] == 1) {
                String coef = coefs[7][j];
                String[] split = coef.split(",");
                pos_mult *= calculatePolinomial(split, x[j]);
            }
        }
        for (int j = 0; j < 15; j++) {
            if (cons[7][j] == -1) {
                String coef = coefs[7][j];
                String[] split = coef.split(",");
                neg_mult *= calculatePolinomial(split, x[j]);
            }
        }
        dxdt[7] = ((0.5*t + t) * pos_mult) - ((Math.sin(t) + Math.sin(t-1)) * neg_mult);

        pos_mult = 1;
        neg_mult = 1;
        for (int j = 0; j < 15; j++) {
            if (cons[8][j] == 1) {
                String coef = coefs[8][j];
                String[] split = coef.split(",");
                pos_mult *= calculatePolinomial(split, x[j]);
            }
        }
        for (int j = 0; j < 15; j++) {
            if (cons[8][j] == -1) {
                String coef = coefs[8][j];
                String[] split = coef.split(",");
                neg_mult *= calculatePolinomial(split, x[j]);
            }
        }
        dxdt[8] = ((0.5*t) * pos_mult) - ((Math.sin(t) + Math.sin(t-1)) * neg_mult);

        pos_mult = 1;
        neg_mult = 1;
        for (int j = 0; j < 15; j++) {
            if (cons[9][j] == 1) {
                String coef = coefs[9][j];
                String[] split = coef.split(",");
                pos_mult *= calculatePolinomial(split, x[j]);
            }
        }
        for (int j = 0; j < 15; j++) {
            if (cons[9][j] == -1) {
                String coef = coefs[9][j];
                String[] split = coef.split(",");
                neg_mult *= calculatePolinomial(split, x[j]);
            }
        }
        dxdt[9] = ((0) * pos_mult) - ((Math.sin(t) + Math.sin(t-1)) * neg_mult);

        pos_mult = 1;
        neg_mult = 1;
        for (int j = 0; j < 15; j++) {
            if (cons[10][j] == 1) {
                String coef = coefs[10][j];
                String[] split = coef.split(",");
                pos_mult *= calculatePolinomial(split, x[j]);
            }
        }
        for (int j = 0; j < 15; j++) {
            if (cons[10][j] == -1) {
                String coef = coefs[10][j];
                String[] split = coef.split(",");
                neg_mult *= calculatePolinomial(split, x[j]);
            }
        }
        dxdt[10] = ((0) * pos_mult) - ((Math.sin(t) + Math.sin(t-1)) * neg_mult);

        pos_mult = 1;
        neg_mult = 1;
        for (int j = 0; j < 15; j++) {
            if (cons[11][j] == 1) {
                String coef = coefs[11][j];
                String[] split = coef.split(",");
                pos_mult *= calculatePolinomial(split, x[j]);
            }
        }
        for (int j = 0; j < 15; j++) {
            if (cons[11][j] == -1) {
                String coef = coefs[11][j];
                String[] split = coef.split(",");
                neg_mult *= calculatePolinomial(split, x[j]);
            }
        }
        dxdt[11] = ((0) * pos_mult) - ((2.5*t) * neg_mult);

        pos_mult = 1;
        neg_mult = 1;
        for (int j = 0; j < 15; j++) {
            if (cons[12][j] == 1) {
                String coef = coefs[12][j];
                String[] split = coef.split(",");
                pos_mult *= calculatePolinomial(split, x[j]);
            }
        }
        for (int j = 0; j < 15; j++) {
            if (cons[12][j] == -1) {
                String coef = coefs[12][j];
                String[] split = coef.split(",");
                neg_mult *= calculatePolinomial(split, x[j]);
            }
        }
        dxdt[12] = ((2*t) * pos_mult) - ((Math.sin(t) + Math.sin(t-1)) * neg_mult);

        pos_mult = 1;
        neg_mult = 1;
        for (int j = 0; j < 15; j++) {
            if (cons[13][j] == 1) {
                String coef = coefs[13][j];
                String[] split = coef.split(",");
                pos_mult *= calculatePolinomial(split, x[j]);
            }
        }
        for (int j = 0; j < 15; j++) {
            if (cons[13][j] == -1) {
                String coef = coefs[13][j];
                String[] split = coef.split(",");
                neg_mult *= calculatePolinomial(split, x[j]);
            }
        }
        dxdt[13] = ((Math.sin(t)) * pos_mult) - ((t + Math.sin(t-1)) * neg_mult);

        pos_mult = 1;
        neg_mult = 1;
        for (int j = 0; j < 15; j++) {
            if (cons[14][j] == 1) {
                String coef = coefs[14][j];
                String[] split = coef.split(",");
                pos_mult *= calculatePolinomial(split, x[j]);
            }
        }
        for (int j = 0; j < 15; j++) {
            if (cons[14][j] == -1) {
                String coef = coefs[14][j];
                String[] split = coef.split(",");
                neg_mult *= calculatePolinomial(split, x[j]);
            }
        }
        dxdt[14] = ((-1 * t) * pos_mult) - ((t) * neg_mult);
        return dxdt;
    }

    public int[][] getCons() {
        return cons;
    }

    public void setCons(int[][] cons) {
        this.cons = cons;
    }

    public String[][] getCoefs() {
        return coefs;
    }

    public void setCoefs(String[][] coefs) {
        this.coefs = coefs;
    }

    private double calculatePolinomial(String[] coefs, double x) {
        return Integer.parseInt(coefs[0]) * Math.pow(x, 3) + Integer.parseInt(coefs[1]) * Math.pow(x,2) +
                Integer.parseInt(coefs[2]) * Math.pow(x, 1) + Integer.parseInt(coefs[3]);
    }
}
