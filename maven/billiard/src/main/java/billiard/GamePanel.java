package billiard;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class GamePanel extends Canvas {
    public Table table;
    public Cue cue;
    public boolean mousePressed = false;
    private double dragStartX, dragStartY;
    private BilliardGame game;

    private AnimationTimer loop;

    public GamePanel(double width, double height, BilliardGame game) {
        super(width, height);
        this.game = game;
        table = new Table();
        // create cue ball
        Ball cueBall = new Ball(width * 0.25, height/2, 0);
        table.balls.add(cueBall);
        // add balls in a standard 8-ball triangular rack (balls 1..15)
        double baseX = width * 0.75;
        double baseY = height / 2;
        double spacingX = 25; // horizontal spacing between rows
        double spacingY = 15; // vertical half-spacing between balls
        int num = 1;
        for (int row = 0; row < 5 && num <= 15; row++) {
            double x = baseX + row * spacingX;
            for (int j = 0; j <= row && num <= 15; j++) {
                double y = baseY - row * spacingY + j * (2 * spacingY);
                table.balls.add(new Ball(x, y, num));
                num++;
            }
        }
        cue = new Cue(cueBall);

        setOnMousePressed(this::mousePressed);
        setOnMouseDragged(this::mouseDragged);
        setOnMouseReleased(this::mouseReleased);

        startGameLoop();
    }

    private void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            mousePressed = true;
            dragStartX = e.getX();
            dragStartY = e.getY();
        }
    }

    private void mouseDragged(MouseEvent e) {
        if (!mousePressed) return;
        cue.updateAngle(e.getX(), e.getY());
        double dx = dragStartX - e.getX();
        double dy = dragStartY - e.getY();
        double dist = Math.sqrt(dx*dx + dy*dy);
        cue.setPower(dist);
    }

    private void mouseReleased(MouseEvent e) {
        if (!mousePressed) return;
        mousePressed = false;
        // Shoot
        cue.shoot();
        // hide cue while the cue ball is in motion
        cue.hide();
        game.state = GameState.BALLS_MOVING;
    }

    public void paintComponent(GraphicsContext g) {
        try {
            // clear
            g.clearRect(0, 0, getWidth(), getHeight());
            // render table
            table.render(g);
            // render cue with power visualization
            cue.render(g, cue.getPowerPercent());
        } catch (Exception e) {
            System.err.println("Error rendering: " + e.getMessage());
        }
    }

    public void updateGameLoop() {
        try {
            double dt = 1.0 / 60.0;
            
            // Update table (sudah include semua collision & friction)
            table.update(dt);
            
            // HAPUS baris ini karena sudah ada di table.update():
            // PhysicsEngine.checkAllBallCollisions(table.balls); // <-- HAPUS!
            
            // Check pocket collision
            PhysicsEngine.handlePocketedBalls(table, game);
            
            // Update cue visibility
            if (cue != null && cue.cueBall != null) {
                if (cue.cueBall.isMoving()) {
                    cue.hide();
                } else {
                    if (!cue.cueBall.sunk) cue.show();
                }
            }

        // Check game state
        if (table.areBallsStopped()) {
            if (game.state == GameState.BALLS_MOVING) {
                game.switchTurn();
            }
        }
    } catch (Exception e) {
        System.err.println("Error in game loop: " + e.getMessage());
    }
}
    private void startGameLoop() {
        GraphicsContext gc = getGraphicsContext2D();
        loop = new AnimationTimer() {
            long last = 0;
            @Override
            public void handle(long now) {
                try {
                    if (last == 0) last = now;
                    double elapsed = (now - last) / 1e9;
                    if (elapsed >= 1.0/60.0) {
                        updateGameLoop();
                        paintComponent(gc);
                        last = now;
                    }
                } catch (Exception e) {
                    System.err.println("AnimationTimer error: " + e.getMessage());
                }
            }
        };
        loop.start();
    }

    public void stop() {
        if (loop != null) loop.stop();
    }
}
