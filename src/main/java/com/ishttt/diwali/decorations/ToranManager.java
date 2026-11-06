package com.ishttt.diwali.decorations;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.items.DiwaliItemType;
import com.ishttt.diwali.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.UUID;

public class ToranManager {

    private final DiwaliPlugin plugin;
    private final NamespacedKey toranIdKey;
    private final NamespacedKey displayUuidKey;

    public ToranManager(DiwaliPlugin plugin) {
        this.plugin = plugin;
        this.toranIdKey = new NamespacedKey(plugin, "toran_id");
        this.displayUuidKey = new NamespacedKey(plugin, "toran_display_uuid");
    }

    public NamespacedKey getToranIdKey() {
        return toranIdKey;
    }

    public void placeToran(Player player, Location blockLoc, BlockFace face) {
        World world = blockLoc.getWorld();
        if (world == null) return;

        // Position slightly outward from the block surface
        Location placeLoc = blockLoc.clone().add(0.5, 0.5, 0.5);
        float yaw = 0f;

        switch (face) {
            case NORTH -> {
                placeLoc.add(0, 0, -0.48);
                yaw = 180f;
            }
            case SOUTH -> {
                placeLoc.add(0, 0, 0.48);
                yaw = 0f;
            }
            case WEST -> {
                placeLoc.add(-0.48, 0, 0);
                yaw = 90f;
            }
            case EAST -> {
                placeLoc.add(0.48, 0, 0);
                yaw = 270f;
            }
            case DOWN -> {
                // Hanging under a doorway beam
                placeLoc.add(0, -0.5, 0);
                yaw = player.getLocation().getYaw() + 180f;
            }
            default -> {
                placeLoc.add(0, 0.5, 0);
                yaw = player.getLocation().getYaw();
            }
        }
        placeLoc.setYaw(yaw);
        placeLoc.setPitch(0f);

        String id = UUID.randomUUID().toString();
        ItemStack item = plugin.getItemManager().createItem(DiwaliItemType.MARIGOLD_TORAN, 1);

        ItemDisplay display = world.spawn(placeLoc, ItemDisplay.class, ent -> {
            ent.setItemStack(item);
            ent.setBillboard(Display.Billboard.FIXED);
            ent.setBrightness(new Display.Brightness(12, 12));
            ent.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(0, 0, 0, 1),
                    new Vector3f(1.0f, 0.6f, 0.1f),
                    new AxisAngle4f(0, 0, 0, 1)
            ));

            PersistentDataContainer pdc = ent.getPersistentDataContainer();
            pdc.set(toranIdKey, PersistentDataType.STRING, id);
        });

        // Spawn physical interaction hitbox so players can punch/break or snip it
        world.spawn(placeLoc.clone().add(0, -0.2, 0), Interaction.class, inter -> {
            inter.setInteractionWidth(0.9f);
            inter.setInteractionHeight(0.5f);
            inter.setResponsive(true);

            PersistentDataContainer pdc = inter.getPersistentDataContainer();
            pdc.set(toranIdKey, PersistentDataType.STRING, id);
            pdc.set(displayUuidKey, PersistentDataType.STRING, display.getUniqueId().toString());
        });

        ChatUtils.playSound(placeLoc, Sound.BLOCK_GRASS_PLACE, 1.0f, 1.1f);
        player.sendMessage(ChatUtils.color("&6🌸 Placed &eMarigold Flower Toran&6! Snip with shears or punch to collect."));
    }

    public void removeToran(Entity entity, Player player) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (!pdc.has(toranIdKey, PersistentDataType.STRING)) return;

        String id = pdc.get(toranIdKey, PersistentDataType.STRING);
        World world = entity.getWorld();
        Location loc = entity.getLocation();

        // Remove linked entities
        if (pdc.has(displayUuidKey, PersistentDataType.STRING)) {
            try {
                UUID displayUuid = UUID.fromString(pdc.get(displayUuidKey, PersistentDataType.STRING));
                Entity disp = world.getEntity(displayUuid);
                if (disp != null) disp.remove();
            } catch (Exception ignored) {}
        }

        // Also clean up any lingering entities with same toranId in 2 blocks
        for (Entity nearby : world.getNearbyEntities(loc, 2, 2, 2)) {
            PersistentDataContainer npdc = nearby.getPersistentDataContainer();
            if (id.equals(npdc.get(toranIdKey, PersistentDataType.STRING))) {
                nearby.remove();
            }
        }
        entity.remove();

        // Drop item
        world.dropItemNaturally(loc, plugin.getItemManager().createItem(DiwaliItemType.MARIGOLD_TORAN, 1));
        ChatUtils.playSound(loc, Sound.BLOCK_GRASS_BREAK, 1.0f, 1.0f);
        if (player != null) {
            player.sendMessage(ChatUtils.color("&eCollected &6Marigold Flower Toran&e!"));
        }
    }
}
