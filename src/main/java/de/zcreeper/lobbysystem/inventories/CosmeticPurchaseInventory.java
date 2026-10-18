package de.zcreeper.lobbysystem.inventories;

import de.zcreeper.lobbysystem.cosmetics.Cosmetic;
import de.zcreeper.lobbysystem.cosmetics.CosmeticShop;
import de.zcreeper.lobbysystem.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CosmeticPurchaseInventory implements InventoryHolder {

    public static final String TITLE = "§6Cosmetic kaufen";

    public static final int SIZE = 45;

    public static final int SLOT_CONFIRM = 20;
    public static final int SLOT_PREVIEW = 22;
    public static final int SLOT_CANCEL = 24;

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

    private final Cosmetic cosmetic;
    private final Inventory inventory;

    public CosmeticPurchaseInventory(Player player, Cosmetic cosmetic) {
        this.cosmetic = cosmetic;
        this.inventory = Bukkit.createInventory(this, SIZE, TITLE);

        fill(BUTTON_SLOTS, placeholder(Material.STONE_BUTTON));
        fill(BLACK_SLOTS, placeholder(Material.BLACK_STAINED_GLASS_PANE));
        fill(GRAY_SLOTS, placeholder(Material.GRAY_STAINED_GLASS_PANE));

        long price = cosmetic.getPrice();
        long coins = CosmeticShop.getCoins(player);

        inventory.setItem(SLOT_PREVIEW, new ItemBuilder(cosmetic.getIcon())
                .displayname(cosmetic.getDisplayName())
                .lore(
                        "§7" + cosmetic.getDescription(),
                        "",
                        "§7Kategorie: " + cosmetic.getCategory().getDisplayName(),
                        "§7Preis: §e" + CosmeticShop.format(price) + " Coins"
                )
                .flag(ItemFlag.HIDE_ATTRIBUTES)
                .build());

        inventory.setItem(SLOT_CONFIRM, new ItemBuilder(Material.LIME_DYE)
                .displayname("§aKaufen")
                .lore(
                        "",
                        "§7Preis: §e" + CosmeticShop.format(price) + " Coins",
                        "§7Deine Coins: §e" + CosmeticShop.format(coins),
                        "§7Danach: §e" + CosmeticShop.format(Math.max(0, coins - price)) + " Coins",
                        "",
                        coins >= price ? "§8» §aKlicke zum Kaufen" : "§8» §cDir fehlen " + CosmeticShop.format(price - coins) + " Coins"
                )
                .build());

        inventory.setItem(SLOT_CANCEL, new ItemBuilder(Material.RED_DYE)
                .displayname("§cAbbrechen")
                .lore("", "§8» §7Zurück zur Übersicht")
                .build());
    }

    public static void open(Player player, Cosmetic cosmetic) {
        player.openInventory(new CosmeticPurchaseInventory(player, cosmetic).getInventory());
    }

    public Cosmetic getCosmetic() {
        return cosmetic;
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
