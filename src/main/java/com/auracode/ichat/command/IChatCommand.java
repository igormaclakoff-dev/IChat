package com.auracode.ichat.command;

import com.auracode.ichat.IChatPlugin;
import com.auracode.ichat.model.Channel;
import com.auracode.ichat.model.ChannelMode;
import com.auracode.ichat.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Handles {@code /ichat help|reload|version}.
 */
public final class IChatCommand implements CommandExecutor {

    private static final String PRIMARY = "<#8A5CF6>";
    private static final String ACCENT = "<#FF7000>";
    private static final String MUTED = "<#9E9E9E>";
    private static final String TEXT = "<#E0E0E0>";

    private final IChatPlugin plugin;

    public IChatCommand(IChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadAll();
            sender.sendMessage(Text.parse(PRIMARY + "◆</#8A5CF6> " + TEXT + "Конфигурация IChat перезагружена.</#E0E0E0>"));
            return true;
        }
        if (args[0].equalsIgnoreCase("version")) {
            sendVersion(sender);
            return true;
        }
        sender.sendMessage(Text.parse(MUTED + "Использование: " + TEXT + "/" + label + " help</#E0E0E0>"));
        return true;
    }

    private void sendVersion(CommandSender sender) {
        sender.sendMessage(Text.parse(PRIMARY + "<bold>IChat</bold></#8A5CF6> <dark_gray>│</dark_gray> " + MUTED + "от AuraCode</#9E9E9E>"));
        sender.sendMessage(Text.parse(MUTED + "Версия: " + TEXT + plugin.getPluginMeta().getVersion() + "</#E0E0E0>"));
        sender.sendMessage(Text.parse(MUTED + "Автор: " + TEXT + "AuraCode</#E0E0E0>"));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Text.parse("<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"));
        sender.sendMessage(Text.parse("<gradient:#8A5CF6:#FF7000><bold>IChat</bold></gradient> <dark_gray>│</dark_gray> <gray>список команд</gray> <dark_gray>v" + plugin.getPluginMeta().getVersion() + "</dark_gray>"));

        sender.sendMessage(Text.parse("<#FF7000><bold>Игроку</bold></#FF7000>"));
        helpLine(sender, "/msg <игрок> <сообщение>", "Отправить личное сообщение", "/msg ", "ichat.msg");
        helpLine(sender, "/r <сообщение>", "Ответить на последнее личное сообщение", "/r ", "ichat.msg");
        helpLine(sender, "/socialspy", "Показывать личные сообщения других игроков", "/socialspy", "ichat.socialspy");
        helpLine(sender, "/commandspy", "Показывать команды других игроков", "/commandspy", "ichat.commandspy");
        helpLine(sender, "/afk", "Уйти в AFK или вернуться", "/afk", "ichat.afk");
        helpLine(sender, "/chat <on|off>", "Включить или отключить общий чат", "/chat ", "ichat.chat.toggle");
        helpLine(sender, "/clearchat", "Очистить чат", "/clearchat", "ichat.clearchat");

        sendChannelHelp(sender);

        sender.sendMessage(Text.parse("<#FF7000><bold>Администрирование</bold></#FF7000>"));
        helpLine(sender, "/ichat help", "Показать этот список команд", "/ichat help", "ichat.admin");
        helpLine(sender, "/ichat reload", "Перезагрузить конфиги плагина", "/ichat reload", "ichat.admin");
        helpLine(sender, "/ichat version", "Показать версию плагина", "/ichat version", "ichat.admin");
        sender.sendMessage(Text.parse("<dark_gray>━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━</dark_gray>"));
    }

    private void sendChannelHelp(CommandSender sender) {
        boolean header = false;
        for (Channel channel : plugin.getChannelRegistry().all()) {
            if (channel.mode() != ChannelMode.COMMAND || !channel.enabled() || channel.command().isEmpty()) {
                continue;
            }
            if (!channel.permission().isEmpty() && !sender.hasPermission(channel.permission())) {
                continue;
            }
            if (!header) {
                sender.sendMessage(Text.parse("<#FF7000><bold>Каналы</bold></#FF7000>"));
                header = true;
            }
            helpLine(sender, "/" + channel.command() + " <сообщение>",
                    "Канал «" + channel.id() + "»", "/" + channel.command() + " ", channel.permission());
        }
    }

    private void helpLine(CommandSender sender, String usage, String description, String suggest, String permission) {
        if (!permission.isEmpty() && !sender.hasPermission(permission)) {
            return;
        }
        String safeUsage = usage.replace("<", "\\<").replace(">", "\\>");
        Component hover = Text.parse("<gray>" + description + "</gray>")
                .append(Component.newline())
                .append(Text.parse("<dark_gray>нажмите, чтобы вставить команду</dark_gray>"));
        Component line = Text.parse("<dark_gray>▏ </dark_gray>" + PRIMARY + safeUsage + "</#8A5CF6> <dark_gray>—</dark_gray> <gray>" + description + "</gray>")
                .hoverEvent(HoverEvent.showText(hover))
                .clickEvent(ClickEvent.suggestCommand(suggest));
        sender.sendMessage(line);
    }
}
