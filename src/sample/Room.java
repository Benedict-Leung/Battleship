package sample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Room {
    private ClientConnectionHandler player1;
    private ClientConnectionHandler player2;
    private int[][] player1Board;
    private int[][] player2Board;
    private int numReady = 0;
    private int playersTurn = 1;
    private ArrayList<String> shipNames = new ArrayList<>(Arrays.asList("Aircraft Carrier", "Battleship", "Submarine", "Cruiser", "Destroyer"));

    public Room(ClientConnectionHandler player1, ClientConnectionHandler player2) throws IOException {
        super();
        this.player1 = player1;
        this.player2 = player2;
        this.player1.setRoom(this);
        this.player2.setRoom(this);
        sendAll("INIT");
    }

    private void sendAll(Object object) {
        player1.send(object);
        player2.send(object);
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

    public void fire(ClientConnectionHandler player, int x, int y) {
        int[][] board = player == player1 ? player2Board : player1Board; // Opponent's board
        ClientConnectionHandler opponent = (player == player1) ? player2 : player1;

        if (board[x][y] == -1) {
            sendMessage((playersTurn == 1) ? player1 : player2, "You missed! \nYour opponent's turn");
            sendMessage((playersTurn == 1) ? player2 : player1, "Your opponent missed!\nYour turn");

            player.send("HITSTATUS " + x + " " + y);
            player.send("miss");
            opponent.send("UPDATE " + x + " " + y);
            opponent.send("miss");
        } else {
            int shipValue = board[x][y];
            boolean exists = false;
            board[x][y] = -2;

            if (checkWin(board)) {
                sendMessage((playersTurn == 1) ? player1 : player2, "You destroyed all their ships! \nYou win!");
                sendMessage((playersTurn == 1) ? player2 : player1, "Your opponent hit all your ships! \nYou lose...");
                sendAll("FINISH");
            } else {
                for (int[] row : board)
                    for (int cell : row)
                        if (cell == shipValue) {
                            exists = true;
                            break;
                        }

                if (exists) {
                    sendMessage((playersTurn == 1) ? player1 : player2, "You hit a ship! \nYour opponent's turn");
                    sendMessage((playersTurn == 1) ? player2 : player1, "Your opponent hit your " + shipNames.get(shipValue) + "! \nYour turn");
                } else {
                    sendMessage((playersTurn == 1) ? player1 : player2, "You destroyed their " + shipNames.get(shipValue) + "! \nYour opponent's turn");
                    sendMessage((playersTurn == 1) ? player2 : player1, "Your opponent destroyed your " + shipNames.get(shipValue) + "! \nYour turn");
                }
            }
            player.send("HITSTATUS " + x + " " + y);
            player.send("hit");
            opponent.send("UPDATE " + x + " " + y);
            opponent.send("hit");
        }
        playersTurn = (playersTurn == 1) ? 2 : 1;
    }

    public boolean checkWin(int[][] board) {
        for (int[] row : board)
            for (int cell : row)
                if (cell >= 0)
                    return false;
        return true;
    }

    public void init() {
        sendAll("START");
        // Send each player the other player's board
        player1.send(player2Board);
        player2.send(player1Board);
        sendMessage(player1, "Your turn");
        sendMessage(player2, "Opponent's turn");
    }

    public void sendMessage(ClientConnectionHandler player, String message) {
        player.send("MESSAGE");
        player.send(message);
    }

    public boolean getIfPlayerTurn(ClientConnectionHandler player) {
        return (player == player1 && playersTurn == 1 || player == player2 && playersTurn == 2);
    }

    public void disconnect() {
        sendAll("MESSAGE");
        sendAll("Opponent Disconnected");
    }
}
