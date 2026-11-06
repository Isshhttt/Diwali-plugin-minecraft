package com.ishttt.diwali.sweets;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.items.DiwaliItemType;
import com.ishttt.diwali.utils.ChatUtils;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MithaiListener implements Listener {

    private final DiwaliPlugin plugin;

    public MithaiListener(DiwaliPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        DiwaliItemType type = plugin.getItemManager().getDiwaliType(item);
        if (type == null) return;

        Player player = event.getPlayer();

        switch (type) {
            case KAJU_KATLI -> {
                int dur = plugin.getConfig().getInt("sweets.kaju-katli.haste-duration", 180) * 20;
                player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, dur, 1, false, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, dur, 0, false, true, true));
                playSweetEffects(player, Color.fromRGB(240, 230, 210), "✨ The silver foil Kaju Katli energizes your spirit!");
            }
            case MOTICHOOR_LADOO -> {
                int dur = plugin.getConfig().getInt("sweets.motichoor-ladoo.absorption-duration", 180) * 20;
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, dur, 1, false, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, dur, 0, false, true, true));
                playSweetEffects(player, Color.fromRGB(255, 140, 0), "✨ Golden Motichoor Ladoo warms and strengthens you!");
            }
            case GULAB_JAMUN -> {
                int dur = plugin.getConfig().getInt("sweets.gulab-jamun.regeneration-duration", 180) * 20;
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, dur, 0, false, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, dur, 0, false, true, true));
                playSweetEffects(player, Color.fromRGB(180, 50, 50), "✨ Sweet saffron syrup fills you with celebratory health & glow!");
            }
            default -> {}
        }
    }

    private void playSweetEffects(Player player, Color color, String message) {
        player.sendMessage(ChatUtils.color(message));
        ChatUtils.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 0.8f, 1.2f);
        ChatUtils.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.8f);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1.5, 0), 4, 0.3, 0.3, 0.3, 0.05);
        for (int i = 0; i < 8; i++) {
            ChatUtils.spawnDust(player.getLocation().add((Math.random() - 0.5) * 0.8, 1.0 + Math.random() * 0.5, (Math.random() - 0.5) * 0.8), color, 1.2f);
        }
    }
}
