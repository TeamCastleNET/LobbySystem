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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FriendsInventory implements InventoryHolder {

    public static final String TITLE = "§6Freunde";

    public static final int SIZE = 54;

    public static final int SLOT_PROFILE = 4;
    public static final int SLOT_ADD = 38;
    public static final int SLOT_REQUESTS = 40;
    public static final int SLOT_OUTGOING = 42;
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
    private final Map<Integer, Friend> friends = new HashMap<>();
    private final int page;
    private final int pages;

    public FriendsInventory(Player player, FriendService.Overview overview, int page) {
        this.inventory = Bukkit.createInventory(this, SIZE, TITLE);

        fill(BUTTON_SLOTS, placeholder(Material.STONE_BUTTON));
        fill(BLACK_SLOTS, placeholder(Material.BLACK_STAINED_GLASS_PANE));
        fill(GRAY_SLOTS, placeholder(Material.GRAY_STAINED_GLASS_PANE));

        List<Friend> friendList = overview.friends();
        int incoming = overview.incoming().size();
        int outgoing = overview.outgoing().size();

        this.pages = Math.max(1, (int) Math.ceil(friendList.size() / (double) FRIEND_SLOTS.length));
        this.page = Math.max(0, Math.min(page, pages - 1));

        long inLobby = friendList.stream().filter(friend -> FriendService.isInLobby(friend.uuid())).count();

        inventory.setItem(SLOT_PROFILE, head(player.getUniqueId(), "§6Deine Freunde",
                "§7Freunde: §e" + friendList.size(),
                "§7In der Lobby: §a" + inLobby));

        if (friendList.isEmpty()) {
            inventory.setItem(SLOT_EMPTY, new ItemBuilder(Material.SKELETON_SKULL)
                    .displayname("§cDu hast noch keine Freunde")
                    .lore("§7Füge Spieler hinzu, um sie", "§7hier zu sehen.")
                    .build());
        }

        int start = this.page * FRIEND_SLOTS.length;

        for (int i = 0; i < FRIEND_SLOTS.length && start + i < friendList.size(); i++) {
            Friend friend = friendList.get(start + i);
            boolean online = FriendService.isInLobby(friend.uuid());

            friends.put(FRIEND_SLOTS[i], friend);
            inventory.setItem(FRIEND_SLOTS[i], head(friend.uuid(),
                    (online ? "§a" : "§7") + FriendService.getName(friend.uuid()),
                    online ? "§a● In der Lobby" : "§7● Nicht in der Lobby",
                    "§7Freunde seit: §e" + FriendService.formatDate(friend.since()),
                    "",
                    "§8» §eKlicke für Optionen"));
        }

        inventory.setItem(SLOT_ADD, new ItemBuilder(Material.LIME_DYE)
                .displayname("§aFreund hinzufügen")
                .lore("§7Sende einem Spieler", "§7eine Freundschaftsanfrage.", "", "§8» §eKlicke §7und gib den Namen ein")
                .build());

        ItemStack requests = new ItemBuilder(Material.PAPER)
                .displayname("§eFreundschaftsanfragen")
                .lore("§7Offene Anfragen: §e" + incoming, "", "§8» §eKlicke zum Öffnen")
                .amount(Math.max(1, Math.min(incoming, 64)))
                .build();

        if (incoming > 0) glint(requests);

        inventory.setItem(SLOT_REQUESTS, requests);
        inventory.setItem(SLOT_OUTGOING, new ItemBuilder(Material.WRITABLE_BOOK)
                .displayname("§7Gesendete Anfragen")
                .lore("§7Ausstehend: §e" + outgoing, "", "§8» §eKlicke zum Öffnen")
                .build());

        if (hasPreviousPage()) {
            inventory.setItem(SLOT_PREVIOUS, new ItemBuilder(Material.ARROW).displayname("§7Vorherige Seite").build());
        }

        if (hasNextPage()) {
            inventory.setItem(SLOT_NEXT, new ItemBuilder(Material.ARROW).displayname("§7Nächste Seite").build());
        }
    }

    public static void open(Player player, FriendService.Overview overview, int page) {
        player.openInventory(new FriendsInventory(player, overview, page).getInventory());
    }

    public Friend getFriend(int slot) {
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

    static ItemStack head(UUID owner, String name, String... lore) {
        ItemStack item = new ItemBuilder(Material.PLAYER_HEAD).displayname(name).lore(lore).build();
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        meta.setPlayerProfile(FriendService.getProfile(owner));
        item.setItemMeta(meta);

        return item;
    }

    private static void glint(ItemStack item) {
        ItemMeta meta = item.getItemMeta();

        meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
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
