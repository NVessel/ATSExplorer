package controller;

import builder.ViewComponentsBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import lombok.extern.java.Log;
import service.CalculationService;
import service.ResultsDemonstrationService;
import suppliers.ParametersDependenciesMatrixSupplier;
import suppliers.StatisticsSupplier;

import java.io.File;
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
    private VBox equationsVBox;

    private final ResultsDemonstrationService resultsDemonstrationService = new ResultsDemonstrationService();
    private final CalculationService calculationService = new CalculationService();
    private final ViewComponentsBuilder viewComponentsBuilder = new ViewComponentsBuilder();

    @FXML
    private void calculateOnGivenDependencies() {

        /*Write results*/
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
                equationsVBox.getChildren()
                        .remove(equationsVBox.getChildren().size() - 1);
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
