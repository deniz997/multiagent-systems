module mat.agent.reactive {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires org.apache.logging.log4j;

    opens mat.agent.reactive to javafx.fxml;
    exports mat.agent.reactive;
    exports mat.agent.reactive.model;
    exports mat.agent.reactive.controller;
    opens mat.agent.reactive.controller to javafx.fxml;
}