package de.zcreeper.lobbysystem.utils;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RankProvider {

    private static final String DEFAULT_RANK = "§7Spieler";

    private static Boolean available;

    public static String getRank(Player player) {
        if (!isAvailable()) return DEFAULT_RANK;

        String prefix = LuckPermsHook.getPrefix(player);

        if (prefix == null) return DEFAULT_RANK;

        // "&4Admin &8| " -> "&4Admin"
        String rank = ChatColor.translateAlternateColorCodes('&', prefix.replace("|", "")).trim();

        while (rank.endsWith("§")) rank = rank.substring(0, rank.length() - 1).trim();
        while (rank.length() >= 2 && rank.charAt(rank.length() - 2) == '§') rank = rank.substring(0, rank.length() - 2).trim();

        return ChatColor.stripColor(rank).isBlank() ? DEFAULT_RANK : rank;
    }

    private static boolean isAvailable() {
        if (available == null) available = Bukkit.getPluginManager().isPluginEnabled("LuckPerms");

        return available;
    }

    /*
    Eigene Klasse, damit die LuckPerms-Klassen nur geladen werden, wenn LuckPerms auch installiert ist.
     */
    private static class LuckPermsHook {

        private static String getPrefix(Player player) {
            LuckPerms luckPerms = LuckPermsProvider.get();
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());

            return user == null ? null : user.getCachedData().getMetaData().getPrefix();
        }

    }

}
