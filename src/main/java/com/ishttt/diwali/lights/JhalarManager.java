package com.ishttt.diwali.lights;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.items.DiwaliItemType;
import com.ishttt.diwali.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Light;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class JhalarManager {

    private final DiwaliPlugin plugin;
    private final NamespacedKey jhalarIdKey;
    private final NamespacedKey jhalarTypeKey;
    private final Map<UUID, PendingSelection> pendingSelections = new HashMap<>();
    private final Map<String, List<UUID>> wireEntities = new HashMap<>();
    private final Map<String, List<Location>> wireLightBlocks = new HashMap<>();
    private final File storageFile;
    private YamlConfiguration storage;

    public record PendingSelection(Location pointA, DiwaliItemType itemType) {}

    public JhalarManager(DiwaliPlugin plugin) {
        this.plugin = plugin;
        this.jhalarIdKey = new NamespacedKey(plugin, "jhalar_wire_id");
        this.jhalarTypeKey = new NamespacedKey(plugin, "jhalar_variant");
        this.storageFile = new File(plugin.getDataFolder(), "jhalars.yml");
        loadStoredWires();
    }

    public NamespacedKey getJhalarIdKey() {
        return jhalarIdKey;
    }

    public void handleClick(Player player, Block clickedBlock, DiwaliItemType itemType, ItemStack itemInHand) {
        Location clickedLoc = clickedBlock.getLocation().add(0.5, 0.5, 0.5);
        UUID uuid = player.getUniqueId();

        if (!pendingSelections.containsKey(uuid)) {
            // Step 1: Set Point A
            pendingSelections.put(uuid, new PendingSelection(clickedLoc, itemType));
            ChatUtils.playSound(clickedLoc, Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.0f, 1.5f);
            ChatUtils.spawnDust(clickedLoc, Color.YELLOW, 1.2f);
            String msg = plugin.getConfigManager().getMessage("jhalar.point-a-set")
                    .replace("{x}", String.valueOf(clickedBlock.getX()))
                    .replace("{y}", String.valueOf(clickedBlock.getY()))
                    .replace("{z}", String.valueOf(clickedBlock.getZ()));
            player.sendMessage(msg);
        } else {
            // Step 2: Set Point B & Spawn Wire
            PendingSelection pending = pendingSelections.remove(uuid);
            Location pointA = pending.pointA();
            Location pointB = clickedLoc;

            if (!Objects.equals(pointA.getWorld(), pointB.getWorld())) {
                player.sendMessage(plugin.getConfigManager().getMessage("jhalar.cancelled"));
                return;
            }

            double distance = pointA.distance(pointB);
            int maxDistance = plugin.getConfig().getInt("jhalar.max-span-distance", 256);

            if (maxDistance > 0 && distance > maxDistance) {
                String msg = plugin.getConfigManager().getMessage("jhalar.too-far")
                        .replace("{max}", String.valueOf(maxDistance));
                player.sendMessage(msg);
                return;
            }

            if (distance < 1.0) {
                player.sendMessage(plugin.getConfigManager().getMessage("jhalar.cancelled"));
                return;
            }

            // Consume one Jhalar item in survival
            if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                itemInHand.setAmount(itemInHand.getAmount() - 1);
            }

            String wireId = UUID.randomUUID().toString();
            spawnWire(wireId, pointA, pointB, pending.itemType());
            saveWire(wireId, pointA, pointB, pending.itemType());

            ChatUtils.playSound(pointB, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.8f);
            ChatUtils.playSound(pointB, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.2f);
            player.sendMessage(plugin.getConfigManager().getMessage("jhalar.hung-success"));
        }
    }

    public void cancelSelection(Player player) {
        if (pendingSelections.remove(player.getUniqueId()) != null) {
            player.sendMessage(plugin.getConfigManager().getMessage("jhalar.cancelled"));
        }
    }

    public boolean isPending(Player player) {
        return pendingSelections.containsKey(player.getUniqueId());
    }

    public void spawnWire(String wireId, Location a, Location b, DiwaliItemType type) {
        World world = a.getWorld();
        if (world == null) return;

        double distance = a.distance(b);
        int steps = Math.max(3, (int) Math.round(distance * 1.5)); // Bulb every ~0.66 blocks
        // Proportional catenary sag curve so the hanging drape is clearly visible at any span length
        double sagFactor = Math.max(0.7, Math.min(14.0, 0.4 + (distance * 0.12)));

        List<UUID> entityIds = new ArrayList<>();
        List<Location> lightLocs = new ArrayList<>();

        ItemStack bulbItem = plugin.getItemManager().createItem(type, 1);

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double x = a.getX() + t * (b.getX() - a.getX());
            double z = a.getZ() + t * (b.getZ() - a.getZ());
            // Parabolic catenary sag formula: 4 * sag * t * (1 - t)
            double sag = 4.0 * sagFactor * t * (1.0 - t);
            double y = a.getY() + t * (b.getY() - a.getY()) - sag;

            Location nodeLoc = new Location(world, x, y, z);

            ItemDisplay display = world.spawn(nodeLoc, ItemDisplay.class, ent -> {
                ent.setItemStack(bulbItem);
                ent.setBillboard(Display.Billboard.FIXED);
                ent.setBrightness(new Display.Brightness(15, 15));
                ent.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new AxisAngle4f(0, 0, 0, 1),
                        new Vector3f(0.55f, 0.55f, 0.55f),
                        new AxisAngle4f(0, 0, 0, 1)
                ));

                PersistentDataContainer pdc = ent.getPersistentDataContainer();
                pdc.set(jhalarIdKey, PersistentDataType.STRING, wireId);
                pdc.set(jhalarTypeKey, PersistentDataType.STRING, type.getId());
            });

            entityIds.add(display.getUniqueId());

            // Add interaction hitbox every 2 nodes so shears can snip the bulb anywhere along the wire
            if (i % 2 == 0 || i == steps) {
                world.spawn(nodeLoc, org.bukkit.entity.Interaction.class, inter -> {
                    inter.setInteractionWidth(0.75f);
                    inter.setInteractionHeight(0.6f);
                    inter.setResponsive(true);

                    PersistentDataContainer pdc = inter.getPersistentDataContainer();
                    pdc.set(jhalarIdKey, PersistentDataType.STRING, wireId);
                    pdc.set(jhalarTypeKey, PersistentDataType.STRING, type.getId());
                    entityIds.add(inter.getUniqueId());
                });
            }

            // Place real Light block every 4 nodes if air
            if (i % 4 == 0) {
                Block block = nodeLoc.getBlock();
                if (block.getType() == Material.AIR) {
                    block.setType(Material.LIGHT);
                    if (block.getBlockData() instanceof Light lightData) {
                        lightData.setLevel(15);
                        block.setBlockData(lightData, false);
                        lightLocs.add(block.getLocation());
                    }
                }
            }
        }

        wireEntities.put(wireId, entityIds);
        wireLightBlocks.put(wireId, lightLocs);
    }

    public boolean removeWireByEntity(Entity entity, Player snipper) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (!pdc.has(jhalarIdKey, PersistentDataType.STRING)) return false;

        String wireId = pdc.get(jhalarIdKey, PersistentDataType.STRING);
        String typeId = pdc.get(jhalarTypeKey, PersistentDataType.STRING);
        DiwaliItemType type = DiwaliItemType.fromId(typeId);
        if (type == null) type = DiwaliItemType.JHALAR_GOLDEN;

        removeWire(wireId);
        removeStoredWire(wireId);

        Location dropLoc = entity.getLocation();
        if (dropLoc.getWorld() != null) {
            dropLoc.getWorld().dropItemNaturally(dropLoc, plugin.getItemManager().createItem(type, 1));
            ChatUtils.playSound(dropLoc, Sound.ENTITY_SHEEP_SHEAR, 1.0f, 1.2f);
            ChatUtils.playSound(dropLoc, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0f, 1.5f);
        }

        if (snipper != null) {
            snipper.sendMessage(plugin.getConfigManager().getMessage("jhalar.snipped"));
        }
        return true;
    }

    public void removeWire(String wireId) {
        List<UUID> entities = wireEntities.remove(wireId);
        if (entities != null) {
            for (UUID id : entities) {
                Entity ent = Bukkit.getEntity(id);
                if (ent != null && ent.isValid()) {
                    ent.remove();
                }
            }
        }

        List<Location> lightLocs = wireLightBlocks.remove(wireId);
        if (lightLocs != null) {
            for (Location loc : lightLocs) {
                Block block = loc.getBlock();
                if (block.getType() == Material.LIGHT) {
                    block.setType(Material.AIR);
                }
            }
        }
    }

    private void saveWire(String wireId, Location a, Location b, DiwaliItemType type) {
        storage.set("wires." + wireId + ".world", a.getWorld().getName());
        storage.set("wires." + wireId + ".type", type.getId());
        storage.set("wires." + wireId + ".ax", a.getX());
        storage.set("wires." + wireId + ".ay", a.getY());
        storage.set("wires." + wireId + ".az", a.getZ());
        storage.set("wires." + wireId + ".bx", b.getX());
        storage.set("wires." + wireId + ".by", b.getY());
        storage.set("wires." + wireId + ".bz", b.getZ());
        try {
            storage.save(storageFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save jhalar wire: " + e.getMessage());
        }
    }

    private void removeStoredWire(String wireId) {
        storage.set("wires." + wireId, null);
        try {
            storage.save(storageFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to remove stored wire: " + e.getMessage());
        }
    }

    private void loadStoredWires() {
        if (!storageFile.exists()) {
            storage = new YamlConfiguration();
            return;
        }
        storage = YamlConfiguration.loadConfiguration(storageFile);
        if (!storage.contains("wires")) return;

        var section = storage.getConfigurationSection("wires");
        if (section == null) return;

        for (String wireId : section.getKeys(false)) {
            String worldName = section.getString(wireId + ".world");
            World world = Bukkit.getWorld(worldName != null ? worldName : "world");
            if (world == null) continue;

            DiwaliItemType type = DiwaliItemType.fromId(section.getString(wireId + ".type", "jhalar-golden"));
            if (type == null) type = DiwaliItemType.JHALAR_GOLDEN;

            Location a = new Location(world, section.getDouble(wireId + ".ax"), section.getDouble(wireId + ".ay"), section.getDouble(wireId + ".az"));
            Location b = new Location(world, section.getDouble(wireId + ".bx"), section.getDouble(wireId + ".by"), section.getDouble(wireId + ".bz"));

            spawnWire(wireId, a, b, type);
        }
    }
}
