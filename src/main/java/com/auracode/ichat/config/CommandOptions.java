package com.auracode.ichat.config;

import com.auracode.ichat.IChatPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

/**
 * Typed access to the standalone {@code commands.yml} file.
 */
public final class CommandOptions {

    private final IChatPlugin plugin;
    private YamlConfiguration config;

    public CommandOptions(IChatPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        java.io.File file = new java.io.File(plugin.getDataFolder(), "commands.yml");
        if (!file.exists()) {
            plugin.saveResource("commands.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        load();
    }

    public String string(String path, String fallback) {
        return config.getString(path, fallback);
    }

    public boolean bool(String path, boolean fallback) {
        return config.getBoolean(path, fallback);
    }

    public long longValue(String path, long fallback) {
        return config.getLong(path, fallback);
    }

    public double doubleValue(String path, double fallback) {
        return config.getDouble(path, fallback);
    }

    public List<String> list(String path) {
        return config.getStringList(path);
    }
}
