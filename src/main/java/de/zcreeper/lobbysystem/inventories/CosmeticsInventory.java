package de.zcreeper.lobbysystem.inventories;

import de.zcreeper.lobbysystem.cosmetics.Cosmetic;
import de.zcreeper.lobbysystem.cosmetics.CosmeticCategory;
import de.zcreeper.lobbysystem.cosmetics.CosmeticManager;
import de.zcreeper.lobbysystem.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CosmeticsInventory implements InventoryHolder {

    public static final String TITLE = "§6Cosmetics";

    public static final int SIZE = 45;

    public static final int SLOT_REMOVE_ALL = 31;

    private static final int[] CATEGORY_SLOTS = {19, 21, 23, 25};

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
            20, 22, 24,
            28, 29, 30, 32, 33, 34
    };

    private final Inventory inventory;

    public CosmeticsInventory(Player player) {
        this.inventory = Bukkit.createInventory(this, SIZE, TITLE);

        fill(BUTTON_SLOTS, placeholder(Material.STONE_BUTTON));
        fill(BLACK_SLOTS, placeholder(Material.BLACK_STAINED_GLASS_PANE));
        fill(GRAY_SLOTS, placeholder(Material.GRAY_STAINED_GLASS_PANE));

        CosmeticCategory[] categories = CosmeticCategory.values();

        for (int i = 0; i < categories.length && i < CATEGORY_SLOTS.length; i++) {
            inventory.setItem(CATEGORY_SLOTS[i], categoryItem(player, categories[i]));
        }

        inventory.setItem(SLOT_REMOVE_ALL, new ItemBuilder(Material.BARRIER)
                .displayname("§cAlle Cosmetics ablegen")
                .lore("§7Legt alle ausgerüsteten", "§7Cosmetics auf einmal ab.")
                .build());
    }

    public static void open(Player player) {
        player.openInventory(new CosmeticsInventory(player).getInventory());
    }

    public static CosmeticCategory getCategory(int slot) {
        CosmeticCategory[] categories = CosmeticCategory.values();

        for (int i = 0; i < categories.length && i < CATEGORY_SLOTS.length; i++) {
            if (CATEGORY_SLOTS[i] == slot) return categories[i];
        }

        return null;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    private ItemStack categoryItem(Player player, CosmeticCategory category) {
        Cosmetic selected = CosmeticManager.getEquipped(player, category);
        long unlocked = category.getCosmetics().stream().filter(cosmetic -> cosmetic.isUnlocked(player)).count();

        return new ItemBuilder(category.getIcon())
                .displayname(category.getDisplayName())
                .lore(
                        "§7" + category.getDescription(),
                        "",
                        "§7Freigeschaltet: §e" + unlocked + "§8/§e" + category.getCosmetics().size(),
                        "§7Ausgewählt: " + (selected == null ? "§cNichts" : selected.getDisplayName()),
                        "",
                        "§eKlicke zum Öffnen"
                )
                .flag(ItemFlag.HIDE_ATTRIBUTES)
                .build();
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
