package builder;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

public class ViewComponentsBuilder {

    private static final int POLYNOMIAL_HBOX_WIDTH = 559;

    public HBox buildAnotherPolynomialHBox() {
        HBox anotherPolynomailHBox = new HBox(this.getFilledLabelForAnotherPolynomialHBox("f"), this.getFilledTextFieldForAnotherPolynomialHBox("1"),
                this.getFilledLabelForAnotherPolynomialHBox("(t) ="), this.getFilledTextFieldForAnotherPolynomialHBox("1"), this.getFilledLabelForAnotherPolynomialHBox("x^3 +"),
                this.getFilledTextFieldForAnotherPolynomialHBox("1"), this.getFilledLabelForAnotherPolynomialHBox("x^2 +"), this.getFilledTextFieldForAnotherPolynomialHBox("1"),
                this.getFilledLabelForAnotherPolynomialHBox("x +"), this.getFilledTextFieldForAnotherPolynomialHBox("0"));
        anotherPolynomailHBox.setPrefWidth(POLYNOMIAL_HBOX_WIDTH);
        return anotherPolynomailHBox;
    }

    public HBox buildInitialConditionHBox(String hboxLabelValue) {
        HBox initialConditionHBox = new HBox(new Label(hboxLabelValue), this.getFilledTextFieldForInitialConditionHBox());
        initialConditionHBox.setAlignment(Pos.CENTER_RIGHT);
        return initialConditionHBox;
    }

    public HBox buildLimitValueHBox(String hboxLabelValue) {
        HBox initialConditionHBox = new HBox(new Label(hboxLabelValue), this.getFilledTextFieldForInitialConditionHBox());
        initialConditionHBox.setAlignment(Pos.CENTER_RIGHT);
        return initialConditionHBox;
    }

    public HBox buildExternalFactorHBox(String externalFactorName) {
        HBox externalFactorHBox = new HBox(new Label(externalFactorName + "(t) ="), this.getFilledTextFieldForExternalFactorHBox(),
                new Label("t^2 +"), this.getFilledTextFieldForExternalFactorHBox(), new Label("t +"), this.getFilledTextFieldForExternalFactorHBox());
        externalFactorHBox.setAlignment(Pos.CENTER_LEFT);
        return externalFactorHBox;
    }

    private Label getFilledLabelForAnotherPolynomialHBox(String labelValue) {
        Label label = new Label(labelValue);
        label.setFont(new Font(18));
        HBox.setMargin(label, new Insets(0, 0, 0, 2));
        return label;
    }

    private TextField getFilledTextFieldForAnotherPolynomialHBox(String fieldValue) {
        TextField textField = new TextField(fieldValue);
        textField.setFont(new Font(14));
        textField.setAlignment(Pos.CENTER_RIGHT);
        textField.setPrefWidth(60);
        HBox.setMargin(textField, new Insets(0, 0, 0, 3));
        return textField;
    }

    private TextField getFilledTextFieldForInitialConditionHBox() {
        TextField textField = new TextField("0");
        textField.setAlignment(Pos.CENTER_RIGHT);
        textField.setPrefWidth(60);
        return textField;
    }

    private TextField getFilledTextFieldForExternalFactorHBox() {
        TextField textField = new TextField("1");
        textField.setAlignment(Pos.CENTER_RIGHT);
        textField.setPrefWidth(60);
        HBox.setMargin(textField, new Insets(0, 3, 0, 3));
        return textField;
    }
}
