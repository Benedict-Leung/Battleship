package sample;

import java.io.IOException;

public class Room {
    private ClientConnectionHandler player1;
    private ClientConnectionHandler player2;

    public Room(ClientConnectionHandler player1, ClientConnectionHandler player2) throws IOException {
        super();
        this.player1 = player1;
        this.player2 = player2;
        sendAll("INIT");
    }

    private void sendAll(Object object) throws IOException {
        player1.send(object);
        player2.send(object);
    }
}
