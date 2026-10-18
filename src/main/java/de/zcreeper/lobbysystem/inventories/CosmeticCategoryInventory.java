package de.zcreeper.lobbysystem.inventories;

import de.zcreeper.lobbysystem.cosmetics.Cosmetic;
import de.zcreeper.lobbysystem.cosmetics.CosmeticCategory;
import de.zcreeper.lobbysystem.cosmetics.CosmeticManager;
import de.zcreeper.lobbysystem.cosmetics.CosmeticShop;
import de.zcreeper.lobbysystem.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CosmeticCategoryInventory implements InventoryHolder {

    public static final int SIZE = 54;

    public static final int SLOT_COINS = 4;
    public static final int SLOT_BACK = 45;
    public static final int SLOT_REMOVE = 49;

    private static final int[] COSMETIC_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final CosmeticCategory category;
    private final Inventory inventory;
    private final Map<Integer, Cosmetic> cosmetics = new HashMap<>();

    public CosmeticCategoryInventory(Player player, CosmeticCategory category) {
        this.category = category;
        this.inventory = Bukkit.createInventory(this, SIZE, "§6Cosmetics §8» " + category.getDisplayName());

        render(player);
    }

    public static void open(Player player, CosmeticCategory category) {
        player.openInventory(new CosmeticCategoryInventory(player, category).getInventory());
    }

    public void render(Player player) {
        inventory.clear();
        cosmetics.clear();

        ItemStack black = placeholder(Material.BLACK_STAINED_GLASS_PANE);

        for (int slot = 0; slot < SIZE; slot++) {
            if (slot < 9 || slot >= 45 || slot % 9 == 0 || slot % 9 == 8) inventory.setItem(slot, black.clone());
        }

        List<? extends Cosmetic> list = category.getCosmetics();

        for (int i = 0; i < list.size() && i < COSMETIC_SLOTS.length; i++) {
            Cosmetic cosmetic = list.get(i);

            cosmetics.put(COSMETIC_SLOTS[i], cosmetic);
            inventory.setItem(COSMETIC_SLOTS[i], cosmeticItem(player, cosmetic));
        }

        inventory.setItem(SLOT_COINS, coinsItem(player));
        inventory.setItem(SLOT_BACK, new ItemBuilder(Material.ARROW).displayname("§7Zurück").build());
        inventory.setItem(SLOT_REMOVE, new ItemBuilder(Material.BARRIER)
                .displayname("§cAblegen")
                .lore("§7Legt dein Cosmetic aus", "§7dieser Kategorie ab.")
                .build());
    }

    public Cosmetic getCosmetic(int slot) {
        return cosmetics.get(slot);
    }

    public CosmeticCategory getCategory() {
        return category;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    private ItemStack cosmeticItem(Player player, Cosmetic cosmetic) {
        boolean unlocked = cosmetic.isUnlocked(player);
        boolean equipped = CosmeticManager.isEquipped(player, cosmetic);

        ItemBuilder builder = new ItemBuilder(cosmetic.getIcon())
                .displayname(cosmetic.getDisplayName())
                .lore("§7" + cosmetic.getDescription(), "")
                .flag(ItemFlag.HIDE_ATTRIBUTES);

        if (equipped) {
            builder.lore("§a✔ Ausgerüstet", "§8» §7Klicke zum Ablegen");
        } else if (unlocked) {
            builder.lore("§a✔ Freigeschaltet", "§8» §eKlicke zum Ausrüsten");
        } else if (CosmeticShop.canAfford(player, cosmetic)) {
            builder.lore("§7Preis: §e" + CosmeticShop.format(cosmetic.getPrice()) + " Coins", "§8» §eKlicke zum Kaufen");
        } else {
            long missing = cosmetic.getPrice() - CosmeticShop.getCoins(player);
            builder.lore("§7Preis: §e" + CosmeticShop.format(cosmetic.getPrice()) + " Coins", "§8» §cDir fehlen " + CosmeticShop.format(missing) + " Coins");
        }

        ItemStack item = builder.build();

        if (equipped) {
            ItemMeta meta = item.getItemMeta();
            meta.setEnchantmentGlintOverride(true);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack coinsItem(Player player) {
        return new ItemBuilder(Material.GOLD_NUGGET)
                .displayname("§6Deine Coins")
                .lore("", "§8» §e" + CosmeticShop.format(CosmeticShop.getCoins(player)) + " Coins")
                .build();
    }

    private ItemStack placeholder(Material material) {
        return new ItemBuilder(material).displayname("§8").build();
    }

}
