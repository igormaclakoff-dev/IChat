package com.auracode.ichat.listener;

import com.auracode.ichat.IChatPlugin;
import com.auracode.ichat.model.Channel;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Captures player chat, resolves the channel and rewrites the message. The
 * asynchronous event is cancelled and delivery is deferred to the main thread
 * so that PlaceholderAPI and other Bukkit calls stay thread-safe.
 */
public final class ChatMessageListener implements Listener {

    private final IChatPlugin plugin;

    public ChatMessageListener(IChatPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        event.setCancelled(true);
        String plain = PlainTextComponentSerializer.plainText().serialize(event.message());
        plugin.getServer().getScheduler().runTask(plugin, () -> deliver(sender, plain));
    }

    private void deliver(Player sender, String plain) {
        if (!plugin.getChatManager().isEnabled() && !plugin.getChatManager().canBypass(sender)) {
            plugin.getChatRenderer().send(sender, "commands.chat.messages.blocked",
                    "<red>◆</red> <gray>Общий чат сейчас отключён.</gray>");
            return;
        }
        Channel channel = plugin.getChannelRouter().channelFor(sender, plain);
        if (channel == null) {
            return;
        }
        if (!plugin.getChannelRouter().hasAccess(sender, channel)) {
            plugin.getChatRenderer().send(sender, "commands.channel.messages.no-permission",
                    "<red>◆</red> <gray>Вы не можете использовать этот канал.</gray>");
            return;
        }
        String content = plugin.getChannelRouter().stripTrigger(channel, plain);
        if (content.isEmpty()) {
            return;
        }
        plugin.getChannelDispatcher().dispatch(sender, channel, content);
    }
}
