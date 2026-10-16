package com.auracode.ichat.model;

import java.util.Locale;

/**
 * Delivery mode of a chat channel.
 */
public enum ChannelMode {

    /** Only players within {@code radius} receive the message. */
    LOCAL,
    /** Every eligible player receives the message. */
    GLOBAL,
    /** The channel is invoked through its own command or alias. */
    COMMAND;

    public static ChannelMode from(String value) {
        if (value == null) {
            return LOCAL;
        }
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "global" -> GLOBAL;
            case "command" -> COMMAND;
            default -> LOCAL;
        };
    }
}
