package com.auracode.ichat.manager;

import com.auracode.ichat.IChatPlugin;
import com.auracode.ichat.model.Channel;
import org.bukkit.entity.Player;

/**
 * Resolves which channel a message belongs to and whether a viewer may receive
 * it.
 */
public final class ChannelRouter {

    private final IChatPlugin plugin;

    public ChannelRouter(IChatPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Finds the channel for a plain chat message, falling back to the local
     * channel when no trigger matches.
     *
     * @param player       the sender
     * @param plainMessage the raw plain-text message
     * @return the matching channel, or {@code null} when none is available
     */
    public Channel channelFor(Player player, String plainMessage) {
        for (Channel channel : plugin.getChannelRegistry().all()) {
            if (!channel.enabled()) {
                continue;
            }
            String trigger = channel.trigger();
            if (trigger != null && !trigger.isEmpty() && plainMessage.startsWith(trigger)) {
                return channel;
            }
        }
        Channel local = plugin.getChannelRegistry().byId("local");
        return local != null && local.enabled() ? local : null;
    }

    public boolean hasAccess(Player player, Channel channel) {
        String permission = channel.permission();
        return permission == null || permission.isEmpty() || player.hasPermission(permission);
    }

    public String stripTrigger(Channel channel, String message) {
        String trigger = channel.trigger();
        if (trigger != null && !trigger.isEmpty() && message.startsWith(trigger)) {
            return message.substring(trigger.length()).trim();
        }
        return message.trim();
    }

    public boolean canReceive(Player sender, Player viewer, Channel channel) {
        if (!channel.enabled() || !hasAccess(viewer, channel)) {
            return false;
        }
        return switch (channel.mode()) {
            case GLOBAL, COMMAND -> true;
            case LOCAL -> withinRadius(sender, viewer, channel.radius());
        };
    }

    private boolean withinRadius(Player sender, Player viewer, int radius) {
        if (!sender.getWorld().equals(viewer.getWorld())) {
            return false;
        }
        return sender.getLocation().distanceSquared(viewer.getLocation()) <= (double) radius * radius;
    }
}
