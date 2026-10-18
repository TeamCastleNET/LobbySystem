package de.zcreeper.lobbysystem.listener;

import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.cosmetics.CosmeticManager;
import de.zcreeper.lobbysystem.data.PlayerDataManager;
import de.zcreeper.lobbysystem.items.HotbarItems;
import de.zcreeper.lobbysystem.utils.PlayerHider;
import de.zcreeper.lobbysystem.utils.ScoreboardManager;
import de.zcreeper.lobbysystem.utils.SpawnManager;
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        PlayerDataManager.handlePreLogin(event);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLoginResult(AsyncPlayerPreLoginEvent event) {
        PlayerDataManager.handleLoginResult(event);
    }

    @EventHandler
    public void onSpawnLocation(AsyncPlayerSpawnLocationEvent event) {
        Location spawn = SpawnManager.getSpawn();

        if (spawn != null) event.setSpawnLocation(spawn);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        event.setJoinMessage(LobbySystem.getPrefix() + "§6" + player.getName() + " §ahat das Netzwerk betreten.");

        player.setLevel(0);
        player.setExp(0F);
        player.setFoodLevel(20);
        player.setMaxHealth(6);
        player.setHealth(6);
        player.setLevel(2026);

        PlayerHider.handleJoin(player);
        ScoreboardManager.create(player);

        PlayerDataManager.handleJoin(player);

        HotbarItems.getItems(player);
        CosmeticManager.load(player);

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        event.setQuitMessage(LobbySystem.getPrefix() + "§6" + player.getName() + " §chat das Netzwerk verlassen.");
        player.getInventory().clear();
        PlayerHider.handleQuit(player);
        ScoreboardManager.remove(player);
        CosmeticManager.handleQuit(player);
        PlayerDataManager.handleQuit(player);
    }

}
