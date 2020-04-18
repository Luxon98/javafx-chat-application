package chatclient;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

import static chatclient.SqlQueries.*;


public class SignUpController {

    @FXML
    private Button backButton;

    @FXML
    private Button registerButton;

    @FXML
    private TextField loginTextField;

    @FXML
    private TextField emailTextField;

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

    private void showDataTakenAlert(String type) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(type + " is already taken");
        alert.setHeaderText(null);
        alert.setContentText("User with the specified " + type.toLowerCase() + " already exist");

        alert.setX(backButton.getLayoutX() + 395);
        alert.setY(backButton.getLayoutY() - 175);

        alert.showAndWait();
    }

//    private void showInvalidEmailAlert() {
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setTitle("Error");
//        alert.setHeaderText(null);
//        alert.setContentText("The given e-mail address is not valid");
//
//        alert.setX(backButton.getLayoutX() + 395);
//        alert.setY(backButton.getLayoutY() - 175);
//
//        alert.showAndWait();
//    }

    private void showDifferentPasswordsAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("The entered passwords do not match");

        alert.setX(backButton.getLayoutX() + 395);
        alert.setY(backButton.getLayoutY() - 175);

        alert.showAndWait();
    }

    private void showUserCreatedAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("A new user has been created");

        alert.setX(backButton.getLayoutX() + 395);
        alert.setY(backButton.getLayoutY() - 175);

        alert.showAndWait();
    }

    @FXML
    public void registerAccount(ActionEvent event) {
        String login = loginTextField.getText();
        if (isUsernameAlreadyTaken(login)) {
            showDataTakenAlert("Username");
            return;
        }

        String email = emailTextField.getText();
        if (isEmailAlreadyTaken(email)) {
            showDataTakenAlert("E-mail address");
            return;
        }
//        else if (isValid(email)) {
//            showInvalidEmailAlert();
//            return;
//        }

        String password1 = (passwordCheckBox.isSelected() ? passwordTextField.getText() : passwordField.getText());
        String password2 = (passwordCheckBox.isSelected() ? confirmPasswordTextField.getText() : confirmPasswordField.getText());
        if (!password1.equals(password2)) {
            showDifferentPasswordsAlert();
            return;
        }

        insertNewUser(login, email, BCrypt.hashpw(password1, BCrypt.gensalt()));
        showUserCreatedAlert();
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

    @FXML
    public void changeRegisterButtonStyle() {
        registerButton.setStyle("-fx-background-color: #7c66e8; -fx-font-size: 14; -fx-border-color: #3a3d39; -fx-background-radius: 10; -fx-border-radius: 10");
    }

    @FXML
    public void restoreRegisterButtonStyle() {
        registerButton.setStyle("-fx-background-color: #389af5; -fx-font-size: 14; -fx-border-color: #3a3d39; -fx-background-radius: 10; -fx-border-radius: 10");
    }

    @FXML
    public void changeBackButtonStyle() {
        backButton.setStyle("-fx-background-color: #de1d1d; -fx-border-color: #3a3d39; -fx-background-radius: 10; -fx-border-radius: 10");
    }

    @FXML
    public void restoreBackButtonStyle() {
        backButton.setStyle("-fx-background-color: #0e782e; -fx-border-color: #3a3d39; -fx-background-radius: 10; -fx-border-radius: 10");
    }
}
