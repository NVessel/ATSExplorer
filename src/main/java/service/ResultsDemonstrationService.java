package service;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

@Log
public class ResultsDemonstrationService {

    private static final Desktop DESKTOP = Desktop.getDesktop();

    public void showSolutionResults() {
        try {
            DESKTOP.open(new File("resultBook.xlsx"));
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error happened while opening resultBook", e);
        }
    }

    public void showDependenciesGraphsResults() {
        try {
            DESKTOP.open(new File("resultPolyBook.xlsx"));
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error happened while opening resultPolyBook", e);
        }
    }

    public void showDifferentialEquationsSystemResults() {
        try {
            DESKTOP.open(new File("equationSystem.pdf"));
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error happened while opening equationSystem", e);
        }
    }
}
