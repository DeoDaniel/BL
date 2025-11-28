package billiard;

import java.util.ArrayList;
import java.util.List;

public class Player {
    public String name;
    public int ballsPocketed = 0;
    public BallGroup assignedGroup = null; // SOLIDS / STRIPES / null
    public boolean hasTurn = false;
    public boolean fouled = false;
    public List<Ball> pocketedBalls = new ArrayList<>();

    public Player(String name) {
        this.name = name;
    }

    public void assignGroup(BallGroup group) {
        this.assignedGroup = group;
    }

    public void pocketBall(Ball ball) {
        pocketedBalls.add(ball);
        ballsPocketed++;
    }

    public boolean hasWon() {
        // simple rule: kalau sudah 7 bola grupnya masuk
        return assignedGroup != null && ballsPocketed >= 7;
    }
}
