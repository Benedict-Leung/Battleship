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
    // If true, all buttons which are moused over will be highlighted
    private boolean showSingleSelection = false;
    // If true and a ship is selected, then all tiles where this ship would be placed will be highlighted
    private boolean showShipSelection = false;
    // Whether ships that are placed should be done horizontally or vertically
    private String shipPlacementOrientation = "horizontal";
    // The current ship type that should be placed
    private int selectedShip = 0;

    public static final int BOARD_SIZE = 10;
    public static final ArrayList<String> SHIP_STYLES = new ArrayList<>(Arrays.asList("aircraftcarrier", "battleship", "submarine", "cruiser", "destroyer"));
    public static final ArrayList<Integer> SHIP_LENGTHS = new ArrayList<>(Arrays.asList(5, 4, 3, 3, 2));

    /**
     * Constructor of board
     */
    public Board() {
        gridPane = new GridPane();
    }

    /**
     * Return grid pane
     * @return The grid pane
     */
    public GridPane getGridPane() {
        return gridPane;
    }

    /**
     * Gets all the children of the grid pane
     *
     * @return All the children of the grid pane
     */
    public ObservableList<Node> getChildren() {
        return gridPane.getChildren();
    }

    /**
     * Creates a ship on a Node which is a child of this Board.
     *
     * @param node A child of the GridPane belonging to this Board.
     * @param shipName The ship name
     */
    public void placeShipAtNode(Node node, String shipName) {
        node.getStyleClass().add("ship");
        node.getStyleClass().add(shipName);
    }

    /**
     * Automatically places the currently selected ship at all currently selected squares on the board.
     *
     * @return True if the operation succeeded, false if it was blocked.
     */
    public boolean placeShipAtSelection() {
        if (selectedShip == -1) {
            return false;
        }

        ArrayList<Node> selectedNodes = new ArrayList<>();

        for (Node node : getChildren()) {
            if (node.getStyleClass().contains("selected")) {
                if (!node.getStyleClass().contains("ship")) {
                    selectedNodes.add(node);
                } else {
                    return false;
                }
            }
        }
        String shipName = SHIP_STYLES.get(selectedShip);

        for (Node node : selectedNodes) {
            placeShipAtNode(node, shipName);
        }

        return true;
    }

    /**
     * Creates a missile on a Node which is a child of this Board.
     *
     * @param node A child of the GridPane belonging to this Board.
     * @param missileStatus The status of the missile (miss/hit)
     */
    public void placeMissileAtNode(Node node, String missileStatus) {
        node.getStyleClass().add("missile");
        node.getStyleClass().add(missileStatus);
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

                if (j == 0)
                    button.getStyleClass().add("firstRow");
                if (i == 0)
                    button.getStyleClass().add("firstColumn");
                if (j == 9)
                    button.getStyleClass().add("lastRow");
                if (i == 9)
                    button.getStyleClass().add("lastColumn");
                gridPane.add(button, i, j);

                button.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> onButtonMouseEntered(e));
                button.addEventHandler(MouseEvent.MOUSE_EXITED, e -> onButtonMouseExited(e));
            }
        }
    }

    /**
     * Displays nodes based on type of selection (single/ship) by adding a style class, 'selected'
     *
     * @param e The mouse event
     */
    protected void onButtonMouseEntered(MouseEvent e) {
        if (showSingleSelection) {
            Node node = (Node)e.getSource();
            if (!node.getStyleClass().contains("selected"))
                node.getStyleClass().add("selected");
        } else if (showShipSelection) {
            clear();
            Button source = (Button)e.getSource();
            int sourceRow = GridPane.getRowIndex(source);
            int sourceCol = GridPane.getColumnIndex(source);

            if (selectedShip == -1) {
                throw new RuntimeException("Invalid ship type was attempted to be placed!");
            }
            int length = SHIP_LENGTHS.get(selectedShip);

            // Check if the ship is not out of the grid (i.e. Cursor on the edge). If so, move source coordinate accordingly
            if (shipPlacementOrientation.equalsIgnoreCase("horizontal")) {
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

            // Depending on the orientation it will display the ship by adding a style class
            for (int k = 0; k < length; k++) {
                for (Node node : getChildren()) {
                    if (!node.getStyleClass().contains("selected")) {
                        if (shipPlacementOrientation.equalsIgnoreCase("horizontal")) {
                            // Select nodes left and right
                            if (GridPane.getColumnIndex(node) == sourceCol - Math.ceil((double) k / 2) && GridPane.getRowIndex(node) == sourceRow
                                    || GridPane.getColumnIndex(node) == sourceCol + Math.ceil((double) k / 2) && GridPane.getRowIndex(node) == sourceRow) {
                                node.getStyleClass().add("selected");
                                break;
                            }
                        } else {
                            // Select nodes up and down
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
    }

    /**
     * Clears style class when mouse exits the node
     *
     * @param e     The MouseEvent
     */
    protected void onButtonMouseExited(MouseEvent e) {
        if (showSingleSelection) {
            Node node = (Node)e.getSource();
            node.getStyleClass().remove("selected");
        } else if (showShipSelection) {
            for (Node node : getChildren())
                node.getStyleClass().remove("selected");
        }
    }

    /**
     * Add event handler to node
     *
     * @param eventType         The event type
     * @param eventHandler      The event handler
     */
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
     * Return if single selection is showing
     *
     * @return if single selection is showing
     */
    public boolean getShowSingleSelection() {
        return showSingleSelection;
    }

    /**
     * Sets single selection status
     *
     * @param value Single selection status
     */
    public void setShowSingleSelection(boolean value) {
        showSingleSelection = value;
        if (!value) {
            // Reset any selections which may still exist
            clear();
        }
    }

    /**
     * Return if ship selection is showing
     *
     * @return if ship selection is showing
     */
    public boolean getShowShipSelection() {
        return showShipSelection;
    }

    /**
     * Sets ship selection status
     *
     * @param value Ship selection status
     */
    public void setShowShipSelection(boolean value) {
        showShipSelection = value;
        if (!value) {
            // Reset any selections which may still exist
            clear();
        }
    }

    /**
     * Toggles ship orientation
     */
    public void toggleShipPlacementOrientation() {
        shipPlacementOrientation = shipPlacementOrientation.equalsIgnoreCase("horizontal") ? "vertical" : "horizontal";
    }

    /**
     * Sets the ship
     *
     * @param value The index of the ship
     */
    public void setSelectedShip(int value) {
        selectedShip = value;
    }
}