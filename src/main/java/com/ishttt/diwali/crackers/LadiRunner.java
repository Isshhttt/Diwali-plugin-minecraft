package com.ishttt.diwali.crackers;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

public final class LadiRunner {

    private LadiRunner() {}

    public static void run(DiwaliPlugin plugin, Entity entity, int size) {
        Location baseLoc = entity.getLocation();
        World world = baseLoc.getWorld();
        if (world == null) return;

        boolean isMega = size >= 1000;
        int durationSeconds = isMega ?
                plugin.getConfig().getInt("crackers.ladi-1000.duration-seconds", 13) :
                plugin.getConfig().getInt("crackers.ladi-100.duration-seconds", 6);

        int maxTicks = durationSeconds * 20;
        double dangerRadius = isMega ?
                plugin.getConfig().getDouble("crackers.ladi-1000.danger-radius-blocks", 2.5) :
                plugin.getConfig().getDouble("crackers.ladi-100.danger-radius-blocks", 2.0);
        double damageHearts = 0.5;

        // Fuse sizzle (1.5s)
        new BukkitRunnable() {
            int fuseTicks = 0;

            @Override
            public void run() {
                if (!entity.isValid()) {
                    cancel();
                    return;
                }

                if (fuseTicks < 30) {
                    world.spawnParticle(Particle.SMALL_FLAME, baseLoc.clone().add(0, 0.1, 0), 2, 0.05, 0.05, 0.05, 0.01);
                    fuseTicks += 5;
                    return;
                }

                cancel();
                startPops(plugin, entity, baseLoc, maxTicks, dangerRadius, damageHearts, isMega);
            }
        }.runTaskTimer(plugin, 1L, 5L);
    }

    private static void startPops(DiwaliPlugin plugin, Entity entity, Location baseLoc,
                                  int maxTicks, double dangerRadius, double damageHearts, boolean isMega) {
        World world = baseLoc.getWorld();
        if (world == null) return;

        double trailLength = isMega ? 4.0 : 1.5;

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!entity.isValid() || ticks >= maxTicks) {
                    // Grand finale double bang!
                    world.spawnParticle(Particle.EXPLOSION, baseLoc, 3, 0.5, 0.2, 0.5, 0.0);
                    world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, baseLoc, 20, 1.0, 0.5, 1.0, 0.05);
                    ChatUtils.playSound(baseLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 1.4f);
                    ChatUtils.playSound(baseLoc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.2f, 1.8f);

                    entity.remove();
                    cancel();
                    return;
                }

                // Rapid machine-gun pops along the trail
                int bursts = isMega ? 2 : 1;
                for (int b = 0; b < bursts; b++) {
                    double offsetProgress = ((double) ticks / maxTicks) * trailLength;
                    double jitter = (Math.random() - 0.5) * 0.4;
                    Location popLoc = baseLoc.clone().add(offsetProgress + jitter, 0.08, jitter);

                    world.spawnParticle(Particle.ELECTRIC_SPARK, popLoc, 3, 0.05, 0.05, 0.05, 0.04);
                    world.spawnParticle(Particle.SCRAPE, popLoc, 1, 0.02, 0.02, 0.02, 0.02);
                    world.spawnParticle(Particle.SMOKE, popLoc, 4, 0.1, 0.1, 0.1, 0.02);
                    if (ticks % 4 == 0) {
                        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, popLoc, 1, 0.05, 0.1, 0.05, 0.01);
                    }

                    float pitch = 1.6f + (float) (Math.random() * 0.4);
                    ChatUtils.playSound(popLoc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.8f, pitch);
                }

                // Danger damage
                if (ticks % 20 == 0) {
                    CrackerManager.applyDangerDamage(baseLoc, dangerRadius, damageHearts);
                }

                ticks += 3;
            }
        }.runTaskTimer(plugin, 1L, 3L);
    }
}
