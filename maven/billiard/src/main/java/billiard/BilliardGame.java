package billiard;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BilliardGame extends Application {

    public Stage frame;
    public GamePanel panel;
    public List<Player> players = new ArrayList<>();
    public Player currentPlayer;
    public GameState state = GameState.AIMING;
    public PhysicsEngine physics = new PhysicsEngine();
    // Tambahkan di bagian atas class BilliardGame
    public boolean cueBallHitAnyBall = false; // Flag untuk mengecek apakah bola putih kena sasaran
    public GameSession loadedSession; // For loading saved games
    public PlayerVsAIMode aiMode = null; // AI mode handler (null if not using AI)

    // --- HUD COMPONENTS ---
    private Label p1NameLabel;
    private Label p2NameLabel;
    private Label p3NameLabel;
    private Label p1GroupLabel; // Label untuk menampilkan "SOLIDS" atau "STRIPES"
    private Label p2GroupLabel;
    private Label p3GroupLabel;
    private HBox p1BallContainer; 
    private HBox p2BallContainer;
    private HBox p3BallContainer;
    private VBox p1PanelBox;
    private VBox p2PanelBox;
    private VBox p3PanelBox;
    private Label stopwatchLabel; // Stopwatch label untuk menampilkan waktu
    
    // Stopwatch
    public Stopwatch stopwatch = new Stopwatch();
    public boolean foulThisTurn = false;
    public boolean scoredThisTurn = false;  // Track if player scored on this turn
    
    // Ball 8 tracking
    public boolean ball8Pocketed = false;
    public String ball8Winner = null; // Nama pemain yang menang dengan bola 8

    @Override
    public void start(Stage primaryStage) {
        this.frame = primaryStage;

        // Start background music before showing the menu
        SoundManager.startBackgroundMusic();

        // Show main menu
        MainMenu menu = new MainMenu(primaryStage, this);
        menu.show();
    }

    private VBox createTopBar() {
        // --- PLAYER 1 SIDE (Left) ---
        p1NameLabel = new Label("PLAYER 1");
        p1NameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        p1GroupLabel = new Label("OPEN TABLE");
        p1GroupLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11; -fx-font-weight: bold;");
        p1BallContainer = new HBox(5);
        p1BallContainer.setAlignment(Pos.CENTER_LEFT);
        p1BallContainer.setPrefHeight(30);
        HBox p1Row = new HBox(15, p1NameLabel, p1GroupLabel, p1BallContainer);
        p1Row.setAlignment(Pos.CENTER_LEFT);
        p1PanelBox = new VBox(2, p1Row);
        p1PanelBox.setPadding(new Insets(8, 20, 8, 20));
        p1PanelBox.setStyle("-fx-background-color: #333333; -fx-background-radius: 12; -fx-border-color: #555; -fx-border-radius: 12;");

        // --- PLAYER 2 SIDE (Center) ---
        p2NameLabel = new Label("PLAYER 2");
        p2NameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        p2GroupLabel = new Label("OPEN TABLE");
        p2GroupLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11; -fx-font-weight: bold;");
        p2BallContainer = new HBox(5);
        p2BallContainer.setAlignment(Pos.CENTER_LEFT);
        p2BallContainer.setPrefHeight(30);
        HBox p2Row = new HBox(15, p2NameLabel, p2GroupLabel, p2BallContainer);
        p2Row.setAlignment(Pos.CENTER_LEFT);
        p2PanelBox = new VBox(2, p2Row);
        p2PanelBox.setPadding(new Insets(8, 20, 8, 20));
        p2PanelBox.setStyle("-fx-background-color: #333333; -fx-background-radius: 12; -fx-border-color: #555; -fx-border-radius: 12;");

        // --- PLAYER 3 SIDE (Right) ---
        p3NameLabel = new Label("PLAYER 3");
        p3NameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        p3GroupLabel = new Label("OPEN TABLE");
        p3GroupLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11; -fx-font-weight: bold;");
        p3BallContainer = new HBox(5);
        p3BallContainer.setAlignment(Pos.CENTER_LEFT);
        p3BallContainer.setPrefHeight(30);
        HBox p3Row = new HBox(15, p3NameLabel, p3GroupLabel, p3BallContainer);
        p3Row.setAlignment(Pos.CENTER_LEFT);
        p3PanelBox = new VBox(2, p3Row);
        p3PanelBox.setPadding(new Insets(8, 20, 8, 20));
        p3PanelBox.setStyle("-fx-background-color: #333333; -fx-background-radius: 12; -fx-border-color: #555; -fx-border-radius: 12;");
        p3PanelBox.setVisible(false);
        p3PanelBox.setManaged(false);

        // --- STOPWATCH (CENTER) ---
        stopwatchLabel = new Label("00:00");
        stopwatchLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-weight: bold; -fx-font-size: 24;");

        // --- MAIN TOP BAR ---
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        HBox topBar = new HBox(10, p1PanelBox, spacer, p2PanelBox, spacer2, p3PanelBox);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #444; -fx-border-width: 0 0 2 0;");
        topBar.setAlignment(Pos.CENTER);

        VBox fullTopBar = new VBox(6);
        fullTopBar.setPadding(new Insets(10));
        fullTopBar.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #444; -fx-border-width: 0 0 2 0;");

        HBox timerRow = new HBox(stopwatchLabel);
        timerRow.setAlignment(Pos.CENTER);
        fullTopBar.getChildren().addAll(topBar, timerRow);
        
        return fullTopBar;
    }

    public void setLoadedSession(GameSession session) {
        this.loadedSession = session;
    }

    /**
     * Apply a loaded GameSession to the current game objects (table, players, stopwatch).
     */
    public void applyLoadedSession(GameSession session) {
        if (session == null) return;

        // Restore players
        players.clear();
        for (int i = 0; i < session.playerNames.size(); i++) {
            String name = session.playerNames.get(i);
            Player p = new Player(name);
            if (i < session.playerScores.size()) p.ballsPocketed = session.playerScores.get(i);
            if (i < session.playerGroups.size()) {
                String g = session.playerGroups.get(i);
                if (g != null && !"null".equals(g)) {
                    try {
                        p.assignedGroup = BallGroup.valueOf(g);
                    } catch (Exception ex) {
                        p.assignedGroup = null;
                    }
                }
            }
            players.add(p);
        }

        // Set current player
        if (session.currentPlayerIndex >= 0 && session.currentPlayerIndex < players.size()) {
            currentPlayer = players.get(session.currentPlayerIndex);
            currentPlayer.hasTurn = true;
        }

        // Restore balls into the table
        panel.table.balls.clear();
        for (GameSession.BallState bs : session.ballStates) {
            Ball b = new Ball(bs.x, bs.y, bs.ballNumber);
            b.vx = bs.vx;
            b.vy = bs.vy;
            b.sunk = bs.sunk;
            b.radius = bs.radius;
            panel.table.balls.add(b);
        }

        // Restore which balls were pocketed by which player
        for (GameSession.BallState bs : session.ballStates) {
            if (bs.pocketedByPlayer >= 0 && bs.pocketedByPlayer < players.size()) {
                // find the corresponding ball instance in table
                for (Ball b : panel.table.balls) {
                    if (b.number == bs.ballNumber) {
                        players.get(bs.pocketedByPlayer).pocketedBalls.add(b);
                        break;
                    }
                }
            }
        }

        // Ensure cue reference points to the correct cue ball instance
        Ball cueBall = null;
        for (Ball b : panel.table.balls) {
            if (b.number == 0) {
                cueBall = b;
                break;
            }
        }
        if (cueBall != null) {
            panel.cue.cueBall = cueBall;
            panel.cue.setTableReference(panel.table);
        }

        // Restore game state
        try {
            this.state = GameState.valueOf(session.gameState);
        } catch (Exception ex) {
            this.state = GameState.AIMING;
        }

        // Restore stopwatch
        this.stopwatch.setElapsedMillis(session.elapsedTimeMillis);

        // Update HUD to reflect restored state
        updateHud();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void startNewGame() {
        // Get current game mode from settings
        String gameMode = Settings.getInstance().getGameMode();
        startGameWithMode(gameMode);
    }

    /**
     * Start game with specific game mode
     */
    public void startGameWithMode(String gameMode) {
        // Clear previous players if any
        players.clear();
        aiMode = null; // Reset AI mode
        Settings.getInstance().setGameMode(gameMode);
        
        // Init Players based on game mode
        if ("Player vs AI".equals(gameMode)) {
            players.add(new Player("PLAYER"));
            players.add(new Player("Computer (AI)"));
        } else if ("3 Players".equals(gameMode)) {
            players.add(new Player("PLAYER 1"));
            players.add(new Player("PLAYER 2"));
            players.add(new Player("PLAYER 3"));
        } else if ("9-Ball".equals(gameMode)) {
            players.add(new Player("PLAYER 1"));
            players.add(new Player("PLAYER 2"));
        } else if ("Straight Pool".equals(gameMode)) {
            players.add(new Player("PLAYER 1"));
            players.add(new Player("PLAYER 2"));
        } else {
            players.add(new Player("PLAYER 1"));
            players.add(new Player("PLAYER 2"));
        }
        
        currentPlayer = players.get(0);
        currentPlayer.hasTurn = true;
        startTurn();

        // Root Layout (create first so we can pass to GamePanel)
        BorderPane root = new BorderPane();
        
        // Create center StackPane first
        StackPane center = new StackPane();
        center.setStyle("-fx-background-color: #0d0d0d;"); 
        
        // Game Panel (pass both root and center)
        panel = new GamePanel(1120, 580, this, root, center);
        
        // Initialize AI mode if "Player vs AI"
        if ("Player vs AI".equals(gameMode)) {
            aiMode = new PlayerVsAIMode(this, panel);
        }
        
        // If a session was loaded before starting, apply it now
        if (this.loadedSession != null) {
            applyLoadedSession(this.loadedSession);
            this.loadedSession = null;
        }
        
        center.getChildren().add(panel);
        root.setCenter(center);

        // --- CUSTOM HUD SETUP ---
        VBox topBar = createTopBar();
        root.setTop(topBar);

        // Update pertama kali
        updateHud();
        
        // Start stopwatch when game begins
        stopwatch.start();

        Scene scene = new Scene(root, 1120, 680); 

        frame.setTitle("Billiard Game JavaFX");
        frame.setScene(scene);
        frame.show();
    }

    public void startTurn() {
        scoredThisTurn = false;
        foulThisTurn = false;
        currentPlayer.fouled = false;
    }

    public Player getOtherPlayer() {
        if (players.isEmpty() || currentPlayer == null) return null;
        int idx = players.indexOf(currentPlayer);
        return players.get((idx + 1) % players.size());
    }

    public void switchTurn() {
        int idx = players.indexOf(currentPlayer);
        currentPlayer.hasTurn = false;
        currentPlayer = players.get((idx + 1) % players.size());
        currentPlayer.hasTurn = true;
        state = GameState.AIMING;
        startTurn();
        if (aiMode != null) {
            aiMode.onTurnStarted();
        }
        updateHud();
    }

    public void onBallPocketed(Ball ball) {
            // 1. Cek Foul Bola Putih
            if (ball.isCueBall()) {
                foulThisTurn = true;
                currentPlayer.fouled = true;
                updateHud();
                
                // PENTING: Jangan return; langsung. Kita harus set Ball in Hand.
                triggerBallInHand(); 
                return;
            }

            // 2. Special handling for 9-Ball and Straight Pool
            String mode = Settings.getInstance().getGameMode();
            if ("9-Ball".equals(mode) && ball.number == 9) {
                currentPlayer.pocketBall(ball);
                state = GameState.GAME_OVER;
                ball8Winner = currentPlayer.name;
                updateHud();
                return;
            }

            if ("Straight Pool".equals(mode)) {
                currentPlayer.pocketBall(ball);
                scoredThisTurn = true;
                updateHud();
                if (currentPlayer.ballsPocketed >= 100) {
                    state = GameState.GAME_OVER;
                    ball8Winner = currentPlayer.name;
                }
                return;
            }

            // 3. Cek Bola 8 (Hitam) - LOGIKA BARU
            if (ball.number == 8) {
                // Cek apakah pemain saat ini sudah memasukkan semua 7 bola grupnya
                boolean currentPlayerFinishedGroup = hasPlayerFinishedGroup(currentPlayer);
                
                if (currentPlayerFinishedGroup) {
                    // MENANG! Pemain sudah memasukkan semua bolanya dan bola 8
                    ball8Pocketed = true;
                    ball8Winner = currentPlayer.name;
                    state = GameState.GAME_OVER;
                    System.out.println("GAME OVER! " + currentPlayer.name + " MENANG!");
                } else {
                    // FOUL! Bola 8 masuk sebelum waktunya
                    System.out.println("FOUL: Bola 8 masuk sebelum waktunya! Dikembalikan ke posisi semula.");
                    
                    // Kembalikan bola 8 ke posisi awal (tengah meja area rack)
                    ball.sunk = false;
                    ball.x = panel.getWidth() * 0.75; // Posisi rack
                    ball.y = panel.getHeight() / 2;
                    ball.vx = 0;
                    ball.vy = 0;
                    
                    // Cari posisi kosong jika ada bola lain di situ
                    findEmptySpotForBall(ball);
                    
                    // Set foul dan ball in hand untuk lawan
                    foulThisTurn = true;
                    currentPlayer.fouled = true;
                }
                updateHud();
                return;
            }

            // 4. Logika Penetapan Kepemilikan Bola
            if (currentPlayer.assignedGroup == null) {
                // --- KONDISI OPEN TABLE (Belum ada grup) ---
                // Pemain yang memasukkan bola pertama kali menentukan grupnya
                
                currentPlayer.pocketBall(ball); // Masuk ke pemain saat ini
                scoredThisTurn = true;          // Dia berhak lanjut main

                // Tentukan grup berdasarkan bola yang masuk
                if (ball.isSolid()) {
                    currentPlayer.assignGroup(BallGroup.SOLIDS);
                    getOtherPlayer().assignGroup(BallGroup.STRIPES);
                } else if (ball.isStripe()) {
                    currentPlayer.assignGroup(BallGroup.STRIPES);
                    getOtherPlayer().assignGroup(BallGroup.SOLIDS);
                }

            } else {
                // --- KONDISI SUDAH ADA GRUP (Solids vs Stripes) ---
                
                // Cek apakah bola ini sesuai dengan grup pemain saat ini
                boolean isMyBall = (currentPlayer.assignedGroup == BallGroup.SOLIDS && ball.isSolid()) ||
                                (currentPlayer.assignedGroup == BallGroup.STRIPES && ball.isStripe());

                if (isMyBall) {
                    // A. Jika bola MILIK SENDIRI
                    currentPlayer.pocketBall(ball); // Masukkan ke list pemain ini
                    scoredThisTurn = true;          // Lanjut main (kecuali nanti foul)
                } else {
                    // B. Jika bola MILIK LAWAN (Salah Masuk) = FOUL!
                    getOtherPlayer().pocketBall(ball); // Masukkan ke list LAWAN
                    
                    // FOUL: Memasukkan bola lawan
                    foulThisTurn = true;
                    currentPlayer.fouled = true;
                    scoredThisTurn = false;
                    System.out.println("FOUL: " + currentPlayer.name + " memasukkan bola lawan!"); 
                }
            }

            // Update tampilan agar bola muncul di sisi pemain yang benar
            updateHud();
        }
    public boolean checkWinCondition() {
        for (Player p : players) {
            if (p.hasWon()) {
                state = GameState.GAME_OVER;
                return true;
            }
        }
        return false;
    }
    
    /**
     * Cek apakah pemain sudah memasukkan semua 7 bola dari grupnya
     */
    public boolean hasPlayerFinishedGroup(Player p) {
        if (p.assignedGroup == null) return false;
        
        // Hitung berapa bola grup pemain ini yang sudah masuk (dari semua bola sunk)
        int count = 0;
        for (Ball b : panel.table.balls) {
            if (b.sunk && !b.isCueBall() && b.number != 8) {
                if (p.assignedGroup == BallGroup.SOLIDS && b.isSolid()) {
                    count++;
                } else if (p.assignedGroup == BallGroup.STRIPES && b.isStripe()) {
                    count++;
                }
            }
        }
        
        return count >= 7; // Semua 7 bola dari grup sudah masuk
    }
    
    /**
     * Cari posisi kosong untuk bola (digunakan saat mengembalikan bola 8)
     */
    private void findEmptySpotForBall(Ball ball) {
        double centerX = panel.getWidth() * 0.75;
        double centerY = panel.getHeight() / 2;
        double spacing = ball.radius * 2.5;
        
        // Coba posisi tengah dulu
        if (isPositionEmpty(centerX, centerY, ball)) {
            ball.x = centerX;
            ball.y = centerY;
            return;
        }
        
        // Cari posisi kosong dalam spiral
        for (int ring = 1; ring <= 5; ring++) {
            for (int i = 0; i < 8 * ring; i++) {
                double angle = (2 * Math.PI * i) / (8 * ring);
                double testX = centerX + Math.cos(angle) * spacing * ring;
                double testY = centerY + Math.sin(angle) * spacing * ring;
                
                if (isPositionEmpty(testX, testY, ball)) {
                    ball.x = testX;
                    ball.y = testY;
                    return;
                }
            }
        }
    }
    
    /**
     * Cek apakah posisi kosong dari bola lain
     */
    private boolean isPositionEmpty(double x, double y, Ball excludeBall) {
        for (Ball b : panel.table.balls) {
            if (b == excludeBall || b.sunk) continue;
            double dx = x - b.x;
            double dy = y - b.y;
            double dist = Math.sqrt(dx * dx + dy * dy);
            if (dist < b.radius * 2 + 5) {
                return false;
            }
        }
        return true;
    }

    // --- LOGIKA UPDATE TAMPILAN (HUD) ---

    public void updateHud() {
        if (players.isEmpty()) return;

        if ("Player vs AI".equals(Settings.getInstance().getGameMode())) {
            p2NameLabel.setText("Computer (AI)");
        } else {
            p2NameLabel.setText("PLAYER 2");
        }

        Player p1 = players.size() > 0 ? players.get(0) : null;
        Player p2 = players.size() > 1 ? players.get(1) : null;
        Player p3 = players.size() > 2 ? players.get(2) : null;

        if (p1 != null) {
            highlightActivePlayer(p1, p1PanelBox);
            updateGroupLabel(p1, p1GroupLabel);
            renderBallsToContainer(p1, p1BallContainer);
        }
        if (p2 != null) {
            highlightActivePlayer(p2, p2PanelBox);
            updateGroupLabel(p2, p2GroupLabel);
            renderBallsToContainer(p2, p2BallContainer);
        }
        if (p3 != null) {
            highlightActivePlayer(p3, p3PanelBox);
            updateGroupLabel(p3, p3GroupLabel);
            renderBallsToContainer(p3, p3BallContainer);
            p3PanelBox.setVisible(true);
            p3PanelBox.setManaged(true);
        } else {
            p3PanelBox.setVisible(false);
            p3PanelBox.setManaged(false);
        }
        
        // Update stopwatch display
        if (stopwatchLabel != null) {
            stopwatchLabel.setText(stopwatch.getFormattedTime());
        }
    }
    
    // Method untuk mereset bola putih ke mode "Ball in Hand"
    public void triggerBallInHand() {
        state = GameState.BALL_IN_HAND;
        
        // Cari bola putih
        Ball cueBall = panel.table.balls.get(0);
        
        // Jika bola putih masuk lubang (sunk), kita hidupkan lagi
        if (cueBall.sunk) {
            cueBall.sunk = false;
            cueBall.vx = 0;
            cueBall.vy = 0;
            // Taruh sementara di tengah meja (nanti user geser sendiri)
            cueBall.x = panel.getWidth() / 2;
            cueBall.y = panel.getHeight() / 2;
            panel.cue.cueBall = cueBall; // Re-link ke stick
            panel.cue.visible = false;   // Sembunyikan stick saat memindahkan bola
        }
    }

    private void highlightActivePlayer(Player p, VBox panelBox) {
        // Efek border emas jika giliran pemain tersebut
        String baseStyle = "-fx-background-radius: 12; -fx-border-radius: 12; -fx-padding: 8 20 8 20; ";
        if (p.hasTurn) {
            panelBox.setStyle(baseStyle + "-fx-background-color: #3d3d3d; -fx-border-color: #FFD700; -fx-border-width: 2; -fx-effect: dropshadow(three-pass-box, rgba(255, 215, 0, 0.4), 10, 0, 0, 0);");
        } else {
            panelBox.setStyle(baseStyle + "-fx-background-color: #222222; -fx-border-color: #444; -fx-border-width: 1;");
        }
    }

    // FUNGSI UTAMA UNTUK MENGUBAH TULISAN STRIPES/SOLIDS
    private void updateGroupLabel(Player p, Label lbl) {
        if (p.assignedGroup == BallGroup.SOLIDS) {
            lbl.setText("SOLIDS (1-7)");
            // Warna Merah/Oranye cerah untuk Solids
            lbl.setStyle("-fx-text-fill: #ff6b6b; -fx-font-weight: bold; -fx-font-size: 12; -fx-background-color: rgba(255, 107, 107, 0.1); -fx-padding: 2 6; -fx-background-radius: 4;");
        } else if (p.assignedGroup == BallGroup.STRIPES) {
            lbl.setText("STRIPES (9-15)");
            // Warna Biru/Cyan cerah untuk Stripes
            lbl.setStyle("-fx-text-fill: #4dabf7; -fx-font-weight: bold; -fx-font-size: 12; -fx-background-color: rgba(77, 171, 247, 0.1); -fx-padding: 2 6; -fx-background-radius: 4;");
        } else {
            lbl.setText("OPEN TABLE");
            // Warna Abu-abu netral
            lbl.setStyle("-fx-text-fill: #888888; -fx-font-size: 11; -fx-font-style: italic;");
        }
    }

    private void renderBallsToContainer(Player p, HBox container) {
        container.getChildren().clear();
        for (Ball b : p.pocketedBalls) {
            if (b.isCueBall()) continue; 
            Canvas ballIcon = createBallIcon(b);
            container.getChildren().add(ballIcon);
        }
    }

    private Canvas createBallIcon(Ball originalBall) {
        double size = 22; 
        double radius = size / 2.0;

        Canvas canvas = new Canvas(size, size);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Ball dummy = new Ball(radius, radius, originalBall.number);
        dummy.radius = radius; 
        dummy.render(gc);

        return canvas;
    }
}