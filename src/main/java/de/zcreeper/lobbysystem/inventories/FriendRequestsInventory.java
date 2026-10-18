package de.zcreeper.lobbysystem.inventories;

import de.zcreeper.lobbysystem.friends.FriendService;
import de.zcreeper.lobbysystem.utils.ItemBuilder;
import net.teamcastle.teamcastleapi.api.friends.FriendRequest;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FriendRequestsInventory implements InventoryHolder {

    public static final int SIZE = 54;

    public static final int SLOT_BACK = 45;

    private static final int SLOT_EMPTY = 22;

    private static final int[] REQUEST_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final Inventory inventory;
    private final boolean incoming;
    // Slot -> der andere Spieler der Anfrage (Absender bei eingehenden, Empfänger bei gesendeten)
    private final Map<Integer, UUID> requests = new HashMap<>();

    public FriendRequestsInventory(List<FriendRequest> list, boolean incoming) {
        this.incoming = incoming;
        this.inventory = Bukkit.createInventory(this, SIZE, "§6Freunde §8» " + (incoming ? "§eAnfragen" : "§7Gesendete Anfragen"));

        ItemStack black = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).displayname("§8").build();

        for (int slot = 0; slot < SIZE; slot++) {
            if (slot < 9 || slot >= 45 || slot % 9 == 0 || slot % 9 == 8) inventory.setItem(slot, black.clone());
        }

        if (list.isEmpty()) {
            inventory.setItem(SLOT_EMPTY, new ItemBuilder(Material.SKELETON_SKULL)
                    .displayname(incoming ? "§cKeine offenen Anfragen" : "§cKeine gesendeten Anfragen")
                    .build());
        }

        for (int i = 0; i < list.size() && i < REQUEST_SLOTS.length; i++) {
            FriendRequest request = list.get(i);
            UUID other = incoming ? request.sender() : request.target();

            requests.put(REQUEST_SLOTS[i], other);
            inventory.setItem(REQUEST_SLOTS[i], incoming
                    ? FriendsInventory.head(Bukkit.getOfflinePlayer(other), "§e" + FriendService.getName(other),
                    "§7Gesendet am: §e" + FriendService.formatDate(request.createdAt()),
                    "",
                    "§8» §aLinksklick §7zum Annehmen",
                    "§8» §cRechtsklick §7zum Ablehnen")
                    : FriendsInventory.head(Bukkit.getOfflinePlayer(other), "§7" + FriendService.getName(other),
                    "§7Gesendet am: §e" + FriendService.formatDate(request.createdAt()),
                    "",
                    "§8» §cKlicke §7zum Zurückziehen"));
        }

        if (list.size() > REQUEST_SLOTS.length) {
            inventory.setItem(49, new ItemBuilder(Material.PAPER)
                    .displayname("§7+" + (list.size() - REQUEST_SLOTS.length) + " weitere")
                    .lore("§7Bearbeite zuerst diese Anfragen.")
                    .build());
        }

        inventory.setItem(SLOT_BACK, new ItemBuilder(Material.ARROW).displayname("§7Zurück").build());
    }

    public static void open(Player player, List<FriendRequest> list, boolean incoming) {
        player.openInventory(new FriendRequestsInventory(list, incoming).getInventory());
    }

    public UUID getRequest(int slot) {
        return requests.get(slot);
    }

    public boolean isIncoming() {
        return incoming;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

}
