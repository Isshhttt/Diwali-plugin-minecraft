package com.ishttt.diwali.crackers;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

public final class SkyrocketRunner {

    private SkyrocketRunner() {}

    public static void run(DiwaliPlugin plugin, Entity entity) {
        Location startLoc = entity.getLocation();
        World world = startLoc.getWorld();
        if (world == null) return;

        // Fuse 1.5s
        new BukkitRunnable() {
            int fuseTicks = 0;

            @Override
            public void run() {
                if (!entity.isValid()) {
                    cancel();
                    return;
                }

                if (fuseTicks < 30) {
                    world.spawnParticle(Particle.SMALL_FLAME, startLoc.clone().add(0, 0.2, 0), 2, 0.02, 0.02, 0.02, 0.01);
                    fuseTicks += 5;
                    return;
                }

                cancel();
                launch(plugin, entity, startLoc);
            }
        }.runTaskTimer(plugin, 1L, 5L);
    }

    private static void launch(DiwaliPlugin plugin, Entity entity, Location startLoc) {
        World world = startLoc.getWorld();
        if (world == null) return;

        double targetHeight = plugin.getConfig().getDouble("crackers.skyrocket.flight-height-blocks", 30.0);
        float whistlePitch = (float) plugin.getConfig().getDouble("crackers.skyrocket.whistle-pitch", 1.8);

        ChatUtils.playSound(startLoc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.5f, whistlePitch);
        ChatUtils.playSound(startLoc, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0f, 2.0f);

        new BukkitRunnable() {
            double currentY = startLoc.getY();
            double speed = 0.8;

            @Override
            public void run() {
                if (!entity.isValid() || currentY >= startLoc.getY() + targetHeight) {
                    // Detonation at apex
                    Location burstLoc = startLoc.clone();
                    burstLoc.setY(currentY);

                    world.spawnParticle(Particle.EXPLOSION, burstLoc, 2, 0.2, 0.2, 0.2, 0.0);
                    world.spawnParticle(Particle.FIREWORK, burstLoc, 40, 1.2, 1.2, 1.2, 0.1);
                    ChatUtils.playSound(burstLoc, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 2.0f, 1.2f);

                    entity.remove();
                    cancel();
                    return;
                }

                currentY += speed;
                speed = Math.min(2.2, speed + 0.1); // Accelerates upward

                Location currentLoc = startLoc.clone();
                currentLoc.setY(currentY);
                entity.teleport(currentLoc);

                world.spawnParticle(Particle.FLAME, currentLoc, 2, 0.05, 0.05, 0.05, 0.01);
                world.spawnParticle(Particle.SMOKE, currentLoc, 2, 0.05, 0.05, 0.05, 0.02);
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}
