package de.zcreeper.lobbysystem.listener;

import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.cookieclicker.CookieClickerManager;
import de.zcreeper.lobbysystem.inventories.TeleporterInventory;
import de.zcreeper.lobbysystem.items.HotbarItems;
import de.zcreeper.lobbysystem.utils.ServerConnector;
import de.zcreeper.lobbysystem.utils.SpawnManager;
import de.zcreeper.lobbysystem.utils.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class TeleporterListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!HotbarItems.isTeleporter(event.getItem())) return;

        event.setCancelled(true);

        TeleporterInventory.open(event.getPlayer());
        Sounds.open(event.getPlayer());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof TeleporterInventory)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() != event.getInventory()) return;

        if (event.isLeftClick()) {
            switch (event.getSlot()) {
                case TeleporterInventory.SLOT_SPAWN -> teleportToSpawn(player);
                case TeleporterInventory.SLOT_GEMRUSH -> connect(player, "gemrush", "§aGemRush");
                case TeleporterInventory.SLOT_LAVABATTLE -> connect(player, "lavabattle", "§dLavaBattle");
                case TeleporterInventory.SLOT_COOKIECLICKER -> teleportToCookieClicker(player);
                default -> {
                }
            }
        }

        if (event.isRightClick()) {
            switch (event.getSlot()) {
                case TeleporterInventory.SLOT_SPAWN -> teleportToSpawn(player);
                case TeleporterInventory.SLOT_GEMRUSH -> message(player);
                case TeleporterInventory.SLOT_LAVABATTLE -> message(player);
                case TeleporterInventory.SLOT_COOKIECLICKER -> teleportToCookieClicker(player);
            }
        }
    }

    private void teleportToSpawn(Player player) {
        player.closeInventory();

        if (!SpawnManager.teleportToSpawn(player)) {
            Sounds.error(player);
            player.sendMessage(LobbySystem.getPrefix() + "§cEs wurde noch kein Spawn gesetzt.");
            return;
        }

        Sounds.teleport(player);
        player.sendMessage(LobbySystem.getPrefix() + "§aDu wurdest zum Spawn teleportiert.");
    }

    private void teleportToCookieClicker(Player player) {
        player.closeInventory();

        Location location = CookieClickerManager.getTeleportLocation();

        if (location == null) {
            Sounds.error(player);
            player.sendMessage(LobbySystem.getPrefix() + "§cEs wurde noch kein CookieClicker gesetzt.");
            return;
        }

        player.teleport(location);
        Sounds.teleport(player);
        player.sendMessage(LobbySystem.getPrefix() + "§aDu wurdest zum §6CookieClicker §ateleportiert.");
    }

    private void connect(Player player, String key, String displayName) {
        player.closeInventory();

        if (!ServerConnector.connect(player, key)) {
            Sounds.error(player);
            player.sendMessage(LobbySystem.getPrefix() + "§cDieser Server ist aktuell nicht verfügbar.");
            return;
        }

        Sounds.connect(player);
        player.sendMessage(LobbySystem.getPrefix() + "§7Du wirst zu " + displayName + " §7verbunden...");
    }

    private void message(Player player) {
        player.closeInventory();
        Sounds.error(player);
        player.sendMessage(LobbySystem.getPrefix() + "§cDiese Funktion ist noch in Arbeit.");
    }

}
