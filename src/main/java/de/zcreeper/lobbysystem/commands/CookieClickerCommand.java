package de.zcreeper.lobbysystem.commands;

import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.cookieclicker.CookieClickerManager;
import de.zcreeper.lobbysystem.inventories.CookieClickerInventory;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CookieClickerCommand implements TabExecutor {

    private static final String PERMISSION = "lobbysystem.cookieclicker.admin";
    private static final int MAX_DISTANCE = 6;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LobbySystem.getConsole());
            return true;
        }

        if (args.length == 0) {
            CookieClickerInventory.open(player);
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(LobbySystem.getNoperms());
            return true;
        }

        if (args.length != 1 || (!args[0].equalsIgnoreCase("set") && !args[0].equalsIgnoreCase("remove"))) {
            player.sendMessage(LobbySystem.getPrefix() + "§cBenutze: §6/" + label + " <set|remove>");
            return true;
        }

        Block block = player.getTargetBlockExact(MAX_DISTANCE);

        if (block == null || block.getType().isAir()) {
            player.sendMessage(LobbySystem.getPrefix() + "§cDu musst einen Block ansehen.");
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            if (!CookieClickerManager.add(block, player.getLocation())) {
                player.sendMessage(LobbySystem.getPrefix() + "§cDieser Block ist bereits ein CookieClicker.");
                return true;
            }

            player.sendMessage(LobbySystem.getPrefix() + "§aDer Block wurde in einen §6CookieClicker §averwandelt.");
            return true;
        }

        if (!CookieClickerManager.remove(block)) {
            player.sendMessage(LobbySystem.getPrefix() + "§cDieser Block ist kein CookieClicker.");
            return true;
        }

        player.sendMessage(LobbySystem.getPrefix() + "§aDer §6CookieClicker §awurde entfernt.");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1 || !sender.hasPermission(PERMISSION)) return List.of();

        return List.of("set", "remove").stream()
                .filter(option -> option.startsWith(args[0].toLowerCase()))
                .toList();
    }

}
