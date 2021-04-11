package sample;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.Arrays;

public class Board {
    private final GridPane gridPane;

    public static final int BOARD_SIZE = 10;
    public static final ArrayList<String> SHIP_NAMES = new ArrayList<>(Arrays.asList("Aircraft Carrier", "Battleship", "Submarine", "Cruiser", "Destroyer"));
    public static final ArrayList<String> SHIP_STYLES = new ArrayList<>(Arrays.asList("aircraftcarrier", "battleship", "submarine", "cruiser", "destroyer"));
    public static final ArrayList<Integer> SHIP_LENGTHS = new ArrayList<>(Arrays.asList(5, 4, 3, 3, 2));

    public Board() {
        gridPane = new GridPane();
    }

    public GridPane getGridPane() {
        return gridPane;
    }

    public ObservableList<Node> getChildren() {
        return gridPane.getChildren();
    }

    public Node getChildAtCoord(int x, int y) {
        if (!isInBounds(x, y))
            return null;
        return gridPane.getChildren().get(x + (y * BOARD_SIZE));
    }

    /**
     * Creates a ship on a Node at the specified coordinates (origin top-left).
     * @param x
     * @param y
     * @param shipName
     */
    public void placeShipAtCoord(int x, int y, String shipName) {
        Node node = getChildAtCoord(x, y);
        if (node == null) {
            // Bounds check failed in getChildAtCoord()
            return;
        }
        placeShipAtNode(node, shipName);
    }

    /**
     * Creates a ship on a Node which is a child of this Board.
     * @param node A child of the GridPane belonging to this Board.
     * @param shipName
     */
    public void placeShipAtNode(Node node, String shipName) {
        node.getStyleClass().add("ship");
        node.getStyleClass().add(shipName);
    }

    public boolean isInBounds(int x, int y) {
        if (x < 0 || x >= BOARD_SIZE || y < 0 || x >= BOARD_SIZE) {
            System.out.println("Attempted to access invalid board coordinate: " + String.valueOf(x) + " " + String.valueOf(y));
            return false;
        }
        return true;
    }

    /**
     * Removes all selections from this board.
     */
    public void clear() {
        for (Node node : gridPane.getChildren())
            node.getStyleClass().remove("selected");
    }

    /**
     * Re-creates all buttons for this Board.
     */
    public void reset() {
        gridPane.getChildren().removeAll(gridPane.getChildren());

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Button button = new Button();
                button.setPrefSize(50, 50);
                button.setMaxSize(50, 50);
                button.setMinSize(50, 50);
                button.getStyleClass().add("board");

                // These are now set via addButtonEventHandler
                //button.setOnMouseEntered(e -> displayShip((Button) e.getSource()));
                //button.setOnMouseExited(e -> clearBoard());
                //button.setOnMouseClicked(e -> placeShip());

                if (j == 0)
                    button.getStyleClass().add("firstRow");
                if (i == 0)
                    button.getStyleClass().add("firstColumn");
                if (j == 9)
                    button.getStyleClass().add("lastRow");
                if (i == 9)
                    button.getStyleClass().add("lastColumn");
                gridPane.add(button, i, j);
            }
        }
    }

    public <T extends Event> void addButtonEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        for (Node node : gridPane.getChildren()) {
            Button button = (Button)node;
            button.addEventHandler(eventType, eventHandler);
        }
    }

    /**
     * Converts this Board into an integer array to be sent over the network
     * @return
     */
    public int[][] getIntArray() {
        int[][] parsed = new int[BOARD_SIZE][BOARD_SIZE];
        for (Node node : gridPane.getChildren()) {
            if (node.getStyleClass().contains("ship")) {
                for (String style : node.getStyleClass()) {
                    if (SHIP_STYLES.contains(style)) {
                        parsed[GridPane.getRowIndex(node)][GridPane.getColumnIndex(node)] = SHIP_STYLES.indexOf(style);
                    }
                }
            } else {
                parsed[GridPane.getRowIndex(node)][GridPane.getColumnIndex(node)] = -1;
            }
        }
        return parsed;
    }

    /**
     * Creates a Board instance representing the int array provided
     * @param intArray
     * @return
     */
    public static Board fromIntArray(int[][] intArray) {
        Board board = new Board();
        board.reset();

        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                int shipType = intArray[x][y];
                if (shipType != -1) {
                    // If there is a ship here
                    board.placeShipAtCoord(x, y, SHIP_STYLES.get(shipType));
                }
            }
        }

        return board;
    }
}
