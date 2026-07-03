package billiard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Manager class for saving and loading game sessions.
 */
public class GameSaveManager {
    private static final String SAVE_DIRECTORY = "saves";
    private static final String FILE_EXTENSION = ".sav";
    
    static {
        // Create saves directory if it doesn't exist
        File saveDir = new File(SAVE_DIRECTORY);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
    }
    
    /**
     * Save a game session to a file
     */
    public static boolean saveGame(GameSession session, String fileName) {
        try {
            String filePath = SAVE_DIRECTORY + File.separator + fileName + FILE_EXTENSION;
            FileOutputStream fos = new FileOutputStream(filePath);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(session);
            oos.close();
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Load a game session from a file
     */
    public static GameSession loadGame(String fileName) {
        try {
            String filePath = SAVE_DIRECTORY + File.separator + fileName + FILE_EXTENSION;
            FileInputStream fis = new FileInputStream(filePath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            GameSession session = (GameSession) ois.readObject();
            ois.close();
            fis.close();
            return session;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get list of all saved game files
     */
    public static List<String> getSaveFileNames() {
        List<String> fileNames = new ArrayList<>();
        try (Stream<Path> paths = Files.list(Paths.get(SAVE_DIRECTORY))) {
            paths.filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(FILE_EXTENSION))
                .forEach(p -> {
                    String name = p.getFileName().toString();
                    fileNames.add(name.substring(0, name.length() - FILE_EXTENSION.length()));
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileNames;
    }
    
    /**
     * Delete a save file
     */
    public static boolean deleteSaveFile(String fileName) {
        try {
            String filePath = SAVE_DIRECTORY + File.separator + fileName + FILE_EXTENSION;
            Files.deleteIfExists(Paths.get(filePath));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
