package billiard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class GamePanel extends Canvas {
    public Table table;
    public Cue cue;
    public boolean mousePressed = false;
    private double dragStartX, dragStartY;
    private final BilliardGame game;
    private final BorderPane gameRoot;
    private final StackPane gameCenter;
    private boolean isPaused = false;

    private AnimationTimer loop;

    public GamePanel(double width, double height, BilliardGame game, BorderPane gameRoot, StackPane gameCenter) {
        super(width, height);
        this.game = game;
        this.gameRoot = gameRoot;
        this.gameCenter = gameCenter;
        table = new Table();
        Ball cueBall = new Ball(width * 0.25, height / 2, 0);
        table.balls.add(cueBall);

        String gameMode = Settings.getInstance().getGameMode();
        if ("9-Ball".equals(gameMode)) {
            setupNineBallRack(width, height);
        } else if ("Straight Pool".equals(gameMode)) {
            setupStraightPoolRack(width, height);
        } else {
            setupStandardRack(width, height);
        }

        cue = new Cue(cueBall);
        cue.setTableReference(table); // Set reference untuk aim line

        setOnMousePressed(this::mousePressed);
        setOnMouseDragged(this::mouseDragged);
        setOnMouseMoved(this::mouseMoved);
        setOnMouseReleased(this::mouseReleased);
        setOnKeyPressed(this::keyPressed);
        
        // Request focus for key events
        setFocusTraversable(true);
        requestFocus();

        startGameLoop();
    }

    private void setupStandardRack(double width, double height) {
        double baseX = width * 0.75;
        double baseY = height / 2;
        double spacingX = 25;
        double spacingY = 15;
        int num = 1;
        for (int row = 0; row < 5 && num <= 15; row++) {
            double x = baseX + row * spacingX;
            for (int j = 0; j <= row && num <= 15; j++) {
                double y = baseY - row * spacingY + j * (2 * spacingY);
                table.balls.add(new Ball(x, y, num));
                num++;
            }
        }
    }

    private void setupStraightPoolRack(double width, double height) {
        double centerX = width * 0.75;
        double centerY = height / 2;
        double spacingX = 24;
        double spacingY = 24;

        // Rotated traditional straight pool arrangement: horizontal triangle
        // Ball 1 and 5 should occupy the two corner positions on the long side.
        int[][] columns = {
            {1},
            {2, 3},
            {4, 5, 6},
            {7, 8, 9, 10},
            {11, 12, 13, 14, 15}
        };

        // Build a flat list of the remaining balls, excluding 1 and 5
        List<Integer> remaining = new ArrayList<>();
        for (int n = 2; n <= 15; n++) {
            if (n != 5) {
                remaining.add(n);
            }
        }
        Collections.shuffle(remaining);

        // Place the rack, preserving 1 in the first column and 5 in one corner of the last column.
        int index = 0;
        for (int col = 0; col < columns.length; col++) {
            int[] colBalls = columns[col];
            int count = colBalls.length;
            for (int row = 0; row < count; row++) {
                int number;
                if (col == 0 && row == 0) {
                    number = 1;
                } else if (col == columns.length - 1 && row == count - 1) {
                    number = 5;
                } else {
                    number = remaining.get(index++);
                }
                double x = centerX + col * spacingX;
                double y = centerY + (row - (count - 1) / 2.0) * spacingY;
                table.balls.add(new Ball(x, y, number));
            }
        }
    }

    private void setupNineBallRack(double width, double height) {
        double centerX = width * 0.75;
        double centerY = height / 2;
        double spacingX = 24;
        double spacingY = 24;

        // 9-ball diamond rack oriented horizontally: columns of 1, 2, 3, 2, 1
        int[][] columns = {
            {1},
            {2, 3},
            {4, 9, 5},
            {6, 7},
            {8}
        };

        for (int col = 0; col < columns.length; col++) {
            int[] colBalls = columns[col];
            int count = colBalls.length;
            for (int row = 0; row < count; row++) {
                double x = centerX + col * spacingX;
                double y = centerY + (row - (count - 1) / 2.0) * spacingY;
                table.balls.add(new Ball(x, y, colBalls[row]));
            }
        }
    }

    private boolean isAITurnWaiting() {
        return game.aiMode != null
            && game.currentPlayer != null
            && game.players.indexOf(game.currentPlayer) == 1
            && game.state == GameState.AIMING;
    }

    private void mousePressed(MouseEvent e) {
        if (isAITurnWaiting()) {
            return;
        }

        // KONDISI 1: Jika sedang BALL_IN_HAND, klik untuk menaruh bola (Confirm)
        if (game.state == GameState.BALL_IN_HAND) {
            // Cek apakah posisi valid (tidak menumpuk bola lain & di dalam meja)
            game.state = GameState.AIMING;
            cue.show(); // Munculkan stick lagi
            cue.unlockAngle(); // Pastikan angle tidak terkunci
            return;
        }

        // KONDISI 2: Normal Aiming - Two Stage System
        if (game.state == GameState.AIMING && e.getButton() == MouseButton.PRIMARY) {
            if (!cue.isAngleLocked()) {
                // STAGE 1: Klik pertama untuk mengunci arah
                cue.lockAngle();
                mousePressed = true;
                dragStartX = e.getX();
                dragStartY = e.getY();
            }
        }
        
        // Klik kanan untuk membatalkan lock dan kembali ke aiming
        if (game.state == GameState.AIMING && e.getButton() == MouseButton.SECONDARY) {
            if (cue.isAngleLocked()) {
                cue.unlockAngle();
                mousePressed = false;
            }
        }
    }
    
    private void mouseDragged(MouseEvent e) {
        if (isAITurnWaiting()) {
            return;
        }

        // LOGIKA BARU: Jika Ball in Hand, bola putih ikut mouse
        if (game.state == GameState.BALL_IN_HAND) {
            Ball cueBall = table.balls.get(0);
            
            // Batasi agar bola tidak keluar dari meja (Clamping)
            double margin = table.PADDING + table.INNER_PADDING + cueBall.radius;
            double newX = Math.max(margin, Math.min(getWidth() - margin, e.getX()));
            double newY = Math.max(margin, Math.min(getHeight() - margin, e.getY()));
            
            // Cek apakah posisi baru menabrak bola lain
            boolean validPosition = true;
            for (Ball b : table.balls) {
                if (b == cueBall || b.sunk) continue;
                double dx = newX - b.x;
                double dy = newY - b.y;
                double dist = Math.sqrt(dx*dx + dy*dy);
                if (dist < cueBall.radius + b.radius + 2) {
                    validPosition = false;
                    break;
                }
            }
            
            if (validPosition) {
                cueBall.x = newX;
                cueBall.y = newY;
            }
            cueBall.vx = 0; // Pastikan diam
            cueBall.vy = 0;
            return; 
        }

        // STAGE 2: Jika angle sudah dikunci, drag untuk mengatur power
        if (cue.isAngleLocked() && mousePressed) {
            // Hitung power berdasarkan jarak drag dari titik awal
            // Arah maju (mendekati bola) = kurangi power
            // Arah mundur (menjauhi bola) = tambah power
            double dx = e.getX() - dragStartX;
            double dy = e.getY() - dragStartY;
            
            // Proyeksikan gerakan mouse ke arah stick (berlawanan dengan arah tembak)
            double stickDirX = -Math.cos(cue.angle);
            double stickDirY = -Math.sin(cue.angle);
            
            // Dot product untuk mendapatkan jarak sepanjang arah stick
            double pullDistance = dx * stickDirX + dy * stickDirY;
            
            // Hanya hitung power jika menarik mundur (pullDistance > 0)
            cue.setPower(Math.max(0, pullDistance));
        }
    }

    private void mouseMoved(MouseEvent e) {
        if (isAITurnWaiting()) {
            return;
        }

        // Update aim line saat mouse bergerak (hanya jika angle belum dikunci)
        if (game.state == GameState.AIMING && !cue.isAngleLocked()) {
            cue.updateAngle(e.getX(), e.getY());
        }
    }
    
    private void keyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.ESCAPE) {
            if (!isPaused) {
                isPaused = true;
                showPauseMenu();
            }
        }
    }
    
    private void showPauseMenu() {
        // Stop the game loop
        loop.stop();
        
        PauseMenu pauseMenu = new PauseMenu(this, game.frame, game);
        
        pauseMenu.setOnContinue(() -> {
            isPaused = false;
            loop.start();
            requestFocus(); // Restore focus
        });
        
        pauseMenu.setOnReset(() -> {
            loop.stop();
            // Remove pause menu from parent
            gameCenter.getChildren().removeIf(node -> node instanceof PauseMenu);
            game.startNewGame();
        });
        
        pauseMenu.setOnMainMenu(() -> {
            loop.stop();
            game.frame.close();
            MainMenu menu = new MainMenu(game.frame, game);
            menu.show();
        });
        
        // Add pause menu overlay to the center StackPane
        gameCenter.getChildren().add(pauseMenu);
        pauseMenu.requestFocus();
    }
    private void mouseReleased(MouseEvent e) {
        if (isAITurnWaiting()) {
            return;
        }

        if (game.state == GameState.BALL_IN_HAND) return; // Jangan nembak pas mindahin bola
        if (e.getButton() == MouseButton.SECONDARY) return; // Ignore right click release

        if (!mousePressed) return;
        mousePressed = false;
        
        // Hanya tembak jika ada power (sudah ditarik)
        if (cue.getPowerPercent() > 0.01) {
            // Reset flag collision sebelum menembak
            game.cueBallHitAnyBall = false; 

            cue.shoot();
            cue.hide();
            game.state = GameState.BALLS_MOVING;
        } else {
            // Jika tidak ada power, batalkan dan kembali ke aiming
            cue.unlockAngle();
        }
    }

    public void paintComponent(GraphicsContext g) {
        try {
            // clear
            g.clearRect(0, 0, getWidth(), getHeight());
            // render table
            table.render(g);
            // render cue with power visualization
            cue.render(g, cue.getPowerPercent());
        } catch (Exception e) {
            System.err.println("Error rendering: " + e.getMessage());
        }
    }

    public void updateGameLoop() {
        try {
            double dt = 1.0 / 60.0;
            
            // Update AI mode if active
            if (game.aiMode != null) {
                game.aiMode.update(dt);
            }

            if (cue != null) {
                cue.updateAnimation(dt);
            }
            
            // Update table (sudah include semua collision & friction)
            table.update(dt);
            
            // --- DETEKSI APAKAH CUE BALL MENABRAK ---
            Ball cueBall = table.balls.get(0);
            if (!cueBall.sunk && cueBall.isMoving()) {
                for (Ball b : table.balls) {
                    if (b == cueBall || b.sunk) continue;
                    
                    double dx = cueBall.x - b.x;
                    double dy = cueBall.y - b.y;
                    double dist = Math.sqrt(dx*dx + dy*dy);
                    
                    // Jika nempel, anggap tabrakan
                    if (dist <= cueBall.radius + b.radius + 0.1) {
                        game.cueBallHitAnyBall = true;
                    }
                }
            }
            // HAPUS baris ini karena sudah ada di table.update():
            // PhysicsEngine.checkAllBallCollisions(table.balls); // <-- HAPUS!
            
            // Check pocket collision
            PhysicsEngine.handlePocketedBalls(table, game);
            
            // Update cue visibility
            if (cue != null && cue.cueBall != null) {
                if (cue.cueBall.isMoving()) {
                    cue.hide();
                } else {
                    if (!cue.cueBall.sunk) cue.show();
                }
            }

        // Check game state
        if (table.areBallsStopped()) {
            if (game.state == GameState.BALLS_MOVING) {
                // Cek apakah game over (bola 8 masuk dengan benar)
                if (game.state == GameState.GAME_OVER) {
                    return; // Game sudah selesai
                }
                
                // Cek Foul: Jika bola putih TIDAK mengenai bola apapun (No Hit)
                if (!game.cueBallHitAnyBall) {
                    System.out.println("FOUL: No ball hit!");
                    game.foulThisTurn = true;
                    game.currentPlayer.fouled = true;
                }
                
                // Logika giliran:
                // - Jika FOUL: giliran berganti + lawan dapat ball-in-hand
                // - Jika SCORE (tanpa foul): giliran tetap
                // - Jika tidak score dan tidak foul: giliran berganti biasa
                if (game.foulThisTurn) {
                    game.switchTurn();
                    game.triggerBallInHand(); // Lawan dapat ball-in-hand
                } else if (game.scoredThisTurn) {
                    // Tetap giliran pemain ini, reset turn state
                    game.startTurn();
                    game.state = GameState.AIMING;
                    cue.show();
                    cue.unlockAngle();
                } else {
                    // Tidak score, tidak foul: giliran berganti biasa
                    game.switchTurn();
                }
            }
        }
    } catch (Exception e) {
        System.err.println("Error in game loop: " + e.getMessage());
    }
}
    private void startGameLoop() {
        GraphicsContext gc = getGraphicsContext2D();
        loop = new AnimationTimer() {
            long last = 0;
            @Override
            public void handle(long now) {
                try {
                    if (last == 0) last = now;
                    double elapsed = (now - last) / 1e9;
                    if (elapsed >= 1.0/60.0) {
                        updateGameLoop();
                        game.updateHud(); // Update HUD including stopwatch every frame
                        paintComponent(gc);
                        last = now;
                    }
                } catch (Exception e) {
                    System.err.println("AnimationTimer error: " + e.getMessage());
                }
            }
        };
        loop.start();
    }

    public void stop() {
        if (loop != null) loop.stop();
    }
}
