package chatapplication.client;

import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.image.Image;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static chatapplication.client.ChatUtility.*;


public class ChatController {

    @FXML
    private Label usernameLabel;

    @FXML
    private Pane interlocutorPane;

    @FXML
    private Label interlocutorLabel;

    @FXML
    private Pane messagesPane;

    @FXML
    private ScrollPane messagesScrollPane;

    @FXML
    private Pane friendsListPane;

    @FXML
    private Pane userAndFriendsPane;

    @FXML
    private TextArea messageTextArea;

    @FXML
    private Button addFriendButton;

    @FXML
    private Button optionsButton;

    @FXML
    private Button sendMessageButton;

    @FXML
    private Circle userAvatarCircle;

    private Pane[] friendsPanes;
    private Image[] images;
    private ClientApplication clientApplication;
    private AtomicBoolean inUse = new AtomicBoolean();
    private int currentInterlocutorId;
    private int currentMessagesCounter = 0;
    private int additionalHeight = 0;

    @FXML
    public void initialize() {
        if (clientApplication == null) {
            int id = DatabaseQueries.getId(Client.getInstance().getUsername());
            clientApplication = new ClientApplication(id);
            initImages();
            checkAndHandleUpcomingContent();
            drawUserPanel();
            messagesScrollPane.vvalueProperty().bind(messagesPane.heightProperty());
        }

        currentInterlocutorId = -1;
        messageTextArea.setDisable(true);
        drawFriendsPanel();
        clearInterlocutorPanel();
        removeMessages();
    }

    @FXML
    private void sendMessage() {
        if (isProperInterlocutorId(currentInterlocutorId)) {
            String message = messageTextArea.getText();
            if (message != null && !message.isEmpty()) {
                clientApplication.sendMessage(currentInterlocutorId, message);
                DatabaseQueries.insertNewMessage(clientApplication.getUserId(), currentInterlocutorId, message);
                messageTextArea.setText(null);
                drawMessageLabel(message, false);
            }
        }
        else {
            showChooseFriendAlert();
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
    private void changeAddFriendButtonStyle() {
        addFriendButton.setStyle("-fx-background-color: #0c9454; -fx-background-radius: 10");
    }

    @FXML
    private void restoreAddFriendButtonStyle() {
        addFriendButton.setStyle("-fx-background-color: #466163; -fx-background-radius: 10");
    }

    @FXML
    private void changeOptionsButtonStyle() {
        optionsButton.setStyle("-fx-background-color: #3d3c57;");
        optionsButton.setGraphic(new ImageView(images[8]));
    }

    @FXML
    private void restoreOptionsButtonStyle() {
        optionsButton.setStyle("-fx-background-color: #3d3c57;");
        optionsButton.setGraphic(new ImageView(images[7]));
    }

    @FXML
    private void changeSendMessageButtonStyle() {
        sendMessageButton.setStyle("-fx-background-color: #3548db; -fx-background-radius: 5");
    }

    @FXML
    private void restoreSendMessageButtonStyle() {
        sendMessageButton.setStyle("-fx-background-color: #d63900; -fx-background-radius: 5");
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

    @FXML
    private void showChooseAvatarTypeDialog() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("1", getAvatarsList());
        dialog.setTitle("Select an avatar");
        dialog.setContentText("Choose an avatar for yourself: ");

        Label contextLabel = new Label();
        contextLabel.setGraphic(new ImageView("file:images/choose_avatar_img.png"));
        dialog.getDialogPane().setHeader(contextLabel);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(type -> {
            int chosenType = Integer.parseInt(type);
            DatabaseQueries.updateAvatarType(clientApplication.getUserId(), chosenType);
            userAvatarCircle.setFill(new ImagePattern(images[chosenType + 2]));
        });
    }

    private void initImages() {
        images = new Image[9];
        images[0] = new Image("file:images/new_message.png");
        images[1] = new Image("file:images/green_circle.png");
        images[2] = new Image("file:images/red_circle.png");

        for (int i = 0; i < 4; ++i) {
            images[3 + i] = new Image("file:images/default_avatar" + String.valueOf(i + 1) + ".png");
        }

        images[7] = new Image("file:images/options1.png");
        images[8] = new Image("file:images/options2.png");
    }

    private void clearInterlocutorPanel() {
        interlocutorPane.getChildren().clear();
        interlocutorLabel.setText(null);
        interlocutorPane.getChildren().add(interlocutorLabel);
    }

    private void removeMessages() {
        messagesPane.getChildren().clear();
        currentMessagesCounter = 0;
    }

    private void checkAndHandleUpcomingContent() {
        Thread thread = new Thread(() -> {
            while (!Client.getInstance().isProgramClosed()) {
                if (inUse.compareAndSet(false, true)) {
                    checkAndHandle();
                    inUse.set(false);
                }
            }
        });
        thread.start();
    }

    private void checkAndHandle() {
        if (clientApplication.containsMessages()) {
            handleIncomingMessage();
        }
        if (clientApplication.containsInvitations()) {
            handleIncomingInvitation();
        }
        if (clientApplication.areFriendsStatusesUpdated()) {
            updateFriendsStatuses();
        }
        if (clientApplication.shouldPanelBeRedrawn()) {
            clientApplication.updateFriendsList();
            Platform.runLater(this::initialize);
            clientApplication.unsetRedrawPanelFlag();
        }
    }

    private void handleIncomingMessage() {
        Message message = clientApplication.getMessage();
        if (message.isFromCurrentInterlocutor(currentInterlocutorId)) {
            Platform.runLater(() -> drawMessageLabel(message.getMessage(), true));
            DatabaseQueries.updateMessages(currentInterlocutorId, clientApplication.getUserId());
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
        if (isAcceptingInvitation(DatabaseQueries.getUsername(senderId))) {
            DatabaseQueries.insertNewFriendship(senderId, clientApplication.getUserId());
            clientApplication.updateFriendsList();
            clientApplication.sendRedrawPanelCommand(senderId);
            Platform.runLater(this::initialize);
        }
        DatabaseQueries.removeInvitation(senderId, clientApplication.getUserId());
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

        return getAnswer(alert);
    }

    private boolean getAnswer(Alert alert) {
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
            if (statusIw != null) {
                statusIw.setImage(friendsStatuses[i] ? images[1] : images[2]);
            }
        }

        ImageView interlocutorStatusIw = (ImageView) interlocutorPane.lookup("#interlocutorStatusImg");
        if (interlocutorStatusIw != null) {
            interlocutorStatusIw.setImage(clientApplication.isFriendOnline(currentInterlocutorId) ? images[1] : images[2]);
        }

        clientApplication.unsetUpdateFlag();
    }

    private void processInvitationAttempt(String username) {
        if (clientApplication.isAlreadyFriend(username)) {
            showAlreadyFriendAlert(username);
        }
        else if (usernameLabel.getText().equals(username)) {
            showCannotInviteYourselfAlert();
        }
        else if (DatabaseQueries.isExistingUsername(username)) {
            int receiverId = DatabaseQueries.getId(username);
            if (!DatabaseQueries.isExistingInvitation(clientApplication.getUserId(), receiverId)) {
                showInvitationSendAlert(username);
                clientApplication.sendInvitation(receiverId);
                DatabaseQueries.insertNewInvitation(clientApplication.getUserId(), receiverId);
            }
            else {
                showInvitationSentAlreadyAlert();
            }
        }
        else {
            showUserDoesntExistAlert();
        }
    }

    private void showAlreadyFriendAlert(String username) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText("The user " + username + " is your friend already");

        alert.showAndWait();
    }

    private void showInvitationSendAlert(String username) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Invitation sent");
        alert.setHeaderText(null);
        alert.setContentText("The invitation to user " + username + " was sent");

        alert.showAndWait();
    }

    private void showCannotInviteYourselfAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("You cannot invite yourself!");

        alert.showAndWait();
    }

    private void showInvitationSentAlreadyAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText("You have already invited this user");

        alert.showAndWait();
    }

    private void showUserDoesntExistAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("The user with the given name does not exist \nin the database");

        alert.showAndWait();
    }

    private void showChooseFriendAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        Label textLabel = new Label("If you want to send a message, you must select \na friend by clicking on the panel with" +
                " his \nnickname on the left. If you don't have friends \nyet, you can add them using the button on the " +
                "\nbottom left.");
        textLabel.setStyle("-fx-padding: 10 10 10 10;");

        alert.getDialogPane().setContent(textLabel);
        alert.showAndWait();
    }

    private void drawUserPanel() {
        int userAvatarType = DatabaseQueries.getAvatarType(clientApplication.getUserId());
        userAvatarCircle.setFill(new ImagePattern(images[userAvatarType + 2]));

        ImageView userStatusImg = getStatusImage();
        userStatusImg.setImage(images[1]);
        userAndFriendsPane.getChildren().add(userStatusImg);

        usernameLabel.setText(Client.getInstance().getUsername());
    }

    private void showNewMessageImage(int index) {
        ImageView imageView = (ImageView) friendsPanes[index].lookup("#messageImg");
        if (imageView != null) {
            imageView.setVisible(true);
        }
    }

    private void drawFriendsPanel() {
        List<Friend> friendsList = clientApplication.getFriendsList();
        friendsPanes = new Pane[friendsList.size()];
        friendsListPane.getChildren().clear();

        int index = 0, positionY = 0;
        for (Friend friend : friendsList) {
            friendsPanes[index] = new Pane();
            setFriendPaneStyle(friendsPanes[index], positionY);

            ImageView messageImg = getMessageImage();
            ImageView statusImg = getStatusImage();

            Label friendNameLabel = getFriendNameLabel(friend);

            Circle friendAvatarCircle = getFriendAvatarCircle(friend.getId());
            friendsPanes[index].getChildren().add(friendAvatarCircle);

            addFriendPaneComponents(friendsPanes[index], new ImageView[]{messageImg, statusImg}, friendNameLabel);
            setMouseClickEvent(friendsPanes[index], friendNameLabel.getText(), messageImg, friend.getId());

            Button removeFriendButton = getRemoveButton(friend.getId());
            friendsPanes[index].getChildren().add(removeFriendButton);

            friendsListPane.getChildren().add(friendsPanes[index]);

            if (DatabaseQueries.isUnreadMessage(friend.getId(), clientApplication.getUserId())) {
                messageImg.setVisible(true);
            }

            if (positionY != 495) {
                positionY += 55;
            }
            ++index;
        }
    }

    private void setFriendPaneStyle(Pane friendPane, int y) {
        friendPane.setPrefSize(200, 55);
        friendPane.setLayoutY(y);
        friendPane.setStyle("-fx-border-color: #a2a3a2; -fx-border-width: 0 0 1 0;");
    }

    private ImageView getMessageImage() {
        ImageView image = new ImageView(images[0]);
        image.setLayoutX(168);
        image.setLayoutY(25);
        image.setId("messageImg");
        image.setVisible(false);
        return image;
    }

    private ImageView getStatusImage() {
        ImageView image = new ImageView(images[2]);
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

    private Circle getFriendAvatarCircle(int friendId) {
        Circle circle = new Circle(18);
        circle.setLayoutX(30);
        circle.setLayoutY(27);
        circle.setStyle("-fx-stroke: #efff3b;");

        int friendAvatarType = DatabaseQueries.getAvatarType(friendId);
        circle.setFill(new ImagePattern(images[friendAvatarType + 2]));

        return circle;
    }

    private void addFriendPaneComponents(Pane pane, ImageView[] images, Label friendNameLabel) {
        pane.getChildren().add(images[0]);
        pane.getChildren().add(images[1]);
        pane.getChildren().add(friendNameLabel);
    }

    private void setMouseClickEvent(Pane friendPane, String username, ImageView messageImageView, int friendId) {
        friendPane.setOnMouseClicked(t -> {
            if (currentInterlocutorId != friendId) {
                interlocutorLabel.setText(username);
                currentInterlocutorId = friendId;
                removeMessages();
                additionalHeight = 0;
                messagesPane.setPrefHeight(485);
                messageImageView.setVisible(false);
                messageTextArea.setDisable(false);

                updateInterlocutorImages();
                DatabaseQueries.updateMessages(friendId, clientApplication.getUserId());
                drawMessages(friendId);
            }
        });
    }

    private Button getRemoveButton(int friendId) {
        Button button = new Button();
        button.setText("x");
        button.setLayoutX(177);
        button.setLayoutY(0);
        button.setTextFill(Color.color(0.88, 0.52, 0.15));
        button.setBackground(Background.EMPTY);
        button.setOnAction(event -> handleDeletionAttempt(friendId));

        return button;
    }

    private void handleDeletionAttempt(int friendId) {
        if (getConfirmation(DatabaseQueries.getUsername(friendId))) {
            DatabaseQueries.removeFriendship(clientApplication.getUserId(), friendId);
            clientApplication.sendRedrawPanelCommand(friendId);
            clientApplication.updateFriendsList();
            Platform.runLater(this::initialize);
        }
    }

    private boolean getConfirmation(String username) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm");
        alert.setHeaderText(null);
        alert.setContentText("Do you want to remove the user " + username + " from friends?");

        return getAnswer(alert);
    }

    private void updateInterlocutorImages() {
        Circle interlocutorAvatarCircle = getInterlocutorAvatarCircle();
        interlocutorPane.getChildren().add(interlocutorAvatarCircle);

        ImageView interlocutorStatusImg = getInterlocutorStatusImageView();
        interlocutorPane.getChildren().add(interlocutorStatusImg);
    }

    private Circle getInterlocutorAvatarCircle() {
        Circle circle = new Circle(18);
        circle.setLayoutX(46);
        circle.setLayoutY(26);
        circle.setStyle("-fx-stroke: #8a1e1e;");

        int interlocutorAvatarType = DatabaseQueries.getAvatarType(currentInterlocutorId);
        circle.setFill(new ImagePattern(images[interlocutorAvatarType + 2]));

        return circle;
    }

    private ImageView getInterlocutorStatusImageView() {
        Image statusImage = (clientApplication.isFriendOnline(currentInterlocutorId) ? images[1] : images[2]);
        ImageView imageView = new ImageView(statusImage);
        imageView.setX(56);
        imageView.setY(35);
        imageView.setId("interlocutorStatusImg");

        return imageView;
    }

    private void drawMessages(int friendId) {
        Message[] messages = DatabaseQueries.getMessages(clientApplication.getUserId(), friendId);
        messages = getLastMessages(messages);
        for (Message message : messages) {
            drawMessageLabel(message.getMessage(), message.isReceived());
        }
    }

    private void drawMessageLabel(String message, boolean isReceived) {
        Label label = new Label();

        if (isTooLong(message)) {
            message = splitWithNewLine(message);
        }

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

        label.setLayoutY(10 + additionalHeight + currentMessagesCounter * 35);
        additionalHeight += (isTooLong(message) ? 28 : 0);
    }

    private void adjustMessagesPane() {
        messagesPane.setPrefHeight(20 + additionalHeight + currentMessagesCounter * 35);
    }
}
