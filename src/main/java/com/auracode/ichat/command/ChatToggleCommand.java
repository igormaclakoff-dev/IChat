package com.auracode.ichat.command;

import com.auracode.ichat.IChatPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles {@code /chat}.
 */
public final class ChatToggleCommand implements CommandExecutor {

    private final IChatPlugin plugin;

    public ChatToggleCommand(IChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getChatRenderer().send(sender, "commands.chat.messages.player-only",
                    "<red>◆</red> <gray>Команду может использовать только игрок.</gray>");
            return true;
        }
        String permission = plugin.getCommandSettings().string("commands.chat.permission", "ichat.chat.toggle");
        if (!permission.isEmpty() && !player.hasPermission(permission)) {
            plugin.getChatRenderer().send(player, "commands.chat.messages.no-permission",
                    "<red>◆</red> <gray>Вы не можете управлять чатом.</gray>");
            return true;
        }
        if (args.length == 0) {
            boolean enabled = plugin.getChatManager().isEnabled();
            plugin.getChatRenderer().send(player,
                    enabled ? "commands.chat.messages.status-enabled" : "commands.chat.messages.status-disabled",
                    enabled ? "<green>◆</green> <gray>Чат сейчас включён.</gray>"
                            : "<red>◆</red> <gray>Чат сейчас отключён.</gray>");
            return true;
        }
        if (args.length == 1 && (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off"))) {
            boolean requested = args[0].equalsIgnoreCase("on");
            plugin.getChatManager().setEnabled(requested);
            plugin.getChatRenderer().send(player,
                    requested ? "commands.chat.messages.enabled" : "commands.chat.messages.disabled",
                    requested ? "<green>◆</green> <gray>Чат включён.</gray>"
                            : "<red>◆</red> <gray>Чат отключён.</gray>");
            return true;
        }
        plugin.getChatRenderer().send(player, "commands.chat.messages.usage",
                "<red>◆</red> <gray>Использование: <white>/chat <on|off></white></gray>");
        return true;
    }
}
