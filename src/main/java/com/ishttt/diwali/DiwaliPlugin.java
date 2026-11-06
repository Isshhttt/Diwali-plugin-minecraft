package com.ishttt.diwali;

import com.ishttt.diwali.commands.DiwaliCommand;
import com.ishttt.diwali.config.ConfigManager;
import com.ishttt.diwali.crackers.CrackerManager;
import com.ishttt.diwali.crackers.PhuljhadiListener;
import com.ishttt.diwali.decorations.KandilManager;
import com.ishttt.diwali.decorations.ToranManager;
import com.ishttt.diwali.diyas.DiyaManager;
import com.ishttt.diwali.gifts.GiftBoxManager;
import com.ishttt.diwali.items.ItemManager;
import com.ishttt.diwali.lights.JhalarManager;
import com.ishttt.diwali.listeners.DiwaliBreakListener;
import com.ishttt.diwali.listeners.DiwaliInteractListener;
import com.ishttt.diwali.listeners.DiwaliJoinListener;
import com.ishttt.diwali.shop.DiwaliShopGUI;
import com.ishttt.diwali.sweets.MithaiListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class DiwaliPlugin extends JavaPlugin {

    private static DiwaliPlugin instance;

    private ConfigManager configManager;
    private ItemManager itemManager;
    private JhalarManager jhalarManager;
    private DiyaManager diyaManager;
    private CrackerManager crackerManager;
    private GiftBoxManager giftBoxManager;
    private ToranManager toranManager;
    private KandilManager kandilManager;
    private DiwaliShopGUI shopGUI;

    public static DiwaliPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Load configs
        this.configManager = new ConfigManager(this);

        // Export resource pack zip to plugins/Diwali/DiwaliPack.zip
        exportResourcePack();

        // Initialize core systems
        this.itemManager = new ItemManager(this);
        this.jhalarManager = new JhalarManager(this);
        this.diyaManager = new DiyaManager(this);
        this.crackerManager = new CrackerManager(this);
        this.giftBoxManager = new GiftBoxManager(this);
        this.toranManager = new ToranManager(this);
        this.kandilManager = new KandilManager(this);
        this.shopGUI = new DiwaliShopGUI(this);

        // Register event listeners
        var pm = getServer().getPluginManager();
        pm.registerEvents(new DiwaliInteractListener(this), this);
        pm.registerEvents(new DiwaliBreakListener(this), this);
        pm.registerEvents(new DiwaliJoinListener(this), this);
        pm.registerEvents(new PhuljhadiListener(this), this);
        pm.registerEvents(giftBoxManager, this);
        pm.registerEvents(shopGUI, this);
        pm.registerEvents(new MithaiListener(this), this);

        // Register commands
        var cmd = getCommand("diwali");
        if (cmd != null) {
            DiwaliCommand executor = new DiwaliCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        getLogger().info("==================================================");
        getLogger().info("  🪔 Diwali Plugin v" + getDescription().getVersion() + " Enabled! ✨");
        getLogger().info("  Festival lights, Diyas, Patakhe & Sweets active!");
        getLogger().info("  Resource pack exported: plugins/Diwali/DiwaliPack.zip");
        getLogger().info("==================================================");
    }

    @Override
    public void onDisable() {
        if (diyaManager != null) {
            diyaManager.cleanup();
        }
        getLogger().info("🪔 Diwali Plugin Disabled. Shubh Deepavali!");
    }

    private void exportResourcePack() {
        File packFile = new File(getDataFolder(), "DiwaliPack.zip");
        if (!packFile.exists()) {
            getDataFolder().mkdirs();
            try (InputStream in = getResource("DiwaliPack.zip")) {
                if (in != null) {
                    try (FileOutputStream out = new FileOutputStream(packFile)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                }
            } catch (Exception e) {
                getLogger().warning("Could not export DiwaliPack.zip: " + e.getMessage());
            }
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public JhalarManager getJhalarManager() {
        return jhalarManager;
    }

    public DiyaManager getDiyaManager() {
        return diyaManager;
    }

    public CrackerManager getCrackerManager() {
        return crackerManager;
    }

    public GiftBoxManager getGiftBoxManager() {
        return giftBoxManager;
    }

    public ToranManager getToranManager() {
        return toranManager;
    }

    public KandilManager getKandilManager() {
        return kandilManager;
    }

    public DiwaliShopGUI getShopGUI() {
        return shopGUI;
    }
}
