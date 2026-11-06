package com.ishttt.diwali.listeners;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.items.DiwaliItemType;
import com.ishttt.diwali.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class DiwaliBreakListener implements Listener {

    private final DiwaliPlugin plugin;

    public DiwaliBreakListener(DiwaliPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        Entity target = event.getEntity();
        if (!(target instanceof ItemDisplay) && !(target instanceof Interaction)) return;

        PersistentDataContainer pdc = target.getPersistentDataContainer();
        ItemStack held = player.getInventory().getItemInMainHand();

        // 1. Jhalar Wire Bulb
        if (pdc.has(plugin.getJhalarManager().getJhalarIdKey(), PersistentDataType.STRING)) {
            event.setCancelled(true);
            if (held.getType() == Material.SHEARS) {
                plugin.getJhalarManager().removeWireByEntity(target, player);
            } else {
                player.sendMessage(ChatUtils.color("&e✂ Use &6Shears &eto snip and collect the Jhalar light wire!"));
            }
            return;
        }

        // 2. Diya
        if (pdc.has(plugin.getDiyaManager().getDiyaIdKey(), PersistentDataType.STRING)) {
            event.setCancelled(true);
            plugin.getDiyaManager().removeDiya(target, player);
            return;
        }

        // 3. Toran (Marigold Garland)
        if (pdc.has(plugin.getToranManager().getToranIdKey(), PersistentDataType.STRING)) {
            event.setCancelled(true);
            plugin.getToranManager().removeToran(target, player);
            return;
        }

        // 4. Akash Kandil (Paper Lantern)
        if (pdc.has(plugin.getKandilManager().getKandilIdKey(), PersistentDataType.STRING)) {
            event.setCancelled(true);
            plugin.getKandilManager().removeKandil(target, player);
            return;
        }

        // 5. Firecrackers / Bombs (Left-click to break & pick up)
        if (pdc.has(plugin.getCrackerManager().getCrackerTypeKey(), PersistentDataType.STRING)) {
            event.setCancelled(true);
            plugin.getCrackerManager().removeCracker(target, player);
            return;
        }

        // 6. Gift Box
        if (pdc.has(plugin.getGiftBoxManager().getGiftIdKey(), PersistentDataType.STRING)) {
            event.setCancelled(true);

            // Sneaking breaks to collect
            if (player.isSneaking()) {
                plugin.getGiftBoxManager().handleBreak(target, player);
                return;
            }

            // Left-click unwraps the gift!
            boolean unwrapped = plugin.getGiftBoxManager().handleUnwrap(target, player);
            if (!unwrapped) {
                plugin.getGiftBoxManager().handleBreak(target, player);
            }
            return;
        }
    }
}
