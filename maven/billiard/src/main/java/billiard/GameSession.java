package billiard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a saved game session that can be serialized and loaded.
 */
public class GameSession implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public String gameMode;
    public String sessionName;
    public long timestamp;
    
    // Game state
    public List<BallState> ballStates;
    public int currentPlayerIndex;
    public String gameState;
    
    // Player info
    public List<String> playerNames;
    public List<Integer> playerScores;      // Score (balls pocketed) for each player
    public List<String> playerGroups;       // Assigned group (SOLIDS/STRIPES/null) for each player
    public long elapsedTimeMillis;          // Elapsed time in milliseconds
    
    public GameSession() {
        this.ballStates = new ArrayList<>();
        this.playerNames = new ArrayList<>();
        this.playerScores = new ArrayList<>();
        this.playerGroups = new ArrayList<>();
        this.timestamp = System.currentTimeMillis();
        this.elapsedTimeMillis = 0;
    }
    
    /**
     * Represents the state of a single ball
     */
    public static class BallState implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public double x;
        public double y;
        public double vx;
        public double vy;
        public int ballNumber;
        public boolean sunk;
        public double radius;
        public int pocketedByPlayer = -1;  // -1 = not pocketed, 0+ = player index
        
        public BallState(double x, double y, double vx, double vy, int ballNumber, boolean sunk, double radius) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.ballNumber = ballNumber;
            this.sunk = sunk;
            this.radius = radius;
        }
    }
}
