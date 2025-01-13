package controller;

import builder.ViewComponentsBuilder;
import javafx.fxml.FXML;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import lombok.extern.java.Log;
import service.CalculationService;
import service.FilesDemonstrationService;
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

    private final FilesDemonstrationService filesDemonstrationService = new FilesDemonstrationService();
    private final CalculationService calculationService = new CalculationService();
    private final ViewComponentsBuilder viewComponentsBuilder = new ViewComponentsBuilder();
    private ParametersDependenciesMatrixSupplier parametersDependenciesMatrixSupplier;
    private StatisticsSupplier statisticsSupplier;

    @FXML
    private void calculateOnGivenDependencies() {
        if (areManualSettingsReady()) {
            calculationService.calculateOnManualSettings();
        } else {
            log.warning("Can not calculate manually because some boxes are empty");
        }
    }

    @FXML
    private void calculateOnStatistics() {
        try {
            calculationService.calculateOnStatistics(this.parametersDependenciesMatrixSupplier, this.statisticsSupplier);
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
        this.parametersDependenciesMatrixSupplier = new ParametersDependenciesMatrixSupplier(matrixFile);
        this.redrawInitialConditions();
        this.redrawExternalFactors();
    }

    @FXML
    private void loadStatisticFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("excelFilter", "*.xlsx"));
        File statisticFile = fileChooser.showOpenDialog(mainPageRootVBox.getScene().getWindow());
        this.statisticsSupplier = new StatisticsSupplier(statisticFile);
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
        this.filesDemonstrationService.showSolutionResults();
    }

    @FXML
    private void showDependenciesGraphs() {
        this.filesDemonstrationService.showDependenciesGraphsResults();
    }

    @FXML
    private void showDifferentialEquationsSystem() {
        this.filesDemonstrationService.showDifferentialEquationsSystemResults();
    }

    @FXML
    private void closeApplication() {
        System.exit(0);
    }

    private void redrawInitialConditions() {
        initialConditionsVBox.getChildren().clear();
        for (String parameterName : this.parametersDependenciesMatrixSupplier.getParametersNames()) {
            initialConditionsVBox.getChildren().add(viewComponentsBuilder.buildInitialConditionHBox(parameterName));
        }
    }

    private boolean areManualSettingsReady() {
        return !initialConditionsVBox.getChildren().isEmpty()
                && !equationsVBox.getChildren().isEmpty()
                && !equationsVBox.getChildren().isEmpty();
    }

    private void redrawExternalFactors() {
        externalFactorsVBox.getChildren().clear();
        for (String externalFactorName : this.parametersDependenciesMatrixSupplier.getExternalFactorsNames()) {
            externalFactorsVBox.getChildren().add(viewComponentsBuilder.buildExternalFactorHBox(externalFactorName));
        }
    }
}
