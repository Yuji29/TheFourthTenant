package theforthtenant;

import java.util.HashMap;
import java.util.Map;

/**
 * Static lookup table for item metadata: display name, description text,
 * and (optionally) the class path path of the large description image.
 */
public final class ItemDatabase {

    public static final class Item {
        public final String id;
        public final String displayName;
        public final String description;
        public final String imagePath;   // null → default path is used

        Item(String id, String displayName, String description, String imagePath) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.imagePath = imagePath;
        }
    }

    private static final Map<String, Item> ITEMS   = new HashMap<>();
    private static final Map<String, String> RECIPES = new HashMap<>();

    // ---------------------------------------------------------------
    // Item registry
    // ---------------------------------------------------------------
    static {
        register("padlock",
                "Scratched Padlocks",
                "Deep scratches on the padlock of the door leading to the rooftop.");

        register("toolbox",
                "Toolbox",
                "A missing lockpick and pliers from Cardo's always-open toolbox in the living room.");

        register("mug",
                "Abby's Coffee",
                "Traces of a strong sleeping pill in Abby's leftover coffee in the kitchen.");

        register("prescription",
                "Prescription Receipt",
                "A torn prescription receipt from Andy's medical bag left on the dining table.");

        register("footmarks",
                "Footprints (Size 9)",
                "Large muddy size 9 shoe footprints around the water drum on the rooftop.");

        register("boots",
                "Wet Heavy Boots",
                "Cardo's soaking wet heavy boots hidden behind the washing machine.");

        register("rag",
                "Bleach-Soaked Rag",
                "A rag smelling heavily of bleach, under Cathy's bed.");

        register("drum",
                "Blue Water Drum",
                "Traces of strong bleach on the side of the drum where the body was found.");

        register("usb",
                "Flash Drive",
                "An audio recording retrieved from a neighbor's CCTV system, capturing a female voice screaming \"Leave me alone\" around 2:00 AM.");

        register("phone",
                "Abby's Smartphone",
                "Protected by a 4-digits PIN passcode. Unlock to view its contents.");

        register("dragpath",
                "Floor Drag Marks",
                "Heavy drag marks on the rooftop floor, indicating something heavy was pulled.");

        register("belt",
                "Broken Strap",
                "A broken strap from a large canvas gym bag belonging to Cathy, left near the rooftop door");
        
        register("love_letters",
                "Love Letters",
                "A stack of old love letters and hand written notes kept in Cardo's room, detailing his stormy, on-and-off relationship with Abby.");
        
        register("ledger",
                "Ledger",
                "A small, concealed ledger notebook found inside Andy's medical bag listing personal financial records, including unpaid debts owed to Abby totaling \"67,000\".");

        // ---- Combined items (produced by the combine system) ----
        register("damaged_lock",
                "Damaged Lock",
                "The rooftop door was forced open using Cardo's tools.",
                combinedPath("damaged_lock"));

        register("drugged_coffee",
                "Drugged Coffee",
                "Abby was first drugged indoors using a medication that is prescribed to Andy, causing her to lose consciousness.",
                combinedPath("drugged_coffee"));

        register("shoeprints",
                "Size 9 Shoeprints",
                "The killer wore Cardo's boots, leaving large footprints.",
                combinedPath("shoeprints"));

        register("blood_cleanup",
                "Blood Clean-Up",
                "A struggle ensued, resulting in fatal head trauma. The killer cleaned the scene and hid the bleach-soaked rag in Cathy's belongings.",
                combinedPath("blood_cleanup"));

        register("audio_log",
                "Audio Log",
                "CCTV audio recording on the flash captures an intense argument between Abby and another woman shortly before the murder, pointing to a female suspect.",
                combinedPath("audio_log"));

        register("gym_bag_trace",
                "Gym Bag Trace",
                "Cathy's bag was used to drag an unconscious Abby upstairs to the rooftop drum before the final struggle.",
                combinedPath("gym_bag_trace"));
        
        // --- NEW COMBINED ITEMS ---
        register("jealousy",
                "Possessive Jealousy",
                "Cardo harbored extreme, possessive jealousy towards Abby and previously threatened that neither of them would live if she left him.",
                combinedPath("jealousy"));
        
        register("opened_ledger",
                "Financial Debt",
                "Andy owed Abby a massive sum of money (67,000) and was facing severe pressure and harsh demands from her for immediate repayment.",
                combinedPath("opened_ledger"));

        
        // ---- Recipes ----
        recipe("damaged_lock",   "padlock",      "toolbox");
        recipe("drugged_coffee", "mug",          "prescription");
        recipe("shoeprints",     "footmarks",    "boots");
        recipe("blood_cleanup",  "rag",          "drum");
        recipe("audio_log",      "usb",          "phone");
        recipe("gym_bag_trace",  "dragpath",     "belt");
        
        // --- NEW RECIPES ---
        recipe("jealousy",       "phone",        "love_letters");
        recipe("opened_ledger",  "phone",        "ledger");
    }

    // ---------------------------------------------------------------
    // Registration helpers
    // ---------------------------------------------------------------

    private static void register(String id, String name, String desc) {
        register(id, name, desc, null);
    }

    private static void register(String id, String name, String desc, String imageOverride) {
        ITEMS.put(id, new Item(id, name, desc, imageOverride));
    }

    private static void recipe(String result, String a, String b) {
        String key = (a.compareTo(b) <= 0) ? a + "+" + b : b + "+" + a;
        RECIPES.put(key, result);
    }

    // ---------------------------------------------------------------
    // Lookup
    // ---------------------------------------------------------------

    public static Item get(String id) {
        return ITEMS.get(id);
    }

    public static String descriptionImagePath(String id) {
        Item it = ITEMS.get(id);
        if (it != null && it.imagePath != null) return it.imagePath;
        return "/Images/gameplay/inventory/item_description/description_slot_" + id + ".png";
    }

    public static String getRecipe(String a, String b) {
        if (a == null || b == null) return null;
        String key = (a.compareTo(b) <= 0) ? a + "+" + b : b + "+" + a;
        return RECIPES.get(key);
    }

    private static String combinedPath(String id) {
        return "/Images/gameplay/inventory/combined/combined_" + id + ".png";
    }

    private ItemDatabase() {}
}