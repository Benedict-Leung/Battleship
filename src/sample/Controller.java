package sample;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class Controller extends Thread {
    @FXML public Button connect;
    @FXML public Button exit;

    private Stage stage;
    private GridPane container = new GridPane();
    //private GridPane board = new GridPane();
    private Board board = new Board();
    //private int length = 5;
    //private String orientation = "horizontal";
    private Label messageLabel;
    private Label responseLabel;
    private ComboBox<String> comboBox = new ComboBox<>();
    private ArrayList<Integer> lengthOfShips = new ArrayList<>(Arrays.asList(5, 4, 3, 3, 2));

    private Board opponentBoard = new Board();
    private int[][] opponentShips;

    HBox options = new HBox();

    private Socket socket = null;
    private ObjectOutputStream networkOut = null;
    private ObjectInputStream networkIn = null;

    public static String SERVER_ADDRESS = "localhost";
    public static int SERVER_PORT = 16789;

    public Controller() {
        super();
    }

    public void init(Stage primaryStage) {
        this.stage = primaryStage;

        connect.setOnMouseClicked(event -> {
            connect();
            container.getChildren().add(new Label("Waiting for opponent..."));
            container.setAlignment(Pos.CENTER);
            Scene scene = new Scene(container, 700, 700);
            scene.getStylesheets().add("sample/board.css");
            stage.setScene(scene);
            this.start();
        });

        exit.setOnMouseClicked(event -> System.exit(0));
    }

    public void run() {
        while (true) {
            try {
                String command = (String) networkIn.readObject();

                if (command.equalsIgnoreCase("INIT")) {
                    init();
                } else if (command.equalsIgnoreCase("MESSAGE")) {
                    String message = (String) networkIn.readObject();

                    Platform.runLater(() -> {
                        messageLabel.setText(message);
                    });
                } else if (command.equalsIgnoreCase("START")) {
                    // Both players are now ready
                    opponentShips = (int[][])networkIn.readObject();
                    opponentBoard.setShowSingleSelection(true);
                }
                System.out.println(command);

            } catch (IOException | ClassNotFoundException e) {
                this.disconnect();
                System.out.println("Socket closed");
                break;
            }
        }
    }

    public void init() {
        Platform.runLater(() -> {
            container.getChildren().removeAll(container.getChildren());
            container.setHgap(10);
            container.setVgap(10);
            resetBoard();

            comboBox.setOnAction((event) -> {
                int index = comboBox.getSelectionModel().getSelectedIndex();
                if (index != -1) {
                    board.setSelectedShip(0);
                } else {
                    board.setSelectedShip(-1);
                }
            });

            Button rotate = new Button("Rotate");
            //rotate.setOnMouseClicked(e -> orientation = (orientation.equalsIgnoreCase("horizontal")) ? "vertical" : "horizontal");
            rotate.setOnMouseClicked(e -> board.toggleShipPlacementOrientation());

            Button resetBoard = new Button("Reset Board");
            resetBoard.setOnMouseClicked(e -> resetBoard());

            Button ready = new Button("Ready");
            ready.setOnMouseClicked(e -> ready());

            messageLabel = new Label("Place your ships");
            responseLabel = new Label("Waiting for opponent..."); // Used for letting the player know what is going on

            //HBox options = new HBox();
            options.getChildren().add(comboBox);
            options.getChildren().add(rotate);
            options.getChildren().add(resetBoard);
            options.getChildren().add(ready);
            options.setAlignment(Pos.CENTER);
            options.setSpacing(10);

            container.add(messageLabel, 0, 0);
            GridPane.setHalignment(messageLabel, HPos.CENTER);
            container.add(responseLabel, 0, 3);

            container.add(options, 0, 1);
            container.add(board.getGridPane(), 0, 2);
        });
    }

    /**
     * After player clicks ready prepares for the game by showing both their board and opponents
     */
    public void showBothBoards(){
        System.out.println("Both boards test!");

        stage.setWidth(1150);
        container.getChildren().remove(options);

        board.setShowShipSelection(false);
        opponentBoard.reset();

        opponentBoard.addButtonEventHandler(MouseEvent.MOUSE_CLICKED, e -> fireMissile());

        Label opponentsMessageLabel = new Label("Opponents Ships");
        GridPane.setHalignment(opponentsMessageLabel, HPos.CENTER);
        container.add(opponentsMessageLabel, 1, 0);

        container.add(opponentBoard.getGridPane(), 1, 2);
        messageLabel.setText("Your Ships");
    }

    /**
     * Fire a missile at a selected node
     */
    // TODO: This is an unfinished method (this is based off of placeShip but for the intention of firing a missile)
    public void fireMissile() {
        if (!opponentBoard.getShowSingleSelection()) {
            // Don't do anything if there is no selection, because both players are not ready
            return;
        }

        // TODO: Check if it's this player's turn

        ArrayList<Node> selectedNodes = new ArrayList<>();

        for (Node node : opponentBoard.getChildren()) {
            if (node.getStyleClass().contains("selected")) {
                if (!node.getStyleClass().contains("missile")) {
                    selectedNodes.add(node);
                } else {
                    responseLabel.setText("You've already fired a missile!");
                    return;
                }
            }
        }

        // TODO: This can be likely be removed later based on implementation.
        if(selectedNodes.size() != 1){
            responseLabel.setText("You must only select one place to fire a missile!");
        }

        // TODO: Communicate with server to determine if missile hit a ship or not (hitStatus = "hit" if yes, or "miss" if no)
        Node node = selectedNodes.get(0);
        int x = GridPane.getColumnIndex(node);
        int y = GridPane.getRowIndex(node);
        String hitStatus = "";
        try {
            networkOut.writeObject("FIRE");
            networkOut.writeObject(x);
            networkOut.writeObject(y);
            hitStatus = (String)networkIn.readObject();
            System.out.println(hitStatus);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Exception when firing missile: " + e.toString());
            return;
        }
        opponentBoard.placeMissileAtNode(node, hitStatus);
    }

    public void resetBoard() {
        board.reset();
        board.setShowShipSelection(true);
        board.addButtonEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            boolean result = board.placeShipAtSelection();
            if (result) {
                // Ship was successfully placed
                lengthOfShips.remove(comboBox.getSelectionModel().getSelectedIndex());
                comboBox.getItems().remove(comboBox.getSelectionModel().getSelectedItem());
                if (comboBox.getItems().size() > 0) {
                    // There are more ships we can place
                    comboBox.getSelectionModel().selectNext();
                } else {
                    // There are no more ships we can place
                    board.setShowShipSelection(false);
                }
                messageLabel.setText("Place your ships");
            } else {
                messageLabel.setText("Space is already occupied");
            }
        });

        lengthOfShips = new ArrayList<>(Arrays.asList(5, 4, 3, 3, 2));
        comboBox.getItems().removeAll(comboBox.getItems());
        comboBox.getItems().add("Aircraft Carrier");
        comboBox.getItems().add("Battleship");
        comboBox.getItems().add("Submarine");
        comboBox.getItems().add("Cruiser");
        comboBox.getItems().add("Destroyer");
        comboBox.getSelectionModel().select(0);
    }

    public void ready() {
        if (comboBox.getItems().size() == 0) {
            try {
                int[][] parseBoard = board.getIntArray();
                networkOut.writeObject("READY");
                networkOut.writeObject(parseBoard);
                showBothBoards();
            } catch (IOException e) {
                System.out.println("IOException when ready");
            }
        } else {
            messageLabel.setText(comboBox.getItems().size() + " more ships need to placed");
        }

    }

    public void connect() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + SERVER_ADDRESS);
        } catch (IOException e) {
            System.err.println("IOException while connecting to server: " + SERVER_ADDRESS);
        }

        if (socket == null) {
            System.err.println("Socket is null");
        }

        try {
            networkOut = new ObjectOutputStream(socket.getOutputStream());
            networkIn = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("IOException while opening a read/write connection");
        }
    }

    public void disconnect() {
        try {
            if (networkIn != null && networkOut != null) {
                networkIn.close();
                networkOut.close();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
