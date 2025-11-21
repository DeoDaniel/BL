package billiard;

import javafx.scene.canvas.GraphicsContext;

public class Cue {
    public Ball cueBall;
    public double angle = 0;
    public double maxPower = 5000;
    public boolean visible = true;
    private double powerPercent = 0;

    public Cue(Ball cueBall) {
        this.cueBall = cueBall;
    }

    public void updateAngle(double mouseX, double mouseY) {
        angle = Math.atan2(mouseY - cueBall.y, mouseX - cueBall.x);
    }

    public void setPower(double distanceDragged) {
        // convert some drag distance to percent
        powerPercent = Math.min(1.0, Math.max(0.0, distanceDragged / 200.0));
    }

    public void shoot() {
        double power = maxPower * powerPercent;
        cueBall.applyForce(power, angle);
        powerPercent = 0;
    }

public void render(GraphicsContext g, double powerPercent) {
    if (!visible || cueBall.sunk) return;

    g.setStroke(javafx.scene.paint.Color.YELLOW);
    g.setLineWidth(3);

    double len = 100 + powerPercent * 100;
    double x2 = cueBall.x - Math.cos(angle) * len;
    double y2 = cueBall.y - Math.sin(angle) * len;
    g.strokeLine(cueBall.x, cueBall.y, x2, y2);
}

    public void hide() {
        visible = false;
    }

    public void show() {
        visible = true;
    }
}
