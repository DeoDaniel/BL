package billiard;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MainMenu {
    private final Stage stage;
    private final BilliardGame game;

    public MainMenu(Stage stage, BilliardGame game) {
        this.stage = stage;
        this.game = game;
    }

    public void show() {
        // Root container
        VBox root = new VBox(30);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a1a, #0d0d0d);");
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));

        // Title
        Label titleLabel = new Label("POOL MASTER XD");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 72));
        titleLabel.setTextFill(Color.web("#ffd700"));

        Label subtitleLabel = new Label("8-Ball Pool Game");
        subtitleLabel.setFont(Font.font("Arial", 24));
        subtitleLabel.setTextFill(Color.web("#ffffff"));

        // Buttons container
        VBox buttonsContainer = new VBox(15);
        buttonsContainer.setAlignment(Pos.CENTER);
        buttonsContainer.setPadding(new Insets(40, 0, 0, 0));

        // Play button
        Button playButton = createButton("PLAY");
        playButton.setOnAction(e -> {
            game.startNewGame();
        });

        // Modes button (coming soon)
        Button modesButton = createButton("MODES (Coming Soon)");
        modesButton.setDisable(true);
        modesButton.setOpacity(0.6);

        // Exit button
        Button exitButton = new Button("EXIT");
        exitButton.setStyle(
            "-fx-padding: 15 60 15 60;" +
            "-fx-font-size: 18;" +
            "-fx-font-weight: bold;" +
            "-fx-background-color: #cc0000;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        exitButton.setOnAction(e -> System.exit(0));

        buttonsContainer.getChildren().addAll(playButton, modesButton, exitButton);

        // Add to root
        root.getChildren().addAll(titleLabel, subtitleLabel, buttonsContainer);

        Scene scene = new Scene(root, 1120, 650);
        stage.setScene(scene);
        stage.setTitle("Billiard Game - Main Menu");
        stage.show();
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-padding: 15 60 15 60;" +
            "-fx-font-size: 18;" +
            "-fx-font-weight: bold;" +
            "-fx-background-color: #1f7f4d;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        return button;
    }
}
