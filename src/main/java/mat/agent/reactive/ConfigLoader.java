package mat.agent.reactive;

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

            Warehouse setup = new Warehouse();
            setup.setSize((int) Objects.requireNonNull(json.get("size")));

            return Optional.of(setup);
        } catch(NullPointerException e) {
            return Optional.empty();
        }

    }
}
