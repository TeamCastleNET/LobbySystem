package de.zcreeper.lobbysystem.inventories;

import de.zcreeper.lobbysystem.friends.FriendService;
import de.zcreeper.lobbysystem.utils.ItemBuilder;
import de.zcreeper.lobbysystem.utils.ServerConnector;
import net.teamcastle.teamcastleapi.api.friends.Friend;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FriendOptionsInventory implements InventoryHolder {

    public static final int SIZE = 45;

    public static final int SLOT_TELEPORT = 20;
    public static final int SLOT_PROFILE = 22;
    public static final int SLOT_REMOVE = 24;
    public static final int SLOT_BACK = 40;

    private static final int[] BUTTON_SLOTS = {0, 8, 36, 44};

    private static final int[] BLACK_SLOTS = {
            1, 2, 3, 4, 5, 6, 7,
            9, 17,
            18, 26,
            27, 35,
            37, 38, 39, 41, 42, 43
    };

    private static final int[] GRAY_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 21, 23, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private final Friend friend;
    private final Inventory inventory;

    // Entfernen braucht einen zweiten Klick zur Bestätigung
    private boolean confirmRemove;

    public FriendOptionsInventory(Friend friend) {
        this.friend = friend;
        this.inventory = Bukkit.createInventory(this, SIZE, "§6Freunde §8» §e" + FriendService.getName(friend.uuid()));

        fill(BUTTON_SLOTS, placeholder(Material.STONE_BUTTON));
        fill(BLACK_SLOTS, placeholder(Material.BLACK_STAINED_GLASS_PANE));
        fill(GRAY_SLOTS, placeholder(Material.GRAY_STAINED_GLASS_PANE));

        render();
    }

    public static void open(Player player, Friend friend) {
        player.openInventory(new FriendOptionsInventory(friend).getInventory());
    }

    public void render() {
        String name = FriendService.getName(friend.uuid());
        String server = FriendService.getServer(friend.uuid());

        inventory.setItem(SLOT_PROFILE, FriendsInventory.head(friend.uuid(),
                FriendService.getNameColor(friend.uuid()) + name,
                FriendService.getStatusLine(friend.uuid()),
                "§7Freunde seit: §e" + FriendService.formatDate(friend.since())));

        ItemStack teleport;

        if (server != null) {
            teleport = new ItemBuilder(Material.ENDER_PEARL)
                    .displayname("§bNachspringen")
                    .lore("§7Verbinde dich mit " + ServerConnector.getDisplayName(server), "§7und spiele mit §e" + name + "§7.",
                            "", "§8» §eKlicke zum Verbinden")
                    .build();
        } else if (FriendService.isInLobby(friend.uuid())) {
            teleport = new ItemBuilder(Material.GRAY_DYE)
                    .displayname("§7Nachspringen")
                    .lore("§e" + name + " §7ist auch in der Lobby.")
                    .build();
        } else {
            teleport = new ItemBuilder(Material.GRAY_DYE)
                    .displayname("§7Nachspringen")
                    .lore("§e" + name + " §7ist gerade offline.")
                    .build();
        }

        inventory.setItem(SLOT_TELEPORT, teleport);

        inventory.setItem(SLOT_REMOVE, confirmRemove
                ? new ItemBuilder(Material.TNT)
                .displayname("§c§lWirklich entfernen?")
                .lore("§7Ihr seid danach keine", "§7Freunde mehr.", "", "§8» §cKlicke erneut zum Bestätigen")
                .build()
                : new ItemBuilder(Material.RED_DYE)
                .displayname("§cFreund entfernen")
                .lore("§7Entferne §e" + name + " §7aus", "§7deiner Freundesliste.", "", "§8» §cKlicke zum Entfernen")
                .build());

        inventory.setItem(SLOT_BACK, new ItemBuilder(Material.ARROW).displayname("§7Zurück").build());
    }

    public Friend getFriend() {
        return friend;
    }

    public boolean isConfirmRemove() {
        return confirmRemove;
    }

    public void setConfirmRemove(boolean confirmRemove) {
        this.confirmRemove = confirmRemove;
        render();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    private void fill(int[] slots, ItemStack item) {
        for (int slot : slots) {
            inventory.setItem(slot, item.clone());
        }
    }

    private ItemStack placeholder(Material material) {
        return new ItemBuilder(material).displayname("§8").build();
    }

}
