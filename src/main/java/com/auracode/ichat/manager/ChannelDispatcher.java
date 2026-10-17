package com.auracode.ichat.manager;

import com.auracode.ichat.IChatPlugin;
import com.auracode.ichat.model.Channel;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Delivers a message to every recipient of a channel. Used both by
 * command-driven channels and by the chat listener after chat is captured.
 */
public final class ChannelDispatcher {

    private final IChatPlugin plugin;

    public ChannelDispatcher(IChatPlugin plugin) {
        this.plugin = plugin;
    }

    public void dispatch(Player sender, Channel channel, String message) {
        if (channel == null || !channel.enabled()) {
            return;
        }
        String content = message == null ? "" : message.trim();
        if (content.isEmpty()) {
            return;
        }
        if (!plugin.getChannelRouter().hasAccess(sender, channel)) {
            plugin.getChatRenderer().send(sender, "commands.channel.messages.no-permission",
                    "<red>◆</red> <gray>Вы не можете использовать этот канал.</gray>");
            return;
        }
        Component rendered = plugin.getChatRenderer().render(sender, channel, content);
        rendered = plugin.getMentionManager().colorize(rendered);

        int recipients = 0;
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (plugin.getChannelRouter().canReceive(sender, viewer, channel)) {
                viewer.sendMessage(rendered);
                if (!viewer.equals(sender)) {
                    recipients++;
                }
            }
        }
        Bukkit.getConsoleSender().sendMessage(rendered);
        plugin.getMentionManager().notifyMentions(content, sender, channel);
        if (recipients == 0) {
            plugin.getChatRenderer().notifyUnheard(sender, channel);
        }
    }
}
