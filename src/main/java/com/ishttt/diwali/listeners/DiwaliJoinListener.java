package com.ishttt.diwali.listeners;

import com.ishttt.diwali.DiwaliPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class DiwaliJoinListener implements Listener {

    private final DiwaliPlugin plugin;

    public DiwaliJoinListener(DiwaliPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("settings.announce-on-join", true)) return;

        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                List<String> greeting = plugin.getConfigManager().getMessageList("general.join-greeting");
                for (String line : greeting) {
                    player.sendMessage(line);
                }
            }
        }, 40L); // 2 seconds after join
    }
}
