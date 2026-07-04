package billiard;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Ball {
    public double x;
    public double y;
    public double vx;
    public double vy;
    public double radius = 10;
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

        double d = radius * 2;
        double left = x - radius;
        double top = y - radius;

        // CUE BALL
        if (isCueBall()) {
            g.setFill(Color.WHITE);
            g.fillOval(left, top, d, d);
            return;
        }

        // Tentukan warna berdasarkan nomor
        Color mainColor;
        if (number == 8 || number == 9) {
            mainColor = Color.BLACK;
        } else {
            int idx = isSolid() ? number : number - 8; // 1..7
            switch (idx) {
                case 1:
                    mainColor = Color.YELLOW;
                    break;
                case 2:
                    mainColor = Color.BLUE;
                    break;
                case 3:
                    mainColor = Color.RED;
                    break;
                case 4:
                    mainColor = Color.PURPLE;
                    break;
                case 5:
                    mainColor = Color.ORANGE;
                    break;
                case 6:
                    mainColor = Color.GREEN;
                    break;
                case 7:
                    mainColor = Color.BROWN;
                    break;
                default:
                    mainColor = Color.GRAY;
            }
        }

        if (isStripe()) {
            // dasar putih
            g.setFill(Color.WHITE);
            g.fillOval(left, top, d, d);

            // stripe warna di tengah
            g.setFill(mainColor);
            double stripeHeight = d * 0.4;
            double stripeTop = y - stripeHeight / 2.0;
            g.fillRect(left, stripeTop, d, stripeHeight);
        } else {
            // solid (termasuk bola 8)
            g.setFill(mainColor);
            g.fillOval(left, top, d, d);
        }

        // lingkaran putih kecil di tengah (area nomor)
        g.setFill(Color.WHITE);
        g.fillOval(x - radius * 0.4, y - radius * 0.4, radius * 0.8, radius * 0.8);
    }

    public void applyForce(double power, double angle) {
        // Simplified impulse
        vx += Math.cos(angle) * power;
        vy += Math.sin(angle) * power;
    }

    public void checkWallCollision(Table table) {
        if (sunk) return;

        double left   = table.PADDING + table.INNER_PADDING + radius;
        double right  = table.WIDTH - table.PADDING - table.INNER_PADDING - radius;
        double top    = table.PADDING + table.INNER_PADDING + radius;
        double bottom = table.HEIGHT - table.PADDING - table.INNER_PADDING - radius;

        // LEFT
        if (x < left) {
            x = left;
            PhysicsEngine.applyWallCollision(this, true);
        }

        // RIGHT
        if (x > right) {
            x = right;
            PhysicsEngine.applyWallCollision(this, true);
        }

        // TOP
        if (y < top) {
            y = top;
            PhysicsEngine.applyWallCollision(this, false);
        }

        // BOTTOM
        if (y > bottom) {
            y = bottom;
            PhysicsEngine.applyWallCollision(this, false);
        }
    }


    public boolean checkPocketCollision(Pocket pocket) {
        if (sunk) return false;
        double dx = x - pocket.x;
        double dy = y - pocket.y;
        double distSq = dx * dx + dy * dy;
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
