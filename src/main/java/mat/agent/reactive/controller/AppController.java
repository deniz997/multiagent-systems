package mat.agent.reactive.controller;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import mat.agent.reactive.model.*;

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
                        cell.setBackground(createBackground(Color.hsb(75, 0.45, 0.93)));
                        break;
                    case DROP_ZONE:
                        cell.setBackground(createBackground(Color.hsb(58, 0.5, 1)));
                        break;
                }
            }
        }
        ;

        // Commit agent states
        List<Coordinate> coordinates = warehouse.getAgentCoordinates();

        int index = 0;
        for (Coordinate coordinate : coordinates) {
            Agent agent = warehouse.getAgentByCoordinate(coordinate);
            HBox row = (HBox) view.getChildren().get(coordinate.y);
            HBox cell = (HBox) row.getChildren().get(coordinate.x);

            Order order = agent.getOrder();
            if (Objects.nonNull(order)) {
                List<Good> goods = order.getGoods();

                for (int i = 0; i < goods.size(); i++) {
                    Good good = goods.get(i);

                    HBox orderRow = (HBox) view.getChildren().get(good.getCoordinate().y);
                    HBox orderCell = (HBox) orderRow.getChildren().get(good.getCoordinate().x);
                    orderCell.setBackground(createBackground(Color.hsb(200, 0.5, 1)));
                    orderCell.getChildren().clear();
                    Text text = new Text("(" + order.getId() + "," + (i + 1) + ")");
                    text.setStyle("-fx-font-size: 8px;");
                    text.setTextAlignment(TextAlignment.CENTER);
                    orderCell.setAlignment(Pos.CENTER);
                    orderCell.getChildren().add(text);
                }
            }

            if (index == 0) {
                cell.setBackground(createBackground(Color.hsb(0, 0.5, 1)));
                Optional<Coordinate> nextCoordinate = agent.getMovingTo();
                if (nextCoordinate.isPresent()) {
                    Coordinate next = nextCoordinate.get();
                    Text text = new Text(agent.getId() + "\n" + "(" + next.x + ", " + next.y + ")");
                    // Set size of text
                    text.setStyle("-fx-font-size: 8px;");
                    text.setTextAlignment(TextAlignment.CENTER);
                    text.toFront();
                    // Center text
                    cell.setAlignment(Pos.CENTER);
                    cell.getChildren().add(text);
                }
            } else {
                cell.setBackground(createBackground(Color.hsb(0, 0, 0.6)));
            }
            index++;
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