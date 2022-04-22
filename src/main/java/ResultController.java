import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class ResultController {

    @FXML
    private GridPane result_table;

    public void displayResults(double[][] yn) {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 10; j++) {
                Label label = new Label(String.valueOf(yn[i][j]));
                result_table.add(label, j, i);
            }
        }
    }
}
