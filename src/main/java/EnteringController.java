import flanagan.integration.RungeKutta;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

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
        private TextField x100Field;

        @FXML
        private TextField x10Field;

        @FXML
        private TextField x110Field;

        @FXML
        private TextField x120Field;

        @FXML
        private TextField x130Field;

        @FXML
        private TextField x140Field;

        @FXML
        private TextField x150Field;

        @FXML
        private TextField x20Field;

        @FXML
        private TextField x30Field;

        @FXML
        private TextField x40Field;

        @FXML
        private TextField x50Field;

        @FXML
        private TextField x60Field;

        @FXML
        private TextField x70Field;

        @FXML
        private TextField x80Field;

        @FXML
        private TextField x90Field;

        @FXML
        void calculateDerivs(ActionEvent event) {
                int[][] cons = PosNegMatrixSupplier.getMatrix();
                String[][] coefs = CoefMatrixSupplier.getMatrix();
                double[] y0 = new double[15];
                y0[0] = Double.parseDouble(x10Field.getText());
                y0[1] = Double.parseDouble(x20Field.getText());
                y0[2] = Double.parseDouble(x30Field.getText());
                y0[3] = Double.parseDouble(x40Field.getText());
                y0[4] = Double.parseDouble(x50Field.getText());
                y0[5] = Double.parseDouble(x60Field.getText());
                y0[6] = Double.parseDouble(x70Field.getText());
                y0[7] = Double.parseDouble(x80Field.getText());
                y0[8] = Double.parseDouble(x90Field.getText());
                y0[9] = Double.parseDouble(x100Field.getText());
                y0[10] = Double.parseDouble(x110Field.getText());
                y0[11] = Double.parseDouble(x120Field.getText());
                y0[12] = Double.parseDouble(x130Field.getText());
                y0[13] = Double.parseDouble(x140Field.getText());
                y0[14] = Double.parseDouble(x150Field.getText());
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
                double xn = 1.0D;
                double[ ] yn = new double[15];         //results
                DerivSystem systemDeriv = new DerivSystem();
                systemDeriv.setCoefs(coefs);
                systemDeriv.setCons(cons);
                RungeKutta rk = new RungeKutta();
                rk.setInitialValueOfX(x0);
                rk.setFinalValueOfX(xn);
                rk.setStepSize(h);
                rk.setInitialValuesOfY(y0);
                yn = rk.fourthOrder(systemDeriv);
                System.out.println("Fourth order Runge-Kutta results");
                for (int i = 0; i < 15; i++) {
                        System.out.println(xn + " " + yn[i]);
                }
                System.out.println("Number of iterations = " + rk.getNumberOfIterations());
        }
}
