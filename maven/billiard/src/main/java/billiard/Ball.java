package billiard;

import javafx.scene.canvas.GraphicsContext;

public class Ball {
    public double x;
    public double y;
    public double vx;
    public double vy;
    public double radius = 11.5;
    public int number; // 0 = cue ball, 1-15 object balls
    public boolean sunk = false;

    public Ball(double x, double y, int number) {
        this.x = x;
        this.y = y;
        this.number = number;
        this.vx = 0;
        this.vy = 0;
    }

    public boolean isCueBall() {
        return number == 0;
    }

    public boolean isSolid() {
        return number >= 1 && number <= 7;
    }

    public boolean isStripe() {
        return number >= 9 && number <= 15;
    }

    public void update(double dt) {
        if (sunk) return;
        x += vx * dt;
        y += vy * dt;
    }

public void render(GraphicsContext g) {
    if (sunk) return;

    if (number == 0) {
        g.setFill(javafx.scene.paint.Color.WHITE); // cue ball
    } else {
        g.setFill(javafx.scene.paint.Color.ORANGE); // sementara untuk test
    }

    g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
}


    public void applyForce(double power, double angle) {
        // Simplified impulse
        vx += Math.cos(angle) * power;
        vy += Math.sin(angle) * power;
    }

    public void checkWallCollision(Table table) {
        if (sunk) return;
        // left/right
        if (x - radius < 0) {
            x = radius;
            vx = -vx;
        } else if (x + radius > table.WIDTH) {
            x = table.WIDTH - radius;
            vx = -vx;
        }
        // top/bottom
        if (y - radius < 0) {
            y = radius;
            vy = -vy;
        } else if (y + radius > table.HEIGHT) {
            y = table.HEIGHT - radius;
            vy = -vy;
        }
    }

    public boolean checkPocketCollision(Pocket pocket) {
        if (sunk) return false;
        double dx = x - pocket.x;
        double dy = y - pocket.y;
        double distSq = dx*dx + dy*dy;
        double rsum = radius + pocket.radius;
        if (distSq <= rsum * rsum) {
            sunk = true;
            vx = vy = 0;
            return true;
        }
        return false;
    }

    public boolean isMoving() {
        double speedSq = vx * vx + vy * vy;
        return speedSq > 1e-2;
    }
}
