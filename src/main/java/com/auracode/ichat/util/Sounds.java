package com.auracode.ichat.util;

import net.kyori.adventure.sound.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * Plays configured sounds without relying on deprecated enum lookups.
 */
public final class Sounds {

    private Sounds() {
    }

    /**
     * Plays a sound identified by its Bukkit enum name, namespaced key or
     * key form.
     *
     * @param player the recipient
     * @param name   sound name or key, may be {@code null}
     * @param volume the sound volume
     * @param pitch  the sound pitch
     */
    public static void play(Player player, String name, float volume, float pitch) {
        if (name == null || name.isBlank()) {
            return;
        }
        org.bukkit.Sound matched = resolve(name.trim());
        if (matched == null) {
            return;
        }
        player.playSound(Sound.sound(matched, Sound.Source.MASTER, volume, pitch));
    }

    private static org.bukkit.Sound resolve(String name) {
        NamespacedKey key = toKey(name);
        return key == null ? null : Registry.SOUNDS.get(key);
    }

    private static NamespacedKey toKey(String name) {
        if (name.indexOf(':') >= 0) {
            return NamespacedKey.fromString(name.toLowerCase(Locale.ROOT));
        }
        if (name.indexOf('.') >= 0) {
            return NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT));
        }
        return NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT).replace('_', '.'));
    }
}
