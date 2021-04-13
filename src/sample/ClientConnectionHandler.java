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

    /**
     * Constructor of the ClientConnectionHandler and initializes streams
     *
     * @param socket The client's socket
     */
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

    /**
     * Parse incoming commands
     */
    public void run() {
        while (true) {
            try {
                String command = String.valueOf(in.readObject());

                if (command.equalsIgnoreCase("READY")) { // Tells room player is ready
                    int[][] board = (int[][]) in.readObject();
                    room.ready(this, board);
                } else if (command.equalsIgnoreCase("FIRE")) { // Send fire coordinates to room
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

    /**
     * Send commands to player
     *
     * @param object The object to send to player
     */
    public void send(Object object) {
        try {
            out.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the room
     *
     * @param room The room
     */
    public void setRoom(Room room) {
        this.room = room;
    }

    /**
     * Closes streams and socket
     */
    public void disconnect() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (room != null) {
                room.disconnect();
            }
        }
    }

}
