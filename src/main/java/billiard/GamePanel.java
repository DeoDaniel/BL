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
    public double shotPower = 0;
    private double dragStartX, dragStartY;
    private BilliardGame game;

    private AnimationTimer loop;

    public GamePanel(double width, double height, BilliardGame game) {
        super(width, height);
        this.game = game;
        table = new Table();
        // create cue ball and some balls
        Ball cueBall = new Ball(width * 0.25, height/2, 0);
        table.balls.add(cueBall);
        // add few balls as example
        table.balls.add(new Ball(width * 0.75, height/2, 1));
        table.balls.add(new Ball(width * 0.75 + 25, height/2 + 15, 2));
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
        shotPower = Math.min(1.0, dist / 200.0);
    }

    private void mouseReleased(MouseEvent e) {
        if (!mousePressed) return;
        mousePressed = false;
        // Shoot
        cue.shoot();
        game.state = GameState.BALLS_MOVING;
    }

    public void paintComponent(GraphicsContext g) {
        // clear
        g.clearRect(0, 0, getWidth(), getHeight());
        // render table
        table.render(g);
        // render cue with power visualization
        cue.render(g, shotPower);
    }

    public void updateGameLoop() {
        double dt = 1.0 / 60.0;
        table.update(dt);
        PhysicsEngine.checkAllBallCollisions(table.balls);
        PhysicsEngine.handlePocketedBalls(table, game.currentPlayer);
        // update state
        if (table.areBallsStopped()) {
            if (game.state == GameState.BALLS_MOVING) {
                game.switchTurn();
            }
        }
    }

    private void startGameLoop() {
        GraphicsContext gc = getGraphicsContext2D();
        loop = new AnimationTimer() {
            long last = 0;
            @Override
            public void handle(long now) {
                if (last == 0) last = now;
                double elapsed = (now - last) / 1e9;
                if (elapsed >= 1.0/60.0) {
                    updateGameLoop();
                    paintComponent(gc);
                    last = now;
                }
            }
        };
        loop.start();
    }

    public void stop() {
        if (loop != null) loop.stop();
    }
}
