package theforthtenant;

import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Central place for all short one-shot SFX.
 * Holds a single global volume (0..100) that every SFX clip respects.
 *
 * Looping ambience (rain, thunder, music) is still handled by the caller.
 * Only use this for the little "click", "hover", "typing" style clips.
 */
public final class SfxManager {

    private static int volumePercent = 100;
    private static int musicVolumePercent = 100;

    private SfxManager() {}

    /** Update the master SFX volume. Applies to all *future* clips; already-playing ones keep going. */
    public static void setVolumePercent(int percent) {
        volumePercent = Math.max(0, Math.min(100, percent));
        java.util.prefs.Preferences.userRoot()
            .node("theforthtenant")
            .putInt("sfxVolume", volumePercent);
    }

    // And on first load:
    static {
        java.util.prefs.Preferences prefs = java.util.prefs.Preferences.userRoot()
            .node("theforthtenant");
        volumePercent      = prefs.getInt("sfxVolume",   100);
        musicVolumePercent = prefs.getInt("musicVolume", 100);
    }

    public static int getVolumePercent() {
        return volumePercent;
    }
    
    public static void setMusicVolumePercent(int percent) {
        musicVolumePercent = Math.max(0, Math.min(100, percent));
        java.util.prefs.Preferences.userRoot()
            .node("theforthtenant")
            .putInt("musicVolume", musicVolumePercent);
    }

    public static int getMusicVolumePercent() {
        return musicVolumePercent;
    }

    /**
     * Load and play a one-shot SFX at the current master volume.
     * Returns the Clip so the caller can stop it if needed, or null on failure.
     */
    public static Clip playOneShot(Class<?> resourceAnchor, String path) {
        try {
            InputStream is = resourceAnchor.getResourceAsStream(path);
            if (is == null) {
                System.out.println("SFX not found: " + path);
                return null;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(is);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            applyVolume(clip, volumePercent);
            clip.start();
            return clip;
        } catch (Exception e) {
            System.out.println("SFX error (" + path + "): " + e.getMessage());
            return null;
        }
    }

    /** Apply the current master volume to an already-open clip. */
    public static void applyVolume(Clip clip, int percent) {
        if (clip == null) return;
        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl)
                        clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (percent <= 0)
                        ? gain.getMinimum()
                        : (float)(20.0 * Math.log10(percent / 100.0));
                dB = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));
                gain.setValue(dB);
            }
        } catch (Exception ignored) {}
    }
}