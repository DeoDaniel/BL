package billiard;

import java.util.ArrayList;
import java.util.List;

public class Player {
    public String name;
    public int ballsPocketed = 0;
    public String assignedGroup = null; // "solids" or "stripes"
    public boolean hasTurn = false;
    public boolean fouled = false;
    public List<Ball> pocketedBalls = new ArrayList<>();

    public Player(String name) {
        this.name = name;
    }

    public void assignGroup(String group) {
        this.assignedGroup = group;
    }

    public void pocketBall(Ball ball) {
        pocketedBalls.add(ball);
        ballsPocketed++;
        // if 8-ball rules, you'd check if winning
    }

    public boolean hasWon() {
        // simplistic: if pocketed 7 of assigned group (not robust)
        return assignedGroup != null && ballsPocketed >= 7;
    }
}
