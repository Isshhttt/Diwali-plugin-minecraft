package com.ishttt.diwali.diyas;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.items.DiwaliItemType;
import com.ishttt.diwali.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DiyaManager {

    private final DiwaliPlugin plugin;
    private final NamespacedKey diyaIdKey;
    private final NamespacedKey diyaFueledKey;
    private final NamespacedKey diyaLitKey;
    private final NamespacedKey diyaTypeKey;
    private final NamespacedKey fuelExpiryKey;

    private final Map<String, DiyaData> activeDiyas = new HashMap<>();
    private final File storageFile;
    private YamlConfiguration storage;
    private BukkitTask particleTask;
    private BukkitTask fuelCheckTask;

    public static class DiyaData {
        public String id;
        public UUID displayUuid;
        public Location location;
        public boolean isFueled;
        public boolean isLit;
        public long fuelExpiresAt;
        public DiwaliItemType type;

        public DiyaData(String id, UUID displayUuid, Location location, boolean isFueled, boolean isLit, long fuelExpiresAt, DiwaliItemType type) {
            this.id = id;
            this.displayUuid = displayUuid;
            this.location = location;
            this.isFueled = isFueled;
            this.isLit = isLit;
            this.fuelExpiresAt = fuelExpiresAt;
            this.type = type;
        }
    }

    public DiyaManager(DiwaliPlugin plugin) {
        this.plugin = plugin;
        this.diyaIdKey = new NamespacedKey(plugin, "diya_id");
        this.diyaFueledKey = new NamespacedKey(plugin, "diya_fueled");
        this.diyaLitKey = new NamespacedKey(plugin, "diya_lit");
        this.diyaTypeKey = new NamespacedKey(plugin, "diya_type");
        this.fuelExpiryKey = new NamespacedKey(plugin, "diya_fuel_expiry");
        this.storageFile = new File(plugin.getDataFolder(), "diyas.yml");

        loadStoredDiyas();
        startTasks();
    }

    public NamespacedKey getDiyaIdKey() {
        return diyaIdKey;
    }

    public void placeDiya(Player player, Location targetLoc, DiwaliItemType type) {
        World world = targetLoc.getWorld();
        if (world == null) return;

        // Position slightly above block or water surface
        Location spawnLoc = targetLoc.clone().add(0.5, 0.05, 0.5);

        String id = UUID.randomUUID().toString();
        ItemStack itemStack = plugin.getItemManager().createItem(type, 1);

        ItemDisplay display = world.spawn(spawnLoc, ItemDisplay.class, ent -> {
            ent.setItemStack(itemStack);
            ent.setBillboard(Display.Billboard.FIXED);
            ent.setBrightness(new Display.Brightness(10, 10));
            ent.setTransformation(new Transformation(
                    new Vector3f(0, 0.35f, 0),
                    new AxisAngle4f(0, 0, 0, 1),
                    new Vector3f(0.85f, 0.85f, 0.85f),
                    new AxisAngle4f(0, 0, 0, 1)
            ));

            PersistentDataContainer pdc = ent.getPersistentDataContainer();
            pdc.set(diyaIdKey, PersistentDataType.STRING, id);
            pdc.set(diyaTypeKey, PersistentDataType.STRING, type.getId());
            pdc.set(diyaFueledKey, PersistentDataType.BYTE, (byte) 0);
            pdc.set(diyaLitKey, PersistentDataType.BYTE, (byte) 0);
        });

        // Spawn physical interaction hitbox so right-click and left-click work reliably
        world.spawn(spawnLoc.clone().add(0, 0.05, 0), org.bukkit.entity.Interaction.class, inter -> {
            inter.setInteractionWidth(0.65f);
            inter.setInteractionHeight(0.5f);
            inter.setResponsive(true);

            PersistentDataContainer pdc = inter.getPersistentDataContainer();
            pdc.set(diyaIdKey, PersistentDataType.STRING, id);
            pdc.set(diyaTypeKey, PersistentDataType.STRING, type.getId());
        });

        DiyaData data = new DiyaData(id, display.getUniqueId(), spawnLoc, false, false, 0, type);
        activeDiyas.put(id, data);
        saveDiya(data);

        ChatUtils.playSound(spawnLoc, Sound.BLOCK_STONE_PLACE, 1.0f, 1.2f);
        player.sendMessage(plugin.getConfigManager().getMessage("diyas.placed"));
    }

    public void handleInteract(Player player, Entity entity, ItemStack heldItem) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (!pdc.has(diyaIdKey, PersistentDataType.STRING)) return;

        String id = pdc.get(diyaIdKey, PersistentDataType.STRING);
        DiyaData data = activeDiyas.get(id);
        if (data == null) return;

        DiwaliItemType heldType = plugin.getItemManager().getDiwaliType(heldItem);

        // Interaction 1: Adding Ghee
        if (heldType == DiwaliItemType.GHEE) {
            if (data.isFueled && (data.fuelExpiresAt - System.currentTimeMillis()) > 10 * 60 * 1000) {
                player.sendMessage(plugin.getConfigManager().getMessage("diyas.already-fueled"));
                return;
            }

            int burnMinutes = plugin.getConfig().getInt("diyas.burn-duration-minutes", 120);
            data.isFueled = true;
            data.fuelExpiresAt = System.currentTimeMillis() + (long) burnMinutes * 60 * 1000;

            if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
                heldItem.setAmount(heldItem.getAmount() - 1);
                player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
            }

            ChatUtils.playSound(data.location, Sound.ITEM_BOTTLE_EMPTY, 1.0f, 1.0f);
            ChatUtils.playSound(data.location, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 0.6f, 1.5f);
            player.sendMessage(plugin.getConfigManager().getMessage("diyas.fueled"));
            saveDiya(data);
            return;
        }

        // Interaction 2: Igniting with Lighter
        if (heldType == DiwaliItemType.LIGHTER) {
            if (!data.isFueled || System.currentTimeMillis() >= data.fuelExpiresAt) {
                player.sendMessage(plugin.getConfigManager().getMessage("diyas.needs-fuel"));
                return;
            }

            if (data.isLit) {
                return;
            }

            data.isLit = true;
            updateDiyaLight(data, true);

            // Deduct lighter durability
            damageLighter(player, heldItem);

            ChatUtils.playSound(data.location, Sound.ITEM_FLINTANDSTEEL_USE, 1.0f, 1.2f);
            ChatUtils.playSound(data.location, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.0f);
            player.sendMessage(plugin.getConfigManager().getMessage("diyas.lit"));
            saveDiya(data);
            return;
        }

        // Inform player status if right-clicking without ghee or lighter
        if (!data.isFueled) {
            player.sendMessage(plugin.getConfigManager().getMessage("diyas.needs-fuel"));
        } else if (!data.isLit) {
            player.sendMessage(ChatUtils.color("&eThis Diya has fuel! Right-click with your &6Diwali Lighter &eto ignite it."));
        }
    }

    public boolean removeDiya(Entity entity, Player player) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (!pdc.has(diyaIdKey, PersistentDataType.STRING)) return false;

        String id = pdc.get(diyaIdKey, PersistentDataType.STRING);
        DiyaData data = activeDiyas.remove(id);
        if (data != null) {
            updateDiyaLight(data, false);
            removeStoredDiya(id);
            entity.remove();

            Location dropLoc = data.location;
            if (dropLoc.getWorld() != null) {
                // Remove linked display entity if entity was the interaction hitbox
                try {
                    Entity disp = dropLoc.getWorld().getEntity(data.displayUuid);
                    if (disp != null) disp.remove();
                } catch (Exception ignored) {}
                for (Entity nearby : dropLoc.getWorld().getNearbyEntities(dropLoc, 1.5, 1.5, 1.5)) {
                    PersistentDataContainer npdc = nearby.getPersistentDataContainer();
                    if (id.equals(npdc.get(diyaIdKey, PersistentDataType.STRING))) {
                        nearby.remove();
                    }
                }
                dropLoc.getWorld().dropItemNaturally(dropLoc, plugin.getItemManager().createItem(data.type, 1));
                ChatUtils.playSound(dropLoc, Sound.BLOCK_STONE_BREAK, 1.0f, 1.2f);
            }
            return true;
        }
        return false;
    }

    public void launchSkyLantern(Player player, Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        Location spawnLoc = loc.clone().add(0, 0.5, 0);
        ItemStack lanternItem = plugin.getItemManager().createItem(DiwaliItemType.SKY_LANTERN, 1);

        ItemDisplay display = world.spawn(spawnLoc, ItemDisplay.class, ent -> {
            ent.setItemStack(lanternItem);
            ent.setBillboard(Display.Billboard.CENTER);
            ent.setBrightness(new Display.Brightness(15, 15));
            ent.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(0, 0, 0, 1),
                    new Vector3f(0.8f, 0.8f, 0.8f),
                    new AxisAngle4f(0, 0, 0, 1)
            ));
        });

        ChatUtils.playSound(spawnLoc, Sound.ITEM_FIRECHARGE_USE, 1.0f, 0.8f);
        ChatUtils.playSound(spawnLoc, Sound.BLOCK_LAVA_EXTINGUISH, 0.8f, 1.4f);

        // Smooth ascent animation task
        new BukkitRunnable() {
            int ticks = 0;
            final double driftX = (Math.random() - 0.5) * 0.03;
            final double driftZ = (Math.random() - 0.5) * 0.03;

            @Override
            public void run() {
                if (!display.isValid() || ticks > 20 * 120 || display.getLocation().getY() > 280) {
                    display.remove();
                    cancel();
                    return;
                }

                Location current = display.getLocation();
                current.add(driftX, 0.08, driftZ);
                display.teleport(current);

                if (ticks % 3 == 0) {
                    world.spawnParticle(Particle.FLAME, current.clone().add(0, -0.2, 0), 1, 0.05, 0.05, 0.05, 0.01);
                    world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, current.clone().add(0, -0.2, 0), 1, 0.05, 0.05, 0.05, 0.005);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void updateDiyaLight(DiyaData data, boolean lit) {
        Block block = data.location.getBlock();
        if (lit) {
            if (block.getType() == Material.AIR) {
                block.setType(Material.LIGHT);
                if (block.getBlockData() instanceof Light lightData) {
                    lightData.setLevel(plugin.getConfig().getInt("diyas.light-level", 15));
                    block.setBlockData(lightData, false);
                }
            }
        } else {
            if (block.getType() == Material.LIGHT) {
                block.setType(Material.AIR);
            }
        }
    }

    private void damageLighter(Player player, ItemStack lighter) {
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;
        ItemMeta meta = lighter.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        int uses = pdc.getOrDefault(plugin.getItemManager().getLighterUsesKey(), PersistentDataType.INTEGER, 100);
        uses--;
        if (uses <= 0) {
            lighter.setAmount(lighter.getAmount() - 1);
            ChatUtils.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        } else {
            pdc.set(plugin.getItemManager().getLighterUsesKey(), PersistentDataType.INTEGER, uses);
            List<String> lore = meta.getLore();
            if (lore != null && lore.size() > 1) {
                lore.set(1, ChatUtils.color("&6Uses: &e" + uses + "/100"));
                meta.setLore(lore);
            }
            lighter.setItemMeta(meta);
        }
    }

    private void startTasks() {
        // Particle & visual flame loop for lit diyas
        particleTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (DiyaData diya : activeDiyas.values()) {
                    if (diya.isLit) {
                        World world = diya.location.getWorld();
                        if (world == null || !world.isChunkLoaded(diya.location.getBlockX() >> 4, diya.location.getBlockZ() >> 4)) continue;
                        Location flameLoc = diya.location.clone().add(0, 0.4, 0);
                        world.spawnParticle(Particle.SMALL_FLAME, flameLoc, 1, 0.02, 0.02, 0.02, 0.005);
                    }
                }
            }
        }.runTaskTimer(plugin, 10L, 10L);

        // Fuel expiration check loop (every 30 seconds)
        fuelCheckTask = new BukkitRunnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                for (DiyaData diya : activeDiyas.values()) {
                    if (diya.isFueled && now >= diya.fuelExpiresAt) {
                        diya.isFueled = false;
                        diya.isLit = false;
                        updateDiyaLight(diya, false);
                        saveDiya(diya);
                        World world = diya.location.getWorld();
                        if (world != null) {
                            world.spawnParticle(Particle.SMOKE, diya.location.clone().add(0, 0.2, 0), 8, 0.1, 0.1, 0.1, 0.02);
                            ChatUtils.playSound(diya.location, Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.2f);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L * 30, 20L * 30);
    }

    private void saveDiya(DiyaData data) {
        storage.set("diyas." + data.id + ".world", data.location.getWorld().getName());
        storage.set("diyas." + data.id + ".x", data.location.getX());
        storage.set("diyas." + data.id + ".y", data.location.getY());
        storage.set("diyas." + data.id + ".z", data.location.getZ());
        storage.set("diyas." + data.id + ".uuid", data.displayUuid.toString());
        storage.set("diyas." + data.id + ".type", data.type.getId());
        storage.set("diyas." + data.id + ".fueled", data.isFueled);
        storage.set("diyas." + data.id + ".lit", data.isLit);
        storage.set("diyas." + data.id + ".fuelExpiry", data.fuelExpiresAt);
        try {
            storage.save(storageFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save diya data: " + e.getMessage());
        }
    }

    private void removeStoredDiya(String id) {
        storage.set("diyas." + id, null);
        try {
            storage.save(storageFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to remove stored diya: " + e.getMessage());
        }
    }

    private void loadStoredDiyas() {
        if (!storageFile.exists()) {
            storage = new YamlConfiguration();
            return;
        }
        storage = YamlConfiguration.loadConfiguration(storageFile);
        if (!storage.contains("diyas")) return;

        var section = storage.getConfigurationSection("diyas");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            String worldName = section.getString(id + ".world");
            World world = Bukkit.getWorld(worldName != null ? worldName : "world");
            if (world == null) continue;

            Location loc = new Location(world, section.getDouble(id + ".x"), section.getDouble(id + ".y"), section.getDouble(id + ".z"));
            UUID uuid = UUID.fromString(section.getString(id + ".uuid", UUID.randomUUID().toString()));
            DiwaliItemType type = DiwaliItemType.fromId(section.getString(id + ".type", "clay-diya"));
            if (type == null) type = DiwaliItemType.CLAY_DIYA;

            boolean fueled = section.getBoolean(id + ".fueled");
            boolean lit = section.getBoolean(id + ".lit");
            long expiry = section.getLong(id + ".fuelExpiry");

            DiyaData data = new DiyaData(id, uuid, loc, fueled, lit, expiry, type);
            activeDiyas.put(id, data);
            if (lit) {
                updateDiyaLight(data, true);
            }
        }
    }

    public void cleanup() {
        if (particleTask != null) particleTask.cancel();
        if (fuelCheckTask != null) fuelCheckTask.cancel();
    }
}
