package billiard;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles Player vs AI game mode with difficulty-based AI logic.
 * AI difficulty levels:
 * - EASY: Random shots, lower accuracy, 50% power
 * - MEDIUM: Targets closest ball, medium accuracy, 70% power
 * - HARD: Targets best shot, high accuracy, full power
 */
public class PlayerVsAIMode {
    private final BilliardGame game;
    private final GamePanel gamePanel;
    private String aiDifficulty;
    private static final long AI_THINK_DELAY = 2500; // 2.5 seconds delay before AI shoots
    private long aiActionTime = 0;
    private boolean aiActionScheduled = false;
    private boolean aiTurnPending = false;

    public PlayerVsAIMode(BilliardGame game, GamePanel gamePanel) {
        this.game = game;
        this.gamePanel = gamePanel;
        this.aiDifficulty = Settings.getInstance().getAiDifficulty();
    }

    /**
     * Update AI logic each frame - should be called from GamePanel update
     */
    public void update(double deltaTime) {
        if (!isAITurn() || game.state != GameState.AIMING || areBallsMoving()) {
            if (!isAITurn()) {
                aiTurnPending = false;
                aiActionScheduled = false;
            }
            return;
        }

        if (!aiTurnPending) {
            aiTurnPending = true;
            aiActionScheduled = true;
            aiActionTime = System.currentTimeMillis();
            return;
        }

        if (aiActionScheduled && System.currentTimeMillis() - aiActionTime >= AI_THINK_DELAY) {
            executeAIMove();
            aiActionScheduled = false;
            aiTurnPending = false;
        }
    }

    public void onTurnStarted() {
        aiTurnPending = false;
        aiActionScheduled = false;
        aiActionTime = 0;
    }

    /**
     * Check if current player is AI
     */
    private boolean isAITurn() {
        if (game.currentPlayer == null) return false;
        // AI is player 2 (index 1)
        return game.players.indexOf(game.currentPlayer) == 1;
    }

    /**
     * Check if any balls are still moving
     */
    private boolean areBallsMoving() {
        return !gamePanel.table.areBallsStopped();
    }

    /**
     * Execute the AI move based on difficulty level
     */
    private void executeAIMove() {
        if (game.state != GameState.AIMING) {
            return; // Can only shoot in AIMING state
        }

        if (!isAITurn()) {
            return;
        }

        // Update difficulty from settings
        this.aiDifficulty = Settings.getInstance().getAiDifficulty();

        AIShot shot = null;

        if ("Easy".equals(aiDifficulty)) {
            shot = calculateEasyShot();
        } else if ("Hard".equals(aiDifficulty)) {
            shot = calculateHardShot();
        } else {
            // Medium difficulty (default)
            shot = calculateMediumShot();
        }

        if (shot != null) {
            executeShot(shot);
        }
    }

    /**
     * Easy difficulty: Random ball, lower accuracy, 50% power
     */
    private AIShot calculateEasyShot() {
        Ball cueBall = gamePanel.table.balls.get(0);
        List<Ball> shootableBalls = getShootableBalls();

        if (shootableBalls.isEmpty()) {
            return null;
        }

        // Pick random ball
        Ball target = shootableBalls.get((int) (Math.random() * shootableBalls.size()));

        // Add randomness to angle (±15 degrees)
        double angle = calculateAngleToTarget(cueBall, target);
        double randomOffset = (Math.random() - 0.5) * Math.toRadians(30);
        angle += randomOffset;

        // 50% power
        double power = 0.5 + Math.random() * 0.1; // 50-60% power

        return new AIShot(angle, power);
    }

    /**
     * Medium difficulty: Target closest ball, medium accuracy, 70% power
     */
    private AIShot calculateMediumShot() {
        Ball cueBall = gamePanel.table.balls.get(0);
        List<Ball> shootableBalls = getShootableBalls();

        if (shootableBalls.isEmpty()) {
            return null;
        }

        // Find closest ball
        Ball closestBall = shootableBalls.get(0);
        double closestDist = distance(cueBall, closestBall);

        for (Ball ball : shootableBalls) {
            double dist = distance(cueBall, ball);
            if (dist < closestDist) {
                closestDist = dist;
                closestBall = ball;
            }
        }

        // Calculate angle with small randomness (±5 degrees)
        double angle = calculateAngleToTarget(cueBall, closestBall);
        double randomOffset = (Math.random() - 0.5) * Math.toRadians(10);
        angle += randomOffset;

        // 70% power
        double power = 0.70 + Math.random() * 0.05; // 70-75% power

        return new AIShot(angle, power);
    }

    /**
     * Hard difficulty: Target best shot (closest to pocket), high accuracy, full power
     */
    private AIShot calculateHardShot() {
        Ball cueBall = gamePanel.table.balls.get(0);
        List<Ball> shootableBalls = getShootableBalls();

        if (shootableBalls.isEmpty()) {
            return null;
        }

        // Try to find a direct pocketing shot by checking pockets and clearance
        AIShot best = null;
        double bestPriority = Double.MAX_VALUE;

        for (Ball target : shootableBalls) {
            for (Pocket pocket : gamePanel.table.pockets) {
                // direction from target to pocket
                double tx = pocket.x - target.x;
                double ty = pocket.y - target.y;
                double distTP = Math.sqrt(tx * tx + ty * ty);
                if (distTP < 1e-6) continue;
                double ux = tx / distTP;
                double uy = ty / distTP;

                // contact point where the cue ball center should be at collision
                double contactDist = target.radius * 2.0; // two-ball centers separation for equal balls
                double contactX = target.x - ux * contactDist;
                double contactY = target.y - uy * contactDist;

                // Check path from target to pocket is clear (ignore the target itself)
                if (!isPathClear(target.x, target.y, pocket.x, pocket.y, target)) continue;

                // Check path from cue ball to contact point is clear (ignore cue and target)
                if (!isPathClear(cueBall.x, cueBall.y, contactX, contactY, target)) continue;

                // Compute angle to aim (from cue ball to contact point)
                double angle = Math.atan2(contactY - cueBall.y, contactX - cueBall.x);

                // Power proportional to distance (clamped)
                double d = Math.hypot(contactX - cueBall.x, contactY - cueBall.y);
                double power = Math.min(1.0, d / 150.0);

                // Prioritize shorter total path (cue->contact + target->pocket)
                double priority = d + distTP;
                if (priority < bestPriority) {
                    bestPriority = priority;
                    best = new AIShot(angle, Math.max(0.6, power)); // ensure decent power
                }
            }
        }

        if (best != null) return best;

        // Fallback: pick closest-to-pocket ball (previous behavior)
        Ball bestBall = shootableBalls.get(0);
        double bestScore = calculateDistanceToPocket(bestBall);

        for (Ball ball : shootableBalls) {
            double score = calculateDistanceToPocket(ball);
            if (score < bestScore) {
                bestScore = score;
                bestBall = ball;
            }
        }

        double angle = calculateAngleToTarget(cueBall, bestBall);
        double randomOffset = (Math.random() - 0.5) * Math.toRadians(4);
        angle += randomOffset;

        double power = 0.90 + Math.random() * 0.10;

        return new AIShot(angle, power);
    }

    /**
     * Check if the straight-line path between (x1,y1) and (x2,y2) is free of other balls.
     * Ignores the provided excludeBall (can be null).
     */
    private boolean isPathClear(double x1, double y1, double x2, double y2, Ball excludeBall) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double lenSq = dx * dx + dy * dy;
        for (Ball b : gamePanel.table.balls) {
            if (b.sunk) continue;
            if (b == excludeBall) continue;
            // skip cue ball endpoints
            // compute distance from b to segment
            double t = 0;
            if (lenSq > 0) {
                t = ((b.x - x1) * dx + (b.y - y1) * dy) / lenSq;
                t = Math.max(0, Math.min(1, t));
            }
            double projX = x1 + t * dx;
            double projY = y1 + t * dy;
            double dist = Math.hypot(b.x - projX, b.y - projY);
            // If another ball is within a blocking distance, consider blocked
            if (dist < b.radius * 2.0 - 2.0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get list of balls that can be shot (not sunk, not cue ball)
     */
    private List<Ball> getShootableBalls() {
        List<Ball> shootable = new ArrayList<>();
        for (Ball ball : gamePanel.table.balls) {
            if (ball.number != 0 && !ball.sunk) {
                shootable.add(ball);
            }
        }
        return shootable;
    }

    /**
     * Calculate angle from cue ball to target ball
     */
    private double calculateAngleToTarget(Ball from, Ball to) {
        return Math.atan2(to.y - from.y, to.x - from.x);
    }

    /**
     * Calculate distance from ball to closest pocket
     */
    private double calculateDistanceToPocket(Ball ball) {
        double minDist = Double.MAX_VALUE;
        for (Pocket pocket : gamePanel.table.pockets) {
            double dist = distance(ball.x, ball.y, pocket.x, pocket.y);
            if (dist < minDist) {
                minDist = dist;
            }
        }
        return minDist;
    }

    /**
     * Calculate Euclidean distance between two balls
     */
    private double distance(Ball ball1, Ball ball2) {
        return distance(ball1.x, ball1.y, ball2.x, ball2.y);
    }

    /**
     * Calculate Euclidean distance between two points
     */
    private double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Execute the calculated shot
     */
    private void executeShot(AIShot shot) {
        Cue cue = gamePanel.cue;
        cue.startAnimation(shot.angle, shot.powerPercent);
        cue.setAnimationCompleteAction(() -> {
            game.cueBallHitAnyBall = false;
            cue.shoot();
            cue.hide();
            game.state = GameState.BALLS_MOVING;
        });
        cue.show();
        game.state = GameState.AIMING;
    }

    /**
     * Inner class to represent an AI shot decision
     */
    public static class AIShot {
        public double angle;
        public double powerPercent;

        public AIShot(double angle, double powerPercent) {
            this.angle = angle;
            this.powerPercent = Math.min(1.0, Math.max(0.0, powerPercent));
        }
    }
}
