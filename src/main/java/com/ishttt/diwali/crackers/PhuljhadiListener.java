package com.ishttt.diwali.crackers;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.items.DiwaliItemType;
import com.ishttt.diwali.utils.ChatUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class PhuljhadiListener implements Listener {

    private final DiwaliPlugin plugin;
    private final Map<UUID, IgnitionProgress> chargingPlayers = new HashMap<>();
    private final Map<UUID, Long> litBurnTimers = new HashMap<>();

    public static class IgnitionProgress {
        public int currentTicks = 0;
        public int targetTicks;

        public IgnitionProgress(int targetTicks) {
            this.targetTicks = targetTicks;
        }
    }

    public PhuljhadiListener(DiwaliPlugin plugin) {
        this.plugin = plugin;
        startLoop();
    }

    private void startLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    handlePlayer(player);
                }
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }

    private void handlePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        ItemStack held = player.getInventory().getItemInMainHand();
        DiwaliItemType type = plugin.getItemManager().getDiwaliType(held);

        // Case 1: Player is actively burning a lit Phuljhadi
        if (litBurnTimers.containsKey(uuid)) {
            long expiresAt = litBurnTimers.get(uuid);
            if (System.currentTimeMillis() >= expiresAt || type != DiwaliItemType.PHULJHADI) {
                // Extinguish & consume
                litBurnTimers.remove(uuid);
                if (type == DiwaliItemType.PHULJHADI) {
                    held.setAmount(held.getAmount() - 1);
                    player.sendMessage(plugin.getConfigManager().getMessage("crackers.phuljhadi-burnt"));
                    ChatUtils.playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.7f, 1.2f);
                    player.getWorld().spawnParticle(Particle.SMOKE, player.getLocation().add(0, 1.2, 0), 6, 0.1, 0.1, 0.1, 0.02);
                }
                return;
            }

            // Sizzle trail particles in front of player
            Location eye = player.getEyeLocation();
            Vector dir = eye.getDirection().normalize().multiply(0.6);
            Location tip = eye.add(dir).add(0, -0.2, 0);

            player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, tip, 5, 0.08, 0.08, 0.08, 0.05);
            player.getWorld().spawnParticle(Particle.SCRAPE, tip, 2, 0.05, 0.05, 0.05, 0.03);
            player.getWorld().spawnParticle(Particle.FIREWORK, tip, 2, 0.05, 0.05, 0.05, 0.02);
            ChatUtils.playSound(tip, Sound.BLOCK_LAVA_POP, 0.7f, 1.9f);
            ChatUtils.playSound(tip, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 0.5f, 2.0f);
            return;
        }

        // Case 2: Player holds unlit Phuljhadi and may be near heat source
        if (type != DiwaliItemType.PHULJHADI) {
            chargingPlayers.remove(uuid);
            return;
        }

        boolean nearFlame = isNearFlame(player.getLocation());
        if (!nearFlame) {
            chargingPlayers.remove(uuid);
            return;
        }

        // Player is near flame: charge ignition
        IgnitionProgress prog = chargingPlayers.computeIfAbsent(uuid, id -> {
            int minSec = plugin.getConfig().getInt("crackers.phuljhadi.charge-min-seconds", 15);
            int maxSec = plugin.getConfig().getInt("crackers.phuljhadi.charge-max-seconds", 20);
            int randSec = minSec + (int) (Math.random() * (maxSec - minSec + 1));
            return new IgnitionProgress(randSec * 2); // 10-tick intervals = 2 ticks per second
        });

        prog.currentTicks++;
        int percent = Math.min(100, (int) (((double) prog.currentTicks / prog.targetTicks) * 100));

        // Visual progress bar
        sendActionBar(player, percent);
        ChatUtils.playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 0.4f, 1.8f);
        player.getWorld().spawnParticle(Particle.SMALL_FLAME, player.getLocation().add(0, 1.0, 0), 1, 0.05, 0.05, 0.05, 0.01);

        if (prog.currentTicks >= prog.targetTicks) {
            // Ignition complete!
            chargingPlayers.remove(uuid);
            int burnSeconds = plugin.getConfig().getInt("crackers.phuljhadi.burn-duration-seconds", 50);
            litBurnTimers.put(uuid, System.currentTimeMillis() + (long) burnSeconds * 1000);

            ChatUtils.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.5f);
            ChatUtils.playSound(player.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 1.0f, 1.0f);
            player.sendMessage(plugin.getConfigManager().getMessage("crackers.phuljhadi-lit"));
        }
    }

    private void sendActionBar(Player player, int percent) {
        int totalBars = 20;
        int filled = (int) ((percent / 100.0) * totalBars);
        StringBuilder bar = new StringBuilder("&a");
        for (int i = 0; i < totalBars; i++) {
            if (i == filled) bar.append("&7");
            bar.append("█");
        }
        String text = ChatUtils.color("&eIgniting Phuljhadi: " + bar + " &6" + percent + "%");
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
    }

    private boolean isNearFlame(Location center) {
        World world = center.getWorld();
        if (world == null) return false;

        int radius = 3;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.clone().add(x, y, z).getBlock();
                    Material m = block.getType();
                    if (m == Material.CAMPFIRE || m == Material.SOUL_CAMPFIRE ||
                            m == Material.FIRE || m == Material.SOUL_FIRE ||
                            m == Material.TORCH || m == Material.WALL_TORCH ||
                            m == Material.CANDLE || m == Material.CANDLE_CAKE ||
                            m == Material.LAVA || m == Material.LIGHT) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
