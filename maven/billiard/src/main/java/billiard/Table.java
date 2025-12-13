package billiard;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;

public class Table {
    public final double WIDTH = 1120;
    public final double HEIGHT = 560;
    public final double FRICTION = 0.97;

    public final double PADDING = 40;      // sama dengan render()
    public final double INNER_PADDING = 20;

    public Pocket[] pockets = new Pocket[6];
    public List<Ball> balls = new ArrayList<>();

    public Table() {
        // posisi tepi meja hijau (berdasarkan padding)
        double leftX   = PADDING;
        double rightX  = WIDTH - PADDING;
        double topY    = PADDING;
        double bottomY = HEIGHT - PADDING;
        double cornerOffset = 12; // jarak pocket dari sudut meja

        // 6 pocket: 3 atas, 3 bawah
        pockets[0] = new Pocket(leftX + cornerOffset, topY + cornerOffset);
        pockets[1] = new Pocket(WIDTH / 2, topY);
        pockets[2] = new Pocket(rightX - cornerOffset, topY + cornerOffset);

        pockets[3] = new Pocket(leftX + cornerOffset, bottomY - cornerOffset);
        pockets[4] = new Pocket(WIDTH / 2, bottomY);
        pockets[5] = new Pocket(rightX - cornerOffset, bottomY - cornerOffset);
    }


    public void render(GraphicsContext g) {
        // ==== 1. Background (border kayu) ====
        g.setFill(javafx.scene.paint.Color.SADDLEBROWN);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // ==== 2. Inner dark corners ====
        g.setFill(javafx.scene.paint.Color.DARKSLATEGRAY);
        double cornerSize = 80;
        g.fillRect(0, 0, cornerSize, cornerSize);
        g.fillRect(WIDTH - cornerSize, 0, cornerSize, cornerSize);
        g.fillRect(0, HEIGHT - cornerSize, cornerSize, cornerSize);
        g.fillRect(WIDTH - cornerSize, HEIGHT - cornerSize, cornerSize, cornerSize);

        // ==== 3. Main playing field ====
        javafx.scene.paint.Color darkGreen = javafx.scene.paint.Color.web("#0a8f2d");
        javafx.scene.paint.Color midGreen  = javafx.scene.paint.Color.web("#1fc255");

        g.setFill(darkGreen);
        g.fillRect(
            PADDING + INNER_PADDING - 15,
            PADDING + INNER_PADDING - 15,
            WIDTH - (PADDING + INNER_PADDING) * 1.5,
            HEIGHT - (PADDING + INNER_PADDING) * 1.5
        );

        // ==== 4. Light green cushion edge (trapezoid polygons) ====
        javafx.scene.paint.Color cushionColor = javafx.scene.paint.Color.web("#1db34b");
        double cushionWidth = INNER_PADDING + 1;
        double centerX = WIDTH / 2;
        double pocketGapWidth = 30;  // Gap width for middle pocket
        
        // Top-left cushion trapezoid
        g.setFill(cushionColor);
        double[] topLeftXs = {
            PADDING + cushionWidth, 
            centerX - pocketGapWidth / 2, 
            centerX - pocketGapWidth / 2 - cushionWidth * 0.5, 
            PADDING + cushionWidth * 2.2
        };
        double[] topLeftYs = {
            PADDING, 
            PADDING, 
            PADDING + cushionWidth, 
            PADDING + cushionWidth
        };
        g.fillPolygon(topLeftXs, topLeftYs, 4);
        
        // Top-right cushion trapezoid
        double[] topRightXs = {
            centerX + pocketGapWidth / 2, 
            WIDTH - PADDING - cushionWidth, 
            WIDTH - PADDING - cushionWidth * 2.1, 
            centerX + pocketGapWidth / 2 + cushionWidth * 0.5
        };
        double[] topRightYs = {
            PADDING, 
            PADDING, 
            PADDING + cushionWidth, 
            PADDING + cushionWidth
        };
        g.fillPolygon(topRightXs, topRightYs, 4);
        
        // Bottom-left cushion trapezoid
        double[] bottomLeftXs = {
            PADDING + cushionWidth * 2.1, 
            centerX - pocketGapWidth / 2 - cushionWidth * 0.5, 
            centerX - pocketGapWidth / 2, 
            PADDING + cushionWidth
        };
        double[] bottomLeftYs = {
            HEIGHT - PADDING - cushionWidth, 
            HEIGHT - PADDING - cushionWidth, 
            HEIGHT - PADDING, 
            HEIGHT - PADDING
        };
        g.fillPolygon(bottomLeftXs, bottomLeftYs, 4);
        
        // Bottom-right cushion trapezoid
        double[] bottomRightXs = {
            centerX + pocketGapWidth / 2, 
            WIDTH - PADDING - cushionWidth, 
            WIDTH - PADDING - cushionWidth * 2.1, 
            centerX + pocketGapWidth / 2 + cushionWidth * 0.5
        };
        double[] bottomRightYs = {
            HEIGHT - PADDING, 
            HEIGHT - PADDING, 
            HEIGHT - PADDING - cushionWidth, 
            HEIGHT - PADDING - cushionWidth
        };
        g.fillPolygon(bottomRightXs, bottomRightYs, 4);
        
        // Left cushion trapezoid
        double[] leftXs = {
            PADDING, 
            PADDING + cushionWidth, 
            PADDING + cushionWidth, 
            PADDING
        };
        double[] leftYs = {
            PADDING + cushionWidth, 
            PADDING + cushionWidth * 2.1, 
            HEIGHT - PADDING - cushionWidth * 2.1, 
            HEIGHT - PADDING - cushionWidth
        };
        g.fillPolygon(leftXs, leftYs, 4);
        
        // Right cushion trapezoid
        double[] rightXs = {
            WIDTH - PADDING - cushionWidth, 
            WIDTH - PADDING, 
            WIDTH - PADDING, 
            WIDTH - PADDING - cushionWidth
        };
        double[] rightYs = {
            PADDING + cushionWidth * 2.1, 
            PADDING + cushionWidth, 
            HEIGHT - PADDING - cushionWidth, 
            HEIGHT - PADDING - cushionWidth * 2.1
        };
        g.fillPolygon(rightXs, rightYs, 4);

        // gradient highlight center
        g.setFill(midGreen.deriveColor(0, 1, 1, 0.25));
        g.fillOval(WIDTH / 2 - 300, HEIGHT / 2 - 300, 600, 600);

        // ==== 5. Draw pockets ====
        g.setFill(javafx.scene.paint.Color.BLACK);
        for (Pocket p : pockets) {
            g.fillOval(p.x - p.radius, p.y - p.radius, p.radius * 2, p.radius * 2);
        }

        // ==== 6. Draw balls ====
        for (Ball b : balls) {
            b.render(g);
        }
    }


    public void update(double dt) {
        // 1. Update posisi semua bola (tanpa friction dulu)
        for (Ball b : balls) {
            if (!b.sunk) {
                b.update(dt);
            }
        }
        
        // 2. Check collision antar bola (PENTING!)
        // Ini sudah dipanggil di GamePanel, tapi lebih baik ada di sini juga
        for (int i = 0; i < balls.size(); i++) {
            Ball b1 = balls.get(i);
            if (b1.sunk) continue;
            
            for (int j = i + 1; j < balls.size(); j++) {
                Ball b2 = balls.get(j);
                if (b2.sunk) continue;
                
                PhysicsEngine.resolveBallCollision(b1, b2);
                
                // --- LOGIKA DETEKSI HIT ---
                // Jika salah satu bola adalah Cue Ball (nomor 0)
                // Dan collision terjadi (kita cek sederhana: apakah velocity berubah drastis? 
                // Atau hitung jarak. Tapi karena resolveBallCollision void, kita pakai trik jarak di sini)
                
                double dx = b1.x - b2.x;
                double dy = b1.y - b2.y;
                double dist = Math.sqrt(dx*dx + dy*dy);
                
                // Jika mereka bersentuhan
                if (dist <= b1.radius + b2.radius + 0.5) { // +0.5 toleransi
                    if (b1.isCueBall() || b2.isCueBall()) {
                        // Kita butuh akses ke variable game.cueBallHitAnyBall.
                        // Karena Table.java tidak pegang instance BilliardGame secara langsung,
                        // cara paling cepat: set variabel static atau public static di BilliardGame,
                        // ATAU instance BilliardGame harus di-pass ke table.
                        
                        // SOLUSI MUDAH:
                        // Anggaplah kita akses via static method (Not ideal but works)
                        // ATAU lebih baik: Logika ini dipindah ke GamePanel.
                    }
                }
            }
        }    

        // 3. Check wall collision SETELAH ball collision
        for (Ball b : balls) {
            if (!b.sunk) {
                b.checkWallCollision(this);
            }
        }
        
        // 4. Apply friction SEKALI SAJA di akhir
        for (Ball b : balls) {
            if (!b.sunk) {
                PhysicsEngine.applyFriction(b, dt);
            }
        }
    }

    public boolean areBallsStopped() {
        for (Ball b : balls) {
            if (!b.sunk && b.isMoving()) return false;
        }
        return true;
    }
}