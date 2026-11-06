package com.ishttt.diwali.items;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ItemManager {

    private final DiwaliPlugin plugin;
    private final NamespacedKey itemKey;
    private final NamespacedKey recipientKey;
    private final NamespacedKey senderKey;
    private final NamespacedKey giftContentsKey;
    private final NamespacedKey lighterUsesKey;

    public ItemManager(DiwaliPlugin plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "diwali_item");
        this.recipientKey = new NamespacedKey(plugin, "gift_recipient");
        this.senderKey = new NamespacedKey(plugin, "gift_sender");
        this.giftContentsKey = new NamespacedKey(plugin, "gift_contents");
        this.lighterUsesKey = new NamespacedKey(plugin, "lighter_uses");

        registerRecipes();
    }

    public NamespacedKey getItemKey() {
        return itemKey;
    }

    public NamespacedKey getRecipientKey() {
        return recipientKey;
    }

    public NamespacedKey getSenderKey() {
        return senderKey;
    }

    public NamespacedKey getGiftContentsKey() {
        return giftContentsKey;
    }

    public NamespacedKey getLighterUsesKey() {
        return lighterUsesKey;
    }

    public ItemStack createItem(DiwaliItemType type, int amount) {
        ItemStack item = new ItemStack(type.getMaterial(), amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatUtils.color(type.getDisplayName()));
            meta.setCustomModelData(type.getCustomModelData());
            try {
                java.lang.reflect.Method m = meta.getClass().getMethod("setItemModel", org.bukkit.NamespacedKey.class);
                m.invoke(meta, new org.bukkit.NamespacedKey("diwali", type.getId().replace("-", "_")));
            } catch (Throwable ignored) {}

            List<String> coloredLore = new ArrayList<>();
            for (String line : type.getLore()) {
                coloredLore.add(ChatUtils.color(line));
            }
            meta.setLore(coloredLore);

            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(itemKey, PersistentDataType.STRING, type.getId());

            if (type == DiwaliItemType.LIGHTER) {
                pdc.set(lighterUsesKey, PersistentDataType.INTEGER, 100);
            }

            item.setItemMeta(meta);
        }
        return item;
    }

    public DiwaliItemType getDiwaliType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(itemKey, PersistentDataType.STRING)) return null;
        String id = pdc.get(itemKey, PersistentDataType.STRING);
        return DiwaliItemType.fromId(id);
    }

    public boolean isDiwaliItem(ItemStack item, DiwaliItemType type) {
        return getDiwaliType(item) == type;
    }

    private void registerRecipes() {
        registerGheeRecipe();
        registerLighterRecipe();
        registerClayDiyaRecipe();
        registerAkashKandilRecipe();
        registerSkyLanternRecipe();
        registerJhalarRecipes();
        registerCrackerRecipes();
        registerToranRecipe();
        registerSweetsRecipes();
        registerGiftBoxRecipe();
    }

    private void registerGheeRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "recipe_ghee_smelt");
        Bukkit.removeRecipe(recipeKey);
        FurnaceRecipe recipe = new FurnaceRecipe(recipeKey, createItem(DiwaliItemType.GHEE, 1),
                Material.MILK_BUCKET, 0.5f, 200);
        Bukkit.addRecipe(recipe);
    }

    private void registerLighterRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "recipe_lighter");
        Bukkit.removeRecipe(recipeKey);
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, createItem(DiwaliItemType.LIGHTER, 1));
        recipe.shape(" G ", " I ", " F ");
        recipe.setIngredient('G', Material.GOLD_NUGGET);
        recipe.setIngredient('I', Material.IRON_NUGGET);
        recipe.setIngredient('F', Material.FLINT);
        Bukkit.addRecipe(recipe);
    }

    private void registerClayDiyaRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "recipe_clay_diya");
        Bukkit.removeRecipe(recipeKey);
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, createItem(DiwaliItemType.CLAY_DIYA, 2));
        recipe.shape("   ", "C C", " C ");
        recipe.setIngredient('C', Material.CLAY_BALL);
        Bukkit.addRecipe(recipe);
    }

    private void registerAkashKandilRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "recipe_akash_kandil");
        Bukkit.removeRecipe(recipeKey);
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, createItem(DiwaliItemType.AKASH_KANDIL, 1));
        recipe.shape(" S ", "PTP", " D ");
        recipe.setIngredient('S', Material.STICK);
        recipe.setIngredient('P', Material.PAPER);
        recipe.setIngredient('T', Material.TORCH);
        recipe.setIngredient('D', Material.RED_DYE);
        Bukkit.addRecipe(recipe);
    }

    private void registerSkyLanternRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "recipe_sky_lantern");
        Bukkit.removeRecipe(recipeKey);
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, createItem(DiwaliItemType.SKY_LANTERN, 1));
        recipe.shape(" P ", "PTP", " S ");
        recipe.setIngredient('P', Material.PAPER);
        recipe.setIngredient('T', Material.TORCH);
        recipe.setIngredient('S', Material.STRING);
        Bukkit.addRecipe(recipe);
    }

    private void registerJhalarRecipes() {
        registerJhalarVariant("golden", DiwaliItemType.JHALAR_GOLDEN, Material.YELLOW_DYE);
        registerJhalarVariant("rgb", DiwaliItemType.JHALAR_RGB, Material.CYAN_DYE);
        registerJhalarVariant("white", DiwaliItemType.JHALAR_WHITE, Material.WHITE_DYE);
        registerJhalarVariant("saffron", DiwaliItemType.JHALAR_SAFFRON, Material.ORANGE_DYE);
    }

    private void registerJhalarVariant(String name, DiwaliItemType type, Material dye) {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "recipe_jhalar_" + name);
        Bukkit.removeRecipe(recipeKey);
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, createItem(type, 4));
        recipe.shape("SSS", "GDG", "SSS");
        recipe.setIngredient('S', Material.STRING);
        recipe.setIngredient('G', Material.GLOWSTONE_DUST);
        recipe.setIngredient('D', dye);
        Bukkit.addRecipe(recipe);
    }

    private void registerCrackerRecipes() {
        // Chakri
        NamespacedKey chakriKey = new NamespacedKey(plugin, "recipe_chakri");
        Bukkit.removeRecipe(chakriKey);
        ShapelessRecipe chakri = new ShapelessRecipe(chakriKey, createItem(DiwaliItemType.CHAKRI, 2));
        chakri.addIngredient(Material.GUNPOWDER);
        chakri.addIngredient(Material.PAPER);
        chakri.addIngredient(Material.GOLD_NUGGET);
        Bukkit.addRecipe(chakri);

        // Anar
        NamespacedKey anarKey = new NamespacedKey(plugin, "recipe_anar");
        Bukkit.removeRecipe(anarKey);
        ShapedRecipe anar = new ShapedRecipe(anarKey, createItem(DiwaliItemType.ANAR, 2));
        anar.shape(" G ", " C ", " F ");
        anar.setIngredient('G', Material.GUNPOWDER);
        anar.setIngredient('C', Material.CLAY_BALL);
        anar.setIngredient('F', Material.FIRE_CHARGE);
        Bukkit.addRecipe(anar);

        // Phuljhadi
        NamespacedKey phuljhadiKey = new NamespacedKey(plugin, "recipe_phuljhadi");
        Bukkit.removeRecipe(phuljhadiKey);
        ShapedRecipe phuljhadi = new ShapedRecipe(phuljhadiKey, createItem(DiwaliItemType.PHULJHADI, 4));
        phuljhadi.shape(" G ", " I ", " I ");
        phuljhadi.setIngredient('G', Material.GLOWSTONE_DUST);
        phuljhadi.setIngredient('I', Material.IRON_NUGGET);
        Bukkit.addRecipe(phuljhadi);

        // Ladi 100
        NamespacedKey ladiKey = new NamespacedKey(plugin, "recipe_ladi_100");
        Bukkit.removeRecipe(ladiKey);
        ShapedRecipe ladi = new ShapedRecipe(ladiKey, createItem(DiwaliItemType.LADI_100, 2));
        ladi.shape(" R ", " G ", " P ");
        ladi.setIngredient('R', Material.REDSTONE);
        ladi.setIngredient('G', Material.GUNPOWDER);
        ladi.setIngredient('P', Material.PAPER);
        Bukkit.addRecipe(ladi);

        // Ladi 1000
        NamespacedKey megaLadiKey = new NamespacedKey(plugin, "recipe_ladi_1000");
        Bukkit.removeRecipe(megaLadiKey);
        ShapedRecipe megaLadi = new ShapedRecipe(megaLadiKey, createItem(DiwaliItemType.LADI_1000, 1));
        megaLadi.shape("LLL", "GSG", "LLL");
        megaLadi.setIngredient('L', Material.REDSTONE);
        megaLadi.setIngredient('G', Material.GUNPOWDER);
        megaLadi.setIngredient('S', Material.STRING);
        Bukkit.addRecipe(megaLadi);

        // Sutli Bomb
        NamespacedKey bombKey = new NamespacedKey(plugin, "recipe_sutli_bomb");
        Bukkit.removeRecipe(bombKey);
        ShapedRecipe bomb = new ShapedRecipe(bombKey, createItem(DiwaliItemType.SUTLI_BOMB, 2));
        bomb.shape(" S ", "SGS", " S ");
        bomb.setIngredient('S', Material.STRING);
        bomb.setIngredient('G', Material.GUNPOWDER);
        Bukkit.addRecipe(bomb);

        // Skyrocket
        NamespacedKey skyrocketKey = new NamespacedKey(plugin, "recipe_skyrocket");
        Bukkit.removeRecipe(skyrocketKey);
        ShapedRecipe skyrocket = new ShapedRecipe(skyrocketKey, createItem(DiwaliItemType.SKYROCKET, 2));
        skyrocket.shape(" F ", " P ", " G ");
        skyrocket.setIngredient('F', Material.FIREWORK_STAR);
        skyrocket.setIngredient('P', Material.PAPER);
        skyrocket.setIngredient('G', Material.GUNPOWDER);
        Bukkit.addRecipe(skyrocket);

        // Skyshot
        NamespacedKey skyshotKey = new NamespacedKey(plugin, "recipe_skyshot");
        Bukkit.removeRecipe(skyshotKey);
        ShapedRecipe skyshot = new ShapedRecipe(skyshotKey, createItem(DiwaliItemType.SKYSHOT, 1));
        skyshot.shape(" D ", " F ", " G ");
        skyshot.setIngredient('D', Material.DIAMOND);
        skyshot.setIngredient('F', Material.FIREWORK_STAR);
        skyshot.setIngredient('G', Material.GUNPOWDER);
        Bukkit.addRecipe(skyshot);

        // Skyshot Cake
        NamespacedKey cakeKey = new NamespacedKey(plugin, "recipe_skyshot_cake");
        Bukkit.removeRecipe(cakeKey);
        ShapedRecipe cake = new ShapedRecipe(cakeKey, createItem(DiwaliItemType.SKYSHOT_CAKE, 1));
        cake.shape("RRR", "RCR", "RRR");
        cake.setIngredient('R', Material.FIREWORK_ROCKET);
        cake.setIngredient('C', Material.CHEST);
        Bukkit.addRecipe(cake);
    }

    private void registerToranRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "recipe_marigold_toran");
        Bukkit.removeRecipe(recipeKey);
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, createItem(DiwaliItemType.MARIGOLD_TORAN, 2));
        recipe.shape("SSS", "YOY", "OYO");
        recipe.setIngredient('S', Material.STRING);
        recipe.setIngredient('Y', Material.YELLOW_DYE);
        recipe.setIngredient('O', Material.ORANGE_DYE);
        Bukkit.addRecipe(recipe);
    }

    private void registerSweetsRecipes() {
        // Kaju Katli
        NamespacedKey kajuKey = new NamespacedKey(plugin, "recipe_kaju_katli");
        Bukkit.removeRecipe(kajuKey);
        ShapelessRecipe kaju = new ShapelessRecipe(kajuKey, createItem(DiwaliItemType.KAJU_KATLI, 4));
        kaju.addIngredient(Material.SUGAR);
        kaju.addIngredient(Material.MILK_BUCKET);
        kaju.addIngredient(Material.IRON_NUGGET);
        Bukkit.addRecipe(kaju);

        // Motichoor Ladoo
        NamespacedKey ladooKey = new NamespacedKey(plugin, "recipe_motichoor_ladoo");
        Bukkit.removeRecipe(ladooKey);
        ShapelessRecipe ladoo = new ShapelessRecipe(ladooKey, createItem(DiwaliItemType.MOTICHOOR_LADOO, 4));
        ladoo.addIngredient(Material.SUGAR);
        ladoo.addIngredient(Material.WHEAT);
        ladoo.addIngredient(Material.ORANGE_DYE);
        Bukkit.addRecipe(ladoo);

        // Gulab Jamun
        NamespacedKey jamunKey = new NamespacedKey(plugin, "recipe_gulab_jamun");
        Bukkit.removeRecipe(jamunKey);
        ShapelessRecipe jamun = new ShapelessRecipe(jamunKey, createItem(DiwaliItemType.GULAB_JAMUN, 4));
        jamun.addIngredient(Material.SUGAR);
        jamun.addIngredient(Material.MILK_BUCKET);
        jamun.addIngredient(Material.HONEY_BOTTLE);
        Bukkit.addRecipe(jamun);
    }

    private void registerGiftBoxRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "recipe_gift_box");
        Bukkit.removeRecipe(recipeKey);
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, createItem(DiwaliItemType.DIWALI_GIFT_BOX, 1));
        recipe.shape(" R ", "PCP", " P ");
        recipe.setIngredient('R', Material.RED_DYE);
        recipe.setIngredient('P', Material.PAPER);
        recipe.setIngredient('C', Material.CHEST);
        Bukkit.addRecipe(recipe);
    }
}
