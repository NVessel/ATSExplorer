package controller;

import deriv.DerivSystem;
import deriv.DerivSystemV2;
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
import service.CalculationService;
import suppliers.CoefMatrixSupplier;
import suppliers.PosNegMatrixSupplier;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EnteringController implements Initializable {

        private final Logger logger = Logger.getLogger(DerivSystemV2.class.getName());

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
                double h = 0.01;
                double x0 = 0.0D;
                double xn = 0.1D;
                double[] yn;
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
        void calculateOnStatistics(ActionEvent event) {
            CalculationService calculationService = new CalculationService();
            try {
                calculationService.calculateOnStatistics();
                logger.log(Level.INFO, "Calculation is completed!");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Something was wrong in calculation", e);
            }
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
