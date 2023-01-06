package mat.agent.reactive.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import mat.agent.reactive.model.Agent;
import mat.agent.reactive.model.Coordinate;
import mat.agent.reactive.model.Order;
import mat.agent.reactive.model.Warehouse;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AppController implements ControllerInterface {
    private final VBox view;

    public AppController(Parent view) {
        this.view = (VBox) view;
    }

    @Override
    public void stop() {

    }

    private Background createBackground(Color color) {
        return new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
    }

    private HBox buildCell() {
        HBox cell = new HBox();
        cell.setPrefHeight(30);
        cell.setPrefWidth(30);
        cell.setMaxHeight(30);
        cell.setMaxWidth(30);

        return cell;
    }

    private HBox buildRow() {
        HBox row = new HBox();
        row.setMaxHeight(30);
        row.setPrefHeight(30);
        row.setAlignment(Pos.CENTER);

        return row;
    }

    public void commitState(Warehouse warehouse) {
        Warehouse.GridCellType[][] grid = warehouse.getSetup();

        // Commit cell states which should not change
        for (int y = 0; y < grid.length; y++) {
            HBox row = (HBox) view.getChildren().get(y);

            for (int x = 0; x < grid[y].length; x++) {
                HBox cell = (HBox) row.getChildren().get(x);
                Warehouse.GridCellType cellType = grid[y][x];
                cell.getChildren().clear();

                switch (cellType) {
                    case FREE:
                        cell.setBackground(createBackground(Color.GRAY));
                        break;
                    case IDLING_ZONE:
                        cell.setBackground(createBackground(Color.GREEN));
                        break;
                    case DROP_ZONE:
                        cell.setBackground(createBackground(Color.YELLOW));
                        break;
                }
            }
        };

        // Commit agent states
        List<Coordinate> coordinates = warehouse.getAgentCoordinates();

        for (Coordinate coordinate : coordinates) {
            Agent agent = warehouse.getAgentByCoordinate(coordinate);
            HBox row = (HBox) view.getChildren().get(coordinate.y);
            HBox cell = (HBox) row.getChildren().get(coordinate.x);
            cell.setBackground(createBackground(Color.RED));

            Optional<Coordinate> nextCoordinate = agent.getMovingTo();
            if (nextCoordinate.isPresent()) {
                Coordinate next = nextCoordinate.get();
                Text text = new Text(agent.getId() + "\n" + "(" + next.x + ", " + next.y + ")");
                // Set size of text
                text.setStyle("-fx-font-size: 8px;");
                // Center text
                cell.setAlignment(Pos.CENTER);
                cell.getChildren().add(text);
            }
        }
    }

    public void buildGrid(Warehouse warehouse) {
        int sizeX = warehouse.getSizeX();
        int sizeY = warehouse.getSizeY();

        for (int y = 0; y < sizeY; y++) {
            HBox row = buildRow();

            for (int x = 0; x < sizeX; x++) {
                row.getChildren().add(buildCell());
            }

            view.getChildren().add(row);
        }

        commitState(warehouse);
    }
}