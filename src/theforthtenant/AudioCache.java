package theforthtenant;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * Central cache for every audio clip the game uses.
 * Clips are registered on one of two channels — SFX or MUSIC — so the two
 * Options sliders can control them independently. Call {@link #closeAll()}
 * only when the game really exits.
 */
public final class AudioCache {

    /** Which volume slider controls this clip. */
    public enum Channel { SFX, MUSIC }

    /** A cached clip together with the channel it was loaded on. */
    private static class Entry {
        final Clip clip;
        final Channel channel;

        Entry(Clip c, Channel ch) {
            clip = c;
            channel = ch;
        }
    }

    private static final Map<String, Entry> cache =
        java.util.Collections.synchronizedMap(new HashMap<>());

    private static int sfxVolumePercent   = 100;
    private static int musicVolumePercent = 100;

    private AudioCache() {}

    // ---------------------------------------------------------------
    // Volume control
    // ---------------------------------------------------------------

    /** Sets the SFX volume (0–100) and re-applies it to all cached SFX clips. */
    public static void setSfxVolume(int percent) {
        sfxVolumePercent = Math.max(0, Math.min(100, percent));
        applyAllVolumes();
    }

    /** Sets the music volume (0–100) and re-applies it to all cached music clips. */
    public static void setMusicVolume(int percent) {
        musicVolumePercent = Math.max(0, Math.min(100, percent));
        applyAllVolumes();
    }

    public static int getSfxVolume()   { return sfxVolumePercent; }
    public static int getMusicVolume() { return musicVolumePercent; }

    /** Re-applies the current channel volumes to every cached clip. */
    private static void applyAllVolumes() {
        for (Entry e : new ArrayList<>(cache.values())) {
            applyVolume(e.clip, volumeFor(e.channel));
        }
    }

    /** Returns the current volume percentage for the given channel. */
    private static int volumeFor(Channel ch) {
        return ch == Channel.MUSIC ? musicVolumePercent : sfxVolumePercent;
    }

    // ---------------------------------------------------------------
    // Loading + playback
    // ---------------------------------------------------------------

    /** Returns the cached clip for the given path, loading it on the SFX channel if needed. */
    public static Clip get(String path) {
        return get(path, Channel.SFX);
    }

    /**
     * Returns the cached clip for the given path, loading it on the given
     * channel if it isn't cached yet. Paths may be given with or without a
     * leading slash.
     *
     * @return the loaded clip, or {@code null} if the audio file is missing
     *         or could not be opened
     */
    public static Clip get(String path, Channel channel) {
        if (path == null) return null;
        if (!path.startsWith("/")) path = "/" + path;

        Entry e = cache.get(path);
        if (e != null && e.clip.isOpen()) return e.clip;

        try {
            InputStream is = AudioCache.class.getResourceAsStream(path);
            if (is == null) { System.out.println("Missing: " + path); return null; }
            AudioInputStream ais = AudioSystem.getAudioInputStream(is);
            Clip c = AudioSystem.getClip();
            c.open(ais);
            applyVolume(c, volumeFor(channel));
            cache.put(path, new Entry(c, channel));
            return c;
        } catch (Exception ex) {
            System.out.println("Load failed: " + path + " — " + ex.getMessage());
            return null;
        }
    }

    /** Plays the clip once from the start on the SFX channel. */
    public static Clip play(String path) {
        return play(path, Channel.SFX);
    }

    /**
     * Plays the clip once from the start on the given channel, restarting it
     * if it was already playing.
     *
     * @return the playing clip, or {@code null} if it could not be loaded
     */
    public static Clip play(String path, Channel channel) {
        Clip c = get(path, channel);
        if (c == null) {
            System.out.println("play(" + path + "): clip is NULL");
            return null;
        }
        System.out.println("play(" + path + "): clip length="
            + (c.getMicrosecondLength() / 1000) + "ms");
        c.stop();
        c.setFramePosition(0);
        c.start();
        return c;
    }

    /** Plays the clip continuously in a loop on the SFX channel. */
    public static Clip loop(String path) {
        return loop(path, Channel.SFX);
    }

    /**
     * Plays the clip continuously in a loop on the given channel, restarting
     * it from the beginning if it was already playing.
     *
     * @return the looping clip, or {@code null} if it could not be loaded
     */
    public static Clip loop(String path, Channel channel) {
        Clip c = get(path, channel);
        if (c == null) return null;
        c.stop();
        c.setFramePosition(0);
        c.loop(Clip.LOOP_CONTINUOUSLY);
        return c;
    }

    /** Pre-loads the given clips into the cache on the SFX channel. */
    public static void prepare(String... paths) {
        for (String p : paths) get(p);
    }

    /** Pre-loads the given clips into the cache on the given channel. */
    public static void prepare(Channel channel, String... paths) {
        for (String p : paths) get(p, channel);
    }

    /**
     * Stops and closes every cached clip and empties the cache.
     * Call this only when the game is truly exiting — clips cannot be
     * reopened after being closed.
     */
    public static void closeAll() {
        for (Entry e : new ArrayList<>(cache.values())) {
            try { e.clip.stop(); e.clip.close(); } catch (Exception ignored) {}
        }
        cache.clear();
    }

    /**
     * Applies the given volume percentage (0–100) to a clip via its
     * MASTER_GAIN control, if supported. 0 mutes; 100 leaves it unchanged.
     */
    public static void applyVolume(Clip c, int percent) {
        if (c == null) return;
        try {
            if (c.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl g = (FloatControl) c.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (percent <= 0) ? g.getMinimum()
                        : (float)(20.0 * Math.log10(percent / 100.0));
                g.setValue(Math.max(g.getMinimum(), Math.min(g.getMaximum(), dB)));
            }
        } catch (Exception ignored) {}
    }
}