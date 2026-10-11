package de.zcreeper.lobbysystem.commands;

import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.inventories.CosmeticsInventory;
import de.zcreeper.lobbysystem.utils.Sounds;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CosmeticsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LobbySystem.getConsole());
            return true;
        }

        CosmeticsInventory.open(player);
        Sounds.open(player);
        return true;
    }

}
