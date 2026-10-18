package de.zcreeper.lobbysystem.listener;

import de.zcreeper.lobbysystem.inventories.CookieClickerInventory;
import de.zcreeper.lobbysystem.inventories.CosmeticCategoryInventory;
import de.zcreeper.lobbysystem.inventories.CosmeticPurchaseInventory;
import de.zcreeper.lobbysystem.inventories.CosmeticsInventory;
import de.zcreeper.lobbysystem.inventories.FriendOptionsInventory;
import de.zcreeper.lobbysystem.inventories.FriendRequestsInventory;
import de.zcreeper.lobbysystem.inventories.FriendsInventory;
import de.zcreeper.lobbysystem.inventories.TeleporterInventory;
import de.zcreeper.lobbysystem.utils.Sounds;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public class InventorySoundListener implements Listener {

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        // Nur wenn der Spieler selbst schließt - beim Wechsel zwischen Menüs oder nach
        // einer Aktion (closeInventory) läuft schon ein eigener Sound
        if (event.getReason() != InventoryCloseEvent.Reason.PLAYER) return;
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!isLobbyInventory(event.getInventory().getHolder())) return;

        Sounds.close(player);
    }

    private boolean isLobbyInventory(InventoryHolder holder) {
        return holder instanceof TeleporterInventory
                || holder instanceof CosmeticsInventory
                || holder instanceof CosmeticCategoryInventory
                || holder instanceof CosmeticPurchaseInventory
                || holder instanceof FriendsInventory
                || holder instanceof FriendRequestsInventory
                || holder instanceof FriendOptionsInventory
                || holder instanceof CookieClickerInventory;
    }

}
