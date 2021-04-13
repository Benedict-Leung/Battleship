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

    /**
     * Constructor of room
     *
     * @param player1 Player 1's ClientConnectionHandler
     * @param player2 Player 2's ClientConnectionHandler
     *
     * @throws IOException IOException when initializing
     */
    public Room(ClientConnectionHandler player1, ClientConnectionHandler player2) throws IOException {
        super();
        this.player1 = player1;
        this.player2 = player2;
        this.player1.setRoom(this);
        this.player2.setRoom(this);
        sendAll("INIT");
    }

    /**
     * Send info to all players
     *
     * @param object The object to send
     */
    private void sendAll(Object object) {
        player1.send(object);
        player2.send(object);
    }

    /**
     * Set player's board and starts game if two players are ready
     *
     * @param player The player's ClientConnectionHandler
     * @param board Player's board
     */
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

    /**
     * Parse fire command based on board state and fire coordinates
     *
     * @param player The player's ClientConnectionHandler
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void fire(ClientConnectionHandler player, int x, int y) {
        int[][] board = player == player1 ? player2Board : player1Board; // Opponent's board
        ClientConnectionHandler opponent = (player == player1) ? player2 : player1; // Opponent's board

        // Send miss message to all players if there are no ships hit. Otherwise, send hit message
        if (board[x][y] == -1) {
            // Send miss message
            sendMessage((playersTurn == 1) ? player1 : player2, "You missed! \nYour opponent's turn");
            sendMessage((playersTurn == 1) ? player2 : player1, "Your opponent missed!\nYour turn");

            // Update boards
            player.send("HITSTATUS " + x + " " + y);
            player.send("miss");
            opponent.send("UPDATE " + x + " " + y);
            opponent.send("miss");
        } else {
            int shipValue = board[x][y];
            boolean exists = false;
            board[x][y] = -2;

            // Check if there are still ships
            if (checkWin(board)) {
                // Send game over message
                sendMessage((playersTurn == 1) ? player1 : player2, "You destroyed all their ships! \nYou win!");
                sendMessage((playersTurn == 1) ? player2 : player1, "Your opponent hit all your ships! \nYou lose...");
                sendAll("FINISH");
            } else {
                // Checks if the ship is destroyed
                for (int[] row : board)
                    for (int cell : row)
                        if (cell == shipValue) {
                            exists = true;
                            break;
                        }

                // Send the appropriate message if ship is destroyed or not
                if (exists) {
                    sendMessage((playersTurn == 1) ? player1 : player2, "You hit a ship! \nYour opponent's turn");
                    sendMessage((playersTurn == 1) ? player2 : player1, "Your opponent hit your " + shipNames.get(shipValue) + "! \nYour turn");
                } else {
                    sendMessage((playersTurn == 1) ? player1 : player2, "You destroyed their " + shipNames.get(shipValue) + "! \nYour opponent's turn");
                    sendMessage((playersTurn == 1) ? player2 : player1, "Your opponent destroyed your " + shipNames.get(shipValue) + "! \nYour turn");
                }
            }
            // Update boards
            player.send("HITSTATUS " + x + " " + y);
            player.send("hit");
            opponent.send("UPDATE " + x + " " + y);
            opponent.send("hit");
        }
        // Change player's turn
        playersTurn = (playersTurn == 1) ? 2 : 1;
    }

    /**
     * Check if board has no ships
     *
     * @param board The player's board
     * @return If board has no ships
     */
    public boolean checkWin(int[][] board) {
        for (int[] row : board)
            for (int cell : row)
                if (cell >= 0)
                    return false;
        return true;
    }

    /**
     * Initializes game
     */
    public void init() {
        sendAll("START");
        // Send each player the other player's board
        sendMessage(player1, "Your turn");
        sendMessage(player2, "Opponent's turn");
    }

    /**
     * Send command to player
     *
     * @param player The player's ClientConnectionHandler
     * @param message The message to send
     */
    public void sendMessage(ClientConnectionHandler player, String message) {
        player.send("MESSAGE");
        player.send(message);
    }

    /**
     * Return if it is the player's turn
     *
     * @param player The player's ClientConnectionHandler
     * @return If it is the player's turn
     */
    public boolean getIfPlayerTurn(ClientConnectionHandler player) {
        return (player == player1 && playersTurn == 1 || player == player2 && playersTurn == 2);
    }

    /**
     * Send message that opponent has disconnected
     */
    public void disconnect() {
        sendAll("MESSAGE");
        sendAll("Opponent Disconnected");
    }
}
