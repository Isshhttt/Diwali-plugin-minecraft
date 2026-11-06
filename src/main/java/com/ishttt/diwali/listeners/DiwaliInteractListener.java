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
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

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

        // Prevent Diwali Lighter from placing ugly fire on blocks
        if (type == DiwaliItemType.LIGHTER) {
            event.setCancelled(true);
            // Check if clicking near an unignited cracker to ignite it directly
            for (Entity nearby : clicked.getWorld().getNearbyEntities(clicked.getLocation().add(0.5, 0.5, 0.5), 1.2, 1.2, 1.2)) {
                if (nearby.getPersistentDataContainer().has(plugin.getCrackerManager().getCrackerTypeKey(), PersistentDataType.STRING)) {
                    plugin.getCrackerManager().igniteCracker(player, nearby, held);
                    return;
                }
                if (nearby.getPersistentDataContainer().has(plugin.getDiyaManager().getDiyaIdKey(), PersistentDataType.STRING)) {
                    plugin.getDiyaManager().handleInteract(player, nearby, held);
                    return;
                }
            }
            return;
        }

        // Jhalar placement (Two-point connection)
        if (type != null && (type == DiwaliItemType.JHALAR_GOLDEN || type == DiwaliItemType.JHALAR_RGB
                || type == DiwaliItemType.JHALAR_WHITE || type == DiwaliItemType.JHALAR_SAFFRON)) {
            event.setCancelled(true);
            plugin.getJhalarManager().handleClick(player, clicked, type, held);
            return;
        }

        Block placeBlock = clicked.getRelative(event.getBlockFace());

        // Marigold Toran placement (NEVER vanilla vine!)
        if (type == DiwaliItemType.MARIGOLD_TORAN) {
            event.setCancelled(true);
            plugin.getToranManager().placeToran(player, clicked.getLocation(), event.getBlockFace());
            if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                held.setAmount(held.getAmount() - 1);
            }
            return;
        }

        // Akash Kandil placement (NEVER vanilla lantern!)
        if (type == DiwaliItemType.AKASH_KANDIL) {
            event.setCancelled(true);
            plugin.getKandilManager().placeKandil(player, placeBlock.getLocation());
            if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                held.setAmount(held.getAmount() - 1);
            }
            return;
        }

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
    public void onEntityInteractAt(PlayerInteractAtEntityEvent event) {
        handleEntityInteraction(event.getPlayer(), event.getRightClicked(), event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        handleEntityInteraction(event.getPlayer(), event.getRightClicked(), event);
    }

    private void handleEntityInteraction(Player player, Entity entity, org.bukkit.event.Cancellable event) {
        if (!(entity instanceof ItemDisplay) && !(entity instanceof Interaction)) return;

        ItemStack held = player.getInventory().getItemInMainHand();
        var pdc = entity.getPersistentDataContainer();

        // 1. Diya interaction (fuel with ghee or light with lighter)
        if (pdc.has(plugin.getDiyaManager().getDiyaIdKey(), PersistentDataType.STRING)) {
            event.setCancelled(true);
            plugin.getDiyaManager().handleInteract(player, entity, held);
            return;
        }

        // 2. Cracker interaction (ignite with lighter)
        if (pdc.has(plugin.getCrackerManager().getCrackerTypeKey(), PersistentDataType.STRING)) {
            event.setCancelled(true);
            if (plugin.getItemManager().isDiwaliItem(held, DiwaliItemType.LIGHTER) || held.getType() == Material.FLINT_AND_STEEL) {
                plugin.getCrackerManager().igniteCracker(player, entity, held);
            } else {
                player.sendMessage(ChatUtils.color("&eRight-click with your &6Diwali Lighter &eto ignite this cracker!"));
            }
            return;
        }

        // 3. Gift Box interaction (pack gifts)
        if (pdc.has(plugin.getGiftBoxManager().getGiftIdKey(), PersistentDataType.STRING)) {
            event.setCancelled(true);
            plugin.getGiftBoxManager().openPackingGUI(player, entity);
            return;
        }
    }
}
