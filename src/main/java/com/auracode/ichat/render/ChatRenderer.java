package com.auracode.ichat.render;

import com.auracode.ichat.IChatPlugin;
import com.auracode.ichat.model.Channel;
import com.auracode.ichat.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Builds the final chat line: channel format, player hover/click, colored
 * message and PlaceholderAPI support.
 */
public final class ChatRenderer {

    private static final String TOKEN_PLAYER = "\uE000";
    private static final String TOKEN_DISPLAY = "\uE001";
    private static final String TOKEN_MESSAGE = "\uE002";

    private final IChatPlugin plugin;

    public ChatRenderer(IChatPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Parses a raw MiniMessage string into a component.
     *
     * @param raw raw string, may be {@code null}
     * @return the parsed component
     */
    public Component parse(String raw) {
        return Text.parse(raw);
    }

    /**
     * Renders a channel message for a sender.
     *
     * @param sender  the message author
     * @param channel the target channel
     * @param message the plain message body
     * @return the fully rendered component
     */
    public Component render(Player sender, Channel channel, String message) {
        String format = channel.format();
        if (format == null || format.isEmpty()) {
            format = "<white>%player%</white> <gray>»</gray> <white>%message%</white>";
        }
        String prepared = tokenize(format);
        prepared = plugin.getPlaceholders().apply(sender, prepared);

        Component result = parse(prepared);
        Component hover = buildHover(sender);
        boolean hoverEnabled = plugin.getSettings().hoverEnabled();
        boolean clickEnabled = plugin.getSettings().clickEnabled();
        String clickAction = plugin.getSettings().clickAction();

        result = result.replaceText(builder -> builder.matchLiteral(TOKEN_PLAYER)
                .replacement(decorate(withAfkSuffix(sender, Component.text(sender.getName())), sender, hover, hoverEnabled, clickEnabled, clickAction)));
        result = result.replaceText(builder -> builder.matchLiteral(TOKEN_DISPLAY)
                .replacement(decorate(withAfkSuffix(sender, sender.displayName()), sender, hover, hoverEnabled, clickEnabled, clickAction)));
        result = result.replaceText(builder -> builder.matchLiteral(TOKEN_MESSAGE)
                .replacement(playerMessage(sender, message)));
        return result;
    }
    public void send(CommandSender recipient, String path, String fallback) {
        String message = plugin.getCommandSettings().string(path, fallback);
        if (recipient instanceof Player player) {
            message = plugin.getPlaceholders().apply(player, message);
        }
        recipient.sendMessage(parse(message));
    }

    /**
     * Informs the sender that nobody received their message.
     *
     * @param sender  the message author
     * @param channel the channel used
     */
    public void notifyUnheard(Player sender, Channel channel) {
        if (!plugin.getSettings().unheardEnabled()) {
            return;
        }
        boolean enabled = channel.unheardMessage() == null || channel.unheardEnabled();
        if (!enabled) {
            return;
        }
        String message = channel.unheardMessage();
        if (message == null || message.isEmpty()) {
            message = plugin.getSettings().unheardMessage();
        }
        message = message.replace("%player%", sender.getName());
        message = plugin.getPlaceholders().apply(sender, message);
        sender.sendMessage(parse(message));
    }

    private Component playerMessage(Player sender, String message) {
        if (!plugin.getSettings().chatColorEnabled()) {
            return Component.text(message);
        }
        String permission = plugin.getSettings().chatColorPermission();
        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
            return Component.text(Text.stripTags(message));
        }
        return parse(message);
    }

    private Component withAfkSuffix(Player player, Component component) {
        if (!plugin.getAfkManager().isAfk(player)) {
            return component;
        }
        String suffix = plugin.getCommandSettings().string("commands.afk.status-suffix", " <gray>[AFK]</gray>");
        if (suffix == null || suffix.isEmpty()) {
            return component;
        }
        return component.append(parse(suffix));
    }

    private Component decorate(Component component, Player target, Component hover,
                               boolean hoverEnabled, boolean clickEnabled, String clickAction) {
        Component result = component;
        if (hoverEnabled) {
            result = result.hoverEvent(HoverEvent.showText(hover));
        }
        if (clickEnabled && clickAction.equalsIgnoreCase("suggest-message")) {
            result = result.clickEvent(ClickEvent.suggestCommand("/msg " + target.getName() + " "));
        }
        return result;
    }

    private Component buildHover(Player player) {
        List<String> lines = plugin.getSettings().hoverLines();
        if (lines.isEmpty()) {
            return Component.text(player.getName());
        }
        Component hover = Component.empty();
        for (int index = 0; index < lines.size(); index++) {
            String line = lines.get(index)
                    .replace("%player%", player.getName())
                    .replace("%displayname%", plain(player))
                    .replace("%display_name%", plain(player));
            hover = hover.append(parse(line));
            if (index < lines.size() - 1) {
                hover = hover.append(Component.newline());
            }
        }
        return hover;
    }

    private String plain(Player player) {
        return PlainTextComponentSerializer.plainText().serialize(player.displayName());
    }

    private String tokenize(String format) {
        return format
                .replace("%player%", TOKEN_PLAYER)
                .replace("%displayname%", TOKEN_DISPLAY)
                .replace("%display_name%", TOKEN_DISPLAY)
                .replace("%message%", TOKEN_MESSAGE);
    }
}
