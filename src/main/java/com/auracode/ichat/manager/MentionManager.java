package com.auracode.ichat.manager;

import com.auracode.ichat.IChatPlugin;
import com.auracode.ichat.model.Channel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Highlights {@code @name} mentions and notifies the mentioned players.
 */
public final class MentionManager {

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([A-Za-z0-9_]{1,16})");

    private final IChatPlugin plugin;

    public MentionManager(IChatPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Replaces every online player mention inside the component with a
     * highlighted, clickable variant.
     *
     * @param message source component
     * @return the processed component
     */
    public Component colorize(Component message) {
        if (!plugin.getSettings().mentionsEnabled()) {
            return message;
        }
        List<Player> online = new ArrayList<>(Bukkit.getOnlinePlayers());
        online.sort((a, b) -> Integer.compare(b.getName().length(), a.getName().length()));
        Component result = message;
        for (Player target : online) {
            String mention = "@" + target.getName();
            String template = plugin.getSettings().mentionFormat().replace("%player%", target.getName());
            Component mentionComponent = plugin.getChatRenderer().parse(template)
                    .hoverEvent(HoverEvent.showText(Component.text(target.getName())))
                    .clickEvent(ClickEvent.suggestCommand("/msg " + target.getName() + " "));
            if (plugin.getAfkManager().isAfk(target)) {
                String suffix = plugin.getCommandSettings().string("commands.afk.status-suffix", " <gray>[AFK]</gray>");
                if (suffix != null && !suffix.isEmpty()) {
                    mentionComponent = mentionComponent.append(plugin.getChatRenderer().parse(suffix));
                }
            }
            final Component replacement = mentionComponent;
            result = result.replaceText(builder -> builder.matchLiteral(mention).replacement(replacement));
        }
        return result;
    }

    /**
     * Notifies mentioned players and sends offline/not-found feedback to the
     * sender. Runs on the main thread because it may query offline players.
     *
     * @param plainMessage the plain message text
     * @param sender       the message author
     * @param channel      the channel the message was sent to
     */
    public void notifyMentions(String plainMessage, Player sender, Channel channel) {
        if (!plugin.getSettings().mentionsEnabled() || !plugin.getSettings().mentionNotificationsEnabled()) {
            return;
        }
        Set<String> names = findMentions(plainMessage);
        if (names.isEmpty()) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            for (String name : names) {
                Player online = findOnline(name);
                if (online != null) {
                    if (!online.equals(sender) && plugin.getChannelRouter().canReceive(sender, online, channel)) {
                        notifyMentioned(online, sender);
                    }
                    continue;
                }
                OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
                if (offline.hasPlayedBefore()) {
                    sendStatus(sender, plugin.getSettings().mentionOfflineMessage(), name);
                } else {
                    sendStatus(sender, plugin.getSettings().mentionNotFoundMessage(), name);
                }
            }
        });
    }

    private Set<String> findMentions(String message) {
        Set<String> names = new LinkedHashSet<>();
        if (message == null || message.isEmpty()) {
            return names;
        }
        Matcher matcher = MENTION_PATTERN.matcher(message);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    private Player findOnline(String name) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().equalsIgnoreCase(name)) {
                return player;
            }
        }
        return null;
    }

    private void notifyMentioned(Player target, Player sender) {
        String message = plugin.getSettings().mentionNotificationMessage().replace("%player%", sender.getName());
        message = plugin.getPlaceholders().apply(sender, message);
        target.sendMessage(plugin.getChatRenderer().parse(message));
        playMentionSound(target);
    }

    private void sendStatus(Player sender, String template, String name) {
        String message = template.replace("%player%", name);
        sender.sendMessage(plugin.getChatRenderer().parse(message));
    }

    private void playMentionSound(Player player) {
        if (!plugin.getSettings().mentionSoundEnabled()) {
            return;
        }
        String name = plugin.getSettings().mentionSoundName();
        float volume = plugin.getSettings().mentionSoundVolume();
        float pitch = plugin.getSettings().mentionSoundPitch();
        try {
            player.playSound(player.getLocation(), Sound.valueOf(name.toUpperCase(Locale.ROOT)), volume, pitch);
        } catch (IllegalArgumentException ignored) {
            // Unknown sound name configured - silently skip.
        }
    }
}
