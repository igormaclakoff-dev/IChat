package com.auracode.ichat.manager;

import com.auracode.ichat.IChatPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Applies whitelist/blacklist rules and per-command cooldowns.
 */
public final class CommandGuard implements Listener {

    private final IChatPlugin plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public CommandGuard(IChatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String raw = event.getMessage();
        if (raw == null || raw.length() < 2 || raw.charAt(0) != '/') {
            return;
        }
        Player player = event.getPlayer();
        String bypass = plugin.getCommandSettings().string(
                "commands.command-control.bypass-permission", "ichat.commandcontrol.bypass");
        if (!bypass.isEmpty() && player.hasPermission(bypass)) {
            return;
        }
        String command = raw.substring(1).split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        int namespace = command.indexOf(':');
        if (namespace >= 0) {
            command = command.substring(namespace + 1);
        }
        if (isOwnCommand(command)) {
            return;
        }

        if (plugin.getCommandSettings().bool("commands.command-control.whitelist-enabled", false)
                && !isListed("commands.command-control.whitelist", command)) {
            event.setCancelled(true);
            plugin.getChatRenderer().send(player, "commands.command-control.messages.not-whitelisted",
                    "<red>◆</red> <gray>Эта команда запрещена.</gray>");
            return;
        }
        if (plugin.getCommandSettings().bool("commands.command-control.blacklist-enabled", false)
                && isListed("commands.command-control.blacklist", command)) {
            event.setCancelled(true);
            plugin.getChatRenderer().send(player, "commands.command-control.messages.blacklisted",
                    "<red>◆</red> <gray>Эта команда заблокирована.</gray>");
            return;
        }
        applyCooldown(player, command, event);
    }

    private void applyCooldown(Player player, String command, PlayerCommandPreprocessEvent event) {
        long seconds = plugin.getCommandSettings().longValue("commands.command-control.delays." + command, 0L);
        if (seconds <= 0L) {
            return;
        }
        String delayBypass = plugin.getCommandSettings().string(
                "commands.command-control.delay-permissions.bypass", "ichat.commanddelay.bypass");
        if (!delayBypass.isEmpty() && player.hasPermission(delayBypass)) {
            return;
        }
        String reducePermission = plugin.getCommandSettings().string(
                "commands.command-control.delay-permissions.reduce", "ichat.commanddelay.reduce");
        if (!reducePermission.isEmpty() && player.hasPermission(reducePermission)) {
            seconds = plugin.getCommandSettings().longValue(
                    "commands.command-control.delay-permissions.reduced-seconds", seconds);
        }
        if (seconds <= 0L) {
            return;
        }
        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(player.getUniqueId(), key -> new HashMap<>());
        long now = System.currentTimeMillis();
        long until = playerCooldowns.getOrDefault(command, 0L);
        if (until > now) {
            long left = Math.max(1L, (until - now + 999L) / 1000L);
            event.setCancelled(true);
            String message = plugin.getCommandSettings().string("commands.command-control.messages.cooldown",
                    "<red>◆</red> <gray>Подождите <white>%seconds%</white> сек.</gray>").replace("%seconds%", Long.toString(left));
            player.sendMessage(plugin.getChatRenderer().parse(message));
            return;
        }
        playerCooldowns.put(command, now + seconds * 1000L);
    }

    private boolean isOwnCommand(String command) {
        if (plugin.getChannelRegistry().byCommand(command) != null) {
            return true;
        }
        org.bukkit.command.PluginCommand pluginCommand = plugin.getServer().getPluginCommand(command);
        return pluginCommand != null && pluginCommand.getPlugin().equals(plugin);
    }

    private boolean isListed(String path, String command) {
        List<String> entries = plugin.getCommandSettings().list(path);
        for (String entry : entries) {
            if (entry == null) {
                continue;
            }
            String value = entry.trim().toLowerCase(Locale.ROOT);
            if (value.startsWith("/")) {
                value = value.substring(1);
            }
            if (value.equals(command)) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }
}
