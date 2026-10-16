package com.auracode.ichat.listener;

import com.auracode.ichat.IChatPlugin;
import com.auracode.ichat.model.Channel;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;

/**
 * Intercepts {@code /<command> <message>} for command-driven channels, since
 * those commands are not registered with Bukkit.
 */
public final class ChannelCommandListener implements Listener {

    private final IChatPlugin plugin;

    public ChannelCommandListener(IChatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (message == null || message.length() < 2 || message.charAt(0) != '/') {
            return;
        }
        String commandLine = message.substring(1).trim();
        if (commandLine.isEmpty()) {
            return;
        }
        String[] parts = commandLine.split("\\s+");
        Channel channel = plugin.getChannelRegistry().byCommand(parts[0]);
        if (channel == null) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        if (!plugin.getChannelRouter().hasAccess(player, channel)) {
            plugin.getChatRenderer().send(player, "commands.channel.messages.no-permission",
                    "<red>◆</red> <gray>Вы не можете использовать этот канал.</gray>");
            return;
        }
        if (parts.length < 2) {
            String usage = plugin.getCommandSettings().string("commands.channel.messages.usage",
                    "<red>◆</red> <gray>Использование: <white>/%command% <сообщение></white></gray>");
            player.sendMessage(plugin.getChatRenderer().parse(usage.replace("%command%", parts[0])));
            return;
        }
        String content = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
        plugin.getChannelDispatcher().dispatch(player, channel, content);
    }
}
