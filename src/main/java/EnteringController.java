import flanagan.integration.RungeKutta;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

public class EnteringController {

        @FXML
        private Button calculateButton;

        @FXML
        private TextField fFifthLeft;

        @FXML
        private TextField fFifthRight;

        @FXML
        private TextField fFifthX0;

        @FXML
        private TextField fFifthX1;

        @FXML
        private TextField fFifthX2;

        @FXML
        private TextField fFifthX3;

        @FXML
        private TextField fFirstLeft;

        @FXML
        private TextField fFirstRight;

        @FXML
        private TextField fFirstX0;

        @FXML
        private TextField fFirstX1;

        @FXML
        private TextField fFirstX2;

        @FXML
        private TextField fFirstX3;

        @FXML
        private TextField fFourthLeft;

        @FXML
        private TextField fFourthRight;

        @FXML
        private TextField fFourthX0;

        @FXML
        private TextField fFourthX1;

        @FXML
        private TextField fFourthX2;

        @FXML
        private TextField fFourthX3;

        @FXML
        private TextField fSecondLeft;

        @FXML
        private TextField fSecondRight;

        @FXML
        private TextField fSecondX0;

        @FXML
        private TextField fSecondX1;

        @FXML
        private TextField fSecondX2;

        @FXML
        private TextField fSecondX3;

        @FXML
        private TextField fThirdLeft;

        @FXML
        private TextField fThirdRight;

        @FXML
        private TextField fThirdX0;

        @FXML
        private TextField fThirdX1;

        @FXML
        private TextField fThirdX2;

        @FXML
        private TextField fThirdX3;

        @FXML
        private AnchorPane left_scroll_anchor_pane;

        @FXML
        void calculateDerivs(ActionEvent event) {
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
                int i1 = Integer.parseInt(fFirstLeft.getText());
                int i2 = Integer.parseInt(fFirstRight.getText());
                i1--; i2--;
                String join = String.join(",", fFirstX3.getText(),
                        fFirstX2.getText(), fFirstX1.getText(), fFirstX0.getText());
                coefs[i1][i2] = join;
                i1 = Integer.parseInt(fSecondLeft.getText());
                i2 = Integer.parseInt(fSecondRight.getText());
                i1--; i2--;
                join = String.join(",", fSecondX3.getText(),
                        fSecondX2.getText(), fSecondX1.getText(), fSecondX0.getText());
                coefs[i1][i2] = join;
                i1 = Integer.parseInt(fThirdLeft.getText());
                i2 = Integer.parseInt(fThirdRight.getText());
                i1--; i2--;
                join = String.join(",", fThirdX3.getText(),
                        fThirdX2.getText(), fThirdX1.getText(), fThirdX0.getText());
                coefs[i1][i2] = join;
                i1 = Integer.parseInt(fFourthLeft.getText());
                i2 = Integer.parseInt(fFourthRight.getText());
                i1--; i2--;
                join = String.join(",", fFourthX3.getText(),
                        fFourthX2.getText(), fFourthX1.getText(), fFourthX0.getText());
                coefs[i1][i2] = join;
                i1 = Integer.parseInt(fFifthLeft.getText());
                i2 = Integer.parseInt(fFifthRight.getText());
                i1--; i2--;
                join = String.join(",", fFifthX3.getText(),
                        fFifthX2.getText(), fFifthX1.getText(), fFifthX0.getText());
                coefs[i1][i2] = join;

                double h = 0.01;                      // step size
                double x0 = 0.0D;                     // initial value of x
                double xn = 0.1D;
                double[ ] yn = new double[15];         //results
                DerivSystem systemDeriv = new DerivSystem();
                systemDeriv.setCoefs(coefs);
                systemDeriv.setCons(cons);
                for (int j = 0; j < 10; j++) {
                        yn = RungeKutta.fourthOrder(systemDeriv, x0, y0, xn, h);
                        System.out.println("Fourth order Runge-Kutta results");
                        for (int i = 0; i < 15; i++) {
                                System.out.println(xn + " " + yn[i]);
                        }
                        y0 = yn;
                        x0 += 0.1;
                        xn += 0.1;
                }
        }
}
