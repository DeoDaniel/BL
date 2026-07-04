module billiard {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;

    opens billiard to javafx.fxml;
    exports billiard;
}
