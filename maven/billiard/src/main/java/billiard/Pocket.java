package billiard;

public class Pocket {
    public double x;
    public double y;
    public double radius = 17;

    public Pocket(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public boolean contains(Ball ball) {
        double dx = ball.x - x;
        double dy = ball.y - y;
        return dx*dx + dy*dy <= (radius + ball.radius) * (radius + ball.radius);
    }
}
