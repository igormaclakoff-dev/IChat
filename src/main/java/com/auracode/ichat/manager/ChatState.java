package com.auracode.ichat.manager;

import com.auracode.ichat.IChatPlugin;
import org.bukkit.entity.Player;

/**
 * Holds the global chat toggle state.
 */
public final class ChatState {

    private final IChatPlugin plugin;
    private volatile boolean enabled = true;

    public ChatState(IChatPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean canBypass(Player player) {
        String permission = plugin.getCommandSettings().string("commands.chat.bypass-permission", "ichat.chat.bypass");
        return permission.isEmpty() || player.hasPermission(permission);
    }
}
