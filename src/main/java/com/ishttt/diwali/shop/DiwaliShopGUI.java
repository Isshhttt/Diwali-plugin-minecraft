package com.ishttt.diwali.shop;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.items.DiwaliItemType;
import com.ishttt.diwali.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class DiwaliShopGUI implements Listener {

    private final DiwaliPlugin plugin;
    private final String shopTitle;
    private final Map<Integer, DiwaliItemType> slotItemMap = new HashMap<>();

    public DiwaliShopGUI(DiwaliPlugin plugin) {
        this.plugin = plugin;
        this.shopTitle = ChatUtils.color("&6&l🪔 Diwali Festival Bazaar");
        setupShopSlots();
    }

    private void setupShopSlots() {
        // Lighting & Fuel Row
        slotItemMap.put(10, DiwaliItemType.LIGHTER);
        slotItemMap.put(11, DiwaliItemType.GHEE);
        slotItemMap.put(12, DiwaliItemType.CLAY_DIYA);
        slotItemMap.put(13, DiwaliItemType.AKASH_KANDIL);
        slotItemMap.put(14, DiwaliItemType.SKY_LANTERN);
        slotItemMap.put(15, DiwaliItemType.MARIGOLD_TORAN);
        slotItemMap.put(16, DiwaliItemType.DIWALI_GIFT_BOX);

        // Jhalar Lights Row
        slotItemMap.put(19, DiwaliItemType.JHALAR_GOLDEN);
        slotItemMap.put(20, DiwaliItemType.JHALAR_RGB);
        slotItemMap.put(21, DiwaliItemType.JHALAR_WHITE);
        slotItemMap.put(22, DiwaliItemType.JHALAR_SAFFRON);

        // Sweets Row
        slotItemMap.put(23, DiwaliItemType.KAJU_KATLI);
        slotItemMap.put(24, DiwaliItemType.MOTICHOOR_LADOO);
        slotItemMap.put(25, DiwaliItemType.GULAB_JAMUN);

        // Crackers (Patakhe) Row
        slotItemMap.put(28, DiwaliItemType.PHULJHADI);
        slotItemMap.put(29, DiwaliItemType.CHAKRI);
        slotItemMap.put(30, DiwaliItemType.ANAR);
        slotItemMap.put(31, DiwaliItemType.LADI_100);
        slotItemMap.put(32, DiwaliItemType.LADI_1000);
        slotItemMap.put(33, DiwaliItemType.SUTLI_BOMB);
        slotItemMap.put(34, DiwaliItemType.SKYROCKET);
        slotItemMap.put(37, DiwaliItemType.SKYSHOT);
        slotItemMap.put(38, DiwaliItemType.SKYSHOT_CAKE);
    }

    public void openShop(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, shopTitle);

        // Decorative glass border
        ItemStack border = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta bMeta = border.getItemMeta();
        if (bMeta != null) {
            bMeta.setDisplayName(" ");
            border.setItemMeta(bMeta);
        }

        ItemStack yellowBorder = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta yMeta = yellowBorder.getItemMeta();
        if (yMeta != null) {
            yMeta.setDisplayName(" ");
            yellowBorder.setItemMeta(yMeta);
        }

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inv.setItem(i, (i % 2 == 0) ? border : yellowBorder);
            }
        }

        // Fill shop items
        for (var entry : slotItemMap.entrySet()) {
            int slot = entry.getKey();
            DiwaliItemType type = entry.getValue();
            int price = plugin.getConfig().getInt("shop.prices." + type.getId(), 2);

            ItemStack shopItem = plugin.getItemManager().createItem(type, 1);
            ItemMeta meta = shopItem.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add(" ");
                lore.add(ChatUtils.color("&6Price: &a" + price + " Emeralds"));
                lore.add(ChatUtils.color("&eClick to purchase!"));
                meta.setLore(lore);
                shopItem.setItemMeta(meta);
            }
            inv.setItem(slot, shopItem);
        }

        player.openInventory(inv);
        ChatUtils.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
    }

    @EventHandler
    public void onShopClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(shopTitle)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        int slot = event.getRawSlot();

        DiwaliItemType type = slotItemMap.get(slot);
        if (type == null) return;

        int price = plugin.getConfig().getInt("shop.prices." + type.getId(), 2);
        if (!player.getInventory().containsAtLeast(new ItemStack(Material.EMERALD), price)) {
            String msg = plugin.getConfigManager().getMessage("shop.not-enough-emeralds")
                    .replace("{price}", String.valueOf(price));
            player.sendMessage(msg);
            ChatUtils.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Deduct emeralds
        player.getInventory().removeItem(new ItemStack(Material.EMERALD, price));

        // Give Diwali item
        ItemStack purchasedItem = plugin.getItemManager().createItem(type, 1);
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(purchasedItem);
        if (!overflow.isEmpty()) {
            for (ItemStack left : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), left);
            }
        }

        ChatUtils.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        String msg = plugin.getConfigManager().getMessage("shop.bought")
                .replace("{item}", type.getDisplayName())
                .replace("{price}", String.valueOf(price));
        player.sendMessage(msg);
    }
}
