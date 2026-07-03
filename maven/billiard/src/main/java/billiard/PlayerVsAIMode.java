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
    private static final long AI_THINK_DELAY = 1500; // 1.5 seconds delay before AI shoots
    private long aiActionTime = 0;
    private boolean aiActionScheduled = false;

    public PlayerVsAIMode(BilliardGame game, GamePanel gamePanel) {
        this.game = game;
        this.gamePanel = gamePanel;
        this.aiDifficulty = Settings.getInstance().getAiDifficulty();
    }

    /**
     * Update AI logic each frame - should be called from GamePanel update
     */
    public void update(double deltaTime) {
        // Check if AI player's turn and no balls are moving
        if (isAITurn() && !areBallsMoving()) {
            if (!aiActionScheduled) {
                aiActionScheduled = true;
                aiActionTime = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - aiActionTime >= AI_THINK_DELAY) {
                executeAIMove();
                aiActionScheduled = false;
            }
        }
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

        // Find ball that's closest to any pocket
        Ball bestBall = shootableBalls.get(0);
        double bestScore = calculateDistanceToPocket(bestBall);

        for (Ball ball : shootableBalls) {
            double score = calculateDistanceToPocket(ball);
            if (score < bestScore) {
                bestScore = score;
                bestBall = ball;
            }
        }

        // Calculate angle with minimal randomness (±2 degrees)
        double angle = calculateAngleToTarget(cueBall, bestBall);
        double randomOffset = (Math.random() - 0.5) * Math.toRadians(4);
        angle += randomOffset;

        // Full power (90-100%)
        double power = 0.90 + Math.random() * 0.10;

        return new AIShot(angle, power);
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

        // Set angle
        cue.angle = shot.angle;
        cue.lockAngle();

        // Set power through the cue power system
        double distanceDragged = shot.powerPercent * 150.0; // Convert power to distance
        cue.setPower(distanceDragged);

        // Execute shot
        cue.shoot();

        // Reset game state to BALLS_MOVING so GamePanel can process turn logic when balls stop
        if (game.state == GameState.AIMING) {
            game.state = GameState.BALLS_MOVING;
        }
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
