package chatapplication.client;

import java.util.ArrayList;
import java.util.List;


class ChatUtility {

    public static boolean isTooLong(String message) {
        return (message.length() > 30);
    }

    public static boolean isTooManyMessages(int messagesNumber) {
        return (messagesNumber >= 13);
    }

    public static String splitWithNewLine(String message) {
        int index = message.indexOf(" ", 29);
        if (index == -1) {
            index = message.indexOf(" ", 19);
        }

        if (index != -1) {
            message = message.substring(0, index) + "\n" + message.substring(index);
        }
        else {
            message = message.substring(0, 30);
        }

        return message;
    }

    public static String cutNewLineCharacter(String message) {
        message = message.substring(0, message.length() - 1);
        return message;
    }

    public static boolean isProperIndex(int index) {
        return !(index < 0);
    }

    public static boolean isProperInterlocutorId(int id) {
        return (id != -1);
    }

    public static Message[] getLastMessages(Message[] messages) {
        if (messages.length <= 20) {
            return messages;
        }
        else {
            Message[] lastMessages = new Message[20];
            for (int i = messages.length - 20, j = 0; i < messages.length; ++i, ++j) {
                lastMessages[j] = messages[i];
            }
            return lastMessages;
        }
    }

    public static List<String> getAvatarsList() {
        List<String> choices = new ArrayList<>();
        choices.add("1");
        choices.add("2");
        choices.add("3");
        choices.add("4");

        return choices;
    }

    // This project is written for learning purposes.
    // I don't even send a confirmation e-mail anywhere (I only store it in the database),
    // so the method to check the correctness of the e-mail address is very simple.
    // In a professional project it is better to use one of the proven and popular libraries for this.
    public static boolean isProperEmailAddress(String emailAddress) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return emailAddress.matches(regex);
    }

    // For the above reasons, also checking the password is unsophisticated - we only
    // require 8 characters including a number and a capital and a small letter.
    public static boolean isSecurePassword(String password) {
        String regex = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}";
        return password.matches(regex);
    }
}
