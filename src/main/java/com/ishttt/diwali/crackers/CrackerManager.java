package com.ishttt.diwali.crackers;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.items.DiwaliItemType;
import com.ishttt.diwali.utils.ChatUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.UUID;

public class CrackerManager {

    private final DiwaliPlugin plugin;
    private final NamespacedKey crackerIdKey;
    private final NamespacedKey crackerTypeKey;
    private final NamespacedKey displayUuidKey;

    public CrackerManager(DiwaliPlugin plugin) {
        this.plugin = plugin;
        this.crackerIdKey = new NamespacedKey(plugin, "cracker_id");
        this.crackerTypeKey = new NamespacedKey(plugin, "cracker_type");
        this.displayUuidKey = new NamespacedKey(plugin, "cracker_display_uuid");
    }

    public NamespacedKey getCrackerIdKey() {
        return crackerIdKey;
    }

    public NamespacedKey getCrackerTypeKey() {
        return crackerTypeKey;
    }

    public NamespacedKey getDisplayUuidKey() {
        return displayUuidKey;
    }

    public void placeCracker(Player player, Location targetLoc, DiwaliItemType type) {
        World world = targetLoc.getWorld();
        if (world == null) return;

        Location spawnLoc = targetLoc.clone().add(0.5, 0.05, 0.5);
        ItemStack item = plugin.getItemManager().createItem(type, 1);
        String id = UUID.randomUUID().toString();

        ItemDisplay display = world.spawn(spawnLoc, ItemDisplay.class, ent -> {
            ent.setItemStack(item);
            ent.setBillboard(Display.Billboard.FIXED);
            ent.setBrightness(new Display.Brightness(10, 10));
            ent.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(0, 0, 0, 1),
                    new Vector3f(0.5f, 0.5f, 0.5f),
                    new AxisAngle4f(0, 0, 0, 1)
            ));

            PersistentDataContainer pdc = ent.getPersistentDataContainer();
            pdc.set(crackerIdKey, PersistentDataType.STRING, id);
            pdc.set(crackerTypeKey, PersistentDataType.STRING, type.getId());
        });

        // Spawn physical interaction hitbox so player right-click with Lighter directly hits this entity
        world.spawn(spawnLoc.clone().add(0, 0.05, 0), Interaction.class, inter -> {
            inter.setInteractionWidth(0.55f);
            inter.setInteractionHeight(0.55f);
            inter.setResponsive(true);

            PersistentDataContainer pdc = inter.getPersistentDataContainer();
            pdc.set(crackerIdKey, PersistentDataType.STRING, id);
            pdc.set(crackerTypeKey, PersistentDataType.STRING, type.getId());
            pdc.set(displayUuidKey, PersistentDataType.STRING, display.getUniqueId().toString());
        });

        ChatUtils.playSound(spawnLoc, Sound.BLOCK_WOOD_PLACE, 1.0f, 1.2f);
        player.sendMessage(ChatUtils.color("&ePlaced &6" + type.getDisplayName() + "&e. Right-click it with your &6Diwali Lighter&e!"));
    }

    public void igniteCracker(Player player, Entity entity, ItemStack lighter) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (!pdc.has(crackerTypeKey, PersistentDataType.STRING)) return;

        String typeId = pdc.get(crackerTypeKey, PersistentDataType.STRING);
        DiwaliItemType type = DiwaliItemType.fromId(typeId);
        if (type == null) return;

        // Damage lighter durability
        damageLighter(player, lighter);

        Entity runnerEntity = entity;
        if (entity instanceof Interaction) {
            String dispUuidStr = pdc.get(displayUuidKey, PersistentDataType.STRING);
            if (dispUuidStr != null) {
                try {
                    Entity disp = entity.getWorld().getEntity(UUID.fromString(dispUuidStr));
                    if (disp != null) runnerEntity = disp;
                } catch (Exception ignored) {}
            }
            entity.remove(); // Remove hitbox so particles and movements are clear
        }

        Location loc = runnerEntity.getLocation();
        ChatUtils.playSound(loc, Sound.ITEM_FLINTANDSTEEL_USE, 1.0f, 1.2f);
        ChatUtils.playSound(loc, Sound.ENTITY_CREEPER_PRIMED, 0.7f, 1.6f);

        String msg = plugin.getConfigManager().getMessage("crackers.ignited")
                .replace("{cracker}", type.getDisplayName());
        player.sendMessage(msg);

        switch (type) {
            case CHAKRI -> ChakriRunner.run(plugin, runnerEntity);
            case ANAR -> AnarRunner.run(plugin, runnerEntity);
            case LADI_100 -> LadiRunner.run(plugin, runnerEntity, 100);
            case LADI_1000 -> LadiRunner.run(plugin, runnerEntity, 1000);
            case SUTLI_BOMB -> SutliBombRunner.run(plugin, runnerEntity);
            case SKYROCKET -> SkyrocketRunner.run(plugin, runnerEntity);
            case SKYSHOT -> SkyshotRunner.run(plugin, runnerEntity, false);
            case SKYSHOT_CAKE -> SkyshotRunner.run(plugin, runnerEntity, true);
            default -> {}
        }
    }

    public void removeCracker(Entity entity, Player player) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (!pdc.has(crackerTypeKey, PersistentDataType.STRING)) return;

        String typeId = pdc.get(crackerTypeKey, PersistentDataType.STRING);
        DiwaliItemType crackerType = DiwaliItemType.fromId(typeId);
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
        String id = pdc.get(crackerIdKey, PersistentDataType.STRING);
        if (id != null) {
            for (Entity nearby : world.getNearbyEntities(loc, 1.5, 1.5, 1.5)) {
                PersistentDataContainer npdc = nearby.getPersistentDataContainer();
                if (id.equals(npdc.get(crackerIdKey, PersistentDataType.STRING))) {
                    nearby.remove();
                }
            }
        }
        entity.remove();

        if (crackerType != null) {
            world.dropItemNaturally(loc, plugin.getItemManager().createItem(crackerType, 1));
            ChatUtils.playSound(loc, Sound.BLOCK_WOOD_BREAK, 1.0f, 1.0f);
            if (player != null) {
                player.sendMessage(ChatUtils.color("&eCollected &6" + crackerType.getDisplayName() + "&e!"));
            }
        }
    }

    public static void applyDangerDamage(Location center, double radius, double damageHearts) {
        World world = center.getWorld();
        if (world == null) return;

        double damageHp = damageHearts * 2.0;
        for (Player player : world.getPlayers()) {
            if (player.getLocation().distance(center) <= radius) {
                double newHealth = Math.max(0.0, player.getHealth() - damageHp);
                player.setHealth(newHealth);
                player.playHurtAnimation(0.0f);
                ChatUtils.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.6f, 1.2f);
            }
        }
    }

    public static void applyBurntEffect(Player player, int durationSeconds) {
        int durationTicks = durationSeconds * 20;

        ChatUtils.playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 0.5f);
        ChatUtils.playSound(player.getLocation(), Sound.BLOCK_BELL_RESONATE, 1.0f, 0.4f);
        player.sendMessage(ChatUtils.color("&8&l[&4&lBURNT&8&l] &cThe intense blast charred you! Armor offered no defense."));

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 0, false, true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 4 * 20, 0, false, true, true));

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!player.isOnline() || ticks >= durationTicks) {
                    cancel();
                    return;
                }
                Location pLoc = player.getLocation().add(0, 1.0, 0);
                player.getWorld().spawnParticle(Particle.ASH, pLoc, 6, 0.4, 0.6, 0.4, 0.05);
                player.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, pLoc, 2, 0.3, 0.5, 0.3, 0.02);
                if (ticks % 10 == 0) {
                    player.getWorld().spawnParticle(Particle.FLAME, pLoc, 2, 0.2, 0.4, 0.2, 0.01);
                }
                ticks += 5;
            }
        }.runTaskTimer(DiwaliPlugin.getInstance(), 1L, 5L);
    }

    private void damageLighter(Player player, ItemStack lighter) {
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;
        var meta = lighter.getItemMeta();
        if (meta == null) return;
        var pdc = meta.getPersistentDataContainer();
        int uses = pdc.getOrDefault(plugin.getItemManager().getLighterUsesKey(), PersistentDataType.INTEGER, 100);
        uses--;
        if (uses <= 0) {
            lighter.setAmount(lighter.getAmount() - 1);
            ChatUtils.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        } else {
            pdc.set(plugin.getItemManager().getLighterUsesKey(), PersistentDataType.INTEGER, uses);
            var lore = meta.getLore();
            if (lore != null && lore.size() > 1) {
                lore.set(1, ChatUtils.color("&6Uses: &e" + uses + "/100"));
                meta.setLore(lore);
            }
            lighter.setItemMeta(meta);
        }
    }
}
