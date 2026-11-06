package com.ishttt.diwali.crackers;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.utils.ChatUtils;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

public final class AnarRunner {

    private AnarRunner() {}

    public static void run(DiwaliPlugin plugin, Entity entity) {
        Location loc = entity.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        // Fuse phase: 1.5s
        new BukkitRunnable() {
            int fuseTicks = 0;

            @Override
            public void run() {
                if (!entity.isValid()) {
                    cancel();
                    return;
                }

                if (fuseTicks < 30) {
                    world.spawnParticle(Particle.SMALL_FLAME, loc.clone().add(0, 0.3, 0), 2, 0.02, 0.02, 0.02, 0.01);
                    fuseTicks += 5;
                    return;
                }

                cancel();
                startFountain(plugin, entity);
            }
        }.runTaskTimer(plugin, 1L, 5L);
    }

    private static void startFountain(DiwaliPlugin plugin, Entity entity) {
        Location loc = entity.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        int durationSeconds = plugin.getConfig().getInt("crackers.anar.fountain-duration-seconds", 14);
        int maxTicks = durationSeconds * 20;
        double maxHeight = plugin.getConfig().getDouble("crackers.anar.height-blocks", 6.5);
        double dangerRadius = plugin.getConfig().getDouble("crackers.anar.danger-radius-blocks", 2.0);
        double damageHearts = plugin.getConfig().getDouble("crackers.anar.damage-per-second", 0.5);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!entity.isValid() || ticks >= maxTicks) {
                    world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc.clone().add(0, 0.3, 0), 12, 0.2, 0.2, 0.2, 0.05);
                    ChatUtils.playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 0.8f, 1.0f);
                    entity.remove();
                    cancel();
                    return;
                }

                // Determine height curve: rises to apex then falls gently
                double progress = (double) ticks / maxTicks;
                double currentHeight;
                if (progress < 0.2) {
                    currentHeight = 2.0 + (progress / 0.2) * (maxHeight - 2.0);
                } else if (progress < 0.75) {
                    currentHeight = maxHeight;
                } else {
                    currentHeight = maxHeight * (1.0 - ((progress - 0.75) / 0.25) * 0.6);
                }

                // Fountain particles with stage colors
                int particleCount = 6;
                for (int i = 0; i < particleCount; i++) {
                    double spread = 0.15 + (Math.random() * 0.35);
                    double angle = Math.random() * 2 * Math.PI;
                    double h = Math.random() * currentHeight;
                    double px = loc.getX() + Math.cos(angle) * (spread * (h / currentHeight));
                    double pz = loc.getZ() + Math.sin(angle) * (spread * (h / currentHeight));
                    Location pLoc = new Location(world, px, loc.getY() + 0.3 + h, pz);

                    // Stage 1 (0-30%): Pure Golden
                    // Stage 2 (30-75%): Transition to Emerald Green & Ruby Red
                    // Stage 3 (75-100%): Crackling Gold & White
                    if (progress >= 0.3 && progress < 0.75) {
                        if (i % 2 == 0) {
                            ChatUtils.spawnDust(pLoc, Color.fromRGB(0, 230, 118), 1.2f); // Emerald
                        } else {
                            ChatUtils.spawnDust(pLoc, Color.fromRGB(255, 23, 68), 1.2f);  // Ruby
                        }
                    } else {
                        world.spawnParticle(Particle.FIREWORK, pLoc, 1, 0.02, 0.05, 0.02, 0.03);
                    }
                }

                // Crackle audio
                if (ticks % 3 == 0) {
                    ChatUtils.playSound(loc, Sound.BLOCK_LAVA_POP, 0.9f, 1.8f);
                    ChatUtils.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5f, 1.5f);
                }

                // True fixed damage per second in danger radius
                if (ticks % 20 == 0) {
                    CrackerManager.applyDangerDamage(loc, dangerRadius, damageHearts);
                }

                ticks += 2;
            }
        }.runTaskTimer(plugin, 1L, 2L);
    }
}
