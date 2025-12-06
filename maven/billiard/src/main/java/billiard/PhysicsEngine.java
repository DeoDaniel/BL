package billiard;

import java.util.List;

public class PhysicsEngine {
    // Coefficient of restitution (elasticity) for ball-to-ball collisions
    // Tinggi = lebih elastis/bouncy
    private static final double RESTITUTION = 0.98;
    
    // Coefficient of restitution for wall collisions
    private static final double WALL_RESTITUTION = 0.95;
    
    // Minimum speed threshold
    private static final double MIN_SPEED = 0.3;

    /**
     * Resolve elastic collision between two balls using proper physics.
     * Accounts for equal mass, momentum conservation, and energy loss (restitution).
     * Improved version with better separation and more accurate physics.
     */
    public static void resolveBallCollision(Ball a, Ball b) {
        if (a.sunk || b.sunk) return;
        
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double distSq = dx * dx + dy * dy;
        double minDist = a.radius + b.radius;
        
        // Quick rejection test
        if (distSq > minDist * minDist || distSq == 0) return;
        
        double dist = Math.sqrt(distSq);
        
        // Normalized collision normal
        double nx = dx / dist;
        double ny = dy / dist;
        
        // === STEP 1: Separate overlapping balls ===
        // Push balls apart so they don't overlap
        double overlap = minDist - dist;
        if (overlap > 0) {
            double separation = overlap / 2.0 + 0.1; // Extra 0.1 to prevent sticking
            a.x -= nx * separation;
            a.y -= ny * separation;
            b.x += nx * separation;
            b.y += ny * separation;
        }

        // === STEP 2: Calculate relative velocity ===
        double dvx = a.vx - b.vx;
        double dvy = a.vy - b.vy;
        
        // Relative velocity along collision normal
        double dvn = dvx * nx + dvy * ny;
        
        // Only resolve if balls are moving toward each other
        if (dvn <= 0) return;

        // === STEP 3: Calculate impulse ===
        // For equal mass elastic collision with restitution:
        // J = (1 + e) * dvn / 2
        double impulse = (1.0 + RESTITUTION) * dvn / 2.0;
        
        // === STEP 4: Apply impulse ===
        a.vx -= impulse * nx;
        a.vy -= impulse * ny;
        b.vx += impulse * nx;
        b.vy += impulse * ny;
        
        // === STEP 5: Clamp very small velocities ===
        if (Math.abs(a.vx) < MIN_SPEED && Math.abs(a.vy) < MIN_SPEED) {
            if (a.vx * a.vx + a.vy * a.vy < MIN_SPEED * MIN_SPEED) {
                a.vx = 0;
                a.vy = 0;
            }
        }
        if (Math.abs(b.vx) < MIN_SPEED && Math.abs(b.vy) < MIN_SPEED) {
            if (b.vx * b.vx + b.vy * b.vy < MIN_SPEED * MIN_SPEED) {
                b.vx = 0;
                b.vy = 0;
            }
        }
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
     * Improved with speed-dependent friction.
     */
    public static void applyFriction(Ball ball, double dt) {
        double speed = Math.sqrt(ball.vx * ball.vx + ball.vy * ball.vy);
        
        if (speed < 0.3) {
            // Stop ball if it's moving very slowly
            ball.vx = 0;
            ball.vy = 0;
            return;
        }
        
        // Rolling friction (konstan)
        double rollingFriction = 0.996;
        
        // Air friction (quadratic drag) - semakin cepat semakin besar hambatannya
        double airDragCoefficient = 0.0001;
        double airDrag = 1.0 - (airDragCoefficient * speed);
        airDrag = Math.max(0.95, airDrag); // Clamp agar tidak terlalu ekstrem
        
        // Combined friction
        double totalFriction = rollingFriction * airDrag;
        
        // Apply friction
        ball.vx *= totalFriction;
        ball.vy *= totalFriction;
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
