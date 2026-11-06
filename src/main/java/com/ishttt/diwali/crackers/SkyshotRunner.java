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

public final class SkyshotRunner {

    private static final Color[][] COLOR_PALETTES = {
            // Golden Willow
            {Color.fromRGB(255, 215, 0), Color.fromRGB(255, 248, 220), Color.fromRGB(255, 165, 0)},
            // Emerald Rain
            {Color.fromRGB(0, 230, 118), Color.fromRGB(76, 175, 80), Color.fromRGB(185, 246, 202)},
            // Ruby Crimson
            {Color.fromRGB(255, 23, 68), Color.fromRGB(245, 0, 87), Color.fromRGB(255, 128, 171)},
            // Royal Festive (Purple, Gold, Cyan)
            {Color.fromRGB(170, 0, 255), Color.fromRGB(255, 215, 0), Color.fromRGB(0, 229, 255)}
    };

    private SkyshotRunner() {}

    public static void run(DiwaliPlugin plugin, Entity entity, boolean isMultiCake) {
        Location baseLoc = entity.getLocation();
        World world = baseLoc.getWorld();
        if (world == null) return;

        // Fuse 2.0s
        new BukkitRunnable() {
            int fuseTicks = 0;

            @Override
            public void run() {
                if (!entity.isValid()) {
                    cancel();
                    return;
                }

                if (fuseTicks < 40) {
                    world.spawnParticle(Particle.SMALL_FLAME, baseLoc.clone().add(0, 0.3, 0), 2, 0.05, 0.05, 0.05, 0.01);
                    fuseTicks += 5;
                    return;
                }

                cancel();
                if (isMultiCake) {
                    launchCake(plugin, entity, baseLoc);
                } else {
                    launchSingle(plugin, entity, baseLoc, 0);
                    // Remove mortar box after single shot
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            entity.remove();
                        }
                    }.runTaskLater(plugin, 40L);
                }
            }
        }.runTaskTimer(plugin, 1L, 5L);
    }

    private static void launchCake(DiwaliPlugin plugin, Entity entity, Location baseLoc) {
        int totalShots = 12;

        new BukkitRunnable() {
            int shot = 0;

            @Override
            public void run() {
                if (!entity.isValid() || shot >= totalShots) {
                    entity.remove();
                    cancel();
                    return;
                }

                launchSingle(plugin, entity, baseLoc, shot);
                shot++;
            }
        }.runTaskTimer(plugin, 1L, 22L);
    }

    private static void launchSingle(DiwaliPlugin plugin, Entity entity, Location baseLoc, int paletteIndex) {
        World world = baseLoc.getWorld();
        if (world == null) return;

        double targetAltitude = plugin.getConfig().getDouble("crackers.skyshot.burst-altitude-blocks", 38.0);
        double burstRadius = plugin.getConfig().getDouble("crackers.skyshot.burst-radius", 6.0);

        // Heavy mortar thump audio
        ChatUtils.playSound(baseLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.8f);
        ChatUtils.playSound(baseLoc, Sound.ITEM_FIRECHARGE_USE, 1.5f, 0.7f);
        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, baseLoc.clone().add(0, 0.4, 0), 10, 0.2, 0.2, 0.2, 0.05);

        // Rocket comet flight
        new BukkitRunnable() {
            double currentY = baseLoc.getY() + 0.5;
            final double driftX = (Math.random() - 0.5) * 0.12;
            final double driftZ = (Math.random() - 0.5) * 0.12;
            double posX = baseLoc.getX();
            double posZ = baseLoc.getZ();

            @Override
            public void run() {
                if (currentY >= baseLoc.getY() + targetAltitude) {
                    // Huge Aerial Starburst!
                    Location burstLoc = new Location(world, posX, currentY, posZ);
                    burstCanopy(world, burstLoc, burstRadius, paletteIndex);
                    cancel();
                    return;
                }

                currentY += 1.4;
                posX += driftX;
                posZ += driftZ;

                Location cometLoc = new Location(world, posX, currentY, posZ);
                world.spawnParticle(Particle.FLAME, cometLoc, 3, 0.05, 0.05, 0.05, 0.01);
                world.spawnParticle(Particle.FIREWORK, cometLoc, 1, 0.02, 0.02, 0.02, 0.01);
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private static void burstCanopy(World world, Location center, double radius, int paletteIndex) {
        Color[] palette = COLOR_PALETTES[paletteIndex % COLOR_PALETTES.length];

        ChatUtils.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 3.0f, 0.9f);
        ChatUtils.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 2.5f, 1.2f);
        ChatUtils.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.2f);

        world.spawnParticle(Particle.EXPLOSION, center, 2, 0.5, 0.5, 0.5, 0.0);

        // Spherical starburst canopy
        int points = 80;
        for (int i = 0; i < points; i++) {
            double u = Math.random();
            double v = Math.random();
            double theta = u * 2.0 * Math.PI;
            double phi = Math.acos(2.0 * v - 1.0);
            double r = Math.cbrt(Math.random()) * radius;

            double sinPhi = Math.sin(phi);
            double x = center.getX() + r * sinPhi * Math.cos(theta);
            double y = center.getY() + r * Math.cos(phi);
            double z = center.getZ() + r * sinPhi * Math.sin(theta);

            Location starLoc = new Location(world, x, y, z);
            Color starColor = palette[i % palette.length];
            ChatUtils.spawnDust(starLoc, starColor, 1.4f);

            if (i % 3 == 0) {
                world.spawnParticle(Particle.FIREWORK, starLoc, 1, 0.02, 0.02, 0.02, 0.02);
            }
        }
    }
}
