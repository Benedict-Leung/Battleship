package sample;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnectionHandler extends Thread {
    protected Socket socket;
    protected ObjectOutputStream out = null;
    protected ObjectInputStream in = null;

    public ClientConnectionHandler(Socket socket) {
        super();
        this.socket = socket;

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("IOException while opening a read/write connection");
        }
        this.start();
    }

    public void run() {
    }

    public void send(Object object) throws IOException {
        out.writeObject(object);
    }
}
