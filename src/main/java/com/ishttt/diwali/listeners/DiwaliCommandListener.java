package com.ishttt.diwali.listeners;

import com.ishttt.diwali.DiwaliPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class DiwaliCommandListener implements Listener {

    private final DiwaliPlugin plugin;

    public DiwaliCommandListener(DiwaliPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String msg = event.getMessage().trim().toLowerCase();
        if (msg.equals("/clear diwali") || msg.startsWith("/clear diwali ")
                || msg.equals("/minecraft:clear diwali") || msg.startsWith("/minecraft:clear diwali ")
                || msg.equals("/diwali:clear diwali") || msg.startsWith("/diwali:clear diwali ")) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            if (!player.hasPermission("diwali.admin")) {
                player.sendMessage(plugin.getConfigManager().getMessage("general.no-permission"));
                return;
            }
            plugin.getPurgeManager().executePurge(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onServerCommand(ServerCommandEvent event) {
        String cmd = event.getCommand().trim().toLowerCase();
        if (cmd.equals("clear diwali") || cmd.startsWith("clear diwali ")
                || cmd.equals("/clear diwali") || cmd.startsWith("/clear diwali ")
                || cmd.equals("minecraft:clear diwali") || cmd.startsWith("minecraft:clear diwali ")) {
            event.setCancelled(true);
            plugin.getPurgeManager().executePurge(event.getSender());
        }
    }
}
