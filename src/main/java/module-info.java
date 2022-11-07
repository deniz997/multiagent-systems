module mat.agent.reactive {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;

    opens mat.agent.reactive to javafx.fxml;
    exports mat.agent.reactive;
    exports mat.agent.reactive.model;
}