package de.zcreeper.lobbysystem;

import de.zcreeper.lobbysystem.commands.SetSpawnCommand;
import de.zcreeper.lobbysystem.commands.SpawnCommand;
import de.zcreeper.lobbysystem.listener.ChatMention;
import de.zcreeper.lobbysystem.listener.ConnectionListener;
import de.zcreeper.lobbysystem.listener.JumpPadListener;
import de.zcreeper.lobbysystem.listener.ProtectionListener;
import de.zcreeper.lobbysystem.listener.TeleporterListener;
import de.zcreeper.lobbysystem.listener.VisibilityListener;
import de.zcreeper.lobbysystem.utils.ScoreboardManager;
import de.zcreeper.lobbysystem.utils.ServerConnector;
import de.zcreeper.lobbysystem.utils.SpawnManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class LobbySystem extends JavaPlugin {

    public static LobbySystem instance;

    public static String prefix = "§6Team§bCastle §8| ";
    public static String noperms = prefix + "§cDazu hast du keine Rechte.";
    public static String console = prefix + "§cDazu musst du ein Spieler sein.";

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("LobbySystem wird gestartet.");

        saveDefaultConfig();

        SpawnManager.loadSpawn();
        ScoreboardManager.start();

        getServer().getMessenger().registerOutgoingPluginChannel(this, ServerConnector.CHANNEL);

        registerCommand("setspawn", new SetSpawnCommand());
        registerCommand("spawn", new SpawnCommand());

        getServer().getPluginManager().registerEvents(new ConnectionListener(), this);
        getServer().getPluginManager().registerEvents(new ChatMention(), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new TeleporterListener(), this);
        getServer().getPluginManager().registerEvents(new VisibilityListener(), this);
        getServer().getPluginManager().registerEvents(new JumpPadListener(), this);
    }

    private void registerCommand(String name, CommandExecutor executor) {
        PluginCommand command = getCommand(name);

        if (command != null) command.setExecutor(executor);
    }

    @Override
    public void onDisable() {
        getLogger().info("LobbySystem wird gestoppt.");

    }

    public static LobbySystem getInstance() {
        return instance;
    }

    public static String getPrefix() {
        return prefix;
    }

    public static String getNoperms() {
        return noperms;
    }

    public static String getConsole() {
        return console;
    }
}
