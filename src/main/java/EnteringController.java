import flanagan.integration.RungeKutta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class EnteringController implements Initializable {

        private static final int ITERATIONS_COUNT = 10;
        private static final Random RANDOM = new Random();

        @FXML
        private Button calculateButton;

        @FXML
        private AnchorPane left_scroll_anchor_pane;

        @FXML
        private Pane right_pane;

        @FXML
        void calculateDerivs(ActionEvent event) throws IOException {
                int[][] cons = PosNegMatrixSupplier.getMatrix();
                String[][] coefs = CoefMatrixSupplier.getMatrix();
                double[] y0 = new double[15];
                ObservableList<Node> leftScrollAnchorPaneChildren = left_scroll_anchor_pane.getChildren();
                int fieldCounter = 0;
                for (Node hbox: leftScrollAnchorPaneChildren) {
                        for (Node labelOrInput: ((HBox)hbox).getChildren()) {
                                if (labelOrInput instanceof TextField) {
                                        TextField textField = (TextField) labelOrInput;
                                        y0[fieldCounter++] = Double.parseDouble(textField.getText());
                                }
                        }
                }
                ObservableList<Node> rightPaneChildren = right_pane.getChildren();
                for (Node node: rightPaneChildren) {
                        HBox hbox = (HBox) node;
                        HBox leftHbox = (HBox) hbox.getChildren().get(0);
                        HBox rightHbox = (HBox) hbox.getChildren().get(1);
                        ChoiceBox<Integer> choiceLeft = (ChoiceBox<Integer>) leftHbox.getChildren().get(1);
                        ChoiceBox<Integer> choiceRight = (ChoiceBox<Integer>) leftHbox.getChildren().get(2);
                        Integer choiceLeftValue = choiceLeft.getValue();
                        Integer choiceRightValue = choiceRight.getValue();
                        String joinCoefs = "";
                        for (Node rightHboxChild : rightHbox.getChildren()) {
                                if (rightHboxChild instanceof TextField) {
                                        joinCoefs += ((TextField) rightHboxChild).getText() + ",";
                                }
                        }
                        coefs[--choiceLeftValue][--choiceRightValue] = joinCoefs;
                }
                double h = 0.01;                      // step size
                double x0 = 0.0D;                     // initial value of x
                double xn = 0.1D;
                double[] yn; //results
                double[][] ynParts = new double[15][10];
                DerivSystem systemDeriv = new DerivSystem();
                systemDeriv.setCoefs(coefs);
                systemDeriv.setCons(cons);
                for (int j = 0; j < 10; j++) {
                        yn = RungeKutta.fourthOrder(systemDeriv, x0, y0, xn, h);
                        for (int i = 0; i < 15; i++) {
                                ynParts[i][j] = yn[i];
                        }
                        y0 = yn;
                        x0 += 0.1;
                        xn += 0.1;
                }
                FXMLLoader fxmlLoader= new FXMLLoader(getClass().getResource("Untitled2.fxml"));
                Parent load = fxmlLoader.load();
                ResultController resultController = fxmlLoader.getController();
                resultController.displayResults(ynParts);
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(load);
                stage.setScene(scene);
                stage.show();
        }

        @FXML
        void calculateOnStatistics(ActionEvent event) throws URISyntaxException, IOException, InvalidFormatException {
                List<List<Integer>> posNegMatrix = PosNegMatrixSupplier.getExternalMatrix();
                List<List<Double>> statisticsMatrix = StatisticsSupplier.getExternalStatistics();
                List<String> parametersNames = PosNegMatrixSupplier.getParametersNames();
                double[] y0 = extractInitialValues(statisticsMatrix, posNegMatrix.size());
                double[] y0copy = y0;
                double h = 0.01;
                double x0 = 0.0D;
                double xn = 0.1D;
                double[] yn;
                double[][] ynParts = new double[posNegMatrix.size()][ITERATIONS_COUNT];
                DerivSystemV2 systemDeriv = new DerivSystemV2(posNegMatrix, statisticsMatrix);
                for (int j = 0; j < ITERATIONS_COUNT; j++) {
                        yn = RungeKutta.fourthOrder(systemDeriv, x0, y0, xn, h);
                        for (int i = 0; i < posNegMatrix.size(); i++) {
                                ynParts[i][j] = yn[i];
                        }
                        y0 = yn;
                        x0 += 0.1;
                        xn += 0.1;
                }
                showResults(y0copy, ynParts, parametersNames);
        }

        //cell styles to provider class
        private void showResults(double[] y0copy, double[][] ynParts, List<String> parametersNames) throws IOException {
                XSSFWorkbook resultBook = new XSSFWorkbook();
                XSSFSheet resultSheet = resultBook.createSheet("resultSheet");
                Row firstRow = resultSheet.createRow(0);
                int shiftForNamings = 1;
                for (int k = 0; k <= ITERATIONS_COUNT; k++) {
                        Cell cell = firstRow.createCell(k + shiftForNamings);
                        CellStyle cellStyle = resultBook.createCellStyle();
                        Font headerFont = resultBook.createFont();
                        headerFont.setBold(true);
                        cellStyle.setFont(headerFont);
                        cell.setCellStyle(cellStyle);
                        double timeStep = (double) k / ITERATIONS_COUNT;
                        cell.setCellValue("t = " + timeStep);
                }

                for (int i = 0; i < ynParts.length; i++) {
                        //i+1 j+1 because of place for naming
                        XSSFRow newRow = resultSheet.createRow(i + 1);
                        for (int j = 0; j < ITERATIONS_COUNT; j++) {
                                XSSFCell cell = newRow.createCell(j + 1 + shiftForNamings);
                                cell.setCellValue(ynParts[i][j]);
                        }
                        XSSFCell cell = newRow.createCell(shiftForNamings); //because of beginning
                        cell.setCellValue(y0copy[i]);
                        cell = newRow.createCell(0); //because it's edge
                        cell.setCellValue(parametersNames.get(i));
                }
                /*Row firstRow = resultSheet.createRow(0);
                for (int i = 0; i < 5; i++) {
                        Cell rowCell = firstRow.createCell(i);
                        rowCell.setCellValue(i);
                }
                Row secondRow = resultSheet.createRow(1);
                for (int i = 0; i < 5; i++) {
                        Cell rowCell = secondRow.createCell(i);
                        rowCell.setCellValue(i * i);
                }
                XSSFDrawing drawing = resultSheet.createDrawingPatriarch();
                XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 4, 7, 26);
                XSSFChart chart = drawing.createChart(anchor);
                chart.setTitleText("Test graph");
                chart.setTitleOverlay(false);
                XDDFChartLegend legend = chart.getOrAddLegend();
                legend.setPosition(LegendPosition.TOP_RIGHT);

                XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
                bottomAxis.setTitle("argument");
                XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
                leftAxis.setTitle("pow");

                XDDFNumericalDataSource<Double> arguments = XDDFDataSourcesFactory.fromNumericCellRange(resultSheet,
                        new CellRangeAddress(0, 0, 0, 4));

                XDDFNumericalDataSource<Double> pows = XDDFDataSourcesFactory.fromNumericCellRange(resultSheet,
                        new CellRangeAddress(1, 1, 0, 4));

                XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);

                XDDFLineChartData.Series series1 = (XDDFLineChartData.Series) data.addSeries(arguments, pows);

                series1.setTitle("explanation", null);
                series1.setSmooth(true);
                series1.setMarkerStyle(MarkerStyle.STAR);

                chart.plot(data);*/


                try (FileOutputStream fileOutputStream = new FileOutputStream("result.xlsx")) {
                        resultBook.write(fileOutputStream);
                }
        }

        private double[] extractInitialValues(List<List<Double>> statisticsMatrix, int processLimit) {
                double[] initials = new double[processLimit];
                for (int i = 0; i < processLimit; i++) {
                        initials[i] = statisticsMatrix.get(i).get(0);
                }
                return initials;
        }

        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {
                for (Node node: right_pane.getChildren()) {
                        HBox hbox = (HBox) node;
                        HBox leftHbox = (HBox) hbox.getChildren().get(0);
                        ChoiceBox<Integer> choiceLeft = (ChoiceBox<Integer>) leftHbox.getChildren().get(1);
                        ChoiceBox<Integer> choiceRight = (ChoiceBox<Integer>) leftHbox.getChildren().get(2);
                        List<Integer> integers = new ArrayList<>();
                        for (int i = 1; i < 16; i++) {
                                integers.add(i);
                        }
                        choiceLeft.getItems().clear();
                        choiceLeft.setItems(FXCollections.observableList(integers));
                        choiceLeft.setValue(RANDOM.nextInt(14) + 1);
                        choiceRight.getItems().clear();
                        choiceRight.setItems(FXCollections.observableList(integers));
                        choiceRight.setValue(RANDOM.nextInt(14) + 1);
                }
        }
}
