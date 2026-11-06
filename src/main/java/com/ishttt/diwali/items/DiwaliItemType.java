package com.ishttt.diwali.items;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public enum DiwaliItemType {

    LIGHTER("lighter", Material.FLINT_AND_STEEL, 1020,
            "&#FFA500&lDiwali Lighter",
            Arrays.asList("&7A reliable golden spark lighter.", "&6Uses: &e100", "&eUsed to ignite Diyas & Crackers.")),

    GHEE("ghee", Material.HONEY_BOTTLE, 1021,
            "&#FFD700&lPure Desi Ghee",
            Arrays.asList("&7Golden clarified butter in an earthen jar.", "&6Use: &eFuels 1 Diya for 2 hours.", "&eRight-click an unlit Diya to fuel it.")),

    CLAY_DIYA("clay-diya", Material.BRICK, 1001,
            "&#E07A5F&lEarthen Clay Diya",
            Arrays.asList("&7Traditional hand-sculpted clay diya.", "&eCan be placed on solid blocks or water.", "&6Requires Desi Ghee & Lighter to burn.")),

    AKASH_KANDIL("akash-kandil", Material.LANTERN, 1003,
            "&#FF4500&lAkash Kandil",
            Arrays.asList("&7Vibrant octagonal hanging paper lantern.", "&eHangs under ceilings & fences with tassels.", "&6Emits warm festive glow.")),

    SKY_LANTERN("sky-lantern", Material.PAPER, 1004,
            "&#FFA07A&lSky Lantern",
            Arrays.asList("&7A delicate floating paper lantern.", "&6Right-click with Lighter &eto release it", "&einto the night sky with a warm glow.")),

    JHALAR_GOLDEN("jhalar-golden", Material.LEAD, 1010,
            "&#FFD700&lJhalar Light &8(&eGolden&8)",
            Arrays.asList("&7Fairy wire lights with warm golden bulbs.", "&6Click Block A, then Block B &eto hang.", "&eEmits bright Level 15 light.")),

    JHALAR_RGB("jhalar-rgb", Material.LEAD, 1011,
            "&#00FFFF&lJhalar Light &8(&dMulticolor RGB&8)",
            Arrays.asList("&7Fairy wire lights cycling festive colors.", "&6Click Block A, then Block B &eto hang.", "&eEmits bright Level 15 light.")),

    JHALAR_WHITE("jhalar-white", Material.LEAD, 1012,
            "&#FFFFFF&lJhalar Light &8(&fCool White&8)",
            Arrays.asList("&7Fairy wire lights with bright white bulbs.", "&6Click Block A, then Block B &eto hang.", "&eEmits bright Level 15 light.")),

    JHALAR_SAFFRON("jhalar-saffron", Material.LEAD, 1013,
            "&#FF8C00&lJhalar Light &8(&6Saffron&8)",
            Arrays.asList("&7Fairy wire lights with rich saffron bulbs.", "&6Click Block A, then Block B &eto hang.", "&eEmits bright Level 15 light.")),

    CHAKRI("chakri", Material.FIREWORK_STAR, 1030,
            "&#FFD700&lChakri &8(&eGround Spinner&8)",
            Arrays.asList("&7Whirls rapidly across the floor in bright sparks.", "&6Place on ground & ignite with Lighter.", "&cDanger radius: 2.5 blocks &8(0.5 heart/s)")),

    ANAR("anar", Material.FIRE_CHARGE, 1031,
            "&#E63946&lAnar &8(&6Flower Pot&8)",
            Arrays.asList("&7Towering cone shooting dazzling multi-colored sparks.", "&6Place on ground & ignite with Lighter.", "&cDanger radius: 2.0 blocks &8(0.5 heart/s)")),

    PHULJHADI("phuljhadi", Material.IRON_NUGGET, 1032,
            "&#F4A261&lPhuljhadi &8(&eSparkler Stick&8)",
            Arrays.asList("&7Handheld sparkling wire stick.", "&eHold near flame/diya for 15-20s to ignite.", "&6Burns for ~50s with sizzle trails.")),

    LADI_100("ladi-100", Material.REDSTONE, 1034,
            "&#D62828&lLadi &8(&e100-Wala Garland&8)",
            Arrays.asList("&7Chain of rapid-fire pop-pop explosions.", "&6Place on ground & ignite with Lighter.", "&eDuration: 6 seconds rapid pops.")),

    LADI_1000("ladi-1000", Material.TNT, 1035,
            "&#9D0208&lMega Ladi &8(&c1000-Wala Garland&8)",
            Arrays.asList("&7Massive chain of machine-gun popping crackers.", "&6Place on ground & ignite with Lighter.", "&eHeavy smoke & loud consecutive bangs!")),

    SUTLI_BOMB("sutli-bomb", Material.SLIME_BALL, 1036,
            "&#2B9348&lSutli Bomb &8(&aAtom Bomb&8)",
            Arrays.asList("&7Jute string bomb with tremendous explosive power.", "&cFuse: 4s | 7 Hearts True Blast Damage!", "&4Causes Burnt effect! Terrain is safe.")),

    SKYROCKET("skyrocket", Material.FIREWORK_ROCKET, 1037,
            "&#00B4D8&lDiwali Skyrocket",
            Arrays.asList("&7High-speed screaming whistling rocket.", "&6Place or light &eto shoot 30 blocks up", "&ewith a sharp celebratory crack!")),

    SKYSHOT("skyshot", Material.FIREWORK_ROCKET, 1038,
            "&#7209B7&lDiwali Skyshot &8(&dGrand Shell&8)",
            Arrays.asList("&7Mortar shell launching a giant willow starburst.", "&6Place on ground & ignite with Lighter.", "&eColossal aerial explosion.")),

    SKYSHOT_CAKE("skyshot-cake", Material.FIREWORK_ROCKET, 1039,
            "&#F72585&lDiwali Skyshot Cake &8(&e12-Shot&8)",
            Arrays.asList("&7Heavy firework box firing 12 consecutive shells.", "&6Place on ground & ignite with Lighter.", "&eDazzling 15-second sky spectacle!")),

    MARIGOLD_TORAN("marigold-toran", Material.VINE, 1040,
            "&#FFB703&lMarigold Flower Toran",
            Arrays.asList("&7Vibrant yellow and orange marigold garland.", "&ePlace above doorways and gates.", "&6Snip with Shears to collect.")),

    KAJU_KATLI("kaju-katli", Material.COOKIE, 1050,
            "&#E9D8A6&lKaju Katli",
            Arrays.asList("&7Diamond-shaped cashew sweet with silver foil.", "&aGrants: Haste II & Speed I &8(3 min)", "&6Delicious festive mithai.")),

    MOTICHOOR_LADOO("motichoor-ladoo", Material.GOLDEN_CARROT, 1051,
            "&#FB8500&lMotichoor Ladoo",
            Arrays.asList("&7Melt-in-mouth golden gram flour pearls.", "&aGrants: Absorption II & Saturation &8(3 min)", "&6A Diwali classic sweet.")),

    GULAB_JAMUN("gulab-jamun", Material.BEETROOT, 1052,
            "&#6F1D1B&lGulab Jamun",
            Arrays.asList("&7Soft deep-fried milk dumplings in saffron syrup.", "&aGrants: Regeneration I & Festive Glow &8(3 min)", "&6Rich and decadent.")),

    DIWALI_GIFT_BOX("diwali-gift-box", Material.CHEST, 1060,
            "&#E63946&lDiwali Gift Box",
            Arrays.asList("&7Festive gift box wrapped in ribbon.", "&6Right-click &eto pack up to 9 gifts for a friend.", "&eRecipient left-clicks to unwrap with confetti!"));

    private final String id;
    private final Material material;
    private final int customModelData;
    private final String displayName;
    private final List<String> lore;

    DiwaliItemType(String id, Material material, int customModelData, String displayName, List<String> lore) {
        this.id = id;
        this.material = material;
        this.customModelData = customModelData;
        this.displayName = displayName;
        this.lore = lore;
    }

    public String getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getLore() {
        return lore;
    }

    public static DiwaliItemType fromId(String id) {
        for (DiwaliItemType item : values()) {
            if (item.getId().equalsIgnoreCase(id) || item.name().equalsIgnoreCase(id)) {
                return item;
            }
        }
        return null;
    }
}
