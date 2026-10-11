package de.zcreeper.lobbysystem.inventories;

import de.zcreeper.lobbysystem.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TeleporterInventory implements InventoryHolder {

    public static final String TITLE = "§6Teleporter";

    public static final int SIZE = 45;

    public static final int SLOT_GEMRUSH = 20;
    public static final int SLOT_SPAWN = 22;
    public static final int SLOT_LAVABATTLE = 24;
    public static final int SLOT_COOKIECLICKER = 31;

    private static final int[] BUTTON_SLOTS = {0, 8, 36, 44};

    private static final int[] BLACK_SLOTS = {
            1, 2, 3, 4, 5, 6, 7,
            9, 17,
            18, 26,
            27, 35,
            37, 38, 39, 40, 41, 42, 43
    };

    private static final int[] GRAY_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 21, 23, 25,
            28, 29, 30, 32, 33, 34
    };

    private final Inventory inventory;

    public TeleporterInventory() {
        this.inventory = Bukkit.createInventory(this, SIZE, TITLE);

        fill(BUTTON_SLOTS, placeholder(Material.STONE_BUTTON));
        fill(BLACK_SLOTS, placeholder(Material.BLACK_STAINED_GLASS_PANE));
        fill(GRAY_SLOTS, placeholder(Material.GRAY_STAINED_GLASS_PANE));

        inventory.setItem(SLOT_GEMRUSH, new ItemBuilder(Material.EMERALD)
                .displayname("§aGemRush")
                .lore(
                        "",
                        "§7Kämpfe im §e3er-Team §7um die Gems.",
                        "§7Sammelt als Erstes §a10 Gems§7,",
                        "§7um die Runde zu gewinnen!",
                        "",
                        "§8» §eLinksklick §7zum Verbinden"
                )
                .build());
        inventory.setItem(SLOT_SPAWN, new ItemBuilder(Material.NETHER_STAR)
                .displayname("§6Spawn")
                .lore("", "§8» §eKlicke §7zum Teleportieren")
                .build());
        inventory.setItem(SLOT_LAVABATTLE, new ItemBuilder(Material.LAVA_BUCKET)
                .displayname("§dLavaBattle")
                .lore(
                        "",
                        "§7Meistere den §6Parkour §7und sammle",
                        "§7unterwegs §bItems§7.",
                        "§7Rüste dich damit aus und besiege",
                        "§7die anderen im §cPvP§7!",
                        "",
                        "§8» §eLinksklick §7zum Verbinden"
                )
                .build());
        inventory.setItem(SLOT_COOKIECLICKER, new ItemBuilder(Material.COOKIE)
                .displayname("§6CookieClicker")
                .lore(
                        "",
                        "§7Klicke auf den Cookie und sammle",
                        "§7so viele §6Cookies §7wie möglich.",
                        "§7Später kannst du sie in §bCoins",
                        "§7auszahlen lassen.",
                        "",
                        "§8» §eKlicke §7zum Teleportieren"
                )
                .build());
    }

    public static void open(Player player) {
        player.openInventory(new TeleporterInventory().getInventory());
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
