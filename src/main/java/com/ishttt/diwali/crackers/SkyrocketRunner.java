package com.ishttt.diwali.crackers;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.utils.ChatUtils;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

public final class SkyrocketRunner {

    private SkyrocketRunner() {}

    public static void run(DiwaliPlugin plugin, Entity entity) {
        Location startLoc = entity.getLocation();
        World world = startLoc.getWorld();
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
                    world.spawnParticle(Particle.SMALL_FLAME, startLoc.clone().add(0, 0.3, 0), 3, 0.02, 0.02, 0.02, 0.01);
                    world.spawnParticle(Particle.ELECTRIC_SPARK, startLoc.clone().add(0, 0.3, 0), 2, 0.02, 0.02, 0.02, 0.03);
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

        ChatUtils.playSound(startLoc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 2.0f, whistlePitch);
        ChatUtils.playSound(startLoc, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.5f, 2.0f);

        new BukkitRunnable() {
            double currentY = startLoc.getY();
            double speed = 0.9;

            @Override
            public void run() {
                if (!entity.isValid() || currentY >= startLoc.getY() + targetHeight) {
                    Location burstLoc = startLoc.clone();
                    burstLoc.setY(currentY);

                    // Real Firework entity detonation
                    world.spawnParticle(Particle.FLASH, burstLoc, 1, 0, 0, 0, 0);
                    Firework fw = world.spawn(burstLoc, Firework.class, f -> {
                        FireworkMeta meta = f.getFireworkMeta();
                        meta.addEffect(FireworkEffect.builder()
                                .with(FireworkEffect.Type.BALL_LARGE)
                                .withColor(Color.fromRGB(255, 215, 0), Color.fromRGB(255, 69, 0))
                                .withFade(Color.WHITE, Color.YELLOW)
                                .flicker(true)
                                .trail(true)
                                .build());
                        f.setFireworkMeta(meta);
                    });
                    fw.detonate();

                    ChatUtils.playSound(burstLoc, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 3.0f, 1.1f);
                    entity.remove();
                    cancel();
                    return;
                }

                currentY += speed;
                speed = Math.min(2.4, speed + 0.12);

                Location currentLoc = startLoc.clone();
                currentLoc.setY(currentY);
                entity.teleport(currentLoc);

                world.spawnParticle(Particle.FLAME, currentLoc, 4, 0.05, 0.05, 0.05, 0.02);
                world.spawnParticle(Particle.ELECTRIC_SPARK, currentLoc, 3, 0.05, 0.05, 0.05, 0.04);
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}
