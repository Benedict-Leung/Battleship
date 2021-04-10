package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
    private GridPane board = new GridPane();
    private int length = 5;
    private String orientation = "horizontal";
    private Label messageLabel;
    private ComboBox<String> comboBox = new ComboBox<>();
    private ArrayList<Integer> lengthOfShips = new ArrayList<>(Arrays.asList(5, 4, 3, 3, 2));

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
                    length = lengthOfShips.get(index);
                } else {
                    length = 0;
                }
            });

            Button rotate = new Button("Rotate");
            rotate.setOnMouseClicked(e -> orientation = (orientation.equalsIgnoreCase("horizontal")) ? "vertical" : "horizontal");

            Button resetBoard = new Button("Reset Board");
            resetBoard.setOnMouseClicked(e -> resetBoard());

            Button ready = new Button("Ready");
            ready.setOnMouseClicked(e -> ready());

            messageLabel = new Label("Place your ships");

            HBox options = new HBox();
            options.getChildren().add(comboBox);
            options.getChildren().add(rotate);
            options.getChildren().add(resetBoard);
            options.getChildren().add(ready);
            options.setAlignment(Pos.CENTER);
            options.setSpacing(10);

            container.add(messageLabel, 0, 0);
            GridPane.setHalignment(messageLabel, HPos.CENTER);
            container.add(options, 0, 1);
            container.add(board, 0, 2);
        });
    }

    public void displayShip(Button source) {
        clearBoard();
        int sourceRow = GridPane.getRowIndex(source);
        int sourceCol = GridPane.getColumnIndex(source);

        if (orientation.equalsIgnoreCase("horizontal")) {
            if (sourceCol < length / 2) {
                sourceCol += length / 2 - sourceCol;
            } else if (9 - sourceCol < length / 2) {
                sourceCol -= Math.ceil((double) length / 2) - (10 - sourceCol);
            }
        } else {
            if (sourceRow < length / 2) {
                sourceRow += length / 2 - sourceRow;
            } else if (9 - sourceRow < length / 2) {
                sourceRow -= Math.ceil((double) length / 2) - (10 - sourceRow);
            }
        }

        for (int k = 0; k < length; k++) {
            for (Node node : board.getChildren()) {
                if (!node.getStyleClass().contains("selected")) {
                    if (orientation.equalsIgnoreCase("horizontal")) {
                        if (GridPane.getColumnIndex(node) == sourceCol - Math.ceil((double) k / 2) && GridPane.getRowIndex(node) == sourceRow
                                || GridPane.getColumnIndex(node) == sourceCol + Math.ceil((double) k / 2) && GridPane.getRowIndex(node) == sourceRow) {
                            node.getStyleClass().add("selected");
                            break;
                        }
                    } else {
                        if (GridPane.getRowIndex(node) == sourceRow - Math.ceil((double) k / 2) && GridPane.getColumnIndex(node) == sourceCol
                                || GridPane.getRowIndex(node) == sourceRow + Math.ceil((double) k / 2) && GridPane.getColumnIndex(node) == sourceCol) {
                            node.getStyleClass().add("selected");
                            break;
                        }
                    }
                }
            }
        }
    }

    public void placeShip() {
        ArrayList<Node> selectedNodes = new ArrayList<>();

        for (Node node : board.getChildren()) {
            if (node.getStyleClass().contains("selected")) {
                if (!node.getStyleClass().contains("ship")) {
                    selectedNodes.add(node);
                } else {
                    messageLabel.setText("Space is already occupied");
                    return;
                }
            }
        }
        String shipName = comboBox.getValue().toLowerCase().replaceAll("\s", "");

        for (Node node : selectedNodes) {
            node.getStyleClass().add("ship");
            node.getStyleClass().add(shipName);
        }
        lengthOfShips.remove(comboBox.getSelectionModel().getSelectedIndex());
        comboBox.getItems().remove(comboBox.getSelectionModel().getSelectedItem());
        comboBox.getSelectionModel().selectNext();
    }

    public void clearBoard() {
        for (Node node : board.getChildren())
            node.getStyleClass().remove("selected");
    }

    public void resetBoard() {
        board.getChildren().removeAll(board.getChildren());

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Button button = new Button();
                button.setPrefSize(50, 50);
                button.setMaxSize(50, 50);
                button.setMinSize(50, 50);
                button.getStyleClass().add("board");
                button.setOnMouseEntered(e -> displayShip((Button) e.getSource()));
                button.setOnMouseExited(e -> clearBoard());
                button.setOnMouseClicked(e -> placeShip());

                if (j == 0)
                    button.getStyleClass().add("firstRow");
                if (i == 0)
                    button.getStyleClass().add("firstColumn");
                if (j == 9)
                    button.getStyleClass().add("lastRow");
                if (i == 9)
                    button.getStyleClass().add("lastColumn");
                board.add(button, i, j);
            }
        }
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
        ArrayList<String> shipNames = new ArrayList<>(Arrays.asList("aircraftcarrier", "battleship", "submarine", "cruiser", "destroyer"));
        if (comboBox.getItems().size() == 0) {
            try {
                int[][] parseBoard = new int[10][10];
                for (Node node : board.getChildren()) {
                    if (node.getStyleClass().contains("ship")) {
                        for (String style : node.getStyleClass()) {
                            if (shipNames.contains(style)) {
                                parseBoard[GridPane.getRowIndex(node)][GridPane.getColumnIndex(node)] = shipNames.indexOf(style);
                            }
                        }
                    } else {
                        parseBoard[GridPane.getRowIndex(node)][GridPane.getColumnIndex(node)] = -1;
                    }
                }
                networkOut.writeObject("READY");
                networkOut.writeObject(parseBoard);
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
