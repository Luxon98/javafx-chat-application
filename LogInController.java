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

        if (!Database.isUser(login, password)) {
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
}
