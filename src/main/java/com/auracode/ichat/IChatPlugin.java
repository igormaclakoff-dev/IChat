package com.auracode.ichat;

import com.auracode.ichat.command.AfkToggleCommand;
import com.auracode.ichat.command.ChatToggleCommand;
import com.auracode.ichat.command.ChatClearCommand;
import com.auracode.ichat.command.IChatCommand;
import com.auracode.ichat.command.IChatTabCompleter;
import com.auracode.ichat.command.MessageCommand;
import com.auracode.ichat.command.MessageTabCompleter;
import com.auracode.ichat.command.SpyToggleCommand;
import com.auracode.ichat.config.CommandOptions;
import com.auracode.ichat.config.PluginConfig;
import com.auracode.ichat.hook.IChatExpansion;
import com.auracode.ichat.listener.ChannelCommandListener;
import com.auracode.ichat.listener.ChannelTabListener;
import com.auracode.ichat.listener.ChatMessageListener;
import com.auracode.ichat.manager.AfkService;
import com.auracode.ichat.manager.ChannelDispatcher;
import com.auracode.ichat.manager.ChannelRegistry;
import com.auracode.ichat.manager.ChannelRouter;
import com.auracode.ichat.manager.ChatState;
import com.auracode.ichat.manager.CommandGuard;
import com.auracode.ichat.manager.MentionManager;
import com.auracode.ichat.manager.SpyService;
import com.auracode.ichat.render.ChatRenderer;
import com.auracode.ichat.util.Placeholders;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * IChat plugin entry point. Wires managers, listeners and commands together.
 */
public final class IChatPlugin extends JavaPlugin {

    private PluginConfig settings;
    private CommandOptions commandSettings;
    private Placeholders placeholders;
    private ChatRenderer chatRenderer;
    private ChannelRegistry channelRegistry;
    private ChannelRouter channelRouter;
    private ChannelDispatcher channelDispatcher;
    private ChatState chatManager;
    private SpyService spyManager;
    private AfkService afkManager;
    private MentionManager mentionManager;
    private CommandGuard commandControlManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.settings = new PluginConfig(this);
        this.commandSettings = new CommandOptions(this);
        this.placeholders = new Placeholders();
        this.chatRenderer = new ChatRenderer(this);
        this.channelRegistry = new ChannelRegistry(this);
        this.channelRouter = new ChannelRouter(this);
        this.channelDispatcher = new ChannelDispatcher(this);
        this.chatManager = new ChatState(this);
        this.spyManager = new SpyService(this);
        this.afkManager = new AfkService(this);
        this.mentionManager = new MentionManager(this);
        this.commandControlManager = new CommandGuard(this);

        registerCommands();
        registerListeners();
        registerPlaceholders();

        getLogger().info(placeholders.isAvailable()
                ? "IChat включён — интеграция с PlaceholderAPI активна."
                : "IChat включён — работа без PlaceholderAPI.");
    }

    @Override
    public void onDisable() {
        getLogger().info("IChat выключен.");
    }

    /**
     * Reloads config.yml, commands.yml and the channel registry.
     */
    public void reloadAll() {
        settings.reload();
        channelRegistry.load();
        commandSettings.reload();
    }

    private void registerCommands() {
        register("ichat", new IChatCommand(this), new IChatTabCompleter());
        MessageCommand privateMessages = new MessageCommand(this);
        register("msg", privateMessages, new MessageTabCompleter());
        register("r", privateMessages, null);
        register("socialspy", new SpyToggleCommand(this, true), null);
        register("commandspy", new SpyToggleCommand(this, false), null);
        register("clearchat", new ChatClearCommand(this), null);
        register("afk", new AfkToggleCommand(this), null);
        register("chat", new ChatToggleCommand(this), null);
    }

    private void register(String name, CommandExecutor executor, TabCompleter completer) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().warning("Команда '" + name + "' не объявлена в plugin.yml.");
            return;
        }
        command.setExecutor(executor);
        if (completer != null) {
            command.setTabCompleter(completer);
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ChatMessageListener(this), this);
        getServer().getPluginManager().registerEvents(new ChannelCommandListener(this), this);
        getServer().getPluginManager().registerEvents(new ChannelTabListener(this), this);
        getServer().getPluginManager().registerEvents(spyManager, this);
        getServer().getPluginManager().registerEvents(afkManager, this);
        getServer().getPluginManager().registerEvents(commandControlManager, this);
    }

    private void registerPlaceholders() {
        if (placeholders.isAvailable()) {
            new IChatExpansion(this).register();
        }
    }

    public PluginConfig getSettings() {
        return settings;
    }

    public CommandOptions getCommandSettings() {
        return commandSettings;
    }

    public Placeholders getPlaceholders() {
        return placeholders;
    }

    public ChatRenderer getChatRenderer() {
        return chatRenderer;
    }

    public ChannelRegistry getChannelRegistry() {
        return channelRegistry;
    }

    public ChannelRouter getChannelRouter() {
        return channelRouter;
    }

    public ChannelDispatcher getChannelDispatcher() {
        return channelDispatcher;
    }

    public ChatState getChatManager() {
        return chatManager;
    }

    public SpyService getSpyManager() {
        return spyManager;
    }

    public AfkService getAfkManager() {
        return afkManager;
    }

    public MentionManager getMentionManager() {
        return mentionManager;
    }

    public CommandGuard getCommandControlManager() {
        return commandControlManager;
    }
}
