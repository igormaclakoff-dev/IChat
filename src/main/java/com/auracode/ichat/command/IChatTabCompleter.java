package com.auracode.ichat.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Tab completion for {@code /ichat}.
 */
public final class IChatTabCompleter implements TabCompleter {

    private static final List<String> OPTIONS = List.of("help", "reload", "version");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1 || !sender.hasPermission("ichat.admin")) {
            return List.of();
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> matches = new ArrayList<>();
        for (String option : OPTIONS) {
            if (option.startsWith(prefix)) {
                matches.add(option);
            }
        }
        return matches;
    }
}
