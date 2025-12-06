package billiard;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import java.util.List;

public class Cue {
    public Ball cueBall;
    public double angle = 0;
    public double maxPower = 2500; // Increased power for stronger shots
    public boolean visible = true;
    private double powerPercent = 0;
    private List<Ball> allBalls; // Referensi ke semua bola untuk aim line
    private Table table; // Referensi ke meja untuk boundary
    
    // State untuk two-stage aiming
    public boolean angleLocked = false; // Apakah sudut sudah dikunci

    public Cue(Ball cueBall) {
        this.cueBall = cueBall;
    }
    
    public void setTableReference(Table table) {
        this.table = table;
        this.allBalls = table.balls;
    }

    public void updateAngle(double mouseX, double mouseY) {
        // Hanya update sudut jika belum dikunci
        if (!angleLocked) {
            angle = Math.atan2(mouseY - cueBall.y, mouseX - cueBall.x);
        }
    }
    
    public void lockAngle() {
        angleLocked = true;
    }
    
    public void unlockAngle() {
        angleLocked = false;
        powerPercent = 0;
    }
    
    public boolean isAngleLocked() {
        return angleLocked;
    }

    public void setPower(double distanceDragged) {
        // Sensitivitas drag - pembagi lebih kecil = lebih sensitif
        powerPercent = Math.min(1.0, Math.max(0.0, distanceDragged / 150.0));
    }

    public double getPowerPercent() {
        return powerPercent;
    }

    public void shoot() {
        double power = maxPower * powerPercent;
        cueBall.applyForce(power, angle);
        powerPercent = 0;
        angleLocked = false; // Reset lock setelah menembak
    }

    public void render(GraphicsContext g, double powerPercent) {
        if (!visible || cueBall.sunk) return;

        double baseX = cueBall.x;
        double baseY = cueBall.y;
        
        // === RENDER AIM LINE TERLEBIH DAHULU (di bawah stick) ===
        renderAimLine(g);
        
        // === RENDER POWER BAR jika angle sudah dikunci ===
        if (angleLocked) {
            renderPowerBar(g, powerPercent);
        }

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
    
    /**
     * Render garis bantu aiming dari cue ball ke arah tembakan
     */
    private void renderAimLine(GraphicsContext g) {
        if (table == null || allBalls == null) return;
        
        // === INDICATOR ANGLE LOCKED ===
        if (angleLocked) {
            // Gambar lingkaran hijau di sekitar cue ball menandakan angle terkunci
            g.setStroke(Color.rgb(100, 255, 100, 0.7));
            g.setLineWidth(2);
            g.strokeOval(cueBall.x - cueBall.radius - 5, cueBall.y - cueBall.radius - 5,
                        (cueBall.radius + 5) * 2, (cueBall.radius + 5) * 2);
        }
        
        // Arah tembakan (kebalikan dari arah stick)
        double dirX = Math.cos(angle);
        double dirY = Math.sin(angle);
        
        // Titik awal garis (dari permukaan bola putih)
        double startX = cueBall.x + dirX * cueBall.radius;
        double startY = cueBall.y + dirY * cueBall.radius;
        
        // Cari titik tabrakan terdekat (bola atau dinding)
        double maxDist = 800; // Panjang maksimal garis bantu
        double hitDist = maxDist;
        Ball hitBall = null;
        
        // Cek tabrakan dengan bola lain
        for (Ball b : allBalls) {
            if (b == cueBall || b.sunk) continue;
            
            // Ray-sphere intersection
            double dx = b.x - cueBall.x;
            double dy = b.y - cueBall.y;
            
            // Proyeksi ke arah tembakan
            double proj = dx * dirX + dy * dirY;
            if (proj <= 0) continue; // Bola di belakang
            
            // Jarak tegak lurus dari bola ke garis tembakan
            double perpX = cueBall.x + dirX * proj - b.x;
            double perpY = cueBall.y + dirY * proj - b.y;
            double perpDist = Math.sqrt(perpX * perpX + perpY * perpY);
            
            // Jika garis melewati bola (dengan radius collision)
            double combinedRadius = cueBall.radius + b.radius;
            if (perpDist < combinedRadius) {
                // Hitung jarak ke titik tabrakan
                double halfChord = Math.sqrt(combinedRadius * combinedRadius - perpDist * perpDist);
                double impactDist = proj - halfChord;
                
                if (impactDist > 0 && impactDist < hitDist) {
                    hitDist = impactDist;
                    hitBall = b;
                }
            }
        }
        
        // Cek tabrakan dengan dinding
        double left = table.PADDING + table.INNER_PADDING + cueBall.radius;
        double right = table.WIDTH - table.PADDING - table.INNER_PADDING - cueBall.radius;
        double top = table.PADDING + table.INNER_PADDING + cueBall.radius;
        double bottom = table.HEIGHT - table.PADDING - table.INNER_PADDING - cueBall.radius;
        
        // Dinding kiri
        if (dirX < 0) {
            double d = (left - cueBall.x) / dirX;
            if (d > 0 && d < hitDist) hitDist = d;
        }
        // Dinding kanan
        if (dirX > 0) {
            double d = (right - cueBall.x) / dirX;
            if (d > 0 && d < hitDist) hitDist = d;
        }
        // Dinding atas
        if (dirY < 0) {
            double d = (top - cueBall.y) / dirY;
            if (d > 0 && d < hitDist) hitDist = d;
        }
        // Dinding bawah
        if (dirY > 0) {
            double d = (bottom - cueBall.y) / dirY;
            if (d > 0 && d < hitDist) hitDist = d;
        }
        
        // Titik akhir garis bantu
        double endX = cueBall.x + dirX * hitDist;
        double endY = cueBall.y + dirY * hitDist;
        
        // === GAMBAR GARIS BANTU UTAMA (Dotted Line) ===
        g.setStroke(Color.rgb(255, 255, 255, 0.4));
        g.setLineWidth(1.5);
        g.setLineDashes(8, 6); // Garis putus-putus
        g.strokeLine(startX, startY, endX, endY);
        g.setLineDashes(); // Reset ke solid
        
        // === GAMBAR LINGKARAN TARGET DI TITIK IMPACT ===
        if (hitBall != null) {
            // Lingkaran di posisi cue ball saat impact
            g.setStroke(Color.rgb(255, 255, 255, 0.5));
            g.setLineWidth(1.5);
            g.strokeOval(endX - cueBall.radius, endY - cueBall.radius, 
                        cueBall.radius * 2, cueBall.radius * 2);
            
            // === GAMBAR GARIS PREDIKSI ARAH BOLA TARGET ===
            // Hitung arah bola target akan bergerak (dari pusat cue ke pusat target)
            double targetDirX = hitBall.x - endX;
            double targetDirY = hitBall.y - endY;
            double targetDirLen = Math.sqrt(targetDirX * targetDirX + targetDirY * targetDirY);
            if (targetDirLen > 0) {
                targetDirX /= targetDirLen;
                targetDirY /= targetDirLen;
                
                // Garis prediksi arah bola target
                double predLength = 100;
                g.setStroke(Color.rgb(255, 200, 100, 0.6));
                g.setLineWidth(1.5);
                g.setLineDashes(5, 4);
                g.strokeLine(hitBall.x, hitBall.y, 
                            hitBall.x + targetDirX * predLength, 
                            hitBall.y + targetDirY * predLength);
                g.setLineDashes();
            }
            
            // === GAMBAR GARIS PREDIKSI ARAH CUE BALL SETELAH IMPACT ===
            // Arah cue ball setelah tabrakan (perpendicular component)
            double dot = dirX * targetDirX + dirY * targetDirY;
            double cueBounceX = dirX - targetDirX * dot;
            double cueBounceY = dirY - targetDirY * dot;
            double bounceLen = Math.sqrt(cueBounceX * cueBounceX + cueBounceY * cueBounceY);
            if (bounceLen > 0.1) {
                cueBounceX /= bounceLen;
                cueBounceY /= bounceLen;
                
                double cueLineLen = 60;
                g.setStroke(Color.rgb(150, 200, 255, 0.5));
                g.setLineWidth(1.5);
                g.setLineDashes(4, 3);
                g.strokeLine(endX, endY, 
                            endX + cueBounceX * cueLineLen, 
                            endY + cueBounceY * cueLineLen);
                g.setLineDashes();
            }
        } else {
            // Jika tidak ada bola yang kena, gambar X di dinding
            g.setStroke(Color.rgb(255, 100, 100, 0.5));
            g.setLineWidth(2);
            double crossSize = 6;
            g.strokeLine(endX - crossSize, endY - crossSize, endX + crossSize, endY + crossSize);
            g.strokeLine(endX + crossSize, endY - crossSize, endX - crossSize, endY + crossSize);
        }
    }
    
    /**
     * Render power bar indicator saat aiming (angle sudah dikunci)
     */
    private void renderPowerBar(GraphicsContext g, double power) {
        // Posisi power bar di pojok kiri bawah meja
        double barX = 70;
        double barY = 500;
        double barWidth = 200;
        double barHeight = 20;
        
        // Background bar (abu-abu)
        g.setFill(Color.rgb(50, 50, 50, 0.8));
        g.fillRoundRect(barX - 2, barY - 2, barWidth + 4, barHeight + 4, 8, 8);
        
        // Border
        g.setStroke(Color.rgb(100, 100, 100));
        g.setLineWidth(2);
        g.strokeRoundRect(barX - 2, barY - 2, barWidth + 4, barHeight + 4, 8, 8);
        
        // Power fill dengan gradient warna (hijau -> kuning -> merah)
        double fillWidth = barWidth * power;
        if (fillWidth > 0) {
            Color powerColor;
            if (power < 0.33) {
                powerColor = Color.rgb(100, 200, 100); // Hijau
            } else if (power < 0.66) {
                powerColor = Color.rgb(255, 200, 50); // Kuning
            } else {
                powerColor = Color.rgb(255, 80, 80); // Merah
            }
            g.setFill(powerColor);
            g.fillRoundRect(barX, barY, fillWidth, barHeight, 6, 6);
        }
        
        // Label "POWER"
        g.setFill(Color.WHITE);
        g.fillText("POWER", barX, barY - 8);
        
        // Persentase
        g.fillText(String.format("%.0f%%", power * 100), barX + barWidth + 10, barY + 15);
        
        // Instruksi
        g.setFill(Color.rgb(200, 200, 200));
        g.fillText("Tarik mundur untuk power | Klik kanan untuk batal", barX, barY + 40);
    }
}