package suppliers;

import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
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
import java.util.logging.Level;

@Log
@AllArgsConstructor
public class ParametersDependenciesMatrixSupplier {

    private static final String PARAMETER_CATCH_STRING = "X";
    private static final String EXTERNAL_FACTOR_CATCH_STRING = "F";
    private final File matrixFile;

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

    public List<String> getParametersNames() {
        try {
            return getSystemElementsNames(PARAMETER_CATCH_STRING);
        } catch (IOException | InvalidFormatException e) {
            log.log(Level.SEVERE, "Matrix file wasn't opened");
            throw new RuntimeException(e);
        }
    }

    public List<String> getExternalFactorsNames() {
        try {
            return getSystemElementsNames(EXTERNAL_FACTOR_CATCH_STRING);
        } catch (IOException | InvalidFormatException e) {
            log.log(Level.SEVERE, "Matrix file wasn't opened");
            throw new RuntimeException(e);
        }
    }

    private List<String> getSystemElementsNames(String catchString) throws IOException, InvalidFormatException {
        XSSFWorkbook matrixWorkbook = new XSSFWorkbook(matrixFile);
        XSSFSheet firstSheet = matrixWorkbook.getSheetAt(0);
        List<String> names = new ArrayList<>();
        for (Row row: firstSheet) {
            for (Cell cell: row) {
                CellType nextType = cell.getCellType();
                if (nextType == CellType.STRING
                        && cell.getStringCellValue().contains(catchString)) {
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
