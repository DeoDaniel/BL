package billiard;

/**
 * Singleton class to manage all game settings across the application.
 * Ensures settings are synchronized between MainMenu and PauseMenu.
 */
public class Settings {
    private static Settings instance;
    
    private double sfxVolume = 0.7;
    private double bgmVolume = 0.7;
    private String aiDifficulty = "Medium";
    private String screenResolution = "1120x720";
    
    private Settings() {
    }
    
    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }
    
    // SFX Volume
    public double getSfxVolume() {
        return sfxVolume;
    }
    
    public void setSfxVolume(double sfxVolume) {
        this.sfxVolume = sfxVolume;
    }
    
    // BGM Volume
    public double getBgmVolume() {
        return bgmVolume;
    }
    
    public void setBgmVolume(double bgmVolume) {
        this.bgmVolume = bgmVolume;
    }
    
    // AI Difficulty
    public String getAiDifficulty() {
        return aiDifficulty;
    }
    
    public void setAiDifficulty(String aiDifficulty) {
        this.aiDifficulty = aiDifficulty;
    }
    
    // Screen Resolution
    public String getScreenResolution() {
        return screenResolution;
    }
    
    public void setScreenResolution(String screenResolution) {
        this.screenResolution = screenResolution;
    }

    // Game Mode
    private String gameMode = "Original";

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }
}
