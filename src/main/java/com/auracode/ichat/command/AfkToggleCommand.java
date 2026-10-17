package com.auracode.ichat.command;

import com.auracode.ichat.IChatPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles {@code /afk}.
 */
public final class AfkToggleCommand implements CommandExecutor {

    private final IChatPlugin plugin;

    public AfkToggleCommand(IChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getChatRenderer().send(sender, "commands.afk.messages.player-only",
                    "<red>◆</red> <gray>Команду может использовать только игрок.</gray>");
            return true;
        }
        if (!plugin.getCommandSettings().bool("commands.afk.enabled", true)) {
            plugin.getChatRenderer().send(player, "commands.afk.messages.feature-disabled",
                    "<red>◆</red> <gray>AFK отключён.</gray>");
            return true;
        }
        String permission = plugin.getCommandSettings().string("commands.afk.permission", "ichat.afk");
        if (!permission.isEmpty() && !player.hasPermission(permission)) {
            plugin.getChatRenderer().send(player, "commands.afk.messages.no-permission",
                    "<red>◆</red> <gray>У вас нет доступа к AFK.</gray>");
            return true;
        }
        boolean enabled = plugin.getAfkManager().toggle(player);
        plugin.getChatRenderer().send(player, enabled ? "commands.afk.messages.enabled" : "commands.afk.messages.disabled",
                enabled ? "<#8A5CF6>◆</#8A5CF6> <gray>Вы отошли от клавиатуры.</gray>"
                        : "<#8A5CF6>◆</#8A5CF6> <gray>Вы снова активны.</gray>");
        return true;
    }
}
