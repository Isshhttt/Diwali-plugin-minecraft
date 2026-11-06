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
                    world.spawnParticle(Particle.SMALL_FLAME, loc.clone().add(0, 0.4, 0), 3, 0.02, 0.02, 0.02, 0.01);
                    world.spawnParticle(Particle.ELECTRIC_SPARK, loc.clone().add(0, 0.4, 0), 2, 0.03, 0.03, 0.03, 0.04);
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
        double dangerRadius = plugin.getConfig().getDouble("crackers.anar.danger-radius-blocks", 2.2);
        double damageHearts = plugin.getConfig().getDouble("crackers.anar.damage-per-second", 0.5);

        Location fountainTop = loc.clone().add(0, 0.4, 0);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!entity.isValid() || ticks >= maxTicks) {
                    world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, fountainTop, 15, 0.2, 0.2, 0.2, 0.05);
                    ChatUtils.playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
                    entity.remove();
                    cancel();
                    return;
                }

                double progress = (double) ticks / maxTicks;
                double currentHeight;
                if (progress < 0.2) {
                    currentHeight = 2.0 + (progress / 0.2) * (maxHeight - 2.0);
                } else if (progress < 0.75) {
                    currentHeight = maxHeight;
                } else {
                    currentHeight = maxHeight * (1.0 - ((progress - 0.75) / 0.25) * 0.6);
                }

                // Dense roaring fountain erupting with upward velocity
                int particleCount = 14;
                for (int i = 0; i < particleCount; i++) {
                    double angle = Math.random() * 2 * Math.PI;
                    double spread = 0.04 + (Math.random() * 0.12);
                    double vx = Math.cos(angle) * spread;
                    double vz = Math.sin(angle) * spread;
                    double vy = 0.25 + (Math.random() * 0.4) * (currentHeight / 3.0);

                    // Stage 1 (0-30%): Pure Golden Iron Filings
                    // Stage 2 (30-75%): Emerald Green & Ruby Red Fireworks
                    // Stage 3 (75-100%): White Hot Crackling Finale
                    if (progress >= 0.3 && progress < 0.75) {
                        Color col = (i % 2 == 0) ? Color.fromRGB(0, 230, 118) : Color.fromRGB(255, 23, 68);
                        double h = Math.random() * currentHeight;
                        Location pLoc = fountainTop.clone().add(vx * h * 3, h, vz * h * 3);
                        ChatUtils.spawnDust(pLoc, col, 1.3f);
                        world.spawnParticle(Particle.FIREWORK, pLoc, 1, 0.02, 0.05, 0.02, 0.02);
                    } else {
                        world.spawnParticle(Particle.ELECTRIC_SPARK, fountainTop, 0, vx, vy, vz, 0.2);
                        if (i % 3 == 0) {
                            world.spawnParticle(Particle.SCRAPE, fountainTop, 0, vx, vy, vz, 0.15);
                        }
                    }
                }

                // Popping golden ember stars
                if (ticks % 3 == 0) {
                    world.spawnParticle(Particle.LAVA, fountainTop, 2, 0.1, 0.2, 0.1, 0.05);
                    ChatUtils.playSound(loc, Sound.BLOCK_LAVA_POP, 1.0f, 1.8f);
                    ChatUtils.playSound(loc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.6f, 1.6f);
                }

                // Damage per second in danger radius
                if (ticks % 20 == 0) {
                    CrackerManager.applyDangerDamage(loc, dangerRadius, damageHearts);
                }

                ticks += 3;
            }
        }.runTaskTimer(plugin, 1L, 3L);
    }
}
