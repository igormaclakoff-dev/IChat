package com.auracode.ichat.hook;

import com.auracode.ichat.IChatPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Exposes IChat state to PlaceholderAPI so other plugins (TAB, scoreboards, ...)
 * can display it. Placeholders use the identifier {@code ichat}, for example
 * {@code %ichat_afk%} which renders the configured AFK marker for AFK players
 * and an empty string otherwise.
 */
public final class IChatExpansion extends PlaceholderExpansion {

    private static final String DEFAULT_AFK_MARKER = " &7[AFK]";

    private final IChatPlugin plugin;

    public IChatExpansion(IChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ichat";
    }

    @Override
    public @NotNull String getAuthor() {
        return "AuraCode";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        Player online = player.getPlayer();
        if (online == null) {
            return "";
        }
        boolean afk = plugin.getAfkManager().isAfk(online);
        return switch (params.toLowerCase()) {
            case "afk" -> afk ? afkMarker() : "";
            case "afk_bool" -> afk ? "true" : "false";
            default -> null;
        };
    }

    private String afkMarker() {
        String marker = plugin.getCommandSettings().string("commands.afk.tab-marker", DEFAULT_AFK_MARKER);
        return marker == null ? "" : marker;
    }
}
