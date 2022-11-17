import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class StatisticsSupplier {

    public static List<List<Double>> getExternalStatistics() throws URISyntaxException, IOException, InvalidFormatException {
        URL url = PosNegMatrixSupplier.class.getResource("/excel/norm.xlsx");
        assert url != null;
        File resourceFile = new File(url.toURI());
        XSSFWorkbook matrixWorkbook = new XSSFWorkbook(resourceFile);
        XSSFSheet firstSheet = matrixWorkbook.getSheet("norm data");
        List<List<Double>> result = new ArrayList<>();
        for (Row row: firstSheet) {
            List<Double> rowCells = new ArrayList<>();
            for (Cell cell: row) {
                CellType nextType = cell.getCellType();
                switch (nextType) {
                    case STRING:
                        System.out.println("The cell was string and value was " + cell.getStringCellValue());
                        break;
                    case NUMERIC:
                        System.out.println("The cell was numeric and value was " + cell.getNumericCellValue());
                        break;
                    case BLANK:
                        System.out.println("The cell was blank format and address was " + cell.getAddress());
                        break;
                    case FORMULA:
                        XSSFFormulaEvaluator formulaEvaluator = matrixWorkbook.getCreationHelper().createFormulaEvaluator();
                        CellValue evaluated = formulaEvaluator.evaluate(cell);
                        System.out.println("The cell was formula and expr was " + evaluated.getNumberValue());
                        rowCells.add(evaluated.getNumberValue());
                        break;
                    default:
                        throw new IllegalStateException("wrong cell type: " + nextType);
                }
            }
            if (!rowCells.isEmpty()) {
                result.add(rowCells);
            }
        }
        return transposeMatrix(result);
    }

    private static List<List<Double>> transposeMatrix(List<List<Double>> matrix) {
        List<List<Double>> result = new ArrayList<>();
        int oldRowsSize = matrix.size();
        int oldColumnsSize = matrix.get(0).size();
        for (int i = 0; i < oldColumnsSize; i++) {
            List<Double> stat = new ArrayList<>();
            for (int j = 0; j < oldRowsSize; j++) {
                stat.add(matrix.get(j).get(i));
            }
            result.add(stat);
        }
        return result;
    }
}
