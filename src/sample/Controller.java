package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;

public class Controller extends Thread {
   @FXML public GridPane container;
   @FXML public Button connect;
   @FXML public Button exit;

    private Socket socket = null;
    private ObjectOutputStream networkOut = null;
    private ObjectInputStream networkIn = null;

    public static String SERVER_ADDRESS = "localhost";
    public static int SERVER_PORT = 16789;

    public Controller() {
        super();
    }

    public void init() {
        connect.setOnMouseClicked(event -> {
            connect();
            container.getChildren().removeAll(container.getChildren());
            container.getChildren().add(new Label("Waiting for opponent..."));
            this.start();
        });

        exit.setOnMouseClicked(event -> System.exit(0));
    }

    public void run() {
        try {
            System.out.println("Listening");
            String command = (String) networkIn.readObject();

            if (command.equalsIgnoreCase("INIT")) {
                Platform.runLater(() -> {
                    container.getChildren().removeAll(container.getChildren());
                    GridPane grid = new GridPane();

                    for (int i = 0; i < 10; i++) {
                        for (int j = 0; j < 10; j++) {
                            Button button = new Button();
                            button.setPrefSize(50, 50);
                            button.setMaxSize(50, 50);
                            button.setMinSize(50, 50);
                            grid.add(button, i, j);
                        }
                    }
                    container.getChildren().setAll(grid);
                });
            }
            System.out.println(command);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
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
}
