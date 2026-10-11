package de.zcreeper.lobbysystem.inventories;

import de.zcreeper.lobbysystem.cookieclicker.CookieClickerManager;
import de.zcreeper.lobbysystem.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CookieClickerInventory implements InventoryHolder {

    public static final String TITLE = "§6CookieClicker";

    public static final int SIZE = 45;

    public static final int SLOT_INFO = 20;
    public static final int SLOT_COOKIES = 22;
    public static final int SLOT_PAYOUT = 24;

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
            28, 29, 30, 31, 32, 33, 34
    };

    private final Inventory inventory;

    public CookieClickerInventory(Player player) {
        this.inventory = Bukkit.createInventory(this, SIZE, TITLE);

        fill(BUTTON_SLOTS, placeholder(Material.STONE_BUTTON));
        fill(BLACK_SLOTS, placeholder(Material.BLACK_STAINED_GLASS_PANE));
        fill(GRAY_SLOTS, placeholder(Material.GRAY_STAINED_GLASS_PANE));

        inventory.setItem(SLOT_INFO, new ItemBuilder(Material.BOOK)
                .displayname("§eSo funktioniert's")
                .lore(
                        "",
                        "§7Klicke mit §eLinksklick §7auf den",
                        "§7CookieClicker, um §6Cookies §7zu sammeln.",
                        "§7Deine Cookies kannst du später",
                        "§7in §bCoins §7auszahlen lassen."
                )
                .build());
        inventory.setItem(SLOT_COOKIES, new ItemBuilder(Material.COOKIE)
                .displayname("§6Deine Cookies")
                .lore("", "§8» §e" + CookieClickerManager.format(CookieClickerManager.getCookies(player)) + " §7Cookies")
                .build());
        inventory.setItem(SLOT_PAYOUT, new ItemBuilder(Material.GOLD_INGOT)
                .displayname("§bIn Coins auszahlen")
                .lore("", "§7Tausche deine Cookies", "§7gegen Coins ein.", "", "§8» §eKlicke §7zum Auszahlen")
                .build());
    }

    public static void open(Player player) {
        player.openInventory(new CookieClickerInventory(player).getInventory());
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
