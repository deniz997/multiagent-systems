package mat.agent.reactive;

import mat.agent.reactive.model.Coordinate;
import mat.agent.reactive.model.Order;
import mat.agent.reactive.model.Warehouse;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

public class ConfigLoader {
    private static final String WAREHOUSE_SETUP_FILE_NAME = "input.json";

    private static Optional<String> loadFile(String filePath) {
        try {
            InputStream inputStream = Objects.requireNonNull(App.class.getResourceAsStream(filePath));
            Scanner s = new Scanner(inputStream).useDelimiter("\\A");
            String text = s.hasNext() ? s.next() : "";

            return Optional.of(text);
        } catch (NullPointerException e) {
            return Optional.empty();
        }
    }

    public static Optional<Warehouse> getWarehouseSetup() {
        Optional<String> jsonText = loadFile(WAREHOUSE_SETUP_FILE_NAME);

        if (jsonText.isEmpty()) {
            return Optional.empty();
        }

        try {
            JSONObject json = new JSONObject(jsonText.get());

            int sizeX = (int) Objects.requireNonNull(json.get("sizeX"));
            int sizeY = (int) Objects.requireNonNull(json.get("sizeY"));

            // TODO: Assert enough idling zones are provided (>= count agents)

            Warehouse setup = new Warehouse(sizeX, sizeY);
            setup.setDropZones(new Coordinate(2, 0));
            setup.setIdlingZones(new Coordinate(3, 0));
            setup.spawnAgents(new Coordinate(0, 0), new Coordinate(1, 1), new Coordinate(6, 6), new Coordinate(4, 4));
            Order order1 = new Order(new Coordinate(3, 3), new Coordinate(3, 5));
            Order order2 = new Order(new Coordinate(5, 3), new Coordinate(4, 5));
            setup.placeOrders(order1, order2);

            return Optional.of(setup);
        } catch(NullPointerException e) {
            return Optional.empty();
        }

    }
}
