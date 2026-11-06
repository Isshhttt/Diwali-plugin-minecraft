package com.ishttt.diwali.config;

import com.ishttt.diwali.DiwaliPlugin;
import com.ishttt.diwali.utils.ChatUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class ConfigManager {

    private final DiwaliPlugin plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;

    public ConfigManager(DiwaliPlugin plugin) {
        this.plugin = plugin;
        loadConfigs();
    }

    public void loadConfigs() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        InputStream defMsgStream = plugin.getResource("messages.yml");
        if (defMsgStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defMsgStream, StandardCharsets.UTF_8));
            messagesConfig.setDefaults(defConfig);
        }
    }

    public String getPrefix() {
        return ChatUtils.color(messagesConfig.getString("prefix", "&6&l[🪔 Diwali]&r "));
    }

    public String getMessage(String path) {
        String msg = messagesConfig.getString(path, "&cMissing message: " + path);
        return ChatUtils.color(getPrefix() + msg);
    }

    public String getRawMessage(String path) {
        return ChatUtils.color(messagesConfig.getString(path, ""));
    }

    public List<String> getMessageList(String path) {
        List<String> list = messagesConfig.getStringList(path);
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(ChatUtils::color).toList();
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }
}
