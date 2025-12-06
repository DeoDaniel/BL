package billiard;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class GamePanel extends Canvas {
    public Table table;
    public Cue cue;
    public boolean mousePressed = false;
    private double dragStartX, dragStartY;
    private BilliardGame game;

    private AnimationTimer loop;

    public GamePanel(double width, double height, BilliardGame game) {
        super(width, height);
        this.game = game;
        table = new Table();
        // create cue ball
        Ball cueBall = new Ball(width * 0.25, height/2, 0);
        table.balls.add(cueBall);
        // add balls in a standard 8-ball triangular rack (balls 1..15)
        double baseX = width * 0.75;
        double baseY = height / 2;
        double spacingX = 25; // horizontal spacing between rows
        double spacingY = 15; // vertical half-spacing between balls
        int num = 1;
        for (int row = 0; row < 5 && num <= 15; row++) {
            double x = baseX + row * spacingX;
            for (int j = 0; j <= row && num <= 15; j++) {
                double y = baseY - row * spacingY + j * (2 * spacingY);
                table.balls.add(new Ball(x, y, num));
                num++;
            }
        }
        cue = new Cue(cueBall);
        cue.setTableReference(table); // Set reference untuk aim line

        setOnMousePressed(this::mousePressed);
        setOnMouseDragged(this::mouseDragged);
        setOnMouseMoved(this::mouseMoved);
        setOnMouseReleased(this::mouseReleased);

        startGameLoop();
    }

    private void mousePressed(MouseEvent e) {
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
        // Update aim line saat mouse bergerak (hanya jika angle belum dikunci)
        if (game.state == GameState.AIMING && !cue.isAngleLocked()) {
            cue.updateAngle(e.getX(), e.getY());
        }
    }
    
    private void mouseReleased(MouseEvent e) {
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
