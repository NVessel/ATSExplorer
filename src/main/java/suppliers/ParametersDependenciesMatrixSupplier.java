package suppliers;

import lombok.AllArgsConstructor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class ParametersDependenciesMatrixSupplier {

    private static final String STRING_FOR_SEEK = "X";

    private final File matrixFile;

    public static int[][] getMatrix() {
        int[][] mas = new int[15][15];
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                mas[i][j] = 0;
            }
        }
        mas[0][1] = -1;
        mas[0][2] = 1;
        mas[0][3] = -1;
        mas[0][4] = 1;
        mas[0][5] = 1;
        mas[0][6] = 1;
        mas[0][7] = -1;
        mas[0][9] = 1;
        mas[0][11] = 1;

        mas[1][7] = 1;
        mas[1][10] = 1;
        mas[1][13] = 1;

        mas[2][1] = 1;
        mas[2][10] = 1;
        mas[2][12] = 1;

        mas[3][1] = 1;
        mas[3][2] = -1;
        mas[3][14] = 1;

        mas[4][1] = -1;
        mas[4][2] = 1;
        mas[4][3] = -1;
        mas[4][7] = -1;
        mas[4][8] = -1;
        mas[4][9] = 1;
        mas[4][10] = -1;
        mas[4][11] = 1;

        mas[5][1] = -1;
        mas[5][2] = 1;
        mas[5][3] = -1;
        mas[5][7] = -1;
        mas[5][8] = -1;
        mas[5][9] = 1;
        mas[5][10] = -1;
        mas[5][11] = 1;

        mas[6][1] = -1;
        mas[6][2] = 1;
        mas[6][3] = -1;
        mas[6][9] = 1;

        mas[7][1] = 1;
        mas[7][4] = 1;
        mas[7][5] = 1;
        mas[7][12] = 1;
        mas[7][14] = 1;

        mas[8][1] = 1;
        mas[8][7] = 1;
        mas[8][12] = 1;

        mas[9][2] = 1;

        mas[10][2] = 1;
        mas[10][12] = 1;

        mas[12][0] = -1;
        mas[12][1] = 1;
        mas[12][3] = 1;
        mas[12][4] = -1;
        mas[12][5] = 1;
        mas[12][6] = -1;
        mas[12][7] = 1;
        mas[12][8] = 1;
        mas[12][9] = -1;
        mas[12][10] = 1;
        mas[12][13] = -1;

        mas[13][2] = 1;
        mas[13][8] = 1;
        mas[13][12] = 1;

        mas[14][1] = -1;
        mas[14][3] = -1;
        mas[14][4] = 1;
        mas[14][5] = 1;
        mas[14][6] = 1;
        mas[14][7] = -1;
        mas[14][8] = -1;
        mas[14][9] = 1;
        mas[14][10] = -1;

        return mas;
    }

    public List<List<Integer>> getExternalMatrix() throws IOException, InvalidFormatException {
        XSSFWorkbook matrixWorkbook = new XSSFWorkbook(matrixFile);
        XSSFSheet firstSheet = matrixWorkbook.getSheetAt(0);
        List<List<Integer>> matrix = new ArrayList<>();
        for (Row row: firstSheet) {
            List<Integer> rowCells = new ArrayList<>();
            for (Cell cell: row) {
                CellType nextType = cell.getCellType();
                if (nextType == CellType.NUMERIC) {
                    rowCells.add((int) cell.getNumericCellValue());
                }
            }
            if (!rowCells.isEmpty()) {
                matrix.add(rowCells);
            }
        }
        matrixWorkbook.close();
        return matrix;
    }

    public List<String> getParametersNames() throws IOException, InvalidFormatException {
        XSSFWorkbook matrixWorkbook = new XSSFWorkbook(matrixFile);
        XSSFSheet firstSheet = matrixWorkbook.getSheetAt(0);
        List<String> names = new ArrayList<>();
        for (Row row: firstSheet) {
            for (Cell cell: row) {
                CellType nextType = cell.getCellType();
                if (nextType == CellType.STRING
                        && cell.getStringCellValue().contains(STRING_FOR_SEEK)) {
                             names.add(cell.getStringCellValue());
                }
            }
            if (!names.isEmpty()) {
                break;
            }
        }
        matrixWorkbook.close();
        return names;
    }
}
