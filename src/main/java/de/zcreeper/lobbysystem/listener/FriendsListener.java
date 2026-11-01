package de.zcreeper.lobbysystem.listener;

import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.friends.FriendService;
import de.zcreeper.lobbysystem.inventories.FriendOptionsInventory;
import de.zcreeper.lobbysystem.inventories.FriendRequestsInventory;
import de.zcreeper.lobbysystem.inventories.FriendsInventory;
import de.zcreeper.lobbysystem.items.HotbarItems;
import de.zcreeper.lobbysystem.utils.Sounds;
import net.teamcastle.teamcastleapi.api.friends.Friend;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class FriendsListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!HotbarItems.isFriends(event.getItem())) return;

        event.setCancelled(true);

        Player player = event.getPlayer();

        FriendService.openOverview(player, 0);
        Sounds.open(player);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (!(holder instanceof FriendsInventory) && !(holder instanceof FriendRequestsInventory)
                && !(holder instanceof FriendOptionsInventory)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() != event.getInventory()) return;

        if (holder instanceof FriendsInventory inventory) handleOverviewClick(player, inventory, event.getSlot());
        if (holder instanceof FriendRequestsInventory inventory) handleRequestsClick(player, inventory, event);
        if (holder instanceof FriendOptionsInventory inventory) handleOptionsClick(player, inventory, event.getSlot());
    }

    // Chat-Eingabe für "Freund hinzufügen" abfangen, bevor die Nachricht im Chat landet
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!FriendService.isAwaitingName(player)) return;

        event.setCancelled(true);

        String message = event.getMessage();

        Bukkit.getScheduler().runTask(LobbySystem.getInstance(), () -> FriendService.handleNameInput(player, message));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        FriendService.handleJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        FriendService.handleQuit(event.getPlayer());
    }

    private void handleOverviewClick(Player player, FriendsInventory inventory, int slot) {
        switch (slot) {
            case FriendsInventory.SLOT_PREVIOUS -> {
                if (inventory.hasPreviousPage()) changePage(player, inventory.getPage() - 1);
            }
            case FriendsInventory.SLOT_NEXT -> {
                if (inventory.hasNextPage()) changePage(player, inventory.getPage() + 1);
            }
            case FriendsInventory.SLOT_PROFILE -> Sounds.click(player);
            case FriendsInventory.SLOT_ADD -> FriendService.requestNameInput(player);
            case FriendsInventory.SLOT_REQUESTS -> {
                Sounds.click(player);
                FriendService.openRequests(player, true);
            }
            case FriendsInventory.SLOT_OUTGOING -> {
                Sounds.click(player);
                FriendService.openRequests(player, false);
            }
            default -> {
                Friend friend = inventory.getFriend(slot);

                if (friend == null) return;

                Sounds.click(player);
                FriendService.openOptionsWithStatus(player, friend);
            }
        }
    }

    private void handleRequestsClick(Player player, FriendRequestsInventory inventory, InventoryClickEvent event) {
        if (event.getSlot() == FriendRequestsInventory.SLOT_BACK) {
            Sounds.page(player);
            FriendService.openOverview(player, 0);
            return;
        }

        UUID other = inventory.getRequest(event.getSlot());

        if (other == null) return;

        if (!inventory.isIncoming()) {
            FriendService.cancelRequest(player, other);
            return;
        }

        if (event.isLeftClick()) FriendService.accept(player, other);
        else if (event.isRightClick()) FriendService.denyRequest(player, other);
    }

    private void handleOptionsClick(Player player, FriendOptionsInventory inventory, int slot) {
        UUID friend = inventory.getFriend().uuid();

        switch (slot) {
            case FriendOptionsInventory.SLOT_BACK -> {
                Sounds.page(player);
                FriendService.openOverview(player, 0);
            }
            case FriendOptionsInventory.SLOT_TELEPORT -> {
                if (FriendService.getServer(friend) != null) {
                    FriendService.jumpTo(player, friend);
                } else {
                    Sounds.error(player);
                }
            }
            case FriendOptionsInventory.SLOT_REMOVE -> {
                if (!inventory.isConfirmRemove()) {
                    Sounds.click(player);
                    inventory.setConfirmRemove(true);
                    return;
                }

                FriendService.removeFriend(player, friend);
            }
            default -> {
            }
        }
    }

    private void changePage(Player player, int page) {
        Sounds.page(player);
        FriendService.openOverview(player, page);
    }

}
