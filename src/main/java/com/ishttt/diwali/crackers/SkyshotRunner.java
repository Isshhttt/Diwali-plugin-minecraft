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

public final class SkyshotRunner {

    private static final Color[][] COLOR_PALETTES = {
            // Golden Willow & Saffron
            {Color.fromRGB(255, 215, 0), Color.fromRGB(255, 179, 0), Color.fromRGB(255, 248, 220)},
            // Emerald Green & Lime
            {Color.fromRGB(0, 230, 118), Color.fromRGB(76, 175, 80), Color.fromRGB(185, 246, 202)},
            // Ruby Crimson & Rose
            {Color.fromRGB(255, 23, 68), Color.fromRGB(245, 0, 87), Color.fromRGB(255, 128, 171)},
            // Royal Festive (Purple, Gold, Electric Cyan)
            {Color.fromRGB(170, 0, 255), Color.fromRGB(255, 215, 0), Color.fromRGB(0, 229, 255)}
    };

    private SkyshotRunner() {}

    public static void run(DiwaliPlugin plugin, Entity entity, boolean isMultiCake) {
        Location baseLoc = entity.getLocation();
        World world = baseLoc.getWorld();
        if (world == null) return;

        // Fuse phase: 2.0s
        new BukkitRunnable() {
            int fuseTicks = 0;

            @Override
            public void run() {
                if (!entity.isValid()) {
                    cancel();
                    return;
                }

                if (fuseTicks < 40) {
                    world.spawnParticle(Particle.SMALL_FLAME, baseLoc.clone().add(0, 0.4, 0), 3, 0.05, 0.05, 0.05, 0.02);
                    world.spawnParticle(Particle.ELECTRIC_SPARK, baseLoc.clone().add(0, 0.4, 0), 2, 0.05, 0.05, 0.05, 0.03);
                    fuseTicks += 5;
                    return;
                }

                cancel();
                if (isMultiCake) {
                    launchCake(plugin, entity, baseLoc);
                } else {
                    launchSingle(plugin, entity, baseLoc, 0);
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
        }.runTaskTimer(plugin, 1L, 20L);
    }

    private static void launchSingle(DiwaliPlugin plugin, Entity entity, Location baseLoc, int paletteIndex) {
        World world = baseLoc.getWorld();
        if (world == null) return;

        double targetAltitude = plugin.getConfig().getDouble("crackers.skyshot.burst-altitude-blocks", 36.0);

        // Mortar launch thump
        ChatUtils.playSound(baseLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 1.8f);
        ChatUtils.playSound(baseLoc, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 2.0f, 0.8f);
        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, baseLoc.clone().add(0, 0.5, 0), 15, 0.2, 0.2, 0.2, 0.06);
        world.spawnParticle(Particle.FLAME, baseLoc.clone().add(0, 0.5, 0), 8, 0.1, 0.3, 0.1, 0.05);

        // Comet flight upward
        new BukkitRunnable() {
            double currentY = baseLoc.getY() + 0.5;
            final double driftX = (Math.random() - 0.5) * 0.15;
            final double driftZ = (Math.random() - 0.5) * 0.15;
            double posX = baseLoc.getX();
            double posZ = baseLoc.getZ();

            @Override
            public void run() {
                if (currentY >= baseLoc.getY() + targetAltitude) {
                    Location burstLoc = new Location(world, posX, currentY, posZ);
                    detonateFireworkBurst(world, burstLoc, paletteIndex);
                    cancel();
                    return;
                }

                currentY += 1.6;
                posX += driftX;
                posZ += driftZ;

                Location cometLoc = new Location(world, posX, currentY, posZ);
                world.spawnParticle(Particle.FLAME, cometLoc, 4, 0.05, 0.05, 0.05, 0.02);
                world.spawnParticle(Particle.ELECTRIC_SPARK, cometLoc, 2, 0.04, 0.04, 0.04, 0.04);
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private static void detonateFireworkBurst(World world, Location center, int paletteIndex) {
        Color[] palette = COLOR_PALETTES[paletteIndex % COLOR_PALETTES.length];

        // Flash of light across the night sky
        world.spawnParticle(Particle.FLASH, center, 1, 0, 0, 0, 0);

        // Spawn real Minecraft Firework entity and detonate immediately
        Firework fw = world.spawn(center, Firework.class, f -> {
            FireworkMeta meta = f.getFireworkMeta();
            meta.setPower(1);

            FireworkEffect.Type type = (paletteIndex % 2 == 0)
                    ? FireworkEffect.Type.BALL_LARGE
                    : FireworkEffect.Type.STAR;

            FireworkEffect effect1 = FireworkEffect.builder()
                    .with(type)
                    .withColor(palette[0], palette[1])
                    .withFade(palette[2], Color.WHITE)
                    .flicker(true)
                    .trail(true)
                    .build();

            FireworkEffect effect2 = FireworkEffect.builder()
                    .with(FireworkEffect.Type.BURST)
                    .withColor(Color.fromRGB(255, 215, 0))
                    .withFade(Color.fromRGB(255, 255, 255))
                    .flicker(true)
                    .trail(true)
                    .build();

            meta.addEffects(effect1, effect2);
            f.setFireworkMeta(meta);
        });

        fw.detonate();

        // Audio & companion electric spark ring
        ChatUtils.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 3.5f, 0.9f);
        ChatUtils.playSound(center, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 3.0f, 1.2f);
        ChatUtils.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 1.1f);

        for (int i = 0; i < 35; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double speed = 0.3 + (Math.random() * 0.7);
            double vx = Math.cos(angle) * speed;
            double vz = Math.sin(angle) * speed;
            double vy = (Math.random() - 0.5) * speed;
            world.spawnParticle(Particle.ELECTRIC_SPARK, center, 0, vx, vy, vz, 0.15);
        }
    }
}
