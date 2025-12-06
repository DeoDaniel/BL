package billiard;

public class Pocket {
    public double x;
    public double y;
    // Radius pocket diperbesar untuk hitbox lebih besar
    public double radius = 25; 

    public Pocket(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public boolean contains(Ball ball) {
        double dx = ball.x - x;
        double dy = ball.y - y;
        // Jarak dari pusat bola ke pusat pocket
        double distance = Math.sqrt(dx * dx + dy * dy); 
        
        // Bola masuk jika jaraknya cukup dekat dengan pusat pocket
        // Hitbox lebih forgiving
        return distance <= radius; 
    }
}