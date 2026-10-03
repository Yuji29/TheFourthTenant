package theforthtenant;

import java.util.HashMap;
import java.util.Map;

/**
 * Static lookup table for item metadata: display name, description text,
 * and (optionally) the classpath path of the large description image.
 *
 * Image path defaults to:
 *   /Images/gameplay/inventory/item_description/description_slot_<id>.png
 * so you only need to override it if an item uses a different file name.
 *
 * IDs are stable and match GameState / inventory icon filenames.
 * Display names are what the player sees.
 */
public final class ItemDatabase {

    public static final class Item {
        public final String id;
        public final String displayName;   // shown in red above the divider
        public final String description;   // shown in white below the divider
        public final String imagePath;     // nullable → falls back to default path

        Item(String id, String displayName, String description, String imagePath) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.imagePath = imagePath;
        }
    }

    private static final Map<String, Item> ITEMS = new HashMap<>();

    static {
        register("padlock",
                "Scratched Padlocks",
                "Deep scratches on the padlock of the door leading to the rooftop.");

        register("toolbox",
                "Toolbox",
                "A missing lockpick and pliers from Cardo's always-open toolbox in the living room.");

        register("mug",
                "Abby's Leftover Coffee",
                "Traces of a strong sleeping pill in Abby's leftover coffee in the kitchen.");

        register("prescription",
                "Torn Prescription Receipt",
                "A torn prescription receipt from Andy's medical bag left on the dining table.");

        register("footmarks",
                "Muddy Footprints (Size 9)",
                "Large muddy size 9 shoe footprints around the water drum on the rooftop.");

        register("boots",
                "Wet Heavy Boots",
                "Cardo's soaking wet heavy boots hidden behind the washing machine.");

        register("rag",
                "Bleach-Soaked Bloody Rag",
                "A bloody rag smelling heavily of bleach, under Cathy's bed.");

        register("drum",
                "Blue Water Drum",
                "Traces of strong bleach on the side of the drum where the body was found.");

        register("usb",
                "Flash Drive",
                "Audio recording from a neighbor's CCTV, featuring a woman screaming \"Leave me alone!\" around 2:00 AM.");

        register("phone",
                "Abby's Smartphone",
                "A drafted text message on Abby's phone created at 1:55 AM stating, \"You are completely losing it, girl. Stay away from me.\"");

        register("dragpath",
                "Floor Drag Marks",
                "Heavy drag marks on the rooftop floor, indicating something heavy was pulled.");

        register("belt",
                "Broken Strap",
                "A broken strap from a large canvas gym bag belonging to Cathy, left near the rooftop door");
    }

    private static void register(String id, String name, String desc) {
        register(id, name, desc, null);
    }

    private static void register(String id, String name, String desc, String imageOverride) {
        ITEMS.put(id, new Item(id, name, desc, imageOverride));
    }

    /** Returns the item, or {@code null} if the id is unknown. */
    public static Item get(String id) {
        return ITEMS.get(id);
    }

    /** Returns the classpath path to the large description image for the id. */
    public static String descriptionImagePath(String id) {
        Item it = ITEMS.get(id);
        if (it != null && it.imagePath != null) return it.imagePath;
        return "/Images/gameplay/inventory/item_description/description_slot_" + id + ".png";
    }

    private ItemDatabase() {}
}