package de.zcreeper.lobbysystem.items;

import de.zcreeper.lobbysystem.LobbySystem;
import de.zcreeper.lobbysystem.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class HotbarItems {

    private static final String TELEPORTER_ID = "teleporter";
    private static final String VISIBILITY_ID = "visibility";
    private static final String COSMETIC_ID = "cosmetic";

    public static void getItems(Player player) {
        player.getInventory().setItem(4, getTeleporter());
        player.getInventory().setItem(7, getCosmetics());
        player.getInventory().setItem(1, getVisibilityItem(false));
    }

    public static ItemStack getTeleporter() {
        return mark(new ItemBuilder(Material.NETHER_STAR).displayname("§6Teleporter").build(), TELEPORTER_ID);
    }

    public static ItemStack getCosmetics() {
        return mark(new ItemBuilder(Material.TOTEM_OF_UNDYING).displayname("§6Cosmetics").build(), COSMETIC_ID);
    }

    public static ItemStack getVisibilityItem(boolean hiding) {
        ItemStack item = hiding
                ? new ItemBuilder(Material.GRAY_DYE).displayname("§cSpieler versteckt").build()
                : new ItemBuilder(Material.ENDER_EYE).displayname("§aSpieler sichtbar").build();

        return mark(item, VISIBILITY_ID);
    }

    public static boolean isTeleporter(ItemStack item) {
        return hasId(item, TELEPORTER_ID);
    }

    public static boolean isVisibilityItem(ItemStack item) {
        return hasId(item, VISIBILITY_ID);
    }

    private static ItemStack mark(ItemStack item, String id) {
        ItemMeta meta = item.getItemMeta();

        meta.getPersistentDataContainer().set(getKey(id), PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);

        return item;
    }

    private static boolean hasId(ItemStack item, String id) {
        if (item == null || !item.hasItemMeta()) return false;

        return item.getItemMeta().getPersistentDataContainer().has(getKey(id), PersistentDataType.BYTE);
    }

    private static NamespacedKey getKey(String id) {
        return new NamespacedKey(LobbySystem.getInstance(), id);
    }

}
