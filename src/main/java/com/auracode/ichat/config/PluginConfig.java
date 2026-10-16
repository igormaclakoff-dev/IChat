package com.auracode.ichat.config;

import com.auracode.ichat.IChatPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * Typed access to {@code config.yml}. Values are read lazily so a reload is
 * picked up immediately.
 */
public final class PluginConfig {

    private final IChatPlugin plugin;

    public PluginConfig(IChatPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public FileConfiguration raw() {
        return plugin.getConfig();
    }

    public boolean hoverEnabled() {
        return raw().getBoolean("player-interaction.hover.enabled", true);
    }

    public List<String> hoverLines() {
        return raw().getStringList("player-interaction.hover.text");
    }

    public boolean clickEnabled() {
        return raw().getBoolean("player-interaction.click.enabled", true);
    }

    public String clickAction() {
        return raw().getString("player-interaction.click.action", "suggest-message");
    }

    public boolean mentionsEnabled() {
        return raw().getBoolean("mentions.enabled", true);
    }

    public String mentionFormat() {
        return raw().getString("mentions.format", "<#FF7000>@%player%</#FF7000>");
    }

    public boolean mentionNotificationsEnabled() {
        return raw().getBoolean("mentions.notifications.enabled", true);
    }

    public String mentionNotificationMessage() {
        return raw().getString("mentions.notifications.message",
                "<#8A5CF6>◆</#8A5CF6> <gray><white>%player%</white> упомянул вас.</gray>");
    }

    public String mentionOfflineMessage() {
        return raw().getString("mentions.notifications.offline",
                "<yellow>⚠</yellow> <gray><white>%player%</white> не в сети.</gray>");
    }

    public String mentionNotFoundMessage() {
        return raw().getString("mentions.notifications.not-found",
                "<red>⚠</red> <gray><white>%player%</white> не найден.</gray>");
    }

    public boolean mentionSoundEnabled() {
        return raw().getBoolean("mentions.notifications.sound.enabled", true);
    }

    public String mentionSoundName() {
        return raw().getString("mentions.notifications.sound.name", "ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    public float mentionSoundVolume() {
        return (float) raw().getDouble("mentions.notifications.sound.volume", 1.0D);
    }

    public float mentionSoundPitch() {
        return (float) raw().getDouble("mentions.notifications.sound.pitch", 1.0D);
    }

    public boolean chatColorEnabled() {
        return raw().getBoolean("chat-color.enabled", true);
    }

    public String chatColorPermission() {
        return raw().getString("chat-color.permission", "ichat.chatcolor");
    }

    public boolean unheardEnabled() {
        return raw().getBoolean("unheard-message.enabled", true);
    }

    public String unheardMessage() {
        return raw().getString("unheard-message.message", "<red>⚠</red> <gray>Вас никто не услышал.</gray>");
    }
}
