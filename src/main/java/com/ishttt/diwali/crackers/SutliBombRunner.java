package com.ishttt.diwali.crackers;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.utils.ChatUtils;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class SutliBombRunner {

    private SutliBombRunner() {}

    public static void run(DiwaliPlugin plugin, Entity entity) {
        Location loc = entity.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        int fuseSec = plugin.getConfig().getInt("crackers.sutli-bomb.fuse-seconds", 4);
        int totalFuseTicks = fuseSec * 20;

        // Fuse burning with sparks
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!entity.isValid()) {
                    cancel();
                    return;
                }

                if (ticks < totalFuseTicks) {
                    Location fuseTip = loc.clone().add(0, 0.45, 0);
                    world.spawnParticle(Particle.SMALL_FLAME, fuseTip, 2, 0.02, 0.02, 0.02, 0.01);
                    world.spawnParticle(Particle.ELECTRIC_SPARK, fuseTip, 3, 0.03, 0.03, 0.03, 0.04);
                    world.spawnParticle(Particle.SMOKE, fuseTip, 2, 0.02, 0.05, 0.02, 0.01);
                    if (ticks % 10 == 0) {
                        ChatUtils.playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 0.7f, 1.8f);
                    }
                    ticks += 5;
                    return;
                }

                cancel();
                detonate(plugin, entity, loc);
            }
        }.runTaskTimer(plugin, 1L, 5L);
    }

    private static void detonate(DiwaliPlugin plugin, Entity entity, Location loc) {
        World world = loc.getWorld();
        if (world == null) return;

        double blastRadius = plugin.getConfig().getDouble("crackers.sutli-bomb.blast-radius-blocks", 4.5);
        double damageHearts = plugin.getConfig().getDouble("crackers.sutli-bomb.blast-damage-hearts", 7.0);
        int burntDuration = plugin.getConfig().getInt("crackers.sutli-bomb.burnt-effect-duration-seconds", 5);

        // Visual shockwave & heavy smoke (NO block destruction)
        world.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1, 0, 0, 0, 0);
        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 50, 1.8, 1.0, 1.8, 0.08);
        world.spawnParticle(Particle.FLAME, loc, 30, 1.4, 0.6, 1.4, 0.1);
        world.spawnParticle(Particle.LAVA, loc, 15, 0.8, 0.5, 0.8, 0.1);
        world.spawnParticle(Particle.ELECTRIC_SPARK, loc, 30, 1.2, 0.8, 1.2, 0.2);

        ChatUtils.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.7f);
        ChatUtils.playSound(loc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 2.0f, 0.8f);

        // Apply true fixed 7-heart damage and Burnt effect to nearby players
        double damageHp = damageHearts * 2.0; // 7 hearts = 14 HP
        for (Player p : world.getPlayers()) {
            if (p.getLocation().distance(loc) <= blastRadius) {
                double newHealth = Math.max(0.0, p.getHealth() - damageHp);
                p.setHealth(newHealth);
                p.playHurtAnimation(0.0f);
                CrackerManager.applyBurntEffect(p, burntDuration);
            }
        }

        entity.remove();
    }
}
