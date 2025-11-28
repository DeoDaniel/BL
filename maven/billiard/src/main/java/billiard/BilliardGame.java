package billiard;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class BilliardGame extends Application {

    public Stage frame;
    public GamePanel panel;
    public List<Player> players = new ArrayList<>();
    public Player currentPlayer;
    public GameState state = GameState.AIMING;
    public PhysicsEngine physics = new PhysicsEngine();

    // HUD
    public Label player1Label;
    public Label player2Label;

    // turn info
    public boolean scoredThisTurn = false;
    public boolean foulThisTurn = false;

    @Override
    public void start(Stage primaryStage) {
        this.frame = primaryStage;

        // create players
        players.add(new Player("Player 1"));
        players.add(new Player("Player 2"));
        currentPlayer = players.get(0);
        currentPlayer.hasTurn = true;
        startTurn();

        panel = new GamePanel(1120, 560, this);

        // root layout: HUD di atas, canvas di tengah
        BorderPane root = new BorderPane();
        StackPane center = new StackPane(panel);
        root.setCenter(center);

        // HUD
        player1Label = new Label();
        player2Label = new Label();
        HBox hud = new HBox(40, player1Label, player2Label);
        hud.setAlignment(Pos.CENTER);
        hud.setPadding(new Insets(8));
        hud.setStyle("-fx-background-color: #222;");
        root.setTop(hud);

        updateHud();

        Scene scene = new Scene(root, 1120, 600);

        primaryStage.setTitle("Billiard Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void startNewGame() {
        players.forEach(p -> {
            p.ballsPocketed = 0;
            p.assignedGroup = null;
            p.fouled = false;
            p.pocketedBalls.clear();
        });
        currentPlayer = players.get(0);
        currentPlayer.hasTurn = true;
        startTurn();

        panel = new GamePanel(1120, 560, this);
        state = GameState.AIMING;
        updateHud();
    }

    public void startTurn() {
        scoredThisTurn = false;
        foulThisTurn = false;
        currentPlayer.fouled = false;
    }

    public Player getOtherPlayer() {
        return currentPlayer == players.get(0) ? players.get(1) : players.get(0);
    }

    public void switchTurn() {
        int idx = players.indexOf(currentPlayer);
        currentPlayer.hasTurn = false;
        currentPlayer = players.get((idx + 1) % players.size());
        currentPlayer.hasTurn = true;
        state = GameState.AIMING;
        startTurn();
        updateHud();
    }

    public void onBallPocketed(Ball ball) {
        // cue ball foul
        if (ball.isCueBall()) {
            foulThisTurn = true;
            currentPlayer.fouled = true;
            updateHud();
            return;
        }

        // catat bola ke player
        currentPlayer.pocketBall(ball);

        // assign grup kalau belum
        if (currentPlayer.assignedGroup == null) {
            if (ball.isSolid()) {
                currentPlayer.assignGroup(BallGroup.SOLIDS);
                getOtherPlayer().assignGroup(BallGroup.STRIPES);
            } else if (ball.isStripe()) {
                currentPlayer.assignGroup(BallGroup.STRIPES);
                getOtherPlayer().assignGroup(BallGroup.SOLIDS);
            }
        }

        // cek apakah bola ini milik player ini atau lawan
        if (currentPlayer.assignedGroup == null) {
            // masih fase open table (setelah break dan sebelum grup ditentukan)
            scoredThisTurn = true;
        } else {
            boolean correctGroup =
                    (currentPlayer.assignedGroup == BallGroup.SOLIDS && ball.isSolid()) ||
                    (currentPlayer.assignedGroup == BallGroup.STRIPES && ball.isStripe());

            if (correctGroup) {
                scoredThisTurn = true;
            } else {
                foulThisTurn = true; // masukin bola lawan → foul
            }
        }

        // bola 8: rule sederhana (belum super lengkap)
        if (ball.number == 8) {
            state = GameState.GAME_OVER;
        }

        updateHud();
    }

    public boolean checkWinCondition() {
        for (Player p : players) {
            if (p.hasWon()) {
                state = GameState.GAME_OVER;
                return true;
            }
        }
        return false;
    }

    public void updateHud() {
        if (player1Label == null || player2Label == null) return;
        Player p1 = players.get(0);
        Player p2 = players.get(1);

        player1Label.setText(buildPlayerText(p1));
        player2Label.setText(buildPlayerText(p2));

        player1Label.setStyle(buildStyleFor(p1));
        player2Label.setStyle(buildStyleFor(p2));
    }

    private String buildPlayerText(Player p) {
        String groupText = "-";
        if (p.assignedGroup == BallGroup.SOLIDS) groupText = "Solids";
        else if (p.assignedGroup == BallGroup.STRIPES) groupText = "Stripes";

        StringBuilder balls = new StringBuilder();
        for (Ball b : p.pocketedBalls) {
            if (!b.isCueBall() && b.number != 8) {
                balls.append(b.number).append(" ");
            }
        }

        return String.format("%s | Group: %s | In: %s",
                p.name,
                groupText,
                balls.toString().trim());
    }

    private String buildStyleFor(Player p) {
        String base = "-fx-font-size: 16; -fx-padding: 4;";
        if (p == currentPlayer) {
            return base + "-fx-font-weight: bold; -fx-text-fill: #ffd700;";
        } else {
            return base + "-fx-text-fill: #ffffff;";
        }
    }
}
