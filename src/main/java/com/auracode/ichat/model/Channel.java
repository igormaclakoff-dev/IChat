package com.auracode.ichat.model;

import java.util.List;

/**
 * Immutable description of a chat channel loaded from {@code config.yml}.
 *
 * @param id             unique channel id
 * @param enabled        whether the channel is active
 * @param mode           delivery mode
 * @param format         MiniMessage format of a message
 * @param aliases        command aliases for {@link ChannelMode#COMMAND}
 * @param trigger        in-chat trigger prefix, may be empty
 * @param command        primary command for {@link ChannelMode#COMMAND}
 * @param permission     required permission, may be empty
 * @param radius         local radius in blocks
 * @param unheardEnabled per-channel "nobody heard you" override
 * @param unheardMessage per-channel override message, {@code null} to use global
 */
public record Channel(String id,
                      boolean enabled,
                      ChannelMode mode,
                      String format,
                      List<String> aliases,
                      String trigger,
                      String command,
                      String permission,
                      int radius,
                      boolean unheardEnabled,
                      String unheardMessage) {

    public boolean hasPermission() {
        return permission != null && !permission.isEmpty();
    }

    public boolean hasTrigger() {
        return trigger != null && !trigger.isEmpty();
    }

    public boolean hasCommand() {
        return command != null && !command.isEmpty();
    }

    public boolean isLocal() {
        return mode == ChannelMode.LOCAL;
    }

    public boolean isGlobal() {
        return mode == ChannelMode.GLOBAL;
    }

    public boolean isCommand() {
        return mode == ChannelMode.COMMAND;
    }
}
