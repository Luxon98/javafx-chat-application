package chatserver;

import java.io.OutputStream;


public class ClientResource {
    private int userId;
    private OutputStream outputStream;

    public ClientResource(int userId, OutputStream outputStream) {
        this.userId = userId;
        this.outputStream = outputStream;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }
}
