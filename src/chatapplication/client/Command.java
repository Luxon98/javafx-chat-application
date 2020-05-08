package chatapplication.client;


enum Command {
    CONNECT(1),
    MESSAGE(2),
    DISCONNECT(3),
    FRIENDS_STATUSES(4),
    INVITATION(5),
    REDRAW_PANEL(6);

    int commandNumber;
    private static Command[] integerValues = values();

    private Command(int commandNumber) {
        this.commandNumber = commandNumber;
    }

    public int getCommandNumber() {
        return commandNumber;
    }

    public static Command fromInteger(int number) {
        if (number > 0 && number <= 6) {
            return integerValues[number - 1];
        }
        return null;
    }
}