package billiard;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class BilliardGame extends Application {

    public Stage frame;
    public GamePanel panel;
    public List<Player> players = new ArrayList<>();
    public Player currentPlayer;
    public GameState state = GameState.AIMING;
    public PhysicsEngine physics = new PhysicsEngine();
    // Tambahkan di bagian atas class BilliardGame
    public boolean cueBallHitAnyBall = false; // Flag untuk mengecek apakah bola putih kena sasaran

    // --- HUD COMPONENTS ---
    private Label p1NameLabel;
    private Label p2NameLabel;
    private Label p1GroupLabel; // Label untuk menampilkan "SOLIDS" atau "STRIPES"
    private Label p2GroupLabel;
    private HBox p1BallContainer; 
    private HBox p2BallContainer;
    private VBox p1PanelBox;
    private VBox p2PanelBox;

    // Turn info
    public boolean scoredThisTurn = false;
    public boolean foulThisTurn = false;
    
    // Ball 8 tracking
    public boolean ball8Pocketed = false;
    public String ball8Winner = null; // Nama pemain yang menang dengan bola 8

    @Override
    public void start(Stage primaryStage) {
        this.frame = primaryStage;

        // Init Players
        players.add(new Player("PLAYER 1"));
        players.add(new Player("PLAYER 2"));
        currentPlayer = players.get(0);
        currentPlayer.hasTurn = true;
        startTurn();

        // Game Panel
        panel = new GamePanel(1120, 560, this);

        // Root Layout
        BorderPane root = new BorderPane();
        StackPane center = new StackPane(panel);
        center.setStyle("-fx-background-color: #0d0d0d;"); 
        root.setCenter(center);

        // --- CUSTOM HUD SETUP ---
        HBox topBar = createTopBar();
        root.setTop(topBar);

        // Update pertama kali
        updateHud();

        Scene scene = new Scene(root, 1120, 650); 

        primaryStage.setTitle("Billiard Game JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createTopBar() {
        // --- PLAYER 1 SIDE (Left) ---
        p1NameLabel = new Label("PLAYER 1");
        p1NameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        
        // Label Grup P1 (Awalnya OPEN)
        p1GroupLabel = new Label("OPEN TABLE"); 
        p1GroupLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11; -fx-font-weight: bold;");

        p1BallContainer = new HBox(5);
        p1BallContainer.setAlignment(Pos.CENTER_LEFT);
        p1BallContainer.setPrefHeight(30);

        // Susunan P1: Nama di atas, Bola & Grup di bawahnya (atau sejajar)
        // Disini saya buat sejajar: Nama | Bola | Grup
        HBox p1Row = new HBox(15, p1NameLabel, p1GroupLabel, p1BallContainer);
        p1Row.setAlignment(Pos.CENTER_LEFT);
        
        p1PanelBox = new VBox(2, p1Row);
        p1PanelBox.setPadding(new Insets(8, 20, 8, 20));
        p1PanelBox.setStyle("-fx-background-color: #333333; -fx-background-radius: 12; -fx-border-color: #555; -fx-border-radius: 12;");
        
        // --- PLAYER 2 SIDE (Right) ---
        p2NameLabel = new Label("PLAYER 2");
        p2NameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");

        // Label Grup P2
        p2GroupLabel = new Label("OPEN TABLE");
        p2GroupLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11; -fx-font-weight: bold;");

        p2BallContainer = new HBox(5);
        p2BallContainer.setAlignment(Pos.CENTER_RIGHT);
        p2BallContainer.setPrefHeight(30);

        // Susunan P2: Grup | Bola | Nama (Mirror dari P1)
        HBox p2Row = new HBox(15, p2BallContainer, p2GroupLabel, p2NameLabel);
        p2Row.setAlignment(Pos.CENTER_RIGHT);

        p2PanelBox = new VBox(2, p2Row);
        p2PanelBox.setPadding(new Insets(8, 20, 8, 20));
        p2PanelBox.setStyle("-fx-background-color: #333333; -fx-background-radius: 12; -fx-border-color: #555; -fx-border-radius: 12;");

        // --- MAIN TOP BAR ---
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(10, p1PanelBox, spacer, p2PanelBox);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #444; -fx-border-width: 0 0 2 0;");
        
        return topBar;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void startNewGame() {
        players.forEach(p -> {
            p.ballsPocketed = 0;
            p.assignedGroup = null;
            p.fouled = false;
            p.pocketedBalls.clear();
        });
        currentPlayer = players.get(0);
        currentPlayer.hasTurn = true;
        startTurn();

        panel = new GamePanel(1120, 560, this);
        state = GameState.AIMING;
        updateHud();
    }

    public void startTurn() {
        scoredThisTurn = false;
        foulThisTurn = false;
        currentPlayer.fouled = false;
    }

    public Player getOtherPlayer() {
        return currentPlayer == players.get(0) ? players.get(1) : players.get(0);
    }

    public void switchTurn() {
        int idx = players.indexOf(currentPlayer);
        currentPlayer.hasTurn = false;
        currentPlayer = players.get((idx + 1) % players.size());
        currentPlayer.hasTurn = true;
        state = GameState.AIMING;
        startTurn();
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

            // 2. Cek Bola 8 (Hitam) - LOGIKA BARU
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

            // 3. Logika Penetapan Kepemilikan Bola
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
        Player p1 = players.get(0);
        Player p2 = players.get(1);

        highlightActivePlayer(p1, p1PanelBox);
        highlightActivePlayer(p2, p2PanelBox);

        // Update tulisan SOLIDS / STRIPES
        updateGroupLabel(p1, p1GroupLabel);
        updateGroupLabel(p2, p2GroupLabel);

        renderBallsToContainer(p1, p1BallContainer);
        renderBallsToContainer(p2, p2BallContainer);
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