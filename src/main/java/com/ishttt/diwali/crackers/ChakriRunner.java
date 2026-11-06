package com.ishttt.diwali.crackers;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

public final class ChakriRunner {

    private ChakriRunner() {}

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
                    world.spawnParticle(Particle.SMALL_FLAME, loc.clone().add(0, 0.15, 0), 2, 0.02, 0.02, 0.02, 0.01);
                    world.spawnParticle(Particle.ELECTRIC_SPARK, loc.clone().add(0, 0.15, 0), 2, 0.02, 0.02, 0.02, 0.03);
                    fuseTicks += 5;
                    return;
                }

                cancel();
                startSpinning(plugin, entity);
            }
        }.runTaskTimer(plugin, 1L, 5L);
    }

    private static void startSpinning(DiwaliPlugin plugin, Entity entity) {
        Location loc = entity.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        int durationSeconds = plugin.getConfig().getInt("crackers.chakri.spin-duration-seconds", 11);
        int maxTicks = durationSeconds * 20;
        double dangerRadius = plugin.getConfig().getDouble("crackers.chakri.danger-radius-blocks", 2.5);
        double damageHearts = plugin.getConfig().getDouble("crackers.chakri.damage-per-second", 0.5);
        boolean drift = plugin.getConfig().getBoolean("crackers.chakri.enable-drift", true);

        new BukkitRunnable() {
            int ticks = 0;
            double angle = 0;
            Location currentLoc = loc.clone();

            @Override
            public void run() {
                if (!entity.isValid() || ticks >= maxTicks) {
                    world.spawnParticle(Particle.SMOKE, currentLoc.clone().add(0, 0.1, 0), 12, 0.2, 0.1, 0.2, 0.05);
                    ChatUtils.playSound(currentLoc, Sound.BLOCK_FIRE_EXTINGUISH, 0.9f, 1.2f);
                    entity.remove();
                    cancel();
                    return;
                }

                // Random dancing ground drift
                if (drift && ticks % 4 == 0) {
                    double dx = (Math.random() - 0.5) * 0.08;
                    double dz = (Math.random() - 0.5) * 0.08;
                    Location target = currentLoc.clone().add(dx, 0, dz);
                    if (target.getBlock().isPassable()) {
                        currentLoc = target;
                        entity.teleport(currentLoc);
                    }
                }

                // High-velocity spinning spiral arms of electric sparks and embers
                angle += Math.PI / 3;
                for (int arm = 0; arm < 4; arm++) {
                    double armAngle = angle + (arm * (Math.PI / 2));
                    double r = 0.3 + (Math.random() * 1.6);
                    double px = currentLoc.getX() + Math.cos(armAngle) * r;
                    double pz = currentLoc.getZ() + Math.sin(armAngle) * r;
                    Location particleLoc = new Location(world, px, currentLoc.getY() + 0.1, pz);

                    world.spawnParticle(Particle.ELECTRIC_SPARK, particleLoc, 1, 0.02, 0.02, 0.02, 0.03);
                    if (arm % 2 == 0) {
                        world.spawnParticle(Particle.SCRAPE, particleLoc, 1, 0.02, 0.02, 0.02, 0.02);
                        world.spawnParticle(Particle.SMALL_FLAME, particleLoc, 1, 0.01, 0.01, 0.01, 0.01);
                    }
                }

                // Occasional popping lava spark
                if (ticks % 6 == 0) {
                    world.spawnParticle(Particle.LAVA, currentLoc.clone().add(0, 0.1, 0), 1, 0.05, 0.05, 0.05, 0.02);
                }

                // Whirring hiss & crackle audio
                if (ticks % 3 == 0) {
                    ChatUtils.playSound(currentLoc, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0f, 1.9f);
                    ChatUtils.playSound(currentLoc, Sound.BLOCK_LAVA_POP, 0.7f, 1.9f);
                }

                if (ticks % 20 == 0) {
                    CrackerManager.applyDangerDamage(currentLoc, dangerRadius, damageHearts);
                }

                ticks += 3;
            }
        }.runTaskTimer(plugin, 1L, 3L);
    }
}
