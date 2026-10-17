package com.auracode.ichat.manager;

import com.auracode.ichat.IChatPlugin;
import com.auracode.ichat.model.Channel;
import com.auracode.ichat.model.ChannelMode;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads and indexes the channels declared in {@code config.yml}.
 */
public final class ChannelRegistry {

    private final IChatPlugin plugin;
    private volatile List<Channel> channels = List.of();

    public ChannelRegistry(IChatPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("channels");
        if (section == null) {
            plugin.getLogger().warning("В config.yml отсутствует секция 'channels'.");
            return;
        }
        List<Channel> loaded = new ArrayList<>();
        for (String id : section.getKeys(false)) {
            ConfigurationSection node = section.getConfigurationSection(id);
            if (node != null) {
                loaded.add(read(id, node));
            }
        }
        this.channels = List.copyOf(loaded);
        plugin.getLogger().info("Загружено каналов: " + channels.size() + ".");
    }

    private Channel read(String id, ConfigurationSection node) {
        ChannelMode mode = ChannelMode.from(node.getString("mode", "local"));
        return new Channel(
                id,
                node.getBoolean("enabled", true),
                mode,
                node.getString("format", defaultFormat(id, mode)),
                List.copyOf(node.getStringList("aliases")),
                node.getString("trigger", ""),
                node.getString("command", ""),
                node.getString("permission", ""),
                Math.max(0, node.getInt("radius", 100)),
                node.getBoolean("unheard-message.enabled", true),
                node.contains("unheard-message.message") ? node.getString("unheard-message.message") : null
        );
    }

    private String defaultFormat(String id, ChannelMode mode) {
        return switch (mode) {
            case GLOBAL -> "<#FF7000>%player%</#FF7000> <gray>»</gray> <white>%message%</white>";
            case COMMAND -> "<gradient:#8A5CF6:#FF7000>[" + id + "]</gradient> <#8A5CF6>%player%</#8A5CF6> <gray>»</gray> <white>%message%</white>";
            case LOCAL -> "<#8A5CF6>%player%</#8A5CF6> <gray>»</gray> <white>%message%</white>";
        };
    }

    public Channel byId(String id) {
        for (Channel channel : channels) {
            if (channel.id().equalsIgnoreCase(id)) {
                return channel;
            }
        }
        return null;
    }

    public Channel byCommand(String command) {
        if (command == null || command.isEmpty()) {
            return null;
        }
        String clean = command.startsWith("/") ? command.substring(1) : command;
        for (Channel channel : channels) {
            if (!channel.enabled()) {
                continue;
            }
            if (!channel.command().isEmpty() && channel.command().equalsIgnoreCase(clean)) {
                return channel;
            }
            for (String alias : channel.aliases()) {
                if (alias != null && alias.equalsIgnoreCase(clean)) {
                    return channel;
                }
            }
        }
        return null;
    }

    public List<Channel> all() {
        return channels;
    }
}
