package billiard;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class PauseMenu extends StackPane {
    private final GamePanel gamePanel;
    private Runnable onContinue;
    private Runnable onReset;
    private Runnable onMainMenu;

    public PauseMenu(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        initializeUI();
    }

    private void initializeUI() {
        // Semi-transparent overlay background
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        // Blur effect on game panel only
        GaussianBlur blur = new GaussianBlur(10);
        gamePanel.setEffect(blur);

        // Menu container
        VBox menuContainer = new VBox(25);
        menuContainer.setStyle("-fx-background-color: rgba(13, 13, 13, 0.95); -fx-border-color: #ffd700; -fx-border-width: 3;");
        menuContainer.setAlignment(Pos.CENTER);
        menuContainer.setPadding(new Insets(60));
        menuContainer.setPrefWidth(500);
        menuContainer.setMaxWidth(500);

        // Title
        Label titleLabel = new Label("PAUSED");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 64));
        titleLabel.setTextFill(Color.web("#ffd700"));

        // Buttons container
        VBox buttonsContainer = new VBox(15);
        buttonsContainer.setAlignment(Pos.CENTER);
        buttonsContainer.setPadding(new Insets(40, 0, 0, 0));

        // Continue button
        Button continueButton = createButton("CONTINUE");
        continueButton.setOnAction(e -> {
            hide();
            if (onContinue != null) onContinue.run();
        });

        // Reset button
        Button resetButton = createButton("RESET GAME");
        resetButton.setOnAction(e -> {
            hide();
            if (onReset != null) onReset.run();
        });

        // Settings button (disabled)
        Button settingsButton = createButton("SETTINGS (Coming Soon)");
        settingsButton.setDisable(true);
        settingsButton.setOpacity(0.6);

        // Main Menu button
        Button mainMenuButton = new Button("MAIN MENU");
        mainMenuButton.setStyle(
            "-fx-padding: 15 60 15 60;" +
            "-fx-font-size: 18;" +
            "-fx-font-weight: bold;" +
            "-fx-background-color: #cc0000;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        mainMenuButton.setOnAction(e -> {
            hide();
            if (onMainMenu != null) onMainMenu.run();
        });

        buttonsContainer.getChildren().addAll(continueButton, resetButton, settingsButton, mainMenuButton);

        // Add to menu container
        menuContainer.getChildren().addAll(titleLabel, buttonsContainer);

        // Center the menu on screen
        this.getChildren().add(menuContainer);
        StackPane.setAlignment(menuContainer, Pos.CENTER);
    }

    public void setOnContinue(Runnable callback) {
        this.onContinue = callback;
    }

    public void setOnReset(Runnable callback) {
        this.onReset = callback;
    }

    public void setOnMainMenu(Runnable callback) {
        this.onMainMenu = callback;
    }

    public void hide() {
        // Remove blur effect from game panel
        gamePanel.setEffect(null);
        // Remove this overlay from parent
        if (getParent() != null && getParent() instanceof StackPane) {
            ((StackPane) getParent()).getChildren().remove(this);
        }
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
