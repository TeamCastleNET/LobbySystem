package de.zcreeper.lobbysystem.inventories;

import de.zcreeper.lobbysystem.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsInventory implements InventoryHolder {

    public static final String TITLE = "§6Freunde";

    public static final int SIZE = 54;

    public static final int SLOT_PROFILE = 4;
    public static final int SLOT_ADD = 38;
    public static final int SLOT_REQUESTS = 40;
    public static final int SLOT_SETTINGS = 42;
    public static final int SLOT_PREVIOUS = 48;
    public static final int SLOT_NEXT = 50;

    private static final int SLOT_EMPTY = 22;

    private static final int[] FRIEND_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private static final int[] BUTTON_SLOTS = {0, 8, 45, 53};

    private static final int[] BLACK_SLOTS = {
            1, 2, 3, 5, 6, 7,
            9, 17,
            18, 26,
            27, 35,
            36, 44,
            46, 47, 48, 49, 50, 51, 52
    };

    private static final int[] GRAY_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final Inventory inventory;
    private final Map<Integer, OfflinePlayer> friends = new HashMap<>();
    private final int page;
    private final int pages;

    public FriendsInventory(Player player, int page) {
        this.inventory = Bukkit.createInventory(this, SIZE, TITLE);

        fill(BUTTON_SLOTS, placeholder(Material.STONE_BUTTON));
        fill(BLACK_SLOTS, placeholder(Material.BLACK_STAINED_GLASS_PANE));
        fill(GRAY_SLOTS, placeholder(Material.GRAY_STAINED_GLASS_PANE));

        List<OfflinePlayer> friendList = getFriends(player);

        this.pages = Math.max(1, (int) Math.ceil(friendList.size() / (double) FRIEND_SLOTS.length));
        this.page = Math.max(0, Math.min(page, pages - 1));

        inventory.setItem(SLOT_PROFILE, head(player, "§6Deine Freunde",
                "§7Freunde: §e" + friendList.size(),
                "§7Online: §a" + friendList.stream().filter(OfflinePlayer::isOnline).count()));

        if (friendList.isEmpty()) {
            inventory.setItem(SLOT_EMPTY, new ItemBuilder(Material.SKELETON_SKULL)
                    .displayname("§cDu hast noch keine Freunde")
                    .lore("§7Füge Spieler hinzu, um sie", "§7hier zu sehen.")
                    .build());
        }

        int start = this.page * FRIEND_SLOTS.length;

        for (int i = 0; i < FRIEND_SLOTS.length && start + i < friendList.size(); i++) {
            OfflinePlayer friend = friendList.get(start + i);

            friends.put(FRIEND_SLOTS[i], friend);
            inventory.setItem(FRIEND_SLOTS[i], head(friend, "§6" + friend.getName(),
                    "§7Status: " + (friend.isOnline() ? "§aOnline" : "§cOffline"),
                    "",
                    "§eKlicke für Optionen"));
        }

        inventory.setItem(SLOT_ADD, new ItemBuilder(Material.LIME_DYE)
                .displayname("§aFreund hinzufügen")
                .lore("§7Sende einem Spieler", "§7eine Freundschaftsanfrage.")
                .build());
        inventory.setItem(SLOT_REQUESTS, new ItemBuilder(Material.PAPER)
                .displayname("§eFreundschaftsanfragen")
                .lore("§7Offene Anfragen: §e0")
                .build());
        inventory.setItem(SLOT_SETTINGS, new ItemBuilder(Material.COMPARATOR)
                .displayname("§7Einstellungen")
                .lore("§7Lege fest, wer dir Anfragen", "§7schicken darf.")
                .build());

        if (hasPreviousPage()) {
            inventory.setItem(SLOT_PREVIOUS, new ItemBuilder(Material.ARROW).displayname("§7Vorherige Seite").build());
        }

        if (hasNextPage()) {
            inventory.setItem(SLOT_NEXT, new ItemBuilder(Material.ARROW).displayname("§7Nächste Seite").build());
        }
    }

    public static void open(Player player) {
        open(player, 0);
    }

    public static void open(Player player, int page) {
        player.openInventory(new FriendsInventory(player, page).getInventory());
    }

    public OfflinePlayer getFriend(int slot) {
        return friends.get(slot);
    }

    public int getPage() {
        return page;
    }

    public boolean hasPreviousPage() {
        return page > 0;
    }

    public boolean hasNextPage() {
        return page < pages - 1;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    private List<OfflinePlayer> getFriends(Player player) {
        // Wird befüllt, sobald es das FreundeSystem gibt
        return List.of();
    }

    private ItemStack head(OfflinePlayer owner, String name, String... lore) {
        ItemStack item = new ItemBuilder(Material.PLAYER_HEAD).displayname(name).lore(lore).build();
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.setOwningPlayer(owner);
        item.setItemMeta(meta);

        return item;
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
