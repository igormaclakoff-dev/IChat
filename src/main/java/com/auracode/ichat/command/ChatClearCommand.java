package com.auracode.ichat.command;

import com.auracode.ichat.IChatPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles {@code /clearchat}.
 */
public final class ChatClearCommand implements CommandExecutor {

    private static final int BLANK_LINES = 100;

    private final IChatPlugin plugin;

    public ChatClearCommand(IChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String permission = plugin.getCommandSettings().string("commands.clearchat.permission", "ichat.clearchat");
        if (!permission.isEmpty() && !sender.hasPermission(permission)) {
            plugin.getChatRenderer().send(sender, "commands.clearchat.messages.no-permission",
                    "<red>◆</red> <gray>Вы не можете очистить чат.</gray>");
            return true;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (int line = 0; line < BLANK_LINES; line++) {
                player.sendMessage(" ");
            }
        }
        plugin.getChatRenderer().send(sender, "commands.clearchat.messages.success",
                "<#8A5CF6>◆</#8A5CF6> <gray>Чат очищен.</gray>");
        return true;
    }
}
