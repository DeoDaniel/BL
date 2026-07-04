package billiard;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MainMenu {
    private final Stage stage;
    private final BilliardGame game;
    private String gameMode = "Original"; // Default game mode
    private Scene mainScene;
    private final Settings settings = Settings.getInstance();

    public MainMenu(Stage stage, BilliardGame game) {
        this.stage = stage;
        this.game = game;
    }

    public void show() {
        showMainMenuView();
        stage.setTitle("Billiard Game - Main Menu");
        stage.show();
    }

    private void showMainMenuView() {
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
            // Always start a fresh game using the last selected mode
            // Clear any loaded session and reset the stopwatch to zero
            game.setLoadedSession(null);
            game.stopwatch.reset();
            // Ensure Settings' game mode is used by startNewGame()
            game.startNewGame();
        });

        // Leaderboard button
        Button leaderboardButton = createButton("LEADERBOARD");
        leaderboardButton.setOnAction(e -> showLeaderboardView());

        // Load button
        Button loadButton = createButton("LOAD GAME");
        loadButton.setOnAction(e -> showLoadGameMenu());

        // Modes button
        Button modesButton = createButton("GAME MODES");
        modesButton.setOnAction(e -> showModeSelectionView());

        // Settings button
        Button settingsButton = createButton("SETTINGS");
        settingsButton.setOnAction(e -> showSettingsView());

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

        buttonsContainer.getChildren().addAll(playButton, leaderboardButton, loadButton, modesButton, settingsButton, exitButton);

        // Add to root
        root.getChildren().addAll(titleLabel, subtitleLabel, buttonsContainer);

        mainScene = new Scene(root, 1120, 650);
        stage.setScene(mainScene);
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

    private void showModeSelectionView() {
        // Root container
        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a1a, #0d0d0d);");
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));

        // Title
        Label titleLabel = new Label("GAME MODES");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.web("#ffd700"));

        // Radio buttons container
        VBox modesContainer = new VBox(15);
        modesContainer.setAlignment(Pos.CENTER_LEFT);
        modesContainer.setPadding(new Insets(20, 40, 20, 40));

        // Toggle group for radio buttons
        ToggleGroup modeGroup = new ToggleGroup();

        // Create radio buttons for each mode
        String[] modes = {"Original", "Player vs AI", "3 Players", "9-Ball", "Straight Pool"};
        RadioButton[] radioButtons = new RadioButton[modes.length];

        for (int i = 0; i < modes.length; i++) {
            radioButtons[i] = new RadioButton(modes[i]);
            radioButtons[i].setToggleGroup(modeGroup);
            radioButtons[i].setFont(Font.font("Arial", 18));
            radioButtons[i].setTextFill(Color.web("#ffffff"));
            
            // Set style for radio button
            radioButtons[i].setStyle(
                "-fx-font-size: 18;" +
                "-fx-text-fill: #ffffff;" +
                "-fx-padding: 10;"
            );

            // Select the first option by default (or previously selected)
            if (modes[i].equals(gameMode)) {
                radioButtons[i].setSelected(true);
            }

            modesContainer.getChildren().add(radioButtons[i]);
        }

        // Buttons container
        VBox buttonContainer = new VBox(10);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(20, 0, 0, 0));

        // Confirm button
        Button confirmButton = new Button("CONFIRM");
        confirmButton.setStyle(
            "-fx-padding: 12 50 12 50;" +
            "-fx-font-size: 16;" +
            "-fx-font-weight: bold;" +
            "-fx-background-color: #1f7f4d;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        confirmButton.setOnAction(e -> {
            RadioButton selectedRadio = (RadioButton) modeGroup.getSelectedToggle();
            if (selectedRadio != null) {
                gameMode = selectedRadio.getText();
                settings.setGameMode(gameMode);  // Save to Settings singleton
            }
            showMainMenuView();
        });

        // Back button
        Button backButton = new Button("BACK");
        backButton.setStyle(
            "-fx-padding: 12 50 12 50;" +
            "-fx-font-size: 16;" +
            "-fx-font-weight: bold;" +
            "-fx-background-color: #cc0000;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        backButton.setOnAction(e -> showMainMenuView());

        buttonContainer.getChildren().addAll(confirmButton, backButton);

        // Add all components to root
        root.getChildren().addAll(titleLabel, modesContainer, buttonContainer);

        mainScene = new Scene(root, 1120, 650);
        stage.setScene(mainScene);
    }

    private void showLeaderboardView() {
        java.util.List<GameSession> sessions = GameSaveManager.getAllSavedSessions();

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a1a, #0d0d0d);");
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(40));

        Label titleLabel = new Label("LEADERBOARD");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.web("#ffd700"));

        VBox sessionContainer = new VBox(15);
        sessionContainer.setAlignment(Pos.TOP_CENTER);
        sessionContainer.setPadding(new Insets(20));

        if (sessions.isEmpty()) {
            Label emptyLabel = new Label("No game sessions found. Save a game to create leaderboard entries.");
            emptyLabel.setFont(Font.font("Arial", 16));
            emptyLabel.setTextFill(Color.web("#ffffff"));
            sessionContainer.getChildren().add(emptyLabel);
        } else {
            for (GameSession session : sessions) {
                VBox entryBox = new VBox(6);
                entryBox.setStyle(
                    "-fx-background-color: rgba(30, 30, 30, 0.9);" +
                    "-fx-border-color: #ffd700;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;" +
                    "-fx-padding: 16;"
                );

                Label nameLabel = new Label(session.sessionName != null ? session.sessionName : "Unnamed Session");
                nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
                nameLabel.setTextFill(Color.web("#ffffff"));

                Label detailsLabel = new Label(
                    String.format("Mode: %s | Date: %s | Duration: %s",
                        session.gameMode != null ? session.gameMode : "Unknown",
                        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date(session.timestamp)),
                        session.getFormattedElapsedTime()
                    )
                );
                detailsLabel.setFont(Font.font("Arial", 12));
                detailsLabel.setTextFill(Color.web("#cccccc"));

                Label winnerLabel = new Label("Result: " + session.getWinnerLabel());
                winnerLabel.setFont(Font.font("Arial", 14));
                winnerLabel.setTextFill(Color.web("#ffffff"));

                Label scoreLabel = new Label(session.getScoreSummary());
                scoreLabel.setFont(Font.font("Arial", 13));
                scoreLabel.setTextFill(Color.web("#ffffff"));

                entryBox.getChildren().addAll(nameLabel, detailsLabel, winnerLabel, scoreLabel);
                sessionContainer.getChildren().add(entryBox);
            }
        }

        ScrollPane scrollPane = new ScrollPane(sessionContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPrefViewportHeight(420);

        Button backButton = new Button("BACK");
        backButton.setStyle(
            "-fx-padding: 15 60 15 60;" +
            "-fx-font-size: 18;" +
            "-fx-font-weight: bold;" +
            "-fx-background-color: #cc0000;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        backButton.setOnAction(e -> showMainMenuView());

        root.getChildren().addAll(titleLabel, scrollPane, backButton);

        mainScene = new Scene(root, 1120, 650);
        stage.setScene(mainScene);
    }

    private void showSettingsView() {
        // Root container
        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a1a, #0d0d0d);");
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(40));

        // Title
        Label titleLabel = new Label("SETTINGS");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.web("#ffd700"));

        // Settings container
        VBox settingsContainer = new VBox(25);
        settingsContainer.setAlignment(Pos.TOP_LEFT);
        settingsContainer.setPadding(new Insets(20, 60, 20, 60));

        // === AUDIO SETTINGS ===
        Label audioLabel = new Label("AUDIO");
        audioLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        audioLabel.setTextFill(Color.web("#ffd700"));

        // SFX Volume
        HBox sfxBox = new HBox(15);
        sfxBox.setAlignment(Pos.CENTER_LEFT);
        Label sfxLabel = new Label("SFX Volume:");
        sfxLabel.setFont(Font.font("Arial", 14));
        sfxLabel.setTextFill(Color.web("#ffffff"));
        sfxLabel.setMinWidth(150);

        Slider sfxSlider = new Slider(0, 1.0, settings.getSfxVolume());
        sfxSlider.setStyle("-fx-control-inner-background: #1f7f4d;");
        sfxSlider.setPrefWidth(300);
        sfxSlider.valueProperty().addListener((obs, oldVal, newVal) -> settings.setSfxVolume(newVal.doubleValue()));

        Label sfxValueLabel = new Label(String.format("%.0f%%", settings.getSfxVolume() * 100));
        sfxValueLabel.setFont(Font.font("Arial", 14));
        sfxValueLabel.setTextFill(Color.web("#ffffff"));
        sfxValueLabel.setMinWidth(50);
        sfxSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            sfxValueLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100)));

        sfxBox.getChildren().addAll(sfxLabel, sfxSlider, sfxValueLabel);

        // BGM Volume
        HBox bgmBox = new HBox(15);
        bgmBox.setAlignment(Pos.CENTER_LEFT);
        Label bgmLabel = new Label("BGM Volume:");
        bgmLabel.setFont(Font.font("Arial", 14));
        bgmLabel.setTextFill(Color.web("#ffffff"));
        bgmLabel.setMinWidth(150);

        Slider bgmSlider = new Slider(0, 1.0, settings.getBgmVolume());
        bgmSlider.setStyle("-fx-control-inner-background: #1f7f4d;");
        bgmSlider.setPrefWidth(300);
        bgmSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            settings.setBgmVolume(newVal.doubleValue());
            SoundManager.updateBgmVolume(newVal.doubleValue());
        });

        Label bgmValueLabel = new Label(String.format("%.0f%%", settings.getBgmVolume() * 100));
        bgmValueLabel.setFont(Font.font("Arial", 14));
        bgmValueLabel.setTextFill(Color.web("#ffffff"));
        bgmValueLabel.setMinWidth(50);
        bgmSlider.valueProperty().addListener((obs, oldVal, newVal) -> 
            bgmValueLabel.setText(String.format("%.0f%%", newVal.doubleValue() * 100)));

        bgmBox.getChildren().addAll(bgmLabel, bgmSlider, bgmValueLabel);

        // === GAMEPLAY SETTINGS ===
        Label gameplayLabel = new Label("GAMEPLAY");
        gameplayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gameplayLabel.setTextFill(Color.web("#ffd700"));

        // AI Difficulty
        HBox aiBox = new HBox(15);
        aiBox.setAlignment(Pos.CENTER_LEFT);
        Label aiLabel = new Label("AI Difficulty:");
        aiLabel.setFont(Font.font("Arial", 14));
        aiLabel.setTextFill(Color.web("#ffffff"));
        aiLabel.setMinWidth(150);

        ToggleGroup aiGroup = new ToggleGroup();
        RadioButton easyRadio = new RadioButton("Easy");
        RadioButton mediumRadio = new RadioButton("Medium");
        RadioButton hardRadio = new RadioButton("Hard");

        easyRadio.setToggleGroup(aiGroup);
        mediumRadio.setToggleGroup(aiGroup);
        hardRadio.setToggleGroup(aiGroup);

        easyRadio.setFont(Font.font("Arial", 14));
        mediumRadio.setFont(Font.font("Arial", 14));
        hardRadio.setFont(Font.font("Arial", 14));

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
        HBox resolutionBox = new HBox(15);
        resolutionBox.setAlignment(Pos.CENTER_LEFT);
        Label resolutionLabel = new Label("Resolution:");
        resolutionLabel.setFont(Font.font("Arial", 14));
        resolutionLabel.setTextFill(Color.web("#ffffff"));
        resolutionLabel.setMinWidth(150);

        ComboBox<String> resolutionCombo = new ComboBox<>();
        resolutionCombo.getItems().addAll("1120x720", "1280x720", "1600x900", "1920x1080");
        resolutionCombo.setValue(settings.getScreenResolution());
        resolutionCombo.setStyle(
            "-fx-font-size: 14;" +
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
        VBox buttonContainer = new VBox(10);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(20, 0, 0, 0));

        // Back button
        Button backButton = new Button("BACK");
        backButton.setStyle(
            "-fx-padding: 12 50 12 50;" +
            "-fx-font-size: 16;" +
            "-fx-font-weight: bold;" +
            "-fx-background-color: #cc0000;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        backButton.setOnAction(e -> showMainMenuView());

        buttonContainer.getChildren().add(backButton);

        // Add all to root
        root.getChildren().addAll(titleLabel, settingsContainer, buttonContainer);

        mainScene = new Scene(root, 1120, 650);
        stage.setScene(mainScene);
    }

    private void applyResolution() {
        String[] res = settings.getScreenResolution().split("x");
        int width = Integer.parseInt(res[0]);
        int height = Integer.parseInt(res[1]);
        stage.setWidth(width);
        stage.setHeight(height);
    }

    public String getGameMode() {
        return gameMode;
    }

    public double getSfxVolume() {
        return settings.getSfxVolume();
    }

    public double getBgmVolume() {
        return settings.getBgmVolume();
    }

    public String getAiDifficulty() {
        return settings.getAiDifficulty();
    }

    public String getScreenResolution() {
        return settings.getScreenResolution();
    }

    private void showLoadGameMenu() {
        // Get list of save files
        java.util.List<String> saveFiles = GameSaveManager.getSaveFileNames();
        
        if (saveFiles.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Saves");
            alert.setHeaderText("No saved games found");
            alert.setContentText("Create a save by pausing the game and using the Save option.");
            alert.showAndWait();
            return;
        }

        // Root container
        VBox root = new VBox(20);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a1a, #0d0d0d);");
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));

        // Title
        Label titleLabel = new Label("LOAD GAME");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.web("#ffd700"));

        // Save files container
        VBox filesContainer = new VBox(10);
        filesContainer.setAlignment(Pos.TOP_CENTER);
        filesContainer.setPadding(new Insets(20));

        // Create buttons for each save file
        for (String fileName : saveFiles) {
            Button fileButton = new Button(fileName);
            fileButton.setPrefWidth(300);
            fileButton.setStyle(
                "-fx-padding: 12 30 12 30;" +
                "-fx-font-size: 14;" +
                "-fx-font-weight: bold;" +
                "-fx-background-color: #1f7f4d;" +
                "-fx-text-fill: #ffffff;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
            );
            fileButton.setOnAction(e -> loadGameSession(fileName));
            filesContainer.getChildren().add(fileButton);
        }

        // Buttons container
        VBox buttonContainer = new VBox(10);
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setPadding(new Insets(20, 0, 0, 0));

        // Back button
        Button backButton = new Button("BACK");
        backButton.setStyle(
            "-fx-padding: 12 50 12 50;" +
            "-fx-font-size: 16;" +
            "-fx-font-weight: bold;" +
            "-fx-background-color: #cc0000;" +
            "-fx-text-fill: #ffffff;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        backButton.setOnAction(e -> showMainMenuView());

        buttonContainer.getChildren().add(backButton);

        // Add to root
        root.getChildren().addAll(titleLabel, filesContainer, buttonContainer);

        mainScene = new Scene(root, 1120, 650);
        stage.setScene(mainScene);
    }

    private void loadGameSession(String fileName) {
        GameSession session = GameSaveManager.loadGame(fileName);
        if (session != null) {
            // Set the game mode from the save file
            if (session.gameMode != null) {
                settings.setGameMode(session.gameMode);
            }
            // Attach the loaded session first so startNewGame() can apply it
            game.setLoadedSession(session);
            // Start the game (it will load with the saved state)
            game.startNewGame();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Load Failed");
            alert.setHeaderText("Failed to load game");
            alert.setContentText("Could not load the save file: " + fileName);
            alert.showAndWait();
        }
    }
}
