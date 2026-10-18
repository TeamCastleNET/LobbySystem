package de.zcreeper.lobbysystem;

import de.zcreeper.lobbysystem.commands.CookieClickerCommand;
import de.zcreeper.lobbysystem.commands.SetSpawnCommand;
import de.zcreeper.lobbysystem.commands.CosmeticsCommand;
import de.zcreeper.lobbysystem.commands.SpawnCommand;
import de.zcreeper.lobbysystem.cookieclicker.CookieClickerManager;
import de.zcreeper.lobbysystem.cosmetics.CosmeticManager;
import de.zcreeper.lobbysystem.data.PlayerDataManager;
import de.zcreeper.lobbysystem.listener.ChatMention;
import de.zcreeper.lobbysystem.listener.CookieClickerListener;
import de.zcreeper.lobbysystem.listener.CosmeticListener;
import de.zcreeper.lobbysystem.listener.FriendsListener;
import de.zcreeper.lobbysystem.listener.InventorySoundListener;
import de.zcreeper.lobbysystem.listener.ConnectionListener;
import de.zcreeper.lobbysystem.listener.JumpPadListener;
import de.zcreeper.lobbysystem.listener.ProtectionListener;
import de.zcreeper.lobbysystem.listener.TeleporterListener;
import de.zcreeper.lobbysystem.listener.VisibilityListener;
import de.zcreeper.lobbysystem.utils.DatabaseManager;
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

        if (DatabaseManager.connect()) {
            PlayerDataManager.start();
        } else {
            getLogger().severe("Ohne Datenbank können keine Spieler joinen. Prüfe die mysql-Einstellungen in der config.yml.");
        }

        SpawnManager.loadSpawn();
        CookieClickerManager.load();
        ScoreboardManager.start();
        CosmeticManager.start();

        getServer().getMessenger().registerOutgoingPluginChannel(this, ServerConnector.CHANNEL);

        registerCommand("setspawn", new SetSpawnCommand());
        registerCommand("spawn", new SpawnCommand());
        registerCommand("cosmetics", new CosmeticsCommand());
        registerCommand("cookieclicker", new CookieClickerCommand());

        getServer().getPluginManager().registerEvents(new ConnectionListener(), this);
        getServer().getPluginManager().registerEvents(new ChatMention(), this);
        getServer().getPluginManager().registerEvents(new ProtectionListener(), this);
        getServer().getPluginManager().registerEvents(new TeleporterListener(), this);
        getServer().getPluginManager().registerEvents(new VisibilityListener(), this);
        getServer().getPluginManager().registerEvents(new JumpPadListener(), this);
        getServer().getPluginManager().registerEvents(new CosmeticListener(), this);
        getServer().getPluginManager().registerEvents(new FriendsListener(), this);
        getServer().getPluginManager().registerEvents(new CookieClickerListener(), this);
        getServer().getPluginManager().registerEvents(new InventorySoundListener(), this);
    }

    private void registerCommand(String name, CommandExecutor executor) {
        PluginCommand command = getCommand(name);

        if (command != null) command.setExecutor(executor);
    }

    @Override
    public void onDisable() {
        getLogger().info("LobbySystem wird gestoppt.");

        CosmeticManager.stop();
        PlayerDataManager.stop();
        DatabaseManager.disconnect();

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
