package billiard;

import java.util.List;

public class PhysicsEngine {

    // resolve simple elastic collision between two balls
    public static void resolveBallCollision(Ball a, Ball b) {
        if (a.sunk || b.sunk) return;
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double dist = Math.sqrt(dx*dx + dy*dy);
        if (dist == 0) return;
        double minDist = a.radius + b.radius;
        if (dist < minDist) {
            // push them apart
            double overlap = 0.5 * (minDist - dist);
            double nx = dx/dist;
            double ny = dy/dist;
            a.x -= nx * overlap;
            a.y -= ny * overlap;
            b.x += nx * overlap;
            b.y += ny * overlap;

            // relative velocity
            double dvx = b.vx - a.vx;
            double dvy = b.vy - a.vy;
            double impactSpeed = dvx * nx + dvy * ny;
            if (impactSpeed > 0) return;

            double impulse = -2 * impactSpeed / 2; // equal mass
            a.vx -= impulse * nx;
            a.vy -= impulse * ny;
            b.vx += impulse * nx;
            b.vy += impulse * ny;
        }
    }

    public static void applyFriction(Ball ball, double friction) {
        ball.vx *= friction;
        ball.vy *= friction;
    }

    public static void checkAllBallCollisions(List<Ball> balls) {
        int n = balls.size();
        for (int i = 0; i < n; i++) {
            Ball a = balls.get(i);
            for (int j = i + 1; j < n; j++) {
                Ball b = balls.get(j);
                resolveBallCollision(a, b);
            }
        }
    }

    public static void handlePocketedBalls(Table table, Player player) {
        for (Ball b : table.balls) {
            if (!b.sunk) {
                for (Pocket p : table.pockets) {
                    if (b.checkPocketCollision(p)) {
                        // assign to player if object ball
                        if (!b.isCueBall() && player != null) {
                            player.pocketBall(b);
                        } else if (b.isCueBall() && player != null) {
                            // cue ball pocketed - foul behaviour could be set
                            player.fouled = true;
                        }
                    }
                }
            }
        }
    }
}
