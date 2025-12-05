package billiard;

public class Pocket {
    public double x;
    public double y;
    // Ditingkatkan dari 17 menjadi 25 untuk membuat lubang lebih besar
    public double radius = 21; 

    public Pocket(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public boolean contains(Ball ball) {
        double dx = ball.x - x;
        double dy = ball.y - y;
        // Jarak dari pusat bola ke pusat pocket
        double distance = Math.sqrt(dx * dx + dy * dy); 
        
        // Bola masuk jika jaraknya lebih kecil dari radius pocket 
        // dikurangi sedikit untuk "memancing" bola masuk
        return distance <= radius * 0.9; 
    }
}