package com.auracode.ichat.manager;

import com.auracode.ichat.IChatPlugin;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks social/command spy observers and forwards activity to them.
 */
public final class SpyService implements Listener {

    private static final Set<String> IGNORED_COMMANDS = Set.of(
            "socialspy", "ss", "commandspy", "cs", "ichat", "ic",
            "msg", "m", "tell", "whisper", "w", "r", "reply");

    private final IChatPlugin plugin;
    private final Set<UUID> socialSpies = ConcurrentHashMap.newKeySet();
    private final Set<UUID> commandSpies = ConcurrentHashMap.newKeySet();

    public SpyService(IChatPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean toggleSocialSpy(Player player) {
        return toggle(socialSpies, player.getUniqueId());
    }

    public boolean toggleCommandSpy(Player player) {
        return toggle(commandSpies, player.getUniqueId());
    }

    private boolean toggle(Set<UUID> target, UUID id) {
        if (target.remove(id)) {
            return false;
        }
        target.add(id);
        return true;
    }

    public void notifyPrivateMessage(Player sender, Player recipient, String message) {
        if (socialSpies.isEmpty()) {
            return;
        }
        String format = plugin.getCommandSettings().string("commands.socialspy.format",
                "<#8A5CF6>◆ SS</#8A5CF6> <gray>%sender% → %recipient%:</gray> <gray>%message%</gray>");
        String filled = format
                .replace("%sender%", sender.getName())
                .replace("%sender_displayname%", plain(sender))
                .replace("%recipient%", recipient.getName())
                .replace("%recipient_displayname%", plain(recipient))
                .replace("%message%", message);
        filled = plugin.getPlaceholders().apply(sender, filled);
        for (UUID id : socialSpies) {
            if (id.equals(sender.getUniqueId()) || id.equals(recipient.getUniqueId())) {
                continue;
            }
            Player spy = plugin.getServer().getPlayer(id);
            if (spy != null) {
                spy.sendMessage(plugin.getChatRenderer().parse(filled));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (commandSpies.isEmpty()) {
            return;
        }
        String raw = event.getMessage();
        if (raw == null || raw.length() < 2 || raw.charAt(0) != '/') {
            return;
        }
        String label = raw.substring(1).split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        int namespace = label.indexOf(':');
        if (namespace >= 0) {
            label = label.substring(namespace + 1);
        }
        if (IGNORED_COMMANDS.contains(label)) {
            return;
        }
        Player sender = event.getPlayer();
        String format = plugin.getCommandSettings().string("commands.commandspy.format",
                "<#8A5CF6>◆ CS</#8A5CF6> <gray>%player% →</gray> <gray>%command%</gray>");
        String filled = plugin.getPlaceholders().apply(sender,
                format.replace("%player%", sender.getName()).replace("%command%", raw));
        for (UUID id : commandSpies) {
            Player spy = plugin.getServer().getPlayer(id);
            if (spy != null) {
                spy.sendMessage(plugin.getChatRenderer().parse(filled));
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        socialSpies.remove(id);
        commandSpies.remove(id);
    }

    private String plain(Player player) {
        return PlainTextComponentSerializer.plainText().serialize(player.displayName());
    }
}
