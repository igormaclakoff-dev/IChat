package com.auracode.ichat.util;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Thin bridge to PlaceholderAPI. When the plugin is absent every call is a
 * no-op, so IChat works standalone.
 */
public final class Placeholders {

    private final boolean available;

    public Placeholders() {
        this.available = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public boolean isAvailable() {
        return available;
    }

    /**
     * Applies PlaceholderAPI placeholders for the given player.
     *
     * @param player context player, may be {@code null}
     * @param text   input text, may be {@code null}
     * @return the replaced text, or the input when integration is unavailable
     */
    public String apply(Player player, String text) {
        if (!available || player == null || text == null || text.isEmpty()) {
            return text;
        }
        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
