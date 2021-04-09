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
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Controller extends Thread {
   @FXML public Button connect;
   @FXML public Button exit;

   private Stage stage;
   GridPane container = new GridPane();
   private GridPane board = null;
   private int length = 5;


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
        try {
            System.out.println("Listening");
            while (true) {
                String command = (String) networkIn.readObject();

                if (command.equalsIgnoreCase("INIT")) {
                    init();
                }
                System.out.println(command);
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Socket closed");
        }
    }

    public void init() {
        Platform.runLater(() -> {
            ComboBox<String> comboBox = new ComboBox<>();
            int[] lengthOfShips = {5, 4, 3, 3, 2};

            comboBox.getItems().add("Aircraft Carrier");
            comboBox.getItems().add("Battleship");
            comboBox.getItems().add("Submarine");
            comboBox.getItems().add("Cruiser");
            comboBox.getItems().add("Destroyer");

            comboBox.setOnAction((event) -> {
                System.out.println(comboBox.getSelectionModel().getSelectedIndex());
                length = lengthOfShips[comboBox.getSelectionModel().getSelectedIndex()];
            });
            comboBox.getSelectionModel().select(0);

            board = new GridPane();

            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    Button button = new Button();
                    button.setPrefSize(50, 50);
                    button.setMaxSize(50, 50);
                    button.setMinSize(50, 50);
                    button.setOnMouseEntered(e -> displayShip((Button) e.getSource()));

                    if (i == 0) {
                        button.getStyleClass().add("firstRow");
                    }

                    if (j == 0) {
                        button.getStyleClass().add("firstColumn");
                    }
                    board.add(button, i, j);
                }
            }
            container.add(comboBox, 0, 0);
            container.add(board, 0, 1);
        });
    }

    public void displayShip(Button source) {
        clearBoard();
        source.getStyleClass().add("selected");
        int sourceRow = GridPane.getRowIndex(source);
        int sourceCol = GridPane.getColumnIndex(source);

        for (int k = 1; k < length; k++) {
            for (Node node : board.getChildren()) {
                if (!node.getStyleClass().contains("selected")) {
                    if (GridPane.getColumnIndex(node) == sourceCol - Math.ceil((double) k / 2) && GridPane.getRowIndex(node) == sourceRow) {
                        node.getStyleClass().add("selected");
                        break;
                    } else if (GridPane.getColumnIndex(node) == sourceCol + Math.ceil((double) k / 2) && GridPane.getRowIndex(node) == sourceRow) {
                        node.getStyleClass().add("selected");
                        break;
                    }
                }
            }
        }
    }

    public void clearBoard() {
        for (Node node : board.getChildren()) {
            node.getStyleClass().remove("selected");
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
