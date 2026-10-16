package com.auracode.ichat.command;

import com.auracode.ichat.IChatPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles {@code /socialspy} and {@code /commandspy}.
 */
public final class SpyToggleCommand implements CommandExecutor {

    private final IChatPlugin plugin;
    private final boolean social;

    public SpyToggleCommand(IChatPlugin plugin, boolean social) {
        this.plugin = plugin;
        this.social = social;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getChatRenderer().send(sender, root() + ".messages.player-only",
                    "<red>◆</red> <gray>Команду может использовать только игрок.</gray>");
            return true;
        }
        String permission = plugin.getCommandSettings().string(root() + ".permission",
                social ? "ichat.socialspy" : "ichat.commandspy");
        if (!permission.isEmpty() && !player.hasPermission(permission)) {
            plugin.getChatRenderer().send(player, root() + ".messages.no-permission",
                    "<red>◆</red> <gray>У вас нет доступа к этой функции.</gray>");
            return true;
        }
        boolean enabled = social ? plugin.getSpyManager().toggleSocialSpy(player)
                : plugin.getSpyManager().toggleCommandSpy(player);
        plugin.getChatRenderer().send(player, root() + (enabled ? ".messages.enabled" : ".messages.disabled"),
                enabled ? "<#8A5CF6>◆</#8A5CF6> <gray>включён.</gray>"
                        : "<#8A5CF6>◆</#8A5CF6> <gray>выключен.</gray>");
        return true;
    }

    private String root() {
        return social ? "commands.socialspy" : "commands.commandspy";
    }
}
