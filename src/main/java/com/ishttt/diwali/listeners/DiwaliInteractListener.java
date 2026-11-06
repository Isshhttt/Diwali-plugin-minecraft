package com.ishttt.diwali.listeners;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.items.DiwaliItemType;
import com.ishttt.diwali.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class DiwaliInteractListener implements Listener {

    private final DiwaliPlugin plugin;

    public DiwaliInteractListener(DiwaliPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack held = event.getItem();
        DiwaliItemType type = plugin.getItemManager().getDiwaliType(held);

        // Cancel pending Jhalar with left click air
        if ((event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) && plugin.getJhalarManager().isPending(player)) {
            plugin.getJhalarManager().cancelSelection(player);
            event.setCancelled(true);
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        // Sky Lantern launch
        if (type == DiwaliItemType.SKY_LANTERN) {
            event.setCancelled(true);
            ItemStack offHand = player.getInventory().getItemInOffHand();
            boolean hasLighter = plugin.getItemManager().isDiwaliItem(offHand, DiwaliItemType.LIGHTER)
                    || player.getInventory().contains(Material.FLINT_AND_STEEL);

            if (!hasLighter) {
                player.sendMessage(ChatUtils.color("&cYou need a &6Diwali Lighter &eto ignite and release the Sky Lantern!"));
                return;
            }

            if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                held.setAmount(held.getAmount() - 1);
            }
            plugin.getDiyaManager().launchSkyLantern(player, player.getLocation());
            player.sendMessage(ChatUtils.color("&6✨ You released a glowing Sky Lantern into the night sky!"));
            return;
        }

        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        // Jhalar placement (Two-point connection)
        if (type != null && (type == DiwaliItemType.JHALAR_GOLDEN || type == DiwaliItemType.JHALAR_RGB
                || type == DiwaliItemType.JHALAR_WHITE || type == DiwaliItemType.JHALAR_SAFFRON)) {
            event.setCancelled(true);
            plugin.getJhalarManager().handleClick(player, clicked, type, held);
            return;
        }

        Block placeBlock = clicked.getRelative(event.getBlockFace());

        // Diya placement
        if (type == DiwaliItemType.CLAY_DIYA) {
            event.setCancelled(true);
            plugin.getDiyaManager().placeDiya(player, placeBlock.getLocation(), type);
            if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                held.setAmount(held.getAmount() - 1);
            }
            return;
        }

        // Firecrackers placement
        if (type != null && (type == DiwaliItemType.CHAKRI || type == DiwaliItemType.ANAR
                || type == DiwaliItemType.LADI_100 || type == DiwaliItemType.LADI_1000
                || type == DiwaliItemType.SUTLI_BOMB || type == DiwaliItemType.SKYROCKET
                || type == DiwaliItemType.SKYSHOT || type == DiwaliItemType.SKYSHOT_CAKE)) {
            event.setCancelled(true);
            plugin.getCrackerManager().placeCracker(player, placeBlock.getLocation(), type);
            if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                held.setAmount(held.getAmount() - 1);
            }
            return;
        }

        // Gift Box placement
        if (type == DiwaliItemType.DIWALI_GIFT_BOX) {
            event.setCancelled(true);
            plugin.getGiftBoxManager().placeGiftBox(player, placeBlock.getLocation(), held);
            return;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityInteract(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Entity entity = event.getRightClicked();
        if (!(entity instanceof ItemDisplay)) return;

        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();

        // Check if clicked entity is a Diya
        if (entity.getPersistentDataContainer().has(plugin.getDiyaManager().getDiyaIdKey(), org.bukkit.persistence.PersistentDataType.STRING)) {
            event.setCancelled(true);
            plugin.getDiyaManager().handleInteract(player, entity, held);
            return;
        }

        // Check if clicked entity is a Cracker (ignite with lighter)
        if (entity.getPersistentDataContainer().has(plugin.getCrackerManager().getCrackerTypeKey(), org.bukkit.persistence.PersistentDataType.STRING)) {
            event.setCancelled(true);
            if (plugin.getItemManager().isDiwaliItem(held, DiwaliItemType.LIGHTER) || held.getType() == Material.FLINT_AND_STEEL) {
                plugin.getCrackerManager().igniteCracker(player, entity, held);
            } else {
                player.sendMessage(ChatUtils.color("&eRight-click with your &6Diwali Lighter &eto ignite this cracker!"));
            }
            return;
        }

        // Check if clicked entity is a Gift Box (pack gifts)
        if (entity.getPersistentDataContainer().has(plugin.getGiftBoxManager().getGiftIdKey(), org.bukkit.persistence.PersistentDataType.STRING)) {
            event.setCancelled(true);
            plugin.getGiftBoxManager().openPackingGUI(player, entity);
            return;
        }
    }
}
