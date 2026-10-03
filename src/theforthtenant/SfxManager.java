package theforthtenant;

/**
 * Persists SFX and music volumes to {@link java.util.prefs.Preferences} and
 * pushes them into {@link AudioCache} so every cached clip updates immediately.
 */
public final class SfxManager {

    // ---- Cached volume state (0–100) ----
    private static int volumePercent      = 100;   // SFX
    private static int musicVolumePercent = 100;   // Music

    // ---- Load saved volumes on first use ----
    static {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userRoot()
            .node("theforthtenant");
        volumePercent      = prefs.getInt("sfxVolume",   100);
        musicVolumePercent = prefs.getInt("musicVolume", 100);

        // Push the loaded values into the cache.
        AudioCache.setSfxVolume(volumePercent);
        AudioCache.setMusicVolume(musicVolumePercent);
    }

    private SfxManager() {}

    /** Sets, persists, and applies the SFX volume (0–100). */
    public static void setVolumePercent(int percent) {
        volumePercent = Math.max(0, Math.min(100, percent));
        java.util.prefs.Preferences.userRoot()
            .node("theforthtenant")
            .putInt("sfxVolume", volumePercent);
        AudioCache.setSfxVolume(volumePercent);
    }

    /** Returns the current SFX volume (0–100). */
    public static int getVolumePercent() {
        return volumePercent;
    }

    /** Sets, persists, and applies the music volume (0–100). */
    public static void setMusicVolumePercent(int percent) {
        musicVolumePercent = Math.max(0, Math.min(100, percent));
        java.util.prefs.Preferences.userRoot()
            .node("theforthtenant")
            .putInt("musicVolume", musicVolumePercent);
        AudioCache.setMusicVolume(musicVolumePercent);
    }

    /** Returns the current music volume (0–100). */
    public static int getMusicVolumePercent() {
        return musicVolumePercent;
    }
}