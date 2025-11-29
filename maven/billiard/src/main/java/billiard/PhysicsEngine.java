package billiard;

import java.util.List;

public class PhysicsEngine {
    // Coefficient of restitution (elasticity) for ball-to-ball collisions
    private static final double RESTITUTION = 0.95;
    
    // Coefficient of restitution for wall collisions
    private static final double WALL_RESTITUTION = 0.92;

    /**
     * Resolve elastic collision between two balls using proper physics.
     * Accounts for equal mass, momentum conservation, and energy loss (restitution).
     */
    public static void resolveBallCollision(Ball a, Ball b) {
        if (a.sunk || b.sunk) return;
        
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        
        if (dist == 0 || dist > a.radius + b.radius) return;
        
        double minDist = a.radius + b.radius;
        
        // Separate overlapping balls
        double overlap = 0.5 * (minDist - dist + 0.01);
        double nx = dx / dist;
        double ny = dy / dist;
        a.x -= nx * overlap;
        a.y -= ny * overlap;
        b.x += nx * overlap;
        b.y += ny * overlap;

        // Relative velocity
        double dvx = b.vx - a.vx;
        double dvy = b.vy - a.vy;
        
        // Relative velocity along collision normal
        double dvn = dvx * nx + dvy * ny;
        
        // Only resolve if balls are moving toward each other
        if (dvn >= 0) return;

        // For equal mass elastic collision:
        // impulse = -(1 + e) * dvn / 2, where e is restitution
        double impulse = -(1.0 + RESTITUTION) * dvn / 2.0;
        
        // Apply impulse
        a.vx -= impulse * nx;
        a.vy -= impulse * ny;
        b.vx += impulse * nx;
        b.vy += impulse * ny;
    }

    /**
     * Apply realistic wall collision with energy loss and bounce.
     */
    public static void applyWallCollision(Ball ball, boolean hitVertical) {
        if (hitVertical) {
            // Vertical wall (left or right)
            ball.vx *= -WALL_RESTITUTION;
        } else {
            // Horizontal wall (top or bottom)
            ball.vy *= -WALL_RESTITUTION;
        }
    }

    /**
     * Apply friction to the ball based on its velocity.
     * Simulates rolling resistance and air resistance.
     */
    public static void applyFriction(Ball ball, double dt) {
        double speed = Math.sqrt(ball.vx * ball.vx + ball.vy * ball.vy);
        
        if (speed < 0.01) {
            // Stop ball if it's moving very slowly
            ball.vx = 0;
            ball.vy = 0;
            return;
        }
        
        // Quadratic drag (more realistic at higher speeds)
        // F = -0.5 * rho * A * Cd * v^2
        // Simplified: friction factor increases with speed
        double dragFactor = 0.98;  // Rolling/sliding friction
        
        ball.vx *= dragFactor;
        ball.vy *= dragFactor;
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

    public static void handlePocketedBalls(Table table, BilliardGame game) {
        for (Ball b : table.balls) {
            if (!b.sunk) {
                for (Pocket p : table.pockets) {
                    if (b.checkPocketCollision(p)) {
                        game.onBallPocketed(b);
                    }
                }
            }
        }
    }
}
