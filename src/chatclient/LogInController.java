package chatclient;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;


public class LogInController {

    @FXML
    private TextField loginTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private Button newAccountButton;

    @FXML
    private Button loginButton;

    private void showLoginFailedAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("User not found");
        alert.setHeaderText(null);
        alert.setContentText("User with the specified login and password does not exist");

        alert.setX(passwordTextField.getLayoutX() + 275);
        alert.setY(passwordTextField.getLayoutY());

        alert.showAndWait();
        passwordTextField.setText(null);
    }

    private void setChatScene() throws IOException {
        Stage stage = (Stage) newAccountButton.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("chat.fxml"));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void logIn() {
        String login = loginTextField.getText();
        String password = passwordTextField.getText();

        if (!AuxiliaryDatabase.isUser(login, password)) {
            showLoginFailedAlert();
            return;
        }

        try {
            Client.getInstance().setUsername(login);
            setChatScene();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void setSignUpScene(ActionEvent event) throws IOException {
        Stage stage = (Stage) newAccountButton.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("signUp.fxml"));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void handlePasswordFieldKey(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            logIn();
        }
    }

    @FXML
    public void changeNewAccountButtonStyle() {
        newAccountButton.setStyle("-fx-background-color: #b2d6eb; -fx-border-color: #3a3d39; -fx-text-fill: #4d190b; -fx-background-radius: 8; -fx-border-radius: 8");
    }

    @FXML
    public void restoreNewAccountButtonStyle() {
        newAccountButton.setStyle("-fx-background-color: #e89910; -fx-border-color: #3a3d39; -fx-text-fill: #000000; -fx-background-radius: 8; -fx-border-radius: 8");
    }

    @FXML
    public void changeLoginButtonStyle() {
        loginButton.setStyle("-fx-background-color: #3e4eab; -fx-font-size: 14; -fx-border-color: #3a3d39; -fx-background-radius: 9; -fx-border-radius: 9");
    }

    @FXML
    public void restoreLoginButtonStyle() {
        loginButton.setStyle("-fx-background-color: #53adb5; -fx-font-size: 14; -fx-border-color: #3a3d39; -fx-background-radius: 9; -fx-border-radius: 9");
    }
}
