package suppliers;

public class CoefMatrixSupplier {

    private static final String VECTOR_ONE = "0,0,0,1";

    public static String[][] getMatrix() {
        String[][] mas = new String[15][15];

        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                mas[i][j] = "0,0,0,0";
            }
        }

        mas[0][1] = VECTOR_ONE;
        mas[0][2] = VECTOR_ONE;
        mas[0][3] = VECTOR_ONE;
        mas[0][4] = VECTOR_ONE;
        mas[0][5] = VECTOR_ONE;
        mas[0][6] = VECTOR_ONE;
        mas[0][7] = VECTOR_ONE;
        mas[0][9] = VECTOR_ONE;
        mas[0][11] = VECTOR_ONE;

        mas[1][7] = VECTOR_ONE;
        mas[1][10] = VECTOR_ONE;
        mas[1][13] = VECTOR_ONE;

        mas[2][1] = VECTOR_ONE;
        mas[2][10] = VECTOR_ONE;
        mas[2][12] = VECTOR_ONE;

        mas[3][1] = VECTOR_ONE;
        mas[3][2] = VECTOR_ONE;
        mas[3][14] = VECTOR_ONE;

        mas[4][1] = VECTOR_ONE;
        mas[4][2] = VECTOR_ONE;
        mas[4][3] = VECTOR_ONE;
        mas[4][7] = VECTOR_ONE;
        mas[4][8] = VECTOR_ONE;
        mas[4][9] = VECTOR_ONE;
        mas[4][10] = VECTOR_ONE;
        mas[4][11] = VECTOR_ONE;

        mas[5][1] = VECTOR_ONE;
        mas[5][2] = VECTOR_ONE;
        mas[5][3] = VECTOR_ONE;
        mas[5][7] = VECTOR_ONE;
        mas[5][8] = VECTOR_ONE;
        mas[5][9] = VECTOR_ONE;
        mas[5][10] = VECTOR_ONE;
        mas[5][11] = VECTOR_ONE;

        mas[6][1] = VECTOR_ONE;
        mas[6][2] = VECTOR_ONE;
        mas[6][3] = VECTOR_ONE;
        mas[6][9] = VECTOR_ONE;

        mas[7][1] = VECTOR_ONE;
        mas[7][4] = VECTOR_ONE;
        mas[7][5] = VECTOR_ONE;
        mas[7][12] = VECTOR_ONE;
        mas[7][14] = VECTOR_ONE;

        mas[8][1] = VECTOR_ONE;
        mas[8][7] = VECTOR_ONE;
        mas[8][12] = VECTOR_ONE;

        mas[9][2] = VECTOR_ONE;

        mas[10][2] = VECTOR_ONE;
        mas[10][12] = VECTOR_ONE;

        mas[12][0] = VECTOR_ONE;
        mas[12][1] = VECTOR_ONE;
        mas[12][3] = VECTOR_ONE;
        mas[12][4] = VECTOR_ONE;
        mas[12][5] = VECTOR_ONE;
        mas[12][6] = VECTOR_ONE;
        mas[12][7] = VECTOR_ONE;
        mas[12][8] = VECTOR_ONE;
        mas[12][9] = VECTOR_ONE;
        mas[12][10] = VECTOR_ONE;
        mas[12][13] = VECTOR_ONE;

        mas[13][2] = VECTOR_ONE;
        mas[13][8] = VECTOR_ONE;
        mas[13][12] = VECTOR_ONE;

        mas[14][1] = VECTOR_ONE;
        mas[14][3] = VECTOR_ONE;
        mas[14][4] = VECTOR_ONE;
        mas[14][5] = VECTOR_ONE;
        mas[14][6] = VECTOR_ONE;
        mas[14][7] = VECTOR_ONE;
        mas[14][8] = VECTOR_ONE;
        mas[14][9] = VECTOR_ONE;
        mas[14][10] = VECTOR_ONE;

        return mas;
    }
}
