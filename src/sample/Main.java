package sample;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("menu.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        primaryStage.setScene(new Scene(root, 700, 700));
        primaryStage.setTitle("Battleship");
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> System.exit(0));

        controller.init();
    }

    public static void main(String[] args) {
        launch(args);
    }
}