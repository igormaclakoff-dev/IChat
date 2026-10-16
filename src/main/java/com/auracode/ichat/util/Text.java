package com.auracode.ichat.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * MiniMessage helpers shared across the plugin.
 */
public final class Text {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().strict(false).build();

    private Text() {
    }

    /**
     * Parses a MiniMessage string, translating legacy ampersand codes first.
     *
     * @param input raw format string, may be {@code null}
     * @return the parsed component, never {@code null}
     */
    public static Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(legacy(input));
    }

    /**
     * Removes every MiniMessage tag, leaving only plain text.
     *
     * @param input raw string, may be {@code null}
     * @return the stripped string, never {@code null}
     */
    public static String stripTags(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return MINI_MESSAGE.stripTags(input);
    }

    /**
     * Translates legacy ampersand color codes into MiniMessage tags.
     *
     * @param input raw string, may be {@code null}
     * @return the translated string, never {@code null}
     */
    public static String legacy(String input) {
        if (input == null) {
            return "";
        }
        if (input.indexOf('&') < 0) {
            return input;
        }
        StringBuilder builder = new StringBuilder(input.length());
        for (int index = 0; index < input.length(); index++) {
            char current = input.charAt(index);
            if (current == '&' && index + 1 < input.length()) {
                String tag = tagFor(Character.toLowerCase(input.charAt(index + 1)));
                if (tag != null) {
                    builder.append('<').append(tag).append('>');
                    index++;
                    continue;
                }
            }
            builder.append(current);
        }
        return builder.toString();
    }

    private static String tagFor(char code) {
        return switch (code) {
            case '0' -> "black";
            case '1' -> "dark_blue";
            case '2' -> "dark_green";
            case '3' -> "dark_aqua";
            case '4' -> "dark_red";
            case '5' -> "dark_purple";
            case '6' -> "gold";
            case '7' -> "gray";
            case '8' -> "dark_gray";
            case '9' -> "blue";
            case 'a' -> "green";
            case 'b' -> "aqua";
            case 'c' -> "red";
            case 'd' -> "light_purple";
            case 'e' -> "yellow";
            case 'f' -> "white";
            case 'k' -> "obfuscated";
            case 'l' -> "bold";
            case 'm' -> "strikethrough";
            case 'n' -> "underlined";
            case 'o' -> "italic";
            case 'r' -> "reset";
            default -> null;
        };
    }
}
