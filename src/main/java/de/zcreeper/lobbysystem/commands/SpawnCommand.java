package de.zcreeper.lobbysystem.commands;

import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.utils.SpawnManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpawnCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LobbySystem.getConsole());
            return true;
        }

        if (!SpawnManager.teleportToSpawn(player)) {
            player.sendMessage(LobbySystem.getPrefix() + "§cEs wurde noch kein Spawn gesetzt.");
            return true;
        }

        player.sendMessage(LobbySystem.getPrefix() + "§aDu wurdest zum Spawn teleportiert.");
        return true;
    }

}
