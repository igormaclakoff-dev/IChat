package com.auracode.ichat.listener;

import com.auracode.ichat.IChatPlugin;
import com.auracode.ichat.model.Channel;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Captures player chat, resolves the channel and rewrites the message.
 */
public final class ChatMessageListener implements Listener {

    private final IChatPlugin plugin;

    public ChatMessageListener(IChatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        if (!plugin.getChatManager().isEnabled() && !plugin.getChatManager().canBypass(sender)) {
            event.setCancelled(true);
            plugin.getChatRenderer().send(sender, "commands.chat.messages.blocked",
                    "<red>◆</red> <gray>Глобальный чат сейчас отключён.</gray>");
            return;
        }

        String plain = PlainTextComponentSerializer.plainText().serialize(event.message());
        Channel channel = plugin.getChannelRouter().channelFor(sender, plain);
        if (channel == null) {
            event.setCancelled(true);
            return;
        }
        if (!plugin.getChannelRouter().hasAccess(sender, channel)) {
            event.setCancelled(true);
            plugin.getChatRenderer().send(sender, "commands.channel.messages.no-permission",
                    "<red>◆</red> <gray>Вы не можете использовать этот канал.</gray>");
            return;
        }

        String content = plugin.getChannelRouter().stripTrigger(channel, plain);
        if (content.isEmpty()) {
            event.setCancelled(true);
            return;
        }

        Component rendered = plugin.getChatRenderer().render(sender, channel, content);
        Component finalFormatted = plugin.getMentionManager().colorize(rendered);

        event.viewers().removeIf(audience -> audience instanceof Player viewer
                && !plugin.getChannelRouter().canReceive(sender, viewer, channel));

        plugin.getMentionManager().notifyMentions(plain, sender, channel);

        boolean heard = event.viewers().stream()
                .anyMatch(audience -> audience instanceof Player viewer && !viewer.equals(sender));
        if (!heard) {
            plugin.getChatRenderer().notifyUnheard(sender, channel);
        }

        event.renderer((source, sourceDisplayName, messageComponent, viewer) -> finalFormatted);
    }
}
