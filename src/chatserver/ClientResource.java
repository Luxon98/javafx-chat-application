package chatserver;

import java.io.OutputStream;


class ClientResource {
    private int userId;
    private OutputStream outputStream;

    public ClientResource(int userId, OutputStream outputStream) {
        this.userId = userId;
        this.outputStream = outputStream;
    }

    public int getUserId() {
        return userId;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }
}
