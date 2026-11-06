package com.ishttt.diwali.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ChatUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private ChatUtils() {}

    public static String color(String message) {
        if (message == null) return "";
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static void send(Player player, String message) {
        if (player != null && message != null && !message.isEmpty()) {
            player.sendMessage(color(message));
        }
    }

    public static void playSound(Location loc, Sound sound, float volume, float pitch) {
        if (loc.getWorld() != null) {
            loc.getWorld().playSound(loc, sound, volume, pitch);
        }
    }

    public static void spawnDust(Location loc, Color color, float size) {
        if (loc.getWorld() != null) {
            Particle.DustOptions dust = new Particle.DustOptions(color, size);
            loc.getWorld().spawnParticle(Particle.DUST, loc, 1, dust);
        }
    }
}
