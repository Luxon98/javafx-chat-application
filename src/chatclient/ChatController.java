package chatclient;

import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.image.Image;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static chatclient.ChatUtility.*;
import static chatclient.DatabaseQueries.*;


public class ChatController {

    @FXML
    private Label usernameLabel;

    @FXML
    private Label interlocutorLabel;

    @FXML
    private TextArea messageTextArea;

    @FXML
    private Pane messagesPane;

    @FXML
    private ScrollPane messagesScrollPane;

    @FXML
    private Pane friendsListPane;

    @FXML
    private Button sendMessageButton;

    private Pane[] friendsPanes;
    private Image[] images;
    private ClientApplication clientApplication;
    private AtomicBoolean inUse = new AtomicBoolean();
    private int currentInterlocutorId;
    private int currentMessagesCounter = 0;

    @FXML
    public void initialize() {
        if (clientApplication == null) {
            int id = getId(Client.getInstance().getUsername());
            clientApplication = new ClientApplication(id);
            currentInterlocutorId = id;
            checkForUpcomingContent();
            initImages();
            drawUserPanel();
        }
        drawFriendsPanel();
    }

    @FXML
    private void sendMessage() {
        String message = messageTextArea.getText();
        if (message != null && !message.isEmpty()) {
            clientApplication.sendMessage(currentInterlocutorId, message);
            messageTextArea.setText(null);

            if (isTooLong(message)) {
                message = splitWithNewLine(message);
            }

            drawMessageLabel(message, false);
        }
    }

    @FXML
    private void handleTextAreaKey(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            String message = messageTextArea.getText();
            message = cutNewLineCharacter(message);
            messageTextArea.setText(message);

            sendMessage();
        }
    }

    @FXML
    private void changeSendMessageButtonStyle() {
        sendMessageButton.setStyle("-fx-background-color: #3548db;");
    }

    @FXML
    private void restoreSendMessageButtonStyle() {
        sendMessageButton.setStyle("-fx-background-color: #d63900;");
    }

    @FXML
    private void showUsernameTextInputDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Invite a friend");
        dialog.setHeaderText("Type username");
        dialog.setContentText("Please, enter your friend's username:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String username = result.get();
            processInvitationAttempt(username);
        }
    }

    private void checkForUpcomingContent() {
        Thread thread = new Thread(() -> {
            while (!Client.getInstance().isProgramClosed()) {
                if (inUse.compareAndSet(false, true)) {
                    if (clientApplication.containsMessages()) {
                        handleIncomingMessage();
                    }
                    if (clientApplication.containsInvitations()) {
                        handleIncomingInvitation();
                    }
                    if (clientApplication.areFriendsStatusesUpdated()) {
                        updateFriendsStatuses();
                    }
                    inUse.set(false);
                }
            }
        });
        thread.start();
    }

    private void handleIncomingMessage() {
        Message message = clientApplication.getMessage();
        if (message.isFromCurrentInterlocutor(currentInterlocutorId)) {
            Platform.runLater(() -> drawMessageLabel(message.getMessage(), true));
        }
        else {
            int index = clientApplication.getListIndex(message.getSenderId());
            if (isProperIndex(index)) {
                showNewMessageImage(index);
            }
        }
    }

    private void handleIncomingInvitation() {
        int senderId = clientApplication.getInvitingUserId();
        if (isAcceptingInvitation(getUsername(senderId))) {
            insertNewFriendship(senderId, clientApplication.getUserId());
            clientApplication.updateFriendsList();
            Platform.runLater(this::initialize);
        }
    }

    private boolean isAcceptingInvitation(String username) {
        boolean result = false;
        final FutureTask query = new FutureTask(() -> getUserAnswer(username));
        Platform.runLater(query);
        try {
            result = (boolean) query.get();
        }
        catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean getUserAnswer(String username) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Invitation");
        alert.setHeaderText("The user " + username + " has invited you to friends");
        alert.setContentText("Do you want to accept the invitation");

        ButtonType confirmationButton = new ButtonType("Yes");
        ButtonType rejectionButton = new ButtonType("No");
        alert.getButtonTypes().setAll(confirmationButton, rejectionButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.filter(buttonType -> (buttonType == confirmationButton)).isPresent();
    }

    private void updateFriendsStatuses() {
        Boolean[] friendsStatuses = clientApplication.getFriendsStatuses();
        for (int i = 0; i < friendsStatuses.length; ++i) {
            ImageView statusIw = (ImageView) friendsPanes[i].lookup("#statusImg");
            statusIw.setImage(friendsStatuses[i] ? images[2] : images[3]);
        }

        clientApplication.unsetUpdateFlag();
    }

    private void processInvitationAttempt(String username) {
        if (clientApplication.isAlreadyFriend(username)) {
            showAlreadyFriendDialog(username);
        }
        else if (usernameLabel.getText().equals(username)) {
            showCannotInviteYourselfDialog();
        }
        else if (isExistingUsername(username)) {
            int receiverId = getId(username);
            if (!isExistingInvitation(clientApplication.getUserId(), receiverId)) {
                showInvitationSendDialog(username);
                clientApplication.sendInvitation(receiverId);
            }
            else {
                showInvitationSentAlreadyDialog();
            }
        }
        else {
            showUserDoesntExistDialog();
        }
    }

    private void showInvitationSendDialog(String username) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invitation sent");
        alert.setHeaderText(null);
        alert.setContentText("The invitation to user " + username + " was sent");

        alert.showAndWait();
    }

    private void showAlreadyFriendDialog(String username) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText("The user " + username + " is your friend already");

        alert.showAndWait();
    }

    private void showUserDoesntExistDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("The user with the given name does not exist \nin the database");

        alert.showAndWait();
    }

    private void showCannotInviteYourselfDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("You cannot invite yourself!");

        alert.showAndWait();
    }

    private void showInvitationSentAlreadyDialog() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText("You have already invited this user");

        alert.showAndWait();
    }

    private void initImages() {
        images = new Image[4];
        images[0] = new Image("file:images/new_message.png");
        images[1] = new Image("file:images/default_avatar.png");
        images[2] = new Image("file:images/green_circle.png");
        images[3] = new Image("file:images/red_circle.png");
    }

    private void drawUserPanel() {
        ImageView userAvatar = getUserAvatar();
        friendsListPane.getChildren().add(userAvatar);
        usernameLabel.setText(Client.getInstance().getUsername());
    }

    private void showNewMessageImage(int index) {
        ImageView iw = (ImageView) friendsPanes[index].lookup("#messageImg");
        iw.setVisible(true);
    }

    private ImageView getUserAvatar() {
        ImageView avatar = new ImageView(images[1]);
        avatar.setLayoutX(18);
        avatar.setLayoutY(12);
        return avatar;
    }

    private void drawFriendsPanel() {
        List<Friend> friendsList = clientApplication.getFriendsList();
        friendsPanes = new Pane[friendsList.size()];

        int index = 0, positionY = 65;
        for (Friend friend : friendsList) {
            friendsPanes[index] = new Pane();
            setFriendPaneStyle(friendsPanes[index], positionY);

            ImageView avatarImg = getFriendAvatar();
            ImageView messageImg = getMessageImage();
            ImageView statusImg = getStatusImage();

            Label friendNameLabel = getFriendNameLabel(friend);

            addFriendPaneComponents(friendsPanes[index], new ImageView[]{messageImg, avatarImg, statusImg}, friendNameLabel);
            setMouseClickEvent(friendsPanes[index], friendNameLabel, messageImg, friend.getId());

            friendsListPane.getChildren().add(friendsPanes[index]);
            positionY += 55;
            ++index;
        }
    }

    private void setFriendPaneStyle(Pane friendPane, int y) {
        friendPane.setPrefSize(200, 55);
        friendPane.setLayoutY(y);
        friendPane.setStyle("-fx-border-color: aliceblue; -fx-border-color: #a2a3a2; -fx-border-width: 0 0 1 0;");
    }

    private ImageView getFriendAvatar() {
        ImageView avatar = new ImageView(images[1]);
        avatar.setLayoutX(18);
        avatar.setLayoutY(12);
        return avatar;
    }

    private ImageView getMessageImage() {
        ImageView image = new ImageView(images[0]);
        image.setLayoutX(147);
        image.setLayoutY(13);
        image.setId("messageImg");
        image.setVisible(false);
        return image;
    }

    private ImageView getStatusImage() {
        ImageView image = new ImageView(images[3]);
        image.setLayoutX(41);
        image.setLayoutY(34);
        image.setId("statusImg");
        return image;
    }

    private Label getFriendNameLabel(Friend friend) {
        Label label = new Label();
        label.setLayoutX(58);
        label.setLayoutY(18);
        label.setTextFill(Color.WHITE);
        label.setText(friend.getUsername());
        return label;
    }

    private void addFriendPaneComponents(Pane pane, ImageView[] images, Label friendNameLabel) {
        pane.getChildren().add(images[0]);
        pane.getChildren().add(images[1]);
        pane.getChildren().add(images[2]);
        pane.getChildren().add(friendNameLabel);
    }

    private void setMouseClickEvent(Pane friendPane, Label friendNameLabel, ImageView messageImg, int id) {
        friendPane.setOnMouseClicked(t -> {
            if (currentInterlocutorId != id) {
                interlocutorLabel.setText(friendNameLabel.getText());
                currentInterlocutorId = id;
                removeMessages();
                messagesPane.setPrefHeight(485);
                messageImg.setVisible(false);
            }
        });
    }

    private void removeMessages() {
        messagesPane.getChildren().clear();
        currentMessagesCounter = 0;
    }

    private void drawMessageLabel(String message, boolean isReceived) {
        Label label = new Label();
        setLabelStyle(label, message, isReceived);

        messagesPane.getChildren().add(label);
        ++currentMessagesCounter;

        if (isTooManyMessages(currentMessagesCounter)) {
            adjustMessagesPane();
        }
    }

    private void setLabelStyle(Label label, String message, boolean isReceived) {
        label.setPrefHeight(isTooLong(message) ? 46 : 23);
        label.setText(message);

        if (isReceived) {
            label.setStyle("-fx-background-color: #eb34d2; -fx-padding: 4 4 4 4; -fx-background-radius: 3; " +
                    "-fx-font-family: Arial; -fx-font-size: 14; -fx-text-fill: #ffffff;-fx-text-alignment: center;");
            label.setLayoutX(28);
        }
        else {
            label.setStyle("-fx-background-color: #05b529; -fx-padding: 4 4 4 4; -fx-background-radius: 3; " +
                    "-fx-font-family: Arial; -fx-font-size: 14; -fx-text-fill: #ffffff;-fx-text-alignment: center");
            Text text = new Text(label.getText());
            text.setFont(label.getFont());
            label.setLayoutX(546 - text.getBoundsInLocal().getWidth());
        }
        label.setLayoutY(10 + currentMessagesCounter * 35);
    }

    private void adjustMessagesPane() {
        messagesPane.setPrefHeight(messagesPane.getHeight() + 40);
        messagesScrollPane.setVvalue(1.0);
    }

    @FXML
    private void test() {
        clientApplication.sendTest();
    }
}
