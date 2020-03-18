package chatclient;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;


public class SignUpController {

    @FXML
    private Button backButton;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField confirmPasswordTextField;

    @FXML
    private CheckBox passwordCheckBox;

    @FXML
    public void registerAccount(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login is already taken");
        alert.setHeaderText(null);
        alert.setContentText("User with the specified login already exist");

        alert.setX(backButton.getLayoutX() + 395);
        alert.setY(backButton.getLayoutY() - 175);

        alert.showAndWait();
    }

    @FXML
    public void changeTextFields(ActionEvent event) {
        if (passwordCheckBox.isSelected()) {
            passwordTextField.setText(passwordField.getText());
            confirmPasswordTextField.setText(confirmPasswordField.getText());
            passwordTextField.setVisible(true);
            confirmPasswordTextField.setVisible(true);
            passwordField.setVisible(false);
            confirmPasswordField.setVisible(false);
        }
        else {
            passwordField.setText(passwordTextField.getText());
            confirmPasswordField.setText(confirmPasswordTextField.getText());
            passwordField.setVisible(true);
            confirmPasswordField.setVisible(true);
            passwordTextField.setVisible(false);
            confirmPasswordTextField.setVisible(false);
        }
    }

    @FXML
    public void setLogInScene(ActionEvent event) throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("logIn.fxml"));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
