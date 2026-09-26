package de.zcreeper.lobbysystem.utils;

import de.zcreeper.lobbysystem.LobbySystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerHider {

    private static final Set<UUID> hiding = ConcurrentHashMap.newKeySet();

    public static boolean isHiding(Player player) {
        return hiding.contains(player.getUniqueId());
    }

    public static boolean toggle(Player player) {
        if (isHiding(player)) {
            hiding.remove(player.getUniqueId());
            showAll(player);
            return false;
        }

        hiding.add(player.getUniqueId());
        hideAll(player);
        return true;
    }

    public static void handleJoin(Player joined) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(joined)) continue;

            if (isHiding(online)) online.hidePlayer(LobbySystem.getInstance(), joined);
        }
    }

    public static void handleQuit(Player player) {
        hiding.remove(player.getUniqueId());
    }

    private static void hideAll(Player player) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;

            player.hidePlayer(LobbySystem.getInstance(), online);
        }
    }

    private static void showAll(Player player) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.equals(player)) continue;

            player.showPlayer(LobbySystem.getInstance(), online);
        }
    }

}
