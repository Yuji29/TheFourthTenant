package theforthtenant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Session-wide state for a single playthrough. Holds everything that must
 * survive a frame being closed and re-opened: collected evidence, unlocked
 * menus, last visited room, and (later) notes, suspect flags, and case-file
 * entries.
 *
 * All state is static — one instance per JVM run. Call {@link #reset()} when
 * starting a new game.
 */
public final class GameState {

    // ---------------------------------------------------------------
    // Evidence
    // ---------------------------------------------------------------

    private static final List<String> collected =
        Collections.synchronizedList(new ArrayList<>());

    /** Total number of collectable items in the game. */
    public static final int TOTAL_ITEMS = 12;

    public static void markCollected(String id) {
        if (id == null) return;
        if (!collected.contains(id)) collected.add(id);
    }

    public static List<String> getCollected() {
        synchronized (collected) {
            return new ArrayList<>(collected);
        }
    }

    public static boolean isCollected(String id) {
        return collected.contains(id);
    }

    public static int collectedCount() {
        return collected.size();
    }

    public static boolean allCollected() {
        return collectedCount() >= TOTAL_ITEMS;
    }

    // ---------------------------------------------------------------
    // Scene position
    // ---------------------------------------------------------------

    private static int lastSceneIndex = 0;

    public static int  getLastSceneIndex()          { return lastSceneIndex; }
    public static void setLastSceneIndex(int index) { lastSceneIndex = index; }

    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

    /** Wipes all state — call when starting a new game. */
    public static void reset() {
        collected.clear();
        lastSceneIndex = 0;
    }

    private GameState() {}
}