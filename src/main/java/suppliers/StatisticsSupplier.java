package suppliers;

import lombok.AllArgsConstructor;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class StatisticsSupplier {

    private final File statisticsFile;

    public List<List<Double>> getExternalStatistics() throws IOException, InvalidFormatException {
        XSSFWorkbook matrixWorkbook = new XSSFWorkbook(statisticsFile);
        XSSFSheet firstSheet = matrixWorkbook.getSheet("norm data");
        List<List<Double>> result = new ArrayList<>();
        for (Row row: firstSheet) {
            List<Double> rowCells = new ArrayList<>();
            for (Cell cell: row) {
                CellType nextType = cell.getCellType();
                if (nextType == CellType.FORMULA) {
                    XSSFFormulaEvaluator formulaEvaluator = matrixWorkbook.getCreationHelper().createFormulaEvaluator();
                    CellValue evaluated = formulaEvaluator.evaluate(cell);
                    double roundValue = BigDecimal.valueOf(evaluated.getNumberValue())
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                    rowCells.add(roundValue);
                }
            }
            if (!rowCells.isEmpty()) {
                result.add(rowCells);
            }
        }
        matrixWorkbook.close();
        return transposeMatrix(result);
    }

    private List<List<Double>> transposeMatrix(List<List<Double>> matrix) {
        List<List<Double>> transposedMatrix = new ArrayList<>();
        int oldColumnsSize = matrix.get(0).size();
        for (int i = 0; i < oldColumnsSize; i++) {
            List<Double> stat = new ArrayList<>();
            for (List<Double> doubles : matrix) {
                stat.add(doubles.get(i));
            }
            transposedMatrix.add(stat);
        }
        return transposedMatrix;
    }
}
