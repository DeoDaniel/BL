package billiard;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class PauseMenu extends StackPane {
    private final GamePanel gamePanel;
    private final Stage stage;
    private final BilliardGame game;
    private Runnable onContinue;
    private Runnable onReset;
    private Runnable onMainMenu;
    private final Settings settings = Settings.getInstance();

    public PauseMenu(GamePanel gamePanel, Stage stage, BilliardGame game) {
        this.gamePanel = gamePanel;
        this.stage = stage;
        this.game = game;
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

        // Save button
        Button saveButton = createButton("SAVE GAME");
        saveButton.setOnAction(e -> showSaveGameMenu());

        // Settings button
        Button settingsButton = createButton("SETTINGS");
        settingsButton.setOnAction(e -> showSettingsView());

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

        buttonsContainer.getChildren().addAll(continueButton, resetButton, saveButton, settingsButton, mainMenuButton);

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

    private void showSettingsView() {
        // Clear the current pause menu overlay
        this.getChildren().clear();

        // Root container for settings
        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: rgba(13, 13, 13, 0.95); -fx-border-color: #ffd700; -fx-border-width: 3;");
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(40));
        root.setPrefWidth(700);
        root.setMaxWidth(700);

        // Title
        Label titleLabel = new Label("SETTINGS");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.web("#ffd700"));

        // Settings container
        VBox settingsContainer = new VBox(15);
        settingsContainer.setAlignment(Pos.TOP_LEFT);
        settingsContainer.setPadding(new Insets(10, 20, 10, 20));

        // === AUDIO SETTINGS ===
        Label audioLabel = new Label("AUDIO");
        audioLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        audioLabel.setTextFill(Color.web("#ffd700"));

        // SFX Volume
        HBox sfxBox = new HBox(10);
        sfxBox.setAlignment(Pos.CENTER_LEFT);
        Label sfxLabel = new Label("SFX Volume:");
        sfxLabel.setFont(Font.font("Arial", 12));
        sfxLabel.setTextFill(Color.web("#ffffff"));
        sfxLabel.setMinWidth(120);

        Slider sfxSlider = new Slider(0, 1.0, settings.getSfxVolume());
        sfxSlider.setStyle("-fx-control-inner-background: #1f7f4d;");
        sfxSlider.setPrefWidth(250);
        sfxSlider.valueProperty().addListener((obs, oldVal, newVal) -> settings.setSfxVolume(newVal.doubleValue()));

        Label sfxValueLabel = new Label(String.format("%.0f%%", settings.getSfxVolume() * 100));
        sfxValueLabel.setFont(Font.font("Arial", 12));
        sfxValueLabel.setTextFill(Color.web("#ffffff"));
        sfxValueLabel.setMinWidth(40);
        sfxSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            sfxValueLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100)));

        sfxBox.getChildren().addAll(sfxLabel, sfxSlider, sfxValueLabel);

        // BGM Volume
        HBox bgmBox = new HBox(10);
        bgmBox.setAlignment(Pos.CENTER_LEFT);
        Label bgmLabel = new Label("BGM Volume:");
        bgmLabel.setFont(Font.font("Arial", 12));
        bgmLabel.setTextFill(Color.web("#ffffff"));
        bgmLabel.setMinWidth(120);

        Slider bgmSlider = new Slider(0, 1.0, settings.getBgmVolume());
        bgmSlider.setStyle("-fx-control-inner-background: #1f7f4d;");
        bgmSlider.setPrefWidth(250);
        bgmSlider.valueProperty().addListener((obs, oldVal, newVal) -> settings.setBgmVolume(newVal.doubleValue()));

        Label bgmValueLabel = new Label(String.format("%.0f%%", settings.getBgmVolume() * 100));
        bgmValueLabel.setFont(Font.font("Arial", 12));
        bgmValueLabel.setTextFill(Color.web("#ffffff"));
        bgmValueLabel.setMinWidth(40);
        bgmSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            bgmValueLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100)));

        bgmBox.getChildren().addAll(bgmLabel, bgmSlider, bgmValueLabel);

        // === GAMEPLAY SETTINGS ===
        Label gameplayLabel = new Label("GAMEPLAY");
        gameplayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gameplayLabel.setTextFill(Color.web("#ffd700"));

        // AI Difficulty
        HBox aiBox = new HBox(10);
        aiBox.setAlignment(Pos.CENTER_LEFT);
        Label aiLabel = new Label("AI Difficulty:");
        aiLabel.setFont(Font.font("Arial", 12));
        aiLabel.setTextFill(Color.web("#ffffff"));
        aiLabel.setMinWidth(120);

        ToggleGroup aiGroup = new ToggleGroup();
        RadioButton easyRadio = new RadioButton("Easy");
        RadioButton mediumRadio = new RadioButton("Medium");
        RadioButton hardRadio = new RadioButton("Hard");

        easyRadio.setToggleGroup(aiGroup);
        mediumRadio.setToggleGroup(aiGroup);
        hardRadio.setToggleGroup(aiGroup);

        easyRadio.setFont(Font.font("Arial", 12));
        mediumRadio.setFont(Font.font("Arial", 12));
        hardRadio.setFont(Font.font("Arial", 12));

        easyRadio.setTextFill(Color.web("#ffffff"));
        mediumRadio.setTextFill(Color.web("#ffffff"));
        hardRadio.setTextFill(Color.web("#ffffff"));

        if ("Easy".equals(settings.getAiDifficulty())) easyRadio.setSelected(true);
        else if ("Hard".equals(settings.getAiDifficulty())) hardRadio.setSelected(true);
        else mediumRadio.setSelected(true);

        easyRadio.setOnAction(e -> settings.setAiDifficulty("Easy"));
        mediumRadio.setOnAction(e -> settings.setAiDifficulty("Medium"));
        hardRadio.setOnAction(e -> settings.setAiDifficulty("Hard"));

        aiBox.getChildren().addAll(aiLabel, easyRadio, mediumRadio, hardRadio);

        // Screen Resolution
        HBox resolutionBox = new HBox(10);
        resolutionBox.setAlignment(Pos.CENTER_LEFT);
        Label resolutionLabel = new Label("Resolution:");
        resolutionLabel.setFont(Font.font("Arial", 12));
        resolutionLabel.setTextFill(Color.web("#ffffff"));
        resolutionLabel.setMinWidth(120);

        ComboBox<String> resolutionCombo = new ComboBox<>();
        resolutionCombo.getItems().addAll("1120x650", "1280x720", "1600x900", "1920x1080");
        resolutionCombo.setValue(settings.getScreenResolution());
        resolutionCombo.setStyle(
            "-fx-font-size: 12;" +
            "-fx-control-inner-background: #1f7f4d;" +
            "-fx-text-fill: #ffffff;"
        );
        resolutionCombo.setPrefWidth(200);
        resolutionCombo.setOnAction(e -> {
            settings.setScreenResolution(resolutionCombo.getValue());
            applyResolution();
        });

        resolutionBox.getChildren().addAll(resolutionLabel, resolutionCombo);

        // Add settings to container
        settingsContainer.getChildren().addAll(
            audioLabel, sfxBox, bgmBox,
            gameplayLabel, aiBox, resolutionBox
        );

        // Buttons container
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(20, 0, 0, 0));

        // Back button
        Button backButton = new Button("BACK");
        backButton.setStyle(
            "-fx-padding: 12 40 12 40;" +
            "-fx-font-size: 14;" +
            "-fx-font-weight: bold;" +
            "-fx-background-color: #cc0000;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        backButton.setOnAction(e -> returnToPauseMenu());

        buttonContainer.getChildren().add(backButton);

        // Add all to root
        root.getChildren().addAll(titleLabel, settingsContainer, buttonContainer);

        // Add to this overlay
        this.getChildren().add(root);
        StackPane.setAlignment(root, Pos.CENTER);
    }

    private void returnToPauseMenu() {
        // Clear settings view
        this.getChildren().clear();
        
        // Rebuild pause menu
        initializeUI();
    }

    private void applyResolution() {
        String[] res = settings.getScreenResolution().split("x");
        int width = Integer.parseInt(res[0]);
        int height = Integer.parseInt(res[1]);
        stage.setWidth(width);
        stage.setHeight(height);
    }

    private void showSaveGameMenu() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Save Game");
        dialog.setHeaderText("Enter save file name:");
        dialog.setContentText("File name:");

        var result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String fileName = result.get().trim();
            GameSession session = new GameSession();
            session.sessionName = fileName;
            session.gameMode = Settings.getInstance().getGameMode() != null ? Settings.getInstance().getGameMode() : "Original";
            
            // Capture current game state
            session.currentPlayerIndex = game.players.indexOf(game.currentPlayer);
            session.gameState = game.state != null ? game.state.toString() : "AIMING";
            
            // Capture ball states (all balls on table)
            for (Ball ball : gamePanel.table.balls) {
                GameSession.BallState ballState = new GameSession.BallState(
                    ball.x, ball.y, ball.vx, ball.vy,
                    ball.number, ball.sunk, ball.radius
                );
                session.ballStates.add(ballState);
            }
            
            // Capture player information (names, scores, assigned groups)
            for (int i = 0; i < game.players.size(); i++) {
                Player player = game.players.get(i);
                session.playerNames.add(player.name);
                session.playerScores.add(player.ballsPocketed);
                session.playerGroups.add(player.assignedGroup != null ? player.assignedGroup.toString() : "null");
            }
            
            // Capture elapsed time
            session.elapsedTimeMillis = game.stopwatch.getElapsedMillis();
            
            if (GameSaveManager.saveGame(session, fileName)) {
                // Show success message
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText("Game Saved");
                successAlert.setContentText("Game saved as: " + fileName);
                successAlert.showAndWait();
            } else {
                // Show error message
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Save Failed");
                errorAlert.setContentText("Failed to save game.");
                errorAlert.showAndWait();
            }
        }
    }
}
