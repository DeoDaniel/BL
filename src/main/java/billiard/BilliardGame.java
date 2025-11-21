package billiard;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
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

    @Override
    public void start(Stage primaryStage) {
        this.frame = primaryStage;

        // create players
        players.add(new Player("Player 1"));
        players.add(new Player("Player 2"));
        currentPlayer = players.get(0);
        currentPlayer.hasTurn = true;

        panel = new GamePanel(1120, 560, this);
        StackPane root = new StackPane();
        root.getChildren().add(panel);
        Scene scene = new Scene(root, 1120, 560);

        primaryStage.setTitle("Billiard Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void startNewGame() {
        // reset players/board
        players.forEach(p -> {
            p.ballsPocketed = 0;
            p.assignedGroup = null;
            p.fouled = false;
            p.pocketedBalls.clear();
        });
        currentPlayer = players.get(0);
        panel = new GamePanel(1120, 560, this);
        state = GameState.AIMING;
    }

    public void switchTurn() {
        int idx = players.indexOf(currentPlayer);
        currentPlayer.hasTurn = false;
        currentPlayer = players.get((idx + 1) % players.size());
        currentPlayer.hasTurn = true;
        state = GameState.AIMING;
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
}
