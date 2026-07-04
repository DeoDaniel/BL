package billiard;

/**
 * Simple stopwatch utility for tracking elapsed time during gameplay
 */
public class Stopwatch {
    private long startTime = 0;
    private long pausedTime = 0;
    private boolean running = false;

    /**
     * Start the stopwatch
     */
    public void start() {
        if (!running) {
            startTime = System.currentTimeMillis() - pausedTime;
            running = true;
        }
    }

    /**
     * Stop/pause the stopwatch
     */
    public void stop() {
        running = false;
    }

    /**
     * Reset the stopwatch
     */
    public void reset() {
        startTime = 0;
        pausedTime = 0;
        running = false;
    }

    /**
     * Get elapsed time in milliseconds
     */
    public long getElapsedMillis() {
        if (running) {
            return System.currentTimeMillis() - startTime;
        }
        return pausedTime;
    }

    /**
     * Get elapsed time formatted as mm:ss
     */
    public String getFormattedTime() {
        long elapsed = getElapsedMillis();
        long totalSeconds = elapsed / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Check if stopwatch is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Set elapsed time (ms) for restoring from save. Sets stopwatch to paused state.
     */
    public void setElapsedMillis(long ms) {
        this.pausedTime = Math.max(0, ms);
        this.running = false;
        this.startTime = System.currentTimeMillis() - this.pausedTime;
    }
}
