import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("MainPageV2.fxml")));
        stage.setTitle("Программный комплекс для работы с АТС");
        stage.setScene(new Scene(root, 1200, 650));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
