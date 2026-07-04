package billiard;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javafx.application.Platform;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundManager {
    private static final String RESOURCE_PREFIX = "/billiard/sfx/";
    private static final String BGM_RESOURCE_PATH = "/billiard/ost/bgm.mp3";
    private static final String BGM_FILE_PATH = "maven/billiard/src/main/resources/billiard/ost/bgm.mp3";
    private static final Random rnd = new Random();
    private static final Map<String, AudioClip> clips = new ConcurrentHashMap<>();
    private static MediaPlayer bgmPlayer;

    public static void play(String filename) {
        play(filename, Settings.getInstance().getSfxVolume());
    }

    public static void play(String filename, double volume) {
        final double vol = Math.max(0.0, Math.min(1.0, volume));
        new Thread(() -> {
            AudioClip clip = getAudioClip(filename);
            if (clip == null) {
                System.err.println("SFX not found: " + filename);
                return;
            }
            // Play with the valid AudioClip overload: volume only.
            clip.play(vol);
            System.err.println("Playing SFX: " + filename + " @ " + vol);
        }, "sfx-play-" + filename).start();
    }

    private static AudioClip getAudioClip(String filename) {
        return clips.computeIfAbsent(filename, key -> {
            URL resource = SoundManager.class.getResource(RESOURCE_PREFIX + key);
            if (resource == null) {
                return null;
            }
            return new AudioClip(resource.toExternalForm());
        });
    }

    public static void playCueHit() {
        play("cue_hit.wav");
    }

    public static void playBallIntoPocket() {
        play("ball_into_pocket.wav");
    }

    public static void playBallHit() {
        if (rnd.nextBoolean()) play("ball_hit_1.wav"); else play("ball_hit_2.wav");
    }

    public static void playBallHit(double volume) {
        if (rnd.nextBoolean()) play("ball_hit_1.wav", volume); else play("ball_hit_2.wav", volume);
    }

    public static void startBackgroundMusic() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose();
        }

        Media bgmMedia = loadBgmMedia();
        if (bgmMedia == null) {
            System.err.println("BGM media not found: " + BGM_RESOURCE_PATH);
            return;
        }

        bgmPlayer = new MediaPlayer(bgmMedia);
        bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        bgmPlayer.setVolume(Settings.getInstance().getBgmVolume());
        bgmPlayer.play();
    }

    public static void updateBgmVolume(double volume) {
        if (bgmPlayer != null) {
            Platform.runLater(() -> bgmPlayer.setVolume(Math.max(0.0, Math.min(1.0, volume))));
        }
    }

    private static Media loadBgmMedia() {
        URL resource = SoundManager.class.getResource(BGM_RESOURCE_PATH);
        if (resource != null) {
            return new Media(resource.toExternalForm());
        }

        File file = new File(BGM_FILE_PATH);
        if (file.exists()) {
            return new Media(file.toURI().toString());
        }

        return null;
    }
}
