package de.zcreeper.lobbysystem.listener;

import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.inventories.FriendsInventory;
import de.zcreeper.lobbysystem.items.HotbarItems;
import de.zcreeper.lobbysystem.utils.Sounds;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class FriendsListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!HotbarItems.isFriends(event.getItem())) return;

        event.setCancelled(true);

        Player player = event.getPlayer();

        FriendsInventory.open(player);
        Sounds.open(player);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof FriendsInventory inventory)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() != event.getInventory()) return;

        switch (event.getSlot()) {
            case FriendsInventory.SLOT_PREVIOUS -> {
                if (inventory.hasPreviousPage()) changePage(player, inventory.getPage() - 1);
            }
            case FriendsInventory.SLOT_NEXT -> {
                if (inventory.hasNextPage()) changePage(player, inventory.getPage() + 1);
            }
            case FriendsInventory.SLOT_PROFILE -> Sounds.click(player);
            case FriendsInventory.SLOT_ADD, FriendsInventory.SLOT_REQUESTS, FriendsInventory.SLOT_SETTINGS -> message(player);
            default -> {
                if (inventory.getFriend(event.getSlot()) != null) message(player);
            }
        }
    }

    private void changePage(Player player, int page) {
        FriendsInventory.open(player, page);
        Sounds.page(player);
    }

    private void message(Player player) {
        player.closeInventory();
        Sounds.error(player);
        player.sendMessage(LobbySystem.getPrefix() + "§cDas FreundeSystem ist noch in Arbeit.");
    }

}
