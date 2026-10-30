package theforthtenant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Session-wide state for a single playthrough. Holds everything that must
 * survive a frame being closed and re-opened: collected evidence, unlocked
 * menus, last visited room, and discovered deductions (from the combine
 * system).
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
    public static final int TOTAL_ITEMS = 14;

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
    
    public static boolean isFullyComplete() {
        return allCollected() && discoveredCount() >= 8; 
    }
    
    private static boolean phoneUnlocked = false;

    public static boolean isPhoneUnlocked() {
        return phoneUnlocked;
    }

    public static void setPhoneUnlocked(boolean unlocked) {
        phoneUnlocked = unlocked;
    }
    
    private static boolean tapeAnimationPlayed = false;

    public static boolean hasTapeAnimationPlayed() {
        return tapeAnimationPlayed;
    }

    public static void setTapeAnimationPlayed(boolean played) {
        tapeAnimationPlayed = played;
    }    
    
    // ---------------------------------------------------------------
    // Interrogation Triggers (for the Suspects board)
    // ---------------------------------------------------------------
    private static final Set<String> interrogatedTopics = 
        Collections.synchronizedSet(new HashSet<>());
        
    private static final List<String> interrogationOrder = new ArrayList<>();

    public static void markInterrogated(String topicId) {
        if (topicId != null && !interrogatedTopics.contains(topicId)) {
            interrogatedTopics.add(topicId);
            interrogationOrder.add(topicId);
        }
    }
    
    public static List<String> getInterrogationOrder() {
        return new ArrayList<>(interrogationOrder);
    }    

    public static boolean isInterrogated(String topicId) {
        return interrogatedTopics.contains(topicId);
    }
    
    // ---------------------------------------------------------------
    // HealthBar
    // ---------------------------------------------------------------

    private static int health = 100;
    public static int getHealth() { return health; }
    public static void setHealth(int h) { health = Math.max(0, Math.min(100, h)); }

    // ---------------------------------------------------------------
    // Discovered deductions (combine results)
    // ---------------------------------------------------------------

    /**
     * Result ids (e.g. "medicine") that the player has successfully combined
     * at least once this session. Used so the "New Discovery" toast only
     * fires on the first combine, not on every repeat.
     */
    private static final Set<String> discovered =
        Collections.synchronizedSet(new HashSet<>());

    /** Records that a combination (result id) has been discovered. */
    public static void markDiscovered(String resultId) {
        if (resultId != null) discovered.add(resultId);
    }

    /** Returns true if the combination (result id) has already been discovered. */
    public static boolean isDiscovered(String resultId) {
        return resultId != null && discovered.contains(resultId);
    }

    /** Returns the number of distinct deductions discovered this session. */
    public static int discoveredCount() {
        return discovered.size();
    }

    // ---------------------------------------------------------------
    // Scene position
    // ---------------------------------------------------------------

    private static int lastSceneIndex = 0;

    public static int  getLastSceneIndex()          { return lastSceneIndex; }
    public static void setLastSceneIndex(int index) { lastSceneIndex = index; }

    
    // ---------------------------------------------------------------
    // Interrogate
    // ---------------------------------------------------------------  
    
    /** Returns true if all 10 core interrogation topics have been triggered. */
    public static boolean allInterrogated() {
        // Cardo (4)
        boolean cardoDone = interrogatedTopics.contains("cardo_time") &&
                            interrogatedTopics.contains("cardo_boots") &&
                            interrogatedTopics.contains("cardo_damage_lock") &&
                            interrogatedTopics.contains("cardo_jealousy");
                            
        // Cathy (4)
        boolean cathyDone = interrogatedTopics.contains("cathy_time") &&
                            interrogatedTopics.contains("cathy_audio") &&
                            interrogatedTopics.contains("cathy_rag") &&
                            interrogatedTopics.contains("cathy_gym_bag");

        // Andy (3)
        boolean andyDone = interrogatedTopics.contains("andy_time") &&
                           interrogatedTopics.contains("andy_debt") &&
                           interrogatedTopics.contains("andy_pills");

        return cardoDone && cathyDone && andyDone;
    }    
    
    // ---------------------------------------------------------------
    // Detective's Notes
    // ---------------------------------------------------------------
    private static String detectiveNotesLeft = "";
    private static String detectiveNotesRight = "";

    public static String getDetectiveNotesLeft() { return detectiveNotesLeft; }
    public static void setDetectiveNotesLeft(String notes) { detectiveNotesLeft = notes; }

    public static String getDetectiveNotesRight() { return detectiveNotesRight; }
    public static void setDetectiveNotesRight(String notes) { detectiveNotesRight = notes; } 
    
    // ---------------------------------------------------------------
    // Endgame phase
    // ---------------------------------------------------------------
    
    private static boolean policeReportDone = false;

    public static boolean isPoliceReportDone() { return policeReportDone; }
    public static void setPoliceReportDone(boolean done) { policeReportDone = done; }   
    
    private static boolean cathyConfronted = false;
    public static boolean isCathyConfronted() { return cathyConfronted; }
    public static void setCathyConfronted(boolean done) { cathyConfronted = done; }
    
    private static boolean cathyArrested = false;
    public static boolean isCathyArrested() { return cathyArrested; }
    public static void setCathyArrested(boolean arrested) { cathyArrested = arrested; }
    
    // ---------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------

    /** Wipes all state — call when starting a new game. */
    public static void reset() {
        collected.clear();
        discovered.clear();
        lastSceneIndex = 0;
        tapeAnimationPlayed = false;
        interrogatedTopics.clear();
        interrogationOrder.clear();
        policeReportDone = false;
        cathyConfronted = false;
        health = 100; 
        phoneUnlocked = false; 
        policeReportDone = false;
        cathyConfronted = false;
        cathyArrested = false;     
        detectiveNotesLeft = "";
        detectiveNotesRight = "";
    }   
    
    private GameState() {}
}