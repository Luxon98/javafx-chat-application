package chatclient;

public class ChatUtility {

    static public boolean isTooLong(String message) {
        return (message.length() > 30);
    }

    static boolean isTooManyMessages(int messagesNumber) {
        return (messagesNumber >= 13);
    }

    static String splitWithNewLine(String message) {
        message = message.substring(0, 30) + "\n" + message.substring(30);
        return message;
    }

    static String cutNewLineCharacter(String message) {
        message = message.substring(0, message.length() - 1);
        return message;
    }

    static boolean isProperIndex(int index) {
        return !(index < 0);
    }
}
