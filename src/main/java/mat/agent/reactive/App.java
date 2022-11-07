package mat.agent.reactive;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mat.agent.reactive.model.Warehouse;

import java.io.IOException;
import java.util.Optional;

public class App extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Optional<Warehouse> warehouse = ConfigLoader.getWarehouseSetup();

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}