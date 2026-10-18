package de.zcreeper.lobbysystem.inventories;

import de.zcreeper.lobbysystem.friends.FriendService;
import de.zcreeper.lobbysystem.utils.ItemBuilder;
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
        boolean online = FriendService.isInLobby(friend.uuid());
        String name = FriendService.getName(friend.uuid());

        inventory.setItem(SLOT_PROFILE, FriendsInventory.head(Bukkit.getOfflinePlayer(friend.uuid()),
                (online ? "§a" : "§7") + name,
                online ? "§a● In der Lobby" : "§7● Nicht in der Lobby",
                "§7Freunde seit: §e" + FriendService.formatDate(friend.since())));

        inventory.setItem(SLOT_TELEPORT, online
                ? new ItemBuilder(Material.ENDER_PEARL)
                .displayname("§bTeleportieren")
                .lore("§7Teleportiere dich zu §e" + name + "§7.", "", "§8» §eKlicke zum Teleportieren")
                .build()
                : new ItemBuilder(Material.GRAY_DYE)
                .displayname("§7Teleportieren")
                .lore("§e" + name + " §7ist gerade", "§7nicht in der Lobby.")
                .build());

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
