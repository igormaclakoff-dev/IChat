package com.auracode.ichat.listener;

import com.auracode.ichat.IChatPlugin;
import com.auracode.ichat.model.Channel;
import com.auracode.ichat.model.ChannelMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Provides tab completion for command-driven channels without registering them
 * with Bukkit.
 */
public final class ChannelTabListener implements Listener {

    private final IChatPlugin plugin;

    public ChannelTabListener(IChatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTabComplete(TabCompleteEvent event) {
        String buffer = event.getBuffer();
        if (buffer == null || !buffer.startsWith("/")) {
            return;
        }
        String input = buffer.substring(1);
        int space = input.indexOf(' ');
        if (space >= 0) {
            String command = input.substring(0, space);
            Channel channel = plugin.getChannelRegistry().byCommand(command);
            if (channel != null && channel.mode() == ChannelMode.COMMAND
                    && input.substring(space + 1).trim().isEmpty()) {
                event.setCompletions(new ArrayList<>());
            }
            return;
        }
        String prefix = input.toLowerCase(Locale.ROOT);
        List<String> completions = new ArrayList<>();
        for (Channel channel : plugin.getChannelRegistry().all()) {
            collectCompletions(channel, prefix, completions);
        }
        if (!completions.isEmpty()) {
            event.setCompletions(completions);
        }
    }

    private void collectCompletions(Channel channel, String prefix, List<String> completions) {
        if (!channel.enabled() || channel.mode() != ChannelMode.COMMAND) {
            return;
        }
        String command = channel.command();
        if (command != null && !command.isEmpty() && command.toLowerCase(Locale.ROOT).startsWith(prefix)) {
            completions.add(command);
        }
        for (String alias : channel.aliases()) {
            if (alias != null && !alias.isEmpty() && alias.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                completions.add(alias);
            }
        }
    }
}
