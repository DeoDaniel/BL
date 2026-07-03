package billiard;

import java.util.List;

public class PhysicsEngine {
    // Coefficient of restitution (elasticity) for ball-to-ball collisions
    // Real billiard balls: ~0.92-0.93 (phenolic resin)
    private static final double RESTITUTION = 0.93;
    
    // Coefficient of restitution for wall collisions
    // Rails absorb more energy than ball-to-ball collisions
    private static final double WALL_RESTITUTION = 0.88;
    
    // Minimum speed threshold below which balls stop completely
    // Lower threshold for finer movement control
    private static final double MIN_SPEED_THRESHOLD = 0.1;

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
        if (Math.abs(a.vx) < MIN_SPEED_THRESHOLD && Math.abs(a.vy) < MIN_SPEED_THRESHOLD) {
            if (a.vx * a.vx + a.vy * a.vy < MIN_SPEED_THRESHOLD * MIN_SPEED_THRESHOLD) {
                a.vx = 0;
                a.vy = 0;
            }
        }
        if (Math.abs(b.vx) < MIN_SPEED_THRESHOLD && Math.abs(b.vy) < MIN_SPEED_THRESHOLD) {
            if (b.vx * b.vx + b.vy * b.vy < MIN_SPEED_THRESHOLD * MIN_SPEED_THRESHOLD) {
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
     * Simulates rolling resistance on the billiard table.
     * Realistic decay: ~8-15% velocity loss per second depending on table condition.
     */
    public static void applyFriction(Ball ball, double dt) {
        double speedSq = ball.vx * ball.vx + ball.vy * ball.vy;
        double speed = Math.sqrt(speedSq);
        
        // Stop completely if below threshold
        if (speed < MIN_SPEED_THRESHOLD) {
            ball.vx = 0;
            ball.vy = 0;
            return;
        }
        
        // Rolling resistance coefficient (~0.985 = ~1.5% loss per frame)
        // Real billiard tables: ball can roll for 20-30 seconds from initial shot
        double rollingFriction = 0.985;
        
        // Air resistance is minimal on a billiard table - mostly rolling resistance
        // Very slight quadratic drag for high speeds
        double airDragCoefficient = 0.00005;
        double airDrag = 1.0 - (airDragCoefficient * speed);
        airDrag = Math.max(0.98, airDrag); // Minimal impact
        
        // Combined friction (dominated by rolling resistance)
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
