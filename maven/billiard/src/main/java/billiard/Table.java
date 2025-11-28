package billiard;

import javafx.scene.canvas.GraphicsContext;
import java.util.ArrayList;
import java.util.List;

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

        // 6 pocket: 3 atas, 3 bawah
        pockets[0] = new Pocket(leftX, topY);
        pockets[1] = new Pocket(WIDTH / 2, topY);
        pockets[2] = new Pocket(rightX, topY);

        pockets[3] = new Pocket(leftX, bottomY);
        pockets[4] = new Pocket(WIDTH / 2, bottomY);
        pockets[5] = new Pocket(rightX, bottomY);
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

        // ==== 3. Light green cushion edge ====
        g.setFill(javafx.scene.paint.Color.web("#1db34b"));
        g.fillRect(PADDING, PADDING, WIDTH - PADDING * 2, HEIGHT - PADDING * 2);

        // ==== 4. Main playing field ====
        javafx.scene.paint.Color darkGreen = javafx.scene.paint.Color.web("#0a8f2d");
        javafx.scene.paint.Color midGreen  = javafx.scene.paint.Color.web("#1fc255");

        g.setFill(darkGreen);
        g.fillRect(
            PADDING + INNER_PADDING,
            PADDING + INNER_PADDING,
            WIDTH - (PADDING + INNER_PADDING) * 2,
            HEIGHT - (PADDING + INNER_PADDING) * 2
        );

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
        for (Ball b : balls) {
            b.update(dt);
            b.checkWallCollision(this);

            b.vx *= Math.pow(FRICTION, dt * 60);
            b.vy *= Math.pow(FRICTION, dt * 60);
        }
    }

    public boolean areBallsStopped() {
        for (Ball b : balls) {
            if (!b.sunk && b.isMoving()) return false;
        }
        return true;
    }
}
