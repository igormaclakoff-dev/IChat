package com.auracode.ichat.manager;

import com.auracode.ichat.IChatPlugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks AFK players. Any movement or chat activity clears the AFK flag.
 */
public final class AfkService implements Listener {

    private final IChatPlugin plugin;
    private final Set<UUID> afk = ConcurrentHashMap.newKeySet();

    public AfkService(IChatPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean toggle(Player player) {
        UUID id = player.getUniqueId();
        if (afk.remove(id)) {
            return false;
        }
        afk.add(id);
        return true;
    }

    public boolean isAfk(Player player) {
        return afk.contains(player.getUniqueId());
    }

    public void remove(Player player) {
        afk.remove(player.getUniqueId());
    }

    private void becomeActive(Player player) {
        if (!afk.remove(player.getUniqueId())) {
            return;
        }
        Runnable notice = () -> plugin.getChatRenderer().send(player, "commands.afk.messages.disabled",
                "<#8A5CF6>◆</#8A5CF6> <gray>Вы снова активны.</gray>");
        if (Bukkit.isPrimaryThread()) {
            notice.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, notice);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.getFrom().toVector().equals(event.getTo().toVector())) {
            becomeActive(event.getPlayer());
        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        becomeActive(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        remove(event.getPlayer());
    }
}
