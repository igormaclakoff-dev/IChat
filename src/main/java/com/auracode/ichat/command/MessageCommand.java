package com.auracode.ichat.command;

import com.auracode.ichat.IChatPlugin;
import com.auracode.ichat.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Handles {@code /msg} and {@code /r}.
 */
public final class MessageCommand implements CommandExecutor {

    private final IChatPlugin plugin;
    private final Map<UUID, UUID> lastReply = new HashMap<>();

    public MessageCommand(IChatPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getChatRenderer().send(sender, "commands.msg.messages.player-only",
                    "<red>◆</red> <gray>Команду может использовать только игрок.</gray>");
            return true;
        }
        if (command.getName().equalsIgnoreCase("r")) {
            return handleReply(player, args);
        }
        return handleMessage(player, args);
    }

    private boolean handleMessage(Player sender, String[] args) {
        if (!plugin.getCommandSettings().bool("commands.msg.enabled", true)) {
            plugin.getChatRenderer().send(sender, "commands.msg.messages.disabled",
                    "<red>◆</red> <gray>Личные сообщения отключены.</gray>");
            return true;
        }
        if (!hasPermission(sender)) {
            plugin.getChatRenderer().send(sender, "commands.msg.messages.no-permission",
                    "<red>◆</red> <gray>Вы не можете использовать личные сообщения.</gray>");
            return true;
        }
        if (args.length < 2) {
            plugin.getChatRenderer().send(sender, "commands.msg.messages.usage",
                    "<red>◆</red> <gray>Использование: <white>/msg <игрок> <сообщение></white></gray>");
            return true;
        }
        Player target = findPlayer(args[0]);
        if (target == null) {
            String message = plugin.getCommandSettings().string("commands.msg.messages.player-not-found",
                    "<red>◆</red> <gray><white>%player%</white> не найден или не в сети.</gray>").replace("%player%", args[0]);
            sender.sendMessage(plugin.getChatRenderer().parse(message));
            return true;
        }
        if (target.equals(sender)) {
            plugin.getChatRenderer().send(sender, "commands.msg.messages.self",
                    "<red>◆</red> <gray>Нельзя писать самому себе.</gray>");
            return true;
        }
        sendPrivateMessage(sender, target, join(args, 1));
        return true;
    }

    private boolean handleReply(Player sender, String[] args) {
        if (!plugin.getCommandSettings().bool("commands.r.enabled", true)) {
            plugin.getChatRenderer().send(sender, "commands.r.messages.disabled",
                    "<red>◆</red> <gray>Ответы отключены.</gray>");
            return true;
        }
        if (!hasPermission(sender)) {
            plugin.getChatRenderer().send(sender, "commands.r.messages.no-permission",
                    "<red>◆</red> <gray>Вы не можете использовать личные сообщения.</gray>");
            return true;
        }
        if (args.length == 0) {
            plugin.getChatRenderer().send(sender, "commands.r.messages.usage",
                    "<red>◆</red> <gray>Использование: <white>/r <сообщение></white></gray>");
            return true;
        }
        UUID targetId = lastReply.get(sender.getUniqueId());
        if (targetId == null) {
            plugin.getChatRenderer().send(sender, "commands.r.messages.no-target",
                    "<red>◆</red> <gray>Некому отвечать.</gray>");
            return true;
        }
        Player target = Bukkit.getPlayer(targetId);
        if (target == null) {
            plugin.getChatRenderer().send(sender, "commands.r.messages.target-offline",
                    "<red>◆</red> <gray>Этот игрок сейчас не в сети.</gray>");
            return true;
        }
        sendPrivateMessage(sender, target, join(args, 0));
        return true;
    }

    private void sendPrivateMessage(Player sender, Player target, String message) {
        lastReply.put(sender.getUniqueId(), target.getUniqueId());
        lastReply.put(target.getUniqueId(), sender.getUniqueId());
        sendFormatted("commands.msg.messages.format.outgoing", sender, sender, target, message);
        sendFormatted("commands.msg.messages.format.incoming", target, sender, target, message);
        plugin.getSpyManager().notifyPrivateMessage(sender, target, message);
        if (plugin.getAfkManager().isAfk(target)) {
            String notice = plugin.getCommandSettings().string("commands.afk.messages.sender-notice",
                    "<yellow>◆</yellow> <gray><white>%player%</white> сейчас AFK.</gray>")
                    .replace("%player%", target.getName());
            sender.sendMessage(plugin.getChatRenderer().parse(plugin.getPlaceholders().apply(sender, notice)));
        }
    }

    private void sendFormatted(String path, Player recipient, Player sender, Player target, String message) {
        List<String> lines = plugin.getCommandSettings().list(path);
        if (lines.isEmpty()) {
            lines = List.of(
                    "<gray>%sender% → %recipient%</gray>",
                    "<gray>%message%</gray>");
        }
        boolean incoming = path.endsWith("incoming");
        boolean clickable = incoming
                && plugin.getCommandSettings().bool("commands.msg.notifications.click.enabled", true);
        String hover = plugin.getCommandSettings().string(
                "commands.msg.notifications.click.hover", "<gray>Нажмите, чтобы ответить</gray>");

        for (String line : lines) {
            String formatted = line
                    .replace("%sender%", sender.getName())
                    .replace("%sender_displayname%", plain(sender))
                    .replace("%recipient%", target.getName())
                    .replace("%recipient_displayname%", plain(target))
                    .replace("%message%", message);
            formatted = plugin.getPlaceholders().apply(recipient, formatted);
            Component component = plugin.getChatRenderer().parse(formatted);
            if (clickable) {
                component = component.clickEvent(ClickEvent.suggestCommand("/r "));
                if (hover != null && !hover.isEmpty()) {
                    component = component.hoverEvent(HoverEvent.showText(Text.parse(hover)));
                }
            }
            recipient.sendMessage(component);
        }
        if (incoming && plugin.getCommandSettings().bool("commands.msg.notifications.sound.enabled", true)) {
            playNotificationSound(recipient);
        }
    }

    private void playNotificationSound(Player player) {
        String name = plugin.getCommandSettings().string(
                "commands.msg.notifications.sound.name", "ENTITY_EXPERIENCE_ORB_PICKUP");
        float volume = (float) plugin.getCommandSettings().doubleValue("commands.msg.notifications.sound.volume", 1.0D);
        float pitch = (float) plugin.getCommandSettings().doubleValue("commands.msg.notifications.sound.pitch", 1.0D);
        try {
            player.playSound(player.getLocation(), Sound.valueOf(name.toUpperCase(Locale.ROOT)), volume, pitch);
        } catch (IllegalArgumentException ignored) {
            // Unknown sound name configured - silently skip.
        }
    }

    private boolean hasPermission(Player player) {
        String permission = plugin.getCommandSettings().string("commands.msg.permission", "ichat.msg");
        return permission.isEmpty() || player.hasPermission(permission);
    }

    private Player findPlayer(String name) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }

    private String join(String[] args, int start) {
        StringBuilder builder = new StringBuilder();
        for (int index = start; index < args.length; index++) {
            if (index > start) {
                builder.append(' ');
            }
            builder.append(args[index]);
        }
        return builder.toString();
    }

    private String plain(Player player) {
        return PlainTextComponentSerializer.plainText().serialize(player.displayName());
    }
}
