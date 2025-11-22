module billiard {
    requires javafx.controls;
    requires javafx.fxml;

    opens billiard to javafx.fxml;
    exports billiard;
}
