package com.ishttt.diwali.cleanup;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class PurgeManager {

    private final DiwaliPlugin plugin;

    public PurgeManager(DiwaliPlugin plugin) {
        this.plugin = plugin;
    }

    public static class PurgeResult {
        public int entitiesRemoved = 0;
        public int lightBlocksRemoved = 0;
        public int itemsRemoved = 0;
        public int playersCleaned = 0;
    }

    public PurgeResult purgeAll() {
        PurgeResult result = new PurgeResult();

        // 1. Clear stored and active Jhalar wires
        result.entitiesRemoved += plugin.getJhalarManager().clearAllWires();

        // 2. Clear stored and active Diyas
        result.entitiesRemoved += plugin.getDiyaManager().clearAllDiyas();

        // 3. World entity & Light block sweep across all worlds
        List<NamespacedKey> diwaliKeys = Arrays.asList(
                plugin.getJhalarManager().getJhalarIdKey(),
                new NamespacedKey(plugin, "jhalar_variant"),
                plugin.getDiyaManager().getDiyaIdKey(),
                new NamespacedKey(plugin, "diya_type"),
                new NamespacedKey(plugin, "diya_fueled"),
                new NamespacedKey(plugin, "diya_lit"),
                plugin.getCrackerManager().getCrackerIdKey(),
                plugin.getCrackerManager().getCrackerTypeKey(),
                plugin.getCrackerManager().getDisplayUuidKey(),
                plugin.getToranManager().getToranIdKey(),
                new NamespacedKey(plugin, "toran_display_uuid"),
                plugin.getKandilManager().getKandilIdKey(),
                new NamespacedKey(plugin, "kandil_display_uuid"),
                plugin.getGiftBoxManager().getGiftIdKey(),
                new NamespacedKey(plugin, "gift_display_uuid")
        );

        for (World world : Bukkit.getWorlds()) {
            for (Entity ent : world.getEntities()) {
                // Remove dropped Diwali items on ground
                if (ent instanceof Item droppedItem) {
                    if (plugin.getItemManager().isAnyDiwaliItem(droppedItem.getItemStack())) {
                        result.itemsRemoved += droppedItem.getItemStack().getAmount();
                        result.entitiesRemoved++;
                        droppedItem.remove();
                        continue;
                    }
                }

                PersistentDataContainer pdc = ent.getPersistentDataContainer();
                boolean isDiwali = false;
                for (NamespacedKey key : pdc.getKeys()) {
                    if (key.getNamespace().equalsIgnoreCase("diwali") || key.getNamespace().equalsIgnoreCase(plugin.getName())) {
                        isDiwali = true;
                        break;
                    }
                }
                if (!isDiwali) {
                    for (NamespacedKey key : diwaliKeys) {
                        if (pdc.has(key, PersistentDataType.STRING) || pdc.has(key, PersistentDataType.BYTE)) {
                            isDiwali = true;
                            break;
                        }
                    }
                }

                if (isDiwali) {
                    Block b = ent.getLocation().getBlock();
                    if (b.getType() == Material.LIGHT) {
                        b.setType(Material.AIR, false);
                        result.lightBlocksRemoved++;
                    }
                    ent.remove();
                    result.entitiesRemoved++;
                }
            }
        }

        // 4. Inventory sweep across all online players (main inventory, armor, offhand, ender chest, cursor, open inventory)
        for (Player player : Bukkit.getOnlinePlayers()) {
            boolean playerCleaned = false;

            // Main inventory + armor + offhand
            ItemStack[] contents = player.getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                if (plugin.getItemManager().isAnyDiwaliItem(contents[i])) {
                    result.itemsRemoved += contents[i].getAmount();
                    player.getInventory().setItem(i, null);
                    playerCleaned = true;
                }
            }

            // Ender chest
            ItemStack[] enderContents = player.getEnderChest().getContents();
            for (int i = 0; i < enderContents.length; i++) {
                if (plugin.getItemManager().isAnyDiwaliItem(enderContents[i])) {
                    result.itemsRemoved += enderContents[i].getAmount();
                    player.getEnderChest().setItem(i, null);
                    playerCleaned = true;
                }
            }

            // Item held on cursor
            ItemStack cursor = player.getItemOnCursor();
            if (plugin.getItemManager().isAnyDiwaliItem(cursor)) {
                result.itemsRemoved += cursor.getAmount();
                player.setItemOnCursor(null);
                playerCleaned = true;
            }

            // Top inventory if opened
            if (player.getOpenInventory() != null) {
                Inventory topInv = player.getOpenInventory().getTopInventory();
                if (topInv != null) {
                    ItemStack[] topContents = topInv.getContents();
                    for (int i = 0; i < topContents.length; i++) {
                        if (plugin.getItemManager().isAnyDiwaliItem(topContents[i])) {
                            result.itemsRemoved += topContents[i].getAmount();
                            topInv.setItem(i, null);
                            playerCleaned = true;
                        }
                    }
                }
            }

            if (playerCleaned) {
                result.playersCleaned++;
                player.sendMessage(ChatUtils.color("&6[Diwali] &eFestival season has ended. All Diwali items have been cleared."));
            }
        }

        return result;
    }

    public void executePurge(CommandSender sender) {
        sender.sendMessage(ChatUtils.color("&6&l[Diwali Purge] &eStarting complete festival cleanup across all worlds..."));
        PurgeResult res = purgeAll();

        sender.sendMessage(ChatUtils.color("&a&l✔ Diwali Festival Cleanup Complete!"));
        sender.sendMessage(ChatUtils.color("&7- &eEntities removed: &f" + res.entitiesRemoved + " &7(Diyas, Jhalars, Crackers, Torans, Lanterns)"));
        sender.sendMessage(ChatUtils.color("&7- &eLight blocks removed: &f" + res.lightBlocksRemoved));
        sender.sendMessage(ChatUtils.color("&7- &eFestival items cleared: &f" + res.itemsRemoved + " &7from &f" + res.playersCleaned + " &7player(s)"));
        sender.sendMessage(ChatUtils.color("&aYou can now safely uninstall or disable the plugin!"));
    }
}
