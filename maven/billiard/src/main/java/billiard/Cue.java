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
        // Sensitivitas drag bisa diatur di pembagi (200.0)
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

        // --- KONFIGURASI UKURAN ---
        double cueLength = 400; // Panjang total stick
        double initialGap = 25; // Jarak awal tip ke bola (biar ga nempel banget visualnya)
        
        // --- LOGIC PERBAIKAN ---
        // Seberapa jauh stick mundur saat ditarik full.
        // Semakin besar angka ini, semakin jauh sticknya mundur.
        double pullDistance = powerPercent * 150; 

        // Hitung jarak Tip (ujung depan) dari pusat bola
        // Jarak Tip = Jarak Awal + Jarak Tarikan (INI KUNCINYA AGAR MUNDUR)
        double currentTipDist = initialGap + pullDistance;

        // Hitung jarak End (ujung belakang) dari pusat bola
        // Jarak End = Posisi Tip + Panjang Stick (supaya panjang stick tetap, tidak melar)
        double currentEndDist = currentTipDist + cueLength;

        // --- HITUNG KOORDINAT ---
        
        // 1. Posisi TIP (Depan)
        double tipX = baseX - Math.cos(angle) * currentTipDist;
        double tipY = baseY - Math.sin(angle) * currentTipDist;

        // 2. Posisi END (Belakang)
        double endX = baseX - Math.cos(angle) * currentEndDist;
        double endY = baseY - Math.sin(angle) * currentEndDist;

        // ========================================
        // 1. SHADOW (Bayangan)
        // ========================================
        g.setLineWidth(7);
        g.setStroke(new Color(0, 0, 0, 0.25));
        // Bayangan sedikit offset (+3)
        g.strokeLine(tipX + 3, tipY + 3, endX + 3, endY + 3);

        // ========================================
        // 2. WOOD BODY (Badan Stick)
        // ========================================
        g.setLineWidth(6);
        LinearGradient wood = new LinearGradient(
                tipX, tipY, endX, endY, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(230, 200, 150)),
                new Stop(1, Color.rgb(160, 110, 70))
        );
        g.setStroke(wood);
        g.strokeLine(tipX, tipY, endX, endY);

        // ========================================
        // 3. GRIP (Pegangan Hitam di Belakang)
        // ========================================
        // Grip dihitung dari posisi END mundur ke arah depan
        double gripLength = 100; 
        // Titik mulai grip (dari belakang stick)
        double gripStartX = endX + Math.cos(angle) * gripLength; 
        double gripStartY = endY + Math.sin(angle) * gripLength;

        g.setLineWidth(9);
        g.setStroke(Color.rgb(40, 40, 40)); // Warna karet grip
        g.strokeLine(gripStartX, gripStartY, endX, endY);

        // ========================================
        // 4. TIP (Chalk Biru di Depan)
        // ========================================
        double chalkLen = 6;
        // Ujung chalk (titik paling depan stick) = tipX, tipY
        // Pangkal chalk = mundur sedikit dari tipX

        // KOREKSI ARAH CHALK:
        // Karena kita menggambar dari Tip ke End (menjauhi bola), 
        // koordinat "mundur" ke dalam stick berarti MENAMBAH jarak dari bola.
        double chalkBaseDist = currentTipDist + chalkLen;
        double chalkBaseX = baseX - Math.cos(angle) * chalkBaseDist;
        double chalkBaseY = baseY - Math.sin(angle) * chalkBaseDist;

        g.setLineWidth(6); // Sedikit lebih kecil dari body biar rapi
        g.setStroke(Color.rgb(80, 170, 255));
        g.strokeLine(tipX, tipY, chalkBaseX, chalkBaseY);

        // ========================================
        // 5. OUTLINE HALUS (Optional)
        // ========================================
        g.setLineWidth(1);
        g.setStroke(new Color(0, 0, 0, 0.5));
        g.strokeLine(tipX, tipY, endX, endY);
    }

    public void hide() {
        visible = false;
    }

    public void show() {
        visible = true;
    }
}