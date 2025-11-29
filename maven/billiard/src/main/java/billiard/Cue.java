package billiard;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

public class Cue {
    public Ball cueBall;
    public double angle = 0;
    public double maxPower = 1500;
    public boolean visible = true;
    private double powerPercent = 0;

    public Cue(Ball cueBall) {
        this.cueBall = cueBall;
    }

    public void updateAngle(double mouseX, double mouseY) {
        angle = Math.atan2(mouseY - cueBall.y, mouseX - cueBall.x);
    }

    public void setPower(double distanceDragged) {
        powerPercent = Math.min(1.0, Math.max(0.0, distanceDragged / 200.0));
    }

    public double getPowerPercent() {
        return powerPercent;
    }

    public void shoot() {
        double power = maxPower * powerPercent;
        cueBall.applyForce(power, angle);
        powerPercent = 0;
    }

    public void render(GraphicsContext g, double powerPercent) {
        if (!visible || cueBall.sunk) return;

        double baseX = cueBall.x;
        double baseY = cueBall.y;

        // cue moves back and forth based on power (constant length)
        double cueLength = 140;
        double backDistance = powerPercent * 80;  // moves back up to 80 units

        // ujung stick (depan - contact point with cue ball)
        double tipX = baseX - Math.cos(angle) * 25;
        double tipY = baseY - Math.sin(angle) * 25;

        // pangkal panjang (adjusted for back movement)
        double endX = baseX - Math.cos(angle) * (cueLength + 25 + backDistance);
        double endY = baseY - Math.sin(angle) * (cueLength + 25 + backDistance);

        // ================================
        // 1. SHADOW (bayangan)
        // ================================
        g.setLineWidth(6);
        g.setStroke(new Color(0, 0, 0, 0.25));
        g.strokeLine(tipX + 2, tipY + 2, endX + 2, endY + 2);

        // ================================
        // 2. WOOD BODY (batang kayu)
        // ================================
        g.setLineWidth(5);

        LinearGradient wood = new LinearGradient(
                tipX, tipY, endX, endY, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(230, 200, 150)),
                new Stop(1, Color.rgb(160, 110, 70))
        );

        g.setStroke(wood);
        g.strokeLine(tipX, tipY, endX, endY);

        // ================================
        // 3. GRIP (bagian pegangan belakang)
        // ================================
        double gripLen = 70;
        double gripX = baseX - Math.cos(angle) * (gripLen + 40 + backDistance);
        double gripY = baseY - Math.sin(angle) * (gripLen + 40 + backDistance);

        g.setLineWidth(7);
        g.setStroke(Color.rgb(40, 40, 40));
        g.strokeLine(gripX, gripY, endX, endY);

        // ================================
        // 4. TIP (ujung biru)
        // ================================
        double chalkX = tipX;
        double chalkY = tipY;

        g.setLineWidth(7);
        g.setStroke(Color.rgb(80, 170, 255)); // biru chalk
        g.strokeLine(chalkX, chalkY, tipX - Math.cos(angle) * 5, tipY - Math.sin(angle) * 5);

        // ================================
        // 5. OUTLINE halus
        // ================================
        g.setLineWidth(1.8);
        g.setStroke(Color.rgb(20, 20, 20, 0.4));
        g.strokeLine(tipX, tipY, endX, endY);
    }

    public void hide() {
        visible = false;
    }

    public void show() {
        visible = true;
    }
}
