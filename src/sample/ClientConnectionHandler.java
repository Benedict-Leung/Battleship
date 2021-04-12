package sample;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientConnectionHandler extends Thread {
    private Room room;
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
        while (true) {
            try {
                String command = String.valueOf(in.readObject());

                if (command.equalsIgnoreCase("READY")) {
                    int[][] board = (int[][]) in.readObject();
                    room.ready(this, board);
                } else if (command.equalsIgnoreCase("FIRE")) {
                    if (room.getIfPlayerTurn(this)) {
                        int x = (int)in.readObject();
                        int y = (int)in.readObject();
                        room.fire(this, x, y);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                this.disconnect();
                break;
            }
        }
    }

    public void send(Object object) {
        try {
            out.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void disconnect() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            room.disconnect();
        }
    }

}
