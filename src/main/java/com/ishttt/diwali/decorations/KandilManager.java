package com.ishttt.diwali.decorations;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.items.DiwaliItemType;
import com.ishttt.diwali.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Light;
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

public class KandilManager {

    private final DiwaliPlugin plugin;
    private final NamespacedKey kandilIdKey;
    private final NamespacedKey displayUuidKey;

    public KandilManager(DiwaliPlugin plugin) {
        this.plugin = plugin;
        this.kandilIdKey = new NamespacedKey(plugin, "kandil_id");
        this.displayUuidKey = new NamespacedKey(plugin, "kandil_display_uuid");
    }

    public NamespacedKey getKandilIdKey() {
        return kandilIdKey;
    }

    public void placeKandil(Player player, Location targetLoc) {
        World world = targetLoc.getWorld();
        if (world == null) return;

        Location spawnLoc = targetLoc.clone().add(0.5, 0.1, 0.5);
        String id = UUID.randomUUID().toString();
        ItemStack item = plugin.getItemManager().createItem(DiwaliItemType.AKASH_KANDIL, 1);

        ItemDisplay display = world.spawn(spawnLoc, ItemDisplay.class, ent -> {
            ent.setItemStack(item);
            ent.setBillboard(Display.Billboard.FIXED);
            ent.setBrightness(new Display.Brightness(15, 15));
            ent.setTransformation(new Transformation(
                    new Vector3f(0, 0.45f, 0),
                    new AxisAngle4f(0, 0, 0, 1),
                    new Vector3f(1.25f, 1.25f, 1.25f),
                    new AxisAngle4f(0, 0, 0, 1)
            ));

            PersistentDataContainer pdc = ent.getPersistentDataContainer();
            pdc.set(kandilIdKey, PersistentDataType.STRING, id);
        });

        // Spawn physical interaction hitbox
        world.spawn(spawnLoc.clone().add(0, 0.1, 0), Interaction.class, inter -> {
            inter.setInteractionWidth(0.85f);
            inter.setInteractionHeight(1.4f);
            inter.setResponsive(true);

            PersistentDataContainer pdc = inter.getPersistentDataContainer();
            pdc.set(kandilIdKey, PersistentDataType.STRING, id);
            pdc.set(displayUuidKey, PersistentDataType.STRING, display.getUniqueId().toString());
        });

        // Place block light level 15
        Block b = targetLoc.getBlock();
        if (b.getType() == Material.AIR || b.getType() == Material.CAVE_AIR) {
            b.setType(Material.LIGHT);
            if (b.getBlockData() instanceof Light lightData) {
                lightData.setLevel(15);
                b.setBlockData(lightData);
            }
        }

        ChatUtils.playSound(spawnLoc, Sound.BLOCK_LANTERN_PLACE, 1.0f, 1.1f);
        player.sendMessage(ChatUtils.color("&6🏮 Placed &eAkash Kandil&6! Casting warm festival illumination."));
    }

    public void removeKandil(Entity entity, Player player) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (!pdc.has(kandilIdKey, PersistentDataType.STRING)) return;

        String id = pdc.get(kandilIdKey, PersistentDataType.STRING);
        World world = entity.getWorld();
        Location loc = entity.getLocation();

        // Remove linked display
        if (pdc.has(displayUuidKey, PersistentDataType.STRING)) {
            try {
                UUID displayUuid = UUID.fromString(pdc.get(displayUuidKey, PersistentDataType.STRING));
                Entity disp = world.getEntity(displayUuid);
                if (disp != null) disp.remove();
            } catch (Exception ignored) {}
        }

        // Clean up entities in radius
        for (Entity nearby : world.getNearbyEntities(loc, 2, 2, 2)) {
            PersistentDataContainer npdc = nearby.getPersistentDataContainer();
            if (id.equals(npdc.get(kandilIdKey, PersistentDataType.STRING))) {
                nearby.remove();
            }
        }
        entity.remove();

        // Clear light block if present
        Block b = loc.getBlock();
        if (b.getType() == Material.LIGHT) {
            b.setType(Material.AIR);
        }

        // Drop item
        world.dropItemNaturally(loc, plugin.getItemManager().createItem(DiwaliItemType.AKASH_KANDIL, 1));
        ChatUtils.playSound(loc, Sound.BLOCK_LANTERN_BREAK, 1.0f, 1.0f);
        if (player != null) {
            player.sendMessage(ChatUtils.color("&eCollected &6Akash Kandil&e!"));
        }
    }
}
