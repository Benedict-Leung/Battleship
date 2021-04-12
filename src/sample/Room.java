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
        if (player1 == player) {
            player1Board = board;
        } else {
            player2Board = board;
        }

        if (numReady == 2) {
            this.init();
        }
    }

    public boolean fire(ClientConnectionHandler player, int x, int y) {
        System.out.println("Received hit request at " + x + " " + y);
        int[][] board = player == player1 ? player2Board : player1Board; // Opponent's board
        if (board[x][y] == -1) {
            System.out.println("miss");
            return false;
        } else {
            System.out.println("hit");
            return true;
        }
    }

    public void init() {
        sendAll("START");
        // Send each player the other player's board
        try {
            player1.send(player2Board);
            player2.send(player1Board);
        } catch (IOException e) {
            System.out.println("Failed to send a player a board: " + e.toString());
            return;
        }
    }

    public void disconnect() {
        sendAll("MESSAGE");
        sendAll("Opponent Disconnected");
    }
}
