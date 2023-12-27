package controller;

import builder.ViewComponentsBuilder;
import deriv.DerivSystem;
import flanagan.integration.RungeKutta;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.java.Log;
import service.CalculationService;
import service.ResultsDemonstrationService;
import suppliers.CoefMatrixSupplier;
import suppliers.ParametersDependenciesMatrixSupplier;
import suppliers.StatisticsSupplier;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

@Log
public class MainPageController {

    private static final int EQUATIONS_MIN_QUANTITY = 2;
    @FXML
    private VBox mainPageRootVBox;
    @FXML
    private VBox initialConditionsVBox;
    @FXML
    private VBox externalFactorsVBox;
    @FXML
    private AnchorPane left_scroll_anchor_pane;
    @FXML
    private AnchorPane equationsAnchorPane;
    @FXML
    private VBox equationsVBox;

    private final ResultsDemonstrationService resultsDemonstrationService = new ResultsDemonstrationService();
    private final CalculationService calculationService = new CalculationService();
    private final ViewComponentsBuilder viewComponentsBuilder = new ViewComponentsBuilder();

    @FXML
    private void calculateDerivs(ActionEvent event) throws IOException {
        int[][] cons = ParametersDependenciesMatrixSupplier.getMatrix();
        String[][] coefs = CoefMatrixSupplier.getMatrix();
        double[] y0 = new double[15];
        ObservableList<Node> leftScrollAnchorPaneChildren = left_scroll_anchor_pane.getChildren();
        int fieldCounter = 0;
        for (Node hbox : leftScrollAnchorPaneChildren) {
            for (Node labelOrInput : ((HBox) hbox).getChildren()) {
                if (labelOrInput instanceof TextField) {
                    TextField textField = (TextField) labelOrInput;
                    y0[fieldCounter++] = Double.parseDouble(textField.getText());
                }
            }
        }
        ObservableList<Node> rightPaneChildren = equationsAnchorPane.getChildren();
        for (Node node : rightPaneChildren) {
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
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SolvingResultsPage.fxml"));
        Parent load = fxmlLoader.load();
        ResultController resultController = fxmlLoader.getController();
        resultController.displayResults(ynParts);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(load);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void calculateOnStatistics() {
        try {
            calculationService.calculateOnStatistics();
            log.log(Level.INFO, "Calculation is completed!");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Something was wrong in calculation", e);
        }
    }

    @FXML
    private void loadMatrixFileAndRewriteSystemElements() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("excelFilter", "*.xlsx"));
        File matrixFile = fileChooser.showOpenDialog(mainPageRootVBox.getScene().getWindow());
        ParametersDependenciesMatrixSupplier parametersDependenciesMatrixSupplier = new ParametersDependenciesMatrixSupplier(matrixFile);
        this.redrawInitialConditions(parametersDependenciesMatrixSupplier);
        this.redrawExternalFactors(parametersDependenciesMatrixSupplier);
        this.calculationService.setParametersDependenciesMatrixSupplier(parametersDependenciesMatrixSupplier);
    }

    @FXML
    private void loadStatisticFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("excelFilter", "*.xlsx"));
        File statisticFile = fileChooser.showOpenDialog(mainPageRootVBox.getScene().getWindow());
        this.calculationService.setStatisticsSupplier(new StatisticsSupplier(statisticFile));
    }

    @FXML
    private void addAnotherPolynomial() {
        equationsVBox.getChildren().add(new Separator());
        equationsVBox.getChildren().add(viewComponentsBuilder.buildAnotherPolynomialHBox());
    }

    @FXML
    private void deleteAnotherPolynomial() {
        if (equationsVBox.getChildren().size() > EQUATIONS_MIN_QUANTITY) {
            //two times because of separator
            for (int i = 0; i < 2; i++) {
                equationsVBox.getChildren().remove(equationsVBox.getChildren().size() - 1);
            }
        }
    }

    @FXML
    private void showSolutionResults() {
        this.resultsDemonstrationService.showSolutionResults();
    }

    @FXML
    private void showDependenciesGraphs() {
        this.resultsDemonstrationService.showDependenciesGraphsResults();
    }

    @FXML
    private void showDifferentialEquationsSystem() {
        this.resultsDemonstrationService.showDifferentialEquationsSystemResults();
    }

    @FXML
    private void closeApplication() {
        System.exit(0);
    }

    private void redrawInitialConditions(ParametersDependenciesMatrixSupplier parametersDependenciesMatrixSupplier) {
        initialConditionsVBox.getChildren().clear();
        for (String parameterName : parametersDependenciesMatrixSupplier.getParametersNames()) {
            initialConditionsVBox.getChildren().add(viewComponentsBuilder.buildInitialConditionHBox(parameterName));
        }
    }

    private void redrawExternalFactors(ParametersDependenciesMatrixSupplier parametersDependenciesMatrixSupplier) {
        externalFactorsVBox.getChildren().clear();
        for (String externalFactorName : parametersDependenciesMatrixSupplier.getExternalFactorsNames()) {
            externalFactorsVBox.getChildren().add(viewComponentsBuilder.buildExternalFactorHBox(externalFactorName));
        }
    }
}
