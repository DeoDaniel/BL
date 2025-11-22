package billiard;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

public class Table {
    public final double WIDTH = 1120;
    public final double HEIGHT = 560;
    public final double FRICTION = 0.97;
    public Pocket[] pockets = new Pocket[6];
    public List<Ball> balls = new ArrayList<>();

    public Table() {
        // set pockets (corners and middle of long sides)
        pockets[0] = new Pocket(0, 0);
        pockets[1] = new Pocket(WIDTH/2, 0);
        pockets[2] = new Pocket(WIDTH, 0);
        pockets[3] = new Pocket(0, HEIGHT);
        pockets[4] = new Pocket(WIDTH/2, HEIGHT);
        pockets[5] = new Pocket(WIDTH, HEIGHT);
    }

public void render(GraphicsContext g) {
    // warna meja hijau
    g.setFill(javafx.scene.paint.Color.DARKGREEN);
    g.fillRect(0, 0, WIDTH, HEIGHT);

    // warna lubang hitam
    g.setFill(javafx.scene.paint.Color.BLACK);
    for (Pocket p : pockets) {
        g.fillOval(p.x - p.radius, p.y - p.radius, p.radius*2, p.radius*2);
    }

    // render bola
    for (Ball b : balls) {
        b.render(g);
    }
}


    public void update(double dt) {
        for (Ball b : balls) {
            b.update(dt);
            b.checkWallCollision(this);
            // apply friction
            b.vx *= Math.pow(FRICTION, dt*60); // scaled by dt
            b.vy *= Math.pow(FRICTION, dt*60);
        }
    }

    public boolean areBallsStopped() {
        for (Ball b : balls) {
            if (!b.sunk && b.isMoving()) return false;
        }
        return true;
    }
}
