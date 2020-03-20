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
    }

    private void setChatScene() throws IOException {
        Stage stage = (Stage) newAccountButton.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("chat.fxml"));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void logIn(ActionEvent event) {
        String login = loginTextField.getText();
        String password = passwordTextField.getText();

        if (!Database.isUser(login, password)) {
            showLoginFailedAlert();
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
}
