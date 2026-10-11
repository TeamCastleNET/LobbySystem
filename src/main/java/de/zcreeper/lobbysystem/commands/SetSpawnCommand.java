package de.zcreeper.lobbysystem.commands;

import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.utils.SpawnManager;
import de.zcreeper.lobbysystem.utils.Sounds;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetSpawnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LobbySystem.getConsole());
            return true;
        }

        if (!player.hasPermission("lobbysystem.setspawn")) {
            Sounds.error(player);
            player.sendMessage(LobbySystem.getNoperms());
            return true;
        }

        if (args.length != 0) {
            Sounds.error(player);
            player.sendMessage(LobbySystem.getPrefix() + "§cBenutze: §6/" + label);
            return true;
        }

        SpawnManager.setSpawn(player.getLocation());
        Sounds.success(player);
        player.sendMessage(LobbySystem.getPrefix() + "§aDer Spawn wurde auf deine aktuelle Position gesetzt.");
        return true;
    }

}
