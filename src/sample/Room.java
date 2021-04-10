package sample;

import java.io.IOException;
import java.util.Arrays;

public class Room {
    private ClientConnectionHandler player1;
    private ClientConnectionHandler player2;
    private int[][] player1Board;
    private int[][] player2Board;
    private int numReady = 0;

    public Room(ClientConnectionHandler player1, ClientConnectionHandler player2) throws IOException {
        super();
        this.player1 = player1;
        this.player2 = player2;
        this.player1.setRoom(this);
        this.player2.setRoom(this);
        sendAll("INIT");
    }

    private void sendAll(Object object) {
        try {
            player1.send(object);
        } catch (IOException e) {
            System.out.println("Player 1 disconnected");
        }

        try {
            player2.send(object);
        } catch (IOException e) {
            System.out.println("Player 2 disconnected");
        }
    }

    public void ready(ClientConnectionHandler player, int[][] board) {
        numReady++;
        System.out.println(Arrays.deepToString(board));
        if (player1 == player) {
            player1Board = board;
        } else {
            player2Board = board;
        }

        if (numReady == 2) {
            this.init();
        }
    }

    public void init() {
        sendAll("START");
    }

    public void disconnect() {
        sendAll("MESSAGE");
        sendAll("Opponent Disconnected");
    }
}
